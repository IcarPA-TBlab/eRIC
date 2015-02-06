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

import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


class ClassificationSchemeDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ClassificationSchemeDAO.class);
    
    /**
     * Use this constructor only.
     */
    ClassificationSchemeDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "ClassScheme";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    
    protected void deleteComposedObjects(Object object)  throws RegistryException {
        ClassificationSchemeType ro = (ClassificationSchemeType)object;
            
        super.deleteComposedObjects(ro);
        
        ArrayList<String> parentIds = new ArrayList<String>();
        parentIds.add(ro.getId());

    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
            
        super.insertComposedObjects(object);
        
        ClassificationSchemeType scheme = (ClassificationSchemeType)object;
        
        ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
        classificationNodeDAO.setParent(scheme);
        classificationNodeDAO.insert(scheme.getClassificationNode());        
    }
    
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        ClassificationSchemeType scheme = (ClassificationSchemeType)ro;
            
        String stmtFragment = null;
               
        String isInternal = "'F'";

        if (scheme.isIsInternal()) {
            isInternal = "'T'";
        }

        String nodeType = null;
        
        if (scheme.getNodeType() != null) 
        	nodeType = scheme.getNodeType().toString();

        if (nodeType == null) {
            nodeType = "UniqueCode";
        }

        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO ClassScheme " +
                super.getSQLStatementFragment(ro) +
                    ", " + isInternal + ", '" + nodeType + 
                    "' ) ";            
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE ClassScheme SET " +
                super.getSQLStatementFragment(ro) +
                    ", isInternal=" + isInternal + 
                    ", nodeType='" + nodeType + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    

    // Should we make index on ClassifcationScheme in Classification????
    protected String checkClassificationReferences(java.sql.Connection conn,
        String schemeId) throws RegistryException {
        String classId = null;
        PreparedStatement stmt = null;

        try {
            String sql = "SELECT id FROM Classification WHERE " +
                "classificationScheme=? AND classificationScheme IS NOT NULL";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, schemeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                classId = rs.getString(1);
            }

            return classId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    // Should we make index on parent in ClassificationNode????
    protected String checkClassificationNodeReferences(
        java.sql.Connection conn, String schemeId) throws RegistryException {
        String nodeId = null;
        PreparedStatement stmt = null;

        try {
            String sql = "SELECT id FROM ClassificationNode WHERE parent=? AND parent IS NOT NULL";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, schemeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nodeId = rs.getString(1);
            }

            return nodeId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof ClassificationSchemeType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ClassficationSchemeExpected",
                        new Object[]{obj}));
            }

            ClassificationSchemeType ebClassificationSchemeType = (ClassificationSchemeType) obj;
            super.loadObject(ebClassificationSchemeType, rs);

            String isInternal = rs.getString("isInternal");

            if (isInternal.equals("T")) {
                ebClassificationSchemeType.setIsInternal(true);
            } else {
                ebClassificationSchemeType.setIsInternal(false);
            }

            String nodeType = rs.getString("nodeType");
            ebClassificationSchemeType.setNodeType(nodeType);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ClassificationSchemeType ebClassificationSchemeType = bu.rimFac.createClassificationSchemeType();
        
        return ebClassificationSchemeType;
    }
    
}
