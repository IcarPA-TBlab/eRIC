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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * Class Declaration for Class1
 *
 */
public class RegistryResponseHolder {
    @SuppressWarnings("unused")
	private ArrayList<Object> collection = new ArrayList<Object>();
    private RegistryResponseType ebRegistryResponseType = null;
    private HashMap<String, Object> responseAttachments = null;

    /**
     * Construct an empty successful BulkResponse
     */
    @SuppressWarnings("unused")
	private RegistryResponseHolder()  {
    }

    public RegistryResponseHolder(RegistryResponseType ebRegistryResponseType, HashMap<String, Object> responseAttachments) {
        this.ebRegistryResponseType = ebRegistryResponseType;
        this.responseAttachments = responseAttachments;
    }

    /**
     * Get the RegistryException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no RegistryException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public List<RegistryException> getExceptions() throws RegistryException {
        ArrayList<RegistryException> registryExceptions = new ArrayList<RegistryException>();
        
        RegistryErrorList ebRegistryErrorList = ebRegistryResponseType.getRegistryErrorList();

        if (ebRegistryErrorList != null) {
            List<RegistryError> errs = ebRegistryErrorList.getRegistryError();
            Iterator<RegistryError> iter = errs.iterator();

            while (iter.hasNext()) {
                RegistryError error = iter.next();

                //TODO: Need to add additional error info to exception somehow
                registryExceptions.add(new RegistryException(error.getValue()));
            }
        }
        return registryExceptions;
    }

    public List<?> getCollection() throws RegistryException {
        List<?> roList = null;
        
        if (ebRegistryResponseType instanceof AdhocQueryResponse) {
            AdhocQueryResponse ebAdhocQueryResponse = (AdhocQueryResponse)ebRegistryResponseType;
            RegistryObjectListType ebRegistryObjectListType = ebAdhocQueryResponse.getRegistryObjectList();
            roList = ebRegistryObjectListType.getIdentifiable();
        }

        return roList;
    }
    
    public RegistryResponseType getRegistryResponseType() {
        return ebRegistryResponseType;
    }
    
    
    public HashMap<String, Object> getAttachmentsMap() {
        return responseAttachments;
    }
    
}
