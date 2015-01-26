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
package it.cnr.icar.eric.common.security.wss4j;

import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.common.CredentialInfo;

import java.util.List;

import javax.xml.soap.SOAPEnvelope;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.util.WSSecurityUtil;
import org.opensaml.saml2.core.Assertion;
import org.w3c.dom.Document;

public class WSS4JSecurityUtilSAML extends WSS4JSecurityUtilBase {
	
    private static Crypto getIssuerCrypto() throws WSSecurityException {
    	return CryptoFactory.getInstance(CommonProperties.getInstance().
                getProperty("eric.common.security.idp.crypto"));
    }
    private static String getIssuerAlias() {
    	return CommonProperties.getInstance().
                getProperty("eric.common.security.idp.alias");
    }
    private static String getIssuerKeypass() {
    	return CommonProperties.getInstance().
                getProperty("eric.common.security.idp.keypass");
    }


	/*
	 * Server has role SOAP receiver <-- signSOAPEnvelopeOnClientSAML()
	 * 
	 * 		assertion branch: RegistrySAMLServlet
	 */
	public static void verifySOAPEnvelopeOnServerSAML(SOAPEnvelope soapEnvelope, CredentialInfo credentialInfo) throws WSSecurityException {
		WSSecurityEngine secEngine = new WSSecurityEngine();
    	
        WSSConfig.init();
        
		Document doc = soapEnvelope.getOwnerDocument();
        
		List<WSSecurityEngineResult> results = 
                secEngine.processSecurityHeader(doc, null, null, getIssuerCrypto());
		
		WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        AssertionWrapper assertionWrapper = 
            (AssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        
        Assertion assertion = assertionWrapper.getSaml2();
                
        credentialInfo.assertion = assertion;
	}


	
	
	
	/*
	 * Client has role SOAP sender --> verifySOAPEnvelopeOnServerSAML()
	 * 
	 * 	Assertion branch. Modus: Assertion (SAML).
	 */
	public static void signSOAPEnvelopeOnClientSAML(SOAPEnvelope se, CredentialInfo credentialInfo) {
		try {
			
        	// call to local class
    		signSOAPEnvelopeSAML(se, credentialInfo, getIssuerCrypto());
			  
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /*
     * Signing SOAP message with assertion
     */
	protected static void signSOAPEnvelopeSAML(SOAPEnvelope se, CredentialInfo credentialInfo, Crypto issuerCrypto)
			throws WSSecurityException {
		
        WSSConfig.init();
		
        // inject empty SecHeader into Document
        Document doc = se.getOwnerDocument();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        WSSecTimestamp timestamp = createTimestamp();
        timestamp.build(doc, secHeader);
        
		WSS4JSignatureSAML sign = new WSS4JSignatureSAML();
		
		// <wsse:BinarySecurityToken ...>
		sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
		
		// <ds:KeyInfo>
		sign.setCertUri(CanonicalConstants.CANONICAL_URI_SENDER_CERT);
		
		String soapNamespace = WSSecurityUtil.getSOAPNamespace(doc.getDocumentElement());

		List<WSEncryptionPart> parts = createReferences(soapNamespace);
		sign.setParts(parts);
		
		AssertionWrapper assertionWrapper = new AssertionWrapper(credentialInfo.assertion);
		sign.build(
		        doc, 				// W3C envelope
		        null, 				// uCrypto
		        assertionWrapper,  	// assertion
		        issuerCrypto, 			// iCrypto
		        getIssuerAlias(), 	// iKeyName
		        getIssuerKeypass(), // iKeyPW
		        secHeader			// secHeader
		    );
	}



	
}
