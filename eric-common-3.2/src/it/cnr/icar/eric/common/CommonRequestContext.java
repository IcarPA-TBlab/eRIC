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

import it.cnr.icar.eric.common.spi.RequestContext;

import java.util.HashMap;
import java.util.Stack;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 *
 * @author najmi
 */
public class CommonRequestContext implements RequestContext {
    
    //The stack of RegistryRequests associated with the context
    private Stack<RegistryRequestType> requestStack = new Stack<RegistryRequestType>();   
    
    //The user associated with the context
    private UserType user = null;   
    
    //The context id
    private String id = null;
    
    //Map of  repositoryItems within the request with id keys and RepositoryItem values
    private HashMap<String, Object> repositoryItemsMap = new HashMap<String, Object>();
    
    
    @SuppressWarnings("unused")
	private CommonRequestContext() {
    }

    public CommonRequestContext(String id, RegistryRequestType request) {
        this.id = id;
        if (request != null) {
            this.pushRegistryRequest(request);
        }
    }

    public RegistryRequestType getCurrentRegistryRequest() {
        return getRegistryRequestStack().peek();
    }

    public void pushRegistryRequest(RegistryRequestType req) {
        getRegistryRequestStack().push(req);
    }    
    
    public RegistryRequestType popRegistryRequest() {
        return getRegistryRequestStack().pop();
    }    
    
    public UserType getUser() {
        return user;
    }

    public void setUser(UserType user) throws RegistryException {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
    }

    public HashMap<String, Object> getRepositoryItemsMap() {
        return repositoryItemsMap;
    }

    public void setRepositoryItemsMap(HashMap<String, Object> idToRepositoryItemsMap) {
        this.repositoryItemsMap = idToRepositoryItemsMap;
    }

    public Stack<RegistryRequestType> getRegistryRequestStack() {
        return requestStack;
    }
}
