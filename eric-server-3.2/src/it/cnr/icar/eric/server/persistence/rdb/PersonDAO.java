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
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.oasis.ebxml.registry.bindings.rim.PersonType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


/**
 *
 * @author Farrukh S. Najmi
 * @version
 */
class PersonDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(PersonDAO.class);

    /**
     * Use this constructor only.
     */
    PersonDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Person";
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
        PersonType ro = (PersonType)object;
            
        super.insertComposedObjects(object);
        
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

        PersonType person = (PersonType)ro;
            
        String stmtFragment = null;
                
        PersonNameType personName = person.getPersonName();
        String firstName = personName.getFirstName();

        if (firstName != null) {
            firstName = "'" + firstName + "'";
        }

        String middleName = personName.getMiddleName();

        if (middleName != null) {
            middleName = "'" + middleName + "'";
        }

        String lastName = personName.getLastName();

        if (lastName != null) {
            lastName = "'" + lastName + "'";
        }
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName() + " " +
                super.getSQLStatementFragment(ro) +
                    ", " + firstName +  
                    ", " + middleName + 
                    ", " + lastName +
                    " ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE " + getTableName() + " SET " +
                super.getSQLStatementFragment(ro) +
                    ", personName_firstName=" + firstName + 
                    ", personName_middleName=" + middleName + 
                    ", personName_lastName=" + lastName + 
                    " WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }    

    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadObject( Object obj,
        ResultSet rs)
        throws RegistryException {
        try {
            if (!(obj instanceof org.oasis.ebxml.registry.bindings.rim.PersonType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.PersonExpected",
                        new Object[]{obj}));
            }

            PersonType person = (PersonType) obj;
            super.loadObject( obj, rs);

            String firstName = rs.getString("personName_firstName");
            String middleName = rs.getString("personName_middleName");
            String lastName = rs.getString("personName_lastName");

            PersonNameType ebPersonNameType = bu.rimFac.createPersonNameType();
            ebPersonNameType.setFirstName(firstName);
            ebPersonNameType.setMiddleName(middleName);
            ebPersonNameType.setLastName(lastName);

            person.setPersonName(ebPersonNameType);

            PostalAddressDAO postalAddressDAO = new PostalAddressDAO(context);
            postalAddressDAO.setParent(person);
            List addresses = postalAddressDAO.getByParent();
            if (addresses != null) {
                person.getAddress().addAll(addresses);
            }

            TelephoneNumberDAO telephoneNumberDAO = new TelephoneNumberDAO(context);
            telephoneNumberDAO.setParent(person);
            List phones = telephoneNumberDAO.getByParent();
            if (phones != null) {
                person.getTelephoneNumber().addAll(phones);
            }

            EmailAddressDAO emailAddressDAO = new EmailAddressDAO(context);
            emailAddressDAO.setParent(person);
            List emails = emailAddressDAO.getByParent();
            if (emails != null) {
                person.getEmailAddress().addAll(emails);
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
        PersonType ebPersonType = bu.rimFac.createPersonType();
        
        return ebPersonType;
    }
}
