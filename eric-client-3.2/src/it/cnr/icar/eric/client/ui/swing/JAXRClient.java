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
package it.cnr.icar.eric.client.ui.swing;

//import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;

import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.jaas.LoginModuleManager;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.BindingUtility;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//import javax.swing.SwingUtilities;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.RegistryObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains the JAXR client code used by the browser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JAXRClient {
    /** DOCUMENT ME! */
    ConnectionImpl connection;
    
    /*
     * This class handles all JAAS authentication tasks
     */
    LoginModuleManager lmm;
    
    /** DOCUMENT ME! */
    BusinessQueryManager bqm;
    private DeclarativeQueryManagerImpl dqm;
    
    /** DOCUMENT ME! */
    it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl lcm;
    
    /** DOCUMENT ME! */
    private static final Log log = LogFactory.getLog(JAXRClient.class);

    /** Flag for this client being connected or not. */
    private boolean connected = false;

    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     * @return boolean true if connected, false otherwise.
     */
    public synchronized boolean createConnection(String url) {
        try {
            if (!RegistryBrowser.localCall) {
                (new URL(url)).openStream().read();
            }
            
            Thread.currentThread().setContextClassLoader(RegistryBrowser.getInstance().classLoader);
            
            ProviderProperties.getInstance().put("javax.xml.registry.queryManagerURL",
            url);
            
            ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();

            connection = (it.cnr.icar.eric.client.xml.registry.ConnectionImpl) connFactory.createConnection();
            RegistryService service = connection.getRegistryService();
            bqm = service.getBusinessQueryManager();
            dqm = (it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            lcm = (it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl)service.getBusinessLifeCycleManager();
            
            lmm = connection.getLoginModuleManager();
            lmm.setParentFrame(RegistryBrowser.getInstance());
            connected = true;
        } catch (final JAXRException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        } catch (MalformedURLException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        } catch (IOException e) {
            connected = false;
            String msg = JavaUIResourceBundle.getInstance().getString(
                "message.error.failedConnecting",
                new String[] {url, e.getLocalizedMessage()});
            log.error(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        return connected;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param t DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getStackTraceFromThrowable(Throwable t) {
        String trace = null;
        
        if (t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            trace = sw.toString();
        }
        
        return trace;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public Connection getConnection() throws JAXRException {
        isConnected();
        return connection;
    }
    
    /**
     * returns the business life cycle query manager. This should go
     * away once the client code has all been moved here.
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BusinessLifeCycleManager getBusinessLifeCycleManager()
    throws JAXRException {
        isConnected();
        return lcm;
    }
    
    /**
     * returns the business life cycle query manager. This should go
     * away once the client code has all been moved here.
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BusinessQueryManager getBusinessQueryManager()
        throws JAXRException {
        isConnected();
        return bqm;
    }
    
    /**
     * Find classification schemes
     *
     * @return DOCUMENT ME!
     */
    @SuppressWarnings({ "static-access", "rawtypes" })
	Collection<?> getClassificationSchemes() {
        Collection<?> schemes = null;
        
        String errMsg = "Error getting ClassificationSchemes";
        
        try {
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, BindingUtility.getInstance().CANONICAL_QUERY_GetClassificationSchemesById);
            Query query = getDeclarativeQueryManager().createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse response = getDeclarativeQueryManager().executeQuery(query, queryParams);
            checkBulkResponse(response);
            schemes = response.getCollection();
        } catch (JAXRException e) {
            RegistryBrowser.displayError(errMsg, e);
            schemes = new ArrayList();
        }
        
        return schemes;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    CapabilityProfile getCapabilityProfile() {
        CapabilityProfile profile = null;
        
        try {
            profile = connection.getRegistryService().getCapabilityProfile();
        } catch (JAXRException e) {
            e.printStackTrace();
            RegistryBrowser.displayError(e);
        }
        
        return profile;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param objectsToSave DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public BulkResponse saveObjects(Collection<?> objectsToSave, boolean versionMetadata, boolean versionContent)
    throws JAXRException {
        BulkResponse resp = null;
        
        try {
            HashMap<String, String> slotsMap = new HashMap<String, String>();
            if (!versionMetadata) {
                slotsMap.put(BindingUtility.CANONICAL_SLOT_LCM_DONT_VERSION, "true");
            }
            if (!versionContent) {
                slotsMap.put(BindingUtility.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
            }
            resp = lcm.saveObjects(objectsToSave, slotsMap);
            checkBulkResponse(resp);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
            e.fillInStackTrace();
            throw e;
        }
        
        return resp;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param objectsToExport DOCUMENT ME!
     */
    public void exportObjects(Collection<?> objectsToExport) {
        try {
            Iterator<?> iter = objectsToExport.iterator();
            
            while (iter.hasNext()) {
                RegistryObject ro = (RegistryObject) iter.next();
                System.err.println(ro.toXML());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param resp DOCUMENT ME!
     */
    public void checkBulkResponse(BulkResponse resp) {
        try {
            if ((resp != null) &&
            (!(resp.getStatus() == JAXRResponse.STATUS_SUCCESS))) {
                Collection<?> exceptions = resp.getExceptions();
                
                if (exceptions != null) {
                    Iterator<?> iter = exceptions.iterator();
                    
                    while (iter.hasNext()) {
                        Exception e = (Exception) iter.next();
                        RegistryBrowser.displayError(e);
                    }
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void isConnected() throws JAXRException {
        if (!connected) {
            // try again, now synchro. will wait if in startup.
            synchronized (this) {
                if (!connected) {
                    throw new JAXRException(JavaUIResourceBundle.getInstance()
                        .getString("message.error.noConnection"));
                }
            }
        }
    }
    
    public DeclarativeQueryManagerImpl getDeclarativeQueryManager() {
        return dqm;
    }
    
}
