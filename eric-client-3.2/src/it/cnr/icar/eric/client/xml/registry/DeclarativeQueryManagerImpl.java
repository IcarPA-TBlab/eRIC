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
package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.IterativeQueryParams;

import java.util.Map;
import java.math.BigInteger;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Connection;
import javax.xml.registry.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;

/**
 * Implements JAXR API interface named DeclarativeQueryManager.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class DeclarativeQueryManagerImpl extends QueryManagerImpl
    implements DeclarativeQueryManager {
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(DeclarativeQueryManagerImpl.class);
    @SuppressWarnings("unused")
	private org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    @SuppressWarnings("unused")
	private org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    @SuppressWarnings("unused")
	private org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    private BindingUtility bu = BindingUtility.getInstance();

    public final static String CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS =
        "urn:javax:xml:registry:DeclarativeQueryManager:responseOption:returnComposedObjects:transientslot";        
    
    public final static String CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE =
        "urn:javax:xml:registry:DeclarativeQueryManager:responseOption:returnType:transientslot";        
    
    DeclarativeQueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm) throws JAXRException {
        super(regService, lcm, null);

        lcmFac = bu.lcmFac;
        rimFac = bu.rimFac;
        queryFac = bu.queryFac;
    }

    /**
     * Creates a Query object given a queryType (e.g. SQL)
     * that represents a query in the syntax appropriate for queryType.
     * No query string is passed to this method.  So, user must call
     * DeclarativeQueryManager.executeQuery(query, queryParams)
     * Must throw an InvalidRequestException if the sqlQuery is not valid.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     * @see DeclarativeQueryManager#executeQuery(Query query, Map queryParams)
     */
    public Query createQuery(int queryType)
        throws InvalidRequestException, JAXRException {
        if (queryType != Query.QUERY_TYPE_SQL) {
            throw new InvalidRequestException(
                "Type must be Query.QUERY_TYPE_SQL");
        }

        return new QueryImpl(queryType);
    }
    
    /**
     * Creates a Query object given a queryType (e.g. SQL) and a String
     * that represents a query in the syntax appropriate for queryType.
     * Must throw and InvalidRequestException if the sqlQuery is not valid.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     */
    public Query createQuery(int queryType, String queryString)
        throws InvalidRequestException, JAXRException {
        if ((queryType != Query.QUERY_TYPE_SQL) && (queryType != Query.QUERY_TYPE_EBXML_FILTER_QUERY))  {
            throw new InvalidRequestException(
                "Type must be Query.QUERY_TYPE_SQL or QUERY_TYPE_EBXML_FILTER_QUERY");
        }

        // TODO: check queryString syntax
        return new QueryImpl(queryType, queryString);
    }
    
    /**
     * Execute a query as specified by query paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the queryParams or query parameter is null
     */
    public BulkResponse executeQuery(Query query) throws JAXRException {
        BulkResponse br = executeQuery(query, null);           
        return br;
    }
    
    /**
     * Execute a query as specified by query and parameters paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query. This parameter
     *   is required.
     * @param queryParams
     *   A java.util.Map of query parameters. This parameter is optional.
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the query parameter is null
     */
    public BulkResponse executeQuery(Query query, Map<String, String> queryParams) throws JAXRException {
        return executeQuery(query, queryParams, new IterativeQueryParams());
    }
    
    //TODO: Add this as interface to JAXR 2.0??   
	public BulkResponse executeQuery(Query query, Map<String, String> queryParams, IterativeQueryParams iterativeParams)
			throws JAXRException {
		BulkResponse bresp = null;

		try {
			if (query == null) {
				throw new NullPointerException("query is null");
			}

			AdhocQueryRequest ebAdhocQueryRequest = ((QueryImpl) query).toBindingObject();
			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl:executeQuery",
					ebAdhocQueryRequest);
			bresp = executeQuery(context, queryParams, iterativeParams);
		} catch (JAXBException ex) {
			ex.printStackTrace();
		}
		return bresp;
	}
    
    //TODO: Add this as interface to JAXR 2.0??   
    /**
     * Execute a query as specified by query and parameters paramater.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @param query
     *   A javax.xml.registry.Query object containing the query. This parameter
     *   is required.
     * @param queryParams
     *   A java.util.Map of query parameters. This parameter is optional.
     * @param iterativeParams
     *   A IterativeQueryParams instance that holds parameters used in making
     *   iterative queriesClientContext
     * @throws JAXRException
     *   This exception is thrown if there is a problem with a JAXB marshall,
     *   a problem with the Registry and other JAXR provider errors
     * @throws NullPointerException
     *   This is thrown if the query parameter is null
     */
    @SuppressWarnings("static-access")
	public BulkResponse executeQuery(ClientRequestContext context, Map<String, String> queryParams, 
                                     IterativeQueryParams iterativeParams) 
        throws JAXRException {
        try {
            AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) context.getCurrentRegistryRequest();            
            Connection connection = ((RegistryServiceImpl)getRegistryService()).getConnection();
            JAXRUtility.addCreateSessionSlot(ebAdhocQueryRequest, connection);
            
            // If parameter Map is not null, set parameters on the AdhocQuery object as a
            // slot list
            if (queryParams != null) {                               
                ResponseOptionType responseOption = ebAdhocQueryRequest.getResponseOption();
                
                //Extract (remove) transient Slots from slotsMap if specified
                String responseOptionReturnComposedObjectSlotValue = queryParams.remove(this.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS);
                String responseOptionReturnTypeSlotValue = queryParams.remove(this.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE);
                
                if (responseOptionReturnTypeSlotValue != null) {
                	if (responseOptionReturnTypeSlotValue.equals("OBJECT_REF"))
                		responseOptionReturnTypeSlotValue = "ObjectRef";
                	else if (responseOptionReturnTypeSlotValue.equals("REGISTRY_OBJECT"))
                		responseOptionReturnTypeSlotValue = "RegistryObject";
                	else if (responseOptionReturnTypeSlotValue.equals("LEAF_CLASS"))
                		responseOptionReturnTypeSlotValue = "LeafClass";
                	else if (responseOptionReturnTypeSlotValue.equals("LEAF_CLASS_WITH_REPOSITORY_ITEM"))
                		responseOptionReturnTypeSlotValue = "LeafClassWithRepositoryItem";
                	
                    ReturnType returnType = ReturnType.fromValue(responseOptionReturnTypeSlotValue);
                    responseOption.setReturnType(returnType);
                }
                if (responseOptionReturnComposedObjectSlotValue != null) {
                    boolean returnComposedObjects = Boolean.valueOf(responseOptionReturnComposedObjectSlotValue).booleanValue();
                    responseOption.setReturnComposedObjects(returnComposedObjects);
                }
                
                bu.addSlotsToRequest(ebAdhocQueryRequest, queryParams);
            }
            // Add iterative query parameters to request
            ebAdhocQueryRequest.setStartIndex(new BigInteger(String.valueOf(iterativeParams.startIndex)));
            ebAdhocQueryRequest.setMaxResults(new BigInteger(String.valueOf(iterativeParams.maxResults)));
            
            AdhocQueryResponse ebAdhocQueryResponse = serverQMProxy.submitAdhocQuery(context);            
            BulkResponse br = new BulkResponseImpl(lcm, ebAdhocQueryResponse, context.getRepositoryItemsMap());           
            return br;
        } catch (JAXBException e) {
            throw new JAXRException(e);
        }
    }


}
