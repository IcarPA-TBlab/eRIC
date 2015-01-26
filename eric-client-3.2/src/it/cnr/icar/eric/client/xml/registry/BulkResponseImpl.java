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

import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.FindException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Implements JAXR API interface named BulkResponse
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class BulkResponseImpl implements BulkResponse {
    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(BulkResponseImpl.class.getName());

    String requestId;
    private int status;
    @SuppressWarnings("rawtypes")
	private ArrayList collection = new ArrayList();
    private Collection<RegistryException> registryExceptions;
    private RegistryResponseType ebResponse;

    /**
     * Construct an empty successful BulkResponse
     */
    BulkResponseImpl() throws JAXRException {
        status = STATUS_SUCCESS;
    }

    /**
     * Note: BulkResponseImpl is not an infomodel object even though this
     * constructor looks like constructors in the infomodel subpackage.
     * Therefore, the LifeCycleManagerImpl argument is not stored.
     */
    public BulkResponseImpl(LifeCycleManagerImpl lcm,
        RegistryResponseType ebResponse, @SuppressWarnings("rawtypes") Map responseAttachments)
        throws JAXRException {
        requestId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        this.ebResponse = ebResponse;
        String ebStatus = ebResponse.getStatus();

        if (ebStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success)) {
            status = STATUS_SUCCESS;
        } else if (ebStatus.equals(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Unavailable)) {
            status = STATUS_UNAVAILABLE;
        } else {
            status = STATUS_FAILURE;
        }

        if (ebResponse instanceof AdhocQueryResponse) {
            AdhocQueryResponse aqr = (AdhocQueryResponse) ebResponse;
            RegistryObjectListType queryResult = aqr.getRegistryObjectList();
            processQueryResult(queryResult, lcm, responseAttachments);
        }

        RegistryErrorList errList = ebResponse.getRegistryErrorList();

        if (errList != null) {
            List<RegistryError> errs = errList.getRegistryError();
            Iterator<RegistryError> iter = errs.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();
                RegistryError ebRegistryError = (RegistryError) obj;

                // XXX Need to add additional error info to exception somehow
                addRegistryException(new FindException(ebRegistryError.getValue()));
            }

            // XXX What to do about optional highestSeverity attr???
            //             errList.getHighestSeverity();
        }

        ((RegistryServiceImpl)(lcm.getRegistryService())).setBulkResponse(this);
    }

    /**
     * Get the Collection of of objects returned as a response of a
     * bulk operation.
     * Caller thread will block here if result is not yet available.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    @SuppressWarnings("rawtypes")
	public Collection getCollection() throws JAXRException {
        return collection;
    }

    /**
     * Sets the Collection of objects returned for the response
     * Package protected access meant to be called only by provider impl.
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	void setCollection(Collection c) {
        collection.clear();
        collection.addAll(c);
    }

    /**
     * Get the JAXRException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no JAXRException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public Collection<RegistryException> getExceptions() throws JAXRException {
        return registryExceptions;
    }

    /**
     * Returns true if the reponse is a partial response due to large result set
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isPartialResponse() throws JAXRException {
        // Write your code here
        return false;
    }

    /**
     * Returns the unique id for the request that generated this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public String getRequestId() throws JAXRException {
        // Write your code here
        return requestId;
    }

    /**
     * Returns the status for this response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public int getStatus() throws JAXRException {
        return status;
    }

    void setStatus(int status) throws JAXRException {
        this.status = status;
    }

    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public boolean isAvailable() throws JAXRException {
        //?? stub
        return true;
    }

    void addExceptions(Collection<RegistryException> exes) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList<RegistryException>();
        }

        registryExceptions.addAll(exes);
    }

    private void addRegistryException(RegistryException rex) {
        if (registryExceptions == null) {
            registryExceptions = new ArrayList<RegistryException>();
        }

        registryExceptions.add(rex);
    }

    @SuppressWarnings("unchecked")
	private void processQueryResult(RegistryObjectListType sqlResult,
        LifeCycleManagerImpl lcm, @SuppressWarnings("rawtypes") Map repositoryItemsMap) throws JAXRException {
        @SuppressWarnings("unused")
		ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();
        
		/*	             
		 * getIdentifiable() returns JAXBelement<? extends IdentifiableType>
         * 
         * return should be List of ComplexType
         */
//        List items = sqlResult.getIdentifiable();
        List<JAXBElement<? extends IdentifiableType>> ebIdentifiableList = sqlResult.getIdentifiable(); 

		// take ComplexType from each Element
		Iterator<JAXBElement<? extends IdentifiableType>> ebIdentifiableListIter = ebIdentifiableList.iterator();
		List<IdentifiableType> ebIdentifiableTypeList = new ArrayList<IdentifiableType>();
		while (ebIdentifiableListIter.hasNext()) {
			ebIdentifiableTypeList.add(ebIdentifiableListIter.next().getValue());
		}
        
//        collection.addAll(JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, items, repositoryItemsMap));
        collection.addAll(JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, ebIdentifiableTypeList, repositoryItemsMap));
    }

    RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;

        // check for errors
        Collection<RegistryException> exceptions = getExceptions();

        if (exceptions != null) {
            Iterator<RegistryException> iter = exceptions.iterator();
            Exception exception = null;

            while (iter.hasNext()) {
                exception = iter.next();
                throw new JAXRException(exception);
            }
        }

        Collection<?> results = getCollection();
        Iterator<?> iter = results.iterator();

        if (iter.hasNext()) {
            ro = (RegistryObject) iter.next();
        }

        return ro;
    }

    public RegistryResponseType getRegistryResponse() {
        return ebResponse;
    }
}
