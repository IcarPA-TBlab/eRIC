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

package it.cnr.icar.eric.service.catalogingTest.cppaCataloging;

import it.cnr.icar.eric.common.jaxrpc.cms.cataloging.client.ContentCatalogingServicePortType;
import it.cnr.icar.eric.service.catalogingTest.AbstractCatalogingTestService;

import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.oasis.ebxml.registry.bindings.cms.CatalogContentRequest;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;

import java.io.StringWriter;

import java.rmi.RemoteException;
import java.util.Collection;

import java.util.HashMap;

import javax.activation.DataHandler;
import javax.xml.bind.util.JAXBSource;
import javax.xml.rpc.handler.MessageContext;

import javax.xml.soap.SOAPElement;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CPPACataloging extends AbstractCatalogingTestService
    implements ContentCatalogingServicePortType {
    private static final Log log = LogFactory.getLog(CPPACataloging.class.getName());
    static final String errorCodeContext = "CPPACataloging.catalogContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement catalogContent(SOAPElement partCatalogContentRequest)
        throws RemoteException {
        try {
            if (log.isDebugEnabled()) {
                printNodeToConsole(partCatalogContentRequest);
            }

            final HashMap<String, DataHandler> repositoryItemDHMap = getRepositoryItemDHMap();

            if (log.isDebugEnabled()) {
                log.debug("Attachments: " + repositoryItemDHMap.size());
            }

            Object requestObj = getBindingObjectFromNode(partCatalogContentRequest);

            if (!(requestObj instanceof CatalogContentRequest)) {
                throw new Exception(
                    "Wrong response received from validation service.  Expected CatalogContentRequest, got: " +
                    partCatalogContentRequest.getElementName().getQualifiedName());
            }

            ccReq = (CatalogContentRequest) requestObj;

            IdentifiableType originalContentIT = ccReq.getOriginalContent()
                                                                         .getIdentifiable()
                                                                         .get(0).getValue();
            IdentifiableType invocationControlIT = ccReq.getInvocationControlFile()
                                                                           .get(0);

            DataHandler invocationControlDH = repositoryItemDHMap.get(invocationControlIT.getId());

            if (log.isDebugEnabled()) {
                log.debug("originalContentIT id: " + originalContentIT.getId());
                log.debug("invocationControlIT id: " +
                    invocationControlIT.getId());
            }

            StreamSource invocationControlSrc = new StreamSource(invocationControlDH.getInputStream());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(invocationControlSrc);

            transformer.setURIResolver(new URIResolver() {
                public Source resolve(String href,
                    String base)
                throws TransformerException {
                    Source source = null;
                    try {
                        // Should this check that href is UUID URN first?
                        source = new StreamSource((repositoryItemDHMap.get(href)).getInputStream());
                    } catch (Exception e) {
                        source = null;
                    }
            
                    return source;
                }
            });
            transformer.setErrorListener(new ErrorListener() {
                    public void error(TransformerException exception)
                        throws TransformerException {
                        log.info(exception);
                    }

                    public void fatalError(TransformerException exception)
                        throws TransformerException {
                        log.error(exception);
                        throw exception;
                    }

                    public void warning(TransformerException exception)
                        throws TransformerException {
                        log.info(exception);
                    }
                });

            //Set respository item as parameter
            transformer.setParameter("repositoryItem",
                originalContentIT.getId());

            StringWriter sw = new StringWriter();
            transformer.transform(new JAXBSource(jaxbContext, originalContentIT), new StreamResult(sw));

            ccResp = cmsFac.createCatalogContentResponse();

            RegistryObjectListType catalogedMetadata = (RegistryObjectListType) getUnmarshaller()
                                                                                  .unmarshal(new StreamSource(new StringReader(sw.toString())));
            RegistryObjectListType roList = rimFac.createRegistryObjectListType();
            ccResp.setCatalogedContent(roList);
            // FIXME: Setting catalogedMetadata as CatalogedContent results in incorrect serialization.
            roList.getIdentifiable().addAll(catalogedMetadata.getIdentifiable());
            
            ccResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            
            ccRespElement = getSOAPElementFromBindingObject(ccResp);
            
            // Copy request's attachments to response to exercise attachment-processing code on client.
            MessageContext mc = servletEndpointContext.getMessageContext();
            mc.setProperty(com.sun.xml.rpc.server.ServerPropertyConstants.SET_ATTACHMENT_PROPERTY, (Collection<?>) mc.getProperty(com.sun.xml.rpc.server.ServerPropertyConstants.GET_ATTACHMENT_PROPERTY));

        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return ccRespElement;
    }
}
