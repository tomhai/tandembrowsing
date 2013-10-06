/**
 * CallbackService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tandembrowsing.io.soap.out;

public interface CallbackService extends javax.xml.rpc.Service {
    public java.lang.String getCallbackAddress();

    public org.tandembrowsing.io.soap.out.Callback getCallback() throws javax.xml.rpc.ServiceException;

    public org.tandembrowsing.io.soap.out.Callback getCallback(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
