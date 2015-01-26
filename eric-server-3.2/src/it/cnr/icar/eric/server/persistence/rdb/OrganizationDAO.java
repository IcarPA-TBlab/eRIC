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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 *
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 * @version
 */
class OrganizationDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(OrganizationDAO.class);

    /**
     * Use this constructor only.
     */
    OrganizationDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Organization";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    protected void deleteComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.deleteComposedObjects(ro);
        
        @SuppressWarnings("unused")
		String parentId = ro.getId();
        PostalAddressDAO postalAddressDAO = new PostalAddressDAO(context);
        postalAddressDAO.setParent(object);
        postalAddressDAO.deleteByParent();
        
        EmailAddressDAO emailAddressDAO = new EmailAddressDAO(context);
        emailAddressDAO.setParent(object);
        emailAddressDAO.deleteByParent();
        
        TelephoneNumberDAO telephoneNumberDAO = new TelephoneNumberDAO(context);
        telephoneNumberDAO.setParent(object);
        telephoneNumberDAO.deleteByParent();
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
        OrganizationType ro = (OrganizationType)object;
            
        super.insertComposedObjects(ro);
        
        String parentId = ro.getId();
        PostalAddressDAO postalAddressDAO = new PostalAddressDAO(context);
        postalAddressDAO.insert(parentId, ro.getAddress());
        
        EmailAddressDAO emailAddressDAO = new EmailAddressDAO(context);
        emailAddressDAO.insert(parentId, ro.getEmailAddress());
        
        TelephoneNumberDAO telephoneNumberDAO = new TelephoneNumberDAO(context);
        telephoneNumberDAO.insert(parentId, ro.getTelephoneNumber());
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        OrganizationType org = (OrganizationType)ro;
            
        String stmtFragment = null;
        String parentId = org.getParent();

        if (parentId != null) {
            parentId = "'" + parentId + "'";
        }

        String primaryContactId = org.getPrimaryContact();
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO Organization " +
                super.getSQLStatementFragment(ro) +
                    ", " + parentId + 
                    ", '" + primaryContactId + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE Organization SET " +
                super.getSQLStatementFragment(ro) +
                    ", parent=" + parentId + 
                    ", primaryContact='" + primaryContactId + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof OrganizationType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.OrganizationExpected",
                        new Object[]{obj}));
            }

            OrganizationType ebOrganizationType = (OrganizationType) obj;
            super.loadObject( obj, rs);

            String parentId = rs.getString("parent");

            if (parentId != null) {
                ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
                ebObjectRefType.setId(parentId);
                context.getObjectRefs().add(ebObjectRefType);
                ebOrganizationType.setParent(parentId);
            }

            String primaryContactId = rs.getString("primaryContact");

            ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
            ebObjectRefType.setId(primaryContactId);
            context.getObjectRefs().add(ebObjectRefType);
            ebOrganizationType.setPrimaryContact(primaryContactId);

            PostalAddressDAO postalAddressDAO = new PostalAddressDAO(context);
            postalAddressDAO.setParent(ebOrganizationType);
            List addresses = postalAddressDAO.getByParent();
            if ((addresses != null) && (addresses.size() > 0)) {
                ebOrganizationType.getAddress().addAll(addresses);
            }

            TelephoneNumberDAO telephoneNumberDAO = new TelephoneNumberDAO(context);
            telephoneNumberDAO.setParent(ebOrganizationType);
            List phones = telephoneNumberDAO.getByParent();
            if (phones != null) {
                ebOrganizationType.getTelephoneNumber().addAll(phones);
            }
            
            EmailAddressDAO emailAddressDAO = new EmailAddressDAO(context);
            emailAddressDAO.setParent(ebOrganizationType);
            List emails = emailAddressDAO.getByParent();
            if (emails != null) {
                ebOrganizationType.getEmailAddress().addAll(emails);
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
        OrganizationType ebOrganizationType = bu.rimFac.createOrganizationType();
        
        return ebOrganizationType;
    }
}
