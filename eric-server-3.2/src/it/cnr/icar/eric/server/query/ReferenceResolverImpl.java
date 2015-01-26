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
package it.cnr.icar.eric.server.query;

import it.cnr.icar.eric.client.common.CommonResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * The default implementation of the ReferenceResolver interface
 */
public class ReferenceResolverImpl implements ReferenceResolver {
    
    private static Log log = LogFactory.getLog(ReferenceResolverImpl.class);
    
    public static final String ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX = "eric.server.referenceResolver.associations.includeFilterList";
    public static final String ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX = "eric.server.referenceResolver.associations.excludeFilterList";
    private Map<String, String[]> assocIncludeFiltersMap = null;
    private Map<String, String[]> assocExcludeFiltersMap = null;
    
    /**
     * This method prefetches referenced objects up to the default specified 
     * depth level: 0 implies only fetch matched objects.
     *
     * @param context 
     * The ServerRequestContext used in this request
     * @param ro
     * The RegistryObjecType for which reference resolution is requested
     */
    public Collection<RegistryObjectType> getReferencedObjects(ServerRequestContext context, 
                                           RegistryObjectType ro)
        throws RegistryException {
        return this.getReferencedObjects(context, ro, 0);
    }
                                      
    /**
     * This method prefetches referenced objects up to specified depth level.
     * Depth = 0 (default) implies only fetch matched objects.
     * Depth = n implies, also fetch all objects referenced by matched
     * objects upto depth of n
     * Depth = -1 implies, also fetch all objects referenced by matched
     * objects upto any level.
     *
     * @param context
     * The ServerRequestContext used in this request
     * @param ro
     * The target RegistryObjectType for which referenced objects are requested
     * @param depth int
     * The depth of the target RegistryObjectType dependency resolution
     */
    public Collection<RegistryObjectType> getReferencedObjects(ServerRequestContext context, 
                                           RegistryObjectType ro, 
                                           int depth)
        throws RegistryException {
        
        log.trace("start: getReferencedObjects");
        ArrayList<RegistryObjectType> refObjs = new ArrayList<RegistryObjectType>();
        internalGetReferencedObjects(context, ro, depth, new HashMap<Object, Object>(), refObjs);        
        log.trace("end: getReferencedObjects");
        return refObjs;
    }
    
    /*
     * Gets the Collection of ReferenceInfos for all object references within specified RegistryObject.
     * TODO: replace with reflections API when JAXB bindings use special class for ReferenceURI.
     *
     * Reference attributes based on scanning rim.xsd for anyURI.
     *
     * @param ro specifies the RegistryObject whose ObjectRefs are being sought.
     *
     * @param idMap The Map with old temporary id to new permanent id mapping.
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void internalGetReferencedObjects(ServerRequestContext serverContext,
                                              RegistryObjectType ro, 
                                              int depth,                                                     Map<Object, Object> idMap, 
                                              Collection<RegistryObjectType> refObjs) 
        throws RegistryException {
        log.trace("start: internalGetReferencedObjects");
        try {
            if ((ro != null) && (!refObjs.contains(ro))) {
                if (log.isDebugEnabled()) {
                    log.debug("get references for this ro: "+ro.getId() + " "+ro.getObjectType());
                }
                refObjs.add(ro);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.RegistryObjectType", idMap, "ObjectType", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType", idMap, "Parent", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassificationNode", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassificationScheme", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ClassificationType", idMap, "ClassifiedObject", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", idMap, "IdentificationScheme", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType", idMap, "RegistryObject", refObjs);

                //FederationType fed = (FederationType)ro;
                //TODO: Fix so it adds only Strings not ObjectRefType
                //refInfos.addAll(fed.getMembers().getObjectRef());

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "AssociationType", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "SourceObject", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AssociationType1", idMap, "TargetObject", refObjs);


                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", idMap, "User", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.AuditableEventType", idMap, "RequestId", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.OrganizationType", idMap, "Parent", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.RegistryType", idMap, "Operator", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", idMap, "Service", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.ServiceBindingType", idMap, "TargetBinding", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", idMap, "ServiceBinding", refObjs);
                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType", idMap, "SpecificationObject", refObjs);

                processRefAttribute(serverContext, ro, "org.oasis.ebxml.registry.bindings.rim.SubscriptionType", idMap, "Selector", refObjs);
                
                if (depth != 0) {
                    depth--;
                    // Now process composed objects
                    Set<RegistryObjectType> composedObjects = BindingUtility.getInstance().getComposedRegistryObjects(ro, 1);
                    Collection<?> composedNoDups = checkForDuplicates(refObjs, composedObjects);
                    // Now process associated objects
                    Collection<?> associatedObjects = getAssociatedObjects(serverContext, ro, depth, idMap, refObjs);                                   
                    Collection assocNoDups = checkForDuplicates(refObjs, associatedObjects);
                    Collection relatedObjects = new ArrayList();
                    relatedObjects.addAll(composedNoDups);
                    relatedObjects.addAll(assocNoDups);
                    Iterator iter = relatedObjects.iterator();
                    while (iter.hasNext()) {
                        Object obj = iter.next();
                        if (obj instanceof RegistryObjectType) {
                            RegistryObjectType regObj = (RegistryObjectType)obj;
                            internalGetReferencedObjects(serverContext, regObj, depth, idMap, refObjs);
                        }
                    }
                }
            }
        } catch (RegistryException re) {
            throw re;
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        log.trace("end: internalGetReferencedObjects");
    }
    
    /*
     * This method gets all objects that are associated with the ro. These
     * objects are placed in the ref
     */
    private Collection<?> getAssociatedObjects(ServerRequestContext serverContext, 
                                            RegistryObjectType ro, 
                                            int depth,                                             Map<?, ?> idMap,
                                            Collection<?> refObjs) 
        throws RegistryException {
        log.trace("start: getAssociatedObjects");
        Collection<?> results = new ArrayList<Object>();
        try {
            String id = ro.getId();
            String sqlQuery = getSQLStringForGettingAssociatedObjects(ro);
            List<String> queryParams = new ArrayList<String>();
            queryParams.add(id.toUpperCase());
            ResponseOptionType ebResponseOptionType = BindingUtility.getInstance().queryFac.createResponseOptionType();
            ebResponseOptionType.setReturnComposedObjects(true);
            ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
            ArrayList<Object> objectRefs = new ArrayList<Object>();
            List<?> queryResults = PersistenceManagerFactory.getInstance()
                                               .getPersistenceManager()
                                               .executeSQLQuery(serverContext,
					                        sqlQuery,
                                                                queryParams,
                                                                ebResponseOptionType,
                                                                "RegistryObject",
                                                                objectRefs);
            if (queryResults != null) {
                results = queryResults; 
            }
        } catch (Throwable t) {
            throw new RegistryException(t);
        }
        log.trace("end: getAssociatedObjects");
        return results;
    }
    
