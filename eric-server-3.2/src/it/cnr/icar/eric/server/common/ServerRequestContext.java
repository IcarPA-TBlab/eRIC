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
package it.cnr.icar.eric.server.common;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CommonRequestContext;
import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.ReferenceInfo;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.event.EventManager;
import it.cnr.icar.eric.server.event.EventManagerFactory;
import it.cnr.icar.eric.server.lcm.replication.ReplicationManager;
import it.cnr.icar.eric.server.lcm.versioning.VersionProcessor;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.plugin.RequestInterceptorManager;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * Keeps track of the state and context for a client request
 * as it makes its way through the server.
 *
 * @author  Farrukh S. Najmi
 */
public class ServerRequestContext extends CommonRequestContext {
    private Log log = LogFactory.getLog(this.getClass());
    private static BindingUtility bu = BindingUtility.getInstance();
    private static PersistenceManager pm = PersistenceManagerFactory.getInstance()
    .getPersistenceManager();
    private static QueryManager qm = QueryManagerFactory.getInstance()
    .getQueryManager();
    
    //Map of top level Identifiable objects within the request with id keys and IdentifiableType values
    private Map<String, RegistryObjectType> topLevelObjectsMap = new HashMap<String, RegistryObjectType>();
    
    //Ids of subset of submittedObjects that are new and not pre-existing in registry
    private Set<String> newSubmittedObjectIds = null;
    
    //New versions of RegistryObjects that are a subset of topLevelObjects that were created by Versioning feature
    private Map<RegistryObjectType, RegistryObjectType> newROVersionMap = new HashMap<RegistryObjectType, RegistryObjectType>();
    
    //New versions of RepositoryItems that were created by Versioning feature
    private Map<RepositoryItem, RepositoryItem> newRIVersionMap = new HashMap<RepositoryItem, RepositoryItem>();
    
    //Map of composed RegistryObjects within the request with id keys and RegistryObjectType values
    private HashMap<Object, Object> composedObjectsMap = new HashMap<Object, Object>();
    
    //Map of all submitted RegistryObjects objects within the request with id keys and IdentifiableType values
    //includes composedObjects
    
    //Set of all RegistryObject ids referenced from submitted (top level + composed) objects
    private Set<Object> referencedInfos = null;
    
    //Set of solved id references for this request
    private SortedSet<String> checkedRefs = new TreeSet<String>();
    
    //Map of RegistryObject owners with RO id keys and ownerId string values
    private SortedMap<String, String> fetchedOwners = new TreeMap<String, String>();
    
    //Map of submitted RegistryObjects with RO id keys and RegistryObjectType values
    private Map<String, Object> submittedObjectsMap = new HashMap<String, Object>();
    
    //Map of ObjectRefs within the request with id keys and ObjectRef values
    private Map<String, ObjectRefType> objectRefsMap = new HashMap<String, ObjectRefType>();
    
    //Maps temporary id key to permanent id value
    private Map<String, String> idMap = new HashMap<String, String>();
    
    //Used only by QueryManagerImpl to pass results of a query for read access control check.
    @SuppressWarnings("rawtypes")
	private ArrayList queryResults = new ArrayList();
    
    //Short lived memory used only in handling special queries related to cache based optimizations.
    @SuppressWarnings("rawtypes")
	private List specialQueryResults = null;
    
    //Short lived memory used only in handling stored query invocation
    private List<String> storedQueryParams = new ArrayList<String>();;
    
    //The RegistryErrorList for this request
    private RegistryErrorList errorList = null;
    
    //Tracks those associations that are being confirmed
    private Map<String, AssociationType1> confirmationAssociations = new HashMap<String, AssociationType1>();
    
    private Locale localeOfCaller = Locale.getDefault();
    
    //Begin former DAOContext members
    private Connection connection=null;
    private AuditableEventType ebCreateEventType;
    private AuditableEventType ebUpdateEventType;
    private AuditableEventType ebVersionEventType;
    private AuditableEventType ebSetStatusEventType;
    private AuditableEventType ebApproveEventType;
    private AuditableEventType ebDeprecateEventType;
    private AuditableEventType ebUnDeprecateEventType;
    private AuditableEventType ebDeleteEventType;
    private AuditableEventType ebRelocateEventType;
    private ResponseOptionType ebResponseOptionType;
    private List<ObjectRefType> objectRefs;
    
    //Consolidation of events of all types above into a single List. This is initialized in saveAuditableEVents()
	private List<IdentifiableType> auditableEvents = new ArrayList<IdentifiableType>();
    private HashMap<String, RegistryObjectType> affectedObjectsMap = new HashMap<String, RegistryObjectType>();
    
