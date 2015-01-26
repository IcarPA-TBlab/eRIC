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
package it.cnr.icar.eric.server.interfaces.rest;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.UnimplementedException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.interfaces.common.SessionManager;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.RepositoryItemListType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ObjectFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * @author Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class URLHandler {
	private static final Log log = LogFactory.getLog(URLHandler.class);
	private BindingUtility bu = BindingUtility.getInstance();
	private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();

	HttpServletRequest request = null;
	HttpServletResponse response = null;
	protected static TransformerFactory xFormerFactory;
	private String baseUrl = null;
	protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
	protected UserType currentUser = null;

	protected URLHandler() {
	};

	URLHandler(HttpServletRequest request, HttpServletResponse response) throws RegistryException {

		this.request = request;
		this.response = response;

		// First see if user context can be gotten from cert is SSLContext for
		// request
		getUserFromRequest();

		if (currentUser == null) {
			if (SessionManager.getInstance().isSessionEstablished(request)) {
				currentUser = SessionManager.getInstance().getUserFromSession(request);
			}
		}

		if (currentUser == null) {
			// Force authentication if so configured.
			boolean samlMode = Boolean.valueOf(
					CommonProperties.getInstance().getProperty("eric.common.security.samlMode", "false"))
					.booleanValue();
			boolean forceAuthentication = Boolean.valueOf(
					CommonProperties.getInstance().getProperty("eric.common.security.forceAuthentication", "false"))
					.booleanValue();

			if (samlMode && forceAuthentication) {
				// TODO: Need to redirect to AM Login screen
			}

			// See if user can be gotten from pricipal
			currentUser = findUserByPrincipal(request.getUserPrincipal());

			boolean establishSession = true;
			// If the UserType is registryGuest. there was no authentication,
			// and,
			// thus, there is no need to create a session
			if (currentUser == AuthenticationServiceImpl.getInstance().registryGuest) {
				establishSession = false;
			}
			SessionManager.getInstance().establishSession(request, currentUser, establishSession);
		}

		if (xFormerFactory == null) {
			try {
				xFormerFactory = TransformerFactory.newInstance();
			} catch (Throwable t) {
				log.error(ServerResourceBundle.getInstance().getString("message.ProblemInitializingTransformerFactory",
						new Object[] { t.getMessage() }));
			}
		}
	}

	private void getUserFromRequest() {
		Object certObj = request.getAttribute("javax.servlet.request.X509Certificate");

		if (certObj != null) {
			Certificate[] certs = (Certificate[]) certObj;

			try {
				if (certs.length > 0) {
					currentUser = ac.getUserFromCertificate((X509Certificate) certs[0]);
				}
			} catch (RegistryException e) {
				return;
			}
		}
	}

	/**
	 * Processes a Get Request
	 */
	void processGetRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException,
			ObjectNotFoundException {
		throw new UnimplementedException(ServerResourceBundle.getInstance().getString("message.unimplementedGETMethod"));
	}

	/**
	 * Processes a POST Request
	 */
	void processPostRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException,
			ObjectNotFoundException {
		throw new UnimplementedException(ServerResourceBundle.getInstance()
				.getString("message.unimplementedPOSTMethod"));
	}

	/**
	 * Submit the SQL query to the registry as the specified user.
	 * 
	 * @param queryString
	 * @param user
	 * @throws RegistryException
	 * @return A List of IdentifiableType objects representing the registry
	 *         objects that match the query.
	 * @see #findUserByPrincipal(Principal)
	 */
	List<RegistryObjectType> submitQueryAs(String queryString, UserType user) throws RegistryException {
		try {
			AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest(queryString);
			ObjectFactory queryFac = BindingUtility.getInstance().queryFac;
			ResponseOptionType responseOption = queryFac.createResponseOptionType();
			responseOption.setReturnComposedObjects(true);
			responseOption.setReturnType(ReturnType.LEAF_CLASS);
			req.setResponseOption(responseOption);

			ServerRequestContext context = new ServerRequestContext("URLHandler.submitQueryAs", req);
			context.setUser(user);
			AdhocQueryResponse ebAdhocQueryResponse = qm.submitAdhocQuery(context);

			/*
			 * getIdentifiable() returns JAXBelement<? extends IdentifiableType>
			 * 
			 * return should be List of ComplexType
			 */

			RegistryObjectListType ebRegistryObjectListType = ebAdhocQueryResponse.getRegistryObjectList();
			// get List of Element
			List<JAXBElement<? extends IdentifiableType>> results = ebRegistryObjectListType.getIdentifiable();
			// prepare List of ComplexType
			List<RegistryObjectType> ebRegistryObjectTypeList = new ArrayList<RegistryObjectType>();

			Iterator<JAXBElement<? extends IdentifiableType>> ebRegistryObjectListTypeIter = results.iterator();
			while (ebRegistryObjectListTypeIter.hasNext()) {
				// take ComplexType from Element
				ebRegistryObjectTypeList.add((RegistryObjectType) ebRegistryObjectListTypeIter.next().getValue());
			}

			// List of ComplexType
			return ebRegistryObjectTypeList;
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	@SuppressWarnings("unchecked")
	List<IdentifiableType> invokeParameterizedQuery(ServerRequestContext context, String queryId, Map<String, String> queryParams,
			UserType user, int startIndex, int maxResults) throws RegistryException {

		List<IdentifiableType> ebRegistryObjectTypeList = null;

		try {
			AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
			req.setStartIndex(BigInteger.valueOf(startIndex));
			req.setMaxResults(BigInteger.valueOf(maxResults));

			Map<String, String> slotsMap = new HashMap<String, String>();
			slotsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, queryId);
			if ((queryParams != null) && (queryParams.size() > 0)) {
				slotsMap.putAll(queryParams);
			}
			BindingUtility.getInstance().addSlotsToRequest(req, slotsMap);

			// Now execute the query
			HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
			context.setRepositoryItemsMap(idToRepositoryItemMap);

			boolean doCommit = false;
			try {
				context.pushRegistryRequest(req);
				AdhocQueryResponse ebAdhocQueryResponse = qm.submitAdhocQuery(context);
				bu.checkRegistryResponse(ebAdhocQueryResponse);
				ebRegistryObjectTypeList = (List<IdentifiableType>) bu.getIdentifiableTypeList(ebAdhocQueryResponse
						.getRegistryObjectList());
				doCommit = true;
			} finally {
				context.popRegistryRequest();
				try {
					closeContext(context, doCommit);
				} catch (Exception ex) {
					log.error(ex, ex);
				}
			}
		} catch (JAXBException e) {
			throw new RegistryException(e);
		} catch (JAXRException e) {
			throw new RegistryException(e);
		}

		return ebRegistryObjectTypeList;
	}

	protected void closeContext(ServerRequestContext context, boolean doCommit) throws Exception {
		if (doCommit) {
			context.commit();
		} else {
			context.rollback();
		}
	}

	/**
	 * Get the User object that is associated with a Slot named
	 * 'urn:oasis:names:tc:ebxml-regrep:3.0:rim:User:principalName' whose value
	 * matches the principal name specified. If the principal is
	 * <code>null</code>, use the value of the
	 * <code>eric.security.anonymousUserPrincipalName</code> property as the
	 * principal name. If the property is not set, or no User is found, return
	 * the RegistryGuest user.
	 * 
	 * @param principal
	 * @throws JAXRException
	 * @return
	 */
	protected UserType findUserByPrincipal(Principal principal) throws RegistryException {
		try {
			UserType user = null;
			if (principal == null) {
				String principalName = CommonProperties.getInstance().getProperty(
						"eric.security.anonymousUserPrincipalName");
				if (principalName != null) {
					user = findUserByPrincipalName(principalName);
				}
			} else {
				user = findUserByPrincipalName(principal.getName());
			}
			if (user == null) {
				user = AuthenticationServiceImpl.getInstance().registryGuest;
			}
			return user;
		} catch (RegistryException re) {
			throw re;
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	/**
	 * Get the User object that is associated with a Slot named
	 * 'urn:oasis:names:tc:ebxml-regrep:3.0:rim:User:principalName' whose value
	 * matches the principal name specified. If no User is found, return
	 * <code>null</code>.
	 * <p/>
	 * This method must query the persitance manager directly so as to avoid the
	 * authorization restrictions imposed by the QueryManager.
	 * 
	 * @param principalName
	 * @throws JAXRException
	 * @return
	 */
	protected UserType findUserByPrincipalName(String principalName) throws RegistryException {
		UserType user = null;
		ServerRequestContext context = null;

		try {
			context = new ServerRequestContext("URLHandler.findUserByPrincipalName", null);

			String sqlQuery = "SELECT u.* " + "FROM user_ u, slot s " + "WHERE u.id = s.parent AND s.name_='"
					+ BindingUtility.CANONICAL_PRINCIPAL_NAME_URI + "' AND value='" + principalName + "'";
			ResponseOptionType ebResponseOptionType = BindingUtility.getInstance().queryFac.createResponseOptionType();
			ebResponseOptionType.setReturnComposedObjects(true);
			ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
			ArrayList<Object> objectRefs = new ArrayList<Object>();
			PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
			Iterator<IdentifiableType> results = pm
					.executeSQLQuery(context, sqlQuery, ebResponseOptionType, "RegistryObject", objectRefs).iterator();
			while (results.hasNext()) {
				user = (UserType) results.next();
				break;
			}
		} catch (RegistryException re) {
			context.rollback();
			throw re;
		} catch (Exception e) {
			context.rollback();
			throw new RegistryException(e);
		}

		context.commit();
		return user;
	}

	private synchronized Transformer createTransformer() throws TransformerConfigurationException,
			MalformedURLException, IOException {
		Transformer xFormer = null;
		if (xFormerFactory == null) {
			xFormer = null;
		} else {
			// TODO: Replace next line with server.common.Utility.getBaseURL();
			URL url = new URL(getBaseUrl() + "?interface=QueryManager&" + "method=getRepositoryItem&"
					+ "param-id=urn:uuid:82239fb0-c075-44e3-ac37-a8ea69383907");
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			StreamSource source = new StreamSource(is);
			xFormer = xFormerFactory.newTransformer(source);
		}
		return xFormer;
	}

	void transformResponse(StringWriter sWriter, Writer out, HttpServletResponse response, String flavor)
			throws IOException {
		if (sWriter == null || out == null) {
			throw new IllegalArgumentException(ServerResourceBundle.getInstance().getString(
					"message.nullTransformResponseParammeter", new Object[] { sWriter, out }));
		}
		try {
			Transformer transformer = createTransformer();
			if (transformer == null) {
				out.write(sWriter.toString());
			} else {
				String responseStr = sWriter.toString();
				// The next two string replacements take care of some strange
				// processing behavior by Xalan
				responseStr = responseStr.replaceFirst("xmlns=", "xmlns:rim=");
				responseStr = responseStr.replaceAll("<LocalizedString", " <LocalizedString");
				StreamSource inputSrc = new StreamSource(new StringReader(responseStr));
				StreamResult sResult = new StreamResult(out);
				response.setContentType(flavor);
				transformer.transform(inputSrc, sResult);
			}
		} catch (Throwable t) {
			log.error(
					ServerResourceBundle.getInstance().getString("message.ProblemTransformingResponseToNonXml",
							new Object[] { t.getMessage() }), t);
			response.setContentType("text/xml; charset=UTF-8");
			out.write(sWriter.toString());
		}
	}

	protected String getBaseUrl() {
		if (baseUrl == null) {
			String requestUri = request.getRequestURI();
			@SuppressWarnings("unused")
			String servletPath = request.getServletPath();
			String scheme = request.getScheme();
			String serverName = request.getServerName();
			@SuppressWarnings("unused")
			String queryString = request.getQueryString();
			int serverPort = request.getServerPort();
			StringBuffer sb = new StringBuffer();
			sb.append(scheme).append("://").append(serverName).append(':');
			sb.append(serverPort);
			sb.append(requestUri);
			baseUrl = sb.toString();
			log.info(ServerResourceBundle.getInstance().getString("message.BaseURL", new Object[] { baseUrl }));
		}

		return baseUrl;
	}

	/**
	 * Writes XML RepositoryItems as a RepositoryItemList. Ignores any other
	 * type of RepositoryItem.s
	 */
	void writeRepositoryItems(List<?> eos) throws IOException, RegistryException, ObjectNotFoundException {
		ServerRequestContext context = new ServerRequestContext("URLHandler.writeRepositoryItem", null);
		ServletOutputStream sout = response.getOutputStream();
		boolean doCommit = false;
		try {
			RepositoryItemListType ebRepositoryItemListType = bu.lcmFac.createRepositoryItemListType();

			Iterator<?> iter = eos.iterator();
			while (iter.hasNext()) {
				ExtrinsicObjectType eo = (ExtrinsicObjectType) iter.next();
				String id = eo.getId();

				RepositoryItem ri = QueryManagerFactory.getInstance().getQueryManager().getRepositoryItem(context, id);

				if (ri == null) {
					throw new ObjectNotFoundException(id, ServerResourceBundle.getInstance().getString(
							"message.repositoryItem"));
				} else {
					if (eo.getMimeType().equals("text/xml")) {
						DataHandler dataHandler = ri.getDataHandler();
						InputStream fStream = dataHandler.getInputStream();

						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document document = builder.parse(fStream);
						Element rootElement = document.getDocumentElement();

						ebRepositoryItemListType.getAny().add(rootElement);
					}
				}
			}
			// javax.xml.bind.Marshaller marshaller =
			// bu.lcmFac.createMarshaller();
			javax.xml.bind.Marshaller marshaller = bu.getJAXBContext().createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			JAXBElement<RepositoryItemListType> ebRepositoryItemList = bu.lcmFac
					.createRepositoryItemList(ebRepositoryItemListType);
			marshaller.marshal(ebRepositoryItemList, sout);
			doCommit = true;

		} catch (JAXBException e) {
			throw new RegistryException(e);
		} catch (ParserConfigurationException e) {
			throw new RegistryException(e);
		} catch (SAXException e) {
			throw new RegistryException(e);
		} finally {
			if (sout != null) {
				sout.close();
				sout = null;
			}
			try {
				closeContext(context, doCommit);
			} catch (Exception ex) {
				log.error(ex, ex);
			}
		}
	}

	void writeRepositoryItem(ExtrinsicObjectType eo) throws IOException, RegistryException, ObjectNotFoundException {
		String id = eo.getId();
		ServerRequestContext context = new ServerRequestContext("URLHandler.writeRepositoryItem", null);

		try {
			RepositoryItem ri = QueryManagerFactory.getInstance().getQueryManager().getRepositoryItem(context, id);

			if (ri == null) {
				throw new ObjectNotFoundException(id, ServerResourceBundle.getInstance().getString(
						"message.repositoryItem"));
			} else {
				response.setContentType(eo.getMimeType());

				DataHandler dataHandler = ri.getDataHandler();
				ServletOutputStream sout = response.getOutputStream();
				InputStream fStream = dataHandler.getInputStream();
				int bytesSize = fStream.available();
				byte[] b = new byte[bytesSize];
				fStream.read(b);
				sout.write(b);
				sout.close();
			}
			context.commit();
			context = null;
		} finally {
			if (context != null) {
				context.rollback();
			}

		}
	}

	void writeRegistryObject(RegistryObjectType ebRegistryObjectType) throws IOException, RegistryException,
			ObjectNotFoundException {
		PrintWriter out = null;
		try {
			log.info(ServerResourceBundle.getInstance().getString("message.FoundRegistryObjectWithId",
					new Object[] { ebRegistryObjectType.getId() }));
			response.setContentType("text/xml; charset=UTF-8");

			out = response.getWriter();

			// Marshaller marshaller = bu.rimFac.createMarshaller();
			Marshaller marshaller = bu.getJAXBContext().createMarshaller();

			JAXBElement<RegistryObjectType> ebRegistryObject = bu.rimFac.createRegistryObject(ebRegistryObjectType);

			marshaller.marshal(ebRegistryObject, out);
		} catch (JAXBException e) {
			throw new RegistryException(e);
		} finally {
			// silent procedure
			if (out != null) {
				out.close();
			}
		}
	}

	protected String getClosestValue(InternationalStringType is) {
		String str = null;
		List<LocalizedStringType> l = is.getLocalizedString();
		if (l != null && l.size() > 0) {
			str = (l.get(0)).getValue();
		}
		return str;
	}

}
