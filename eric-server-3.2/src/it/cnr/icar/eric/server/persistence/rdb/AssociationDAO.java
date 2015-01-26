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
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


class AssociationDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(AssociationDAO.class);

    /**
     * Use this constructor only.
     */
    AssociationDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Association";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
    
    
    //TODO: Remove association confirmation in spec and replace with ACP
    //TODO: put associationConfirmation here if still in spec
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    @SuppressWarnings("unused")
	protected String getSQLStatementFragment(Object ro) throws RegistryException {

        AssociationType1 ass = (AssociationType1)ro;
            
        String stmtFragment = null;
                
        String srcId = ass.getSourceObject();
        String targetId = ass.getTargetObject();

        String associationType = ass.getAssociationType();

        if (associationType != null) {
            associationType = "'" + associationType + "'";
        }

        UserType sourceOwner = null;
        UserType targetOwner = null;

        //TODO: Illegal workaround until AE is fixed for V3
        UserType user = context.getUser();
        sourceOwner = user;
        targetOwner = user;
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO Association " +
                super.getSQLStatementFragment(ro) +
                    ", " + associationType + 
                    ",'" + srcId + 
                    "', '" + targetId + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE Association SET " +
                super.getSQLStatementFragment(ro) +
                    ", associationType=" + associationType + 
                    ", sourceObject='" + srcId + 
                    "', targetObject='" + targetId + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof AssociationType1)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.AssociationExpected",
                        new Object[]{obj}));
            }

            AssociationType1 ebAssociationType = (AssociationType1) obj;

            super.loadObject( ebAssociationType, rs);

            String associationType = rs.getString("associationType");
            ebAssociationType.setAssociationType(associationType);

            String sourceObjectId = rs.getString("sourceObject");

            ObjectRefType ebSourceObjectRefType = bu.rimFac.createObjectRefType();
            ebSourceObjectRefType.setId(sourceObjectId);
            context.getObjectRefs().add(ebSourceObjectRefType);
            ebAssociationType.setSourceObject(sourceObjectId);

            String targetObjectId = rs.getString("targetObject");
            ObjectRefType ebTargetObjectRefType = bu.rimFac.createObjectRefType();
            ebTargetObjectRefType.setId(targetObjectId);
            context.getObjectRefs().add(ebTargetObjectRefType);
            ebAssociationType.setTargetObject(targetObjectId);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        AssociationType1 ebAssociationType = bu.rimFac.createAssociationType1();
        
        return ebAssociationType;
    }
    
}
