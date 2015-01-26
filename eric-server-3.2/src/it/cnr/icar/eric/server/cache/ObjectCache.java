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

import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.registry.RegistryException;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

/**
 * The server side Object cache for registry server.
 * Contains all accessed registry objects except classification schemes.<br>
 * This cache imitates the TRANSACTION_NONE transaction isolation because
 * the objects are generally read from the database in the current context.
 * Updated but not committed objects are stored in the cache and will not
 * be removed if the transaction rolls back.
 * 
 * @author Farrukh Najmi
 * @author Doug Bunting
 */
class ObjectCache extends AbstractCache {  
    private static final Log log = LogFactory.getLog(ObjectCache.class);
        
    private RegistryType registry = null;
    
    private static ObjectCache instance = null;
    
    protected ObjectCache() {
        //Key is id, value is the RegistryObjectType
        internalCache = cacheMgr.getCache(ObjectCache.class.getName() );
    }
    
    public synchronized static ObjectCache getInstance() {
        if (instance == null) {
            instance = new ObjectCache();
        }

        return instance;
    }
    
    /**
     * Initializes the cache.
     */
    protected void initialize() {
	if (primeCacheEvent.equalsIgnoreCase("onCacheInit")) {
	    ServerRequestContext context = null;

	    try {
		context = getCacheContext("ObjectCache.initialize");
		primeCache(context);
	    } catch (RegistryException e) {
		// Ignore whatever caused getCacheContext() to fail.
	    } finally {
		try {
		    if (null != context) {
			// This cache never writes to dB so no need to commit
			context.rollback();
		    }
		} catch (RegistryException e) {
		    log.warn(e);
		}
	    }
	}
    }

    /**
     * Prime the cache, loading query, user, and registry objects.
     */
    @SuppressWarnings("static-access")
	protected void primeCache(ServerRequestContext context) {
	long startTime = 0;
	if (log.isTraceEnabled()) {
	    log.trace("primeCache: started");
	    startTime = System.currentTimeMillis();
	}

	// Avoid infinite recursion through getRegistryObject().
	cacheIsPrimed = true;

	try {
	    cacheStoredQueries(context);
	    cachePredefinedUsers(context);
	    getRegistryObject(context,
			      bu.CANONICAL_ROOT_FOLDER_ID,
			      "RegistryPackage");
	} catch (Exception e) {
	    // Exceptions during priming are most likely of the "table not
	    // found" ilk, especially during dB load.  In any case, no
	    // priming exception should prevent use of public methods from
	    // this class.
	}

	if (log.isTraceEnabled()) {
	    long endTime = System.currentTimeMillis();
	    log.trace("primeCache: ended. elapsedTime:" + (endTime-startTime));
	}
    }
    
