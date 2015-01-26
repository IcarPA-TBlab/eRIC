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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;


/**
 *
 * @author  kwalsh
 * @author Adrian Chong
 * @version
 */
class ServiceDAO extends RegistryObjectDAO {
    
    /**
     * Use this constructor only.
     */
    ServiceDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Service";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    protected void deleteComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.deleteComposedObjects(ro);
        
        ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
        serviceBindingDAO.setParent(ro);
        serviceBindingDAO.deleteByParent();
    }
    
    protected void insertComposedObjects(Object object)  throws RegistryException {
        RegistryObjectType ro = (RegistryObjectType)object;
            
        super.insertComposedObjects(ro);
        
        //TODO: Need to pass ServiceBinding and Service
        ServiceType service = (ServiceType)ro;
        ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
        serviceBindingDAO.setParent(ro);
        serviceBindingDAO.insert(service.getServiceBinding());        
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        @SuppressWarnings("unused")
		ServiceType serviceBinding = (ServiceType)ro;
            
        String stmtFragment = null;
                        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO Service " +
                super.getSQLStatementFragment(ro) +
                    " ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE Service SET " +
                super.getSQLStatementFragment(ro) +
                    " WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }    
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        if (!(obj instanceof ServiceType)) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ServiceExpected",
                    new Object[]{obj}));
        }

        ServiceType ebServiceType = (ServiceType) obj;

        super.loadObject( ebServiceType, rs);

        boolean returnComposedObjects = context.getResponseOption().isReturnComposedObjects();

        if (returnComposedObjects) {
            ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
            serviceBindingDAO.setParent(ebServiceType);
            List<Object> serviceBindings = serviceBindingDAO.getByParent();
            Iterator<Object> iter = serviceBindings.iterator();

            while (iter.hasNext()) {
                ServiceBindingType sb = (ServiceBindingType) iter.next();
                ebServiceType.getServiceBinding().add(sb);
            }
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ServiceType ebServiceType = bu.rimFac.createServiceType();
        
        return ebServiceType;
    }
}
