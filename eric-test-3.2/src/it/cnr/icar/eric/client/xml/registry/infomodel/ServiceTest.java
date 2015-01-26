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
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * JUnit Test for ServiceImpl.
 *
 * Warning: This test MUST be run in sequence as test methods have dependencies.
 *
 * @author Diego Ballve / Republica Corp.
 */
public class ServiceTest extends ClientTest {
    
    static String serviceId = "urn:freebxml:registry:test:client:ServiceTest:service1";
    static String org1Id = "urn:freebxml:registry:test:client:ServiceTest:org1";
    static String org2Id = "urn:freebxml:registry:test:client:ServiceTest:org2";
    
    public ServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ServiceTest.class);
        return suite;
    }
    
    /** Test submit of a Service */
    public void testSubmit() throws Exception {
        Service service = getLCM().createService("ServiceTest_Service1");
        service.setKey(getLCM().createKey(serviceId));
        System.err.println("Adding service with id: " + serviceId);
        
        //Set to expire on my 100th birthday. Test expiration truncation bug reported by Rajesh.
        service.setExpiration(new java.util.Date((new java.util.GregorianCalendar(2061, 0, 10)).getTimeInMillis()));    

        service.setStability(Service.STABILITY_STATIC);
        
        ArrayList<Service> objects = new ArrayList<Service>();
        objects.add(service);
        
        BulkResponse resp = getLCM().saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);
    }
    
    /** Test query of a Service */
    //public void testQuery() throws Exception {        
    //}

    public void testSetProvidingOrganization() throws Exception {
        Organization org1 = createOrganization("ServiceTest_Org1");
        org1.setKey(getLCM().createKey(org1Id));
        System.err.println("Adding org with id: " + org1Id);
        
        Organization org2 = createOrganization("ServiceTest_Org2");
        org2.setKey(getLCM().createKey(org2Id));
        System.err.println("Adding org with id: " + org2Id);
        
        Collection<RegistryObject> objects = new ArrayList<RegistryObject>();
        objects.add(org1);
        objects.add(org2);
        BulkResponse resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);
        
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        
        // Set the providing organization to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);

        // Set the providing organization to 'org2'.
        service.setProvidingOrganization(org2);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org2Id);

        // Set providing organization to null.
        service.setProvidingOrganization(null);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, null);

        // Set the providing organization back to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);
    }
    
    /** Test delete of a Service */
    public void testDelete() throws Exception {
        //Delete the service that was created in testSubmit
        ArrayList<Key> keys = new ArrayList<Key>();
        keys.add(getLCM().createKey(serviceId));
        BulkResponse resp = getLCM().deleteObjects(keys, LifeCycleManager.SERVICE);
        JAXRUtility.checkBulkResponse(resp);
        
        // delete associations of service.
        resp = getBQM().findAssociations(null, null, serviceId, null);
        JAXRUtility.checkBulkResponse(resp);
        resp = lcm.deleteObjects(JAXRUtility.getKeysFromObjects(resp.getCollection()));
        JAXRUtility.checkBulkResponse(resp);
        
        //Delete the organization that was created in testSetProvidingOrganization().
        keys = new ArrayList<Key>();
        keys.add(getLCM().createKey(org1Id));
        keys.add(getLCM().createKey(org2Id));
        resp = getLCM().deleteObjects(keys, LifeCycleManager.ORGANIZATION);
        JAXRUtility.checkBulkResponse(resp);
    }

    protected void assertOrgIsProvidingOrg(String serviceId, String orgId) throws Exception {
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        
        if (orgId == null) {
            assertNull("Service should have null providing Organization.", service.getProvidingOrganization());
        } else {
            try {
                Organization org = service.getProvidingOrganization();
                assertNotNull("Service's providing Organization should not be null", org);
                assertEquals("Service's providing Organization id should match.", orgId, org.getKey().getId());
            } catch (ClassCastException e) {
                fail("Service's providing Organization should be an Organization.");
            }
	}
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
