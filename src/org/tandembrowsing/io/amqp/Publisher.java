package org.tandembrowsing.io.amqp;

import java.io.IOException;
import java.util.logging.Logger;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Publisher {
	private static Logger logger = Logger.getLogger("org.tandembrowsing.io.amqp");
	private static final String EXCHANGE = "lmevent";
	private static final String ROUTING_KEY = "fi.ubioulu.lmevent";
	private String routing_key = null;
	Connection conn = null;
	Channel channel = null;
	private String instance_id = null;

	public void initialize(String instance_id) throws IOException{
		this.instance_id = instance_id;
		routing_key = ROUTING_KEY+"."+instance_id;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("vm0076.virtues.fi");
		factory.setPort(5672);
		factory.setUsername("middleware");
		factory.setPassword("5492pn0GE884E5Ma6nO44KO0N7875W4v");
		conn = factory.newConnection();
		channel = conn.createChannel();			
	}
	
	public void publish(String message) {
		if(channel != null)
			try {
				channel.basicPublish(EXCHANGE, routing_key, null, message.getBytes());
			} catch (IOException e) {
				logger.severe(e.getMessage());
			} catch (AlreadyClosedException ace) {
				// try to re-establish once
				try {
					conn = null;
					channel = null;
					initialize(instance_id);
					if(channel != null)
						channel.basicPublish(EXCHANGE, routing_key, null, message.getBytes());
				} catch (IOException e) {
					logger.severe(e.getMessage());
				}				
			}
	}

	protected void finalize() throws Throwable {
	    try {
	    	 conn.close();
	    } finally {
	         super.finalize();
	    }
	 }
}