    /*
     * This method checks to see if any of the members of the results collection
     * are contained in the running total refObjs collection.  If they are
     * contained, identify them as a duplicate and do not include in new 
     * no dups collection.
     */
    @SuppressWarnings("rawtypes")
	private Collection checkForDuplicates(Collection refObjects, Collection results) {
        log.trace("start: checkForDuplicates");
        @SuppressWarnings("unchecked")
		Collection resultsWithNoDups = new ArrayList(results);
        if (refObjects != null && results != null) {
            Iterator resultsItr = results.iterator();
            while (resultsItr.hasNext()) {
                Object obj = resultsItr.next();
                RegistryObjectType ro = null;
                if (obj instanceof RegistryObjectType) {
                    ro = (RegistryObjectType)obj;
                    String roId = ro.getId();
                    Iterator refItr = refObjects.iterator();
                    while (refItr.hasNext()) {
                        RegistryObjectType refRO = (RegistryObjectType)refItr.next();
                        if (refRO.getId().equalsIgnoreCase(roId)) {
                            resultsWithNoDups.remove(ro);
                        }
                    }
                }
            }
        }
        log.trace("end: checkForDuplicates");
        return resultsWithNoDups;
    }
    
    /*
     * This method gets any configured association filters as a String[].
     * These filters are configured in eric.properties.
     */
    @SuppressWarnings("unused")
	private String[] getAssocFilterStrings(RegistryObjectType ro) {
        log.trace("start: getAssocFilterStrings");
        String type = ro.getObjectType();
        // Use object type to get any configured filters
        // Includes filter takes precedence over excludes
        String[] filters = getAssociationIncludeFiltersMap().get(type);
        if (filters == null || filters.length > 0) {
            // If no includes filters, look for excludes
            filters = getAssociationExcludeFiltersMap().get(type);
        }
        log.trace("end: getAssocFilterStrings");
        return filters;
    }
    
    /*
     * This method is used to get the SQL string used in resolving any 
     * assocations of the target RO
     */
    private String getSQLStringForGettingAssociatedObjects(RegistryObjectType targetRO) {
        log.trace("start: getSQLString");
        StringBuffer sb = new StringBuffer();
        String sqlQueryFragment = "SELECT * FROM registryobject WHERE id IN "+
                               "(SELECT targetObject FROM association WHERE UPPER(sourceobject) = "+
                               "UPPER(?)";
        sb.append(sqlQueryFragment);
        // Since include filters take precedence over exclude filter, look
        // for them first
        String[] filters = getAssociationIncludeFiltersMap().get(targetRO.getObjectType());
        if (filters == null || filters.length == 0) {
            // Since no include filters, look for exclude filters
            filters = getAssociationExcludeFiltersMap().get(targetRO.getObjectType());
            // Process exclude filters
            sb.append(getAssocationTypeSQLPredicate(filters, true));
        } else {
            // Process includes filters
            sb.append(getAssocationTypeSQLPredicate(filters, false));
        }
        sb.append(')');        
        log.trace("end: getSQLString");
        return sb.toString();
    }
    
