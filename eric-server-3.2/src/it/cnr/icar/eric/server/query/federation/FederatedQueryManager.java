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
package it.cnr.icar.eric.server.query.federation;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Manages all Federations that this Registry is a member of.
 * This is a long-lived class. It creates a new instance of a FederatedQueryProcessor
 * to process each federated query.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryManager {
    private static FederatedQueryManager instance = null;

    BindingUtility bu = BindingUtility.getInstance();
    AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();    
        
    HashMap<String, HashSet<RegistryType>> federationCache = null;

    //A context only to be used for internal usage by FederatedQuerymanager
    ServerRequestContext internalContext = null;
    
    protected FederatedQueryManager() {
        try {
            internalContext = new ServerRequestContext("FederatedQueryManager:internalContext", null);
            internalContext.setUser(ac.registryOperator);        
        } catch (RegistryException ex) {
            throw new UndeclaredThrowableException(ex);
        }        
    }
    
    public synchronized static FederatedQueryManager getInstance() {
        if (instance == null) {
            instance = new FederatedQueryManager();
        }

        return instance;
    }
    
    /**
     * Submits an AdhocQueryRequest to all Registries thare are members of specified federation.
     *
     * @param user
     * @param adhocQueryRequest the request sent to every registry ias a parrallel distrubuted query.
     */
    public AdhocQueryResponse submitAdhocQuery(ServerRequestContext context)
    throws RegistryException {
        AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest)context.getCurrentRegistryRequest();
        @SuppressWarnings("unused")
		UserType user = context.getUser();
        getFederationCache();
        
        //This optional parameter specifies the id of the target Federation for a
        //federated query in case the registry is a member of multiple federations. 
        //In the absence of this parameter a registry must route the federated query to all 
        //federations of which it is a member.
        String federationId = ebAdhocQueryRequest.getFederation();        
        HashSet<RegistryType> federationMembers = null;
        try {
            // Get members for federation(s) specified in request
            federationMembers = getFederationMembers(federationId);
        } catch (InvalidRequestException e) {
            //It is possible that the federation was added since cache was initialized.
            //Try updating cache and retrying once
            federationCache = null;
            getFederationCache();
            try {
                federationMembers = getFederationMembers(federationId);
            } catch (InvalidRequestException e1) {
            }
        }
        
        FederatedQueryProcessor fqp = new FederatedQueryProcessor(federationMembers);
        
        return fqp.submitAdhocQuery(context);        
    }
    
    /*
     * Initializes the Federation configuration cache
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private HashMap<String, HashSet<RegistryType>> getFederationCache() throws RegistryException {
        
        try {
            if (federationCache == null) {
                federationCache = new HashMap<String, HashSet<RegistryType>>();

                //TODO: Make this a FederationCache call
                String query = "SELECT f.* FROM Federation f";

                AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest(query);
                internalContext.pushRegistryRequest(queryRequest);
                AdhocQueryResponse queryResp = null;
                try {
                    queryResp = qm.submitAdhocQuery(internalContext);
                } finally {
                    internalContext.popRegistryRequest();
                }
                List<JAXBElement<? extends IdentifiableType>> federations = queryResp.getRegistryObjectList().getIdentifiable();

                Iterator<JAXBElement<? extends IdentifiableType>> iter = federations.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof FederationType) {
                        FederationType federation = (FederationType)obj;
                        
                        //TODO: Make this a FederationCache call or at least a PreparedStatement
                        query = "SELECT r.* FROM Registry r, Federation f, Association a WHERE a.associationType = '" +
                            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember +
                            "' AND a.targetObject = r.id AND a.sourceObject = '" + federation.getId() +
                            "'";
                        queryRequest = bu.createAdhocQueryRequest(query);
                        internalContext.pushRegistryRequest(queryRequest);
                        try {
                            queryResp = qm.submitAdhocQuery(internalContext);
                        } finally {
                            internalContext.popRegistryRequest();
                        }
                        List _members = queryResp.getRegistryObjectList().getIdentifiable();
                        HashSet<RegistryType> members = new HashSet<RegistryType>();
                        members.addAll(_members);
                        
                        //If a Federation has no members then add this registry to the Federation
                        if (members.size() == 0) {
                            members.add(ServerCache.getInstance().getRegistry(internalContext));
                        }
                        federationCache.put(federation.getId(), members);
                    }
                }
            }                            
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return federationCache;
    }
    
    /**
     * Gets the Registrys that are members of specified. 
     *
     * @param federation The specified Federation. A null value implies all Federations this registry is a member of.
     */
    private HashSet<RegistryType> getFederationMembers(String federationId) throws RegistryException, InvalidRequestException {
        HashSet<RegistryType> members = new HashSet<RegistryType>();
        
        if ((federationId != null) && (federationId.length() > 0)) {
            //federation specified so only get members for specified federation
            HashSet<RegistryType> _members = federationCache.get(federationId);
            if (_members == null) {
                throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.noFederationConfigured",
                        new Object[]{federationId}));
            } else {
                members.addAll(_members);
            }
        } else {
            //federation not specified so get members for all federations. Bug: Allows duplicate members
            Iterator<HashSet<RegistryType>> iter = federationCache.values().iterator();
            while (iter.hasNext()) {
                HashSet<RegistryType> _members = iter.next();
                members.addAll(_members);
            }
        }
        
        return members;
    }
    
}
