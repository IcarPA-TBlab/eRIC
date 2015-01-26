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

import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.Locale;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.LocalizedString;


//import org.oasis.ebxml.registry.bindings.rim.*;

/**
 * Implements JAXR API interface named LocalizedString.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class LocalizedStringImpl implements LocalizedString {
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private Locale locale = null;
    private String value = null;
    private String charsetName = null;
    private boolean modified = false;

    private LifeCycleManagerImpl lcm;

    public LocalizedStringImpl(LifeCycleManagerImpl lcm) {
        this.lcm = lcm;
    }

    public LocalizedStringImpl(LifeCycleManagerImpl lcm, LocalizedStringType ebLocalizedStringType) {
        this(lcm);

        //Need to parse language and country from lang
        String xsdLang = ebLocalizedStringType.getLang();
        locale = xsdLang2Locale(xsdLang);

        value = ebLocalizedStringType.getValue();
        charsetName = ebLocalizedStringType.getCharset();
    }

    public boolean isModified() {
        return modified;
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    private Locale xsdLang2Locale(String xsdLang) {
        Locale locale = null;

        //Need to see if following is robust enough for all cases.
        // For now assuming 3 formats: xx, xx-yy, or xx-yy-zz where xx is
	// language, yy is country, and zz is variant
        String language = xsdLang.substring(0, 2);

        if (xsdLang.length() >= 5) {
            String country = xsdLang.substring(3, 5);
	    if (xsdLang.length() == 5) {
		locale = new Locale(language, country);
	    } else {
		String variant = xsdLang.substring(7);
		locale = new Locale(language, country, variant);
	    }
        } else {
            locale = new Locale(language);
        }

        return locale;
    }

    private String locale2xsdLang(Locale locale) {
        String xsdLang = locale.toString().replace('_','-');

        return xsdLang;
    }

    public String getCharsetName() throws JAXRException {
        String csName = null;

        if (charsetName == null || "".equals(charsetName.trim())) {
            csName = DEFAULT_CHARSET_NAME;
        } else {
            csName = charsetName;
        }

        return csName;
    }

    public Locale getLocale() throws JAXRException {
        Locale l = null;

        if (locale == null) {
            l = Locale.getDefault();
        } else {
            l = locale;
        }

        return l;
    }

    public String getValue() throws JAXRException {
        return value;
    }

    public void setCharsetName(String par1) throws JAXRException {
        if ((charsetName != null && !charsetName.equals(par1)) || (charsetName == null && par1 != null)) {
            charsetName = par1;
            setModified(true);
        }
    }

    public void setLocale(Locale par1) throws JAXRException {
        if ((locale != null && !locale.equals(par1)) || (locale == null && par1 != null)){
            locale = par1;
            setModified(true);
        }
    }

    public void setValue(String par1) throws JAXRException {
        if ((value != null && !value.equals(par1)) || (value == null && par1 != null)) {
            value = par1;
            setModified(true);
        }
    }

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        LocalizedStringType ebLocalizedStringType = factory.createLocalizedStringType(); 
		setBindingObject(ebLocalizedStringType);
		
//		JAXBElement<LocalizedStringType> ebLocalizedString = factory.createLocalizedString(ebLocalizedStringType);
		
		return ebLocalizedStringType;

    }
    
    protected void setBindingObject(LocalizedStringType ebLocalizedStringType)
    throws JAXRException {
//        super.setBindingObject(ebExtrinsicObjectType);

        ebLocalizedStringType.setLang(locale2xsdLang(getLocale()));
        ebLocalizedStringType.setValue(getValue());
        ebLocalizedStringType.setCharset(getCharsetName());
        
    }

    public Object clone() {
        LocalizedStringImpl _clone = null;

        try {
            _clone = new LocalizedStringImpl(lcm);
            _clone.setCharsetName(getCharsetName());
            _clone.setLocale(getLocale());
            _clone.setValue(getValue());
            setModified(false);
        } catch (JAXRException e) {
            //Cannot happen.
            e.printStackTrace();
        }

        return _clone;
    }
    
}
