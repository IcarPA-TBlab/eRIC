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
import it.cnr.icar.eric.common.jaxrpc.notificationListener.client.NotificationListenerPortType_Stub;
import it.cnr.icar.eric.common.jaxrpc.notificationListener.client.NotificationListenerSOAPService_Impl;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;

import javax.xml.soap.SOAPElement;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;

/**
 * Notifier used to send notifications to a SOAP-based web service when its Subscription matches a registry event.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SOAPServiceNotifier extends AbstractNotifier {
    
    protected void sendNotification(ServerRequestContext context, NotifyActionType notifyAction, 
        NotificationType notification, AuditableEventType ae) throws RegistryException {
        System.err.println("Sending notification to web service");
        
        try {
            //Get the ServiceBinding id that represents the endPoint
            String endPoint =  notifyAction.getEndPoint();                       

            //Now get the ServiceBinding and its acceessURI
            ServiceBindingType serviceBinding = 
                (ServiceBindingType)PersistenceManagerFactory.getInstance().
                getPersistenceManager().
                getRegistryObject(context, endPoint, "ServiceBinding");
            
            String accessURI = serviceBinding.getAccessURI();
            
            NotificationListenerPortType_Stub stub = (NotificationListenerPortType_Stub)
            (new NotificationListenerSOAPService_Impl().getNotificationListenerPort());
            (stub)._setProperty(
            javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, accessURI);
            
            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(notification, System.err);
            
            SOAPElement soapElem = BindingUtility.getInstance().getSOAPElementFromBindingObject(notification);
            
            String notificationOption =  notifyAction.getNotificationOption();

            if (notificationOption.equals(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_Objects)) {
                stub.onObjectsNotification(soapElem);
            }
            else if (notificationOption.equals(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_ObjectRefs)) {
                stub.onObjectRefsNotification(soapElem);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RegistryException(e);
        } 
        
    }
    
}
