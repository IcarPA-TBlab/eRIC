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

package it.cnr.icar.eric.server.event;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;

import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 * @author Tony Graham / Sun Microsystems
 */
public class EmailNotifierTest extends ServerTest {
    private EmailNotifier emailNotifier = new EmailNotifier();
    
    String smtpAddress;
    String fromAddress;
    String recipient;
    
    /**
     * Constructor for EmailNotifierTest
     *
     * @param name
     */
    public EmailNotifierTest(String name) {
        super(name);
        
        smtpAddress = RegistryProperties.getInstance().
        getProperty("eric.server.event.EmailNotifier.smtp.host");
        fromAddress = RegistryProperties.getInstance().
        getProperty("eric.server.event.EmailNotifier.smtp.from");
        recipient = RegistryProperties.getInstance().
        getProperty("eric.server.event.EmailNotifierTest.recipient");
    }
    
    @SuppressWarnings({ "static-access" })
	public void testSendNotification() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("EmailNotifierTest.testSendNotification", null);
            NotifyActionType notifyAction = BindingUtility.getInstance().rimFac.createNotifyActionType();

            notifyAction.setEndPoint("mailto:" + recipient);

            // Use ExternalLinkType just because it's simple to fill.
            ExternalLinkType el = BindingUtility.getInstance().rimFac.createExternalLinkType();

            el.setExternalURI("testSendNotification");
            el.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());

            ArrayList<JAXBElement<? extends IdentifiableType>> objectList = new ArrayList<JAXBElement<? extends IdentifiableType>>(1);

            objectList.add(BindingUtility.getInstance().rimFac.createExternalLink(el));

            // Copied from AbstractNotifier.java
            NotificationType notification =
                BindingUtility.getInstance().rimFac.createNotificationType();
            RegistryObjectListType roList =
                BindingUtility.getInstance().rimFac.createRegistryObjectListType();
            roList.getIdentifiable().addAll(objectList);
            notification.setRegistryObjectList(roList);

            // End of code copied from AbstractNotifier.java

            notification.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
            notification.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            // Set subscription id to one created in DemoDB target
            notification.setSubscription("urn:freebxml:registry:demoDB:subscription:EpidemicAlert");
            
            AuditableEventType ae = BindingUtility.getInstance().rimFac.createAuditableEventType();
            ae.setEventType(bu.CANONICAL_EVENT_TYPE_ID_Updated);
            ae.setUser(ac.ALIAS_FARRUKH);

            emailNotifier.sendNotification(context, notifyAction, notification, ae);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }
    
    public static Test suite() {
        return new TestSuite(EmailNotifierTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
