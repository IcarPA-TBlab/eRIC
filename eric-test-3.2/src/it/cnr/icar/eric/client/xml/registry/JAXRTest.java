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


package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;

import java.util.Properties;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;



/**
 * Common base class for all JAXR tests
 *
 * To use the JAAS authentication mechanisms you must create a file ~/.java.login.config with following content
 *
 *  JAXRTest {
 *   com.sun.security.auth.module.KeyStoreLoginModule required debug=true keyStoreURL="file://c:/Docume~1/najmi/jaxr-ebxml/security/keystore.jks";
 *  };
 *
 * Note that the keyStoreURL must point to wherever your keySTore file is. The ~ home directory is teh one pointed to the 
 * user.home System property. On windows 2000 it is file://c:/Docume~1/<uour login>.
 *
 * The password dialog usually takes a little while to pop up and does not always appear on top of other windows.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class JAXRTest {
    
    Connection connection = null;
    RegistryService service = null;
    BusinessLifeCycleManager lcm = null;
    BusinessQueryManager bqm = null;
    DeclarativeQueryManager dqm = null;
    
    @SuppressWarnings("unused")
	private JAXRTest() {
        //Not allowed to be used
    }
    
    /** Creates new JAXRTest */
    public JAXRTest(Properties connectionProps) throws JAXRException {        
        createConnection(connectionProps);
    }
    
    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     */
    public void createConnection(Properties connectionProps) throws JAXRException {        
        if (connectionProps == null) {
            connectionProps = new Properties();
            connectionProps.put("javax.xml.registry.queryManagerURL", 
                "http://localhost:8080/eric/registry/soap"); //http://registry.csis.hku.hk:8201/ebxmlrr/registry/soap
        }
        
        ConnectionFactory connFactory = getConnectionFactory(connectionProps);
        connFactory.setProperties(connectionProps);
        connection = connFactory.createConnection();
        service = connection.getRegistryService();
        
        bqm = service.getBusinessQueryManager();
        dqm = service.getDeclarativeQueryManager();
        lcm = service.getBusinessLifeCycleManager();        
    }
    
    private ConnectionFactory getConnectionFactory(Properties connectionProps) throws JAXRException {
        //Get factory class
        ConnectionFactory connFactory =  JAXRUtility.getConnectionFactory();
        String url = (String)connectionProps.get("javax.xml.registry.queryManagerURL");
        if (url == null) {
            throw new JAXRException("Connection property javax.xml.registry.queryManagerURL not defined.");
        }      
        return connFactory;
    }
        
}
