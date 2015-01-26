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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

/**
 * A backing bean for user preferences
 *
 * @author  Diego Ballve / Digital Artefacts Europe
 */
public class UserPreferencesBean {
    
    /** The log. */
    private static final Log log = LogFactory.getLog(UserPreferencesBean.class);

    /**
     * Holds value of property contentLocale.
     */
    private Locale contentLocale;

    /**
     * Holds value of property uiLocale.
     */
    private Locale uiLocale;

    /**
     * Holds value of property charset.
     */
    private String charset;

    //cache
    private Collection<SelectItem> allLocalesSelectItems;
    private Collection<SelectItem> supportedLocalesSelectItems;
    
    /** Creates a new instance of LocaleBean */
    public UserPreferencesBean() {
        initLocales();
    }

    /** Sets the locales to 1st supported locale (directly or indirectly). */
    private void initLocales() {
        Iterator<?> requestLocales = FacesContext.getCurrentInstance().getExternalContext().getRequestLocales();
        Collection<Locale> supportedLocales = getSupportedUiLocales();
        //iterate though client preferred locales until find supported 
        while (requestLocales.hasNext()) {
            Locale loc = (Locale)requestLocales.next();
            // try direct match
            if (supportedLocales.contains(loc)) {
                init(loc);
                return;
            }
            // try to use language country, without variant
            if (loc.getVariant() != null && !"".equals(loc.getVariant())) {
                loc = new Locale(loc.getLanguage(), loc.getCountry());
                if (supportedLocales.contains(loc)) {
                    init(loc);
                    return;
                }
            }
            // try to use language without country and variant
            if (loc.getCountry() != null && !"".equals(loc.getCountry())) {
                loc = new Locale(loc.getLanguage());
                if (supportedLocales.contains(loc)) {
                    init(loc);
                    return;
                }
            }
        }
        // fall to default locale, from properties (or en_US, if not defined)
        init(getDefaultLocale());
    }
    
    private void init(Locale loc) {
        uiLocale = loc;
        contentLocale = loc;
        charset = "UTF-8";        
    }
    
    public int getNumSupportedUiLocales() {
        return getSupportedUiLocales().size();
    }
    
    /** Returns a Collection of available Locales. */
    public Collection<Locale> getSupportedUiLocales() {
        String supportedLocales = ProviderProperties.getInstance().
            getProperty("eric.client.thinbrowser.supportedlocales", "en_US");
        StringTokenizer tkz = new StringTokenizer(supportedLocales, "|");
        ArrayList<Locale> locales = new ArrayList<Locale>();
        while (tkz.hasMoreTokens()) {
            Locale locale = parseLocale(tkz.nextToken());
            if (locale != null) {
                locales.add(locale);
            }
        }
        return locales;
    }

    /** returns the localized supported locales display names */
    public Collection<String> getSupportedUiLocalesDisplayNames() {
        String supportedLocales = ProviderProperties.getInstance().
        getProperty("eric.client.thinbrowser.supportedlocales", "en_US");
        StringTokenizer tkz = new StringTokenizer(supportedLocales, "|");
        ArrayList<String> locales = new ArrayList<String>();
        while (tkz.hasMoreTokens()) {
            Locale locale = parseLocale(tkz.nextToken());
            if (locale != null && this.uiLocale != null) {
                locales.add(locale.getDisplayName(this.uiLocale) + " (" + locale + ")");
            }
        }
        return locales;
    }
    
    /** Returns a Collection of available locales as SelectItems. */
    public Collection<SelectItem> getSupportedLocalesSelectItems() {
        if (supportedLocalesSelectItems == null) {
            ArrayList<SelectItem> selectItems = new ArrayList<SelectItem>();
            for (Iterator<Locale> it = getSupportedUiLocales().iterator(); it.hasNext(); ) {
                Locale loc = it.next();
                SelectItem item = new SelectItem(loc.toString(), loc.getDisplayName(loc));
                selectItems.add(item);
            }
            supportedLocalesSelectItems = selectItems;
        }
        return supportedLocalesSelectItems;
    }

