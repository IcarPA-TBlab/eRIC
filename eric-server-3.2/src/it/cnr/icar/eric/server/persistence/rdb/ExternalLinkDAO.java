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

import it.cnr.icar.eric.common.exceptions.UnresolvedURLsException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.Utility;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 *
 * @see <{RegistryObject}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class ExternalLinkDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ExternalLinkDAO.class);

    /**
     * Use this constructor only.
     */
    ExternalLinkDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "ExternalLink";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
    
    protected void prepareToInsert(Object object) throws RegistryException {        
        ExternalLinkType ro = (ExternalLinkType)object;
        
        super.prepareToInsert(object);
        validateURI(ro);
    }
                
    protected void prepareToUpdate(Object object) throws RegistryException {        
        ExternalLinkType ro = (ExternalLinkType)object;
        
        super.prepareToUpdate(object);
        validateURI(ro);
    }
    
    protected void validateURI(ExternalLinkType object) throws RegistryException {        
        
        // check those ExternalLink with http url
        String check = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.ExternalLinkDAO.checkURLs");

        if (check.equalsIgnoreCase("true")) {
            ArrayList<ExternalLinkType> objs = new ArrayList<ExternalLinkType>();
            objs.add(object);
            ArrayList<Object> invalidExtLinks = Utility.getInstance().validateURIs(objs);

            if (invalidExtLinks.size() > 0) {
                throw new UnresolvedURLsException(invalidExtLinks);
            }
        }

    }
                
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        ExternalLinkType extLink = (ExternalLinkType)ro;
            
        String stmtFragment = null;
        String extURI = extLink.getExternalURI();        
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO ExternalLink " +
                super.getSQLStatementFragment(ro) +
                    ", '" + extURI + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE ExternalLink SET " +
                super.getSQLStatementFragment(ro) +
                    ", externalURI='" + extURI + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    


    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof ExternalLinkType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ExternalLinkTypeExpected",
                        new Object[]{obj}));
            }

            ExternalLinkType ebExternalLinkType = (ExternalLinkType) obj;
            super.loadObject( obj, rs);

            String externalURI = rs.getString("externalURI");
            ebExternalLinkType.setExternalURI(externalURI);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ExternalLinkType ebExternalLinkType = bu.rimFac.createExternalLinkType();
        
        return ebExternalLinkType;
    }
}
