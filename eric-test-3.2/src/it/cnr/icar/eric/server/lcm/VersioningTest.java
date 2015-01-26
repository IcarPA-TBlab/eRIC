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

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.exceptions.RepositoryItemNotFoundException;
import it.cnr.icar.eric.server.cms.CMSTestUtility;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.repository.RepositoryItemKey;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;

import junit.framework.Test;


import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * @author Farrukh Najmi
 *
 * Test the Versioning feature.
 * These tests must be run in specific order.
 * Currently they run in the order the test methods appear in code.
 * Do not change this order.
 *
 */
public class VersioningTest extends ServerTest {
            
    static String originalObjectId = null;
    static String lid = "urn:freebxml:registry:VersioningTest:TestExtrinsicObject";
    private String versionableClassList_oldValue;
    protected static URL cppaURL = CMSTestUtility.class.getResource(
            "/resources/CPP1.xml");

    
    /**
     * Constructor for VersioningTest.
     *
     * @param name
     */
    public VersioningTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        // save old value and restore it in tearDown.. is this needed?
        versionableClassList_oldValue = RegistryProperties.getInstance().
                getProperty("eric.server.lcm.VersionManager.versionableClassList");
        
        // Make sure our test object class is marked as versionable
        RegistryProperties.getInstance().put(
                "eric.server.lcm.VersionManager.versionableClassList", "ExtrinsicObject");
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        
        if (versionableClassList_oldValue != null) {
            RegistryProperties.getInstance().put(
                    "eric.server.lcm.VersionManager.versionableClassList", versionableClassList_oldValue);
        }
    }
    
    public static Test suite() {
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new VersioningTest("testSetup"));
        suite.addTest(new VersioningTest("testVersioningOfCatalogedRepositoryItem"));        
        suite.addTest(new VersioningTest("testAddComposedObjects"));        
        suite.addTest(new VersioningTest("testSubmitOriginalROVersionNoLid"));
        suite.addTest(new VersioningTest("testSubmitOriginalROVersionLidSpecified"));
        suite.addTest(new VersioningTest("testDontVersionROOnUpdate"));
        suite.addTest(new VersioningTest("testVersionROOnlyOnUpdate1"));
        suite.addTest(new VersioningTest("testVersionROOnlyOnUpdate2"));
        suite.addTest(new VersioningTest("testVersionRIOnUpdate1"));
        suite.addTest(new VersioningTest("testVersionRIOnUpdate2"));
        suite.addTest(new VersioningTest("testRepositoryItemsForAllVersions"));
        suite.addTest(new VersioningTest("testVersionNoVersionInfo"));
        suite.addTest(new VersioningTest("testVersionOver10"));
        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }        
    
    public void testSetup() throws Exception {
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + lid + "' ");
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testSetup", removeRequest);
        context.setUser(ac.registryOperator);
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }
    
    /**
     * Publishes RO with a CPA RI which gets cataloged by the CPA Cataloger.
     *
     * Verfies that repository item is not lost in the versioned RO/RI.
     *
     */
    public void testVersioningOfCatalogedRepositoryItem() throws Exception {
        String lid = "urn:freebxml:registry:test:VersioningTest:testVersioningOfCatalogedRepositoryItem:eo";
        RegistryResponseType resp = null;
        
        try {
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            eo.setMimeType("text/xml");
            eo.setObjectType("urn:freebxml:registry:sample:profile:cpp:objectType:cppa:CPP");
            setVersionInfo(eo, "1.1", "1.1");
            eo.setId(lid);
            eo.setLid(lid);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            submitRequest.setComment( "1.1" );

            //Save first version
            idToRepositoryItemMap.clear();
            DataHandler dh = new javax.activation.DataHandler(cppaURL);
            RepositoryItem ri1 = new RepositoryItemImpl(lid, dh);
            idToRepositoryItemMap.put(lid, ri1);
            
            ServerRequestContext context = new ServerRequestContext("VersioningTest:testVersioningOfCatalogedRepositoryItem", submitRequest);
            context.setUser(ac.registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            eo = getLatestExtrinsicObjectVersion(lid, 1);
            RepositoryItem ri = rm.getRepositoryItem(eo.getId());
            assertTrue("RepsitoryItem was lost during versioning.", (ri != null));
            
            //Save second version
            submitRequest = bu.createSubmitRequest(false, false, null);
            setVersionInfo(eo, "1.2", "1.2");
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            submitRequest.setComment( "1.2" );

            idToRepositoryItemMap.clear();
            dh = new javax.activation.DataHandler(cppaURL);
            RepositoryItem ri2 = new RepositoryItemImpl(lid, dh);
            idToRepositoryItemMap.put(lid, ri2);
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            
            eo = getLatestExtrinsicObjectVersion(lid, 2);
            ri = rm.getRepositoryItem(eo.getId());
            assertTrue("RepsitoryItem was lost during versioning.", (ri != null));
        } catch (RepositoryItemNotFoundException e) {
            fail("RepsitoryItem was lost during versioning. This is a known bug and Farrukh is working on it.");
        } catch (Exception e) {
            throw e;
        } finally {            
            //Now cleanup.
            String queryStr = "SELECT * FROM ExtrinsicObject WHERE lid = '" + lid + "' ";
            ServerRequestContext context = new ServerRequestContext("VersioningTest:testAddComposedObjects", null);
            context.setUser(ac.registryOperator);
            remove(context, null, queryStr, forceRemoveRequestSlotsMap);
        }        
    }
    
    /**
     * Tests for bug found by Kim, Paul where add adding a composed object 
     * (e.g. Classification) to an existing object added it to the old version 
     * rather than the new.
     *
     */
    @SuppressWarnings({ "static-access" })
	public void testAddComposedObjects() throws Exception {
        String id = "urn:freebxml:registry:test:VersioningTest:testAddComposedObjects:eo";
        RegistryResponseType resp = null;
        
        try {
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            eo.setMimeType("text/plain");
            setVersionInfo(eo, "1.1", null);
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

            submitRequest.setComment( "1.1" );

            //Save first time with no composed Classification
            idToRepositoryItemMap.clear();
            ServerRequestContext context = new ServerRequestContext("VersioningTest:testAddComposedObjects", submitRequest);
            context.setUser(ac.registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);

            //Now save a new version with a composed Classification            
            ClassificationType classification1 = bu.createClassificationType(id, bu.CANONICAL_OBJECT_TYPE_ID_XML);
            classification1.setId("urn:freebxml:registry:test:VersioningTest:testAddComposedObjects:classification1");
            InternationalStringType classificationName1 = bu.createInternationalStringType("classification1");
            classification1.setName(classificationName1);
            eo.getClassification().add(classification1);

            submitRequest = bu.createSubmitRequest(false, false, null);        
            submitRequest.setComment( "1.1" );
            eo.setLid(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            idToRepositoryItemMap.clear();
            context = new ServerRequestContext("VersioningTest:testAddComposedObjects", submitRequest);
            context.setUser(ac.registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            
            //TODO: include other composed objects in test

            //Now make sure that original version does not have the classification that was just added to new version
            AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + id + "' AND versionName='" + "1.1'");
            context = new ServerRequestContext("VersioningTest:testAddComposedObjects", queryRequest);
            context.setUser(ac.registryOperator);
            AdhocQueryResponse queryResp = qm.submitAdhocQuery(context);
            bu.checkRegistryResponse(queryResp);

            List<?> results = queryResp.getRegistryObjectList().getIdentifiable();
            RegistryObjectType originalRO = (RegistryObjectType)((JAXBElement<?>)results.get(0)).getValue();
            assertTrue("Found Classifications on original object when none were expected.", (originalRO.getClassification().size() == 0));

            //Now make sure that new version does have the classification that was just added to new version
            queryRequest = bu.createAdhocQueryRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + id + "' AND versionName='" + "1.2'");
            context = new ServerRequestContext("VersioningTest:testAddComposedObjects", queryRequest);
            context.setUser(ac.registryOperator);
            queryResp = qm.submitAdhocQuery(context);
            bu.checkRegistryResponse(queryResp);

            results = queryResp.getRegistryObjectList().getIdentifiable();
            RegistryObjectType newRO = (RegistryObjectType)((JAXBElement<?>)results.get(0)).getValue();
            assertEquals("Did not find correct number of Classifications.", 1, newRO.getClassification().size());        
        } catch (Exception e) {
            throw e;
        } finally {            
            //Now cleanup.
            String queryStr = "SELECT * FROM ExtrinsicObject WHERE lid = '" + id + "' ";
            ServerRequestContext context = new ServerRequestContext("VersioningTest:testAddComposedObjects", null);
            context.setUser(ac.registryOperator);
            remove(context, null, queryStr, forceRemoveRequestSlotsMap);
        }
    }
    
    /**
     * Test publish of an RO with no lid specified and no repository item.
     * Test that the lid defaults to the specified id.
     *
     * Why is this test in VersioningTest instead of LCMTest??
     */
    public void testSubmitOriginalROVersionNoLid() throws Exception {        
        //Submit original version
        originalObjectId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, true, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setMimeType("text/plain");
        setVersionInfo(eo, "1.1", null);
        eo.setId(originalObjectId);
        //eo.setLid(lid);   //Do not setLid for this test
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
        submitRequest.setComment( "1.1" );
                
        idToRepositoryItemMap.clear();
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testSubmitOriginalROVersionNoLid", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Read original version and verify that version 1.0 has been assigned
        eo = (ExtrinsicObjectType)qm.getRegistryObject(context, originalObjectId);        
        checkExtrinsicObject(eo, "1.1", null, false);
        
        String _lid = eo.getLid();
        assertEquals("Registry did not assign id as value for lid when client did not specify it.", originalObjectId, _lid);        
    }
       
    /**
     * Test publish of an RO with lid specified and no repository item.
     * Tests that the registry honours the lid that was specified.
     * Note that subsequent tests will version this object. 
     *
     * Why is this test in VersioningTest instead of LCMTest??
     */
    public void testSubmitOriginalROVersionLidSpecified() throws Exception {        
        //Submit original version
        originalObjectId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, true, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setMimeType("text/plain");
        setVersionInfo(eo, "1.1", null);
        eo.setId(originalObjectId);
        eo.setLid(lid);   //Do setLid for this test
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
        submitRequest.setComment( "1.1" );
        
        idToRepositoryItemMap.clear();        
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testSubmitOriginalROVersionLidSpecified", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Read original version and verify that version 1.0 has been assigned
        ExtrinsicObjectType originalObject = (ExtrinsicObjectType)qm.getRegistryObject(context, originalObjectId);
        checkExtrinsicObject(originalObject, "1.1", null, false);        
                
        String _lid = originalObject.getLid();
        assertEquals("Registry did not honour lid when client did specify it.", lid, _lid);        
    }
       
    /**
     * Republishes same RO with RO versioning OFF and RI versioning ON.
     *
     * Tests that neither RO nor RI are versioned when RO versioning is OFF
     * even if RI versioning is ON.
     *
     */
    @SuppressWarnings("static-access")
	public void testDontVersionROOnUpdate() throws Exception {        
        String contentVersion = "1.1";
        
        //Read original version and verify that version 1.0 has been assigned
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testDontVersionROOnUpdate", null);
        context.setUser(ac.registryOperator);
        ExtrinsicObjectType eo = (ExtrinsicObjectType)qm.getRegistryObject(context, originalObjectId);        
        setVersionInfo(eo, "1.1", "1.1");
                
        //dontVersionContent is false but it should be overridden to be true when dontVersion is true
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(true, false, null);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
        submitRequest.setComment( "1.1" );
        
        RepositoryItem ri = createRepositoryItem(originalObjectId, contentVersion, ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR, true);
        idToRepositoryItemMap.clear();
        idToRepositoryItemMap.put(originalObjectId, ri);
        
        context = new ServerRequestContext("VersioningTest:testDontVersionROOnUpdate", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        eo = getLatestExtrinsicObjectVersion(lid, 1);                        
        checkExtrinsicObject(eo, "1.1", contentVersion, true);                
    }
    
    /**
     * Republishes same RO with RO versioning ON and RI versioning OFF.
     *
     * Tests that RO is versioned but RI is not versioned.
     *
     */
    public void testVersionROOnlyOnUpdate1() throws Exception {        
        update(false, true, "1.2", 2, "1.1", 1);
    }
    
    /**
     * Republishes same RO with RO versioning ON and RI versioning OFF.
     *
     * Tests that again RO is versioned but RI is not versioned.
     *
     */
    public void testVersionROOnlyOnUpdate2() throws Exception {        
        update(false, true, "1.3", 3, "1.1", 1);
    }
    
    /**
     * Republishes same RO with both RO versioning ON and RI versioning ON.
     *
     * Tests that RO as well as RI are versioned.
     *
     */
    public void testVersionRIOnUpdate1() throws Exception {        
        update(false, false, "1.4", 4, "1.2", 2);
    }
    
    /**
     * Republishes same RO with both RO versioning ON and RI versioning ON.
     *
     * Tests that again RO as well as RI are versioned.
     *
     */
    public void testVersionRIOnUpdate2() throws Exception {        
        update(false, false, "1.5", 5, "1.3", 3);
    }

    /**
     * Republishes many times the same RO with both RO versioning ON and RI versioning ON.
     *
     * Tests that RO as well as RI are versioned beyond version 1.10
     *
     */
    public void testVersionOver10() throws Exception {
        update(false, false, "1.7", 7, "1.4", 4);
        update(false, false, "1.8", 8, "1.5", 5);
        update(false, false, "1.9", 9, "1.6", 6);
        update(false, false, "1.10", 10, "1.7", 7);
        update(false, false, "1.11", 11, "1.8", 8);
        update(false, false, "1.12", 12, "1.9", 9);
        update(false, false, "1.13", 13, "1.10", 10);
        update(false, false, "1.14", 14, "1.11", 11);
    }
    
    /**
     * Tests an update without version info and without RepositoryItem
     *
     */
    public void testVersionNoVersionInfo() throws Exception {        
        //update(false, false, "1.6", 6, null, 0);
        String newId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, true, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setMimeType("text/plain");
        //setVersionInfo(eo, "1.1", null); //Do not set versionInfo for this test
        eo.setId(newId);
        eo.setLid(lid);   
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
        
        idToRepositoryItemMap.clear();        
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testVersionNoVersionInfo", submitRequest);
        context.setUser(ac.registryOperator);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        String alteredID = context.getIdMap().get(newId);  // Refresh the id from submitted object, since the versioning process might have suffixed it with version info
            
        if( alteredID != null ) {
            newId = alteredID; 
        }
        
        //Read original version and verify that version 1.0 has been assigned
        ExtrinsicObjectType eot = (ExtrinsicObjectType)qm.getRegistryObject(context, newId);
        assertEquals("Registry did not assign correct versionName", "1.6", eot.getVersionInfo().getVersionName());
        assertNull("versionName does not match comment.", eot.getVersionInfo().getComment());

        RepositoryItem ri = qm.getRepositoryItem(context, eot.getId());
        assertNotNull("Unable to read back RepositoryItem", ri);
                
        String _lid = eot.getLid();
        assertEquals("Registry did not honour lid when client did specify it.", lid, _lid);        
    }
    
    /**
     * Updates ExtrinsicObject and RepositoryItem.
     *
     * @param dontVersion if true dont version ExtrinsicObject
     * @param dontVersionContent if true dont version RepositoryItem. Implicitly true if dontVersion params is true
     * @param newVersionName the expected value for ExtrinsicObject.versionName 
     * @param newVersionCount the expected count or ExtrinsicObject versions 
     * @param newContentVersionName the expected value for ExtrinsicObject.contentVersionName and RepositoryItem.versionName
     * @param newContentVersionCount the expected count or RepositoryItem versions 
     * 
     */
    private void update(boolean dontVersion, boolean dontVersionContent,
        String newVersionName, int newVersionCount, 
        String newContentVersionName, int newContentVersionCount) throws Exception {
            
        ExtrinsicObjectType eo = getLatestExtrinsicObjectVersion(lid, newVersionCount-1);
        setVersionInfo(eo, newVersionName, newContentVersionName);
                
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(dontVersion, dontVersionContent, null);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
        submitRequest.setComment( newVersionName );
        
        String content = newContentVersionName;
        @SuppressWarnings("static-access")
		RepositoryItem ri = createRepositoryItem(eo.getId(), content, ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR, true);
        idToRepositoryItemMap.clear();
        idToRepositoryItemMap.put(eo.getId(), ri);
        
        ServerRequestContext context = new ServerRequestContext("VersioningTest:update", submitRequest);
        context.setUser(ac.registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        eo = getLatestExtrinsicObjectVersion(lid, newVersionCount);
        checkExtrinsicObject(eo, newVersionName, newContentVersionName, true);
    }
        
    /**
     * Fetches all versions of ExtrinsicObject, then fetches RepositoryItem
     * for each ExtrinsicObject version and confirms that it has the expected 
     * content. Content should match the contentVersionInfo on the ExtrinsicObject.
     *
     */
    public void testRepositoryItemsForAllVersions() throws Exception {
        AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + lid + "' ORDER BY versionName DESC");
        ServerRequestContext context = new ServerRequestContext("VersioningTest:testRepositoryItemsForAllVersions", queryRequest);
        context.setUser(ac.registryOperator);
        AdhocQueryResponse queryResp = qm.submitAdhocQuery(context);
        bu.checkRegistryResponse(queryResp);
        
        List<JAXBElement<? extends IdentifiableType>> results = queryResp.getRegistryObjectList().getIdentifiable();
        
        Iterator<JAXBElement<? extends IdentifiableType>> iter = results.iterator();
        while (iter.hasNext()) {
            ExtrinsicObjectType eo = (ExtrinsicObjectType)iter.next().getValue();
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            
            if (contentVersionInfo != null) {
                String contentVersionName = contentVersionInfo.getVersionName();

                if ((contentVersionName != null) && (contentVersionName.length() > 0)) {
                    checkRepositoryItemForExtrinsicObject(eo);
                }
            }
        }
    }
    
    private void checkExtrinsicObject(
        ExtrinsicObjectType eo,
        String versionName, 
        String contentVersionName, 
        boolean checkRepositoryItem) throws Exception {
            
        assertEquals("Registry did not assign correct versionName", versionName, eo.getVersionInfo().getVersionName());
        assertEquals("versionName does not match comment.", versionName, eo.getVersionInfo().getComment());
        
        if (checkRepositoryItem) {
            //Make sure that repository item was not lost during versioning
            RepositoryItemKey riKey = new RepositoryItemKey(eo.getLid(), contentVersionName);
            assertTrue("RepsitoryItem was lost during versioning.", rm.itemExists(riKey));
            
            assertEquals("Registry did not assign correct contentVersionName", contentVersionName, eo.getContentVersionInfo().getVersionName());
            assertEquals("contentVersionName does not match contentComment.", contentVersionName, eo.getContentVersionInfo().getComment());
            
            checkRepositoryItemForExtrinsicObject(eo);
        } else {
            assertNull("Registry did not set contentVersion to null.", eo.getContentVersionInfo());
        }
        
    }
    
    private void checkRepositoryItemForExtrinsicObject(ExtrinsicObjectType eo) throws Exception {        
        String contentVersionName = eo.getContentVersionInfo().getVersionName();

        ServerRequestContext context = new ServerRequestContext("VersioningTest:checkRepositoryItemForExtrinsicObject", null);
        context.setUser(ac.registryOperator);
        RepositoryItem ri = qm.getRepositoryItem(context, eo.getId());
        assertNotNull("Unable to read back RepositoryItem", ri);

        InputStream in = ri.getDataHandler().getInputStream();
        assertNotNull(in);
        byte actual[] = readBytes(in);

        File file = createTempFile(true, contentVersionName);
        byte expected[] = readBytes(new DataHandler(new FileDataSource(file)).getInputStream());

        assertTrue("Content has changed!", Arrays.equals(expected, actual)); 
    }
        
    private ExtrinsicObjectType getLatestExtrinsicObjectVersion(String eoLid, int expectedVersionCount) throws Exception {
        AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest("SELECT * FROM ExtrinsicObject WHERE lid = '" + eoLid + "'");
        ServerRequestContext context = new ServerRequestContext("VersioningTest:getLatestExtrinsicObjectVersion", queryRequest);
        context.setUser(ac.registryOperator);
        AdhocQueryResponse queryResp = qm.submitAdhocQuery(context);
        bu.checkRegistryResponse(queryResp);
        
        List<?> results = queryResp.getRegistryObjectList().getIdentifiable();
        assertEquals("Registry did not create a new version.", expectedVersionCount, results.size());
        ExtrinsicObjectType eo = getLatest(results);
        return eo;
    }
    
    private void setVersionInfo(ExtrinsicObjectType eo, String versionName, String contentVersionName) throws Exception {
        //Set versionName on comment and name
        VersionInfoType versionInfo = bu.rimFac.createVersionInfoType();
        versionInfo.setVersionName("Error");
        eo.setName(bu.createInternationalStringType(versionName)); 
        eo.setVersionInfo(versionInfo);
        
        //Set contentVersionName on contentComment and description
        if (contentVersionName != null) {
            VersionInfoType contentVersionInfo = bu.rimFac.createVersionInfoType();       
            contentVersionInfo.setVersionName("Error");
            contentVersionInfo.setComment(contentVersionName);
            eo.setDescription(bu.createInternationalStringType(contentVersionName));
            eo.setContentVersionInfo(contentVersionInfo);
        }
    }
    
    private ExtrinsicObjectType getLatest(Collection<?> selected) {
        if (selected == null || selected.isEmpty()) {
            throw new RuntimeException("Null or empty collection.");
        } else if (selected.size() > 1) {
            String latestVersion = null;
            String lid = null;
            ExtrinsicObjectType latest = null;
            for (@SuppressWarnings("unchecked")
			Iterator<JAXBElement<?>> it = (Iterator<JAXBElement<?>>) selected.iterator(); it.hasNext(); ) {
                if (latest == null) {
                    latest = (ExtrinsicObjectType)it.next().getValue();
                    lid = latest.getLid();
                    latestVersion = latest.getVersionInfo().getVersionName();
                    continue;
                }

                ExtrinsicObjectType next = (ExtrinsicObjectType)(it.next()).getValue();
                String nextLid = next.getLid();
                String nextVersion = next.getVersionInfo().getVersionName();
                
                if (!nextLid.equals(lid)) {
                    throw new RuntimeException("Multiple LIDs found. Expecting only 1.");
                }
                
                if (compareVersions(nextVersion, latestVersion) > 0) {
                    latest = next;
                    lid = latest.getLid();
                    latestVersion = latest.getVersionInfo().getVersionName();
                }
            }
            return latest;
        } else {
            return (ExtrinsicObjectType)((JAXBElement<?>)selected.iterator().next()).getValue();
        }
    }
    
    /**
     * Returns
     * - 0 if params are equal
     * - +1 if 1st is greater than 2nd
     * - -1 if 2nd is greater than 1st
     */
    private int compareVersions(String v1, String v2) {
        String parts1 [] = v1.split("\\.", 2);
        String parts2 [] = v2.split("\\.", 2);
        
        int iCompare = Integer.parseInt(parts1[0]) - Integer.parseInt(parts2[0]);
        if (iCompare == 0) {
            // equal.. try subversions
            if (parts1.length == 1 && parts2.length == 1) {
                // really equal
                return 0;
            } else if (parts1.length == 1) {
                // other is bigger (v2)
                return -1;
            } else if (parts2.length == 1) {
                // other is bigger (v1)
                return +1;
            } else {
                // try subversions
                return compareVersions(parts1[1], parts2[1]);
            }
        } else {
            return iCompare;
        }
    }    
}
