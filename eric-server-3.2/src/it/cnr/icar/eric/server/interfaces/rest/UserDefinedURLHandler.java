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
package it.cnr.icar.eric.server.interfaces.rest;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.UnimplementedException;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.registry.InvalidRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class UserDefinedURLHandler extends URLHandler {
    
    
    @SuppressWarnings("unused")
	private Log log = LogFactory.getLog(this.getClass());
    @SuppressWarnings("unused")
	private BindingUtility bu = BindingUtility.getInstance();


    protected UserDefinedURLHandler() {
    }
    
    UserDefinedURLHandler(HttpServletRequest request,
        HttpServletResponse response) throws RegistryException {
            
        super(request, response);
    }    
    
    
    /**
     * Processes a Get Request
     */
    void processGetRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {       
        try {
            //First see if URL matches a repositoryItem
            getRepositoryItemByURI();
        }
        catch (ObjectNotFoundException e) {
            //No match on repository item so see if it matches a RegistryObject
            getRegistryObjectByURI();
        }        
    }
    
    
    /**
     * Attempt to get a RepositoryItem by its URI.
     *
     */
    private List<?> getRepositoryItemByURI() 
        throws IOException, RegistryException, ObjectNotFoundException 
    {
        List<?> results = null;
        String pathInfo = request.getPathInfo();
        
        //If path begins with a '/' then we also need to check for same patch but without leading '/'
        //because zip files with relative entry paths when cataloged do not have the leading '/'
        String pathInfo2 = new String(pathInfo);
        if (pathInfo2.charAt(0) == '/') {
            pathInfo2 = pathInfo2.substring(1);
        }

        try {
            ExtrinsicObjectType ro = null;
            
            String queryString = 
                "SELECT eo.* FROM ExtrinsicObject eo, Slot s " +
                "WHERE (s.value='" + pathInfo + "' OR s.value='" + pathInfo2 + "') AND " +
                    "s.name_='" + BindingUtility.CANONICAL_SLOT_CONTENT_LOCATOR + "' AND s.parent=eo.id";
            
            results = submitQueryAs(queryString, currentUser);
            
            if (results.size() == 0) {
                throw new ObjectNotFoundException(ServerResourceBundle.getInstance().getString("message.noRepositoryItemFound",
                        new Object[]{pathInfo})); 
            } else if (results.size() == 1) {
                ro = (ExtrinsicObjectType) results.get(0);
                writeRepositoryItem(ro);                
            } else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.duplicateRegistryObjects",
                        new Object[]{pathInfo})); 
            }
        } 
        catch (NullPointerException e) {
            e.printStackTrace();
            throw new RegistryException(it.cnr.icar.eric.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
        }

        return results;
    }

    /**
     * Attempt to get a RegistryObject by its URI.
     *
     */
    private List<?> getRegistryObjectByURI() 
        throws IOException, RegistryException, ObjectNotFoundException  
    {
        List<?> results = null;
        @SuppressWarnings("unused")
		Locale locale = request.getLocale();
        String pathInfo = request.getPathInfo();

        try {
            RegistryObjectType ro = null;
            
            String queryString = 
                "SELECT ro.* FROM RegistryObject ro, Slot s " +
                "WHERE s.value='" + pathInfo + "' AND " +
                    "s.name_='" + BindingUtility.CANONICAL_SLOT_LOCATOR + "' AND s.parent=ro.id";
            
            results = submitQueryAs(queryString, currentUser);

            if (results.size() == 0) {                
                throw new ObjectNotFoundException(
                    ServerResourceBundle.getInstance().getString("message.noRegistryObjectFound",
                        new Object[]{pathInfo})); 
            } else if (results.size() == 1) {
                ro = (RegistryObjectType) results.get(0);
                writeRegistryObject(ro);
            } else {
                throw new RegistryException(
                    ServerResourceBundle.getInstance().getString("message.duplicateRegistryObjects",
                        new Object[]{pathInfo})); 
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new RegistryException(it.cnr.icar.eric.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
        }

        if ((results == null) || (results.size() == 0)) {
            throw new ObjectNotFoundException(ServerResourceBundle.getInstance().getString("message.noRegistryObjectFound",
                    new Object[]{pathInfo})); 
        }
        
        return results;
    }
    
}
