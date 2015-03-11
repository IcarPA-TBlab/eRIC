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
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * jUnit Test used as micro benchmark for eric performance.
 *
 * testXXXQuery Notes:
 * 1. Parameterized queries should use different parameters in each invocation
 * otherwise a good DBMS impl will cache the query plan even when no PreparedStataments
 * are used making it hard to measure performance improvements of PreparedStataments
 *
 * @author Farrukh Najmi
 */
public class EricBenchmarkTest extends ClientTest {
    
    static int count=0;
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EricBenchmarkTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new EricBenchmarkTest("testGetRegistryObject"));
        //suite.addTest(new EricBenchmarkTest("testPublishOneExtrinsicObject"));
        //suite.addTest(new EricBenchmarkTest("testPublishOneExtrinsicObjectWithSlots"));
        //suite.addTest(new EricBenchmarkTest("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem"));
        //suite.addTest(new EricBenchmarkTest("testPublishOneExtrinsicObjectWithSlotsClassifications"));
        //suite.addTest(new EricBenchmarkTest("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem"));
        
        /*
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        suite.addTest(new EricBenchmarkTest("testQueryRegistryObjectById"));
        for (int i=0; i<1; i++) {
            //suite.addTest(new EricBenchmarkTest("testQueryAuditTrailForRegistryObject"));
            suite.addTest(new EricBenchmarkTest("testQueryBasic"));
        }
         */
        return suite;
    }
    
    
    public EricBenchmarkTest(String testName) {
        super(testName);        
    }
    
    public void testPublishOneExtrinsicObject() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObject: entered");
 
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObject", false, false, false);
        ArrayList<ExtrinsicObject> saveObjects = new ArrayList<ExtrinsicObject>(); 
        saveObjects.add(eo);
        BulkResponse br = (lcm).saveObjects(saveObjects);        

        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObject: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlots() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlots: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlots", true, false, false);        
        ArrayList<ExtrinsicObject> saveObjects = new ArrayList<ExtrinsicObject>();        
        saveObjects.add(eo);
        BulkResponse br = (lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlots: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlotsClassifications() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassifications: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlotsClassifications", true, true, false);
        ArrayList<ExtrinsicObject> saveObjects = new ArrayList<ExtrinsicObject>();        
        saveObjects.add(eo);
        BulkResponse br = (lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassifications: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem: entered");
        ExtrinsicObject eo = createExtrinsicObject("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem", true, true, true);        
        ArrayList<ExtrinsicObject> saveObjects = new ArrayList<ExtrinsicObject>();        
        saveObjects.add(eo);
        BulkResponse br = (lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishOneExtrinsicObjectWithSlotsClassificationsRepositoryItem: exit elapedTimeMillis=" + (endTime-startTime));
    }
    
    public void testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem() throws Exception {
        long startTime = System.currentTimeMillis();
        System.err.println("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem: entered");
        ArrayList<ExtrinsicObject> saveObjects = new ArrayList<ExtrinsicObject>();        
        
        int eoCount = 10;
        for (int i=0; i<eoCount; i++) {
            ExtrinsicObject eo = createExtrinsicObject("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem", true, true, true);        
            saveObjects.add(eo);
        }
        
        BulkResponse br = (lcm).saveObjects(saveObjects);        
        assertTrue("ExtrinsicObject creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
        
        long endTime = System.currentTimeMillis();
        System.err.println("testPublishManyExtrinsicObjectWithSlotsClassificationsRepositoryItem: exit elapedTimeMillis=" + (endTime-startTime));
    }

    private ExtrinsicObject createExtrinsicObject(
            String nameSuffix, 
            boolean addSlots, 
            boolean addClassifications, 
            boolean addRepositoryItem) throws Exception {
        
        ExtrinsicObject eo = lcm.createExtrinsicObject((javax.activation.DataHandler)null);
        InternationalString is = lcm.createInternationalString("EricBenchmarkTest." + nameSuffix);
        eo.setName(is);
        
        if (addSlots) {
            ArrayList<Slot> slots = new ArrayList<Slot>();
            int slotCnt = 10;
            for (int i=0; i<slotCnt; i++) {
                String slotName = "slot" + i;
                Slot slot = lcm.createSlot(slotName, slotName + "Value1", null);
                slots.add(slot);
            }
            eo.addSlots(slots);
        }
        
        if (addClassifications) {
            ArrayList<Classification> classifictions = new ArrayList<Classification>();
            ClassificationScheme associationTypeScheme = (ClassificationScheme)bqm.getRegistryObject(CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType);
            Collection<?> concepts = associationTypeScheme.getChildrenConcepts();
            Iterator<?> iter = concepts.iterator();
            while (iter.hasNext()) {
                Concept concept = (Concept)iter.next();
                Classification c = lcm.createClassification(concept);
                classifictions.add(c);
            }
            eo.addClassifications(classifictions);
        }
        
        if (addRepositoryItem) {
            //Add repository item: current size ~5KB
            String riResourceName = "/resources/StandaloneTest.wsdl";
            URL riResourceUrl = getClass().getResource(riResourceName);
            assertNotNull("Missing test resource: " + riResourceName, riResourceUrl);
            File repositoryItemFile = new File(riResourceUrl.getFile());
            assertTrue("Missing test resource: " + riResourceUrl.getFile(), repositoryItemFile.canRead());
            DataHandler repositoryItem = new DataHandler(new FileDataSource(repositoryItemFile));
            eo.setRepositoryItem(repositoryItem);
        }
        
        return eo;
    }
    
    public void testUpdate() throws Exception {
    }
    
    /**
     * Gets a RegistryObject by id using qm.getRegistryObject(...)
     */
    @SuppressWarnings("unused")
	public void testGetRegistryObject() throws Exception {
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        RegistryObject existingRO = bqm.getRegistryObject(CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
        RegistryObject nonExistingRO = bqm.getRegistryObject(id);        
    }    
    
    /**
     * Gets a RegistryObject by id with table being RegistryObject.
     */
    public void testQueryRegistryObjectById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        HashMap<String, String> queryParams = new HashMap<String, String>();
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        queryParams.put("$id", id);
        queryParams.put("$tableName", "RegistryObject");
        try {
            @SuppressWarnings("unused")
			Collection<?> registryObjects = executeQuery(queryId, queryParams);
        } catch (ObjectNotFoundException e) {
            //These are being thrown incorrectly. Fix qm.getRegistryObject() to not 
            //handle this exception and return a null for backward compatibility
        }
    }
    
    /**
     * Gets a RegistryObject by id with table being a leaf RIM class
     * Served from cache so will be fast if cache is primed.
     */
    public void testQueryLeafObjectById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map<String, String> queryParams = new HashMap<String, String>();
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        queryParams.put("$id", id);
        queryParams.put("$tableName", "Service");
        try {
            @SuppressWarnings("unused")
			Collection<?> registryObjects = executeQuery(queryId, queryParams);
        } catch (ObjectNotFoundException e) {
            //These are being thrown incorrectly. Fix qm.getRegistryObject() to not 
            //handle this exception and return a null for backward compatibility
        }
    }
    
    /**
     * Gets audit trail for a RegistryObject.
     */
    public void testQueryAuditTrailForRegistryObject() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetAuditTrailForRegistryObject;
        HashMap<String, String> queryParams = new HashMap<String, String>();
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        queryParams.put("$lid", id);
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get scheme by id. 
     * Served from cache so will be fast if cache is primed.
     */
    public void testQuerySchemeById() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetClassificationSchemesById;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$idPattern", CanonicalSchemes.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get User for caller.
     */
    public void testQueryCallersUser() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetCallersUser;
        Map<String, String> queryParams = new HashMap<String, String>();
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    
    /**
     * Get RegistryObjects owned by caller. 
     * Potentially very time consuming.
     */
    public void testQueryCallersObjects() throws Exception {
        /* Need to implemented this query
        SAtring queryId = CanonicalConstants.CANONICAL_QUERY_GetCallersObjects;
        Map queryParams = new HashMap();
        Collection registryObjects = executeQuery(queryId, queryParams);
         */
    }
    
    /**
     *  Gets objects matching specified name, description, status, classifications.
     */
    /*
    public void testQueryBasic() throws Exception {
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        String queryId = CanonicalConstants.CANONICAL_QUERY_BasicQuery;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$objectTypePath", "%/RegistryObject");
        queryParams.put("$name", id);
        queryParams.put("$classificationPath1", id);
        queryParams.put("$description", id+"%");
        queryParams.put("$status", "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:StatusType/Submitted");
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    */
    
    /**
     * WSDLDiscoveryQuery: Find WSDL files with $targetNamespace matching "%urn:goes:here"
     */
    /*
    public void testQueryWsdl() throws Exception {
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        String queryId = it.cnr.icar.eric.common.profile.ws.wsdl.CanonicalConstants.CANONICAL_QUERY_WSDL_DISCOVERY;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$targetNamespace", id);
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    */
    
    /**
     * WSDL ServiceDiscoveryQuery: Find WSDL Service with $service.name matching "%regrep%"
     */
    /*
    public void testQueryWsdlService() throws Exception {
        String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        String queryId = it.cnr.icar.eric.common.profile.ws.wsdl.CanonicalConstants.CANONICAL_QUERY_SERVICE_DISCOVERY;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$service.name", id);
        queryParams.put("$considerPort", "1");
        queryParams.put("$considerBinding", "0");
        queryParams.put("$considerPortType", "0");
        @SuppressWarnings("unused")
		Collection<?> registryObjects = executeQuery(queryId, queryParams);
    }
    */
    
    public void testDelete() throws Exception {
        //Find and delete all objects created by this test
        //All objects created by this test are assumed to have name prefix EricBenchmarkTest.
        //Also need to delete AuditableEvents for these objects somehow in future to have zero growth.
        String namePattern = "EricBenchmarkTest%";

        it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl ahq = 
                    (it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl)lcm.createObject("AdhocQuery");
        ahq.setString("SELECT ro.id FROM RegistryObject ro, Name_ nm WHERE nm.value LIKE '" + namePattern + "' AND ro.id = nm.parent");
        ArrayList<?> keys = new ArrayList<Object>();
        
        try {
            //Now do the delete
            @SuppressWarnings("unused")
			BulkResponse br = (lcm).deleteObjects(keys, ahq, null, null);
        } catch (Exception e) {
            //Temporary hack to workaround concurrency related bug in server here.
            //Otherwise japex just hangs on the error.
        }
    }
    
}
