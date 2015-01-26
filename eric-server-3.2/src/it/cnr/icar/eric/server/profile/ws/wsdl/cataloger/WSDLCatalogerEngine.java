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

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.URN;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.cms.CatalogingServiceEngine;
import it.cnr.icar.eric.common.cms.CatalogingServiceInput;
import it.cnr.icar.eric.common.cms.CatalogingServiceOutput;
import it.cnr.icar.eric.common.exceptions.CatalogingException;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.profile.ws.wsdl.CanonicalConstants;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;



/**
 * Extracts information from an image RepositoryItem and adds it to the
 * OriginalContent as named slots.
 *
 * @author Farrukh.Najmi@sun.com
 */
public class WSDLCatalogerEngine implements CatalogingServiceEngine {
    
    private HashMap<String, String> dontVersionSlotsMap = new HashMap<String, String>();
        
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    
    private static final Log log = LogFactory.getLog(WSDLCatalogerEngine.class.getName());
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
    private static BindingUtility bu = BindingUtility.getInstance();
    
    WSDLDocument wsdlDocument = null;
    
    //HashMap where key is id and value is the RIM object representing a WSDL object 
    HashMap<String, RegistryObjectType> idToRIMMap = new HashMap<String, RegistryObjectType>();
    
    //HashMap where key is id and value is the WSDL object 
    HashMap<String, Element> idToWSDLMap = new HashMap<String, Element>();
    
    HashSet<RegistryObjectType> registryObjects = new HashSet<RegistryObjectType>();
    HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
    // This Map holds the files that are contained in a single zip file. It is
    // stored here so that any file in the zip file can create an Imports
    // assocation with any other file
    Map<String, File> idToFileMap = new HashMap<String, File>();
    
    //key: relativeFileName, value: id for ExtrinsicObject for that file
    //TODO: refactor to AbstractCatalogerEngine
    HashMap<Object, Object> fileNameToIdMap = new HashMap<Object, Object>();        
    
    CatalogingServiceInput input = null;
    CatalogingServiceOutput output = null;
    HashMap<Object, Object> namespaceLookup = new HashMap<Object, Object>();
    String currentRelativeFilename = null;
    
    @SuppressWarnings("static-access")
	WSDLCatalogerEngine() {
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
    }
    
    //Spec Issue: What about versioning profile?? turned off??
    // TODO: verify thread safety
    public CatalogingServiceOutput catalogContent(CatalogingServiceInput input) throws CatalogingException {
        try {
            this.input = input;
            resetState();
            //registryObject MUST be ExrinsicObject or ExternalLink of objectType WSDL
            RegistryObjectType registryObject = input.getRegistryObject();
            if (registryObject instanceof ExtrinsicObjectType) {
                ExtrinsicObjectType eo = (ExtrinsicObjectType)input.getRegistryObject();
                DataHandler repositoryItem = input.getRepositoryItem();
                InputSource inputSource = new InputSource(repositoryItem.getInputStream());
                if (eo.getMimeType().equalsIgnoreCase("text/xml")) {
                    performWSDLCataloging(eo, inputSource, true);
                } else {
                    catalogWSDLExtrinsicObject(eo, inputSource);
                }
            } else if (registryObject instanceof ExternalLinkType) {
                ExternalLinkType extLink = (ExternalLinkType)input.getRegistryObject();
                String urlStr = extLink.getExternalURI();
                String wsdlLoc = Utility.absolutize(Utility.getFileOrURLName(urlStr));
                InputSource inputSource = new InputSource(wsdlLoc);
                catalogWSDLExternalLink(extLink, inputSource);
            } else {
                throw new CatalogingException(ServerResourceBundle.getInstance()
                                                                  .getString("message.ExtrinsicObjectOrExternalLinkExpected", 
                                                                             new Object[]{registryObject.getObjectType()}));
            }
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CatalogingException(e);
        }

         registryObjects.addAll(idToRIMMap.values());
        output = new CatalogingServiceOutput(registryObjects, idToRepositoryItemMap);
        return output;
    }
    
    /*
     * This method is used to reset the class attributes at the beginning of 
     * each call to catalogContent(CatalogingServiceInput input).
     */
    private void resetState() {
        idToRIMMap.clear();
        idToWSDLMap.clear();
        registryObjects.clear();
        idToRepositoryItemMap.clear();
        idToFileMap.clear();
    }
    
