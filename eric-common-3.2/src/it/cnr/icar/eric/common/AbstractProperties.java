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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract class for configuration properties. Contains common methods
 * refactored from other properties classes, i.e. RegistryProperties,
 * ProviderProperties.
 *
 * @author  Diego Ballve / Republica Corp
 */
public abstract class AbstractProperties {
    private static final Log log = LogFactory.getLog(AbstractProperties.class);

    //Do not do following as it causes cyclic dependency related problem.
    //protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();

    /** Default home dir, relative to user home. */
    protected static final String DEFAULT_ERIC_HOME = "eric";
    /** Property containing full path to eric home directory. */
    protected static final String ERIC_HOME_KEY = "eric.home";

    protected Properties props = new Properties();

    protected AbstractProperties() {
    }


    /**
     * Checks if a directory for the given name exists. If not, try to create it.
     *
     * @param homeDir A File descriptor for the directory to be checked.
     * @return Full path to the directory.
     * @throw JAXRException if it fails to find and create the directory.
     */
    protected static String initHomeDir (String propName, File homeDir) {
        try {
            if (!homeDir.exists()) {
                if (!homeDir.mkdirs()) {
                    throw new RuntimeException(CommonResourceBundle.getInstance().getString("message.createDirectory",
                            new String[] {propName, homeDir.getPath()}));
                }
            } else {
                if (!homeDir.isDirectory()) {
                    throw new RuntimeException(CommonResourceBundle.getInstance().getString("message.accessDirectory",
                            new String[] {propName, homeDir.getPath()}));
                }
            }
            String homeDirStr = homeDir.getCanonicalPath();
            return homeDirStr;
        } catch (SecurityException se) {
            throw new RuntimeException(CommonResourceBundle.getInstance().getString("message.permissionDenied",
                            new String[] {propName, homeDir.getPath(), se.toString()}));
        } catch (IOException io) {
            throw new RuntimeException(CommonResourceBundle.getInstance().getString("message.createAccessDirectory",
                            new String[] {propName, homeDir.getPath(), io.toString()}));
        }
    }

    protected static void initEricHomeDir(Properties properties) {
        //String ericHome = getEricHome(properties);
        String ericHome = getEricHome();
        File ericHomeFile = new File(ericHome);
        initHomeDir(ERIC_HOME_KEY, ericHomeFile);
        properties.put(ERIC_HOME_KEY, ericHome);
    }

