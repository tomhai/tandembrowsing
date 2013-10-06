package org.tandembrowsing.state;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.scxml.EventDispatcher;


/**
 * Trivial EventDispatcher implementation.
 * No remote eventing.
 *
 */
public final class SCXMLEventDispatcher implements EventDispatcher, Serializable {

     /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Implementation independent log category. */
    private static Logger logger = Logger.getLogger("org.tandembrowsing");

    /**
     *  Constructor.
     */
    public SCXMLEventDispatcher() {
        super();
    }

    /**
     * @see EventDispatcher#cancel(String)
     */
    public void cancel(final String sendId) {
       	logger.info("cancel( sendId: " + sendId + ")");
    }

    /**
     *	Supports only delayed trigger of event in current StateMachine
     *
     */
    public void send(final String sendId, final String target,
            final String targetType, final String event, final Map params,
            final Object hints, final long delay, final List externalNodes) {
    	logger.info("send( event: " + event + ", delay: " + delay + ")");
    	new Timer().schedule(new TimerTask() {          
    	    @Override
    	    public void run() {
    	        StateMachine.getInstance().triggerEvent("",event);
    	    }
    	}, delay);
    }
}

