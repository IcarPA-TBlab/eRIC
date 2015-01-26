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

import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.util.List;
import java.util.Set;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

/**
 * The server side master cache for registry server. Contains package private
 * slave caches that are for more specific purposes. This cache is the
 * <i>main</i> public interface for the server-side caches. (The QueryCache is
 * currently separately accessed.)
 * 
 * @author Farrukh Najmi
 * @author Doug Bunting
 */
public class ServerCache extends AbstractCache implements Runnable {
	private static ServerCache instance = null;
	private long primeCacheDelay = 60000;

	private static RoleCache roleCache = RoleCache.getInstance();

	private static ClassificationSchemeCache schemeCache = ClassificationSchemeCache.getInstance();

	private boolean initialized = false;

	protected ServerCache() {
		@SuppressWarnings("unused")
		long primeCacheDelay = Long.parseLong(RegistryProperties.getInstance().getProperty(
				"eric.server.cache.primeCacheDelay", "60000"));
	}

	public synchronized static ServerCache getInstance() {
		if (instance == null) {
			instance = new ServerCache();
		}

		return instance;
	}

	public synchronized void initialize() {
		// Ensure no such thread is already running. The servlet init()
		// method is called twice for some reason. Doesn't cause any real
		// harm but multiple initializing threads confuses the log.
		if (!initialized) {
			initialized = true;

			Thread preloader = new Thread(this, ServerCache.class.getName() + "#initialize");
			preloader.start();
		}
	}

	/**
	 * Prime the cache, a no-op for this cache.
	 */
	protected void primeCache(ServerRequestContext context) {
		// No further priming, done in slave caches
	}

	/**
	 * All of the Runnable interface we need. Used to make calls to various
	 * cache instance initialize() methods in a separate thread.
	 */
	public void run() {
		try {
			initializeInternal();
		} catch (Exception e) {
			// Ensure this thread does not end in a violent death, any
			// errors should already have been logged.
		}
	}

	/**
	 * Initializes the cache. Calls initialize() on slave caches.
	 */
	private void initializeInternal() {
		try {
			Thread.sleep(primeCacheDelay);
		} catch (InterruptedException e) {
			// do nothing
		}
		objectCache.initialize();
		roleCache.initialize();
		schemeCache.initialize();
	}

	/**
	 * Check if object exists in cache.
	 */
	public boolean contains(String objectId) throws RegistryException {
		boolean containsObject = false;

		// All objects except those in scheme cache must be in object cache
		if (schemeCache.contains(objectId) || objectCache.contains(objectId)) {
			containsObject = true;
		}

		return containsObject;
	}

	/**
	 * Gets the singleton Registry instance for this registry.
	 */
	public RegistryType getRegistry(ServerRequestContext context) throws RegistryException {

		return objectCache.getRegistry(context);
	}

	/**
	 * Returns a RegistryObject of type 'objectType' with specified id.
	 * 
	 * @param context
	 *            ServerRequestContext w/in which dB queries should occur
	 * @param id
	 *            a URN that identifies the desired object
	 * @param objectType
	 *            the desired object type (string name)
	 * @return RegistryObject
	 * @throws ObjectNotFoundException
	 *             if (id,objectType) not found.
	 * @throws RegistryException
	 *             if other RegistryException happens.
	 */
	public RegistryObjectType getRegistryObject(ServerRequestContext context, String id, String objectType)
			throws RegistryException {

		RegistryObjectType result;

		// ??This is inefficient to do 2 queries. Need to fix this sometime.
		List<?> schemes = null;
		String tableName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType);
		if ((tableName.equalsIgnoreCase("ClassScheme")) || (tableName.equalsIgnoreCase("RegistryObject"))) {
			schemes = schemeCache.getClassificationSchemesById(context, id);
		}

		if (null == schemes || 0 == schemes.size()) {
			result = objectCache.getRegistryObject(context, id, objectType);
		} else {
			result = (RegistryObjectType) schemes.get(0);
		}
		return result;
	}

	/**
	 * Gets the roles associated with the user associated with specified
	 * ServerRequestContext.
	 */
	public Set<?> getRoles(ServerRequestContext context) throws RegistryException {

		return roleCache.getRoles(context);
	}

	/**
	 * Gets all schemes. Note that descendent nodes of schemes up to the depth
	 * level that is configured for the cache may also be loaded into the object
	 * cache.
	 */
	public List<?> getAllClassificationSchemes(ServerRequestContext context) throws RegistryException {

		return schemeCache.getAllClassificationSchemes(context);
	}

	/**
	 * Gets schemes that match id pattern. Note that descendent nodes of schemes
	 * up to the depth level that is configured for the cache may also be loaded
	 * into the object cache.
	 */
	public List<?> getClassificationSchemesById(ServerRequestContext context, String idPattern) throws RegistryException {

		return schemeCache.getClassificationSchemesById(context, idPattern);
	}

	/**
	 * This method is used to retrieve a ClassificationNode based on its path.
	 * Note: if an invalid path is passed to this method, this method will
	 * return null
	 * 
	 * @param path
	 *            A String containing the ClassificationNodeType path
	 * @return A ClassificationNodeType instance that matches the path
	 */
	public ClassificationNodeType getClassificationNodeByPath(String path) throws RegistryException {
		return schemeCache.getClassificationNodeByPath(path);
	}

	/**
	 * Gets ClassificationNodes whose parent's id matches the specified id.
	 * 
	 * @param parentId
	 *            The id of parent ClassificationNode or ClassificationScheme.
	 *            Must be an exact id rather than an id pattern.
	 */
	public List<?> getClassificationNodesByParentId(ServerRequestContext context, String parentId)
			throws RegistryException {

		return schemeCache.getClassificationNodesByParentId(context, parentId);
	}

	/**
	 * Relay event to all slave caches so they can sync.
	 */
	public void onEvent(ServerRequestContext context, AuditableEventType ae) {
		objectCache.onEvent(context, ae);
		roleCache.onEvent(context, ae);
		schemeCache.onEvent(context, ae);
	}
}
