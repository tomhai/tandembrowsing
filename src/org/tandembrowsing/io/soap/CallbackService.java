package org.tandembrowsing.io.soap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.logging.Logger;

import org.apache.axis.client.AdminClient;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.axis.utils.Options;


public class CallbackService {
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing.io.soap");
	
	private static IEventThread iEventThreadRef;
	
	public static void start(IEventThread iEventThread) {
		start(iEventThread, 50001);
	}
	
	public static void start(IEventThread iEventThread, int port) {
		iEventThreadRef = iEventThread;
		try {
			InputStream deploymentDescriptorStream = new FileInputStream(new File("conf/simple-server-config.wsdd"));
			logger.info("Starting server on port "+port);
			SimpleAxisServer server = new SimpleAxisServer();
		    ServerSocket ss = null;

		    for (int i = 0; i < 5; i++) {
		    	try {
		    		ss = new ServerSocket(port);
		            break;
		        } catch (java.net.BindException be){
		        	logger.info(be.getMessage());
		            if (i < 4) {
		                // At 3 second intervals.
		                Thread.sleep(3000);
		            } else {
		                throw new Exception("Unable to start server on port " +port);
		            }
		        }
		    }
		    
		    server.setServerSocket(ss);
		    server.start(true);
	
		    // now deploy CallbackServer web service
		    logger.info("Deploying CallbackServer web service");
		    AdminClient adminClient = new AdminClient();
		    adminClient.process(new Options(new String[] {"-ddd","-tlocal"}), deploymentDescriptorStream);
		    logger.info("Started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static String displayEvent(String eventType) {
		return iEventThreadRef.notifyScreenEvent(eventType);
	}
}
