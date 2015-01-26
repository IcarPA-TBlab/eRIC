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
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.cms.CatalogingServiceEngine;
import it.cnr.icar.eric.common.cms.CatalogingServiceInput;
import it.cnr.icar.eric.common.cms.CatalogingServiceOutput;
import it.cnr.icar.eric.common.exceptions.MissingRepositoryItemException;
import it.cnr.icar.eric.server.cms.ContentCatalogingServiceImpl;
import it.cnr.icar.eric.server.cms.InvocationController;
import it.cnr.icar.eric.server.cms.ServiceInput;
import it.cnr.icar.eric.server.cms.ServiceOutput;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

import javax.activation.DataHandler;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;



/**
 * Extracts information from an image RepositoryItem and adds it to the
 * OriginalContent as named slots.
 *
 * @author Tony.Graham@sun.com
 */
public class WSDLCataloger extends ContentCatalogingServiceImpl {
    private static final Log log = LogFactory.getLog(WSDLCataloger.class.getName());
    static final String errorCodeContext = "WSDLCataloger.catalogContent";
    private static BindingUtility bu = BindingUtility.getInstance();

    /**
     * Catalogs WSDL files.
     *
     * @param partCatalogContentRequest CatalogContentRequest containing
     * the ExtrinisicObject representing the WSDL file.
     *
     * @throws RemoteException if an error occurs
     * @return SOAPElement containing an updated ExtrinsicObject
     */
    @SuppressWarnings("unchecked")
	public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        
        if (log.isTraceEnabled()) {
            log.trace("WSDLCataloger.invoke()");
        }

        RegistryObjectType registryObject = input.getRegistryObject();
        RepositoryItem repositoryItem = input.getRepositoryItem();
        DataHandler dh = null;
        if (repositoryItem != null) {
            dh = repositoryItem.getDataHandler();
        }

        if ((registryObject instanceof ExtrinsicObjectType) && (repositoryItem == null)) {
            throw new MissingRepositoryItemException(input.getRegistryObject()
                                                          .getId());
        }

        ServerRequestContext outputContext = null;

        try {
            outputContext = context; //new RequestContext(null);

            CatalogingServiceEngine engine = new WSDLCatalogerEngine();
            CatalogingServiceInput input1 = new CatalogingServiceInput((DataHandler)null, dh, registryObject);
            CatalogingServiceOutput output1 = engine.catalogContent(input1);
            

            RegistryObjectListType catalogedMetadata = bu.rimFac.createRegistryObjectListType();
            
            // FIXME: Setting catalogedMetadata as CatalogedContent results in incorrect serialization.
            catalogedMetadata.getIdentifiable().addAll((Collection<? extends JAXBElement<? extends IdentifiableType>>) output1.getRegistryObjects());
            
            //Add cataloged repository items to outputContext
            HashMap<String, Object> idToRepositoryItemMap = output1.getRepositoryItemMap();            
            outputContext.getRepositoryItemsMap().putAll(idToRepositoryItemMap);
            
            // TODO: User should refer to "Service object for the
            // Content Management Service that generated the
            // Cataloged Content."
            outputContext.setUser(user);

            bu.getObjectRefsAndRegistryObjects(catalogedMetadata, outputContext.getTopLevelRegistryObjectTypeMap(), outputContext.getObjectRefTypeMap());
        } catch (Exception e) {
            if (outputContext != context) {
                outputContext.rollback();
            }
            throw new RegistryException(e);
        }

        ServiceOutput so = new ServiceOutput();
        so.setOutput(outputContext);

        // Setting this error list is redundant, but Content Validation Services
        // currently output a Boolean and a RegistryErrorList, so using
        // same mechanism to report errors from Content Cataloging Services.
        so.setErrorList(outputContext.getErrorList());

        if (outputContext != context) {
            outputContext.commit();
        }
        return so;        
    }
    
}
