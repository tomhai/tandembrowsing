package org.tandembrowsing.model;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.w3c.dom.Element;


public class VirtualScreen {

	private String id;
	private String resource;
	private String browser;
	private float width;
	private float height;
	private float xPosition;
	private float yPosition;
	private int zIndex;
	private float border;
	private boolean resizable;
	private String session_id = null;
	private String user_id = null;
	private String lease_id = null; 
	
	public static final String ID = "id";
	public static final String RESOURCE = "resource";
	public static final String BROWSER = "browser";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String X_POSITION = "xPosition";
	public static final String Y_POSITION = "yPosition";
	public static final String Z_INDEX = "zIndex";
	public static final String BORDER = "border";
	public static final String RESIZABLE = "resizable";
	public static final String SESSION_ID = "session_id";
	public static final String USER_ID = "user_id";
	public static final String LEASE_ID = "lease_id";
		
	public VirtualScreen(String id, String resource, String browser, float width, float height, float xPosition, float yPosition) {
		this(id, resource, browser, width, height, xPosition, yPosition, 0, 0, false);
	}
		
	public VirtualScreen(String id, String resource, String browser, float width, float height, float xPosition, float yPosition, int zIndex, float border, boolean resizable) {
		this.id = id;
		this.resource = resource;
		this.browser = browser;
		this.width = width;
		this.height = height;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.zIndex = zIndex;
		this.border = border;
		this.resizable = resizable;
		// store virtualscreen in db here
		if(resource.indexOf("?") != -1) {
			try {
				Map <String, String> urlParams = initMap(resource.substring(resource.indexOf("?") + 1));
				this.session_id = urlParams.get(SESSION_ID);
				this.user_id = urlParams.get(USER_ID);
				this.lease_id = urlParams.get(LEASE_ID);				
			} catch (UnsupportedEncodingException e) {
				// leave them unset
			}
		}
	}
	
	public VirtualScreen(String id, String resource, String browser, float width, float height, float xPosition, float yPosition, int zIndex, float border, boolean resizable, String session_id, String user_id, String lease_id) {
		this.id = id;
		this.resource = resource;
		this.browser = browser;
		this.width = width;
		this.height = height;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.zIndex = zIndex;
		this.border = border;
		this.resizable = resizable;
		this.session_id = session_id;
		this.user_id = user_id;
		this.lease_id = lease_id;				
	}
	
	private VirtualScreen() {}
	
	// a copy constructor
	public VirtualScreen(VirtualScreen virtualscreen) {
		this(virtualscreen.id, virtualscreen.resource, virtualscreen.browser, virtualscreen.width, virtualscreen.height, virtualscreen.xPosition, virtualscreen.yPosition, virtualscreen.zIndex, virtualscreen.border, virtualscreen.resizable, virtualscreen.session_id, virtualscreen.user_id, virtualscreen.lease_id);
	}
	
	public static VirtualScreen parse(Element virtualscreen) throws ParsingException {
		String id = virtualscreen.getAttribute("id");
		if(id == null || id.length() == 0)
			throw new ParsingException("id is invalid " +id);
		String resource = virtualscreen.getAttribute("resource");
		if(resource == null || resource.length() == 0)
			throw new ParsingException("resource is invalid " +resource);
		String browser = virtualscreen.getAttribute("browser");
		if(browser == null || browser.length() == 0)
			browser = null;
		float width = virtualscreen.getAttribute("width").length() == 0 ? 1 : Float.parseFloat(virtualscreen.getAttribute("width"));
		float height = virtualscreen.getAttribute("height").length() == 0 ? 1 : Float.parseFloat(virtualscreen.getAttribute("height"));
		float xPosition = virtualscreen.getAttribute("xPosition").length() == 0 ? 0 :Float.parseFloat(virtualscreen.getAttribute("xPosition"));
		float yPosition = virtualscreen.getAttribute("yPosition").length() == 0 ? 0 :Float.parseFloat(virtualscreen.getAttribute("yPosition"));
		int zIndex = virtualscreen.getAttribute("zIndex").length() == 0 ? 0 : Integer.parseInt(virtualscreen.getAttribute("zIndex"));
		float border = virtualscreen.getAttribute("border").length() == 0 ? 0 : Float.parseFloat(virtualscreen.getAttribute("border"));
		boolean resizable = virtualscreen.getAttribute("resizable").length() == 0 ? false : Boolean.parseBoolean(virtualscreen.getAttribute("resizable"));
		return new VirtualScreen(id, resource, browser, width, height, xPosition, yPosition, zIndex, border, resizable);
	}

