package org.tandembrowsing.io.soap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.JDOMException;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.EventQueue;


/**
 * LayoutManagerSOAP is a web service interface to external components to
 * send events to the LayoutManager component.
 * 
 * @author tjh
 *
 */

public class LayoutManagerService {
	private static Logger logger = Logger.getLogger("org.tandembrowsing");

	public static String processEvent(String payload) {
		try {
			EventQueue.getInstance().add(Event.parse(payload));
			return "OK";
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "Failed to parse: "+payload, e);
			return "NOK:"+e.getMessage();
		} catch (JDOMException e) {
			logger.log(Level.SEVERE, "Failed to parse: "+payload, e);
			return "NOK:"+e.getMessage();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to parse: "+payload, e);
			return "NOK:"+e.getMessage();
		}
	}
}
