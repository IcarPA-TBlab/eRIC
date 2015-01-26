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

import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.List;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.bind.JAXBException;
import javax.xml.registry.UnsupportedCapabilityException;


import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;


/**
 * Implements JAXR API interface named Query.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class QueryImpl implements Query {
    
    private AdhocQueryRequest ebAdhocQueryRequest = null;
    private static BindingUtility bu = BindingUtility.getInstance();        
    
    /**
     * This constant is added to support new stored query. This queryType
     * involves the use of the XPath query language.
     *
     * TODO JAXR 2.0: this constant will be added to javax.xml.registry.Query for 
     * JAXR 2.0. After that, remove from this constant from the class.
     */
    private static int qtFilterQuery = QUERY_TYPE_EBXML_FILTER_QUERY;
    public static final int QUERY_TYPE_XPATH = (qtFilterQuery + 1);
    
    public QueryImpl(int queryType) 
        throws JAXRException {
        
        this.ebAdhocQueryRequest = bu.queryFac.createAdhocQueryRequest();
		ebAdhocQueryRequest.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

		AdhocQueryType ebAdhocQueryType = bu.rimFac.createAdhocQueryType();
		ebAdhocQueryType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

		QueryExpressionType ebQueryExpressionType = bu.rimFac.createQueryExpressionType();
		ebAdhocQueryType.setQueryExpression(ebQueryExpressionType);
		ebQueryExpressionType.setQueryLanguage(mapQueryTypeFromJAXR2ebXML(queryType));
		//queryExp.getContent().add(queryStr);

		ebAdhocQueryRequest.setAdhocQuery(ebAdhocQueryType);

		ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnComposedObjects(true);
		
		//Do not specify LEAF_CLASS_WITH_REPOSITORY ITEM by default as repositoryitems are fetched lazily.
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		ebAdhocQueryRequest.setResponseOption(ebResponseOptionType);
    }
     
    public QueryImpl(int queryType, String queryString) 
        throws JAXRException {
        // The queryString is null, set to empty string. Reason: 
        // AdhocQueryRequestType cannot have a null QueryExpression
        //if (queryString == null) {
        //    queryString = "";
        //}
          
        this.ebAdhocQueryRequest = bu.queryFac.createAdhocQueryRequest();
		ebAdhocQueryRequest.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

		AdhocQueryType ebAdhocQueryType = bu.rimFac.createAdhocQueryType();
		ebAdhocQueryType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

		QueryExpressionType ebQueryExpressionType = bu.rimFac.createQueryExpressionType();
		ebAdhocQueryType.setQueryExpression(ebQueryExpressionType);
		ebQueryExpressionType.setQueryLanguage(mapQueryTypeFromJAXR2ebXML(queryType));

		if (queryString != null) {
		    ebQueryExpressionType.getContent().add(queryString);
		}

		ebAdhocQueryRequest.setAdhocQuery(ebAdhocQueryType);

		ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnComposedObjects(true);

		//Do not specify LEAF_CLASS_WITH_REPOSITORY ITEM by default as repositoryitems are fetched lazily.
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		ebAdhocQueryRequest.setResponseOption(ebResponseOptionType);
    }

    public QueryImpl(AdhocQueryRequest req) 
        throws JAXRException {

        this.ebAdhocQueryRequest = req;
    }
    
    /*
     * Sets whether this is a federated query or not.
     * TODO: Add to JAXR 2.0
     */
    public void setFederated(boolean federated) throws JAXRException {
        ebAdhocQueryRequest.setFederated(federated);
    }
    
    /*
     * Determines whether this is a federated query or not.
     * TODO: Add to JAXR 2.0
     */
    public boolean isFederated() throws JAXRException {
        return ebAdhocQueryRequest.isFederated();
    }
    
    /*
     * Sets the federation id for a federated.
     * TODO: Add to JAXR 2.0
     */
    public void setFederation(String federationId) throws JAXRException  {
        ebAdhocQueryRequest.setFederation(federationId);
        if ((federationId != null) && (!isFederated())) {
            setFederated(true);
        }
    }
    
    public String getFederation() {
        return ebAdhocQueryRequest.getFederation();
    }
    
    /**
     * Gets the type of Query (e.g. QUERY_TYPE_SQL)
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @see Query#QUERY_TYPE_SQL
     * @see Query#QUERY_TYPE_XQUERY
     * @return the type of query
     */
    public int getType() throws JAXRException {
        String queryLanguageConceptId = ebAdhocQueryRequest.getAdhocQuery().getQueryExpression().getQueryLanguage();        
        return mapQueryTypeFromebXML2JAXR(queryLanguageConceptId);
    }

    /**
     * Must print the String representing the query. For example
     * in case of SQL query prints the SQL query as a string.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     */
    public String toString() {
        String queryString = null;
        List<?> content = ebAdhocQueryRequest.getAdhocQuery().getQueryExpression().getContent();  
        if (content.size() >= 1) {
            Object obj = content.get(0);
            if (obj instanceof String) {
                queryString = (String)obj;
            }
        }
        return queryString;
    }
    

    /**
     * This method is used to return a AdhocQueryRequestType object that is
     * bound to this class.
     *
     * @return
     *   An AdhocQueryRequestType instance
     * @throws JAXBException
     * @throws JAXRException
     */
    public AdhocQueryRequest toBindingObject() throws JAXBException, JAXRException {           
        return ebAdhocQueryRequest;
    }
    
    private static int mapQueryTypeFromebXML2JAXR(String queryLanguageConceptId) throws JAXRException {
        if (queryLanguageConceptId.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery)) {
            return Query.QUERY_TYPE_EBXML_FILTER_QUERY;
        } else if (queryLanguageConceptId.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
            return Query.QUERY_TYPE_SQL;
        } else {
            throw new UnsupportedCapabilityException(JAXRResourceBundle.getInstance().getString("message.error.query.type",new Object[] {queryLanguageConceptId}));
        }
    }
    
    private static String mapQueryTypeFromJAXR2ebXML(int jaxrQueryType) throws JAXRException {
        if (jaxrQueryType == Query.QUERY_TYPE_EBXML_FILTER_QUERY) {
            return BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery;
        } else if (jaxrQueryType == Query.QUERY_TYPE_SQL) {
            return BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92;
            
            // TODO: xxx pa 111228 add new querylanguage
            
        } else {
            throw new UnsupportedCapabilityException(JAXRResourceBundle.getInstance().getString("message.error.query.type",new Object[] {new Integer(jaxrQueryType)}));
        }
    }
    
}
