package org.tandembrowsing.state;

import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.commons.scxml.model.Parallel;
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
	private String previousStateMachine = null;
	private SCXMLExecutor executor;
	private String recoveryState = null;
	private boolean persistent = false;

	private static LayoutManager layoutManager;
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	private Map <String, List<Operation>> overrideOperations = new HashMap<String, List<Operation>>();
	// smsparallel is a history Map to keep track which parallel states are active and their branches 
	private Map <String, String> branches = new HashMap<String, String>();
	
	public StateMachineSession (String smSession, String stateMachine, boolean persistent) {
		layoutManager = LayoutManager.getInstance();
		layoutManager.addLayoutSession(smSession);
		this.setSmSession(smSession);	
		this.setStateMachine(stateMachine);
		this.setPersistent(persistent);
	}
	
	public StateMachineSession (String smSession, String stateMachine, boolean persistent, String recoveryState) {
		this(smSession, stateMachine, persistent);
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
	
	private String recursiveFindBranch(TransitionTarget state) {
		if(state.getParent() != null) {
			if(branches.containsKey(state.getParent().getId())) {
				return state.getParent().getId();
			} else {
				return recursiveFindBranch(state.getParent());
			}
		} else 
			return null;
	}
	
	@Override
	public void onEntry(TransitionTarget state) {
		logger.info("/"+ state.getId());	

		if(state instanceof Parallel) {			
			Set <TransitionTarget> children = ((Parallel) state).getChildren();
			Iterator <TransitionTarget> chi = children.iterator();
			int i = 0;
			while(chi.hasNext()) {
				i++;
				String temp = chi.next().getId();
				branches.put(temp, state.getId());
				if(i == children.size())
					layoutManager.getMultipartPage(smSession).setCurrentVirtualScreensForRemoval(temp, state.getId()); 
			}
		} else {
			String branch = null;
			String parallel = null;
			if(branches.size() > 0) {
				branch = recursiveFindBranch(state);
				parallel = branches.get(branch);
			}
			// if yes, then don't create the UI, as it is recovered separately			
			if(recoveryState != null) {
				recoveryState = null;
			} else {
				DBUtil.setState(this.smSession, state.getId());
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
							
							layoutManager.changeLayout(smSession, newMultipartpage, operationTypes, branch, parallel);
							
						}
					}
				} catch (ParsingException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				}
			}
		}
	}
	@Override
	public void onExit(TransitionTarget state) {
		logger.fine("Exit: "+ state.getId());	
		if(state instanceof Parallel) {		
			while(branches.values().remove(state.getId()));
		}
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
		this.setPreviousStateMachine(this.stateMachine);
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
	
	public boolean hasRecoveryState() {
		return recoveryState != null;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public String getPreviousStateMachine() {
		return previousStateMachine;
	}

	public void setPreviousStateMachine(String previousStateMachine) {
		this.previousStateMachine = previousStateMachine;
	}

	public boolean hasPreviousStatemachine() {
		return this.previousStateMachine != null;
	}
}
