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
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 *
 * @author  kwalsh
 * @version
 */
class TelephoneNumberDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(TelephoneNumberDAO.class);

    /**
     * Use this constructor only.
     */
    TelephoneNumberDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "TelephoneNumber";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
    *         @param registryObjects is a List of Organizations or Users
    *         @throws RegistryException if the RegistryObject is not Organization or User,
    *         or it has SQLException when inserting their PostalAddress
    */
    @SuppressWarnings("resource")
	public void insert(@SuppressWarnings("rawtypes") List registryObjects)
        throws RegistryException {
        Statement stmt = null;

        if (registryObjects.size() == 0) {
            return;
        }

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> rosIter = registryObjects.iterator();

            while (rosIter.hasNext()) {
                Object ro = rosIter.next();
                String parentId = null;
                Iterator<?> telephoneNumbers;

                if (ro instanceof OrganizationType) {
                    OrganizationType org = (OrganizationType) ro;
                    telephoneNumbers = org.getTelephoneNumber().iterator();
                    parentId = org.getId();
                } else if (ro instanceof UserType) {
                    UserType user = (UserType) ro;
                    telephoneNumbers = user.getTelephoneNumber().iterator();
                    parentId = user.getId();
                } else {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.incorrectRegistryObject"));
                }

                while (telephoneNumbers.hasNext()) {
                    TelephoneNumberType telephoneNumber = (TelephoneNumberType) telephoneNumbers.next();

                    String areaCode = telephoneNumber.getAreaCode();

                    if (areaCode != null) {
                        areaCode = "'" + areaCode + "'";
                    }

                    String countryCode = telephoneNumber.getCountryCode();

                    if (countryCode != null) {
                        countryCode = "'" + countryCode + "'";
                    }

                    String extension = telephoneNumber.getExtension();

                    if (extension != null) {
                        extension = "'" + extension + "'";
                    }

                    String number = telephoneNumber.getNumber();

                    if (number != null) {
                        number = "'" + number + "'";
                    }

                    String phoneType = telephoneNumber.getPhoneType();

                    if (phoneType != null) {
                        phoneType = "'" + phoneType + "'";
                    }

                    String str = "INSERT INTO TelephoneNumber " + "VALUES( " +
                        areaCode + ", " + countryCode + ", " + extension +
                        ", " + number + ", " + phoneType + ", " +
                        "'" + parentId + "' )";

                    log.trace("stmt = " + str);
                    stmt.addBatch(str);
                }
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    /**
         * Does a bulk insert of a Collection of objects that match the type for this persister.
         *
         */
    public void insert(String parentId,
        List<?> telephoneNumbers) throws RegistryException {
        Statement stmt = null;

        if (telephoneNumbers.size() == 0) {
            return;
        }

        log.debug(ServerResourceBundle.getInstance().getString("message.InsertingTelephoneNumbersSize", 
		                                                       new Object[]{new Integer(telephoneNumbers.size())}));

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = telephoneNumbers.iterator();

            while (iter.hasNext()) {
                TelephoneNumberType telephoneNumber = (TelephoneNumberType) iter.next();

                //Log.print(Log.TRACE, 8, "\tDATABASE EVENT: storing TelephoneNumber " );
                String areaCode = telephoneNumber.getAreaCode();

                if (areaCode != null) {
                    areaCode = "'" + areaCode + "'";
                }

                String countryCode = telephoneNumber.getCountryCode();

                if (countryCode != null) {
                    countryCode = "'" + countryCode + "'";
                }

                String extension = telephoneNumber.getExtension();

                if (extension != null) {
                    extension = "'" + extension + "'";
                }

                String number = telephoneNumber.getNumber();

                if (number != null) {
                    number = "'" + number + "'";
                }

                String phoneType = telephoneNumber.getPhoneType();

                if (phoneType != null) {
                    phoneType = "'" + phoneType + "'";
                }

                String str = "INSERT INTO TelephoneNumber " + "VALUES( " +
                    areaCode + ", " + countryCode + ", " + extension + ", " +
                    number + ", " + phoneType + ", " + "'" +
                    parentId + "' )";

                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            if (telephoneNumbers.size() > 0) {
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    protected void loadObject( Object obj,
        ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof TelephoneNumberType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.TelephoneNumberTypeExpected",
                        new Object[]{obj}));
            }

            TelephoneNumberType phone = (TelephoneNumberType) obj;

            String areaCode = rs.getString("areaCode");
            phone.setAreaCode(areaCode);

            String countryCode = rs.getString("countryCode");
            phone.setCountryCode(countryCode);

            String extension = rs.getString("extension");
            phone.setExtension(extension);

            String number = rs.getString("number_");
            phone.setNumber(number);

            String phoneType = rs.getString("phoneType");
            phone.setPhoneType(phoneType);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        TelephoneNumberType obj = bu.rimFac.createTelephoneNumberType();
        
        return obj;
    }
}
