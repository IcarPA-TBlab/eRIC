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

import it.cnr.icar.eric.client.xml.registry.BulkResponseImpl;
import it.cnr.icar.eric.client.xml.registry.ClientRequestContext;
import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.server.common.ConnectionManager;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.HashMap;
import java.util.Map;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import java.util.concurrent.BrokenBarrierException;
//import EDU.oswego.cs.dl.util.concurrent.BrokenBarrierException;
//import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import java.util.concurrent.CyclicBarrier;

/**
 * Handles dispatching of a federated query to a registry and processes its results.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryWorker implements Runnable {
    
    private static Log log = LogFactory.getLog(FederatedQueryWorker.class);
    BindingUtility bu = BindingUtility.getInstance();
    private final CyclicBarrier barrier;
    private final RegistryType registry;
    private final long timeout;
    private final UserType user;
    private final AdhocQueryRequest queryRequest;
    
    private AdhocQueryResponse response = null;    
    private Exception exception = null;
    private Map<String, Object> repositoryItemsMap = null;
    
    protected DeclarativeQueryManagerImpl dqm=null;
    protected Connection connection=null;
    
    
    /**
     * TODO Document me
     */
    FederatedQueryWorker(java.util.concurrent.CyclicBarrier barrier2,
    RegistryType destination,
    long timeout,
    UserType user,
    AdhocQueryRequest partAdhocQueryRequest) {
        this.barrier = barrier2;
        this.registry = destination;
        this.timeout = timeout;
        this.user = user;
        this.queryRequest = partAdhocQueryRequest;                
    }
    
        /*
         * @see java.lang.Runnable#run()
         */
    public void run() {
        
        try {
            setUpConnection();
            
            // Send the actual request here
            AdhocQueryResponse result = sendRequestToDestination(registry, user, queryRequest, timeout);
            this.response = result;
            // And return the result
            // We are not using a timeout here. A timeout should only happen if we have a lingering request
            // and we need to stop such requests before entering the rendezous,
            // otherwise we will accumulate hanging threads.
            //barrier.barrier();
            barrier.await();
            
        } catch (BrokenBarrierException e) {
            
            // Well, I cannot think of anything to do here except log a big, fat error.
            
        } catch (RegistryException re) {
            
            try {
                
                // If the request failed, return the exception instead of a result
                this.exception = re;
                //barrier.barrier();
                barrier.await();
                
            } catch (InterruptedException | BrokenBarrierException ie) {
                // log this
            }
            
        } catch (JAXRException re) {
            
            try {
                
                // If the request failed, return the exception instead of a result
                this.exception = re;
                //barrier.barrier();
                barrier.await();
                
            } catch (InterruptedException | BrokenBarrierException ie) {
                // log this
            }
            
        } catch (InterruptedException ie) {
            // log this
        }
        
    }
    
    /**
     * @param registry
     * @param user
     * @param queryRequest
     * @param timeout
     * @return
     */
    @SuppressWarnings("static-access")
	private AdhocQueryResponse sendRequestToDestination(RegistryType registry, UserType user, AdhocQueryRequest queryRequest, long timeout)
    throws RegistryException {
        AdhocQueryResponse resp = null;
        
        try {
            
            /*
            AdhocQueryType ahq = queryRequest.getAdhocQuery();            
            QueryExpressionType queryExp = ahq.getQueryExpression();
            String queryLang = queryExp.getQueryLanguage();
            String queryStr = (String)queryExp.getContent().get(0);
            Query query = null;
            if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANG_ID_SQL_92)) {
                query = dqm.createQuery(Query.QUERY_TYPE_SQL,  queryStr);
            } else {
                //TODO: filter query 
                throw new JAXRException("No support for Query Language: " + queryLang + " in persistent queries.");
            }
            */
            
            //If registry is local (connection == null) then use local call. 
            //Otherwise use JAXR connection / SOAP
            if (connection == null) {
                try {
                    it.cnr.icar.eric.common.spi.QueryManager qm = it.cnr.icar.eric.common.spi.QueryManagerFactory.getInstance().getQueryManager();
                    ServerRequestContext context = new ServerRequestContext("FederatedQueryWorker:sendRequestToDestination", queryRequest);
                    context.setUser(user);
                    resp = qm.submitAdhocQuery(context);
                    repositoryItemsMap = new HashMap<String, Object>();
                } catch (JAXRException e) {
                    String msg = ServerResourceBundle.getInstance().getString("message.error.localQuery", 
                            new Object[]{registry.getId(), bu.getInternationalStringAsString(registry.getName())});
                    throw new RegistryException(msg, e);                    
                }
            } else {            
                //Using impl specific convenient constructor
                try {
                    HashMap<String, String> queryParams = new HashMap<String, String>();
                    String returnType = ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM.toString();
                    queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);
                    
                    ClientRequestContext context = new ClientRequestContext("it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl:executeQuery", queryRequest);
                    
                    BulkResponseImpl br = (BulkResponseImpl)dqm.executeQuery(context, queryParams, new IterativeQueryParams());
                    log.trace("Registry id: " + registry.getId() + " name: " + bu.getInternationalStringAsString(registry.getName()) + " returned the following objects: " + br.getCollection());
                    resp = (AdhocQueryResponse)br.getRegistryResponse();
                    
                    bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
                    this.repositoryItemsMap = context.getRepositoryItemsMap();                    
                } catch (Exception e) {
                    String msg = ServerResourceBundle.getInstance().getString("message.error.remoteQuery", 
                            new Object[]{registry.getId(), bu.getInternationalStringAsString(registry.getName())});
                    throw new RegistryException(msg, e);
                }
            }
            
        } catch (JAXRException e) {
            //This exception is thrown potentially (unlikely) by bu.getInternationalStringAsString
            throw new RegistryException(e);
        }
                
        return resp;
    }
        
    /**
     * @return
     */
    public Exception getException() {
        return this.exception;
    }
    
    /**
     * @return
     */
    public AdhocQueryResponse getResponse() {
        return this.response;
    }
    
    public Map<String, Object> getRepositoryItemsMap() {
        return this.repositoryItemsMap;
    }
    
    /** Setup JAXR Connection for target registry */
    protected void setUpConnection() throws JAXRException {
        
        String home = registry.getHome();
        if ((home != null) && (connection == null)) {
            connection = ConnectionManager.getInstance().getConnection(home);
            ((ConnectionImpl)connection).setLocalCallMode(false);

            RegistryService service = connection.getRegistryService();
            dqm = (DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
        }        
    }
    
    
}
