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
package it.cnr.icar.eric.client.xml.registry.util;

import it.cnr.icar.eric.common.AbstractResourceBundle;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.registry.JAXRException;


/**
 * Internationalization utilities
 *
 * This class expects to obtain messages from a ResourceBundle in the
 * same package, with a baseName of "messages".  Typically, this is
 * done using Java property files loaded via PropertyResourceBundle.
 *
 * @author Edwin Goei
 */
public class JAXRResourceBundle extends AbstractResourceBundle {
    private static final JAXRResourceBundle INSTANCE = new JAXRResourceBundle();
    private ResourceBundle bundle;
    public static final String BASE_NAME =
		"it.cnr.icar.eric.client.xml.registry.util.ResourceBundle";


    private JAXRResourceBundle() {
        // May want to add a setLocale() method in the future??
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static JAXRResourceBundle getInstance() {
        return INSTANCE;
    }

    protected Object handleGetObject(String key) {
        Object ret = null;

        try {
            ret = bundle.getObject(key);
        } catch (MissingResourceException x) {
	    // ??? The following line should probably be executed only if a
	    // ??? debug or verbose option is on somewhere.  Where?
            x.printStackTrace();
            ret = "[MissingResourceException] key=" + key;
        }

        return ret;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public JAXRException newJAXRException(String key) {
        return new JAXRException(getString(key));
    }

    public JAXRException newJAXRException(String key, Object[] params) {
        return new JAXRException(getString(key, params));
    }
}
