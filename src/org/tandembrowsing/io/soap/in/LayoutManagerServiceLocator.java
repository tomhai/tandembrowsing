/**
 * LayoutManagerServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tandembrowsing.io.soap.in;

public class LayoutManagerServiceLocator extends org.apache.axis.client.Service implements org.tandembrowsing.io.soap.in.LayoutManagerService {

    public LayoutManagerServiceLocator() {
    }


    public LayoutManagerServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public LayoutManagerServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for LayoutManager
    private java.lang.String LayoutManager_address;

    public java.lang.String getLayoutManagerAddress() {
        return LayoutManager_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String LayoutManagerWSDDServiceName = "LayoutManager";

    public java.lang.String getLayoutManagerWSDDServiceName() {
        return LayoutManagerWSDDServiceName;
    }

    public void setLayoutManagerWSDDServiceName(java.lang.String name) {
        LayoutManagerWSDDServiceName = name;
    }

    public org.tandembrowsing.io.soap.in.LayoutManager getLayoutManager() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(LayoutManager_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getLayoutManager(endpoint);
    }

    public org.tandembrowsing.io.soap.in.LayoutManager getLayoutManager(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.tandembrowsing.io.soap.in.LayoutManagerSoapBindingStub _stub = new org.tandembrowsing.io.soap.in.LayoutManagerSoapBindingStub(portAddress, this);
            _stub.setPortName(getLayoutManagerWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setLayoutManagerEndpointAddress(java.lang.String address) {
        LayoutManager_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.tandembrowsing.io.soap.in.LayoutManager.class.isAssignableFrom(serviceEndpointInterface)) {
                org.tandembrowsing.io.soap.in.LayoutManagerSoapBindingStub _stub = new org.tandembrowsing.io.soap.in.LayoutManagerSoapBindingStub(new java.net.URL(LayoutManager_address), this);
                _stub.setPortName(getLayoutManagerWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("LayoutManager".equals(inputPortName)) {
            return getLayoutManager();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.tandembrowsing.org", "LayoutManagerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.tandembrowsing.org", "LayoutManager"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("LayoutManager".equals(portName)) {
            setLayoutManagerEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
