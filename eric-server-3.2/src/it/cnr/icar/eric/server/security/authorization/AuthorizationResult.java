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
package it.cnr.icar.eric.server.security.authorization;

import it.cnr.icar.eric.common.exceptions.UnauthorizedRequestException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



/** 
  *
  * @author Derek Hilder
  */
public class AuthorizationResult {
    
    public static final int INDETERMINATE = 0;
    public static final int PERMIT_ALL = 1;     // 0001
    public static final int PERMIT_NONE = 2;    // 0010
    public static final int PERMIT_SOME = 4;    // 0100

    @SuppressWarnings("unused")
	private String subjectId;
    private HashSet<String> permittedResources;
    private HashMap<String, UnauthorizedRequestException> deniedResourceExceptions;
    

    public AuthorizationResult(String subjectId) {
        this.subjectId = subjectId;
        permittedResources = new HashSet<String>();
        deniedResourceExceptions = new HashMap<String, UnauthorizedRequestException>();
    }
    
    public void addPermittedResource(String resourceId) {
        permittedResources.add(resourceId);
    }
    
    public void addPermittedResources(Collection<String> resourceIds) {
        permittedResources.addAll(resourceIds);
    }
    
    public void addDeniedResourceException(UnauthorizedRequestException e) {
        deniedResourceExceptions.put(e.getId(), e);
    }
            
    public int getResult() {
        int result = INDETERMINATE;
        if (deniedResourceExceptions.isEmpty()) {
            result = PERMIT_ALL;
        }
        else if (permittedResources.isEmpty()) {
            result = PERMIT_NONE;
        }
        else {
            result = PERMIT_SOME;
        }
        return result;
    }
    
    public Set<String> getPermittedResources() {
        return permittedResources;
    }
    
    public Set<String> getDeniedResources() {
        return deniedResourceExceptions.keySet();
    }
    
    /** Throw an UnauthorizedRequestException if the authorization results
      * match any of the results specified.
      *
      * @param results
      *     The results to throw an exception on. This may be PERMIT_ALL,
      *     PERMIT_NONE, PERMIT_SOME, or any combination of these or'd together.
      */    
    public void throwExceptionOn(int results) 
        throws UnauthorizedRequestException
    {
        int result = getResult();
        if ((result & results) != 0) {
            String firstDeniedTargetId = (String)getDeniedResources().toArray()[0];
            UnauthorizedRequestException e = deniedResourceExceptions.get(firstDeniedTargetId);
            throw e;
        }
    }
}
