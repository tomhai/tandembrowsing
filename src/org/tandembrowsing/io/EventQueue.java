package org.tandembrowsing.io;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUnderflowException;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import org.tandembrowsing.state.StateMachine;


/**
 * EventQueue synchronizes interaction events i.e. EventQueue enforces events to
 * go trough a FIFO pipe and polls them one by one for processing.  
 * 
 * @author tjh
 *
 */

public class EventQueue  {
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	private Buffer eventQueue;
	private static EventQueue singletonObject;
	
	public void add(Event event) {
		eventQueue.add(event);
	}
	
	private Event poll() {
		Event e = null;
		try {
			e = (Event)eventQueue.remove();
		} catch (BufferUnderflowException ex) {
			e = null;
		}
		return e;
	}
	
	public static EventQueue getInstance() {
		if (singletonObject == null) {
			singletonObject = new EventQueue();
			QueuePoller executor = singletonObject.new QueuePoller();
			executor.start();
		}
		return singletonObject;		
	}
	
	private EventQueue() {
		eventQueue =  BufferUtils.synchronizedBuffer(new CircularFifoBuffer(20));
	}

	private class QueuePoller extends Thread {
	    public void run() {
	        while(true) {
	        	try {
	            	Event event = poll();
	            	if(event != null) {
	            		StateMachine.getInstance().processEvent(event);
	            	}
		            try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
	        	} catch (Exception e) {
	        		logger.log(Level.SEVERE, "Failure in statemachine", e);
	        		//StateMachine.getInstance().resetStatemachine();
	        	}
		    }        	
	    }	
	}
}
