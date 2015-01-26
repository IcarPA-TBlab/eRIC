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
package it.cnr.icar.eric.client.ui.thin;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.lang.reflect.Method;
import javax.faces.application.Application;

import javax.faces.context.FacesContext;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryEntry;

import javax.xml.registry.JAXRException;
import it.cnr.icar.eric.client.ui.common.UIUtility;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SpecificationLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SlotImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExternalLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AssociationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.OrganizationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ObjectTypeConfigType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SearchResultsColumnType;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceBindingImpl;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CanonicalSchemes;

/**
 *
 * @author  xwsrrsf
 */
public class RegistryObjectBean implements java.io.Serializable {

    private RegistryObject registryObject;
    private LifeCycleManager lcm;
    private Object nonRegistryObject;
    private HashSet<?> relatedObjects;
    private RegistryObjectBean relatedObjectsDetails;
    private Map<String, Object> relatedObjectsLookup;
    private Map<String, List<?>> relatedObjectsTypeMap;
    private Map<?, List<?>> refLookup;
    private List<Object> slotList;
    private List<Object> telephoneNumberList;
    private List<Object> postalAddressList;
    private List<Object> emailAddressList;
    private String objectType;
    private String relatedObjectType;
    private Collection<String> relatedObjectTypesList = null;
    @SuppressWarnings("unused")
	private Object _value;
    private Map<?, ?> fields;
    private Log log = LogFactory.getLog(this.getClass());
    private Iterator<?> registryObjectRefsItr = null;
    private String id = null;
    private boolean relatedObjectInitialized = false;
    private Collection<SearchResultValueBean> searchResultValueBeans = null;
    private Iterator<SearchResultValueBean> searchResultValueBeansItr = null;
    private int valueBeanCounter = -1;
    private SearchResultValueBean srvb = null;
    private boolean isSelected = false;
    private boolean isRelatedSelected = false;
    private boolean isPinned = false;
    private boolean isInvalidSpecObj = false;
    private boolean isSpecificationObjectIsNull = false;
    private boolean isDirty = false;
    private String specificationObject=null;
    private boolean isNew = false;
    private ObjectTypeConfigType otCfg = null;
    private Iterator<?> srColItr = null;
    // if the user clicks a tab, do not update changed values in the form
    // If this is set to 'true' by the tab component, updates will be skipped
    private boolean ignoreFormUpdates = false;
    private String memberObjectId = null;
    private boolean removeRoFromRegistryPackage = false;
    private boolean addRoToRegistryPackage = false;
    private boolean saveChangesToDrilldownObject = false;

    static final long serialVersionUID = -3326494001436824271L;

    // This variable is used to toggle a workaround on and off.  The problem
    // is <h:commandLink /> tags with nested <f:param /> tags cause the
    // getValue method to be called an extra time.  This bug throws off the
    // proper iterating of this bean by the SearchResults.jsp page.
    // To skip the extra call caused by <f:param/>, set this boolean to 'true'.
    // For tags that do not have embedded <f:param/> tags, set to 'false'.
    // TO DO: fix this bug and remove workaround.
    private boolean enableIterateAdjustment = true;
    private boolean isRepositoryItemRemoved = false;
    private boolean isNewUpload = false;

    /** ID for objectType:
     * - ExtrinsicObject: eo.objectType
     * - Association: ass.associationType
     * - other: RIM type */
    private String extendedObjectType;

    /** Set of all the objectTypes that have a extended details page. */
    protected static final Set<String> jspExtensions = new HashSet<String>();

    /**
    * These variables are used for AccessURI and TragetBinding operation for
    * ServiceBinding.
    */
    private String registryObjectErrorMessage = null;
    private String targetBinding = null;
    private boolean isInvalidTargetBinding = false;
    private String accessURI = null;

    private String registryOperator = null;
    private boolean isInvalidRegistryOperator = false;

    /**
    * This variable is used for ExternalURI  for ExternalLink.
    */
    private String externalURI = null;
    private boolean isFirstAccess = true;
    private String statusTypeConceptId = null;
    private String currentDetailsPaneId = null;

    // Static initializer for jspExtensions
    static {
        final String JSP_EXTENSIONS_KEY = "jaxr-ebxml.thinbrowser.jsp-extensions";
        String st = ProviderProperties.getInstance().getProperty(JSP_EXTENSIONS_KEY, "");
        StringTokenizer tkz = new StringTokenizer(st,",");
        while (tkz.hasMoreTokens()) {
            jspExtensions.add(tkz.nextToken());
        }
    }


