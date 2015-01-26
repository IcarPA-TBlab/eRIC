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

import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.*;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import javax.xml.bind.Unmarshaller;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import java.util.HashMap;

/**
 * @author Farrukh S. Najmi
 */
public class AdhocQueryDAOTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    
    protected static final String packageId =  "urn:org:freebxml:eric:server:persistence:rdb:AdhocQueryDAOTest";
    private final String largeQueryId = packageId + ":largeQuery";

    /**
     * Constructor for AdhocQueryDAOTest
     *
     * @param name name of the test
     */
    public AdhocQueryDAOTest(String name) {
        super(name);
    }


    /**
     *
     * @return the suite of tests to run.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AdhocQueryDAOTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AdhocQueryDAOTest("testUpdateSpillOverQuery"));
        return suite;
        
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {
        final String contextId = packageId + ":setUp";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        removeIfExist(context, largeQueryId);
        
    }

    protected void tearDown() throws Exception {
        final String contextId = packageId + ":setUp";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        removeIfExist(context, largeQueryId);
    }
    
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testCreateSpillOverQuery() throws Exception {
        createSpillOverQuery();
        readSpillOverQuery();
        executeSpillOverQuery();
    }
    
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testUpdateSpillOverQuery() throws Exception {
        createSpillOverQuery();
        createSpillOverQuery();
        readSpillOverQuery();
        executeSpillOverQuery();
    }
    
    /**
     * Tests creation of a large query that should spillover into a repository item
     */
    public void testDeleteSpillOverQuery() throws Exception {
        createSpillOverQuery();
        deleteSpillOverQuery();
    }
    
    /**
     * Utility method for creating a spillOverQuery
     */
    private void createSpillOverQuery() throws Exception {
        final String contextId = packageId + ":createSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {            
            String largeQueryPath = "/resources/SubmitObjectsRequest_LargeQuery.xml";
            URL largeQueryURL = getClass().getResource(largeQueryPath);
            String largeQueryFileName = largeQueryURL.getFile();
            
            Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
            SubmitObjectsRequest req = (SubmitObjectsRequest)unmarshaller.unmarshal(new File(largeQueryFileName));
            AdhocQueryType ahq = (AdhocQueryType)req.getRegistryObjectList().getIdentifiable().get(0).getValue();
            ahq.setId(largeQueryId);
            ahq.setLid(largeQueryId);
            
            submit(context, ahq);

        } finally {
            context.commit();
        }                
    }
    
    /**
     * Utility method for reading a spillOverQuery
     */
    private void readSpillOverQuery() throws Exception {        
        final String contextId = packageId + ":readSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        try {
            AdhocQueryType ahq = (AdhocQueryType)qm.getRegistryObject(context, largeQueryId);
            if (ahq == null) {
                fail("Could not read spillover query back.");
            }
            
            //Make sure query column does not contain a spillOverId
            QueryExpressionType queryExp = ahq.getQueryExpression();
            String query = (String)queryExp.getContent().get(0);
            if (query.startsWith("urn:")) {
                fail("Query column contains a spillover id instead of actual query.");
            }
            
            //Check length
            if (query.length() < 4000) {
                fail("Query column does not have enough characters.");
            }
        } finally {
            context.commit();
        }
    }
    
    /**
     * Utility method for executing a spillOverQuery
     */
    private void executeSpillOverQuery() throws Exception {
        final String contextId = packageId + ":executeSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            @SuppressWarnings("unused")
			String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
            HashMap<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("$service.name", "%ebxml%");
            queryParams.put("$considerPort", "1");
            queryParams.put("$considerBinding", "0");
            queryParams.put("$considerPortType", "0");
            @SuppressWarnings("unused")
			Collection<?> registryObjects = executeQuery(context, largeQueryId, queryParams); 
        } finally {
            context.commit();
        }
    }
            
    /**
     * Utility method for deleting a spillOverQuery and its EO/RI sub-objects
     */
    private void deleteSpillOverQuery() throws Exception {        
        final String contextId = packageId + ":deleteSpillOverQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        try {
            removeIfExist(context, largeQueryId);
            
            //Now check that EO/RI sub-objects have also been deleted 
            AdhocQueryDAO dao = new AdhocQueryDAO(context);
            @SuppressWarnings("unused")
			String spillOverId = dao.getSpillOverRepositoryItemId(largeQueryId, AdhocQueryDAO.QUERY_COL_COLUMN_INFO);
            
            try {
                @SuppressWarnings("unused")
				ExtrinsicObjectType eo = (ExtrinsicObjectType)qm.getRegistryObject(context, largeQueryId);
                fail("Deleting a spillover query did not delete its EO/RI sub-objects");
            } catch (ObjectNotFoundException e) {
                //Expected
            }
                        
        } finally {
            context.commit();
        }
    }
}
