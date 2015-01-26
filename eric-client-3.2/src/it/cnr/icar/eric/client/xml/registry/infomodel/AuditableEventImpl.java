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
import it.cnr.icar.eric.common.BindingUtility;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;


import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;

/**
 * Implements JAXR API interface named AuditableEvent.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class AuditableEventImpl extends RegistryObjectImpl
    implements AuditableEvent {
        
    //Add to AuditableEvent interface in JAXR 2.0??
    public static int eventUndeprecated = EVENT_TYPE_UNDEPRECATED;
    public static final int EVENT_TYPE_APPROVED = (eventUndeprecated + 1);
    public static final int EVENT_TYPE_DOWNLOADED = (EVENT_TYPE_APPROVED + 1);
    public static final int EVENT_TYPE_RELOCATED = (EVENT_TYPE_DOWNLOADED + 1);
    public static ArrayList<String> eventTypes;

    static {
        eventTypes = new ArrayList<String>();

        //Order is based on the numerical order of constants in AuditableEvent
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Created);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deprecated);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Updated);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Undeprecated);
                
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Approved);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Downloaded);
        eventTypes.add(BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated);
    };
        
        
    private RegistryObjectRef userRef = null;
    private Timestamp timestamp = null;
    private String eventType = null;
    private String requestId = null;
    
    //List of RegistryObjectRefs
    private List<RegistryObjectRef> affectedObjectRefs = new ArrayList<RegistryObjectRef>();
    private ArrayList<RegistryObject> affectedObjects = new ArrayList<RegistryObject>();

    public AuditableEventImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public AuditableEventImpl(LifeCycleManagerImpl lcm, AuditableEventType ebAE)
        throws JAXRException {
        super(lcm, ebAE);

        // Set the eventType
        eventType = ebAE.getEventType();

        List<ObjectRefType> _affectedObjectRefs = ebAE.getAffectedObjects().getObjectRef();
        Iterator<ObjectRefType> iter = _affectedObjectRefs.iterator();
        while (iter.hasNext()) {
            ObjectRefType ref = iter.next();            
            RegistryObjectRef registryObjectRef = new RegistryObjectRef(lcm, ref);
            affectedObjectRefs.add(registryObjectRef);
        }
        timestamp = new Timestamp(ebAE.getTimestamp().getMillisecond());
//        timestamp = new Timestamp(ebAE.getTimestamp().getTimeInMillis());
        userRef = new RegistryObjectRef(lcm, ebAE.getUser());
        requestId = ebAE.getRequestId();
    }

    //Possible addition to JAXR 2.0??
    public RegistryObjectRef getUserRef() throws JAXRException {
        return userRef;
    }

    public User getUser() throws JAXRException {
        User user = null;

        if (userRef != null) {
            user = (User) userRef.getRegistryObject("User");
        }

        return user;
    }

    public Timestamp getTimestamp() throws JAXRException {
        return timestamp;
    }

    /**
     * Deprecate in JAXR 2.0??
     */
    public int getEventType() throws JAXRException {
        int eventTypeAsInteger = eventTypes.indexOf(eventType);        

        return eventTypeAsInteger;
    }

    /**
     * Gets the event type as String.
     * Add to JAXR 2.0??
     * 
     */
    public String getEventType1() throws JAXRException {
        return eventType;
    }
    
    /**
     * An AuditableEvent is now associated with multiple objects
     * that were impacted in the same request. This method is left
     * for API compliance and backward compatibility.
     *
     * @retun the first RegistryObject in list of RegistryObjects affected by this AuditableEvent.
     *
     * @deprecated: Use getAffectedObjects instead.
     */
    public RegistryObject getRegistryObject() throws JAXRException {
        RegistryObject ro = null;
        
        List<RegistryObject> ros = getAffectedObjects();
        if (ros.size() > 0) {
            ro = ros.get(0);
        }
        
        return ro;
    }

    /**
     * Add to JAXR 2.0
     *
     * @return the List of objectReferences for objects affected by this event
     */
    public List<RegistryObjectRef> getAffectedObjectRefs() throws JAXRException {
        return affectedObjectRefs;
    }   
    
    /**
     * Add to JAXR 2.0
     *
     * @return the List of objects affected by this event
     */
    public List<RegistryObject> getAffectedObjects() throws JAXRException {
        if (affectedObjects.size() < 1) {
            Iterator<RegistryObjectRef> objRefItr = affectedObjectRefs.iterator();
            while (objRefItr.hasNext()) {
                RegistryObjectRef rof = objRefItr.next();
                RegistryObject ro = rof.getRegistryObject("RegistryObject");
                if (!(ro.getKey().getId()).equals(getId())) {
                    affectedObjects.add(ro);
                } 
            }
        }
        return affectedObjects;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getAffectedObjects());
        return composedObjects;
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

        AuditableEventType ebAuditableEventType = factory.createAuditableEventType();
        setBindingObject(ebAuditableEventType);

//		JAXBElement<AuditableEventType> ebAuditableEvent = factory.createAuditableEvent(ebAuditableEventType);

        return ebAuditableEventType;
    }

    public String getRequestId() {
        return requestId;
    }
}
