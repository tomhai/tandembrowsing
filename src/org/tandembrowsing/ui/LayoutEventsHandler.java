package org.tandembrowsing.ui;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.EventQueue;
import org.tandembrowsing.io.Operation;
import org.tandembrowsing.io.soap.CallbackClient;
import org.tandembrowsing.state.StateMachine;

public class LayoutEventsHandler {
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	
	public synchronized void initSession(String uuid_key, String browser, String requestIP, String method, int fullWidth, int fullHeight, String smSession, String statemachine)
	{
		LayoutManager display = LayoutManager.getInstance();
		display.lock.lock();
		try {
			display.initSession(uuid_key, browser, requestIP, method, fullWidth, fullHeight, smSession);
			// if we have a statemachine request, we need to start one
			if(statemachine != null && statemachine.length() > 0 && !statemachine.equals("null")) {
				Event event = new Event(Event.EVENT_CONFIGURE, "", smSession, "");
				Operation op = event.addOperation(Event.SET_STATEMACHINE);
				op.addParameter(StateMachine.STATEMACHINE_URL, statemachine);
				EventQueue.getInstance().add(event);	
			} 
		} catch(LayoutException e) {
			logger.severe(e.getMessage());
		} finally {
			display.lock.unlock();
		}			
	}

	public synchronized void interactionDetected() {
		
		try {			
	    	ResourceBundle properties = ResourceBundle.getBundle("org.tandembrowsing.display");
	    	String address = properties.getString("Callback_address");
			CallbackClient.displayEvent("interaction", address);		
		} catch (Exception e) {
			logger.severe(e.getMessage());
		} 
	}
	
	public synchronized void logout() {
		try {			
			ResourceBundle properties = ResourceBundle.getBundle("org.tandembrowsing.display");
	    	String address = properties.getString("Callback_address");
			CallbackClient.displayEvent("logout", address);		
		} catch (Exception e) {
			logger.severe(e.getMessage());
		} 
	}
	
	public synchronized void callback(String correlationId) {
		LayoutManagerView.getInstance().removeCallback(correlationId);
	}
	
	public synchronized void isAlive(String uuid_key, String browser) {		
		LayoutManagerView.getInstance().keepAlive(uuid_key, browser);			
	}
	
	public synchronized void playbackError(String message) {
		logger.severe(message);
	}


}
