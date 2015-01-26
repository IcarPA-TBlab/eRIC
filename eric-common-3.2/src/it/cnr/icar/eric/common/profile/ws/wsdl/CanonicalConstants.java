/*
 * ====================================================================
 * This file is part of the ebXML Registry by Icar Cnr v3.2 
 * ("eRICv32" in the following disclaimer).
 *
 * "eRICv32" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "eRICv32" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License Version 3
 * along with "eRICv32".  If not, see <http://www.gnu.org/licenses/>.
 *
 * eRICv32 is a forked, derivative work, based on:
 * 	- freebXML Registry, a royalty-free, open source implementation of the ebXML Registry standard,
 * 	  which was published under the "freebxml License, Version 1.1";
 *	- ebXML OMAR v3.2 Edition, published under the GNU GPL v3 by S. Krushe & P. Arwanitis.
 * 
 * All derivative software changes and additions are made under
 *
 * Copyright (C) 2013 Ing. Antonio Messina <messina@pa.icar.cnr.it>
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the freebxml Software Foundation.  For more
 * information on the freebxml Software Foundation, please see
 * "http://www.freebxml.org/".
 *
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * ====================================================================
 */
package it.cnr.icar.eric.common.profile.ws.wsdl;

/**
 * This interface should contains all Canonical Constants defined by this profile.
 *
 * @author Farrukh.Najmi@sun.com
 */
public interface CanonicalConstants extends it.cnr.icar.eric.common.CanonicalConstants {
    
    //Canonical Slot names
    public final static String CANONICAL_SLOT_WSDL_PROFILE_REFERENCED_NAMESPACES =
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:referencedNamespaces";
    public final static String CANONICAL_SLOT_WSDL_PROFILE_TARGET_NAMESPACE =
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace";
        
    //TODO: Add other TYPE_CODES?
    public static final String CANONICAL_OBJECT_TYPE_CODE_WSDL = "WSDL";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_SERVICE = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Service";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_PORT = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Port";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_BINDING = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Binding";
    public static final String CANONICAL_OBJECT_TYPE_ID_WSDL_PORT_TYPE = 
        "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:PortType";
    
    public static final String CANONICAL_PROTOCOL_TYPE_ID_SOAP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:SOAP";
    public static final String CANONICAL_PROTOCOL_TYPE_ID_AS2 = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:AS2";
    public static final String CANONICAL_PROTOCOL_TYPE_ID_ATOM = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:ProtocolType:Atom";
    
    public static final String CANONICAL_TRANSPORT_TYPE_ID_HTTP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:HTTP";
    public static final String CANONICAL_TRANSPORT_TYPE_ID_MOM = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:MOM";
    public static final String CANONICAL_TRANSPORT_TYPE_ID_BEEP = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:TransportType:BEEP";
    
    public static final String CANONICAL_SOAP_STYLE_TYPE_ID_RPC = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:SOAPStyleType:RPC";
    public static final String CANONICAL_SOAP_STYLE_TYPE_ID_DOCUMENT = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:SOAPStyleType:Document";
    
    public static final String CANONICAL_QUERY_WSDL_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:WSDLDiscoveryQuery";
    public static final String CANONICAL_QUERY_SERVICE_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:ServiceDiscoveryQuery";
    public static final String CANONICAL_QUERY_PORT_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortDiscoveryQuery";
    public static final String CANONICAL_QUERY_BINDING_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:BindingDiscoveryQuery";
    public static final String CANONICAL_QUERY_PORTTYPE_DISCOVERY = 
        "urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortTypeDiscoveryQuery";
}
