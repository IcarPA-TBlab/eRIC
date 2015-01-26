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
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.ericTest;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.ObjectsNotFoundException;
import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.LifeCycleManagerFactory;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.security.authorization.AuthorizationServiceImpl;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;



/**
 * @author Farrukh Najmi
 *
 * Common base class for server-side tests.
 * Server-side tests should extend this class.
 * Any code commonly useful to any server-side test
 * should be added to this class.
 *
 */
public abstract class ServerTest extends ericTest {
    
    protected static LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    protected RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    protected static AuthorizationServiceImpl az = AuthorizationServiceImpl.getInstance();
    protected static URL cppaURL = ServerTest.class.getResource("/resources/CPP1.xml");
    protected HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
    
    /** Creates a new instance of ServerTest */
    public ServerTest(String name) {
        super(name);
        //lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
        //qm = QueryManagerFactory.getInstance().getQueryManager();
	//bu = BindingUtility.getInstance();
    }
    
    public RemoveObjectsRequest createRemoveObjectsRequest(String query) throws Exception {
        AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest(query);
        ServerRequestContext context = new ServerRequestContext("ServerTest:createRemoveObjectsRequest", queryRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        RemoveObjectsRequest rmRequest = bu.lcmFac.createRemoveObjectsRequest();
        rmRequest.setAdhocQuery(queryRequest.getAdhocQuery());
        
        return rmRequest;
    }
    
    private int contextCounter = 0;
    protected ServerRequestContext getContext(UserType user) throws Exception {
        String contextId = "Context:" + getClass().getName().replaceAll("\\.", ":")
            + ":" + getName() + ":" + (contextCounter++);
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(user);
        return context;
    }

    protected void closeContext(ServerRequestContext context, boolean commit) throws Exception {
        if (commit) {
            context.commit();
        } else {
            context.rollback();
        }
    }
    
    protected void submit(ServerRequestContext context, Object object) throws Exception {
        //submit(context, Collections.singletonList(object));
    	HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
    	dosubmit(context, Collections.singletonList(object), idToRepositoryItemMap);
    }
    
    protected void submit(ServerRequestContext context, Object object, Map<String, Object> idToRepositoryItemMap) throws Exception {
        //submit(context, Collections.singletonList(object), idToRepositoryItemMap);
    	dosubmit(context, Collections.singletonList(object), (HashMap<String, Object>) idToRepositoryItemMap);
    }
    
    protected void submit(ServerRequestContext context, List<Object> objects) throws Exception {
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        dosubmit(context, objects, idToRepositoryItemMap);
    }    

    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected void dosubmit(ServerRequestContext context, List objects, HashMap<String, Object> idToRepositoryItemMap) throws Exception {
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        boolean requestOk = false;
        try {
            context.pushRegistryRequest(submitRequest);
            RegistryResponseType resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }    
    
    protected List<?> executeQuery(ServerRequestContext context, String queryId, Map<String,String> queryParams) throws Exception {
        List<?> res = null;
        AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
        int startIndex = 0;
        int maxResults = -1;
        req.setStartIndex(BigInteger.valueOf(startIndex));
        req.setMaxResults(BigInteger.valueOf(maxResults));
        
        Map<String,String> slotsMap = new HashMap<String,String>();
        slotsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, queryId);
        if ((queryParams != null) && (queryParams.size() > 0)) {
            slotsMap.putAll(queryParams);
        }
        BindingUtility.getInstance().addSlotsToRequest(req, slotsMap);
        
        
                
        //Now execute the query
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        boolean requestOk = false;
        try {
            context.pushRegistryRequest(req);
            AdhocQueryResponse resp = qm.submitAdhocQuery(context);
            bu.checkRegistryResponse(resp);
            res = resp.getRegistryObjectList().getIdentifiable();
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
        
        return res;
    }    

    @SuppressWarnings("static-access")
	protected void removeIfExist(ServerRequestContext context, String objectId) throws Exception {
        try {
            ServerCache.getInstance().getRegistryObject(context, objectId, "RegistryObject");
            //Turn on forced delete mode.
            HashMap<String, String> removeRequestSlots = new HashMap<String, String>();
            removeRequestSlots.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "true");
            remove(context, Collections.singleton(objectId), null, removeRequestSlots);
        } catch (ObjectNotFoundException o) {
            return;
        } catch (ObjectsNotFoundException o) {
            return;
        }       
    }
    
    protected void remove(ServerRequestContext context, String objectId) throws Exception {        
        remove(context, Collections.singleton(objectId), null);
    }
    
    @SuppressWarnings("static-access")
	protected void remove(ServerRequestContext context, Set<String> objectIds, String queryString) throws Exception {
        HashMap<String, String> removeRequestSlots = new HashMap<String, String>();
        removeRequestSlots.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "false");
        remove(context, objectIds, queryString, removeRequestSlots);
    }
    
