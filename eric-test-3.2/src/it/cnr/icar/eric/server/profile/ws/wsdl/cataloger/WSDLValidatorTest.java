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
import it.cnr.icar.eric.common.exceptions.ValidationException;
import it.cnr.icar.eric.common.profile.ws.wsdl.CanonicalConstants;
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
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.xml.registry.JAXRException;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * JUnit TestCase for ContentCatalogingServiceImpl.  Uses the test Cataloging
 * service loaded with DemoDB as the service that the CMSManager invokes.
 */
public class WSDLValidatorTest extends ServerTest {
    protected static final PersistenceManager pm =
	PersistenceManagerFactory.getInstance().getPersistenceManager();
    
    /**
     * Constructor for ContentCatalogingServiceImplTest
     *
     * @param name
     */
    public WSDLValidatorTest(String name) {
        super(name);
        
    }

    public static Test suite() {
        boolean bypassCMS = Boolean.valueOf(RegistryProperties.getInstance()
                           .getProperty("it.cnr.icar.eric.server.lcm.bypassCMS", "false")).booleanValue();
        Test test = null;
        if (bypassCMS) {
            test = new TestSuite();
        } else {
            test = new TestSuite(WSDLValidatorTest.class);
        }
        return test;
    }

    private File createRegistryWSDLZipFileValidateSoapBinding() throws Exception {
        String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
        String[] relativeFilePaths = {
            "ebXMLRegistryServices_TestSoapBinding.wsdl",
            "ebXMLRegistryBindings.wsdl",
            "ebXMLRegistryInterfaces.wsdl",
        };       
        File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();       
        return zipFile;
    }
    
    private File createRegistryWSDLZipFileValidateSoapStyle() throws Exception {
        String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
        String[] relativeFilePaths = {
            "ebXMLRegistryServices_TestSoapStyle.wsdl",
            "ebXMLRegistryBindings.wsdl",
            "ebXMLRegistryInterfaces.wsdl",
	    // Currently, this test succeeds; we need all files for full validation
	    "lcm.xsd",
	    "query.xsd",
	    "rim.xsd",
	    "rs.xsd"
        };       
        File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();      
        return zipFile;
    }
    
    private File createRegistryWSDLZipFileValidateSoapTransport() throws Exception {
        String baseDir = getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/").toExternalForm();
        String[] relativeFilePaths = {
            "ebXMLRegistryServices_TestSoapTransport.wsdl",
            "ebXMLRegistryBindings.wsdl",
            "ebXMLRegistryInterfaces.wsdl",
        };       
        File zipFile = File.createTempFile("eric-testCreateZipOutputStream", ".zip");
        zipFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = Utility.createZipOutputStream(baseDir, relativeFilePaths, fos);
        zos.close();      
        return zipFile;
    }
    
