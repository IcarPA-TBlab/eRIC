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

package it.cnr.icar.eric.client.ui.thin;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

import javax.faces.context.FacesContext;

import it.cnr.icar.eric.common.AbstractResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to load i18n and other resources from a resource bundle
 * file.
 *
 * @author  Paul Sterk
 */
public class WebUIResourceBundle extends AbstractResourceBundle {

    @SuppressWarnings("unused")
	private ResourceBundle resourceBundle = null;
    private static WebUIResourceBundle instance = null;
    private static String messageBundleName = null;
    private static final Log log = LogFactory.getLog(WebUIResourceBundle.class);

    private WebUIResourceBundle() {
        initResources();
    }

    protected void initResources() {
        messageBundleName = FacesContext.getCurrentInstance()
                                            .getApplication()
                                            .getMessageBundle();
        if (messageBundleName == null) {
            messageBundleName = "it.cnr.icar.eric.client.ui.thin.ResourceBundle";
        }
    }

    private ResourceBundle getLocalizedResourceBundle() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        return ResourceBundle.getBundle(messageBundleName, locale);
    }

    /**
     * Implement Singleton class, this method is only way to get this object.
     */
    public synchronized static WebUIResourceBundle getInstance() {
        if (instance == null) {
            instance = new WebUIResourceBundle();
        }
        return instance;
    }

    public ResourceBundle getBundle() {
        getInstance();
        return getLocalizedResourceBundle();
    }


    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(messageBundleName, locale);
    }

    /**
     * Gets an object for the given key from this resource bundle or one of
     * its parents.
     *  @param key
     *  The key to use to obtain the resource
     */
    protected Object handleGetObject(String key) {
        Object ret = null;

        try {
            ret = getBundle().getObject(key);
        } catch (MissingResourceException ex) {
            log.debug("Could not find resource with this key: "+key+
                " Please check your resource file");
        }
        if (ret == null || ret.equals("")) {
            // Try converting key to Camel Case
            // 1. remove empty spaces
            key = key.replaceAll(" ", "");
            // 2. convert first char to lower case
            key = key.substring(0,1).toLowerCase() + key.substring(1);
            try {
                ret = getBundle().getObject(key);
            } catch (MissingResourceException ex) {
		ret = null;
            }

	    // ??? Though it is not clear getObject() ever returns null,
	    // ??? leave this code outside exception handler above for now.
	    // ??? (no harm)
            if (ret == null) {
                log.warn(getString("missingMessage", new Object[]{key}));
                // TODO: refactor this into AbstractResourceBundle
                ret = "???"+key+"???";
            }
        }
        return ret;
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     * If this string is null, use the defaultString
     *
     * @param key
     *  The key to use to obtain the resource
     * @param defaultString
     *  The default string to use if the obtained resource is null
     */
    public String getString(String key, String defaultString) {
        String resourceString = getString(key);
        if (resourceString == null || resourceString.equals("")) {
            if (defaultString == null) {
                throw new NullPointerException(getString("excDefaultStringIsNull"));
            }
            resourceString = defaultString;
        }
        return resourceString;
    }

}
