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

import it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;

import javax.xml.registry.LifeCycleManager;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.FederationType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.datatype.Duration;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Implements a future JAXR API interface named Federation.
 * TODO: Add this as interface to JAXR 2.0
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class FederationImpl extends RegistryEntryImpl
    implements RegistryEntry {
        
    Duration replicationSyncLatency;
        
    public FederationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public FederationImpl(LifeCycleManagerImpl lcm,
        FederationType federation) throws JAXRException {
        super(lcm, federation);
        
        replicationSyncLatency = federation.getReplicationSyncLatency();
    }
    
    public void join(RegistryImpl registry) throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);
        Association ass = lcm.createAssociation(registry, assocType);
        addAssociation(ass);

        //No need to call setModified(true) since RIM modified object is an Assoociation				
        
    }

    public void join(RegistryObjectRef registryRef) throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);
        AssociationImpl ass = (AssociationImpl)lcm.createObject(LifeCycleManager.ASSOCIATION);
        ass.setAssociationType(assocType);
        ass.setTargetObjectRef(registryRef);
        addAssociation(ass);

        //No need to call setModified(true) since RIM modified object is an Assoociation				
        
    }
    
    public void leave(RegistryImpl registry) throws JAXRException {
	HashSet<Object> associations = new HashSet<Object>(getAssociations());
        Iterator<Object> iter = associations.iterator();

        while (iter.hasNext()) {
            Association ass = (Association) iter.next();

            if (ass.getTargetObject().equals(registry)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
		    removeAssociation(ass);
		}
            }
        }

        //No need to call setModified(true) since RIM modified object is an Association				        
    }
    
    public void leave(RegistryObjectRef registryRef) throws JAXRException {
	HashSet<Object> associations = new HashSet<Object>(getAssociations());
        Iterator<Object> iter = associations.iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getTargetObjectRef().equals(registryRef)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
		    removeAssociation(ass);
		}
            }
        }

        //No need to call setModified(true) since RIM modified object is an Association				        
    }
    
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        FederationType ebFederationType = factory.createFederationType(); 
		setBindingObject(ebFederationType);
		
//		JAXBElement<FederationType> ebFederation = factory.createFederation(ebFederationType);
		
		return ebFederationType;
	}
    
    public Set<RegistryObject> getFederationMembers() throws JAXRException {
        Set<RegistryObject> members = new HashSet<RegistryObject>();

        Iterator<?> iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
                members.add(ass.getTargetObject());
            }
        }

        return members;
    }    

}
