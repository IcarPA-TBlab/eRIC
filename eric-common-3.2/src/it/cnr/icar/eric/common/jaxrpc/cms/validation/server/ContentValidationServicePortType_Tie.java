// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, Build R1)
// Generated source version: 1.1.3

package it.cnr.icar.eric.common.jaxrpc.cms.validation.server;

import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.soap.streaming.*;
import com.sun.xml.rpc.soap.message.*;
import javax.xml.namespace.QName;
import java.lang.reflect.*;
//import java.lang.Class;
import com.sun.xml.rpc.server.*;

public class ContentValidationServicePortType_Tie
    extends com.sun.xml.rpc.server.TieBase implements SerializerConstants {
    
    
    
    public ContentValidationServicePortType_Tie() throws Exception {
        super(new it.cnr.icar.eric.common.jaxrpc.cms.validation.server.ContentValidationServiceSOAPService_SerializerRegistry().getRegistry());
        initialize(internalTypeMappingRegistry);
    }
    
    /*
     * This method does the actual method invocation for operation: validateContent
     */
    private void invoke_validateContent(StreamingHandlerState state) throws Exception {
        
        javax.xml.soap.SOAPElement mySOAPElement = null;
        Object mySOAPElementObj =
            state.getRequest().getBody().getValue();
        
        if (mySOAPElementObj instanceof SOAPDeserializationState) {
            mySOAPElement = (javax.xml.soap.SOAPElement)((SOAPDeserializationState)mySOAPElementObj).getInstance();
        } else {
            mySOAPElement = (javax.xml.soap.SOAPElement)mySOAPElementObj;
        }
        
        try {
            javax.xml.soap.SOAPElement partValidateContentResponse = ((it.cnr.icar.eric.common.jaxrpc.cms.validation.server.ContentValidationServicePortType) getTarget()).validateContent(mySOAPElement);
            
            @SuppressWarnings("unused")
			SOAPHeaderBlockInfo headerInfo;
            
            
            javax.xml.soap.SOAPElement _response = partValidateContentResponse;
            SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns1_validateContent_ValidateContentResponse_QNAME);
            bodyBlock.setValue(_response);
            bodyBlock.setSerializer(ns1_ns1_ValidateContentResponse_TYPE_QNAME_Serializer);
            state.getResponse().setBody(bodyBlock);
        } catch (javax.xml.rpc.soap.SOAPFaultException e) {
            SOAPFaultInfo fault = new SOAPFaultInfo(e.getFaultCode(),
                e.getFaultString(), e.getFaultActor(), e.getDetail());
            SOAPBlockInfo faultBlock = new SOAPBlockInfo(com.sun.xml.rpc.encoding.soap.SOAPConstants.QNAME_SOAP_FAULT);
            faultBlock.setValue(fault);
            faultBlock.setSerializer(new SOAPFaultInfoSerializer(false, e.getDetail()==null));
            state.getResponse().setBody(faultBlock);
            state.getResponse().setFailure(true);
        }
    }
    
    /*
     * This method must determine the opcode of the operation that has been invoked.
     */
    protected void peekFirstBodyElement(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingHandlerState state) throws Exception {
        if (bodyReader.getName().equals(ns1_validateContent_ValidateContentRequest_QNAME)) {
            state.getRequest().setOperationCode(validateContent_OPCODE);
        }
        else {
            throw new SOAPProtocolViolationException("soap.operation.unrecognized", bodyReader.getName().toString());
        }
    }
    
    /*
     *  this method deserializes the request/response structure in the body
     */
    protected void readFirstBodyElement(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingHandlerState  state) throws Exception {
        int opcode = state.getRequest().getOperationCode();
        switch (opcode) {
            case validateContent_OPCODE:
                deserialize_validateContent(bodyReader, deserializationContext, state);
                break;
            default:
                throw new SOAPProtocolViolationException("soap.operation.unrecognized", java.lang.Integer.toString(opcode));
        }
    }
    
    
    
    /*
     * This method deserializes the body of the validateContent operation.
     */
    private void deserialize_validateContent(XMLReader bodyReader, SOAPDeserializationContext deserializationContext, StreamingHandlerState state) throws Exception {
        java.lang.Object mySOAPElementObj =
            ns1_ns1_ValidateContentRequest_TYPE_QNAME_Serializer.deserialize(ns1_validateContent_ValidateContentRequest_QNAME,
                bodyReader, deserializationContext);
        
        SOAPBlockInfo bodyBlock = new SOAPBlockInfo(ns1_validateContent_ValidateContentRequest_QNAME);
        bodyBlock.setValue(mySOAPElementObj);
        state.getRequest().setBody(bodyBlock);
    }
    
    
    /*
     * This method must invoke the correct method on the servant based on the opcode.
     */
    protected void processingHook(StreamingHandlerState state) throws Exception {
        switch (state.getRequest().getOperationCode()) {
            case validateContent_OPCODE:
                invoke_validateContent(state);
                break;
            default:
                throw new SOAPProtocolViolationException("soap.operation.unrecognized", java.lang.Integer.toString(state.getRequest().getOperationCode()));
        }
    }
    
    protected java.lang.String getDefaultEnvelopeEncodingStyle() {
        return null;
    }
    
    public java.lang.String getImplicitEnvelopeEncodingStyle() {
        return "";
    }
    
    
    /*
     * This method must determine the opcode of the operation given the QName of the first body element.
     */
    public int getOpcodeForFirstBodyElementName(QName name) {
        if (name == null) {
            return InternalSOAPMessage.NO_OPERATION;
        }
        if (name.equals(ns1_validateContent_ValidateContentRequest_QNAME)) {
            return validateContent_OPCODE;
        }
        return super.getOpcodeForFirstBodyElementName(name);
    }
    
    
    private Method internalGetMethodForOpcode(int opcode) throws ClassNotFoundException, NoSuchMethodException {
        
        Method theMethod = null;
        
        switch(opcode) {
            case validateContent_OPCODE:
                {
                    Class<?>[] carray = { javax.xml.soap.SOAPElement.class };
                    theMethod = (it.cnr.icar.eric.common.jaxrpc.cms.validation.server.ContentValidationServicePortType.class).getMethod("validateContent", carray);
                }
                break;
            
            default:
        }
        return theMethod;
    }
    
    private Method[] methodMap = new Method[1];
    
    /*
     * This method returns the Method Obj for a specified opcode.
     */
    public Method getMethodForOpcode(int opcode) throws ClassNotFoundException, NoSuchMethodException {
         
        if (opcode <= InternalSOAPMessage.NO_OPERATION ) {
            return null;
        }
         
        if (opcode >= 1 ) {
            return null;
        }
         
        if (methodMap[opcode] == null)  {
            methodMap[opcode] = internalGetMethodForOpcode(opcode);
        }
         
        return methodMap[opcode];
    }
    
    /*
     * This method returns an array containing (prefix, nsURI) pairs.
     */
    protected java.lang.String[] getNamespaceDeclarations() {
        return myNamespace_declarations;
    }
    
    /*
     * This method returns an array containing the names of the headers we understand.
     */
    public javax.xml.namespace.QName[] getUnderstoodHeaders() {
        return understoodHeaderNames;
    }
    
    
    protected boolean preHandlingHook(StreamingHandlerState state) throws Exception {
        boolean bool = false;
        bool = super.preHandlingHook(state);
        return bool;
    }
    
    
    
    protected void postEnvelopeReadingHook(StreamingHandlerState state) throws Exception {
        super.postEnvelopeReadingHook(state);
        switch (state.getRequest().getOperationCode()) {
            case validateContent_OPCODE:
                getNonExplicitAttachment(state);
                break;
        }
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void getNonExplicitAttachment(StreamingHandlerState state) throws Exception {
        javax.xml.rpc.handler.soap.SOAPMessageContext smc = state.getMessageContext();
        javax.xml.soap.SOAPMessage message = state.getRequest().getMessage();
        java.util.ArrayList attachments = null;
        for(java.util.Iterator<?> iter = message.getAttachments(); iter.hasNext();) {
            if(attachments == null) {
                attachments = new java.util.ArrayList();
            }
            attachments.add(iter.next());
        }
        smc.setProperty(com.sun.xml.rpc.server.ServerPropertyConstants.GET_ATTACHMENT_PROPERTY, attachments);
    }
    private void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns1_ns1_ValidateContentResponse_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns1_ValidateContentResponse_TYPE_QNAME);
        ns1_ns1_ValidateContentRequest_TYPE_QNAME_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.soap.SOAPElement.class, ns1_ValidateContentRequest_TYPE_QNAME);
    }
    
    @SuppressWarnings("unused")
	private static final javax.xml.namespace.QName portName = new QName("urn:your:urn:goes:here", "ContentValidationServicePort");
    private static final int validateContent_OPCODE = 0;
    private static final javax.xml.namespace.QName ns1_validateContent_ValidateContentRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:cms:3.0", "ValidateContentRequest");
    private static final javax.xml.namespace.QName ns1_ValidateContentRequest_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:cms:3.0", "ValidateContentRequest");
    private CombinedSerializer ns1_ns1_ValidateContentRequest_TYPE_QNAME_Serializer;
    private static final javax.xml.namespace.QName ns1_validateContent_ValidateContentResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:cms:3.0", "ValidateContentResponse");
    private static final javax.xml.namespace.QName ns1_ValidateContentResponse_TYPE_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:cms:3.0", "ValidateContentResponse");
    private CombinedSerializer ns1_ns1_ValidateContentResponse_TYPE_QNAME_Serializer;
    private static final java.lang.String[] myNamespace_declarations =
                                        new java.lang.String[] {
                                            "ns0", "urn:oasis:names:tc:ebxml-regrep:xsd:cms:3.0"
                                        };
    
    private static final QName[] understoodHeaderNames = new QName[] {  };
}
