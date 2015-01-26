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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;


/**
 * DAO for UsageParameter
 *
 * @see <{User}>
 * @author Farrukh S. Najmi
 */
class UsageParameterDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(UsageParameterDAO.class);

    /**
     * Use this constructor only.
     */
    UsageParameterDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "UsageParameter";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Does a bulk insert of a Collection of objects that match the type for this persister.
     *
     */
    public void insert(String parentId, List<?> usageParams)
        throws RegistryException {
        Statement stmt = null;

        if (usageParams.size() == 0) {
            return;
        }

        try {
            stmt = context.getConnection().createStatement();

            Iterator<?> iter = usageParams.iterator();

            while (iter.hasNext()) {
                String value = (String) iter.next();

                String str = "INSERT INTO UsageParameter " + "VALUES( " + "'" +
                    value + "', " + "'" + parentId + "' )";
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }

            if (usageParams.size() > 0) {
                @SuppressWarnings("unused")
				int[] updateCounts = stmt.executeBatch();
            }
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }
    //TODO:Need to review the usability of this method. 
    protected void loadObject( Object obj,
        ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof java.lang.String)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.javaLangStringExpected",
                        new Object[]{obj}));
            }

            String value = (String) obj;
            value.concat(rs.getString("value"));
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }

    List<String> getUsageParametersByParent(String parentId)
        throws RegistryException {
        ArrayList<String> usageParams = new ArrayList<String>();
        PreparedStatement stmt = null;

        try {
            String sql = "SELECT * FROM UsageParameter WHERE parent = ?";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, parentId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String usageParam = new String();
                loadObject( usageParam, rs);
                usageParams.add(rs.getString("value"));
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }

        return usageParams;
    }
    
    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        String obj = new String();
        
        return obj;
    }
    
}