	public String getId() {
		return id;
	}	
	public void setId(String id) {
		this.id = id;
	}
	public void setResource(String resource) {
		this.resource = resource;
		if(resource.indexOf("?") != -1) {
			try {
				Map <String, String> urlParams = initMap(resource.substring(resource.indexOf("?") + 1));
				this.session_id = urlParams.get(SESSION_ID);
				this.user_id = urlParams.get(USER_ID);
				this.lease_id = urlParams.get(LEASE_ID);				
			} catch (UnsupportedEncodingException e) {
				// leave them unset
			}
		}
	}
	public String getResource() {
		return resource;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public void appendWidth(float width) {
		this.width = this.width + width;
	}
	public float getWidth() {
		return width;
	}
	public void setHeight(float height) {
		this.height = height;
	}
	public void appendHeight(float height) {
		this.height = this.height + height;
	}
	public float getHeight() {
		return height;
	}
	public void setXPosition(float xPosition) {
		this.xPosition = xPosition;
	}
	public float getXPosition() {
		return xPosition;
	}
	public void setYPosition(float yPosition) {
		this.yPosition = yPosition;
	}
	public float getYPosition() {
		return yPosition;
	}
	
	public float getSize() {
		return this.width*this.height;
	}
	
	public boolean isBigger(VirtualScreen virtualscreen) {
		int cr = Float.compare(this.getSize(),virtualscreen.getSize());
		if(cr > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isSmaller(VirtualScreen virtualscreen) {
		int cr = Float.compare(this.getSize(),virtualscreen.getSize());
		if(cr < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasEqualDimensions(VirtualScreen virtualscreen) {
		if(this.width == virtualscreen.getWidth() && this.height == virtualscreen.getHeight() && this.xPosition == virtualscreen.getXPosition() && this.yPosition == virtualscreen.getYPosition() && this.zIndex == virtualscreen.getZIndex() && this.border == virtualscreen.getBorder()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isCloserToOrigo(VirtualScreen virtualscreen) {
		if(this.getXPosition() + this.getYPosition() < virtualscreen.getXPosition() + virtualscreen.getYPosition())
			return true;
		else
			return false;
	}
		
	public double distanceFrom(VirtualScreen virtualscreen) {
		return Math.hypot(virtualscreen.getXPosition() - this.getXPosition(), virtualscreen.getYPosition() - this.getYPosition()); 
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if(((VirtualScreen)obj).getId() == this.getId())
			return true;
		else
			return false;
	}

	public boolean isHorizontal() {
		if (this.height > this.width)
			return false;
		else
			return true;		
	}

	public void setBorder(float border) {
		this.border = border;
	}

	public float getBorder() {
		return border;
	}
	
	public boolean isBorder() {
		return border > 0;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public int getZIndex() {
		return zIndex;
	}
	
	public String getSession_id() {
		return session_id;
	}

	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getLease_id() {
		return lease_id;
	}

	public void setLease_id(String lease_id) {
		this.lease_id = lease_id;
	}
	
	@Override
	public String toString() {
		return "id="+id+";resource="+resource+";browser="+browser+";width="+width+";height="+height+";xPosition="+xPosition+";yPosition="+yPosition+";zIndex="+zIndex+";border="+border+";resizable="+resizable+";session_id="+session_id+";user_id="+user_id+";lease_id="+lease_id;
	}

	private Map <String,String> initMap(String search) throws UnsupportedEncodingException {
		Map <String,String> parmsMap = new HashMap<String,String>();
		String params[] = search.split("&");
	
		for (String param : params) {
			String temp[] = param.split("=");
			parmsMap.put(temp[0], (temp.length > 1)? java.net.URLDecoder.decode(temp[1], "UTF-8"): null);
	    }
		return parmsMap;
	}

	public JSONObject toJSON() {
		JSONObject virtualscreen=new JSONObject();	
		virtualscreen.put(ID, id);
		virtualscreen.put(RESOURCE, resource);
		virtualscreen.put(BROWSER, browser);
		virtualscreen.put(WIDTH, width);
		virtualscreen.put(HEIGHT, height);
		virtualscreen.put(X_POSITION, xPosition);
		virtualscreen.put(Y_POSITION, yPosition);
		virtualscreen.put(Z_INDEX, zIndex);
		virtualscreen.put(BORDER, border);
		virtualscreen.put(RESIZABLE, resizable);
		return virtualscreen;
	}

	public void copyDimensions(VirtualScreen virtualscreen) {
		this.width = virtualscreen.width;
		this.height = virtualscreen.height;
		this.xPosition = virtualscreen.xPosition;
		this.yPosition = virtualscreen.yPosition;
		this.zIndex = virtualscreen.zIndex;		
	}
}
