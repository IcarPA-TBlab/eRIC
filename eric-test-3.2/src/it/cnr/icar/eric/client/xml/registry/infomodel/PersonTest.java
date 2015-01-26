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

import java.util.*;

import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.User;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;


import java.net.*;

/**
 * jUnit Test for Person
 *
 * @author <a href="Mohammed.Fazuluddin@Sun.com">Fazul</a>
 */
public class PersonTest extends ClientTest {

    PersonImpl personImpl;
    public PersonTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(PersonTest.class);
        return suite;
    }
    
    @SuppressWarnings({ "unused", "unchecked" })
	public void testAddTelephoneNumbers() throws Exception {
        TelephoneNumber tn1, tn2, tn3 = null;
        BulkResponse br= null;
        ArrayList<User> personsList = new ArrayList<User>();
        
        PersonImpl person = createPerson("TestPersonWithTN");

        tn1 = lcm.createTelephoneNumber();
        tn1.setType("Office Phone");
        
        tn2 = lcm.createTelephoneNumber();
        tn2.setType("Mobile Phone");

        tn3 = lcm.createTelephoneNumber();
        tn3.setType("Fax");
        
        List<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tn1);
        tels.add(tn2);
        tels.add(tn3);
        
        person.setTelephoneNumbers(tels);
        personsList.add(person);
        lcm.saveObjects(personsList);

        PersonImpl retrievePerson = (PersonImpl)dqm.getRegistryObject(person.getKey().getId());
        assertNotNull("person was not saved", retrievePerson);
        
        Collection<TelephoneNumber> retList = retrievePerson.getTelephoneNumbers("Fax");
        assertEquals("Count of Telephone Numbers returned from Person should be 1.", 1, retList.size());
    }
             
    public void testUrl() throws JAXRException,Exception{
        
        //Creating a new User
        User user =lcm.createUser();
        
        //Adding URL to User
        URL url =new URL("http://TheCoffeeBreak.com/JaneMDoe.html");
        user.setUrl(url);
             
        PersonName personName = lcm.createPersonName("Fazuluddin","","Mohammed");
        user.setPersonName(personName);

        //Adding PostalAddress to User
        Collection<PostalAddress> postalAddr = new ArrayList<PostalAddress>();
        PostalAddress postalAddress = lcm.createPostalAddress("1112","Longford","Bangalore","Karnataka","India","600292","String");
        postalAddr.add(postalAddress);
        user.setPostalAddresses(postalAddr);

        //Adding User Informaion to Collection Object and Saving to Registry
        Collection<User> userObject =new ArrayList<User>();
        userObject.add(user);
        lcm.saveObjects(userObject);
        
        User user1  = (User)bqm.getRegistryObject(user.getKey().getId(), LifeCycleManager.USER);
        
        //Testing with expected result
        assertEquals(url,user1.getUrl());
                    
    }

}
