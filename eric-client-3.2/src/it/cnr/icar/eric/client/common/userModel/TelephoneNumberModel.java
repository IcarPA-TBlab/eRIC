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

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class TelephoneNumberModel extends RegistryMappedModel {
    public static final String[] PHONE_TYPES = {
            "Office Phone", "Home Phone", "Mobile Phone", "Beeper", "FAX"
        };
    private final User user;
    private final HashMap<String, TelephoneNumber> numbers = new HashMap<String, TelephoneNumber>();

    TelephoneNumberModel(User u) {
        super(PHONE_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setCountryCode.
     * @param phoneType
     * @param text
     */
    public void setCountryCode(String countryCode) throws JAXRException {
        countryCode = countryCode.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = this.numbers.get(this.key);
            phoneNumber.setCountryCode(countryCode);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setCountryCode(countryCode);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setAreaCode.
     * @param phoneType
     * @param text
     */
    public void setAreaCode(String areaCode) throws JAXRException {
        areaCode = areaCode.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = this.numbers.get(this.key);
            phoneNumber.setAreaCode(areaCode);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setAreaCode(areaCode);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setNumber.
     * @param phoneType
     * @param text
     */
    public void setNumber(String number) throws JAXRException {
        number = number.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = this.numbers.get(this.key);
            phoneNumber.setNumber(number);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setNumber(number);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setExtension.
     * @param phoneType
     * @param text
     */
    public void setExtension(String extension) throws JAXRException {
        extension = extension.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = this.numbers.get(this.key);
            phoneNumber.setExtension(extension);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setExtension(extension);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    /**
     * Method setURL.
     * @param phoneType
     * @param text
     */
    public void setURL(String url) throws JAXRException {
        url = url.trim();

        if (this.numbers.containsKey(this.key)) {
            // Number already exists, just need to modify entry.
            TelephoneNumber phoneNumber = this.numbers.get(this.key);
            phoneNumber.setUrl(url);
        } else {
            // Create new number and add to user.
            TelephoneNumber phoneNumber = this.user.getLifeCycleManager()
                                                   .createTelephoneNumber();
            phoneNumber.setUrl(url);
            this.numbers.put(this.key, phoneNumber);
            this.user.setTelephoneNumbers(this.numbers.values());
        }
    }

    public TelephoneNumber getNumber() {
        TelephoneNumber number = this.numbers.get(this.key);

        return number;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection<TelephoneNumber> numberSet = this.numbers.values();
        TelephoneNumber number = null;
        Iterator<TelephoneNumber> i = numberSet.iterator();

        while (i.hasNext()) {
            number = i.next();

            String areaCode = number.getAreaCode();
            String countryCode = number.getCountryCode();
            String extension = number.getExtension();
            String phoneNumber = number.getNumber();
            String url = number.getUrl();

            // Takes all fields except the address type into account.
            if (((areaCode == null) || (areaCode.length() == 0)) &&
                    ((countryCode == null) || (countryCode.length() == 0)) &&
                    ((extension == null) || (extension.length() == 0)) &&
                    ((phoneNumber == null) || (phoneNumber.length() == 0)) &&
                    ((url == null) || (url.length() == 0))) {
                this.numbers.remove(number.getType());
            }
        }

        this.user.setTelephoneNumbers(this.numbers.values());
    }
}
