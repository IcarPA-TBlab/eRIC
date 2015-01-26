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
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.infomodel.RegistryObject;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;

/**
 * jUnit Test for Subscriptions.
 *
 * @author "Raavicharla, Praveena" <praavicharla@fgm.com>
 */
public class SubscriptionTest extends ClientTest {
    
    
    public SubscriptionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SubscriptionTest.class);
        return suite;
    }
    
    public void testSubmitSubscription() throws Exception {
        
        // the id of the selector is an adhoc query id from the demo DB.
        SubscriptionImpl subs = lcm.createSubscription("urn:freebxml:registry:demoDB:query:EpidemicAlertQuery");        
      
        @SuppressWarnings("unchecked")
		List<NotifyActionType> actionl = subs.getAction();
        NotifyActionType notifyAct = BindingUtility.getInstance().rimFac.createNotifyActionType();
        notifyAct.setEndPoint("urn:freebxml:registry:demoDB:serviceBinding:EpidemicAlertListenerServiceBinding");
        notifyAct.setNotificationOption(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_Objects);
        actionl.add(notifyAct);
        
        String id = subs.getKey().getId();
        List<Object> l = new ArrayList<Object>();
        l.clear();
        l.add(subs);
        lcm.saveObjects(l);

        RegistryObject ro = bqm.getRegistryObject(id);
        assertNotNull("The Subscription Object is not saved.", ro);
        
        l.clear();
        l.add(subs.getKey());
        lcm.deleteObjects(l);
        ro = bqm.getRegistryObject(id);
        assertNull("The Subscription Object is not deleted.", ro);
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
    
}
