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

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.EmailAddressType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Represents an email address
 *
 * @see <{User}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class EmailAddressDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(EmailAddressDAO.class);
    
    /**
     * Use this constructor only.
     */
    EmailAddressDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "EmailAddress";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    public void insert(@SuppressWarnings("rawtypes") List users)
        throws RegistryException {
        // log.info(ServerResourceBundle.getInstance().getString("message.InsertingEmailAddresss", new Object[]{new Integer(emailAddresss.size())}));
        if (users.size() == 0) {
            return;
        }

        Statement stmt = null;

        try {
            Iterator<?> usersIter = users.iterator();
            stmt = context.getConnection().createStatement();

            while (usersIter.hasNext()) {
                UserType user = (UserType) usersIter.next();

                if (log.isDebugEnabled()) {
                    try {
                        StringWriter writer = new StringWriter();
//                        bu.rimFac.createMarshaller()
                        bu.getJAXBContext().createMarshaller()
                            .marshal(user, writer);
                        log.debug("Inserting user: " + writer.getBuffer().toString());
                    } catch (Exception e) {
                        log.debug("Failed to marshal user: ", e);
                    }
                }
                
                String parentId = user.getId();

                List<EmailAddressType> emails = user.getEmailAddress();
                Iterator<EmailAddressType> emailsIter = emails.iterator();

                while (emailsIter.hasNext()) {
                    //Log.print(Log.TRACE, 8, "\tDATABASE EVENT: storing EmailAddress " );
                    Object obj = emailsIter.next();

                    EmailAddressType emailAddress = (EmailAddressType) obj;

                    String address = emailAddress.getAddress();

                    String type = emailAddress.getType();

                    if (type != null) {
                        type = "'" + type + "'";
                    }

                    String str = "INSERT INTO " + getTableName() + " VALUES( " +
                        "'" + address + "', " + type + ", " + "'" + parentId +
                        "' )";
                    log.trace("stmt = " + str);
                    stmt.addBatch(str);
                }
            }

            if (users.size() > 0) {
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }

    /**
     * Does a bulk insert of a Collection of objects that match the type for this persister.
     *
     */
    public void insert(String parentId,
        List<?> emailAddresss) throws RegistryException {
        log.debug(ServerResourceBundle.getInstance().getString("message.InsertingEmailAddresss", new Object[]{new Integer(emailAddresss.size())}));

        if (emailAddresss.size() == 0) {
            return;
        }

        Statement stmt = null;

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = emailAddresss.iterator();

            while (iter.hasNext()) {
                EmailAddressType emailAddress = (EmailAddressType) iter.next();

                //Log.print(Log.TRACE, 8, "\tDATABASE EVENT: storing EmailAddress " );
                String address = emailAddress.getAddress();

                String type = emailAddress.getType();

                if (type != null) {
                    type = "'" + type + "'";
                }

                String str = "INSERT INTO EmailAddress " + "VALUES( " + "'" +
                    address + "', " + type + ", " + "'" + parentId + "' )";
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            if (emailAddresss.size() > 0) {
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }

    /**
     * Does a bulk update of a Collection of objects that match the type for this persister.
     *
     */
    public void update(String parentId,
        List<?> emailAddresss) throws RegistryException {
        log.debug(ServerResourceBundle.getInstance().getString("message.UpdatingEmailAddresss", new Object[]{new Integer(emailAddresss.size())}));

        Statement stmt = null;

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = emailAddresss.iterator();

            while (iter.hasNext()) {
                EmailAddressType ebEmailAddressType = (EmailAddressType) iter.next();

                String address = ebEmailAddressType.getAddress();

                String type = ebEmailAddressType.getType();

                if (type != null) {
                    type = "'" + type + "'";
                }

                String str = "UPDATE EmailAddress SET " + 
                    //"accesControlPolicy = null, " +
                    "SET address = '" + address + "', " + "SET type = " + type +
                    " WHERE parent = '" + parentId + "' ";
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }

    protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof EmailAddressType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.EmailAddressTypeExpected",
                        new Object[]{obj}));
            }

            EmailAddressType addr = (EmailAddressType) obj;

            String address = rs.getString("address");
            addr.setAddress(address);

            String type = rs.getString("type");
            addr.setType(type);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        EmailAddressType ebEmailAddressType = bu.rimFac.createEmailAddressType();
        
        return ebEmailAddressType;
    }
}
