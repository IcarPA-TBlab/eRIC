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
package it.cnr.icar.eric.server.persistence;

import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 * Interface exposed by all PersistenceManagers.
 * This is the contract implemented by the persistence layer of the
 * registry architecture.
 *
 * @author Farrukh Najmi
 */
public interface PersistenceManager {
    /**
     * Does a bulk insert of a heterogeneous Collection of RegistryObjects.
     *
     */
    public void insert(ServerRequestContext context, List<IdentifiableType> registryObjects)
        throws RegistryException;

    /**
     * Does a bulk update of a heterogeneous Collection of RegistryObjects.
     *
     */
    public void update(ServerRequestContext context, List<IdentifiableType> registryObjects)
        throws RegistryException;

    /**
     * Update the status of specified objects to the specified status.
     *
     */
    public void updateStatus(ServerRequestContext context, List<String> registryObjectsIds,
        String status) throws RegistryException;

    /**
     * Does a bulk delete of a Collection of ObjectRefTypes.
     *
     */
    public void delete(ServerRequestContext context, List<ObjectRefType> objectRefs)
        throws RegistryException;
    
    /**
     * Updates the idToLidMap in context entries with RegistryObject id as Key and RegistryObject lid as value 
     * for each object that matches specified id.
     *
     */    
    public void updateIdToLidMap(ServerRequestContext context, Set<String> ids, String tableName) throws RegistryException;       
    
    /**
     * Checks each object being deleted to make sure that it does not have any currently existing references.
     *
     * @throws ReferencesExistException if references exist to any of the RegistryObject ids specified in roIds
     */
    public void checkIfReferencesExist(ServerRequestContext context, List<String> roIds) throws RegistryException;
    
    /**
     * Gets the specified object using specified id and className
     *
     */
    public RegistryObjectType getRegistryObject(ServerRequestContext context, String id, String className)
        throws RegistryException;

    /**
     * Gets the specified object using specified ObjectRef
     *
     */
    public RegistryObjectType getRegistryObject(ServerRequestContext context, ObjectRefType ebObjectRefType)
        throws RegistryException;
    
    /**
     * Executes and SQL query using specified parameters.
     *
     * @return An List of RegistryObjectType instances
     */
    public List<IdentifiableType> executeSQLQuery(ServerRequestContext context, String sqlQuery,
        ResponseOptionType responseOption, String tableName, List<?> objectRefs)
        throws RegistryException;
    
    /**
     * Executes and SQL query using specified parameters.
     *
     * @return An List of RegistryObjectType instances
     */
    public List<?> executeSQLQuery(ServerRequestContext context, String sqlQuery,
        ResponseOptionType responseOption, String tableName, List<?> objectRefs,
        IterativeQueryParams paramHolder)
        throws RegistryException;
    

    /**
     * Executes an SQL Query.
     */
    public List<?> executeSQLQuery(ServerRequestContext context, String sqlQuery, List<String> queryParams,
        ResponseOptionType responseOption, String tableName, List<?> objectRefs)
        throws RegistryException;
    
    /**
     * Executes and SQL query using specified parameters.
     * This variant is used to invoke stored queries.
     *
     * @return An List of RegistryObjectType instances
     */
    public List<IdentifiableType> executeSQLQuery(ServerRequestContext context, String parametrizedQuery, List<String> queryParams,
        ResponseOptionType responseOption, String tableName, List<?> objectRefs,
        IterativeQueryParams paramHolder)
        throws RegistryException;
    
    /**
    *     Get a HashMap with registry object id as key and owner id as value
    */
    public HashMap<String, String> getOwnersMap(ServerRequestContext context, List<String> ids) throws RegistryException;
    
    /**
     * Sets the owner on the specified objects based upon RequestContext.
     */
    public void changeOwner(ServerRequestContext context, List<?> objects) throws RegistryException;
    
    /**
     * Gets a JDBC Connection.
     */
    public Connection getConnection(ServerRequestContext context) throws RegistryException;
    
    /**
     * Releases or relinqueshes a JDBC connection.
     */
    public void releaseConnection(ServerRequestContext context, Connection connection) throws RegistryException;
}
