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
import it.cnr.icar.eric.common.exceptions.MissingParentReferenceException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;


/**
 * Implements JAXR API interface named ServiceBinding.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ServiceBindingImpl extends RegistryObjectImpl
    implements ServiceBinding {
    private RegistryObjectRef serviceRef = null;
    private String accessURI = null;
    private RegistryObjectRef targetBindingRef = null;
    private HashSet<SpecificationLink> specLinks = new HashSet<SpecificationLink>();
    private boolean validateURI = true;

    public ServiceBindingImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ServiceBindingImpl(LifeCycleManagerImpl lcm,
        ServiceBindingType ebBinding) throws JAXRException {
        super(lcm, ebBinding);

        serviceRef = new RegistryObjectRef(lcm, ebBinding.getService());

        accessURI = ebBinding.getAccessURI();

        Object targetBindingObj = ebBinding.getTargetBinding();

        if (targetBindingObj != null) {
            targetBindingRef = new RegistryObjectRef(lcm, targetBindingObj);
        }

        Iterator<SpecificationLinkType> ebSpecLinks = ebBinding.getSpecificationLink().iterator();

        while (ebSpecLinks.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType ebSpecLink =
                ebSpecLinks.next();
            addSpecificationLink(new SpecificationLinkImpl(lcm, ebSpecLink));
        }
    }

    public String getAccessURI() throws JAXRException {
        return accessURI;
    }

    public void setAccessURI(String uri) throws JAXRException {
        if ((uri != null) && (targetBindingRef != null) && (uri.length() > 0 )) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.targetbinding.notnull.error"));
        }
        validateURI(uri);
        accessURI = uri;
        setModified(true);
    }

    //??JAXR 2.0
    public RegistryObjectRef getTargetBindingRef() throws JAXRException {
        return targetBindingRef;
    }

    public ServiceBinding getTargetBinding() throws JAXRException {
        ServiceBinding targetBinding = null;

        if (targetBindingRef != null) {
            targetBinding = (ServiceBinding) targetBindingRef.getRegistryObject(
                    "ServiceBinding");
        }

        return targetBinding;
    }

    /**
     * Internal method to set the targetBinding
     */
    void setTargetBindingInternal(ServiceBinding targetBinding)
        throws JAXRException {
        targetBindingRef = new RegistryObjectRef(lcm, targetBinding);
        setModified(true);
    }

    public void setTargetBinding(ServiceBinding _binding)
        throws JAXRException {
        if (_binding == null){
            targetBindingRef = null;
        }else{
        if ((_binding != null) && (accessURI != null) && (accessURI.length() > 0)) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.accessURI.notnull.error"));
        }
        if ((targetBindingRef == null) || (!(_binding.getKey().getId().equals(targetBindingRef.getId())))) {
            if (!(_binding instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl",new Object[] {_binding}));
            }

            setTargetBindingInternal(_binding);
        }
       }
    }

    //??JAXR 2.0
    public RegistryObjectRef getServiceRef() throws JAXRException {
        return serviceRef;
    }

    public Service getService() throws JAXRException {
        Service service = null;

        if (serviceRef != null) {
            service = (Service) serviceRef.getRegistryObject("Service");
        }

        return service;
    }

    /**
     * Internal method to set the service
     */
    void setServiceInternal(Service service) throws JAXRException {
        if (service != null) {
            serviceRef = new RegistryObjectRef(lcm, service);
        } else {
            serviceRef = null;
        }
        setModified(true);
    }

    /**
     * Not to be used by clients. Expected to be used only by ServiceImpl.
     */
    void setService(Service service) throws JAXRException {
        if (serviceRef != null) {
            if (service != null) {
                if (!(service.getKey().getId().equals(serviceRef.getId()))) {
                    if (!(service instanceof ServiceImpl)) {
                        throw new InvalidRequestException(
                            JAXRResourceBundle.getInstance().getString("message.error.expected.ServiceImpl",new Object[] {service}));
                    }
                }
            }
        }

        setServiceInternal(service);
    }

    public void addSpecificationLink(SpecificationLink specLink)
        throws JAXRException {
        specLinks.add(specLink);
        ((SpecificationLinkImpl) specLink).setServiceBinding(this);

        //In version 2.1 there is no parent attribute in SpecificationLink (fixed in 2.5).
        //This means that a SpecificationLink must be saved within a ServiceBinding.
        //Therefor we must force the ServiceBinding to be saved. 
        setModified(true);
    }

    public void addSpecificationLinks(@SuppressWarnings("rawtypes") Collection _specLinks)
        throws JAXRException {
        Iterator<?> iter = _specLinks.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();

            if (!(obj instanceof SpecificationLinkImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.SpecificationLinkImpl",new Object[] {obj}));
            }

            SpecificationLinkImpl specLink = (SpecificationLinkImpl) obj;
            addSpecificationLink(specLink);
        }
    }

    public void removeSpecificationLink(SpecificationLink specLink)
        throws JAXRException {
        specLinks.remove(specLink);
        ((SpecificationLinkImpl) specLink).setServiceBinding(null);

        //In version 2.1 there is no parent attribute in SpecificationLink (fixed in 2.5).
        //This means that a SpecificationLink must be saved within a ServiceBinding.
        //Therefor we must force the ServiceBinding to be saved. 
        setModified(true);
    }

    public void removeSpecificationLinks(@SuppressWarnings("rawtypes") Collection _specLinks)
        throws JAXRException {
        Iterator<?> iter = _specLinks.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();

            if (!(obj instanceof SpecificationLinkImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.SpecificationLinkImpl",new Object[] {obj}));
            }

            SpecificationLinkImpl specLink = (SpecificationLinkImpl) obj;
            removeSpecificationLink(specLink);
        }
    }

    @SuppressWarnings("unchecked")
	public Collection<SpecificationLink> getSpecificationLinks() throws JAXRException {
        return (Collection<SpecificationLink>) (specLinks.clone());
    }

    public boolean getValidateURI() throws JAXRException {
        return validateURI;
    }     

    public void setValidateURI(boolean validateURI) throws JAXRException {
        this.validateURI = validateURI;
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

        ServiceBindingType ebServiceBindingType = factory.createServiceBindingType(); 
		setBindingObject(ebServiceBindingType);
		
//		JAXBElement<ServiceBindingType> ebServiceBinding = factory.createServiceBinding(ebServiceBindingType);
		
		return ebServiceBindingType;
    }

    protected void setBindingObject(ServiceBindingType ebServiceBindingType)
        throws JAXRException {
        super.setBindingObject(ebServiceBindingType);

        if (accessURI != null) {
            ebServiceBindingType.setAccessURI(accessURI);
        }

        if (targetBindingRef != null) {
            ebServiceBindingType.setTargetBinding(targetBindingRef.getId());
        }

        if (serviceRef != null) {
            ebServiceBindingType.setService(serviceRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.service.id", new Object[] {getId()}));
        }

        Iterator<SpecificationLink> iter = getSpecificationLinks().iterator();

        while (iter.hasNext()) {
            SpecificationLinkImpl specLink = (SpecificationLinkImpl) iter.next();
            SpecificationLinkType ebSpecificationLinkType = (SpecificationLinkType) specLink.toBindingObject();
            ebServiceBindingType.getSpecificationLink().add(ebSpecificationLinkType);
        }
    }

    public HashSet<?> getRIMComposedObjects()
        throws JAXRException {
        return getComposedObjects();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        
        Collection<SpecificationLink> specLinks = getSpecificationLinks();
        composedObjects.addAll(specLinks);
        
        return composedObjects;
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<RegistryObjectRef> getRegistryObjectRefs() {
        HashSet<RegistryObjectRef> refs = new HashSet<RegistryObjectRef>();

        //refs.addAll(super.getRegistryObjectRefs());
        if (targetBindingRef != null) {
            refs.add(targetBindingRef);
        }

        if (serviceRef != null) {
            refs.add(serviceRef);
        }

        return refs;
    }
    
    private void validateURI(String uri) throws InvalidRequestException {        
        
        if (validateURI) {
            // check the http url
            boolean isValid = it.cnr.icar.eric.common.Utility.getInstance().isValidURI(uri);

            if (!isValid) {
                throw new InvalidRequestException(" "+JAXRResourceBundle.getInstance().getString("message.error.url.not.resolvable",new Object[] {uri}));
            }
        }
    }
    
}
