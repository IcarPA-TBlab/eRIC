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

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.InvalidConfigurationException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;


/**
 * Content Cataloging Service manager
 */
public class ContentCatalogingServiceManager implements CMSTypeManager {
    private static final Log log = LogFactory.getLog(ContentCatalogingServiceManager.class.getName());
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();

    /**
     * Invokes appropriate Content Management Services for the
     * in the <code>RegistryObject</code>.
     *
     * @param ebRegistryObjectType a <code>RegistryObject</code> value
     * @param ri a <code>RepositoryItem</code> value
     */
    public boolean invokeServiceForObject(ServiceInvocationInfo sii,
        RegistryObjectType ebRegistryObjectType, RepositoryItem ri, ServerRequestContext context) 
        throws RegistryException {
        
        //Cataloging services only apply to Submit/UpdateObjectsRequests
        RegistryRequestType request = context.getCurrentRegistryRequest();
        if (!((request instanceof SubmitObjectsRequest) ||
              (request instanceof UpdateObjectsRequest))) {
            
            return false;
        }
        
        
        if (log.isTraceEnabled()) {
            log.trace(
                "ContentCatalogingServiceManager.invokeServiceForObject()");
        }

        try {
            ContentManagementService cms = (ContentManagementService) sii.getConstructor()
                                                                         .newInstance((java.lang.Object[]) null);

            System.err.println("cms: " + cms.getClass().getName());

            ServiceOutput so = null;
            
            //Note that ri will be null for ExternalLink ro.
            so = cms.invoke(context, new ServiceInput(ebRegistryObjectType, ri),
                sii.getService(), sii.getInvocationController(), context.getUser());            
            
            if (!(so.getOutput() instanceof ServerRequestContext)) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.CatalogingServiceInstanceShouldReturnRequestContext", 
                                                    new Object[] {so.getOutput().getClass().getName()}));
            }

            ServerRequestContext outputContext = (ServerRequestContext) so.getOutput();

            if (outputContext != null) {
                ArrayList<IdentifiableType> list = new ArrayList<IdentifiableType>(outputContext.getTopLevelRegistryObjectTypeMap().values());

                if (log.isDebugEnabled()) {
                    Iterator<IdentifiableType> listIter = list.iterator();

                    while (listIter.hasNext()) {
                        RegistryObjectType debugRO = (RegistryObjectType) listIter.next();
                        log.debug(debugRO.getId() + "  " +
                            debugRO.getClass().getName() + "  " +
                            debugRO.getName());
                    }

                    log.debug("Objects found: " + list.size());
                }

                outputContext.checkObjects();

                pm.insert(outputContext, list);
            }
        } catch (RegistryException re) {
            log.error(re, re);
            throw re;
        } catch (Exception e) {
            log.error(e, e);
            throw new RegistryException(e);
        }

        return true;
    }
}
