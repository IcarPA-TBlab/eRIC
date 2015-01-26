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
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;


/**
 *
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class AffectedObjectDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(AffectedObjectDAO.class);

    /**
     * Use this constructor only.
     */
    AffectedObjectDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "AffectedObject";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
                        
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object object) throws RegistryException {
        //object must be an ObjectRef for an objects affected by parent event
        ObjectRefType affectedObject = (ObjectRefType)object;
            
        String stmtFragment = null;
        
        
        String id = affectedObject.getId();
        String home = affectedObject.getHome();
        
        if (home != null) {
            home = "'" + home + "'";
        }
        
        String eventId = ((AuditableEventType)parent).getId();
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO AffectedObject VALUES(" +
                    " '" + id + 
                    "', " + home + 
                    ", '" + eventId + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.cannotUpdateComposedObject"));
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(object);
        }
        
        return stmtFragment;
    }
    

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRefType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ObjectRefTypeExpected",
                        new Object[]{obj}));
            }

            ObjectRefType ro = (ObjectRefType) obj;
            ro.setId(rs.getString("id"));
            
            String home = rs.getString("home");
            if (home != null) {
                ro.setHome(home);
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
        ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
        
        return ebObjectRefType;
    }
    
    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "eventId";
    }
        
}
