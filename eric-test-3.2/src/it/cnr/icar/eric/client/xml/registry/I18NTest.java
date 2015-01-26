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


package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.common.ClientTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A jUnit TestCase to test the ebxmlrr provider I18N issues.
 * Uses a RegistryPackage as test material.
 *
 * @author Diego Ballve
 */
public class I18NTest extends ClientTest {
        
    /** The UUID for the temporary test object */
    private static String testPackageUUID = null;
    
    // The localized names to be tested
    static final String[] intlNames = {"testPackageUS",
                                       "testPackUK" ,
                                       "testPacag??????????????????",
                                       "testiP?????????k?????????g?????????"};

    // The locales to be tested
    static final String[][] locales = {{"en","us"},
                                       {"en","uk"},
                                       {"fr", null},
                                       {"fi", null}};

  
    public I18NTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(I18NTest.class);
        return suite;
    }

    // test methods 
    
    //
     public void testAddInternationalStringPackage() throws Exception {
    	RegistryPackage pkg;
        InternationalString names;
        
        if (locales[0][1] == null) {
            names = lcm.createInternationalString(
                new Locale(locales[0][0]), intlNames[0]);
        } else {
            names = lcm.createInternationalString(
                new Locale(locales[0][0], locales[0][1]), intlNames[0]);
        }

        for (int i = 1; i < locales.length; i++) {
            if (locales[i][1] == null) {
                names.addLocalizedString(lcm.createLocalizedString(
                    new Locale(locales[i][0]), intlNames[i]));
            } else {
                names.addLocalizedString(lcm.createLocalizedString(
                    new Locale(locales[i][0], locales[i][1]), intlNames[i]));
            }
        }

        pkg = lcm.createRegistryPackage(names);

        pkg.setDescription(lcm.createInternationalString("testPackage description"));
        ArrayList<RegistryPackage> al = new ArrayList<RegistryPackage>();
        al.add(pkg);
        System.err.println("\nSaving 1 RegistryPackage");
        BulkResponse br = lcm.saveObjects(al);
        assertTrue("testPackage creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        testPackageUUID = pkg.getKey().getId();
     }

    public void testAddedLocales() throws Exception {
        RegistryPackage pkg = null;
        try {
	        pkg = (RegistryPackage)bqm.getRegistryObject(testPackageUUID);
	    } catch (ClassCastException c) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' is not a RegistryPackage");
	    }
        if(pkg == null) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' doesn't exists in the registry");
        } else {
            for (int i = 0; i < locales.length; i++) {
                if (locales[i][1] == null) {
                    assertNotNull("Missing locale '" + locales[i][0] + "'!",
                        pkg.getName().getValue(new Locale(locales[i][0])));
                } else {
                    assertNotNull("Missing locale '" + locales[i][0] + "-" + locales[i][1] + "'!",
                        pkg.getName().getValue(new Locale(locales[i][0], locales[i][1])));
                }
            }
        }        
    }

    public void testAddedIntlNames() throws Exception {
        RegistryPackage pkg = null;
        try {
	        pkg = (RegistryPackage)bqm.getRegistryObject(testPackageUUID);
	    } catch (ClassCastException c) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' is not a RegistryPackage");
	    }
        if(pkg == null) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' doesn't exists in the registry");
        } else {
            for (int i = 0; i < locales.length; i++) {
                String value;
                if (locales[i][1] == null) {
                    value = pkg.getName().getValue(new Locale(locales[i][0]));
                } else {
                    value = pkg.getName().getValue(new Locale(locales[i][0], locales[i][1]));
                }

                assertTrue("Strings (encoding) messed up. Should be '" +
			   intlNames[i] + "': " + value,
			   value.equals(intlNames[i]));
            }
        }        
    }
    
    public void testRemoveInternationalStringPackage() throws Exception {
        RegistryPackage pkg = null;
        try {
	        pkg = (RegistryPackage)bqm.getRegistryObject(testPackageUUID);
	    } catch (ClassCastException c) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' is not a RegistryPackage");
	    }
        if(pkg == null) {
            fail("RegistryObject for UUID '" + testPackageUUID + "' doesn't exists in the registry");
        } else {
            ArrayList<Key> keys = new ArrayList<Key>(1);
            keys.add(pkg.getKey());
            BulkResponse br = lcm.deleteObjects(keys);
            assertTrue("testPackage removal Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }        
    }
    
 /**
   * Utility method to lookup registry object based on name and type
   */
  public RegistryObject findRegistryObject(String roName, String roType)
    throws Exception
  {
    RegistryObject retVal = null;
    
    roName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(roName);
    String query = "select ro.* from registryobject ro, name_ n where ro.id = n.parent and n.value = '" + roName + "'" +
                   " and ro.objecttype = '" + roType + "'";
                   
    Query q = dqm.createQuery(Query.QUERY_TYPE_SQL, query);
    BulkResponse br = dqm.executeQuery(q);
    if(br.getStatus() == BulkResponse.STATUS_SUCCESS)
    {
      Collection<?> c = br.getCollection();
      Iterator<?> it = c.iterator();
      if(it.hasNext())
        retVal = (RegistryObject)it.next();
    }
    else
    {
      throw new Exception("findRegistryObject Failed!");
    }
      
    return retVal;
  }
    
}
