package org.tandembrowsing.model;

public class ParsingException extends Exception {

	public ParsingException() {
		super();
	}
	public ParsingException(String message) {
		super(message);
	}
	public ParsingException(Throwable cause) { 
		super(cause); 
	}  
	public ParsingException(String msg, Throwable cause) { 
		super(msg, cause); 
	}  
	
}
