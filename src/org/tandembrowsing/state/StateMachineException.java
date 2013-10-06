package org.tandembrowsing.state;

public class StateMachineException extends Exception {

	public StateMachineException() {
		super();
	}
	public StateMachineException(String message) {
		super(message);
	}
	public StateMachineException(Throwable cause) { 
		super(cause); 
	}  
	public StateMachineException(String msg, Throwable cause) { 
		super(msg, cause); 
	}  
}
