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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * {@link Helper} implementation providing Derby-specific support.
 * Initializes ${derby.system.home} if (container-specific) caller believes
 * this is necessary.  During shutdown, checkpoints the Derby
 * database. Originally copied in part from {@link
 * it.cnr.icar.eric.server.persistence.rdb.SQLPersistenceManagerImpl}.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.1 $
 * @see Helper
 */
class DerbyHelper implements Helper {
    // for logging
    private static Log log = LogFactory.getLog(DerbyHelper.class);
    private static String rbName =
	DerbyHelper.class.getPackage().getName() + ".ResourceBundle";
    private static ResourceBundle rb = ResourceBundle.getBundle(rbName);

    /**
     * {@link GenericListener} object from which this {@link DerbyHelper}
     * should retrieve necessary properties.  This class invokes various
     * listenser.getDatabase*() methods but none of the setter methods.
     */
    protected GenericListener listener;

    /**
     * class constructor
     */
    public DerbyHelper() {
    }

    /**
     * {@inheritDoc}
     * In this case, set ${derby.system.home} System property to provided
     * {@code configDirectory} value, if any.  Does nothing for callers
     * with a consistent current directory -- for which this property
     * setting and the parameter value are unecessary.
     */
    public void initialize(String configDirectory,
			   GenericListener listener) {
	if (null != configDirectory && 0 < configDirectory.length()) {
	    try {
		AccessController.
		    doPrivileged(new SetDerbySystemProp(configDirectory));
	    } catch (Exception e) {
		log.error(rb.getString("message.failureSettingDerbyHome"), e);
	    }
	}

	this.listener = listener;
    }

    /**
     * inner class used to perform actual System property setting while
     * avoiding privilege exceptions
     */
    private static class SetDerbySystemProp implements PrivilegedAction<Object> {
	private String propertyValue;

	public SetDerbySystemProp(String propertyValue) {
	    this.propertyValue = propertyValue;
	}

	public Object run() {
	    System.setProperty("derby.system.home", propertyValue);
	    return null;
	}
    }

    /**
     * Log appropriate information about a SQLException.  In some cases,
     * the exception may be a normal consequence of connecting to the
     * shutdown URL.  Logs something (message.stoppedDatabase at least) in
     * all cases.
     *
     * @param sqlException the {@link java.sql.SQLException} which should
     * be discarded (as normal) or logged
     */
    protected void logDatabaseException(SQLException sqlException) {
	// In general, we do not know much about what these exceptions mean
	if (log.isDebugEnabled()) {
	    while (null != sqlException) {
		// ??? add a message for unrecognized SQL exceptions
		log.error(sqlException);
		log.debug("Error Code: " + sqlException.getErrorCode());
		log.debug("SQL State: " + sqlException.getSQLState());
		sqlException = sqlException.getNextException();
	    }
	} else {
	    // ??? add a message for unrecognized SQL exceptions
	    log.error(sqlException);
	}
    }

    /**
     * {@inheritDoc}
     * In this case, check point the Derby database.
     */
    public void shutdownDatabase() {
	if (null == listener.getDatabaseClass() ||
	    null == listener.getDatabaseShutdownURL() ||
	    0 == listener.getDatabaseClass().length() ||
	    0 == listener.getDatabaseShutdownURL().length()) {
	    log.error(rb.getString("message.incorrectShutdownConfig"));
	    return;
	}

	try {
	    Class.forName(listener.getDatabaseClass());
	} catch (ClassNotFoundException e) {
	    // JDBC driver not found
	    // ??? Add message for this case
	    log.error(e);
	    return;
	} catch (Exception e) {
	    // ??? Add message for this case
	    log.error(e);
	    return;
	}

	Connection connection = null;
	try {
	    log.info(MessageFormat.
		     format(rb.getString("message.stoppingDatabase"),
			    new Object[] {listener.getDatabaseShutdownURL()}));
	    if (null == listener.getDatabaseUsername() ||
		0 == listener.getDatabaseUsername().length()) {
		connection = DriverManager.
		    getConnection(listener.getDatabaseShutdownURL());
	    } else {
		connection = DriverManager.
		    getConnection(listener.getDatabaseShutdownURL(),
				  listener.getDatabaseUsername(),
				  listener.getDatabasePassword());
	    }
	    // do not expect to reach this point because Derby always
	    // raises an exception when shutting down the database; may
	    // mean the configured shutdown URL does not include
	    // shutdown=true parameter (a configuration error)
	    log.error(rb.getString("message.incorrectShutdownURLConfig"));
	} catch (SQLException e) {
	    logDatabaseException(e);
	} catch (Exception e) {
	    // ??? Add message for this case
	    log.error(e);
	} finally {
	    if (null != connection) {
		try {
		    connection.close();
		} catch (Exception e) {
		    // Do nothing
			connection = null;
		}
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    public boolean startupServer() {
	// Do nothing
	return false;
    }

    /**
     * {@inheritDoc}
     */
    public void shutdownServer() {
	// Do nothing
    }
}
