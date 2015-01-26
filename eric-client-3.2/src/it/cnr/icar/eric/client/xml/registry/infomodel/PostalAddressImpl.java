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
import it.cnr.icar.eric.common.BindingUtility;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.Slot;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;


/**
 * Implements JAXR API interface named PostalAddress.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class PostalAddressImpl extends ExtensibleObjectImpl
    implements PostalAddress {
    private String street = null;
    private String streetNumber = null;
    private String city = null;
    private String stateOrProvince = null;
    private String postalCode = null;
    private String country = null;
    private String type = null;
    private ClassificationScheme postalScheme = null;

    private PostalAddressImpl() throws JAXRException {
        super(null);
    }

    public PostalAddressImpl(LifeCycleManagerImpl lcm)  throws JAXRException {
        super(lcm);
    }

    public PostalAddressImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.PostalAddressType address)  throws JAXRException {
        super(lcm);

        // Todo: Pass ExtensibleObject components to super class???
        if (address == null) {
            return;
        }

        city = address.getCity();
        country = address.getCountry();
        postalCode = address.getPostalCode();
        stateOrProvince = address.getStateOrProvince();
        street = address.getStreet();
        streetNumber = address.getStreetNumber();
    }

    public String getStreet() throws JAXRException {
        if (street == null) {
            street = "";
        }

        return street;
    }

    public void setStreet(String par1) throws JAXRException {
        street = par1;
    }

    public String getStreetNumber() throws JAXRException {
        if (streetNumber == null) {
            streetNumber = "";
        }

        return streetNumber;
    }

    public void setStreetNumber(String par1) throws JAXRException {
        streetNumber = par1;
    }

    public String getCity() throws JAXRException {
        if (city == null) {
            city = "";
        }

        return city;
    }

    public void setCity(String par1) throws JAXRException {
        city = par1;
    }

    public String getStateOrProvince() throws JAXRException {
        if (stateOrProvince == null) {
            stateOrProvince = "";
        }

        return stateOrProvince;
    }

    public void setStateOrProvince(String par1) throws JAXRException {
        stateOrProvince = par1;
    }

    public String getPostalCode() throws JAXRException {
        if (postalCode == null) {
            postalCode = "";
        }

        return postalCode;
    }

    public void setPostalCode(String par1) throws JAXRException {
        postalCode = par1;
    }

    public String getCountry() throws JAXRException {
        if (country == null) {
            country = "";
        }

        return country;
    }

    public void setCountry(String par1) throws JAXRException {
        country = par1;
    }

    public String getType() throws JAXRException {
        if (type == null) {
            type = "";
        }

        return type;
    }

    public void setType(String par1) throws JAXRException {
        type = par1;
    }

    public void setPostalScheme(ClassificationScheme par1)
        throws JAXRException {
        postalScheme = par1;
    }

    public ClassificationScheme getPostalScheme() throws JAXRException {
        return postalScheme;
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

		PostalAddressType ebPostalAddressType = factory.createPostalAddressType(); 
		setBindingObject(ebPostalAddressType);
		
//		JAXBElement<PostalAddressType> ebPostalAddress = factory.createPostalAddress(ebPostalAddressType);
		
		return ebPostalAddressType;
    }

    void setBindingObject(PostalAddressType ebPostalAddressType)
        throws JAXRException {
//        super.setBindingObject(ebPostalAddressType);

        ebPostalAddressType.setStreet(street);
        ebPostalAddressType.setStreetNumber(streetNumber);
        ebPostalAddressType.setCity(city);
        ebPostalAddressType.setStateOrProvince(stateOrProvince);
        ebPostalAddressType.setPostalCode(postalCode);
        ebPostalAddressType.setCountry(country);

    }

    public String toString() {
        String addrStr = "";

        try {
            addrStr = getStreetNumber() + " " + getStreet() + ", " + getCity() +
                " " + getStateOrProvince() + " " + getPostalCode() + ", " +
                getCountry();

            Collection<Slot> slots = getSlots();

            Iterator<Slot> slotsIter = slots.iterator();

            while (slotsIter.hasNext()) {
                Slot slot = slotsIter.next();
                Collection<?> values = slot.getValues();

                Iterator<?> valuesIter = values.iterator();

                while (valuesIter.hasNext()) {
                    String value = (String) valuesIter.next();
                    addrStr += (" " + value);
                }

                if (slotsIter.hasNext()) {
                    addrStr += ",";
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
            addrStr = e.toString();
        }

        return addrStr;
    }
}
