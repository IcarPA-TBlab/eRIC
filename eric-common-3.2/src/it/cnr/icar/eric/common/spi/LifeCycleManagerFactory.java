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
package it.cnr.icar.eric.common.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.cnr.icar.eric.common.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author  najmi
 */
public class LifeCycleManagerFactory {
    private static LifeCycleManagerFactory instance; //singleton instance
    public static final String LIFECYCLE_MANAGER_CLASS_PROPERTY = "eric.common.LifeCycleManagerFactory.lifeCycleManagerClass";
    private LifeCycleManager lcm = null;
    private static final Log log = LogFactory.getLog(LifeCycleManagerFactory.class);

    /** Creates a new instance of LifeCycleManager */
    protected LifeCycleManagerFactory() {
    }

    public synchronized static LifeCycleManagerFactory getInstance() {
        if (instance == null) {
            instance = new LifeCycleManagerFactory();
        }

        return instance;
    }

    public LifeCycleManager getLifeCycleManager() {
        if (lcm == null) {
            synchronized (this) {
                if (lcm == null) {
                    String pluginClass = null;
                    try {
                        pluginClass = CommonProperties.getInstance()
                                                               .getProperty(LIFECYCLE_MANAGER_CLASS_PROPERTY);

                        lcm = (LifeCycleManager) createPluginInstance(pluginClass);
                    } catch (Exception e) {
                        String errmsg = "[LifeCycleManager] Cannot instantiate " +
                            "LifeCycleManager plugin class named: '" + pluginClass + "'. Please check that " +
                            "property '" + LIFECYCLE_MANAGER_CLASS_PROPERTY +
                            "' is correctly set in eric-common.properties file.";
                        log.error(errmsg, e);
                    }
                }
            }
        }

        return lcm;
    }

    /**
    * Creates the instance of the pluginClass
    */
    private Object createPluginInstance(String pluginClass)
        throws Exception {
        Object plugin = null;

        if (log.isDebugEnabled()) {
            log.debug("pluginClass = " + pluginClass);
        }

        Class<?> theClass = Class.forName(pluginClass);

        //try to invoke constructor using Reflection, 
        //if this fails then try invoking getInstance()
        try {
            Constructor<?> constructor = theClass.getConstructor((java.lang.Class[])null);
            plugin = constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            //log.warn("No accessible constructor. " +
            //    "invoking getInstance() instead.");

            Method factory = theClass.getDeclaredMethod("getInstance", (java.lang.Class[])null);
            plugin = factory.invoke((java.lang.Class[])null, new Object[0]);
        }

        return plugin;
    }
}
