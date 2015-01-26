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
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.TelephoneNumber;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for Organization
 *
 * @author Zahra Zahid
 */
public class OrganizationTest extends ClientTest {
    
    public OrganizationTest(String testName) {
        super(testName);
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
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(OrganizationTest.class);
        return suite;
    }
    
    @SuppressWarnings("unused")
	public void testAddTelephoneNumbers() throws Exception {
        Organization org = null;
        TelephoneNumber tn1, tn2, tn3 = null;
        BulkResponse br= null;
        List<Object> orgsList = new ArrayList<Object>();
        
        org = createOrganization("TestOrgWithTN");

        tn1 = lcm.createTelephoneNumber();
        tn1.setType("Office Phone");
        
        tn2 = lcm.createTelephoneNumber();
        tn2.setType("Mobile Phone");

        tn3 = lcm.createTelephoneNumber();
        tn3.setType("Fax");
        
        List<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tn1);
        tels.add(tn2);
        tels.add(tn3);
        
        org.setTelephoneNumbers(tels);
        orgsList.add(org);
        lcm.saveOrganizations(orgsList);

        Organization retrieveOrg = (Organization)dqm.getRegistryObject(org.getKey().getId());
        assertNotNull("Organization was not saved", retrieveOrg);
        
        Collection<?> retList = retrieveOrg.getTelephoneNumbers("Fax");
        assertEquals("Count of Telephone Numbers returned from Organization should be 1.", 1, retList.size());
    }
    
    
   /*
    *   Duplicates a bug found in ExternalIdentifier by JAXR TCK.
    *
    *
    *   @test_Strategy: Create an organization with ExternalLinks and ExternalIdentifier.
    *                   Publish the organization.  Retrieve it and verify
    *                   ExternalLinks and ExternalIdentifier.
    *
    */
    @SuppressWarnings({ "unused", "unchecked" })
	public void test_registryObject_Test() throws Exception {
        // External Links
        
        String name = "United States";
        String eivalue = "US";
        
        ExternalLink el = null;
        Collection<Object> orgKeys = null;
        BulkResponse br = null;
        Collection<Object> keys = null;
        Collection<Object> services = null;
        Organization org = null;
        try {
            ArrayList<Object> orgs = new ArrayList<Object>();
            Collection<String> externalURIs = new ArrayList<String>();
            Collection<Object> els = new ArrayList<Object>();
            
            //Create ExternalLinks
            String [] externalURI ={
                "http://www.sun.com",
                "http://ebxmlrr.sourceforge.net"
            } ;
            String [] description = {"Sun page", "freeebXML Registry page" };
            for ( int i = 0; i < externalURI.length; i++) {
                els.add(lcm.createExternalLink(externalURI[i], description[i]));
                externalURIs.add(externalURI[i]);
            }
            
            org = lcm.createOrganization(lcm.createInternationalString("TestOrg"));
            org.addExternalLinks(els);
            els = null;
            
            // = ExternalIdentifier instances provide identification information to a RegistryObject
            //--
            String schemeName = "Geography";
            String schemeDescription = "North American Regions";
            
            ClassificationScheme cs = lcm.createClassificationScheme(schemeName,schemeDescription);
            Collection<ClassificationScheme> schemes = new ArrayList<ClassificationScheme>();
            schemes.add(cs);
            br = lcm.saveClassificationSchemes(schemes);
            assertResponseSuccess("Error during saveClassificationSchemes", br);
            keys = new ArrayList<Object>();
            keys = br.getCollection(); // get a collection of keys
            Key ClassificationschemeKey = null;
            Iterator<Object> iterate  = keys.iterator();
            while (iterate.hasNext()){
                ClassificationschemeKey = (Key)iterate.next();
            }
            String keyId = ClassificationschemeKey.getId();
            ClassificationScheme scheme = (ClassificationScheme)bqm.getRegistryObject(keyId, LifeCycleManager.CLASSIFICATION_SCHEME);
            // --
            InternationalString iName = lcm.createInternationalString("United States");
            ExternalIdentifier ei = lcm.createExternalIdentifier(scheme,iName,eivalue);
            org.addExternalIdentifier(ei);
            ei = null;
            
            // = Service instances are RegistryObjects that provide information on services
            //                   (e.g. web services) offered by an Organization.
            
            // create a specification link.
            SpecificationLink specificationlink = lcm.createSpecificationLink();
            
            specificationlink.setUsageDescription(lcm.createInternationalString("detail of specification link usage"));
            Collection<String> slUsageParameters = new ArrayList<String>();
            slUsageParameters.add("usage=test");
            specificationlink.setUsageParameters(slUsageParameters);
            
            /* Following code in JAXR TCK is wrong. It creates a new object with same id as scheme.
             * We have fixed server to detect duplicate Names on an object within same parent and lang.
             * TODO: Fix server to detect objects with same id as existing object but different objectType.
             *
            Concept specificationConcept = (Concept)
            lcm.createObject(lcm.CONCEPT);
            specificationConcept.setKey(scheme.getKey());
            specificationConcept.setName(lcm.createInternationalString("Conceptname"));
            specificationConcept.setValue("Concept value");
             */
            specificationlink.setSpecificationObject(cs);
            
            // create a service binding.
            ServiceBinding serviceBinding = lcm.createServiceBinding();
            // set accessURI
            serviceBinding.setValidateURI(false);
            serviceBinding.setAccessURI(externalURI[0]);
            // add specificationlink to service binding
            serviceBinding.addSpecificationLink(specificationlink);
            // create a service
            Service service = lcm.createService("TestService");
            // attach bindings to the service
            service.addServiceBinding(serviceBinding);
            // add the service to the organization
            org.addService(service);
            
            orgs.add(org);
            org = null;
            // Publish the organization to the registry. Check for errors.
            br = lcm.saveOrganizations(orgs);
            assertResponseSuccess("Error during saveOrganization", br);
            
            // save the keys
            orgKeys = br.getCollection();
            
            
            // Retrieve the newly published organization from the registry
            br = bqm.getRegistryObjects(orgKeys,LifeCycleManager.ORGANIZATION);
            assertResponseSuccess("Error during getRegistryObjects.", br);
            Collection<Object> retOrgs = br.getCollection();
            // Verify that we got back 1 organization
            assertEquals("Unexpected org size", 1, retOrgs.size());
            
            // get our organization so we can verify it.
            Iterator<Object> iter = retOrgs.iterator();
            while(iter.hasNext() ) {
                org = (Organization) iter.next();
            }
            // successfully retrieved newly published organization from the registry - now verify it.
            
            
            // Verify Service
            service = null;
            services = org.getServices();
            iter = services.iterator();
            while(iter.hasNext() ) {
                service = (Service) iter.next();
                Collection<?> serviceBindings = service.getServiceBindings();
                serviceBinding = null;
                Iterator<?> itr = serviceBindings.iterator();
                while(itr.hasNext() ) {
                    serviceBinding = (ServiceBinding) itr.next();
                    Collection<?> specificationlinks = serviceBinding.getSpecificationLinks();
                    Iterator<?> it = specificationlinks.iterator();
                    while ( it.hasNext()) {
                        specificationlink = (SpecificationLink)it.next();
                        
                    } // end of while specificationLinks
                    
                } // end of while servicebinding.
                
            }// end of while service
            
            // Verify ExternalIdentifier.
            // get Organizations External Identifier
            Collection<Object> eis = org.getExternalIdentifiers();
            iter = eis.iterator();
            while(iter.hasNext() ) {
                ei = (ExternalIdentifier) iter.next();
                assertTrue("String returned from ExternalIdentifier.getValue did not match value.", (ei.getValue().equals(eivalue)) );
            }
            
            // Verify ExternalLinks.
            els = org.getExternalLinks();
            iter = els.iterator();
            int count = 0;
            while(iter.hasNext() ) {
                el = (ExternalLink) iter.next();
                if ( externalURIs.contains(el.getExternalURI() ) ) {
                    count = count + 1;
                } else {
                    int i=0;
                }
            }
            assertTrue("Retrieved unexpected ExternalLink from organization.", ( count == externalURI.length));
        } finally {
            // cleanup...
            try {
                org.removeServices(services);
                br = lcm.deleteObjects(keys, null, forceRemoveRequestSlotsMap, (String)null);
                br = lcm.deleteObjects(orgKeys, null, forceRemoveRequestSlotsMap, (String)null );
            }catch (Exception ee) {
            }
        }
        
    } // end of method.
   
    
    
    public void testAddUsers() throws Exception {
        Organization org = createOrganization("testAddUsersOrg");
        ArrayList<User> users = new ArrayList<User>();
        User testUser1 = createUser("testUser1");
        users.add(testUser1);
        User testUser2 = createUser("testUser2");
        users.add(testUser2);
        User testUser3 = createUser("testUser3");
        users.add(testUser3);
        
        org.addUsers(users);
        
        ArrayList<Object> saveObjects = new ArrayList<Object>();
        saveObjects.add(org);
        BulkResponse br = lcm.saveObjects(saveObjects);
        assertResponseSuccess("Error during save of org", br);
        
        //Now read back both orgs to verify that they were saved
        Organization sorg = (Organization)dqm.getRegistryObject(org.getKey().getId());
        assertNotNull("Org was not saved", sorg);
        
        Collection<?> sUsers = sorg.getUsers();
        //Note that sUsers has an extra user because the submitter automatically gets added as primaryContact.
        assertEquals("Retrieved org.getUsers() does not contain all users added with org.addUsers().", users.size(), sUsers.size());
    }
    
    public void testParentOrganization() throws Exception {
        
        Organization childOrg = createOrganization("childOrg");
        Organization parentOrg = createOrganization("parnetOrg");
        parentOrg.addChildOrganization(childOrg);
        
        System.err.println("Saving parentOrg with id: " + parentOrg.getKey().getId());
        System.err.println("Saving childOrg with id: " + childOrg.getKey().getId());
        
        //Now save both orgs
        ArrayList<Object> orgs = new ArrayList<Object>();
        orgs.add(parentOrg);
        @SuppressWarnings("unused")
		BulkResponse response = lcm.saveObjects(orgs);
        
        //Now read back both orgs to verify that they were saved
        Organization schildOrg=(Organization)dqm.getRegistryObject(childOrg.getKey().getId());
        assertNotNull("Child was not saved", schildOrg);
        
        Organization sparentOrg=(Organization)dqm.getRegistryObject(parentOrg.getKey().getId());
        assertNotNull("Parent was not saved", sparentOrg);
        
        sparentOrg = childOrg.getParentOrganization();
        assertNotNull("Parent ref on child is null",sparentOrg);
        
        if (!sparentOrg.getKey().getId().equals(parentOrg.getKey().getId())) {
            assertEquals("Unexpected parent ref", parentOrg.getKey().getId(), sparentOrg.getKey().getId());
        }
        
        //Now test deleteOrganization
        orgs.clear();
        orgs.add(parentOrg.getKey());
        orgs.add(childOrg.getKey());
        lcm.deleteObjects(orgs, null, forceRemoveRequestSlotsMap, (String)null);
        
        //Now read back both orgs to verify that they were deleted
        schildOrg=(Organization)dqm.getRegistryObject(childOrg.getKey().getId());
        assertNull("Child was not deleted", schildOrg);
        
        sparentOrg=(Organization)dqm.getRegistryObject(parentOrg.getKey().getId());
        assertNull("Parent was not deleted", sparentOrg);
        
    }
    

    /*   @test_Strategy: Create and save a collection of services 
     *                   Verify the services have been added.  Then do a cleanup 
     */
    @SuppressWarnings({ "unused", "static-access", "unchecked" })
	public void testAddService() throws Exception {
        String testName = "BusinessLifeCycleManager_SaveServicesTest";
        boolean pass = false;
        Organization org = null;
        Collection<?> keys = null;
        Key key = null;
        Key savedOrgKey = null; // save organization key for findServices call
        int numOrgs = 3;
        Collection<Object> orgs = null;
        BulkResponse br = null;
        Iterator<Object> iter = null;
        Collection<Object> services = null;
        String serviceName = "Name:Test Service";
        Collection<Object> orgKeys = null;
        try {
            org = lcm.createOrganization(lcm.createInternationalString("TestOrg"));
           //  Collection services = org.getServices();

            orgs = new ArrayList<Object>();
            orgs.add(org); 
            System.err.println("Saving this organization:: name: " + org.getName().getValue());
            br = lcm.saveOrganizations(orgs);
            assertResponseSuccess("Error during saveOrganizations", br);
            // BulkResponse containing the Collection of keys for those objects that were saved successfully
            orgKeys = br.getCollection();
            assertEquals("OrgKey count returned from saveOrganization should be 1.", 1, orgKeys.size());
            // get the key for this organization - should be just one
            iter = orgKeys.iterator();
            while (iter.hasNext()) {
               savedOrgKey = (Key)iter.next();
	       System.err.println("saved org key is: " + savedOrgKey.getId());
            }

            // BulkResponse containing a hetrogeneous Collection of RegistryObjects - in this case orgs  
            br = bqm.getRegistryObjects(orgKeys, LifeCycleManager.ORGANIZATION);
            assertResponseSuccess("Error during saveOrganizations", br);
            Collection<Object> myOrgs = br.getCollection();
            services = new ArrayList<Object>();
            Service theService =
                        (Service)lcm.createObject(lcm.SERVICE);
            // iterate thru the collection of orgs returned from getRegistryObjects - we only have 1
            iter = myOrgs.iterator();
            Organization myOrg = null;
            theService.setName(lcm.createInternationalString(serviceName));
            theService.setDescription(lcm.createInternationalString("Description: Testservice"));
            while (iter.hasNext()) {
               myOrg = (Organization)iter.next();
               myOrg.addService(theService);
               services.add(theService);
            }

/////======
            // save the new service back to the registry
            br = lcm.saveServices(services);
            assertResponseSuccess("Error during saveServices", br);

            // This should not return any services since Organization not saved.
            br = bqm.findServices(savedOrgKey, null, null,  null, null); 
            assertResponseSuccess("Error during findServices", br);

            // get the collection of services returned from findServices - should be zero
            Service s = null;
            Collection<Object> ss = br.getCollection();
            assertEquals("Count returned from findServices should be zero", 0, ss.size());

            ArrayList<String> names = new ArrayList<String>();
            names.add(serviceName);

            // This should one service since it does not use orgKey.
            br = bqm.findServices(null, null, names,  null, null); 
            assertResponseSuccess("Error during findServices", br);

            // get the collection of services returned from findServices - should be one
            s = null;
            ss = br.getCollection();
            assertTrue("Count returned from findServices should be at least one", ss.size() >= 1);

            // save the organization back to the registry
            br = lcm.saveOrganizations(myOrgs);
            assertResponseSuccess("Error during saveOrganizations", br);

            // --             
            // Now we must verify that the organization was saved successfully....
            // --

            // This should return one service since Organization is saved.
            br = bqm.findServices(savedOrgKey, null, null,  null, null); 
            assertResponseSuccess("Error during findServices", br);

            // get the collection of services returned from findServices - should be zero
            s = null;
            ss = br.getCollection();
            assertEquals("Count returned from findServices should be one", 1, ss.size());
            
            // This should return one service as well.
            br = bqm.findServices(savedOrgKey, null, names,  null, null); 
            assertResponseSuccess("Error during findServices", br);

            // get the collection of services returned from findServices - should be one
            s = null;
            ss = br.getCollection();
            assertEquals("Count returned from findServices should be one", 1, ss.size());

            iter = ss.iterator();
            while(iter.hasNext()) {
                s =   (Service)iter.next();
                System.err.println("And the service returned is: " + s.getName().getValue() + "\n");
                
            } 

            assertEquals("Service name should match.", serviceName, s.getName().getValue());

            } finally {
            // cleanup...
		try {
		    org.removeServices(services);
                    br = bqm.findAssociations(null, savedOrgKey.getId(), null, null);
                    br = lcm.deleteObjects(JAXRUtility.getKeysFromObjects(br.getCollection()), null, forceRemoveRequestSlotsMap, (String)null);
		    br = lcm.deleteObjects(JAXRUtility.getKeysFromObjects(services), null, forceRemoveRequestSlotsMap, (String)null);
		    br = lcm.deleteObjects(orgKeys, null, forceRemoveRequestSlotsMap, (String)null);
		}catch (Exception ee) {
                    ee.printStackTrace();
		}
            }
    } // end of test method
         
    /*   @test_Strategy: "Tests creating an Organization, adding a Service 
     *                    to it and then removing 
     *                    the Service prior to doing any save"
     */
    public void testRemoveService() throws Exception {
        Organization org;
        Collection<Object> services = null;
        String serviceName = "Name:Test Service1";
        String serviceName1 = "Name:Test Service2";
        try
        {
            // Create an organization.
            String orgName = "burlingtonCampus";
            InternationalString iorgName = lcm.createInternationalString(orgName);
            org = lcm.createOrganization(iorgName);
            // Create two Service instances
            Service myService = lcm.createService(serviceName);
            Service anotherService = lcm.createService(serviceName1);
            Collection<String> myServiceNames = new ArrayList<String>();
            myServiceNames.add(serviceName);
            myServiceNames.add(serviceName1);

            services = new ArrayList<Object>();
            services.add(myService);
            services.add(anotherService);

            //Adding collection of Service objects to organization
            org.addServices(services);
             
            //Removing Service object from organization
            org.removeServices(services);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    
    /*   @test_Strategy: Tests creating an organization object ,adding and removing 
     *			 of user objects,which are associated with organization object.
     */
    public void testRemoveUsers() throws Exception {

       InternationalString orgName = lcm.createInternationalString("TestRemoveUser");
       Organization org = lcm.createOrganization(orgName);
       org.setName(orgName);
       User user1 = lcm.createUser();
       PersonName personName1 = lcm.createPersonName("Test1","M","User1");
       user1.setPersonName(personName1);
       User user2 = lcm.createUser();
       PersonName personName2 = lcm.createPersonName("Test2","M","User2");
       user2.setPersonName(personName2);

       ArrayList<User> users = new ArrayList<User>();
       users.add(user1);
       users.add(user2);

       //Adding collection of users objects to organization object
       org.addUsers(users);

       ArrayList<Object> orgs = new ArrayList<Object>();
       orgs.add(org);
       BulkResponse br = lcm.saveOrganizations(orgs);
       assertResponseSuccess("Error during saveOrganizations", br);
       //Fetch organization that was just saved from registry
       Organization orgNew  = (Organization)bqm.getRegistryObject(org.getKey().getId(), LifeCycleManager.ORGANIZATION);
       Collection<?> colUsers = orgNew.getUsers();
       Collection<Object> userKeys = new ArrayList<Object>();
       Iterator<?> iter = colUsers.iterator();
       
       //Fetching of user object keys,which are associated with organization			
       while(iter.hasNext()) {
       User user = (User)iter.next();
       userKeys.add(user.getKey());
       }

       //Deleting an User objects ,which are associated with Organization
       BulkResponse resp = lcm.deleteObjects(userKeys, null, forceRemoveRequestSlotsMap, (String)null);
       assertEquals(BulkResponse.STATUS_SUCCESS,resp.getStatus());

       //Fetching an organization object to check wether user objects deleted or not from registry.
       Organization orgNew1  = (Organization)bqm.getRegistryObject(org.getKey().getId(), LifeCycleManager.ORGANIZATION);
       assertEquals("No Users found...",0,orgNew1.getUsers().size());
    }
}
