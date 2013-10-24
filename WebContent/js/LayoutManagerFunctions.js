// public
function muteVirtualScreen(virtualscreenId) {
    // Assumes that the broadcast virtualscreen is the first frame and contains the mute and unMute-functions
	top.frames[0].mute();
}

//public
function unMuteVirtualScreen(virtualscreenId) {
    // Assumes that the broadcast virtualscreen is the first frame and contains the mute and unMute-functions
	// if virtualscreenId == 'broadcast'
	top.frames[0].unMute();
}

function setStateMachine(stateMachine) {
	if(statemachine.lenght > 0) {
		var stateObj = { foo: "bar" };
		history.pushState(stateObj, "page 2", "index.jsp?browser="+browser+"&session="+session+"&statemachine="+stateMachine+"&uuid_key="+uuid_key);
	} 
}
// usage unknown
function debug(divName) {
    iframeName = createIframeName(divName);
    frames[iframeName].onclick = 'alert("test");';
}

// private
function createIframeName(divName) {
    return divName + '_iframe';
}

var appendurl = '&browser='+browser+'&proxyhost='+proxyhost;
function setAttribute(attributeName, attributeValue) {
	if(attributeName == 'browser')
		browser = attributeValue;
	if(appendurl.indexOf(attributeName) == -1) {
	    appendurl =appendurl+'&'+attributeName+'='+attributeValue;
	} else {
		var start = appendurl.indexOf(attributeName);
	    var temp_end = appendurl.substring(start, appendurl.length).indexOf("&");
	    if(temp_end == -1)
		    temp_end = appendurl.length;
	    else
	        temp_end = temp_end + start;
	    oldValueLength = temp_end - start - attributeName.length - 1;
	    appendurl = appendurl.substring(0, start +attributeName.length+1)+attributeValue+appendurl.substring(start+attributeName.length+1+oldValueLength, appendurl.length);
	}
	if(statemachine.lenght > 0) {
		var stateObj = { foo: "bar" };
		history.pushState(stateObj, "page 2", "index.jsp?browser="+browser+"&session="+session+"&statemachine="+stateMachine+"&uuid_key="+uuid_key);
	} else {
		var stateObj = { foo: "bar" };
		history.pushState(stateObj, "page 2", "index.jsp?browser="+browser+"&session="+session+"&uuid_key="+uuid_key);
	}
}

// public
// argument 1 an arbitrary string which can be used as an handle for later access to the virtualscreen
// arguments 2-5 are relative (a float from 0 to 1) and optional (i.e. can be left out in the function call)
// the origo is in the upper left corner of the window
// The border width can be set by passing the sixth argument (a float) 
function addVirtualScreen(divName, newWidth, newHeight, xPosition, yPosition, zIndex, borderWeight, content) {
    var newdiv = document.createElement('div');
    newdiv.setAttribute("id", divName);
    newdiv.setAttribute("style","position:absolute;background-color:transparent;z-index:"+zIndex);
    var iframeName = createIframeName(divName);

	if(content.indexOf('?') == -1) {
		content = content+'?session='+session+'&id='+uuid_key+appendurl;
	} else {
		content = content+'&session='+session+'&id='+uuid_key+appendurl;
	}
	var innerHtmlString = '<iframe style="background-color:transparent;overflow:hidden;" name="' + iframeName + '" id="' + iframeName + '" width="100%" height="100%" src="'+content+'" frameborder="0" vspace="0" hspace="0" marginwidth="0" marginheight="0"></iframe>';
  
    var verticalEnlargementFactor   = (1 / newHeight); // Smaller virtualscreens should have a proportionally bigger border in percent
    var horizontalEnlargementFactor = (1 / newWidth); // Horizontal aspect ratio is shrunk according to aspect ratio
    var verticalBorderSize   = verticalEnlargementFactor   * borderWeight;
    var horizontalBorderSize = horizontalEnlargementFactor * borderWeight;    
    var verticalOffsetPercent   = (verticalBorderSize * 100)   + '%';
    var horizontalOffsetPercent = (horizontalBorderSize * 100) + '%';
    var divWidthPercent  = (1 - (horizontalBorderSize * 2)) * 100  + '%';
    var divHeightPercent = (1 - (verticalBorderSize * 2)) * 100  + '%';
    innerHtmlString = '<div style="position:absolute; top: ' + verticalOffsetPercent + '; left: ' + horizontalOffsetPercent + '; width: ' + divWidthPercent + '; height: ' + divHeightPercent + '">' + innerHtmlString + '</div>';
    newdiv.innerHTML = innerHtmlString;
    document.body.appendChild(newdiv);
    //setVirtualScreenFrameHTMLContents(iframeName, '&nbsp;');

    if(typeof newWidth != 'undefined' ) {
        setVirtualScreenWidth(divName, newWidth);
    }
    if(typeof newHeight != 'undefined' ) {
        setVirtualScreenHeight(divName, newHeight);
    }
    if(typeof xPosition != 'undefined' ) {
        setVirtualScreenXPosition(divName, xPosition);
    }
    if(typeof yPosition != 'undefined' ) {
        setVirtualScreenYPosition(divName, yPosition);
    }
}

