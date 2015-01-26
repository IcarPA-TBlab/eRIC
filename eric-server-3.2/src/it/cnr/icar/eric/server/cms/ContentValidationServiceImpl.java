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

import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType_Stub;
import it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServiceSOAPService;
import it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServiceSOAPService_Impl;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.oasis.ebxml.registry.bindings.cms.ValidateContentRequest;
import org.oasis.ebxml.registry.bindings.cms.ValidateContentResponse;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Content validation service
 * @see
 * @author Tony Graham
 *
 */
public class ContentValidationServiceImpl
    extends AbstractContentValidationService {
    private static final Log log = LogFactory.getLog(ContentValidationServiceImpl.class.getName());

    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        System.err.println("ContentValidationServiceImpl.invoke():: input: " +
            input + "; Service: " + service + "; invocationController: " +
            invocationController + "; user: " + user);

        List<ServiceBindingType> serviceBindings = service.getServiceBinding();
        Iterator<ServiceBindingType> debugIter = serviceBindings.iterator();

        while (debugIter.hasNext()) {
            ServiceBindingType binding = debugIter.next();
            System.err.println("URL: " + binding.getAccessURI());
        }

        String accessURI = selectAccessURI(service);

        SOAPElement responseElement;
        Object responseObj;

        try {
            ValidateContentRequest ebValidateContentRequest = bu.cmsFac.createValidateContentRequest();
            RegistryObjectListType ebRegistryObjectListType = bu.rimFac.createRegistryObjectListType();
            
            JAXBElement<RegistryObjectType> ebRegistryObject = bu.rimFac.createRegistryObject(input.getRegistryObject());
            ebRegistryObjectListType.getIdentifiable().add(ebRegistryObject);
            ebValidateContentRequest.setOriginalContent(ebRegistryObjectListType);
            
            
            System.err.println("InvocationControlFile class: " +
                qm.getRegistryObject(context, invocationController.getEoId()).getClass()
                  .getName());

            // FIXME: Adding existing ExtrinsicObjects with 'request.getInvocationControlFile().add()' gives incorrect serialization.
            ExtrinsicObjectType icfEOT = bu.rimFac.createExtrinsicObjectType();
            icfEOT.setId(invocationController.getEoId());
            ebValidateContentRequest.getInvocationControlFile().add(icfEOT);

            System.out.println("\n\nOriginalContent:");
            printNodeToConsole(bu.getSOAPElementFromBindingObject(
                    input.getRegistryObject()));

            ArrayList<AttachmentPart> attachments = new ArrayList<AttachmentPart>();

            // RepositoryItem for input to be validated.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    input.getRegistryObject().getId()));

            // RepositoryItem for InvocationControlFile.
            attachments.add(getRepositoryItemAsAttachmentPart(
                    invocationController.getEoId()));

            ContentValidationServiceSOAPService soapService = new ContentValidationServiceSOAPService_Impl();
            ContentValidationServicePortType_Stub stub = (ContentValidationServicePortType_Stub) soapService.getContentValidationServicePort();
            stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            stub._setProperty(com.sun.xml.rpc.client.StubPropertyConstants.SET_ATTACHMENT_PROPERTY,
                attachments);

            responseElement = stub.validateContent(bu.getSOAPElementFromBindingObject(
                        ebValidateContentRequest));

            responseObj = bu.getBindingObjectFromSOAPElement(responseElement);
        } catch (Exception e) {
            throw new RegistryException(e);
        }

        if (!(responseObj instanceof ValidateContentResponse)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ValidationContentResponseExpected", 
                    new Object[]{responseElement.getElementName().getQualifiedName()}));
        }

        ValidateContentResponse ebValidateContentResponse = (ValidateContentResponse) responseObj;

        String status = ebValidateContentResponse.getStatus();

        System.err.println("Status: " + status);

        if (log.isDebugEnabled()) {
            log.debug("Status: " + status);
        }

        ServiceOutput output = new ServiceOutput();
        output.setErrorList(ebValidateContentResponse.getRegistryErrorList());

        if (status.equals(CanonicalSchemes.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success)) {
            output.setOutput(Boolean.TRUE);
        } else if (status.equals(CanonicalSchemes.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure)) {
            output.setOutput(Boolean.FALSE);
        } else {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ResponseStatusTypeExpected", new Object[]{status}));
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
}
