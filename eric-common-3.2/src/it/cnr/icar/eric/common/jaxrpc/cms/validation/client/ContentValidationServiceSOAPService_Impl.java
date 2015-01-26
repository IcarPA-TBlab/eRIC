// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, Build R1)
// Generated source version: 1.1.3

package it.cnr.icar.eric.common.jaxrpc.cms.validation.client;

import com.sun.xml.rpc.client.ServiceExceptionImpl;
import com.sun.xml.rpc.util.exception.*;
import com.sun.xml.rpc.client.HandlerChainImpl;
import javax.xml.rpc.*;
import javax.xml.namespace.QName;

public class ContentValidationServiceSOAPService_Impl extends com.sun.xml.rpc.client.BasicService implements ContentValidationServiceSOAPService {
    private static final QName serviceName = new QName("urn:your:urn:goes:here", "ContentValidationServiceSOAPService");
    private static final QName ns1_ContentValidationServicePort_QNAME = new QName("urn:your:urn:goes:here", "ContentValidationServicePort");
    @SuppressWarnings("rawtypes")
	private static final Class contentValidationServicePortType_PortClass = it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType.class;
    
    public ContentValidationServiceSOAPService_Impl() {
        super(serviceName, new QName[] {
                        ns1_ContentValidationServicePort_QNAME
                    },
            new it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServiceSOAPService_SerializerRegistry().getRegistry());
        
    }
    
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, @SuppressWarnings("rawtypes") java.lang.Class serviceDefInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (portName.equals(ns1_ContentValidationServicePort_QNAME) &&
                serviceDefInterface.equals(contentValidationServicePortType_PortClass)) {
                return getContentValidationServicePort();
            }
        } catch (Exception e) {
            throw new ServiceExceptionImpl(new LocalizableExceptionAdapter(e));
        }
        return super.getPort(portName, serviceDefInterface);
    }
    
    public java.rmi.Remote getPort(@SuppressWarnings("rawtypes") java.lang.Class serviceDefInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (serviceDefInterface.equals(contentValidationServicePortType_PortClass)) {
                return getContentValidationServicePort();
            }
        } catch (Exception e) {
            throw new ServiceExceptionImpl(new LocalizableExceptionAdapter(e));
        }
        return super.getPort(serviceDefInterface);
    }
    
    public it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType getContentValidationServicePort() {
        java.lang.String[] roles = new java.lang.String[] {};
        HandlerChainImpl handlerChain = new HandlerChainImpl(getHandlerRegistry().getHandlerChain(ns1_ContentValidationServicePort_QNAME));
        handlerChain.setRoles(roles);
        it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType_Stub stub = new it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType_Stub(handlerChain);
        try {
            stub._initialize(super.internalTypeRegistry);
        } catch (JAXRPCException e) {
            throw e;
        } catch (Exception e) {
            throw new JAXRPCException(e.getMessage(), e);
        }
        return stub;
    }
}
