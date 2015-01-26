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
import it.cnr.icar.eric.common.IdentifiableComparator;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;

//import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

/**
 * Handles dispatching of a federated query to all federation member Registries
 * and processes individual responses into a single unified response.
 *
 * @author Fabian Ritzmann
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class FederatedQueryProcessor {
    
    private static Log log = LogFactory.getLog(FederatedQueryProcessor.class);
    BindingUtility bu = BindingUtility.getInstance();
    AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();    
    
    private long TIMEOUT_CONSTANT = 50000L;
    private List<FederatedQueryWorker> workers = new LinkedList<FederatedQueryWorker>();
    private List<Thread> threads = new LinkedList<Thread>();
    
    // This will contain the list of responses, once the threads have run through
    private List<AdhocQueryResponse> responses = new LinkedList<AdhocQueryResponse>();
    // This will contain the list of exceptions from the threads that have not received a response
    private List<Exception> exceptions = new LinkedList<Exception>();
    
    private HashSet<RegistryType> members = null;
    private AdhocQueryRequest adhocQueryRequest = null;
        
    @SuppressWarnings("unused")
	private FederatedQueryProcessor() {        
    }
    
    FederatedQueryProcessor(HashSet<RegistryType> members) {
        this.members = members;
    }
    
    /**
     * Submits an AdhocQueryRequest to all Registries thare are members of specified federation.
     *
     * @param user
     * @param adhocQueryRequest the request sent to every registry ias a parrallel distrubuted query.
     */
    public AdhocQueryResponse submitAdhocQuery(final ServerRequestContext context)
    throws RegistryException {

        this.adhocQueryRequest = (AdhocQueryRequest)context.getCurrentRegistryRequest();
        try {
            //Reset federated flag before doing federated query to avoid infinite loop
            adhocQueryRequest.setFederated(false);

            //Reset federation flag before doing federated query to avoid potential 
            //for implementations to interpret non-null values as implying federated query
            adhocQueryRequest.setFederation(null);

            // Create a barrier for all worker threads
            log.trace("Dispatching federated query to " + members.size() + " member registries.");
            CyclicBarrier barrier = new CyclicBarrier(members.size(),
            new Runnable() {
                public void run() {
                    retrieveResults(context);
                }
            });

            ThreadGroup threadGroup = new ThreadGroup("federatedQuery");
            // Send a request to all destinations
            Iterator<RegistryType> i = members.iterator();
            while (i.hasNext()) {
                RegistryType registry = i.next();
                FederatedQueryWorker worker = new FederatedQueryWorker(barrier,
                registry,
                TIMEOUT_CONSTANT,
                context.getUser(),
                adhocQueryRequest);
                workers.add(worker);
                //Thread thread = new Thread(threadGroup, worker);
                Thread thread = new Thread(threadGroup, worker, "Federated_Query_" + registry.getId());
                threads.add(thread);
                log.trace("Dispatching query to registry with id: " + registry.getId() + " name: " + bu.getInternationalStringAsString(registry.getName()));
                thread.start();
            }

            Iterator<Thread> i1 = threads.iterator();
            while (i1.hasNext()) {
                Thread thread = i1.next();
                // Wait until all threads have finished.
                // CAVEAT: The timeouts will add up, max. waiting time is (number of threads) * (timeout)
                try {
                    thread.join(TIMEOUT_CONSTANT);
                } catch (InterruptedException e) {
                    //TODO: Try to kill the thread somehow
                }
            }
        } catch (JAXRException e) {
            //This exception is thrown potentially (unlikely) by bu.getInternationalStringAsString
            throw new RegistryException(e);
        }
                
        return getUnifiedResponse();        
    }
    
    private AdhocQueryResponse getUnifiedResponse() throws RegistryException {
        AdhocQueryResponse response = null;
        try {
            response = bu.queryFac.createAdhocQueryResponse();
            RegistryObjectListType ebRegistryObjectListType = bu.rimFac.createRegistryObjectListType();
            response.setRegistryObjectList(ebRegistryObjectListType);
            int maxTotalResultCount = -1;
            RegistryErrorList ebRegistryErrorList = null;
            
            Iterator<AdhocQueryResponse> i = responses.iterator();
            while (i.hasNext()) {
                AdhocQueryResponse ebAdhocQueryResponse = i.next();
                int totalResultCount = ebAdhocQueryResponse.getTotalResultCount().intValue();
                if (totalResultCount > maxTotalResultCount) {
                    maxTotalResultCount = totalResultCount;
                }
                
                if ((ebAdhocQueryResponse.getRegistryErrorList() != null) && (ebAdhocQueryResponse.getRegistryErrorList().getRegistryError().size() > 0)) {
                    if (ebRegistryErrorList == null) {
                        ebRegistryErrorList = bu.rsFac.createRegistryErrorList();
                        response.setRegistryErrorList(ebRegistryErrorList);                        
                    }
                    response.getRegistryErrorList().getRegistryError().addAll(ebAdhocQueryResponse.getRegistryErrorList().getRegistryError());
                }
                
                //Spec Issue: How to handle duplicate id across registries?? 
                //Need to return one. Which one? The latest? Probably the one from current registry.
                //May require always querying current registry last since code below keeps replacing existing objects 
                //with new ones that have same id.
                if (ebAdhocQueryResponse.getRegistryObjectList() != null) {
                    IdentifiableComparator comparator = new IdentifiableComparator();                    
                    List<JAXBElement<? extends IdentifiableType>> unifiedObjects = response.getRegistryObjectList().getIdentifiable();
                    List<JAXBElement<? extends IdentifiableType>> currentObjects = ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable();
                    Collections.sort(unifiedObjects, comparator);
                    
                    //Remove duplicates. 
                    //unifiedObjects.removeAll(currentObjects) will not work as there is no comparator implemented for JAXB objects
                    Iterator<JAXBElement<? extends IdentifiableType>> currentObjectsIter = currentObjects.iterator();
                    while (currentObjectsIter.hasNext()) {
                        RegistryObjectType currentObject = (RegistryObjectType)currentObjectsIter.next().getValue();
                        int index = Collections.binarySearch(unifiedObjects, currentObject, comparator);
                        if (index >= 0) {
                            unifiedObjects.remove(index);
                            log.trace("Removing duplicate object returned by a previous registry: id=" + currentObject.getId() + " name=" + bu.getInternationalStringAsString(currentObject.getName()));
                        }
                        log.trace("Adding object returned by registry: id=" + currentObject.getId() + " name=" + bu.getInternationalStringAsString(currentObject.getName()));
                    }
                    
                    //Add the currentObjects to unified objects
                    unifiedObjects.addAll(currentObjects);                    
                }
            }
            
            if ((response.getRegistryErrorList() != null) && (response.getRegistryErrorList().getRegistryError().size() > 0)) {
                response.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
            } else {
                response.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            }
            
            //Set iterative query related attributes
            response.setStartIndex(adhocQueryRequest.getStartIndex());
            response.setTotalResultCount(BigInteger.valueOf(maxTotalResultCount));
            
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
                
        return response;
    }
    
    /**
     * This method is run by the barrier once all workers have entered the barrier
     */
    private void retrieveResults(ServerRequestContext context) {
        
        Iterator<FederatedQueryWorker> i = workers.iterator();
        while (i.hasNext()) {
            
            FederatedQueryWorker worker = i.next();
            if (worker.getResponse() != null) {
                responses.add(worker.getResponse());
                
                //Add repositoryItems to context's repositoryItemMap
                context.getRepositoryItemsMap().putAll(worker.getRepositoryItemsMap());
            }
            else if (worker.getException() != null) {
                exceptions.add(worker.getException());
            }
        }
        
    }        
}
