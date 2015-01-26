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


import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.MissingParentReferenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;


/**
 * Implements JAXR API interface named SpecificationLink.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class SpecificationLinkImpl extends RegistryObjectImpl
    implements SpecificationLink {
    private RegistryObjectRef specificationObjectRef = null;
    private InternationalString usageDescription = null;
    private ArrayList<String> usageParams = new ArrayList<String>();
    private RegistryObjectRef bindingRef = null;
   
    public SpecificationLinkImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public SpecificationLinkImpl(LifeCycleManagerImpl lcm,
        SpecificationLinkType ebSpecLink) throws JAXRException {
        super(lcm, ebSpecLink);

        //In version 2.1 there is no servicebinding on specLink due to a spec bug.
        //SO instead of a lazy fetch we will fetch up front
        bindingRef = new RegistryObjectRef(lcm, ebSpecLink.getServiceBinding());        
        if (ebSpecLink.getSpecificationObject() != null) {
            specificationObjectRef = new RegistryObjectRef(lcm,
                    ebSpecLink.getSpecificationObject());
        }

        if (ebSpecLink.getUsageDescription() != null) {
            usageDescription = new InternationalStringImpl(lcm,
                    ebSpecLink.getUsageDescription());
        }

        Iterator<String> usageParamsIt = ebSpecLink.getUsageParameter().iterator();

        while (usageParamsIt.hasNext()) {
            usageParams.add(usageParamsIt.next());
        }
    }

    public RegistryObject getSpecificationObject() throws JAXRException {
        RegistryObject specificationObject = null;

        if (specificationObjectRef != null) {
            specificationObject = specificationObjectRef.getRegistryObject(
                    "RegistryObject");
        }

        return specificationObject;
    }

    public void setSpecificationObject(RegistryObject specificationObject)
        throws JAXRException {
        specificationObjectRef = new RegistryObjectRef(lcm, specificationObject);
        setModified(true);
    }

    public InternationalString getUsageDescription() throws JAXRException {
        if (usageDescription == null) {
            usageDescription = lcm.createInternationalString();
        }

        return usageDescription;
    }

    public void setUsageDescription(InternationalString desc)
        throws JAXRException {
        usageDescription = desc;
        setModified(true);
    }

    public Collection<String> getUsageParameters() throws JAXRException {
        return usageParams;
    }

    @SuppressWarnings("unchecked")
	public void setUsageParameters(@SuppressWarnings("rawtypes") Collection par1) throws JAXRException {
        usageParams.clear();
        usageParams.addAll(par1);
        setModified(true);
    }

    //??JAXR 2.0
    public RegistryObjectRef getServiceBindingRef() throws JAXRException {
        return bindingRef;
    }

    public ServiceBinding getServiceBinding() throws JAXRException {
        ServiceBinding binding = null;

        if (bindingRef != null) {
            binding = (ServiceBinding) bindingRef.getRegistryObject(
                    "ServiceBinding");
        } else {
            if (!isNew()) {
                //Do a query to get the parent ServiceBinding from server
                //???
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unfinished.code"));
            }
        }

        return binding;
    }

    /**
     * Internal method to set the sourceObject
     */
    void setServiceBindingInternal(ServiceBinding binding)
        throws JAXRException {
        if (binding == null) {
            bindingRef = null;
        } else {
            bindingRef = new RegistryObjectRef(lcm, binding);
        }
        setModified(true);
    }

    /**
     * Not to be used by clients. Expected to be used only by ServiceBindingImpl.
     */
    void setServiceBinding(ServiceBinding _binding) throws JAXRException {
        RegistryObjectRef serviceBindingRef = getServiceBindingRef();

        if ((_binding == null && bindingRef != null) || ((serviceBindingRef == null) || (!(_binding.getKey().getId().equals(serviceBindingRef.getId()))))) {
            setServiceBindingInternal(_binding);
        }
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        SpecificationLinkType ebSpecificationLinkType = factory.createSpecificationLinkType(); 
		setBindingObject(ebSpecificationLinkType);
		
//		JAXBElement<SpecificationLinkType> ebSpecificationLink = factory.createSpecificationLink(ebSpecificationLinkType);
		
		return ebSpecificationLinkType;
    }

    protected void setBindingObject(SpecificationLinkType ebSpecificationLinkType)
        throws JAXRException {
        super.setBindingObject(ebSpecificationLinkType);

        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        //private ArrayList usageParams = new ArrayList();

        /*
         * Version 2.1 does not have serviceBinding attribute on SpecificationLink
        if (binding != null) {
            org.oasis.ebxml.registry.bindings.rim.ObjectRef ebServiceBindingRef =
            new org.oasis.ebxml.registry.bindings.rim.ObjectRef();
            ebServiceBindingRef.setId(binding.getKey().getId());
            ebSpecLink.setServiceBinding(ebServiceBindingRef);
        }
         */
        if (specificationObjectRef != null) {
            ebSpecificationLinkType.setSpecificationObject(specificationObjectRef.getId());
        }

        if (bindingRef != null) {
            ebSpecificationLinkType.setServiceBinding(bindingRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.servicebinding.id",new Object[] {getId()}));
        }

		/*
		 * Description
		 */
		InternationalStringType ebUsageDescriptionType = factory.createInternationalStringType(); 
		((InternationalStringImpl)getUsageDescription()).setBindingObject(ebUsageDescriptionType);
		
		ebSpecificationLinkType.setName(ebUsageDescriptionType);

        Iterator<String> iter = getUsageParameters().iterator();

        while (iter.hasNext()) {
            String usageParam = iter.next();
            ebSpecificationLinkType.getUsageParameter().add(usageParam);
        }
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<RegistryObjectRef> getRegistryObjectRefs() {
        HashSet<RegistryObjectRef> refs = new HashSet<RegistryObjectRef>();

        //refs.addAll(super.getRegistryObjectRefs());
        if (specificationObjectRef != null) {
            refs.add(specificationObjectRef);
        }

        return refs;
    }
    


}
