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

import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.RequestContext;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

//import it.cnr.icar.eric.common.CredentialInfo;
//import it.cnr.icar.eric.common.SOAPMessenger;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class QueryManagerSOAPProxy implements QueryManager {
	protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
	private static BindingUtility bu = BindingUtility.getInstance();
	@SuppressWarnings("unused")
	private String registryURL;
	@SuppressWarnings("unused")
	private CredentialInfo credentialInfo;
	private SOAPMessenger msgr;

	public QueryManagerSOAPProxy(String registryURL, CredentialInfo credentialInfo) {
		msgr = new SOAPMessenger(registryURL, credentialInfo);
	}

	public AdhocQueryResponse submitAdhocQuery(RequestContext context) throws RegistryException {

		try {
			RegistryResponseHolder resp = submitAdhocQueryInternal(context);
			RegistryResponseType ebRegistryResponseType = resp.getRegistryResponseType();

			return (AdhocQueryResponse) ebRegistryResponseType;
		} catch (RegistryException e) {
			throw e;
		}
	}

	private RegistryResponseHolder submitAdhocQueryInternal(RequestContext context) throws RegistryException {
		RegistryRequestType req = context.getCurrentRegistryRequest();
		RegistryResponseHolder resp = null;
		try {
			StringWriter sw = new StringWriter();
			// Marshaller marshaller = bu.queryFac.createMarshaller();
			Marshaller marshaller = bu.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(req, sw);
			
			resp = msgr.sendSoapRequest(sw.toString());

			RegistryResponseType ebResp = resp.getRegistryResponseType();
			bu.checkRegistryResponse(ebResp);
			context.setRepositoryItemsMap(resp.getAttachmentsMap());

			return resp;
		} catch (JAXBException e) {
			throw new RegistryException(e);
		} catch (RegistryException e) {
			throw e;
		} catch (JAXRException e) {
			throw new RegistryException(e);
		}
	}

	public RegistryObjectType getRegistryObject(RequestContext context, String id) throws RegistryException {
		return getRegistryObject(context, id, "RegistryObject");
	}

	@SuppressWarnings("unchecked")
	public RegistryObjectType getRegistryObject(RequestContext context, String id, String typeName)
			throws RegistryException {
		RegistryObjectType ebRegistryObjectType = null;
		try {
			typeName = Utility.getInstance().mapTableName(typeName);

			HashMap<String,String> queryParams = new HashMap<String, String>();
			queryParams.put("$id", id);
			queryParams.put("$tableName", typeName);
			AdhocQueryRequest ebAdhocQueryRequest = bu.createAdhocQueryRequest(
					"urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType", queryParams);

			context.pushRegistryRequest(ebAdhocQueryRequest);
			RegistryResponseHolder respHolder = submitAdhocQueryInternal(context);

			List<?> results = respHolder.getCollection();
			if (results.size() == 1) {
				ebRegistryObjectType = ((JAXBElement<RegistryObjectType>) results.get(0)).getValue();
			}
		} catch (RegistryException e) {
			throw e;
		} catch (JAXBException e) {
			throw new RegistryException(e);
		} finally {
			context.popRegistryRequest();
		}

		return ebRegistryObjectType;
	}

	public RepositoryItem getRepositoryItem(RequestContext context, String id) throws RegistryException {
		it.cnr.icar.eric.common.RepositoryItem repositoryItem = null;

		try {
			String queryStr = "SELECT * from ExtrinsicObject WHERE id='" + id + "'";
			AdhocQueryRequest ebAdhocQueryRequest = bu.createAdhocQueryRequest(queryStr);

			ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
			ebResponseOptionType.setReturnComposedObjects(true);
			ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
			ebAdhocQueryRequest.setResponseOption(ebResponseOptionType);

			context.pushRegistryRequest(ebAdhocQueryRequest);
			RegistryResponseHolder respHolder = submitAdhocQueryInternal(context);

			Map<String, Object> attachmentsMap = respHolder.getAttachmentsMap();
			if ((attachmentsMap != null) && attachmentsMap.containsKey(id)) {
				repositoryItem = new RepositoryItemImpl(id, (DataHandler) attachmentsMap.get(id));
			}
		} catch (RegistryException e) {
			throw e;
		} catch (JAXBException e) {
			throw new RegistryException(e);
		} finally {
			context.popRegistryRequest();
		}
		return repositoryItem;
	}

	public UserType getUser(X509Certificate cert) throws RegistryException {
		throw new RegistryException(resourceBundle.getString("message.unimplemented"));
	}

}
