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


package it.cnr.icar.eric.server.query;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 *
 * @author psterk
 */
public class QueryFilterPluginTest extends ServerTest {
    
    /**
     * Creates a new instance of QueryFilterPluginTest
     */
    public QueryFilterPluginTest(String name) {
        super(name);
        RegistryProperties.getInstance().put("it.cnr.icar.eric.server.query.bypassCMS", "false");
    }
    
    /**
     * This test method requests that the StandaloneTest.wsdl is to be compressed.
     * This method verifies that a zip file is returned.
     */
    @SuppressWarnings("unchecked")
	public void testCompressContentRequestStandaloneWSDLFile() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:StandaloneFilterQueryTest.wsdl";                
        String queryString = "SELECT * FROM ExtrinsicObject WHERE id = '"+ id +"'";
        try {
            String fileName = "StandaloneTest.wsdl";
            String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
            File file = new File(baseDir+fileName);
            String path = file.getPath();
            URL wsdlURL = new URL(path);
            DataHandler dh = new DataHandler(wsdlURL);
            RepositoryItem ri = new RepositoryItemImpl(id, dh);
            HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
            idToRepositoryItemMap.put(id, ri);
            // Construct SubmitObjectsRequest and place WSDL in it
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);
            ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testQueryWithCompressContentRequest", submitRequest);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("text/xml");
            eo.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL");
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

            submit(context, eo, idToRepositoryItemMap);

            // Query for the object and indicate compressed content
            @SuppressWarnings("rawtypes")
			HashMap queryParamsMap = new HashMap();
            Collection<String> filterQueryIds = new ArrayList<String>();
            filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
            queryParamsMap.put("$filterQueryIds", filterQueryIds);
            queryParamsMap.put("$query", queryString);
            ServerRequestContext queryContext = new ServerRequestContext("QueryManagerImplTest:testCompressContentQueryPlugin", null);
            queryContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            List<?> res = executeQuery(queryContext, CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery, queryParamsMap);
            assertEquals("testQueryWithCompressContentRequest.", 1, res.size());
        } catch (Exception e) {
            throw e;
        } finally {
            cleanup(id);
        }
    }

    /**
     * This test method requests compressed content of the Test1.wsdl test file
     * that imports Test2.wsdl. The CANONICAL_SEARCH_DEPTH_PARAMETER is set to -1
     * to indicate that the search depth will include all levels.
     * This method verifies that a zip file is returned.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCompressContentRequestWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
            String[] relativeFilePaths = {
                    "topDir/test1/Test1.wsdl",
                    "topDir/test2/Test2.wsdl",
                };
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);       
            ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
            bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
            bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
            eo.setMimeType("application/zip");
            eo.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL");
            eo.setId(id);
            bu.addRegistryObjectToSubmitRequest(submitRequest, eo);
            File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");        
            zipFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = null;
            try {
                zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
            } finally {
                zos.close();
            }
            URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());

            DataHandler dh = new DataHandler(wsdlURL);
            RepositoryItem ri = new RepositoryItemImpl(id, dh);
            HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
            idToRepositoryItemMap.put(id, ri);

            ServerRequestContext context = new ServerRequestContext("QueryFilterPluginTest:testCompressContentRequestWSDLFileWithImport", submitRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            RegistryResponseType resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            // Query for the object and indicate compressed content
            HashMap queryParamsMap = new HashMap();
            Collection filterQueryIds = new ArrayList();
            filterQueryIds.add(BindingUtility.FREEBXML_REGISTRY_FILTER_QUERY_COMPRESSCONTENT);
            queryParamsMap.put("$filterQueryIds", filterQueryIds);
            
            String queryString = "SELECT * FROM ExtrinsicObject WHERE id ='" +
                                  id1 +"'";
            queryParamsMap.put("$query", queryString);
            queryParamsMap.put(CanonicalConstants.CANONICAL_SEARCH_DEPTH_PARAMETER, "-1");
            ServerRequestContext queryContext = new ServerRequestContext("QueryManagerImplTest:testCompressContentQueryPlugin", null);
            queryContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            List res = executeQuery(queryContext, CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery, queryParamsMap);
            assertEquals("testQueryWithCompressContentRequest.", 1, res.size());             
        } catch (Exception e) {
            throw e;
        } finally {
            cleanup(id1);
        }
    }
    
    private void cleanup(String id) throws Exception {
        String cleanupAssocQueryString = "SELECT * FROM Association WHERE sourceObject = "+
                                        "('"+ id +"')";
        RemoveObjectsRequest removeAssocRequest = createRemoveObjectsRequest(cleanupAssocQueryString);
        ServerRequestContext assocContext = new ServerRequestContext("RepositoryTest:testDelete", removeAssocRequest);
        assocContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType assocResponse = lcm.removeObjects(assocContext);
        bu.checkRegistryResponse(assocResponse);
        String cleanupQueryString = "SELECT * FROM ExtrinsicObject WHERE id = "+
                                    "('"+ id +"')";
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(cleanupQueryString);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
}
