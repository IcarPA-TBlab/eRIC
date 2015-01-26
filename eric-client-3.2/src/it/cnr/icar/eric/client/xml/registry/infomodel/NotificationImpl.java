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
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;

import javax.xml.registry.InvalidRequestException;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;

import java.util.List;

import javax.xml.registry.JAXRException;


/**
 * Implements future JAXR API interface named Notification.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class NotificationImpl extends RegistryObjectImpl implements Notification {
    NotificationType ebNotification = null;
    
    public NotificationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public NotificationImpl(LifeCycleManagerImpl lcm,
        NotificationType ebNotification) throws JAXRException {
        super(lcm, ebNotification);
        this.ebNotification = ebNotification;
    }


    /**
     * Gets the objects in the notification.
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List getRegistryObjects() throws JAXRException {                
    	
		List<IdentifiableType> ebIdentifiableType = (List<IdentifiableType>) bu.getIdentifiableTypeList(ebNotification
				.getRegistryObjectList());
        return  JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, ebIdentifiableType, null);
    }
    
    /**
     * Gets the reference to the Subscription object that this Notification is for.
     */
    public RegistryObjectRef getSubscriptionRef() throws JAXRException {
        RegistryObjectRef subscriptionRef = new RegistryObjectRef(lcm, ebNotification.getSubscription());
        return subscriptionRef;
    }    

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de

        ObjectFactory factory = BindingUtility.getInstance().rimFac;

		NotificationType ebNotificationType = factory.createNotificationType(); 
		setBindingObject(ebNotificationType);
		
		JAXBElement<NotificationType> ebNotification = factory.createNotification(ebNotificationType);
		
		return ebNotification;
         **/
        
        throw new InvalidRequestException("Cannot save Notification via client API to registry. It can only be read from registry.");
    }

}
