package org.tandembrowsing.state;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.SimpleErrorHandler;
import org.apache.commons.scxml.env.jsp.ELContext;
import org.apache.commons.scxml.env.jsp.ELEvaluator;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
import org.xml.sax.SAXException;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.Operation;
import org.tandembrowsing.io.ajax.Control;
import org.tandembrowsing.io.db.DBUtil;
import org.tandembrowsing.io.soap.CallbackClient;
import org.tandembrowsing.ui.LayoutException;
import org.tandembrowsing.ui.LayoutManager;

/**
 * StateMachine manages the display state based on the interaction events it gets.
 * The StateMachine is responsible of the valid state transitions and that the 
 * display is always in a valid state.
 * 
 * @author tjh
 *
 */

public class StateMachine {
	
	private static StateMachine singletonObject;
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	
	// ERROR Strings
	private static final String STATE_OK = "OK";
	private static final String STATE_SAME = "ERROR:same state";
	private static final String STATE_NOT_EXPECTED = "ERROR:state not expected";
	private static final String UNKNOWN_EVENT_TYPE = "ERROR:event type unknown";
	
	public static final String STATEMACHINE_URL = "resource";
	
	private static LayoutManager layoutManager;
	
	private Map <String, StateMachineSession> stateMachineSessions = new HashMap <String, StateMachineSession>();
	private Control control = null;
	private Map <String, StateMachineSession> recoverySessions = new HashMap<String, StateMachineSession>();	
	
	private StateMachine() {
		layoutManager = LayoutManager.getInstance();
		control = new Control();
		DBUtil.getStateMachines(recoverySessions);
		logger.info("Recoverable sessions in db " +recoverySessions.size());
		new RecoveryCleaner(300);

	}
	
	private void start(String smSession, String scxmlURL) throws StateMachineException {
        try {
			logger.info("parse " +scxmlURL);
			SCXML scxml = SCXMLParser.parse(new URL(scxmlURL), new SimpleErrorHandler());     

			StateMachineSession session = null;
			if(stateMachineSessions.containsKey(smSession)) {
				session = stateMachineSessions.get(smSession);
				session.setStateMachine(scxmlURL);
			} else
				session = new StateMachineSession(smSession, scxmlURL);
			
			Evaluator engine = new ELEvaluator();
	        EventDispatcher ed = new SCXMLEventDispatcher();
	        SCXMLExecutor newexec = new SCXMLExecutor(engine, ed, session);
	        newexec.setStateMachine(scxml);
	        newexec.addListener(scxml, session);
	        newexec.setRootContext(new ELContext());            
			newexec.go();
			session.setExecutor(newexec);
			//this will kill the previous state machine if exist for the same smSession
			stateMachineSessions.put(smSession, session);
			logger.info("Statemachines running " +stateMachineSessions.size());
		    DBUtil.setStateMachine(smSession, scxmlURL);
        } catch (ModelException e) {
			throw new StateMachineException(e);
		} catch (IOException e) {
			throw new StateMachineException(e);
		} catch (SAXException e) {
			throw new StateMachineException(e);
		} 
	}
			
	@SuppressWarnings("unchecked")
	private String getCurrentState(String smSession) {
		Set <State> currentStates = stateMachineSessions.get(smSession).getExecutor().getCurrentStatus().getStates();
		return ((State)currentStates.iterator().next()).getId();
	}
	
	private String getCurrentStateMachine(String smSession) {
		return stateMachineSessions.get(smSession).getStateMachine();
	}
	
	public void resetStatemachine(String smSession) {
		if(stateMachineSessions.containsKey(smSession))
			try {
				logger.log(Level.INFO, "Reset statemachine "+smSession);
				stateMachineSessions.get(smSession).getExecutor().reset();
			} catch (ModelException e) {
				logger.log(Level.SEVERE, "Reset failed for "+smSession, e);
			}
	}
	
	public static StateMachine getInstance() {
		if (singletonObject == null) {
			singletonObject = new StateMachine();
		}
		return singletonObject;
	}

	public synchronized void triggerEvent(String smSession, String event) {
		TriggerEvent te = new TriggerEvent(event, TriggerEvent.SIGNAL_EVENT);
		try {
			stateMachineSessions.get(smSession).getExecutor().triggerEvent(te);
		} catch (ModelException e) {
			logger.log(Level.SEVERE, "", e);
		} 
	}
	
