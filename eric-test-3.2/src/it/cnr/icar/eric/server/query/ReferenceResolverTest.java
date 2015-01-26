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

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 *
 * @author psterk
 */
public class ReferenceResolverTest extends ServerTest {
    
    /**
     * Creates a new instance of QueryFilterPluginTest
     */
    public ReferenceResolverTest(String name) {
        super(name);
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references only for the target object.
     */
    public void testResolveAllReferencesForStandaloneWSDLFile() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:StandaloneQueryFilterTest.wsdl";                
        @SuppressWarnings("unused")
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
            //submit(context, eo, idToRepositoryItemMap);
            submit(context, bu.rimFac.createExtrinsicObject(eo), idToRepositoryItemMap);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection<?> objRefs = refResolver.getReferencedObjects(context, eo);
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 2, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(null, id);
            } catch (Throwable t) {
                //ignore
            }
        }
    }

    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object.
     */
    public void testResolveReferencesAllForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // Get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection<?> objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 29, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references to one depth level for the target Test1.wsdl object.
     */
    public void testResolveReferencesDepth1ForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection<?> objRefs = refResolver.getReferencedObjects(context, ro, 1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 9, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }

    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object with an
     * includes assocation filter set to AssociationType:Imports
     */
    public void testResolveReferencesAssocIncludeFilterForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            RegistryProperties.getInstance().put(
                "eric.server.referenceResolver.associations.includeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Imports");
            
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection<?> objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 3, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test method verifies that the reference resolver will resolve
     * references to all depths for the target Test1.wsdl object with an
     * excludes assocation filter set to AssociationType:Contains
     */
    public void testResolveReferencesAssocExcludeFilterForWSDLFileWithImport() throws Exception {
        // Upload test WSDL file to compress
        String id = "urn:your:urn:goes:here:ContentCompressOneImportWSDLZip";
        String id1 = "urn:your:urn:goes:here:topDir:test1:Test1.wsdl";
        try {
            RegistryProperties.getInstance().put(
                "eric.server.referenceResolver.associations.excludeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains");
            
            // Submit the test zip file
            ServerRequestContext context = submitTestZipFile(id);
            
            // The get Test1.wsdl EO
            RegistryObjectType ro = QueryManagerFactory.getInstance().getQueryManager().getRegistryObject(context, id1);
            
            // Use ReferenceResolver to resolve all references
            ReferenceResolver refResolver = new ReferenceResolverImpl();
            Collection<?> objRefs = refResolver.getReferencedObjects(context, ro, -1);
            
            // Verify you got all references
            assertEquals("testQueryWithCompressContentRequest.", 3, objRefs.size());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                cleanupFiles(id, id1);
            } catch (Throwable t) {
                //ignore
            }
        }
    }
    
    /*
     * This test verifies that the Reference Resolver can handle circular references
     * such as those that exist with the urn:freebxml:registry:demoDB:acp:folderACP1
     * object. This object has an AccessControlPolicyFor for the folder1 RP.
     * The folder1 RP has a hasMember association with the folderACP1 EO.
     * Thus, circular references exist in this case.
     */
    @SuppressWarnings("static-access")
	public void testResolveAllReferencesForXACMLPolicyFile() throws Exception {
        // Upload test WSDL file to compress
        String id1 = "urn:freebxml:registry:demoDB:acp:folderACP1";
        try {
            RegistryProperties.getInstance().put(
                "eric.server.referenceResolver.associations.excludeFilterList.urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL", 
                "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains");
            
            // Create the ServerRequestContext
            SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);
            ServerRequestContext context = new ServerRequestContext("ReferenceResolverTest:testResolveAllReferencesForXACMLPolicyFile", submitRequest);
            
            // Get folderACP1 EO
            RegistryObjectType ro = QueryManagerFactory.getInstance()
                                                       .getQueryManager().getRegistryObject(context, id1);
            if (ro == null) {
                this.assertNull("This test method requires that the demoDB is installed", ro);
            } else {
                // Use ReferenceResolver to resolve all references
                ReferenceResolver refResolver = new ReferenceResolverImpl();
                Collection<?> objRefs = refResolver.getReferencedObjects(context, ro, -1);

                // Verify you got all references
                assertEquals("testQueryWithCompressContentRequest.", 4, objRefs.size());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void cleanupFiles(String zipFileId, String id1) throws Exception {
        String cleanupAssocQueryString = "SELECT * FROM Association WHERE sourceObject = "+
                                    "('"+ id1 +"')";
        RemoveObjectsRequest removeAssocRequest = createRemoveObjectsRequest(cleanupAssocQueryString);
        ServerRequestContext assocContext = new ServerRequestContext("RepositoryTest:testDelete", removeAssocRequest);
        assocContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType assocResponse = lcm.removeObjects(assocContext);
        bu.checkRegistryResponse(assocResponse);
        String cleanupQueryString = "SELECT * FROM ExtrinsicObject WHERE id = "+
                                    "('"+ id1 +"')";
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(cleanupQueryString);
        ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType response = lcm.removeObjects(context);
        bu.checkRegistryResponse(response);
    }
    
    private ServerRequestContext submitTestZipFile(String id) throws Exception {
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
        //submit(context, eo, idToRepositoryItemMap);
        submit(context, bu.rimFac.createExtrinsicObject(eo), idToRepositoryItemMap);
        
        return context;
    }
}
