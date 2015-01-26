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

import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 * Concept instances are used to define tree structures where each node in the tree is a Concept. Such
 * classificationNode trees constructed with Concepts are used to define classificationNode schemes or ontologies.
 *
 * Note this interface used to be called ClassificationNode.
 *
 * @see <{ClassificationNode}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class ClassificationNodeDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ClassificationNodeDAO.class);

    boolean codeCannotBeNull = Boolean.valueOf(RegistryProperties.getInstance()
        .getProperty("eric.persistence.rdb.ClassificationNodeDAO.CodeCannotBeNull", "true")).booleanValue();
    
    /**
     * Use this constructor only.
     */
    ClassificationNodeDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "ClassificationNode";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    protected void deleteComposedObjects(Object object)  throws RegistryException {
            
        super.deleteComposedObjects(object);
        
        if (object instanceof RegistryObjectType) {
            RegistryObjectType ro = (RegistryObjectType)object;
            ArrayList<String> parentIds = new ArrayList<String>();
            parentIds.add(ro.getId());
        }
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
            
        super.insertComposedObjects(object);
        
        if (object instanceof RegistryObjectType) {
            RegistryObjectType ro = (RegistryObjectType)object;
            ClassificationNodeType node = (ClassificationNodeType)ro;

            ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
            classificationNodeDAO.setParent(node);
            classificationNodeDAO.insert(node.getClassificationNode());   
        }
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        ClassificationNodeType classificationNode = (ClassificationNodeType)ro;
            
        String stmtFragment = null;
               
        String code = classificationNode.getCode();
        
        String parentId = classificationNode.getParent();
        if (parentId == null) {
            if (parent != null) {
                parentId = ((RegistryObjectType)parent).getId();
                classificationNode.setParent(parentId);
            }            
        }
        
        if (parentId != null) {
            parentId = "'" + parentId + "'";
        }
        
        String path = classificationNode.getPath();
        if (action != DAO_ACTION_DELETE) {
            if (path == null) {
                path = generatePath(classificationNode);
                classificationNode.setPath(path);
            }
        }
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO ClassificationNode " +
                super.getSQLStatementFragment(ro) +
                    ", '" + code + 
                    "', " + parentId + 
                    ", '" + path +
                    "' ) ";            
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE ClassificationNode SET " +
                super.getSQLStatementFragment(ro) +
                    ", code='" + code + 
                    "', parent=" + parentId + 
                    ", path='" + path + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    /**
     * Generate the path for the specified ClassificationNode. The path starts with a slash, followed
     * by the id of its ClassificationScheme, if any, which is followed by codes
     * of the intermidate ClassificationNodes and ends with the code of this
     * ClassificationNode. If any of the ClassificationNode does not have code,
     * empty string is appended for that ClassificationNode.
     * <br>
     */
    public String generatePath(ClassificationNodeType node) throws RegistryException {
        String path = null;

        String parentId = node.getParent();
        RegistryObjectType _parent = null;
        if (parentId == null) {
		    //It is possible for a ClassificationNode to have nu parent scheme
		    //in this case the path is just "/<id of node>"
		    path = "/" + node.getId();
		} else {
		    /*
		     * Build the path of the ClassificationNode in the form of
		     * /.../.../.. . It is composed of ClassificationScheme 's id as root and
		     * then the codes of enclosing ClassificationNodes
		     */

		    if (_parent == null) {
		        //Try and get _parent from request

		        _parent = (RegistryObjectType)context.getSubmittedObjectsMap().get(parentId);
		        if (_parent == null) {
		            //Try and get parent from database
		            PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
		            ObjectRefType ebParentObjectRefType = bu.rimFac.createObjectRefType();
		            ebParentObjectRefType.setId(parentId);
		            _parent = pm.getRegistryObject(context, ebParentObjectRefType);

		            if (_parent == null) {
		                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.couldNotFindParent",
		                        new Object[]{parentId, node.getId(), node.getCode()}));
		            }
		        }
		    }

		    String parentPath = null;
		    if (_parent instanceof ClassificationSchemeType) {
		        parentPath = null;
		    } else if (_parent instanceof ClassificationNodeType) {               
		        parentPath = ((ClassificationNodeType)_parent).getPath();
		    } else {
		        throw new RegistryException(
		        ServerResourceBundle.getInstance().getString("message.ClassificationNodeNotMatch",
		                new Object[]{node.getId(), node.getCode(), _parent.getClass().getName()}));
		    }

		    if (parentPath == null) {
		        if (_parent instanceof ClassificationSchemeType) {
		            parentPath = "/" + _parent.getId();
		        } else if (_parent instanceof ClassificationNodeType) {               
		            parentPath = generatePath((ClassificationNodeType)_parent);
		            ((ClassificationNodeType)_parent).setPath(parentPath);
		        }
		    }

		    String code = node.getCode();
		    if (code == null) {
		        String msg = ServerResourceBundle.getInstance().getString("message.ClassificationNodeNotMatch", new Object[]{node.getId()});
		        if (codeCannotBeNull) {
		            throw new RegistryException(msg);
		        } else {
		            log.warn(msg);
		        }
		    }
		    path = parentPath + "/" + code;
		    node.setPath(path);
		}

        return path;
    }

    // Should we make index on parent in ClassificationNode????
    protected String checkClassificationNodeReferences(
        java.sql.Connection conn, String nodeId) throws RegistryException {
        String referencingNodeId = null;
        PreparedStatement stmt = null;

        try {
            String sql = "SELECT id FROM ClassificationNode WHERE " +
                "parent=? AND parent IS NOT NULL";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, nodeId);
            
            ResultSet rs = stmt.executeQuery();
            log.trace(sql);

            if (rs.next()) {
                referencingNodeId = rs.getString(1);
            }

            return referencingNodeId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof ClassificationNodeType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ClassificationNodeExpected",
                        new Object[]{obj}));
            }

            ClassificationNodeType ebClassificationNodeType = (ClassificationNodeType) obj;
            super.loadObject( obj, rs);

            String code = rs.getString("code");
            ebClassificationNodeType.setCode(code);

            String parent = rs.getString("parent");

            if (parent != null) {
                ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
                ebObjectRefType.setId(parent);
                context.getObjectRefs().add(ebObjectRefType);
                ebClassificationNodeType.setParent(parent);
            }

            String path = rs.getString("path");
            ebClassificationNodeType.setPath(path);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ClassificationNodeType ebClassificationNodeType = bu.rimFac.createClassificationNodeType();
        
        return ebClassificationNodeType;
    }
    
}
