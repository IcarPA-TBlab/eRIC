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

import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;

import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;

/**
 * Implements JAXR API interface named Association.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class AssociationImpl extends RegistryObjectImpl implements Association {
    private static ClassificationScheme assocTypeScheme = null;
    private RegistryObjectRef sourceObjectRef = null;
    private RegistryObjectRef targetObjectRef = null;
    private RegistryObjectRef assocTypeRef = null;

    public AssociationImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public AssociationImpl(LifeCycleManagerImpl lcm, AssociationType1 ebAss)
        throws JAXRException {
        super(lcm, ebAss);

        @SuppressWarnings("unused")
		DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl) lcm.getRegistryService()
                                                                           .getDeclarativeQueryManager();

        if (assocTypeScheme == null) {
            assocTypeScheme = (ClassificationScheme) bqm.getRegistryObject(BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType,
                    LifeCycleManager.CLASSIFICATION_SCHEME);
        }

        assocTypeRef = new RegistryObjectRef(lcm, ebAss.getAssociationType());

        sourceObjectRef = new RegistryObjectRef(lcm, ebAss.getSourceObject());
        targetObjectRef = new RegistryObjectRef(lcm, ebAss.getTargetObject());
    }

    //??JAXR 2.0
    public RegistryObjectRef getSourceObjectRef() throws JAXRException {
        return sourceObjectRef;
    }

    public RegistryObject getSourceObject() throws JAXRException {
        RegistryObject sourceObject = null;

        if (sourceObjectRef != null) {
            sourceObject = sourceObjectRef.getRegistryObject("RegistryObject");
        }

        return sourceObject;
    }

    /**
     * Internal method to set the sourceObject
     * TODO: Current design is messy. Need to figure out
     * clean way to set sourceObject only if different,
     * add association to RegistryObject only if not already added
     * and remove from previous sourceObject if any.
     */
    void setSourceObjectInternal(RegistryObject sourceObject)
        throws JAXRException {        
        setSourceObjectRef( new RegistryObjectRef(lcm, sourceObject));
    }

    public void setSourceObject(RegistryObject sourceObject)
        throws JAXRException {
        sourceObject.addAssociation(this);
    }

    //??JAXR 2.0
    public void setSourceObjectRef(RegistryObjectRef sourceObjectRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.sourceObjectRef == null) || (!(this.sourceObjectRef.getId().equals(sourceObjectRef.getId())))) {
            this.sourceObjectRef = sourceObjectRef;
            setModified(true);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getTargetObjectRef() throws JAXRException {
        return targetObjectRef;
    }

    public RegistryObject getTargetObject() throws JAXRException {
        RegistryObject targetObject = null;

        if (targetObjectRef != null) {
            targetObject = targetObjectRef.getRegistryObject("RegistryObject");
        }

        return targetObject;
    }

    public void setTargetObject(RegistryObject targetObject)
        throws JAXRException {
        targetObjectRef = new RegistryObjectRef(lcm, targetObject);
        setModified(true);
    }

    //??JAXR 2.0
    public void setTargetObjectRef(RegistryObjectRef targetObjectRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.targetObjectRef == null) || (!(this.targetObjectRef.getId().equals(targetObjectRef.getId())))) {
            this.targetObjectRef = targetObjectRef;
            setModified(true);
        }
    }

    public Concept getAssociationType() throws JAXRException {
        Concept assocType = null;

        if (assocTypeRef != null) {
            assocType = (Concept)assocTypeRef.getRegistryObject("ClassificationNode");
        }
        return assocType;
    }
    
    //??JAXR 2.0
    public RegistryObjectRef getAssociationTypeRef() throws JAXRException {
        return assocTypeRef;
    }    

    public void setAssociationType(Concept par1) throws JAXRException {
        if (par1 == null) {
            assocTypeRef = null;
        }
        else {
            assocTypeRef = new RegistryObjectRef(lcm, par1);
        }
        setModified(true);
    }

    //??JAXR 2.0
    public void setAssociationTypeRef(RegistryObjectRef assocTypeRef)
        throws JAXRException {
            
        //Only set if different
        if ((this.assocTypeRef == null) || (!(this.assocTypeRef.getId().equals(assocTypeRef.getId())))) {
            this.assocTypeRef = assocTypeRef;
            setModified(true);
        }
    }

    public boolean isExtramural() throws JAXRException {
        // To check if Association is Extramural requires us to determine
        // owners of the three involved objects: source, target, Association.

        RegistryObjectImpl srcObject = ((RegistryObjectImpl) getSourceObject());
        RegistryObjectImpl targetObject = ((RegistryObjectImpl) getTargetObject());

        if (srcObject == null) {
            return false;
        }

        if (targetObject == null) {
            return false;
        }

        UserImpl assOwner = (UserImpl) getOwner();

        if (assOwner == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.owner.association"));
        }

        UserImpl sourceOwner = (UserImpl) (srcObject.getOwner());
        UserImpl targetOwner = (UserImpl) (targetObject.getOwner());

        return !(assOwner.getId().equals(targetOwner.getId()) &&
                assOwner.getId().equals(sourceOwner.getId()));
        /*
        if (assOwner.getId().equals(targetOwner.getId()) &&
                assOwner.getId().equals(sourceOwner.getId())) {
            return false;
        } else {
            return true;
        }
        */
    }

    /**
     * Determines if the Association has been confirmed by the owner of the sourceObject.
     *
     * Note that ebXML Registry 3.0 no longer support Association confirmation after
     * realizing that it is a bogus idea and instead using 3.0 access control mechanisms
     * to control who is allowed to create an Association with one's objects
     * and under what constrainst. The custom access control policies can do much more than
     * what association confirmation allowed us to do in the past.
     * 
     * However, freebXML Registry supports association confirmation in order to pass JAXR TCK
     * as a implementation specific feature. The implementation uses Slots to store the
     * confirmation status.
     *
     */
    public boolean isConfirmedBySourceOwner() throws JAXRException {
        boolean confirmedBySourceOwner = false;
        
        if (!isExtramural()) {
            confirmedBySourceOwner = true;
        } else {        
            Collection<?> slots = this.getSlots();
            Iterator<?> iter = slots.iterator();
            while (iter.hasNext()) {
                Slot slot = (Slot)iter.next();
                String slotName = slot.getName();
                if (slotName.equals(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER)) {
                    Collection<?> values = slot.getValues();
                    if (values.size() > 0) {
                        String value = (String)((values.toArray())[0]);
                        confirmedBySourceOwner = Boolean.valueOf(value).booleanValue();
                    }
                    break;
                }
            }
        }
        
        return confirmedBySourceOwner;
    }

    /**
     * Determines if the Association has been confirmed by the owner of the targetObject.
     *
     * Note that ebXML Registry 3.0 no longer support Association confirmation after
     * realizing that it is a bogus idea and instead using 3.0 access control mechanisms
     * to control who is allowed to create an Association with one's objects
     * and under what constrainst. The custom access control policies can do much more than
     * what association confirmation allowed us to do in the past.
     * 
     * However, freebXML Registry supports association confirmation in order to pass JAXR TCK
     * as a implementation specific feature. The implementation uses Slots to store the
     * confirmation status.
     *
     */
    public boolean isConfirmedByTargetOwner() throws JAXRException {
        boolean confirmedByTargetOwner = false;
        
        if (!isExtramural()) {
            confirmedByTargetOwner = true;
        } else {        
            Collection<?> slots = this.getSlots();
            Iterator<?> iter = slots.iterator();
            while (iter.hasNext()) {
                Slot slot = (Slot)iter.next();
                String slotName = slot.getName();
                if (slotName.equals(BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER)) {
                    Collection<?> values = slot.getValues();
                    if (values.size() > 0) {
                        String value = (String)((values.toArray())[0]);
                        confirmedByTargetOwner = Boolean.valueOf(value).booleanValue();
                    }
                    break;
                }
            }
        }
        
        return confirmedByTargetOwner;
    }

    public boolean isConfirmed() throws JAXRException {
        boolean confirmed = false;

        if (isConfirmedBySourceOwner() && isConfirmedByTargetOwner()) {
            confirmed = true;
        }

        return confirmed;
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
		
		AssociationType1 ebAssociationType = factory.createAssociationType1();
		setBindingObject(ebAssociationType);

//		JAXBElement<AssociationType1> ebAssociation = factory.createAssociation(ebAssociationType);
		
		return ebAssociationType;
    }

    protected void setBindingObject(AssociationType1 ebAssociationType)
        throws JAXRException {
        super.setBindingObject(ebAssociationType);
        
		ObjectFactory factory = BindingUtility.getInstance().rimFac;

        if (assocTypeRef != null) {
            ebAssociationType.setAssociationType(assocTypeRef.getId());
        }

        ObjectRefType ebSourceObjectRefType = factory.createObjectRefType();
        ebSourceObjectRefType.setId(sourceObjectRef.getId());
        ebAssociationType.setSourceObject(sourceObjectRef.getId());

        ObjectRefType ebTargetObjectRefType = factory.createObjectRefType();
        ebTargetObjectRefType.setId(targetObjectRef.getId());
        ebAssociationType.setTargetObject(targetObjectRef.getId());
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<RegistryObjectRef> getRegistryObjectRefs() {
        HashSet<RegistryObjectRef> refs = new HashSet<RegistryObjectRef>();

        //refs.addAll(super.getRegistryObjectRefs());
        if (sourceObjectRef != null) {
            refs.add(sourceObjectRef);
        }

        if (targetObjectRef != null) {
            refs.add(targetObjectRef);
        }

        if (assocTypeRef != null) {
            refs.add(assocTypeRef);
        }

        return refs;
    }
    
    public String toString() {
        String str = super.toString();

        str = "sourceObject:" + sourceObjectRef + " targetObject:" + targetObjectRef + " " + str;

        return str;
    }
}
