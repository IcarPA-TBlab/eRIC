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
package it.cnr.icar.eric.server.container;

/**
 * Base interface for all container-specific life cycle listener
 * implementations.  Provides getters and setters for values current {@link
 * Helper} and container-specific {@code GenericListener} implementations
 * require.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.1 $
 */
public class GenericListener {
    /*
     * overall control parameters from Context
     */

    /**
     * When this parameter is true, the database server is
     * started as the life cycle begins.
     */
    protected boolean startupServer = false;
    /**
     * When this parameter is true, the database is shut down (check
     * pointed) at the end of the life cycle.
     */
    protected boolean shutdownDatabase = false;
    /**
     * When this parameter is true, the database server is shut
     * down at the end of the life cycle.
     * Ignored if {@link #startupServer} is false
     */
    protected boolean shutdownServer = false;


    /*
     * parameters deciding which specific Helper class should be used
     */

    /**
     * String containing database-specific {@link Helper} class name.  This
     * class contains methods which should be executed when life cycle
     * start and end events occur.  This parameter must have a value.
     */
    protected String helperClass;

    /**
     * String containing database-specific {@link Helper} class name.  This
     * class is used if the {@link #helperClass} cannot be instantiated.
     */
    protected String helperFallbackClass;


    /*
     * parameters deciding how to shut the database down
     */

    /**
     * String containing the class name needed to perform database
     * shut down.
     * Ignored if {@link #shutdownDatabase} is false.
     */
    protected String databaseClass;
    /**
     * String containing the password needed to perform database
     * shut down.
     * Ignored if {@link #shutdownDatabase} is false.
     */
    protected String databasePassword;
    /**
     * String containing the URL needed to perform database shut
     * down.
     * Ignored if {@link #shutdownDatabase} is false.
     */
    protected String databaseShutdownURL;
    /**
     * String containing the username needed to perform database
     * shut down.
     * Ignored if {@link #shutdownDatabase} is false.
     */
    protected String databaseUsername;


    /**
     * class constructor
     */
    public GenericListener() {
    }


    /*
     * Getters and setters for parameters
     */

    /**
     * Retrieve current {@link #startupServer} parameter value.
     * When this parameter is true, the database server is
     * started as the life cycle begins.
     *
     * @return current value of {@link #startupServer} parameter
     */
    public boolean isStartupServer() {
	return startupServer;
    }

    /**
     * Set new value for {@link #startupServer} parameter.
     * When this parameter is true, the database server is
     * started as the life cycle begins.
     *
     * @param val new boolean for {@link #startupServer} parameter
     */
    public void setStartupServer(boolean val) {
	startupServer = val;
    }

    /**
     * Retrieve current {@link #shutdownDatabase} parameter value.
     * When this parameter is true, the database is shut down (check
     * pointed) at the end of the life cycle.
     *
     * @return current value of {@link #shutdownDatabase} parameter
     */
    public boolean isShutdownDatabase() {
	return shutdownDatabase;
    }

    /**
     * Set new value for {@link #shutdownDatabase} parameter.
     * When this parameter is true, the database is shut down (check
     * pointed) at the end of the life cycle.
     *
     * @param val new boolean for {@link #shutdownDatabase} parameter
     */
    public void setShutdownDatabase(boolean val) {
	shutdownDatabase = val;
    }

    /**
     * Retrieve current {@link #shutdownServer} parameter value.
     * When this parameter is true, the database server is shut
     * down at the end of the life cycle.
     *
     * @return current value of {@link #shutdownServer} parameter
     */
    public boolean isShutdownServer() {
	return shutdownServer;
    }

    /**
     * Set new value for {@link #shutdownServer} parameter.
     * When this parameter is true, the database server is shut
     * down at the end of the life cycle.
     *
     * @param val new boolean for {@link #shutdownServer} parameter
     */
    public void setShutdownServer(boolean val) {
	shutdownServer = val;
    }


    /**
     * Retrieve current {@link #helperClass} parameter value.
     * This parameter provides the database-specific {@link Helper} class
     * name.  This class contains methods which should be executed when
     * life cycle start and end events occur.  This parameter must have a
     * value.
     *
     * @return current value of {@link #helperClass} parameter
     */
    public String getHelperClass() {
	return helperClass;
    }

    /**
     * Set new value for {@link #helperClass} parameter.
     * This parameter provides the database-specific {@link Helper} class
     * name.  This class contains methods which should be executed when
     * life cycle start and end events occur.  This parameter must have a
     * value.
     *
     * @param val new string for {@link #helperClass} parameter
     */
    public void setHelperClass(String val) {
	helperClass = val;
    }

    /**
     * Retrieve current {@link #helperFallbackClass} parameter value.
     * This parameter provides the database-specific {@link Helper} class
     * name.  This class contains methods which should be executed when
     * life cycle start and end events occur.
     *
     * @return current value of {@link #helperFallbackClass} parameter
     */
    public String getHelperFallbackClass() {
	return helperFallbackClass;
    }

    /**
     * Set new value for {@link #helperFallbackClass} parameter.
     * This parameter provides the database-specific {@link Helper} class
     * name.  This class is used if the {@link #helperClass} cannot be
     * instantiated.
     *
     * @param val new string for {@link #helperFallbackClass} parameter
     */
    public void setHelperFallbackClass(String val) {
	helperFallbackClass = val;
    }


    /**
     * Retrieve current {@link #databaseClass} parameter value.
     * This parameter provides the class name needed to perform database
     * shut down.
     *
     * @return current value of {@link #databaseClass} parameter
     */
    public String getDatabaseClass() {
	return databaseClass;
    }

    /**
     * Set new value for {@link #databaseClass} parameter.
     * This parameter provides the class name needed to perform database
     * shut down.
     *
     * @param val new string for {@link #databaseClass} parameter
     */
    public void setDatabaseClass(String val) {
	databaseClass = val;
    }

    /**
     * Retrieve current {@link #databasePassword} parameter value.
     * This parameter provides the password needed to perform database
     * shut down.
     *
     * @return current value of {@link #databasePassword} parameter
     */
    public String getDatabasePassword() {
	return databasePassword;
    }

    /**
     * Set new value for {@link #databasePassword} parameter.
     * This parameter provides the password needed to perform database
     * shut down.
     *
     * @param val new string for {@link #databasePassword} parameter
     */
    public void setDatabasePassword(String val) {
	databasePassword = val;
    }

    /**
     * Retrieve current {@link #databaseShutdownURL} parameter value.
     * This parameter provides the URL needed to perform database shut
     * down.
     *
     * @return current value of {@link #databaseShutdownURL} parameter
     */
    public String getDatabaseShutdownURL() {
	return databaseShutdownURL;
    }

    /**
     * Set new value for {@link #databaseShutdownURL} parameter.
     * This parameter provides the URL needed to perform database shut
     * down.
     *
     * @param val new string for {@link #databaseShutdownURL} parameter
     */
    public void setDatabaseShutdownURL(String val) {
	databaseShutdownURL = val;
    }

    /**
     * Retrieve current {@link #databaseUsername} parameter value.
     * This parameter provides the username needed to perform database
     * shut down.
     *
     * @return current value of {@link #databaseUsername} parameter
     */
    public String getDatabaseUsername() {
	return databaseUsername;
    }

    /**
     * Set new value for {@link #databaseUsername} parameter.
     * This parameter provides the username needed to perform database
     * shut down.
     *
     * @param val new string for {@link #databaseUsername} parameter
     */
    public void setDatabaseUsername(String val) {
	databaseUsername = val;
    }
}
