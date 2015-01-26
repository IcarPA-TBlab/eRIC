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
import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.SOAPMessenger;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.RegistryException;
import javax.xml.soap.SOAPEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

public class WSS4JSecurityUtilBase {

	/** Key for configuration property 'max clock skew', passed to wss4j-security API.
	 *  "The assumed maximum skew (milliseconds) between the local times of any two systems."
	 */
	private static String getKeyMaxClockSkew() {
		return CommonProperties.getInstance().getProperty("eric.common.security.maxClockSkew");
	}

	private static Log log = LogFactory.getLog(SOAPMessenger.class);

	/**
	 * Convenience method to extract an UUID/URN from a Content ID (CID). CIDs
	 * are used for identifying attachments in signed SOAP messages.
	 * 
	 * @param cid
	 *            The attachment's Content-ID.
	 * @return The corresponding UUID/URN.
	 */
	public static String convertContentIdToUUID(String cid) throws RegistryException {
		if (!(cid.charAt(0) == '<' && cid.charAt(cid.length() - 1) == '>')) {
			// error, not a cid URI Scheme id.
			throw new RegistryException(CommonResourceBundle.getInstance().getString("message.CIDURIExpected",
					new Object[] { cid }));
		}
	
		String uuid = cid.substring(1, cid.length() - 1);
		return uuid;
	}

	/**
	 * Convenience method to turn an UUID/URN into a Content ID (CID). CIDs are
	 * used for identifying attachments in signed SOAP messages.
	 * 
	 * @param uuid
	 *            The original UUID/URN.
	 * @return The generated CID to be set as attachment Content-ID.
	 */
	public static String convertUUIDToContentId(String uuid) {
		String cid = "<" + uuid + ">";
		return cid;
	}

	protected static List<WSEncryptionPart> createReferences(String soapNamespace) {
		List<WSEncryptionPart> parts = new ArrayList<WSEncryptionPart>();
        WSEncryptionPart encP1 =
                new WSEncryptionPart(
                    WSConstants.TIMESTAMP_TOKEN_LN,
                    WSConstants.WSU_NS,
                    "Element");
            parts.add(encP1);
        WSEncryptionPart encP2 =
            new WSEncryptionPart(
                    WSConstants.ELEM_BODY, 
                    soapNamespace, 
                    "Content"
        			);
        parts.add(encP2);
		return parts;
	}


	protected static WSSecTimestamp createTimestamp() {
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        
        int timeToLive = 300; // default
        String maxClockSkew = getKeyMaxClockSkew();
        
        if (maxClockSkew != null) {
            try {
            	timeToLive = Integer.parseInt(maxClockSkew);
            } catch (NumberFormatException e) {
                log.warn(CommonResourceBundle.getInstance().getString("message.InvalidlongValueForProperty", new Object[]{maxClockSkew}), e);
            }
        }
        
        timestamp.setTimeToLive(timeToLive);
		return timestamp;
	}
	
	
	/*
	 * Client has role SOAP receiver <-- signSOAPEnvelopeOnServerBST()
	 */
	public static void verifySOAPEnvelopeOnClientBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
		// remark: this method is not used since OMAR v3.0
		// client side verification of signatures is not implemented  
	    try {
			Crypto crypto = CryptoFactory.getInstance("crypto-client.properties");
			verifySOAPEnvelopeBST(se, credentialInfo, crypto);
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // default crypto

	}

	
	/*
	 * Server has role SOAP receiver <-- signSOAPEnvelopeOnClientBST()
	 * 
	 * 		certificate branch: RegistryBSTServlet
	 */
	public static void verifySOAPEnvelopeOnServerBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
	    try {
			Crypto crypto = CryptoFactory.getInstance("crypto-server.properties");
			verifySOAPEnvelopeBST(se, credentialInfo, crypto);
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // default crypto

	}


	protected static void verifySOAPEnvelopeBST(SOAPEnvelope se, CredentialInfo credentialInfo, Crypto crypto) throws WSSecurityException {
		
		WSSecurityEngine secEngine = new WSSecurityEngine();
		
	    WSSConfig.init();
		
		Document doc = se.getOwnerDocument();
	    
		List<WSSecurityEngineResult> results = 
	            secEngine.processSecurityHeader(doc, null, null, crypto);
	
		if (results != null) {
			    				
	        WSSecurityEngineResult actionResult =
	                WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
	        X509Certificate cert = 
	                (X509Certificate)actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
			
			// inject certificate for further processing
	        credentialInfo.cert = cert;
		}
	}

	
	/*
	 * Server has role SOAP sender --> verifySOAPEnvelopeOnClientBST()
	 */
    public static void signSOAPEnvelopeOnServerBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
		try {
			signSOAPEnvelopeBST(se, credentialInfo, CryptoFactory.getInstance("crypto-server.properties"));
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /*
     * Signing SOAP message with certificate
     */
	protected static void signSOAPEnvelopeBST(SOAPEnvelope se, CredentialInfo credentialInfo, Crypto userCrypto)
			throws WSSecurityException {
		
		WSSConfig.init();

		// inject empty SecHeader into Document
		Document doc = se.getOwnerDocument();
		WSSecHeader secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader(doc);

		WSSecTimestamp timestamp = createTimestamp();
		timestamp.build(doc, secHeader);

		// overridden WSS4J path for explicit setPrivateKey()
		// <ds:Signature>
		WSS4JSignatureBST sign = new WSS4JSignatureBST();

		// <wsse:BinarySecurityToken ...>
		sign.setX509Certificate(credentialInfo.cert);
		sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

		sign.setPrivateKey(credentialInfo.privateKey);

		// <ds:KeyInfo>
		sign.setCertUri(CanonicalConstants.CANONICAL_URI_SENDER_CERT);

		String soapNamespace = WSSecurityUtil.getSOAPNamespace(doc.getDocumentElement());

		// signature references
		// <ds:Reference>
		List<WSEncryptionPart> parts = createReferences(soapNamespace);
		sign.setParts(parts);

		sign.build(doc, 
				userCrypto, 
				secHeader);
	}
	
}