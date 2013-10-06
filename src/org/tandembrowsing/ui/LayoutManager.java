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
import org.tandembrowsing.model.Cell;
import org.tandembrowsing.model.Layout;
import org.tandembrowsing.state.StateMachine;

public class LayoutManager {
	
	private static LayoutManager singletonObject;
	
	public final ReentrantLock lock = new ReentrantLock();
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing");

	private Map <String, Layout> layouts = new HashMap<String, Layout>(); 
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
	 * addCell adds a cell to the layout
	 * @param cell
	 * @return 
	 * @throws LayoutException 
	 */
	public void addCell(String smSession, Cell cell) throws LayoutException {	
		try {
			cell.setResource(transformURL(cell.getResource()));
			logger.log(Level.INFO, "addCell " +cell.toString());
			view.addCell(smSession, cell);
		} catch (LayoutException e) {
			logger.log(Level.SEVERE, "No response from view for cell "+cell.getId()+","+cell.getResource(), e);
			throw e;
		}
	}
	
	public void reload(String smSession, Cell cell) throws LayoutException {
		if(cell == null)
			throw new LayoutException("Cell does not exits. reload ignored.");
		logger.log(Level.INFO, "reload " +cell.getId());
		setContent(smSession, cell);
	}
	
	public void resizeAndMove(String smSession, Operation operation) throws LayoutException {
		Cell modifiedCell = layouts.get(smSession).getCell(operation.getParameterValue(Cell.ID));
		if(modifiedCell == null)
			throw new LayoutException("Cell "+operation.getParameterValue(Cell.ID)+" does not exits. resizeAndMove ignored.");
		if(modifiedCell.isResizable()) {
			if(operation.hasParameter(Cell.WIDTH))
				modifiedCell.setWidth(Float.parseFloat(operation.getParameterValue(Cell.WIDTH)));
			if(operation.hasParameter(Cell.HEIGHT))
				modifiedCell.setHeight(Float.parseFloat(operation.getParameterValue(Cell.HEIGHT)));
			if(operation.hasParameter(Cell.X_POSITION))
				modifiedCell.setXPosition(Float.parseFloat(operation.getParameterValue(Cell.X_POSITION)));
			if(operation.hasParameter(Cell.Y_POSITION))
				modifiedCell.setYPosition(Float.parseFloat(operation.getParameterValue(Cell.Y_POSITION)));
			if(operation.hasParameter(Cell.Z_INDEX))
				modifiedCell.setZIndex(Integer.parseInt(operation.getParameterValue(Cell.Z_INDEX)));
			view.modifyCell(smSession, modifiedCell);
			logger.log(Level.INFO, operation.getName()+" " +modifiedCell.toString());
		} else {
			logger.severe("resizeAndMove ignored for "+operation.getParameterValue(Cell.ID));
		}
	}
	
	
	/**
	 * setContent sets new content on a given cell that already exist on the layout
	 * @param cell
	 * @return
	 * @throws LayoutException 
	 */
	public void setContent(String smSession, Cell cell) throws LayoutException {
		try {
			cell.setResource(transformURL(cell.getResource()));
			logger.log(Level.INFO, "setContent " +cell.toString());
			view.setContent(smSession, cell);
		} catch (LayoutException e) {
			logger.log(Level.SEVERE, "No response from view for cell "+cell.getId()+","+cell.getResource(), e);
			throw e;
		}
	}
	
	/**
	 * mute mutes the cell with cellId if possible. This method can be run multiple times. 
	 * Further calls won't make any effect if the cell is already muted.
	 *  
	 * @param cellId
	 * @throws LayoutException
	 */
	public void mute(String smSession, Cell cell) throws LayoutException {
		logger.log(Level.INFO, "mute " +cell.getId());
		view.mute(smSession, cell);
	}
	
	/**
	 * mute mutes the cell with cellId if possible. This method can be run multiple times. 
	 * Further calls won't make any effect if the cell is already muted.
	 *  
	 * @param cellId
	 * @throws LayoutException
	 */
	public void unMute(String smSession, Cell cell) throws LayoutException {
		logger.log(Level.INFO, "unMute " +cell.getId());
		view.unMute(smSession, cell);
	}
	
	/**
	 * Called when index.html is loaded. Propagates the created cells. 
	 */
	public synchronized void initSession(String uuid_key, String browser, String requestIP, String method, int fullWidth, int fullHeight, String smSession) throws LayoutException	{
		logger.log(Level.INFO, "Session "+smSession+" initialized from "+requestIP+" via "+method+" by " + browser + ".");
		view.initSession(uuid_key, browser, smSession);
		initLayout(smSession, browser, fullWidth, fullHeight);
	}
		
