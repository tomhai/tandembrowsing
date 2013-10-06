<%@ page language="java" contentType="application/json; charset=ISO-8859-1" pageEncoding="ISO-8859-1" import="java.util.*,org.tandembrowsing.state.*, org.tandembrowsing.model.*,org.tandembrowsing.io.*"%>[
<%
		if(request.getParameterNames().hasMoreElements()) {
				boolean runCmd = true;
				boolean mustHaveOperation = false;
				Event event = null;
				String eventType = request.getParameter("event");	
				if(eventType != null && eventType.length() > 0) {				
						if(eventType.equals(Event.EVENT_CHANGE_STATE)) {
								String eventTarget = request.getParameter("target");	
								if(eventTarget != null && eventTarget.length() > 0) {
										event = new Event(eventType,eventTarget,"");							
								} else {
										runCmd = false;
										%>{"error": "<%= "Missing parameter target"%>"},
{"msg": "<%="target={targetState} mandatory for "+ Event.EVENT_CHANGE_STATE%>"}<%
								}								
						} else if(eventType.equals(Event.EVENT_MODIFY_STATE)) {
								event = new Event(eventType,"","");
								mustHaveOperation = true;
						} else if(eventType.equals(Event.EVENT_GET_STATE)) {
								event = new Event(eventType,"","");
						} else if(eventType.equals(Event.EVENT_GET_STATEMACHINE)) {
								event = new Event(eventType,"","");
						} else if(eventType.equals(Event.EVENT_CONFIGURE)) {
								event = new Event(eventType,"","");
						} else {
								runCmd = false;
								%>{"error": "<%= "Event type not supported"%>"},
{"msg": "<%="event=["+ Event.EVENT_CHANGE_STATE+"|"+Event.EVENT_MODIFY_STATE+"|"+Event.EVENT_GET_STATE+"|"+Event.EVENT_GET_STATEMACHINE+"|"+Event.EVENT_CHANGE_STATE+"]"%>"}<%
						}					
				} else {
						runCmd = false;
						%>{"error": "<%= "Missing parameter event"%>"},
{"msg": "<%="event=["+ Event.EVENT_CHANGE_STATE+"|"+Event.EVENT_MODIFY_STATE+"|"+Event.EVENT_GET_STATE+"|"+Event.EVENT_GET_STATEMACHINE+"|"+Event.EVENT_CHANGE_STATE+"]"%>"}<%
				}
				
				String operationType = request.getParameter("operation");
				if(operationType != null && event != null) {
						Operation operation = event.addOperation(operationType);
						if(operationType.equals(Event.SET_CONTENT)) {
								String cellId = request.getParameter(Cell.ID);
								if(cellId != null && cellId.length() > 0) {
										operation.addParameter(Cell.ID, cellId);
								} else {
										runCmd = false;
										%>{"error", "<%= "Missing parameter id"%>"},
{"msg", "<%="id={cellId} mandatory for "+Event.SET_CONTENT %>"}<%
								}	
								String resource = request.getParameter(Cell.RESOURCE);
								if(resource != null && resource.length() > 0) {
										operation.addParameter(Cell.RESOURCE, resource);
								} else {
										runCmd = false;
										%>{"error", "<%= "Missing parameter resource"%>"},
{"msg", "<%="resource={cellResource} mandatory for "+Event.SET_CONTENT%>"}<%
								}	
						} else if(operationType.equals(Event.RESIZE_AND_MOVE)) {
								String cellId = request.getParameter(Cell.ID);
								if(cellId != null && cellId.length() > 0) {
										operation.addParameter(Cell.ID, cellId);
								} else {
										runCmd = false;
										%>{"error", "<%= "Missing parameter id"%>"},
{"msg", "<%="id={cellId} mandatory for "+Event.RESIZE_AND_MOVE %>"}<%
								}	
								boolean atLeastOne = false;
								String width = request.getParameter(Cell.WIDTH);
								if(width != null && width.length() > 0) {
										operation.addParameter(Cell.WIDTH, width);
										atLeastOne = true;
								} 
								String height = request.getParameter(Cell.HEIGHT);
								if(height != null && height.length() > 0) {
										operation.addParameter(Cell.HEIGHT, height);
										atLeastOne = true;
								} 
								String xPosition = request.getParameter(Cell.X_POSITION);
								if(xPosition != null && xPosition.length() > 0) {
										operation.addParameter(Cell.X_POSITION, xPosition);
										atLeastOne = true;
								} 
								String yPosition = request.getParameter(Cell.Y_POSITION);
								if(yPosition != null && yPosition.length() > 0) {
										operation.addParameter(Cell.Y_POSITION, yPosition);
										atLeastOne = true;
								} 
								String zIndex = request.getParameter(Cell.Z_INDEX);
								if(zIndex != null && zIndex.length() > 0) {
										operation.addParameter(Cell.Z_INDEX, zIndex);
										atLeastOne = true;
								} 
								if(!atLeastOne) {
										runCmd = false;
										%>{"error", "<%= "Missing parameter."%>"},
{"msg", "<%="width={cellWidth} optional for "+Event.RESIZE_AND_MOVE
													+", height={cellHeight} optional for "+Event.RESIZE_AND_MOVE
													+", xPosition={cellXPosition} optional for "+Event.RESIZE_AND_MOVE
													+", yPosition={cellYPosition} optional for "+Event.RESIZE_AND_MOVE
													+", zIndex={cellZIndex} optional for "+Event.RESIZE_AND_MOVE %>"}<%
								}
						} else if(operationType.equals(Event.RELOAD) || operationType.equals(Event.MUTE) || operationType.equals(Event.UNMUTE)) {
								String cellId = request.getParameter(Cell.ID);
								if(cellId != null && cellId.length() > 0) {
										operation.addParameter(Cell.ID, cellId);
								} else {
										runCmd = false;
										%>{"error", "<%= "Missing parameter id"%>"},
{"msg", "<%="id={cellId} mandatory for "+Event.RELOAD+", "+Event.MUTE+" and "+Event.UNMUTE %>"}<%
								}
						} else if(operationType.equals(Event.SET_STATEMACHINE)) { 
								String url = request.getParameter(StateMachine.STATEMACHINE_URL);
								if(url != null && url.length() > 0) {
										operation.addParameter(StateMachine.STATEMACHINE_URL, url);
								} else {
										runCmd = false;
										%>{"error", "<%= "Missing parameter url"%>"},
{"msg", "<%="url={url} mandatory for "+Event.SET_STATEMACHINE %>"}<%
								}									
						} else {
								runCmd = false;
								%>{"error", "<%= "Operation type not supported"%>"},
{"msg", "<%="operation=["+Event.SET_CONTENT+"|"+Event.RESIZE_AND_MOVE+"|"+Event.RELOAD+"|"+Event.MUTE+"|"+Event.UNMUTE+"] mandatory for "+ Event.EVENT_MODIFY_STATE+" and optional for "+ Event.EVENT_CHANGE_STATE%>"}<%
						}		
				} else if(mustHaveOperation) {
						runCmd = false;
						%>{"error", "<%= "Missing operation"%>"},
{"msg", "<%="operation=["+Event.SET_CONTENT+"|"+Event.RESIZE_AND_MOVE+"|"+Event.RELOAD+"|"+Event.MUTE+"|"+Event.UNMUTE+"] mandatory for "+ Event.EVENT_MODIFY_STATE+" and optional for "+ Event.EVENT_CHANGE_STATE%>"}<%
				}	
				
				if(runCmd) {
						EventQueue.getInstance().add(event);
				}
		} else {
				%>{"error", "<%= "Missing parameter"%>"},
{"msg", "<%= "event=["+ Event.EVENT_CHANGE_STATE+"|"+Event.EVENT_MODIFY_STATE+"|"+Event.EVENT_GET_STATE+"|"+Event.EVENT_GET_STATEMACHINE+"|"+Event.EVENT_CONFIGURE+"]" 
								+", target={targetState} mandatory for "+ Event.EVENT_CHANGE_STATE
							+", operation=["+Event.SET_CONTENT+"|"+Event.RESIZE_AND_MOVE+"|"+Event.RELOAD+"|"+Event.MUTE+"|"+Event.UNMUTE+"] mandatory for "+ Event.EVENT_MODIFY_STATE+" and optional for "+ Event.EVENT_CHANGE_STATE
							+", id={cellId} mandatory for "+Event.SET_CONTENT+", "+Event.RESIZE_AND_MOVE+", "+Event.RELOAD+", "+Event.MUTE+" and "+Event.UNMUTE
							+", resource={cellResource} mandatory for "+Event.SET_CONTENT
							+", width={cellWidth} optional for "+Event.RESIZE_AND_MOVE
							+", height={cellHeight} optional for "+Event.RESIZE_AND_MOVE
							+", xPosition={cellXPosition} optional for "+Event.RESIZE_AND_MOVE
							+", yPosition={cellYPosition} optional for "+Event.RESIZE_AND_MOVE
							+", zIndex={cellZIndex} optional for "+Event.RESIZE_AND_MOVE %>"}"
<%} %>]