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
package it.cnr.icar.eric.server.persistence.rdb;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.repository.RepositoryItemKey;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;

class ExtrinsicObjectDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ExtrinsicObjectDAO.class);

    RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                   .getRepositoryManager();
    /**
     * Use this constructor only.
     */
    ExtrinsicObjectDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "ExtrinsicObject";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        ExtrinsicObjectType extrinsicObject = (ExtrinsicObjectType)ro;
        String id = extrinsicObject.getId();
            
        String stmtFragment = null;
        String isOpaque = "'F'";

        if (extrinsicObject.isIsOpaque()) {
            isOpaque = "'T'";
        }

        String mimeType = extrinsicObject.getMimeType();

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        String contentVersionName = null;
        String contentVersionComment = null;
        
        //Set contentVersion only if RI submitted or if contentVersion
        //matches an existing RI version for same lid
        //
        //Note that contentVersion must be honoured if specified and matches an existing RO version
        //even when there is no RepositoryItem since the new EO
        //could reference an existing RO version.
        
        VersionInfoType contentVersionInfo = extrinsicObject.getContentVersionInfo();
        if (contentVersionInfo != null) {
            contentVersionName = contentVersionInfo.getVersionName();
            contentVersionComment = contentVersionInfo.getComment();
        }
        
        RepositoryItem roNew = (RepositoryItem)context.getRepositoryItemsMap().get(id);
        boolean setContentVersionInfo = false;
        if (roNew == null) {
            //Check if existing RI with same lid and contentVersion
            if ((contentVersionName != null) && (contentVersionName.length() > 0)) {
                RepositoryItemKey key = new RepositoryItemKey(extrinsicObject.getLid(), contentVersionName);
                setContentVersionInfo = rm.itemExists(key);
            }
        } else {
            setContentVersionInfo = true;
        }
        
        if (setContentVersionInfo) {
            if ((contentVersionName != null) && (contentVersionName.length() > 0)) {
                contentVersionName = "'" + contentVersionName + "'";
            } else {
                contentVersionName = null;
            }

            if ((contentVersionComment != null) && (contentVersionComment.length() > 0)) {
                contentVersionComment = "'" + contentVersionComment + "'";
            } else {
                contentVersionComment = null;
            }
        } else {
            contentVersionName = null;
            contentVersionComment = null;
        }
        
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO ExtrinsicObject " +
                super.getSQLStatementFragment(ro) +
                    ", " + isOpaque + 
                    ", '" + mimeType + 
                    "', " + contentVersionName + 
                    ", " + contentVersionComment +
                    " ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE ExtrinsicObject SET " +
                super.getSQLStatementFragment(ro) +
                    ", isOpaque=" + isOpaque + 
                    ", mimeType='" + mimeType + 
                    "', contentVersionName=" + contentVersionName +
                    ", contentVersionComment=" + contentVersionComment +
                    " WHERE id = '" + id + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof ExtrinsicObjectType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ExtrinsicObjectExpected",
                        new Object[]{obj}));
            }

            ExtrinsicObjectType ebExtrinsicObjectType = (ExtrinsicObjectType) obj;
            super.loadObject( obj, rs);

            String isOpaque = rs.getString("isOpaque");

            if (isOpaque.equals("T")) {
                ebExtrinsicObjectType.setIsOpaque(true);
            } else {
                ebExtrinsicObjectType.setIsOpaque(false);
            }

            String mimeType = rs.getString("mimeType");
            ebExtrinsicObjectType.setMimeType(mimeType);
            
            //Now set contentVersionInfo if either contentComment and contentVersionName are non-null
            //Make sure to not set contentVersionInfo if both contentComment and contentVersionName are null
            VersionInfoType contentVersionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
            String contentVersionName = rs.getString("contentVersionName");
            String contentComment = rs.getString("contentVersionComment");            
            
            if ((contentVersionName != null) || (contentComment != null)) {
                if (contentVersionName != null) {
                    contentVersionInfo.setVersionName(contentVersionName);
                }

                if (contentComment != null) {
                    contentVersionInfo.setComment(contentComment);
                }
                ebExtrinsicObjectType.setContentVersionInfo(contentVersionInfo);
            }
            
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
    	ExtrinsicObjectType ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
        
        return ebExtrinsicObjectType;
    }

}
