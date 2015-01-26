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

package it.cnr.icar.eric.client.xml.registry.util;

import it.cnr.icar.eric.client.common.ClientTest;

import java.io.File;
import javax.xml.registry.infomodel.User;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * jUnit Test for CertificateUtil class
 *
 * @author Farrukh Najmi
 */
public class CertificateUtilTest extends ClientTest {
    static final String alias = "testGenerateRegistryIssuedCertificate";
    static final String p12FileName = alias + ".p12";

    public CertificateUtilTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(CertificateUtilTest.class);
        return suite;
    }

    /**
     * Ensure we don't get confused by a .p12 file that already exists or
     * leave one lying around
     */
    private void setUp_tearDown_internal() throws Exception {
	// TODO: could also move userRegInfo construction and removal of
	// existing certificate from client keystore into this method
        File p12File = new File(p12FileName);
        if (p12File.exists()) {
            p12File.delete();
        }
    }

    /**
     * Ensure we don't get confused by a .p12 file that already exists
     */
    public void setUp() throws Exception {
	super.setUp();
	setUp_tearDown_internal();
    }

    /**
     * Ensure we don't leave a .p12 file lying around
     */
    public void tearDown() throws Exception {
	super.tearDown();
        setUp_tearDown_internal();
    }

    /**
     * Tests generateRegistryIssuedCertificate method.
     */
    public void testGenerateRegistryIssuedCertificate() throws Exception {
        User user = createUser("TestUser." + alias);
        UserRegistrationInfo userRegInfo = new UserRegistrationInfo(user);
        char[] storePassword = userRegInfo.getStorePassword();
        char[] keyPassword = alias.toCharArray();

        userRegInfo.setAlias(alias);
        userRegInfo.setKeyPassword(keyPassword);
        userRegInfo.setP12File(p12FileName);

        // Remove existing cert if any
        if (CertificateUtil.certificateExists(alias, storePassword)) {
            CertificateUtil.removeCertificate(alias, storePassword);
        }

	CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
	assertTrue("Certificate not found after creating it",
		   CertificateUtil.certificateExists(alias, storePassword));

	File p12File = new File(p12FileName);
	assertTrue("p12 file was not created", p12File.exists());

	CertificateUtil.removeCertificate(alias, storePassword);
	assertFalse("Certificate found when it should have been removed",
		    CertificateUtil.certificateExists(alias, storePassword));
    }
}
