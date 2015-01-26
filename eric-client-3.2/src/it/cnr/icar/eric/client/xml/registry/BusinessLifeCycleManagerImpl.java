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

import it.cnr.icar.eric.client.xml.registry.infomodel.AssociationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnexpectedObjectException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;


/**
 * Implements JAXR API interface named BusinessLifeCycleManager.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class BusinessLifeCycleManagerImpl extends LifeCycleManagerImpl
    implements BusinessLifeCycleManager {
    BusinessLifeCycleManagerImpl(RegistryServiceImpl service) {
        super(service);
    }

    /**
     * Saves specified Organizations.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     *
     */
    public BulkResponse saveOrganizations(@SuppressWarnings("rawtypes") Collection organizations)
        throws JAXRException {
        Iterator<?> iter = organizations.iterator();

        while (iter.hasNext()) {
            Object org = iter.next();

            if (!(org instanceof Organization)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.organization",new Object[] {org.getClass().getName()}));
            }
        }

        return saveObjects(organizations);
    }

    /**
     * Saves specified Services.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServices(@SuppressWarnings("rawtypes") Collection services)
        throws JAXRException {
        Iterator<?> iter = services.iterator();

        while (iter.hasNext()) {
            Object service = iter.next();

            if (!(service instanceof Service)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.service",new Object[] {service.getClass().getName()}));
            }
        }

        return saveObjects(services);
    }

    /**
     * Saves specified ServiceBindings.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServiceBindings(@SuppressWarnings("rawtypes") Collection bindings)
        throws JAXRException {
        Iterator<?> iter = bindings.iterator();

        while (iter.hasNext()) {
            Object binding = iter.next();

            if (!(binding instanceof ServiceBinding)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.servicebinding",new Object[] {binding.getClass().getName()}));
            }
        }

        return saveObjects(bindings);
    }

    /**
     * Saves specified Concepts.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveConcepts(@SuppressWarnings("rawtypes") Collection concepts)
        throws JAXRException {
        Iterator<?> iter = concepts.iterator();

        while (iter.hasNext()) {
            Object concept = iter.next();

            if (!(concept instanceof Concept)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.concept",new Object[] {concept.getClass().getName()}));
            }
        }

        return saveObjects(concepts);
    }

    /**
     * Saves specified ClassificationScheme instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveClassificationSchemes(@SuppressWarnings("rawtypes") Collection schemes)
        throws JAXRException {
        Iterator<?> iter = schemes.iterator();

        while (iter.hasNext()) {
            Object scheme = iter.next();

            if (!(scheme instanceof ClassificationScheme)) {
                throw new UnexpectedObjectException(
                    JAXRResourceBundle.getInstance().getString("message.error.expecting.classScheme",new Object[] {scheme.getClass().getName()}));
            }
        }

        return saveObjects(schemes);
    }

    /**
     * Saves specified Association instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param replace If set to true then the specified associations replace any existing associations owned by the caller. If set to false specified associations are saved while preserving any existing associations that are not being updated by this call.
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveAssociations(@SuppressWarnings("rawtypes") Collection associations,
        boolean replace) throws JAXRException {
        Iterator<?> iter = associations.iterator();

        while (iter.hasNext()) {
            Object association = iter.next();

            if (!(association instanceof Association)) {
                throw new UnexpectedObjectException(
                     JAXRResourceBundle.getInstance().getString("message.error.expecting.association",new Object[] {association.getClass().getName()}));
            }
        }

        //??What to do with replace
        return saveObjects(associations);
    }

    /**
     * Deletes the organizations corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteOrganizations(@SuppressWarnings("rawtypes") Collection organizationKeys)
        throws JAXRException {
        return deleteObjects(organizationKeys);
    }

    /**
     * Delete the services corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServices(@SuppressWarnings("rawtypes") Collection serviceKeys)
        throws JAXRException {
        return deleteObjects(serviceKeys);
    }

    /**
     * Delete the ServiceBindings corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServiceBindings(@SuppressWarnings("rawtypes") Collection bindingKeys)
        throws JAXRException {
        return deleteObjects(bindingKeys);
    }

    /**
     * Delete the Concepts corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteConcepts(@SuppressWarnings("rawtypes") Collection conceptKeys)
        throws JAXRException {
        return deleteObjects(conceptKeys);
    }

    /**
     * Delete the ClassificationSchemes corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteClassificationSchemes(@SuppressWarnings("rawtypes") Collection schemeKeys)
        throws JAXRException {
        return deleteObjects(schemeKeys);
    }

    /**
     * Delete the Associations corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first
     * DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those
     * objects that were deleted successfully and any DeleteException that
     * was encountered in case of partial commit.
     */
    public BulkResponse deleteAssociations(@SuppressWarnings("rawtypes") Collection assKeys)
        throws JAXRException {
        return deleteObjects(assKeys);
    }

    public void confirmAssociation(Association association)
        throws JAXRException, InvalidRequestException {
        if (!association.isExtramural()) {
            return;
        }

        if (association.isConfirmed()) {
            return;
        }

        RegistryObject src = association.getSourceObject();
        RegistryObject target = association.getTargetObject();
        User srcOwner = ((RegistryObjectImpl)src).getOwner();
        User targetOwner = ((RegistryObjectImpl)target).getOwner();
        
        // Confirm the Association by saving the same object again
        AssociationImpl assImpl = (AssociationImpl) association;
        User caller = ((BusinessQueryManagerImpl)(getRegistryService().getBusinessQueryManager())).getCallersUser();

        ArrayList<String> slotValue = new ArrayList<String>();
        slotValue.add("true");
        slotValue.add(caller.getKey().getId()); //Also remember id of user doing confirmation

        if (caller.equals(srcOwner)) {
            //Just in case it is already there, remove Slot before adding
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER);
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
            
            //Add a special Slot as hint that this is a confirmation
            Slot slot1 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED,
                "true", null);
            assImpl.addSlot(slot1);
            Slot slot2 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER,
                slotValue, null);
            assImpl.addSlot(slot2);
            
        } else if (caller.equals(targetOwner)) {
            //Just in case it is already there, remove Slot before adding
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER);
            assImpl.removeSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
            
            //Add a special Slot as hint that this is a confirmation
            Slot slot1 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED,
                "true", null);
            assImpl.addSlot(slot1);
            Slot slot2 = this.createSlot(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER,
                slotValue, null);
            assImpl.addSlot(slot2);
            
        } else {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.confirm.association.exception"));
        }
    }

    public void unConfirmAssociation(Association association)
        throws JAXRException, InvalidRequestException {
        //??
        // Send a removeObjectsReq on ass similar to confirm and submitObjects
        // me = getMyUser
        // if (src owner is me) then unconfirm src
        // else if (target owner is me then unconfirm target
    }
}
