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

import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/*
 *
 *
*/
class ConnectionPool {
    private Log log = LogFactory.getLog(this.getClass());
    @SuppressWarnings("unused")
	private String name;
    private String URL;
    private String user;
    private String password;
    private int transactionIsolation;
    private int initConns;
    private int maxConns;
    private int timeOut;
    private HashMap<Connection, String> checkedOutConnections = new HashMap<Connection, String>();
    private ArrayList<Connection> freeConnections = new ArrayList<Connection>();

    /**
    @param timeOut is the time in seconds after this has been elasped but the
    connection cannot be returned, getConnection() will return SQLException
    */
    public ConnectionPool(String name, String URL, String user,
        String password, int maxConns, int initConns, int timeOut, int transactionIsolation) {
        this.name = name;
        this.URL = URL;
        this.user = user;
        this.password = password;
        this.initConns = initConns;
        this.maxConns = maxConns;
        this.timeOut = (timeOut > 0) ? timeOut : 5;
        this.transactionIsolation = transactionIsolation;
        
        // initialise the pool
        for (int i = 0; i < initConns; i++) {
            try {
                Connection pc = newConnection();
                freeConnections.add(pc);
            } catch (SQLException e) {
                throw new java.lang.reflect.UndeclaredThrowableException(e,
                    ServerResourceBundle.
		       getInstance().getString("message.noConnectionAvailable",
			 new Object[]{ new Integer(freeConnections.size()),
				       new Integer(initConns)}));
            }
        }

        if (log.isInfoEnabled()) {
            log.info(ServerResourceBundle.getInstance().getString("message.DatabaseConnectionPoolingEnabled"));
            log.info(getStats());
        }
    }

    public Connection getConnection(String contextId) throws SQLException {
        try {
            //System.err.println("**********getConnection: contextId=" + contextId + " " + getStats());
            if ((maxConns < initConns) || (maxConns <= 0)) {
                throw new SQLException(
                    ServerResourceBundle.getInstance().getString("message.invalidSizeOfConnectionPool"));
            }

            if ((freeConnections.size() == 0) && (initConns != 0)) {
                // for some reasons the pool cannot be initialised
                throw new SQLException(
                    ServerResourceBundle.getInstance().getString("message.noConnectionAvailable",
                        new Object[]{new Integer(freeConnections.size()),new Integer(initConns)}));
            }

            Connection conn = getConnection(contextId, timeOut * 1000);

            return conn;
        } catch (SQLException e) {
            log.trace(getStats());
            throw e;
        }
    }

    private synchronized Connection getConnection(String contextId, long timeout)
        throws SQLException {
        // Get a pooled Connection from the cache or a new one.
        // Wait if all are checked out and the max limit has
        // been reached.
        long startTime = System.currentTimeMillis();
        long remaining = timeout;
        Connection conn = null;

        while ((conn = getPooledConnection(contextId)) == null) {
            try {
                wait(remaining);
            } catch (InterruptedException e) {
            }

            remaining = timeout - (System.currentTimeMillis() - startTime);

            if (remaining <= 0) {
                // Timeout has expired
                throw new SQLException(ServerResourceBundle.getInstance().getString("message.databaseConnectionTimedOut"));
            }
        }

        // Check if the Connection is still OK
        if (!isConnectionOK(conn)) {
            // It was bad. Try again with the remaining timeout
            return getConnection(contextId, remaining);
        }

        //Got a good connection
        checkedOutConnections.put(conn, contextId);

        return conn;
    }

    private boolean isConnectionOK(Connection connection) {
        Statement testStmt = null;

        try {
            if (!connection.isClosed()) {
                // Try to createStatement to see if it's really alive
                testStmt = connection.createStatement();
                testStmt.close();
            } else {
                return false;
            }
        } catch (SQLException e) {
            if (testStmt != null) {
                try {
                    testStmt.close();
                } catch (SQLException se) {
                	testStmt = null;
                }
            }

            return false;
        }

        return true;
    }

    private Connection getPooledConnection(String contextId) throws SQLException {
        Connection conn = null;

        if (freeConnections.size() > 0) {
            // Pick the first Connection in the Vector
            // to get round-robin usage
            conn = freeConnections.remove(0);
        } else if (checkedOutConnections.size() < maxConns) {
            conn = newConnection();
        }

        return conn;
    }

    private Connection newConnection() throws SQLException {
        Connection conn = null;

        if ((user==null) || (user.length() == 0)) {
            conn = DriverManager.getConnection(URL);
        } else {
            conn = DriverManager.getConnection(URL, user, password);
        }

        // Set Transaction Isolation and AutoComit
        // WARNING: till present Oracle dirvers (9.2.0.5) do not accept
        // setTransactionIsolation being called after setAutoCommit(false)
        conn.setTransactionIsolation(transactionIsolation);
        conn.setAutoCommit(false);
        
        return conn;
    }

    public synchronized void freeConnection(Connection conn)
        throws SQLException {

        @SuppressWarnings("unused")
		String contextId = checkedOutConnections.get(conn);
        checkedOutConnections.remove(conn);
        //System.err.println("    **********freeConnection: contextId=" + contextId + " " + getStats());
        
        // Put the connection at the end of the Vector
        freeConnections.add(conn);
        notifyAll();
    }

    private String getStats() {
        return "Total connections: " + (freeConnections.size() + checkedOutConnections.size()) +
        " Available: " + freeConnections.size() + " Checked-out: " +
        checkedOutConnections.size() + " " + checkedOutConnections.values().toString();
    }
}
