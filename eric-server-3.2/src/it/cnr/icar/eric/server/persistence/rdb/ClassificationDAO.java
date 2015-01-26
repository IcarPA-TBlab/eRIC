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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


class ClassificationDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ClassificationDAO.class);

    /**
     * Use this constructor only.
     */
    ClassificationDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Classification";
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

        ClassificationType classification = (ClassificationType)ro;
            
        String stmtFragment = null;
               
        String classificationNodeId = classification.getClassificationNode();

        if (classificationNodeId != null) {
            classificationNodeId = "'" + classificationNodeId + "'";
        }

        String schemeId = classification.getClassificationScheme();

        if (schemeId != null) {
            schemeId = "'" + schemeId + "'";
        }

        String classifiedObjectId = classification.getClassifiedObject();

        String nodeRep = classification.getNodeRepresentation();

        if (nodeRep != null) {
            nodeRep = "'" + nodeRep + "'";
        }
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO Classification " +
                super.getSQLStatementFragment(ro) +
                    ", " + classificationNodeId + ", " + schemeId + 
                    ", '" + classifiedObjectId + 
                    "', " + nodeRep +
                    " ) ";            
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE Classification SET " +
                super.getSQLStatementFragment(ro) +
                    ", classificationNode=" + classificationNodeId + 
                    ", classificationScheme=" + schemeId + 
                    ", classifiedObject='" + classifiedObjectId + 
                    "', nodeRepresentation=" + nodeRep +                     
                    " WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "classifiedObject";
    }
    
    protected void loadObject( Object obj, ResultSet resultSet) throws RegistryException {
        try {
            if (!(obj instanceof ClassificationType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ClassificationExpected",
                        new Object[]{obj}));
            }

            ClassificationType ebClassificationType = (ClassificationType) obj;
            super.loadObject( ebClassificationType, resultSet);

            ObjectRefType ebObjectRefType = null;

            String classificationNodeId = resultSet.getString("classificationNode");

            if (classificationNodeId != null) {
                ebObjectRefType = bu.rimFac.createObjectRefType();
                ebObjectRefType.setId(classificationNodeId);
                context.getObjectRefs().add(ebObjectRefType);
                ebClassificationType.setClassificationNode(classificationNodeId);
            }

            String classificationSchemeId = resultSet.getString("classificationScheme");

            if (classificationSchemeId != null) {
                ebObjectRefType = bu.rimFac.createObjectRefType();
                ebObjectRefType.setId(classificationSchemeId);
                context.getObjectRefs().add(ebObjectRefType);
                ebClassificationType.setClassificationScheme(classificationSchemeId);
            }

            String classifiedObjectId = resultSet.getString("classifiedObject");

            ebObjectRefType = bu.rimFac.createObjectRefType();
            ebObjectRefType.setId(classifiedObjectId);
            context.getObjectRefs().add(ebObjectRefType);
            ebClassificationType.setClassifiedObject(classifiedObjectId);

            String nodeRep = resultSet.getString("nodeRepresentation");
            ebClassificationType.setNodeRepresentation(nodeRep);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ClassificationType ebClassificationType = bu.rimFac.createClassificationType();
        
        return ebClassificationType;
    }
    
}
