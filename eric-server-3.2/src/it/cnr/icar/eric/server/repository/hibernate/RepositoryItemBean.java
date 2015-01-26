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
package it.cnr.icar.eric.server.repository.hibernate;

import it.cnr.icar.eric.server.repository.RepositoryItemKey;

import java.sql.Blob;

/**
 * A bean to be used to persist a RepositoryItem
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class RepositoryItemBean {
  
    //TODO: Optimization: Make this implement RepositoryItem directly and we
    //can used the bean cached by hibernate directly
    
    private Blob blobContent;
    private Blob blobSignature;
    private byte binaryContent[];
    private byte binarySignature[];
    private RepositoryItemKey key;
    
    /** Creates a new instance of RepositoryItemBean */
    public RepositoryItemBean() {
    }
    
    /**
     * Getter for property blobContent.
     * @return Value of property blobContent.
     */
    public java.sql.Blob getBlobContent() {
        return blobContent;
    }
    
    /**
     * Setter for property blobContent.
     * @param blobContent New value of property blobContent.
     */
    public void setBlobContent(java.sql.Blob blobContent) {
        this.blobContent = blobContent;
    }
    
    /**
     * Getter for property blobSignature.
     * @return Value of property blobSignature.
     */
    public java.sql.Blob getBlobSignature() {
        return blobSignature;
    }
    
    /**
     * Setter for property blobSignature.
     * @param blobSignature New value of property blobSignature.
     */
    public void setBlobSignature(java.sql.Blob blobSignature) {
        this.blobSignature = blobSignature;
    }
    
    /**
     * Getter for property binaryContent.
     * @return Value of property binaryContent.
     */
    public byte[] getBinaryContent() {
        return this.binaryContent;
    }
    
    /**
     * Setter for property binaryContent.
     * @param binaryContent New value of property binaryContent.
     */
    public void setBinaryContent(byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }
    
    /**
     * Getter for property binarySignature.
     * @return Value of property binarySignature.
     */
    public byte[] getBinarySignature() {
        return this.binarySignature;
    }
    
    /**
     * Setter for property binarySignature.
     * @param binarySignature New value of property binarySignature.
     */
    public void setBinarySignature(byte[] binarySignature) {
        this.binarySignature = binarySignature;
    }

    /**
     * Getter for property key.
     * @return Value of property lid.
     */
    public RepositoryItemKey getKey() {
        return key;
    }
    
    /**
     * Setter for property lid.
     * @param lid New value of property lid.
     */
    public void setKey(RepositoryItemKey key) {
        this.key = key;
    }        
}
