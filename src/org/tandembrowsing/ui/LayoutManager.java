package org.tandembrowsing.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.Operation;
import org.tandembrowsing.model.VirtualScreen;
import org.tandembrowsing.model.MultipartPage;
import org.tandembrowsing.state.StateMachine;

public class LayoutManager {
	
	private static LayoutManager singletonObject;
	
	public final ReentrantLock lock = new ReentrantLock();
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing");

	private Map <String, MultipartPage> layouts = new HashMap<String, MultipartPage>(); 
	private static LayoutManagerView view;
	private static String LM_HOST = "LM_HOST";
	private String hostName = System.getenv("HOSTNAME");
		
	private LayoutManager() {
		view = LayoutManagerView.getInstance();
		logger.log(Level.INFO, "Layout manager started");
	}
	
	/**
	 * Singleton getInstance creator, that guarantees that only one instance of LayoutManager can exist.
	 * @return existing instance of LayoutManager if exists, or creates a new one.
	 */	
	public static LayoutManager getInstance() {
		if (singletonObject == null) {
			singletonObject = new LayoutManager();
		}
		return singletonObject;		
	}
	
	/**
	 * addVirtualScreen adds a virtualscreen to the layout
	 * @param virtualscreen
	 * @return 
	 * @throws LayoutException 
	 */
	public void addVirtualScreen(String smSession, VirtualScreen virtualscreen) throws LayoutException {	
		try {
			virtualscreen.setResource(transformURL(virtualscreen.getResource()));
			logger.log(Level.INFO, "addVirtualScreen " +virtualscreen.toString());
			view.addVirtualScreen(smSession, virtualscreen);
		} catch (LayoutException e) {
			logger.log(Level.SEVERE, "No response from view for virtualscreen "+virtualscreen.getId()+","+virtualscreen.getResource(), e);
			throw e;
		}
	}
	
	public void reload(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		if(virtualscreen == null)
			throw new LayoutException("VirtualScreen does not exits. reload ignored.");
		logger.log(Level.INFO, "reload " +virtualscreen.getId());
		setContent(smSession, virtualscreen);
	}
	
	public void resizeAndMove(String smSession, Operation operation) throws LayoutException {
		VirtualScreen modifiedVirtualScreen = layouts.get(smSession).getVirtualScreen(operation.getParameterValue(VirtualScreen.ID));
		if(modifiedVirtualScreen == null)
			throw new LayoutException("VirtualScreen "+operation.getParameterValue(VirtualScreen.ID)+" does not exits. resizeAndMove ignored.");
		if(modifiedVirtualScreen.isResizable()) {
			if(operation.hasParameter(VirtualScreen.WIDTH))
				modifiedVirtualScreen.setWidth(Float.parseFloat(operation.getParameterValue(VirtualScreen.WIDTH)));
			if(operation.hasParameter(VirtualScreen.HEIGHT))
				modifiedVirtualScreen.setHeight(Float.parseFloat(operation.getParameterValue(VirtualScreen.HEIGHT)));
			if(operation.hasParameter(VirtualScreen.X_POSITION))
				modifiedVirtualScreen.setXPosition(Float.parseFloat(operation.getParameterValue(VirtualScreen.X_POSITION)));
			if(operation.hasParameter(VirtualScreen.Y_POSITION))
				modifiedVirtualScreen.setYPosition(Float.parseFloat(operation.getParameterValue(VirtualScreen.Y_POSITION)));
			if(operation.hasParameter(VirtualScreen.Z_INDEX))
				modifiedVirtualScreen.setZIndex(Integer.parseInt(operation.getParameterValue(VirtualScreen.Z_INDEX)));
			view.modifyVirtualScreen(smSession, modifiedVirtualScreen);
			logger.log(Level.INFO, operation.getName()+" " +modifiedVirtualScreen.toString());
		} else {
			logger.severe("resizeAndMove ignored for "+operation.getParameterValue(VirtualScreen.ID));
		}
	}
	
	
	/**
	 * setContent sets new content on a given virtualscreen that already exist on the layout
	 * @param virtualscreen
	 * @return
	 * @throws LayoutException 
	 */
	public void setContent(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		try {
			virtualscreen.setResource(transformURL(virtualscreen.getResource()));
			logger.log(Level.INFO, "setContent " +virtualscreen.toString());
			view.setContent(smSession, virtualscreen);
		} catch (LayoutException e) {
			logger.log(Level.SEVERE, "No response from view for virtualscreen "+virtualscreen.getId()+","+virtualscreen.getResource(), e);
			throw e;
		}
	}
	
	/**
	 * mute mutes the virtualscreen with virtualscreenId if possible. This method can be run multiple times. 
	 * Further calls won't make any effect if the virtualscreen is already muted.
	 *  
	 * @param virtualscreenId
	 * @throws LayoutException
	 */
	public void mute(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		logger.log(Level.INFO, "mute " +virtualscreen.getId());
		view.mute(smSession, virtualscreen);
	}
	
