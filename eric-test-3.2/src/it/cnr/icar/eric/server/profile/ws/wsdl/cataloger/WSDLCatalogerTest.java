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

package it.cnr.icar.eric.server.profile.ws.wsdl.cataloger;

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.profile.ws.wsdl.CanonicalConstants;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;

/**
 * JUnit TestCase for ContentCatalogingServiceImpl.  Uses the test Cataloging
 * service loaded with DemoDB as the service that the CMSManager invokes.
 */
public class WSDLCatalogerTest extends ServerTest {
    protected static final PersistenceManager pm =
	PersistenceManagerFactory.getInstance().getPersistenceManager();

    /**
     * Constructor for ContentCatalogingServiceImplTest
     *
     * @param name
     */
    public WSDLCatalogerTest(String name) {
        super(name);

    }

    public static Test suite() {
        boolean bypassCMS = Boolean.valueOf(RegistryProperties.getInstance()
                                   .getProperty("it.cnr.icar.eric.server.lcm.bypassCMS", "false")).booleanValue();        
        Test test = null;
        if (bypassCMS) {
            test = new TestSuite();
        } else {
            test = new TestSuite(WSDLCatalogerTest.class);
        }
        return test;
    }

    private File createRegistryWSDLZipFile(String[] relativeFilePaths) throws Exception {
        String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
        File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();
        
        return zipFile;
    }
    
