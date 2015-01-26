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

package it.cnr.icar.eric.service.validationTest.returnInvalidContentException;


import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;

import it.cnr.icar.eric.common.jaxrpc.cms.validation.client.ContentValidationServicePortType;
import it.cnr.icar.eric.service.validationTest.AbstractValidationTestService;

import java.rmi.RemoteException;

import javax.xml.soap.SOAPElement;


/**
 * @author najmi
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReturnInvalidContentException extends AbstractValidationTestService
    implements ContentValidationServicePortType {
    static final String errorCodeContext = "ReturnInvalidContentException.validateContent";
    protected org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

    public SOAPElement validateContent(SOAPElement partValidateContentRequest)
        throws RemoteException {
        try {
            RegistryError re = rsFac.createRegistryError();
            re.setValue("InvalidContentException");
            re.setCodeContext(errorCodeContext);
            re.setErrorCode("InvalidContentException");

            RegistryErrorList el = rsFac.createRegistryErrorList();
            el.getRegistryError().add(re);
            el.setHighestSeverity("Failure");

            vcResp = cmsFac.createValidateContentResponse();
            vcResp.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
            vcResp.setRegistryErrorList(el);

            vcRespElement = getSOAPElementFromBindingObject(vcResp);
        } catch (Exception e) {
            throw new RemoteException("Could not create response.", e);
        }

        return vcRespElement;
    }
}
