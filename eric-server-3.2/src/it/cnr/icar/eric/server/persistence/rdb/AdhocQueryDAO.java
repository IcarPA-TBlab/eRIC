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
package it.cnr.icar.eric.server.persistence.rdb;

import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 *
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class AdhocQueryDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(AdhocQueryDAO.class);
        
    public static final String DEFAULT_ADHOCQUERY_QUERY_LENGTH = "4096";
    
    public static final int adhocQueryQueryLength = Integer.parseInt(RegistryProperties.getInstance().getProperty(
        "eric.persistence.rdb.adhocQueryQueryLength", 
        DEFAULT_ADHOCQUERY_QUERY_LENGTH));
    
    public static final String QUERY_COL_COLUMN_INFO = "adhocQuery:query";

    /**
     * Use this constructor only.
     */
    AdhocQueryDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "AdhocQuery";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
    
    protected void prepareToInsert(Object object) throws RegistryException {        
        AdhocQueryType ro = (AdhocQueryType)object;
        super.prepareToInsert(object);
        validateQuery(ro);
    }
                
    protected void prepareToUpdate(Object object) throws RegistryException {        
        AdhocQueryType ro = (AdhocQueryType)object;
        super.prepareToUpdate(object);
        validateQuery(ro);
    }
    
    protected void validateQuery(AdhocQueryType object) throws RegistryException {        
        //TODO: Put query syntax validation code here.
    }
                    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        AdhocQueryType ahq = null;
            
        String stmtFragment = null;
        String query = null;
        String queryLang = null;
                
        if (ro instanceof AdhocQueryType) {
            ahq = (AdhocQueryType)ro;
            
            QueryExpressionType queryExp = ahq.getQueryExpression();
            queryLang = queryExp.getQueryLanguage();
            query = (String)queryExp.getContent().get(0);
            
            /*
            if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
                query = (String)queryExp.getContent().get(0);
            } else {
                //TODO: filter query persistence
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.noSupportForQueryLanguage",
                        new Object[]{queryLang}));
            }*/
        }
        
        if (action == DAO_ACTION_INSERT) {
            query = spillOverQueryIfNeeded(ahq);
            stmtFragment = "INSERT INTO AdhocQuery " +
                super.getSQLStatementFragment(ro) +
                    ", '" + queryLang + 
                    "', '" + query + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            query = spillOverQueryIfNeeded(ahq);
            stmtFragment = "UPDATE AdhocQuery SET " +
                super.getSQLStatementFragment(ro) +
                    ", query='" + query + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            deleteSpillOverQueryIfNeeded(ahq);
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    
    private String spillOverQueryIfNeeded(AdhocQueryType ahq) throws RegistryException {
        QueryExpressionType queryExp = ahq.getQueryExpression();
        String query = (String)queryExp.getContent().get(0);
        if (query == null) {
            return query="";
        }
        //Check if query will fit in column size available.                
        if (query.length() > adhocQueryQueryLength) {
            //Need to store query in a repository item and store its id in the query column
            //This introduces a level of indirection which is resolved when the query is read.
            String queryId = ahq.getId();

            query = marshalToRepositoryItem(queryId, QUERY_COL_COLUMN_INFO, query);                    
        }
        
        return query;
    }

    private void deleteSpillOverQueryIfNeeded(AdhocQueryType ahq) throws RegistryException {
        QueryExpressionType queryExp = ahq.getQueryExpression();
        String query = (String)queryExp.getContent().get(0);

        //Check if query will fit in column size available.                
        if (query.length() > adhocQueryQueryLength) {
            removeSpillOverRepositoryItem(ahq.getId(), QUERY_COL_COLUMN_INFO);
        }        
    }
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            //TODO: Add support for filter query type later
            if (!(obj instanceof AdhocQueryType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.SQLQueryExpected",
                        new Object[]{obj}));
            }

            AdhocQueryType ebAdhocQueryType = (AdhocQueryType) obj;
            super.loadObject( obj, rs);

            String query = rs.getString("query");
            
            //Check if query is actually a ref to a query stored in a repository item 
            //If so we needs to dereference it to get actual query string
            if (query.startsWith("urn:freebxml:registry:spillOverId:")) {
                //query is actually in a repository item and this is just the id of that repository item
                //Fetch the actual query by dereferencing this id
                
                
                try {
                    query = unmarshallFromRepositoryItem(query);
                    
                    //We escape single quotes in the query with another single quote when storing as VARCHAR. 
                    //JDBC strips the first single quote on read from VARCHAR column
                    //When reading from content of a RepositoryItem replace "''" with "'"
                    query = query.replaceAll("''", "'");
                                        
                } catch (Exception e) {
                    //throw new RegistryException(e);
                    log.error(e, e);
                }
            }
            
            String queryLang = rs.getString("queryLanguage");
            
            QueryExpressionType ebQueryExpressionType = bu.rimFac.createQueryExpressionType();
            ebQueryExpressionType.setQueryLanguage(queryLang);
            ebQueryExpressionType.getContent().add(query); 
            ebAdhocQueryType.setQueryExpression(ebQueryExpressionType);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        AdhocQueryType ebAdhocQueryType = bu.rimFac.createAdhocQueryType();
        
        return ebAdhocQueryType;
    }
    
}
