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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;

/**
 * Represents a link or reference to a technical specification used within a SpecificationLinkBinding.
 * It serves the same purpose as the union of tModelInstanceInfo and instanceDetails in
 * UDDI.
 *
 * <p><DL><DT><B>Capability Level: 0 </B><DD>This interface is required to be implemented by JAXR Providers at or above capability level 0.</DL>
 *
 * @see <{Concept}>
 * @author Farrukh S. Najmi
 */
class SpecificationLinkDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(SpecificationLinkDAO.class);

    /**
     * Use this constructor only.
     */
    SpecificationLinkDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "SpecificationLink";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
   
    protected void deleteComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.deleteComposedObjects(ro);
        
        //Delete description atribute for the specified objects
        UsageDescriptionDAO usageDescriptionDAO = new UsageDescriptionDAO(context);
        usageDescriptionDAO.setParent(ro);
        usageDescriptionDAO.deleteByParent();

        UsageParameterDAO usageParameterDAO = new UsageParameterDAO(context);
        usageParameterDAO.setParent(ro);
        usageParameterDAO.deleteByParent();
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.insertComposedObjects(ro);
        
        SpecificationLinkType specLink = (SpecificationLinkType)ro;
        String id = specLink.getId();
        
        UsageDescriptionDAO usageDescriptionDAO = new UsageDescriptionDAO(context);
        usageDescriptionDAO.insert(id, specLink.getUsageDescription());

        UsageParameterDAO usageParameterDAO = new UsageParameterDAO(context);
        usageParameterDAO.insert(id, specLink.getUsageParameter());
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        SpecificationLinkType specLink = (SpecificationLinkType)ro;
            
        String stmtFragment = null;
                
        String serviceBinding = specLink.getServiceBinding();
        if (serviceBinding == null) {
            if (parent != null) {
                serviceBinding = ((ServiceBindingType)parent).getId();
            }
            else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.specificationLinkHasNoParentServiceBinding",
                        new Object[]{specLink.getId()}));
            }
        }
        
        String specificationObject = specLink.getSpecificationObject();
        if (specificationObject == null) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.specificationLinkHasNoPreentSpecificationLink",
                    new Object[]{specLink.getId()}));
        }
         
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO SpecificationLink " +
                super.getSQLStatementFragment(ro) +
                    ", '" + serviceBinding +  
                    "', '" + specificationObject + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE SpecificationLink SET " +
                super.getSQLStatementFragment(ro) +
                    ", serviceBinding='" + serviceBinding + 
                    "', specificationObject='" + specificationObject + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }    

    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "serviceBinding";
    }
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        if (!(obj instanceof SpecificationLinkType)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.SpecificationLinkExpected",
                    new Object[]{obj}));
        }

        SpecificationLinkType ebSpecificationLinkType = (SpecificationLinkType) obj;

        super.loadObject( ebSpecificationLinkType, rs);

        try {
            String specificationObjectId = rs.getString("specificationObject");
            ebSpecificationLinkType.setSpecificationObject(specificationObjectId);
            ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
            ebObjectRefType.setId(specificationObjectId);
            context.getObjectRefs().add(ebObjectRefType);
            
            
            String serviceBindingId = rs.getString("serviceBinding");
            if (serviceBindingId != null) {
                ObjectRefType ebServiceBindingObjectRefType = bu.rimFac.createObjectRefType();
                context.getObjectRefs().add(ebServiceBindingObjectRefType);
                ebServiceBindingObjectRefType.setId(serviceBindingId);
                ebSpecificationLinkType.setServiceBinding(serviceBindingId);
            }
            String specLinkId = rs.getString("id");
            if (specLinkId != null) {
               UsageDescriptionDAO usageDescriptionDAO = new UsageDescriptionDAO(context);
               usageDescriptionDAO.setParent(specLinkId);
               InternationalStringType usagesDescription = usageDescriptionDAO.getUsageDescriptionByParent(specLinkId);
               if(usagesDescription != null){ 
                   ebSpecificationLinkType.setUsageDescription(usagesDescription);
               }
               UsageParameterDAO usageParameterDAO = new UsageParameterDAO(context);
               usageParameterDAO.setParent(specLinkId);
               
               List<String> usageParameters = usageParameterDAO.getUsageParametersByParent(specLinkId);
               
               if (usageParameters != null) {
                   Iterator<String> iter = usageParameters.iterator();

                   while (iter.hasNext()) {
                        String usageParam = iter.next();
                        ebSpecificationLinkType.getUsageParameter().add(usageParam);
                   }
               }
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
        SpecificationLinkType ebSpecificationLinkType = bu.rimFac.createSpecificationLinkType();
        
        return ebSpecificationLinkType;
    }
}
