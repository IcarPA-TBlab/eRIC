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

package it.cnr.icar.eric.common.security;

import java.lang.Math;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.ericTest;
import it.cnr.icar.eric.common.security.SoapSecurityUtil;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

/**
 * Look for a memory leak while creating and signing SOAP messages.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 * @author Farrukh Najmi, Sun Microsystems
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.8 $
 */
public class SoapSecurityUtilMemTest extends ericTest {
    // Set ${test.repetitions.inner} to about 100 for real test!
    private static final int TIMES_TO_RUN = 16 * testRepetitionsInner;

    private static final long MEMORY_LEAK_TOLERANCE = 5*1024*1024; //5MB

    /** Test SOAP message, as String. */
    // ??? Very small message.
    private static final String TEST_SOAP_MESSAGE_STRING =
        "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap-env:Header/>" +
        "<soap-env:Body>" +
        "data" +
        "</soap-env:Body>" +
        "</soap-env:Envelope>";

    public SoapSecurityUtilMemTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite =
	    new junit.framework.TestSuite(SoapSecurityUtilMemTest.class);
        return suite;
    }

    /**
     * Test memory leak on sign
     */
    public void testSignMem() throws Exception {
        //ResourceResolver.init();
        CredentialInfo credentialInfo = createCredentialInfo();

        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        MessageFactory factory = MessageFactory.newInstance();

        // run once to initialize classes
        runOnce(ssu, factory, credentialInfo);

        Runtime rt = Runtime.getRuntime();
        rt.runFinalization();
        rt.gc();

        long start = rt.totalMemory() - rt.freeMemory();
	long max = start, min = start;
        System.out.println("Starting memory test with used memory of " +
			   start);

        for (int i = 0; i < TIMES_TO_RUN;) {
	    i++;
            runOnce(ssu, factory, credentialInfo);

            rt.runFinalization();
            rt.gc();

	    long middle = rt.totalMemory() - rt.freeMemory();
	    max = Math.max(max, middle);
	    min = Math.min(min, middle);

	    // first 10, last 10, every 50 in between.
	    if (10 > i || (TIMES_TO_RUN - 10) < i || 0 == i % 50) {
		System.out.println("Memory test after " + i + " runs: " +
				   middle + "\t(" + (middle-start) + ")");
	    }
            //Thread.currentThread().sleep(1000);
        }
        //Thread.currentThread().sleep(60*60*1000);


	// should be redundant with last garbage collection in loop
        rt.runFinalization();
        rt.gc();

        long end = rt.totalMemory() - rt.freeMemory();
        System.out.println("Memory test completed, memory now: " +
			   end + "\t(" + (end-start) + ")");

	max = Math.max(max, end);
	min = Math.min(min, end);
	long diff = max - min;
        assertTrue("Memory leak (tolerance " + MEMORY_LEAK_TOLERANCE + "): " +
		   diff, diff < MEMORY_LEAK_TOLERANCE);
    }

     public void runOnce(SoapSecurityUtil ssu,
			 MessageFactory factory,
			 CredentialInfo credentialInfo)
	 throws Exception {
        InputStream soapStream = new ByteArrayInputStream(
            TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        SOAPMessage soapMessage = factory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));

        ssu.signSoapMessage(soapMessage, credentialInfo);
    }

    /** Convenience method to create test credentials. */
    private static CredentialInfo createCredentialInfo() throws Exception {
        // xws-security SignFilter supports only RSA!
        // Generating 512 bit RSA key pair and self-signed certificate
        // (SHA1WithRSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("RSA", "SHA1WithRSA");
        X500Name x500name =
	    new X500Name("Tester", "Test Unit", "OMAR", "JKL", "KS", "FI");
        certandkeygen.generate(512);
        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] =
	    certandkeygen.getSelfCertificate(x500name,  90 * 24 * 60 * 60);

        //Create the CredentialInfo wrapper.
        CredentialInfo credentialInfo = new CredentialInfo("TestUserAlias",
            ax509cert[0], ax509cert, privateKey);
        return credentialInfo;
    }
}