    private void cacheStoredQueries(ServerRequestContext context)
	throws RegistryException {

        try {
            String sqlQuery = "Select q.* from AdhocQuery q";
            List<?> results = executeQueryInternal(context,
						sqlQuery,
                                                null,
						"AdhocQuery");
            
            Iterator<?> iter = results.iterator();
            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)iter.next();
                putRegistryObject(ro);
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(e);
        }
    }
    
    private void cachePredefinedUsers(ServerRequestContext context)
	throws RegistryException {

        getRegistryObject(context,
			  AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR,
			  "User_");
        getRegistryObject(context,
			  AuthenticationServiceImpl.ALIAS_REGISTRY_GUEST,
			  "User_");
        getRegistryObject(context,
			  AuthenticationServiceImpl.ALIAS_FARRUKH,
			  "User_");
        getRegistryObject(context,
			  AuthenticationServiceImpl.ALIAS_NIKOLA,
			  "User_");
    }
        
    void putRegistryObject(RegistryObjectType ro) throws RegistryException {
        if (ro != null) {
            Element elem = new Element(ro.getId(), ro);
            internalCache.put(elem);
        }
    }

    void putRegistryObjects(List<?> registryObjects) throws RegistryException {
        Iterator<?> iter = registryObjects.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj != null) {
                if (obj instanceof RegistryObjectType) {
                    RegistryObjectType ro = (RegistryObjectType)obj;
                    putRegistryObject(ro);
                } else {
                    throw new RegistryException(CommonResourceBundle.getInstance().getString("message.unexpectedObjectType", new Object[]{obj.getClass(), "RegistryObject"}));
                }
            }
        }
    }
            
    /**
     * Returns a RegistryObject of type 'objectType' with specified id.
     *
     * @param context ServerRequestContext w/in which dB queries should occur
     * @param id a URN that identifies the desired object
     * @param objectType the desired object type (string name)
     * @return RegistryObject
     * @throws ObjectNotFoundException if (id,objectType) not found.
     * @throws RegistryException if other RegistryException happens.
     */
    public RegistryObjectType getRegistryObject(ServerRequestContext context,
						String id,
						String objectType) 
	throws RegistryException {

        RegistryObjectType ro = null;
        
	primeCacheOnFirstUse(context);
        
        try {
            Element elem = internalCache.get(id);

            if (elem == null) {
                //Cache miss. Get from registry
                //log.trace("ObjectCache: cache miss for id: " + id);            
                ro =
                    getRegistryObjectInternal(context, id, objectType);
                if (ro == null) {
                    throw new ObjectNotFoundException(id, objectType);
                } else {
                    putRegistryObject(ro);
                }
            } else {
                //log.trace("ObjectCache: cache hit for id: " + id);
                ro = (RegistryObjectType)elem.getValue();
            }
        } catch (CacheException e) {
            throw new RegistryException(e);
        }
        
        return ro;
    }
    
    /**
     * Gets the singleton Registry instance for this registry.
     */
    public RegistryType getRegistry(ServerRequestContext context)
	throws RegistryException {

	primeCacheOnFirstUse(context);
        if (registry == null) {
	    // ??? This code is more complicated than done in primeCache()
	    // ??? to load a Registry object.  Should primeCache() set the
	    // ??? registry variable?  Is getRegistryObject(...)
	    // ??? sufficient?
            String sqlQuery = "SELECT * FROM Registry reg, AuditableEvent ae, AffectedObject ao WHERE ao.eventId =  ae.id AND ao.id = reg.id ORDER BY ae.timestamp_ ASC";
            List<?> results = executeQueryInternal(context, sqlQuery, null, "Registry");

            if (results.size() >= 1) {
                registry = (RegistryType) results.get(0);
            } else {
                throw new ObjectNotFoundException("%", "Registry");
            }
        }        
        
        return registry;
    }
    
    /**
     * Clear all affectedObjects in AuditableEvent from cache regardless of
     * event type.
     */
    public void onEvent(ServerRequestContext context, AuditableEventType ebAuditableEventType) {
        try {
            List<ObjectRefType> affectedObjects = ebAuditableEventType.getAffectedObjects().getObjectRef();

            primeCacheOnFirstUse(context);

            Iterator<ObjectRefType> iter = affectedObjects.iterator();
            while (iter.hasNext()) {
                //Remove affectedObject from objectCache
                ObjectRefType ebObjectRefType = iter.next();

                //If affectedObject is a composedObject then MUST remove it and its ancestors from cache
                //until ancestor not in cache or object removed not a composedObject (has no parentId)
                RegistryObjectType ebRegistryObjectType = context.getAffectedObjectsMap().get(ebObjectRefType.getId());
                
                //In case of setSTatus protocol it is possible for ro to not be
                //in affectedObjects but it still needs to be removed from cache.
                if (null == ebRegistryObjectType) {
                    internalCache.remove(ebObjectRefType.getId());
                }

                while (null != ebRegistryObjectType) {
                    String id = ebRegistryObjectType.getId();
                    
                    //Remove it whether it is in cache or not.
                    //Reason is that if it is not in cache but is composed 
                    //and parent is in cache then code below will remove parent as required.
                    internalCache.remove(id);

                    //Now see if it has an parent. If so it must be removed from cache too
                    @SuppressWarnings("static-access")
					String parentId = bu.getParentIdForComposedObject(ebRegistryObjectType);
                    if (parentId != null) {
                        //parent exists. Check if it is in cache.
                        Element elem = internalCache.get(parentId);
                        if (elem != null) {
                            //It is in cache. Get the ro for it and then loop to repeat process for parent
                            ebRegistryObjectType = (RegistryObjectType)elem.getValue();
                        } else {
                            //parent not in cache. So we are done.
                            break;
                        }
                    } else {
                        //Not a composed object (no parent). So we are done.
                        break;
                    }
                }            
            }
        } catch (CacheException e) {
            log.error(e);
            try {
                internalCache.removeAll();
            } catch (IOException e1) {
                log.error(e);
            }
        }        
    }
}
