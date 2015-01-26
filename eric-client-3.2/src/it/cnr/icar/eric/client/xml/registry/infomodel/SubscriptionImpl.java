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


import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Calendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.registry.JAXRException;


/**
 * Implements future JAXR API interface named Subscription.
 *
 * @author <a href="mailto:Paul.Sterk@Sun.COM">Paul Sterk</a>
 */
public class SubscriptionImpl extends RegistryObjectImpl {

    @SuppressWarnings("rawtypes")
	private List action = new ArrayList();
    private XMLGregorianCalendar endTime;
    private Duration notificationInterval;
    
    // Reference to an AdhocQuery object
    private RegistryObjectRef selector;
    private XMLGregorianCalendar startTime;
    
    public SubscriptionImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm); 
    }

    public SubscriptionImpl(LifeCycleManagerImpl lcm,
        SubscriptionType subscriptionObj) throws JAXRException {
        super(lcm, subscriptionObj);

        action = bu.getActionTypeListFromElements(subscriptionObj.getAction());
        endTime = subscriptionObj.getEndTime();
        notificationInterval = subscriptionObj.getNotificationInterval();
        selector = new RegistryObjectRef(lcm, subscriptionObj.getSelector());
        startTime = subscriptionObj.getStartTime();
    }
    
    @SuppressWarnings("rawtypes")
	public List getAction() {
        return action;
    }
    
    public Calendar getEndTime() {
        return endTime.toGregorianCalendar();
    }
    
    public void setEndTime(Calendar endTime) throws DatatypeConfigurationException {
    	// GregorianCalendar is a subclass of abstract Calendar and can be used to instantiate XMLGregorianCalendar
        this.endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)endTime);
    }
    
    public String getNotificationInterval() {
        return (notificationInterval == null) ? "P1D" : notificationInterval.toString();
    }
           
    public void setNotificationInterval(String notificationInterval) throws DatatypeConfigurationException {
        this.notificationInterval = DatatypeFactory.newInstance().newDuration(notificationInterval);
    }
  
    public RegistryObjectRef getSelector() {
        return selector;
    }
    
    public void setSelector(RegistryObjectRef selector) {
        this.selector = selector;
    }
    
    public Calendar getStartDate() {
        return startTime.toGregorianCalendar();
    }
    
    public void setStartDate(Calendar startTime) throws DatatypeConfigurationException {
    	// GregorianCalendar is a subclass of abstract Calendar and can be used to instantiate XMLGregorianCalendar
        this.startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)startTime);
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        SubscriptionType ebSubscriptionType = factory.createSubscriptionType(); 
		setBindingObject(ebSubscriptionType);
		
//		JAXBElement<SubscriptionType> ebSubscription = factory.createSubscription(ebSubscriptionType);
		
		return ebSubscriptionType;

    }
    
    
    @SuppressWarnings("unchecked")
	protected void setBindingObject(SubscriptionType ebSubscriptionType)
        throws JAXRException {
        super.setBindingObject(ebSubscriptionType);
        
        bu.getActionTypeListFromElements(ebSubscriptionType.getAction()).addAll(action);
        ebSubscriptionType.setSelector(selector.getId());
        ebSubscriptionType.setStartTime(startTime);
        ebSubscriptionType.setEndTime(endTime);
        ebSubscriptionType.setNotificationInterval(notificationInterval);
    }
}
