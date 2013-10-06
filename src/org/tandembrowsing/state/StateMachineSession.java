package org.tandembrowsing.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.Data;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.w3c.dom.Node;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.Operation;
import org.tandembrowsing.io.db.DBUtil;
import org.tandembrowsing.model.VirtualScreen;
import org.tandembrowsing.model.MultipartPage;
import org.tandembrowsing.model.ParsingException;
import org.tandembrowsing.ui.LayoutManager;

public class StateMachineSession implements SCXMLListener, ErrorReporter {
	private String smSession;
	private String stateMachine;
	private SCXMLExecutor executor;
	private String recoveryState;

	private static LayoutManager layoutManager;
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	private Map <String, List<Operation>> overrideOperations = new HashMap<String, List<Operation>>();
	
	public StateMachineSession (String smSession, String stateMachine) {
		layoutManager = LayoutManager.getInstance();
		layoutManager.addLayoutSession(smSession);
		this.setSmSession(smSession);	
		this.setStateMachine(stateMachine);
	}
	
	public StateMachineSession (String smSession, String stateMachine, String recoveryState) {
		this(smSession, stateMachine);
		this.setRecoveryState(recoveryState);
	}
			
	
	public void overrideOperations(String eventName, List<Operation> operations) {
		overrideOperations.put(eventName, operations);
	}
	
	private void removeOverride(TransitionTarget state) {
		if(overrideOperations.size() == 0 || state == null)
			return;
		else if(overrideOperations.remove(state.getId()) == null)
			removeOverride(state.getParent());
	}

	private boolean hasOverrideOperation(List<Operation> overrideList, VirtualScreen key) {
		if(overrideList == null)
			return false;
		else {
			Iterator <Operation>it = overrideList.iterator();
			while(it.hasNext()) {
				Operation op = it.next();
				// matching id found
				if(op.getParameterValue(VirtualScreen.ID).equals(key.getId())) {
					return true;
				}					
			}
			// no match found
			return false;
		}
	}
	
	/**
	 * Finds the correct List of override operations
	 * 
	 * @param current state
	 * @return List of override operations
	 */
	private List<Operation> getOverrideOperation(TransitionTarget state) {
		if(overrideOperations.size() == 0 || state == null)
			return null;		
		else if(overrideOperations.containsKey(state.getId()))
			return overrideOperations.get(state.getId()); // don't remove yet
		else
			return getOverrideOperation(state.getParent());
	}
	
	private Set <String> overrideVirtualScreens(List<Operation> overrideList, Map<String, VirtualScreen> newVirtualScreens, TransitionTarget state) {
		Set <String> contentChanged = new HashSet<String>();
		if(overrideList == null) {
			return contentChanged;
		} else {			
			Iterator <Operation>it = overrideList.iterator();
			while(it.hasNext()) {
				Operation op = it.next();
				String overriddenVirtualScreenId = op.getParameterValue(VirtualScreen.ID);
				// matching id found
				if(newVirtualScreens.containsKey(overriddenVirtualScreenId)) {
					// in case of setContent
					if(op.getName().equals(Event.SET_CONTENT)) {
						String overriddenResource = op.getParameterValue(VirtualScreen.RESOURCE);
						// check if this is just a parameter update
						newVirtualScreens.get(overriddenVirtualScreenId).setResource(LayoutManager.parseResource(overriddenResource, newVirtualScreens.get(overriddenVirtualScreenId).getResource()));
						contentChanged.add(overriddenVirtualScreenId+Event.SET_CONTENT);	
						logger.fine("Override virtualscreen "+overriddenVirtualScreenId+" content with "+ overriddenResource);
					} else if(op.getName().equals(Event.RESIZE_AND_MOVE)) {
						VirtualScreen modifiedVirtualScreen = newVirtualScreens.get(overriddenVirtualScreenId);
						if(op.hasParameter(VirtualScreen.WIDTH))
							modifiedVirtualScreen.setWidth(Float.parseFloat(op.getParameterValue(VirtualScreen.WIDTH)));
						if(op.hasParameter(VirtualScreen.HEIGHT))
							modifiedVirtualScreen.setHeight(Float.parseFloat(op.getParameterValue(VirtualScreen.HEIGHT)));
						if(op.hasParameter(VirtualScreen.X_POSITION))
							modifiedVirtualScreen.setXPosition(Float.parseFloat(op.getParameterValue(VirtualScreen.X_POSITION)));
						if(op.hasParameter(VirtualScreen.Y_POSITION))
							modifiedVirtualScreen.setYPosition(Float.parseFloat(op.getParameterValue(VirtualScreen.Y_POSITION)));
						if(op.hasParameter(VirtualScreen.Z_INDEX))
							modifiedVirtualScreen.setZIndex(Integer.parseInt(op.getParameterValue(VirtualScreen.Z_INDEX)));
						
						logger.fine("Override virtualscreen "+overriddenVirtualScreenId+" dimensions");
					} else if(op.getName().equals(Event.MUTE) || op.getName().equals(Event.UNMUTE)) {
						VirtualScreen modifiedVirtualScreen = newVirtualScreens.get(overriddenVirtualScreenId);
						// check if this is just a parameter update
						contentChanged.add(overriddenVirtualScreenId+op.getName());
						logger.fine("Override virtualscreen "+overriddenVirtualScreenId+" with "+ op.getName());
					}
				}					
			}
			// now we can remove it
			removeOverride(state);	
			return contentChanged;
		}
		
	}
	
