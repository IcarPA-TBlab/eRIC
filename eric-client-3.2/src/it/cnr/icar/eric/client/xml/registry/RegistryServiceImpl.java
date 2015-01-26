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

import it.cnr.icar.eric.common.CredentialInfo;

import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryService;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ClassificationScheme;


/**
 * Implements JAXR API interface named RegistryService.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryServiceImpl implements RegistryService {
    private ConnectionImpl connection ;
    private BusinessQueryManagerImpl bqm ;
    private DeclarativeQueryManagerImpl dqm ;
    private BusinessLifeCycleManagerImpl lcm ;
    private ObjectCache objectCache ;
    private BulkResponse lastBulkResponse = null;

    RegistryServiceImpl(ConnectionImpl connection) {
        this.connection = connection;

        objectCache = new ObjectCache(this);

        try {
            getDeclarativeQueryManager();
            getBusinessLifeCycleManager();
            getBusinessQueryManager();
        }
        catch (JAXRException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns the CapabilityProfile for the JAXR provider
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @associates <{CapabilityProfile}>
     * @see <{LifeCycleManager}>
     */
    public CapabilityProfile getCapabilityProfile() throws JAXRException {
        CapabilityProfile profile = new CapabilityProfileImpl();

        return profile;
    }

    public ConnectionImpl getConnection() {
        return connection;
    }

    private BusinessLifeCycleManagerImpl getBusinessLifeCycleManagerImpl()
        throws JAXRException {
        if (lcm == null) {
            lcm = new BusinessLifeCycleManagerImpl(this);
        }

        return lcm;
    }

    /**
     * Returns the BusinessLifeCycleManager interface implemented by the
     * JAXR provider
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @associates <{BusinessLifeCycleManager}>
     * @see <{LifeCycleManager}>
     */
    public BusinessLifeCycleManager getBusinessLifeCycleManager()
        throws JAXRException {
        return getBusinessLifeCycleManagerImpl();
    }

    /**
     * Returns the basic LifeCycleManager interface implemented by the JAXR
     * provider
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @associates <{LifeCycleManager}>
     */
    public LifeCycleManager getLifeCycleManager() throws JAXRException {
        // This method will be removed from JAXR API
        return getBusinessLifeCycleManager();
    }

    /**
     * Returns the BusinessQueryManager interface implemented by the JAXR
     * provider
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @associates <{BusinessQueryManager}>
     * @directed
     */
    public BusinessQueryManager getBusinessQueryManager()
        throws JAXRException {
        if (bqm == null) {
            bqm = new BusinessQueryManagerImpl(this,
                    getBusinessLifeCycleManagerImpl());
        }

        return bqm;
    }

    /**
     * Returns the DeclarativeQueryManager interface implemented by the
     * JAXR provider Registries should throws
     * UnsupportedCapabilityException if they do not implement this
     * optional feature.
     *
     * <p><DL><DT><B>Capability Level: 0 (optional) </B></DL>
     *
     * @associates <{DeclarativeQueryManager}>
     * @directed
     */
    public DeclarativeQueryManager getDeclarativeQueryManager()
        throws JAXRException, UnsupportedCapabilityException {
        if (dqm == null) {
            dqm = new DeclarativeQueryManagerImpl(this,
                    getBusinessLifeCycleManagerImpl());
        }

        return dqm;
    }

    void setBulkResponse(BulkResponse br) {
        lastBulkResponse = br;
    }

    /**
     * Returns the BulkResponse associated with specified requestId.  Once
     * a client retrieves a BulkResponse for a particular requestId any
     * subsequent calls to retrieve the Bulkresponse for the same requestId
     * should result in an InvalidRequestException.  Throws an
     * InavlidRequestException if no responses exist for specified
     * requestId.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public BulkResponse getBulkResponse(String requestId)
        throws InvalidRequestException, JAXRException {
        // Async feature supported barely to meet JAXR TCK needs.
        if ((lastBulkResponse != null) && (lastBulkResponse.getRequestId().equals(requestId))) {
            BulkResponse br = lastBulkResponse;
            lastBulkResponse = null;
            return br;
        } else {
            throw new javax.xml.registry.InvalidRequestException(
                "Unknown requestId '" + requestId + "'.");
        }
    }

    /**
     * Get the default user-defined postal scheme for codifying the
     * attributes of PostalAddress
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public ClassificationScheme getDefaultPostalScheme()
        throws JAXRException {
        // Write your code here
        return null;
    }

    /**
     * This method takes a String that is an XML request in a registry
     * specific format. It sends the request to the registry and returns a
     * String that is the registry specific XML response.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return String that is the XML response in a registry specific manner.
     */
    public String makeRegistrySpecificRequest(String request)
        throws JAXRException {
        // Write your code here
        return null;
    }

    public ObjectCache getObjectCache() {
        return objectCache;
    }

    void setCredentialInfo(CredentialInfo credentialInfo) throws JAXRException {
        dqm.setCredentialInfo(credentialInfo);
        bqm.setCredentialInfo(credentialInfo);
        lcm.setCredentialInfo(credentialInfo);
    }

}
