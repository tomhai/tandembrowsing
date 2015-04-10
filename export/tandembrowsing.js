function TandemBrowsing(proxyHost, proxyPort, session) {
	var mHost = proxyHost;
	var mPort = '';
	if(proxyPort.length > 0)
		mPort = proxyPort;
	else
		mPort = '8080';
	
	var mSession = '';
	if(session.length > 0)
		mSession = session;
	else
		mSession = gup("session");
		
	
	 // init
	function gup( name ) {
		name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp( regexS );
		var results = regex.exec( window.location.href );
		if( results == null )
			return "";
		else
			return results[1];
	}

	/**
	 * Exposes the public methods outside
	 */
	return {
		/** Adds parameter to a session
		 * 
		 * @param name
		 * @param value
		 * @returns
		 */
		addParameter: 	function(name, value) {
			var event = {'event' : {'type':'manageSession', 'name':'', 'id':'', 'session':mSession, 'operation': []}};					
			event.event.operation.push({'type':'setAttribute', 'parameter' : []});
			event.event.operation[0].parameter.push({'type':'targetKey','name':'session'});
			event.event.operation[0].parameter.push({'type':'targetValue','name':mSession});
			event.event.operation[0].parameter.push({'type':'key','name':name});
			event.event.operation[0].parameter.push({'type':'value','name':value});
			
			parent.postMessage(JSON.stringify(event), 'http://'+mHost+':'+mPort);			
		},
		/** Gets parameter from the session
		 * 
		 * @param name of the attribute
		 * @param callback function
		 * @returns
		 */	
		getParameter: 	function(name, callback) {
			var event = {'event' : {'type':'manageSession', 'name':'', 'id':'', 'session':mSession, 'operation': []}};					
			event.event.operation.push({'type':'getAttribute', 'parameter' : []});
			event.event.operation[0].parameter.push({'name':name});
			parent.postMessage(JSON.stringify(event), 'http://'+mHost+':'+mPort);
			
			function receiveMessage(event) {
				if (event.origin !== 'http://'+mHost+':'+mPort)
					return;
				eventBody = JSON.parse(event.data);
				if(eventBody.event.type =='manageSessionReply' && eventBody.event.operation[0].type == 'getAttribute')
					callback(eventBody.event.operation[0].parameter[0].name);
				else
					console.log('no');
				
				window.removeEventListener("message", receiveMessage);
			}
			window.addEventListener('message', receiveMessage, false);
		},
		/** Invokes change state
		 * 
		 * @param exchange
		 * @param data
		 * @returns
		 */
		changeState:	function(target) {
			var event = {'event' : {'type':'changeState', 'name':target, 'id':'', 'session':mSession, 'operation': []}};					
			parent.postMessage(JSON.stringify(event), 'http://'+mHost+':'+mPort);
		}
	};
};
