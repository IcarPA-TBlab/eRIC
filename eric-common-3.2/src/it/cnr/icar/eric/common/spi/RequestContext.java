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
package it.cnr.icar.eric.common.spi;

import java.util.HashMap;
import java.util.Stack;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * The interface that carries Request specific context information.
 * Implemented differently by client and server to serve their 
 * specific needs for providing state and context during the 
 * processing of a request.
 * 
 * @author Farrukh Najmi
 * @author Diego Ballve
 *
 */
public interface RequestContext {
    /**
     * Gets the current RegistryRequest being processed by the context.
     * The context keeps a stack of RegistryRequests internally and return the top
     * of the stack.
     *
     * @return the current RegistryRequestType at the top of the stack of RegistryRequests.
     */
    public RegistryRequestType getCurrentRegistryRequest();
    
    /**
     * Pushes a new request on the stack of RegistryRequests.
     * This is used when client needs to issue a new RegistryRequest
     * while processing an existing RegistryRequest.
     *
     */
    public void pushRegistryRequest(RegistryRequestType req);
    
    /**
     * Pops the last request on the stack of RegistryRequests.
     * This is used when client is done with issuing a new RegistryRequest
     * while processing an existing RegistryRequest.
     *
     * @return the current RegistryRequestType at the top of the stack of RegistryRequests.
     */
    public RegistryRequestType popRegistryRequest();
    
    /**
     * Gets the Stack of RegistryRequests associated with this context.
     *
     */
    public Stack<RegistryRequestType> getRegistryRequestStack();
    
    /**
     * Gets the user associated with the RequestContext.
     *
     * @return the user associated with the request.
     */
    public UserType getUser();
    
    
    /**
     * Sets the user associated with this context.
     */
    public void setUser(UserType user) throws RegistryException;
    
    /**
     * Gets the id associated with this context.
     *
     */
    public String getId();
    
    /**
     * Sets the id associated with this context.
     * Typically this SHOULD be a URN that identifies the class and method where this context was created
     * or the registry interface method where it is being used.
     */
    public void setId(String id);

    /**
     * Gets the Map that keeps track of association between and ExtrinsicObjects and its RepositoryItem.
     *
     * @return the Map where key is RegistryObject id and value is a RepositoryItem.
     */
    public HashMap<String, Object> getRepositoryItemsMap();

    /**
     * Sets the Map that keeps track of association between and ExtrinsicObjects and its RepositoryItem.
     *
     * @param repositoryItemsMap the Map where key is RegistryObject id and value is a RepositoryItem.
     */
    public void setRepositoryItemsMap(HashMap<String, Object> repositoryItemsMap);
    
}
