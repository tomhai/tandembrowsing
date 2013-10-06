package org.tandembrowsing.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.db.DBUtil;
import org.tandembrowsing.ui.LayoutManagerView;

public class MultipartPage {

	protected Map <String, VirtualScreen> virtualscreens; 
	public static final String REMOVED = "removed"; 
	public static final String EQUAL = "equal";
	public static final String SHRUNK = "shrunk";
	public static final String GROWN = "grown";
	public static final String NEW = "new";
	// The order in which the virtualscreen changes are operated
	private static final String [] CASE1 = {REMOVED,EQUAL,SHRUNK,GROWN,NEW};
	private static final String [] CASE2 = {REMOVED,GROWN,EQUAL,NEW};
	private static final String [] CASE3 = {EQUAL,REMOVED,NEW};
	private static final Comparator COMPARATOR1 = new Comparator() { public int compare(Object o1, Object o2) { return ((Comparable)indexOf(CASE1,(String)((Map.Entry) o1).getValue())).compareTo(indexOf(CASE1,(String)((Map.Entry) o2).getValue()));}};
	private static final Comparator COMPARATOR2 = new Comparator() { public int compare(Object o1, Object o2) { return ((Comparable)indexOf(CASE2,(String)((Map.Entry) o1).getValue())).compareTo(indexOf(CASE2,(String)((Map.Entry) o2).getValue()));}};
	private static final Comparator COMPARATOR3 = new Comparator() { public int compare(Object o1, Object o2) {	return ((Comparable)indexOf(CASE3,(String)((Map.Entry) o1).getValue())).compareTo(indexOf(CASE3,(String)((Map.Entry) o2).getValue()));}};
	
	public static final float FULL_WIDTH = 1;
	public static final float FULL_HEIGHT = 1;
	public static final float ORIGO_X = 0;
	public static final float ORIGO_Y = 0;
	private String hostname;
	private String smSession;
	
	public static final String DEFAULTNS = "http://www.tandembrowsing.org/multipartpage/";
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	
	
	public MultipartPage(String hostname, String smSession) {
		this.hostname = hostname;
		this.smSession = smSession;
		virtualscreens = Collections.synchronizedMap(new LinkedHashMap<String, VirtualScreen>());
		DBUtil.load(smSession, virtualscreens);
	} 

	public String [] getIds() {
		return virtualscreens.keySet().toArray(new String[0]);
	}
	
	public VirtualScreen [] getVirtualScreens() {
		return virtualscreens.values().toArray(new VirtualScreen[0]);
	}
	
	public VirtualScreen [] getBrowserVirtualScreens(String browser) {
		List <VirtualScreen> virtualscreenList = new ArrayList <VirtualScreen>();
		for (VirtualScreen virtualscreen : virtualscreens.values()) {
			// accept virtualscreens that don't have browser info and the matching browsers
			if(virtualscreen.getBrowser() == null || virtualscreen.getBrowser().equals(browser))
				virtualscreenList.add(new VirtualScreen(virtualscreen));
		}
		return virtualscreenList.toArray(new VirtualScreen[0]);
	}
	
	public static String serializeDoc(Node doc){
		String xmlString = new String();
		StringWriter stringOut = new StringWriter();
		if (doc != null) {
			OutputFormat opfrmt = new OutputFormat(doc.getOwnerDocument(),"UTF-8", true);
			opfrmt.setIndenting(true);
			opfrmt.setPreserveSpace(false);
			opfrmt.setLineWidth(500);
			XMLSerializer serial = new XMLSerializer(stringOut, opfrmt);
			try {
				serial.asDOMSerializer();
				serial.serialize(doc);
				xmlString = stringOut.toString();
			} catch (java.io.IOException ioe) {
				xmlString = null;
			}
		} else
			xmlString = null;
		return xmlString;
	}
	
	public static Map <String, VirtualScreen> parseMultipartPage(Node multipartpage) throws ParsingException {
		Map <String, VirtualScreen> newmultipartpage = Collections.synchronizedMap(new LinkedHashMap<String, VirtualScreen>());
		if(multipartpage.getNamespaceURI() != DEFAULTNS || multipartpage.getLocalName() != "multipartpage")
			throw new ParsingException("Invalid name or namespace "+multipartpage.getLocalName()+" : "+multipartpage.getNamespaceURI());
		NodeList list = multipartpage.getChildNodes();
		for(int i = 0;i<list.getLength();i++) {
			Element virtualscreen = (Element)list.item(i);
			if(virtualscreen.getNamespaceURI() != DEFAULTNS || virtualscreen.getLocalName() != "virtualscreen")
				throw new ParsingException("Invalid name or namespace "+virtualscreen.getLocalName()+" : "+virtualscreen.getNamespaceURI());
			VirtualScreen newvirtualscreen = VirtualScreen.parse((Element)list.item(i));
			newmultipartpage.put(newvirtualscreen.getId(), newvirtualscreen);
		}
		return newmultipartpage;
	}



