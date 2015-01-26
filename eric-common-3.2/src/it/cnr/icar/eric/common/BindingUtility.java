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
package it.cnr.icar.eric.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.ActionType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for using JAXB bindings.
 *
 * @author Farrukh S. Najmi
 * @version   1.2, 05/02/00
 */
public class BindingUtility implements CanonicalConstants {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    private Log log = LogFactory.getLog(BindingUtility.class);

    //Implementation-specific constants
    public static final String ASSOCIATION_TYPE_ID_ProviderOf = "urn:oasis:names:tc:ebxml-regrep:AssociationType:ProviderOf";
    public static final String ASSOCIATION_TYPE_LID_ProviderOf = "urn:oasis:names:tc:ebxml-regrep:AssociationType:ProviderOf";
    public static final String ASSOCIATION_TYPE_CODE_ProviderOf = "ProviderOf";
    public static final String DEMO_DB_LID_PREFIX = "urn:freebxml:registry:demoDB:";
    public static final String FEDERATION_TEST_DATA_LID_PREFIX = DEMO_DB_LID_PREFIX + "federation:";
    public static final String CPP_CLASSIFICATION_NODE_ID = "urn:freebxml:registry:sample:profile:cpp:objectType:cppa:CPP";
    public static final String FREEBXML_REGISTRY_ORGANIZATION_ID = "urn:freebxml:registry:Organization:freebXMLRegistry";
    public static final String FREEBXML_REGISTRY_PROTOCOL_SIGNCERT = "urn:freebxml:registry:protocol:signCert";
    public static final String FREEBXML_REGISTRY_PROTOCOL_SETSTATUS = "urn:freebxml:registry:protocol:setStatus";
    public static final String FREEBXML_REGISTRY_PROTOCOL_NAME = "urn:freebxml:registry:protocol:name";
    public static final String FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT = "urn:freebxml:registry:query:filter:CompressContent";
    public static final String FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT_FILENAME = "urn:freebxml:registry:query:filter:CompressContent:filename";
    
    //Slot names defined for singleton Registry instance
    //The interceptors slot name
    public static final String FREEBXML_REGISTRY_REGISTRY_INTERCEPTORS = "urn:freebxml:registry:Registry:interceptors";

    //Constants shared between CertificateAuthority on server side and CertificateUtil on client side
    public static final String FREEBXML_REGISTRY_USERCERT_ALIAS_REQ = "usercertreq";
    public static final String FREEBXML_REGISTRY_USERCERT_ALIAS_RESP = "usercertresp";
    public static final String FREEBXML_REGISTRY_CACERT_ALIAS = "cacert";
    public static final String FREEBXML_REGISTRY_KS_PASS_REQ = "kspassreq";
    public static final String FREEBXML_REGISTRY_KS_PASS_RESP = "kspassresp";

    public static final String FREEBXML_REGISTRY_DEFAULT_NOTIFICATION_FORMATTER = "urn:freebxml:registry:xslt:notificationToHTML.xsl";

    // client to server communication of SOAP capabilities (sent in each
    // message): names one or more SOAP Header element(s), each containing
    // a capability URI
    public static final String SOAP_CAPABILITY_HEADER_LocalName =
    "capabilities";
    public static final String SOAP_CAPABILITY_HEADER_Namespace =
    "urn:freebxml:registry:soap";

    // client supports "modern" (SOAP 1.1 compliant) fault code mapping
    public static final String SOAP_CAPABILITY_ModernFaultCodes =
    "urn:freebxml:registry:soap:modernFaultCodes";

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private BindingUtility _utility; */
    private static BindingUtility instance = null;
    public org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    public org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
    public org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    public org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    public org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac;
    //public org.oasis.saml.bindings._20.protocol.ObjectFactory samlProtocolFac;
    //public org.oasis.saml.bindings._20.assertion.ObjectFactory samlAssertionFac;
    JAXBContext jaxbContext = null;

    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected BindingUtility() {
        try {
            getJAXBContext();
            rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
            lcmFac = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
            queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
            cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
            //samlProtocolFac = new org.oasis.saml.bindings._20.protocol.ObjectFactory();
            //samlAssertionFac = new org.oasis.saml.bindings._20.assertion.ObjectFactory();
        } catch (JAXBException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public synchronized static BindingUtility getInstance() {
        if (instance == null) {
            instance = new BindingUtility();
        }

        return instance;
    }

    
    public JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
//            jaxbContext = JAXBContext.newInstance(
//                    "org.oasis.ebxml.registry.bindings.rim:org.oasis.ebxml.registry.bindings.rs:org.oasis.ebxml.registry.bindings.lcm:org.oasis.ebxml.registry.bindings.query:org.oasis.ebxml.registry.bindings.cms",
//                    this.getClass().getClassLoader());
//            ;
            jaxbContext = JAXBContext.newInstance(
                    "org.oasis.ebxml.registry.bindings.rim:org.oasis.ebxml.registry.bindings.rs:org.oasis.ebxml.registry.bindings.lcm:org.oasis.ebxml.registry.bindings.query:org.oasis.ebxml.registry.bindings.cms"
                    );
        }

        return jaxbContext;
    }

    /*
     * Gets the id for the objectType for specified RIM class.
     *
     * @return Return the canonical id for the ClassificationNode representing the
     * objectType for specified RIM class.
     */
    public String getObjectTypeId(String rimClassName) throws JAXRException {
        String objectTypeId = null;

        try {
            Class<? extends BindingUtility> clazz = this.getClass();
            Field field = clazz.getField("CANONICAL_OBJECT_TYPE_ID_" + rimClassName);
            Object obj = field.get(this);
            objectTypeId = (String)obj;
        }
        catch (NoSuchFieldException e) {
            throw new JAXRException(e);
        }
        catch (SecurityException e) {
            throw new JAXRException(e);
        }
        catch (IllegalAccessException e) {
            throw new JAXRException(e);
        }

        return objectTypeId;
    }

    /**
     * Gets the objectType for the specified RegistryObject from class name.
     */
    public String getObjectType(RegistryObjectType ebRegistryObjectType) throws JAXRException {
        String objectType = ebRegistryObjectType.getObjectType();

        if (objectType == null) {
            String className = ebRegistryObjectType.getClass().getName();
            //Make sure className is not package qualified and does not end in Impl
            int index = className.lastIndexOf('.');
            if (index >=0 ) {
                className = className.substring(index+1,className.length()-4);
            }

            objectType = getObjectTypeId(className);
            
        }

        return objectType;
    }