    /**
     * Catalogs WSDL when submitted as an ExtrinsicObject - RepositoryItem pair.
     *
     */
    private void catalogWSDLExtrinsicObject(ExtrinsicObjectType eo, 
                                            InputSource inputSource) 
        throws CatalogingException {
        try {
            if (eo.getMimeType().equalsIgnoreCase("application/zip")) {
                processZipFile(eo, inputSource);
            } else {
                //Following will fail if there are unresolved imports in the WSDL
                performWSDLCataloging(eo, inputSource);
            }
        } catch (CatalogingException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e, e);
            throw new CatalogingException(e);
        }
    }
    
    /**
     * Assign a contentLocator slot to support URL access for the repositoryItem associated with
     * specified ExtrinsObject.
     */
    @SuppressWarnings("static-access")
	private void assignURL(ExtrinsicObjectType eo, String urlSuffix) throws JAXBException {
        //Assign URL to ExtrinsicObject based upon its targetNamespace
        HashMap<String, String> slotsMap = new HashMap<String, String>();
        slotsMap.put(bu.CANONICAL_SLOT_CONTENT_LOCATOR, urlSuffix);
        bu.addSlotsToRegistryObject(eo, slotsMap);
    }
    
    /**
     * Catalogs XMLSchema when submitted as an ExtrinsicObject - RepositoryItem pair.
     *
     */
    private void catalogXMLSchemaExtrinsicObject(RegistryObjectType ro,
                                                 InputSource source) 
        throws CatalogingException {
        try { 
            registryObjects.add(ro);
            Document document = parseXML(source);
            Element schemaElement = document.getDocumentElement();
            String documentLocalName = schemaElement.getLocalName();
            String documentNamespaceURI = schemaElement.getNamespaceURI();
            if (documentLocalName.equalsIgnoreCase("schema") && 
                documentNamespaceURI.endsWith("XMLSchema")) {
                Attr attribute = schemaElement.getAttributeNode("targetNamespace");
                String namespaceURI = attribute.getValue();
                // Set the id for the XMLSchema EO
                updateRegistryObjectId(namespaceURI, ro, false);
                // Check if this XSD file imports another file (usually XSD)
                NodeList nodeList = schemaElement.getChildNodes();
                int length = nodeList.getLength();
                for (int i = 0; i < length; i++) {
                    Node node = nodeList.item(i);
                    String localName = node.getLocalName();
                    if (localName != null && localName.equalsIgnoreCase("import")) {
                        // This XSD imports another file
                        NamedNodeMap importNamedNodeMap = node.getAttributes();
                        Node namespaceNode = importNamedNodeMap.getNamedItem("namespace");
                        String importNamespace = null;
                        if (namespaceNode != null) {
                            importNamespace = namespaceNode.getNodeValue();
                         }
                        String schemaLocation = null;
                        Node schemaLocationNode = importNamedNodeMap.getNamedItem("schemaLocation");
                        if (schemaLocationNode != null) {
                            schemaLocation = schemaLocationNode.getNodeValue();
                        }
                        RegistryObjectType importedObject = catalogImportStatement(ro, importNamespace, schemaLocation);
                        createImportsAssociation(ro, importedObject);
                    }
                }
            }
        } catch (CatalogingException e) {
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            CatalogingException ce = new CatalogingException(e);
            throw ce;
        }
    }
    
    /*
     * Method creates EO for imported files.
     */
    private RegistryObjectType catalogImportStatement(RegistryObjectType ro,
                                                      String importedNamespace, 
                                                      String location) 
        throws CatalogingException {
        // TODO: handle cases when xsd:include and xsd:redefine are used and
        // there is no namespace attribute. Use the targetNamespace attribute
        // of the <schema> element in the <>.xsd file.
        RegistryObjectType ebImportedRegistryObjectType = null;
        //if (importedNamespace == null && location == location) {
        if (importedNamespace == null) {
            // Ignore these cases: <xsd:import/>
            return null;
        }
        try {
            String entryId = null;
            if (location != null) {
                entryId = location;
                ebImportedRegistryObjectType = getRegistryObject(entryId);
            }
            if (ebImportedRegistryObjectType == null) {
                if (importedNamespace != null && entryId != null) {
                    String absoluteFileName = getCompleteRelativeFileName(entryId);
                    String qualifiedId = getQualifiedId(importedNamespace, absoluteFileName);
                    ebImportedRegistryObjectType = getRegistryObject(qualifiedId);
                }
            }
            if (ebImportedRegistryObjectType == null) {
                if (location != null) {
                    ebImportedRegistryObjectType = createExtrinsicObject(location);
                }
            }
            if (ebImportedRegistryObjectType == null) {          
                // Check if namespace or schemaLocation is an absolute URL.
                // If it is, create ExternalLink to the file
                URL schemaLocationURL = null;                   
                String id = null;
                String namespaceURN = null;
                if (location == null) {
                    // Support case of just namespace attribute. e.g.
                    // <xsd:import namespace="http://www.w3.org/2001/xml.xsd"/  
                    URN urn = new URN(importedNamespace);
                    urn.makeValid();
                    namespaceURN = urn.getURN();
                    location = importedNamespace;
                } else {
                    if (importedNamespace != null) {
                        try {
                            URN urn = new URN(importedNamespace);
                            urn.makeValid();
                            namespaceURN = urn.getURN();
                        } catch (URISyntaxException e) {
                            // Presume that the namespace is a URN
                            namespaceURN = importedNamespace;
                        } 
                    }
                    try {
                        schemaLocationURL = new URL(location);
                        id = Utility.fixURN(namespaceURN + schemaLocationURL.getPath());
                    } catch (MalformedURLException e) {
                        if (ro instanceof ExtrinsicObjectType) {                            
                            // Error is either a malformed absolute URL or relative
                            // URL to a file not submitted in this request. 
                            // Provide messages to cover both cases.                            
                            String message1 = ServerResourceBundle.getInstance()
                                                                  .getString("message.error.missingFile", 
                                                                             new Object[]{location});
                            String message2 = ServerResourceBundle.getInstance()
                                                                  .getString("message.error.couldNotResolveURI", 
                                                                             new Object[]{location});

                            String finalMessage = ServerResourceBundle.getInstance()
                                                                 .getString("message.error.errorPossibleCauses",
                                                                             new Object[]{message1, message2});
                            throw new CatalogingException(finalMessage, e);
                        } else {
                            // Create id to be used in lookup/creation of 
                            // an ExternalLinkType object below
                            id = Utility.fixURN(namespaceURN + ":" + location);
                        }
                    }
                }

                // Has this link been created previously during this request?
                ebImportedRegistryObjectType = idToRIMMap.get(id);
                if (ebImportedRegistryObjectType == null) {
                    // Does this link already exist in the Registry?
                    ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetRegistryObject", null);
                    context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
                    try {
                        ebImportedRegistryObjectType = 
                            QueryManagerFactory.getInstance()
                                                                 .getQueryManager()
                                                                 .getRegistryObject(context, 
                                                                                    id, 
                                                                                    "ExternalLink");
                    } catch (ObjectNotFoundException e) {
                        // Object does not exist. Create it below
                    	ebImportedRegistryObjectType = null;
                    } finally {
                        if (context != null) {
                            context.rollback();
                        }
                    }
                }
                if (ebImportedRegistryObjectType == null) {
                    if (Utility.getInstance().isValidURI(location)) {
                        // Create ExternalLink         
                        ebImportedRegistryObjectType = bu.rimFac.createExternalLinkType();
                        ebImportedRegistryObjectType.setId(id);

                        ((ExternalLinkType)ebImportedRegistryObjectType).setExternalURI(location);
                        setObjectAndMimeType(ebImportedRegistryObjectType, location);
                        idToRIMMap.put(location, ebImportedRegistryObjectType);
                    } else {
                        log.warn(ServerResourceBundle.getInstance()
                                                     .getString("message.error.invalidURL", 
                                                                 new Object[] {location}));
                    }
                }
            } else {
                updateRegistryObjectId(importedNamespace, ebImportedRegistryObjectType, false);
            }
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
        return ebImportedRegistryObjectType;
    }
    
    private String getServiceId(Element service) throws CatalogingException {
        String targetNamespace = wsdlDocument.getTargetNamespaceURI(service, WSDLConstants.QNAME_SERVICE);
        String nameStr = wsdlDocument.getAttribute(service, WSDLConstants.ATTR_NAME);
        String id = Utility.fixURN(targetNamespace + ":service:" + nameStr);
        return id;
    }
    
    private String getPortId(Element port) throws CatalogingException {
        String targetNamespace = wsdlDocument.getTargetNamespaceURI(port, WSDLConstants.QNAME_PORT);
        String nameStr = wsdlDocument.getAttribute(port, WSDLConstants.ATTR_NAME);
        String id = Utility.fixURN(targetNamespace + ":port:" + nameStr);
        return id;
    }
   
    private String getBindingId(Element binding) throws CatalogingException {
        String targetNamespace = wsdlDocument.getTargetNamespaceURI(binding, WSDLConstants.QNAME_BINDING);
        String nameStr = wsdlDocument.getAttribute(binding, WSDLConstants.ATTR_NAME);
        String id = Utility.fixURN(targetNamespace + ":binding:" + nameStr);
        return id;
    }
    
    private String getPortTypeId(Element portType) throws CatalogingException {
        String targetNamespace = wsdlDocument.getTargetNamespaceURI(portType, WSDLConstants.QNAME_PORT_TYPE);
        String nameStr = wsdlDocument.getAttribute(portType, WSDLConstants.ATTR_NAME);
        String id = Utility.fixURN(targetNamespace + ":portType:" + nameStr);
        return id;
    }
    
    /*
     * Processes the files contained in the ZIP file and catalogs each item.
     *
     */
    private void processZipFile(ExtrinsicObjectType zipEO, InputSource inputSource) 
        throws JAXRException, JAXBException, IOException, CatalogingException {
        // This method unzips the zip file and places all files in the idToFileMap
        // This method also places all files from a zip file in a Map. This is done so 
        // that a file can be looked up at any time during the processing of any other file
        Collection<File> files = addZipEntriesToMap(zipEO, inputSource);
        //Now iterate and create ExtrinsicObject - Repository Item pair for each unzipped file
        
        Iterator<File> iter = files.iterator();
        while (iter.hasNext()) {
            File file = iter.next();
            @SuppressWarnings("unused")
			String fileName = file.getName();
            String fileNameAbsolute = file.getAbsolutePath();
            String tmp_dir = TMP_DIR;
            if(!tmp_dir.endsWith(File.separator)){
                tmp_dir = tmp_dir+File.separator;
            }
            String fileNameRelative = fileNameAbsolute.substring(tmp_dir.length(), fileNameAbsolute.length());    
            // TODO: the id below is a temporary id. Create final id using 
            // namespace of the wsdl:service
            // Assign repository item to idToRepositoryItemMap
            idToRepositoryItemMap.put(fileNameRelative, new RepositoryItemImpl(fileNameRelative, new DataHandler(new FileDataSource(file))));
            // Get the ExtrinsicObjectType instance for this file using the 
            // relative file name as the key            
            currentRelativeFilename = fileNameRelative;
            ExtrinsicObjectType eo = createExtrinsicObject(fileNameRelative);
            // Create the InputSource
            String urlStr = fileNameAbsolute;
            urlStr = Utility.absolutize(Utility.getFileOrURLName(urlStr));
            inputSource = new InputSource(urlStr);
            if (eo.getObjectType().equals(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL)) {
                catalogWSDLExtrinsicObject(eo, inputSource);
            } else if (eo.getObjectType().equals(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_XMLSchema)) {
                catalogXMLSchemaExtrinsicObject(eo, inputSource);
            }
        }
    }
    
    /*
     * Convenience method for retrieving a stored RegistryObjectType
     */
    private RegistryObjectType getRegistryObject(String id) {
        return idToRIMMap.get(id);
    }
    
    /*
     * Given a relative filename, create an ExtrinsicObjectType.
     * If there is no File with fileNameRelative in the file map
     * this method will return null
     */
    private ExtrinsicObjectType createExtrinsicObject(String fileNameRelative) 
        throws CatalogingException {
        ExtrinsicObjectType ebExtrinsicObjectType = null;
        try {
            String absoluteFileName = getCompleteRelativeFileName(fileNameRelative);
            ebExtrinsicObjectType = (ExtrinsicObjectType)getRegistryObject(absoluteFileName);
            if (ebExtrinsicObjectType == null) {
                // Get the filename
                File file = idToFileMap.get(absoluteFileName);
                if (file != null) {
                    // Create the ExtrinsicObjectType instance
                    ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
                    bu.addSlotsToRegistryObject(ebExtrinsicObjectType,  dontVersionSlotsMap);
                    // Add wsdl:Service namespace to this Id later during parsing
                    // of WSDL file
                    ebExtrinsicObjectType.setId(absoluteFileName);
                    String fileName = file.getName();
                    // Add fileName as name of eo
                    ebExtrinsicObjectType.setName(bu.getName(fileName));
                    // Add metadata
                    setObjectAndMimeType(ebExtrinsicObjectType, fileName);
                    assignURL(ebExtrinsicObjectType, absoluteFileName);
                    idToRIMMap.put(absoluteFileName, ebExtrinsicObjectType);                
                }
            }
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
        return ebExtrinsicObjectType;
    }
    
    /*
     * This method takes a relative file name from an imported file reference
     * and constructs the complete relative file name by removing '../' and './'
     * and adding preceding relative path. See example below.
     */
    private String getCompleteRelativeFileName(String relativeFileName) {
        if (Utility.FILE_SEPARATOR.equalsIgnoreCase("\\")) {                
            // Convert '/' to Windows file separator
            relativeFileName = relativeFileName.replaceAll("/", "\\\\");
        }
        String completeRelativeFileName = relativeFileName;
        // Check if this is a URL first
        try {
            new URL(relativeFileName);
            // This is a URL. Do not process
            return completeRelativeFileName;
        } catch (MalformedURLException ex) {
           // Not a URL. Continue processing
        }
        if (this.currentRelativeFilename != null) {
            // if relativeFileName starts with './' or '../' complete it:
            // Get directory of currently processed filename
            // Do directory update. For example:
            // topDir/dir1/file1.wsdl
            // topDir/dir2/file2.wsdl
            // file2.wsdl has import to ../dir1/file1.wsdl
            // Do following:
            // 1. Calculate directory of file doing the importing
            // In this example, file2.wsdl does import, its dir is: /topDir/dir2/
            // 2. Append relative import to file1.wsdl to file2.wsdl's directory:
            // topDir/dir2/../dir1/file1.wsdl
            // 3. Resolve relative path to file1.wsdl
            // Replace the '../' and the directory above it.
            // topDir/dir2/../dir1/file1.wsdl -> topDir/dir1/file1.wsdl
            // Note, the algorithm will handle any number of '../' in the path        
            int index = currentRelativeFilename.lastIndexOf(Utility.FILE_SEPARATOR);
            String currentRelativeDir = "";
            if (index != -1) {
                currentRelativeDir = currentRelativeFilename.substring(0, index+1);
            }
            String relativeDir = "";
            int index2 = relativeFileName.lastIndexOf(Utility.FILE_SEPARATOR);
            if (index2 != -1) {
                relativeDir = relativeFileName.substring(0, index2+1);
            }
            if (!currentRelativeDir.equalsIgnoreCase(relativeDir)) {
                // In other words, if you do not have the same directory path to
                // both files, continue below
                String fullRelativePath = currentRelativeDir + relativeFileName;
                String[] dirs = null;
                if (Utility.FILE_SEPARATOR.equalsIgnoreCase("\\")) {
                    // Handle Windows dir separator
                    dirs = fullRelativePath.split("\\\\");
                } else {
                    dirs = fullRelativePath.split(Utility.FILE_SEPARATOR);
                }
                StringBuffer completeRelativePath = new StringBuffer();
                for (int i = 0; i < dirs.length; i++) {
                    int completeRelativePathLength = completeRelativePath.length();
                    if (dirs[i].equalsIgnoreCase("..")) {
                        // delete the previously appended dir
                        if (i > 0) {
                            if (completeRelativePathLength > 0) {
                                int lastDirIndex = completeRelativePath.lastIndexOf(Utility.FILE_SEPARATOR);
                                if (lastDirIndex == -1) {
                                    lastDirIndex = 0;
                                }
                                completeRelativePath = completeRelativePath.delete(lastDirIndex, 
                                                                                   completeRelativePathLength);
                            }
                        }
                    } else if (i < dirs.length-1 && dirs[i].equalsIgnoreCase(".")) { 
                        continue;
                    } else {
                        if (i > 0 && completeRelativePathLength > 0) {
                            completeRelativePath.append(Utility.FILE_SEPARATOR);
                        }
                        completeRelativePath.append(dirs[i]);
                    }
                }
                completeRelativeFileName = completeRelativePath.toString();
            }
        }
        return completeRelativeFileName;
    }
    
    /*
     * This method unzips the zip file into a tmp directory.
     * This method also places all files from a zip file in a Map. This is done so 
     * that a file can be looked up at any time during the processing of n number
     * of zipped files.  This is required, for example, when you need to create
     * an assocation between one XSD file that imports another XSD file, but the
     * dependent file has not been processed yet.
     */
    @SuppressWarnings("unused")
	private Collection<File> addZipEntriesToMap(ExtrinsicObjectType zipEO, InputSource inputSource) 
        throws IOException {
        //Unzip the file in tmp dir
        ArrayList<File> files = Utility.unZip(TMP_DIR, inputSource.getByteStream());
        Iterator<File> iter = files.iterator();
        while (iter.hasNext()) {
            File file = iter.next();
            String fileName = file.getName();
            String fileNameAbolute = file.getAbsolutePath();
            //String fileNameRelative = fileNameAbolute.substring(TMP_DIR.length()+1, fileNameAbolute.length());
            String fileNameRelative = fileNameAbolute.substring(TMP_DIR.length(), fileNameAbolute.length());
            idToFileMap.put(fileNameRelative, file);
        }
        return idToFileMap.values();
    }
    
    
    private void setObjectAndMimeType(RegistryObjectType eo, String fileName) throws CatalogingException {
        //TODO: Need to make a utility method and somehow leverage what admin tool does
        String fileType = fileName.substring(fileName.lastIndexOf('.'), fileName.length());            
        if (fileType.equalsIgnoreCase(".wsdl")) {
            eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL);           
            if (eo instanceof ExtrinsicObjectType) {
                ((ExtrinsicObjectType)eo).setMimeType("text/xml");
            }
        } else if (fileType.equalsIgnoreCase(".xsd")) {
            eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_XMLSchema);
            if (eo instanceof ExtrinsicObjectType) {
                ((ExtrinsicObjectType)eo).setMimeType("text/xml");
            }
        } else {
            eo.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
            if (eo instanceof ExtrinsicObjectType) {
                ((ExtrinsicObjectType)eo).setMimeType("application/octet-stream");
            }
            log.warn(ServerResourceBundle.getInstance()
                                         .getString("message.error.unsupportedFileType",
                                                     new Object[] {fileName}));
        }
        
    }
    
    /**
     * Catalogs WSDL when submitted as an ExternalLink.
     *
     */
    private void catalogWSDLExternalLink(ExternalLinkType extLink, 
                                         InputSource inputSource) 
        throws CatalogingException {
        performWSDLCataloging(extLink, inputSource, true);                
    }
    
    /*
     * Method used to perform cataloging of WSDL zip file
     */
    
    private void performWSDLCataloging(RegistryObjectType wsdlEO, 
                                       InputSource inputSource) 
    throws CatalogingException {
        performWSDLCataloging(wsdlEO, inputSource, false);
    }
    
    /*
     * Method used to perform cataloging on either standalone (.wsdl file 
     * submitted) or a wsdl file submitted in a .zip file.
     * Set standaloneRO parameter to 'true' if submitting a standalone wsdl
     */
    private void performWSDLCataloging(RegistryObjectType wsdlRO, 
                                       InputSource inputSource, 
                                       boolean standaloneRO) 
    throws CatalogingException {
        // Do not persist standalone ROs. This was done previously by LCM.
        if (!standaloneRO) {
            registryObjects.add(wsdlRO);
        }
        
        try {
            wsdlDocument = new WSDLDocument(inputSource);
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
        
        addImportedNamespacesSlot(wsdlRO);
        catalogTargetNamespace(wsdlRO);

        List<Element> serviceElements = wsdlDocument.getElements(WSDLConstants.QNAME_SERVICE);
        Iterator<Element> serviceElementItr = serviceElements.iterator();
        @SuppressWarnings("unused")
		String namespaceURI = null;
        while (serviceElementItr.hasNext()) {
            Element serviceElement = serviceElementItr.next();
            // Pass EO to this method so as to create assocation of type Imports
            processService(serviceElement);
            // Set the id for the WSDL EO
            updateRegistryObjectId(wsdlDocument.getTargetNamespaceURI(), wsdlRO, standaloneRO);
            // Create a Contains association between the WSDL EO 
            // and the wsdl:service contained therein
            createContainsAssociation(wsdlRO, getServiceId(serviceElement));
        }

        List<Element> bindings = wsdlDocument.getElements(WSDLConstants.QNAME_BINDING);
        Iterator<Element> bindingElementItr = bindings.iterator();
        while (bindingElementItr.hasNext()) {
            Element bindingElement = bindingElementItr.next();
            // Pass EO to this method so as to create assocation of type Imports
            processBinding(bindingElement);
            // Set the id for the WSDL EO
            updateRegistryObjectId(wsdlDocument.getTargetNamespaceURI(), wsdlRO, standaloneRO);
            // Create a Contains association between the WSDL EO 
            // and the wsdl:binding contained therein
            createContainsAssociation(wsdlRO, getBindingId(bindingElement));
        }
        
        List<Element> portTypes = wsdlDocument.getElements(WSDLConstants.QNAME_PORT_TYPE);
        Iterator<Element> portTypeElementItr = portTypes.iterator();
        while (portTypeElementItr.hasNext()) {
            Element portTypeElement = portTypeElementItr.next();
            processPortType(portTypeElement);
            // Set the id for the WSDL EO
            updateRegistryObjectId(wsdlDocument.getTargetNamespaceURI(), wsdlRO, standaloneRO);
            // Need to create a Contains association between the WSDL EO 
            // and the wsdl:portType contained therein
            createContainsAssociation(wsdlRO, getPortTypeId(portTypeElement));
            // Determine if this WSDL file imports other WSDL files
            // catalogImportStatements(wsdlRO, portType);
            // Resolve any <types> imports
            resolveTypeImports(portTypeElement, wsdlRO); 
        }
        catalogImportStatements(wsdlRO, wsdlDocument);
    }          
    
    /*
     * This method is used to replace a non-qualified RO Id with a qualified one
     * It also updates entries in the RIM and repository maps, using the 
     * qualified key as the new id
     */
    private void updateRegistryObjectId(String namespaceURI, 
                                        RegistryObjectType eo, 
                                        boolean standaloneRO) 
        throws CatalogingException {
        String eoId = eo.getId();
        if (! isURNValid(eoId)) {
            //TODO: Need to use systemId for the repositoryItem in future instead of eoId
            String localPartEOId = Utility.getLocalPart(eoId); 
            String qualifiedId = getQualifiedId(namespaceURI, localPartEOId);
            eo.setId(qualifiedId);
            // Do not persist standalone ROs - they have been previously 
            // persisted by the LCM
            if (!standaloneRO) {
                idToRIMMap.remove(eoId);
                idToRIMMap.put(qualifiedId, eo);
                RepositoryItemImpl ri = (RepositoryItemImpl)idToRepositoryItemMap.get(eoId);
                if (ri != null) {
                    ri.setId(qualifiedId);
                    idToRepositoryItemMap.remove(eoId);
                    idToRepositoryItemMap.put(qualifiedId, ri);
                }
            }
        }
    }
    
    private boolean isURNValid(String urnString) {
        boolean isURNValid = true;
        try {
            URN urn = new URN(urnString);
            urn.validate();
        } catch (URISyntaxException e) {
            isURNValid = false;
        }
        return isURNValid;
    }
    
    /*
     * This method creates a qualified id as a valid URN from a namespaceURI 
     * and an existing id. If the id starts with the namespace URN, then presume 
     * that the Id is already qualified and return it.
     */
    public String getQualifiedId(String namespaceURI, String id) 
        throws CatalogingException {
        if (id == null) {
            throw new CatalogingException(ServerResourceBundle.getInstance()
                                                              .getString("message.error.registryObjectIdIsNull"));
        }
        
        String qualifiedId = null;
        if (namespaceURI == null) {
            qualifiedId = id;
        } else {
            qualifiedId = namespaceURI + ":" + id;
        }

        //make sure id is a valid URN
        qualifiedId = makeValidURN(qualifiedId);
        
        return qualifiedId;
    }
     
    /*
     * This method takes an RO Id and returns a qualified Id as a valid URN
     */
    public String makeValidURN(String id) throws CatalogingException {
        String qualifiedId = null;
        URN urn = new URN(id);
        try {
            urn.makeValid();                
            qualifiedId = urn.getURN();
        } catch (URISyntaxException ex) {
            log.warn(ex, ex);
            throw new CatalogingException(ex);
        }
        return qualifiedId;
    }
    /*
     * This method catalogs content from <wsdl:import> elements
     */    
     private void catalogImportStatements(RegistryObjectType wsdlEO, WSDLDocument wsdlDoc) 
        throws CatalogingException {
        Iterator<Element> itr = wsdlDoc.getElements(WSDLConstants.QNAME_IMPORT).iterator();
        while (itr.hasNext()) {
            // Import maps to a wsdl:import element
            Element impt = itr.next();
            String namespace = impt.getAttribute(WSDLConstants.ATTR_NAMESPACE);
            String location = impt.getAttribute(WSDLConstants.ATTR_LOCATION);
            RegistryObjectType importedObject = catalogImportStatement(wsdlEO, namespace, location);        
            // Create Imports association between the current WSDLEO and 
            // the WSDL EO that it imports
            createImportsAssociation(wsdlEO, importedObject);
        }
    }
    
    private void createContainsAssociation(RegistryObjectType wsdlEO, String rimId) 
        throws CatalogingException {
        try {         
            AssociationType1 ebAssociationType = bu.createAssociationType(wsdlEO.getId(), 
                                                        rimId, 
                                                        CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Contains);
            //May need a more deterministic algorithm for setting this id??
            ebAssociationType.setId(wsdlEO.getId()+":Contains:"+rimId);
            registryObjects.add(ebAssociationType);
        } catch (Throwable t) {
            throw new CatalogingException(t);
        }
    }
    
    private void createImportsAssociation(RegistryObjectType wsdlEO, RegistryObjectType importedWSDLEO) 
        throws CatalogingException {
        if (wsdlEO != null && importedWSDLEO != null) {
            try {
                AssociationType1 ebAssociationType = bu.createAssociationType(wsdlEO.getId(), 
                                                            importedWSDLEO.getId(), 
                                                            CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Imports);
                //May need a more deterministic algorithm for setting this id??
                ebAssociationType.setId(wsdlEO.getId()+":Imports:"+importedWSDLEO.getId());
                registryObjects.add(ebAssociationType);
            } catch (Throwable t) {
                throw new CatalogingException(t);
            }
        }
    }
    
    /*
     * Method adds canonical ws profile referenced namespaces constant to slot
     * and added to RO
     */
    @SuppressWarnings("static-access")
	private void addImportedNamespacesSlot(RegistryObjectType registryObject) throws CatalogingException {
        try {
            SlotType1 ebSlotType = bu.rimFac.createSlotType1();
            ebSlotType.setName(CanonicalConstants.CANONICAL_SLOT_WSDL_PROFILE_REFERENCED_NAMESPACES);
            ebSlotType.setSlotType(bu.CANONICAL_DATA_TYPE_ID_String);
            // document get all namespaces
            Collection<?> nameSpaceURIs = wsdlDocument.getAllNamespaceURIs();
            ValueListType enValueListType = bu.rimFac.createValueListType();
            Iterator<?> iter = nameSpaceURIs.iterator();
            while (iter.hasNext()) {
                enValueListType.getValue().add((String)iter.next());                    
            }
            ebSlotType.setValueList(enValueListType);
            
            registryObject.getSlot().add(ebSlotType);
        } catch (Exception e) {
            log.error(e, e);
            throw new CatalogingException(e);
        }
    }
    
    /*
     * Method adds canonical ws profile target namespaces constant to slot
     * and added to RO
     */
    @SuppressWarnings("static-access")
	private void catalogTargetNamespace(RegistryObjectType registryObject) throws CatalogingException {
        try {
            SlotType1 ebSlotType1 = bu.rimFac.createSlotType1();
            ebSlotType1.setName(CanonicalConstants.CANONICAL_SLOT_WSDL_PROFILE_TARGET_NAMESPACE);
            ebSlotType1.setSlotType(bu.CANONICAL_DATA_TYPE_ID_String);
            
            String targetNamespace = wsdlDocument.getTargetNamespaceURI();
            //TODO: get target namespace from imported docs
            
            ValueListType ebValueListType = bu.rimFac.createValueListType();            
            ebValueListType.getValue().add(targetNamespace);                    
            ebSlotType1.setValueList(ebValueListType);

            registryObject.getSlot().add(ebSlotType1);
        } catch (Exception e) {
            log.error(e, e);
            throw new CatalogingException(e);
        }
    }
    
    /*
     * Method is used to catalog a wsdl:service 
     */
    private void processService(Element serviceElement) throws CatalogingException {
        try { 
            String id = getServiceId(serviceElement);
            
            if (idToWSDLMap.containsKey(id)) {
                return;
            }
            //Create a RIM Service instances for service tag 
            ServiceType ebServiceType = bu.rimFac.createServiceType();
            bu.addSlotsToRegistryObject(ebServiceType,  dontVersionSlotsMap);
                       
            idToRIMMap.put(id, ebServiceType);
            idToWSDLMap.put(id, serviceElement);
            
            //Set id
            ebServiceType.setId(id);
            
            //Set name
            String nameStr = wsdlDocument.getAttribute(serviceElement, WSDLConstants.ATTR_NAME);
            ebServiceType.setName(bu.getName(nameStr));
            
            //Set description
            Element docElement = wsdlDocument.getElement(serviceElement, WSDLConstants.QNAME_DOCUMENTATION);
            if (docElement != null) {
                String docStr = docElement.getFirstChild().getNodeValue();
                ebServiceType.setDescription(bu.getDescription(docStr));
            }
            
            catalogTargetNamespace(ebServiceType);
            
            //Add wsdl Service Classification
            bu.addClassificationToRegistryObject(ebServiceType, CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_SERVICE);           
            
            //Now process the Port instances for the Service
            
            List<?> portElements = wsdlDocument.getRequiredElements(serviceElement, WSDLConstants.QNAME_PORT);
            Iterator<?> portElementItr = portElements.iterator();
            while (portElementItr.hasNext()) {
                Element portElement = (Element)portElementItr.next();
                processPort(portElement);
                ServiceBindingType rimPort = (ServiceBindingType)idToRIMMap.get(getPortId(portElement));
                if (rimPort != null) {
                    ebServiceType.getServiceBinding().add(rimPort);
                }
            }                    
        } catch (Exception e) {
            throw new CatalogingException(e);
        }        
    }
    
    /*
     * Method used to catalog wsdl:port
     */
    private void processPort(Element portElement) throws CatalogingException {
        try {
            String id = getPortId(portElement);
            
            if (idToWSDLMap.containsKey(id)) {
                return;
            }            
            
            //Create a RIM ServiceBinding instances for service tag 
            ServiceBindingType ebServiceBindingType = bu.rimFac.createServiceBindingType();
            bu.addSlotsToRegistryObject(ebServiceBindingType,  dontVersionSlotsMap);
            
            //Set id
            ebServiceBindingType.setId(id);
            
            idToRIMMap.put(id, ebServiceBindingType);
            idToWSDLMap.put(id, portElement);
            
            //Set name
            String nameStr = wsdlDocument.getAttribute(portElement, WSDLConstants.ATTR_NAME);
            
            ebServiceBindingType.setName(bu.getName(nameStr));
            
            //Set description
            Element docElement = wsdlDocument.getElement(portElement,  WSDLConstants.QNAME_DOCUMENTATION);
            if (docElement != null) {
                String docStr = docElement.getFirstChild().getNodeValue();
                ebServiceBindingType.setDescription(bu.getDescription(docStr));
            }
            
            catalogTargetNamespace(ebServiceBindingType);
            
            //Add wsdl Service Classification
            bu.addClassificationToRegistryObject(ebServiceBindingType, CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_PORT);            

            //Set parent link
            Element serviceElement = (Element)portElement.getParentNode();
            ServiceType rimService = (ServiceType)idToRIMMap.get(getServiceId(serviceElement));
            if (rimService != null) {
                ebServiceBindingType.setService(rimService.getId());
            }
           
            //Add accessURI
            String accessURI = null;
            
            Element soapAddressElement = wsdlDocument.getElement(portElement, WSDLConstants.QNAME_SOAP_ADDRESS);
            if (soapAddressElement != null) {
                accessURI = wsdlDocument.getAttribute(soapAddressElement, WSDLConstants.ATTR_LOCATION);

                if (accessURI != null) {
                    ebServiceBindingType.setAccessURI(accessURI);
                }   
            }
            // document get binding
            String bindingStr = wsdlDocument.getAttribute(portElement, WSDLConstants.ATTR_BINDING);
            Element bindingElement = resolveBinding(wsdlDocument, bindingStr);
            if (bindingElement == null) {
                throw new CatalogingException(ServerResourceBundle.getInstance()
                                                                  .getString("message.missingRequiredElement", 
                                                                             new Object[]{wsdlDocument.getSystemId(), bindingElement}));
            }
            //Now process the Binding instances for the Service
            //Note it may be in an imported document
            processBinding(bindingElement);

            //Set Implements Association between Port and Binding
            ExtrinsicObjectType rimBinding = (ExtrinsicObjectType)idToRIMMap.get(getBindingId(bindingElement));
            AssociationType1 ebAssociationType = bu.createAssociationType(ebServiceBindingType.getId(), 
										            		rimBinding.getId(), 
										            		CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Implements);
            //May need a more deterministic algorithm for setting this id??
            ebAssociationType.setId(ebServiceBindingType.getId()+":Implements:"+rimBinding.getId());
            registryObjects.add(ebAssociationType);
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable e) {
            throw new CatalogingException(e);
        }
    }
    
    /*
     * Method used to catalog wsdl:binding
     */
    private void processBinding(Element bindingElement) 
        throws CatalogingException {
        try {
            String id = getBindingId(bindingElement);
            
            if (idToWSDLMap.containsKey(id)) {
                return;
            }
            
            //Create a RIM ExtrinsicObject instance for wsdl binding 
            ExtrinsicObjectType ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
            bu.addSlotsToRegistryObject(ebExtrinsicObjectType,  dontVersionSlotsMap);
            idToRIMMap.put(id, ebExtrinsicObjectType);
            idToWSDLMap.put(id, bindingElement);
            
            //Set id
            ebExtrinsicObjectType.setId(id);
            
            //Set name
            String nameStr = wsdlDocument.getAttribute(bindingElement, WSDLConstants.ATTR_NAME);
            ebExtrinsicObjectType.setName(bu.getName(nameStr));
            
            //Set description
            Element docElement = wsdlDocument.getElement(bindingElement, WSDLConstants.QNAME_DOCUMENTATION);
            if (docElement != null) {
                String docStr = docElement.getFirstChild().getNodeValue();
                ebExtrinsicObjectType.setDescription(bu.getDescription(docStr));
            }
            
            catalogTargetNamespace(ebExtrinsicObjectType);
            
            //Add wsdl Service Classification
            //??Update spec to say classification is also required for consistency with WSDL Service and Port mapping.
            ebExtrinsicObjectType.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_BINDING);     
            bu.addClassificationToRegistryObject(ebExtrinsicObjectType, CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_BINDING);            

            List<Element> soapBindingElements = wsdlDocument.getElements(bindingElement, WSDLConstants.QNAME_SOAP_BINDING);
            Iterator<Element> soapBindingElementItr = soapBindingElements.iterator();
            while (soapBindingElementItr.hasNext()) {
                Element soapBindingElement = soapBindingElementItr.next();

                //Add SOAP Binding Classification
                bu.addClassificationToRegistryObject(ebExtrinsicObjectType, CanonicalConstants.CANONICAL_PROTOCOL_TYPE_ID_SOAP);

                String transport = wsdlDocument.getAttribute(soapBindingElement, WSDLConstants.ATTR_TRANSPORT);
                if (transport.equals(WSDLConstants.URI_SOAP_TRANSPORT_HTTP)) {
                    bu.addClassificationToRegistryObject(ebExtrinsicObjectType, CanonicalConstants.CANONICAL_TRANSPORT_TYPE_ID_HTTP);
                }

                String soapStyleStr = wsdlDocument.getAttribute(soapBindingElement, WSDLConstants.ATTR_STYLE);
                String styleNode = null;
                if (soapStyleStr.equalsIgnoreCase(WSDLConstants.RPC)) {
                    styleNode = CanonicalConstants.CANONICAL_SOAP_STYLE_TYPE_ID_RPC;
                } else if (soapStyleStr.equalsIgnoreCase(WSDLConstants.DOCUMENT)) {
                    styleNode = CanonicalConstants.CANONICAL_SOAP_STYLE_TYPE_ID_DOCUMENT;
                }

                if (styleNode != null) {
                    //Add SOAP Style Classification
                    bu.addClassificationToRegistryObject(ebExtrinsicObjectType, styleNode);
                }
            }
            
            ebExtrinsicObjectType.setMimeType("text/xml"); //Add to spec??
            
            // document get port type
            String portTypeStr = wsdlDocument.getAttribute(bindingElement, WSDLConstants.ATTR_TYPE);
            Element portTypeElement = resolvePortType(wsdlDocument, portTypeStr);
            if (portTypeElement == null) {
                throw new CatalogingException(ServerResourceBundle.getInstance()
                                                                  .getString("message.missingRequiredElement", 
                                                                             new Object[]{wsdlDocument.getSystemId(), portTypeStr}));
            }
            processPortType(portTypeElement);

            //Set Implements Association between Binding and PortType
            ExtrinsicObjectType rimPortType = (ExtrinsicObjectType)idToRIMMap.get(getPortTypeId(portTypeElement));
            AssociationType1 ebAssociationType = bu.createAssociationType(ebExtrinsicObjectType.getId(), rimPortType.getId(), CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Implements);
            //May need a more deterministic algorithm for setting this id??
            ebAssociationType.setId(ebExtrinsicObjectType.getId()+":Implements:"+rimPortType.getId());
            registryObjects.add(ebAssociationType);
        } catch (CatalogingException ce) {
            throw ce;
        } catch (Throwable e) {
            throw new CatalogingException(e);
        }
    }
    
    private void resolveTypeImports(Element portTypeElement, 
                                    RegistryObjectType wsdlEO) 
                                    throws CatalogingException { 
        try {
            String id = getPortTypeId(portTypeElement);
            if (idToRIMMap.get(id) == null) {
                processPortType(portTypeElement);
            }
            @SuppressWarnings("unused")
			ExtrinsicObjectType rimPortType = (ExtrinsicObjectType)idToRIMMap.get(id);
            List<Element> typeElements = resolveTypes(wsdlDocument);
            Iterator<?> schemaItr = resolveSchemaExtensions(typeElements);
            while (schemaItr.hasNext()) {
                Element schemaElement = (Element)schemaItr.next();
                Iterator<?> schemaAttributeItr = wsdlDocument.getAttributes(schemaElement).iterator();
                String location = null;
                @SuppressWarnings("unused")
				String localName = schemaElement.getLocalName();
                String namespace = schemaElement.getNamespaceURI();
                while (schemaAttributeItr.hasNext()) {
                    Attr schemaAttribute = (Attr)schemaAttributeItr.next();
                    String localAttrName = schemaAttribute.getLocalName();
                    if (localAttrName.equalsIgnoreCase("namespace")) {
                        namespace = schemaAttribute.getValue();
                    } else if (localAttrName.equalsIgnoreCase("schemaLocation")) {
                        location = schemaAttribute.getValue();
                    }
                }
                RegistryObjectType importedObject = catalogImportStatement(wsdlEO, namespace, location);
                if (importedObject != null) {
                    AssociationType1 ebAssociationType = bu.createAssociationType(wsdlEO.getId(), 
                                                                 importedObject.getId(), 
                                                                 CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Imports);
                    //May need a more deterministic algorithm for setting this id??
                    ebAssociationType.setId(wsdlEO.getId()+":Imports:"+importedObject.getId());
                    registryObjects.add(ebAssociationType);
                }
            }
        } catch (CatalogingException e) {
            throw e;
        } catch (Exception e) {
            throw new CatalogingException(e);
        }
    }
    
    /*
     * Method catalogs wsdl:portType elements
     */
    private void processPortType(Element portTypeElement) throws CatalogingException {
        try {
            String id = getPortTypeId(portTypeElement);
            
            if (idToWSDLMap.containsKey(id)) {
                return;
            }                        
            
            //Create a RIM ExtrinsicObject instance for wsdl portType 
            ExtrinsicObjectType ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
            bu.addSlotsToRegistryObject(ebExtrinsicObjectType,  dontVersionSlotsMap);
            idToRIMMap.put(id, ebExtrinsicObjectType);
            idToWSDLMap.put(id, portTypeElement);
            
            //Set id
            ebExtrinsicObjectType.setId(id);
            
            //Set name
            String nameStr = wsdlDocument.getAttribute(portTypeElement, WSDLConstants.ATTR_NAME);
            ebExtrinsicObjectType.setName(bu.getName(nameStr));
            
            //Set description
            Element docElement = wsdlDocument.getElement(portTypeElement, WSDLConstants.QNAME_DOCUMENTATION);
            if (docElement != null) {
                String docStr = docElement.getFirstChild().getNodeValue();
                ebExtrinsicObjectType.setDescription(bu.getDescription(docStr));
            }
            
            catalogTargetNamespace(ebExtrinsicObjectType);
            
            //Add wsdl Service Classification
            //??Update spec to say classification is also required for consistency with WSDL Service and Port mapping.
            ebExtrinsicObjectType.setObjectType(CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_PORT_TYPE);            
            bu.addClassificationToRegistryObject(ebExtrinsicObjectType, CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_WSDL_PORT_TYPE);
            
            ebExtrinsicObjectType.setMimeType("text/xml"); //Add to spec??
            
        } catch (CatalogingException e) {
            throw e;
        } catch (Exception e) {
            throw new CatalogingException(e);
        }        
    }
      
    private Iterator<Element> resolveSchemaExtensions(List<Element> typeElements) 
        throws CatalogingException {
        List<Element> schemaElementChildrenList = new ArrayList<Element>();
        Iterator<Element> typeElementItr = typeElements.iterator();
        while (typeElementItr.hasNext()) {
            Element typeElement = typeElementItr.next();
            List<Element> schemaElements = wsdlDocument.getElements(typeElement, WSDLConstants.QNAME_XSD_SCHEMA);
            Iterator<Element> schemaElementsItr = schemaElements.iterator();
            while (schemaElementsItr.hasNext()) {
                Element schemaElement = schemaElementsItr.next();
                // Check for xsd:import
                List<Element> importElements = wsdlDocument.getElements(schemaElement, WSDLConstants.QNAME_XSD_IMPORT);
                schemaElementChildrenList.addAll(importElements);
                // Check for xsd:redefine
                List<Element> redefineElements = wsdlDocument.getElements(schemaElement, WSDLConstants.QNAME_XSD_REDEFINE);
                schemaElementChildrenList.addAll(redefineElements);
                // Check for xsd:include
                List<Element> includeElements = wsdlDocument.getElements(schemaElement, WSDLConstants.QNAME_XSD_INCLUDE);
                schemaElementChildrenList.addAll(includeElements);
            }
        }
        return schemaElementChildrenList.iterator();
    }
     
    private List<Element> resolveTypes(WSDLDocument wsdlDocument) throws CatalogingException {
        return wsdlDocument.getElements(WSDLConstants.QNAME_TYPES);
    }
    
    /*
     * Method takes an InputSource and returns a org.w3c.dom.Document
     */
    private Document parseXML(InputSource source) 
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setValidating(false);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e)
                throws SAXParseException {
                throw e;
            }

            public void fatalError(SAXParseException e)
                throws SAXParseException {
                throw e;
            }

            public void warning(SAXParseException err)
                throws SAXParseException {
                // do nothing
            }
        });

        return builder.parse(source);
    }

    /*
     * This function may return null
     */
    private Element resolveBinding(WSDLDocument wsdlDocument, String portBindingStr) 
        throws CatalogingException {
        Element resolvedBinding = null;
        List<Element> bindings = wsdlDocument.getElements(WSDLConstants.QNAME_BINDING);
        Iterator<Element> bindingItr = bindings.iterator();        
        QName portBindingQName = createQName(null, portBindingStr);
        while (bindingItr.hasNext()) {
            Element binding = bindingItr.next();
            String bindingTypeStr = wsdlDocument.getRequiredAttribute(binding, WSDLConstants.ATTR_NAME);
            // Get target namespace
            String bindingTargetNS = wsdlDocument.getTargetNamespaceURI(binding, WSDLConstants.QNAME_BINDING);
            // Try to resolve based on namespace:localValue
            QName bindingTypeQName = createQName(bindingTargetNS, bindingTypeStr);
            if (portBindingQName.equals(bindingTypeQName) ||
                bindingTypeStr.equals(portBindingStr)) {
                resolvedBinding = binding;
                break;
            }
        }
        return resolvedBinding;
    }
    
    /*
     * This function may return null
     */
    private Element resolvePortType(WSDLDocument wsdlCatalogedDocument, String bindingTypeStr) 
        throws CatalogingException {
        Element resolvedPortType = null;
        List<Element> portTypes = wsdlDocument.getElements(WSDLConstants.QNAME_PORT_TYPE);
        Iterator<Element> portTypeItr = portTypes.iterator();        
        QName portBindingQName = createQName(null, bindingTypeStr);
        while (portTypeItr.hasNext()) {
            Element portType = portTypeItr.next();
            String portTypeStr = wsdlDocument.getRequiredAttribute(portType, WSDLConstants.ATTR_NAME);
            String portTypeTargetNS = wsdlDocument.getTargetNamespaceURI(portType, WSDLConstants.QNAME_PORT_TYPE);
            // Try to resolve based on namespace:localValue
            QName bindingTypeQName = createQName(portTypeTargetNS, portTypeStr);
            if (portBindingQName.equals(bindingTypeQName) ||
                portTypeStr.equals(bindingTypeStr)) {
                resolvedPortType = portType;
                break;
            }
        }
        return resolvedPortType;
    }
    
    private QName createQName(String namespace, String attrValue) {
        QName qname = null;
        if (namespace == null && attrValue == null) {
            qname = null;
        } else {
            if (namespace == null) {
                String prefix = Utility.getPrefix(attrValue);
                if (prefix == null) {
                    qname = new QName("", attrValue);
                } else {
                    namespace = wsdlDocument.getNamespaceURI(prefix);
                    qname = new QName(namespace, Utility.getLocalPart(attrValue));
                }
            } else {
                qname = new QName(namespace, attrValue);
            }
        }
        return qname;
    }
    
    /*
     * This function may return null
     */
    @SuppressWarnings("unused")
	private String createQualifiedType(String attrValue) {
        String qualifiedType = null;
        String attrPrefix = Utility.getPrefix(attrValue);
        String attrNamespace = null;
        if (attrPrefix == null) {
            attrNamespace = wsdlDocument.getTargetNamespaceURI();
        } else {
            attrNamespace = wsdlDocument.getNamespaceURI(attrPrefix);
        }
        if (attrNamespace != null) {
            String attrLocalPart = Utility.getLocalPart(attrValue);
            StringBuffer sb = new StringBuffer(attrNamespace);
            qualifiedType = sb.append(':').append(attrLocalPart).toString();
        }
        return qualifiedType;
    }
    
    @SuppressWarnings("unused")
	private String createQualifiedType(String attrValue, String namespace) {
        String attrLocalPart = Utility.getLocalPart(attrValue);
        StringBuffer sb = new StringBuffer(namespace);
        sb.append(':').append(attrLocalPart);
        return sb.toString();
    }
    
}

