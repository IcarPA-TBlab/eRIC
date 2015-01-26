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

import it.cnr.icar.eric.common.exceptions.UnresolvedURLsException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.Utility;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;


/**
 * Maps to a BindingTemplate in UDDI.
 *
 * @see <{Concept}>
 * @author Farrukh S. Najmi
 * @author Kathy Walsh
 * @author Adrian Chong
 */
class ServiceBindingDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(ServiceBindingDAO.class);

    /**
     * Use this constructor only.
     */
    ServiceBindingDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "ServiceBinding";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    protected void prepareToInsert(Object object) throws RegistryException {        
        ServiceBindingType ro = (ServiceBindingType)object;
        super.prepareToInsert(object);
        validateURI(ro);
    }
                
    protected void prepareToUpdate(Object object) throws RegistryException {        
        ServiceBindingType ebServiceBindingType = (ServiceBindingType)object;
        super.prepareToUpdate(object);
        validateURI(ebServiceBindingType);
    }
    
    protected void validateURI(ServiceBindingType object) throws RegistryException {        
        
        // check those ExternalLink with http url
        String check = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.ServiceBindingDAO.checkURLs");

        if (check.equalsIgnoreCase("true")) {
            ArrayList<ServiceBindingType> objs = new ArrayList<ServiceBindingType>();
            objs.add(object);
            ArrayList<Object> invalidExtLinks = Utility.getInstance().validateURIs(objs);

            if (invalidExtLinks.size() > 0) {
                throw new UnresolvedURLsException(invalidExtLinks);
            }
        }

    }
                
    protected void deleteComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.deleteComposedObjects(ro);
        
        SpecificationLinkDAO specLinkDAO = new SpecificationLinkDAO(context);
        specLinkDAO.setParent(object);
        specLinkDAO.deleteByParent();
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.insertComposedObjects(ro);
        
        ServiceBindingType serviceBinding = (ServiceBindingType)ro;
        SpecificationLinkDAO specLinkDAO = new SpecificationLinkDAO(context);
        specLinkDAO.setParent(object);
        specLinkDAO.insert(serviceBinding.getSpecificationLink());        
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        ServiceBindingType serviceBinding = (ServiceBindingType)ro;
            
        String stmtFragment = null;
                
        String serviceId = serviceBinding.getService();
        if (serviceId == null) {            
            serviceId = ((IdentifiableType)parent).getId();
        }
        
        if (serviceId == null) {            
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.serviceBindingHasNoParentService",
                    new Object[]{serviceBinding.getId()}));
        }
        
        String accessURI = serviceBinding.getAccessURI();
        if (accessURI != null) {
            accessURI = "'" + accessURI + "'";
        }

        
        String targetBindingId = serviceBinding.getTargetBinding();

        if (targetBindingId != null) {
            targetBindingId = "'" + targetBindingId + "'";
        }


        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO ServiceBinding " +
                super.getSQLStatementFragment(ro) +
                    ", '" + serviceId +  
                    "', " + accessURI + 
                    ", " + targetBindingId +
                    " ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE ServiceBinding SET " +
                super.getSQLStatementFragment(ro) +
                    ", service='" + serviceId + 
                    "', accessURI=" + accessURI + 
                    ", targetBinding=" + targetBindingId + 
                    " WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }    

    protected String checkServiceBindingReferences( 
        String serviceBindingId) throws RegistryException {
        String referencingServiceBindingId = null;
        PreparedStatement stmt = null;

        try {
            String sql = "SELECT id FROM ServiceBinding WHERE targetBinding=? AND targetBinding IS NOT NULL";
            stmt = context.getConnection().prepareStatement(sql);
            stmt.setString(1, serviceBindingId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                referencingServiceBindingId = rs.getString(1);
            }

            return referencingServiceBindingId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "service";
    }
    

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        if (!(obj instanceof ServiceBindingType)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ServiceBindingExpected",
                    new Object[]{obj}));
        }

        ServiceBindingType ebServiceBindingType = (ServiceBindingType) obj;

        super.loadObject( ebServiceBindingType, rs);

        String accessUri = null;

        try {
            accessUri = rs.getString("accessuri");
            ebServiceBindingType.setAccessURI(accessUri);

            String targetBindingId = rs.getString("targetBinding");

            if (targetBindingId != null) {
                ObjectRefType ebTargetObjectRefType = bu.rimFac.createObjectRefType();
                context.getObjectRefs().add(ebTargetObjectRefType);
                ebTargetObjectRefType.setId(targetBindingId);
                ebServiceBindingType.setTargetBinding(targetBindingId);
            }

            String serviceId = rs.getString("service");

            if (serviceId != null) {
                ObjectRefType ebServiceObjectRefType = bu.rimFac.createObjectRefType();
                context.getObjectRefs().add(ebServiceObjectRefType);
                ebServiceObjectRefType.setId(serviceId);
                ebServiceBindingType.setService(serviceId);
            }
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }

        boolean returnComposedObjects = context.getResponseOption().isReturnComposedObjects();

        if (returnComposedObjects) {
            SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
            specificationLinkDAO.setParent(ebServiceBindingType);
            List<Object> specLinks = specificationLinkDAO.getByParent();
            Iterator<Object> iter = specLinks.iterator();

            while (iter.hasNext()) {
                SpecificationLinkType ebSpecificationLinkType = (SpecificationLinkType) iter.next();
                ebServiceBindingType.getSpecificationLink().add(ebSpecificationLinkType);
            }
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ServiceBindingType ebServiceBindingType = bu.rimFac.createServiceBindingType();
        
        return ebServiceBindingType;
    }
    
}