    protected void remove(ServerRequestContext context, Set<String> objectIds, String queryString, HashMap<String, String> removeRequestSlots) throws Exception {
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        
        if ((objectIds != null) && (objectIds.size() > 0)) {
            Iterator<String> iter = objectIds.iterator();
            while (iter.hasNext()) {
                String objectId = iter.next();
                ObjectRefType objectRef = bu.rimFac.createObjectRefType();
                objectRef.setId(objectId);        
                objectRefs.add(objectRef);                
            }
        }
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        if (removeRequestSlots != null) {
            bu.addSlotsToRequest(removeRequest, removeRequestSlots);
        }
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        if ((queryString != null) && (queryString.length() > 0)) {
            AdhocQueryType adhocQuery = bu.rimFac.createAdhocQueryType();
            adhocQuery.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

            QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
            adhocQuery.setQueryExpression(queryExp);
            queryExp.setQueryLanguage(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);

            queryExp.getContent().add(queryString);
            removeRequest.setAdhocQuery(adhocQuery);
        }
                
        boolean requestOk = false;
        try {
            context.pushRegistryRequest(removeRequest);
            RegistryResponseType resp = lcm.removeObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
    
    protected void approve(ServerRequestContext context, String objectId) throws Exception {
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType objectRef = bu.rimFac.createObjectRefType();
        objectRef.setId(objectId);        
        objectRefs.add(objectRef);
        
        ApproveObjectsRequest approveRequest = bu.lcmFac.createApproveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        approveRequest.setObjectRefList(orList);

        boolean requestOk = false;
        try {
            context.pushRegistryRequest(approveRequest);
            RegistryResponseType resp = lcm.approveObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
        
    protected void setStatus(ServerRequestContext context, String objectId, String statusId) throws Exception {
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType objectRef = bu.rimFac.createObjectRefType();
        objectRef.setId(objectId);        
        objectRefs.add(objectRef);
        
        SetStatusOnObjectsRequest setStatusRequest = bu.lcmFac.createSetStatusOnObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        setStatusRequest.setObjectRefList(orList);
        setStatusRequest.setStatus(statusId);

        boolean requestOk = false;
        try {
            context.pushRegistryRequest(setStatusRequest);
            RegistryResponseType resp = lcm.setStatusOnObjects(context);
            bu.checkRegistryResponse(resp);
            requestOk = true;
        } finally {
            context.popRegistryRequest();
            closeContext(context, requestOk);
        }
    }
    
    public CredentialInfo getCredentialInfo(String alias, String password) 
        throws Exception 
    {
        X509Certificate cert = ac.getCertificate(alias);
        if (cert == null) {
            throw new RegistryException("X509Certificate not found for alias:" + alias);
        }

        java.security.PrivateKey privateKey = ac.getPrivateKey(alias, password);
        if (privateKey == null) {
            throw new RegistryException("PrivateKey not found for alias:" + alias);
        }

        java.security.cert.Certificate[] certChain = ac.getCertificateChain(alias);
        
        return new CredentialInfo(alias, cert, certChain, privateKey);
    }
    
    /**
     * Create a RepositoryItem containing provided content.
     *
     * @param id id to use when signing the RepositoryItem
     * @param content contents of the created RepositoryItem
     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
     * @param deleteOnExit whether to delete the temporary file containing <CODE>content</CODE> after the test
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    public RepositoryItem createRepositoryItem(String id, String content, String alias, String password, boolean deleteOnExit) throws Exception {
        File file = createTempFile(deleteOnExit, content);
        
        DataHandler dh = new DataHandler(new FileDataSource(file));        
        return createRepositoryItem(dh, id);
    }
    
    /**
     * Creates a signed CPP RepositoryItem.
     * 
     * @param id id to use when signing the RepositoryItem
     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    public RepositoryItem createCPPRepositoryItem(String id)
        throws Exception {
        DataHandler dh = new javax.activation.DataHandler(cppaURL);

        return new RepositoryItemImpl(id, dh);
    }
    
//    /**
//     * Creates a signed CPP RepositoryItem.
//     * 
//     * @param id id to use when signing the RepositoryItem
//     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
//     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
//     * @return a <code>RepositoryItem</code> value
//     * @exception Exception if an error occurs
//     */
//    public RepositoryItem createCPPRepositoryItem(String id, String alias, String password)
//        throws Exception {
//        DataHandler dh = new javax.activation.DataHandler(cppaURL);
//
//        return createSignedRepositoryItem(dh, id, alias, password);
//    }
    
//    /**
//     * Creates a signed RepositoryItem for a DataHandler.
//     * @param dh <CODE>DataHandler</CODE> representing RepositoryItem content.
//     * @param id id to use when signing the RepositoryItem
//     * @param alias alias to use when getting CredentialInfo to sign the RepositoryItem
//     * @param password password to use when getting CredentialInfo to sign the RepositoryItem
//     * @return a <code>RepositoryItem</code> value
//     * @exception Exception if an error occurs
//     */
//    RepositoryItem createSignedRepositoryItem(DataHandler dh, String id, String alias, String password)
//        throws Exception {
//        CredentialInfo credentialInfo = getCredentialInfo(alias, password);
//        RepositoryItem ri = su.signPayload(dh, id, credentialInfo);
//        
//        return ri;
//    }
    
    /**
     * Creates a {@link RepositoryItem}.
     *
     * @param dh the {@link DataHandler} representing the payload
     * @param id the ID to use for the {@link RepositoryItem}
     * @exception Exception if an error occurs
     */
    public RepositoryItem createRepositoryItem(DataHandler dh, String id)
        throws Exception {
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        return ri;
    }
    
}
