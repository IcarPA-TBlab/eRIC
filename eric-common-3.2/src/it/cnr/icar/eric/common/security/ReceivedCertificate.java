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

import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CommonResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.mail.internet.MimeUtility;
import javax.xml.registry.JAXRException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Element;

/**
 * Information about certificate(s) from received SOAP message.
 *
 * @author Doug Bunting (refactored from sister xwssec* packages)
 */
public class ReceivedCertificate {
    /** namespaces used for WSS content */
    public static final String securityNS =
	"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String securityUtilityNS =
	"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /**
     * decoded content of a &lt;wss:BinarySecurityToken/&gt; element
     * adorned with a @wss:Id attribute identifying it for this purpose
     */
    private X509Certificate cert = null;

    /**
     * true if multiple appropriate elements were found (cert will contain
     * content of first in this case)
     */
    private boolean foundMultiple = false;

    /**
     * Find Base64 encoded certificate used to sign given message.  No
     * default constructor: Once content has been created, remains
     * unchanged for life of the instance.
     *
     * @param msg (received) SOAP message to parse
     * @exception JAXRException if any problem at all occurs, wrapping
     *  problems decoding content (from Base64) and any caught
     *  CertificateException or SOAPException
     */
	public ReceivedCertificate(SOAPMessage msg) throws JAXRException {
	// @wss:Id attribute value for <BinarySecurityToken/> element of interest
	final String tokenId = CanonicalConstants.CANONICAL_URI_SENDER_CERT;

        try {
            final Name binSecTokenName = SOAPFactory.newInstance().
                                                    createName("BinarySecurityToken", "wsse", securityNS);

            SOAPHeader hdr = msg.getSOAPHeader();
            Iterator<?> hdrElemIter = hdr.examineAllHeaderElements();
            while (hdrElemIter.hasNext()) {
                Object hdrElemObj = hdrElemIter.next();
                if (hdrElemObj instanceof SOAPHeaderElement) {
		    // found a SOAP header element of some type
                    SOAPHeaderElement hdrElem = (SOAPHeaderElement)hdrElemObj;
                    if ((hdrElem.getLocalName().equals("Security")) &&
                        (hdrElem.getNamespaceURI().equals(securityNS))) {

			// found a <wss:Security/> element
//                        Name binSecTokenName = SOAPFactory.newInstance().
//			    createName("BinarySecurityToken", "wsse", securityNS);
                        Iterator<?> secTokensIter =
			    hdrElem.getChildElements(binSecTokenName);
                        while (secTokensIter.hasNext()) {
                            Object binSecTokenObj = secTokensIter.next();
                            if (binSecTokenObj instanceof Element) {
				// found a <BinarySecurityToken/> element
                                Element binSecTokenElem = (Element)binSecTokenObj;
                                String _tokenId = binSecTokenElem.
				    getAttributeNS(securityUtilityNS, "Id");
                                if (_tokenId.equals(tokenId)) {
				    // found propery identified element
				    if (null == cert) {
					// found first cert content
					InputStream is = null;
					String encodedData = binSecTokenElem.
					    getFirstChild().getNodeValue();
					try {
					    try {
						is = new
						    ByteArrayInputStream(encodedData.
									 getBytes("UTF-8"));
						is = MimeUtility.decode(is,
									"base64");
					    } catch (Exception e) {
						throw new
						    JAXRException(CommonResourceBundle.
								  getInstance().
								  getString("message.UnableToDecodeData"),
								  e);
					    }

					    CertificateFactory cf =
						CertificateFactory.
						getInstance("X.509");
					    cert = (X509Certificate)cf.
						generateCertificate(is);
					} finally {
					    if (is != null) {
						try {
						    is.close();
						} catch (Exception e) {
							is = null;
						}
					    }
					}
				    } else {
					// found second cert content
					foundMultiple = true;
					break;
				    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SOAPException e) {
            throw new JAXRException(CommonResourceBundle.getInstance().
				    getString("message.CouldNotGetCertificate"),
				    e);
        } catch (CertificateException e) {
            throw new JAXRException(CommonResourceBundle.getInstance().
				    getString("message.CouldNotGetCertificate"),
				    e);
        }
    }

    /**
     * retrieve certificate of interest
     *
     * @return certificate found in original message -- decoded content of
     *  a &lt;wss:BinarySecurityToken/&gt; element adorned with a @wss:Id
     *  attribute identifying it for this purpose
     */
    public X509Certificate getCertificate() {
	return cert;
    }

    /**
     * check if muliple certificates were found in message
     *
     * @return true if multiple appropriate elements were found
     *  (getCertificate() will return content of first in this case)
     */
    public boolean isMultiple() {
	return foundMultiple;
    }
}
