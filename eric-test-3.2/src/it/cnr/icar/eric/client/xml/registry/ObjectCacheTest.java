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
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Unit tests for ObjectCache.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class ObjectCacheTest extends ClientTest {

    public ObjectCacheTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ObjectCacheTest.class);
        return suite;
    }

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Test of isCached method, of class it.cnr.icar.eric.client.xml.registry.ObjectCache.
     */
    public void testIsCached() {
        System.out.println("/ntestIsCached");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();
        assertTrue(!cache.isCached("non-existing-id"));
    }

    /**
     * Test of putRegistryObject method, of class it.cnr.icar.eric.client.xml.registry.ObjectCache.
     */
    public void testPutRegistryObject() throws Exception {
        System.out.println("/ntestPutRegistryObject");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        assertTrue(cache.isCached(testObject.getKey().getId()));
    }

    /**
     * Test of getReference method, of class it.cnr.icar.eric.client.xml.registry.ObjectCache.
     */
    @SuppressWarnings("static-access")
	public void testGetReference() throws Exception {
        System.out.println("/ntestGetReference");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a new RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        String id = testObject.getKey().getId();
        String type = "RegistryPackage";
        assertNotNull(cache.getReference(id, type));

        // Use Contains Association Concept as test material
        id = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Contains;
        type = "ClassificationNode";
        assertTrue("Test object already cached", !cache.isCached(id));
        assertNotNull(cache.getReference(id, type));
        assertTrue(cache.isCached(id));

        // Try non-exixstent object
        try {
            cache.getReference("non-existing-id", type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }

    /**
     * Test of getRegistryObject method, of class it.cnr.icar.eric.client.xml.registry.ObjectCache.
     */
    @SuppressWarnings("static-access")
	public void testGetRegistryObject() throws Exception {
        System.out.println("/ntestGetRegistryObject");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // use a new RegistryPackage as test material
        RegistryObject testObject = lcm.createRegistryPackage("test-pack");
        cache.putRegistryObject(testObject);
        String id = testObject.getKey().getId();
        String type = "RegistryPackage";
        RegistryObject cachedObject = cache.getRegistryObject(id, type);
        assertNotNull(cachedObject);
        assertEquals(testObject, cachedObject);

        // Use Extends Association Concept as test material
        id = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Extends;
        type = "ClassificationNode";
        assertTrue("Test object already cached", !cache.isCached(id));
        cachedObject = cache.getRegistryObject(id, type);
        assertNotNull(cachedObject);
        assertEquals(id, cachedObject.getKey().getId());

        // Try non-exixstent object
        try {
            cachedObject = cache.getRegistryObject("non-existing-id", type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }

    /**
     * Test of getRegistryObjects method, of class it.cnr.icar.eric.client.xml.registry.ObjectCache.
     */
    @SuppressWarnings("static-access")
	public void testGetRegistryObjects() throws Exception {
        System.out.println("/ntestGetRegistryObjects");

        // Get object cache
        ObjectCache cache = ((RegistryServiceImpl)service).getObjectCache();

        // Use Contains, Extends and Uses -Association Concepts as test material
        String id1 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Contains;
        String id2 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Extends;
        String id3 = BindingUtility.getInstance().CANONICAL_ASSOCIATION_TYPE_ID_Uses;
        Collection<String> ids = new ArrayList<String>();
        ids.add(id1);
        ids.add(id2);
        ids.add(id3);
        String type = "ClassificationNode";

        @SuppressWarnings({ "unchecked", "rawtypes" })
		Collection<?> cachedObjects = cache.getRegistryObjects(new ArrayList(ids), type);
        assertNotNull(cachedObjects);
        assertEquals(ids.size(), cachedObjects.size());
        for(Iterator<?> iter = cachedObjects.iterator(); iter.hasNext(); ) {
            RegistryObject object = (RegistryObject)iter.next();
            ids.remove(object.getKey().getId());
        }
        assertTrue("Returned RegistryObjects did not match queried objects.", ids.isEmpty());

        // Try non-exixstent object
        try {
            ids = new ArrayList<String>();
            ids.add(id1);
            ids.add("non-existing-id");
            cachedObjects = cache.getRegistryObjects(ids, type);
            fail("JAXRException expected to be thrown if object not found.");
        } catch (JAXRException e) {
        }
    }
}