    /**
     * Replace pre-defined variables in property values with the variable value from the
     * corresponding property.
     *
     * @param properties the Properties object to be changed
     * @param oldKeyPrefix String to be replaced from properties' keys
     * @param newKeyPrefix replacement String
     *
     */
    protected static void substituteVariables(Properties properties, String oldKeySubstring, String newKeySubstring) {
        //Iterate and replace the variable
        int oldKeySubstringSize = oldKeySubstring.length();
        for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            //System.err.println("key = " + key + " value = " + value);
            if (key.startsWith("eric.")) {
                int index = -1;
                boolean modified = false;
                while (true) {
                    index = value.indexOf(oldKeySubstring);
                    if (index == -1) {
                        break;
                    }
                    value = value.substring(0, index) + newKeySubstring +
                        value.substring(index + oldKeySubstringSize);
                    modified = true;
                }
                if (modified) {
                    properties.put(key, value);
                }
            }
        }
    }

    protected static String substituteVariable(String value, String oldKeySubstring, String newKeySubstring) {
        if (value == null) {
            return null;
        }
        int oldKeySubstringSize = oldKeySubstring.length();
        while (true) {
            int index = value.indexOf(oldKeySubstring);
            if (index == -1) {
                break;
            }
            value = value.substring(0, index) + newKeySubstring +
                value.substring(index + oldKeySubstringSize);
        }
        return value;
    }




    /**
     * Getter for property 'propertyName'. Returns null if property is not defined.
     *
     * @param propertyName the property key (name).
     *
     * @return the value in this property list with the specified key value.
     */
    public String getProperty(String propertyName) {
        return props.getProperty(propertyName);
    }

    /**
     * Getter for property 'propertyName', with 'def' being returned if property
     * not defined.
     *
     * @param propertyName the property key (name).
     * @param def a default value
     *
     * @return the value in this property list with the specified key value.
     */
    public String getProperty(String propertyName, String def) {
        return props.getProperty(propertyName, def);
    }

    /** Convenience method. */
    public Iterator<String> getPropertyNamesStartingWith(String prefix) {
        List<String> lst = new LinkedList<String>();
        Enumeration<?> e = props.propertyNames();

        while (e.hasMoreElements()) {
            String propertyName = (String) e.nextElement();

            if (propertyName.startsWith(prefix)) {
                lst.add(propertyName);
            }
        }

        return lst.iterator();
    }

    /**
     * Initializes the properties performing the full loading sequence.
     */
    protected abstract void initProperties();

    /**
     * Load property settings, with the source for properties being
     * provided by each implementation.
     *
     * @param properties a default list of properties
     *
     * @return a Properties object loaded from a predefined resource
     */
    protected abstract Properties loadProperties(Properties defaultProps);

    /**
     * This method is used to reload properties into memory.
     */
    public void reloadProperties() {
        initProperties();
    }

    /**
     * Load default property settings.
     *
     * @return a Properties object loaded from a predefined resource
     */
    protected abstract void loadDefaultProperties(Properties properties);

    /**
     * Loads properties from resourceName (from classpath) to 'properties'.
     *
     * @param classLoader the ClassLoader to use when getting resource
     * @param properties existing properties (might get overwritten)
     * @param resourceName the resource name, to be loaded from classpath
     * @return True if load ok. False otherwise.
     */
    protected static boolean loadResourceProperties(ClassLoader classLoader,
        Properties properties, String resourceName) {
        log.trace(CommonResourceBundle.getInstance().getString("message.LoadPropsFromClasspath", new Object[]{resourceName}));
        try {
            InputStream is = classLoader.getResourceAsStream(resourceName);
            if (is == null) {
                throw new JAXRException(CommonResourceBundle.getInstance().getString("message.resourceNotFound",
                            new String[] {resourceName}));
            }
            properties.load(is);
            is.close();
            return true;
        } catch (Exception e) {
            log.debug(CommonResourceBundle.getInstance().getString("message.IgnoringDueToException", new Object[]{resourceName, e.toString()}));
            return false;
        }
    }

    /**
     * Loads properties from file to 'properties'.
     *
     * @param properties existing properties (might get overwritten)
     * @param file the file, to be loaded from file system
     * @return True if load ok. False otherwise.
     */
    protected static boolean loadFileProperties(Properties properties, File file) {
        log.trace(CommonResourceBundle.getInstance().getString("message.LoadPropsFromFile", new Object[]{file.getPath()}));
        try {
            InputStream is = new FileInputStream(file);
            properties.load(is);
            is.close();
            return true;
        } catch (Exception e) {
            // silently ignore missing client properties
            log.debug(CommonResourceBundle.getInstance().getString("message.IgnoringDueToException", new Object[]{file.getPath(), e.toString()}));
            return false;
        }
    }

    /**
     * Loads system properties to 'properties'.
     *
     * @param properties existing properties (might get overwriten)
     * @return True if load ok. False otherwise.
     */
    protected static boolean loadSystemProperties(Properties properties) {
        log.trace(CommonResourceBundle.getInstance().getString("message.LoadPropsFromSystemProps"));
        try {
            AccessController.doPrivileged(
                new ReadSystemPropsPrivilegedAction(properties));
            return true;
        } catch (Exception e) {
            // silently ignore missing System properties
            log.debug(CommonResourceBundle.getInstance().getString("message.IgnoringSystemPropsDueToException", new Object[]{e.toString()}));
            return false;
        }
    }

    /**
     * Logs all properties as name=value pairs. Requires debug level.
     *
     * @param propsName name for the properties set.
     * @param props Properties to be logged.
     */
    protected static void logProperties(String propsName, Properties props) {
        if (log.isDebugEnabled()) {
            log.debug("==================================================");
            log.debug(propsName + ":");
            Iterator<Object> names = (new TreeSet<Object>(props.keySet())).iterator();
            while (names.hasNext()) {
                String name = (String)names.next();
                String value = props.getProperty(name);
                log.debug(name + "=" + value);
            }
            log.debug("==================================================");
        }
    }

    public Properties cloneProperties() {
        Properties clone = new Properties();
        clone.putAll(props);
        return clone;
    }

    /**
     * Getter for ericHome. If null, sets default value and return.
     *
     * @return String value for registryHome property.
     */
    protected static String getEricHome(Properties properties) {
        String ericHome = properties.getProperty(ERIC_HOME_KEY);
        //TODO: needs doPrivileged?!
        ericHome = System.getProperty(ERIC_HOME_KEY, ericHome);
        ericHome = substituteVariable(ericHome, "$user.home", getUserHome());
        if (ericHome == null) {
            throw new RuntimeException(CommonResourceBundle.getInstance().getString("message.propertyNotDefined",
                            new String[] {ERIC_HOME_KEY}));
        }
        return ericHome;
    }

    protected static String getEricHome() {
        String ericHome = System.getenv("ERIC_HOME");
        if (ericHome == null) {
            throw new RuntimeException("ERIC_HOME is not set");
        }
        return ericHome;
    }
    
    protected static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * @see java.util.Properties#put(Object,Object)
     */
    public void put(Object key, Object value) {
        //todo: consider removing
        this.props.put(key, value);
    }

    /**
     * @see java.util.Properties#remove(Object)
     */
    public void remove(Object key) {
        this.props.remove(key);
    }

    /**
     * Inner class to load System properties.
     */
    private static class ReadSystemPropsPrivilegedAction
	implements PrivilegedAction<Object> {
	private Properties props;

	public ReadSystemPropsPrivilegedAction(Properties props) {
	    this.props = props;
	}

	public Object run() {
	    props.putAll(System.getProperties());
	    return null;
	}
    }
}