    //Map from id to lid for existing objects in registry that are either submitted or referenced in this request
	private Map<String, String> idToLidMap = new HashMap<String, String>();
    
    
    //The queryId for a request that is a parameterized query invocation
    private String queryId;
    @SuppressWarnings("rawtypes")
	private Map queryParamsMap = null;
    
    //true if user has RegistryAdministrator role
    private Boolean isAdmin = null;
    
    /** Creates a new instance of RequestContext */
    public ServerRequestContext(String contextId, RegistryRequestType request) throws RegistryException {
        super(contextId, request);
        
        //Call RequestInterceptors
        //Only intercept top level requests.
        if (request != null) {
            RequestInterceptorManager.getInstance().preProcessRequest(this);
        }
        
        setErrorList(BindingUtility.getInstance().rsFac.createRegistryErrorList());
		
		objectRefs = new ArrayList<ObjectRefType>();
    }
    
    private void createEvents() throws RegistryException {
        UserType user = getUser();
		
		if (user != null) {
		    ebCreateEventType = bu.rimFac.createAuditableEventType();
		    ebCreateEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Created);
		    ebCreateEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebCreateEventType.setRequestId("//TODO");
		    ebCreateEventType.setUser(user.getId());
		    ObjectRefListType createRefList = bu.rimFac.createObjectRefListType();
		    ebCreateEventType.setAffectedObjects(createRefList);
		    
		    ebUpdateEventType = bu.rimFac.createAuditableEventType();
		    ebUpdateEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Updated);
		    ebUpdateEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebUpdateEventType.setRequestId("//TODO");
		    ebUpdateEventType.setUser(user.getId());
		    ObjectRefListType updateRefList = bu.rimFac.createObjectRefListType();
		    ebUpdateEventType.setAffectedObjects(updateRefList);
		    