// public
function getAspectRatio() {
    return (getFullWidth() / getFullHeight());
}

// private
function setVirtualScreenFrameHTMLContents(divName, newHTMLContent) {
    iframeObject = document.getElementById(divName);
    iframeDoc = iframeObject.contentDocument;
    if (iframeDoc == undefined || iframeDoc == null) { // This is for cross-browser compatibility
        iframeDoc = testFrame.contentWindow.document;
    }
    iframeDoc.open();
    iframeDoc.write(newHTMLContent);
    iframeDoc.close();     
}

function resizeAndMove(divName, newWidth, newHeight, newXPosition, newYPosition, newZIndex, transitionTime) {
    var oldWidth = getVirtualScreenWidth(divName);
    var oldHeight = getVirtualScreenHeight(divName);
    var oldXPosition = getVirtualScreenXPosition(divName);
    var oldYPosition = getVirtualScreenYPosition(divName);
    var framesPerSecond = 30;
    // ---- no editable parameters below ----

    var numberOfIncrements = (framesPerSecond / 1000) * transitionTime;
	var widthFactor, heightFactor, xDirection, yDirection;
    if(oldWidth < newWidth) {
        widthFactor = 1; // increment virtualscreen width
    } else {
        widthFactor = -1; // decrement virtualscreen width
    }
    if(oldHeight < newHeight) {
    	heightFactor = 1; // increment virtualscreen height
    } else {
    	heightFactor = -1; // decrement virtualscreen height
    }
    if(oldXPosition > newXPosition) {
    	xDirection = -1;
    } else {
    	xDirection = 1;
    }
    if(oldYPosition > newYPosition) {
    	yDirection = -1; 
    } else {
    	yDirection = 1;
    }

    var widthDifference = Math.abs((newWidth - oldWidth));
    var heightDifference = Math.abs((newHeight - oldHeight));
    var xPositionDifference = Math.abs((newXPosition - oldXPosition));
    var yPositionDifference = Math.abs((newYPosition - oldYPosition));
    
    var currentWidth;
    var currentHeight;
    var currentXPosition;
    var currentYPosition;
    var widthIncrement;
    var heightIncrement;
    var xPositionMovement;
    var yPositionMovement;

    var transitionTimeIncrement = transitionTime / numberOfIncrements;
	var i;
    for (i = 1; i <= numberOfIncrements; i++) {
        linearTransitionStep = i / numberOfIncrements;
        weightedTransitionStep = weightTransitionStep(linearTransitionStep);

        widthIncrement  = widthDifference * weightedTransitionStep;
        heightIncrement = heightDifference * weightedTransitionStep;
        xPositionMovement  = xPositionDifference * weightedTransitionStep;
        yPositionMovement = yPositionDifference * weightedTransitionStep;

        currentWidth  = oldWidth  + (widthIncrement  * widthFactor);
        currentHeight = oldHeight + (heightIncrement * heightFactor);
        currentXPosition = oldXPosition + (xPositionMovement * xDirection);
        currentYPosition = oldYPosition + (yPositionMovement * yDirection);
        
        setTimeout("setVirtualScreenXPosition('"+divName+"', "+currentXPosition+")", transitionTimeIncrement * i);
        setTimeout("setVirtualScreenYPosition('"+divName+"', "+currentYPosition+")", transitionTimeIncrement * i);
        setTimeout("setVirtualScreenzIndex('"+divName+"', "+newZIndex+")", transitionTimeIncrement * i);
        setTimeout("setVirtualScreenWidth( '"+divName+"', "+currentWidth+")", transitionTimeIncrement * i);
        setTimeout("setVirtualScreenHeight('"+divName+"', "+currentHeight+")", transitionTimeIncrement * i);
    }
}

