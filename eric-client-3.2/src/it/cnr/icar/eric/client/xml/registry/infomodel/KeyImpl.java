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
import it.cnr.icar.eric.common.HashCodeUtil;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;



/**
 * Implements JAXR API interface named Key.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class KeyImpl implements Key, Comparable<Object> {
    private String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
    private boolean registryAssignedId = true;

    @SuppressWarnings("unused")
	private KeyImpl() {
    }

    public KeyImpl(LifeCycleManagerImpl lcm) {
    }

    public String getId() throws JAXRException {
        return id;
    }

    public void setId(String par1) throws JAXRException {
        id = par1;
        registryAssignedId = false;
    }

    public String toString() {
        if (id == null) {
            return super.toString();
        }

        return id;
    }
    
    /** Returns true if the object specified is a RegistryObjectImpl
      * with the same id.
      *
      * @param o
      *                The object to compare to.
      * @return
      *                <code>true</code> if the objects are equal.
      * @todo
      *                Do we need to ensure the object is the same type as this
      *                instance? For example, this instance could be a ServiceImpl
      *                and the object could be an ExternalLinkImpl. Could these have
      *                the same id?
      */
    public boolean equals(Object o) {
    	return (compareTo(o) == 0);
    	/*
        if (compareTo(o) == 0) {
            return true;
        } else {
            return false;
        }
        */
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, id);
        return result;
    }    
    
    /**
     * Compares two registries objects.
     * Consider adding Comparable to RegistryObject in JAXR 2.0??
     *
     * @return 0 (equal) is the id of the objects matches this objects id.
     * Otherwise return -1 (this object is less than arg o).
     */
    public int compareTo(Object o) {
        int result = -1;

        if (o instanceof Key) {
            try {
                Key key = (Key)o;
                String myId = getId();                
                String otherId = key.getId();
                result = myId.compareTo(otherId);
            } catch (JAXRException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    boolean isRegistryAssignedId() {
        return registryAssignedId;
    }
}
