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

package it.cnr.icar.eric.server.lcm;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.ReferencesExistException;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * @author Farrukh Najmi
 */
public class LifeCycleManagerImplTest extends ServerTest {
        
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(LifeCycleManagerImplTest.class);
    
    //URL to remote registry for testing cooperating registries features
    //String remoteRegistryURL = System.getProperty("remoteRegistryURL", "http://rollsroyce.sfbay:8000/eric/registry");
    String remoteRegistryURL = System.getProperty("remoteRegistryURL", "http://localhost:8000/eric/registry");
    
    //id for a remote ClassificationNode
    String remoteRefId = System.getProperty("remoteRefId", BindingUtility.CANONICAL_OBJECT_TYPE_LID_RegistryObject);
    
    boolean skipReferenceCheckOnRemove = Boolean.valueOf(RegistryProperties.getInstance()
        .getProperty("eric.persistence.rdb.skipReferenceCheckOnRemove", "false")).booleanValue();
    
    
    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public LifeCycleManagerImplTest(String name) {
        super(name);
    }
    
    /**
     * This test if for making sure that a transaction involving operations across
     * registry and repository are atomic.
     *
     * Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6444810 
     */
    @SuppressWarnings({ "static-access" })
	public void testAtomicTransactionAcrossRegistryAndRepository() throws Exception {
        final String contextId1 = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context1";
        ServerRequestContext context1 = new ServerRequestContext(contextId1, null);
        context1.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        final String contextId2 = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context2";
        ServerRequestContext context2 = new ServerRequestContext(contextId2, null);
        context2.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        final String contextId3 = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testAtomicTransactionAcrossRegistryAndRepository:context3";
        ServerRequestContext context3 = new ServerRequestContext(contextId3, null);
        context3.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        String eoId = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:deleteSpillOverQuery:eo";
                
        try {
            removeIfExist(context1, eoId);
            
            //In contxt1 save an EO by iteself first. Do commit context.
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            eo.setId(eoId);
            eo.setLid(eoId);

            submit(context1, eo);
            
            //In context2 and save EO/RI pair and dont commit context
            //Do not use ServerTest.submit as we want to keep the context open.

            RepositoryItem ri = createRepositoryItem(eoId, "1.1", ac.ALIAS_NIKOLA, ac.ALIAS_NIKOLA, true);
            idToRepositoryItemMap.clear();
            idToRepositoryItemMap.put(eoId, ri);

            SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);

            //roList.getIdentifiable().add(eo);
            roList.getIdentifiable().add(bu.rimFac.createExtrinsicObject(eo));
            submitRequest.setRegistryObjectList(roList);
            context2.setRepositoryItemsMap(idToRepositoryItemMap);

            context2.pushRegistryRequest(submitRequest);
            RegistryResponseType resp = lcm.submitObjects(context2);
            bu.checkRegistryResponse(resp);
            
            //Do not commit context and instead use a separate context to look up the repository item
            try {                
                rm.getRepositoryItem(eoId);
                fail("This is a known issue for Farrukh to fix. Able to see RepositoryItem from a context that has not yet committed");
            } catch (ObjectNotFoundException e) {
                //Correct behavior: RI should not be found by context3 because context2 has not committed yet.
            }
                        
        } finally {
            removeIfExist(context3, eoId);
            context3.commit();
            context2.rollback();
            context1.rollback();
        }
        
    }

    /** 
     * Tests that RemoveObjectsRequest supports the AdhocQuery param added in 3.0
     * which specifies additional objects that MUST be removed in addition to those
     * specified by the ObjectRefList param.
     *
     * AdhocQuery: This parameter specifies a query. A registry MUST remove all objects that match the specified query in addition to any other objects identified by other parameters.
     * ObjectRefList:  This parameter specifies a collection of references to existing RegistryObject instances in the registry. A registry MUST remove all objects that are referenced by this parameter in addition to any other objects identified by other parameters.
     *
     */
    @SuppressWarnings({ "static-access" })
	public void testRemoveWithAdhocQueryAsParam() throws Exception {        
        RegistryPackageType pkg1 = bu.rimFac.createRegistryPackageType();
        String pkg1Id = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:pkg1";
        pkg1.setId(pkg1Id);
        
        RegistryPackageType pkg2 = bu.rimFac.createRegistryPackageType();
        String pkg2Id = "urn:freebxml:eric:server:lcm:LifeCycleManagerImplTest:pkg2";
        pkg2.setId(pkg2Id);
        
        //ArrayList<RegistryPackageType> objects = new ArrayList<RegistryPackageType>();
        //objects.add(pkg1);
        //objects.add(pkg2);
        ArrayList<JAXBElement<RegistryPackageType>> objects = new ArrayList<JAXBElement<RegistryPackageType>>();
        objects.add(bu.rimFac.createRegistryPackage(pkg1));
        objects.add(bu.rimFac.createRegistryPackage(pkg2));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
                
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Test audit trail 
        List<JAXBElement<? extends IdentifiableType>> auditTrail = getAuditTrailForRegistryObject(context, pkg1);
        AuditableEventType firstEvent = (AuditableEventType)auditTrail.get(0).getValue();
	// Timestamps are not unique enough to be sure earliest event is first
	for (int i = 1;
	     ( auditTrail.size() > i &&
	       !firstEvent.getEventType().
	       equals(bu.CANONICAL_EVENT_TYPE_ID_Created) &&
	       firstEvent.getTimestamp().equals(((AuditableEventType)auditTrail.
						 get(i).getValue()).getTimestamp()) );
	     i++) {
	    // Try another 'firstEvent' -- also with earliest known timestamp
	    firstEvent = (AuditableEventType)auditTrail.get(i).getValue();
	}
        assertEquals("First auditable event should have been a create event.", bu.CANONICAL_EVENT_TYPE_ID_Created, firstEvent.getEventType());
        
        //Now remove of what was submitted such that pkg1 is specified by ObjectRefList param
        //and pkg2 is specified by AdhocQuery params
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType pkg1Ref = bu.rimFac.createObjectRefType();
        pkg1Ref.setId(pkg1Id);        
        objectRefs.add(pkg1Ref);
                
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        
        //Set the ObjectRefList param
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        //Set the AdhocQuery param
        AdhocQueryRequest req = bu.createAdhocQueryRequest("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg2Id + "'");
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now make sure that both pkg1 and pkg2 are indeed removed
        req = bu.createAdhocQueryRequest("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg1Id + "' OR rp.id = '" + pkg2Id + "'");
        context = new ServerRequestContext("LifeCycleManagerImplTest:testRemoveWithAdhocQueryAsParam", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        AdhocQueryResponse resp1 = qm.submitAdhocQuery(context);
        bu.checkRegistryResponse(resp1);
        
        assertEquals("Remove failed", 0, resp1.getRegistryObjectList().getIdentifiable().size());        
    }
    
    /*
     * Gets the List of AuditableEventTypes for the specified RegistryObjects. 
     * 
     */
    public List<JAXBElement<? extends IdentifiableType>> getAuditTrailForRegistryObject(RequestContext context, RegistryObjectType ro) throws RegistryException {
        List<JAXBElement<? extends IdentifiableType>> auditTrail = null;

        try {
            HashMap<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("$lid", ro.getLid());
            AdhocQueryRequest req = bu.createAdhocQueryRequest("urn:oasis:names:tc:ebxml-regrep:query:GetAuditTrailForRegistryObject", queryParams);
            context.pushRegistryRequest(req);
            AdhocQueryResponse resp = qm.submitAdhocQuery(context);

            auditTrail = resp.getRegistryObjectList().getIdentifiable();

        } catch (JAXBException e) {
            throw new RegistryException(e);
        }    
        return auditTrail;
    }
    
    
    /**
     * Creates a RegistryPackage, saves it, updates its name once and saves it,
     * and then update its name again and saves it.
     */
    @SuppressWarnings({ "static-access" })
	public void testMultipleUpdatesOfNameWithContextualClassification() throws Exception {
        RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
        String pkgId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        pkg.setId(pkgId);
        
        //Add name to pkg
        InternationalStringType nameIS = bu.createInternationalStringType("name0");
        pkg.setName(nameIS);
        
        //ArrayList<IdentifiableType> objects = new ArrayList<IdentifiableType>();
        //objects.add(pkg);
        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(pkg));
        
        //Add a Classification to pkg        
        ClassificationType classification1 = bu.createClassificationType(pkg.getId(), bu.CANONICAL_OBJECT_TYPE_ID_XML);
        classification1.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
        InternationalStringType classificationName1 = bu.createInternationalStringType("classification1");
        classification1.setName(classificationName1);
        pkg.getClassification().add(classification1);
        //objects.add(classification1);
        objects.add(bu.rimFac.createClassification(classification1));
        
        /*
        //Add a Classification to classification1        
        Classification classification2 = bu.createClassification(classification1.getId(), bu.CANONICAL_OBJECT_TYPE_ID_XML);
        classification2.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());        
        InternationalStringType classificationName2 = bu.createInternationalStringType("classification2");
        classification2.setName(classificationName2);
        classification1.getClassification().add(classification2);
        */
        
        
        //Create submit request 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
                
        //Now do the submit 
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now update name first time and save
        nameIS = bu.createInternationalStringType("name1");
        pkg.setName(nameIS);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now update name second time and save
        nameIS = bu.createInternationalStringType("name2");
        pkg.setName(nameIS);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testMultipleUpdatesOfNameWithContextualClassification", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
    }
    
    //public void testSubmitWithNoUser() throws Exception {
    //}
    
        
    //TODO: Add testInvalidObjectRef
    
    /** Tests submission of an object with an ObjectRef to a remote object */
    public void testSubmitRemoteObjectRef() throws Exception {        
        RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
        String pkgId = "urn:uuid:e2450db9-2fbc-4185-b5ae-e607dfbda524";
        pkg.setId(pkgId);
        
        //Create a remote ObjectRef to US ClassificationNode in ISO 3166 taxonomy
        ObjectRefType remoteRef = bu.rimFac.createObjectRefType();

        remoteRef.setId(remoteRefId);
        remoteRef.setHome(remoteRegistryURL);
        
        //Create a Clasification that has a remote ObjectRef to a ClassificationNode
        ClassificationType classification = bu.rimFac.createClassificationType();
        classification.setClassificationNode(remoteRefId);        
        
        //Classify pkg with Classification that uses remote ClassificationNode
        classification.setClassifiedObject(pkgId);

        //ArrayList<IdentifiableType> objects = new ArrayList<IdentifiableType>();
        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(pkg));
        objects.add(bu.rimFac.createClassification(classification));
        objects.add(bu.rimFac.createObjectRef(remoteRef));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testSubmitRemoteObjectRef", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now verify that local replica of remote object can be retrieved by id
        //Get the default ACP that is expected in any minDB        
        RegistryObjectType ro = qm.getRegistryObject(context, remoteRefId);
        if (ro == null) {
            //TODO: Add this once remote object ref feature is implemented
            fail("Failed to get local replica of remote object with id: " + remoteRefId);
        }
        
        
        //Now do the remove of what was submitted
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType pkgRef = bu.rimFac.createObjectRefType();
        pkgRef.setId(pkgId);        
        objectRefs.add(pkgRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        bu.addSlotsToRequest(removeRequest, forceRemoveRequestSlotsMap);
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        context = new ServerRequestContext("LifeCycleManagerImplTest:testSubmitRemoteObjectRef", removeRequest);
        context.setUser(ac.registryOperator);
        resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }
    
    /*
     * Test Impl specific feature to not commit a request.
     */
    @SuppressWarnings({ "static-access" })
	public void testDontCommitMode() throws Exception {
        RegistryPackageType testFolder = bu.rimFac.createRegistryPackageType();
        String testFolderId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        testFolder.setId(testFolderId);

        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(testFolder));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        
        HashMap<String, String> slotsMap = new HashMap<String, String>();
        slotsMap.put(bu.CANONICAL_SLOT_LCM_DO_NOT_COMMIT, "true");
        bu.addSlotsToRequest(submitRequest, slotsMap);

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
           
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDontCommitMode", submitRequest);
        context.setUser(ac.nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        //Save with CANONICAL_SLOT_LCM_DO_NOT_COMMIT true.
        RegistryResponseType resp = lcm.submitObjects(context);
        assertEquals("Request had errors.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());
        
    }
    
    /*
     * Test Impl specific feature to re-assign owner to a different owner.
     * 
     */
    public void testOwnerReassignMode() throws Exception {
        //Save as a non-RegistryAdministrator role and expect an error.
        testOwnerReassignMode(AuthenticationServiceImpl.getInstance().nikola,
            AuthenticationServiceImpl.getInstance().nikola.getId(),
            "Request should have had errors because owner reassignment can only be done by RegistryAdministrator roles.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
        
        //Save as a RegistryAdministrator role but specify invalid ownerId and expect an error.
        testOwnerReassignMode(AuthenticationServiceImpl.getInstance().registryOperator,
            it.cnr.icar.eric.common.Utility.getInstance().createId(),
            "Request should have had errors because specified owner is not a registered user.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
        
        //Save as a RegistryAdministrator role and valid ownerId and expect success.
        RegistryPackageType testFolder = testOwnerReassignMode(AuthenticationServiceImpl.getInstance().registryOperator,
            AuthenticationServiceImpl.getInstance().nikola.getId(),
            "Request should have been successful.",
            BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);                
        
        //TODO: Need to validate that user's owner is Nikola. Doing it manually for now.
        
        
        //Delete the testFolder.
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType pkgRef = bu.rimFac.createObjectRefType();
        pkgRef.setId(testFolder.getId());        
        objectRefs.add(pkgRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testOwnerReassignMode", removeRequest);
        context.setUser(ac.registryOperator);
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
    }
    
    @SuppressWarnings({ "static-access" })
	private RegistryPackageType testOwnerReassignMode(UserType caller, String newOwnerId,
        String errorMsg,
        String expectedStatus) throws Exception {
            
        RegistryPackageType testFolder = bu.rimFac.createRegistryPackageType();
        String testFolderId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        testFolder.setId(testFolderId);

        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(testFolder));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        
        HashMap<String, String> slotsMap = new HashMap<String, String>();
        slotsMap.put(bu.CANONICAL_SLOT_LCM_OWNER, newOwnerId);
        bu.addSlotsToRequest(submitRequest, slotsMap);

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testOwnerReassignMode", submitRequest);
        context.setUser(caller);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
           
        try {
            RegistryResponseType resp = lcm.submitObjects(context);
            assertEquals(errorMsg, expectedStatus, resp.getStatus());
        } catch (RegistryException e) {
            //Allow this exception if expecting failure
            if (!expectedStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure)) {
                fail(errorMsg);
            }
        }
        
        return testFolder; 
    }
    
    
    /**
     * Tests that LCM does not throw IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor owns the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectSameUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectSameUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextFarrukh, "testNoNewRefsToDeprecatedObjectSameUser");
        } catch (IllegalStateException e) {
            fail("Threw IllegalStateException for new reference to a Deprecated object even when object is my own.");
        }
    }
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor does not own the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectDifferentUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ServerRequestContext contextNikola = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:nikola", null);
        contextNikola.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextNikola, "testNoNewRefsToDeprecatedObjectDifferentUser");
            fail("Did not throw IllegalStateException for new reference to a Deprecated object.");
        } catch (IllegalStateException e) {
            //expected. All is well
        }
    }
    
    
     /*
      * Test Mapping of temp ID's to permanent UUID's (in ServerRequestContext).
      */
    public void testTempIDMappings() throws Exception 
    {        
        RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
        String pkgTempId    = "tempID";
        
        pkg.setId( pkgTempId );
        
        //Create a Clasification 
        ClassificationType classification = bu.rimFac.createClassificationType();
        classification.setClassificationNode( remoteRefId );        
        
        //Classify pkg with this Classification
        classification.setClassifiedObject( pkgTempId );

        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage( pkg ));
        objects.add(bu.rimFac.createClassification( classification ));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll( objects );
        submitRequest.setRegistryObjectList( roList );
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        
        ServerRequestContext context = new ServerRequestContext ( "LifeCycleManagerImplTest:testTempIDMappings", submitRequest );
        context.setUser( ac.registryOperator );
        context.setRepositoryItemsMap( idToRepositoryItemMap );
        
        RegistryResponseType resp = lcm.submitObjects( context );
        bu.checkRegistryResponse( resp );
        
        it.cnr.icar.eric.common.Utility util = it.cnr.icar.eric.common.Utility.getInstance();
        
        assertTrue( "TempID not replaced with valid UUID", util.isValidRegistryId( pkg.getId() ) );
        assertEquals( "TempID not mapped properly in object reference", pkg.getId(), classification.getClassifiedObject() );
        
        //Now do the remove of what was submitted
        removeIfExist(context, pkg.getId());
    }
    
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object.
     *
     */
    private void internalTestNoNewRefsToDeprecatedObject(ServerRequestContext context1, ServerRequestContext context2, String contextId) throws Exception {
        final String pkgId = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:" + contextId + ":pkg1";
        final String assId = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:" + contextId + ":ass1";

        ServerRequestContext contextRegAdmin = new ServerRequestContext("LifeCycleManagerImplTest:testNoNewRefsToDeprecatedObject:registryOperator", null);
        contextRegAdmin.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);

            // Create package
            RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
            pkg.setId(pkgId);
            submit(context1, pkg);

            //Now create and association between ass and pkg
            //This should work as object being referenced is not deprecated
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setSourceObject(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved); //id of some random existing object
            ass.setTargetObject(pkgId);
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            submit(context2, ass);
            
            //Now deprecate the pkg
            setStatus(context1, pkgId, BindingUtility.CANONICAL_STATUS_TYPE_ID_Deprecated);
            
            //Now submit ass again
            //This should fail as object being referenced is deprecated
            //as long as user is not superuser or owner of reference target
            submit(context2, ass);
        } finally {
            // final clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);            
        }                
    }    
    
    /**
     * Tests that LCM throws ReferencesExistException if an object is deleted when it has
     * references from one or more objects.
     *
     */
    public void testDeleteWhenReferencesExist() throws Exception {
        final String pkgId = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExist:pkg1";
        final String assId = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExist:ass1";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDeleteWhenReferencesExist", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);

            // Create package
            RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
            pkg.setId(pkgId);

            //Now create and association between ass and pkg
            //This should work as object being referenced is not deprecated
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setSourceObject(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved); //id of some random existing object
            ass.setTargetObject(pkgId);
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            
            ArrayList<Object> objs = new ArrayList<Object>();
            objs.add(pkg);
            objs.add(ass);
            
            submit(context, objs);
            
            
            
            //Now try deleting the pkg and verify that a ReferenceExistsException is generated
            try {
                remove(context, pkgId);
                
                if (!skipReferenceCheckOnRemove) {
                    fail("Did not throw ReferencesExistsException when deleting an object with references.");
                }
            } catch (ReferencesExistException e) {
                if (skipReferenceCheckOnRemove) {
                    fail("Should not have thrown ReferencesExistException");
                } else {
                    //log.error(e, e);
                    //Expected. Good!
                }
            }
            
            //Now try deleting pkg and ass together. This should be allowed 
            //since referenceSource is also being deleted in same request
            HashSet<String> objIds = new HashSet<String>();
            objIds.add(pkg.getId());
            objIds.add(ass.getId());
            remove(context, objIds, (String)null);          
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);            
        }                
    }    

    /**
     * Tests that LCM throws ReferencesExistException if an object is deleted when it has
     * references from one or more objects via a Slot of slotType ObjectRef.
     *
     */
    @SuppressWarnings({ "static-access" })
	public void testDeleteWhenReferencesExistViaSlot() throws Exception {
        final String pkgId = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot:pkg1";
        final String pkgId2 = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot:pkgId2";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testDeleteWhenReferencesExistViaSlot", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, pkgId2);

            // Create package
            RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
            pkg.setId(pkgId);

            RegistryPackageType pkg2 = bu.rimFac.createRegistryPackageType();
            pkg2.setId(pkgId2);
                        
            SlotType1 slot = bu.rimFac.createSlotType1();
            slot.setName("urn:freebxml:registry:test:LifeCycleManagerImpl:testDeleteWhenReferencesExistViaSlot:slot");
            slot.setSlotType(bu.CANONICAL_DATA_TYPE_ID_ObjectRef);
            
            ValueListType valueList = BindingUtility.getInstance().rimFac.createValueListType();
            
            String valueStr = pkg.getId(); //Reference to pkg
            //Value value = bu.rimFac.createValue();
            //value.setValue(valueStr);
            //valueList.getValue().add(value);
            valueList.getValue().add(valueStr);
            slot.setValueList(valueList);
            pkg2.getSlot().add(slot);
            
            ArrayList<Object> objs = new ArrayList<Object>();
            objs.add(pkg);
            objs.add(pkg2);
            
            submit(context, objs);
            
            //Now try deleting the pkg and verify that a ReferenceExistsException is generated
            try {
                remove(context, pkgId);
                if (!skipReferenceCheckOnRemove) {
                    fail("Did not throw ReferencesExistsException when deleting an object with references.");
                }
            } catch (ReferencesExistException e) {
                if (skipReferenceCheckOnRemove) {
                    fail("Should not have thrown ReferencesExistException");
                } else {
                    //log.error(e, e);
                    //Expected. Good!
                }
            }
            
            //Now try deleting pkg and pkg2 together. This should be allowed 
            //since referenceSource is also being deleted in same request
            HashSet<String> objIds = new HashSet<String>();
            objIds.add(pkg.getId());
            objIds.add(pkg2.getId());
            remove(context, objIds, (String)null);          
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, pkgId2);            
        }                
    } 
   /*
    * Create AdhocQuery with null query String.
    * Bug Fix : 6480083-Regression with adhoc query creation
    */  
    @SuppressWarnings({ "static-access" })
	public void testAdhocQueryNullString() throws Exception{
        final String adhocId = "urn:freebxml:registry:test:nullQueryString:testAdhocQueryNullString:AdhocQuery";
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, true, null);
        ServerRequestContext context = new ServerRequestContext("statusUpdate:testAdhocQueryNullString", submitRequest);
        context.setUser(ac.registryOperator);
        try{
          //create new AdhocQuery Object
          AdhocQueryType adhoc = bu.rimFac.createAdhocQueryType();
          adhoc.setId(adhocId);
          InternationalStringType adhocName = bu.createInternationalStringType("AdhocQuery_NullString _Test");
          adhoc.setName(adhocName);
          QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
          queryExp.setQueryLanguage(bu.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);
          queryExp.getContent().add(null); 
          adhoc.setQueryExpression(queryExp);
          bu.addRegistryObjectToSubmitRequest(submitRequest, adhoc);
          RegistryResponseType resp = lcm.submitObjects(context);
          bu.checkRegistryResponse(resp);
          remove(context, adhocId);
        }catch(Exception e) {
            if(e.getCause() instanceof NullPointerException) {
                fail("Query String is Null for AdhocQuery Object.");
            }
        }finally {
            // final clean-up
            removeIfExist(context, adhocId);
        }
    }
    
    /**
     * Tests that LCM allows nested members within a RegistryPackage
     * and automatically creates HasMember Associations with RegistryPackage
     * and nested members. Test also makes sure that this works to multiple levels
     * of nesting.
     */
    public void testNestedMembersInRegistryPackage() throws Exception {
        final String pkgId1 = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkg1";
        final String pkgId2 = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkgId2";
        final String pkgId3 = "urn:org:freebxml:eric:server:lcm:LifeCycleManagerImplTest:testNestedMembersInRegistryPackage:pkgId3";

        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testNestedMembersInRegistryPackage", null);
        
        //Use registryOperator as even she cannot do deletes when references exist (as this is not an access control issue). 
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId1);
            removeIfExist(context, pkgId2);
            removeIfExist(context, pkgId3);

            // Create package
            RegistryPackageType pkg1 = bu.rimFac.createRegistryPackageType();
            pkg1.setId(pkgId1);

            RegistryPackageType pkg2 = bu.rimFac.createRegistryPackageType();
            pkg2.setId(pkgId2);
            
            RegistryPackageType pkg3 = bu.rimFac.createRegistryPackageType();
            pkg3.setId(pkgId3);
            
            pkg1.setRegistryObjectList(bu.rimFac.createRegistryObjectListType());
            pkg1.getRegistryObjectList().getIdentifiable().add(bu.rimFac.createRegistryPackage(pkg2));
            pkg2.setRegistryObjectList(bu.rimFac.createRegistryObjectListType());
            pkg2.getRegistryObjectList().getIdentifiable().add(bu.rimFac.createRegistryPackage(pkg3));
                        
            
            ArrayList<Object> objs = new ArrayList<Object>();
            objs.add(pkg1);
            
            submit(context, objs);

            //Now make sure pkg2 is a member of pkg1
            HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
            queryParamsMap.put("$packageId", pkgId1);

            List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetMembersByRegistryPackageId, queryParamsMap);
            assertEquals("Nested member not found for pkg1", 1, res.size());
            
            RegistryPackageType p = (RegistryPackageType)((JAXBElement<?>)res.get(0)).getValue();
            assertEquals("Nested member id not correct for pkg1", pkgId2, p.getId());
            
            //Now make sure pkg3 is a member of pkg2
            queryParamsMap.clear();
            queryParamsMap.put("$packageId", pkgId2);

            res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetMembersByRegistryPackageId, queryParamsMap);
            assertEquals("Nested member not found for pkg1", 1, res.size());
            
            p = (RegistryPackageType)((JAXBElement<?>)res.get(0)).getValue();
            assertEquals("Nested member id not correct for pkg1", pkgId3, p.getId());
            
        } finally {
            // final clean-up
            removeIfExist(context, pkgId1);
            removeIfExist(context, pkgId2);            
            removeIfExist(context, pkgId3);            
        }                
    } 
    
    
    public static Test suite() {
        junit.framework.TestSuite suite = new TestSuite(LifeCycleManagerImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new LifeCycleManagerImplTest("testNestedMembersInRegistryPackage"));
        //suite.addTest(new LifeCycleManagerImplTest("testDeleteWhenReferencesExistViaSlot"));
        //suite.addTest(new LifeCycleManagerImplTest("testNoNewRefsToDeprecatedObjectDifferentUser"));
        //suite.addTest(new LifeCycleManagerImplTest("testRemoveWithAdhocQueryAsParam"));
        return suite;
    }
}
