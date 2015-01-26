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

import it.cnr.icar.eric.common.exceptions.DuplicateSlotsException;
import it.cnr.icar.eric.common.exceptions.SlotNotExistException;
import it.cnr.icar.eric.common.exceptions.SlotsExistException;
import it.cnr.icar.eric.common.exceptions.SlotsParentNotExistException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.Utility;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;


class SlotDAO extends AbstractDAO {
    private static final Log log = LogFactory.getLog(SlotDAO.class);

    /**
     * Use this constructor only.
     */
    SlotDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Slot";
    }
    
    public String getTableName() {
        return getTableNameStatic();
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object obj) throws RegistryException {
        throw new RegistryException(ServerResourceBundle.getInstance().getString("message.methodNotSupported"));
    }
    
    /*
     * Initialize a binding object from specified ResultSet.
     */
    protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
        throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unimplementedMethod"));
    }        
    
    List<SlotType1> getSlotsByParent(String parentId)
    throws RegistryException {
        List<SlotType1> slots = new ArrayList<SlotType1>();
        PreparedStatement stmt = null;
        
        try {
            String sql = "SELECT * FROM " + getTableName() +
            " WHERE parent = ? ORDER BY name_, sequenceId ASC";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, parentId);
            
            ResultSet rs = stmt.executeQuery();
            
            String lastName = "";
            SlotType1 ebSlotType = null;
            ValueListType ebValueListType = null;
            
            while (rs.next()) {
                //int sequenceId = rs.getInt("sequenceId");
                String name = rs.getString("name_");
                String slotType = rs.getString("slotType");
                String value = rs.getString("value");
                
                if (!name.equals(lastName)) {
                    ebSlotType = bu.rimFac.createSlotType1();
                    ebSlotType.setName(name);
                    
                    if (slotType != null) {
                        ebSlotType.setSlotType(slotType);
                    }
                    
                    ebValueListType = bu.rimFac.createValueListType();
                    ebSlotType.setValueList(ebValueListType);
                    slots.add(ebSlotType);
                }
                
                lastName = name;
                
                if (value != null) {
                    ebValueListType.getValue().add(value);
                }
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
        
        return slots;
    }
    
    /**
     * Get the List of the names of a Slots that already exist in database
     * @param parentId the id of the parent of those slots
     */
    public List<String> slotsExist(String parentId, List<?> slots)
    throws RegistryException {
        Statement stmt = null;
        List<String> slotsNames = new ArrayList<String>();
        
        if (slots.size() > 0) {
            try {
                Iterator<?> slotsIter = slots.iterator();
                String names = "";
                
                while (slotsIter.hasNext()) {
                    names += ("'" +
                    Utility.escapeSQLChars(((SlotType1) slotsIter.next()).getName()) +
                    "'");
                    
                    if (slotsIter.hasNext()) {
                        names += ",";
                    }
                }
                
                String sql = "SELECT name_ FROM " + getTableName() + " WHERE" +
                " parent=" + "'" + parentId + "'" + " AND " +
                " name_ IN (" + names + ")";
                
                //log.trace("stmt= '" + sql + "'");
                stmt = context.getConnection().createStatement();
                
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    slotsNames.add(rs.getString(1));
                }
                
                return slotsNames;
            } catch (SQLException e) {
                throw new RegistryException(e);
            } finally {
                closeStatement(stmt);
            }
        } else {
            return slotsNames;
        }
    }
    
    /**
     *        It checks whether there exists more than a slot having the same name
     *        @returns List of duplidate slots names
     */
    public List<String> getDuplicateSlots(List<?> slots) {
        ArrayList<String> duplicateSlotsNames = new ArrayList<String>();
        List<String> slotsNames = new ArrayList<String>();
        
        if (slots.size() > 0) {
            Iterator<?> iter = slots.iterator();
            
            while (iter.hasNext()) {
                String slotName = ((SlotType1) iter.next()).getName();
                
                if (slotsNames.contains(slotName)) {
                    duplicateSlotsNames.add(slotName);
                }
                
                slotsNames.add(slotName);
            }
            
            return duplicateSlotsNames;
        } else {
            return duplicateSlotsNames;
        }
    }
    
    public void insert(@SuppressWarnings("rawtypes") List slots) throws RegistryException {
        throw new RegistryException(ServerResourceBundle.getInstance().getString("message.methodNotSupported"));        
    }
    
    /**
     * @param parentInsert It should be set to true if Slot insert is part of new
     * RegistryObject insert (i.e. in the case        of SubmitObjectsRequest). It should
     * be set to false in the case of AddSlotsRequest because the parent of the
     * slot is expected to be already submitted by previous SubmitObjectRequest.
     * In the latter case whether the parents of the slots exist will be checked.
     */
    public void insert(List<?> slots, boolean parentInsert) throws RegistryException {
        PreparedStatement pstmt = null;
        
        String parentId = (String)parent;
        
        if (slots.size() == 0) {
            return;
        }
        
        try {
            String sql = "INSERT INTO " + getTableName() + " (sequenceId, " +
            "name_, slotType, value, parent)" + " VALUES(?, ?, ?, ?, ?)";
            pstmt = context.getConnection().prepareStatement(sql);
            
            List<String> duplicateSlotsNames = getDuplicateSlots(slots);
            
            if (duplicateSlotsNames.size() > 0) {
                // Some slots have duplicate name
                throw new DuplicateSlotsException(parentId, duplicateSlotsNames);
            }
            
            RegistryObjectDAO roDAO = new RegistryObjectDAO(context);
            
            // Check whether the parent exist in database, in case the parent
            // has been inserted by the previous SubmitObjectsRequest
            // (i.e. in the case of AddSlotsRequest)
            if (!parentInsert &&
            !roDAO.registryObjectExist(parentId)) {
                // The parent does not exist
                throw new SlotsParentNotExistException(parentId);
            }
            
            List<String> slotsNamesAlreadyExist = slotsExist(parentId, slots);
            
            if (slotsNamesAlreadyExist.size() > 0) {
                // Some slots for this RegistryObject already exist
                throw new SlotsExistException(parentId, slotsNamesAlreadyExist);
            }
            
            Iterator<?> iter = slots.iterator();
            @SuppressWarnings("unused")
			Vector<Object> slotNames = new Vector<Object>();
            
            while (iter.hasNext()) {
                SlotType1 slot = (SlotType1) iter.next();
                String slotName = slot.getName();
                String slotType = slot.getSlotType();
                List<String> values = slot.getValueList().getValue();
                int size = values.size();
                
                for (int j = 0; j < size; j++) {
                    String value = values.get(j);
                    pstmt.setInt(1, j);
                    pstmt.setString(2, slotName);
                    pstmt.setString(3, slotType);
                    pstmt.setString(4, value);
                    pstmt.setString(5, parentId);
                    
                    log.trace("stmt = " + pstmt.toString());
                    pstmt.addBatch();
                }
            }
            
            if (slots.size() > 0) {
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
    
    
    public void deleteByParentIdAndSlots(
    String parentId, List<?> slots) throws RegistryException {
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String str = "DELETE from " + getTableName() + " WHERE parent = '" +
            parentId + "' AND (";
            Iterator<?> iter = slots.iterator();
            
            while (iter.hasNext()) {
                SlotType1 slot = (SlotType1) iter.next();
                String slotName = slot.getName();
                
                if (iter.hasNext()) {
                    str = str + "name_ = '" + Utility.escapeSQLChars(slotName) +
                    "' OR ";
                } else {
                    str = str + "name_ = '" + Utility.escapeSQLChars(slotName) +
                    "' )";
                }
            }
            
            log.trace("stmt = " + str);
            stmt.execute(str);
            
            int updateCount = stmt.getUpdateCount();
            
            if (updateCount < slots.size()) {
                throw new SlotNotExistException(parentId);
            }
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }
    
    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        SlotType1 obj = bu.rimFac.createSlotType1();
        
        return obj;
    }
}
