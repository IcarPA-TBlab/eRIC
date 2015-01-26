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
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.MissingAttributeException;
import it.cnr.icar.eric.common.exceptions.MissingParentReferenceException;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.RegistryObject;

import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;


/**
 * Implements JAXR API interface named ExternalIdentifier.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ExternalIdentifierImpl extends RegistryObjectImpl
    implements ExternalIdentifier {
    private RegistryObjectRef registryObjectRef = null;
    private RegistryObjectRef schemeRef = null;
    private String value = null;

    public ExternalIdentifierImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ExternalIdentifierImpl(LifeCycleManagerImpl lcm,
        ExternalIdentifierType ebExtIdentifier)
        throws JAXRException {
        super(lcm, ebExtIdentifier);

        registryObjectRef = new RegistryObjectRef(lcm,
                ebExtIdentifier.getRegistryObject());
        schemeRef = new RegistryObjectRef(lcm,
                ebExtIdentifier.getIdentificationScheme());
        value = ebExtIdentifier.getValue();
    }

    //??JAXR 2.0
    public RegistryObjectRef getRegistryObjectRef() throws JAXRException {
        return registryObjectRef;
    }
    
    public RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject registryObject = null;

        if (registryObjectRef != null) {
            registryObject = registryObjectRef.getRegistryObject(
                    "RegistryObject");
        }

        return registryObject;
    }
    
    /**
     * Internal method to set the service
     */
    void setRegistryObjectInternal(RegistryObject registryObject) throws JAXRException {
        if (registryObject != null) {
            registryObjectRef = new RegistryObjectRef(lcm, registryObject);
        } else {
            registryObjectRef = null;
        }
        setModified(true);
    }

    /**
     * Sets the RegistryObject parent for this object.
     * Also adds it to parent if not already added.
     *
     * //TODO: JAXR 2.0??
     */
    public void setRegistryObject(RegistryObject registryObject) throws JAXRException {
        if (registryObjectRef != null) {
            if (registryObject != null) {
                if (!(registryObject.getKey().getId().equals(registryObjectRef.getId()))) {
                    if (!(registryObject instanceof RegistryObjectImpl)) {
                        throw new InvalidRequestException(
                            JAXRResourceBundle.getInstance().getString("message.error.expected.regestry.object",new Object[] {registryObject}));
                    }
                }
            }
        }

        setRegistryObjectInternal(registryObject);
        
        
        //In case this was called directly by client, make sure that this classification is added to classified Object
        Collection<?> extIds = registryObject.getExternalIdentifiers();

        if (!extIds.contains(this)) {
            registryObject.addExternalIdentifier(this);
        }
         
    }
    

    public String getValue() throws JAXRException {
        return value;
    }

    public void setValue(String par1) throws JAXRException {
        value = par1;
        setModified(true);
    }

    public ClassificationScheme getIdentificationScheme()
        throws JAXRException {
        ClassificationScheme scheme = null;

        if (schemeRef != null) {
            scheme = (ClassificationScheme) schemeRef.getRegistryObject(
                    "ClassificationScheme");
        }

        return scheme;
    }

    public void setIdentificationScheme(ClassificationScheme scheme)
        throws JAXRException {
        if (scheme == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.identificationscheme.null"));
        } else if ((schemeRef == null) || (!(scheme.getKey().getId().equals(schemeRef.getId())))) {
            schemeRef = new RegistryObjectRef(lcm, scheme);
            setModified(true);
        }
    }

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        ExternalIdentifierType ebExternalIdentifierType = factory.createExternalIdentifierType(); 
		setBindingObject(ebExternalIdentifierType);
		
//		JAXBElement<ExternalIdentifierType> ebExternalIdentifier = factory.createExternalIdentifier(ebExternalIdentifierType);
		
		return ebExternalIdentifierType;
    }

    protected void setBindingObject(ExternalIdentifierType ebExternalIdentifierType)
        throws JAXRException {
        super.setBindingObject(ebExternalIdentifierType);
        
        if (schemeRef != null) {
                ebExternalIdentifierType.setIdentificationScheme(schemeRef.getId());
        } else {
            throw new MissingAttributeException(this, getId(), "identificationScheme");
        }
        
        if (registryObjectRef != null) {
            ebExternalIdentifierType.setRegistryObject(registryObjectRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.ExternalIdentifier.object.id",new Object[] {getId()}));
        }
        
        ebExternalIdentifierType.setValue(value);
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<RegistryObjectRef> getRegistryObjectRefs() {
        HashSet<RegistryObjectRef> refs = new HashSet<RegistryObjectRef>();

        //refs.addAll(super.getRegistryObjectRefs());
        if (schemeRef != null) {
            refs.add(schemeRef);
        }

        return refs;
    }
}
