// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2, build R23)
// Generated source version: 1.1.2

package it.cnr.icar.eric.common.jaxrpc.notificationListener.server;

import com.sun.xml.rpc.client.BasicService;
import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.soap.*;
import com.sun.xml.rpc.encoding.literal.*;
import javax.xml.rpc.encoding.*;
import javax.xml.namespace.QName;

public class NotificationListenerSOAPService_SerializerRegistry implements SerializerConstants {
    public NotificationListenerSOAPService_SerializerRegistry() {
    }
    
    public TypeMappingRegistry getRegistry() {
        
        TypeMappingRegistry registry = BasicService.createStandardTypeMappingRegistry();
        @SuppressWarnings("unused")
		TypeMapping mapping12 = registry.getTypeMapping(SOAP12Constants.NS_SOAP_ENCODING);
        @SuppressWarnings("unused")
		TypeMapping mapping = registry.getTypeMapping(SOAPConstants.NS_SOAP_ENCODING);
        TypeMapping mapping2 = registry.getTypeMapping("");
        {
            QName type = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ObjectRefsNotification");
            CombinedSerializer serializer = new LiteralFragmentSerializer(type, NOT_NULLABLE, "");
            registerSerializer(mapping2,javax.xml.soap.SOAPElement.class, type, serializer);
        }
        {
            QName type = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ObjectsNotificationType");
            CombinedSerializer serializer = new LiteralFragmentSerializer(type, NOT_NULLABLE, "");
            registerSerializer(mapping2,javax.xml.soap.SOAPElement.class, type, serializer);
        }
        return registry;
    }
    
    private static void registerSerializer(TypeMapping mapping, @SuppressWarnings("rawtypes") Class javaType, QName xmlType,
        Serializer ser) {
        mapping.register(javaType, xmlType, new SingletonSerializerFactory(ser),
            new SingletonDeserializerFactory((Deserializer)ser));
    }
    
}
