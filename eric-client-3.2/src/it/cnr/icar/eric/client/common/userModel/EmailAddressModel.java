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
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class EmailAddressModel extends RegistryMappedModel {
    public static final String[] EMAIL_TYPES = { "Office email", "Home email", };
    private final User user;
    private final Map<String, EmailAddress> addresses = new HashMap<String, EmailAddress>();

    EmailAddressModel(User u) {
        super(EMAIL_TYPES[0]);
        this.user = u;
    }

    /**
     * Method setAddress.
     * @param emailType
     * @param text
     */
    public void setAddress(String address) throws JAXRException {
        address = address.trim();

        // Address already exists, just need to modify entry.
        if (this.addresses.containsKey(this.key)) {
            EmailAddress emailAddress = this.addresses.get(this.key);
            emailAddress.setAddress(address);
        }
        // Create new address and add to user.
        else {
            EmailAddress emailAddress = this.user.getLifeCycleManager()
                                                 .createEmailAddress(address,
                    this.key);
            this.addresses.put(this.key, emailAddress);
            this.user.setEmailAddresses(this.addresses.values());
        }
    }

    public EmailAddress getAddress() {
        EmailAddress address = this.addresses.get(this.key);

        return address;
    }

    public void validate() throws JAXRException {
        // Remove empty addresses
        Collection<EmailAddress> addressSet = this.addresses.values();
        EmailAddress address = null;
        Iterator<EmailAddress> i = addressSet.iterator();

        while (i.hasNext()) {
            address = i.next();

            String emailAddress = address.getAddress();

            if ((emailAddress == null) || (emailAddress.length() == 0)) {
                this.addresses.remove(address.getType());
            }
        }

        this.user.setEmailAddresses(this.addresses.values());
    }
}
