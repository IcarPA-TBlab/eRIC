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
package it.cnr.icar.eric.server.repository.hibernate;

import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.registry.RegistryException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to hold util methods for Hibernate, like session handling.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class RepositoryHibernateUtil extends AbstractHibernateUtil {
    private static final Log log = LogFactory.getLog(RepositoryHibernateUtil.class);
    
    private static RepositoryHibernateUtil instance;
    private static SessionFactory sessionFactory;
    private static Configuration configuration;
    private boolean reclaimUsedDBConnections = false;
    public static final ThreadLocal<Session> threadLocalSession = new ThreadLocal<Session>();
    
    protected RepositoryHibernateUtil() {
        reclaimUsedDBConnections = "true".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("eric.repository.hibernate.reclaimUsedDBConnections", "false"));
        getConfiguration();
    }
    
    /**
     * Singleton instance accessor.
     */
    public synchronized static RepositoryHibernateUtil getInstance() {
        if (instance == null) {
            instance = new RepositoryHibernateUtil();
        }

        return instance;
    }
    
    public synchronized SessionContext getSessionContext()
	throws HibernateException {
        SessionContext sessionContext = super.getSessionContext();
        if (reclaimUsedDBConnections) {
            Session s = sessionContext.getSession();
            if (!s.isConnected()) {
                s.reconnect();
            }
        }
        return sessionContext;
    }
    
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
 
    @SuppressWarnings("unused")
	protected Configuration getConfiguration() {
        if (configuration == null)  {
            synchronized (RepositoryHibernateUtil.class) {
                if (configuration == null)  {
                    try {
			String cfgResource;
                        DataSource ds = null;

			boolean useConnectionPool = Boolean.
			    valueOf(RegistryProperties.getInstance().
				    getProperty("eric.persistence.rdb.useConnectionPooling",
						"true")).
			    booleanValue();
			boolean debugConnectionPool = Boolean.
			    valueOf(RegistryProperties.getInstance().
				    getProperty("eric.persistence.rdb.pool.debug",
						"false")).
			    booleanValue();

			// Try DataSource first, if configured
			if (useConnectionPool && !debugConnectionPool) {
			    cfgResource = "/repository.datasource.cfg.xml";
			    configuration =
				new Configuration().configure(cfgResource);

			    String dataSourceName = configuration
				.getProperty("connection.datasource");
			    if (dataSourceName != null &&
				!"".equals(dataSourceName)) {
				try {
				    Context ctx = new InitialContext();
				    if (ctx != null ) {
					ds = (DataSource)
					    ctx.lookup(dataSourceName);
					if (ds != null) {
					    // create a test connection to
					    // make sure all is well with
					    // DataSource
					    Connection connection = null;
					    try {
						connection =
						    ds.getConnection();
					    } catch (Exception e) {
						ds = null;
						log.info(ServerResourceBundle.
							 getInstance().
		 getString("message.UnableToCreateTestConnectionForDataSource",
			   new Object[]{dataSourceName}), e);
					    } finally {
						if (connection != null) {
						    try {
							connection.close();
						    } catch (Exception e1) {
							//Do nothing.
						    	connection = null;
						    }
						}
					    }
					}
				    } else {
					log.info(ServerResourceBundle.
						 getInstance().
			       getString("message.UnableToGetInitialContext"));
				    }
				} catch (NamingException e) {
				    log.info(ServerResourceBundle.
					     getInstance().
		       getString("message.UnableToGetJNDIContextForDataSource",
				 new Object[]{dataSourceName}));
				}
			    }
			}

                        if (ds == null) {
                            // fall back to jdbc
                            cfgResource = "/repository.jdbc.cfg.xml";
                            configuration = new Configuration().configure(cfgResource);
                        }
                        
                        // support $user.home and $eric.home in eric repository cfg
                        String connUrl = configuration.getProperty("hibernate.connection.url");
                        if (connUrl != null && !"".equals(connUrl)) {
                            connUrl = substituteVariable(connUrl,
                                "$user.home", System.getProperty("user.home"));
                            connUrl = substituteVariable(connUrl,
                                "$eric.home", RegistryProperties.getInstance().getProperty("eric.home"));
                            configuration.setProperty("hibernate.connection.url", connUrl);
                        }

                        sessionFactory = configuration.buildSessionFactory();
                    } catch (HibernateException ex) {
                        throw new RuntimeException(ServerResourceBundle.getInstance().getString("message.buildingSessionFactory", new Object[]{ex.getMessage()}), ex);
                    }
                }
            }
        }
        return configuration;
    }
    
    protected ThreadLocal<Session> getThreadLocalSession() {
        return threadLocalSession;
    }
    
    protected boolean checkSchema(SessionFactory sessionFactory) throws HibernateException {
        // Tries a simple query that *should* work if database OK. Fails otherwise.
        SessionContext sc = null;
        try {
            sc = getSessionContext();
            Session s = sc.getSession();
            @SuppressWarnings("unused")
			int howMany = ((Integer)s.iterate("select count(*) from it.cnr.icar.eric.server.repository.hibernate.RepositoryItemBean").next()).intValue();
            
            // got here, no exception  from query above, dutabase *should* be ok
            return true;
        } catch(HibernateException he) {
            // ops, query failed, database not ok
            return false;
        } finally {
            if(sc != null) {
                try {
                    sc.close();
                } catch (RegistryException e) {
                    log.error(e, e);
                }
            }
        }
    }

    protected void populateDB() throws HibernateException {
        // NO OP
    }
    
    public static void main(String [] args) throws Exception {
        //TODO: handle -cleandb -createdb params!!
        RepositoryHibernateUtil hibernateUtil = RepositoryHibernateUtil.getInstance();
        try {
            hibernateUtil.initDB(true);
            log.info(ServerResourceBundle.getInstance().getString("message.DatabaseCreated"));
        } catch (HibernateException e) {
            log.error(e);
            throw e;
        }
    }

    /** util method to replace variables in a string. */
    private static String substituteVariable(String value, String oldKeySubstring, String newKeySubstring) {
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
    
}