    /** Returns a Collection of all locales as SelectItems. */
    public Collection<SelectItem> getAllLocalesSelectItems() {
        if (allLocalesSelectItems == null) {
            TreeMap<String, SelectItem> selectItemsMap = new TreeMap<String, SelectItem>();
            Locale loc[] = Locale.getAvailableLocales();
            for (int i = 0; i < loc.length; i++) {
                String name = loc[i].getDisplayName(loc[i]);
                SelectItem item = new SelectItem(loc[i].toString(), name);
                selectItemsMap.put(name.toLowerCase(), item);
            }
            Collection<SelectItem> all = new ArrayList<SelectItem>();
            all.addAll(selectItemsMap.values());
            allLocalesSelectItems = all;
        }
        return allLocalesSelectItems;
    }
    
    public Locale getDefaultLocale() {
        final String DEFAULT_LOCALE = "en_US";
        String defaultLocaleSt = ProviderProperties.getInstance().
            getProperty("eric.client.thinbrowser.defaultlocale", DEFAULT_LOCALE);
        Locale loc = parseLocale(defaultLocaleSt);
        if (loc != null) {
            return loc;
        } else {
            return parseLocale(DEFAULT_LOCALE);
        }
    }
    
    /** Returns a Locale, or null if failed to create one. */
    public static Locale parseLocale(String locale) {
        if (locale == null) {
            return null;
        }
	String[] components = locale.split("_");
	Locale newLocale = null;
	if (components.length == 1) {
	    newLocale = new Locale(components[0]);
	} else if (components.length == 2) {
	    newLocale = new Locale(components[0], components[1]);
	} else if (components.length == 3) {
	    newLocale = new Locale(components[0], components[1], components[2]);
	} else {
            if (log.isWarnEnabled()) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.InvalidLocale", new Object[]{locale}));
            }
            return null;
	}

	return newLocale;
    }    

    /** Event listener for UI Locale change. */
    public void changeUiLocaleCode(ValueChangeEvent event) {
        setUiLocaleCode((String)event.getNewValue());
    }
    
    /** Event listener for Content Locale change. */
    public void changeContentLocaleCode(ValueChangeEvent event) {
        setContentLocaleCode((String)event.getNewValue());
    }
    
    /** This method can be used to obtain the current language preferences
     * from the web browser */
    public String resetLocale() {
        initLocales();
        return "";
    }
    
    /**
     * Getter for property contentLocale.
     * @return Value of property contentLocale.
     */
    public Locale getContentLocale() {

        return this.contentLocale;
    }

    /**
     * Setter for property contentLocale.
     * @param contentLocale New value of property contentLocale.
     */
    public void setContentLocale(Locale contentLocale) {

        this.contentLocale = contentLocale;
    }

    /**
     * Getter for property uiLocale.
     * @return Value of property uiLocale.
     */
    public Locale getUiLocale() {

        return this.uiLocale;
    }

    /**
     * Setter for property uiLocale.
     * @param uiLocale New value of property uiLocale.
     */
    public void setUiLocale(Locale uiLocale) {

        this.uiLocale = uiLocale;
    }

    /**
     * Getter for property charset.
     * @return Value of property charset.
     */
    public String getCharset() {

        return this.charset;
    }

    /**
     * Setter for property charset.
     * @param charset New value of property charset.
     */
    public void setCharset(String charset) {

        this.charset = charset;
    }

    /**
     * Getter for property contentLocaleCode.
     * @return Value of property contentLocaleCode.
     */
    public String getContentLocaleCode() {

        return this.contentLocale.toString();
    }

    /**
     * Setter for property contentLocaleCode.
     * @param contentLocaleCode New value of property contentLocaleCode.
     */
    public void setContentLocaleCode(String contentLocaleCode) {

        this.contentLocale = parseLocale(contentLocaleCode);
    }

    /**
     * Getter for property uiLocaleCode.
     * @return Value of property uiLocaleCode.
     */
    public String getUiLocaleCode() {

        return this.uiLocale.toString();
    }

    /**
     * Setter for property uiLocaleCode.
     * @param uiLocaleCode New value of property uiLocaleCode.
     */
    public void setUiLocaleCode(String uiLocaleCode) {

        this.uiLocale = parseLocale(uiLocaleCode);
    }

    public boolean isNameBeforeAddress() {
        boolean nameBeforeAddress = true;
        String uiLocaleCode = getUiLocaleCode();
        if (uiLocaleCode.indexOf("zh") != -1 ||
            uiLocaleCode.indexOf("ko") != -1 ||
            uiLocaleCode.indexOf("ja") != -1) {
            nameBeforeAddress = false;
        }
        return nameBeforeAddress;
    }
     
}
