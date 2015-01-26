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
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.Utility;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 * @author Farruks S. Najmi
 *
 * Base class for all DAOs
 */
abstract class AbstractDAO implements OMARDAO {
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static Utility util = Utility.getInstance();
    
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    
    
    protected static int DAO_ACTION_QUERY = 0;
    protected static int DAO_ACTION_INSERT = 1;
    protected static int DAO_ACTION_UPDATE = 2;
    protected static int DAO_ACTION_DELETE = 3;

    private static final Log log = LogFactory.getLog(AbstractDAO.class);

    protected ServerRequestContext context=null;
    protected int action=DAO_ACTION_QUERY;
    
    protected Object parent;
    static int inClauseTermLimit = Integer.parseInt(RegistryProperties.getInstance()
            .getProperty("eric.persistence.rdb.AbstractDAO.inClauseTermLimit", "100"));    
    
    
    /**
     * Some DAOs are for objects composed with another parent object.
     * This method is to set the parent object.
     */
    public void setParent(Object parent) {
        this.parent = parent;
    }
    
    /**
     * Use this constructor only.
     */
    AbstractDAO(ServerRequestContext context) {
        this.context = context;
    }
    
    /*
     * Should not be used.
     */
    @SuppressWarnings("unused")
	private AbstractDAO() {
    }

    /*
     * Initialize a binding object from specified ResultSet.
     */
    abstract protected void loadObject(Object obj, ResultSet rs) throws RegistryException;
    
    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    abstract Object createObject() throws JAXBException;
    
