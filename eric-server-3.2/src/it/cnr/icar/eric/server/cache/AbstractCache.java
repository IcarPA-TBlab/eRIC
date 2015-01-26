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

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.event.AuditableEventListener;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.registry.RegistryException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * The abstract base class for all server Cache classes.
 * 
 * @author Farrukh Najmi
 * @author Doug Bunting
 */
abstract class AbstractCache implements AuditableEventListener {
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(AbstractCache.class);

    /**
     * AuthenticationServiceImpl instance used when initializing a new
     * ServerRequestContext instance for cache-specific queries.  Since the
     * AuthenticationServiceImpl class is implemented as a singleton, need
     * only one for all AbstractCache (and sub class) instances.
     */
    protected static AuthenticationServiceImpl ac = null;
    protected static BindingUtility bu = BindingUtility.getInstance();

    /**
     * ObjectCache instance which contains all accessed registry objects
     * except classification schemes.
     */
    protected static ObjectCache objectCache = ObjectCache.getInstance();

    /**
     * PersistenceManager instance used when querying the database.  Since
     * the PersistenceManager class is implemented as a singleton, need
     * only one for all AbstractCache (and sub class) instances.
     */
    protected static PersistenceManager pm = null;

    protected String primeCacheEvent = "onCacheInit";
    protected boolean cacheIsPrimed = false;
    
    protected static CacheManager cacheMgr = null;
    protected Cache internalCache = null;

    protected AbstractCache()  {
        primeCacheEvent = RegistryProperties.getInstance()
            .getProperty("eric.server.cache.primeCacheEvent", "onCacheInit");
                
        if (cacheMgr == null) {
            try {
                cacheMgr = CacheManager.getInstance();
            } catch (CacheException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    /**
     * Update the protected variables for use of sub-classes.  Calls to
     * this method should be done as late as possible since both
     * getInstance() calls included will lock some databases (for example,
     * the default Derby embedded mode configuration).
     */
    protected static synchronized void initializeQueryVars(boolean wantAC) {
	if (null == pm) {
	    pm = PersistenceManagerFactory.getInstance().
		getPersistenceManager();
	}
	if (wantAC && null == ac) {
	    try {
		ac = AuthenticationServiceImpl.getInstance();
	    } catch (Exception e) {
		// AuthenticationServiceImpl likely was unable to load
		// users prior to dB load, ignore exception
	    }
	}
    }

    protected static ServerRequestContext getCacheContext(String contextId)
	throws RegistryException {

	ServerRequestContext context =
	    new ServerRequestContext(contextId, null);

	initializeQueryVars(true);
	if (null != ac) {
	    context.setUser(ac.registryOperator);
	}
	return context;
    }

    protected abstract void initialize();

    protected abstract void primeCache(ServerRequestContext context);
    
    protected void primeCacheOnFirstUse(ServerRequestContext context) {
        if (!cacheIsPrimed && primeCacheEvent.equalsIgnoreCase("onFirstUse")) {
            primeCache(context);
        }
    }
    
    @SuppressWarnings("static-access")
	protected RegistryObjectType getRegistryObjectInternal(ServerRequestContext
							   context,
							   String id,
							   String typeName)
	throws RegistryException {

        RegistryObjectType ro = null;

        typeName = bu.mapJAXRNameToEbXMLName(typeName);
        String tableName = Utility.getInstance().mapTableName(typeName);

        try {
            if (id != null) {
                String sqlQuery = "Select * from " + tableName + " WHERE UPPER(id) = ?";
                ArrayList<String> queryParams = new ArrayList<String>();
                queryParams.add(id.toUpperCase());
                List<?> results = executeQueryInternal(context, sqlQuery, queryParams, tableName);

                if (results.size() > 0) {
                    ro = (RegistryObjectType) results.get(0);
                }
                else {
                    ro = null;
                }
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RegistryException(e);
        }

        return ro;
    }
    
    protected List<?> executeQueryInternal(ServerRequestContext context,
					String sqlQuery,
                                        List<String> queryParams,
					String tableName)
	throws RegistryException {

        List<?> results = null;
	initializeQueryVars(false);

        ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnComposedObjects(true);
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);

		@SuppressWarnings("rawtypes")
		List objectRefs = new ArrayList();
		results = pm.executeSQLQuery(context,
				 sqlQuery,
		                             queryParams,
				 ebResponseOptionType,
				 tableName,
				 objectRefs);

        return results;
    }
    
    /**
     * Check if object exists in cache.
     */
    public boolean contains(String objectId) throws RegistryException {
        boolean found = false;
        
        try {
            found = (internalCache.get(objectId) != null);
        } catch (CacheException e) {
            throw new RegistryException(e);
        }
        return found;
    }
}