	/**
	 * mute mutes the virtualscreen with virtualscreenId if possible. This method can be run multiple times. 
	 * Further calls won't make any effect if the virtualscreen is already muted.
	 *  
	 * @param virtualscreenId
	 * @throws LayoutException
	 */
	public void unMute(String smSession, VirtualScreen virtualscreen) throws LayoutException {
		logger.log(Level.INFO, "unMute " +virtualscreen.getId());
		view.unMute(smSession, virtualscreen);
	}
	
	/**
	 * Called when index.html is loaded. Propagates the created virtualscreens. 
	 */
	public synchronized void initSession(String uuid_key, String browser, String requestIP, String method, int fullWidth, int fullHeight, String smSession) throws LayoutException	{
		logger.log(Level.INFO, "Session "+smSession+" initialized from "+requestIP+" via "+method+" by " + browser + ".");
		view.initSession(uuid_key, browser, smSession);
		initLayout(smSession, browser, fullWidth, fullHeight);
	}
		
	/**
	 * Called when index.html is loaded. Propagates the created virtualscreens. 
	 */
	public synchronized void initLayout(String smSession, String browser, int fullWidth, int fullHeight) throws LayoutException	{
		logger.log(Level.INFO, "Initialize layout. Screen size ("+fullWidth+", "+fullHeight+")");
		if(layouts.containsKey(smSession)) {
			VirtualScreen [] virtualscreens = layouts.get(smSession).getBrowserVirtualScreens(browser);
			for(VirtualScreen virtualscreen : virtualscreens) {
				try {			
					virtualscreen.setResource(transformURL(virtualscreen.getResource()));
					view.initLayout(smSession, virtualscreen);
				} catch (LayoutException e) {
					//TODO: inform StateMachine!!!
					logger.log(Level.SEVERE, "No response from view for virtualscreen "+virtualscreen.getId()+","+virtualscreen.getResource(), e);
				}	
			}
		} else {
			StateMachine.getInstance().recoverSession(smSession);
		}
		view.openSession();
	}
	

