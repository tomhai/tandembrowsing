package org.tandembrowsing.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


/**
 * Event is a container for passing commands.
 * Event provides also serialization of it self to the XML format and parsing 
 * and XML back to the Object presentation.
 * 
 * @author tjh
 *
 */

public class Event {
	
	public static final String EVENT_CHANGE_STATE = "changeState";
	public static final String EVENT_MODIFY_STATE = "modifyState";
	public static final String EVENT_GET_STATE = "getState";
	public static final String EVENT_GET_VIRTUALSCREENS = "getVirtualScreens";
	public static final String EVENT_CONFIGURE = "configure";
	public static final String EVENT_GET_STATEMACHINE = "getStateMachine";
	public static final String SET_CONTENT = "setContent";
	public static final String SET_STATEMACHINE = "setStateMachine";
	public static final String SET_ATTRIBUTE = "setAttribute";
	public static final String get_ATTRIBUTE = "getAttribute";
	public static final String RELOAD = "reload";
	public static final String RESIZE_AND_MOVE = "resizeAndMove";
	public static final String MUTE = "mute";
	public static final String UNMUTE = "unMute";
	
	static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_ENDPOINT = "endpoint";
	private static final String ELEMENT_EVENT = "event";
	protected static final String ELEMENT_OPERATION = "operation";
	protected static final String ELEMENT_PARAMETER = "parameter";
	
	private Element eventElement;
	private Document eventDocument;

	public Event(String type, String name, String id) {
		this(type, name, id, "");
	}

	public Event(String type, String name, String id, String endpoint) {
		eventElement = new Element (ELEMENT_EVENT);
		eventElement.setAttribute(ATTRIBUTE_TYPE, type);
		eventElement.setAttribute(ATTRIBUTE_NAME, name);
		eventElement.setAttribute(ATTRIBUTE_ID, id);
		eventElement.setAttribute(ATTRIBUTE_ENDPOINT, endpoint);
		eventDocument = new Document(eventElement);
	}
	
	private Event(Document eventDoc) {
		eventDocument = eventDoc;		
	}
	
	public Operation addOperation(String name) {
		Element operationElement = new Element(ELEMENT_OPERATION);
		operationElement.setAttribute(ATTRIBUTE_NAME, name);		
		eventElement.addContent(operationElement);
		return new Operation(operationElement);
	}
	
	public String serializeToString() {
		XMLOutputter serializer = new XMLOutputter();
		return serializer.outputString(eventDocument);
	}
	
	public static Event parse(String payload) throws UnsupportedEncodingException, JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		Document eventDoc = parser.build(new ByteArrayInputStream(payload.getBytes("UTF-8")));
		return new Event(eventDoc);	
	}

	public String getEventName() {
		if(eventDocument.getRootElement().getAttribute(ATTRIBUTE_NAME) != null)
			return eventDocument.getRootElement().getAttribute(ATTRIBUTE_NAME).getValue();
		else
			return "";
	}
	
	public String getEventType() {
		return eventDocument.getRootElement().getAttribute(ATTRIBUTE_TYPE).getValue();		
	}
	
	public String getEventId() {
		if(eventDocument.getRootElement().getAttribute(ATTRIBUTE_ID) != null)
			return eventDocument.getRootElement().getAttribute(ATTRIBUTE_ID).getValue();	
		else
			return "";
	}
	
	public String getEventEndpoint() {
		if(eventDocument.getRootElement().getAttribute(ATTRIBUTE_ENDPOINT) != null)
			return eventDocument.getRootElement().getAttribute(ATTRIBUTE_ENDPOINT).getValue();
		else
			return "";
	}
	
	public void setEventEndpoint(String endpointAddress) {
		eventDocument.getRootElement().getAttribute(ATTRIBUTE_ENDPOINT).setValue(endpointAddress);		
	}

	@SuppressWarnings("unchecked")
	public List <Operation> getOperations() {
		List <Operation> operationsResult = new ArrayList <Operation> ();		
		List <Element> operations = eventDocument.getRootElement().getChildren(ELEMENT_OPERATION);
		Iterator <Element> it = operations.iterator();
		while(it.hasNext())		
			operationsResult.add(new Operation(it.next()));
		return operationsResult;
	}

	public boolean hasOperations() {
		return !eventDocument.getRootElement().getChildren(ELEMENT_OPERATION).isEmpty();
	}

	public String getOperationsJSON() {
		List <Operation> operations = getOperations();
		Iterator <Operation> it = operations.iterator();
		if (it.hasNext()) {
			String result = ",\"operations\":[";
			boolean first = true;
			while(it.hasNext())	{
				Operation op = it.next();
				if(!first)
					result += ",";
					
				result += "{\"name\":\""+op.getName()+"\"";				
				result += op.getParametersJSON();
				result += "}";				
				first = false;
			}	
			result += "]";
			return result;
		} else 
			return "";
	}
		


}
