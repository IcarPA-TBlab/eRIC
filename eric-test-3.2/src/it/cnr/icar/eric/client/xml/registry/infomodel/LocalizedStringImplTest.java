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

import it.cnr.icar.eric.client.common.ClientTest;

import java.util.Locale;
import javax.xml.registry.LifeCycleManager;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * jUnit Test for LocalizedStringImpl.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class LocalizedStringImplTest extends ClientTest {
    
    public LocalizedStringImplTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
       
        TestSuite suite = new TestSuite(LocalizedStringImplTest.class);
        return suite;
    }
    
    /** Test creating and modifying LString (client side only) */
    public void testLocalizedStringModified() throws Exception {
        LifeCycleManager lcm = getLCM();
        
        Locale locale = Locale.CANADA_FRENCH;
        String value = "textValue";
        String charset = "utf-8";
        
        // create new string, assert modified
        LocalizedStringImpl lString = (LocalizedStringImpl)lcm.createLocalizedString(locale, value, charset);
        assertTrue(lString.isModified());
        
        // clean modified flag, try setting same value to each component, assert not modified
        lString.setModified(false);
        
        lString.setLocale(locale);
        assertEquals(locale, lString.getLocale());
        assertTrue(!lString.isModified());
        
        lString.setCharsetName(charset);
        assertEquals(charset, lString.getCharsetName());
        assertTrue(!lString.isModified());
        
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(!lString.isModified());

        // set new value for each component, assert modified
        locale = Locale.CANADA;
        value = "textValue2";
        charset = "iso-8859-1";
        
        lString.setLocale(locale);
        assertEquals(locale, lString.getLocale());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        lString.setCharsetName(charset);
        assertEquals(charset, lString.getCharsetName());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // try null values
        locale = null;
        value = null;
        charset = null;
        
        // null locale: Locale.getDefault() is used
        lString.setLocale(locale);
        assertEquals(Locale.getDefault(), lString.getLocale());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // null charset: LocalizedStringImpl.DEFAULT_CHARSET_NAME is used
        lString.setCharsetName(charset);
        assertEquals(LocalizedStringImpl.DEFAULT_CHARSET_NAME, lString.getCharsetName());
        assertTrue(lString.isModified());
        lString.setModified(false);
        
        // null value: null is used
        lString.setValue(value);
        assertEquals(value, lString.getValue());
        assertTrue(lString.isModified());
        lString.setModified(false);
    }
}
