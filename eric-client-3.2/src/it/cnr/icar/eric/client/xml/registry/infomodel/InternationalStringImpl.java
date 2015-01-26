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
package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;


/**
 * Implements JAXR API interface named InternationalString.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class InternationalStringImpl implements InternationalString, Cloneable {
    private static final Log log = LogFactory.getLog(InternationalStringImpl.class);
    private HashMap<String, LocalizedString> localizedStringMap = new HashMap<String, LocalizedString>();
    private LifeCycleManagerImpl lcm = null;
    private boolean modified = false;

    public InternationalStringImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        this.lcm = lcm;
        modified = true;
    }

    public InternationalStringImpl(LifeCycleManagerImpl lcm, InternationalStringType ebInternationalStringType)
        throws JAXRException {
        List<LocalizedStringType> ebLocalizedStringTypeList = ebInternationalStringType.getLocalizedString();
        int numItems = ebLocalizedStringTypeList.size();

        for (int i = 0; i < numItems; i++) {
            LocalizedStringType ebLocalizedStringType = ebLocalizedStringTypeList.get(i);
            LocalizedStringImpl ls = new LocalizedStringImpl(lcm, ebLocalizedStringType);
            addLocalizedString_internal(ls);
        }
        
        setModified(false);
    }

    public boolean isModified() {
        if (modified) {
            return modified;
        }
        
        // this might turn to be too costly. Consider having a reference to parent RO and
        // calling setModified directly there. Monitor changes in localizedStringMap then.
        for (Iterator<LocalizedString> it = localizedStringMap.values().iterator(); it.hasNext(); ) {
            LocalizedStringImpl ls = (LocalizedStringImpl)it.next();
            modified = ls.isModified(); 
            if (modified) {
                return modified;
            }
        }
        return modified;
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
        
        // propagate
        if (!modified) {
            for (Iterator<LocalizedString> it = localizedStringMap.values().iterator(); it.hasNext(); ) {
                LocalizedStringImpl ls = (LocalizedStringImpl)it.next();
                ls.setModified(modified);
            }
        }
    }
    
    //The key to the map is a combination of locale and charset
    //This allows map to have multiple entries for the same locale with different charsets
    private String getKey(Locale l, String charSetName) {
        return charSetName + "_" + l.toString();
    }

    public String getValue() throws JAXRException {
        //Call getClosestValue instead of getValue(Locale.getDefault());
        //because it gets the value even when no locale is set on LocalizedString
        //This was need to pass tests in JAXR TCK.
        return getClosestValue();
    }

    public String getValue(Locale locale) throws JAXRException {
        String value = null;
        LocalizedString ls = localizedStringMap.get(getKey(locale,
                    LocalizedStringImpl.DEFAULT_CHARSET_NAME));

        if (ls != null) {
            value = ls.getValue();
        }

        return value;
    }

    /**
     * Convenience method to directly set a value to a Localized String.
     * <code>Locale.getDefault()</code> will be used as <code>locale</code>.
     *
     * @param val New value for Localized String.
     * @throws JAXRException
     * @see setValue(Locale, String)
     */
    public void setValue(String val) throws JAXRException {
        setValue(Locale.getDefault(), val);
    }

    /**
     * Convenience method to directly set a value to a Localized String.
     * 
     * If <code>value</code> is null and LocalizedString exists for <code>locale</code>,
     * LocalizedString will be removed from this InternationalString.
     *
     * If <code>value</code> is not null and LocalizedString does not exists for
     * <code>locale</code>, it will be created with <code>value</code>. Otherwise
     * value will be simply set.
     *
     * InternationalString will be flagged as modified if any operation takes place.
     *
     * @param locale Locale for Localized String.
     * @param val New value for Localized String.
     * @throws JAXRException
     */
    public void setValue(Locale locale, String val) throws JAXRException {
        LocalizedString ls = localizedStringMap.get(getKey(locale,
                    LocalizedStringImpl.DEFAULT_CHARSET_NAME));

        if (ls != null && val != null) {
            ls.setValue(val);
        } else if (ls != null && val == null) {
            removeLocalizedString(ls);
        } else if (val != null && val.length() > 0) {
            ls = new LocalizedStringImpl(null);
            ls.setLocale(locale);
            ls.setValue(val);
            addLocalizedString(ls);
        }
    }

    public void addLocalizedString(LocalizedString ls)
    throws JAXRException {
        addLocalizedString_internal(ls);
        setModified(true);
    }
    
    protected void addLocalizedString_internal(LocalizedString ls)
    throws JAXRException {
        if (!localizedStringMap.containsValue(ls)) {
            localizedStringMap.put(getKey(ls.getLocale(), ls.getCharsetName()), ls);
        } else {
            LocalizedString lsOrig = localizedStringMap.get(getKey(ls.getLocale(),
                                                                                    ls.getCharsetName()));
            if (lsOrig == null) {
                Iterator<String> keys = localizedStringMap.keySet().iterator();
                LocalizedString tempLS = null;
                String tempKey = null;

                while (keys.hasNext() && tempKey == null) {
                    tempKey = keys.next();
                    tempLS = localizedStringMap.get(tempKey);
                    if (tempLS !=ls) {
                        tempKey = null;
                    }
                }
                if (tempKey !=null) {
                    localizedStringMap.remove(tempKey);
                    localizedStringMap.put(getKey(ls.getLocale(), ls.getCharsetName()), ls);
                }
            }
        }
    }

    public void addLocalizedStrings(@SuppressWarnings("rawtypes") Collection localizedStrings)
        throws JAXRException {
        Iterator<?> iter = localizedStrings.iterator();

        while (iter.hasNext()) {
            LocalizedString ls = (LocalizedString) iter.next();
            addLocalizedString_internal(ls);
        }

        setModified(true);
    }

    public void removeLocalizedString(LocalizedString ls)
        throws JAXRException {
        boolean lsRemoved = removeLocalizedString_internal(ls);
        
        // set modified only if something actually removed
        if (lsRemoved) {
            setModified(true);
        }
    }

    public void removeLocalizedStrings(@SuppressWarnings("rawtypes") Collection localizedStrings)
        throws JAXRException {
        Iterator<?> iter = localizedStrings.iterator();

        boolean lsRemoved = false;
        while (iter.hasNext()) {
            LocalizedString ls = (LocalizedString) iter.next();
            lsRemoved = removeLocalizedString_internal(ls) || lsRemoved;
        }
        
        // set modified only if something actually removed
        if (lsRemoved) {
            setModified(true);
        }
    }
    
    protected boolean removeLocalizedString_internal(LocalizedString ls)
        throws JAXRException {
        String key = getKey(ls.getLocale(), ls.getCharsetName());
        LocalizedString old = localizedStringMap.get(key);

        if (ls == old) {
            localizedStringMap.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public LocalizedString getLocalizedString(Locale locale, String charsetName)
        throws JAXRException {
        String key = getKey(locale, charsetName);

        return localizedStringMap.get(key);
    }

    public Collection<LocalizedString> getLocalizedStrings() throws JAXRException {
        Collection<LocalizedString> localizedStrings = localizedStringMap.values();

        return localizedStrings;
    }

    /**
     * Gets the LocalizedString for the default locale (<code>Locale.getDefault()
     * </code>) or the closest match, according to a precedence list (see {@link
     * #getClosestKeys(java.util.Locale, java.lang.String) getClosestKeys(
     * java.util.Locale, java.lang.String)}.
     *
     * @param locale the desired Locale
     * @return LocalizedString for default Locale or for the 1st alternate Locale
     *         found. Null if nothing found.
     */
    public LocalizedString getClosestLocalizedString()
        throws JAXRException {
        return getClosestLocalizedString(Locale.getDefault(),
            LocalizedStringImpl.DEFAULT_CHARSET_NAME);
    }

    /**
     * Gets the LocalizedString for the given locale or the closest match,
     * according to a precedence list (see {@link #getClosestKeys(
     * java.util.Locale, java.lang.String) getClosestKeys(java.util.Locale,
     * java.lang.String)}.
     *
     * @param locale the desired Locale
     * @return LocalizedString for 'locale' or for the 1st alternate Locale found.
     *         Null if nothing found.
     */
    public LocalizedString getClosestLocalizedString(Locale locale,
        String charsetName) throws JAXRException {
        Iterator<String> keys = getClosestKeys(locale, charsetName).iterator();

        while (keys.hasNext()) {
            Object lString = localizedStringMap.get(keys.next());

            if (lString != null) {
                // TO DO: make sure that when returning Java actual object,
                // modifications are handled correctly.
                return (LocalizedString) lString;
            }
        }

        //This fetches a content across locale.
        //If en is selected 'contentLanguage' not present and fr is present.
        //It would get fr value for display.
        if (localizedStringMap != null) {
            Iterator<String> iter = localizedStringMap.keySet().iterator();
            if (iter.hasNext()) {
                Object lString = localizedStringMap.get(iter.next());
                return (LocalizedString)lString;
            }
        }

        return null;
    }

    /**
     * Gets the localized value of an InternationalString using default locale
     * (<code>Locale.getDefault()</code>) or the closest match, according to a
     * precedence list (see {@link #getClosestKeys(java.util.Locale,
     * java.lang.String) getClosestKeys(java.util.Locale, java.lang.String)}.
     *
     * @param locale the desired Locale
     * @return String with LocalizedString's value for 'locale' or for the 1st
     *         alternate Locale found. Null if nothing found.
     */
    public String getClosestValue() throws JAXRException {
        return getClosestValue(Locale.getDefault());
    }

    /**
     * Gets the localized value of an InternationalString for a given Locale or
     * the closest match, according to a precedence list (see {@link
     * #getClosestKeys(java.util.Locale, java.lang.String) getClosestKeys(
     * java.util.Locale, java.lang.String)}.
     *
     * @param locale the desired Locale
     * @return String with LocalizedString's value for 'locale' or for the 1st
     *         alternate Locale found. Null if nothing found.
     */
    public String getClosestValue(Locale locale) throws JAXRException {
        LocalizedString lString = getClosestLocalizedString(locale, null);

        if (lString != null) {
            return lString.getValue();
        }

        return null;
    }

    /**
     * Gets a List of possible keys to be used when searching for the closest
     * LocalizedString for a given locale/charset pair. The List starts with a
     * key for the specific locale (ignored if null) followed by a lookup
     * precedence list of alternative keys (inspired on
     * <code>java.util.ResourceBundle</code>) as shown bellow.
     *
     * <ul>
     * <li> charset + "_" + language1 + "_" + country1 + "_" + variant1 </li>
     * <li> charset + "_" + language1 + "_" + country1                  </li>
     * <li> charset + "_" + language1                                   </li>
     * <li> charset + "_" + language2 + "_" + country2 + "_" + variant2 </li>
     * <li> charset + "_" + language2 + "_" + country2                  </li>
     * <li> charset + "_" + language2                                   </li>
     * <li> charset + "_" + language3 + "_" + country3 + "_" + variant3 </li>
     * <li> charset + "_" + language3 + "_" + country3                  </li>
     * <li> charset + "_" + language3                                   </li>
     * </ul>
     *
     * Where:
     * <ul>
     * <li> Candidate keys where the final component is an empty string are omitted.
     *      For example, if country1 is an empty string, charset + "_" + language1 +
     *      "_" + country1 is omitted; </li>
     * <li> language1, contry1, variant1 come from the 'locale' parameter;</li>
     * <li> language2, contry2, variant2 come from JVM default Locale (Locale.getDefault());</li>
     * <li> language3, contry3, variant3 come from ebxmlrr default Locale (default "en-US");</li>
     * </ul>
     *
     * @param charset the charset name
     * @param locale the locale
     * @return List with the keys along the search path.
     */
    public static List<String> getClosestKeys(Locale locale, String charset) {
        // TO DO: Cache generated ClosestKeysLists (??)
        // TO DO: get it from ProviderProperties
        Locale ebxmlrrDefaultLocale = new Locale("en", "US");
        charset = LocalizedStringImpl.DEFAULT_CHARSET_NAME;

        List<String> keysList = calculateKeys(locale, charset);

        if (!locale.equals(Locale.getDefault())) {
            keysList.addAll(calculateKeys(Locale.getDefault(), charset));
        }

        if (!Locale.getDefault().equals(ebxmlrrDefaultLocale) &&
                !locale.equals(ebxmlrrDefaultLocale)) {
            // TO DO: Decide if default locale should be expanded, too.
            keysList.addAll(calculateKeys(ebxmlrrDefaultLocale, charset));
        }

        return keysList;
    }

    /**
     * Calculate the possible keys List for a locale/charset pair. Candidate keys
     * are:
     * <ul>
     * <li> charset + "_" + language + "_" + country + "_" + variant
     * <li> charset + "_" + language + "_" + countryi
     * <li> charset + "_" + language
     * </ul>
     *
     * Candidate keys where the final component is an empty string are omitted.
     * For example, if country is an empty string, the second candidate key is omitted.
     *
     * @param charset the charset name
     * @param locale the locale
     * @return List with the keys along the search path.
     */
    private static List<String> calculateKeys(Locale locale, String charset) {
        final ArrayList<String> result = new ArrayList<String>(3);
        final String language = locale.getLanguage();
        final int languageLength = language.length();
        final String country = locale.getCountry();
        final int countryLength = country.length();
        final String variant = locale.getVariant();
        final int variantLength = variant.length();

        if ((languageLength + countryLength + variantLength) == 0) {
            //The locale is "", "", "".
            return result;
        }

        final StringBuffer temp = new StringBuffer(charset);
        temp.append('_');
        temp.append(language);

        if (languageLength > 0) {
            result.add(0, temp.toString());
        }

        if ((countryLength + variantLength) == 0) {
            return result;
        }

        temp.append('_');
        temp.append(country);

        if (countryLength > 0) {
            result.add(0, temp.toString());
        }

        if (variantLength == 0) {
            return result;
        }

        temp.append('_');
        temp.append(variant);
        result.add(0, temp.toString());

        return result;
    }

    protected void setBindingObject(InternationalStringType ebInternationalStringType)
        throws JAXRException {
        Iterator<LocalizedString> iter = getLocalizedStrings().iterator();

        while (iter.hasNext()) {
            LocalizedStringImpl ls = (LocalizedStringImpl) iter.next();
            LocalizedStringType ebLocalizedStringType = (LocalizedStringType) ls.toBindingObject();
            ebInternationalStringType.getLocalizedString().add(ebLocalizedStringType);
        }
    }

    public Object clone() {
        InternationalStringImpl _clone = null;

        try {
            _clone = new InternationalStringImpl(lcm);
            
            Iterator<LocalizedString> iter = this.getLocalizedStrings().iterator();
            while (iter.hasNext()) {
                LocalizedStringImpl ls = (LocalizedStringImpl)iter.next();
                LocalizedString lsClone = (LocalizedString)ls.clone();
                _clone.addLocalizedString(lsClone);
            }
        } catch (JAXRException e) {
            //Cannot happen.
            e.printStackTrace();
        }

        return _clone;
    }

    public String toString() {
        String str = super.toString();

        try {
            str = getClosestValue();
        } catch (Exception e) {
            log.warn(e);
        }

        return str;
    }
}
