package org.tandembrowsing.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.directwebremoting.Browser;
import org.directwebremoting.Container;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ScriptSessionFilter;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.extend.ScriptSessionManager;

import org.tandembrowsing.io.soap.CallbackClient;
import org.tandembrowsing.model.VirtualScreen;
import org.tandembrowsing.state.StateMachine;

public class LayoutManagerView {
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	
	private static LayoutManagerView singletonObject;
		
	public static final String CONTEXTROOT = "/tandembrowsing/";

	private static final String BROWSER_KEY = "browser";
	private static final String UUID_KEY = "uuid_key";
	private static final String SESSION = "session";
	private static final String STATUS_KEY = "status";
	private static final String TOUCH = "touch"; 
	
	protected Set <String> callbackQueue;
	
	ScriptSessionManager manager;
	Map <String, Long> sessionTimers;
	Map <String, String> sessionkey;
	
	private LayoutManagerView() 
	{
		callbackQueue = Collections.synchronizedSet(new HashSet<String>());
		Container container = ServerContextFactory.get().getContainer();
		manager = container.getBean(ScriptSessionManager.class);
		sessionTimers = new HashMap <String, Long>();
		sessionkey = new HashMap <String, String>();
	}
	
	public static LayoutManagerView getInstance() {
		if (singletonObject == null) {
			singletonObject = new LayoutManagerView();
			SessionPoller executor = singletonObject.new SessionPoller();
			executor.start();
		}
		return singletonObject;		
	}
	
