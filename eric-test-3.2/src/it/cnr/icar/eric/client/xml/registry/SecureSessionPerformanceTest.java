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
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

import java.util.ArrayList;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * jUnit Test for LifeCycleManager
 *
 * @author Farrukh Najmi
 */
public class SecureSessionPerformanceTest extends ClientTest {
    
    private static final Log log = LogFactory.getLog(SecureSessionPerformanceTest.class);
    
    public SecureSessionPerformanceTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SecureSessionPerformanceTest.class);
        return suite;
    }
    
    public void testImlicitSave() throws Exception {
        //Create the pkg that is the main object to save explicitly
        String createSecureSession = 
            ProviderProperties.getInstance()
                              .getProperty("jaxr-ebxml.security.createSecureSession");    
        log.info("jaxr-ebxml.security.createSecureSession: "+
            createSecureSession);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            
            //Create RegistryPackage
            ArrayList<RegistryPackage> saveObjects = new ArrayList<RegistryPackage>();
            RegistryPackage pkg = lcm.createRegistryPackage("SecureSessionPerformanceTest.pkg"+i);
            saveObjects.add(pkg);
            
            //Save RegistryPackage
            BulkResponse br = lcm.saveObjects(saveObjects);      
            assertTrue("Package creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            //Delete RegistryPackage
            ArrayList<Key> deleteObjects = new ArrayList<Key>();            
            deleteObjects.add(pkg.getKey());
            br = lcm.deleteObjects(deleteObjects);
            assertTrue("Package deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
        long endTime = System.currentTimeMillis();
        log.info("Time to create and delete 10 objects (millisecs): "+
            (endTime - startTime));
    }
        
    public static void main(String[] args) {
	System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}
