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

package it.cnr.icar.eric.server.cms;

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import javax.activation.DataHandler;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;

/**
 * JUnit TestCase for CanonicalXMLCatalogingServiceTest.
 */
public class CanonicalXMLCatalogingServiceTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();

    /**
     * Constructor for CanonicalXMLCatalogingServiceTest
     *
     * @param name
     */
    public CanonicalXMLCatalogingServiceTest(String name) {
        super(name);

    }

    public static Test suite() {
        return new TestSuite(CanonicalXMLCatalogingServiceTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /*
     * This test verifies that the CanonicalXMLCatalogingService can handle an xml file of
     * type 'CPP'
     * TODO: add more unit tests to verify handling of temporary ids
     */
    public void testCatalogContentExtrinsicObjectStandaloneCPP() throws Exception {      
        String id = "urn:freebxml:registry:test:CPP1_xml";
        String sqlString = "SELECT * FROM RegistryObject WHERE lid in ('"+id+"')";
        try {
            //TODO: use standard methods in ServerTest 
            // like executeQuery, submit, remove(), removeIfExists()
            // Add new methods to ServerTest as needed
            String fileName = "CPP1.xml";
            String baseDir = getClass().getResource("/resources/").toExternalForm();
            File file = new File(baseDir+fileName);
            String path = file.getPath();
            URL wsdlURL = new URL(path);
            SubmitObjectsRequest submitRequest = createSubmitRequest(id, "text/xml");
            RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);

            bu.checkRegistryResponse(resp);

            // Execute query to check we have required metadata
            resp = executeAdhocQueryRequest(sqlString);
            bu.checkRegistryResponse(resp);       
            //Make sure that we find the CPP EO
            assertEquals("Did not find this object: " + fileName, 
                          1, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    /*
     * This test verifies that the Canonical XML Cataloging Service can 
     * handle an Submit Objects Request with no RI
     */
    @SuppressWarnings("static-access")
	public void testCatalogContentExtrinsicObjectStandaloneCPPMissingRI() throws Exception {      
        String id = "urn:freebxml:registry:test:CPP1_xml";
        String sqlString = "SELECT * FROM RegistryObject WHERE lid in ('"+id+"')";
        try {
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("text/xml");
            eo.setObjectType(bu.CPP_CLASSIFICATION_NODE_ID);
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            ServerRequestContext context = new ServerRequestContext("WSDLCatalogerTest:testCatalogContentExtrinsicObjectStandaloneCPPNoRI", submitRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            submit(context, eo);
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }

    private RegistryResponseType executeAdhocQueryRequest(String sqlString) 
        throws Exception {
        AdhocQueryRequest req = bu.createAdhocQueryRequest(sqlString);
        ServerRequestContext context = new ServerRequestContext("LifeCycleManagerImplTest:testCatalogContentExtrinsicObject", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);

        QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
        return qm.submitAdhocQuery(context);
    }
    
    private RegistryResponseType executeSubmitRequest(SubmitObjectsRequest submitRequest, 
                                                      String id, URL wsdlURL) 
        throws Exception {
        
        System.err.println("URL:" + wsdlURL.toExternalForm());
        DataHandler dh = new DataHandler(wsdlURL);
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        idToRepositoryItemMap.put(id, ri);
        
        ServerRequestContext context = new ServerRequestContext("WSDLCatalogerTest:testCatalogContentExtrinsicObject", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        return lcm.submitObjects(context);
    }
        
    @SuppressWarnings("static-access")
	private SubmitObjectsRequest createSubmitRequest(String id, String mimeType) throws Exception {
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType(mimeType);
        eo.setObjectType(bu.CPP_CLASSIFICATION_NODE_ID);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        return submitRequest;
    }
        
    private void cleanUpCatalogContentExtrinsicObjects(String sqlString) throws Exception {
        // Delete this object first to prevent false success below?
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(sqlString);
        bu.addSlotsToRequest(removeRequest, forceRemoveRequestSlotsMap);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
    
 

}
