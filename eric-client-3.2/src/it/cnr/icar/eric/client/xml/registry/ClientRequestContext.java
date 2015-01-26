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

import it.cnr.icar.eric.common.CommonRequestContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * Implements the RequestContext interface for JAXR Provider client.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClientRequestContext extends CommonRequestContext {

    //Objects originally specified in request
    @SuppressWarnings("rawtypes")
	private Set originalObjects = new HashSet();

    @SuppressWarnings("rawtypes")
	private Set composedObjects = new HashSet();
    @SuppressWarnings("rawtypes")
	private Set processedObjects = new HashSet();
    @SuppressWarnings("rawtypes")
	private Set candidateSubmitObjects = new HashSet();

	@SuppressWarnings("rawtypes")
	private Map submitObjectsMap = new HashMap();
    private Map<String, String> slotsMap = null;
    
    
    public ClientRequestContext(String contextId, RegistryRequestType request) {
        super(contextId, request);       
    }

    public Set<?> getOriginalObjects() {
        return originalObjects;
    }

    public void setOriginalObjects(Set<?> originalObjects) {
        this.originalObjects = originalObjects;
    }

    public Set<?> getComposedObjects() {
        return composedObjects;
    }

    public void setComposedObjects(Set<?> composedObjects) {
        this.composedObjects = composedObjects;
    }

    @SuppressWarnings("rawtypes")
	public Set getProcessedObjects() {
        return processedObjects;
    }

    public void setProcessedObjects(Set<?> processedObjects) {
        this.processedObjects = processedObjects;
    }

    @SuppressWarnings("rawtypes")
	public Set getCandidateSubmitObjects() {
        return candidateSubmitObjects;
    }

    public void setCandidateSubmitObjects(Set<?> candidateSubmitObjects) {
        this.candidateSubmitObjects = candidateSubmitObjects;
    }



	@SuppressWarnings("rawtypes")
	public Map getSubmitObjectsMap() {
        return submitObjectsMap;
    }

    public void setSubmitObjectsMap(@SuppressWarnings("rawtypes") Map submitObjectsMap) {
        this.submitObjectsMap = submitObjectsMap;
    }

    public Map<String, String> getSlotsMap() {
        return slotsMap;
    }

    public void setSlotsMap(Map<String, String> slotsMap) {
        this.slotsMap = slotsMap;
    }
    
}
