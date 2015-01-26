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

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;


/**
 * An Registry instance represents an ebXML Registry.
 * Every ebXML Registry must have a Registry instance 
 * describing itself.
 *
 * @author Farrukh S. Najmi
 * 
 */
class RegistryDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(RegistryDAO.class);

    /**
     * Use this constructor only.
     */
    RegistryDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "Registry";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        RegistryType  registry = ( RegistryType)ro;
            
        String stmtFragment = null;
        
        Duration catalogingSyncLatency = registry.getCatalogingLatency();  
        String catalogingSyncLatencyString = (catalogingSyncLatency == null) ? "P1D" : catalogingSyncLatency.toString();

        String operator = registry.getOperator();

        Duration replicationSyncLatency = registry.getReplicationSyncLatency();
        String replicationSyncLatencyString = (replicationSyncLatency == null) ? "P1D" : replicationSyncLatency.toString();

        
        String specificationVersion = registry.getSpecificationVersion();
        String conformanceProfile = registry.getConformanceProfile();
                       
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO Registry " +
                super.getSQLStatementFragment(ro) +
                    ", '" +  catalogingSyncLatencyString + 
                    "', '" + conformanceProfile +                     
                    "', '" + operator + 
                    "', '" + replicationSyncLatencyString + 
                    "', '" + specificationVersion + 
                    "' ) ";            
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE Registry SET " +
                super.getSQLStatementFragment(ro) +
                    ",  catalogingSyncLatency='" +  catalogingSyncLatencyString +
                    "', conformanceProfile='" + conformanceProfile +                     
                    "', operator='" + operator + 
                    "', replicationSyncLatency='" + replicationSyncLatencyString + 
                    "', specificationVersion='" + specificationVersion + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof RegistryType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RegistryTypeExpected",
                        new Object[]{obj}));
            }

            RegistryType ebRegistryType = (RegistryType) obj;
            super.loadObject(obj, rs);

            String catalogingSyncLatencyString = rs.getString("catalogingSyncLatency");
            Duration catalogingSyncLatency = DatatypeFactory.newInstance().newDuration(catalogingSyncLatencyString);
            ebRegistryType.setCatalogingLatency(catalogingSyncLatency);
                
            String conformanceProfile = rs.getString("conformanceProfile");
            ebRegistryType.setConformanceProfile(conformanceProfile);

            String operator = rs.getString("operator");
            ebRegistryType.setOperator(operator);

            String replicationSyncLatencyString = rs.getString("replicationSyncLatency");
            Duration replicationSyncLatency = DatatypeFactory.newInstance().newDuration(replicationSyncLatencyString);
            ebRegistryType.setReplicationSyncLatency(replicationSyncLatency);
            
            String specificationVersion = rs.getString("specificationVersion");
            ebRegistryType.setSpecificationVersion(specificationVersion);            
        
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        RegistryType ebRegistryType = bu.rimFac.createRegistryType();
        
        return ebRegistryType;
    }
}
