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
package it.cnr.icar.eric.server.security.authorization;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.AttributeFinderModule;




/**
 * Supports the attributes defined by ebRIM for RegistryObjects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryAttributeFinderModule extends AttributeFinderModule {
    
    private static Log log = LogFactory.getLog(RegistryAttributeFinderModule.class);
    
    
    private static BindingUtility bu = BindingUtility.getInstance();    
    
    /** Returns true always because this module supports designators.
     *
     * @return true always
     */
    public boolean isDesignatorSupported() {
        return true;
    }
    
    /** Returns a <code>Set</code> with a single <code>Integer</code>
     * specifying that environment attributes are supported by this
     * module.
     *
     * @return
     *     a <code>Set</code> with <code>AttributeDesignator.SUBJECT_TARGET</code>
     *     and <code>AttributeDesignator.RESOURCE_TARGET</code> included
     */
    public Set<Integer> getSupportedDesignatorTypes() {
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(new Integer(AttributeDesignator.SUBJECT_TARGET));
        set.add(new Integer(AttributeDesignator.RESOURCE_TARGET));
        set.add(new Integer(AttributeDesignator.ACTION_TARGET));
        //set.add(new Integer(AttributeDesignator.ENVIRONMENT_TARGET));
        return set;
    }
    
    /** Used to get the attributes defined by ebRIM for resources and subjects.
     * If one of those values isn't being asked for, or if the types are wrong,
     * then a empty bag is returned.
     *
     * @param attributeType
     *     the datatype of the attributes to find
     * @param attributeId
     *     the identifier of the attributes to find
     * @param issuer
     *     the issuer of the attributes, or null if unspecified
     * @param subjectCategory
     *     the category of the attribute or null
     * @param context
     *     the representation of the request data
     * @param designatorType
     *     the type of designator
     * @return
     *     the result of attribute retrieval, which will be a bag with
     *     a single attribute, an empty bag, or an error
     */
    public EvaluationResult findAttribute(URI attributeType,
    URI attributeId,
    URI issuer,
    URI subjectCategory,
    EvaluationCtx context,
    int designatorType) {
        log.trace("RegistryAttributeFinderModule.findAttribute: attributeId: " + attributeId);
        
        // figure out which attribute we're looking for
        String attrName = attributeId.toString();
        EvaluationResult res = null;
        
        if (attrName.startsWith(AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_PREFIX)) {
            res = handleRegistryResourceAttribute(attributeId, attributeType, context);
        }
        else if (attrName.startsWith(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_PREFIX)) {
            res = handleRegistrySubjectAttribute(attributeId,
            attributeType,
            subjectCategory,
            context);
        }
        else if (attrName.startsWith(AuthorizationServiceImpl.ACTION_ATTRIBUTE_PREFIX)) {
            res = handleRegistryActionAttribute(attributeId, attributeType, context);
        }
        
        
        if (res == null) {
            res = new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
        }
        
        String str = "RegistryAttributeFinderModule.findAttribute: " + attributeId + " = " + marshalEvaluationResult(res);
        log.trace(str);
        return res;
    }
    
    /**
     * Handles resource attributes as defined ebRIM.
     *
     * @param attributeId
     * @param type
     * @param context
     * @return
     */
    @SuppressWarnings("unused")
	private EvaluationResult handleRegistryResourceAttribute(URI attributeId,
    URI type,
    EvaluationCtx context) {
        EvaluationResult res = null;
        String attributeIdStr = attributeId.toString();
        Object obj = getResourceObject(context, AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_RESOURCE.toString());
        
        
        // Check if attribute is one of the special case attributes.
        // The resource owner is handled earlier and is added to the
        // context, so we just need to handle the selector attribute here.
        // If the attribute is not one of the special cases, then assume
        // it is an attribute of the resource object as defined by the ebRIM.
        if (false) { //(attributeIdStr.equals(REGISTRY_RESOURCE_SELECTOR)) {
            // TO DO:
        }
        else if (attributeIdStr.startsWith(AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_PREFIX)) {
            res = handleRegistryObjectAttribute(obj, getAttributeStackFromAttributeId(attributeId), type, context);
        }
        
        return res;
        
    }
    
    /**
     * Handles subject attributes as defined ebRIM.
     */
    private EvaluationResult handleRegistrySubjectAttribute(URI attributeId,
    URI type,
    URI subjectCategory,
    EvaluationCtx context) {
        EvaluationResult res = null;
        
        String attributeIdStr = attributeId.toString();
        Object user = getSubjectObject(context, AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_USER, subjectCategory);
        
        try {
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            
            // First check if attribute is role or group which are special cases since
            // they are not actual attributes in ebRIM.
            if (attributeIdStr.equals(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_ROLES)) {
                Set<AttributeValue> nodePaths = getClassificationNodePaths(requestContext, user, BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_SubjectRole);
                res = makeBag(nodePaths, new URI(StringAttribute.identifier));
            }
            else if (attributeIdStr.equals(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_GROUPS)) {
                Set<AttributeValue> nodePaths = getClassificationNodePaths(requestContext, user, BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_SubjectGroup);
                res = makeBag(nodePaths, new URI(StringAttribute.identifier));
            }
            else {
                // Not a role or group attribute
                // See if it is a RegistryObject attribute defined by ebRIM.
                res = handleRegistryObjectAttribute(user, getAttributeStackFromAttributeId(attributeId), type, context);
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (RegistryException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return res;
    }
    
    
    /**
     * Handles special action attributes as defined ebRIM or implementation.
     *
     * @param attributeId
     * @param type
     * @param context
     * @return
     */
    @SuppressWarnings("unused")
	private EvaluationResult handleRegistryActionAttribute(URI attributeId,
    URI type,
    EvaluationCtx context) {
        EvaluationResult res = null;
        String attributeIdStr = attributeId.toString();
        Object obj = getReferenceSourceObject(context);
        
        
        // Check if attribute is one of the special case attributes.
        // The resource owner is handled earlier and is added to the
        // context, so we just need to handle the selector attribute here.
        // If the attribute is not one of the special cases, then assume
        // it is an attribute of the resource object as defined by the ebRIM.
        if (false) { //(attributeIdStr.equals(REGISTRY_RESOURCE_SELECTOR)) {
            // TO DO:
        }
        else if (attributeIdStr.startsWith(AuthorizationServiceImpl.ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE_FILTER_PREFIX)) {
            res = handleRegistryObjectAttribute(obj, getAttributeStackFromAttributeId(attributeId), type, context);
        }
        
        return res;
        
    }
    
    /**
     * Gets the Set of id Strings for all the nodes that classify the specified object
     * within specified ClassificationScheme
     */
    private Set<AttributeValue> getClassificationNodePaths(ServerRequestContext requestContext, Object obj, String schemeId)
    throws RegistryException {
        HashSet<AttributeValue> nodePaths = new HashSet<AttributeValue>();
        
        try {
            if (obj instanceof RegistryObjectType) {
                RegistryObjectType ebRegistryObjectType = (RegistryObjectType) obj;
                List<ClassificationType> classifications = ebRegistryObjectType.getClassification();
                Iterator<ClassificationType> iter = classifications.iterator();
                
                while (iter.hasNext()) {
                    ClassificationType ebClassificationType =
                    iter.next();
                    String classificationNodeId = bu.getObjectId(ebClassificationType.getClassificationNode());
                    ClassificationNodeType ebClassificationNodeType =
                    (ClassificationNodeType)(QueryManagerFactory.getInstance()
                    .getQueryManager()
                    .getRegistryObject(requestContext, classificationNodeId));
                    String path = ebClassificationNodeType.getPath();
                    
                    if (path.startsWith("/" + schemeId + "/")) {
                        nodePaths.add(new StringAttribute(path));
                    }
                }
            }
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        } finally {
            requestContext.rollback();
        }
        
        return nodePaths;
    }
    
    /**
     * Handles attributes as defined ebRIM for Any RegistryObject.
     * Used by subject, resource and action attributes handling methods.
     */
    EvaluationResult handleRegistryObjectAttribute(
    Object obj,
    Stack<?> attributeStack,
    URI type,
    EvaluationCtx context) {
        EvaluationResult evaluationResult = null;
        try {
            String attr = (String)attributeStack.pop();
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            log.trace("handleRegistryObjectAttribute: obj=" + obj.toString() + " attrubute = " + attr);
            
            if (requestContext != null && obj != null) {
                @SuppressWarnings("unused")
				RegistryRequestType registryRequest = requestContext.getCurrentRegistryRequest();
                
                //Now invoke a get method to get the value for attribute being sought
                Class<? extends Object> clazz = obj.getClass();
                @SuppressWarnings("unused")
				String clazzName = clazz.getName();
                PropertyDescriptor propDesc = new PropertyDescriptor(attr, clazz, getReadMethodName(attr), null);
                Method method = propDesc.getReadMethod();
                Object attrValObj = method.invoke(obj, (java.lang.Object[])null);
                
                if (attrValObj instanceof Collection) {
                    HashSet<AttributeValue> attrValueObjectIds = new HashSet<AttributeValue>();
                    Iterator<?> iter = ((Collection<?>)attrValObj).iterator();
                    while (iter.hasNext()) {
                        //??Dangerous assumption that Collection is a Collection of IdentifiableTypes
                        String attrValueObjectId = ((IdentifiableType)iter.next()).getId();
                        attrValueObjectIds.add(makeAttribute(attrValueObjectId, type));
                    }
                    evaluationResult = makeBag(attrValueObjectIds, type);
                }
                else {
                    //See if more pointer chasing needs to be done or (!attributeStack.empty()) 
                    if (!attributeStack.empty()) {
                        String id = (String)attrValObj;
                        RegistryObjectType ro = AuthorizationServiceImpl.getInstance().getRegistryObject(requestContext, id, false);
                        if (ro == null) {
                            throw new ObjectNotFoundException(id, "RegistryObject");
                        }
                        evaluationResult = handleRegistryObjectAttribute(ro, attributeStack, type, context);
                    } else {
                        AttributeValue attrVal = makeAttribute(attrValObj, type);
                        evaluationResult = makeBag(attrVal, type);
                    }
                }
            }
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (IntrospectionException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        catch (ParsingException e) {
            e.printStackTrace();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (RegistryException e) {
            e.printStackTrace();
        }
       
        return evaluationResult;
    }
    
    private static String getReadMethodName(String propertyName) {
        String setterMethodName = "get" + capitalize(propertyName);
        return setterMethodName;
    }
    
    private static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }        
    
    /** Get the object from the specified subject attribute id.
     *
     * @param context
     * @param subjectAttributeId
     * @param subjectCategory
     * @return
     */
    private Object getSubjectObject(EvaluationCtx context, String subjectAttributeId, URI subjectCategory) {
        
        Object obj = null;
        try {
            EvaluationResult result = context.getSubjectAttribute
            (new URI(ObjectAttribute.identifier), new URI(subjectAttributeId), subjectCategory);
            AttributeValue attrValue = result.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            if (bagAttr.size() == 1) {
                Iterator<?> iter = bagAttr.iterator();
                ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                if (objAttr != null) {
                    obj = objAttr.getValue();
                }
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        return obj;
    }
    
    /** Get the object from the specified resource id.
     *
     * @param context
     * @param subjectAttributeId
     * @param subjectCategory
     * @return
     */
    private Object getResourceObject(EvaluationCtx context, String resourceAttributeId) {
        
        Object obj = null;
        try {
            EvaluationResult result = context.getResourceAttribute
            (new URI(ObjectAttribute.identifier), new URI(resourceAttributeId), null);
            AttributeValue attrValue = result.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            if (bagAttr.size() == 1) {
                Iterator<?> iter = bagAttr.iterator();
                ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                if (objAttr != null) {
                    obj = objAttr.getValue();
                }
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        return obj;
    }
    
    //Gets the RegistryObject that is the reference source object for a reference action
    private Object getReferenceSourceObject(EvaluationCtx context) {
        
        Object obj = null;
        try {
            EvaluationResult result = context.getActionAttribute
            (new URI(AnyURIAttribute.identifier), new URI(AuthorizationServiceImpl.ACTION_ATTRIBUTE_REFERENCE_SOURCE), null);
            AttributeValue attrValue = result.getAttributeValue();
            BagAttribute bagAttr = (BagAttribute)attrValue;
            if (bagAttr.size() == 1) {
                Iterator<?> iter = bagAttr.iterator();
                AnyURIAttribute attr = (AnyURIAttribute)iter.next();
                if (attr != null) {
                    String refSourceId = attr.getValue().toString();
                    
                    ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
                    obj = AuthorizationServiceImpl.getInstance().getRegistryObject(requestContext, refSourceId, false);
                }
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (RegistryException e) {
            e.printStackTrace();
        }
        
        return obj;
    }
        
    /**
     * Parses the attribute name from a URI rep of the Attribute id
     * If input is: "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:objectType"
     * then return value will be "objectType".
     **/
    private String getAttributeFromAttributeId(URI attributeId) {
        String attr = null;
        
        String attrIdStr = attributeId.toString();
        attr = attrIdStr.substring(attrIdStr.lastIndexOf(':') + 1,
        attrIdStr.length());
        
        return attr;
    }
    
    /**
     * Parses the attribute name from a URI rep of the Attribute id
     * If input is: "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:resource:sourceObject:targetObject:objectType"
     * then return value will be a Stack with entries "sourceObject;targetObject;objectType".
     **/
    private Stack<String> getAttributeStackFromAttributeId(URI attributeId) {
        Stack<String> attrStack = new Stack<String>();
        
        String attrIdStr = attributeId.toString();
        
        String relevantSuffix = null;
        if (attrIdStr.startsWith(AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_PREFIX))      {
            int startIndex = AuthorizationServiceImpl.RESOURCE_ATTRIBUTE_PREFIX.length();
            int endIndex = attrIdStr.length();
            relevantSuffix = attrIdStr.substring(startIndex,  endIndex);
        } else if (attrIdStr.startsWith(AuthorizationServiceImpl.SUBJECT_ATTRIBUTE_ID)) {
            //Special case. We should get rid of use of XACML subject-id attribute from spec??
            relevantSuffix = "id";
        } else if (attrIdStr.startsWith(AuthorizationServiceImpl.ACTION_ATTRIBUTE_PREFIX)) {
            //Not clear what to do here but following will preserve old behavior which may not have been right always
            relevantSuffix = getAttributeFromAttributeId(attributeId);
        }
        
        //Now split into attributes and push them onto stack
        String[] attrs = relevantSuffix.split(":");
        for (int i=attrs.length-1; i>=0; i--){
            attrStack.push(attrs[i]);
        }
        
        return attrStack;
    }
    
    /** Makes an AttributeValue from Object param using the attrType param and
     * the mapping specified in ebRIM between RIM types and XACML data types.
     */
    private AttributeValue makeAttribute(Object attrValObj, URI attrType)
    throws ParsingException, URISyntaxException, ParseException {
        AttributeValue val = null;
        String attrTypeStr = attrType.toString();
        
        if (attrValObj != null) {
            if (attrTypeStr.equals(BooleanAttribute.identifier)) {
                val = BooleanAttribute.getInstance(attrValObj.toString());
            }
            else if (attrTypeStr.equals(StringAttribute.identifier)) {
                val = StringAttribute.getInstance(attrValObj.toString());
            }
            else if (attrTypeStr.equals(AnyURIAttribute.identifier)) {
                val = AnyURIAttribute.getInstance(attrValObj.toString());
            }
            else if (attrTypeStr.equals(IntegerAttribute.identifier)) {
                val = IntegerAttribute.getInstance(attrValObj.toString());
            }
            else if (attrTypeStr.equals(DateTimeAttribute.identifier)) {
                val = DateTimeAttribute.getInstance(attrValObj.toString());
            }
        }
        
        return val;
    }
    
    /** Generate a new processing error status that includes the
     * specified message.
     */
    @SuppressWarnings("unused")
	private EvaluationResult makeProcessingError(String message) {
        List<String> code = new ArrayList<String>();
        code.add(Status.STATUS_PROCESSING_ERROR);
        return new EvaluationResult(new Status(code, message));
    }
    
    /** Creae an evaluation result whose bag contains only the given attribute.
     */
    private EvaluationResult makeBag(AttributeValue attribute, URI attrType) {
        Set<AttributeValue> set = new HashSet<AttributeValue>();
        
        if (attribute != null) {
            set.add(attribute);
        }
        BagAttribute bag = new BagAttribute(attrType, set);
        return new EvaluationResult(bag);
    }
    
    /** Create an evaluation result whose bag contains the specified set of attribute
     * values.
     */
    private EvaluationResult makeBag(Set<AttributeValue> attributeValues, URI attrType) {
        // TO DO: remove any attribute values from the set that
        // are not of the specified type.
        BagAttribute bag = new BagAttribute(attrType, attributeValues);
        return new EvaluationResult(bag);
    }
    
    private String marshalEvaluationResult(EvaluationResult res) {
        String str = "";
        BagAttribute bag = (BagAttribute)res.getAttributeValue();
        Iterator<?> iter = bag.iterator();
        while (iter.hasNext()) {
            str += iter.next();
        }
        
        return str;
    }
}
