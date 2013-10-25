<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="shortcut icon" href="images/LM.gif" type="image/gif" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Dynamic display partitioning</title>
<style>
html, body, div {
    width: 100%; height: 100%; margin: 0; padding: 0;
    overflow:hidden;
}
<% String browser = (request.getParameter("browser")!=null)?request.getParameter("browser"):"display";%>
body {
<% if (browser.equals("mobile")) 
		out.print("background-color:black;");
	else
		out.print("background-image: url('images/background.png');");
%>
}
</style>
<script type='text/javascript'>
var requestIP = '<%= request.getRemoteAddr()%>';
var browser = '<%= browser%>';
var session = '<%= (request.getParameter("session")!=null)?request.getParameter("session"):java.util.UUID.randomUUID().toString()%>';
var statemachine = '<%= request.getParameter("statemachine")%>';
var method = '<%= (request.getParameter("method")!=null)?request.getParameter("method"):"default"%>';
var uuid_key = '<%= java.util.UUID.randomUUID().toString()%>';
var uuid_key2 = '<%= request.getParameter("uuid_key")%>';
if(uuid_key2 != 'null' && uuid_key2.length != 0)
	uuid_key = uuid_key2;
var proxyhost = document.location.host;
</script>
<script type='text/javascript' src='dwr/engine.js'> </script>
<script type='text/javascript' src='dwr/interface/LayoutEventsHandler.js'> </script>
<script type='text/javascript' src='dwr/interface/Control.js'> </script>
<script type='text/javascript' src='js/LayoutManagerFunctions.js'> </script>
<script type='text/javascript'>
var lastTimestamp = new Date().getTime();
var connectionBroken = false;
//This function is meant to monitor the dwr channel and reload the page incase of break in the channel
function keepAlive() {
    top.LayoutEventsHandler.isAlive(uuid_key, session, browser, 
    {
     	timeout:10000,
        errorHandler:function(message) { connectionBroken = true;console.log('noConnection'); }
    });

}

function keepAliveResponse(sessionOpen) {
	console.log('keepAliveResponse '+sessionOpen);
    if(sessionOpen == "reload" || sessionOpen == "state-session")    {
        document.location.href="index.jsp?browser="+browser+"&session="+session+"&uuid_key="+uuid_key;
    	connectionBroken = false;
    }
    lastTimestamp = new Date().getTime();
}

function doOnload() {
	dwr.engine.setActiveReverseAjax(true);
	LayoutEventsHandler.initSession(uuid_key, browser, requestIP, method, getFullWidth(), getFullHeight(), session, statemachine);
	setInterval('keepAlive()', 30000);
	dwr.engine.setErrorHandler(function(message,error) { console.debug(message,error);});
}

/**
 * Statemachine control interface.
 */
function receiveMessage(event) {
	Control.processEvent(event.data, event.origin);
}
window.addEventListener('message', receiveMessage, false);

</script>
</head>

<body onLoad="doOnload()" onunload="dwr.engine.setNotifyServerOnPageUnload(true)">
</body>
</html>
