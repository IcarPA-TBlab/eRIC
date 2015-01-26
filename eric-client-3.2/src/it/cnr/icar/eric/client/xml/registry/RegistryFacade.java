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
package it.cnr.icar.eric.client.xml.registry;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.User;

/*
 * A facade interface for publishing and discovery of artifacts in Registry.
 * Current focus is to support WSDL publish and discovery. 
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public interface RegistryFacade {
    /*
     * Sets the endpoint URL information for target registry server.
     *
     * @param qmEndpoint a String representing the URL for QueryManager interface to registry. Required.
     * @param lcmEndpoint a String representing the URL for LifeCycleManager interface to registry. If null it defaults to qmEndpoint.
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     */
    public void setEndpoints(String qmEndpoint, String lcmEndpoint) throws JAXRException;    
    
    /*
     * Register a new user with registry server.
     * Creates a new registry issued digital certificate and stores
     * it into the client keystore using specified alias and password.
     * Also stores the public key in server keystore and stores the User instance
     * in the registry server.
     *
     * @param user the metadata desribing the new users's name and other information
     * @param alias the alias to be used for the user's new registry issued certificate
     * @param keypass the key password for user's new private key in the client keystore
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     *
     */
    public void registerUser(User user, String alias, String keypass) throws JAXRException;

    /**
     * Logs on with a new user context using the credentials specified. The
     * credentials are used to look up the user's public/private keys in teh client keystore
     * and use then to sign client requests to the server.
     *
     * @param alias the alias for user in the client keystore
     * @param keypass the key password for user's private key in the client keystore
     *
     * @throws JAXRException an exception thrown by JAXR Provider.
     *
     */
    public void logon(String alias, String keypass) throws JAXRException;

    /**
     * Logs off the current user from this RegistryFacade.
     *  
     */
    public void logoff() throws JAXRException;
    
    /*
     * Publishes a set of related files as a single zip file to the registry.
     * At present this method is intended to support publishing of a set
     * of related WSDL and possibly XML Schema documents as a single zip file to the registry.
     *
     *
     * @param baseDirectory the base directory for files being published.
     * @param relativeFilePaths the paths to files being published where each path is relative to baseDirectory.
     * @param objectType the objectType for the zip file being published. Should NOT be NULL. 
     *        For WSDL specify "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL".
     * @param id the identifier for the zip file being published. Must be a URN. Specify null if no id is specified. If null, the registry will assign a unique id.
     * @param name the name for the zip file being published. Specify null if no name is specified.
     * @param description the description for the zip file being published. Specify null if no description is specified.
     *
     * @return the ExtrinsicObject published as metadata.
     * @throws JAXRException an exception thrown by JAXR Provider.
     */
    public ExtrinsicObject publishFilesAsZip(
            String baseDirectory, 
            String[] relativeFilePaths,
            Map<?, ?> metadata 
            ) throws JAXRException;
    
    /**
     * Publishes a repositoryItem and the metadata that describes it.    
     *
     * @param repositoryItem the URL to the repositoryItem being published.
     * @param metadata describing the repositoryItem
     *
     * @return the ExtrinsicObject published as metadata.
     * @see it.cnr.icar.eric.common.CanonicalConstants for constants that may be used to identify keys in metadaat HashMap 
     */
    public ExtrinsicObject publish(URL repositoryItem, Map<?, ?> metadata) throws JAXRException;
    
    
    /**
     * Get the specified artifacts from Service Registry.
     *
     * @see [WSPROF] ebXML Registry Profile for Web Services: http://www.oasis-open.org/committees/document.php?document_id=14756
     *  for parameter descriptions.
     * @param queryId Identifies the discovery query that is preconfigured registry
     * @param queryParams key is a parameter name String (e.g. $service.name), value is a parameter value String as described by [WSPROF]
     * @return Collection of javax.xml.registry.infomodel.RegistryObject.
     * @throws JAXRException an exception thrown by JAXR Provider.
     */
    public Collection<?> executeQuery(String queryId, Map<String, String> queryParams) throws JAXRException;
    
    /**
     * Gets the RegistryService used by this RegistryFacade.
     */
    public RegistryService getService();

    /**
     * Gets the BusinessQueryManagerImpl used by this RegistryFacade.
     */
    public BusinessQueryManagerImpl getBusinessQueryManager();
    
    /**
     * Gets the BusinessLifeCycleManagerImpl used by this RegistryFacade.
     */
    public BusinessLifeCycleManagerImpl getLifeCycleManager();

    /**
     * Gets the DeclarativeQueryManagerImpl used by this RegistryFacade.
     */
    public DeclarativeQueryManagerImpl getDeclarativeQueryManager();    

    /**
     * Gets the Connection used by this RegistryFacade.
     */
    public Connection getConnection();

    
}
