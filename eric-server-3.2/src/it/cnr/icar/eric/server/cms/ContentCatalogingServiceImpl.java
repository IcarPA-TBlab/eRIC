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
import it.cnr.icar.eric.common.jaxrpc.cms.cataloging.client.ContentCatalogingServicePortType_Stub;
import it.cnr.icar.eric.common.jaxrpc.cms.cataloging.client.ContentCatalogingServiceSOAPService;
import it.cnr.icar.eric.common.jaxrpc.cms.cataloging.client.ContentCatalogingServiceSOAPService_Impl;
import it.cnr.icar.eric.common.security.wss4j.WSS4JSecurityUtilBST;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.registry.RegistryException;
import javax.xml.rpc.Stub;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequest;
import org.oasis.ebxml.registry.bindings.cms.CatalogContentResponse;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * A proxy Content cataloging service that invokes the actual
 * Content cataloging service that is a SOAP endpoint.
 *
 * TODO: This should be really be renamed to SOAPContentCatalogingService??
 *
 * @author Tony Graham
 *
 */
public class ContentCatalogingServiceImpl
    extends AbstractContentCatalogingService {
    private static final Log log = LogFactory.getLog(ContentCatalogingServiceImpl.class.getName());

    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("ContentCatalogingServiceImpl.invoke():: input: " +
                input + "; Service: " + service + "; invocationController: " +
                invocationController + "; user: " + user);
        }

        String accessURI = selectAccessURI(service);

        SOAPElement responseElement;
        Object responseObj;
        ServiceOutput output = new ServiceOutput();
        ServerRequestContext outputContext = null;
        
        try {
            outputContext = context; //new RequestContext(null);
            CatalogContentRequest ebCatalogContentRequest = bu.cmsFac.createCatalogContentRequest();
            RegistryObjectListType ebRegistryObjectListType = bu.rimFac.createRegistryObjectListType();
            
            // create element 
            JAXBElement<RegistryObjectType> ebRegistryObject = bu.rimFac.createRegistryObject(input.getRegistryObject());
            // add element
            ebRegistryObjectListType.getIdentifiable().add(ebRegistryObject);
            
            ebCatalogContentRequest.setOriginalContent(ebRegistryObjectListType);
            System.err.println("InvocationControlFile class: " +
                qm.getRegistryObject(outputContext, invocationController.getEoId()).getClass()
                  .getName());

            ExtrinsicObjectType icfEOT = bu.rimFac.createExtrinsicObjectType();
            icfEOT.setId(invocationController.getEoId());
            ebCatalogContentRequest.getInvocationControlFile().add(icfEOT);

            if (log.isDebugEnabled()) {
                log.debug("\n\nOriginalContent:");
                printNodeToConsole(bu.getSOAPElementFromBindingObject(
                        input.getRegistryObject()));
            }

            Collection<AttachmentPart> attachments = new ArrayList<AttachmentPart>();

            // RepositoryItem for input to be cataloged.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    input.getRegistryObject().getId()));

            // RepositoryItem for InvocationControlFile.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    invocationController.getEoId()));

            ContentCatalogingServiceSOAPService soapService = new ContentCatalogingServiceSOAPService_Impl();
            ContentCatalogingServicePortType_Stub stub = (ContentCatalogingServicePortType_Stub) soapService.getContentCatalogingServicePort();
            stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            stub._setProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
                attachments);

            responseElement = stub.catalogContent(bu.getSOAPElementFromBindingObject(
                        ebCatalogContentRequest));

            responseObj = bu.getBindingObjectFromSOAPElement(responseElement);

            if (!(responseObj instanceof CatalogContentResponse)) {
                throw new RegistryException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.WrongResponseReceivedFromCatalogingService", 
                                                    new Object[] {responseElement.getElementName().getQualifiedName()}));
            }

            CatalogContentResponse ebCatalogContentResponse = (CatalogContentResponse) responseObj;

            String status = ebCatalogContentResponse.getStatus();

            if (log.isDebugEnabled()) {
                log.debug("Status: " + status);
            }

            RegistryObjectListType catalogedMetadata = ebCatalogContentResponse.getCatalogedContent();

            // TODO: User should refer to "Service object for the
            // Content Management Service that generated the
            // Cataloged Content."
            outputContext.setUser(user);

            bu.getObjectRefsAndRegistryObjects(catalogedMetadata, outputContext.getTopLevelRegistryObjectTypeMap(), outputContext.getObjectRefTypeMap());
            outputContext.getRepositoryItemsMap().putAll(getRepositoryItems(stub));

            output.setOutput(outputContext);
            output.setErrorList(ebCatalogContentResponse.getRegistryErrorList());
        } catch (Exception e) {
            e.printStackTrace();
            if (outputContext != context) { 
                outputContext.rollback();
            }
            throw new RegistryException(e);
        }

        if (outputContext != context) {
            outputContext.commit();
        }
        return output;
    }

    protected static void printNodeToConsole(Node n) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // also print to console
            transformer.transform(new DOMSource(n), new StreamResult(System.out));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    protected HashMap<String, RepositoryItem> getRepositoryItems(com.sun.xml.rpc.client.StubBase stub)
        throws Exception {
        HashMap<String, RepositoryItem> repositoryItems = new HashMap<String, RepositoryItem>();
        
        Collection<?> attachments = (Collection<?>) stub._getProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY);

        if (attachments != null) {
            Iterator<?> attachmentsIter = attachments.iterator();

            while (attachmentsIter.hasNext()) {
                Object obj = attachmentsIter.next();

                System.err.println("getRepositoryItems:: Attachment: " +
                    obj.getClass().getName());

                if (obj instanceof AttachmentPart) {
                    AttachmentPart ap = (AttachmentPart) obj;

                    String contentId = ap.getContentId();
                    String contentType = ap.getContentType();

                    System.err.println("getRepositoryItems:: contentId: " +
                        contentId + "; contentType: " + contentType);


                    repositoryItems.put(contentId, processIncomingAttachment(ap));
                }
            }
        }

        return repositoryItems;
    }

    private RepositoryItem processIncomingAttachment(AttachmentPart ap)
        throws Exception {
        DataHandler dh = null;

        //ContentId is the id of the repositoryItem (CID UIR
        String id = WSS4JSecurityUtilBST.convertContentIdToUUID(ap.getContentId());

        if (log.isInfoEnabled()) {
            log.info(ServerResourceBundle.getInstance().getString("message.ProcessingAttachmentWithContentId", new Object[]{id}));
        }
        
        if (log.isDebugEnabled()) {
            log.debug(
                "Processing attachment (RepositoryItem):\n" +
                ap.getContent().toString());
        }
        
        dh = ap.getDataHandler();
        return new RepositoryItemImpl(id, dh);
    }
}
