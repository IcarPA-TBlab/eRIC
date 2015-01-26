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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;


/**
 * The top level manager that manges all aspects of event management in the registry. This includes listening for events, matching events to subscriptions and notifying subscribers when an event matching their Subscription occurs.
 *
 * @author Farrukh S. Najmi
 * @author Nikola Stojanovic
 */
public class EventManager implements AuditableEventListener {
    private static final Log log = LogFactory.getLog(EventManager.class);
    
    /** Creates a new instance of SubscriptionManager */
    protected EventManager() {
        try {
            subscriptionMatcher = new SubscriptionMatcher();
            notifier = new NotifierImpl();
        }
        catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.ConstructorFailed"), e);
        }
    }
    
    /*
     * Responds to an AuditableEvent. Called by the PersistenceManager.
     * Gets the subscriptions that match this event.
     * For each matching Subscription, sends notifications to subscribers
     * regarding this event.
     *
     * @see it.cnr.icar.eric.server.persistence.AuditableEventListener#onEvent(org.oasis.ebxml.registry.bindings.rim.AuditableEventType)
     */
    public void onEvent(ServerRequestContext context, AuditableEventType ae)  {
        try {
            //Make a new Context since this once has just ben committed
            //Fixes an infinite loop or deadlock when xxCache.onEvent(...) did other queries
            ServerRequestContext newContext = new ServerRequestContext("it.cnr.icar.eric.server.event.EventManager.onEvent", null);
            newContext.setUser(context.getUser());
            context = newContext;
            
            OnEventRunnable r = new OnEventRunnable(context, ae);
            new Thread(r, "it.cnr.icar.eric.server.event.EventManager#onEvent").start();
        } catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.ExceptionCaughtOnEvent"), e);
        }
        
    }
    
    private void onEventInternal(ServerRequestContext context, AuditableEventType ebAuditableEventType)  {
        
        try {
            javax.xml.bind.Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            //marshaller.marshal(ae, System.err);
            
            //Get the HashMap where keys are the Subscriptions that match this event
            //and values are the matchedObjects for that Subscription.
            HashMap<SubscriptionType, List<IdentifiableType>> subscriptionsMap = subscriptionMatcher.getMatchedSubscriptionsMap(context, ebAuditableEventType);

            //Process each matching Subscription
            Iterator<SubscriptionType> subscriptionsIter = subscriptionsMap.keySet().iterator();
            while (subscriptionsIter.hasNext()) {
                SubscriptionType subscription = subscriptionsIter.next();

                processSubscription(context, subscription, (subscriptionsMap.get(subscription)), ebAuditableEventType);
            }
        }
        catch (Exception e) {
            try {
                context.rollback();
            } catch (RegistryException e1) {
                log.error(e1, e1);
            }
            log.error(ServerResourceBundle.getInstance().getString("message.ExceptionCaughtOnEvent"), e);
        }
        
        try {
            context.commit();
        } catch (RegistryException e) {
            log.error(e, e);
        }
    }
    
    /*
     * The Runnable used to spawn calls to onEventInternal() method in a separate thread.
     */
    class OnEventRunnable implements Runnable {
        ServerRequestContext context = null;
        AuditableEventType ae = null;
         OnEventRunnable(ServerRequestContext context, AuditableEventType ae) {
             this.context = context;
             this.ae = ae;
         }
 
         public void run() {
             onEventInternal(context, ae);
         }
     }
    
    
    private void processSubscription(ServerRequestContext context, SubscriptionType subscription, 
        List<IdentifiableType> list, AuditableEventType ae) throws RegistryException {

        notifier.sendNotifications(context, subscription, list, ae);
                    
    }
            
    public synchronized static EventManager getInstance(){
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }
    
    /**
     * @link aggregationByValue
     */
    private SubscriptionMatcher subscriptionMatcher;
    
    /**
     * @link aggregationByValue
     */
    private NotifierImpl notifier;
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private EventManager _eventManager; */
    private static EventManager instance = null;
}
