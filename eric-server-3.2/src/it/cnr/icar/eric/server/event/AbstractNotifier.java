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
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.Iterator;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.rim.ActionType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

/**
 * Abstract base class for all Notifiers.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class AbstractNotifier implements Notifier {
    
    protected static BindingUtility bu = BindingUtility.getInstance();
        
	/**
	 * @see it.cnr.icar.eric.server.event.Notifier#sendNotifications
	 */
	public void sendNotifications(ServerRequestContext context, SubscriptionType ebSubscriptionType,
			List<IdentifiableType> list, AuditableEventType ae) throws RegistryException {

		// Iterate over actions and send notifications for each action
		List<ActionType> ebActionType = bu.getActionTypeListFromElements(ebSubscriptionType.getAction());
		Iterator<ActionType> actionsIter = ebActionType.iterator();

		while (actionsIter.hasNext()) {
			NotifyActionType action = (NotifyActionType) actionsIter.next();

			NotificationType notification = createNotification(ebSubscriptionType, list, action);

			sendNotification(context, action, notification, ae);
		}
	}

    protected abstract void sendNotification(ServerRequestContext context, NotifyActionType action, 
        NotificationType notification, AuditableEventType ae) throws RegistryException;
    
    private NotificationType createNotification(SubscriptionType subscription,
        List<IdentifiableType> list,
        ActionType action) throws RegistryException {
        
        NotificationType ebNotificationType = null;
        
        try {
        
            if (action instanceof NotifyActionType) {
                NotifyActionType ebNotifyActionType = (NotifyActionType)action;
                String notificationOption =  ebNotifyActionType.getNotificationOption();

				ebNotificationType = BindingUtility.getInstance().rimFac.createNotificationType();
				RegistryObjectListType ebRegistryObjectListType = null;

				if (notificationOption.equals(BindingUtility.CANONICAL_NOTIFICATION_OPTION_TYPE_ID_Objects)) {
                	// construct from ComplexTypes					
					ebRegistryObjectListType = BindingUtility.getInstance().getRegistryObjectListType((RegistryObjectType) list);
					
//                    ebRegistryObjectListType.getIdentifiable().addAll(matchedObjects);
                }
                else {
                	// cretae and addAll Elements
                	ebRegistryObjectListType = BindingUtility.getInstance().rimFac.createRegistryObjectListType();
                    ebRegistryObjectListType.getIdentifiable().addAll(BindingUtility.getInstance().getObjectRefsFromRegistryObjects(list));
                }

                ebNotificationType.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());            
                ebNotificationType.setObjectType(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Notification);            
                ebNotificationType.setRegistryObjectList(ebRegistryObjectListType);
                ebNotificationType.setSubscription(subscription.getId());
                //TODO: spec issue: Notification should have the eventType included.

            }
            else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.onlyNotifyActionsSupported"));
            }
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
        
        return ebNotificationType;
    }
}
