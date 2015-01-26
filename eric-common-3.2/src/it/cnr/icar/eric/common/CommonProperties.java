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
package it.cnr.icar.eric.common;

import java.io.File;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A common class for configuration properties. Can be extended by server
 * and client for more specific property loading.
 *
 * For now only loading from DEFAULT_PROPERTY_RESOURCE.
 * //TODO: load from other places (FS, command line, registry server)
 *
 * @author  Diego Ballve / Republica Corp
 */
public class CommonProperties extends AbstractProperties {

    private static Log log = LogFactory.getLog("it.cnr.icar.eric.common.CommonProperties");    
    private static final String PROPERTY_FILE_NAME = "eric-common.properties";
    private static final String DEFAULT_PROPERTY_FILE_NAME = "eric-common-defaults.properties";
    private static final String DEFAULT_PROPERTY_RESOURCE = "it/cnr/icar/eric/common/eric-common-defaults.properties";

    private static CommonProperties instance = null;

    protected CommonProperties() {
        super();
        initProperties();
    }
        
    /**
     * Initializes the properties performing the full loading sequence. 
     */
    protected void initProperties() {
        props = loadProperties(new Properties());

        // initializes <eric.home>
        initEricHomeDir(props);

        // Substitute variables
        substituteVariables(props);
        
        logProperties("Common properties", props);
    }
    
    /**
     * Implement Singleton class, this method is only way to get this object.
     */
    public synchronized static CommonProperties getInstance() {
        if (instance == null) {
            instance = new CommonProperties();
        }
        return instance;
    }

    protected Properties loadProperties(final Properties props) {
        return staticLoadProperties(getClass().getClassLoader(), props);
    }
    
    public static Properties staticLoadProperties(ClassLoader classLoader, final Properties props) {
        log.trace(CommonResourceBundle.getInstance().getString("message.LoadingEric-commonProperties"));
        Properties properties = new Properties(props);
        
        // Start from default properties
        staticLoadDefaultProperties(classLoader, properties);
        
        // Props from resource at classpath root
        boolean resLoaded = loadResourceProperties(classLoader, properties, PROPERTY_FILE_NAME);
                
        if (!resLoaded) {
            // Load properties from file system
            loadFileProperties(properties, new File(getPropertyFileName(properties)));
        }
        
        //loadFileProperties(properties, new File(getPropertyFileName()));
        //loadFileProperties(properties, new File(getDefaultPropertyFileName()));
        
        //TODO: should it load all or subset
        loadSystemProperties(properties);

        return properties;
    }
    
    /** 
      * Check property eric.properties for a property file name.
      * Default property file name is <registry.home>/eric-common.properties.
      */
    private static String getPropertyFileName(Properties properties) {
        String propertyFileName = getEricHome(properties) + "/" + PROPERTY_FILE_NAME;
        propertyFileName = System.getProperty(PROPERTY_FILE_NAME, propertyFileName);
        return propertyFileName;
    }

    private static String getPropertyFileName() {
        String propertyFileName = getEricHome() + "/conf/" + PROPERTY_FILE_NAME;
        return propertyFileName;
    }

    private static String getDefaultPropertyFileName() {
        String propertyFileName = getEricHome() + "/conf/" + DEFAULT_PROPERTY_FILE_NAME;
        return propertyFileName;
    }
    
    /**
     * Replace pre-defined variables in property values with the variable value from the
     * corresponding property.
     */
    protected void substituteVariables(Properties properties) {
        staticSubstituteVariables(properties);
    }

    public static void staticSubstituteVariables(Properties properties) {
        //Iterate and replace allowed variables
        substituteVariables(properties, "$user.home", getUserHome());
        substituteVariables(properties, "$eric.home", getEricHome(properties));
        //substituteVariables(properties, "$eric.home", getEricHome());
    }
    
    /**
     * Load default property settings for this property list using the 
     * DEFAULT_PROPERTY_RESOURCE.
     *
     * @return a Properties object loaded from a predefined resource
     */
    protected void loadDefaultProperties(Properties properties) {
        staticLoadDefaultProperties(getClass().getClassLoader(), properties);
    }

    private static void staticLoadDefaultProperties(ClassLoader classLoader, Properties properties) {
        loadResourceProperties(classLoader, properties, DEFAULT_PROPERTY_RESOURCE);
    }
}
