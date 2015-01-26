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
package it.cnr.icar.eric.client.xml.registry.example;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Slot;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * An example calss for the JAXR infomodel class extension feature.
 * Defines a class for the CPP object type.
 *
 * @author Diego Ballve / Digital Artefacts Europe
 */
public class CPP extends ExtrinsicObjectImpl {
    
    public final static String SLOT_NAME_ROLE = "Role";
    
    /** Creates a new instance of CPP */
    public CPP(LifeCycleManagerImpl lcm)
    throws JAXRException {
        super(lcm);
    }

    /** Creates a new instance of CPP binding to an existing RegistryObject */
    public CPP(LifeCycleManagerImpl lcm, ExtrinsicObjectType eoType)
    throws JAXRException {
        super(lcm, eoType);
    }

    /**
     * Sets the value to property _role. Maps to a Slot defined by
     * 'SLOT_NAME_ROLE'.
     *
     * @param _role String value to be set.
     * @throws JAXRException if any exception occurs.
     */
    public void setRole(String _role) throws JAXRException {
        String name = SLOT_NAME_ROLE;
        Collection<String> values = new ArrayList<String>();
        values.add(_role);
        Slot slot = getSlot(name);
        if (slot == null) {
            slot = getLifeCycleManager().createSlot(name, values, "String");
        } else {
            removeSlot(name);
            slot.setValues(values);
        }
        addSlot(slot);
    }

    /** Gets the value of property _namespace. Maps to a Slot named
    * 'urn:freebxml:slot:xml-schema:namespace'.
    *
    * @return String value of property _namespace.
    * @throws JAXRException if any exception occurs.
    */
    public String getRole() throws JAXRException {
        String name = SLOT_NAME_ROLE;
        Slot slot = getSlot(name);
        if (slot == null) {
            return null;
        } else {
            return (String)slot.getValues().iterator().next();
        }
    }
}