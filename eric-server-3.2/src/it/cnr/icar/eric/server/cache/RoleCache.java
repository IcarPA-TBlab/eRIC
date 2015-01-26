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
package it.cnr.icar.eric.server.cache;

import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.registry.RegistryException;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * The server side role cache for the registry server.
 * Cache for relationships between a small set of the objectCache content;
 * specifically, from User objects to SubjectRole ClassificationNodes using
 * their ids.
 * 
 * @author Farrukh Najmi
 * @author Doug Bunting
 */
class RoleCache extends AbstractCache {
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(RoleCache.class);
    
    private static RoleCache instance = null;            
    
    protected RoleCache() {
	cacheIsPrimed = true;
        
        //Key is userId, value is a Set of roles associated with userId. 
        //Roles are identified by ids to SubjectRole ClassificationNodes.
        internalCache = cacheMgr.getCache(RoleCache.class.getName());
    }
    
    public synchronized static RoleCache getInstance() {
        if (instance == null) {
            instance = new it.cnr.icar.eric.server.cache.RoleCache();
        }

        return instance;
    }
    
    /**
     * Initialize the cache, a no-op for this cache.
     */
    protected void initialize() {
    }

    /**
     * Prime the cache, a no-op for this cache.
     */
    protected void primeCache(ServerRequestContext context) {
    }
    
    public void putRoles(String userId, Set<String> roles) {
        Element elem = new Element(userId, (Serializable)roles);
        internalCache.put(elem);        
    }
        
    /**
     * Gets the roles associated with the user associated with specified
     * ServerRequestContext.
     */
    @SuppressWarnings("unchecked")
	public Set<String> getRoles(ServerRequestContext context)
	throws RegistryException {

        Set<String> roles = null;
        
        try {
            UserType user = context.getUser();
            if (user != null) {
                Element elem = internalCache.get(user.getId());
                if (elem == null) {
                    //Cache miss. Get from registry
                    //log.trace("RoleCache: cache miss for id: " + id);                            
                    roles = getRoles(context, user);
                    putRoles(user.getId(), roles);
                } else {
                    roles = (Set<String>)elem.getValue();
                }
            } else {
                roles = new HashSet<String>();
            }
        } catch (CacheException e) {
            throw new RegistryException(e);
        }
        return roles;
    }
    
    private Set<String> getRoles(ServerRequestContext context, UserType user)
	throws RegistryException {

        HashSet<String> roles = new HashSet<String>();
        List<ClassificationType> classifications = user.getClassification();

        Iterator<ClassificationType> iter = classifications.iterator();
        while (iter.hasNext()) {
            ClassificationType ebClassificationType = iter.next();
            String classificationNodeId =
		ebClassificationType.getClassificationNode();
            ClassificationNodeType node =
		(ClassificationNodeType)objectCache.
		getRegistryObject(context,
				  classificationNodeId,
				  "ClassificationNode");
	    // Note: ObjectNotFoundException thrown above when node missing.

	    if (node.getPath().
		startsWith("/urn:oasis:names:tc:ebxml-regrep:classificationScheme:SubjectRole/")) {
		roles.add(node.getPath());
            }
        }
        return roles;
    }            
    
    /**
     * Clear all affectedObjects in AuditableEvent from cache regardless of
     * the event type.
     */
    public void onEvent(ServerRequestContext context, AuditableEventType ae) {
        List<ObjectRefType> affectedObjects = ae.getAffectedObjects().getObjectRef();
        
        Iterator<ObjectRefType> iter = affectedObjects.iterator();
        while (iter.hasNext()) {
            ObjectRefType oref = iter.next();
            internalCache.remove(oref.getId());
        }        
    }
}