    /*
     * This method will test that a WSDL doc does not have the binding set to
     * soap.binding. This will be invalid according to the WS Profile Validation Profile
     * rule 1
     */
    public void testValidateContentExtrinsicObjectValidateSoapBinding() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExtrinsicObject";
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType("application/zip");
        eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        File zipFile = createRegistryWSDLZipFileValidateSoapBinding();
        
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        
        URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());
        System.err.println("URL:" + wsdlURL.toExternalForm());
        DataHandler dh = new DataHandler(wsdlURL);
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        idToRepositoryItemMap.put(id, ri);
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExtrinsicObjectValidateSoapBinding", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);

        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            // If we got here with no exception, the validation failed
            fail("Validation of soap:binding element failed");
        } catch (ValidationException ex) {
            // Expected result
        }
    }
       
  
    /*
     * This method will test that a WSDL doc has the soap:binding
     * style attribute set to 'rpc'. This will be invalid according to the WS 
     * Profile Validation Profile rule 2
     * Note: the soap:binding rule is not currently enabled in the default 
     * InvocationControlFile_WSDLValidation.schematron file. Thus, the
     * file should pass the validation check
     */
    public void testValidateContentExtrinsicObjectValidateSoapStyle() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExtrinsicObject";
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType("application/zip");
        eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        File zipFile = createRegistryWSDLZipFileValidateSoapStyle();
        
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        
        URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());
        System.err.println("URL:" + wsdlURL.toExternalForm());
        DataHandler dh = new DataHandler(wsdlURL);
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        idToRepositoryItemMap.put(id, ri);
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExtrinsicObjectValidateSoapStyle", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
        } catch (ValidationException ex) {
            //Make sure that we did not get an exception. This is because the 
            //soap:binding style rule is not enabled in the default 
            //InvocationControlFile_WSDLValidation.schematron file. Thus, the
            //file should pass the validation check
            fail("Validation of soap:binding element failed");
	} catch (RuntimeException re) {
	    throw re;
        } catch (Exception e) {
	    // TODO: Improve wrapping of other Exception classes in validator
	    fail("Unexpected exception occurred");
	}
    }

    /*
     * This method will test that a WSDL doc has the soap:binding
     * transport attribute set to 'smtp'. This will be invalid according to the WS 
     * Profile Validation Profile rule 3
     */
    public void testValidateContentExtrinsicObjectValidateSoapTransport() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExtrinsicObject";
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        bu.addSlotsToRequest(submitRequest, dontVersionContentSlotsMap);
        eo.setMimeType("application/zip");
        eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        eo.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, eo);

        File zipFile = createRegistryWSDLZipFileValidateSoapTransport();
        
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        
        URL wsdlURL = new URL("file:///" + zipFile.getAbsolutePath());
        System.err.println("URL:" + wsdlURL.toExternalForm());
        DataHandler dh = new DataHandler(wsdlURL);
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        idToRepositoryItemMap.put(id, ri);
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExtrinsicObjectValidateSoapTransport", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            // If we got here with no exception, the validation failed
            fail("Validation of soap:binding element failed");
        } catch (JAXRException ex) {
            // Expected result
        }
    }
    
    /*
     * This method will test that a stand alone (no imports) WSDL doc does not have the binding set to
     * soap.binding. This will be invalid according to the WS Profile Validation Profile
     * rule 1
     */
    public void testValidateContentExternalLinkValidateSoapBinding() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExternalLink";
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExternalLinkType extLink = bu.rimFac.createExternalLinkType();
        extLink.setExternalURI(getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/ebXMLRegistryServices_TestSoapBindingNoImports.wsdl").toExternalForm());
        extLink.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        extLink.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, extLink);

        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExternalLinkValidateSoapBinding", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);          
            // If we got here with no exception, the validation failed
            fail("Validation of soap:binding element failed");
        } catch (JAXRException ex) {
            // Expected result
        } 
    }
    
    /*
     * This method will test that a stand alone (no imports) WSDL doc has the soap:binding
     * style attribute set to 'rpc'. This will be invalid according to the WS 
     * Profile Validation Profile rule 2
     */
    public void testValidateContentExternalLinkValidateSoapStyle() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExternalLink";
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExternalLinkType extLink = bu.rimFac.createExternalLinkType();
        //extLink.setExternalURI("http://uddi.org/wsdl/uddi_api_v3_binding.wsdl");
        extLink.setExternalURI(getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/ebXMLRegistryServices_TestSoapStyleNoImports.wsdl").toExternalForm());
        extLink.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        extLink.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, extLink);

        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExternalLinkValidateSoapStyle", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
        } catch (JAXRException ex) {
            //Make sure that we did not get an exception. This is because the 
            //soap:binding style rule is not enabled in the default 
            //InvocationControlFile_WSDLValidation.schematron file. Thus, the
            //file should pass the validation check
            fail("Validation of soap:binding element failed");
        }             
    }
    
    /*
     * This method will test that a stand alone (no imports) WSDL doc has the soap:binding
     * transport attribute set to 'smtp'. This will be invalid according to the WS 
     * Profile Validation Profile rule 3
     */
    public void testValidateContentExternalLinkValidateSoapTransport() throws Exception {
        RegistryResponseType resp = null;
        
        String id = "urn:freebxml:registry:test:WSDLValidatorTest:registryWSDLExternalLink";
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, null);        
        ExternalLinkType extLink = bu.rimFac.createExternalLinkType();
        //extLink.setExternalURI("http://uddi.org/wsdl/uddi_api_v3_binding.wsdl");
        extLink.setExternalURI(getClass().getResource("/it/cnr/icar/eric/server/profile/ws/wsdl/data/ebXMLRegistryServices_TestSoapTransportNoImports.wsdl").toExternalForm());
        extLink.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);
        extLink.setId(id);
        bu.addRegistryObjectToSubmitRequest(submitRequest, extLink);

        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        ServerRequestContext context = new ServerRequestContext("WSDLValidator:testValidateContentExternalLinkValidateSoapTransport", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
            // If we got here with no exception, the validation failed
            fail("Validation of soap:binding element failed");
        } catch (JAXRException ex) {
            // Expected result
        }
    }
}
