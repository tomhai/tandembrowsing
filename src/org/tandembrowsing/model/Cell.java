package org.tandembrowsing.model;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.w3c.dom.Element;


public class Cell {

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
		
	public Cell(String id, String resource, String browser, float width, float height, float xPosition, float yPosition) {
		this(id, resource, browser, width, height, xPosition, yPosition, 0, 0, false);
	}
		
	public Cell(String id, String resource, String browser, float width, float height, float xPosition, float yPosition, int zIndex, float border, boolean resizable) {
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
		// store cell in db here
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
	
	public Cell(String id, String resource, String browser, float width, float height, float xPosition, float yPosition, int zIndex, float border, boolean resizable, String session_id, String user_id, String lease_id) {
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
	
	private Cell() {}
	
	// a copy constructor
	public Cell(Cell cell) {
		this(cell.id, cell.resource, cell.browser, cell.width, cell.height, cell.xPosition, cell.yPosition, cell.zIndex, cell.border, cell.resizable, cell.session_id, cell.user_id, cell.lease_id);
	}
	
	public static Cell parseCell(Element cell) throws ParsingException {
		String id = cell.getAttribute("id");
		if(id == null || id.length() == 0)
			throw new ParsingException("id is invalid " +id);
		String resource = cell.getAttribute("resource");
		if(resource == null || resource.length() == 0)
			throw new ParsingException("resource is invalid " +resource);
		String browser = cell.getAttribute("browser");
		if(browser == null || browser.length() == 0)
			browser = null;
		float width = Float.parseFloat(cell.getAttribute("width"));
		float height = Float.parseFloat(cell.getAttribute("height"));
		float xPosition = Float.parseFloat(cell.getAttribute("xPosition"));
		float yPosition = Float.parseFloat(cell.getAttribute("yPosition"));
		int zIndex = cell.getAttribute("zIndex").length() == 0 ? 0 : Integer.parseInt(cell.getAttribute("zIndex"));
		float border = cell.getAttribute("border").length() == 0 ? 0 : Float.parseFloat(cell.getAttribute("border"));
		boolean resizable = cell.getAttribute("resizable").length() == 0 ? false : Boolean.parseBoolean(cell.getAttribute("resizable"));
		return new Cell(id, resource, browser, width, height, xPosition, yPosition, zIndex, border, resizable);
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
	
	public boolean isBigger(Cell cell) {
		int cr = Float.compare(this.getSize(),cell.getSize());
		if(cr > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isSmaller(Cell cell) {
		int cr = Float.compare(this.getSize(),cell.getSize());
		if(cr < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasEqualDimensions(Cell cell) {
		if(this.width == cell.getWidth() && this.height == cell.getHeight() && this.xPosition == cell.getXPosition() && this.yPosition == cell.getYPosition() && this.zIndex == cell.getZIndex() && this.border == cell.getBorder()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isCloserToOrigo(Cell cell) {
		if(this.getXPosition() + this.getYPosition() < cell.getXPosition() + cell.getYPosition())
			return true;
		else
			return false;
	}
		
	public double distanceFrom(Cell cell) {
		return Math.hypot(cell.getXPosition() - this.getXPosition(), cell.getYPosition() - this.getYPosition()); 
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if(((Cell)obj).getId() == this.getId())
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
		JSONObject cell=new JSONObject();	
		cell.put(ID, id);
		cell.put(RESOURCE, resource);
		cell.put(BROWSER, browser);
		cell.put(WIDTH, width);
		cell.put(HEIGHT, height);
		cell.put(X_POSITION, xPosition);
		cell.put(Y_POSITION, yPosition);
		cell.put(Z_INDEX, zIndex);
		cell.put(BORDER, border);
		cell.put(RESIZABLE, resizable);
		return cell;
	}

	public void copyDimensions(Cell cell) {
		this.width = cell.width;
		this.height = cell.height;
		this.xPosition = cell.xPosition;
		this.yPosition = cell.yPosition;
		this.zIndex = cell.zIndex;		
	}
}