// public
function setVirtualScreenDimensions(divName, newWidth, newHeight) {
    var oldWidth = getVirtualScreenWidth(divName);
    var oldHeight = getVirtualScreenHeight(divName);
   
    var transitionTime = 1000; // ms
    var framesPerSecond = 30;
    // ---- no editable parameters below ----

    var numberOfIncrements = (framesPerSecond / 1000) * transitionTime;
	var widthFactor, heightFactor;
    if(oldWidth < newWidth) {
        widthFactor = 1; // increment virtualscreen width
    } else {
        widthFactor = -1; // decrement virtualscreen width
    }
    if(oldHeight < newHeight) {
        heightFactor = 1; // increment virtualscreen height
    } else {
        heightFactor = -1; // decrement virtualscreen height
    }

    var widthDifference = Math.abs((newWidth - oldWidth));
    var heightDifference = Math.abs((newHeight - oldHeight));
    var currentWidth  = oldWidth;
    var currentHeight = oldHeight;
    var transitionTimeIncrement = transitionTime / numberOfIncrements;
    var widthIncrement;
    var heightIncrement;
    var i;
    for (i = 1; i <= numberOfIncrements; i++) {
        linearTransitionStep = i / numberOfIncrements;
        weightedTransitionStep = weightTransitionStep(linearTransitionStep);
        widthIncrement  = widthDifference * weightedTransitionStep;
        heightIncrement = heightDifference * weightedTransitionStep;
        currentWidth  = oldWidth  + (widthIncrement  * widthFactor);
        currentHeight = oldHeight + (heightIncrement * heightFactor);
        setTimeout("setVirtualScreenWidth( '"+divName+"', "+currentWidth+")", transitionTimeIncrement * i);
        setTimeout("setVirtualScreenHeight('"+divName+"', "+currentHeight+")", transitionTimeIncrement * i);
    }
}

// private
function weightTransitionStep(transitionStep) {
    // First quarter of the sinus-curve
    return Math.sin(0.5 * Math.PI * transitionStep);
}

//private
function setVirtualScreenWidth(divName, newWidth) {
    var div = document.getElementById(divName);
    div.style.width = 100 * newWidth + "%";
}

//private
function setVirtualScreenHeight(divName, newHeight) {
    var div = document.getElementById(divName);
    div.style.height = 100 * newHeight + "%";
}

//private
function getVirtualScreenWidth(divName) {
    var div = document.getElementById(divName);
    var divWidth = div.style.width;

    if(divWidth.substr(-1) == "%") {
        return (parseInt(divWidth) / 100);
    } else if(divWidth.substr(-2) == "px") {
        return (parseInt(divWidth) / getFullWidth());
    } else {
        return divWidth;
    }
}

//private
function getVirtualScreenHeight(divName) {

    var div = document.getElementById(divName);
    var divHeight = div.style.height;

    if(divHeight.substr(-1) == "%") {
        return (parseInt(divHeight) / 100);
    } else if(divHeight.substr(-2) == "px") {
        return (parseInt(divHeight) / getFullHeight());
    } else {
        return divHeight;
    }
}

//private
function getFullWidth() {
    return window.innerWidth;
}

//private
function getFullHeight() {
    return window.innerHeight;
}

// private
function setVirtualScreenXPosition(divName, xPosition) {
    var div = document.getElementById(divName);
    div.style.left=(100*xPosition+"%");
}

//private
function getVirtualScreenXPosition(divName) {
    var div = document.getElementById(divName);
    var divXPosition = div.style.left;
    if(divXPosition.substr(-1) == "%") {
        return (parseInt(divXPosition) / 100);
    } else if(divXPosition.substr(-2) == "px") {
        return (parseInt(divXPosition) / getFullWidth());
    } else {
        return divXPosition;
    }
}

// private
function setVirtualScreenYPosition(divName, yPosition) {
    var div = document.getElementById(divName);
    div.style.top=(100*yPosition)+"%";
}

//private
function getVirtualScreenYPosition(divName) {
    var div = document.getElementById(divName);
    var divYPosition = div.style.top;
    if(divYPosition.substr(-1) == "%") {
        return (parseInt(divYPosition) / 100);
    } else if(divYPosition.substr(-2) == "px") {
        return (parseInt(divYPosition) / getFullHeight());
    } else {
        return divYPosition;
    }
}

//private
function setVirtualScreenzIndex(divName, zIndex) {
    var div = document.getElementById(divName);
    div.style.zIndex=zIndex;
}

// public
function moveVirtualScreen(divName, xPosition, yPosition, zIndex) {
    setVirtualScreenXPosition(divName, xPosition);
    setVirtualScreenYPosition(divName, yPosition);
    setVirtualScreenzIndex(divName, zIndex);
}

// public
function setVirtualScreenContents(divName, newContent) {
    var div = document.getElementById(divName);
    var iframeName = divName + '_iframe';
    setVirtualScreenFrameHTMLContents(iframeName, newContent);
}

// public
function setVirtualScreenContentSrc(divName, newContentSrc) {
    var iframeName = divName + '_iframe';
    iframeObject = document.getElementById(iframeName);
    if(newContentSrc.indexOf('?') == -1) {
		iframeObject.src = newContentSrc+'?session='+session+'&id='+uuid_key+appendurl;
	} else {
		iframeObject.src = newContentSrc+'&session='+session+'&id='+uuid_key+appendurl;
    }
}

// public
function removeVirtualScreen(divName) {
	var olddiv = document.getElementById(divName);
	document.body.removeChild(olddiv);
}

