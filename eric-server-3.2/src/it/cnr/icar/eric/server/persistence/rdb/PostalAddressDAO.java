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

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 *
 * @author Farrukh S. Najmi
 * @version
 */
class PostalAddressDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(PostalAddressDAO.class);

    /**
     * Use this constructor only.
     */
    PostalAddressDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "PostalAddress";
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
            /*
            String sqlStr = "INSERT INTO " + getTableName() +
                              " VALUES( " +
                                                           "?, " + // city
                                                           "?, " + // country
                                                           "?, " + // postalCode
                                                           "?, " + // state
                                                           "?, " + // street
                                                           "?, " + // streetNum
                                                           "?)"; // Parent id

            PreparedStatement pstmt = context.getConnection().prepareStatement(sqlStr);
            */
            stmt = context.getConnection().createStatement();

            Iterator<?> rosIter = registryObjects.iterator();

            while (rosIter.hasNext()) {
                Object ro = rosIter.next();
                String parentId = null;
                PostalAddressType postalAddress = null;

                if (ro instanceof OrganizationType) {
                    OrganizationType org = (OrganizationType) ro;
                    postalAddress = org.getAddress().get(0);
                    parentId = org.getId();
                } else if (ro instanceof UserType) {
                    UserType user = (UserType) ro;

                    //TODO: Save extra addresses, if required
                    postalAddress = user.getAddress().get(0);
                    parentId = user.getId();
                } else {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.incorrectRegistryObject"));
                }

                /*
                stmt.setString(1, postalAddress.getCity());
                                stmt.setString(2, postalAddress.getCountry());
                stmt.setString(3, postalAddress.getPostalCode());
                stmt.setString(4, postalAddress.getStateOrProvince());
                stmt.setString(5, postalAddress.getStreet());
                stmt.setString(6, postalAddress.getStreetNumber());
                stmt.setString(7, org.getId());
                stmt.addBatch();*/
                String city = postalAddress.getCity();

                if (city != null) {
                    city = "'" + city + "'";
                }

                String country = postalAddress.getCountry();

                if (country != null) {
                    country = "'" + country + "'";
                }

                String postalCode = postalAddress.getPostalCode();

                if (postalCode != null) {
                    postalCode = "'" + postalCode + "'";
                }

                String state = postalAddress.getStateOrProvince();

                if (state != null) {
                    state = "'" + state + "'";
                }

                String street = postalAddress.getStreet();

                if (street != null) {
                    street = "'" + street + "'";
                }

                String streetNum = postalAddress.getStreetNumber();

                if (streetNum != null) {
                    streetNum = "'" + streetNum + "'";
                }

                String str = "INSERT INTO PostalAddress " + "VALUES( " + city +
                    ", " + country + ", " + postalCode + ", " + state + ", " +
                    street + ", " + streetNum + ", " + "'" + parentId + "' )";
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            // end looping all Organizations 
            if (registryObjects.size() > 0) {
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
        List<?> postalAddresss) throws RegistryException {
        Statement stmt = null;
        log.debug(ServerResourceBundle.getInstance().getString("message.InsertingPostalAddresss", new Object[]{new Integer(postalAddresss.size())}));

        if (postalAddresss.size() == 0) {
            return;
        }

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = postalAddresss.iterator();

            while (iter.hasNext()) {
                PostalAddressType postalAddress = (PostalAddressType) iter.next();

                //Log.print(Log.TRACE, 8, "\tDATABASE EVENT: storing PostalAddress " );
                String city = postalAddress.getCity();

                if (city != null) {
                    city = "'" + city + "'";
                }

                String country = postalAddress.getCountry();

                if (country != null) {
                    country = "'" + country + "'";
                }

                String postalCode = postalAddress.getPostalCode();

                if (postalCode != null) {
                    postalCode = "'" + postalCode + "'";
                }

                String state = postalAddress.getStateOrProvince();

                if (state != null) {
                    state = "'" + state + "'";
                }

                String street = postalAddress.getStreet();

                if (street != null) {
                    street = "'" + street + "'";
                }

                String streetNum = postalAddress.getStreetNumber();

                if (streetNum != null) {
                    streetNum = "'" + streetNum + "'";
                }

                String str = "INSERT INTO PostalAddress " + "VALUES( " + city +
                    ", " + country + ", " + postalCode + ", " + state + ", " +
                    street + ", " + streetNum + ", " + "'" + parentId + "' )";
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            if (postalAddresss.size() > 0) {
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
        List<?> postalAddresss) throws RegistryException {
        Statement stmt = null;
        log.debug(ServerResourceBundle.getInstance().getString("message.UpdatingPostalAddresss", new Object[]{new Integer(postalAddresss.size())}));

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = postalAddresss.iterator();

            while (iter.hasNext()) {
                PostalAddressType postalAddress = (PostalAddressType) iter.next();

                String city = postalAddress.getCity();

                if (city != null) {
                    city = "'" + city + "'";
                }

                String country = postalAddress.getCountry();

                if (country != null) {
                    country = "'" + country + "'";
                }

                String postalCode = postalAddress.getPostalCode();

                if (postalCode != null) {
                    postalCode = "'" + postalCode + "'";
                }

                String state = postalAddress.getStateOrProvince();

                if (state != null) {
                    state = "'" + state + "'";
                }

                String street = postalAddress.getStreet();

                if (street != null) {
                    street = "'" + street + "'";
                }

                String streetNum = postalAddress.getStreetNumber();

                if (streetNum != null) {
                    streetNum = "'" + streetNum + "'";
                }

                String str = "UPDATE PostalAddress " + "SET city = " + city +
                    ", " + "SET country = " + country + ", " +
                    "SET postalCode = " + postalCode + ", " + "SET state = " +
                    state + ", " + "SET street = " + street + ", " +
                    "SET streetNum = " + streetNum + " " + " WHERE parent = '" +
                    parentId + "' ";
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

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof PostalAddressType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.PostalAddressTypeExpected",
                        new Object[]{obj}));
            }

            PostalAddressType addr = (PostalAddressType) obj;

            String city = rs.getString("city");
            addr.setCity(city);

            String country = rs.getString("country");
            addr.setCountry(country);

            String postalCode = rs.getString("postalCode");
            addr.setPostalCode(postalCode);

            String stateOrProvince = rs.getString("state");
            addr.setStateOrProvince(stateOrProvince);

            String street = rs.getString("street");
            addr.setStreet(street);

            String streetNumber = rs.getString("streetNumber");
            addr.setStreetNumber(streetNumber);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        PostalAddressType obj = bu.rimFac.createPostalAddressType();
        
        return obj;
    }
}