    /**
     * Gets a List of binding objects from specified ResultSet.
     */
    public List<Object> getObjects(ResultSet rs, int startIndex, int maxResults) throws RegistryException {
        List<Object> res = new ArrayList<Object>();

        try {
            if (startIndex > 0) {
                // calling rs.next() is a workaround for some drivers, such
                // as Derby's, that do not set the cursor during call to 
                // rs.relative(...)
                rs.next();
                @SuppressWarnings("unused")
				boolean onRow = rs.relative(startIndex-1);
            }
            
            int cnt = 0;
            while (rs.next()) {
                Object obj = createObject();
                loadObject(obj, rs);
                res.add(obj);
                
                if (++cnt == maxResults) {
                    break;
                }
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } catch (JAXBException j) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), j);
            throw new RegistryException(j);
        }

        return res;
    }
    
    
    /**
     * Does a bulk delete of a Collection of objects that match the type for this persister.
     *
     */
    public void delete(@SuppressWarnings("rawtypes") List objects) throws RegistryException {
        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }
        
        log.trace(ServerResourceBundle.getInstance().getString("message.DeletingRowsInTable", new Object[]{new Integer(objects.size()), getTableName()}));
        action = DAO_ACTION_DELETE;
        
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            @SuppressWarnings("rawtypes")
			Iterator iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                
                prepareToDelete(obj);
                
                String str = getSQLStatementFragment(obj);
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }
            
            @SuppressWarnings("unused")
			int[] updateCounts = stmt.executeBatch();
            
            iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                
                onDelete(obj);
            }
            
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }
    
    protected String getParentId() throws JAXRException {
        String parentId = BindingUtility.getInstance().getObjectId(parent);
        return parentId;
    }
    
    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "parent";
    }
    
    /*
     * Indicate whether the type for this DAO has composed objects or not.
     * Used in deciding whether to deleteComposedObjects or not during delete.
     *
     */
    protected boolean hasComposedObject() {
        return false;
    }
    
    /**
     * Does a bulk delete of objects based upon parent set for this DAO
     *
     */
    public void deleteByParent()
    throws RegistryException {
        PreparedStatement stmt = null;
        
        try {
            
            if (!hasComposedObject()) {
                //Do simple deletion if there are no composed objects for this type
                
                String str = "DELETE from " + getTableName() +
                " WHERE " + getParentAttribute() + " = ? ";
                log.trace("stmt = " + str);
                stmt = context.getConnection().prepareStatement(str);
                stmt.setString(1, getParentId());
                
                stmt.execute();
            }
            else {
                //If there are composed objects for this type then
                //we must first fetch the objects and then use the
                //delete(List objects) method so that composed objects
                //are deleted.
                List<Object> objects = getByParent();                
                delete(objects);
            }
        } catch (SQLException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } catch (JAXRException e) {
            RegistryException exception = new RegistryException(e);
            throw exception;
        } finally {
            closeStatement(stmt);
        }
    }
    
    /**
     * Gets objects based upon parent set for this DAO
     *
     */
    public List<Object> getByParent() throws RegistryException {
        List<Object> objects = new ArrayList<Object>();

        
        PreparedStatement stmt = null;
        
        try {
            String str = "SELECT * FROM " + getTableName() +
            " WHERE " + getParentAttribute() + " = ? ";
            stmt = context.getConnection().prepareStatement(str);
            stmt.setString(1, getParentId());
            
            ResultSet rs = stmt.executeQuery();
            
            objects = getObjects(rs, 0, -1);
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } catch (JAXRException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
                
        return objects;
    }
    
    /**
     * @see it.cnr.icar.eric.server.persistence.rdb.OMARDAO#getTableName()
     */
    public abstract String getTableName();
    
    /**
     * @see it.cnr.icar.eric.server.persistence.rdb.OMARDAO#insert(org.oasis.ebxml.registry.bindings.rim.UserType, java.sql.Connection, java.util.List, java.util.HashMap)
     */
    @SuppressWarnings("unchecked")
	public void insert(@SuppressWarnings("rawtypes") List objects) throws RegistryException {
        
        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }
        
        //First process any objects that may already exists in persistence layer
        objects = processExistingObjects(objects);
        
        //Return immediately if no objects to insert
        if (objects.size() == 0) {
            return;
        }
        
        log.trace(ServerResourceBundle.getInstance().getString("message.InsertingRowsInTable", new Object[]{new Integer(objects.size()), getTableName()}));
        action = DAO_ACTION_INSERT;
        
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            Iterator<ExtrinsicObjectType> iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();                
                
                String str = getSQLStatementFragment(obj);
                log.trace("stmt = " + str);
                stmt.addBatch(str);
                
                prepareToInsert(obj);                
            }
            
            long startTime = System.currentTimeMillis();
            log.trace("AbstractDAO.insert: doing executeBatch");
            @SuppressWarnings("unused")
			int[] updateCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            log.trace("AbstractDAO.insert: done executeBatch elapedTimeMillis=" + (endTime-startTime));
            
            
            iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                                
                onInsert(obj);                
            }
            
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
        
    }
    
    @SuppressWarnings("rawtypes")
	protected List processExistingObjects(List objects) throws RegistryException{
        return objects;
    }
    
    protected void prepareToInsert(Object object) throws RegistryException {
    }
    
    protected void prepareToUpdate(Object object) throws RegistryException {
        deleteComposedObjects(object);
    }
    
    protected void prepareToDelete(Object object) throws RegistryException {
        deleteComposedObjects(object);
    }
    
    protected void onInsert(Object object) throws RegistryException{
        insertComposedObjects(object);
    }
    
    protected void onUpdate(Object object) throws RegistryException{
        insertComposedObjects(object);        
    }
    
    protected void onDelete(Object object) throws RegistryException{
    }
    
    protected void deleteComposedObjects(Object object)  throws RegistryException {
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
    }
    
    protected String getSQLStatementFragment(Object object)
    throws RegistryException {
        throw new RegistryException(ServerResourceBundle.getInstance().getString("message.getSQLStatementFragmentMissing",
                new Object[]{getTableName()}));
    }
    
    /**
     * @see it.cnr.icar.eric.server.persistence.rdb.OMARDAO#update(org.oasis.ebxml.registry.bindings.rim.UserType, java.sql.Connection, java.util.List, java.util.HashMap)
     */
    public void update(@SuppressWarnings("rawtypes") List objects) throws RegistryException {
        
        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }
        
        log.trace(ServerResourceBundle.getInstance().getString("message.UpdatingRowsInTable", new Object[]{new Integer(objects.size()), getTableName()}));
        action = DAO_ACTION_UPDATE;
        
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            Iterator<?> iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                
                prepareToUpdate(obj);
                
                String str = getSQLStatementFragment(obj);
                log.trace("stmt = " + str);
                stmt.addBatch(str);
            }
            
            @SuppressWarnings("unused")
			int[] updateCounts = stmt.executeBatch();
            
            iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                
                onUpdate(obj);
            }
            
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }
    
    /**
     * A convenience method to close a <code>Statement</code> stmt.
     * Calls <code>close()</code> method if stmt is not NULL. Logs
     * <code>SQLException</code> as error.
     *
     * @param stmt Statement to be closed.
     */
    public final void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqle) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
        }
    }
    
    /**
     * Return the id for the repository item used to hold information that
     * could not fit in a db column and therefor "spilled over" into a repository item.
     * The returned id is then stored instead of the content that "spilled over" into 
     * the repository item.
     *
     * @param parentId the id of the parent object whose column data is being "spilled over"
     * @param columnInfo contains information on the table and column whose data is being spilledOver. Format SHOULD be tableName:columnName)
     *
     * @return the id of the "spill over" repository item
     */
    String getSpillOverRepositoryItemId(String parentId, String columnInfo) throws RegistryException {
        String spillOverId = null;
        if ((columnInfo == null) || (columnInfo.length() == 0)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.error.columnInfoUnspecified"));
        } else if (!columnInfo.endsWith(":")) {
            columnInfo += ":";
        }
        
        spillOverId = "urn:freebxml:registry:spillOverId:" + columnInfo + parentId;
        
        if (!it.cnr.icar.eric.common.Utility.getInstance().isValidRegistryId(spillOverId)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.error.spillOverIdTooLong", new Object[]{parentId}));
        }
        
        return spillOverId;
    }
    
    /**
     * Saves content of a db column to an ExtrinsicObject / RepositoryItem pair.
     *
     * TODO: 
     *
     * Need to enforce referential integrity by deleting spillover EO/RI when parent is deleted.
     * This should be done as part of the fix to prevent deletions when there are existing references
     * by treating spillover objects as if they are referenced by their parent.      
     *
     * @param parentId the id of the parent object whose column data is being "spilled over"
     * @param columnInfo contains information on the table and column whose data is being spilledOver. Format SHOULD be tableName:columnName)
     * @param columnData contains the column data being "spilled over" into RepositoryItem
     *
     * @return the id of the "spill over" repository item
     */
    protected String marshalToRepositoryItem(
            String parentId, 
            String columnInfo, 
            String columnData) throws RegistryException {
        
        String spillOverId = getSpillOverRepositoryItemId(parentId, columnInfo);

        try {
            ExtrinsicObjectType ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
            ebExtrinsicObjectType.setId(spillOverId);
            ebExtrinsicObjectType.setLid(spillOverId);
            ebExtrinsicObjectType.setMimeType("text/plain");

            VersionInfoType versionInfo = bu.rimFac.createVersionInfoType();
            versionInfo.setVersionName("1.1");
            ebExtrinsicObjectType.setVersionInfo(versionInfo);
            ebExtrinsicObjectType.setContentVersionInfo(versionInfo);

            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);

            //Delete any previous eo and ri
            removeSpillOverRepositoryItem(parentId, columnInfo);
            
            RepositoryItem ebRepositoryItem = it.cnr.icar.eric.common.Utility.getInstance().
                createRepositoryItem(spillOverId, columnData);

            //Insert the extrinsic objects
            
            //Need to place ri in RepositoryItemsMap otherwise ExtrinsicObjectDAO will not set contentVersionInfo.versionName
            context.getRepositoryItemsMap().put(spillOverId, ebRepositoryItem);
            ArrayList<ExtrinsicObjectType> extrinsicObjects = new ArrayList<ExtrinsicObjectType>();
            extrinsicObjects.add(ebExtrinsicObjectType);
            extrinsicObjectDAO.insert(extrinsicObjects);

            //Inserting the repository item
            rm.insert((context), ebRepositoryItem);
            
            //Remove ri from RepositoryItemsMap to otherwise it will create problems in lcm.submitObjects wrapUp
            context.getRepositoryItemsMap().remove(spillOverId);
        } 
        catch (RegistryException e) {
            throw e;
        }                                        
        catch (Exception e) {
            throw new RegistryException(e);
        }                                        
        
        return spillOverId;
    }
    
    /**
     * Remove specified spillover ExtrinsicObject and ReositoryItem.
     */
    protected void removeSpillOverRepositoryItem(String parentId, String columnInfo) throws RegistryException {
        String spillOverId = getSpillOverRepositoryItemId(parentId, columnInfo);
        
        ExtrinsicObjectType ebExtrinsicObjectType = bu.rimFac.createExtrinsicObjectType();
		ebExtrinsicObjectType.setId(spillOverId);
		ebExtrinsicObjectType.setLid(spillOverId);
		ebExtrinsicObjectType.setMimeType("text/plain");

		VersionInfoType versionInfo = bu.rimFac.createVersionInfoType();
		versionInfo.setVersionName("1.1");
		ebExtrinsicObjectType.setVersionInfo(versionInfo);
		ebExtrinsicObjectType.setContentVersionInfo(versionInfo);

		ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);

		//Delete any previous copy
		try {
		    //Delete existing eo and ri
		    rm.delete(spillOverId);
		} catch (ObjectNotFoundException e) {
		    //Also catches RepositoryItemNotFoundException
		    //Does not exist. All is well
		}

		try {
		    //Delete existing eo and ri
		    extrinsicObjectDAO.delete(Collections.singletonList(ebExtrinsicObjectType));
		} catch (ObjectNotFoundException e) {
		    //Does not exist. All is well
		}
    }     
    
    /**
     * Unmarshalls a "spilled over" RepositoryItem into a String
     *
     * @param spillOverId the id of the ExtrinsicObject associated with the RepositoryItem 
     * used to unmarshal the "spilled over" content from.
     */
    public String unmarshallFromRepositoryItem(String spillOverId) throws RegistryException {
        String content = spillOverId;
                
        InputStream is = null;
        try {
            RepositoryItem ri = rm.getRepositoryItem(spillOverId);
            
            content = it.cnr.icar.eric.common.Utility.getInstance().
                    unmarshalInputStreamToString(ri.getDataHandler().getInputStream());
        } catch (IOException e) {
            throw new RegistryException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
        
        return content;
    }
    
    /**
     * Executes a Select statment that has an IN clause while
     * taking care to execute it in chunks to avoid scalability limits
     * in some databases (Oracle 10g limits terms in IN clause to 1000)
     *
     * Note: Caller is responsible for closing statement associated with each resultSet
     * in resultSets. 
     *
     * @param selectStmtTemplate a string representing the SELECT statment in a parameterized format consistent withebRR parameterized queries.
     * @return a List of Objects
     */    
    public List<ResultSet> executeBufferedSelectWithINClause(String selectStmtTemplate, List<?> terms, int termLimit)
    throws RegistryException {
        ArrayList<ResultSet> resultSets = new ArrayList<ResultSet>();
        
        if (terms.size() == 0) {
            return resultSets;
        }
        
        Iterator<?> iter = terms.iterator();
        
        try {            
            //We need to count the number of terms in "IN" list. 
            //We need to split the SQL Strings into chunks if there are too many terms. 
            //Reason is that some database such as Oracle, do not allow the IN list is too long
            int termCounter = 0;
            
            StringBuffer inTerms = new StringBuffer();
            while (iter.hasNext()) {
                String term = (String) iter.next();
                
                if (iter.hasNext() && (termCounter < termLimit)) {
                    inTerms.append("'" + term + "',");
                } else {
                    inTerms.append("'" + term + "' ");
                    String sql = selectStmtTemplate.replaceAll("\\$InClauseTerms", inTerms.toString());
                    
                    // xxx 120216 pa allow scrollable resultset for jump to end with rs.last()
                    Statement stmt = context.getConnection().createStatement(
    						ResultSet.TYPE_SCROLL_INSENSITIVE, 
    						ResultSet.CONCUR_READ_ONLY);
                    ResultSet rs = stmt.executeQuery(sql);
                    resultSets.add(rs);
                                        
                    termCounter = 0;
                    inTerms = new StringBuffer();
                }
                
                termCounter++;
            }
            
        } catch (SQLException e) {
            throw new RegistryException(e);
        }
        
        return resultSets;
    }
    
    
}
