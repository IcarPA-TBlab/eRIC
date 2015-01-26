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

//import it.cnr.icar.eric.common.CredentialInfo;
//import it.cnr.icar.eric.common.SOAPMessenger;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.RequestContext;

import java.security.cert.X509Certificate;

import javax.xml.registry.RegistryException;


/**
 * Code common to all QueryManagerProxy implementations
 */
public abstract class AbstractQueryManagerProxy implements QueryManager {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected String registryURL;
    protected CredentialInfo credentialInfo;
    protected SOAPMessenger msgr;

    /**
     * Implements methods and contains data common to all QueryManagerProxy implementations.
     * @param registryURL URL of registry to which to connect.
     * @param credentialInfo Credentials to use when connecting to @registryUrl
     */
    public AbstractQueryManagerProxy(String registryURL,
        CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }

    /**
     * Submit the ad hoc query contained in @context.
     * @param context Context containing the ad hoc query.
     * @return an AdhocQueryResponse.
     * @throws javax.xml.registry.RegistryException if an error occurs.
     */
    public abstract AdhocQueryResponse submitAdhocQuery(
        RequestContext context) throws RegistryException;

    /**
     * Gets the registry object with the specified id, if the object exists.
     * @param context Context through which to make the request.
     * @param id Id of the RegistryObject to get.
     * @return the RegistryObject with Id matching @id.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public RegistryObjectType getRegistryObject(RequestContext context,
        String id) throws RegistryException {
        return getRegistryObject(context, id, "RegistryObject");
    }

    /**
     * Gets the RegistryObject with the specified Id and specified object type.
     * @param context The context within which to make the request to the server.
     * @param id Id of the RegistryObject to get.
     * @param typeName Type of the RegistryObject to get.
     * @return the RegistryObject with the specified Id and object type.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public abstract RegistryObjectType getRegistryObject(
        RequestContext context, String id, String typeName)
        throws RegistryException;

    /**
     * Gets the RepositoryItem, if any, corresponding to the RegistryObject with the specified Id.
     * @param context Context within which to make the request to the server.
     * @param id Id of the RegistryObject for which the corresponding RepositoryItem is to be returned.
     * @return the RepositoryItem, if it exists.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public abstract RepositoryItem getRepositoryItem(RequestContext context,
        String id) throws RegistryException;

    /**
     * Gets the User associated with the specified certificate.
     * @param cert Certificate from which to get the User.
     * @return the User for the certificate.
     * @throws javax.xml.registry.RegistryException if an error occurred.
     */
    public UserType getUser(X509Certificate cert) throws RegistryException {
        throw new RegistryException(resourceBundle.getString(
                "message.unimplemented"));
    }
}
