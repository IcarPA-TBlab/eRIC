// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, Build R1)
// Generated source version: 1.1.3

package it.cnr.icar.eric.common.jaxrpc.registry.server;

public interface LifeCycleManagerPortType extends java.rmi.Remote {
    public javax.xml.soap.SOAPElement approveObjects(javax.xml.soap.SOAPElement partApproveObjectsRequest) throws 
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement deprecateObjects(javax.xml.soap.SOAPElement partDeprecateObjectsRequest) throws 
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement undeprecateObjects(javax.xml.soap.SOAPElement partUndeprecateObjectsRequest) throws 
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement removeObjects(javax.xml.soap.SOAPElement partRemoveObjectsRequest) throws 
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement submitObjects(javax.xml.soap.SOAPElement partSubmitObjectsRequest) throws 
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement updateObjects(javax.xml.soap.SOAPElement partUpdateObjectsRequest) throws 
         java.rmi.RemoteException;
}
