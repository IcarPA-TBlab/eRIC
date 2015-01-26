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
package it.cnr.icar.eric.server.repository;

import it.cnr.icar.eric.common.HashCodeUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A composite id for a RepositoryItem.
 * Consists of the lid and versionName columns.
 *
 * @author Farrukh S. Najmi
 */
public class RepositoryItemKey implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6423091086967922346L;
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(RepositoryItemKey.class.getName()); 
    String lid;
    String versionName;
    
    /** Creates a new instance of RepositoryItemBean */
    public RepositoryItemKey() {
    }
    
    /** Creates a new instance of RepositoryItemBean */
    public RepositoryItemKey(String lid, String versionName) {
        this.lid = lid;
        this.versionName = versionName;
    }
    
    /**
     * Getter for property lid.
     * @return Value of property lid.
     */
    public java.lang.String getLid() {
        return lid;
    }
    
    /**
     * Setter for property lid.
     * @param lid New value of property lid.
     */
    public void setLid(java.lang.String lid) {
        this.lid = lid;
    }
    
    /**
     * Getter for property versionName.
     * @return Value of property versionName.
     */
    public java.lang.String getVersionName() {
        return versionName;
    }
    
    /**
     * Setter for property versionName.
     * @param versionName New value of property versionName.
     */
    public void setVersionName(java.lang.String versionName) {
        this.versionName = versionName;
    }
            
    public boolean equals(Object obj) {
        boolean isEqual = false;
        
        if ( this == obj ) return true;
        
        if (obj instanceof RepositoryItemKey) {
            RepositoryItemKey key = (RepositoryItemKey)obj;
            if ((key.lid.equals(lid)) && (key.versionName.equals(versionName))) {
                isEqual = true;
            }
        }
        
        return isEqual;
    }
        
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, lid );
        result = HashCodeUtil.hash( result, versionName );
        return result;
    }
    
    public String toString() {
        String str = super.toString();
        str = "RepositoryItemKey: lid=" + getLid() + " versionName=" + getVersionName();

        return str;
    }
}
