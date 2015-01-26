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

package it.cnr.icar.eric.service.validationTest.cppaValidation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.oasis.ebxml.registry.bindings.cms.ValidateContentRequest;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;

import it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType;
import it.cnr.icar.eric.service.validationTest.AbstractValidationTestService;

import java.io.StringWriter;

import java.rmi.RemoteException;

import java.util.HashMap;

import javax.activation.DataHandler;

import javax.xml.soap.SOAPElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CPPAValidation extends AbstractValidationTestService
    implements ContentValidationServicePortType {
    private static final Log log = LogFactory.getLog(CPPAValidation.class.getName());
    static final String errorCodeContext = "CPPAValidation.validateContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        try {
            if (log.isDebugEnabled()) {
                printNodeToConsole(partValidateContentRequest);
            }

            HashMap<String, DataHandler> repositoryItemDHMap = getRepositoryItemDHMap();

            if (log.isDebugEnabled()) {
                log.debug("Attachments: " + repositoryItemDHMap.size());
            }

            Object requestObj = getBindingObjectFromNode(partValidateContentRequest);

            if (!(requestObj instanceof ValidateContentRequest)) {
                throw new Exception(
                    "Wrong response received from validation service.  Expected ValidationContentRequest, got: " +
                    partValidateContentRequest.getElementName()
                                              .getQualifiedName());
            }

            vcReq = (ValidateContentRequest) requestObj;

            IdentifiableType originalContentIT = vcReq.getOriginalContent()
                                                                         .getIdentifiable()
                                                                         .get(0).getValue();
            IdentifiableType invocationControlIT = vcReq.getInvocationControlFile()
                                                                           .get(0);

            DataHandler originalContentDH = repositoryItemDHMap.get(originalContentIT.getId());
            DataHandler invocationControlDH = repositoryItemDHMap.get(invocationControlIT.getId());

            if (log.isDebugEnabled()) {
                log.debug("originalContentIT id: " + originalContentIT.getId());
                log.debug("invocationControlIT id: " +
                    invocationControlIT.getId());
            }

            StreamSource invocationControlSrc = new StreamSource(invocationControlDH.getInputStream());

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(invocationControlSrc);

            StringWriter sw = new StringWriter();
            transformer.transform(new StreamSource(
                    originalContentDH.getInputStream()), new StreamResult(sw));

            vcResp = cmsFac.createValidateContentResponse();

            boolean success = Boolean.valueOf(sw.toString()).booleanValue();

            if (success) {
                vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            } else {
                RegistryError re = rsFac.createRegistryError();
                re.setValue(sw.toString());
                re.setCodeContext(errorCodeContext);
                re.setErrorCode("InvalidContentException");

                RegistryErrorList el = rsFac.createRegistryErrorList();
                el.getRegistryError().add(re);
                el.setHighestSeverity("Failure");

                vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
                vcResp.setRegistryErrorList(el);
            }

            vcRespElement = getSOAPElementFromBindingObject(vcResp);
        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return vcRespElement;
    }
}
