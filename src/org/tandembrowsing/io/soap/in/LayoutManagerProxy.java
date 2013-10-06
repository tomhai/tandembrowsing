package org.tandembrowsing.io.soap.in;

public class LayoutManagerProxy implements org.tandembrowsing.io.soap.in.LayoutManager {
  private String _endpoint = null;
  private org.tandembrowsing.io.soap.in.LayoutManager layoutManager = null;
  
  public LayoutManagerProxy() {
    _initLayoutManagerProxy();
  }
  
  public LayoutManagerProxy(String endpoint) {
    _endpoint = endpoint;
    _initLayoutManagerProxy();
  }
  
  private void _initLayoutManagerProxy() {
    try {
      layoutManager = (new org.tandembrowsing.io.soap.in.LayoutManagerServiceLocator()).getLayoutManager();
      if (layoutManager != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)layoutManager)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)layoutManager)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (layoutManager != null)
      ((javax.xml.rpc.Stub)layoutManager)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.tandembrowsing.io.soap.in.LayoutManager getLayoutManager() {
    if (layoutManager == null)
      _initLayoutManagerProxy();
    return layoutManager;
  }
  
  public java.lang.String processEvent(java.lang.String payload) throws java.rmi.RemoteException{
    if (layoutManager == null)
      _initLayoutManagerProxy();
    return layoutManager.processEvent(payload);
  }
  
}