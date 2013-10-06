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

public class Layout {

	protected Map <String, Cell> cells; 
	public static final String REMOVED = "removed"; 
	public static final String EQUAL = "equal";
	public static final String SHRUNK = "shrunk";
	public static final String GROWN = "grown";
	public static final String NEW = "new";
	// The order in which the cell changes are operated
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
	
	public static final String DEFAULTNS = "http://www.ubioulu.fi/statemachine/cells";
	
	private static Logger logger = Logger.getLogger("org.tandembrowsing");
	
	
	public Layout(String hostname, String smSession) {
		this.hostname = hostname;
		this.smSession = smSession;
		cells = Collections.synchronizedMap(new LinkedHashMap<String, Cell>());
		DBUtil.load(smSession, cells);
	} 

	public String [] getIds() {
		return cells.keySet().toArray(new String[0]);
	}
	
	public Cell [] getCells() {
		return cells.values().toArray(new Cell[0]);
	}
	
	public Cell [] getBrowserCells(String browser) {
		List <Cell> cellList = new ArrayList <Cell>();
		for (Cell cell : cells.values()) {
			// accept cells that don't have browser info and the matching browsers
			if(cell.getBrowser() == null || cell.getBrowser().equals(browser))
				cellList.add(new Cell(cell));
		}
		return cellList.toArray(new Cell[0]);
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
	
	public static Map <String, Cell> parseCells(Node cells) throws ParsingException {
		Map <String, Cell> newcells = Collections.synchronizedMap(new LinkedHashMap<String, Cell>());
		if(cells.getNamespaceURI() != DEFAULTNS || cells.getLocalName() != "cells")
			throw new ParsingException("Invalid name or namespace "+cells.getLocalName()+" : "+cells.getNamespaceURI());
		NodeList list = cells.getChildNodes();
		for(int i = 0;i<list.getLength();i++) {
			Element cell = (Element)list.item(i);
			if(cell.getNamespaceURI() != DEFAULTNS || cell.getLocalName() != "cell")
				throw new ParsingException("Invalid name or namespace "+cell.getLocalName()+" : "+cell.getNamespaceURI());
			Cell newcell = Cell.parseCell((Element)list.item(i));
			newcells.put(newcell.getId(), newcell);
		}
		return newcells;
	}



	/**
	 * difference
	 * 
	 * Compares two sets of Cells based on ids and finds the differences. 
	 * The result will be grouped as:
	 * Type S. Cells that remain and are smaller
	 * Type G. Cells that remain and are bigger
	 * Type M. Cells that remain and have equal size with different properties
	 * Type E. Cells that are equal
	 * Type R. Cells that are removed
	 * Type N. Cells that are new
	 * 
	 * A Cells properties need to be fetched first, which depicts the order of events.
	 *  
	 * - if at least one S Cell has shrank	 		CASE1
	 *    - first remove R Cells,
	 *    - then modify M Cells,
	 * 	  - then shrank shrunk S Cells,
	 * 	  - then grow grown G Cells,
	 *    - and finally add N Cells.
	 * - else if any N Cell size has grown	 		CASE2
	 * 	  - first remove R Cells,
	 *    - then grow G Cells,
	 *    - then modify M Cells,
	 *    - and finally add N Cells.
	 * - else										CASE3
	 *    - first modify M Cells,
	 *    - then remove R Cells,
	 *    - and finally add N Cells. 
	 *    
	 *  NOTE! If there are A Cells that shrink and increase at the same state transition,
	 *  these transitions could be synchronized or alternatively the shrinking one should 
	 *  be operated first. As there is no such use case yet, the latter is implemented.
	 * 
	 * @param parsedCells
	 * @return
	 */
	public Map <Cell, String> difference(Map<String, Cell> parsedCells, Set <String> contentChanged) {
		Set <String> keyset = parsedCells.keySet();
		Map <Cell, String> difference = new HashMap<Cell, String>();
		boolean growncell = false;
		boolean shrunkcell = false;
		for(String i : keyset) {
			// same cell found
			if(cells.containsKey(i)) {
				// handle situation, where resource has changed
				if(!getCell(i).getResource().replaceFirst("LM_HOST", hostname).startsWith(parsedCells.get(i).getResource().replaceFirst("LM_HOST", hostname)))
					contentChanged.add(i+Event.SET_CONTENT);
				else // else put the old data back
					parsedCells.get(i).setResource(getCell(i).getResource());
				
				// if the Cell is set resizable, its size is considered as outside of statemachine's responsibility
				if(cells.get(i).isResizable()) {	
					parsedCells.get(i).copyDimensions(cells.get(i));
					difference.put(new Cell(parsedCells.get(i)), EQUAL);
				} else {
					// new cell is equal / smaller / bigger 
					if (cells.get(i).hasEqualDimensions(parsedCells.get(i))) {
						logger.fine("equal cell : "+parsedCells.get(i).getId());			
						difference.put(new Cell(parsedCells.get(i)), EQUAL);			
					} else if(cells.get(i).isBigger(parsedCells.get(i))) {
						// make the current smaller and then shrink others
						logger.fine("shrunk cell : "+parsedCells.get(i).getId());
						difference.put(new Cell(parsedCells.get(i)), SHRUNK);
						shrunkcell = true;
					} else {
						// first delete others and then make this large
						// this includes also the cases where xyPosition zIndex or border has changed
						logger.fine("grown cell : "+parsedCells.get(i).getId());
						difference.put(new Cell(parsedCells.get(i)), GROWN);
						growncell = true;
					}	
				}
			} // the cell is new 
			else {
				// just add the cell
				logger.fine("new cell : "+parsedCells.get(i).getId());
				difference.put(new Cell(parsedCells.get(i)), NEW);
			}			
		}
		Set <String> currentkeyset = cells.keySet();
		for(String j : currentkeyset) {
			// remove the cells that don't exist anymore
			if(!parsedCells.containsKey(j)) {
				logger.fine("removed cell : "+cells.get(j).getId());
				difference.put(new Cell(cells.get(j)), REMOVED);
			}
		}
		Map <Cell, String> result;
		// sort the Map based on the CASE
		if(shrunkcell) {
			result = sortByValue(difference, COMPARATOR1);
		} else if (growncell) {
			result = sortByValue(difference, COMPARATOR2);
		} else {
			result = sortByValue(difference, COMPARATOR3);
		}

		// finally replace the old cells by new
		cells = parsedCells;
		DBUtil.store(smSession, cells);
		
		return result;
	}
	
	static int indexOf(String [] array, String value) {
		for(int i = 0; i < array.length; i++)
			if(array[i]==value)
				return i;
		
		throw new IndexOutOfBoundsException();
	}
	
	static Map <Cell, String> sortByValue(Map <Cell, String> map, Comparator comp) {
		List <Map.Entry<Cell, String>> list = new LinkedList<Map.Entry<Cell, String>>(map.entrySet()) ;
		Collections.sort(list, comp);
		Map <Cell, String> result = new LinkedHashMap<Cell, String>();
		for (Iterator <Map.Entry<Cell, String>>it = list.iterator(); it.hasNext();) {
			Map.Entry <Cell, String> entry = (Map.Entry<Cell, String>)it.next();
		    result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public Cell getCell(String id) {
		return cells.get(id);
	}
	
	public boolean hasCell(String id) {
		return cells.containsKey(id);
	}

	public String getCellsJSON() {
		JSONArray cellsJSON = new JSONArray();
		
		for(Cell cell : cells.values()) {
			cellsJSON.add(cell.toJSON());
		}
		return cellsJSON.toJSONString();
	}
}
