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
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.Slot;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;


/**
 * Implements JAXR API interface named ExtensibleObject.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class ExtensibleObjectImpl implements ExtensibleObject {
    // I18n support for all infomodel classes
    protected static final JAXRResourceBundle i18nUtil = JAXRResourceBundle.getInstance();
    private TreeMap<String, Slot> slots = new TreeMap<String, Slot>();
    protected LifeCycleManagerImpl lcm = null;

    //State variable
    private boolean _new = false; //Object is unsaved and has never been committed to registry
    private boolean modified = false; //Object has been modified in memory and not yet committed to registry
    private boolean loaded = false; //Object has not been fully loaded from registry

    //Replace with functions to save memory later??
    protected DeclarativeQueryManagerImpl dqm = null;
    protected BusinessQueryManagerImpl bqm = null;
    
    protected BindingUtility bu = BindingUtility.getInstance();

    
    ExtensibleObjectImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        this.lcm = lcm;
        _new = true;
        
        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());
        
    }

    ExtensibleObjectImpl(LifeCycleManagerImpl lcm, IdentifiableType ebObject)
        throws JAXRException {
        this(lcm);
        _new = false;

        dqm = (DeclarativeQueryManagerImpl) (lcm.getRegistryService()
                                                .getDeclarativeQueryManager());
        bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                             .getBusinessQueryManager());
        
        List<SlotType1> ebSlots = ebObject.getSlot();
        Iterator<SlotType1> iter = ebSlots.iterator();

        while (iter.hasNext()) {
            SlotType1 slot = iter.next();
            internalAddSlot(new SlotImpl(lcm, slot));
        }
    }

    public BusinessQueryManager getBusinessQueryManager()
        throws JAXRException {
        return lcm.getRegistryService().getBusinessQueryManager();
    }

    public DeclarativeQueryManager getDeclarativeQueryManager()
        throws JAXRException {
        return lcm.getRegistryService().getDeclarativeQueryManager();
    }
    
    public LifeCycleManager getLifeCycleManager() throws JAXRException {
        return lcm;
    }    

    /**
     * Implementation private
     */
    public boolean isNew() {
        return _new;
    }

    /**
     * Implementation private
     */
    public void setNew(boolean _new) {
        this._new = _new;
    }

    /**
     * Implementation private
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    boolean isLoaded() {
        return loaded;
    }

    protected void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    private void internalAddSlot(Slot slot) throws JAXRException {
        if (slot != null) {
            String slotName = slot.getName();

            if (!slots.keySet().contains(slotName)) {
                // CHECK THIS! This will set modified to true!!!
                //Check if slot already been added
                //If already added then there could be a name conflict
                //So remove before re-adding
                if (!slots.entrySet().contains(slot)) {
                    removeSlot(slot.getName());
                }

                ((SlotImpl) slot).setParent(this);
                slots.put(slotName, slot);
            } else {
                Object[] objs = { slotName };
                throw new JAXRException(i18nUtil.getString(
                        "slot.name.duplicate", objs));
            }
        }
    }

    public void addSlot(Slot slot) throws JAXRException {
        internalAddSlot(slot);
        setModified(true);
    }

    public void addSlots(@SuppressWarnings("rawtypes") Collection _slots) throws JAXRException {
        //??Issue that if an error is encountered in adding slots
        //than Slots would have been added partially. 
        //Need to compensate for that in case of exception half way thru
        Iterator<?> iter = _slots.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();

            if (!(obj instanceof SlotImpl)) {
                throw new InvalidRequestException(i18nUtil.getString("message.error.expected.slot",new Object[] {obj}));
            }

            SlotImpl slot = (SlotImpl) obj;
            addSlot(slot);
        }
    }

    //??Add to JAXR 2.0
    public void removeSlot(Slot slot) throws JAXRException {
        String slotName = ((SlotImpl)slot).getName();
        removeSlot(slotName);
    }
    
    public void removeSlot(String slotName) throws JAXRException {
        Object removed = slots.remove(slotName);

        if (removed != null) {
            removeSlotInternal((SlotImpl) removed);
        }
    }

    private void removeSlotInternal(SlotImpl slot) throws JAXRException {
        slot.setParent(null);
        setModified(true);
    }

    public void removeSlots(@SuppressWarnings("rawtypes") Collection slotNames) throws JAXRException {
        Iterator<?> iter = slotNames.iterator();

        while (iter.hasNext()) {
            String slotName = (String) iter.next();
            removeSlot(slotName);
        }
    }

    //??Add to JAXR 2.0
    public void removeAllSlots() throws JAXRException {
        @SuppressWarnings("unchecked")
		TreeMap<String, Slot> _slots = (TreeMap<String, Slot>) slots.clone();
        Iterator<Slot> iter = _slots.values().iterator();

        while (iter.hasNext()) {
            SlotImpl slot = (SlotImpl) iter.next();

            //Must avoid case where a name change in SLot could prevent
            //normal removeSlot to not work.
            //String slotName = slot.getName();
            //removeSlot(slotName);            
            removeSlotInternal(slot);
        }

        slots = new TreeMap<String, Slot>();
    }

    public Slot getSlot(String slotName) throws JAXRException {
        return slots.get(slotName);
    }

    @SuppressWarnings("unchecked")
	public Collection<Slot> getSlots() throws JAXRException {
        return ((TreeMap<String, Slot>) (slots.clone())).values();
    }

    //??Add to JAXR 2.0
    public void setSlots(Collection<?> slots) throws JAXRException {
        removeAllSlots();
        addSlots(slots);
    }

    protected void setBindingObject(IdentifiableType ebObject)
        throws JAXRException {
        Iterator<Slot> iter = getSlots().iterator();

        ObjectFactory factory = BindingUtility.getInstance().rimFac;
        while (iter.hasNext()) {
            SlotImpl slot = (SlotImpl) iter.next();

			SlotType1 ebSlotType = factory.createSlotType1();
			slot.setBindingObject(ebSlotType);
			ebObject.getSlot().add(ebSlotType);
        }
    }

    public HashSet<Slot> getRIMComposedObjects()
        throws JAXRException {
        HashSet<Slot> composedObjects = new HashSet<Slot>();
        composedObjects.addAll(slots.values());
        
        return composedObjects;
    }
    
    public HashSet<Slot> getComposedObjects() 
        throws JAXRException {
        HashSet<Slot> composedObjects = new HashSet<Slot>();
        composedObjects.addAll(slots.values());
        
        return composedObjects;
    }
}
