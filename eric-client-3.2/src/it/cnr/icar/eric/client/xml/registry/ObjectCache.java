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
package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.exceptions.UnresolvedReferenceException;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * An cache for JAXR objects fetched from the registry.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ObjectCache {
    private RegistryServiceImpl service = null;
    private HashMap<String, Reference<RegistryObject>> idToReferenceMap = null;

    @SuppressWarnings("unused")
	private ObjectCache() {
    }

    public ObjectCache(RegistryServiceImpl service) {
        this.service = service;
        idToReferenceMap = new HashMap<String, Reference<RegistryObject>>(1024);
    }


    public Reference<?> getReference(String id, String objectType)
    throws JAXRException {
        // First attemp to get cached reference
        Reference<?> ref = idToReferenceMap.get(id);
        //Need to also check if ref.get() == null as referrant could have been GCed
        if ((ref == null) || (ref.get() == null)) {
            //Cache miss. Get from registry
            //System.err.println("ObjectCache: cache miss for id: " + id);
            DeclarativeQueryManager dqm = service.getDeclarativeQueryManager();
            @SuppressWarnings("unused")
			String tablename = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType);
            dqm.getRegistryObject(id, objectType);

            //executeQuery should have fetched object and put it in cache
            ref = idToReferenceMap.get(id);

            if (ref == null) {
                throw new UnresolvedReferenceException(
                    JAXRResourceBundle.getInstance().getString("message.error.Unresolved.ref.object.id",new Object[] {id}));
                //System.err.println("Unresolved reference for object with id: " + id);
            }
        } else {
            //System.err.println("ObjectCache: cache hit for id: " + id);
        }

        return ref;
    }

    private void putReference(String id, Reference<RegistryObject> ref) {
        idToReferenceMap.put(id, ref);
    }


    public void putRegistryObject(RegistryObject ro) throws JAXRException {
        putReference(ro.getKey().getId(), new SoftReference<RegistryObject>(ro));
    }

    /**
     * Returns a RegistryObject of type 'objectType' with id  equals to 'ids'.
     *
     * @param id desired UUID (string)
     * @param objectType the desired object type (string name)
     * @return RegistryObject
     * @throw JAXRException if (id,objectType) not found or other JAXRException happens.
     */
    public RegistryObject getRegistryObject(String id, String objectType)
    throws JAXRException {
        return (RegistryObject)getReference(id, objectType).get();
    }

    /**
     * Returns a Collection of RegistryObjects of type 'objectType' with id in 'ids'.
     *
     * @param ids Collection of UUIDs (strings)
     * @param objectType the desired object type (string name)
     * @return Collection of RegistryObjects
     * @throw JAXRException if (id,objectType) not found or other JAXRException happens.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getRegistryObjects(Collection ids, String objectType)
    throws JAXRException {
        // Get cached objects to 'result' and not cached ids to 'notCached'
        Collection result = new ArrayList(ids.size());
        Collection notCached = new ArrayList();
        Iterator itIds = ids.iterator();
        while (itIds.hasNext()) {
            String id = itIds.next().toString();
            Reference ref = idToReferenceMap.get(id);
            if ((ref != null) && (ref.get() != null)) {
                //Cache hit. Add it to result
                result.add(ref.get());
            } else {
                // Cache miss. add to notCached
                notCached.add(id);
            }
        }

        if (!notCached.isEmpty()) {
            // objects where not found in the cache. Fetch from registry.
            DeclarativeQueryManager dqm = service.getDeclarativeQueryManager();
            
//            String tablename = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType);
//            StringBuffer sb = new StringBuffer("SELECT ro.* FROM ");
//            sb.append(tablename).append(" ro WHERE id IN (");
//            Iterator idNotCached = notCached.iterator();
//            while (idNotCached.hasNext()) {
//                sb.append("'").append(idNotCached.next()).append("'");
//                if (idNotCached.hasNext()) {
//                    sb.append(", ");
//                }
//            }
//            sb.append(")");
//            Query query = dqm.createQuery(
//            Query.QUERY_TYPE_SQL, sb.toString());
            
            Query query = SQLQueryProvider.getRegistryObjects(dqm, objectType, notCached);
            		            
            dqm.executeQuery(query).getCollection().iterator();
        }

        //executeQuery should have fetched objects and put them in cache
        Iterator idNotCached = notCached.iterator();
        while (idNotCached.hasNext()) {
            String id = idNotCached.next().toString();
            Reference ref = idToReferenceMap.get(id);
            if (ref != null && ref.get() != null) {
                //Cache hit. Add it to result
                result.add(ref.get());
            } else {
                throw new JAXRException(
                    JAXRResourceBundle.getInstance().getString("message.error.Unresolved.ref.object.id",new Object[] {id}));
            }
        }

        return result;
    }

    /**
     * Checks if 'id' is cached.
     *
     * @param id String id to be tested.
     * @returns boolean true if object is cached. False otherwise.
     */
    public boolean isCached(String id) {
        Reference<?> ref = idToReferenceMap.get(id);
        //Need to also check if ref.get() == null as referrant could have been GCed
        return !((ref == null) || (ref.get() == null));
        /*
        if ((ref == null) || (ref.get() == null)) {
            return false;
        } else {
            return true;
        }
        */
    }

}
