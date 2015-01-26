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

//import com.sun.xml.wss.WssSoapFaultException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.registry.JAXRException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.security.SoapSecurityUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import org.apache.ws.security.WSSecurityException;

/**
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class SoapSecurityUtilTest extends TestCase {
 
    /** Test SOAP message, as String. */
    private static final String TEST_SOAP_MESSAGE_STRING = 
        "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<soap-env:Header/>" +
        "<soap-env:Body>" +
        "data" +
        "</soap-env:Body>" +
        "</soap-env:Envelope>";
    
    /** Test credentials, created on the fly */
    private static CredentialInfo credentialInfo;

    public SoapSecurityUtilTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SoapSecurityUtilTest.class);
        return suite;
    }

    /**
     * Test signSoapMessage() method, of class
     * it.cnr.icar.eric.common.security.SoapSecurityUtil.
     */
    private SOAPMessage signSoapMessage() throws Exception {
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        CredentialInfo credentialInfo = getCredentialInfo();
        
        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
	SOAPMessage soapMessage = factory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));

        soapMessage = ssu.signSoapMessage(soapMessage, credentialInfo);
        
        soapMessage.writeTo(System.out);
        System.out.println("\n");
	return soapMessage;
    }

    /**
     * Test signSoapMessage() method, of class
     * it.cnr.icar.eric.common.security.SoapSecurityUtil.
     */
    private SOAPMessage signSoapMessageWA() throws Exception {
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        CredentialInfo credentialInfo = getCredentialInfo();
        
        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
	SOAPMessage soapMessageWA = factory.createMessage();
        SOAPPart soapPart = soapMessageWA.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));

        // Construct AttachmentPart and add it to SOAPMessage
        AttachmentPart ap = soapMessageWA.createAttachmentPart("test", "text/plain");
        ap.setContentId("<foo>");
        soapMessageWA.addAttachmentPart(ap);
        
        soapMessageWA = ssu.signSoapMessage(soapMessageWA, credentialInfo);
        
        soapMessageWA.writeTo(System.out);
        System.out.println("\n");
	return soapMessageWA;
    }
    
    /**
     * Test verifySoapMessage() method, of class
     * it.cnr.icar.eric.common.security.SoapSecurityUtil.  Verify
     * verification is destructive.
     */
    public void testVerifySoapMessage() throws Exception {
        System.out.println("testVerifySoapMessage");
	SOAPMessage soapMessage = signSoapMessage();
	CredentialInfo credentialInfo = new CredentialInfo();

        // Verify untouched msg
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        assertTrue("Verification failed unexpectedly",
		   ssu.verifySoapMessage(soapMessage, credentialInfo));
	assertEquals("Verified credentials do not match senders'",
		     getCredentialInfo().cert, credentialInfo.cert);
	System.out.println("... after verification");
	soapMessage.writeTo(System.out);
        System.out.println("\n");
    }

    /**
     * Change message body and verify
     */
    public void testVerifySoapMessage_Changed() throws Exception {
        System.out.println("testVerifySoapMessage_Changed");
	SOAPMessage soapMessage = signSoapMessage();
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
	CredentialInfo credentialInfo = new CredentialInfo();

        try {
            soapMessage.getSOAPBody().addChildElement("unsigned-element");
	    soapMessage.saveChanges();
	    System.out.println("... after change");
	    soapMessage.writeTo(System.out);
	    System.out.println("\n");
            assertFalse("Verification succeeded unexpectedly",
			ssu.verifySoapMessage(soapMessage, credentialInfo));
	    // Should not reach here -- what specific problem occurred?
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            assertTrue("Different Exception class (" +
		       e.getCause().getClass().getName() + ") than expected",
		       e.getCause() instanceof WSSecurityException); // ||
//		       e.getCause() instanceof XWSSecurityException);
        }
    }

    /**
     * Test of verifySoapMessage() method, of class
     * it.cnr.icar.eric.common.security.SoapSecurityUtil.  Verify
     * verification is destructive.
     */
    public void testVerifySoapMessageWA() throws Exception {
        System.out.println("testVerifySoapMessage w/ attachment");
	SOAPMessage soapMessageWA = signSoapMessageWA();
	CredentialInfo credentialInfo = new CredentialInfo();

        // Verify untouched msg
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
        assertTrue("Verification failed unexpectedly",
		   ssu.verifySoapMessage(soapMessageWA, credentialInfo));
	assertEquals("Verified credentials do not match senders'",
		     getCredentialInfo().cert, credentialInfo.cert);
	System.out.println("... after verification");
	soapMessageWA.writeTo(System.out);
        System.out.println("\n");
    }

    /**
     * Add another message attachment and expect verification to fail
     */
    public void testVerifySoapMessageWA_Added() throws Exception {
        System.out.println("testVerifySoapMessage w/ attachment added");
	SOAPMessage soapMessageWA = signSoapMessageWA();
	CredentialInfo credentialInfo = new CredentialInfo();
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();

        try {
	    // Construct AttachmentPart and add it to SOAPMessage
            AttachmentPart ap = soapMessageWA.createAttachmentPart("tampered test", "text/plain");
            ap.setContentId("<foo>");
            soapMessageWA.addAttachmentPart(ap);
	    soapMessageWA.saveChanges();
	    System.out.println("... after addition");
	    soapMessageWA.writeTo(System.out);
	    System.out.println("\n");
            assertFalse("Verification succeeded unexpectedly",
			ssu.verifySoapMessage(soapMessageWA, credentialInfo));
	    // Should not reach here -- what specific problem occurred?
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
	    // Expected failure -- added attachment
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            assertTrue("Different Exception class (" +
		       e.getCause().getClass().getName() + ") than expected",
		       e.getCause() instanceof WSSecurityException); // ||
		     //  e.getCause() instanceof XWSSecurityException);
        }
    }

    /**
     * Change message attachment and expect verification to fail
     */
    public void testVerifySoapMessageWA_Changed() throws Exception {
        System.out.println("testVerifySoapMessage w/ attachment changed");
	SOAPMessage soapMessageWA = signSoapMessageWA();
	CredentialInfo credentialInfo = new CredentialInfo();
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();

        try {
	    // Remove attachments and add similar one to SOAPMessage
            soapMessageWA.removeAllAttachments();
            AttachmentPart ap = soapMessageWA.createAttachmentPart("tampered test", "text/plain");
            ap.setContentId("<foo>");
            soapMessageWA.addAttachmentPart(ap);
	    soapMessageWA.saveChanges();
	    System.out.println("... after change");
	    soapMessageWA.writeTo(System.out);
	    System.out.println("\n");
            assertFalse("Verification succeeded unexpectedly",
			ssu.verifySoapMessage(soapMessageWA, credentialInfo));
	    // Should not reach here -- what specific problem occurred?
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
	    // Expected failure -- attachment with different content
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            assertTrue("Different Exception class (" +
		       e.getCause().getClass().getName() + ") than expected",
		       e.getCause() instanceof WSSecurityException);// ||
//		       e.getCause() instanceof XWSSecurityException);
        }
    }

    /**
     * Remove message attachments and expect verification to fail
     */
    public void testVerifySoapMessageWA_Removed() throws Exception {
        System.out.println("testVerifySoapMessage w/ attachment removed");
	SOAPMessage soapMessageWA = signSoapMessageWA();
	CredentialInfo credentialInfo = new CredentialInfo();
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();

        try {
	    // Get rid of the signed attachment
            soapMessageWA.removeAllAttachments();
	    soapMessageWA.saveChanges();
	    System.out.println("... after removal");
	    soapMessageWA.writeTo(System.out);
	    System.out.println("\n");
            assertFalse("Verification succeeded unexpectedly",
			ssu.verifySoapMessage(soapMessageWA, credentialInfo));
	    // Should not reach here -- what specific problem occurred?
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            fail("Tampered message verification must fail.");
        } catch (JAXRException e) {
	    // Expected failure -- missing attachment
	    assertNull("Credentials should not have been filled in",
		       credentialInfo.cert);
            assertTrue("Different Exception type (" +
		       e.getCause().getClass().getName() + ") than expected",
		       e.getCause() instanceof WSSecurityException);// ||
//		       e.getCause() instanceof XWSSecurityException);
        }
    }

    /**
     * Test verifySoapMessage() method, of class
     * it.cnr.icar.eric.common.security.SoapSecurityUtil.
     */
    public void testVerifySoapMessage_NoSignature() throws Exception {
        System.out.println("testVerifySoapMessage_NoSignature");

        // Create unsigned message
        SoapSecurityUtil ssu = SoapSecurityUtil.getInstance();
	CredentialInfo credentialInfo = new CredentialInfo();

        InputStream soapStream = new ByteArrayInputStream(
                TEST_SOAP_MESSAGE_STRING.getBytes("utf-8"));
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapMessage = factory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContent(new StreamSource(soapStream));
        
        // Verify msg
        assertFalse("verify should return false when no SecurityHeader present",
		    ssu.verifySoapMessage(soapMessage, credentialInfo));
	assertNull("Credentials should not have been filled in",
		   credentialInfo.cert);
    }
    
    /** Convenience method to get test credentials. */
    private CredentialInfo getCredentialInfo() throws Exception {
        if (credentialInfo == null) {
            credentialInfo = createCredentialInfo();
        }
        return credentialInfo;
    }

    /** Convenience method to create test credentials. */
    private CredentialInfo createCredentialInfo() throws Exception {
        // xws-security SignFilter supports only RSA!
        // Generating 512 bit RSA key pair and self-signed certificate (SHA1WithRSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("RSA", "SHA1WithRSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL", "KS", "FI");
        certandkeygen.generate(512);
        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,  90 * 24 * 60 * 60);

        //Create the CredentialInfo wrapper.
        CredentialInfo credentialInfo = new CredentialInfo("TestUserAlias",
            ax509cert[0], ax509cert, privateKey);
        return credentialInfo;
    }
}
