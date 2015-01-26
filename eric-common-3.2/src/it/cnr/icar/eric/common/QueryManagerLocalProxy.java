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

import it.cnr.icar.eric.common.exceptions.RepositoryItemNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.common.spi.RequestContext;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

//import it.cnr.icar.eric.common.CredentialInfo;
//import it.cnr.icar.eric.common.SOAPMessenger;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


public class QueryManagerLocalProxy implements QueryManager {
    
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private static BindingUtility bu = BindingUtility.getInstance();
    private UserType callersUser = null;
    
    
    @SuppressWarnings("unused")
	private String registryURL = null;
    private CredentialInfo credentialInfo = null;
    @SuppressWarnings("unused")
	private SOAPMessenger msgr = null;
    
    public QueryManagerLocalProxy(String registryURL, CredentialInfo credentialInfo) {
        // DBH 4/8/04 - Seems like this was needed, otherwise getCallersUser()
        // always returned null.
        this.credentialInfo = credentialInfo;
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public AdhocQueryResponse submitAdhocQuery(RequestContext context) throws 
         RegistryException {
        AdhocQueryResponse ebAdhocQueryResponse = null;
         context.setUser(getCallersUser());
         ebAdhocQueryResponse = qm.submitAdhocQuery(context);
         bu.convertRepositoryItemMapForClient(context.getRepositoryItemsMap());
         
         return ebAdhocQueryResponse;
    }
    
    public RegistryObjectType getRegistryObject(RequestContext context, String id) throws RegistryException {
        return getRegistryObject(context, id, "RegistryObject");
    }
    
    public RegistryObjectType getRegistryObject(RequestContext context, String id, String typeName) throws RegistryException {
        RegistryObjectType ro = null;
        try {
            typeName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(typeName);
            
            HashMap<String,String> queryParams = new HashMap<String, String>();
            queryParams.put("$id", id);
            queryParams.put("$tableName", typeName);
            AdhocQueryRequest ebAdhocQueryRequest = bu.createAdhocQueryRequest("urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType", queryParams);

            context.pushRegistryRequest(ebAdhocQueryRequest);
            AdhocQueryResponse ebAdhocQueryResponse = submitAdhocQuery(context);
            
            RegistryResponseHolder respHolder = new RegistryResponseHolder(ebAdhocQueryResponse, null);
            List<?> results = respHolder.getCollection();
            if (results.size() == 1) {
            	if (results.get(0) instanceof JAXBElement)
            		 ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType)((JAXBElement<?>)results.get(0)).getValue();
            	else ro = (org.oasis.ebxml.registry.bindings.rim.RegistryObjectType) results.get(0);
            }
        }
        catch (RegistryException e) {
            throw e;
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        } finally {
            context.popRegistryRequest();
        }

        return ro;
    }
    
    public RepositoryItem getRepositoryItem(RequestContext context, String id) throws RegistryException {
        RepositoryItem ri = null;

	try {
	    ri = qm.getRepositoryItem(context, id);
	} catch (RepositoryItemNotFoundException ex) {
	    // Ignore exception: Unexpected for JAXR 1.0, fine in JAXR 2.0
	    // Universally, client code expects null return in this case.
		ri = null;
	}

        return ri;
    }
    
    private UserType getCallersUser() throws RegistryException {
        X509Certificate cert = null;
        if (credentialInfo != null) {
            cert = credentialInfo.cert;
        }
        return getUser(cert);
    }
            
    /**
     * Looks up the server side User object based upon specified public key certificate.
     */
    public UserType getUser(X509Certificate cert) throws RegistryException {
        
        callersUser = qm.getUser(cert);
        
        return callersUser;
    }
        
    
}
