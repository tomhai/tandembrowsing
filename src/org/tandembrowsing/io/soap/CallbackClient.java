package org.tandembrowsing.io.soap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.tandembrowsing.io.soap.out.Callback;
import org.tandembrowsing.io.soap.out.CallbackServiceLocator;

public class CallbackClient {
	private static Logger logger = Logger.getLogger("org.tandembrowsing.rmproxy");
		
	
	public static String displayEvent(String eventType, String address) {
		try {
			CallbackServiceLocator locator = new CallbackServiceLocator();
			logger.info("Using endpoint "+address);
			locator.setCallbackEndpointAddress(address);
			Callback client = locator.getCallback();
			return client.displayEvent(eventType);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not send touch event to "+address, e);
			return null;
		}
	}	
}
