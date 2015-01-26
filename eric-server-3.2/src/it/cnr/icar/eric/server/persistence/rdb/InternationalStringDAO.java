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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;


public abstract class InternationalStringDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(InternationalStringDAO.class);

    /**
     * Use this constructor only.
     */
    InternationalStringDAO(ServerRequestContext context) {
        super(context);
    }
        
    /*
     * Initialize a binding object from specified ResultSet.
     */
    protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
        throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unimplementedMethod"));
    }
    
    InternationalStringType getInternationalStringByParent(
        String parentId) throws RegistryException {
        InternationalStringType is = null;
        PreparedStatement stmt = null;

        try {
            String tableName = getTableName();

            if (tableName.equalsIgnoreCase("Name_")) {
                is = bu.rimFac.createInternationalStringType();
            } else if (tableName.equalsIgnoreCase("Description")) {
                is = bu.rimFac.createInternationalStringType();
            }else if (tableName.equalsIgnoreCase("UsageDescription")) {
                is = bu.rimFac.createInternationalStringType();
            }
            stmt = context.getConnection().prepareStatement("SELECT * FROM " + getTableName() +
                    " WHERE parent = ?");
            stmt.setString(1, parentId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String charsetName = rs.getString("charset");
                String lang = rs.getString("lang");
                String value = rs.getString("value");
                if (value != null) {
                    LocalizedStringType ls = bu.rimFac.createLocalizedStringType();
                    ls.setCharset(charsetName);
                    ls.setLang(lang);
                    ls.setValue(value);
                    is.getLocalizedString().add(ls);
                }
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }

        return is;
    }
    
    public void insert(String parentId,
        InternationalStringType is) throws RegistryException {
        PreparedStatement pstmt = null;

        try {
            String str = "INSERT INTO " + getTableName() + " VALUES(?, " + // charsetName
                "?," + // lang
                "?, " + // value
                "?)"; // parentId
            pstmt = context.getConnection().prepareStatement(str);

            if (is != null) {
                Iterator<LocalizedStringType> lsItems = is.getLocalizedString().iterator();

                while (lsItems.hasNext()) {
                    LocalizedStringType ebLocalizedStringType = lsItems.next();
                    @SuppressWarnings("unused")
					String charset = ebLocalizedStringType.getCharset();
                    String lang = ebLocalizedStringType.getLang();
                    String value = ebLocalizedStringType.getValue();
                    String charsetName = ebLocalizedStringType.getCharset();
                    
                    if (value != null && value.length() > 0) {
                        pstmt.setString(1, charsetName);
                        pstmt.setString(2, lang);
                        pstmt.setString(3, value);
                        pstmt.setString(4, parentId);

                        log.trace("stmt = " + pstmt.toString());
                        pstmt.addBatch();
                    }
                }
            }

            if (is != null) {
                @SuppressWarnings("unused")
				int[] updateCounts = pstmt.executeBatch();
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(pstmt);
        }
    }

    
}
