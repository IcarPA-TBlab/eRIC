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

import it.cnr.icar.eric.client.xml.registry.infomodel.IdentifiableImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef;

import java.util.ArrayList;
//import java.util.Set;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.Key;
//import javax.xml.registry.BusinessQueryManager;
//import javax.xml.registry.Connection;
//import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Service;


import junit.framework.Test;

//import it.cnr.icar.eric.client.common.ClientTest;
//import it.cnr.icar.eric.client.xml.registry.infomodel.FederationImpl;
//import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryImpl;

//import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;


/**
 * Client side System tests for testing multi-registry federation features.
 * Requires that there be 2 instance of registry deployed under eric.name of "eric" and "eric1"
 *
 * @author Farrukh Najmi
 */
public class ReplicationSystemTest extends MultiRegistrySystemTest {
            
    /** Creates a new instance of FederationTest */
    public ReplicationSystemTest(String name) {
        super(name);        
    }
    
    public static Test suite() {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new ReplicationSystemTest("testGetRegistries"));
        suite.addTest(new ReplicationSystemTest("testCreateReplicas"));
        suite.addTest(new ReplicationSystemTest("testQueryReplicas"));
        //TODO: uncomment once checks are done in server to prevent LCM operations on remote objects
        //suite.addTest(new ReplicationSystemTest("testLCMOnReplicas"));
        suite.addTest(new ReplicationSystemTest("testDeleteReplicas"));
                        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
            
    /**
     * Tests creating local replicas of remote objects.
     */
    public void testCreateReplicas() throws Exception {
        //Create service2 and save to registry2
        service2 = lcm2.createService("ReplicationSystemTest Service2");
        service2 = (Service)saveAndGetIdentifiable((IdentifiableImpl)service2);
        
        //Now submit remote ObjectRef to service2 to registry1
        //This should create a replica in registry1 for service2
        RegistryObjectRef service2Ref = new RegistryObjectRef(lcm1);
        service2Ref.setKey(service2.getKey());
        service2Ref.setHome(regSoapUrl2.replaceFirst("/soap",""));
        
        ArrayList<ExtensibleObject> objects = new ArrayList<ExtensibleObject>();
        objects.add(service2Ref);
        BulkResponse br = lcm1.saveObjects(objects);
        assertTrue("Save of remote ObjectRef failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
    }
        
    /**
     * Tests the queries that match expected local replicas.
     */
    public void testQueryReplicas() throws Exception {
        service2Replica = (Service)dqm1.getRegistryObject(service2.getKey().getId());
        assertNotNull("Local replica of service2 not found on registry1", service2Replica);        
    }
    
    /**
     * Tests that LCM operations on local replicas are not allowed.
     */
    public void testLCMOnReplicas() throws Exception {
        //Try updating service2replica and expect an error
        service2Replica.setName(lcm1.createInternationalString("Service1Replica updated"));
        ArrayList<ExtensibleObject> updateObjects = new ArrayList<ExtensibleObject>();
        updateObjects.add(service2Replica);
        BulkResponse br = lcm1.saveObjects(updateObjects);
        assertFalse("service2 replica update succeeded when it should not be allowed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
    }
        
    /**
     * Tests deletion of local replicas
     */
    public void testDeleteReplicas() throws Exception {
        ArrayList<Key> deleteObjects = new ArrayList<Key>();
        deleteObjects.add(service2Replica.getKey());
        BulkResponse br = lcm1.deleteObjects(deleteObjects);
        assertTrue("service2 replica deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);        
    }
    
}