    /**
     * Get Map of id key and RegistryObjectType value.
     * Does not get composed objects.
     */
    public Map<String, RegistryObjectType> getRegistryObjectTypeMap(Collection<IdentifiableType> objs) throws JAXRException {
        Map<String, RegistryObjectType> map = new HashMap<String, RegistryObjectType>();

        if (objs != null) {
            Iterator<IdentifiableType> iter = objs.iterator();

            while (iter.hasNext()) {
            	IdentifiableType obj = iter.next();

                if (obj instanceof RegistryObjectType) {
                    RegistryObjectType ebRegistryObjectType = (RegistryObjectType)obj;
                    map.put(ebRegistryObjectType.getId(), ebRegistryObjectType);
                }
            }
        }

        return map;
    }

    /**
     * Get List of RegistryObject after filtering out ObjectRef from RegistryObjectList.
     * Does not get composed objects.
     */
	public List<IdentifiableType> getRegistryObjectTypeFilteredList(RegistryObjectListType ebRegistryObjectListType)
			throws JAXRException {
        List<IdentifiableType> ebIdentifiableTypeResultList = new ArrayList<IdentifiableType>();

        if (ebRegistryObjectListType != null) {
            @SuppressWarnings("unchecked")
			List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) getIdentifiableTypeList(ebRegistryObjectListType);
            Iterator<IdentifiableType> iter = ebIdentifiableTypeList.iterator();

            while (iter.hasNext()) {

                IdentifiableType ebIdentifiableType = iter.next();
                //IdentifiableType identifiable = obj1.getValueObject();
                if (!(ebIdentifiableType instanceof ObjectRefType)) {
                    ebIdentifiableTypeResultList.add(ebIdentifiableType);
                }
            }
        }