	public void modifyVirtualScreen(String smSession, VirtualScreen modifiedVirtualScreen) throws LayoutException {	
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(BROWSER_KEY, new Check(modifiedVirtualScreen.getBrowser(), false));
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "resizeAndMove", modifiedVirtualScreen.getId(), modifiedVirtualScreen.getWidth(), modifiedVirtualScreen.getHeight(), modifiedVirtualScreen.getXPosition(), modifiedVirtualScreen.getYPosition(), modifiedVirtualScreen.getZIndex(), 1000);
	}
		
	/**
	 * addVirtualScreen does the actual drawing of the new virtualscreen and modifies the existing virtualscreens on javascript side
	 * @param newVirtualScreen
	 * @param embeddedTag
	 */
	public void addVirtualScreen(String smSession, VirtualScreen newVirtualScreen) throws LayoutException {
		if(!newVirtualScreen.getId().startsWith("EMPTY")) {	
			BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
			filter.add(BROWSER_KEY, new Check(newVirtualScreen.getBrowser(), false));
			filter.add(SESSION, new Check(smSession, false));
			addFunctionCall(filter, "addVirtualScreen", newVirtualScreen.getId(), newVirtualScreen.getWidth(), newVirtualScreen.getHeight(), newVirtualScreen.getXPosition(), newVirtualScreen.getYPosition(), newVirtualScreen.getZIndex(), newVirtualScreen.getBorder(), newVirtualScreen.getResource());
			
			/*BrowserScriptSessionFilter exclusiveFilter = new BrowserScriptSessionFilter();
			exclusiveFilter.add(BROWSER_KEY, new Check(newVirtualScreen.getBrowser(), true));
			exclusiveFilter.add(SESSION, new Check(smSession, false));
			addFunctionCall(exclusiveFilter, "addVirtualScreen", newVirtualScreen.getId(), 0, 0, 0, 0, 0, 0, LayoutManagerView.CONTEXTROOT+"stub.html");*/
		}
	}
	
	public void initLayout(String smSession, VirtualScreen newVirtualScreen) throws LayoutException {
		if(!newVirtualScreen.getId().startsWith("EMPTY")) {	
			BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
			filter.add(STATUS_KEY, new Check("initSession", false));
			filter.add(SESSION, new Check(smSession, false));
			addFunctionCall(filter, "addVirtualScreen", newVirtualScreen.getId(), newVirtualScreen.getWidth(), newVirtualScreen.getHeight(), newVirtualScreen.getXPosition(), newVirtualScreen.getYPosition(), newVirtualScreen.getZIndex(), newVirtualScreen.getBorder(), newVirtualScreen.getResource());
		}		
	}
	
	/**
	 * removeVirtualScreen does the actual removing of the virtualscreen and modifies the remaining virtualscreens if needed on the javascript side
	 * @param virtualscreen
	 * @return
	 */
	public void removeVirtualScreen(String smSession, VirtualScreen virtualscreen) throws LayoutException {		
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(BROWSER_KEY, new Check(virtualscreen.getBrowser(), false));
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "removeVirtualScreen", virtualscreen.getId());
	}
	
	/**
	 * setContent does the actual drawing of the new content on the javascript side
	 * @param modifiedVirtualScreen
	 * @param embeddedTag
	 */
	public void setContent(String smSession, VirtualScreen modifiedVirtualScreen) throws LayoutException {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "setVirtualScreenContentSrc", modifiedVirtualScreen.getId(), modifiedVirtualScreen.getResource());
	}
	
	//ei toimi, kun ei oo parsettanut SCXML:הה. Korjaa
	public void mute(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "muteVirtualScreen", virtualscreen.getId());
	}
	//ei toimi, kun ei oo parsettanut SCXML:הה. Korjaa	
	public void unMute(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "unMuteVirtualScreen", virtualscreen.getId());
	}
	
	public synchronized void initSession(String uuid_key, String browser, String smSession) {
		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
		if(browser != null)
			scriptSession.setAttribute(BROWSER_KEY, browser);
		if(uuid_key != null)
			scriptSession.setAttribute(UUID_KEY, uuid_key);
		if(smSession != null)
			scriptSession.setAttribute(SESSION, smSession);
		scriptSession.setAttribute(STATUS_KEY, "initSession");
		
		sessionTimers.put(uuid_key, System.currentTimeMillis());
		sessionkey.put(uuid_key, smSession);
	}
	
	/**
	 * Sets and attribute for a filtered session. 
	 * 
	 * @param filterName
	 * @param filterValue
	 * @param attributeName
	 * @param attributeValue
	 */
	
	public void setAttribute(String filterName, String filterValue, final String attributeName, final String attributeValue) {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(filterName, new Check(filterValue, false));
	    addFunctionCall(filter, "setAttribute", attributeName, attributeValue);    
	    
	    Browser.withPageFiltered(CONTEXTROOT+"index.jsp", filter, new Runnable()
        {
            public void run()
            {            	
            	ScriptSessions.setAttribute(attributeName, attributeValue);
            }
        });
	}
	
	
	/**
	 * Gets and attribute for a filtered session. 
	 * 
	 * @param filterName
	 * @param filterValue
	 * @param attributeName
	 * @param attributeValue
	 */
	
	public void getAttribute(String filterName, String filterValue, final String attributeName) {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(filterName, new Check(filterValue, false));
		Browser.withPageFiltered(CONTEXTROOT+"index.jsp", filter, new Runnable()
		{
	        public void run()
	        {            	
	          	ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
	           	String attributeValue = (String)scriptSession.getAttribute(attributeName);
	           	ScriptBuffer script = new ScriptBuffer();
                script.appendCall("sendAttribute", attributeName, attributeValue);
            	ScriptSessions.addScript(script);
	        }
	    });
	}
	
	public synchronized void openSession() {
		ScriptSession scriptSession = WebContextFactory.get().getScriptSession();
		scriptSession.setAttribute(STATUS_KEY, "sessionOpen");
	}
	
	private void checkCallbacks(String ... corIds) throws LayoutException {
		// try 30 * 100ms = 3 seconds
		int count = 30;
        boolean check;
		while(count-- > 0) {	
			check = true;
			try {
				for (int i = 0; i < corIds.length; i++)
					if(callbackQueue.contains(corIds[i]))
						check = false;
				if(check) {
					return;				
				}					
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		//clear it
		callbackQueue.clear();
		logger.info("callback(s) not received within the timelimit");
		throw new LayoutException("callback(s) not received within the timelimit");	
	}
	
	private synchronized String setCorrelationId() {
		String correlationId = "CorrelationId"+Math.random();
		addCallback(correlationId);
		return correlationId;
	}
	
	private void addCallback(String correlationId) {
		callbackQueue.add(correlationId);
	}
	
	public void removeCallback(String correlationId) {
		boolean result = callbackQueue.remove(correlationId);
		if(result != true) {
			logger.log(Level.SEVERE, "callback " + correlationId +" was not in the queue");
		}
	}
	
	public void addFunctionCall(ScriptSessionFilter filter, final String funcName, final Object... params)
    {       

        Browser.withPageFiltered(CONTEXTROOT+"index.jsp", filter, new Runnable()
        {
            public void run()
            {            	
            	ScriptBuffer script = new ScriptBuffer();
                script.appendCall(funcName, params);
            	ScriptSessions.addScript(script);
            }
        });
    }
	
	public void addFunctionCallAll(final String funcName, final Object... params)
    {       
        Browser.withPage(CONTEXTROOT+"index.jsp", new Runnable()
        {
            public void run()
            {            	
            	ScriptBuffer script = new ScriptBuffer();
                script.appendCall(funcName, params);
            	ScriptSessions.addScript(script);
            }
        });
    }
	
	public void keepAlive(String uuid_key, String browser) {
		if(sessionTimers.get(uuid_key) != null) {
			BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
			filter.add(UUID_KEY, new Check(uuid_key, false));
			if((System.currentTimeMillis() - sessionTimers.get(uuid_key)) > 60000) {
				addFunctionCall(filter, "keepAliveResponse", false);
				logger.log(Level.SEVERE, "No keepalive from "+uuid_key+" in 60 seconds. Session will be killed.");
			} else {
				addFunctionCall(filter, "keepAliveResponse", true);
			}
		} else { // tomcat rebooted?	
			addFunctionCallAll("keepAliveResponse", true);
		}
		sessionTimers.put(uuid_key, System.currentTimeMillis());
	}
	
	private class SessionPoller extends Thread {
	    public void run() {
	        while(true) {
	        	try {
	        		Iterator<Entry<String, Long>> it = sessionTimers.entrySet().iterator();
	        		while (it.hasNext()) {
	        			Map.Entry <String, Long>item = (Map.Entry<String, Long>)it.next();
	        		    String key = item.getKey();
	        		    long value = item.getValue();
	        		    if((System.currentTimeMillis() - value) > 60000) {
	        		    	String smSession = sessionkey.remove(key);
	        		    	it.remove();
	        		    	if(!sessionkey.containsValue(smSession)) {        		    		
	        		    		StateMachine.getInstance().clearSession(smSession);
	        		    	}        		    	
	           		    }  		    
	        		}
		            try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
		        } catch (Exception e) {
	        		logger.log(Level.SEVERE, "Failure session managamenet", e);
	        	}
		    }        	
	    }	
	}

	
	private class Check {
		private String attributeValue;
		private boolean exclusive;
		public Check(String attributeValue, boolean exclusive)
		{
		    this.attributeValue = attributeValue;
		    this.exclusive = exclusive;
		}
	}
	
	private class BrowserScriptSessionFilter implements ScriptSessionFilter {
		Map <String, Check> parameters = new HashMap<String,Check>();
		
		
		public void add(String attributeName, Check check) {
			parameters.put(attributeName, check);
		}
		
		/* (non-Javadoc)
		 * @see org.directwebremoting.ScriptSessionFilter#match(org.directwebremoting.ScriptSession)
		 */
		public boolean match(ScriptSession session)
		{	
			boolean matchAll = true;
			for (Map.Entry<String, Check> entry : parameters.entrySet()) {
				Object check = session.getAttribute(entry.getKey());			
				Check valueCheck = entry.getValue();
				if(valueCheck.exclusive) {
				    if(valueCheck.attributeValue == null || (check != null && check.equals(valueCheck.attributeValue))) {
				    	matchAll = false;
				    } else {
				    }
				} else { 
					if(valueCheck.attributeValue == null || (check != null && check.equals(valueCheck.attributeValue))) {
					} else {
						matchAll = false;
					}
				}
			}
			return matchAll;
		}		
	}

	public void setStateMachine(String smSession, String stateMachine) {
		BrowserScriptSessionFilter filter = new BrowserScriptSessionFilter();
		filter.add(SESSION, new Check(smSession, false));
		addFunctionCall(filter, "setStateMachine", stateMachine);
		
	}

}