	@Override
	public void onEntry(TransitionTarget state) {
		// TODO Auto-generated method stub
		logger.info("/"+ state.getId());
		// check if this is the init state due to a recovery. 
		// if yes, then don't create the UI, as it is recovered separately
		if(recoveryState != null) {
			recoveryState = null;
		} else {
			DBUtil.setState(smSession, state.getId());
			try {
				List <Operation> overrideList = getOverrideOperation(state);
				if( state.getDatamodel() != null) {
					List <Data> list = state.getDatamodel().getData();
					if (list.size() != 1) {
						logger.info("Unexpected number of Data elements size "+list.size());
					} else {
						Data data = (Data)list.get(0);
						Node multipartpage = data.getNode().getFirstChild();
						// parse the target state virtual multipartpage to newMultipartpage
						Map <String, VirtualScreen> newMultipartpage = MultipartPage.parseMultipartPage(multipartpage);
						// override newVirtualScreens with possible overrides 
						Set <String> operationTypes = overrideVirtualScreens(overrideList, newMultipartpage, state);
						
						layoutManager.changeLayout(smSession, newMultipartpage, operationTypes);
						
					}
				}
			} catch (ParsingException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
		}
	}
	@Override
	public void onExit(TransitionTarget state) {
		logger.fine("Exit: "+ state.getId());		
	}
	@Override
	public void onTransition(TransitionTarget from, TransitionTarget to, Transition transition) {
		if(overrideOperations.containsKey(transition.getEvent()))
			overrideOperations.put(to.getId(), overrideOperations.remove(transition.getEvent()));
		logger.fine(from.getId()+ " -> "+to.getId());		
	}

	@Override
	public void onError(String errorCode, String errorDetail, Object errCtx) {
		logger.log(Level.SEVERE, errorCode+" : "+errorDetail, errCtx);
	}
	
	public String getSmSession() {
		return smSession;
	}

	public void setSmSession(String smSession) {
		this.smSession = smSession;
	}

	public String getStateMachine() {
		return stateMachine;
	}

	public void setStateMachine(String stateMachine) {
		this.stateMachine = stateMachine;
		layoutManager.setStateMachine(smSession, stateMachine);
	}

	public SCXMLExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(SCXMLExecutor executor) {
		this.executor = executor;
	}	
	
	public String getRecoveryState() {
		return recoveryState;
	}

	public void setRecoveryState(String recoveryState) {
		this.recoveryState = recoveryState;
	}
}
