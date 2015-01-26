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
package it.cnr.icar.eric.client.common.userModel;


import it.cnr.icar.eric.client.common.RegistryMappedModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class PostalAddressModel extends RegistryMappedModel {
    public static final String[] ADDRESS_TYPES = {
            "Home", "Office", "Vacation", "Temporary"
        };
    private final User user;
    private final Map<String, PostalAddress> addresses = new HashMap<String, PostalAddress>();

    PostalAddressModel(User u) {
        super(ADDRESS_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setStreetNum.
     * @param addressType
     * @param text
     */
    public void setStreetNum(String streetNum) throws JAXRException {
        streetNum = streetNum.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setStreetNumber(streetNum);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress(streetNum,
                    "", "", "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setStreet.
     * @param addressType
     * @param text
     */
    public void setStreet(String street) throws JAXRException {
        street = street.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setStreet(street);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("",
                    street, "", "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setCity.
     * @param addressType
     * @param text
     */
    public void setCity(String city) throws JAXRException {
        city = city.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setCity(city);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    city, "", "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setState.
     * @param addressType
     * @param text
     */
    public void setState(String state) throws JAXRException {
        state = state.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setStateOrProvince(state);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", state, "", "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setPostalCode.
     * @param addressType
     * @param text
     */
    public void setPostalCode(String code) throws JAXRException {
        code = code.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setPostalCode(code);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", "", "", code, this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    /**
     * Method setCountry.
     * @param addressType
     * @param text
     */
    public void setCountry(String country) throws JAXRException {
        country = country.trim();

        if (this.addresses.containsKey(this.key)) {
            // Address already exists, just need to modify entry.
            PostalAddress postalAddress = this.addresses.get(this.key);
            postalAddress.setCountry(country);
        } else {
            // Create new address and add to user.
            PostalAddress postalAddress = this.user.getLifeCycleManager()
                                                   .createPostalAddress("", "",
                    "", "", country, "", this.key);
            this.addresses.put(this.key, postalAddress);
            this.user.setPostalAddresses(this.addresses.values());
        }
    }

    public PostalAddress getAddress() {
        PostalAddress address = this.addresses.get(this.key);

        return address;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection<PostalAddress> addressSet = this.addresses.values();
        PostalAddress address = null;
        Iterator<PostalAddress> i = addressSet.iterator();

        while (i.hasNext()) {
            address = i.next();

            String city = address.getCity();
            String country = address.getCountry();
            String code = address.getPostalCode();
            String state = address.getStateOrProvince();
            String street = address.getStreet();
            String number = address.getStreetNumber();

            // Takes all fields except the address type into account.
            if (((city == null) || (city.length() == 0)) &&
                    ((country == null) || (country.length() == 0)) &&
                    ((code == null) || (code.length() == 0)) &&
                    ((state == null) || (state.length() == 0)) &&
                    ((street == null) || (street.length() == 0)) &&
                    ((number == null) || (number.length() == 0))) {
                this.addresses.remove(address.getType());
            }
        }

        this.user.setPostalAddresses(this.addresses.values());
    }
}