    /*
     * This method gets the correct assocationtype SQL predicate statement.
     * Association excludes filter adds a 'NOT' SQL keyword to the predicate.
     */
    private String getAssocationTypeSQLPredicate(String[] filters, boolean excludesFilter) {        
        log.trace("start: getAssocationTypeSQLPredicate");
        StringBuffer sb = new StringBuffer();
        if (filters != null && filters.length > 0) {
            sb.append(" and associationtype");
            if (excludesFilter) {
                sb.append(" not");
            }
            sb.append(" in (");
            for (int i= 0; i < filters.length;i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("'").append(filters[i]).append("'");
            }
            sb.append(')');
        }      
        log.trace("end: getAssocationTypeSQLPredicate");
        return sb.toString();
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
    @SuppressWarnings("unchecked")
	private void processRefAttribute(ServerRequestContext serverContext, 
                                     RegistryObjectType ro, 
                                     String className, 
                                     Map<?, ?> idMap, 
                                     String attribute, 
                                     @SuppressWarnings("rawtypes") Collection refObjs) throws JAXRException {
        log.trace("start: processRefAttribute");
        try {
            //Use reflections API to get the attribute value, check if it needs to be mapped
            //and set it with mapped value if needed and add the targetObject in refObjs
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
                    Class<?>[] parameterTypes = new Class[1];
                    Object[] parameterValues = new Object[1];
                    parameterTypes[0] = Class.forName("java.lang.String");
                    parameterValues[0] = targetObjectId;
                    String setMethodName = "set" + attribute;
                    Method setMethod = clazz.getMethod(setMethodName, parameterTypes);
                    setMethod.invoke(ro, parameterValues);
                }
                log.debug("sourceObject: "+ro.getId()+" targetObject: " + targetObjectId + " attribute: " + attribute);
                RegistryObjectType refObj = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(serverContext, targetObjectId);
                if (refObj != null && ! refObjs.contains(refObj)) {
                    refObjs.add(refObj);
                }
            }

        }
        catch (Exception e) {
            //throw new OMARExeption("Class = " ro.getClass() + " attribute = " + attribute", e);
            log.error(CommonResourceBundle.getInstance().getString("message.ErrorClassAttribute", new Object[]{ro.getClass(), attribute}));
            e.printStackTrace();
        }        
        log.trace("end: processRefAttribute");
    }
    
    /*
     * This method is used to get a java.util.Map of association include filters
     * configured in eric.properties
     */
    private Map<String, String[]> getAssociationIncludeFiltersMap() {
        log.trace("start: getAssociationIncludeFiltersMap");
        if (assocIncludeFiltersMap == null) {
            synchronized(this) {
                assocIncludeFiltersMap = new HashMap<String, String[]>();
                RegistryProperties props = RegistryProperties.getInstance();
                Iterator<String> propsIter = props.getPropertyNamesStartingWith(ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX);

                while (propsIter.hasNext()) {
                    String prop = propsIter.next();
                    String objectTypeForFilter = prop.substring(ASSOC_INCLUDE_FILTER_PROPERTY_PREFIX.length()+1);
                    String assocFilters = props.getProperty(prop);
                    String[] assocFilterArray = assocFilters.split("\\|");
                    assocIncludeFiltersMap.put(objectTypeForFilter, assocFilterArray);
                }
            }
        }
        log.trace("end: getAssociationIncludeFiltersMap");
        return assocIncludeFiltersMap;
    }
    
    /*
     * This method is used to get a java.util.Map of association exclude filters
     * configured in eric.properties
     */
    private Map<String, String[]> getAssociationExcludeFiltersMap() {
        log.trace("start: getAssociationExcludeFiltersMap");
        if (assocExcludeFiltersMap == null) {
            synchronized(this) {
                assocExcludeFiltersMap = new HashMap<String, String[]>();                
                // The Include Filter Map has precedence over the excludes. If an
                // includes filter is set, ignore the excludes filters
                if (getAssociationIncludeFiltersMap().size() == 0) {
                    RegistryProperties props = RegistryProperties.getInstance();
                    Iterator<String> propsIter = props.getPropertyNamesStartingWith(ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX);

                    while (propsIter.hasNext()) {
                        String prop = propsIter.next();
                        String objectTypeForFilter = prop.substring(ASSOC_EXCLUDE_FILTER_PROPERTY_PREFIX.length()+1);
                        String assocFilters = props.getProperty(prop);
                        String[] assocFilterArray = assocFilters.split("\\|");
                        assocExcludeFiltersMap.put(objectTypeForFilter, assocFilterArray);
                    }
                }
            }
        }
        log.trace("end: getAssociationExcludeFiltersMap");
        return assocExcludeFiltersMap;
    }    
   
}
