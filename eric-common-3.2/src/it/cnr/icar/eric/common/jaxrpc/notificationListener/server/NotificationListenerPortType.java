// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2, build R23)
// Generated source version: 1.1.2

package it.cnr.icar.eric.common.jaxrpc.notificationListener.server;

public interface NotificationListenerPortType extends java.rmi.Remote {
    public void onObjectRefsNotification(javax.xml.soap.SOAPElement body) throws 
         java.rmi.RemoteException;
    public void onObjectsNotification(javax.xml.soap.SOAPElement body) throws 
         java.rmi.RemoteException;
}