	/**
	 * Called when statemachine arrives to a state.
	 * 
	 * The target state virtualscreens properties might have been overridden with the same stateChange command. 
	 * 
	 * Compares the list of virtualscreens in previous state to the virtualscreens in new state in the statemachine description.
	 * The possible differences for each virtualscreen and corresponding actions are:
	 *    1) VirtualScreen already exists
	 *       1.1) EQUAL
	 *           - if override operation exist -> override
	 *           - else -> do nothing 
	 *       1.2) MODIFIED
	 *       	 - if override operations exist -> override
	 *           - else -> set new content 
	 *       1.3) GROWN
	 *       	 - modify virtualscreen size
	 *           - if override operations exist -> override           
	 *       1.4) SHRUNK
	 *           - modify virtualscreen size
	 *           - if override operations exist -> override  
	 *    2) VirtualScreen is NEW
	 *    	 - if override operation exist -> add new virtualscreen with overridden parameters
	 *       - else add new virtualscreen
	 *    3) VirtualScreen has been REMOVED
	 *       - remove virtualscreen (ignore possible override operation)
	 */
	public void changeLayout(String smSession, Map <String, VirtualScreen> newVirtualScreens, Set <String> operationTypes) {
		try {
			// calculate difference "vector" with special formula
			Map <VirtualScreen, String> diff = layouts.get(smSession).difference(newVirtualScreens, operationTypes);				
			Iterator <Map.Entry<VirtualScreen, String>> it = diff.entrySet().iterator();
			while (it.hasNext()) {
		        Map.Entry <VirtualScreen, String> pairs = (Map.Entry<VirtualScreen, String>)it.next();
		        logger.fine("VirtualScreen "+pairs.getKey().getId()+" is "+pairs.getValue());
		        if(pairs.getValue() == MultipartPage.GROWN || pairs.getValue() == MultipartPage.SHRUNK) {			        	
			        view.modifyVirtualScreen(smSession, pairs.getKey()); 
			        if(operationTypes.contains(pairs.getKey().getId()+Event.SET_CONTENT))
			        	setContent(smSession, pairs.getKey());
		        } else if(pairs.getValue() == MultipartPage.EQUAL) {				        
		        	if(operationTypes.contains(pairs.getKey().getId()+Event.SET_CONTENT))
			        	setContent(smSession, pairs.getKey());
		        } else if(pairs.getValue() == MultipartPage.NEW) {
		        	addVirtualScreen(smSession, pairs.getKey());					        	
		        } else if(pairs.getValue() == MultipartPage.REMOVED) {
		        	view.removeVirtualScreen(smSession, pairs.getKey());
		        } else {
		        	logger.severe("Unknown operation : "+pairs.getValue());
		        }        
		        if(operationTypes.contains(pairs.getKey().getId()+Event.MUTE))
	        		mute(smSession, pairs.getKey());
	        	else if(operationTypes.contains(pairs.getKey().getId()+Event.UNMUTE))
	        		unMute(smSession, pairs.getKey());
		    }
		} catch (LayoutException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}

	public void processOperations(String smSession, List<Operation> operations) throws LayoutException {
		this.lock.lock();
		try {
			Iterator <Operation>it = operations.iterator();
			while(it.hasNext()) {
				//TODO: current return values are dropped
				processOperation(smSession, it.next());			
			}
		} finally {
			this.lock.unlock();
		}
	}
		
	private void processOperation(String smSession, Operation operation) throws LayoutException {
		if (operation.getName().equals("setContent")) {
			VirtualScreen modifiedVirtualScreen = layouts.get(smSession).getVirtualScreen(operation.getParameterValue(VirtualScreen.ID));
			if(modifiedVirtualScreen == null)
				throw new LayoutException("VirtualScreen "+operation.getParameterValue(VirtualScreen.ID)+" does not exits. setContent " + operation.getParameterValue(VirtualScreen.RESOURCE) + " ignored.");
			modifiedVirtualScreen.setResource(transformURL(parseResource(operation.getParameterValue(VirtualScreen.RESOURCE), modifiedVirtualScreen.getResource())));
			setContent(smSession, modifiedVirtualScreen);
		} else if (operation.getName().equals("reload")) {
			reload(smSession, layouts.get(smSession).getVirtualScreen(operation.getParameterValue(VirtualScreen.ID)));
		} else if (operation.getName().equals("resizeAndMove")) {
			resizeAndMove(smSession, operation);
		} else if (operation.getName().equals("mute")) {
			// test first we can mute
			if(operation.getParameterValue(VirtualScreen.ID).equals("broadcast") && layouts.get(smSession).hasVirtualScreen(operation.getParameterValue(VirtualScreen.ID))) {
				VirtualScreen virtualscreen = layouts.get(smSession).getVirtualScreen(operation.getParameterValue(VirtualScreen.ID));
				mute(smSession, virtualscreen);
			}
		} else if (operation.getName().equals("unMute")) {
			// test first we can unmute
			if(operation.getParameterValue(VirtualScreen.ID).equals("broadcast") && layouts.get(smSession).hasVirtualScreen(operation.getParameterValue(VirtualScreen.ID))) {
				VirtualScreen virtualscreen = layouts.get(smSession).getVirtualScreen(operation.getParameterValue(VirtualScreen.ID));
				unMute(smSession, virtualscreen);
			}
		} else {
			logger.severe("Unsupported operation : " +operation.getName());
		}
	}
	
	public static String parseResource(String newResource, String oldResource) {
		if(newResource.startsWith("&")) {
			// append the parameters to the previous url
			// replace '&' with '?', if the url doesn't have previous parameters 
			String resource = oldResource;
			if (resource.indexOf("?") != -1) {
				StringTokenizer st = new StringTokenizer(newResource.substring(1), "&");
				while(st.hasMoreTokens()) {
					// e.g. language=fi
					String parameter = st.nextToken();
					String [] parArray = parameter.split("=");
					//if parameter is there, we need to change it. Otherwise just append it
					if(resource.indexOf(parArray[0]) != -1) { 
						resource = resource.replaceFirst(parArray[0]+"=(\\w|\\-)*(&|$)", parameter+"&");
					} else {
						resource += resource.endsWith("&") ? parameter : "&"+parameter;
					}		
				}
				// remove the last & if its there 
				resource = resource.endsWith("&") ? resource.substring(0, resource.length() - 1) : resource;
			} else 
				resource += newResource.replaceFirst("&", "?");
			return resource;
		} else
			return newResource;
	}

	public String transformURL(String url) {
		if(url.indexOf(LM_HOST) == -1)
			return url; 
		
		if(hostName == null || hostName.length() == 0) {
			logger.info("Hostname is not set! \n\tIf using virtual machine, set hostname by executing command: \n\t\thostname vm.<instance_id>.ubioulu.fi \n\tand restart tomcat by executing command: \n\t\tservice tomcat5 restart\n\tNow using localhost.");
			return url.replaceFirst(LM_HOST, "localhost");
		} else {	
			return url.replaceFirst(LM_HOST, hostName);
		}
	}
	


	public String getCurrentVirtualScreens(String smSession) {
		return layouts.get(smSession).getVirtualScreensJSON();
	}

	public void addLayoutSession(String smSession) {
		MultipartPage layout = new MultipartPage(hostName, smSession);
		layouts.put(smSession, layout);
	}

	public void setAttribute(Operation operation) {	
		view.setAttribute(operation.getParameterValue("targetKey"), operation.getParameterValue("targetValue"), operation.getParameterValue("key"), operation.getParameterValue("value"));

	}

	public void clearSession(String smSession) {
		layouts.remove(smSession);
	}

	public void setStateMachine(String smSession, String stateMachine) {
		view.setStateMachine(smSession, stateMachine);
		
	}

}
