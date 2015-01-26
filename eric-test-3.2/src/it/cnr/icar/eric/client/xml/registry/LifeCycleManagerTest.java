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

package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.common.ClientTest;
import it.cnr.icar.eric.client.xml.registry.infomodel.AuditableEventImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryPackageImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceBindingImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.UserImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.UUIDFactory;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.Connection;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Key;
// import javax.xml.registry.infomodel.Organization;
// import javax.xml.registry.infomodel.PostalAddress;
// import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
// import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * jUnit Test for LifeCycleManager
 *
 * @author Farrukh Najmi
 */
public class LifeCycleManagerTest extends ClientTest {
    
    public LifeCycleManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(LifeCycleManagerTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new LifeCycleManagerTest("testClassifyWithSomeoneElsesConcept"));
        return suite;
    }
    
    /**
     * Tests for a bug where an existing ServiceBinding was updated to
     * classify it with a concept that was not owned by user. Bug was that
     * the concept was being saved (when it should not have) resulting in
     * AccessControl exceptions. The concept was in a non-cached scheme.
     *
     * it turns out the problems was quite unrelated and had to do with
     * clone of name in Classification.
     *
     * This test is left in as it is a good example of multi-user
     * client test with cleanup.
     */
    public void testClassifyWithSomeoneElsesConcept() throws Exception {
        String serviceId = "urn:freebxml:test:testClassifyWithSomeoneElsesConcept:service2";
        String bindingId = "urn:freebxml:test:testClassifyWithSomeoneElsesConcept:binding2";
        String pkgId = "urn:freebxml:test:testClassifyWithSomeoneElsesConcept:pkg1";
        String conceptId = "urn:freebxml:test:testClassifyWithSomeoneElsesConcept:node1";
        
        //Create 2 sets of connections etc. for 2 different identities
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection regopConnection = getConnection(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
        BusinessLifeCycleManagerImpl regopLCM = (BusinessLifeCycleManagerImpl) regopConnection.getRegistryService().getBusinessLifeCycleManager();
        
        try {
            //Delete any objects from previous runs.
            deleteIfExist(dqm, regopLCM, serviceId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, bindingId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, pkgId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, conceptId, LifeCycleManager.CONCEPT, forceRemoveRequestSlotsMap);

            //Create new pkg and save as Nikola
            RegistryPackage pkg = nikolaLCM.createRegistryPackage("testClassifyWithSomeoneElsesConcept.pkg");
            pkg.getKey().setId(pkgId);
            ((RegistryPackageImpl)pkg).setLid(pkgId);

            Service service = nikolaLCM.createService("testClassifyWithSomeoneElsesConcept.service");
            service.getKey().setId(serviceId);
            ((ServiceImpl)service).setLid(serviceId);
            ServiceBinding binding = nikolaLCM.createServiceBinding();
            binding.getKey().setId(bindingId);
            ((ServiceBindingImpl)binding).setLid(bindingId);
            service.addServiceBinding(binding);
            
            ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
            objects.add(pkg);
            objects.add(service);
            //objects.add(binding);

            BulkResponse br = ((LifeCycleManagerImpl)nikolaLCM).saveObjects(objects);        
            assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);

            //Create new Concept and save as Farrukh
            Concept parent = (Concept)bqm.getRegistryObject("urn:freebxml:registry:sample:profile:cpp:objectType:cppa", LifeCycleManager.CONCEPT);
            Concept concept = farrukhLCM.createConcept(parent, "node1", "node1");
            concept.getKey().setId(conceptId);

            objects = new ArrayList<RegistryObject>();
            objects.add(concept);

            br = ((LifeCycleManagerImpl)farrukhLCM).saveObjects(objects);        
            assertTrue("Concept creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            
            //Now update Nikola's pkg as Nikola by classifying using Farrukh's node
            concept = (Concept)bqm.getRegistryObject(conceptId, LifeCycleManager.CONCEPT);
            //concept = (Concept)bqm.getRegistryObject("urn:xxx:classificationScheme:AvailabilityType:Available", LifeCycleManager.CONCEPT);
            pkg = (RegistryPackage)bqm.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
            Classification c = nikolaLCM.createClassification(concept);
            pkg.addClassification(c);

            binding = (ServiceBinding)bqm.getRegistryObject(bindingId, LifeCycleManager.SERVICE_BINDING);
            c = nikolaLCM.createClassification(concept);
            binding.addClassification(c);
            
            objects = new ArrayList<RegistryObject>();
            //objects.add(pkg);
            objects.add(binding);

            br = ((LifeCycleManagerImpl)nikolaLCM).saveObjects(objects);        
            assertTrue("RegistryPackage update failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
        } finally {
            deleteIfExist(dqm, regopLCM, serviceId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, bindingId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, pkgId, LifeCycleManager.REGISTRY_PACKAGE, forceRemoveRequestSlotsMap);
            deleteIfExist(dqm, regopLCM, conceptId, LifeCycleManager.CONCEPT, forceRemoveRequestSlotsMap);
        }
    }                

    /**
     * This test test for an unauthprized RemoveObjectsRequest.
     * It also illustrates how the code can be writen to handle the 
     * error being returned in BulkResponse or as a propagated RegistryException.
     */
    public void testUnauthorizedDelete() throws Exception {
        ArrayList<Key> keys = new ArrayList<Key>();
        Key key = lcm.createKey(CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
        //Try deleting the objectType scheme which is not owned by test user and should result in an error.
        keys.add(key);
        
        //Now do the delete
        try {
            BulkResponse br = (lcm).deleteObjects(keys, null, null, null);
            this.assertResponseFailure("Should have gotten an error in BulReponse if RegistryException was not thrown", br);
        } catch (RegistryException e) {
            //This is normal in case server side error was returned as a RegistryException.
            @SuppressWarnings("unused")
			int i=0; //Just to allow setting a break point
        }
    }
    
    /*
     * Test SetStatusOnObject extension protocol.
     * This protocol allows setting status of an Object to any
     * ClassificationNode within canonical StatysType ClassificationScheme.
     *
     * This test sets the status of the callers user to be Withdrawn which
     * is otherwise not possible and then changes it back to Submitted.
     */
    @SuppressWarnings("static-access")
	public void testSetStatusOnObject() throws Exception {
        User user = bqm.getCallersUser();
        List<Key> keys = new ArrayList<Key>();
        keys.add(user.getKey());
        BulkResponse br = lcm.setStatusOnObjects(keys, bu.CANONICAL_STATUS_TYPE_ID_Withdrawn);
        assertResponseSuccess("Error during testSetStatusOnObject", br);
        
        //Dont use getCallersUser as it is cache and does not have status update.
        //This is a known issue that JAXR client objects are not updated if changed in server.
        user = (User)bqm.getRegistryObject(user.getKey().getId(), lcm.USER);
        RegistryObjectRef statusRef = ((UserImpl)user).getStatusRef();
        assertEquals(bu.CANONICAL_STATUS_TYPE_ID_Withdrawn, statusRef.getId());
        
        //Now check that audit trail was generated for setStatus action
        Collection<?> auditTrail = user.getAuditTrail();
	Object events[] = auditTrail.toArray();
        AuditableEvent event = (AuditableEvent)events[auditTrail.size() - 1];
        String latestEventType = ((AuditableEventImpl)event).getEventType1();
	// Timestamps are not unique enough to be sure latest event is last
	for (int i = auditTrail.size() - 2;
	     ( 0 <= i &&
	       !latestEventType.equals(bu.CANONICAL_STATUS_TYPE_ID_Withdrawn) &&
	       event.getTimestamp().equals(((AuditableEvent)events[i])
					   .getTimestamp()) );
	     i--) {
	    // Try another 'event' -- also with latest known timestamp
	    event = (AuditableEvent)events[i];
	    latestEventType = ((AuditableEventImpl)event).getEventType1();
	}
        assertEquals("EventType did not match", bu.CANONICAL_STATUS_TYPE_ID_Withdrawn, latestEventType);        
        
        br = lcm.setStatusOnObjects(keys, bu.CANONICAL_STATUS_TYPE_ID_Submitted);
        assertResponseSuccess("Error during testSetStatusOnObject", br);
        
        user = (User)bqm.getRegistryObject(user.getKey().getId(), lcm.USER);
        statusRef = ((UserImpl)user).getStatusRef();
        assertEquals("Error during testSetStatusOnObject", bu.CANONICAL_STATUS_TYPE_ID_Submitted, statusRef.getId());
        
        //Now check that audit trail was generated for setStatus action
        auditTrail = user.getAuditTrail();
	events = auditTrail.toArray();
	event = (AuditableEvent)events[auditTrail.size() - 1];
        latestEventType = ((AuditableEventImpl)event).getEventType1();
	// Timestamps are not unique enough to be sure latest event is last
	for (int i = auditTrail.size() - 2;
	     ( 0 <= i &&
	       !latestEventType.equals(bu.CANONICAL_STATUS_TYPE_ID_Submitted) &&
	       event.getTimestamp().equals(((AuditableEvent)events[i]).
					   getTimestamp()) );
	     i--) {
	    // Try another 'event' -- also with latest known timestamp
	    event = (AuditableEvent)events[i];
	    latestEventType = ((AuditableEventImpl)event).getEventType1();
	}
        assertEquals("EventType did not match", bu.CANONICAL_STATUS_TYPE_ID_Submitted, latestEventType);
        
        try {
            br = lcm.setStatusOnObjects(keys, "some-text-that-is-not-status-id");
            assertResponseFailure("setStatus accepted any text", br);
        } catch (RegistryException e) {
            //Expected.
        }
        
        try {
            br = lcm.setStatusOnObjects(keys, bu.CANONICAL_ASSOCIATION_TYPE_ID_Contains);
            assertResponseFailure("setStatus accepted any concept", br);
        } catch (RegistryException e) {
            //Expected.
        }
    }
    
    /** 
     * Tests that deleteObjects supports the AdhocQueryImpl param added to support new feature in ebRS 3.0
     * which specifies additional objects that MUST be removed in addition to those
     * specified by the ObjectRefList param.
     *
     * AdhocQuery: This parameter specifies a query. A registry MUST remove all objects that match the specified query in addition to any other objects identified by other parameters.
     * ObjectRefList:  This parameter specifies a collection of references to existing RegistryObject instances in the registry. A registry MUST remove all objects that are referenced by this parameter in addition to any other objects identified by other parameters.
     *
     */
    @SuppressWarnings("static-access")
	public void testRemoveWithAdhocQueryAsParam() throws Exception {
        String pkg1Id = "urn:freebxml:eric:client:lcm:LifeCycleManagerImplTest:pkg1";
        String pkg2Id = "urn:freebxml:eric:client:lcm:LifeCycleManagerImplTest:pkg2";
        
        //cleanup any existing objects with same id
        it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl ahq = 
                    (it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl)lcm.createObject("AdhocQuery");
        ahq.setString("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg1Id + "' OR rp.id = '" + pkg2Id + "'");
        ArrayList<Key> keys = new ArrayList<Key>();
        
        //Now do the delete
        try {
            (lcm).deleteObjects(keys, ahq, null, null);
	} catch (RuntimeException re) {
	    throw re;
        } catch (Exception e) {
            //Any exception here can be ignored as objects may not have existed.
        }
        
        RegistryPackage pkg1 = lcm.createRegistryPackage("pkg1");
        pkg1.getKey().setId(pkg1Id);
	deleteIdToTypeMap.put(pkg1Id, lcm.REGISTRY_PACKAGE);
        
        RegistryPackage pkg2 = lcm.createRegistryPackage("pkg2");
        pkg2.getKey().setId(pkg2Id);
	deleteIdToTypeMap.put(pkg2Id, lcm.REGISTRY_PACKAGE);
        
        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        objects.add(pkg1);
        objects.add(pkg2);
        
        //Now do the submit
        BulkResponse br = (lcm).saveObjects(objects);        
        assertTrue("RegistryPackages creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        
        //Now remove of what was submitted such that pkg1 is specified by ObjectRefList param
        //and pkg2 is specified by AdhocQuery param
        
        //Specify pkg1 for deletion using key
        keys.add(pkg1.getKey());
        
        //Specify pkg2 for deletion using query
        ahq = (it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl)lcm.createObject("AdhocQuery");
        ahq.setString("SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg2Id + "'");
        
        //Now do the delete
        br = (lcm).deleteObjects(keys, ahq, null, null);
        assertTrue("RegistryPackage Deprecation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
                
        //Now make sure that both pkg1 and pkg2 are indeed removed
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, 
                "SELECT rp.id FROM RegistryPackage rp WHERE rp.id = '" + pkg1Id + "' OR rp.id = '" + pkg2Id + "'");
        br = dqm.executeQuery(query);
        assertTrue("Query failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);                
        assertEquals("Remove failed", 0, br.getCollection().size());
    }
    
    /*
     * Test Impl specific feature to perform request as an Intermediary on behalf
     * of another user.
     * 
     */
    @SuppressWarnings("static-access")
	public void testIntermediaryRequest() throws Exception {
        //Create 2 sets of connections etc. for 2 different identities
        Connection regopConnection = getConnection(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
        BusinessLifeCycleManager regopLCM = regopConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
        
        RegistryPackage pkg = nikolaLCM.createRegistryPackage("pkg");
        String pkgId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        pkg.getKey().setId(pkgId);

        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        objects.add(pkg);
        
        //Now do the submit object as nikola 
        BulkResponse br = ((LifeCycleManagerImpl)nikolaLCM).saveObjects(objects);        
        assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
                   
        //Now do the remove of what was submitted as registryOperator with effective user as farrukh
        //Expect an error because farrukh is not allowed to delete nikola's objects
        ArrayList<Key> toDelete = new ArrayList<Key>();
        toDelete.add(regopLCM.createKey(pkgId));
        
        HashMap<String, String> slotsMap = new HashMap<String, String>();
        slotsMap.put(bu.CANONICAL_URI_EFFECTIVE_REQUESTOR, AuthenticationServiceImpl.ALIAS_FARRUKH);
        try {
            ((LifeCycleManagerImpl)regopLCM).deleteObjects(toDelete, slotsMap);
            assertFalse("This is a known exception that need to be investigated and fixed. RegistryPackage deletion should have failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        } catch (RegistryException e) { //Why is LCMProxy not throwing the original UnauthorizedRequestException
            // This is expected, delete using the expected LCM & User
	    nikolaLCM.deleteObjects(toDelete);
        }
    }            
    
    @SuppressWarnings("unchecked")
	public void testAssociateUnsavedObjects() throws Exception {
        // try a 'eo1' relatedTo 'eo2' association
        ExtrinsicObject eo1 = lcm.createExtrinsicObject((javax.activation.DataHandler)null);
        ExtrinsicObject eo2 = lcm.createExtrinsicObject((javax.activation.DataHandler)null);
        Concept relatedToType = (Concept)bqm.getRegistryObject(
            CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo, LifeCycleManager.CONCEPT);
        Association ass = lcm.createAssociation(eo2, relatedToType);
        eo1.getAssociations().add(ass);
    }
    
//    public void testNullParameters() throws Exception {
//        //Call various API methods with null parameter and make sure no NPE happens
//        BulkResponse br = lcm.saveObjects(null);        
//        assertTrue("save failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        br = lcm.deleteObjects(null);        
//        assertTrue("Delete failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//
//        br = lcm.deprecateObjects(null);        
//        assertTrue("Deprecate failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//
//        br = lcm.unDeprecateObjects(null);        
//        assertTrue("unDeprecate failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//    }
//    
//    /**
//     * Tests that true-composed objects are implicitly saved.
//     *
//     * A true-composed object is a composed object within RIM
//     *
//     */
//    public void testImlicitSaveTrueComposedObjects() throws Exception {
//        //Create the pkg that is the main object to save explicitly
//        RegistryPackage pkg1 = lcm.createRegistryPackage("LifeCycleManagerTest.pkg1");
//        String pkg1Id = pkg1.getKey().getId();
//        
//        //Add a Classification as a true composed object
//        Concept xacmlConcept = (Concept)bqm.getRegistryObject(BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML, LifeCycleManager.CONCEPT);
//        assertNotNull("Unable to read xacmlConcept", xacmlConcept);
//        
//        Classification classification = (Classification)lcm.createClassification(xacmlConcept);
//        pkg1.addClassification(classification);
//        
//        ArrayList saveObjects = new ArrayList();        
//        saveObjects.add(pkg1);
//        BulkResponse br = lcm.saveObjects(saveObjects);        
//        assertTrue("pkg1 creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        //Assert that scheme and node were both saved.
//        pkg1 = (RegistryPackage)bqm.getRegistryObject(pkg1Id, LifeCycleManager.REGISTRY_PACKAGE);
//        assertNotNull("Unable to read back pkg1", pkg1);
//        
//        assertEquals("pkg1 has incorrect classification count.", 1, pkg1.getClassifications().size());
//        
//        ArrayList deleteObjects = new ArrayList();
//        deleteObjects.add(pkg1.getKey());
//        br = lcm.deleteObjects(deleteObjects);
//        assertTrue("pkg1 deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        pkg1 = (RegistryPackage)bqm.getRegistryObject(pkg1Id, LifeCycleManager.REGISTRY_PACKAGE);
//        assertNull("Unable to delete pkg1", pkg1);
//        
//    }
//    
//    /*
//     * Test Impl specific feature to not commit a request.
//     */
//    public void testDontCommitMode() throws Exception {
//        RegistryPackage testFolder = lcm.createRegistryPackage("LifeCycleManagerTest.pkg2");
//        String pkgId = testFolder.getKey().getId();
//
//        //Now do the submit        
//        HashMap slotsMap = new HashMap();
//        slotsMap.put(BindingUtility.getInstance().CANONICAL_SLOT_LCM_DO_NOT_COMMIT, "true");
//           
//        ArrayList saveObjects = new ArrayList();        
//        saveObjects.add(testFolder);
//        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects, slotsMap);        
//        assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        //Assert that scheme and node were both saved.
//        testFolder = (RegistryPackage)bqm.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
//        assertNull("Should not be able to read back RegistryPackage", testFolder);
//        
//    }
//    
//    /*
//     * Test Impl specific feature to not commit a request.
//     */
//    public void testApproveObjects() throws Exception {
//        RegistryPackage testFolder = lcm.createRegistryPackage("LifeCycleManagerTest.pkg3");
//        String pkgId = testFolder.getKey().getId();
//
//        //save the object                   
//        ArrayList saveObjects = new ArrayList();        
//        saveObjects.add(testFolder);
//        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
//        assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//                
//        //Now approve the object
//        ArrayList approveObjects = new ArrayList();
//        approveObjects.add(testFolder.getKey());
//        br = ((LifeCycleManagerImpl)lcm).approveObjects(approveObjects);
//        
//        //Retrieve object from server.
//        testFolder = (RegistryPackage)bqm.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
//        assertNotNull("Unable to save RegistryPackage", testFolder);
//        
//        //Assert that object status is approved
//        int status = testFolder.getStatus();
//        assertEquals("Unable to approve object", RegistryEntry.STATUS_APPROVED, status);
//    }
//    
//    
//    /**
//     * Tests that pseudo-composed objects are implicitly saved.
//     *
//     * A pseudo-composed object is not a composed object from the RIM view point.
//     * However, it is composed as far as JAXR API is concerned. Note JAXR 1.0 spec
//     * does not currently make it clear which objects are considered composed object
//     * for each JAXR type.
//     *
//     */
//    public void testImlicitSavePsudoComposedObjects() throws Exception {
//    }
//    
//    /**
//     * Tests that association and associated objects are implicitly saved.
//     *
//     *
//     */
//    public void testImlicitSaveAssociationsAndAssociatedObjects() throws Exception {
//        //Create org11 that is the main object to save explicitly
//        Organization org1 = lcm.createOrganization("LifeCycleManagerTest.org1");
//        String org1Id = org1.getKey().getId();
//        
//        PostalAddress addr = lcm.createPostalAddress("streetNumber",
//                                         "street",
//                                         "city",
//                                         "stateOrProvince",
//                                         "country",
//                                         "postalCode",
//                                         "type");
//        TelephoneNumber tel = lcm.createTelephoneNumber();
//        ArrayList tels = new ArrayList();
//        tels.add(tel);
//        org1.setPostalAddress(addr);
//        org1.setTelephoneNumbers(tels);
//
//        //Create org2 and associate it with org1 to be implicitly saved
//        Organization org2 = lcm.createOrganization("LifeCycleManagerTest.org2");
//        String org2Id = org2.getKey().getId();
//        org2.setPostalAddress(addr);
//        org2.setTelephoneNumbers(tels);
//
//        Concept relatedToType = (Concept)bqm.getRegistryObject(
//            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RELATED_TO, LifeCycleManager.CONCEPT);
//        Association ass1 = lcm.createAssociation(org2, relatedToType);
//        String ass1Id = ass1.getKey().getId();
//        
//        org1.addAssociation(ass1);
//        
//        ArrayList saveObjects = new ArrayList();        
//        saveObjects.add(org1);
//        BulkResponse br = lcm.saveObjects(saveObjects);        
//        assertTrue("org11 creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        //Assert that all explicit and implicit objects were saved.
//        org1 = (Organization)bqm.getRegistryObject(org1Id, LifeCycleManager.ORGANIZATION);
//        assertNotNull("Unable to read back org1", org1);
//        
//        org2 = (Organization)bqm.getRegistryObject(org2Id, LifeCycleManager.ORGANIZATION);
//        assertNotNull("Unable to read back org2", org2);
//        
//        ass1 = (Association)bqm.getRegistryObject(ass1Id, LifeCycleManager.ASSOCIATION);
//        assertNotNull("Unable to read back ass1", ass1);
//                
//        ArrayList deleteObjects = new ArrayList();
//        deleteObjects.add(org1.getKey());
//        deleteObjects.add(org2.getKey());
//        deleteObjects.add(ass1.getKey());
//        br = lcm.deleteObjects(deleteObjects);
//        assertTrue("service1 deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        org1 = (Organization)bqm.getRegistryObject(org1Id, LifeCycleManager.ORGANIZATION);
//        assertNull("Unable to delete org1", org1);
//        org2 = (Organization)bqm.getRegistryObject(org2Id, LifeCycleManager.ORGANIZATION);
//        assertNull("Unable to delete org1", org1);
//        ass1 = (Association)bqm.getRegistryObject(ass1Id, LifeCycleManager.ASSOCIATION);
//        assertNull("Unable to delete org1", org1);
//    }
//    
//    /**
//     * Tests that association and associated objects are implicitly saved.
//     *
//     *
//     */
//    public void testImlicitSaveReferencedObjects() throws Exception {
//        
//    }
//    
//    /**
//     * Tests that true composed objects, pseudo-composed objects, associations,
//     * associated objects and referenced objects are implicitly saved.
//     *
//     * A pseudo-composed object is not a composed object from the RIM view point.
//     * However, it is composed as far as JAXR API is concerned. Note JAXR 1.0 spec
//     * does not currently make it clear which objects are considered composed object
//     * for each JAXR type.
//     *
//     *
//    public void testImlicitSave() throws Exception {
//        //Create the pkg that is the main object to save explicitly
//        RegistryPackage pkg = lcm.createRegistryPackage("LifeCycleManagerTest.pkg");        
//        
//        //Add a Classification as a true composed object
//        Concept xacmlConcept = (Concept)bqm.getRegistryObject(BindingUtility.CANONICAL_OBJECT_TYPE_ID_xacml, LifeCycleManager.CONCEPT);
//        assertNotNull("Unable to read xacmlConcept", xacmlConcept);
//        Classification classification = (Classification)lcm.createClassification(xacmlConcept);
//        pkg.addClassification(classification);        
//        
//        //Add and Association and an associated object
//        ClassificationScheme scheme = lcm.createClassificationScheme("LifeCycleManagerTest.testScheme1", "LifeCycleManagerTest.testScheme1");
//        String schemeId = scheme.getKey().getId();
//
//        
//        //Add a child Concept as pseudo-composed object
//        Concept node = lcm.createConcept(scheme, "LifeCycleManagerTest.testNode1", "LifeCycleManagerTest.testNode1");
//        String nodeId = node.getKey().getId();
//        scheme.addChildConcept(node);
//
//        
//        
//        
//        //Now save the scheme and implicitly all true-composed, pseudo-composed objects,
//        //asociations, associated objects and referenced objects
//        ArrayList saveObjects = new ArrayList();
//        
//        saveObjects.add(scheme);
//        //saveObjects.add(node);
//        
//        BulkResponse br = lcm.saveObjects(saveObjects);
//        assertTrue("Scheme and node creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//        
//        //Assert that scheme and node were both saved.
//        scheme = (ClassificationScheme)bqm.getRegistryObject(schemeId, LifeCycleManager.CLASSIFICATION_SCHEME);
//        assertNotNull("Unable to read back Scheme", scheme);
//        
//        node = (Concept)bqm.getRegistryObject(nodeId, LifeCycleManager.CONCEPT);
//        assertNotNull("Unable to read back node", node);
//        
//        //Delete scheme and node
//        ArrayList deleteObjects = new ArrayList();
//        deleteObjects.add(scheme.getKey());
//        br = lcm.deleteObjects(deleteObjects);
//        assertTrue("Scheme deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//    }
//    */
//
//    public void testDeprecateObject() throws Exception {
//        RegistryPackage testFolder = lcm.createRegistryPackage("LifeCycleManagerTest.testReferenceToDeprecatedObject");
//        String pkgId = testFolder.getKey().getId();
//
//        //save the object                   
//        ArrayList saveObjects = new ArrayList();        
//        saveObjects.add(testFolder);
//        BulkResponse br = ((LifeCycleManagerImpl)lcm).saveObjects(saveObjects);        
//        assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
//                
//        //Now deprecate the object
//        ArrayList deprecatedObjects = new ArrayList();
//        deprecatedObjects.add(testFolder.getKey());
//        br = ((LifeCycleManagerImpl)lcm).deprecateObjects(deprecatedObjects);
//        
//        //Must use a different user than one who just saved the objects
//        //Use Nikola
//        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
//        DeclarativeQueryManager nikolaDQM = nikolaConnection.getRegistryService().getDeclarativeQueryManager();
//        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
//        
//        //Retrieve object from server.
//        testFolder = (RegistryPackage)nikolaDQM.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
//        assertNotNull("Unable to save RegistryPackage", testFolder);
//        
//        //Assert that object status is Deprecated
//        int status = testFolder.getStatus();
//        assertEquals("Unable to deprecate object", RegistryEntry.STATUS_DEPRECATED, status);
//        
//        //Now try creating a reference to deprecated object. It should fail.
//        Concept concept = (Concept)nikolaDQM.getRegistryObject(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HAS_MEMBER);
//        Association asn = nikolaLCM.createAssociation(testFolder, concept);
//        RegistryPackage rootFolder = (RegistryPackage)nikolaDQM.getRegistryObject(BindingUtility.ROOT_FOLDER_ID);        
//        asn.setSourceObject(rootFolder);
//        saveObjects.clear();
//        saveObjects.add(asn);
//        br = nikolaLCM.saveObjects(saveObjects);
//
//        //find if response status successful or failed
//        assertTrue("Able to create a reference to a deprecated object", br.getStatus()==BulkResponse.STATUS_FAILURE);                        
//    }    


    @SuppressWarnings("static-access")
	public void testAuditTrial() throws Exception {
        RegistryPackage testFolder = lcm.createRegistryPackage("LifeCycleManagerTest.testAuditTrial");
        String pkgId = testFolder.getKey().getId();
	deleteIdToTypeMap.put(pkgId, lcm.REGISTRY_PACKAGE);

        //save the object                   
        ArrayList<RegistryObject> saveObjects = new ArrayList<RegistryObject>();        
        saveObjects.add(testFolder);
        BulkResponse br = (lcm).saveObjects(saveObjects);        
        assertTrue("RegistryPackage creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);

        Collection<?> audit = testFolder.getAuditTrail();
	// trail should include only creation
	assertEquals("Unexpected audit trail size", 1, audit.size());

        //Retrieve object from server.
        testFolder = (RegistryPackage)dqm.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to Retrieve RegistryPackage", testFolder);

        audit = testFolder.getAuditTrail();
	// retrieval should not affect audit trail
	assertEquals("Unexpected audit trail size", 1, audit.size());

        //Now deprecate the object
        ArrayList<Key> deprecatedObjects = new ArrayList<Key>();
        deprecatedObjects.add(testFolder.getKey());
        br = (lcm).deprecateObjects(deprecatedObjects);
        assertTrue("RegistryPackage Deprecation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);

        audit = testFolder.getAuditTrail();
	// deprecation should increase audit trail size
	assertEquals("Unexpected audit trail size", 2, audit.size());

        //save the object                   
        saveObjects = new ArrayList<RegistryObject>();        
        saveObjects.add(testFolder);
        br = (lcm).saveObjects(saveObjects);        
        assertTrue("RegistryPackage save failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);

        audit = testFolder.getAuditTrail();
	// no-op save (no changes) should not increase audit trail size
	assertEquals("Unexpected audit trail size", 2, audit.size());

        //Retrieve object from server.
        testFolder = (RegistryPackage)dqm.getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to Retrieve RegistryPackage", testFolder);

        audit = testFolder.getAuditTrail();
	// retrieval should not affect audit trail
	assertEquals("Unexpected audit trail size", 2, audit.size());
    }

    /**
     * Test audit trail for deterministic order: Should not include
     * multiple entries with identical event timestamps.
     */
    @SuppressWarnings("static-access")
	public void testAuditTrailOrder() throws Exception {
        RegistryPackage testFolder =
	    lcm.createRegistryPackage("LifeCycleManagerTest.testAuditTrialOrder");
        String pkgId = testFolder.getKey().getId();
	deleteIdToTypeMap.put(pkgId, lcm.REGISTRY_PACKAGE);

        // Save the object
        ArrayList<RegistryObject> saveObjects = new ArrayList<RegistryObject>();
        saveObjects.add(testFolder);
        BulkResponse br = (lcm).saveObjects(saveObjects);
        assertResponseSuccess("RegistryPackage creation failed.", br);

	// Perform multiple operations in quick succession
	Date expiration = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
	for (int i = 0; 10 >i; i++ ) {
	    expiration.setTime(expiration.getTime() + (60 * 1000));
	    // TODO: Test getExpiration() after save; presently will fail
	    testFolder.setExpiration(expiration);
	    br = lcm.saveObjects(saveObjects);
	    assertResponseSuccess("Error during test package update", br);
	}

        // Retrieve object from server
        testFolder = (RegistryPackage)dqm.
	    getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to Retrieve RegistryPackage", testFolder);

	// Test our now-long audit trail
        Collection<?> audit = testFolder.getAuditTrail();
	// retrieval should not affect audit trail but expiration changes should
	assertEquals("Unexpected audit trail size", 11, audit.size());

	// Make sure audit trail is correctly ordered w/ increasing timestamps
	// TODO: For now expect failure due to timestamp granularity; seems
	// old Oracle and PostgreSQL defects and their work-arounds affect
	// all databases
	AuditableEvent event;
	Date lastStamp = null, stamp;
	Iterator<?> iter = audit.iterator();
	while (iter.hasNext()) {
	    event = (AuditableEvent)iter.next();
	    stamp = event.getTimestamp();
	    assertNotNull("Auditable event has null timestamp", stamp);
	    if (null != lastStamp) {
		assertTrue("Temporarily expected failure: Timestamp not later " +
			   "than previous in audit trail",
			   stamp.after(lastStamp));
	    }
	    lastStamp = stamp;
	}
    }

    @SuppressWarnings("static-access")
	public void testGuestSubmission() throws Exception {        
        //submit an object
        ExtrinsicObject eo = lcm.createExtrinsicObject((javax.activation.DataHandler)null);
        String id = eo.getKey().getId();
	deleteIdToTypeMap.put(id, lcm.EXTRINSIC_OBJECT);

        ArrayList<RegistryObject> c = new ArrayList<RegistryObject>();
        c.add(eo);
        
        //Make sure you are logged of.
        ((it.cnr.icar.eric.client.xml.registry.ConnectionImpl)(connection)).logoff();
        try {
            lcm.saveObjects(c);
            fail("Did not get Exception from server when saving as Guest.");
	} catch (RuntimeException re) {
	    throw re;
        } catch (Exception t) {
            // ignore, ok
        }
        
        RegistryObject ro = null;
        try {
            ro = dqm.getRegistryObject(id, LifeCycleManager.EXTRINSIC_OBJECT);
        } catch (ObjectNotFoundException e) {
            //Expected: do nothing
        }
        assertNull("RegistryObject should not have been saved by Guest", ro);
    }
    
    @SuppressWarnings("static-access")
	public void testSaveNestedConcepts() throws Exception {
        System.out.println("\ntestSaveNestedConcepts");
        
        final String PARENT_ID = "urn:uuid:" +
            UUIDFactory.getInstance().newUUID().toString();
        final String CHILD_ID = "urn:uuid:" +
            UUIDFactory.getInstance().newUUID().toString();

        Concept grandparentConcept = (Concept)bqm.getRegistryObject(
            BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject, "ClassificationNode");

        Concept parentConcept = lcm.createConcept(grandparentConcept, "NestedParent", "NestedParent");
        parentConcept.setKey(lcm.createKey(PARENT_ID));
	deleteIdToTypeMap.put(PARENT_ID, lcm.CONCEPT);

        Concept childConcept = lcm.createConcept(parentConcept, "NestedChild", "NestedChild");
        childConcept.setKey(lcm.createKey(CHILD_ID));
	deleteIdToTypeMap.put(CHILD_ID, lcm.CONCEPT);

        Collection<RegistryObject> toSave = new ArrayList<RegistryObject>();
        toSave.add(parentConcept);
        toSave.add(childConcept);

	BulkResponse br = lcm.saveObjects(toSave);
	assertResponseSuccess(br);

	parentConcept = (Concept)dqm.getRegistryObject(PARENT_ID, LifeCycleManager.CONCEPT);
	assertNotNull("Parent Concept should have been saved", parentConcept);

	childConcept = (Concept)dqm.getRegistryObject(CHILD_ID, LifeCycleManager.CONCEPT);
	assertNotNull("Child Concept should have been saved", childConcept);

	assertEquals("Parent/Child relationship lost", parentConcept, childConcept.getParent());
	assertEquals("Parent/Child relationship lost", grandparentConcept, parentConcept.getParent());

	assertTrue("Incorrect path for parent Concept", 
		   parentConcept.getPath().startsWith(grandparentConcept.getPath()));
	assertTrue("Incorrect path for child Concept", 
		   childConcept.getPath().startsWith(parentConcept.getPath()));
    }
}
