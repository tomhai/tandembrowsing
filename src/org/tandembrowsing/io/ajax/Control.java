package org.tandembrowsing.io.ajax;


import org.directwebremoting.Browser;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSessions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.EventQueue;
import org.tandembrowsing.io.Operation;

/**
 * Control is a JSON over DWR interface for inputting commands from
 * web clients.
 * 
 * @author tjh
 *
 */
public class Control {
	
	public void processEvent(String eventData, String from) {
		System.out.println(eventData);
		JSONObject jsonObj = (JSONObject)JSONValue.parse(eventData);
		JSONObject eventObj = (JSONObject)jsonObj.get("event");
		String eventType = (String)eventObj.get("type");
		String eventName = (String)eventObj.get("name");
		String id = (String)eventObj.get("id");
		String endpoint = (String)eventObj.get("endpoint");
			
		Event event = new Event(eventType, eventName, id, from);
		
		JSONArray operationsObj = (JSONArray)eventObj.get("operation");	
		if(operationsObj != null) {
			for(int j = 0;j<operationsObj.size();j++) {
				JSONObject operationObj = (JSONObject)operationsObj.get(j);
				Operation op = event.addOperation(operationObj.get("type").toString());
				
				JSONArray parametersObj = (JSONArray)operationObj.get("parameter");
				if(parametersObj != null) {
					for(int i = 0;i<parametersObj.size();i++) {
						JSONObject parameterObj = (JSONObject)parametersObj.get(i);
						op.addParameter(parameterObj.get("type").toString(), parameterObj.get("name").toString());			
					}
				}
			}
		} 
		
		
		EventQueue.getInstance().add(event);		
	}
	
	public void sendEvent(String msg, String to, String event) {
		addFunctionCallAll("sendEvent", msg, to, event);
	}
	
	public void addFunctionCallAll(final String funcName, final Object... params)
    {       
        Browser.withPage("/LayoutManager/control.html", new Runnable()
        {
            public void run()
            {            	
            	ScriptBuffer script = new ScriptBuffer();
                script.appendCall(funcName, params);
            	ScriptSessions.addScript(script);
            }
        });
    }

}
