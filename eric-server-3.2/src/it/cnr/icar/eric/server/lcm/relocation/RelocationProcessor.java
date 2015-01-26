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
package it.cnr.icar.eric.server.lcm.relocation;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.AuthorizationException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Processes an object relocation request.
 * Designed for one time use.
 * 
 * @author Farrukh S. Najmi
 * 
 */
public class RelocationProcessor {
    private static BindingUtility bu = BindingUtility.getInstance();
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private it.cnr.icar.eric.server.common.Utility util = it.cnr.icar.eric.server.common.Utility.getInstance();
    private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    private PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    
    private ServerRequestContext context = null;
    boolean isAdmin = false;
    RegistryResponseType resp = null;
    AdhocQueryType ahq = null;
    ObjectRefType srcRegistryRef = null;
    ObjectRefType destRegistryRef = null;
    ObjectRefType ownerAtSourceRef = null;
    ObjectRefType ownerAtDestRef = null;
    
    List<Object> objectsToRelocate = null;
    
    /** Creates a new instance of RelocationManagerImpl */
    public RelocationProcessor(ServerRequestContext context) {
        try {
            this.context = context;
            RelocateObjectsRequest req = (RelocateObjectsRequest)context.getCurrentRegistryRequest();
            isAdmin = ac.hasRegistryAdministratorRole(context.getUser());

            resp = bu.rsFac.createRegistryResponseType();

            ahq = req.getAdhocQuery();
            srcRegistryRef = req.getSourceRegistry();
            destRegistryRef = req.getDestinationRegistry();
            ownerAtSourceRef = req.getOwnerAtSource();
            ownerAtDestRef = req.getOwnerAtDestination();

            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
        }
        catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e,
                    "LifeCycleManagerImpl.relocateObjects", "Unknown");

            // Append any warnings
            List<RegistryError> errs = context.getErrorList().getRegistryError();
            RegistryErrorList ebRegistryErrorList = resp.getRegistryErrorList();
            ebRegistryErrorList.getRegistryError().addAll(errs);
        }    
    }
        
    public RegistryResponseType relocateObjects() {

        try {
            
            if ((destRegistryRef == null) || (srcRegistryRef.getId().equals(destRegistryRef.getId()))) {
                //This is an owner reassignment within same registry
                
                if (isAdmin) {
                    //If local owner reassignment initiated by an admin then just do it
                    //No need to involve ownerAtDestination
                    reAssignOwnerImmediately();
                } else {
                    throw new AuthorizationException(ServerResourceBundle.getInstance().getString("message.ownerReassignmentFailed",
                            new Object[]{context.getUser().getId()}));
                }

            }

            if (context.getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(context.getErrorList());
            }
        } catch (RegistryException e) {
            resp = util.createRegistryResponseFromThrowable(e,
                    "LifeCycleManagerImpl.relocateObjects", "Unknown");

            // Append any warnings
            List<RegistryError> errs = context.getErrorList().getRegistryError();
            RegistryErrorList newEl = resp.getRegistryErrorList();
            newEl.getRegistryError().addAll(errs);       
        }

        return resp;
    }    
    
    /**
     * Called if an admin requests a local owner reAssignement.
     * This is the simplest case and can be done synchronously
     * in same method invocation without any involvement of
     * ownerAtDestination.
     */
    public void reAssignOwnerImmediately() throws RegistryException {
        getObjectsToRelocate();
        context.setUser((UserType)pm.getRegistryObject(context, ownerAtDestRef));
        pm.changeOwner(context, objectsToRelocate);            
    }
    
    
    private void getObjectsToRelocate() throws RegistryException {
        objectsToRelocate = new ArrayList<Object>();
        try {                        
            AdhocQueryRequest ebAdhocQueryRequest = bu.queryFac.createAdhocQueryRequest();
            ebAdhocQueryRequest.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
            ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
            ebResponseOptionType.setReturnComposedObjects(true);
            ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
            ebAdhocQueryRequest.setResponseOption(ebResponseOptionType);            
            ebAdhocQueryRequest.setAdhocQuery(ahq);

            context.pushRegistryRequest(ebAdhocQueryRequest);
            AdhocQueryResponse ebAdhocQueryResponse = qm.submitAdhocQuery(context);
            
            RegistryObjectListType rol = ebAdhocQueryResponse.getRegistryObjectList();            
            objectsToRelocate.addAll(rol.getIdentifiable());
        } finally {
            context.popRegistryRequest();
        }
    }
}
