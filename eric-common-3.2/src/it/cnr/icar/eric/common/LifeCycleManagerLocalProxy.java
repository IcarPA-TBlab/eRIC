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
package it.cnr.icar.eric.common;

import it.cnr.icar.eric.common.exceptions.UserNotFoundException;
import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.LifeCycleManagerFactory;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.common.spi.RequestContext;

import java.lang.reflect.Method;
import java.security.cert.X509Certificate;

import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import it.cnr.icar.eric.common.CredentialInfo;
//import it.cnr.icar.eric.common.SOAPMessenger;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 *
 * @author  najmi
 */
public class LifeCycleManagerLocalProxy implements LifeCycleManager {
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();
    private LifeCycleManager serverLCM = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    private QueryManager serverQM = QueryManagerFactory.getInstance().getQueryManager();
    
    @SuppressWarnings("unused")
	private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    @SuppressWarnings("unused")
	private SOAPMessenger msgr = null;        
    
    /** Creates a new instance of LifeCycleManagerLocalImpl */
    public LifeCycleManagerLocalProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
        this.credentialInfo = credentialInfo;
    }
    
    public RegistryResponseType approveObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.approveObjects(context);
    }
    
    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponseType setStatusOnObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.setStatusOnObjects(context);        
    }
    
    public RegistryResponseType deprecateObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.deprecateObjects(context);
    }

    public RegistryResponseType unDeprecateObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.unDeprecateObjects(context);
    }
    
    public RegistryResponseType removeObjects(RequestContext context) throws RegistryException {
        context.setUser(getCallersUser());
        return serverLCM.removeObjects(context);
    }
        
    public RegistryResponseType submitObjects(RequestContext context) throws RegistryException {
        UserType user =null;
        
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        // handle self registration
        user = checkRegisterUser((SubmitObjectsRequest)context.getCurrentRegistryRequest());
        // null means no registration. Proceed the normal way
        if (user == null) {
            user = getCallersUser();
        }
        context.setUser(user);
        return serverLCM.submitObjects(context);
    }
    
    public RegistryResponseType updateObjects(RequestContext context) throws RegistryException {
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        context.setUser(getCallersUser());
        return serverLCM.updateObjects(context);
    }
    
    private UserType getCallersUser() throws RegistryException {
        X509Certificate cert = null;
        if (credentialInfo != null) {
            cert = credentialInfo.cert;
        }
        return serverQM.getUser(cert);
    }
    
    protected UserType checkRegisterUser(SubmitObjectsRequest req)
    throws RegistryException {
        // is it a user registration?
        // are we in local mode?
        // do we have an x509??
        if (credentialInfo != null && credentialInfo.cert != null) {
            try {
                UserType user = getCallersUser();
                // sanity check
                if (user == null) {
                    throw new UserNotFoundException("catch me");
                }
            } catch (UserNotFoundException e) {
                // UserRegistrar checks for only one user in the request
                // Call user registrar by reflection, mask the dependency of
                // common on server.. Not a best practice, but used by this
                // proxy already. What we want is:
                // UserRegistrar.getInstance().registerUser(
                //            credentialInfo.cert,req);
                try {
                    Class<?> clazz = Class.forName(
                            "it.cnr.icar.eric.server.security.authentication.UserRegistrar");
                    Method getInstance = clazz.getMethod("getInstance", new Class[] {});
                    Object userRegistrar = getInstance.invoke(null, new Object[] {});
                    Method registerUser = clazz.getMethod("registerUser", new Class []
                        {X509Certificate.class, SubmitObjectsRequest.class});
                    Object user = registerUser.invoke(userRegistrar, new Object[]
                        {credentialInfo.cert, req});
                    return (UserType)user;
                } catch (Exception re) {
                    // Log error, return null (proceed with normal way)
                    log.error(CommonResourceBundle.getInstance().getString("message.ExceptionWhenCallingUserRegistrarRegisterUser"), re);
                    //TODO throw exception with "internal server error"?!
                }
            }
        }
        return null;
    }
    
    public RegistryResponseType relocateObjects(RequestContext context) throws RegistryException {
        return serverLCM.relocateObjects(context);
    }
    
    /** Sends an impl specific protocol extension request. */
    public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException {
        
        RegistryResponseHolder respHolder = null;
        bu.convertRepositoryItemMapForServer(context.getRepositoryItemsMap());
        
        context.setUser(getCallersUser());
        respHolder = serverLCM.extensionRequest(context);
        
        bu.convertRepositoryItemMapForClient(respHolder.getAttachmentsMap());
        
        return respHolder;
    }
}
