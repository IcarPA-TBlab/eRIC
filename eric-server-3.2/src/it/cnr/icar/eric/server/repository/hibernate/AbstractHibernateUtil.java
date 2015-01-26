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

import it.cnr.icar.eric.server.util.ServerResourceBundle;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to hold util methods for Hibernate, like session handling.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
abstract class AbstractHibernateUtil {
    private static final Log log = LogFactory.getLog(AbstractHibernateUtil.class);

    //private static final SessionFactory sessionFactory;
    //private static final ThreadLocal session = new ThreadLocal();
    protected abstract ThreadLocal<Session> getThreadLocalSession();
    protected abstract SessionFactory getSessionFactory();
    protected abstract boolean checkSchema(SessionFactory sessionFactory)
	    throws HibernateException;
    protected abstract void populateDB() throws HibernateException ;
    protected abstract Configuration getConfiguration();

/*    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            throw new RuntimeException("Exception building SessionFactory: " +
				       ex.getMessage(), ex);
        }
    }*/

    public void initDB(boolean forceCreate) throws HibernateException {
        //Check if  database exists. If not, create it.
        //TODO: Check all cfg database, use init method at 1st use, not static.
        if (forceCreate || !checkSchema(getSessionFactory())) {
            createDB(getSessionFactory());
            populateDB();
        }
    }


    public synchronized SessionContext getSessionContext()
	    throws HibernateException {
        boolean isNew = false;
        Session s = getThreadLocalSession().get();

        // Open a new Session, if this Thread has none yet
        if (s == null) {
            isNew = true;
            s = getSessionFactory().openSession();
            getThreadLocalSession().set(s);
        }
        if (!s.isConnected()) {
            s.reconnect();
        }
        SessionContext sessionContext = new  SessionContext(s, isNew);
        return sessionContext;
    }

    private void createDB(SessionFactory sf) throws HibernateException {
        log.info(ServerResourceBundle.getInstance().
		 getString("message.RebuildingDBSchema"));

        SchemaExport schemaManager = new SchemaExport(getConfiguration());

        // Remove the DB schema
        schemaManager.drop(true, true);

        // Export the DB schema
        schemaManager.create(true, true);

        // Above method will log error but throw no exception. Check
        // database again.
        if (!checkSchema(sf)) {
            //throw new HibernateException(ServerResourceBundle.getInstance().
	    //			       getString("message.RebuildDBSchemaFailed"));
            throw new HibernateException(ServerResourceBundle.getInstance().
				      getString("message.FailedToCreateDatabase"));
        }
    }
}
