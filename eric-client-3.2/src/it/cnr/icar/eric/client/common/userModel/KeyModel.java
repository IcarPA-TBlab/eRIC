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
import it.cnr.icar.eric.client.xml.registry.util.UserRegistrationInfo;


import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


/**
 * A model for Digital Keys. Adds Model.validate to UserRegistrationInfo.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class KeyModel extends UserRegistrationInfo implements Model {
    private static final int MIN_ALIAS_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Creates a new instance of KeyModel.
     * Calls parent constructor with null argument.
     */
    public KeyModel() throws JAXRException {
        super(null);
    }

    /**
     * Creates a new instance of KeyModel.
     *
     * @param u User
     */
    public KeyModel(User u) throws JAXRException {
        super(u);
    }

    /** Implementation for Model.validate() */
    public void validate() throws JAXRException {
        if ((getAlias() == null) || (getAlias().length() < MIN_ALIAS_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keyAliasLength",
								   new String[] {String.valueOf(MIN_ALIAS_LENGTH)}));
        }

        if ((getKeyPassword() == null) || (getKeyPassword().length < MIN_PASSWORD_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keystorePasswordLength",
								   new String[] {String.valueOf(MIN_PASSWORD_LENGTH)}));
        }

        if ((getStorePassword() == null) || (getStorePassword().length < MIN_PASSWORD_LENGTH)) {
            throw new JAXRException(CommonResourceBundle.getInstance()
                                                        .getString("error.keyPasswordLength",
								   new String[] {String.valueOf(MIN_PASSWORD_LENGTH)}));
        }
    }
}
