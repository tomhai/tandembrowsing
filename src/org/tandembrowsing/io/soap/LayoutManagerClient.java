package org.tandembrowsing.io.soap;

import java.rmi.RemoteException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.soap.in.LayoutManager;
import org.tandembrowsing.io.soap.in.LayoutManagerServiceLocator;

public class LayoutManagerClient {
	
	private LayoutManager client;
	ResourceBundle properties;
	private static Logger logger = Logger.getLogger("org.tandembrowsing.io.soap");
	
	private String endpointAddress;
	
	public String processEvent(Event event) throws RemoteException {
		event.setEventEndpoint(endpointAddress);
		return client.processEvent(event.serializeToString());
	}
	
	public LayoutManagerClient(IEventThread iEventThread) throws ServiceException {
		LayoutManagerServiceLocator locator = new LayoutManagerServiceLocator();		
		String address = null; 
		try {
			properties = ResourceBundle.getBundle("display");
			address = properties.getString("LayoutManager_address");
			logger.info("Using endpoint "+address+ " from conf");
		} catch(MissingResourceException mre) {
	    	properties = ResourceBundle.getBundle("org.tandembrowsing.display");
	    	address = properties.getString("LayoutManager_address");
	    	logger.info("Using endpoint "+address+ " from jar");
		}	  
		locator.setLayoutManagerEndpointAddress(address);
		client = locator.getLayoutManager();
		// start the server 
		if (iEventThread != null)
			CallbackService.start(iEventThread, Integer.parseInt(properties.getString("CallbackService_port")));
		
		endpointAddress = properties.getString("Callback_address");
	}	
}