	/**
	 * difference
	 * 
	 * Compares two sets of VirtualScreens based on ids and finds the differences. 
	 * The result will be grouped as:
	 * Type S. VirtualScreens that remain and are smaller
	 * Type G. VirtualScreens that remain and are bigger
	 * Type M. VirtualScreens that remain and have equal size with different properties
	 * Type E. VirtualScreens that are equal
	 * Type R. VirtualScreens that are removed
	 * Type N. VirtualScreens that are new
	 * 
	 * A VirtualScreens properties need to be fetched first, which depicts the order of events.
	 *  
	 * - if at least one S VirtualScreen has shrank	 		CASE1
	 *    - first remove R VirtualScreens,
	 *    - then modify M VirtualScreens,
	 * 	  - then shrank shrunk S VirtualScreens,
	 * 	  - then grow grown G VirtualScreens,
	 *    - and finally add N VirtualScreens.
	 * - else if any N VirtualScreen size has grown	 		CASE2
	 * 	  - first remove R VirtualScreens,
	 *    - then grow G VirtualScreens,
	 *    - then modify M VirtualScreens,
	 *    - and finally add N VirtualScreens.
	 * - else										CASE3
	 *    - first modify M VirtualScreens,
	 *    - then remove R VirtualScreens,
	 *    - and finally add N VirtualScreens. 
	 *    
	 *  NOTE! If there are A VirtualScreens that shrink and increase at the same state transition,
	 *  these transitions could be synchronized or alternatively the shrinking one should 
	 *  be operated first. As there is no such use case yet, the latter is implemented.
	 * 
	 * @param parsedVirtualScreens
	 * @return
	 */
	public Map <VirtualScreen, String> difference(Map<String, VirtualScreen> parsedVirtualScreens, Set <String> contentChanged) {
		Set <String> keyset = parsedVirtualScreens.keySet();
		Map <VirtualScreen, String> difference = new HashMap<VirtualScreen, String>();
		boolean grownvirtualscreen = false;
		boolean shrunkvirtualscreen = false;
		for(String i : keyset) {
			// same virtualscreen found
			if(virtualscreens.containsKey(i)) {
				// handle situation, where resource has changed
				if(!getVirtualScreen(i).getResource().replaceFirst("LM_HOST", hostname).startsWith(parsedVirtualScreens.get(i).getResource().replaceFirst("LM_HOST", hostname)))
					contentChanged.add(i+Event.SET_CONTENT);
				else // else put the old data back
					parsedVirtualScreens.get(i).setResource(getVirtualScreen(i).getResource());
				
				// if the VirtualScreen is set resizable, its size is considered as outside of statemachine's responsibility
				if(virtualscreens.get(i).isResizable()) {	
					parsedVirtualScreens.get(i).copyDimensions(virtualscreens.get(i));
					difference.put(new VirtualScreen(parsedVirtualScreens.get(i)), EQUAL);
				} else {
					// new virtualscreen is equal / smaller / bigger 
					if (virtualscreens.get(i).hasEqualDimensions(parsedVirtualScreens.get(i))) {
						logger.fine("equal virtualscreen : "+parsedVirtualScreens.get(i).getId());			
						difference.put(new VirtualScreen(parsedVirtualScreens.get(i)), EQUAL);			
					} else if(virtualscreens.get(i).isBigger(parsedVirtualScreens.get(i))) {
						// make the current smaller and then shrink others
						logger.fine("shrunk virtualscreen : "+parsedVirtualScreens.get(i).getId());
						difference.put(new VirtualScreen(parsedVirtualScreens.get(i)), SHRUNK);
						shrunkvirtualscreen = true;
					} else {
						// first delete others and then make this large
						// this includes also the cases where xyPosition zIndex or border has changed
						logger.fine("grown virtualscreen : "+parsedVirtualScreens.get(i).getId());
						difference.put(new VirtualScreen(parsedVirtualScreens.get(i)), GROWN);
						grownvirtualscreen = true;
					}	
				}
			} // the virtualscreen is new 
			else {
				// just add the virtualscreen
				logger.fine("new virtualscreen : "+parsedVirtualScreens.get(i).getId());
				difference.put(new VirtualScreen(parsedVirtualScreens.get(i)), NEW);
			}			
		}
		Set <String> currentkeyset = virtualscreens.keySet();
		for(String j : currentkeyset) {
			// remove the virtualscreens that don't exist anymore
			if(!parsedVirtualScreens.containsKey(j)) {
				logger.fine("removed virtualscreen : "+virtualscreens.get(j).getId());
				difference.put(new VirtualScreen(virtualscreens.get(j)), REMOVED);
			}
		}
		Map <VirtualScreen, String> result;
		// sort the Map based on the CASE
		if(shrunkvirtualscreen) {
			result = sortByValue(difference, COMPARATOR1);
		} else if (grownvirtualscreen) {
			result = sortByValue(difference, COMPARATOR2);
		} else {
			result = sortByValue(difference, COMPARATOR3);
		}

		// finally replace the old virtualscreens by new
		virtualscreens = parsedVirtualScreens;
		DBUtil.store(smSession, virtualscreens);
		
		return result;
	}
	
	static int indexOf(String [] array, String value) {
		for(int i = 0; i < array.length; i++)
			if(array[i]==value)
				return i;
		
		throw new IndexOutOfBoundsException();
	}
	
	static Map <VirtualScreen, String> sortByValue(Map <VirtualScreen, String> map, Comparator comp) {
		List <Map.Entry<VirtualScreen, String>> list = new LinkedList<Map.Entry<VirtualScreen, String>>(map.entrySet()) ;
		Collections.sort(list, comp);
		Map <VirtualScreen, String> result = new LinkedHashMap<VirtualScreen, String>();
		for (Iterator <Map.Entry<VirtualScreen, String>>it = list.iterator(); it.hasNext();) {
			Map.Entry <VirtualScreen, String> entry = (Map.Entry<VirtualScreen, String>)it.next();
		    result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public VirtualScreen getVirtualScreen(String id) {
		return virtualscreens.get(id);
	}
	
	public boolean hasVirtualScreen(String id) {
		return virtualscreens.containsKey(id);
	}

	public String getVirtualScreensJSON() {
		JSONArray virtualscreensJSON = new JSONArray();
		
		for(VirtualScreen virtualscreen : virtualscreens.values()) {
			virtualscreensJSON.add(virtualscreen.toJSON());
		}
		return virtualscreensJSON.toJSONString();
	}
}
