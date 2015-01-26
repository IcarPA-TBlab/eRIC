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

package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.common.ClientTest;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.Slot;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * JUnit Test for RegistryPackage
 *
 * @author Nikita Sawant, Sun Microsystems
 */
public class RegistryPackageTest extends ClientTest {
    @SuppressWarnings("unused")
	private static Log log =
	LogFactory.getLog(RegistryPackageTest.class.getName());
    
    String pkgId = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
    
    //The id for the canonical XML Cataloging Service guaranteed to be in an ebXML Registry
    String serviceId = "urn:oasis:names:tc:ebxml-regrep:Service:CanonicalXMLCatalogingService";
    
    public RegistryPackageTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RegistryPackageTest.class);
        return suite;
    }
    
    /** Test Creation of RegistryPackage*/
    public void testSubmit() throws Exception {
        // -- Create a Registry Package
        RegistryPackage pkg = getLCM().createRegistryPackage("RegistryPackage_SomeName");
        pkg.setKey(getLCM().createKey(pkgId));                
        
        // -- Add the Slots ( a.k.a Attributes to it )
        ArrayList<Slot> slots = new ArrayList<Slot>();
        slots.add( getLCM().createSlot("TargetNamespace", "TargetNamepsace", null) );
        pkg.addSlots(slots);
        
        // -- Save the Object
        ArrayList<RegistryPackage> pkgs = new ArrayList<RegistryPackage>();
        pkgs.add(pkg);
        BulkResponse resp = getLCM().saveObjects(pkgs);
        System.out.println("Created Registry Package with Id " + pkgId);
        JAXRUtility.checkBulkResponse(resp);
    }
    
    
    /** Test addition of Member Services to RegistryPackage */
    public void testAddRegistryObjects() throws Exception {
        
        // -- Get the Registry Package
        RegistryPackage pkg =
	    (RegistryPackage) getBQM().getRegistryObject(pkgId, LifeCycleManager.REGISTRY_PACKAGE);
        Assert.assertNotNull("Could not retrieve test package submitted in testSubmit", pkg);
        
        // -- Get the Service
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        Assert.assertNotNull("Could not retrieve canonical XML Cataloging Service", service);
        
        // -- Add service to Registry Package and save
        ArrayList<Service> members = new ArrayList<Service>();
        members.add(service);
        
        pkg.addRegistryObjects(members);
        
        // -- Save the Object
        ArrayList<RegistryPackage> objectsToSave = new ArrayList<RegistryPackage>();
        objectsToSave.add(pkg);
        
        BulkResponse resp = getLCM().saveObjects(objectsToSave);
        
        JAXRUtility.checkBulkResponse(resp);
        
    }
    
    
    /** Test query of a RegistryPackage and member
     *  Services  **/
    @SuppressWarnings("rawtypes")
	public void testQuery() throws Exception {
        // -- Get the Registry Package
        RegistryPackage
        pkg = (RegistryPackage) getBQM().getRegistryObject(pkgId);
        Assert.assertNotNull("RegistryPackage was not found when queried by id.", pkg);
        
        // -- Get the Member Services
        java.util.Set members = pkg.getRegistryObjects();
        Assert.assertNotNull("RegistryPackage must not have null for members.", members);
        
        Assert.assertEquals("The RegistryPackage does not have expected number of members.", 1, members.size());                
        
        java.util.Iterator itr = members.iterator();
        RegistryObject service = (RegistryObject) itr.next();
        
        Assert.assertEquals("The member does not have the expected id.", serviceId, service.getKey().getId());                
        
    }
        
    /**
     * Test that removeRegistryObject really does remove the
     * association between this RegistryPackage and the member
     * RegistryObject.
     *
     * @exception Exception if an error occurs
     */
    @SuppressWarnings("static-access")
	public void testRemoveRegistryObject() throws Exception {
	HashMap<String, String> saveObjectsSlots = new HashMap<String, String>();

	//The bulk loader MUST turn off versioning because it updates
	//objects in its operations which would incorrectly be created as
	//new objects if versioning is ON when the object is updated.
	saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
	saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");        

	String testName = "testRemoveRegistryObject";

	String uuid1 =
	    it.cnr.icar.eric.common.Utility.getInstance().createId();

        RegistryPackage pkg1 = getLCM().createRegistryPackage(uuid1);
        pkg1.setKey(getLCM().createKey(uuid1));
	pkg1.setDescription(getLCM().createInternationalString(testName));

        // -- Save the Object
        ArrayList<Object> objects = new ArrayList<Object>();
        objects.add(pkg1);
        BulkResponse resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Created Registry Package with Id " + uuid1);

	String uuid2 =
	    it.cnr.icar.eric.common.Utility.getInstance().createId();

        RegistryPackage pkg2 = getLCM().createRegistryPackage(uuid2);
        pkg2.setKey(getLCM().createKey(uuid2));                
	pkg2.setDescription(getLCM().createInternationalString(testName));

        // -- Add pkg2 to Registry Package and save
        pkg1.addRegistryObject(pkg2);
        
        // -- Save the Object
	objects = new ArrayList<Object>();
        objects.add(pkg1);
        objects.add(pkg2);
        
	resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Added Registry Package with Id " + uuid2);
	
	// Remove the package.
	pkg1.removeRegistryObject(pkg2);
        // -- Save the Object
        objects = new ArrayList<Object>();
        objects.add(pkg1);
        
	resp = ((LifeCycleManagerImpl) getLCM()).saveObjects(objects, saveObjectsSlots);
        JAXRUtility.checkBulkResponse(resp);
        System.out.println("Removed Registry Package with Id " + uuid2);

	// Get 'HasMember' associations of pkg1.
	ArrayList<String> associationTypes = new ArrayList<String>();
	associationTypes.add(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);

	resp = getBQM().findAssociations(null, uuid1, null, associationTypes);
	JAXRUtility.checkBulkResponse(resp);

	Collection<?> coll = resp.getCollection();

	if (coll.size() != 0) {
	    Iterator<?> iter = coll.iterator();

	    while (iter.hasNext()) {
		Association ass = (Association) iter.next();

		System.err.println("Association: " + ass.getKey().getId() +
				   "; sourceObject: " + ass.getSourceObject().getKey().getId() +
				   "; targetObject: " + ass.getTargetObject().getKey().getId());
	    }
	}

	assertEquals("uuid1 should not be the sourceObject in any HasMember associations.",
		     0, coll.size());

	objects = new ArrayList<Object>();
        objects.add(pkg1.getKey());
        objects.add(pkg2.getKey());
	if (coll.size() != 0) {
	    Iterator<?> itr = coll.iterator();
	    while (itr.hasNext()) {
		RegistryObject ro = (RegistryObject)itr.next();
	        objects.add(ro.getKey());
	    }
	}
	resp = getLCM().deleteObjects(objects);
        JAXRUtility.checkBulkResponse(resp);
    }

    public void testDelete() throws Exception {        
        //Delete the service that was created in testSubmit
        deleteIfExist(pkgId, LifeCycleManager.REGISTRY_PACKAGE);        
    }
    
    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
