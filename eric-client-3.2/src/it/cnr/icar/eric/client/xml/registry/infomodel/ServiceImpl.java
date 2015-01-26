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
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;


/**
 * Implements JAXR API interface named Service.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ServiceImpl extends RegistryEntryImpl implements Service {
    private static final String PROVIDING_ORGANIZATION_SLOT_NAME = "providingOrganization";
    private HashSet<ServiceBinding> bindings = new HashSet<ServiceBinding>();
    private RegistryObjectRef providingOrgRef = null;

    public ServiceImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public ServiceImpl(LifeCycleManagerImpl lcm, ServiceType ebService)
        throws JAXRException {
        super(lcm, ebService);

        Iterator<ServiceBindingType> ebBindings = ebService.getServiceBinding().iterator();

        while (ebBindings.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.ServiceBindingType ebBinding = ebBindings.next();
            addServiceBinding(new ServiceBindingImpl(lcm, ebBinding));
        }
    }

    public void addServiceBinding(ServiceBinding binding)
        throws JAXRException {
        bindings.add(binding);
        ((ServiceBindingImpl) binding).setService(this);
    }

    public void addServiceBindings(@SuppressWarnings("rawtypes") Collection _bindings)
        throws JAXRException {
        Iterator<?> iter = _bindings.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();

            if (!(obj instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl", new Object[] {obj}));
            }

            ServiceBindingImpl binding = (ServiceBindingImpl) obj;
            addServiceBinding(binding);
        }
    }

    public void removeServiceBinding(ServiceBinding binding)
        throws JAXRException {
        bindings.remove(binding);
        ((ServiceBindingImpl) binding).setService(null);
    }

    public void removeServiceBindings(@SuppressWarnings("rawtypes") Collection _bindings)
        throws JAXRException {
        Iterator<?> iter = _bindings.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();

            if (!(obj instanceof ServiceBindingImpl)) {
                throw new InvalidRequestException(
                    JAXRResourceBundle.getInstance().getString("message.error.expected.serviceBindingImpl", new Object[] {obj}));
            }

            ServiceBindingImpl binding = (ServiceBindingImpl) obj;
            removeServiceBinding(binding);
        }
    }

    public Collection<ServiceBinding> getServiceBindings() throws JAXRException {
        return bindings;
    }

    public void setProvidingOrganization(Organization org) throws JAXRException {
        Slot providingOrgSlot = getSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        
        if (providingOrgSlot != null) {
            removeSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        }
        
        String newOrgKey = null;
        
        if (org != null) {
            newOrgKey = org.getKey().getId();
            providingOrgSlot = lcm.createSlot(PROVIDING_ORGANIZATION_SLOT_NAME, newOrgKey, null);
            addSlot(providingOrgSlot);
        }
    }
        
    public Organization getProvidingOrganization()
        throws JAXRException {
        Organization org = null;
        
        Slot providingOrgSlot = getSlot(PROVIDING_ORGANIZATION_SLOT_NAME);
        
        if (providingOrgSlot != null) {
            Collection<?> providingOrgValues = providingOrgSlot.getValues();
            
            if (providingOrgValues.size() != 1) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.service.more.providing.organization"));
            }
            
            Iterator<?> providingOrgIter = providingOrgValues.iterator();
            providingOrgRef = new RegistryObjectRef(lcm, (String) providingOrgIter.next());
        } else {
            providingOrgRef = null;
        }

        if (providingOrgRef != null) {
            org = (Organization)providingOrgRef.getRegistryObject("Organization");
        }

        
        return org;
    }

    @SuppressWarnings("unused")
	private Association getProviderOfAssociation() throws JAXRException {
        String id = getKey().getId();
//	String queryStr =
//	    "SELECT ass.* FROM Association ass WHERE targetObject = '" + id +
//	    "' AND associationType ='" + BindingUtility.ASSOCIATION_TYPE_ID_ProviderOf + "'";
//	Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        
        Query query = SQLQueryProvider.getProviderOfAssociation(dqm, id);
        
	BulkResponse response = dqm.executeQuery(query);
	checkBulkResponseExceptions(response);
	Collection<?> associations = response.getCollection();
        
        Association assoc;
        switch (associations.size()) {
            case 0:
                assoc = null;
                break;
            case 1:
                Iterator<?> iter = associations.iterator();
                assoc = (Association)iter.next();
                break;
            default:
               throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.service.more.providing.organization"));
        }
        
        return assoc;
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

        ServiceType ebServiceType = factory.createServiceType(); 
		setBindingObject(ebServiceType);
		
//		JAXBElement<ServiceType> ebService = factory.createService(ebServiceType);
		
		return ebServiceType;
    }

    protected void setBindingObject(ServiceType ebServiceType)
        throws JAXRException {
        super.setBindingObject(ebServiceType);

        Iterator<ServiceBinding> iter = getServiceBindings().iterator();

        while (iter.hasNext()) {
            ServiceBindingImpl binding = (ServiceBindingImpl) iter.next();
            ServiceBindingType ebServiceBindingType = (ServiceBindingType) binding.toBindingObject();
            ebServiceType.getServiceBinding().add(ebServiceBindingType);
        }
    }

    @SuppressWarnings("rawtypes")
	public HashSet getRIMComposedObjects()
        throws JAXRException {
        return getComposedObjects();
    }
    
    @SuppressWarnings("unchecked")
	public HashSet<?> getComposedObjects()
        throws JAXRException {
        @SuppressWarnings("rawtypes")
		HashSet composedObjects = super.getComposedObjects();
        
        Collection<ServiceBinding> bindings = getServiceBindings();
        composedObjects.addAll(bindings);
        
        return composedObjects;
    }
}
