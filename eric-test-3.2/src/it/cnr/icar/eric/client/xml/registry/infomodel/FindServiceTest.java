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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


import javax.xml.registry.infomodel.Key;


/**
 * Duplicate a TCK test to reproduce and verify bug fix for Sun bug 6392404
 *
 * @author dianne.jiao@sun.com
 */
public class FindServiceTest extends ClientTest {
    
    static String serviceId = "urn:uuid:2d97634e-c8d2-4fef-b57a-d3987dce16bd";
    static String org1Id = "urn:uuid:b89412f8-dc03-46f8-9cc6-c0c938f44349";
    static String org2Id = "urn:uuid:799595b5-dc26-4b48-b437-c9b34e0b3699";
        
    public FindServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(FindServiceTest.class);
        return suite;
    }
    
    /** 
     *
     * Create and save an organization.
     * Create and save a service.
     * Request only service be returned. 
     * Verify that only the service is returned.
     */

    @SuppressWarnings("static-access")
	public void testgetRegistryObjectsSpecifiedType() throws Exception {        
        
	Key orgkey = null;
	String myServiceName = "TCK_TEST_SERVICE";
	Collection<?> orgKeys = null;
        boolean pass = false;
        
	try {
	    System.out.println("Create a service and an organization \n");
	    Service service = lcm.createService(myServiceName);
	    ArrayList<Service> myServices = new ArrayList<Service>();

	    // create an organization
	    Organization org = (Organization)lcm.createObject(lcm.ORGANIZATION);

	    org.setName(lcm.createInternationalString("CTS_Test_ORG"));
	    // publish the organization
	    ArrayList<Organization> orgs = new ArrayList<Organization>();
	    orgs.add(org);
	    System.out.println("Save the organization and get the key id from getCollection\n");
	    BulkResponse br = lcm.saveOrganizations(orgs); // publish to registry

	    orgKeys = br.getCollection();
	    Iterator<?> iter = orgKeys.iterator();
            while ( iter.hasNext() ) {
		orgkey = (Key) iter.next();
            }
            String orgKeyId = orgkey.getId();
            System.out.println("Saved Organization key id is: " + orgKeyId + "\n");
            System.out.println("Call getRegistryObjects to get all owned objects\n");
            br = bqm.getRegistryObjects();
            System.out.println("Find the saved organization and add the service to it \n");
           // get the org back
           Collection<?> ros = br.getCollection();
           Organization o = null;
           iter = ros.iterator();
           @SuppressWarnings("unused")
		String regKeyId = null;
           while ( iter.hasNext() ) {
               Object obj = iter.next();
               if (obj instanceof Organization) {
                  o = (Organization)obj;
                  if( o.getKey().getId().equals(orgKeyId) ) {
                    System.out.println("Found the organization\n");
                    regKeyId = o.getKey().getId();
                    o.addService(service);
                    myServices.add(service);
                    break;
	    	  }
               }
            }
	    if ( o == null ) {
            	System.out.println("Error: failed to get the Organization with getRegistryObjects \n");
            }
//==
	    System.out.println("save the service to the registry \n");
	    br = lcm.saveServices(myServices);
	    Key servicekey = null;
	    Collection<?> serviceKeys = br.getCollection();
            System.out.println("The number of service keys returned from getCollection is: " + serviceKeys.size() + "\n");
	    iter = serviceKeys.iterator();
	    while ( iter.hasNext() ) {
	    	servicekey = (Key) iter.next();
            }
	    System.out.println("Save the service key returned from saveServices\n");
	    String serviceKeyId = servicekey.getId();
	    System.out.println("Saved Service key id is: " + serviceKeyId +  "\n");
System.out.println("request service objects with getRegistryObjects(LifeCycleManager.SERVICE) \n");
	     br = bqm.getRegistryObjects(LifeCycleManager.SERVICE);
	     // br = bqm.getRegistryObjects();

	    Collection<?> myObjects = br.getCollection();
            System.out.println("Count of objects returned from service request is: " + myObjects.size() + "\n");
	    if ( myObjects.size() == 0 )
            	 System.out.println(" failed - nothing returned from getRegistryObjects");

	    iter = myObjects.iterator();
	    RegistryObject ro = null;

	    while ( iter.hasNext() ) {
	      ro = (RegistryObject)iter.next();
	      if ( ro instanceof Service) {
	    	  System.out.println(" ro is a Service \n");
	    	  if ( ro.getKey().getId().equals(serviceKeyId) ) {
	              System.out.println("Got back my service - Good! \n");
	              pass = true;
	    	  }
	      } else if ( !( ro instanceof Service )) {
	    	  System.out.println(" returned ro not a service! " + ro.toString() + "\n");
              }
            }

	  } catch  (Exception e) {
	    System.out.println("Caught exception: " + e.getMessage());
	    e.printStackTrace();
	  }finally {
	      System.out.println("cleanup at test end \n");
	      //super.cleanUpRegistry(orgKeys, LifeCycleManager.ORGANIZATION);
          }

	assert(pass);

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
