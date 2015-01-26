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

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;


/**
 *
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
abstract class IdentifiableDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(IdentifiableDAO.class);

    static int identifiableExistsBatchCount = Integer.parseInt(RegistryProperties.getInstance()
            .getProperty("eric.persistence.rdb.IdentifiableDAO.identifiableExistsBatchCount", "100"));    
    
    /**
     * Use this constructor only.
     */
    IdentifiableDAO(ServerRequestContext context) {
        super(context);
    }
                        
    /**
     * Delete composed objects that have the specified identifiable
     * as parent.
     */
    @SuppressWarnings("unused")
	protected void deleteComposedObjects(Object object) throws RegistryException {
        super.deleteComposedObjects(object);
        
        if (object instanceof IdentifiableType) {
            IdentifiableType identifiable = (IdentifiableType)object;
            String id = identifiable.getId();

            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(identifiable);

            //Delete Slots
            slotDAO.deleteByParent();
        }
        else {
            int i=0;
        }
    }
    
    /**
     * Insert the composed objects for the specified identifiable
     */
    protected void insertComposedObjects(Object object) throws RegistryException {
        super.insertComposedObjects(object);

        if (object instanceof IdentifiableType) {
            IdentifiableType identifiable= (IdentifiableType)object;
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(identifiable.getId());

            //Now insert Slots for this object
            List<SlotType1> slots = identifiable.getSlot();

            if (slots.size() > 0) {            
                slotDAO.insert(slots, true);
            }
        }
        else {
            @SuppressWarnings("unused")
			int i=0;
        }
    }
            
    /**
     * Returns the SQL fragment string needed by insert or update statements
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object object)
    throws RegistryException {
        
        IdentifiableType ident = (IdentifiableType)object;
        
        String stmtFragment = null;
        
        String id = ident.getId();
        String home = ident.getHome();
        
        if (home != null) {
            home = "'" + home + "'";
        }
               
        if (action == DAO_ACTION_INSERT) {
            stmtFragment =
            " VALUES('" + id + "', " + home + " ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = " id='" + id + "', home=" + home + " ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE from " + getTableName() +
                " WHERE id = '" + id + "' ";
        }
        
        return stmtFragment;
    }
    
    protected List<String> getIdentifiablesIds(List<?> identifiables)
    throws RegistryException {
        List<String> ids = new ArrayList<String>();
        
        try {
            //log.info("size: "  + identifiables.size());
            Iterator<?> iter = identifiables.iterator();
            
            while (iter.hasNext()) {
                String id = BindingUtility.getInstance().getObjectId(iter.next());
                ids.add(id);
            }
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
        
        return ids;
    }
        
    /**
     * Return true if the Identifiable exist
     */
    public boolean identifiableExist(String id)
    throws RegistryException {
        return identifiableExist(id, "identifiable");
    }
    
    /**
     * Check whether the object exists in the specified table.
     */
    public boolean identifiableExist(String id,
    String tableName) throws RegistryException {
        PreparedStatement stmt = null;
        
        try {
            stmt = context.getConnection().prepareStatement("SELECT id FROM " + tableName + " WHERE id=?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            boolean result = false;
            
            if (rs.next()) {
                result = true;
            }
            
            return result;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }
    
    /**
     * Returns List of ids of non-existent Identifiable.
     */
    public List<String> identifiablesExist(List<?> ids)
    throws RegistryException {
        List<String> notExistIdList = identifiablesExist(ids, "identifiable");
                
        return notExistIdList;
    }
    
    /**
     * Returns List of ids of non-existent Identifiable.
     */    
    public List<String> identifiablesExist(List<?> ids, String tableName)
    throws RegistryException {
        List<String> notExistIdList = new ArrayList<String>();
        
        if (ids.size() == 0) {
            return notExistIdList;
        }
        
        Iterator<?> iter = ids.iterator();
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            StringBuffer sql = new StringBuffer("SELECT id FROM " + tableName + " WHERE id IN (");
            List<String> existingIdList = new ArrayList<String>();
            
            /* We need to count the number of item in "IN" list. We need to split the a single
            SQL Strings if it is too long. Some database such as Oracle, does not
            allow the IN list is too long*/
            int listCounter = 0;
            
            while (iter.hasNext()) {
                String id = (String) iter.next();
                
                if (iter.hasNext() && (listCounter < identifiableExistsBatchCount)) {
                    sql.append("'" + id + "',");
                } else {
                    sql.append("'" + id + "')");
                    
                    //log.info("!!!!!!!!!!!!!!!!!!!" + sql.toString());
                    ResultSet rs = stmt.executeQuery(sql.toString());
                    
                    while (rs.next()) {
                        existingIdList.add(rs.getString("id"));
                    }
                    
                    sql = new StringBuffer("SELECT id FROM " + tableName + " WHERE id IN (");
                    listCounter = 0;
                }
                
                listCounter++;
            }
            
            for (int i = 0; i < ids.size(); i++) {
                String id = (String) ids.get(i);
                
                if (!existingIdList.contains(id)) {
                    notExistIdList.add(id);
                }
            }
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
        
        return notExistIdList;
    }
    
    
    protected void loadObject(Object obj, ResultSet rs)
    throws RegistryException {
        try {
            if (!(obj instanceof IdentifiableType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.IdentifiableTypeExpected",
                        new Object[]{obj}));
            }
            
            IdentifiableType ident = (IdentifiableType) obj;
            
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(ident);
        
            String id = rs.getString("id");
            ident.setId(id);
            
            String home = rs.getString("home");
            if (home != null) {
                ident.setHome(home);
            }
                        
            
            boolean returnComposedObjects = context.getResponseOption().isReturnComposedObjects();
            
            if (returnComposedObjects) {
                List<SlotType1> slots = slotDAO.getSlotsByParent(id);
                ident.getSlot().addAll(slots);
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }
    }
    
                                                                                                         
}
