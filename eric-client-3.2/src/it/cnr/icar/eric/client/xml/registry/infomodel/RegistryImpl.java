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
package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;


import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryEntry;


/**
 * Implements future JAXR API interface named Registry.
 * Add Registry to JAXR 2.0??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryImpl extends RegistryEntryImpl
    implements RegistryEntry {
    
    private Duration replicationSyncLatency;
    private Duration catalogingLatency;
    private String specificationVersion="3.0";
    private String conformanceProfile="registryLite";
    private RegistryObjectRef operator=null;
    
    public RegistryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public RegistryImpl(LifeCycleManagerImpl lcm,
        RegistryType registry) throws JAXRException {
        super(lcm, registry);
        
        replicationSyncLatency = registry.getReplicationSyncLatency();
        catalogingLatency = registry.getCatalogingLatency();
        specificationVersion = registry.getSpecificationVersion();
        conformanceProfile = registry.getConformanceProfile();
        operator = new RegistryObjectRef(lcm, registry.getOperator());
    }

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        RegistryType ebRegistryType = factory.createRegistryType(); 
		setBindingObject(ebRegistryType);
		
//		JAXBElement<RegistryType> ebRegistry = factory.createRegistry(ebRegistryType);
		
		return ebRegistryType;
    }

    protected void setBindingObject(RegistryType ebRegistry)
        throws JAXRException {
        super.setBindingObject(ebRegistry);
        
        ebRegistry.setReplicationSyncLatency(replicationSyncLatency);
        ebRegistry.setCatalogingLatency(catalogingLatency);
        ebRegistry.setSpecificationVersion(specificationVersion);
        ebRegistry.setConformanceProfile(conformanceProfile);
        ebRegistry.setOperator(operator.getId());
    }

    public String getReplicationSyncLatency() {
        return (replicationSyncLatency == null) ? "P1D" : replicationSyncLatency.toString();
    }

    public void setReplicationSyncLatency(String replicationSyncLatency) throws DatatypeConfigurationException {
        this.replicationSyncLatency = DatatypeFactory.newInstance().newDuration(replicationSyncLatency);
    }

    public String getCatalogingLatency() {
        return (catalogingLatency == null) ? "P1D" : catalogingLatency.toString();
    }

    public void setCatalogingLatency(String catalogingSyncLatency) throws DatatypeConfigurationException {
        this.catalogingLatency = DatatypeFactory.newInstance().newDuration(catalogingSyncLatency);
    }

    public String getSpecificationVersion() {
        return specificationVersion;
    }

    public void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }

    public String getConformanceProfile() {
        return conformanceProfile;
    }

    public void setConformanceProfile(String conformanceProfile) {
        this.conformanceProfile = conformanceProfile;
    }

    public RegistryObjectRef getOperator() {
        return operator;
    }

    public void setOperator(RegistryObjectRef operator) {
        this.operator = operator;
    }
}
