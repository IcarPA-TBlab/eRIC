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
package it.cnr.icar.eric.server.interfaces.soap;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.security.wss4j.WSS4JSecurityUtilSAML;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.Utility;
import it.cnr.icar.eric.server.interfaces.Response;
import it.cnr.icar.eric.server.interfaces.common.SOAPServlet;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.internet.ParseException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.registry.RegistryException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.opensaml.DefaultBootstrap;
import org.w3c.dom.Node;

public class RegistrySAMLServlet extends SOAPServlet {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RegistrySAMLServlet.class);
	private it.cnr.icar.eric.common.BindingUtility bu = it.cnr.icar.eric.common.BindingUtility.getInstance();

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		init();
	}

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 * It is slightly adapted to log adequate messages in case of fatal errors.
	 */

	public void init() throws ServletException {
		try {
			boolean initCacheOnServerInit = Boolean.valueOf(
					RegistryProperties.getInstance().getProperty("eric.server.cache.initCacheOnServerInit", "true"))
					.booleanValue();

			if (initCacheOnServerInit) {
				// Initialize cache so first client does not have to wait.
				ServerCache.getInstance().initialize();
			}

			// extra initialization issues for opensaml library
			DefaultBootstrap.bootstrap();

		} catch (Exception ex) {
			throw new ServletException(ServerResourceBundle.getInstance().getString(
					"message.registrySOAPServletInitFailed", new Object[] { ex.getMessage() }));

		}
	}

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 * It informs the user about GET requests and the corresponding URL for Web
	 * Access.
	 */

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/plain");

		String scheme = request.getScheme();
		String serverName = request.getServerName();

		String ericName = RegistryProperties.getInstance().getProperty("eric.name", "eric");

		int serverPort = request.getServerPort();

		StringBuffer sb = new StringBuffer();

		sb.append(scheme).append("://").append(serverName).append(':');
		sb.append(serverPort);
		sb.append('/');
		sb.append(ericName);
		sb.append("/registry/thin/browser.jsp");

		String url = sb.toString();

		PrintWriter wt = response.getWriter();

		wt.println(ServerResourceBundle.getInstance().getString("message.urlForSOAP"));
		wt.println(ServerResourceBundle.getInstance().getString("message.urlForWebAccess", new Object[] { url }));

		wt.flush();
		wt.close();
	}

	/*
	 * doPost is taken from the super class, where the SOAPMessage is generated
	 * from the incoming HTTP request; the main message to respond on this
	 * request is "onMessage"
	 * 
	 * the method is taken from RgistrySOAPServlet and adapted to the needs of
	 * the SAML-based request handling
	 */

	@SuppressWarnings("unchecked")
	public SOAPMessage onMessage(SOAPMessage msg, HttpServletRequest req, HttpServletResponse resp) {

		SOAPMessage soapResponse = null;
		SOAPHeader soapHeader = null;

		try {

			// extract SOAP related parts from incoming SOAP message

			// set 'soapHeader' variable ASAP (before "firstly")
			SOAPPart soapPart = msg.getSOAPPart();
			SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
			SOAPBody soapBody = soapEnvelope.getBody();
			soapHeader = soapEnvelope.getHeader();

			/****************************************************************
			 * 
			 * REPOSITORY ITEM HANDLING (IN) REPOSITORY ITEM HANDLING (IN)
			 * 
			 ***************************************************************/

			// Firstly we put save the attached repository items in a map

			HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
			Iterator<AttachmentPart> apIter = msg.getAttachments();

			while (apIter.hasNext()) {
				AttachmentPart ap = apIter.next();

				// Get the content for the attachment
				RepositoryItem ri = processIncomingAttachment(ap);
				idToRepositoryItemMap.put(ri.getId(), ri);
			}

			/****************************************************************
			 * 
			 * LOGGING (IN) LOGGING (IN) LOGGING (IN) LOGGING (IN)
			 * 
			 ***************************************************************/

			// Log received message

			if (log.isTraceEnabled()) {
				// Warning! BAOS.toString() uses platform's default encoding
				ByteArrayOutputStream msgOs = new ByteArrayOutputStream();
				msg.writeTo(msgOs);
				msgOs.close();
				log.trace("incoming message:\n" + msgOs.toString());
			}

			/****************************************************************
			 * 
			 * MESSAGE VERIFICATION (IN) MESSAGE VERIFICATION (IN)
			 * 
			 ***************************************************************/
			CredentialInfo credentialInfo = new CredentialInfo();

			WSS4JSecurityUtilSAML.verifySOAPEnvelopeOnServerSAML(soapEnvelope, credentialInfo);

			/****************************************************************
			 * 
			 * RS:REQUEST HANDLING RS:REQUEST HANDLING RS:REQUEST
			 * 
			 ***************************************************************/

			// The ebXML registry request is the only element in the SOAPBody

			StringWriter requestXML = new StringWriter(); // The request as an
															// XML String
			String requestRootElement = null;

			Iterator<?> iter = soapBody.getChildElements();
			int i = 0;

			while (iter.hasNext()) {
				Object obj = iter.next();

				if (!(obj instanceof SOAPElement)) {
					continue;
				}

				if (i++ == 0) {
					SOAPElement elem = (SOAPElement) obj;
					Name name = elem.getElementName();
					requestRootElement = name.getLocalName();

					StreamResult result = new StreamResult(requestXML);
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer trans = tf.newTransformer();
					trans.transform(new DOMSource(elem), result);
				} else {
					throw new RegistryException(ServerResourceBundle.getInstance().getString("message.invalidRequest"));
				}
			}

			if (requestRootElement == null) {
				throw new RegistryException(ServerResourceBundle.getInstance().getString(
						"message.noebXMLRegistryRequest"));
			}

            // unmarshalling request to message
			Object message = bu.getRequestObject(requestRootElement, requestXML.toString());
			
            if (message instanceof JAXBElement<?>) {
            	// If Element; take ComplexType from Element
            	message = ((JAXBElement<?>) message).getValue();
            }


			SAMLRequest request = new SAMLRequest(req, credentialInfo.assertion, message, idToRepositoryItemMap);

			Response response = request.process();

			/****************************************************************
			 * 
			 * RESPONSE HANDLING RESPONSE HANDLING RESPONSE HANDLING
			 * 
			 ***************************************************************/

			soapResponse = createResponseSOAPMessage(response);

			if (response.getIdToRepositoryItemMap().size() > 0
					&& (response.getMessage().getStatus()
							.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success))) {

				idToRepositoryItemMap = response.getIdToRepositoryItemMap();
				Iterator<String> mapKeysIter = idToRepositoryItemMap.keySet().iterator();

				while (mapKeysIter.hasNext()) {
					String id = mapKeysIter.next();
					RepositoryItem repositoryItem = (RepositoryItem) idToRepositoryItemMap.get(id);

					String cid = WSS4JSecurityUtilSAML.convertUUIDToContentId(id);
					DataHandler dh = repositoryItem.getDataHandler();
					AttachmentPart ap = soapResponse.createAttachmentPart(dh);
					ap.setContentId(cid);
					soapResponse.addAttachmentPart(ap);

					if (log.isTraceEnabled()) {
						log.trace("adding attachment: contentId=" + id);
					}
				}
			}

		} catch (Throwable t) {
			// Do not log ObjectNotFoundException as it clutters the log
			if (!(t instanceof ObjectNotFoundException)) {
				log.error(
						ServerResourceBundle.getInstance().getString("message.CaughtException",
								new Object[] { t.getMessage() }), t);
				Throwable cause = t.getCause();
				while (cause != null) {
					log.error(
							ServerResourceBundle.getInstance().getString("message.CausedBy",
									new Object[] { cause.getMessage() }), cause);
					cause = cause.getCause();
				}
			}

			soapResponse = createFaultSOAPMessage(t, soapHeader);
		}

		if (log.isTraceEnabled()) {
			try {
				ByteArrayOutputStream rspOs = new ByteArrayOutputStream();
				soapResponse.writeTo(rspOs);
				rspOs.close();
				// Warning! BAOS.toString() uses platform's default encoding
				log.trace("response message:\n" + rspOs.toString());
			} catch (Exception e) {
				log.error(
						ServerResourceBundle.getInstance().getString("message.FailedToLogResponseMessage",
								new Object[] { e.getMessage() }), e);
			}
		}
		return soapResponse;
	}

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 * The SAML-based Servlet returns X.509 certificate base SOAP messages.
	 * 
	 */

	private SOAPMessage createFaultSOAPMessage(java.lang.Throwable e, SOAPHeader sh) {
		SOAPMessage msg = null;

		if (log.isDebugEnabled()) {
			log.debug("Creating Fault SOAP Message with Throwable:", e);
		}

		try {
			// Will this method be "legacy" ebRS 3.0 spec-compliant and
			// return a URN as the <faultcode/> value? Default expectation
			// is of a an older client. Overridden to instead be SOAP
			// 1.1-compliant and return a QName as the faultcode value when
			// we know (for sure) client supports new approach.
			boolean legacyFaultCode = true;

			// get SOAPHeaderElement list from the received message
			// TODO: if additional capabilities are needed, move code to
			// elsewhere
			if (null != sh) {
				Iterator<?> headers = sh.examineAllHeaderElements();
				while (headers.hasNext()) {
					Object obj = headers.next();

					// confirm expected Iterator content
					if (obj instanceof SOAPHeaderElement) {
						SOAPHeaderElement header = (SOAPHeaderElement) obj;
						Name headerName = header.getElementName();

						// check this SOAP header for relevant capability
						// signature
						if (headerName.getLocalName().equals(BindingUtility.SOAP_CAPABILITY_HEADER_LocalName)
								&& headerName.getURI().equals(BindingUtility.SOAP_CAPABILITY_HEADER_Namespace)
								&& header.getValue().equals(BindingUtility.SOAP_CAPABILITY_ModernFaultCodes)) {
							legacyFaultCode = false;
							// only interested in one client capability
							break;
						}
					}
				}
			}

			msg = MessageFactory.newInstance().createMessage();
			SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
			SOAPFault fault = msg.getSOAPBody().addFault();

			// set faultCode
			String exceptionName = e.getClass().getName();

			// TODO: SAAJ 1.3 has introduced preferred QName interfaces
			Name name = env.createName(exceptionName, "ns1", BindingUtility.SOAP_FAULT_PREFIX);
			fault.setFaultCode(name);
			if (legacyFaultCode) {
				// we now have an element child, munge its text (hack alert)
				Node faultCode = fault.getElementsByTagName("faultcode").item(0);

				// Using Utility.setTextContent() implementation since Java
				// WSDP 1.5 (containing an earlier DOM API) does not
				// support Node.setTextContent().
				Utility.setTextContent(faultCode, BindingUtility.SOAP_FAULT_PREFIX + ":" + exceptionName);
			}

			// set faultString
			String errorMsg = e.getMessage();
			if (errorMsg == null) {
				errorMsg = "NULL";
			}
			fault.setFaultString(errorMsg);

			// create faultDetail with one entry
			Detail det = fault.addDetail();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String str = sw.toString();

			name = env.createName("StackTrace", "rs", "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0");

			DetailEntry de = det.addDetailEntry(name);
			de.setValue(str);
			// de.addTextNode(str);

			// TODO: Need to put baseURL for this registry here

			msg.saveChanges();

		} catch (SOAPException ex) {
			log.warn(ex, ex);
			// otherwise ignore the problem updating part of the message
		}

		return msg;
	}

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 * The SAML-based Servlet returns X.509 certificate base SOAP messages.
	 * 
	 */

	private SOAPMessage createResponseSOAPMessage(Object obj) {

		SOAPMessage msg = null;

		try {
			RegistryResponseType ebRegistryResponseType = null;

			if (obj instanceof it.cnr.icar.eric.server.interfaces.Response) {
				Response r = (Response) obj;
				ebRegistryResponseType = r.getMessage();

			} else if (obj instanceof java.lang.Throwable) {
				Throwable t = (Throwable) obj;
				ebRegistryResponseType = it.cnr.icar.eric.server.common.Utility.getInstance()
						.createRegistryResponseFromThrowable(t, "RegistrySOAPServlet", "Unknown");
			}

			// Now add resp to SOAPMessage
			StringWriter sw = new StringWriter();
			// javax.xml.bind.Marshaller marshaller =
			// bu.rsFac.createMarshaller();
			javax.xml.bind.Marshaller marshaller = bu.getJAXBContext().createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			if (ebRegistryResponseType.getClass() == RegistryResponseType.class) {
				// if ComplexType is explicit, wrap it into Element
				// only RegistryResponeType is explicit -> equal test instead of
				// isinstance
				JAXBElement<RegistryResponseType> ebRegistryResponse = bu.rsFac
						.createRegistryResponse(ebRegistryResponseType);
				marshaller.marshal(ebRegistryResponse, sw);

			} else {
				// if ComplexType is anonymous, it can be marshalled directly
				marshaller.marshal(ebRegistryResponseType, sw);
			}

			// Now get the RegistryResponse as a String
			String respStr = sw.toString();

			// Use Unicode (utf-8) to getBytes (server and client). Rely on
			// platform default encoding is not safe.
			InputStream soapStream = it.cnr.icar.eric.server.common.Utility.getInstance()
					.createSOAPStreamFromRequestStream(new ByteArrayInputStream(respStr.getBytes("utf-8")));

			boolean signRequired = Boolean.valueOf(
					RegistryProperties.getInstance().getProperty("eric.interfaces.soap.signedResponse")).booleanValue();

			msg = it.cnr.icar.eric.server.common.Utility.getInstance().createSOAPMessageFromSOAPStream(soapStream);

			if (signRequired) {

				AuthenticationServiceImpl auService = AuthenticationServiceImpl.getInstance();
				PrivateKey privateKey = auService.getPrivateKey(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR,
						AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
				java.security.cert.Certificate[] certs = auService
						.getCertificateChain(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);

				CredentialInfo credentialInfo = new CredentialInfo(null, (X509Certificate) certs[0], certs, privateKey,
						null);

				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				WSS4JSecurityUtilSAML.signSOAPEnvelopeOnServerBST(se, credentialInfo);

			}

			// msg.writeTo(new FileOutputStream(new
			// File("signedResponse.xml")));
			soapStream.close();
		} catch (IOException e) {
			log.warn(e, e);
			// otherwise ignore the problem updating part of the message
		} catch (SOAPException e) {
			log.warn(e, e);
			// otherwise ignore the problem updating part of the message
		} catch (javax.xml.bind.JAXBException e) {
			log.warn(e, e);
			// otherwise ignore the problem updating part of the message
		} catch (ParseException e) {
			log.warn(e, e);
			// otherwise ignore the problem updating part of the message
		} catch (RegistryException e) {
			log.warn(e, e);
			// otherwise ignore the problem updating part of the message
		}

		return msg;
	}

	/**
	 * This method is a copy of the respective method from RegistrySOAPServlet.
	 */

	private RepositoryItem processIncomingAttachment(AttachmentPart ap) throws RegistryException {

		RepositoryItem ri = null;
		try {
			// ContentId is the id of the repositoryItem
			String id = WSS4JSecurityUtilSAML.convertContentIdToUUID(ap.getContentId());
			if (log.isInfoEnabled()) {
				log.info(ServerResourceBundle.getInstance().getString("message.ProcessingAttachmentWithContentId",
						new Object[] { id }));
			}

			if (log.isDebugEnabled()) {
				log.debug("Processing attachment (RepositoryItem):\n" + ap.getContent().toString());
			}

			DataHandler dh = ap.getDataHandler();
			ri = new RepositoryItemImpl(id, dh);

		} catch (SOAPException e) {

			RegistryException toThrow = new RegistryException(e);
			toThrow.initCause(e);
			throw toThrow;

		}

		return ri;
	}

}
