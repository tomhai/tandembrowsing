package org.tandembrowsing.io;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;


/**
 * Operation is a container for functions with parameters. 
 * Operation provides also serialization of it self to the XML format and parsing 
 * and XML back to the Object presentation.
 * 
 * @author tjh
 *
 */
public class Operation {
	private Element operationElement;
	
	public Operation(Element element) {
		operationElement = element;
	}
	
	public void addParameter(String name, String value) {
		Element parameterElement = new Element(Event.ELEMENT_PARAMETER);
		parameterElement.setAttribute(Event.ATTRIBUTE_NAME, name);
		parameterElement.setText(value);
		operationElement.addContent(parameterElement);
	}
	
	public String getName() {
		return operationElement.getAttribute(Event.ATTRIBUTE_NAME).getValue();
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasParameter(String name) {
		List <Element> parameters = operationElement.getChildren(Event.ELEMENT_PARAMETER);
		Iterator<Element>it = parameters.iterator();
		while (it.hasNext()) {
			Element parameter = it.next();
			if(parameter.getAttribute(Event.ATTRIBUTE_NAME).getValue().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public String getParameterValue(String name) {
		List <Element> parameters = operationElement.getChildren(Event.ELEMENT_PARAMETER);
		Iterator<Element>it = parameters.iterator();
		while (it.hasNext()) {
			Element parameter = it.next();
			if(parameter.getAttribute(Event.ATTRIBUTE_NAME).getValue().equalsIgnoreCase(name))
				return parameter.getText();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Object[] getParameters() {
		List <Element> eParameters = operationElement.getChildren(Event.ELEMENT_PARAMETER);
		Object [] parameters = new Object [operationElement.getContentSize()];
		Iterator<Element>it = eParameters.iterator();
		int i = 0;
		while (it.hasNext()) {
			parameters[i++] = it.next().getText();
		}
		return parameters;
	}
	
	public String toString() {
		return operationElement.toString();
	}

	public String getParametersJSON() {
		List <Element> parameters = operationElement.getChildren(Event.ELEMENT_PARAMETER);
		Iterator<Element>it = parameters.iterator();
		if(it.hasNext()) {
			String result = ",\"parameters\":[";
			boolean first = true;			
			while (it.hasNext()) {
				Element parameter = it.next();
				if(!first)
					result += ",";
				result += "{\"name\":\""+ parameter.getAttribute(Event.ATTRIBUTE_NAME).getValue()+"\",";
				result += "\"value\":\""+ parameter.getText()+"\"}";
				first = false;
			}
			result += "]";
			return result;
		} else
			return "";
	}
}