    public void testCatalogContentExtrinsicObjectStandaloneWSDL() throws Exception {       
        String sqlString = getExtrinsicObjectStandaloneWSDLSQL();
        try {
        String fileName = "StandaloneTest.wsdl";
        String id = "urn:your:urn:goes:here:StandaloneTest.wsdl";
        // The id above is transfored to urn:your:urn:goes:here:StandaloneTest_wsdl
        // by WSDLCatalogerEngine. The period in the fileName is replaced with '_'. Reason?
        String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
        File file = new File(baseDir+fileName);
        String path = file.getPath();
        URL wsdlURL = new URL(path);
        SubmitObjectsRequest submitRequest = createSubmitRequest(id, "text/xml");
        RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);
        
        bu.checkRegistryResponse(resp);
        
        // Execute query to check we have required metadata
        resp = executeAdhocQueryRequest(sqlString);
        bu.checkRegistryResponse(resp);       
        //Make sure that we find all the objects required by WS Profile spec
        assertEquals("Did not find all objects required by WS Profile spec: " + 
                      printFoundObjects((AdhocQueryResponse)resp), 
                      10, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    private String getExtrinsicObjectStandaloneWSDLSQL() {
        String ids = getExtrinsicObjectStandaloneWSDLIds();
        return "SELECT * FROM RegistryObject WHERE lid in (" + ids + ")";
    }
    
    private String getExtrinsicObjectStandaloneWSDLIds() {
        StringBuffer sb = new StringBuffer();
        // EO for the WSDL file
        sb.append("'urn:your:urn:goes:here:StandaloneTest.wsdl', ");
        // WSDL Profile metadata below
        sb.append("'urn:your:urn:goes:here:binding:StandaloneTestBinding', ");
        sb.append("'urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:service:StandaloneTestService', ");
        sb.append("'urn:your:urn:goes:here:port:StandaloneTestPort', ");
        sb.append("'urn:your:urn:goes:here:StandaloneTest.wsdl:Contains:urn:your:urn:goes:here:binding:StandaloneTestBinding', ");
        sb.append("'urn:your:urn:goes:here:StandaloneTest.wsdl:Contains:urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:StandaloneTest.wsdl:Contains:urn:your:urn:goes:here:service:StandaloneTestService', ");
        sb.append("'urn:your:urn:goes:here:binding:StandaloneTestBinding:Implements:urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:port:StandaloneTestPort:Implements:urn:your:urn:goes:here:binding:StandaloneTestBinding' ");
        return sb.toString();
    }
    
    private String getExtrinsicObjectOneImportWSDLIds() {
        StringBuffer sb = new StringBuffer();
        // EO for the WSDL fileurn:your:urn:goes:here:OneImportTest.wsdl
        sb.append("'urn:your:urn:goes:here:OneImportTest.wsdl', ");
        // WSDL Profile metadata below
        sb.append("'urn:your:urn:goes:here:binding:StandaloneTestBinding', ");
        sb.append("'urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:service:StandaloneTestService', ");
        sb.append("'urn:your:urn:goes:here:port:StandaloneTestPort', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test1.wsdl:Contains:urn:your:urn:goes:here:binding:StandaloneTestBinding', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test1.wsdl:Contains:urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test1.wsdl:Contains:urn:your:urn:goes:here:service:StandaloneTestService', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test1.wsdl:Imports:urn:your:urn:goes:here:topDir:test2:Test2.wsdl', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test2.wsdl:Contains:urn:your:urn:goes:here:binding:StandaloneTestBinding', ");
        sb.append("'urn:your:urn:goes:here:topDir:test1:Test2.wsdl:Contains:urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:binding:StandaloneTestBinding:Implements:urn:your:urn:goes:here:portType:StandaloneTestPortType', ");
        sb.append("'urn:your:urn:goes:here:port:StandaloneTestPort:Implements:urn:your:urn:goes:here:binding:StandaloneTestBinding' ");
        return sb.toString();
    }
    
    public void testCatalogContentExtrinsicObjectStandaloneWSDLInZipFile() throws Exception {        
        String sqlString = getExtrinsicObjectStandaloneWSDLZipSQL();
        try {
            String fileName = "StandaloneTest.wsdl";
            String id = "urn:your:urn:goes:here:StandaloneWSDLZip";

            // Submit the zip file of wsdl and xsd files
            SubmitObjectsRequest submitRequest = createSubmitRequest(id, "application/zip");
            String[] relativeFilePaths = {
                    fileName,
                };
            File zipFile = createRegistryWSDLZipFile(relativeFilePaths);
            URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());

            RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);     
            bu.checkRegistryResponse(resp);

            // Execute query to check we have required metadata
            resp = executeAdhocQueryRequest(sqlString);
            bu.checkRegistryResponse(resp);       
            //Make sure that we find all the objects required by WS Profile spec
            assertEquals("Did not find all objects required by WS Profile spec: " + 
                          printFoundObjects((AdhocQueryResponse)resp), 
                          11, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    private String getExtrinsicObjectOneImportWSDLZipSQL() {
        String ids = getExtrinsicObjectOneImportWSDLZipIds();
        return "SELECT * FROM RegistryObject WHERE lid in (" + ids + ")";
    }
    
    private String getExtrinsicObjectOneImportWSDLZipIds() {
        StringBuffer sb = new StringBuffer();
        // EO for Zip file
        sb.append("'urn:your:urn:goes:here:OneImportWSDLZip', ");
        // Use same Ids from testCatalogContentExtrinsicObjectStandaloneWSDL
        sb.append(getExtrinsicObjectOneImportWSDLIds());
        return sb.toString();
    }
    
    private String getExtrinsicObjectStandaloneWSDLZipSQL() {
        String ids = getExtrinsicObjectStandaloneWSDLZipIds();
        return "SELECT * FROM RegistryObject WHERE lid in (" + ids + ")";
    }
    
    private String getExtrinsicObjectStandaloneWSDLZipIds() {
        StringBuffer sb = new StringBuffer();
        // EO for Zip file
        sb.append("'urn:your:urn:goes:here:StandaloneWSDLZip', ");
        // Use same Ids from testCatalogContentExtrinsicObjectStandaloneWSDL
        sb.append(getExtrinsicObjectStandaloneWSDLIds());
        return sb.toString();
    }
        
    public void testCatalogContentExtrinsicObject() throws Exception {        
        String sqlString = getExtrinsicObjectZipFileSQL();
        try {
        RegistryResponseType resp = null;
        String id = "urn:freebxml:registry:test:WSDLCatalogerEngineTest:registryWSDLExtrinsicObject";
        
        // Submit the zip file of wsdl and xsd files
        SubmitObjectsRequest submitRequest = createSubmitRequest(id, "application/zip");
        String[] relativeFilePaths = {
                "ebXMLRegistryServices.wsdl",
                "ebXMLRegistryBindings.wsdl",
                "ebXMLRegistryInterfaces.wsdl",
                "rs.xsd",
                "lcm.xsd",
                "query.xsd",
                "rim.xsd",
            };
        File zipFile = createRegistryWSDLZipFile(relativeFilePaths);
        URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());
        resp = executeSubmitRequest(submitRequest, id, wsdlURL);     
        bu.checkRegistryResponse(resp);
        
        // Execute query to check we have required metadata
        resp = executeAdhocQueryRequest(sqlString);
        bu.checkRegistryResponse(resp);       
        //Make sure that we find all the objects required by WS Profile spec
        assertEquals("Did not find all objects required by WS Profile spec. Found just these: " + 
                      printFoundObjects((AdhocQueryResponse)resp), 
                      34, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    public void testCatalogContentExtrinsicObjectsOneImport() throws Exception {        
        String sqlString = getExtrinsicObjectOneImportWSDLZipSQL();
        try {
            String id = "urn:your:urn:goes:here:OneImportWSDLZip";

            // Submit the zip file of wsdl files
            SubmitObjectsRequest submitRequest = createSubmitRequest(id, "application/zip");
            String[] relativeFilePaths = {
                    "topDir/test1/Test1.wsdl",
                    "topDir/test2/Test2.wsdl",
                };
            File zipFile = createRegistryWSDLZipFile(relativeFilePaths);
            URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());

            RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);     
            bu.checkRegistryResponse(resp);

            // Execute query to check we have required metadata
            resp = executeAdhocQueryRequest(sqlString);
            bu.checkRegistryResponse(resp);       
            //Make sure that we find all the objects required by WS Profile spec
            assertEquals("Did not find all objects required by WS Profile spec: " + 
                          printFoundObjects((AdhocQueryResponse)resp), 
                          11, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    public void testCatalogContentExtrinsicObjectsOneImport2() throws Exception {        
        String sqlString = getExtrinsicObjectOneImportWSDLZipSQL();
        try {
            String id = "urn:your:urn:goes:here:OneImportWSDLZip";

            // Submit the zip file of wsdl files
            SubmitObjectsRequest submitRequest = createSubmitRequest(id, "application/zip");
            String[] relativeFilePaths = {
                    "topDir/test1/test1.1/Test1.1.wsdl",
                    "topDir/test2/test2.1/Test2.1.wsdl",
                };
            File zipFile = createRegistryWSDLZipFile(relativeFilePaths);
            URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());

            RegistryResponseType resp = executeSubmitRequest(submitRequest, id, wsdlURL);     
            bu.checkRegistryResponse(resp);

            // Execute query to check we have required metadata
            resp = executeAdhocQueryRequest(sqlString);
            bu.checkRegistryResponse(resp);       
            //For now, success means no errors in processing.
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }
    
    private String printFoundObjects(AdhocQueryResponse resp) {
        StringBuffer sb = new StringBuffer();
        RegistryObjectListType rol = resp.getRegistryObjectList();
        Iterator<?> itr = rol.getIdentifiable().iterator();
        while (itr.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType)((JAXBElement<?>)itr.next()).getValue();
            sb.append(ro.getId()).append("<br>");
        }
        return sb.toString();
    }
    
    private String getExtrinsicObjectZipFileSQL() {
        String ids = getExtrinsicObjectTestIds();
        return "SELECT * FROM RegistryObject WHERE lid in (" + ids + ")";
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
        
    private SubmitObjectsRequest createSubmitRequest(String id, String mimeType) throws Exception {
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType(mimeType);
        eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        return submitRequest;
    }
        
    private void cleanUpCatalogContentExtrinsicObjects(String sqlString) throws Exception {
        // Delete this object first to prevent false success below?
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(sqlString);
        bu.addSlotsToRequest(removeRequest, this.forceRemoveRequestSlotsMap);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
    
    private String getExtrinsicObjectTestIds() {          
        StringBuffer sb = new StringBuffer();
        // WSDL Profile metadata below
        sb.append("'urn:your:urn:goes:here:service:TestEbXMLRegistrySOAPService', ");
        sb.append("'urn:your:urn:goes:here:port:TestQueryManagerPort', ");
        sb.append("'urn:your:urn:goes:here:port:TestLifeCycleManagerPort', ");
        sb.append("'urn:your:urn:goes:here:binding:TestQueryManagerSOAPBinding',");
        sb.append("'urn:your:urn:goes:here:binding:TestLifeCycleManagerSOAPBinding',");
        sb.append("'urn:your:urn:goes:here:portType:TestQueryManagerPortType',");
        sb.append("'urn:your:urn:goes:here:portType:TestLifeCycleManagerPortType',");
        sb.append("'urn:your:urn:goes:here:binding:TestLifeCycleManagerSOAPBinding:Implements:urn:your:urn:goes:here:portType:TestLifeCycleManagerPortType', ");
        sb.append("'urn:your:urn:goes:here:binding:TestQueryManagerSOAPBinding:Implements:urn:your:urn:goes:here:portType:TestQueryManagerPortType', ");
        sb.append("'urn:your:urn:goes:here:port:TestLifeCycleManagerPort:Implements:urn:your:urn:goes:here:binding:TestLifeCycleManagerSOAPBinding', ");
        sb.append("'urn:your:urn:goes:here:port:TestQueryManagerPort:Implements:urn:your:urn:goes:here:binding:TestQueryManagerSOAPBinding', ");
        // EOs submitted by user below
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryServices.wsdl', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryBindings.wsdl', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:rs.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0:query.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0:lcm.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:rim.xsd', ");
        // Assocations with XSD EOs below
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0:lcm.xsd:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:rim.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0:lcm.xsd:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:rs.xsd', ");        
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0:query.xsd:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:rim.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0:query.xsd:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:rs.xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:rim.xsd:Imports:urn:www_w3_org:XML:1998:namespace:2001:xml_xsd', ");
        sb.append("'urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:rs.xsd:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:rim.xsd', ");
        // Associations with WSDL EOs below
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryBindings.wsdl:Contains:urn:your:urn:goes:here:binding:TestLifeCycleManagerSOAPBinding', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryBindings.wsdl:Contains:urn:your:urn:goes:here:binding:TestQueryManagerSOAPBinding', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryBindings.wsdl:Imports:urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl:Contains:urn:your:urn:goes:here:portType:TestLifeCycleManagerPortType', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl:Contains:urn:your:urn:goes:here:portType:TestQueryManagerPortType', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0:lcm.xsd', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:rs.xsd', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryInterfaces.wsdl:Imports:urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0:query.xsd', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryServices.wsdl:Contains:urn:your:urn:goes:here:service:TestEbXMLRegistrySOAPService', ");
        sb.append("'urn:your:urn:goes:here:ebXMLRegistryServices.wsdl:Imports:urn:your:urn:goes:here:ebXMLRegistryBindings.wsdl' ");
        return sb.toString();
    }
    
    private String getExternalLinkTestIds() {
        StringBuffer sb = new StringBuffer();
        sb.append("'urn:freebxml:registry:test:WSDLCatalogerEngineTest:uddiWSDLExtrinsicObject', ");
        sb.append("'urn:your:urn:goes:here:port:QueryManagerPort:Implements:urn:oasis:names:tc:ebxml_regrep:wsdl:registry:bindings:3_0:binding:QueryManagerSOAPBinding', ");
        sb.append("'urn:your:urn:goes:here:port:LifeCycleManagerPort:Implements:urn:oasis:names:tc:ebxml_regrep:wsdl:registry:bindings:3_0:binding:LifeCycleManagerSOAPBinding', ");
        sb.append("'urn:freebxml:registry:test:WSDLCatalogerEngineTest:uddiWSDLExtrinsicObject:Contains:urn:your:urn:goes:here:service:ebXMLRegistrySOAPService', ");
        sb.append("'urn:oasis:names:tc:ebxml_regrep:wsdl:registry:bindings:3_0:binding:LifeCycleManagerSOAPBinding:Implements:urn:oasis:names:tc:ebxml_regrep:wsdl:registry:interfaces:3_0:portType:LifeCycleManagerPortType', ");
        sb.append("'urn:freebxml:registry:test:WSDLCatalogerEngineTest:uddiWSDLExtrinsicObject:Imports:urn:oasis:names:tc:ebxml_regrep:wsdl:registry:bindings:3_0:ebXMLRegistryBindings_wsdl', ");
        sb.append("'urn:oasis:names:tc:ebxml_regrep:wsdl:registry:bindings:3_0:binding:QueryManagerSOAPBinding:Implements:urn:oasis:names:tc:ebxml_regrep:wsdl:registry:interfaces:3_0:portType:QueryManagerPortType' ");
        return sb.toString();
    }
    
    private String getExternalLinkSQL() {
        String ids = getExternalLinkTestIds();
        return "SELECT * FROM RegistryObject WHERE lid in (" + ids + ")";
    }
     
    public void testCatalogContentExternalLink() throws Exception {
        String id = "urn:freebxml:registry:test:WSDLCatalogerEngineTest:uddiWSDLExtrinsicObject";
        String sqlString = getExternalLinkSQL();
        try {
            RegistryResponseType resp = null;

            RemoveObjectsRequest removeRequest = createRemoveObjectsRequest("SELECT * FROM ExternalLink WHERE lid = '" + id + "' ");
            ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            RegistryResponseType response = lcm.removeObjects(context);
            bu.checkRegistryResponse(response);

            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
            ExternalLinkType extLink = bu.rimFac.createExternalLinkType();
            //extLink.setExternalURI("http://uddi.org/wsdl/uddi_api_v3_binding.wsdl");
	    if (canUseEbxmlrrSpecHome) {
		extLink.
		    setExternalURI(ebxmlrrSpecHome +
				   "/misc/3.0/services/ebXMLRegistryServices.wsdl");
	    } else {
		// assume server started in root directory of this workspace
		extLink.setExternalURI("src/it/cnr/icar/eric/server/profile/ws"
				       + "/wsdl/data/ebXMLRegistryServices.wsdl");
	    }
            extLink.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
            extLink.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, extLink);

            HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
            context = new ServerRequestContext("WSDLCatalogerTest:testCatalogContentExternalLink", submitRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);

            AdhocQueryRequest req = bu.createAdhocQueryRequest(sqlString);
            context = new ServerRequestContext("LifeCycleManagerImplTest:testCatalogContentExtrinsicObject", req);
            context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);

            QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
            resp = qm.submitAdhocQuery(context);
            bu.checkRegistryResponse(resp);
            bu.getJAXBContext().createMarshaller().marshal(resp, System.err);

            //Make sure that there is at least one object that matched the query
            assertEquals("Did not find all objects required by WS Profile spec. Found just these: " + 
                      printFoundObjects((AdhocQueryResponse)resp), 7, ((AdhocQueryResponse)resp).getRegistryObjectList().getIdentifiable().size());
        } finally {
            cleanUpCatalogContentExtrinsicObjects(sqlString);
        }
    }

}
