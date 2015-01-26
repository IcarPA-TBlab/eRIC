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
package it.cnr.icar.eric.client.ui.swing;


import it.cnr.icar.eric.common.AbstractResourceBundle;

//import java.text.MessageFormat;
//import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * For loading the resource from ResourceBundle.properties
*/
public class JavaUIResourceBundle extends AbstractResourceBundle {
	public static final String BASE_NAME =
		"it.cnr.icar.eric.client.ui.swing.ResourceBundle";
    private static JavaUIResourceBundle instance;
	private static Locale locale;
    private ResourceBundle bundle;

    protected JavaUIResourceBundle() {
        // Load the resource bundle of default locale
        bundle = ResourceBundle.getBundle(BASE_NAME);
    }

    protected JavaUIResourceBundle(Locale locale) {
        // Load the resource bundle of specified locale
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public synchronized static JavaUIResourceBundle getInstance() {
        if (instance == null) {
            instance = new JavaUIResourceBundle();
            locale = Locale.getDefault();
        }

        return instance;
    }

    public synchronized static JavaUIResourceBundle getInstance(Locale locale) {
        if (instance == null) {
            instance = new JavaUIResourceBundle(locale);
        } else {
            if (JavaUIResourceBundle.locale != locale) {
                instance = new JavaUIResourceBundle(locale);
            }
	}
        return instance;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }
}