    public RegistryObjectBean() { }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans) {
        this.searchResultValueBeans = searchResultValueBeans;

        this.fields = new RegistryObjectBeanMap();
    }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
			  boolean enableIteratorAdjustment) {
		this(searchResultValueBeans);
		this.enableIterateAdjustment = enableIteratorAdjustment;
	}

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
                              RegistryObject registryObject) {
         this(searchResultValueBeans);
         this.registryObject = registryObject;
         if (registryObject != null) {
             try {
                this.id = registryObject.getKey().getId();
             } catch (JAXRException ex) {
                 log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetObjectTypeDefaultToRegistryObject"), ex);
                 this.objectType = "RegistryObject";
             }
             try {
                if (nonRegistryObject == null) {
                    this.objectType = registryObject.getObjectType().getValue();
                    Concept objectTypeConcept = registryObject.getObjectType();

                    if (registryObject instanceof Association) {
                        extendedObjectType = ((Association)registryObject)
                          .getAssociationType().getKey().getId();
                    } else {
                        extendedObjectType = objectTypeConcept.getKey().getId();
                    }
                    if (registryObject instanceof ExternalLink) {
                        this.objectType = "ExternalLink";
                    }
                    else if (objectTypeConcept.getPath().startsWith(
                        "/" + CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ExtrinsicObject/")) {
                        this.objectType = "ExtrinsicObject";
                    }
                }
            } catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorInConstructingRegistryObjectBean"), t);
            }
        }
    }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
                              RegistryObject registryObject,
                              String objectType,
                              Object nonRegistryObject) {
         this(searchResultValueBeans, registryObject);
         this.nonRegistryObject = nonRegistryObject;
         this.objectType = objectType;
    }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
                              RegistryObject registryObject,
                              boolean enableIterateAdjustment) {
         this(searchResultValueBeans, registryObject);
         this.enableIterateAdjustment = enableIterateAdjustment;
    }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
                              RegistryObject registryObject,
                              String objectType,
                              Object nonRegistryObject,
                              boolean enableIterateAdjustment) {
         this(searchResultValueBeans, registryObject, objectType, nonRegistryObject);
         this.enableIterateAdjustment = enableIterateAdjustment;
    }

    public RegistryObjectBean(Collection<SearchResultValueBean> searchResultValueBeans,
                              RegistryObject registryObject,
                              boolean enableIterateAdjustment,
                              ObjectTypeConfigType otCfg) {
         this(searchResultValueBeans, registryObject);
         this.enableIterateAdjustment = enableIterateAdjustment;
         this.otCfg = otCfg;
    }

    public LifeCycleManager getLifeCycleManager() {
        return lcm;
    }

    public Collection<SearchResultValueBean> getSearchResultValueBeans() {
        return searchResultValueBeans;
    }

    public int getNumberOfSearchResultValueBeans() {
        return searchResultValueBeans.size();
    }

    public String getHeader() {
        String header = null;
        if (searchResultValueBeansItr == null) {
            searchResultValueBeansItr = searchResultValueBeans.iterator();
        }
        if (searchResultValueBeansItr.hasNext()) {
            SearchResultValueBean result =
                searchResultValueBeansItr.next();
            header = result.getHeader();
            // If we are at the end of the iteration, reset to null
            if (!searchResultValueBeansItr.hasNext()) {
                searchResultValueBeansItr = null;
            }
        }
        return header;
    }

    public Collection<String> getHeaders() {
        List<String> headers = new ArrayList<String>();
        if (searchResultValueBeans != null) {
            Iterator<SearchResultValueBean> beans = searchResultValueBeans.iterator();
            while (beans.hasNext()) {
                SearchResultValueBean result = beans.next();
                headers.add(result.getHeader());
            }
        }
        return headers;
    }


    public SearchResultValueBean getData() {
        if (searchResultValueBeansItr == null) {
            searchResultValueBeansItr = searchResultValueBeans.iterator();
        }
        if (searchResultValueBeansItr.hasNext()) {
            srvb = searchResultValueBeansItr.next();
            if (!searchResultValueBeansItr.hasNext()) {
                searchResultValueBeansItr = null;
            }
        }

        return srvb;
    }


    public Object getValue() {
        Object value = null;
        valueBeanCounter++;
        if (enableIterateAdjustment) {
            // Workaround for <f:param /> calling this bean twice
            // Not needed with JSF 1.2
            try {             
                Application.class.getMethod("getExpressionFactory", (Class<?>[])null);
            } catch (NoSuchMethodException e) {
                // This exception indicates JSF 1.1 is being used. Therefore,
                // workaround is needed.
                if (valueBeanCounter == 1) {
                    return null;
                }
            }
        }
        if (otCfg == null) {
            if (searchResultValueBeansItr == null) {
                searchResultValueBeansItr = searchResultValueBeans.iterator();
            }
            if (searchResultValueBeansItr.hasNext()) {
                SearchResultValueBean result =
                    searchResultValueBeansItr.next();
                value = result.getValue();
                // If we are at the end of the iteration, reset to null
                if (!searchResultValueBeansItr.hasNext()) {
                    searchResultValueBeansItr = null;
                    valueBeanCounter = -1;
                }
            }
        } else {
            if (srColItr == null) {
                List<SearchResultsColumnType> srCols = otCfg.getSearchResultsConfig().getSearchResultsColumn();
                srColItr = srCols.iterator();
            }
            if (valueBeanCounter == 0) {
                try {
                    value = registryObject.getKey().getId();
                } catch (JAXRException ex) {
                    log.error(ex);
                }
            } else {
                if (srColItr.hasNext()) {
                    SearchResultsColumnType srColType =
                        (SearchResultsColumnType)srColItr.next();
                    try {
                        value = UIUtility.getInstance()
                                         .getColumnValue(srColType,
                                                         otCfg.getClassName(),
                                                         registryObject,
                                                         getLocale(),
                                                         getCharset());
                    } catch (Throwable t) {
                        log.error(t);
                    }
                    if (!srColItr.hasNext()) {
                        srColItr = null;
                        valueBeanCounter = -1;
                    }
                }
            }
        }
        return value;
    }

    /**
     * This method provides a level of abstraction so that object ids can be
     * determined for objects that either extend RegistryObject or do not
     * (such as Slots, TelephoneNumbers, PostalAddresses, etc.)
     */
    public String getId() throws JAXRException{
        String id = null;
        if (nonRegistryObject == null) {
            id = registryObject.getKey().getId();
        } else {
            id = registryObject.getKey().getId() + "." +
                nonRegistryObject.hashCode();
        }
        return id;
    }

    public RegistryObject getRegistryObject() {
        return registryObject;
    }

    public void setRegistryObject(RegistryObject registryObject) {
        this.registryObject = registryObject;
    }

    public Object getNonRegistryObject() {
        return nonRegistryObject;
    }

    public void setNonRegistryObject(Object nonRegistryObject) {
        this.nonRegistryObject = nonRegistryObject;
    }
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public RegistryObject getRelatedObject(String id) {
        return (RegistryObject)relatedObjectsLookup.get(id);
    }

    @SuppressWarnings("unused")
	private Map<?, ?> removeUnwantedTypes(Map<?, ?> refLookup) {
        HashMap<Object, Object> refLookupCopy = new HashMap<Object, Object>(refLookup);
        Set<?> keys = refLookup.keySet();
        Iterator<?> keyItr = keys.iterator();
        while (keyItr.hasNext()) {
            synchronized(this) {
                String key = (String)keyItr.next();
                if (key.indexOf("Association\n") != -1) {
                    refLookupCopy.remove(key);
                }
            }
        }
        return refLookupCopy;
    }

    private Map<String, List<?>> getRelatedObjectsTypeMap() {
        if (relatedObjectsTypeMap == null) {
            relatedObjectsTypeMap = new HashMap<String, List<?>>();
            try {
                Iterator<?> itr = ((RegistryObjectImpl)registryObject).getComposedObjects()
                                                                   .iterator();
                while (itr.hasNext()) {
                    Object obj = itr.next();
                    String className = obj.getClass().getName();
                    int index = className.lastIndexOf('.');
                    className = className.substring(index+1, className.length());
                    if (className.endsWith("Impl")) {
                        className = className.substring(0, className.length()-4);
                    }
                    if (className.endsWith("s")) {
                        className = className + "es";
                    } else {
                        className = className + "s";
                    }
                    @SuppressWarnings("unchecked")
					List<Object> relatedObjectList = (List<Object>) relatedObjectsTypeMap.get(className);
                    if (relatedObjectList == null) {
                        relatedObjectList = new ArrayList<Object>();
                    }
                    relatedObjectList.add(obj);
                    relatedObjectsTypeMap.put(className, relatedObjectList);
                }
            } catch (JAXRException ex) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetComposedObjectsForDrilldownObject"), ex);
            }
        }
        return relatedObjectsTypeMap;
    }

    public void initRelatedObjects() {
        if (!relatedObjectInitialized) {
            List<SearchResultValueBean> searchResultsValueBeans = new ArrayList<SearchResultValueBean>();
            try {
              //refLookup = (Map)getFields().get("relatedObjectsMap");
              refLookup = getRelatedObjectsTypeMap();
              Collection<?> relatedTypes = refLookup.keySet();
              // save all related Registry Objects in relatedObjectsLookup
              Iterator<?> typeItr = relatedTypes.iterator();
              while (typeItr.hasNext()) {
                 String objType = (String)typeItr.next();
                 SearchResultValueBean srvb = new SearchResultValueBean(
                     objType, objType);
                 searchResultsValueBeans.add(srvb);
                 Object relObj = refLookup.get(objType);
                 if (relObj instanceof Collection) {
                     Collection<?> relObjs = (Collection<?>)relObj;
                     Iterator<?> roItr = relObjs.iterator();
                     while (roItr.hasNext()) {
                         storeRelatedObject(roItr.next());
                     }
                 } else {
                     storeRelatedObject(relObj);
                 }
              }
              // 1 RegistryObjectBean containing multiple SearchResultValueBeans -
              // one value bean contains the object type and id for each RO
              relatedObjectsDetails = new RegistryObjectBean(searchResultsValueBeans);
              relatedObjectInitialized = true;
              this.lcm = registryObject.getLifeCycleManager();
            } catch (JAXRException ex) {
                String id = null;
                try {
                    id = getId();
                } catch (JAXRException jex) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetIdForRegistryObject"), jex);
                }
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotGetRelatedObjectsForRegistryObject", new Object[]{id}), ex);
            }
        }
    }

    private void storeRelatedObject(Object ro) throws JAXRException {

         if (ro instanceof Slot) {
             if (slotList == null) {
                 slotList = new ArrayList<Object>();
             }
             slotList.add(ro);
         } else if (ro instanceof TelephoneNumber) {
             if (telephoneNumberList == null) {
                 telephoneNumberList = new ArrayList<Object>();
             }
             telephoneNumberList.add(ro);
         } else if (ro instanceof PostalAddress) {
             if (postalAddressList == null) {
                 postalAddressList = new ArrayList<Object>();
             }
             postalAddressList.add(ro);
         } else if (ro instanceof EmailAddress) {
             if (emailAddressList == null) {
                 emailAddressList = new ArrayList<Object>();
             }
             emailAddressList.add(ro);
         } else {
             // TODO: what about non-RegistryObject types?
             if (ro instanceof RegistryObject) {
                 if (relatedObjectsLookup == null) {
                     relatedObjectsLookup = new HashMap<String, Object>();
                 }
                 relatedObjectsLookup.put(
                    ((RegistryObject)ro).getKey().getId(), ro);
             }
         }
    }

    public void removeRelatedObject(Object ro) throws JAXRException {

         if (ro instanceof Slot) {
             if (slotList != null) {
                 slotList.remove(ro);
             }
         } else if (ro instanceof TelephoneNumber) {
             if (telephoneNumberList != null) {
                 telephoneNumberList.remove(ro);
             }
         } else if (ro instanceof PostalAddress) {
             if (postalAddressList != null) {
                 postalAddressList.remove(ro);
             }
         } else if (ro instanceof EmailAddress) {
             if (emailAddressList != null) {
                 emailAddressList.remove(ro);
             }
         } else {
             // TODO: what about non-RegistryObject types?
             if (ro instanceof RegistryObject) {
                if (relatedObjectsLookup != null) {
                    relatedObjectsLookup.remove(((RegistryObject)ro).getKey().getId());
                }
                String className = ro.getClass().getName();
                int index = className.lastIndexOf('.');
                className = className.substring(index+1, className.length());
                if (className.endsWith("Impl")) {
                    className = className.substring(0, className.length()-4);
                }
                if (className.endsWith("s")) {
                    className = className + "es";
                } else {
                    className = className + "s";
                }
                List<?> relatedObjectList = relatedObjectsTypeMap.get(className);
                if (relatedObjectList != null && !relatedObjectList.isEmpty()){
                    relatedObjectList.remove(ro);
                }
             }
         }
    }

    /* pass object type name to Collections.jsp as arguemtn
     * Collections.jsp can use the refLookup map to get refs by type name
     */
      public String getObjectTypesLabel() {

         // ServiceBinding, ExternalLink, etc.
         // Details.jsp will iterator over this collection to show tabs
         if (registryObjectRefsItr == null) {
             registryObjectRefsItr = refLookup.keySet().iterator();
         }

         if (registryObjectRefsItr.hasNext()) {
            relatedObjectType = (String)registryObjectRefsItr.next();
            //ojectTypeRefs = getObjectTypeRefs(relatedObjectType);
            if (!registryObjectRefsItr.hasNext()) {
                registryObjectRefsItr = null;
            }
         }
         return relatedObjectType;
      }

      public Collection<String> getRelatedObjectTypesList() {
          if (relatedObjectTypesList == null) {
              relatedObjectTypesList = new ArrayList<String>();
              relatedObjectTypesList.add("Slots");
              relatedObjectTypesList.add("Classifications");
              relatedObjectTypesList.add("ExternalIdentifiers");
              relatedObjectTypesList.add("Associations");
              relatedObjectTypesList.add("ExternalLinks");
              try {
                  String objectType = registryObject.getObjectType().getValue();
                  if (objectType.equalsIgnoreCase("Organization") ||
                      objectType.equalsIgnoreCase("User")) {
                      relatedObjectTypesList.add("PostalAddresses");
                      relatedObjectTypesList.add("TelephoneNumbers");
                      relatedObjectTypesList.add("EmailAddresses");
                      if (objectType.equalsIgnoreCase("Organization")) {
                        relatedObjectTypesList.add("Users");
                        relatedObjectTypesList.add("Organizations");
                      }
                  } else if (objectType.equalsIgnoreCase("Service")) {
                      relatedObjectTypesList.add("ServiceBindings");
                  } else if (objectType.equalsIgnoreCase("ServiceBinding")) {
                      relatedObjectTypesList.add("SpecificationLinks");
                  } else if (objectType.equalsIgnoreCase("ClassificationScheme")) {
                      relatedObjectTypesList.add("Concepts");
                  } else if (objectType.equalsIgnoreCase("ClassificationNode")) {
                      relatedObjectTypesList.add("Concepts");
                  } else if (objectType.equalsIgnoreCase("RegistryPackage")) {
                      relatedObjectTypesList.add("RegistryObjects");
                  } else if (objectType.equalsIgnoreCase("AuditableEvent")) {
                      relatedObjectTypesList.add("AffectedObjects");
                  }
              } catch (JAXRException ex) {
                  log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetRelatedObjectType"), ex);
              }
          }
          return relatedObjectTypesList;

      }

      public List<RegistryObject> getObjectTypeRefs(String objectTypeName) {
          List<RegistryObject> list = new ArrayList<RegistryObject>();
          Collection<?> typeRefs = refLookup.get((Object)objectTypeName);
          if (typeRefs != null) {
              Iterator<?> typeRefsItr = typeRefs.iterator();
              while (typeRefsItr.hasNext()) {
                  list.add((RegistryObject) typeRefsItr.next());
              }
          }
          return list;
      }

    public List<?> getSlots() {
        return slotList;
    }

    public List<?> getTelephoneNumbers() {
        return telephoneNumberList;
    }

    public List<?> getPostalAddresses() {
        return postalAddressList;
    }

    public List<?> getEmailAddresses() {
        return emailAddressList;
    }

    public RegistryObjectBean getRelatedObjectsDetails() {
        return relatedObjectsDetails;
    }

    public int getNumberOfRelatedObjects() {
        return relatedObjects.size();
    }

    public Map<?, ?> getFields() {
        return fields;
    }


    public boolean isSelected() {
        log.debug("ROB "+ id+" selected state: " + isSelected);
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        log.debug("ROB "+ id +" set selected state: " + isSelected);
        this.isSelected = isSelected;
    }

    public boolean isRelatedSelected() {
        log.debug("ROB "+ id+" related selected state: " + isRelatedSelected);
        return isRelatedSelected;
    }

    public void setRelatedSelected(boolean isRelatedSelected) {
        log.debug("ROB "+ id +" set related selected state: " + isRelatedSelected);
        this.isRelatedSelected = isRelatedSelected;
    }

    public boolean isPinned() {
        log.debug("ROB "+ id+" pinned state: " + isPinned);
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        log.debug("ROB "+ id +" set pinned state: " + isPinned);
        this.isPinned = isPinned;
    }

    class RegistryObjectBeanMap implements Map<Object, Object> {

        public void clear()
        {
        }

        public boolean containsKey(Object key)
        {
            return true;
        }

        public boolean containsValue(Object value)
        {
            return true;
        }

        public Set<java.util.Map.Entry<Object, Object>> entrySet()
        {
            return new HashSet<Entry<Object, Object>>();
        }

        public Object get(Object key) {
            boolean isUrlEncoded = false;
            if (key instanceof String && ((String)key).startsWith("url_encoded_")) {
                isUrlEncoded = true;
                key = ((String)key).substring(12);
            }

            Object value = null;
            Object invokedObject = null;
            if (nonRegistryObject == null) {
                invokedObject = registryObject;
            } else {
                invokedObject = nonRegistryObject;
            }
            @SuppressWarnings("unused")
			Class<? extends Object> clazz = invokedObject.getClass();
            String propertyName = null;
            try {
                ValueObject vo = getValueFromKey(key, invokedObject);
                value = vo.value;
            }
            catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.FailedToInvokeTheGetterMethodForTheProperty", new Object[] {propertyName}), t);
            }

            if (isUrlEncoded && value != null) {
                try {
                    value = URLEncoder.encode(value.toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("Exception", e);
                }
            }

            return value;
        }

        private Collection<?> handleSets(Collection<?> values) {
            return new ArrayList<Object>(values);
        }

        public boolean isEmpty()
        {
            return true;
        }

        public Set<Object> keySet()
        {
            return new HashSet<Object>();
        }

        private ValueObject getValueFromKey(Object key, Object invokedObject) {
            Object value = null;
            Method m = null;
            String propertyName = null;
            Class<? extends Object> clazz = invokedObject.getClass();
            try {
                if (((String)key).startsWith("is")) {
                    propertyName = (String)key;
                    m = clazz.getMethod(propertyName, (java.lang.Class[])null);
                } else {
                    propertyName = ((String)key).substring(0,1).toUpperCase() +
                            ((String)key).substring(1);
                    m = clazz.getMethod("get" + propertyName, (java.lang.Class[])null);
                }

                value = m.invoke(invokedObject, (java.lang.Object[])null);
                if (value instanceof Collection) {
                    if (!(value instanceof Map) || (value instanceof List)) {
                        value = handleSets((Collection<?>)value);
                    }
                } else if (value instanceof InternationalStringImpl) {
                    value = UIUtility.getInstance().convertValue(value,
                        getLocale(), getCharset());
                }
            }
            catch (NoSuchMethodException nsme) {
                log.debug("Could not find the method 'get" + propertyName + "'");
                try {
                    m = clazz.getMethod("is" + propertyName, (java.lang.Class[])null);
                    value = m.invoke(invokedObject, (java.lang.Object[])null);
                }
                catch (NoSuchMethodException nsme2) {
                    log.debug("Could not find the method 'is" + propertyName + "'");
                    try {
                        m = clazz.getMethod("to" + propertyName, (java.lang.Class[])null);
                        value = m.invoke(invokedObject, (java.lang.Object[])null);
                    }
                    catch (NoSuchMethodException nsme3) {
                        log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotFindTheGetterMethod", new Object[]{propertyName}));
                    }
                    catch (Throwable ex) {
                        log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotAccessTheGetterMethod", new Object[]{propertyName}));
                    }
                }
                catch (Throwable iae) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotAccessTheGetterMethod", new Object[]{propertyName}));
                }
            }
            catch (Throwable ex) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.ErrorCannotAccessMethod", new Object[]{propertyName}), ex);
            }
            return (new ValueObject(value, m));
        }

	public Object put(Object key, Object value)
        {
            Object invokedObject = null;
            if (nonRegistryObject == null) {
                invokedObject = registryObject;
            } else {
                invokedObject = nonRegistryObject;
            }
            String propertyName = ((String)key).substring(0,1).toUpperCase() +
                ((String)key).substring(1);
            try {
                ValueObject vo = getValueFromKey(key, invokedObject);
                Method getter = vo.method;
                if (ignoreFormUpdates) {
                    // if we are ignoring updates, use the current value
                    value = vo.value;
                }
                Class<? extends Object> clazz = invokedObject.getClass();
                Class<?> argClass[] = new Class[1];
                argClass[0] = getter.getReturnType();
		Method m = null;
                if (propertyName.equalsIgnoreCase("Id")) {
                    invokedObject = ((RegistryObject)invokedObject).getKey();
                    clazz = invokedObject.getClass();
                    argClass[0] = java.lang.String.class;
                    m = clazz.getMethod("set" + propertyName, argClass);
                } else {
                    m = clazz.getMethod("set" + propertyName, argClass);
                }
                if (m == null) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotFindFollowingMethodSet", new Object[]{key}));
                }
                Object[] args = new Object[1];
                convertValue(argClass[0], args, value);
                value = m.invoke(invokedObject, args);
            } catch (Exception ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotInvokeFollowingMethodSet", new Object[]{key}), ex);
            }
            return value;
        }

        private void convertValue(Class<?> argClass, Object[] args, Object object)
        {
            if (argClass == String.class) {
                args[0] = object;
            } else if (argClass == int.class) {
                if (object instanceof Integer) {
                    args[0] = object;
                } else {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.intRequiresIntegerClassWrapperCannotUseThisClass", new Object[]{object}));
                }
            } else if (argClass == boolean.class) {
                if (object instanceof Boolean) {
                    args[0] = object;
                } else {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.booleanRequiresBooleanClassWrapperCannotUseThisClass", new Object[]{object}));
                }
            } else if (argClass == javax.xml.registry.infomodel.InternationalString.class) {
                String sValue = (String)object;
                Locale locale = RegistryBrowser.getInstance()
                                               .getUserPreferencesBean()
                                               .getContentLocale();
                InternationalString iString = null;
                try {
                    if (sValue.length() > 0) {
                        iString = RegistryBrowser.getBLCM()
                                                 .createInternationalString(locale,
                                                                            sValue);
                    }
                } catch (Throwable t) {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotCreateInternationalString"), t);
                    log.warn(WebUIResourceBundle.getInstance().getString("message.TryingToSaveWithDefaultLocale"));
                    locale = Locale.getDefault();
                    try {
                        iString = RegistryBrowser.getBLCM()
                                                 .createInternationalString(locale,
                                                                            sValue);
                    } catch (Throwable t2) {
                        log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotCreateInternationalStringWithDefaultLocale"));
                    }
                }
                args[0] = iString;
            }
        }

        public void putAll(Map<?, ?> t)
        {
        }

        public Object remove(Object key)
        {
            return new Object();
        }

        public int size()
        {
            return 1;
        }

        public Collection<Object> values()
        {
            return new ArrayList<Object>();
        }

        class ValueObject {
            Object value = null;
            Method method = null;
            ValueObject(Object value, Method method) {
                this.value = value;
                this.method = method;
            }
        }

        @SuppressWarnings("unused")
		private RegistryObjectBean getRegistryObjectBean(){
            return RegistryObjectBean.this;
        }


    }

    public Locale getLocale() {
        UserPreferencesBean userPreferencesBean =
            (UserPreferencesBean)FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("userPreferencesBean");
        return userPreferencesBean.getContentLocale();
    }

    public String getCharset() {
        UserPreferencesBean userPreferencesBean =
            (UserPreferencesBean)FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("userPreferencesBean");
        return userPreferencesBean.getCharset();
    }

    public Map<String, Object> getRelatedObjectsLookup() {
        if (relatedObjectsLookup == null) {
            relatedObjectsLookup = new HashMap<String, Object>();
        }
        return relatedObjectsLookup;
    }

    public boolean isRepositoryItemRemoved(){
        return isRepositoryItemRemoved;
    }
    public void setRepositoryItemRemoved(boolean isRepositoryItemRemoved) {
        this.isRepositoryItemRemoved = isRepositoryItemRemoved;

    }

    /** Checks if this objectType is set to have extended details. */
    protected boolean hasJSPExtension(String objectType, String extendedObjectType) {
        // only for ExtrinsicObject or Association, objectType changes according
        // to displayed tab
        return jspExtensions.contains(extendedObjectType) && (
                "ExtrinsicObject".equals(objectType) ||
                "Association".equals(objectType));
    }

    /** Used by Details.jsp and DetailsWrapper.jsp to import the type specific details page. */
    public String getDetailsPageName() {
        if (hasJSPExtension(objectType, extendedObjectType)) {
            // return the file name like -version of the extendedObjectType,
            // in a 'ext' subdir
            return "ext/" + makeFileName(extendedObjectType);
        } else {
            // returns the plain object type, as before
            return objectType;
        }
    }

    /** Makes a valid filename from the given urn. */
    public String makeFileName(String urn) {
        return urn.replaceAll("[^a-zA-Z0-9\\-]","_");
    }


    public void resetRelatedObjects() {
        relatedObjectInitialized = false;
        slotList = null;
        telephoneNumberList = null;
        postalAddressList = null;
        emailAddressList = null;
        relatedObjectsTypeMap = null;
    }

    public void setSlotValues(String valuesString)
        throws JAXRException {
        try {
            String[] values = valuesString.split("\\|");
            ArrayList<String> valList = new ArrayList<String>();
            for (int i = 0; i < values.length; i++) {
                valList.add(values[i]);
            }
            // Presume that the nonRegistryObject is a Slot when this method
            // is invoked. Catch ClassCastExceptions
            Slot slot = (SlotImpl)this.nonRegistryObject;
            slot.setValues(valList);
        } catch (ClassCastException ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.TheNonRegistryObjectIsNotASlot"));
        }
    }

    public String getSlotValues() throws JAXRException {
        StringBuffer sb = new StringBuffer("");
        // Presume that the nonRegistryObject is a Slot when this method
        // is invoked. Catch ClassCastExceptions
        try {
            Slot slot = (SlotImpl)this.nonRegistryObject;
            Iterator<?> valItr = slot.getValues().iterator();
            for (int i = 0; valItr.hasNext(); i++) {
                if (i > 0) {
                    sb.append('|');
                }
                sb.append(valItr.next());
            }
        } catch (ClassCastException ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.TheNonRegistryObjectIsNotASlot"));
        }
        return sb.toString();
    }

    public void setUsageParamString(String usageParamString) throws JAXRException {
        try{
            String[] values = usageParamString.split("\\|");
            List<String> valList = new ArrayList<String>();
            for (int i = 0; i < values.length; i++) {
                valList.add(values[i].trim());
            }
            // Presume that the nonRegistryObject is a SpecificationLink when
            // this method is invoked. Catch ClassCastExceptions
            SpecificationLink specLink = (SpecificationLink)this.registryObject;
            specLink.setUsageParameters(valList);
        }catch(ClassCastException ex){
            log.error("The non registry object is not a SpecificationLink");
        }
    }

    public String getUsageParamString() throws JAXRException {
        StringBuffer sb = new StringBuffer("");
        // Presume that the nonRegistryObject is a SpecificationLink when
        // this method is invoked. Catch ClassCastExceptions
        try {
            SpecificationLink specLink =
                    (SpecificationLink)this.registryObject;
        Iterator<?> valItr = specLink.getUsageParameters().iterator();
        for (int i = 0; valItr.hasNext(); i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(valItr.next());
        }
        }catch (ClassCastException ex) {
            log.error("The non registry object is not a SpecificationLink");
        }
        return sb.toString();
    }

   /**
    *Used to find if SpecificationObject is valid or not for SpecificationLink
    *
    */
    public boolean getIsInvalidSpecObj(){
        return this.isInvalidSpecObj;
    }

    public void setIsInvalidSpecObj(boolean isInvalidSpecObj){
        this.isInvalidSpecObj = isInvalidSpecObj;
    }

   /**
    * Getter method UUID for specificationObject for SpeceificationLink
    * @ return String
    */
    public String getspecificationObject() {
       try{
            if(this.specificationObject == null &&
                    !this.isInvalidSpecObj && !this.isSpecificationObjectIsNull){
                this.specificationObject =
                        (((SpecificationLinkImpl)
                        this.registryObject).
                        getSpecificationObject()).getKey().getId();
            }
        } catch(Exception ex){
            log.error("unable to get Specification Object"+ex.getMessage());
       }
        return specificationObject;
    }


    /**
    * Setter method UUID for specificationObject for SpeceificationLink
    * @param String
    */
    public void setspecificationObject(String specificationObject) {
        this.specificationObject = specificationObject;
        RegistryObject ro = this.registryObject;
        RegistryObject specObj = null;
        this.isInvalidSpecObj = false;
        this.isSpecificationObjectIsNull = false;

        try {
            if (this.specificationObject.trim().length() > 0){
                @SuppressWarnings("static-access")
				DeclarativeQueryManagerImpl dqm =
                RegistryBrowser.getInstance().getDQM();
                specObj = dqm.getRegistryObject(this.specificationObject);
                if (specObj != null){
                    ((SpecificationLinkImpl)ro).setSpecificationObject(specObj);
                }else {
                    this.setIsInvalidSpecObj(true);
                    this.specificationObject = null;
                }
            } else {
                this.isSpecificationObjectIsNull = true;
                this.specificationObject = null;
            }

        }catch(Exception ex){
            log.error(ex.getMessage());
        }
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isFormUpdateIgnored() {
        return ignoreFormUpdates;
    }

    public void setFormUpdateIgnored(boolean ignoreFormUpdates) {
        this.ignoreFormUpdates = ignoreFormUpdates;
    }

    public String getRegistryObjectErrorMessage(){
        return this.registryObjectErrorMessage;
    }

    public void setRegistryObjectErrorMessage(String registryObjectErrorMessage){
        this.registryObjectErrorMessage = registryObjectErrorMessage;
    }


    /**
    * Getter method UUID for targetBinding for ServiceBinding
    * @ return String
    */
    public String getTargetBindingForSerBinding() {
       try{
            if(this.targetBinding == null){
                if ((((ServiceBindingImpl)this.registryObject)
                        .getTargetBinding()) != null) {
                    this.targetBinding =
                            (((ServiceBindingImpl)
                            this.registryObject).getTargetBinding())
                            .getKey().getId();
                }
            }
        } catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("errorGettingTargetBinding");
            log.error(errMsg+" "+ex.getMessage());
       }
        return targetBinding;
    }

   /**
    * Setter method UUID for targetBinding for ServiceBinding
    * @param String
    */
    public void setTargetBindingForSerBinding(String targetBinding) {
        this.targetBinding = targetBinding;
    }

    /**
    * Change the current targetBinding Object for ServiceBinding
    * @param none
    * @return none
    */
    public void changeTargetBindingForSerBinding() {
        RegistryObject ro = this.registryObject;
        RegistryObject targetBindObj = null;
        this.isInvalidTargetBinding = false;
        try {
            if (this.targetBinding.trim().length() > 0) {
                @SuppressWarnings("static-access")
				DeclarativeQueryManagerImpl dqm =
                RegistryBrowser.getInstance().getDQM();
                targetBindObj = dqm.getRegistryObject(this.targetBinding);
                if (targetBindObj != null){
                    if(!this.targetBinding.equals(this.getId()) &&
                            targetBindObj instanceof ServiceBindingImpl) {
                        ((ServiceBindingImpl)ro)
                        .setTargetBinding((ServiceBindingImpl)targetBindObj);
                    }else {
                        this.setIsInvalidTargetBinding(true);
                        this.targetBinding = null;
                    }
                } else {
                        this.setIsInvalidTargetBinding(true);
                        this.targetBinding=null;
                }
             }else{
                    ((ServiceBindingImpl)ro).setTargetBinding(null);
             }

        }catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeTargetBindingError");
            log.error(errMsg+" "+ex.getMessage());

            log.error(ex.getMessage());
            setRegistryObjectErrorMessage(ex.getMessage());
        }
    }

    /**
    * Used to find if TargetBinding is valid or not for ServiceBinding
    *
    */
    public boolean getIsInvalidTargetBinding(){
        return this.isInvalidTargetBinding;
    }

    public void setIsInvalidTargetBinding(boolean isInvalidTargetBinding){
        this.isInvalidTargetBinding = isInvalidTargetBinding;
    }

    /**
     * Getter method for AccessURI
     * @return String
     */
    public String getAccessURI(){
          try{
            if(this.accessURI == null){
                this.accessURI =
                ((ServiceBindingImpl)this.registryObject).getAccessURI();
             }
        } catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("errorGettingAccessURI");
            log.error(errMsg+" "+ex.getMessage());
       }
        return accessURI;
    }
    /**
     * Setter Method for AccessURI used for ServiceBinding
     * @param String
     */
    public void setAccessURI(String accessURI){
        this.accessURI = accessURI;
    }

    /**
    * Change the current targetBinding Object for ServiceBinding
    * @param none
    * @return none
    */
    public void changeAccessURI(){
        RegistryObject ro = this.registryObject;
        try {
               ((ServiceBindingImpl)ro).setAccessURI(accessURI);
        }catch(Exception ex){
            setRegistryObjectErrorMessage("AccessURI "+ex.getMessage());
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeAccessURIError");
            log.error(errMsg+" "+ex.getMessage());

        }
    }
   /**
    * Getter method for ExternalURI
    * @return String
    */
    public String getExternalURI(){
          try{
            if(this.externalURI == null){
                this.externalURI =
                ((ExternalLinkImpl)this.registryObject).getExternalURI();
             }
        } catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("errorGettingExternalURI");
            log.error(errMsg+" "+ex.getMessage());
       }
        return externalURI;
    }

   /**
    * Setter Method ExternalURI used for ExternalLink
    * @param String
    */
    public void setExternalURI(String externalURI){
        this.externalURI = externalURI;
    }

   /**
    * Change the current ExternalURI for ExternalLink
    * @param none
    * @return none
    */
    public void changeExternalURI(){
        RegistryObject ro = this.registryObject;
        try {
            if(externalURI != null && externalURI.trim().length() >= 0) {
                ((ExternalLinkImpl)ro).setExternalURI(externalURI);
            }else if (this.externalURI == null && ro instanceof ExternalLinkImpl) {
                ((ExternalLinkImpl)ro).setExternalURI(((ExternalLinkImpl)ro).getExternalURI());
            }
        }catch(NullPointerException nep) {
            setRegistryObjectErrorMessage(WebUIResourceBundle
                    .getInstance().getString("nullExternalURIError"));
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeExternalURIError");
            log.error(errMsg+" "+nep.getMessage());
        } catch(java.lang.RuntimeException re) {
            if (re.getCause() instanceof IllegalArgumentException ){
                setRegistryObjectErrorMessage(WebUIResourceBundle.getInstance()
                .getString("illegalExternalURIError"));
            }
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeExternalURIError");
            log.error(errMsg+" "+re.getMessage());
        } catch(Exception ex) {
            setRegistryObjectErrorMessage("ExternalURI "+ex.getMessage());
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeExternalURIError");
            log.error(errMsg+" "+ex.getMessage());

        }
    }

    public String getMemberObjectId() {
        return memberObjectId;
    }

    public void setMemberObjectId(String memberObjectId) {
        this.memberObjectId = memberObjectId;
    }

    public void setRemoveRoFromRegistryPackage(boolean removeRoFromRegistryPackage) {
        this.removeRoFromRegistryPackage = removeRoFromRegistryPackage;
    }

    public boolean isRemoveRoFromRegistryPackage() {
        return removeRoFromRegistryPackage;
    }
    
    public void setAddRoToRegistryPackage(boolean addRoToRegistryPackage) {
        this.addRoToRegistryPackage = addRoToRegistryPackage;
    }

    public boolean isAddRoToRegistryPackage() {
        return addRoToRegistryPackage;
    }

    public String getAssociationType() {
        String associationType = null;
        try{
            associationType =(((AssociationImpl)this.registryObject)
                .getAssociationType()).getKey().getId();
        }catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
            getString("message.ErrorInGettingAssociationType");
            OutputExceptions.error(log, errMsg, ex);
        }
        return associationType;
    }

    public void setAssociationType(String associationType) {
        @SuppressWarnings("unused")
		Class<?> clazz = null;
        @SuppressWarnings("unused")
		String className = null;
        try{
            Concept concept = (Concept)RegistryBrowser.getBQM().getRegistryObject(associationType);
            ((AssociationImpl)this.registryObject).setAssociationType(concept);
        }catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
            getString("message.ErrorInSettingAssociationType");
            OutputExceptions.error(log, errMsg, ex);
        }
    }

    public boolean isFirstAccess() {
        return isFirstAccess;
    }

    public void setFirstAccess(boolean isFirstAccess) {
        this.isFirstAccess = isFirstAccess;
    }

    public void setName (String newIS) {
        try {
            InternationalStringImpl is = ((InternationalStringImpl)registryObject.getName());
            Locale locale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            String clstValue = is.getClosestValue(locale);
            String curValue = is.getValue(locale);
            if ((null == curValue && !newIS.equals("") && !newIS.equals(clstValue))
                || (!(null == curValue && newIS.equals("")) && !newIS.equals(curValue))) {
                LocalizedString ls = (is).getLocalizedString(locale, null);
                if (ls == null) {
                    ls = RegistryBrowser.getBLCM()
                                        .createLocalizedString(locale, newIS);
                    is.addLocalizedString(ls);
                } else {
                    ls.setValue(newIS);
                }
                registryObject.setName(is);
            }
        } catch (Exception ex) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInSettingName"), ex);
        }
    }

    public String getName() {
        String name = "";
        try {
            InternationalStringImpl iString = ((InternationalStringImpl)registryObject.getName());
            Locale locale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            name = iString.getClosestValue(locale);
        } catch(Exception e) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInGettingName"), e);
        }
        return name;
    }

    public String getNameContentLocale() {
        String contentLocaleStr = null;
        try {
            Locale contentLocale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            InternationalStringImpl iString = ((InternationalStringImpl)registryObject.getName());
            LocalizedString lString = null;
            if (iString != null) {
                lString = iString.getClosestLocalizedString(contentLocale, null);
            }
            if (lString == null) {
                contentLocaleStr = contentLocale.toString();
            } else {
                Locale locale = lString.getLocale();
                if (locale == null) {
                    locale = Locale.getDefault();
                }
                contentLocaleStr = locale.toString();
            }
        } catch(Exception e) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInGettingNameContent"), e);
        } finally {
            if (contentLocaleStr == null) {
                contentLocaleStr = Locale.getDefault().toString();
            }
        }
        return contentLocaleStr;
    }

    public void setDescription (String desc){
        try {
            InternationalStringImpl is = ((InternationalStringImpl)registryObject.getDescription());
            Locale locale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            String clstValue = is.getClosestValue(locale);
            String curValue = is.getValue(locale);
            if ((null == curValue && !desc.equals("") && !desc.equals(clstValue))
                || (!(null == curValue && desc.equals("")) && !desc.equals(curValue))) {
                LocalizedString ls = (is).getLocalizedString(locale, null);
                if (ls == null) {
                    ls = RegistryBrowser.getBLCM()
                                        .createLocalizedString(locale, desc);
                    is.addLocalizedString(ls);
                } else {
                    ls.setValue(desc);
                }
                registryObject.setDescription(is);
            }
        } catch (Exception ex) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInSettingDescription"), ex);
        }
    }

    public String getDescription() {
        String desc = "";
        try {
            InternationalStringImpl iString = ((InternationalStringImpl)registryObject.getDescription());
            Locale locale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            desc = iString.getClosestValue(locale);
        } catch(JAXRException e) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInGettingDescription"), e);
        }
        return desc;
    }

    public String getDescriptionContentLocale() {
        String contentLocaleStr = null;
        try {
            Locale contentLocale = RegistryBrowser.getInstance()
                                           .getUserPreferencesBean()
                                           .getContentLocale();
            InternationalStringImpl iString = ((InternationalStringImpl)registryObject.getName());
            LocalizedString lString = null;
            if (iString != null) {
                lString = iString.getClosestLocalizedString(contentLocale, null);
            }
            if (lString == null) {
                contentLocaleStr = contentLocale.toString();
            } else {
                Locale locale = lString.getLocale();
                if (locale == null) {
                    locale = Locale.getDefault();
                }
                contentLocaleStr = locale.toString();
            }
        } catch(Exception e) {
            log.error(WebUIResourceBundle.getInstance()
                                         .getString("message.ErrorInGettingDescriptionContent"), e);
        } finally {
            if (contentLocaleStr == null) {
                contentLocaleStr = Locale.getDefault().toString();
            }
        }
        return contentLocaleStr;
    }

    /**
    * Getter method UUID for Registry Operator
    * @ return String
    */
    public String getOperatorForRegistry() {
       try{
            if(this.registryOperator == null){
                if ((((RegistryImpl)this.registryObject)
                        .getOperator()) != null) {
                    this.registryOperator =
                            (((RegistryImpl)
                            this.registryObject).getOperator())
                            .getKey().getId();
                }
            }
        } catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("errorGettingRegistryOperator");
            log.error(errMsg+" "+ex.getMessage());
       }
        return registryOperator;
    }

   /**
    * Setter method UUID for Registry Operator
    * @param String
    */
    public void setOperatorForRegistry(String registryOperator) {
        this.registryOperator = registryOperator;
    }


    @SuppressWarnings("static-access")
	public void changeOperatorForRegistry(){
        RegistryObject ro = this.registryObject;
        RegistryObject operatorObjRef = null;
        this.isInvalidRegistryOperator = false;
        try{
            if(this.registryOperator.trim().length() > 0){
                operatorObjRef = RegistryBrowser.getInstance().getDQM().
                        getRegistryObject(this.registryOperator);
                if(operatorObjRef !=null) {
                    if(operatorObjRef instanceof OrganizationImpl){

                    ((RegistryImpl)ro)
                    .setOperator(new RegistryObjectRef(RegistryBrowser
                            .getInstance().getBLCM(),operatorObjRef));
                    } else {
                        this.setIsInvalidRegistryOperator(true);
                        this.registryOperator = null;
                    }
                } else {
                    this.setIsInvalidRegistryOperator(true);
                    this.registryOperator = null;
                }
            } else {
                ((RegistryImpl)ro).setOperator(null);
                String errMsg = WebUIResourceBundle.getInstance().
                    getString("operatorObjectRefIsnull");
                setRegistryObjectErrorMessage(errMsg);
            }
        } catch(Exception ex){
            String errMsg = WebUIResourceBundle.getInstance().
                    getString("changeRegistryOperatorError");
            log.error(errMsg+" "+ex.getMessage());
            log.error(ex.getMessage());
            setRegistryObjectErrorMessage(ex.getMessage());
        }
    }

    /**
    * Used to find if RegistryOperator is valid or not for Registry
    *
    */
    public boolean getIsInvalidRegistryOperator(){
        return this.isInvalidRegistryOperator;
    }

    public void setIsInvalidRegistryOperator(boolean isInvalidRegistryOperator){
        this.isInvalidRegistryOperator = isInvalidRegistryOperator;
    }




    public String getRegistryAssignedId() {
        return id;
    }

    public String getStatusTypeConcept() throws JAXRException {
        if (statusTypeConceptId == null) {
            RegistryObjectRef statusRef = ((RegistryObjectImpl)registryObject).getStatusRef();
            if (statusRef != null) {
                statusTypeConceptId = statusRef.getId();
            } else {
                statusTypeConceptId = CanonicalConstants.CANONICAL_STATUS_TYPE_ID_Submitted;
            }
        }
        return statusTypeConceptId;
    }

    public void setStatusTypeConcept(String statusTypeConceptId){
        this.statusTypeConceptId = statusTypeConceptId;
    }

    public boolean isWithdrawn() throws JAXRException {
        int currentStatus = ((RegistryObjectImpl)registryObject).getStatus();
        return RegistryEntry.STATUS_WITHDRAWN == currentStatus;
    }

    public boolean isApproved() throws JAXRException {
        int currentStatus = ((RegistryObjectImpl)registryObject).getStatus();
        boolean checkNew = ((RegistryObjectImpl)registryObject).isNew();
        return (RegistryEntry.STATUS_WITHDRAWN == currentStatus || checkNew || RegistryEntry.STATUS_APPROVED == currentStatus);
    }

    public boolean isDeprecated() throws JAXRException {
        int currentStatus = ((RegistryObjectImpl)registryObject).getStatus();
        boolean checkNew = ((RegistryObjectImpl)registryObject).isNew();
        return (RegistryEntry.STATUS_WITHDRAWN == currentStatus || checkNew || RegistryEntry.STATUS_DEPRECATED == currentStatus);
    }

    public boolean isUnDeprecated() throws JAXRException {
        int currentStatus = ((RegistryObjectImpl)registryObject).getStatus();
        boolean checkNew = ((RegistryObjectImpl)registryObject).isNew();
        return (RegistryEntry.STATUS_WITHDRAWN == currentStatus || checkNew || RegistryEntry.STATUS_DEPRECATED != currentStatus);
    }

    public void setNewUpload(boolean newUpload) {
        isNewUpload = newUpload;
    }

    public boolean isNewUpload() {
        return isNewUpload;
    }
    
    public String getCurrentDetailsPaneId() {
        return currentDetailsPaneId;
    }
    
    public void setCurrentDetailsPaneId(String currentDetailsPaneId) {
        this.currentDetailsPaneId = currentDetailsPaneId;
    }
    
    public void setSaveChangesToDrilldownObject(boolean saveChanges) {
        this.saveChangesToDrilldownObject = saveChanges;
    }
    
    public boolean isSaveChangesToDrilldownObject() {
        return saveChangesToDrilldownObject;
    }
    
    public String getRequiredFieldFlag() {
        String requiredFieldFlag = "";
        RegistrationInfoBean riBean = 
            (RegistrationInfoBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("registrationInfo");
        if (riBean != null) {
            requiredFieldFlag = riBean.getRequiredFieldFlag();
        }
        return requiredFieldFlag;
    }
    
    public boolean isFieldRequired() {
        boolean fieldRequired = false;
        RegistrationInfoBean riBean = 
            (RegistrationInfoBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("registrationInfo");
        if (riBean != null) {
            fieldRequired = riBean.isFieldRequired();
        }
        return fieldRequired;
    }
}
