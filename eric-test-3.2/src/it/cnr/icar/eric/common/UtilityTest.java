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

package it.cnr.icar.eric.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.io.InputStream;


/**
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class UtilityTest extends ericTest {
    
    private static final String TMP_DIR = System.getProperty(
            "java.io.tmpdir");
    
    
    public UtilityTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
	// we're explicit only to handle case where we can't use
	// ebxmlrrSpecHome
	if (canUseEbxmlrrSpecHome) {
	    suite.addTest(new UtilityTest("testCreateZipOutputStream"));
	}
	suite.addTest(new UtilityTest("testUnzip"));
	suite.addTest(new UtilityTest("testIsValidRegistryId"));
	suite.addTest(new UtilityTest("testCreateId"));
	suite.addTest(new UtilityTest("testStripId"));
	suite.addTest(new UtilityTest("testIsValidURN"));
	suite.addTest(new UtilityTest("testIsValidURI"));
	suite.addTest(new UtilityTest("testFixURN"));
	suite.addTest(new UtilityTest("testGetURLPathFromURI"));
        suite.addTest(new UtilityTest("testMakeValidFileName"));
        return suite;
    }
    
    public void testCreateZipOutputStream() throws java.lang.Exception {
        String baseDir = ebxmlrrSpecHome + "/misc/";
        String[] relativeFilePaths = {
            "3.0/services/ebXMLRegistryServices.wsdl",
            "3.0/services/ebXMLRegistryBindings.wsdl",
            "3.0/services/ebXMLRegistryInterfaces.wsdl",
            "3.0/schema/rim.xsd",
            "3.0/schema/query.xsd",
            "3.0/schema/rs.xsd",
            "3.0/schema/lcm.xsd",
            "3.0/schema/cms.xsd",
        };
        
        File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();
        
        FileInputStream fis = new FileInputStream(zipFile);
        ArrayList<File> files = Utility.unZip(TMP_DIR, fis);
        assertTrue(files.size() == relativeFilePaths.length);
    }
    
    /*
     * Test bug where Utility.unzip was failing for certain zip files.
     */
    public void testUnzip() throws java.lang.Exception {
        URL url = new URL("http://docs.oasis-open.org/regrep/v3.0/regrep-3.0-os.zip");
        InputStream is = url.openStream();
        ArrayList<File> files = Utility.unZip(TMP_DIR, is);
        assertTrue("unzip failed.", files.size() > 0);
    }

    /**
     * Test of isValidRegistryId method, of class it.cnr.icar.eric.common.Utility.
     */
    public void testIsValidRegistryId() {
        System.out.println("testIsValidRegistryId");
        
        String uuid = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
        assertTrue(Utility.getInstance().isValidRegistryId(uuid));
        
        //Tests for id too long
        assertFalse("Did not catch an id that was too long.", Utility.getInstance().isValidRegistryId("urn:freebxml:registry:test:it.cnr.icar.eric.common.UtilityTest:testIsValidRegistryId:idtoolong:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    }

    /**
     * Test of createId method, of class it.cnr.icar.eric.common.Utility.
     */
    public void testCreateId() {
        System.out.println("testCreateId");

        String uuid = Utility.getInstance().createId();
        assertTrue(Utility.getInstance().isValidRegistryId(uuid));
    }

    /**
     * Test of stripId method, of class it.cnr.icar.eric.common.Utility.
     */
    public void testStripId() {
        System.out.println("testStripId");
        
        String urn = "urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP";
        String expectedStrippedUrn = "urn_oasis_names_tc_ebxml-regrep_acp_adminOnlyACP";
        String actualStrippedUrn = Utility.getInstance().stripId(urn);
        assertEquals(expectedStrippedUrn, actualStrippedUrn);
        
        String uuid = "urn:uuid:4db0761c-e613-4216-9681-e59534a660cb";
        String expectedStrippedUuid = "4db0761c-e613-4216-9681-e59534a660cb";
        String actualStrippedUuid = Utility.getInstance().stripId(uuid);
        assertEquals(expectedStrippedUuid, actualStrippedUuid);
    }

    /**
     * Test of isValidURN method, of class it.cnr.icar.eric.common.Utility.
     */
    public void testIsValidURN() {
        System.out.println("testIsValidURN");
        
        String urn = "urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP";
        assertTrue(Utility.getInstance().isValidURN(urn));
        assertFalse(Utility.getInstance().isValidURN(null));
    }

    /**
     * Test of isValidURI method, of class it.cnr.icar.eric.common.Utility.
     */
    public void testIsValidURI() {
        System.out.println("testIsValidURI");
        
        assertFalse(Utility.getInstance().isValidURI(null));
        assertTrue(Utility.getInstance().isValidURI("myTestURI"));
        assertTrue(Utility.getInstance().isValidURI("urn:uuid:4db0761c-e613-4216-9681-e59534a660cb"));
        assertTrue(Utility.getInstance().isValidURI("urn:oasis:names:tc:ebxml-regrep:acp:adminOnlyACP"));
        assertTrue(Utility.getInstance().isValidURI("http://www.google.com/"));
        assertFalse(Utility.getInstance().isValidURI("http://www.thisserverrrdusnottttexxxiiisst.com/"));
        assertTrue(Utility.getInstance().isValidURI("ftp://ftp.ibiblio.org/"));
    }
    
    public static void testFixURN() {
        String id = Utility.fixURN("a:b:c#:d/e_:");
        assertEquals(id, "urn:a:b:c_:d:e_:");
    }
    
    public static void testGetURLPathFromURI() throws Exception {
        String id = Utility.getURLPathFromURI("urn:oasis:names:tc:ebxml-regrep:wsdl:registry:bindings:3.0");
        assertEquals (id, "urn/oasis/names/tc/ebxml_regrep/wsdl/registry/bindings/3_0");
        id = Utility.getURLPathFromURI("urn:uddi-org:api_v3_binding");
        assertEquals(id, "urn/uddi_org/api_v3_binding");
    }
    
    public static void testMakeValidFileName() {
        String filename = "urn:org:acme:filename-1.0.xsd";
        filename = Utility.makeValidFileName(filename);
        assertEquals(filename, "urn-org-acme-filename-1.0.xsd");
    }
}
