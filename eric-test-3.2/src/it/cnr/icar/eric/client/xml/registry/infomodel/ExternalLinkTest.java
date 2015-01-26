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
import it.cnr.icar.eric.common.exceptions.ObjectsNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for ExternalLink
 *
 * @author Farrukh Najmi
 */
public class ExternalLinkTest extends ClientTest {
    
    boolean compatibilityMode = Boolean.valueOf(it.cnr.icar.eric.client.xml.registry.util.ProviderProperties.getInstance()
        .getProperty("jaxr-ebxml.tck.compatibilityMode",
        "false")).booleanValue();
        
    public ExternalLinkTest(String testName) {
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
        TestSuite suite = new TestSuite(ExternalLinkTest.class);
        return suite;
    }
        
    /**
     * Test that InvalidRequestException is thrown per JAXR API when
     * externalURI is not a valid URI.
     */
    public void testValidURL() throws Exception {
        //Assumes server is running
        ExternalLink extLink =
	    lcm.createExternalLink("http://www.sun.com", "this is a valid URI");
        assertNotNull("Could not create ExternalLink", extLink);
    }                    
    
    /**
     * Test that InvalidRequestException is thrown per JAXR API when
     * externalURI is not a valid URI.
     */
    public void testInvalidURI() throws Exception {
        try {
            @SuppressWarnings("unused")
			ExternalLink extLink = lcm.createExternalLink("this is not a valid URI", 
                "this is not a valid URI");
            
            if (compatibilityMode) {
                fail("Did not throw InvalidRequestException when URI was not valid URI structure.");
            }
        } catch (InvalidRequestException e) {
            //Success
        }
    }                    
    
    /**
     * Test that InvalidRequestException is thrown per JAXR API when
     * externalURI is not resolvable.
     */
    public void testUnresolvableURL() throws Exception {
        try {
            @SuppressWarnings("unused")
			ExternalLink extLink = lcm.createExternalLink("http://unresolvable.link", 
                "This link is unresolvable.");
            
            if (compatibilityMode) {
                fail("Did not throw InvalidRequestException when URL was not resolvable.");
            }
        } catch (InvalidRequestException e) {
            //Success
        }
    }
    
    /**
     * Tests saving a externalLink directly and not through its clasified object.
     */
    public void testExternalLinkOnly() throws Exception {
        String pkg1Id = "urn:freebxml:registry:test:ExternalLinkTest.testExternalLinkOnly:pkg1";
        String exl1Id = "urn:freebxml:registry:test:ExternalLinkTest.testExternalLinkOnly:exl1";
                
        //Delete in case object is around from past failed run
        ArrayList<Key> deleteObjects = new ArrayList<Key>();
        deleteObjects.add(lcm.createKey(pkg1Id));
        try {
            lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        } catch (ObjectsNotFoundException e) {
            //ObjectsNotFoundException may be ok.
        }

        deleteObjects.clear();
        deleteObjects.add(lcm.createKey(exl1Id));
        try {
            lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        } catch (ObjectsNotFoundException e) {
            //ObjectsNotFoundException may be ok.
        }
        
        // Create a package w/o externalLink
        RegistryPackage pkg1 = lcm.createRegistryPackage(pkg1Id+":name");
        pkg1.getKey().setId(pkg1Id);
        Collection<RegistryObject> saveObjects = new ArrayList<RegistryObject>();
        saveObjects.add(pkg1);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving pkg1 failed.", br);                

        //Create a externalLink for that pkg
        ExternalLink exl1 = lcm.createExternalLink("http://www.yahoo.com","External Link");
        exl1.getKey().setId(exl1Id);
        pkg1.addExternalLink(exl1);

        //Save the pkg (and the externalLink)
        saveObjects.clear();
        saveObjects.add(pkg1);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving pck1 failed.", br);                

        // Read the pkg back, check external link
        RegistryPackage pkg2 = (RegistryPackage)getDQM().getRegistryObject(pkg1Id, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to read back pkg2", pkg2);
        assertEquals("pkg2 does not have correct number of ExternalLinks", 1, pkg2.getExternalLinks().size());
        
        //Modify the externalLink only, save it separately
        exl1.setName(lcm.createInternationalString(exl1+":name"));
        saveObjects.clear();
        saveObjects.add(exl1);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving exl1 failed.", br);                
        
        // Read the pkg back, check external link modified
        pkg2 = (RegistryPackage)getDQM().getRegistryObject(pkg1Id, LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to read back pkg2", pkg2);
        assertEquals("pkg2 does not have correct number of ExternalLinks", 1, pkg2.getExternalLinks().size());
        assertEquals("exl1 has not been saved", exl1+":name", 
                ((ExternalLink)pkg1.getExternalLinks().toArray()[0]).getName().getValue());
                        
        //Now delete pkg1 and exl1
        deleteObjects = new ArrayList<Key>();
        deleteObjects.add(pkg1.getKey());
        deleteObjects.add(exl1.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);
    }
}
