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
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.RegistryResponseHolder;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.UserNotFoundException;
import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.LifeCycleManagerFactory;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.interfaces.Response;
import it.cnr.icar.eric.server.interfaces.common.SessionManager;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.security.authentication.UserRegistrar;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * A Request encapsulates all aspects of an incoming client request to an ebXML
 * registry.
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class BSTRequest {

	private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
	private BindingUtility bu = BindingUtility.getInstance();
	private LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();

	private static AuthenticationServiceImpl authc = AuthenticationServiceImpl.getInstance();
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(BSTRequest.class);

	private CredentialInfo headerCredentialInfo;
	private HttpServletRequest request;
	private ServerRequestContext context = null;

	public BSTRequest(HttpServletRequest req, CredentialInfo headerCredentialInfo, Object message,
			HashMap<String, Object> idToRepositoryItemMap) throws RegistryException {

		this.request = req;
		this.headerCredentialInfo = headerCredentialInfo;

		String contextId = "Request." + message.getClass().getName();
		context = new ServerRequestContext(contextId, (RegistryRequestType) message);

		context.setRepositoryItemsMap(idToRepositoryItemMap);
		UserType user = getRequestUser();

		context.setUser(user);
	}

	/**
	 * Processes the Request by dispatching it to a service in the registry.
	 */
	@SuppressWarnings("static-access")
	public Response process() throws RegistryException {

		Response response = null;
		RegistryResponseType ebRegistryResponseType = null;

		HashMap<String,Object> responseRepositoryItemMap = new HashMap<String, Object>();

		RegistryRequestType message = context.getCurrentRegistryRequest();

		if (message instanceof AdhocQueryRequest) {

			AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) message;

			ebRegistryResponseType = qm.submitAdhocQuery(context);

			ResponseOptionType responseOption = ebAdhocQueryRequest
					.getResponseOption();
			ReturnType returnType = responseOption.getReturnType();

			if (returnType == returnType.LEAF_CLASS_WITH_REPOSITORY_ITEM) {
				responseRepositoryItemMap.putAll(context.getRepositoryItemsMap());
			}
		} else if (message instanceof ApproveObjectsRequest) {
			ebRegistryResponseType = lcm.approveObjects(context);
		} else if (message instanceof SetStatusOnObjectsRequest) {
			ebRegistryResponseType = lcm.setStatusOnObjects(context);
		} else if (message instanceof DeprecateObjectsRequest) {
			ebRegistryResponseType = lcm.deprecateObjects(context);
		} else if (message instanceof UndeprecateObjectsRequest) {
			ebRegistryResponseType = lcm.unDeprecateObjects(context);
		} else if (message instanceof RemoveObjectsRequest) {
			ebRegistryResponseType = lcm.removeObjects(context);
		} else if (message instanceof SubmitObjectsRequest) {
			ebRegistryResponseType = lcm.submitObjects(context);
		} else if (message instanceof UpdateObjectsRequest) {
			ebRegistryResponseType = lcm.updateObjects(context);
		} else if (message instanceof RelocateObjectsRequest) {
			ebRegistryResponseType = lcm.relocateObjects(context);
		} else {
			RegistryResponseHolder respHolder = lcm.extensionRequest(context);

			// Due to bad design few lines down we are idToRepositoryItemMap for
			// response attachment map
			// Following line is a workaround for that
			responseRepositoryItemMap = respHolder.getAttachmentsMap();
			ebRegistryResponseType = respHolder.getRegistryResponseType();
		}
		
		response = new Response(ebRegistryResponseType, responseRepositoryItemMap);

		return response;
	}

	/*
	 * Gets the current user associated with request.
	 * 
	 * Get the user from request header if it is signed. Otherwise get it from
	 * HttpSession. When all else fails use RegistryGuest as user.
	 */
	private UserType getRequestUser() throws RegistryException {

		RegistryRequestType message = context.getCurrentRegistryRequest();
		UserType user = null;

		if (SessionManager.getInstance().isSessionEstablished(request)) {
			user = SessionManager.getInstance().getUserFromSession(request);
		}

		if (user == null) {
			// User associated with Header signature overrides user associated
			// with any HttpSession
			// But does not change the user associated with HttpSession.
			if (headerCredentialInfo != null && headerCredentialInfo.cert != null) {
				try {
					user = authc.getUserFromCertificate(headerCredentialInfo.cert);
					user = getEffectiveUser(user);

				} catch (UserNotFoundException e) {

					if (message instanceof SubmitObjectsRequest) {
						user = UserRegistrar.getInstance().registerUser(headerCredentialInfo.cert,
								(SubmitObjectsRequest) message);
					} else {
						user = authc.registryGuest;
					}
				}
			} else {
				user = authc.registryGuest;

			}
			SessionManager.getInstance().establishSession(request, user, (RegistryRequestType) message);
		}

		// Map registryGuest user to registryOperator user if
		// noUserRegistrationRequired is true
		if (user == authc.registryGuest) {
			boolean noUserRegRequired = Boolean.valueOf(
					CommonProperties.getInstance().getProperty("eric.common.noUserRegistrationRequired", "false"))
					.booleanValue();
			if (noUserRegRequired) {
				user = authc.registryOperator;
			}
		}

		return user;
	}

	/*
	 * If requestor user has role of Intermediary then gets the actual User on
	 * whose behalf the requestor sent the request.
	 * 
	 * @returns the requestor if requestor does not have role of Intermediary or
	 * the user identified by special request slot if requestor does have role
	 * of Intermediary
	 */
	private UserType getEffectiveUser(UserType requestor) throws RegistryException {
		RegistryRequestType message = context.getCurrentRegistryRequest();
		UserType user = requestor;

		try {
			HashMap<String, Object> requestSlots = bu.getSlotsFromRequest(message);
			String userId = (String) requestSlots.get(BindingUtility.CANONICAL_URI_EFFECTIVE_REQUESTOR);

			if (userId != null) {
				boolean isIntermediary = (authc.hasIntermediaryRole(requestor) || authc
						.hasRegistryAdministratorRole(requestor));

				if (isIntermediary) {
					try {
						UserType u = (UserType) qm.getRegistryObject(context, userId, "User");
						if (u != null) {
							user = u;
						}
					} catch (ObjectNotFoundException e) {
						// Missing effective user, fall back to requestor
						userId = null;
					}
				}
			}
		} catch (JAXBException e) {
			throw new RegistryException(e);
		}

		return user;
	}
}
