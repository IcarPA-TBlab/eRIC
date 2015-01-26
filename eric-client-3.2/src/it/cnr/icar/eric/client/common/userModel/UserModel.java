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

import it.cnr.icar.eric.client.common.Model;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonNameImpl;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;


import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


/**
 * Wrapper around {@link javax.xml.registry.infomodel.User}. This allows
 * us to check the validity of the input. Usually one would do that in the
 * model itself (i.e. class User), but an extra layer inbetween gives us
 * more flexibility.
 *
 * @author Fabian Ritzmann
 */
public class UserModel implements Model {
    private final User user;
    private final PersonNameModel personName;
    private final EmailAddressModel emailAddress;
    private final PostalAddressModel postalAddress;
    private final TelephoneNumberModel phoneNumber;
    private final KeyModel key;

    /**
     * @param u Underlying User implementation
     */
    public UserModel(User u) throws JAXRException {
        this.user = u;
        this.key = new KeyModel(u);
        this.personName = new PersonNameModel(u);
        this.emailAddress = new EmailAddressModel(u);
        this.postalAddress = new PostalAddressModel(u);
        this.phoneNumber = new TelephoneNumberModel(u);

        // hard coded for now:
        key.setStorePassword(ProviderProperties.getInstance()
                                               .getProperty("jaxr-ebxml.security.storepass")
                                               .toCharArray());
    }

    public User getUser() {
        return user;
    }

    public KeyModel getUserRegistrationInfo() {
        return key;
    }

    /**
     * Method getPersonNameModel.
     */
    public PersonNameModel getPersonNameModel() {
        return this.personName;
    }

    /**
     * Method getEmailAddressModel.
     */
    public EmailAddressModel getEmailAddressModel() {
        return this.emailAddress;
    }

    /**
     * Method getPostalAddressModel.
     */
    public PostalAddressModel getPostalAddressModel() {
        return this.postalAddress;
    }

    /**
     * Method getTelephoneNumberModel.
     */
    public TelephoneNumberModel getTelephoneNumberModel() {
        return this.phoneNumber;
    }

    public void validate() throws JAXRException {
        this.personName.validate();
        this.emailAddress.validate();
        this.postalAddress.validate();
        this.phoneNumber.validate();
        this.key.validate();

        // TO DO: Review JAXR implementation dependency
        InternationalStringImpl roName = (InternationalStringImpl) this.user.getName();

        if ((roName == null) || (roName.getClosestValue() == null) ||
                (roName.getClosestValue().trim().length() == 0)) {
            String name = ((PersonNameImpl) (this.user.getPersonName())).getFormattedName();
            roName = (InternationalStringImpl) this.user.getLifeCycleManager()
                                                        .createInternationalString(name);
            this.user.setName(roName);
        }
    }
}