	/**
	 * Called when index.html is loaded. Propagates the created cells. 
	 */
	public synchronized void initLayout(String smSession, String browser, int fullWidth, int fullHeight) throws LayoutException	{
		logger.log(Level.INFO, "Initialize layout. Screen size ("+fullWidth+", "+fullHeight+")");
		if(layouts.containsKey(smSession)) {
			Cell [] cells = layouts.get(smSession).getBrowserCells(browser);
			for(Cell cell : cells) {
				try {			
					cell.setResource(transformURL(cell.getResource()));
					view.initLayout(smSession, cell);
				} catch (LayoutException e) {
					//TODO: inform StateMachine!!!
					logger.log(Level.SEVERE, "No response from view for cell "+cell.getId()+","+cell.getResource(), e);
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
	 * The target state cells properties might have been overridden with the same stateChange command. 
	 * 
	 * Compares the list of cells in previous state to the cells in new state in the statemachine description.
	 * The possible differences for each cell and corresponding actions are:
	 *    1) Cell already exists
	 *       1.1) EQUAL
	 *           - if override operation exist -> override
	 *           - else -> do nothing 
	 *       1.2) MODIFIED
	 *       	 - if override operations exist -> override
	 *           - else -> set new content 
	 *       1.3) GROWN
	 *       	 - modify cell size
	 *           - if override operations exist -> override           
	 *       1.4) SHRUNK
	 *           - modify cell size
	 *           - if override operations exist -> override  
	 *    2) Cell is NEW
	 *    	 - if override operation exist -> add new cell with overridden parameters
	 *       - else add new cell
	 *    3) Cell has been REMOVED
	 *       - remove cell (ignore possible override operation)
	 */
	public void changeLayout(String smSession, Map <String, Cell> newCells, Set <String> operationTypes) {
		try {
			// calculate difference "vector" with special formula
			Map <Cell, String> diff = layouts.get(smSession).difference(newCells, operationTypes);				
			Iterator <Map.Entry<Cell, String>> it = diff.entrySet().iterator();
			while (it.hasNext()) {
		        Map.Entry <Cell, String> pairs = (Map.Entry<Cell, String>)it.next();
		        logger.fine("Cell "+pairs.getKey().getId()+" is "+pairs.getValue());
		        if(pairs.getValue() == Layout.GROWN || pairs.getValue() == Layout.SHRUNK) {			        	
			        view.modifyCell(smSession, pairs.getKey()); 
			        if(operationTypes.contains(pairs.getKey().getId()+Event.SET_CONTENT))
			        	setContent(smSession, pairs.getKey());
		        } else if(pairs.getValue() == Layout.EQUAL) {				        
		        	if(operationTypes.contains(pairs.getKey().getId()+Event.SET_CONTENT))
			        	setContent(smSession, pairs.getKey());
		        } else if(pairs.getValue() == Layout.NEW) {
		        	addCell(smSession, pairs.getKey());					        	
		        } else if(pairs.getValue() == Layout.REMOVED) {
		        	view.removeCell(smSession, pairs.getKey());
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
			Cell modifiedCell = layouts.get(smSession).getCell(operation.getParameterValue(Cell.ID));
			if(modifiedCell == null)
				throw new LayoutException("Cell "+operation.getParameterValue(Cell.ID)+" does not exits. setContent " + operation.getParameterValue(Cell.RESOURCE) + " ignored.");
			modifiedCell.setResource(transformURL(parseResource(operation.getParameterValue(Cell.RESOURCE), modifiedCell.getResource())));
			setContent(smSession, modifiedCell);
		} else if (operation.getName().equals("reload")) {
			reload(smSession, layouts.get(smSession).getCell(operation.getParameterValue(Cell.ID)));
		} else if (operation.getName().equals("resizeAndMove")) {
			resizeAndMove(smSession, operation);
		} else if (operation.getName().equals("mute")) {
			// test first we can mute
			if(operation.getParameterValue(Cell.ID).equals("broadcast") && layouts.get(smSession).hasCell(operation.getParameterValue(Cell.ID))) {
				Cell cell = layouts.get(smSession).getCell(operation.getParameterValue(Cell.ID));
				mute(smSession, cell);
			}
		} else if (operation.getName().equals("unMute")) {
			// test first we can unmute
			if(operation.getParameterValue(Cell.ID).equals("broadcast") && layouts.get(smSession).hasCell(operation.getParameterValue(Cell.ID))) {
				Cell cell = layouts.get(smSession).getCell(operation.getParameterValue(Cell.ID));
				unMute(smSession, cell);
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
		return layouts.get(smSession).getCellsJSON();
	}

	public void addLayoutSession(String smSession) {
		Layout layout = new Layout(hostName, smSession);
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
