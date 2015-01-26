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

import it.cnr.icar.eric.common.RegistryResponseHolder;
import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.LifeCycleManagerFactory;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.interfaces.Response;
import it.cnr.icar.eric.server.interfaces.common.SessionManager;
import it.cnr.icar.eric.server.saml.SAMLRegistrar;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.opensaml.saml2.core.Assertion;


/**
 * A SAML Request encapsulates all aspects of an incoming client SAML-based 
 * request to an ebXML registry. It is functionally derived from the Request
 * class of the ebXML Registry reference implementation
 */

public class SAMLRequest {

    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
        
    private static SAMLRegistrar subjectRegistrar = SAMLRegistrar.getInstance();

    private HttpServletRequest request;
    private ServerRequestContext context = null;
    
    public SAMLRequest(HttpServletRequest request, Assertion assertion, Object message, HashMap<String, Object> idToRepositoryItemMap) throws RegistryException {

		this.request = request;
	    
	    String contextId = "Request." + message.getClass().getName();
	    context = new ServerRequestContext(contextId, (RegistryRequestType)message);
	    context.setRepositoryItemsMap(idToRepositoryItemMap);
	    
	    UserType user = getRequestUser(assertion);
	    
	    context.setUser(user);        

    }
    
    /**
     * This method processes the request by dispatching it to a service 
     * in the registry. It is independent of the respective identity
     * handling as the corresponding user is detected already.
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Response process() throws RegistryException {
        
        Response response = null;
        RegistryResponseType ebRegistryResponseType = null;
        
        HashMap responseRepositoryItemMap = new HashMap();

        RegistryRequestType message = context.getCurrentRegistryRequest();
        
        if (message instanceof AdhocQueryRequest) {
        	
        	
            AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest)message;
            
            ebRegistryResponseType = qm.submitAdhocQuery(context);
            
            org.oasis.ebxml.registry.bindings.query.ResponseOptionType responseOption =
                ebAdhocQueryRequest.getResponseOption();
            ReturnType returnType = responseOption.getReturnType();
            
            if (returnType == ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM) {                               
                responseRepositoryItemMap.putAll(context.getRepositoryItemsMap());
            }
        } 
        else if (message instanceof ApproveObjectsRequest) {
            ebRegistryResponseType = lcm.approveObjects(context);
        } 
        else if (message instanceof SetStatusOnObjectsRequest) {
            ebRegistryResponseType = lcm.setStatusOnObjects(context);
        } 
        else if (message instanceof DeprecateObjectsRequest) {
            ebRegistryResponseType = lcm.deprecateObjects(context);
        } 
        else if (message instanceof UndeprecateObjectsRequest) {
            ebRegistryResponseType = lcm.unDeprecateObjects(context);
        } 
        else if (message instanceof RemoveObjectsRequest) {
            ebRegistryResponseType = lcm.removeObjects(context);
        } 
        else if (message instanceof SubmitObjectsRequest) {
            ebRegistryResponseType = lcm.submitObjects(context);
        } 
        else if (message instanceof UpdateObjectsRequest) {
            ebRegistryResponseType = lcm.updateObjects(context);
        }
        else if (message instanceof RelocateObjectsRequest) {
            ebRegistryResponseType = lcm.relocateObjects(context);
        }
        else {
            RegistryResponseHolder respHolder = lcm.extensionRequest(context);
            
            //Due to bad design few lines down we are idToRepositoryItemMap for response attachment map
            //Following line is a workaround for that
            responseRepositoryItemMap = respHolder.getAttachmentsMap();
            ebRegistryResponseType = respHolder.getRegistryResponseType();
        }

        response = new Response(ebRegistryResponseType, responseRepositoryItemMap);
        
        return response;
    }

    // this method retrieves the user of the current request either from an
    // existing HTTP session or from the provided SAML assertion; if the
    // user instance is locally not existing, it is created as a replica
    // from the respective remote user
    
    private UserType getRequestUser(Assertion assertion) throws RegistryException {
        
        UserType user = null;
        
        // try to determine the current user associated with this request
        // from the HTTP_SESSION_USER parameter        
        if (SessionManager.getInstance().isSessionEstablished(request)) {
            user = SessionManager.getInstance().getUserFromSession(request);
        }
        
        if (user == null) {            	
        	// try to get user from SAML assertion; if the user is not yet
        	// locally registered, create a replica from remote user
            user = subjectRegistrar.getUser(assertion);
            
            if (user == null) 
            	user = subjectRegistrar.registerUser(assertion);
        }
        
        if (user == null) {
        	// the authentication service is used to retrieve the registry guest        	
            AuthenticationServiceImpl authnService = AuthenticationServiceImpl.getInstance();
            user = authnService.registryGuest; 
        }
                
        return user;

    }
 
}
