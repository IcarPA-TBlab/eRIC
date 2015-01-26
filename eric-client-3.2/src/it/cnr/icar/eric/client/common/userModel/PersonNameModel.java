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

import it.cnr.icar.eric.client.common.CommonResourceBundle;
import it.cnr.icar.eric.client.common.Model;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.User;


/**
 * @author Fabian Ritzmann
 */
public class PersonNameModel implements Model {
    private final User user;

    PersonNameModel(User name) throws JAXRException {
        this.user = name;
    }

    /**
     * Method setFirstName.
     * @param text
     */
    public void setFirstName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setFirstName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName(text, "", "");
            this.user.setPersonName(name);
        }
    }

    /**
     * Method setMiddleName.
     * @param text
     */
    public void setMiddleName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setMiddleName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName("", text, "");
            this.user.setPersonName(name);
        }
    }

    /**
     * Method setLastName.
     * @param text
     */
    public void setLastName(String text) throws JAXRException {
        text = text.trim();

        PersonName name = user.getPersonName();

        if (name != null) {
            name.setLastName(text);
        } else {
            name = user.getLifeCycleManager().createPersonName("", "", text);
            this.user.setPersonName(name);
        }
    }

    public void validate() throws JAXRException {
        String firstName = user.getPersonName().getFirstName();
        String middleName = user.getPersonName().getMiddleName();
        String lastName = user.getPersonName().getLastName();

        if (((firstName == null) || (firstName.length() == 0)) &&
                ((middleName == null) || (middleName.length() == 0)) &&
                ((lastName == null) || (lastName.length() == 0))) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.missingUserPersonName"));
        }
    }
}