	public synchronized void processEvent(Event event) {
		// should store the events for recovery
		try {
			if(event.getEventType().equalsIgnoreCase(Event.EVENT_CHANGE_STATE)) {
				// for every change state clear all and store the event
				if(event.hasOperations())
					stateMachineSessions.get(event.getEventSession()).overrideOperations(event.getEventName(), event.getOperations());
				triggerEvent(event.getEventSession(), event.getEventName());
				logger.log(Level.INFO, "Got "+event.getEventSession()+" "+event.getEventType()+ " to "+event.getEventName() + " via " +event.getEventInterface());
				logger.fine("Done "+event.getEventSession()+" "+event.getEventType()+" to "+event.getEventName());
			} else if(event.getEventType().equalsIgnoreCase(Event.EVENT_MODIFY_STATE)) {
				// store this as well as it could potentionally change staff
				logger.log(Level.INFO, "Got "+event.getEventSession()+" "+event.getEventType() + " via " +event.getEventInterface());
				layoutManager.processOperations(event.getEventSession(), event.getOperations());
				logger.fine("Done "+event.getEventSession()+" "+event.getEventType());
			} else if(event.getEventType().equalsIgnoreCase(Event.EVENT_MANAGE_SESSION)) {
				logger.info("Got "+event.getEventSession()+" "+event.getEventType() + " via " +event.getEventInterface());
				List <Operation> operations = event.getOperations();
				Iterator <Operation>it = operations.iterator();
				while(it.hasNext()) {
					Operation operation = it.next();
					if(operation.getName().equals(Event.SET_STATEMACHINE)) {
						//how to stop the previous cleanly?
						try {
							start(event.getEventSession(), operation.getParameterValue(STATEMACHINE_URL));
							DBUtil.setStateMachine(event.getEventSession(), operation.getParameterValue(STATEMACHINE_URL));
						} catch (StateMachineException e) {
							if(stateMachineSessions.containsKey(event.getEventSession()))
								logger.log(Level.SEVERE, "Bad state machine " + operation.getParameterValue(STATEMACHINE_URL) + ". Rolling back to previous "+ stateMachineSessions.get(event.getEventSession()).getStateMachine());
							else
								logger.log(Level.SEVERE, "Bad state machine " + operation.getParameterValue(STATEMACHINE_URL) + ". Need a valid state machine to run!");
						}						
					} else if(operation.getName().equals(Event.GET_STATEMACHINE)) {
						if(event.getEventInterface().equals("ajax"))
							control.sendEvent(getCurrentStateMachine(event.getEventSession()), event.getEventEndpoint(), event.getEventType());
						else
							CallbackClient.displayEvent(getCurrentStateMachine(event.getEventSession()), event.getEventEndpoint());
					} else if(operation.getName().equals(Event.SET_ATTRIBUTE)) {
						layoutManager.setAttribute(operation);					
					} else if(operation.getName().equals(Event.GET_STATE)) {
						if(event.getEventInterface().equals("ajax"))
							control.sendEvent(getCurrentState(event.getEventSession()), event.getEventEndpoint(), event.getEventType());
						else
							CallbackClient.displayEvent(getCurrentState(event.getEventSession()), event.getEventEndpoint());
					} else if(operation.getName().equals(Event.GET_VIRTUALSCREENS)) {
						if(event.getEventInterface().equals("ajax"))
							control.sendEvent(layoutManager.getCurrentVirtualScreens(event.getEventSession()), event.getEventEndpoint(), event.getEventType());
						else
							CallbackClient.displayEvent(layoutManager.getCurrentVirtualScreens(event.getEventSession()), event.getEventEndpoint());
					} else {
						logger.log(Level.SEVERE, "Unsupported configuration operation: "+operation.getName());
					}
				}
				logger.fine("Done "+event.getEventSession()+" "+event.getEventType());
			} else {
				logger.severe("Got unknown event type "+event.getEventType() + " via " +event.getEventInterface());
				CallbackClient.displayEvent(UNKNOWN_EVENT_TYPE+":"+event.getEventSession(), event.getEventEndpoint());
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (LayoutException e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public void clearSession(String smSession) {
		stateMachineSessions.remove(smSession);
		DBUtil.removeSession(smSession);
		logger.info("Statemachines running " +stateMachineSessions.size());
		layoutManager.clearSession(smSession);
	}

	public void recoverSession(String smSession) {
		if(recoverySessions.containsKey(smSession)) {
			// lets start from the previous step if possible	        
	        StateMachineSession recoverState = recoverySessions.remove(smSession);
			try {
		        SCXML scxml = SCXMLParser.parse(new URL(recoverState.getStateMachine()), new SimpleErrorHandler());
		        TransitionTarget target = SCXMLUtil.hasState(scxml, recoverState.getRecoveryState());
		        
		        if(target != null) {
		        	logger.info("Recover from " + recoverState.getRecoveryState());
		        	scxml.setInitialTarget(target);  
		        	
		        	Evaluator engine = new ELEvaluator();
			        EventDispatcher ed = new SCXMLEventDispatcher();
			        SCXMLExecutor newexec = new SCXMLExecutor(engine, ed, recoverState);
			        newexec.setStateMachine(scxml);
			        newexec.addListener(scxml, recoverState);
			        newexec.setRootContext(new ELContext());            
					newexec.go();
					recoverState.setExecutor(newexec);
					//this will kill the previous state machine if exist for the same smSession
					stateMachineSessions.put(recoverState.getSmSession(), recoverState);
					logger.info("Statemachines running " +stateMachineSessions.size());
		        }	         
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Recover failed for "+smSession);
			}
		}
	}
	
	public class RecoveryCleaner {
	    Timer timer;

	    public RecoveryCleaner(int seconds) {
	        timer = new Timer();
	        timer.schedule(new RecoveryCleanerTask(), seconds*1000);
		}

	    class RecoveryCleanerTask extends TimerTask {
	        public void run() {
	        	for (String smSession : recoverySessions.keySet()) {
	        	    DBUtil.removeSession(smSession);
	        	}
	        	recoverySessions.clear();
	            timer.cancel(); //Terminate the timer thread
	        }
	    }
	}
}

