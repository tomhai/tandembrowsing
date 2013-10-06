package org.tandembrowsing.io.soap.out;

public class CallbackProxy implements org.tandembrowsing.io.soap.out.Callback {
  private String _endpoint = null;
  private org.tandembrowsing.io.soap.out.Callback rM = null;
  
  public CallbackProxy() {
    _initCallbackProxy();
  }
  
  public CallbackProxy(String endpoint) {
    _endpoint = endpoint;
    _initCallbackProxy();
  }
  
  private void _initCallbackProxy() {
    try {
      rM = (new org.tandembrowsing.io.soap.out.CallbackServiceLocator()).getCallback();
      if (rM != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)rM)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)rM)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (rM != null)
      ((javax.xml.rpc.Stub)rM)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.tandembrowsing.io.soap.out.Callback getCallback() {
    if (rM == null)
      _initCallbackProxy();
    return rM;
  }
  
  public java.lang.String displayEvent(java.lang.String eventType) throws java.rmi.RemoteException{
    if (rM == null)
      _initCallbackProxy();
    return rM.displayEvent(eventType);
  }
  
  
}