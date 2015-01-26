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


import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;


/**
 * Implements JAXR API interface named Slot.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class SlotImpl implements Slot {
    private String name = null;
    private String slotType = null;
    @SuppressWarnings("unused")
	private String value = null;
    private ArrayList<String> values = new ArrayList<String>();
    private ExtensibleObjectImpl parent = null;
    protected LifeCycleManagerImpl lcm = null;

    public SlotImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        this.lcm = lcm;
    }

    SlotImpl(LifeCycleManagerImpl lcm, SlotType1 ebSlot)
        throws JAXRException {
        this.lcm = lcm;

        name = ebSlot.getName();
        slotType = ebSlot.getSlotType();

        ValueListType valList = ebSlot.getValueList();

        if (valList != null) {
            Iterator<String> valListIt = valList.getValue().iterator();
            while (valListIt.hasNext()) {
                values.add(valListIt.next());
            }
        }
    }

    void setParent(ExtensibleObjectImpl _parent) throws InvalidRequestException {
        if ((_parent != null) && (parent != null) && (_parent != parent)) {
            throw new InvalidRequestException(
                JAXRResourceBundle.getInstance().getString("message.error.add.slot.object.already.added.object",new Object[] {_parent,parent}));
        }

        parent = _parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String par1) throws JAXRException {
        name = par1;

        if (parent != null) {
            parent.setModified(true);
        }
    }

    public String getSlotType() throws JAXRException {
        return slotType;
    }

    public void setSlotType(String par1) throws JAXRException {
        slotType = par1;

        if (parent != null) {
            parent.setModified(true);
        }
    }

    @SuppressWarnings("unchecked")
	public Collection<String> getValues() throws JAXRException {
        return (Collection<String>) (values.clone());
    }

    @SuppressWarnings("unchecked")
	public void setValues(@SuppressWarnings("rawtypes") Collection par1) throws JAXRException {
        values.clear();
        values.addAll(par1);

        if (parent != null) {
            parent.setModified(true);
        }
    }

    void setBindingObject(SlotType1 ebSlot)
        throws JAXRException {
        ebSlot.setName(name);
        ebSlot.setSlotType(slotType);

        ObjectFactory factory = BindingUtility.getInstance().rimFac;
        ValueListType ebValueListType = factory.createValueListType();
        Iterator<String> valIterator = values.iterator();
        
        while (valIterator.hasNext()) {
            String val = valIterator.next();
            ebValueListType.getValue().add(val);
        }
        ebSlot.setValueList(ebValueListType);
    }

    @SuppressWarnings("null")
	protected Object clone() throws CloneNotSupportedException {
        SlotImpl _clone = null;

        try {
            new SlotImpl(lcm);
            _clone.setName(getName());
            _clone.setSlotType(getSlotType());
            _clone.setValues(getValues());
            _clone.setParent(parent);
        } catch (JAXRException e) {
            //Cannot happen.
            e.printStackTrace();
        }

        return _clone;
    }
    
    public String toString() {
        String str = super.toString();

        try {
            str += " slotName:" + getName() + " values: " + getValues();
        } catch (JAXRException e) {
        }

        return str;
    }
    
}