        return ebIdentifiableTypeResultList;
    }

    /**
     * Get separate List of RegistryObjects and ObjectRefs.
     * Does not get composed objects.
     */
    public void getObjectRefsAndRegistryObjects(RegistryObjectListType ebRegistryObjectListType, Map<String, RegistryObjectType> ebRegistryObjectTypeMap, Map<String, ObjectRefType> ebObjectTypeRefMap) throws JAXRException {

        if (ebRegistryObjectListType != null) {
            @SuppressWarnings("unchecked")
			List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) getIdentifiableTypeList(ebRegistryObjectListType);
            Iterator<IdentifiableType> iter = ebIdentifiableTypeList.iterator();

            while (iter.hasNext()) {
            	// take ComplexType from Element
            	IdentifiableType ebIdentifiableType = iter.next();

                if (ebIdentifiableType instanceof RegistryObjectType) {
                    ebRegistryObjectTypeMap.put(ebIdentifiableType.getId(), (RegistryObjectType) ebIdentifiableType);
                }
                else if (ebIdentifiableType instanceof ObjectRefType) {
                    ebObjectTypeRefMap.put(ebIdentifiableType.getId(), (ObjectRefType) ebIdentifiableType);
                }
            }
        }

    }

    /**
     * Get the id from an object that could either an ObjectRef or RegistryObject
     */
    public String getObjectId(Object obj) throws JAXRException {
        String id = null;

        if (obj != null) {
            if (obj instanceof ObjectRefType) {
                id = ((ObjectRefType) obj).getId();
            } else if (obj instanceof RegistryObjectType) {
                id = ((RegistryObjectType) obj).getId();
            } else if (obj instanceof String) {
                id = (String) obj;
            } else {
                throw new JAXRException(resourceBundle.getString("message.unexpectedObjectType",
                            new String[] {obj.getClass().toString(), "java.lang.String, org.oasis.ebxml.registry.bindings.rim.ObjectRefType, org.oasis.ebxml.registry.bindings.rim.RegistryObjectType"}));
            }
        }

        return id;
    }

    /**
     * Set the id for an object that could either an ObjectRef or RegistryObject
     */
    public void setObjectId(Object obj, String id) throws JAXRException {
        if (obj != null) {
            if (obj instanceof ObjectRefType) {
                ((ObjectRefType) obj).setId(id);
            } else if (obj instanceof RegistryObjectType) {
                ((RegistryObjectType) obj).setId(id);
            } else {
                throw new JAXRException(resourceBundle.getString("message.unexpectedObjectType",
                            new String[] {obj.getClass().toString(), "org.oasis.ebxml.registry.bindings.rim.ObjectRefType, org.oasis.ebxml.registry.bindings.rim.RegistryObjectType"}));
            }
        }
    }

    /**
     * Gets trhe root element for a registry request
     * @return the root element as a String
     */
    public String getRequestRootElement(InputStream request)
        throws JAXRException {
        String rootElementName = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(request);

            Element root = doc.getDocumentElement();
            rootElementName = root.getLocalName();
        } catch (IOException e) {
            throw new JAXRException(e);
        } catch (ParserConfigurationException e) {
            throw new JAXRException(e);
        } catch (SAXException e) {
            throw new JAXRException(e);
        }

        return rootElementName;
    }

    /**
     * Gets the binding object representing the request from specufied XML file.
     */
    public Object getRequestObject(File file) throws JAXRException {
        Object req = null;

        try {
            Unmarshaller unmarshaller = getUnmarshaller();
            req = unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new JAXRException(resourceBundle.getString("message.unmarshallRequest"), e);
        }

        return req;
    }

    public Object getRequestObject(String rootElement, String message)
        throws JAXRException {
        //TODO: Consider removing String rootElement. Currently not used.
        Object req = null;

        try {
            StreamSource ss = new StreamSource(new StringReader(message));
            Unmarshaller unmarshaller = getUnmarshaller();
            req = unmarshaller.unmarshal(ss);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new JAXRException(resourceBundle.getString("message.unmarshallRequest"), e);
        }

        return req;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        //unmarshaller.setValidating(true);
        unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;

                    return keepOn;
                }
            });

        return unmarshaller;
    }

    /**
     * Gets a String representation of a list ids from a Collection of RegistryObjects.
     */
    public StringBuffer getIdListFromRegistryObjects(List<RegistryObjectType> ebRegistryObjectType) {
        StringBuffer idList = new StringBuffer();

        Iterator<RegistryObjectType> iter = ebRegistryObjectType.iterator();

        while (iter.hasNext()) {
        	RegistryObjectType obj = iter.next();
            String id = obj.getId();
            idList.append("'" + id + "'");

            if (iter.hasNext()) {
                idList.append(", ");
            }
        }

        return idList;
    }

    /**
     * Get List of id of RegistryObjects
     */
    public List<String> getIdsFromRegistryObjectTypes(Collection<IdentifiableType> ebRegistryObjectTypeCollection) {
        List<String> ids = new ArrayList<String>();

        if (ebRegistryObjectTypeCollection.size() > 0) {
            Iterator<IdentifiableType> iter = ebRegistryObjectTypeCollection.iterator();

            while (iter.hasNext()) {
                ids.add(iter.next().getId());
            }
        }
        return ids;
    }

    /**
     * Get List of id of ObjectRefTypes
     */
    public List<String> getIdsFromObjectRefTypes(Collection<ObjectRefType> ebObjectRefTypeCollection) {
        List<String> ids = new ArrayList<String>();

        if (ebObjectRefTypeCollection.size() > 0) {
            Iterator<ObjectRefType> iter = ebObjectRefTypeCollection.iterator();

            while (iter.hasNext()) {
                ids.add(iter.next().getId());
            }
        }
        return ids;
    }

    
    
    
    /**
     * Get List of ObjectRefs for specified RegistryObjects
     */
    public List<JAXBElement<IdentifiableType>> getObjectRefsFromRegistryObjects(List<?> objs) throws JAXRException {
        List<JAXBElement<IdentifiableType>> ebObjectRef = new ArrayList<JAXBElement<IdentifiableType>>();
        // TODO: return #CT instead of #E?
        if (objs.size() > 0) {
		    Iterator<?> iter = objs.iterator();

		    while (iter.hasNext()) {
		        IdentifiableType ebIdentifiableType = (IdentifiableType) iter.next();
		        ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
		        ebObjectRefType.setId(ebIdentifiableType.getId());
		        JAXBElement<IdentifiableType> ref = rimFac.createIdentifiable(ebObjectRefType);
		        ebObjectRef.add(ref);
		    }
		}

        return ebObjectRef;
    }

    /*
     * 
     */
    public List<ObjectRefType> getObjectRefTypeListFromRegistryObjects(List<? extends IdentifiableType> objs) throws JAXRException {
        List<ObjectRefType> ebObjectRefTypeList = new ArrayList<ObjectRefType>();
        // TODO: return #CT instead of #E?
        if (objs.size() > 0) {
		    Iterator<?> iter = objs.iterator();

		    while (iter.hasNext()) {
		    	Object theObject = iter.next();
		    	IdentifiableType ebIdentifiableType;
		    	if (theObject instanceof JAXBElement)
		    		ebIdentifiableType = (IdentifiableType)((JAXBElement<?>)theObject).getValue();
		    	else ebIdentifiableType = (IdentifiableType) theObject;
		        ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
		        ebObjectRefType.setId(ebIdentifiableType.getId());
		        ebObjectRefTypeList.add(ebObjectRefType);
		    }
		}
        return ebObjectRefTypeList;
    }


	/**
	 * Get List of ActionType from List of JAXBElement<ActionType>
	 */
	public List<ActionType> getActionTypeListFromElements(List<JAXBElement<? extends ActionType>> ebActionList) {

		List<ActionType> ebActionTypeList = new ArrayList<ActionType>();

		// take ComplexType from each Element
		Iterator<JAXBElement<? extends ActionType>> ebActionListIter = ebActionList.iterator();

		while (ebActionListIter.hasNext()) {
			ebActionTypeList.add(ebActionListIter.next().getValue());
		}

		return ebActionTypeList;
	}



    /**
     * Get List of ObjectRefs for specified RegistryObjects
     */
    public List<ObjectRefType> getObjectRefsFromRegistryObjectIds(List<String> ids) throws JAXRException {
        List<ObjectRefType> refs = new ArrayList<ObjectRefType>();

        if (ids.size() > 0) {
		    Iterator<String> iter = ids.iterator();

		    while (iter.hasNext()) {
		    	
		        ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
		        ebObjectRefType.setId(iter.next());

		        refs.add(ebObjectRefType);
		    }
		}

        return refs;
    }


    /**
     * Filter out those RegistryObjects whose id are in the List ids
     */
    public List<RegistryObjectType> getRegistryObjectsFromIds(List<?> objs, List<?> ids) {
        ArrayList<RegistryObjectType> ros = new ArrayList<RegistryObjectType>();

        if ((ids.size() > 0) && (objs.size() > 0)) {
            Iterator<?> iter = objs.iterator();

            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType) iter.next();

                if (ids.contains(ro.getId())) {
                    ros.add(ro);
                }
            }
        }

        return ros;
    }

    /**
     * Gets a String representation of a list of ids from an ObjectRefList.
     */
    public StringBuffer getIdListFromObjectRefList(ObjectRefListType refListType) {
        StringBuffer idList = new StringBuffer();

        List<ObjectRefType> refs = refListType.getObjectRef();
        Iterator<ObjectRefType> iter = refs.iterator();
        int cnt = refs.size();
        int i = 0;

        while (iter.hasNext()) {
            ObjectRefType ref = iter.next();
            String id = ref.getId();
            idList.append("'" + id + "'");

            if (i < (cnt - 1)) {
                idList.append(", ");
            }

            i++;
        }

        return idList;
    }

    /**
     * Get comma delimited list of quoted id from List of ids.
     */
    public StringBuffer getIdListFromIds(List<?> ids) {
        StringBuffer idList = new StringBuffer();
        Iterator<?> iter = ids.iterator();

        while (iter.hasNext()) {
            String id = (String) iter.next();
            idList.append("'" + id + "'");

            if (iter.hasNext()) {
                idList.append(',');
            }
        }

        return idList;
    }

    /**
     * Get List of id of ObjectRef under ObjectRefList.
     */
    public List<String> getIdsFromObjectRefList(ObjectRefListType refList) {
        List<String> ids = new ArrayList<String>();

        if (refList != null) {
            List<ObjectRefType> refs = refList.getObjectRef();
            Iterator<ObjectRefType> iter = refs.iterator();

            while (iter.hasNext()) {
                ObjectRefType ref = iter.next();
                ids.add(ref.getId());
            }
        }

        return ids;
    }

    /**
     * Get List of ObjectRefs of ObjectRef under ObjectRefList.
     */
    public List<ObjectRefType> getObjectRefTypeListFromObjectRefListType(ObjectRefListType ebObjectRefListType) {
        List<ObjectRefType> orefs;

        if (ebObjectRefListType == null) {
            orefs = new ArrayList<ObjectRefType>();
        } else {
            orefs = ebObjectRefListType.getObjectRef();
        }

        return orefs;
    }

    /**
     * Get the first-level RegistryObject by id from SubmitObjectsRequest.
     */
    public Object getObjectFromRequest(SubmitObjectsRequest registryRequest,
        String id) throws JAXRException {
        Object result = null;
        RegistryObjectListType objList = registryRequest.getRegistryObjectList();
        List<IdentifiableType> objs = getRegistryObjectTypeFilteredList(objList);

        Iterator<IdentifiableType> iter = objs.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();
            String objId = getObjectId(obj);

            if (id.equalsIgnoreCase(objId)) {
                result = obj;

                break;
            }
        }

        return result;
    }

    /**
     * Get List of Id of first-level RegistryObject or ObjectRef in a request. For
     * those kinds of request having RegistryObject and ObjectRef (e.g. SubmitObjectsRequest),
     * only the id of RegistryObject elements are returned.
     */
    public List<String> getIdsFromRequest(Object registryRequest)
        throws JAXRException
    {
        List<String> ids = new ArrayList<String>();

        if (registryRequest instanceof AdhocQueryRequest) {
        }
        else if (registryRequest instanceof ApproveObjectsRequest) {
            ObjectRefListType refList = ((ApproveObjectsRequest)registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        }
        else if (registryRequest instanceof SetStatusOnObjectsRequest) {
            ObjectRefListType refList = ((SetStatusOnObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        }
        else if (registryRequest instanceof DeprecateObjectsRequest) {
            ObjectRefListType refList = ((DeprecateObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        }
        else if (registryRequest instanceof UndeprecateObjectsRequest) {
            ObjectRefListType refList = ((UndeprecateObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        }
        else if (registryRequest instanceof RemoveObjectsRequest) {
            ObjectRefListType refList = ((RemoveObjectsRequest) registryRequest).getObjectRefList();
            ids.addAll(getIdsFromObjectRefList(refList));
        }
        else if (registryRequest instanceof SubmitObjectsRequest) {
            RegistryObjectListType objList =
                ((SubmitObjectsRequest) registryRequest).getRegistryObjectList();
            List<IdentifiableType> objs = getRegistryObjectTypeFilteredList(objList);
            ids.addAll(getIdsFromRegistryObjectTypes(objs));
        }
        else if (registryRequest instanceof UpdateObjectsRequest) {
            RegistryObjectListType objList =
                ((UpdateObjectsRequest) registryRequest).getRegistryObjectList();
            List<IdentifiableType> objs = getRegistryObjectTypeFilteredList(objList);
            ids.addAll(getIdsFromRegistryObjectTypes(objs));
        }
        else if (registryRequest instanceof RelocateObjectsRequest) {
            // Do nothing.
        }
        else {
            throw new JAXRException(resourceBundle.getString("message.invalidRequest",
                    new String[]{registryRequest.getClass().getName()}));
        }

        return ids;
    }

    public InternationalStringType getName(String name) {

        InternationalStringType ebInternationalStringType = rimFac.createInternationalStringType();

        LocalizedStringType ebLocalizedStringType = rimFac.createLocalizedStringType();
        ebLocalizedStringType.setValue(name);
        ebInternationalStringType.getLocalizedString().add(ebLocalizedStringType);

        return ebInternationalStringType;
    }

    public InternationalStringType getDescription(String desc) {

    	InternationalStringType ebInternationalStringType = rimFac.createInternationalStringType();

        LocalizedStringType ebLocalizedStringType = rimFac.createLocalizedStringType();
        ebLocalizedStringType.setValue(desc);
        ebInternationalStringType.getLocalizedString().add(ebLocalizedStringType);

        return ebInternationalStringType;
    }

    public String getInternationalStringAsString(InternationalStringType is) throws JAXRException {
        String str = "";
        List<LocalizedStringType> localizedStrings = is.getLocalizedString();
        if (localizedStrings.size() > 0) {
            LocalizedStringType ls = localizedStrings.get(0); //TODO: Need to do getClosestValue() in future
            str = ls.getValue();
        }
        return str;
    }

    public SOAPElement getSOAPElementFromBindingObject(Object obj) throws JAXRException {
        SOAPElement soapElem = null;

        try {
            SOAPElement parent =
                SOAPFactory.newInstance().createElement("dummy");

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal( obj, new DOMResult(parent) );
            
            soapElem = (SOAPElement)parent.getChildElements().next();

        }
        catch (Exception e) {
            throw new JAXRException(e);
        }

        return soapElem;
    }

    public Object getBindingObjectFromSOAPElement(SOAPElement soapElem) throws JAXRException {
        Object obj = null;

        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            obj = unmarshaller.unmarshal(soapElem);

        }
        catch (Exception e) {
            throw new JAXRException(e);
        }

        return obj;
    }

    public void checkRegistryResponse(RegistryResponseType resp) throws JAXRException {
        if (!(resp.getStatus().equals(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success))) {
            StringWriter sw = new StringWriter();
            try {
            	
            	
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);

                marshaller.marshal(resp, sw);
                throw new JAXRException(sw.toString());
            }
            catch (Exception e) {
                throw new JAXRException(e);
            }
        }
    }

    /**
     * Gets the Set of ReferenceInfo for all object references within specified RegistryObject.
     * TODO: replace with reflections API when JAXB bindings use special class for ReferenceURI.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose ObjectRefs are being sought.
     *
     * @param idMap The Map with old temporary id to new permanent id mapping.
     *
     */
    public Set<ReferenceInfo> getObjectRefsInRegistryObject(RegistryObjectType ro, Map<?, ?> idMap, Set<RegistryObjectType> processedObjects, int depth) throws JAXRException {
        HashSet<ReferenceInfo> refInfos = new HashSet<ReferenceInfo>();

        if ((ro != null) && (!processedObjects.contains(ro))) {
            processedObjects.add(ro);
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.RegistryObjectType", refInfos, idMap, "ObjectType");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType", refInfos, idMap, "Parent");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", refInfos, idMap, "ClassificationNode");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", refInfos, idMap, "ClassificationScheme");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", refInfos, idMap, "ClassifiedObject");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", refInfos, idMap, "IdentificationScheme");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", refInfos, idMap, "RegistryObject");

            //FederationType fed = (FederationType)ro;
            //TODO: Fix so it adds only Strings not ObjectRefType
            //refInfos.addAll(fed.getMembers().getObjectRef());

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", refInfos, idMap, "AssociationType");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", refInfos, idMap, "SourceObject");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", refInfos, idMap, "TargetObject");


            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", refInfos, idMap, "User");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", refInfos, idMap, "RequestId");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.OrganizationType", refInfos, idMap, "Parent");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.RegistryType", refInfos, idMap, "Operator");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", refInfos, idMap, "Service");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", refInfos, idMap, "TargetBinding");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", refInfos, idMap, "ServiceBinding");
            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", refInfos, idMap, "SpecificationObject");

            processRefAttribute(ro, "org.oasis.ebxml.registry.bindings.rim.SubscriptionType", refInfos, idMap, "Selector");

            --depth;

            //Now process composed objects
            if (depth != 0) {
                Set<RegistryObjectType> composedObjects = getComposedRegistryObjects(ro, 1);
                Iterator<RegistryObjectType> iter = composedObjects.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof RegistryObjectType) {
                        RegistryObjectType composedObject = (RegistryObjectType)obj;
                        Set<ReferenceInfo> composedRefInfos = getObjectRefsInRegistryObject(composedObject, idMap, processedObjects, depth);
                        refInfos.addAll(composedRefInfos);
                    }
                }
            }
        }

        return refInfos;
    }

    /**
     * Gets the Set of ReferenceInfo for specified reference attribute within RegistryObject.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose reference attribute is being sought.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     */
    private void processRefAttribute(RegistryObjectType ro, String className, Set<ReferenceInfo> refInfos, Map<?, ?> idMap, String attribute) throws JAXRException {
        try {
            //Use reflections API to get the attribute value, check if it needs to be mapped
            //and set it with mapped value if needed and add the final value to refInfos
            Class<?> clazz = Class.forName(className);
            if (!(clazz.isInstance(ro))) {
                return;
            }

            //Get the attribute value by calling get method
            String getMethodName = "get" + attribute;
            Method getMethod = clazz.getMethod(getMethodName, (java.lang.Class[])null);

            //Invoke getMethod to get the reference target object's id
            String targetObjectId = (String)getMethod.invoke(ro, (java.lang.Object[])null);

            if (targetObjectId != null) {
                //Check if id has been mapped to a new id
                if (idMap.containsKey(targetObjectId)) {
                    //Replace old id with new id
                    targetObjectId = (String)idMap.get(targetObjectId);

                    //Use set method to set new value on ro
                    @SuppressWarnings("rawtypes")
					Class[] parameterTypes = new Class[1];
                    Object[] parameterValues = new Object[1];
                    parameterTypes[0] = Class.forName("java.lang.String");
                    parameterValues[0] = targetObjectId;
                    String setMethodName = "set" + attribute;
                    Method setMethod = clazz.getMethod(setMethodName, parameterTypes);
                    setMethod.invoke(ro, parameterValues);
                }

                ReferenceInfo refInfo = new ReferenceInfo(ro.getId(), targetObjectId, attribute);
                refInfos.add(refInfo);
            }

        }
        catch (Exception e) {
            //throw new OMARExeption("Class = " ro.getClass() + " attribute = " + attribute", e);
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorClassAttribute", new Object[]{ro.getClass(), attribute}));
            e.printStackTrace();
        }

    }


    /**
     * Gets the composed RegistryObjects within specified RegistryObject.
     * Based on scanning rim.xsd for </sequence>.
     *
     * @param registryObjects specifies the RegistryObjects whose composed objects are being sought.
     * @param depth specifies depth of fetch. -1 implies fetch all levels. 1 implies fetch immediate composed objects.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getComposedRegistryObjects(Collection<?> registryObjects, int depth) {
        HashSet composedObjects = new HashSet();

        Iterator<?> iter = registryObjects.iterator();
        while (iter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType)iter.next();
            composedObjects.addAll(getComposedRegistryObjects(ro, depth));
        }

        return composedObjects;
    }

    public static boolean isComposedObject(RegistryObjectType ro) {
        boolean isComposed = false;

        if ((ro instanceof ClassificationType)
            || (ro instanceof ExternalIdentifierType)
            || (ro instanceof ServiceBindingType)
            || (ro instanceof SpecificationLinkType)) {

            isComposed = true;
        }

        return isComposed;
    }

    public static String getParentIdForComposedObject(RegistryObjectType ro) {
        String parentId = null;

        if (ro instanceof ClassificationType) {
            parentId = ((ClassificationType)ro).getClassifiedObject();
        } else if (ro instanceof ExternalIdentifierType) {
            parentId = ((ExternalIdentifierType)ro).getRegistryObject();
        } else if (ro instanceof ServiceBindingType) {
            parentId = ((ServiceBindingType)ro).getService();
        } else if (ro instanceof SpecificationLinkType) {
            parentId = ((SpecificationLinkType)ro).getServiceBinding();
        }

        return parentId;
    }
    
    
    public static void setParentIdForComposedObject( RegistryObjectType ro, String parentId ) 
    {
        if (ro instanceof ClassificationType) {
             ((ClassificationType)ro).setClassifiedObject( parentId );
        } else if (ro instanceof ExternalIdentifierType) {
            ((ExternalIdentifierType)ro).setRegistryObject( parentId );
        } else if (ro instanceof ServiceBindingType) {
            ((ServiceBindingType)ro).setService( parentId );
        } else if (ro instanceof SpecificationLinkType) {
            ((SpecificationLinkType)ro).setServiceBinding( parentId );
        }
    }


    /**
     * Gets the composed RegistryObjects within specified RegistryObject.
     * Based on scanning rim.xsd for </sequence>.
     *
     * @param ro specifies the RegistryObject whose composed objects are being sought.
     * @param depth specifies depth of fetch. -1 implies fetch all levels. 1 implies fetch immediate composed objects.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<RegistryObjectType> getComposedRegistryObjects(RegistryObjectType ro, int depth) {
        HashSet<RegistryObjectType> composedObjects = new HashSet<RegistryObjectType>();

        if (ro != null) {
            List immediateComposedObjects = new ArrayList();

            immediateComposedObjects.addAll(ro.getClassification());
            immediateComposedObjects.addAll(ro.getExternalIdentifier());


            if (ro instanceof ClassificationNodeType) {
                ClassificationNodeType node = (ClassificationNodeType)ro;
                immediateComposedObjects.addAll(node.getClassificationNode());
            }
            else if (ro instanceof ClassificationSchemeType) {
                ClassificationSchemeType scheme = (ClassificationSchemeType)ro;
                immediateComposedObjects.addAll(scheme.getClassificationNode());
            }
            else if (ro instanceof ServiceBindingType) {
                ServiceBindingType binding = (ServiceBindingType)ro;
                immediateComposedObjects.addAll(binding.getSpecificationLink());
            }
            else if (ro instanceof RegistryPackageType) {
                RegistryPackageType pkg = (RegistryPackageType)ro;
                if (pkg.getRegistryObjectList() != null) {
                    immediateComposedObjects.addAll(getIdentifiableTypeList(pkg.getRegistryObjectList()));
                }
            }
            else if (ro instanceof ServiceType) {
                ServiceType service = (ServiceType)ro;
                immediateComposedObjects.addAll(service.getServiceBinding());
            }

            --depth;

            //Add each immediate composedObject
            Iterator iter = immediateComposedObjects.iterator();
            while (iter.hasNext()) {
                RegistryObjectType composedObject = (RegistryObjectType)iter.next();
                composedObjects.add(composedObject);

                //If depth != 0 then recurse and add descendant composed objects
                if (depth != 0) {
                    composedObjects.addAll(getComposedRegistryObjects(composedObject, depth));
                }
            }
        }

        return composedObjects;
    }

    public String marshalObject(Object obj) throws JAXBException {

        StringWriter sw = new StringWriter();
//        javax.xml.bind.Marshaller marshaller = rsFac.createMarshaller();
        javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.TRUE);
        marshaller.marshal(obj, sw);

        //Now get the object as a String
        String str = sw.toString();
        return str;
    }

    public void printObject(Object obj) throws JAXBException {

        StringWriter sw = new StringWriter();

//        javax.xml.bind.Marshaller marshaller = rsFac.createMarshaller();
        javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.TRUE);
        marshaller.marshal(obj, sw);

        //Now get the object as a String
        String str = sw.toString();
        System.err.println(str);
    }

    /**
     * This method is used for adding a java.util.Map of slots to a RegistryObjectType
     *
     * @param ro
     *   The RegistryObjectType on which to set slots
     * @param slotsMap
     *   The java.util.Map of slots to set on the RegistryObjectType. This Map
     *   must only contain java.lang.String and java.util.Collection values
     * @throws IllegalArgumentException
     *   This RuntimeException is thrown if the slotsMap argument contains
     *   neither a java.lang.String nor a java.util.Collection
     * @throws JAXBException
     *   Thrown when there is a problem marshalling/unmarshalling JAXB objects
     */
    public void addSlotsToRegistryObject(RegistryObjectType ro, Map<?, ?> slotsMap)
        throws JAXBException {

        ArrayList<SlotType1> slots = new ArrayList<SlotType1>();

        Iterator<?> iter = slotsMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object slotValue = slotsMap.get(key);

            SlotType1 ebSlotType = rimFac.createSlotType1();
            ebSlotType.setName(key.toString());
            ValueListType ebValueListType = rimFac.createValueListType();

            //slotValue must either be a String or a Collection of Strings
            if (slotValue instanceof String) {
                ebValueListType.getValue().add((String) slotValue);

            } else if (slotValue instanceof Collection) {
                Collection<?> c = (Collection<?>)slotValue;
                Iterator<?> citer = c.iterator();
                while (citer.hasNext()) {
                    ebValueListType.getValue().add(citer.next().toString());
                }
            } else {
                throw new IllegalArgumentException(resourceBundle.getString("message.addingParameter",
                        new String[]{slotValue.getClass().getName()}));
            }
            ebSlotType.setValueList(ebValueListType);
            slots.add(ebSlotType);
        }

        ro.getSlot().addAll(slots);
    }

    /**
     * This method is used to add a single slot to a RegistryObject.
     *
     * @param ro
     *   The RegistryObjectType on which to set slots
     * @param slotName
     *   The slotname
     * @param slotType
     *   The slotType
     * @param values
     *   The slot values
     * @throws IllegalArgumentException
     *   This RuntimeException is thrown if the slotsMap argument contains
     *   neither a java.lang.String nor a java.util.Collection
     * @throws JAXBException
     *   Thrown when there is a problem marshalling/unmarshalling JAXB objects
     */
    public void addSlotToRegistryObject(RegistryObjectType ro, String slotName, String slotType, List<String> values)
        throws JAXBException {

        SlotType1 ebSlotType = rimFac.createSlotType1();
        ebSlotType.setName(slotName);
        ebSlotType.setSlotType(slotType);
        ValueListType ebValueListType = rimFac.createValueListType();
        
        Iterator<String> valuesIter = values.iterator();
        while (valuesIter.hasNext()) {
        	ebValueListType.getValue().add(valuesIter.next());
        }

        ebSlotType.setValueList(ebValueListType);
        ro.getSlot().add(ebSlotType);
    }
    
    public HashMap<String, Serializable> getSlotsFromRegistryObject(RegistryObjectType ro) throws JAXBException {
        HashMap<String, Serializable> slotsMap = new HashMap<String, Serializable>();

        List<SlotType1> slots = ro.getSlot();

        if (slots == null) {
            return slotsMap;
        }

        Iterator<SlotType1> iter = slots.iterator();
        while (iter.hasNext()) {
            SlotType1 slot = iter.next();
            String slotName = slot.getName();
            List<String> values = slot.getValueList().getValue();

            @SuppressWarnings("unused")
			Object slotValue = null;
            if (values.size() == 1) {
                slotsMap.put(slotName, values.get(0));
            } else if (values.size() > 1) {
                ArrayList<String> al = new ArrayList<String>();
                Iterator<String> valuesIter = values.iterator();
                while (valuesIter.hasNext()) {
                    al.add(valuesIter.next());
                }
                slotsMap.put(slotName, al);
            }
        }


        return slotsMap;
    }

    /**
     * This method is used for adding a java.util.Map of slots to a RegistryRequestType
     *
     * @param req
     *   The RegistryRequestType on which to set slots
     * @param slotsMap
     *   The java.util.Map of slots to set on the RegistryRequestType. This Map
     *   must only contain java.lang.String and java.util.Collection values
     * @throws IllegalArgumentException
     *   This RuntimeException is thrown if the slotsMap argument contains
     *   neither a java.lang.String nor a java.util.Collection
     * @throws JAXBException
     *   Thrown when there is a problem marshalling/unmarshalling JAXB objects
     */
    public void addSlotsToRequest(RegistryRequestType req, Map<String, String> slotsMap) throws JAXBException {

        // Get SlotListType from RegistryRequestType, and add new slot to this list
        SlotListType slotList = req.getRequestSlotList();
        if (slotList == null) {
            slotList = rimFac.createSlotListType();
        }
        Iterator<String> iter = slotsMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object slotValue = slotsMap.get(key);

            SlotType1 ebSlotType = rimFac.createSlotType1();
            ebSlotType.setName(key.toString());
            ValueListType ebValueListType = rimFac.createValueListType();

            //slotValue must either be a String or a Collection of Strings
            if (slotValue instanceof String) {
                ebValueListType.getValue().add((String) slotValue);

            } else if (slotValue instanceof Collection) {
                @SuppressWarnings("unchecked")
				Collection<String> c = (Collection<String>)slotValue;
                Iterator<String> citer = c.iterator();
                while (citer.hasNext()) {
                    ebValueListType.getValue().add(citer.next());
                }
            } else {
                throw new IllegalArgumentException(resourceBundle.getString("message.addingParameter",
                        new String[]{slotValue.getClass().getName()}));
            }
            ebSlotType.setValueList(ebValueListType);
            slotList.getSlot().add(ebSlotType);
        }


        req.setRequestSlotList(slotList);
    }

    /*
     * Returns either <String, String> or <String, ArrayList<String>>
     */
    public HashMap<String, Object> getSlotsFromRequest(RegistryRequestType ebRegistryRequestType) throws JAXBException {        
    	HashMap<String, Object> slotsMap = new HashMap<String, Object>();

        if (ebRegistryRequestType != null) {
            SlotListType ebSlotListType = ebRegistryRequestType.getRequestSlotList();

            if (ebSlotListType == null) {
                return slotsMap;
            }

            List<SlotType1> ebSlotTypeList = ebSlotListType.getSlot();

            Iterator<SlotType1> iter = ebSlotTypeList.iterator();
            while (iter.hasNext()) {
                SlotType1 ebSlotType = iter.next();
                String slotName = ebSlotType.getName();
                List<String> values = ebSlotType.getValueList().getValue();

//                Object slotValue = null;
                if (values.size() == 1) {
                    slotsMap.put(slotName, values.get(0));
                } else if (values.size() > 1) {
                    ArrayList<String> al = new ArrayList<String>();
                    Iterator<String> valuesIter = values.iterator();
                    while (valuesIter.hasNext()) {
                        al.add(valuesIter.next());
                    }
                    slotsMap.put(slotName, al);
                }
            }
        }

        return slotsMap;
    }

    public static String getSchemeIdFromNodePath(String nodePath) {
        String[] pathElements = nodePath.split("/");
        if (pathElements.length < 2) {
            return null;
        } else {
            return pathElements[1];
        }
    }

    public String getSchemeIdForRegistryObject(RegistryObjectType ro) throws JAXRException {
        String schemeId = null;

        if (ro instanceof ClassificationSchemeType) {
            schemeId = ro.getId();
        } else if (ro instanceof ClassificationNodeType) {
            ClassificationNodeType node = (ClassificationNodeType)ro;
            String path = node.getPath();
            schemeId = getSchemeIdFromNodePath(path);
        } else {
            throw new JAXRException(resourceBundle.getString("message.unexpectedObjectType",
                        new String[] {ro.getClass().toString(), "org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType, org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType"}));
        }

        return schemeId;
    }

    /**
     * Makes an identical clone of a RegistryObjectType as a new java object.
     */
    public RegistryObjectType cloneRegistryObject(RegistryObjectType ebRegistryObjectType) throws JAXRException {
        RegistryObjectType ebRegistryObjectTypeNew = null;
        try {
            StringWriter sw = new StringWriter();
            
            // wrap ComplexType as Element
            JAXBElement<RegistryObjectType> ebRegistryObject = rimFac.createRegistryObject(ebRegistryObjectType); 

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(ebRegistryObject, sw);
            
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			@SuppressWarnings("unchecked")
			JAXBElement<RegistryObjectType> ebRegistryObjectNew = (JAXBElement<RegistryObjectType>) unmarshaller
					.unmarshal(new StreamSource(new StringReader(sw.toString())));
			
			// take ComplexType from Element
			ebRegistryObjectTypeNew = ebRegistryObjectNew.getValue();
            
        } catch (javax.xml.bind.JAXBException e) {
            throw new JAXRException(e);
        }

        return ebRegistryObjectTypeNew;
    }

    public InternationalStringType createInternationalStringType(String val) throws JAXRException {
        InternationalStringType ebInternationalStringType = null;

        ebInternationalStringType = rimFac.createInternationalStringType();
        LocalizedStringType ebLocalizedStringType = rimFac.createLocalizedStringType();
        ebLocalizedStringType.setValue(val);
        ebInternationalStringType.getLocalizedString().add(ebLocalizedStringType);
        //Use default values for lang and charset

        return ebInternationalStringType;
    }

    //Creates an AdhocQueryRequest for a parameterized stored query invocation
    public AdhocQueryRequest createAdhocQueryRequest(String queryId, Map<String, String> queryParams) throws JAXBException {
        AdhocQueryRequest req = createAdhocQueryRequest("");

        HashMap<String, String> slotsMap = new HashMap<String, String>(queryParams);
        slotsMap.put(CANONICAL_SLOT_QUERY_ID, queryId);

        this.addSlotsToRequest(req, slotsMap);

        return req;
    }

    //Creates an AdhocQueryRequest for a normal (non-parameterized query
    public AdhocQueryRequest createAdhocQueryRequest(String queryStr) throws JAXBException {
        AdhocQueryRequest ebAdhocQueryRequest = null;

        AdhocQueryType ebAdhocQueryType = createAdhocQueryType(queryStr);
        ebAdhocQueryRequest = queryFac.createAdhocQueryRequest();
        ebAdhocQueryRequest.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
        ebAdhocQueryRequest.setAdhocQuery(ebAdhocQueryType);

        ResponseOptionType ebResponseOptionType = queryFac.createResponseOptionType();
        ebResponseOptionType.setReturnComposedObjects(true);
        ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
        ebAdhocQueryRequest.setResponseOption(ebResponseOptionType);

        return ebAdhocQueryRequest;
    }

    public AdhocQueryType createAdhocQueryType(String queryStr) throws JAXBException {

    	AdhocQueryType ebAdhocQueryType = rimFac.createAdhocQueryType();
        ebAdhocQueryType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

        QueryExpressionType ebQueryExpressionType = rimFac.createQueryExpressionType();
        ebAdhocQueryType.setQueryExpression(ebQueryExpressionType);
        ebQueryExpressionType.setQueryLanguage(CANONICAL_QUERY_LANGUAGE_ID_SQL_92);
        ebQueryExpressionType.getContent().add(queryStr);

        return ebAdhocQueryType;
    }

    public SubmitObjectsRequest createSubmitRequest(boolean dontVersion, boolean dontVersionContent, List<RegistryObjectType> ebRegistryObjectTypeList) throws Exception {
        SubmitObjectsRequest request = lcmFac.createSubmitObjectsRequest();

        HashMap<String, String> slotsMap = new HashMap<String, String>();
        if (dontVersion) {
            slotsMap.put(CANONICAL_SLOT_LCM_DONT_VERSION, "true");
        }

        if (dontVersionContent) {
            slotsMap.put(CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
        }

        if (!slotsMap.isEmpty()) {
            addSlotsToRequest(request, slotsMap);
        }

        if ((ebRegistryObjectTypeList != null) && (ebRegistryObjectTypeList.size() > 0)) {
            request.setRegistryObjectList(getRegistryObjectListType(ebRegistryObjectTypeList));
        }

        return request;
    }

    public void addRegistryObjectToSubmitRequest(SubmitObjectsRequest submitRequest, RegistryObjectType ebRegistryObjectType) throws JAXRException {
		submitRequest.setRegistryObjectList(getRegistryObjectListType(ebRegistryObjectType));
    }

    public ClassificationType createClassificationType(String classifiedObjectId, String classificationNodeId) throws JAXRException {
    	
    	ClassificationType ebClassificationType = rimFac.createClassificationType();
        ebClassificationType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
        ebClassificationType.setClassifiedObject(classifiedObjectId);
        ebClassificationType.setClassificationNode(classificationNodeId);

        return ebClassificationType;
    }

    public void addClassificationToRegistryObject(RegistryObjectType ebRegistryObjectType, String classificationNodeId) throws JAXRException {
        ClassificationType ebClassificationType = createClassificationType(ebRegistryObjectType.getId(), classificationNodeId);
        ebRegistryObjectType.getClassification().add(ebClassificationType);
    }

    public AssociationType1 createAssociationType(String src, String target, String associationTypeNodeId) throws JAXRException {
        String associationId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        return createAssociationType(src, target, associationTypeNodeId, associationId);
    }

    public AssociationType1 createAssociationType(String src, String target, String associationTypeNodeId, String assocId) throws JAXRException {
    	AssociationType1 ebAssociationType = rimFac.createAssociationType1();
        ebAssociationType.setId(assocId);
        ebAssociationType.setAssociationType(associationTypeNodeId);
        ebAssociationType.setSourceObject(src);
        ebAssociationType.setTargetObject(target);
        return ebAssociationType;
    }

    /**
     * Creates as id for an Association based upon the srcId, targetId and associationTypeNodeId.
     * Care should be taken to only use this when there is supposed to be only one Association
     * between two objects of the given associationType.
     */
    public String createAssociationId(String srcId, String targetId, String associationTypeNodeId) throws JAXRException {
        String associationId = null;

        if (associationTypeNodeId != null) {
            associationId = srcId + ":" + associationTypeNodeId + ":" + targetId;
        }


        //Fallback to generated id if id is not valid (usually because it is too long.
        if (!Utility.getInstance().isValidRegistryId(associationId)) {
            associationId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        }

        return associationId;
    }

    public static String mapJAXRNameToEbXMLName(String className) {
        String newName = null;

        if (className.equalsIgnoreCase("Concept")) {
            newName = "ClassificationNode";
        } else {
            newName = className;
        }

        return newName;
    }

    public static String mapEbXMLNameToJAXRName(String ebXMLName) {
        String newName = null;

        if (ebXMLName.equalsIgnoreCase("ClassificationNode")) {
            newName = "Concept";
        } else {
            newName = ebXMLName;
        }

        return newName;
    }

    public static String getActionFromRequest(RegistryRequestType registryRequest) throws JAXRException {
        String action = null;
        if (registryRequest instanceof AdhocQueryRequest) {
            action = ACTION_READ;
        }
        else if (registryRequest instanceof SubmitObjectsRequest) {
            action = ACTION_CREATE;
        }
        else if (registryRequest instanceof ApproveObjectsRequest) {
            action = ACTION_APPROVE;
        }
        else if (registryRequest instanceof DeprecateObjectsRequest) {
            action = ACTION_DEPRECATE;
        }
        else if (registryRequest instanceof UndeprecateObjectsRequest) {
            action = ACTION_UNDEPRECATE;
        }
        else if (registryRequest instanceof UpdateObjectsRequest) {
            action = ACTION_UPDATE;
        }
        else if (registryRequest instanceof RemoveObjectsRequest) {
            action = ACTION_DELETE;
        }
        else if (registryRequest instanceof RelocateObjectsRequest) {
            action = ACTION_RELOCATE;
        }
        else if (registryRequest instanceof SetStatusOnObjectsRequest) {
            action = ACTION_SET_STATUS;
        }
        else {
            //??Get slotName and return the extension protocol name here
            action = ACTION_EXTENSION_REQUEST;
        }
 
        return action;
    }

    /**
     * Client uses Datahandler as repository item while server uses RepositoryItemImpl
     * Need to convert.
     */
    public void convertRepositoryItemMapForServer(HashMap<String, Object> idToRepositoryItemMap) throws RegistryException {
        Set<String> keys = idToRepositoryItemMap.keySet();
        Iterator<String> keysIter = keys.iterator();
        while (keysIter.hasNext()) {
            String key = keysIter.next();
            DataHandler dh = (DataHandler)idToRepositoryItemMap.get(key);
            RepositoryItem ri = new RepositoryItemImpl(key, dh);
            idToRepositoryItemMap.put(key, ri);
        }
    }

    /**
     * Client uses Datahandler as repository item while server uses RepositoryItemImpl
     * Need to convert.
     */
    public void convertRepositoryItemMapForClient(HashMap<String, Object> idToRepositoryItemMap) throws RegistryException {
        Set<String> keys = idToRepositoryItemMap.keySet();
        Iterator<String> keysIter = keys.iterator();
        while (keysIter.hasNext()) {
            String key = keysIter.next();
            RepositoryItem ri = (RepositoryItem)idToRepositoryItemMap.get(key);
            DataHandler dh = ri.getDataHandler();
            idToRepositoryItemMap.put(key, dh);
        }
    }


    
    /*
     * Dr. Krusche & Partner PartG (C) 2011
     */
    public List<? extends IdentifiableType> getIdentifiableTypeList(RegistryObjectListType ebRegistryObjectListType) {
		/*	             
		 * getIdentifiable() returns JAXBelement<? extends IdentifiableType>
         * 
         * return should be List of ComplexType
         */
    	
        List<JAXBElement<? extends IdentifiableType>> ebIdentifiableList = ebRegistryObjectListType.getIdentifiable(); 
		List<IdentifiableType> ebIdentifiableTypeList = new ArrayList<IdentifiableType>();
        
		// take ComplexType from each Element
		Iterator<JAXBElement<? extends IdentifiableType>> ebIdentifiableListIter = ebIdentifiableList.iterator();
		while (ebIdentifiableListIter.hasNext()) {
			//ebIdentifiableTypeList.add(ebIdentifiableListIter.next().getValue());
			Object ro = ebIdentifiableListIter.next();
			if (ro instanceof JAXBElement)
				ebIdentifiableTypeList.add((IdentifiableType) ((JAXBElement<?>)ro).getValue());
			else
				ebIdentifiableTypeList.add((IdentifiableType) ro);
		}
        
        return ebIdentifiableTypeList;

    }

    /*
     * create new RegistryObjectTypeList from one RegistryObjectType
     */
    public RegistryObjectListType getRegistryObjectListType(RegistryObjectType ebRegistryObjectType) {
		
    	RegistryObjectListType ebRegistryObjectListType = rimFac.createRegistryObjectListType();
		ebRegistryObjectListType.getIdentifiable().add(rimFac.createIdentifiable(ebRegistryObjectType));
		
		return ebRegistryObjectListType;
    }

    /*
     * create new RegistryObjectTypeList from Collection of RegistryObjectType's
     */
    public RegistryObjectListType getRegistryObjectListType(Collection<RegistryObjectType> ebRegistryObjectTypeList) {
		
    	RegistryObjectListType ebRegistryObjectListType = rimFac.createRegistryObjectListType();
    	
    	Iterator<RegistryObjectType> iter = ebRegistryObjectTypeList.iterator();
    	while (iter.hasNext()) {
    		ebRegistryObjectListType.getIdentifiable().add(rimFac.createIdentifiable(iter.next()));
    	}
		
		return ebRegistryObjectListType;
    }
}