		    ebVersionEventType = bu.rimFac.createAuditableEventType();
		    ebVersionEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned);
		    ebVersionEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebVersionEventType.setRequestId("//TODO");
		    ebVersionEventType.setUser(user.getId());
		    ObjectRefListType versionRefList = bu.rimFac.createObjectRefListType();
		    ebVersionEventType.setAffectedObjects(versionRefList);
		    
		    ebSetStatusEventType = bu.rimFac.createAuditableEventType();
		    ebSetStatusEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebSetStatusEventType.setRequestId("//TODO");
		    ebSetStatusEventType.setUser(user.getId());
		    ObjectRefListType setStatusRefList = bu.rimFac.createObjectRefListType();
		    ebSetStatusEventType.setAffectedObjects(setStatusRefList);
		    
		    ebApproveEventType = bu.rimFac.createAuditableEventType();
		    ebApproveEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Approved);
		    ebApproveEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebApproveEventType.setRequestId("//TODO");
		    ebApproveEventType.setUser(user.getId());
		    ObjectRefListType approveRefList = bu.rimFac.createObjectRefListType();
		    ebApproveEventType.setAffectedObjects(approveRefList);
		    
		    ebDeprecateEventType = bu.rimFac.createAuditableEventType();
		    ebDeprecateEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deprecated);
		    ebDeprecateEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebDeprecateEventType.setRequestId("//TODO");
		    ebDeprecateEventType.setUser(user.getId());
		    ObjectRefListType deprecateRefList = bu.rimFac.createObjectRefListType();
		    ebDeprecateEventType.setAffectedObjects(deprecateRefList);
		    
		    ebUnDeprecateEventType = bu.rimFac.createAuditableEventType();
		    ebUnDeprecateEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Undeprecated);
		    ebUnDeprecateEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebUnDeprecateEventType.setRequestId("//TODO");
		    ebUnDeprecateEventType.setUser(user.getId());
		    ObjectRefListType unDeprecateRefList = bu.rimFac.createObjectRefListType();
		    ebUnDeprecateEventType.setAffectedObjects(unDeprecateRefList);
		    
		    ebDeleteEventType = bu.rimFac.createAuditableEventType();
		    ebDeleteEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted);
		    ebDeleteEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebDeleteEventType.setRequestId("//TODO");
		    ebDeleteEventType.setUser(user.getId());
		    ObjectRefListType deleteRefList = bu.rimFac.createObjectRefListType();
		    ebDeleteEventType.setAffectedObjects(deleteRefList);
		    
		    ebRelocateEventType = bu.rimFac.createAuditableEventType();
		    ebRelocateEventType.setEventType(BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated);
		    ebRelocateEventType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
		    ebRelocateEventType.setRequestId("//TODO");
		    ebRelocateEventType.setUser(user.getId());
		    ObjectRefListType relocateRefList = bu.rimFac.createObjectRefListType();
		    ebRelocateEventType.setAffectedObjects(relocateRefList);
		}
    }
    
    /**
     * Gets the RegistryObject associated with the specified id.
     * First looks in submittedObjectsMap in case it is a new object being submitted.
     * Next look in ServerCache in case it was previously fetched from registry.
     * Finally looks in registry.
     */
    public RegistryObjectType getRegistryObject(String id, String tableName)
    throws RegistryException {
        
        return getRegistryObject(id, tableName, false);
    }
    
    /**
     * Gets the RegistryObject associated with the specified id.
     * First looks in submittedObjectsMap in case it is a new object being submitted.
     * This is only done if requireExisting is false.
     *
     * Next look in ServerCache in case it was previously fetched from registry.
     * ServerCache will look in registry if not found in cache.
     */
    public RegistryObjectType getRegistryObject(String id, String tableName, boolean requireExisting)
    throws RegistryException {
        
        RegistryObjectType ro = null;
        
        if (!requireExisting) {
            //If request is submit or update then get ro from context
            if ((this.getRegistryRequestStack().size() > 0) && ((this.getCurrentRegistryRequest() instanceof SubmitObjectsRequest)
            || (this.getCurrentRegistryRequest() instanceof UpdateObjectsRequest))) {
                //First look in submitted objects.
                ro = (RegistryObjectType)getSubmittedObjectsMap().get(id);
            }
        }
        if (ro == null) {
            //Next look in registry via the ObjectCache
            ro = ServerCache.getInstance().getRegistryObject(this, id, tableName);
            
            if (ro == null) {
                throw new ObjectNotFoundException(id, tableName);
            }
        }
        
        return ro;
    }
    
    /**
     *
     * Removes object matching specified id from all the various maps.
     */
    public void remove(String id) {
        getTopLevelRegistryObjectTypeMap().remove(id);
        getSubmittedObjectsMap().remove(id);
        getComposedObjectsMap().remove(id);
        getIdMap().remove(id);
    }
    
    /**
     * Checks each object including composed objects.
     */
    @SuppressWarnings("unchecked")
	public void checkObjects() throws RegistryException {
        
        try {
            //Process ObjectRefs and create local replicas of any remote ObjectRefs
            createReplicasOfRemoteObjectRefs();
            
            //Get all submitted objects including composed objects that are part of the submission
            //so that they can be used to resolve references
            getSubmittedObjectsMap().putAll(getTopLevelRegistryObjectTypeMap());
            
            Collection<IdentifiableType> composedObjects = bu.getComposedRegistryObjects(getTopLevelRegistryObjectTypeMap().values(), -1);
            getSubmittedObjectsMap().putAll(bu.getRegistryObjectTypeMap(composedObjects));
            
            pm.updateIdToLidMap(this, getSubmittedObjectsMap().keySet(), "RegistryObject");
            
            getNewSubmittedObjectIds();
            
            //Check id of each object (top level or composed)
            Iterator<Object> iter = getSubmittedObjectsMap().values().iterator();
            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)iter.next();
                
                //AuditableEvents are not allowed to be submitted by clients
                if (ro instanceof AuditableEventType) {
                    throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.auditableEventsNotAllowed"));
                }
                
                checkId(ro);
            }
            
            //Get RegistryObjects referenced by submittedObjects.
            this.getReferenceInfos();
            
            //Append the references to the IdToLidMap
            
            iter = this.referencedInfos.iterator();
            Set<String> referencedIds = new HashSet<String>();
            while( iter.hasNext() ) {
                ReferenceInfo refInfo = (ReferenceInfo)iter.next();
                referencedIds.add( refInfo.targetObject );
            }
            
            pm.updateIdToLidMap(this, referencedIds, "RegistryObject");
            
            
            //Iterate over idMap and replace keys in various structures that use id as key
            //that are based on temporary ids with their permanent id.
            Iterator<String> iter2 = getIdMap().keySet().iterator();
            while (iter2.hasNext()) {
                String idOld = iter2.next();
                String idNew = getIdMap().get(idOld);
                
                //replace in all RequestContext data structures
                Object obj = getTopLevelRegistryObjectTypeMap().remove(idOld);
                if (obj != null) {
                    getTopLevelRegistryObjectTypeMap().put(idNew, (RegistryObjectType) obj);
                }
                obj = getSubmittedObjectsMap().remove(idOld);
                if (obj != null) {
                    getSubmittedObjectsMap().put(idNew, obj);
                }
                if (getNewSubmittedObjectIds().remove(idOld)) {
                    getNewSubmittedObjectIds().add(idNew);
                }
                
                RepositoryItem ri = (RepositoryItem)getRepositoryItemsMap().remove(idOld);
                if (ri != null) {
                    ri.setId(idNew);
                    getRepositoryItemsMap().put(idNew, ri);
                }
            }
            
            //Now replace any old versions of RegistryObjects with new versions
            Iterator<RegistryObjectType> iter3 = getNewROVersionMap().keySet().iterator();
            while (iter3.hasNext()) {
				RegistryObjectType ebRegistryObjectTypeOld = iter3.next();
				RegistryObjectType ebRegistryObjectTypeNew = getNewROVersionMap().get(
						ebRegistryObjectTypeOld);

				// replace in all data structures
                getSubmittedObjectsMap().remove(ebRegistryObjectTypeOld.getId());
                getSubmittedObjectsMap().put(ebRegistryObjectTypeNew.getId(), ebRegistryObjectTypeNew);
                getTopLevelRegistryObjectTypeMap().remove(ebRegistryObjectTypeOld.getId());
                getTopLevelRegistryObjectTypeMap().put(ebRegistryObjectTypeNew.getId(), ebRegistryObjectTypeNew);
            }
            
            //Now replace any old versions of RepositoryItems with new versions
            Iterator<RepositoryItem> iter4 = getNewRIVersionMap().keySet().iterator();
            while (iter4.hasNext()) {
                RepositoryItem riOld = iter4.next();
                RepositoryItem riNew = getNewRIVersionMap().get(riOld);
                
                //replace in all RequestContext data structures
                getRepositoryItemsMap().remove(riOld.getId());
                getRepositoryItemsMap().put(riNew.getId(), riNew);
            }
            
            //resolve references from each object
            resolveObjectReferences();
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    private void createReplicasOfRemoteObjectRefs() throws RegistryException {
        Iterator<ObjectRefType> iter = getObjectRefTypeMap().values().iterator();
        while (iter.hasNext()) {
            ObjectRefType ebObjectRefType = iter.next();
            ReplicationManager replMgr = ReplicationManager.getInstance();
            if (replMgr.isRemoteObjectRef(ebObjectRefType)) {
                //This is a Remote ObjectRef. Resolve reference by creating a replica
                replMgr.createReplica(this, ebObjectRefType);
            }
        }
    }
    
    /**
     * Check if id is a proper UUID. If not make a proper UUID based URN and add
     * a mapping in idMap between old and new Id.
     *
     * @param submittedIds The ArrayList holding ids of all objects (including composed objects) submitted.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     * @throws UUIDNotUniqueException if any UUID is not unique within a
     * SubmitObjectsRequest
     */
    private void checkId(RegistryObjectType ebRegistryObjectType)
    throws RegistryException {
        String id = ebRegistryObjectType.getId();
        
        it.cnr.icar.eric.common.Utility util = it.cnr.icar.eric.common.Utility.getInstance();
        if (!util.isValidRegistryId(id)) {
            // Generate permanent id for this temporary id
            String newId = util.createId();
            ebRegistryObjectType.setId(newId);
            getIdMap().put(id, newId);
        }
        
        VersionProcessor vp = new VersionProcessor(this);
        vp.checkRegistryObjectLid(ebRegistryObjectType);
        
        boolean needToVersionRepositoryItem = false;
        if (ebRegistryObjectType instanceof ExtrinsicObjectType) {
            ExtrinsicObjectType eo = (ExtrinsicObjectType)ebRegistryObjectType;
            needToVersionRepositoryItem = vp.needToVersionRepositoryItem(eo, (RepositoryItem)getRepositoryItemsMap().get(id));
            
            if (needToVersionRepositoryItem) {
                //Following will also create new version of eo implicitly
                vp.createRepositoryItemVersion(eo);
            }
        }
        
        //Only version RegistryObject if RepositoryItem was not versioned
        //This is because RegistryObject is implictly versioned during
        //versioning of RepositoryItem
        if (!needToVersionRepositoryItem) {
            boolean needToVersionRegistryObject = vp.needToVersionRegistryObject(ebRegistryObjectType);
            if (needToVersionRegistryObject) {
                vp.createRegistryObjectVersion(ebRegistryObjectType);
            }
        }
        
    }
    
    /*
     * Resolves each ObjectRef within the specified objects.
     *
     * @param obj the object whose reference attribute are being checked for being resolvable.
     *
     *
     */
    private void resolveObjectReferences()
    throws RegistryException {
        
        try {
            //Get Set of ids for objects referenced from obj
            Set<Object> refInfos = this.referencedInfos;
            
            //Check that each ref is resolvable
            Iterator<Object> iter = refInfos.iterator();
            Set<String> unresolvedRefIds = new HashSet<String>();
            while (iter.hasNext()) {
                ReferenceInfo refInfo = (ReferenceInfo)iter.next();
                String refId = refInfo.targetObject;
                
                // only check referenced id once per request
                if (getCheckedRefs().contains(refId)) {
                    continue;
                } else {
                    getCheckedRefs().add(refId);
                }
                
                @SuppressWarnings("unused")
				ObjectRefType ref = getObjectRefTypeMap().get(refId);
                
                //Remote references already resolved by creating local replica by now
                //First check if resolved within submittedIds
                if (!(getSubmittedObjectsMap().containsKey(refId))) {
                    //ref not resolved within submitted objects
                    
                    //See if exists in the registry
                    if (!(getIdToLidMap().keySet().contains(refId))) {
                        unresolvedRefIds.add(refId);
                    }
                    
                }
            }
            
            if (unresolvedRefIds.size() > 0) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unresolvedReferences",
                        new Object[]{unresolvedRefIds}));
            }
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public ResponseOptionType getResponseOption() throws RegistryException {
        if (ebResponseOptionType == null) {
		    ebResponseOptionType = bu.queryFac.createResponseOptionType();
		    ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		    ebResponseOptionType.setReturnComposedObjects(true);
		}
        return ebResponseOptionType;
    }
    
    public void setResponseOption(ResponseOptionType responseOption) {
        this.ebResponseOptionType = responseOption;
    }
    
    /**
     * Gets ObjectRefs from result of the AdhocQuery specified (if any).
     *
     */
    public List<? extends IdentifiableType> getObjectsRefTypeListFromQueryResults(AdhocQueryType query) throws RegistryException, JAXBException {
        List<IdentifiableType> ebObjectTypeRefList = new ArrayList<IdentifiableType>();
        
        try {
            if (query != null) {
                AdhocQueryRequest req = bu.queryFac.createAdhocQueryRequest();
                req.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
                req.setAdhocQuery(query);
                
                ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
                ebResponseOptionType.setReturnComposedObjects(false);
                ebResponseOptionType.setReturnType(ReturnType.OBJECT_REF);
                req.setResponseOption(ebResponseOptionType);
                this.pushRegistryRequest(req);
                AdhocQueryResponse ebAdhocQueryResponse = qm.submitAdhocQuery(this);
                ebObjectTypeRefList.addAll(bu.getIdentifiableTypeList(ebAdhocQueryResponse.getRegistryObjectList()));
            }
        } finally {
            if (query != null) {
                this.popRegistryRequest();
            }
        }
        
        return ebObjectTypeRefList;
    }
    
    public List<ObjectRefType> getObjectRefs() {
        return objectRefs;
    }
    
    public AuditableEventType getCreateEvent() {
        return ebCreateEventType;
    }
    
    public AuditableEventType getUpdateEvent() {
        return ebUpdateEventType;
    }
    
    public AuditableEventType getVersionEvent() {
        return ebVersionEventType;
    }
    
    public AuditableEventType getSetStatusEvent() {
        return ebSetStatusEventType;
    }
    
    public AuditableEventType getApproveEvent() {
        return ebApproveEventType;
    }
    
    public AuditableEventType getDeprecateEvent() {
        return ebDeprecateEventType;
    }
    
    public AuditableEventType getUnDeprecateEvent() {
        return ebUnDeprecateEventType;
    }
    
    public AuditableEventType getDeleteEvent() {
        return ebDeleteEventType;
    }
    
    public AuditableEventType getRelocateEvent() {
        return ebRelocateEventType;
    }
    
    public Connection getConnection() throws RegistryException {
        if (connection == null) {
            connection = pm.getConnection(this);
        }
        return connection;
    }
    
    private void saveAuditableEvents() throws RegistryException {
        UserType user = getUser();
        
        if (user != null) {
            auditableEvents.clear();

            XMLGregorianCalendar timeNow;
			try {
				timeNow = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(new GregorianCalendar());
            
	            //create events during relocate event should be ignored
	            if (eventOccured(getRelocateEvent())) {
	                getRelocateEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getRelocateEvent());
	                auditableEvents.add(getRelocateEvent());
	            } else if (eventOccured(getCreateEvent())) {
	                getCreateEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getCreateEvent());
	                auditableEvents.add(getCreateEvent());
	            }
	            
	            //Delete during update should be ignored as they are an impl artifact
	            if (eventOccured(getUpdateEvent())) {
	                getUpdateEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getUpdateEvent());
	                auditableEvents.add(getUpdateEvent());
	            } else if (eventOccured(getDeleteEvent())) {
	                getDeleteEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getDeleteEvent());
	                auditableEvents.add(getDeleteEvent());
	            }
	            
	            if (eventOccured(getSetStatusEvent())) {
	                getSetStatusEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getSetStatusEvent());
	                auditableEvents.add(getSetStatusEvent());
	            }
	            
	            if (eventOccured(getApproveEvent())) {
	                getApproveEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getApproveEvent());
	                auditableEvents.add(getApproveEvent());
	            }
	            
	            if (eventOccured(getDeprecateEvent())) {
	                getDeprecateEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getDeprecateEvent());
	                auditableEvents.add(getDeprecateEvent());
	            }
	            
	            if (eventOccured(getUnDeprecateEvent())) {
	                getUnDeprecateEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getUnDeprecateEvent());
	                auditableEvents.add(getUnDeprecateEvent());
	            }
	            
	            if (eventOccured(getVersionEvent())) {
	                getVersionEvent().setTimestamp(timeNow);
	                removeDuplicateAffectedObjects(getVersionEvent());
	                auditableEvents.add(getVersionEvent());
	            }
	            
	            if (auditableEvents.size() > 0) {
	                getCreateEvent().setTimestamp(timeNow);
	                pm.insert(this, auditableEvents);
	            }
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
    }
    
    /**
     * Delete of composed objects such as ClassificationNodes within Schemes
     * can result in duplicate ObjectRefs being deleted.
     */
    private void removeDuplicateAffectedObjects(AuditableEventType ae) {
        HashSet<String> ids = new HashSet<String>();
        HashSet<ObjectRefType> duplicateObjectRefs = new HashSet<ObjectRefType>();
        
        //Determine duplicate ObjectRefs
        Iterator<ObjectRefType> iter = ae.getAffectedObjects().getObjectRef().iterator();
        while (iter.hasNext()) {
            ObjectRefType oref = iter.next();
            String id = oref.getId();
            if (ids.contains(id)) {
                duplicateObjectRefs.add(oref);
            } else {
                ids.add(id);
            }
        }
        
        //Now remove duplicate ObjectRefs
        iter = duplicateObjectRefs.iterator();
        while (iter.hasNext()) {
            ae.getAffectedObjects().getObjectRef().remove(iter.next());
        }
        
    }
    
    private boolean eventOccured(AuditableEventType ae) {
        boolean occured = false;
        
        if ((ae.getAffectedObjects() != null) &&
                (ae.getAffectedObjects().getObjectRef() != null) &&
                (ae.getAffectedObjects().getObjectRef().size() > 0)) {
            
            occured = true;
        }
        
        return occured;
        
    }
    
    private void sendEventsToEventManager() {
        EventManager eventManager = EventManagerFactory.getInstance().getEventManager();
        UserType user = getUser();
        
        if (user != null) {
            if (eventOccured(getCreateEvent())) {
                eventManager.onEvent(this, getCreateEvent());
            }
            
            if (eventOccured(getVersionEvent())) {
                eventManager.onEvent(this, getVersionEvent());
            }
            if (eventOccured(getSetStatusEvent())) {
                eventManager.onEvent(this, getSetStatusEvent());
            }
            if (eventOccured(getApproveEvent())) {
                eventManager.onEvent(this, getApproveEvent());
            }
            if (eventOccured(getDeprecateEvent())) {
                eventManager.onEvent(this, getDeprecateEvent());
            }
            if (eventOccured(getUnDeprecateEvent())) {
                eventManager.onEvent(this, getUnDeprecateEvent());
            }
            
            //Delete during update should be ignored as they are an impl artifact
            if (eventOccured(getUpdateEvent())) {
                eventManager.onEvent(this, getUpdateEvent());
            } else if (eventOccured(getDeleteEvent())) {
                eventManager.onEvent(this, getDeleteEvent());
            }
        }
    }
    
    /*
     * Called to commit the transaction
     * Saves auditable events for this transaction prior to commit.
     * Notifies EventManager after commit.
     */
    public void commit() throws RegistryException {
        //Dont commit unless this is the last request in stack.
        if ((connection != null) && (getRegistryRequestStack().size() <= 1)) {
            try {
                //Save auditable events prior to commit
                saveAuditableEvents();
                
                //Only commit if LCM_DO_NOT_COMMIT is unspecified or false
                String dontCommit = null;
                if (getRegistryRequestStack().size() > 0) {
                    HashMap<String, Object> slotsMap = bu.getSlotsFromRequest(this.getCurrentRegistryRequest());
                    dontCommit = (String)slotsMap.get(BindingUtility.CANONICAL_SLOT_LCM_DO_NOT_COMMIT);
                }
                if ((dontCommit == null) || (dontCommit.equalsIgnoreCase("false"))) {
                    connection.commit();
                    pm.releaseConnection(this, connection);
                    connection = null;
                    //New connection can be created in sendEventsToEventManager() which must be released
                    try {
                        sendEventsToEventManager();
                        updateCache();
                    } catch (Exception e) {
                        rollback();
                        log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
                    }
                    if (connection != null) {
                        connection.commit();
                        pm.releaseConnection(this, connection);
                        connection = null;
                    }
                } else {
                    rollback();
                }
                
            } catch (RegistryException e) {
                rollback();
                throw e;
            } catch (JAXBException e) {
                rollback();
                throw new RegistryException(e);
            } catch (SQLException e) {
                rollback();
                throw new RegistryException(e);
            }
            
            //Call RequestInterceptors
            if (getRegistryRequestStack().size() == 1) {
                //Only intercept top level requests.
                
                //Still causing infinite recursion and StackOverflow
                //RequestInterceptorManager.getInstance().postProcessRequest(this);
            }
        }
    }
    
    private void updateCache() {
        Iterator<?> iter = auditableEvents.iterator();
        while (iter.hasNext()) {
            AuditableEventType ae = (AuditableEventType)iter.next();
            
            //Update the cache for these objects.
            ServerCache.getInstance().onEvent(this, ae);
        }
    }
    
    public void rollback() throws RegistryException {
        try {
            //Dont rollback if there are multiple requests on the requestStack
            if ((connection != null) && (getRegistryRequestStack().size() <= 1)) {
                connection.rollback();
                pm.releaseConnection(this, connection);
                connection = null;
            }
        } catch (SQLException e) {
        	return;
        }
    }
    
    public void setUser(UserType user) throws RegistryException {
        if ((getUser() != user) || (ebCreateEventType == null)) {
            super.setUser(user);
            createEvents();
        }
    }
    
    public Map<String, RegistryObjectType> getTopLevelRegistryObjectTypeMap() {
        return topLevelObjectsMap;
    }
    
    public Set<String> getNewSubmittedObjectIds() {
        if (newSubmittedObjectIds == null) {
            newSubmittedObjectIds = new HashSet<String>();
            newSubmittedObjectIds = getIdsNotInRegistry(getSubmittedObjectsMap().keySet());
        }
        
        return newSubmittedObjectIds;
    }
    
    /*
     * Gets the subset of ids that do not match ids of objects that are already in registry
     */
    public Set<String> getIdsNotInRegistry(Set<String> ids) {
        Set<String> idsNotInRegistry = new HashSet<String>();
        
        Iterator<String> iter = ids.iterator();
        while (iter.hasNext()) {
            String id = iter.next();
            if (!(getIdToLidMap().keySet().contains(id))) {
                idsNotInRegistry.add(id);
            }
        }
        
        return idsNotInRegistry;
    }
    
    public Map<RegistryObjectType, RegistryObjectType> getNewROVersionMap() {
        return newROVersionMap;
    }
    
    public Map<RepositoryItem, RepositoryItem> getNewRIVersionMap() {
        return newRIVersionMap;
    }
    
    public Map<?, ?> getComposedObjectsMap() {
        return composedObjectsMap;
    }
    
    public SortedSet<String> getCheckedRefs() {
        return checkedRefs;
    }
    
    public SortedMap<String, String> getFetchedOwners() {
        return fetchedOwners;
    }
    
    public Map<String, Object> getSubmittedObjectsMap() {
        return submittedObjectsMap;
    }
    
    public Map<String, ObjectRefType> getObjectRefTypeMap() {
        return objectRefsMap;
    }
    
    public Map<String, String> getIdMap() {
        return idMap;
    }
    
    public RegistryErrorList getErrorList() {
        return errorList;
    }
    
    private void setErrorList(RegistryErrorList errorList) {
        this.errorList = errorList;
    }
    
    public Map<String, AssociationType1> getConfirmationAssociations() {
        return confirmationAssociations;
    }
    
    @SuppressWarnings("static-access")
	public Locale getLocale() {
        if (localeOfCaller == null) {
            HashMap<?, ?> slotsMap = null;
            String localeStr = null;
            try {
                slotsMap = bu.getSlotsFromRequest(this.getCurrentRegistryRequest());
                localeStr = (String)slotsMap.get(CommonResourceBundle.LOCALE);
            } catch (Throwable t) {
                log.error(ServerResourceBundle.getInstance().getString("message.CouldNotGetSlotsFromTheRequest"), t);
            }
            localeOfCaller = CommonResourceBundle.getInstance().parseLocale(localeStr);
        }
        return localeOfCaller;
    }
    
    @SuppressWarnings("rawtypes")
	public Collection getQueryResults() {
        return queryResults;
    }
    
    @SuppressWarnings("unchecked")
	public void setQueryResults(@SuppressWarnings("rawtypes") List queryResults) {
        //Need to create a copy because if the List param is a SingletonList
        //then remove() method does not work on it during filtering of objects
        //that are not authorized to be seen by requestor in QueryManager.
        this.queryResults = new ArrayList<Object>();
        this.queryResults.addAll(queryResults);
    }
    
    /**
     * If context is not a ServerRequestContext then convert it to ServerRequestContext.
     * This is used in XXManagerLocalProxy classes to convert a ClientRequestContext to a ServerRequestContext.
     *
     * @return the ServerRequestContext
     */
    public static ServerRequestContext convert(RequestContext context) throws RegistryException {
        ServerRequestContext serverContext = null;
        
        if (context instanceof ServerRequestContext) {
            serverContext = (ServerRequestContext)context;
        } else {
            RegistryRequestType req = null;
            if (context.getRegistryRequestStack().size() > 0) {
                req = context.getCurrentRegistryRequest();
            }
            serverContext = new ServerRequestContext(context.getId(), req);
            serverContext.setUser(context.getUser());
            serverContext.setRepositoryItemsMap(context.getRepositoryItemsMap());
        }
        
        return serverContext;
    }
    
    @SuppressWarnings("rawtypes")
	public List getSpecialQueryResults() {
        return specialQueryResults;
    }
    
    public void setSpecialQueryResults(List<?> specialQueryResults) {
        this.specialQueryResults = specialQueryResults;
    }
    
    public Map<String, RegistryObjectType> getAffectedObjectsMap() {
        return affectedObjectsMap;
    }
    
    public void addAffectedObjectToAuditableEvent(AuditableEventType ae, RegistryObjectType ro) throws RegistryException {
        ObjectRefType ebObjectRefType = BindingUtility.getInstance().rimFac.createObjectRefType();
		ebObjectRefType.setId(ro.getId());
		ae.getAffectedObjects().getObjectRef().add(ebObjectRefType);
		affectedObjectsMap.put(ro.getId(), ro);
    }
    
    public void addAffectedObjectsToAuditableEvent(AuditableEventType ae, ObjectRefListType orefList) throws RegistryException {
        ae.getAffectedObjects().getObjectRef().addAll(orefList.getObjectRef());
        for (Iterator<ObjectRefType> it = orefList.getObjectRef().iterator(); it.hasNext(); ) {
            String id = (it.next()).getId();
            RegistryObjectType ro = getRegistryObject(id, "RegistryObject");
            affectedObjectsMap.put(id, ro);
        }
    }
    
    public List<String> getStoredQueryParams() {
        return storedQueryParams;
    }
    
    public Map<String, String> getIdToLidMap() {
        return idToLidMap;
    }
    
    public Set<?> getReferenceInfos() throws RegistryException {
        if (referencedInfos == null) {
            try {
                referencedInfos = new HashSet<Object>();
                
                Iterator<Entry<String, Object>> iter = getSubmittedObjectsMap().entrySet().iterator();
                while (iter.hasNext()) {
                    Object o = (iter.next()).getValue();
                    
                    if (o instanceof RegistryObjectType) {
                        RegistryObjectType ro = (RegistryObjectType)o;
                        //Get Set of ids for objects referenced from obj
                        Set<ReferenceInfo> refInfos = bu.getObjectRefsInRegistryObject(ro, getIdMap(), new HashSet<RegistryObjectType>(), -1);
                        
                        Iterator<ReferenceInfo> refInfosIter = refInfos.iterator();
                        while( refInfosIter.hasNext() ) {
                            referencedInfos.add( refInfosIter.next() );
                        }
                    }
                }
            } catch (JAXRException e) {
                throw new RegistryException(e);
            }
        }
        
        return referencedInfos;
    }
    
    public void checkClassificationNodeRefConstraint(String nodeId, String expectedSchemeId, String attributeName) throws RegistryException {
        ClassificationSchemeType expectedScheme = null;
        try {
            //Check that objectType is the id of a ClassificationNode in ObjectType scheme
            expectedScheme = (ClassificationSchemeType)ServerCache.getInstance().getRegistryObject(this, expectedSchemeId, "ClassScheme");
            
            ClassificationNodeType node = (ClassificationNodeType)this.getRegistryObject(nodeId, "ClassificationNode");
            String path = node.getPath();
            String schemePath = "/" + expectedScheme.getId();
            
            if ((path != null) && (path.length() > 0)) {
                if (!path.startsWith(schemePath)) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.notTheExpectedTypeOfNode",
                            new Object[]{attributeName, expectedScheme.getId(), nodeId}));
                }
            }
        } catch (ObjectNotFoundException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.notTheExpectedTypeOfNode",
                    new Object[]{attributeName, expectedScheme.getId(), nodeId}));
            
        }
    }
    
    public String getQueryId() {
        return queryId;
    }
    
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
    
    public Map<?, ?> getQueryParamsMap() {
        return queryParamsMap;
    }
    
    public void setQueryParamsMap(Map<?, ?> queryParamsMap) {
        this.queryParamsMap = queryParamsMap;
    }
    
    public boolean isRegistryAdministrator() throws RegistryException {
        if (isAdmin == null) {
            UserType user = getUser();
            if (user != null) {
                isAdmin = Boolean.valueOf(AuthenticationServiceImpl.getInstance().hasRegistryAdministratorRole(user));
            }
        }
        
        return isAdmin.booleanValue();
    }
    
}
