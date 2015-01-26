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

package it.cnr.icar.eric.server.common;

import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;

import java.util.Properties;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;


/**
 * Manages outbound JAXR Connections to other registries.
 * For now does very little other than create a new Connection each time.
 * In future it may be optimized to cache Connections.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConnectionManager {
    
    //maps String home to JAXR Connection
    //private HashMap connectionMap = new HashMap();
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private ConnectionManager _connectionManagerImpl; */
    private static ConnectionManager instance = null;
    
    /** Creates a new instance of ConnectionManager */
    protected ConnectionManager() {
    }
    
    public synchronized static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }

        return instance;
    }        
    
    public Connection getConnection(String home) throws JAXRException {
        return createConnection(home);
    }
    
    /** Setup JAXR Connection for target registry */
    private Connection createConnection(String home) throws JAXRException {
        
        //TODO: Need to use SAML SSO here
        String queryManagerURL = home + "/soap";

        ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();
        Properties props = new Properties();
        props.put("javax.xml.registry.queryManagerURL", queryManagerURL);
        props.put("javax.xml.registry.lifeCycleManagerURL", queryManagerURL);
        connFactory.setProperties(props);

        Connection connection = connFactory.createConnection();
        ((ConnectionImpl)connection).setLocalCallMode(false);
        return connection;
    }
    
}
