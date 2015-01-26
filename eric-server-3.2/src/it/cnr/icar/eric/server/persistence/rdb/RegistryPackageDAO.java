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

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;


/**
 * Package instances are RegistryEntries that group logically related
 * RegistryEntries together. One use of a Package is to allow operations to
 * be performed on an entire package of objects. For example all objects belonging
 * to a Package may be deleted in a single request.
 *
 * <p><DL><DT><B>Capability Level: 1 </B><DD>This interface is required to be implemented by JAXR Providers at or above capability level 1.</DL>
 *
 *
 */
class RegistryPackageDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(RegistryPackageDAO.class);

    /**
     * Use this constructor only.
     */
    public RegistryPackageDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "RegistryPackage";
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

        RegistryPackageType pkg = (RegistryPackageType)ro;
            
        String stmtFragment = null;
                        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO RegistryPackage " +
                super.getSQLStatementFragment(ro) +
                    " ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE RegistryPackage SET " +
                super.getSQLStatementFragment(ro) +
                    " WHERE id = '" + pkg.getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }    
    

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof RegistryPackageType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RegistryPackageExpected",
                        new Object[]{obj}));
            }

            @SuppressWarnings("unused")
			RegistryPackageType ebRegistryPackageType = (RegistryPackageType) obj;
            super.loadObject( obj, rs);

        } catch (Exception e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        RegistryPackageType ebRegistryPackageType = bu.rimFac.createRegistryPackageType();
        
        return ebRegistryPackageType;
    }
}
