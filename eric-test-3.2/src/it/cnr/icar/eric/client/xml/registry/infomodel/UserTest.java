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
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.User;


import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for User
 *
 * @author <a href="mailto:wannes.sels@cronos.be">Wannes Sels</a>
 */
public class UserTest extends ClientTest {

    public UserTest(String testName) {
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
        TestSuite suite = new TestSuite(UserTest.class);
        return suite;
    }

    @SuppressWarnings("unchecked")
	public void testGetOrganization() throws Exception {                    
        // create a User
        User user = createUser("Farid");
        
        // create the default organization
        Organization org  = createOrganization("ScienceTeam");
        Collection<User> users = new ArrayList<User>();
        users.add(user);
        org.addUsers(users);
        Collection<Organization> orgs = new ArrayList<Organization>();
        orgs.add(org);
        @SuppressWarnings("unused")
		BulkResponse br = lcm.saveOrganizations(orgs); // publish to registry

        // Find the published organization
        Organization pubOrg = (Organization)bqm.getRegistryObject(org.getKey().getId(), LifeCycleManager.ORGANIZATION);
        assertNotNull("org not found after saving it.", pubOrg);
        
        //debug.add("Get the Users from this organization \n");
        users.clear();
        users.addAll(pubOrg.getUsers());
        assertNotNull("org.getUsers() returned null.", users);        
        assertTrue("org.getUsers() returned no users.", (users.size() > 0));
        
        if (!(users.contains((user)))) {
            System.err.println("users=" + users + " looking for=" + user);
        }
        assertTrue("Did not find my user in org.getUsers() after saving org.", (users.contains((user))));
        
        //debug.add("Now get the organization from the User \n");
        Organization testOrg = user.getOrganization();
        assertEquals("user.getOrganization() a different org.", testOrg, pubOrg);
        
        users.clear();
        users.addAll(testOrg.getUsers());
        assertNotNull("testOrg.getUsers() returned null.", users);        
        assertTrue("testOrg.getUsers() returned no users.", (users.size() > 0));        
        assertTrue("Did not find my user in testOrg.getUsers() after saving org.", (users.contains((user))));
    }
    
    public void testQueryEmailAddressFromUser() throws Exception {

        //based on demo data

        String FarrukhId = "urn:freebxml:registry:predefinedusers:farrukh";
        @SuppressWarnings("unused")
		LifeCycleManager lcm = getLCM();
        DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl)getDQM();

        //get the user
        User u = (User)dqm.getRegistryObject(FarrukhId);
        Assert.assertNotNull("User was not found when queried by id", u);

        //get the email addresses
        Collection<?> EmailAddresses = u.getEmailAddresses();
        Assert.assertNotNull("User must not have null for EmailAddresses",EmailAddresses);

        Assert.assertEquals ("User does not have expected number of Email Adresses",2,EmailAddresses.size());

    }
}