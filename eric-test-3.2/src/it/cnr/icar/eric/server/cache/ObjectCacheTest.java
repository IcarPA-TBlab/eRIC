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

package it.cnr.icar.eric.server.cache;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryException;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;


/**
 * @author Diego Ballve / Digital Artefacts
 */
public class ObjectCacheTest extends ServerTest {

    public ObjectCacheTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(ObjectCacheTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectNullRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectsNotRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testPutRegistryObjectsNullRegistryObject"));
        //suite.addTest(new ObjectCacheTest("testGetRegistryObjectNullId"));
        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Test that putRegistryObject(RegistryObject ro) does not throw an NPE
     * when the Object is null.
     */
    public void testPutRegistryObjectNullRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNullRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            ObjectCache.getInstance().putRegistryObject(null);            
        } catch (NullPointerException e) {
            fail("putRegistryObject generated NPE with null parameter.");
        }
    }
    
    /**
     * Test that putRegistryObjects(List registryObjects) does not throw an Exception and does not add an object
     * to cache when the Object is null.
     */
    @SuppressWarnings("unchecked")
	public void testPutRegistryObjectsNullRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNotRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ObjectCache cache = ObjectCache.getInstance();
        
        @SuppressWarnings("rawtypes")
		List objects = new ArrayList();
        objects.add(null);
        int preCacheSize = cache.internalCache.getSize();
        cache.putRegistryObjects(objects);
        @SuppressWarnings("unused")
		int postCacheSize = cache.internalCache.getSize();
        assertTrue("Cache size should not have grown", (preCacheSize >= preCacheSize));
    }
    
    /**
     * Test that putRegistryObjects(List registryObjects) throws a RegistryException and does not add an object
     * to cache when the Object is not a RegistryObject.
     */
    public void testPutRegistryObjectsNotRegistryObject() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testPutRegistryObjectNotRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            ArrayList<String> objects = new ArrayList<String>();
            objects.add("This is not a RegistryObject");
            ObjectCache.getInstance().putRegistryObjects(objects);
        } catch (RegistryException e) {
            //Expected
        }
    }
    
    /**
     * Test that getRegistryObjectInternal() does not generate an NPE when a null id is passed
     * and instead returns a null RegistryObject. 
     */
    public void testGetRegistryObjectNullId() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ObjectCacheTest:testGetRegistryObjectInternalNullId", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        try {
            @SuppressWarnings("unused")
			RegistryObjectType ro = ObjectCache.getInstance().getRegistryObject(context, (String)null, "RegistryObject");
        } catch (ObjectNotFoundException e) {
            //Expected
        }
    }
    
    /**
     *  Tries to modify a Classification of a RegistryObject and verify that changes
     *  are also reflected in that object when it is fetched from the cache.
     */
    @SuppressWarnings({ "static-access" })
	public void testModifyComposedObject() throws Exception {
        final String pkgId = "urn:org:freebxml:eric:server:cache:ObjectCacheTest:testModifyComposedObject:pkg";
        final String classifId = "urn:org:freebxml:eric:server:cache:ObjectCacheTest:testModifyComposedObject:classif";

        // initial clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkgId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), classifId);

        // Create pack w/ no classification
        ServerRequestContext context2 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject2", null);
        context2.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
        pkg.setId(pkgId);
        submit(context2, pkg);

        // Check pack w/ no classification
        ServerRequestContext context3 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject3", null);
        context3.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        pkg = (RegistryPackageType)ServerCache.getInstance()
                .getRegistryObject(context3, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertTrue(pkg.getClassification().isEmpty());

        //Must create a new context for each submit or else context vars from prevous submit
        //may resubmit pkg after saving classif
        ServerRequestContext context4 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject4", null);
        context4.setUser(AuthenticationServiceImpl.getInstance().farrukh);

        // Create Classification w/ no slot
        ClassificationType classif = bu.rimFac.createClassificationType();
        classif.setId(classifId);
        classif.setClassifiedObject(pkgId);
        classif.setClassificationNode(bu.CANONICAL_STABILITY_TYPE_ID_Dynamic);
        submit(context4, classif);

        // Check package has classification when fetch without going through cache
        AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest("SELECT * FROM RegistryPackage WHERE id = '" + pkgId + "'");
        ServerRequestContext context5 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject5", queryRequest);
        context5.setUser(ac.registryOperator);
        AdhocQueryResponse queryResp = qm.submitAdhocQuery(context5);
        bu.checkRegistryResponse(queryResp);

        // Make sure that there is at least one object that matched the query
        @SuppressWarnings("unused")
		int cnt = queryResp.getRegistryObjectList().getIdentifiable().size();
        assertEquals("Pkg not found", 1,1);
        pkg = (RegistryPackageType)queryResp.getRegistryObjectList().getIdentifiable().get(0).getValue();
        assertEquals("New classification not present in object retrieved from database.", 1, pkg.getClassification().size());


        // Check pack w/ no classification
        ServerRequestContext context6 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject6", queryRequest);
        context6.setUser(ac.registryOperator);
        pkg = (RegistryPackageType)ServerCache.getInstance()
                .getRegistryObject(context6, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals("New classification not present in cached object.", 1, pkg.getClassification().size());

        ServerRequestContext context7 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject7", queryRequest);
        context7.setUser(ac.registryOperator);
        classif  = (ClassificationType)ServerCache.getInstance()
              .getRegistryObject(context7, classifId, LifeCycleManager.CLASSIFICATION);
        assertNotNull(classif);

        ServerRequestContext context8 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject8", null);
        context8.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        // add Slot to Classification
        SlotType1 slot = bu.rimFac.createSlotType1();
        slot.setName("slot1");
        ValueListType valueList = bu.rimFac.createValueListType();
        slot.setValueList(valueList);
        //String value = bu.rimFac.createValue("value1");
        valueList.getValue().add("value1");
        @SuppressWarnings("unused")
		String value = "value1";
        classif.getSlot().add(slot);
        submit(context8, classif);

        // Check classification w/ slot
        ServerRequestContext context9 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject9", null);
        context9.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        classif  = (ClassificationType)ServerCache.getInstance()
            .getRegistryObject(context9, classifId, LifeCycleManager.CLASSIFICATION);
        assertNotNull(classif);
        assertEquals("New slot not present in cached object.", 1, classif.getSlot().size());

        // Check classification w/ slot from pack
        ServerRequestContext context10 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject10", null);
        context10.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        pkg = (RegistryPackageType)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("New slot not present in cached object.", 1, classif.getSlot().size());

        slot = classif.getSlot().get(0);
        assertNotNull(classif);
        assertEquals("slot1", slot.getName());

        // change status through approve
        ServerRequestContext context11 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject11", null);
        context11.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        approve(context11, classif.getId());
        pkg = (RegistryPackageType)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("Status not changed in cached composed object.", BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved, classif.getStatus());

        // change status through setStatus
        ServerRequestContext context12 = new ServerRequestContext("ClassificationSchemeCacheTest:testModifyComposedObject12", null);
        context12.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        setStatus(context12, classif.getId(), BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn);
        pkg = (RegistryPackageType)ServerCache.getInstance()
            .getRegistryObject(context10, pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull(pkg);
        assertEquals(1, pkg.getClassification().size());

        classif = pkg.getClassification().get(0);  
        assertNotNull(classif);
        assertEquals("Status not changed in cached composed object.", BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn, classif.getStatus());

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkgId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), classifId);
    }        
    
}
