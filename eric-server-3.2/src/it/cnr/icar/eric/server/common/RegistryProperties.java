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

import it.cnr.icar.eric.common.AbstractProperties;
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.File;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Abstraction for Registry Configuration properties.
 *
 * Initial implementation just uses Java Property file,
 * future implementation might acquire configuration info
 * from XML. Thus, all java property methods are called
 * via this abstraction.
 *
 * Registry Property File Search Order
 * 1. If Property "eric.properties", this value is used as the filename
 * to load the eric properties from. Skip to step 3 if this property is
 * set and the properties successfully loaded.
 * 2. If file Property{user.home}/eric.properties exists, the properties
 * are loaded from here.
 * 3. System default properties are read from the CLASSPATH. The first
 * file named it/cnr/icar/eric/server/common/eric.properties will be used to set
 * default Registry properties. These properties are overriden by the
 * same named property set by either steps 1 or 2.
 *
 * When adding a new property, be sure to set a default value for the
 * property in DEFAULT_PROPERTY_RESOURCE.
 *
 * Property Priority
 * 1. Highest priority: any java system properties including command line
 * properties set with -D
 * 2. Medium priority: any properties set in {user.home}/eric.properties
 * 3. Lowest priority: any properties set in the Registry default property file
 */
public class RegistryProperties extends AbstractProperties {
    private static Log log = LogFactory.getLog("it.cnr.icar.eric.server.common.RegistryProperties");
    private static final String PROPERTY_FILE_NAME = "eric.properties";
    private static final String DEFAULT_PROPERTY_FILE_NAME = "eric-defaults.properties";
    private static final String DEFAULT_PROPERTY_RESOURCE = "it/cnr/icar/eric/server/common/eric-defaults.properties";
    private static RegistryProperties instance = null;
    
    /**
     * Cache RegistryProperties.
     */
    private RegistryProperties() {
        super();
        initProperties();
    }
        
    /**
     * Initializes the properties performing the full loading sequence. 
     */
    protected void initProperties() {
        // Complete eric-common loading sequence at first
        Properties commonProps = CommonProperties.staticLoadProperties(getClass().getClassLoader(), new Properties());

        // Load Provider Properties
        props = loadProperties(commonProps);

        // initializes <eric.home>
        initEricHomeDir(props);

        // Substitute variables
        substituteVariables(props);
        
        logProperties("Registry properties", props);
    }

    /** This method is used to load properties. It returns a Properties
      * object that should be cached in memory.  It is called by
      * the RegistryProperties() constructor and by reloadProperties()
      *
      * @param defaultProps a default list of properties
      *
      * @return a Properties object loaded from a predefined resource
      */
    protected Properties loadProperties(final Properties defaultProps) {
        log.info(ServerResourceBundle.getInstance().getString("message.LoadingEricProperties"));
        Properties properties = new Properties(defaultProps);
        
        // Start from default properties
        loadDefaultProperties(properties);
        
        // Props from resource at classpath root
        boolean resLoaded = loadResourceProperties(getClass().getClassLoader(), properties, PROPERTY_FILE_NAME);
        
        if (!resLoaded) {
            // Load properties from file system
            loadFileProperties(properties, new File(getPropertyFileName(properties)));        	
        }
        
            //loadFileProperties(properties, new File(getDefaultPropertyFileName()));
            //loadFileProperties(properties, new File(getPropertyFileName()));

        //TODO: should it load all or subset
        // we don't really want all the system props, should we weed them out?
        loadSystemProperties(properties);

        return properties;
    }

    /** 
      * Check property eric.properties for a property file name.
      * Default property file name is <eric.home>/eric.properties.
      */
    private String getPropertyFileName(Properties properties) {
        String propertyFileName = getEricHome(properties) + "/" + PROPERTY_FILE_NAME;
        propertyFileName = System.getProperty(PROPERTY_FILE_NAME, propertyFileName);
        return propertyFileName;
    }

    private String getPropertyFileName() {
         String propertyFileName = getEricHome() + "/conf/" + PROPERTY_FILE_NAME;
        return propertyFileName;
    }

    private String getDefaultPropertyFileName() {
        String propertyFileName = getEricHome() + "/conf/" + DEFAULT_PROPERTY_FILE_NAME;
        return propertyFileName;
    }

    /**
     * Replace pre-defined variables in property values with the variable value from the
     * corresponding property.
     */
    private void substituteVariables(Properties properties) {
        // at coding time this would replace $user.home and $eric.home
        CommonProperties.staticSubstituteVariables(properties);
        // add more substitutions here if required, like this
        //substituteVariables(properties, "$myVar", "newValue");
    }

    /**
     * Implement Singleton class, this method is only way to get this object.
     */
    public synchronized static RegistryProperties getInstance() {
        if (instance == null) {
            instance = new RegistryProperties();
        }
        return instance;
    }

    /**
     * Load default common properties, default properties from
     * DEFAULT_PROPERTY_RESOURCE and add them all to 'properties'.
     *
     * @param properties and existing property set to be updated.
     */
    public void loadDefaultProperties(Properties properties) {
        loadResourceProperties(getClass().getClassLoader(), properties
            , RegistryProperties.DEFAULT_PROPERTY_RESOURCE);
    }
    
}
