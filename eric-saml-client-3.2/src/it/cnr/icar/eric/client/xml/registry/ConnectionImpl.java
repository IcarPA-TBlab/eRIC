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
package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.jaas.LoginModuleManager;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.CredentialInfo;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Assertion;


/**
 * Implements JAXR API interface named Connection.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ConnectionImpl implements Connection {
    /** DOCUMENT ME! */
    private final Log log = LogFactory.getLog(this.getClass());
    private ConnectionFactoryImpl factory ;
    private RegistryServiceImpl service ;
    private String queryManagerURL;
    private String lifeCycleManagerURL;
    private X500PrivateCredential x500Cred = null;
    private X509Certificate x509Cert;
    private boolean closed = false;
    private boolean synchronous = true;
    private boolean localCallMode;
    private CallbackHandler handler ;
    private LoginModuleManager loginModuleMgr ;

    // support of SAML v2 assertions
    private Assertion assertion;
    
    private static final it.cnr.icar.eric.client.xml.registry.util.SecurityUtil su = it.cnr.icar.eric.client.xml.registry.util.SecurityUtil.getInstance();
    
    /**
     * Creates a new ConnectionImpl object.
     *
     * @param factory DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    ConnectionImpl(ConnectionFactoryImpl factory) throws JAXRException {
        
    	this.factory = factory;

        Properties props = factory.getProperties();
        
        queryManagerURL = props.getProperty("javax.xml.registry.queryManagerURL");
        lifeCycleManagerURL = props.getProperty("javax.xml.registry.lifeCycleManagerURL");
        
        localCallMode = Boolean.valueOf(props.getProperty( "it.cnr.icar.eric.client.xml.registry.localCall", "false")).booleanValue();
        
        if (queryManagerURL == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("queryManagerURL.property.null"));
        }
       
        if (lifeCycleManagerURL == null) {
            lifeCycleManagerURL = queryManagerURL;
        }
        
        loginModuleMgr = new LoginModuleManager();        
    }
    
    /**
     * Determines whether this connection uses local calls to server rather than SOAP over HTTP.
     */
    public boolean isLocalCallMode() {
        return localCallMode;
    }
    
    /**
     * Set whether this connection should use local calls.
     */
    public void setLocalCallMode(boolean localCallMode) {
        this.localCallMode = localCallMode;
    }

    /**
     * Gets the RegistryService interface associated with the
     * Connection. If a Connection property (e.g. credentials) is set
     * after the client calls getRegistryService then the newly set
     * Connection property is visible to the RegistryService
     * previously returned by this call.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @see javax.xml.registry.RegistryService
     */
    public RegistryService getRegistryService() throws JAXRException {
        if (service == null) {
            service = new RegistryServiceImpl(this);
        }

        return service;
    }

    /**
     * Since a provider typically allocates significant resources
     * outside  the JVM on behalf of a Connection, clients should
     * close them when they are not needed.
     *
     * <p>
     *
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @exception JAXRException if a JARR error occurs.
     */
    public void close() throws JAXRException {
        // ??eeg Do we need to do anything to reduce resources here?
        closed = true;
    }

    /**
     * Return true if this Connection has been closed.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isClosed() throws JAXRException {
        return closed;
    }

    /**
     * Return true if client uses synchronous communication with JAXR
     * provider. Note that a JAXR provider must support both modes of
     * communication, while the client can choose which mode it wants
     * to use. Default is a return value of true (synchronous
     * communication).
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0</B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isSynchronous() throws JAXRException {
        return synchronous;
    }

    /**
     * Sets whether the client uses synchronous communication or not.
     * A JAXR client may dynamically change its communication style
     * preference.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param sync DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void setSynchronous(boolean sync) throws JAXRException {
         synchronous = sync;
    }

    /**
     * Sets the Credentials associated with this client. The
     * credentials is used to authenticate the client with the JAXR
     * provider.  A JAXR client may dynamically change its identity by
     * changing the credentials associated with it.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param credentials a Collection of java.lang.Objects which
     *        provide identity-related information for the caller.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public void setCredentials(@SuppressWarnings("rawtypes") Set credentials) throws JAXRException {

    	for (Iterator<?> it = credentials.iterator(); it.hasNext();) {
            Object obj = it.next();

            // enable SAML v2 assertion
            if (obj instanceof Assertion) {
            	
            	assertion = (Assertion)obj;

                ((RegistryServiceImpl)getRegistryService()).setCredentialInfo(getCredentialInfo());
                return;

            } else if (obj instanceof X500PrivateCredential) {

            	x500Cred = (X500PrivateCredential) obj;
                
            	((RegistryServiceImpl)getRegistryService()).setCredentialInfo(getCredentialInfo());
                return;

            } else if (localCallMode && credentials.size() == 1 && obj instanceof X509Certificate) {
                // this should work only for localCallMode mode, where no signing is
                // required and we trust the container to provide credentials
                setX509Certificate((X509Certificate)obj);
                
                ((RegistryServiceImpl)getRegistryService()).setCredentialInfo(getCredentialInfo());
                return;
           
            }            
        }

    	// adapt exception to multi-dimensional credentials
        throw new JAXRException(JAXRResourceBundle.getInstance().getString("Neither SAML v2 assertion nor X.500 credentials provided."));
    
    }
    
    /**
     * Sets the X509Certificate associated with this client. The
     * credentials is used to authenticate the client with the Registry
     * when the client is being used in "localCallMode".
     * 
     * @param x509Cert The X509Certificate which
     *        provide identity related information for the caller.
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public void setX509Certificate(X509Certificate x509cert) throws JAXRException {
        this.x509Cert = x509cert;
    }
    

    /**
     * Gets the credentials associated with this client.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return Set of java.lang.Object instances. The Collection may be
     *         empty but not null.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public Set<Object> getCredentials() throws JAXRException {
        
    	HashSet<Object> ret = new HashSet<Object>();

    	// support assertions
    	if (assertion != null) {
    		ret.add(assertion);
    		
    	} else if  (x500Cred != null) {
            ret.add(x500Cred);
       
    	} else if (x509Cert != null) {
            ret.add(x509Cert);
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The X500PrivateCredential or null if not set
     */
    X500PrivateCredential getX500PrivateCredential() {
        if (x500Cred == null) {
            try {
                authenticate();
            } catch (JAXRException e) {
                log.error(e);
            }
        }

        return x500Cred;
    }
    
    public CredentialInfo getCredentialInfo() throws JAXRException  {
    	
    	// enable support for SAML v2 assertion
    	if (assertion != null) {
    		return new CredentialInfo(null, null, null, null, assertion);
    	}
        
    	// if no rea creds, but certificate was set, return it.
        if (x500Cred == null && x509Cert != null) {
            return new CredentialInfo(null, x509Cert, null, null, null);
        }
        
        if (x500Cred == null) {
            return null;
        }
        if (x500Cred.isDestroyed()) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.credential.destroyed"));
        }

        X509Certificate cert = x500Cred.getCertificate();
        if (cert == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.X509Certificate"));
        }

        PrivateKey privateKey = x500Cred.getPrivateKey();
        if (privateKey == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.PrivateKey"));
        }

        Certificate[] certChain = su.getCertificateChain(cert);
        return new CredentialInfo(x500Cred.getAlias(), cert, certChain, privateKey, null);
    
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getQueryManagerURL() {
        return queryManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLifeCycleManagerURL() {
        return lifeCycleManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionFactoryImpl getConnectionFactory() {
        return factory;
    }

    /**
     * This method is used to get the reference to the LoginModuleManager
     * With this reference, references to a parent Frame and Log can be
     * passed to the LoginModuleManager.
     *
     * @return
     *  A reference to the LoginModuleManager
     */
    public LoginModuleManager getLoginModuleManager() {
        return loginModuleMgr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CallbackHandler getCallbackHandler() throws JAXRException {
        if (handler == null) {
            handler = loginModuleMgr.getCallbackHandler();
        }

        return handler;
    }

    /**
     * DOCUMENT ME!
     *
     * @param handler DOCUMENT ME!
     */
    public void setCallbackHandler(CallbackHandler handler) {
        loginModuleMgr.setDefaultCallbackHandler(handler);
    }

    /**
     * Determine whether the user has already authenticated and setCredentials
     * on the Connection or not.
     * Add to JAXR 2.0??
     *
     * @param handler DOCUMENT ME!
     */
    public boolean isAuthenticated() throws JAXRException {
        boolean authenticated = false;

        // enable support for SAML v2 assertion
        if (assertion != null || x500Cred != null || x509Cert != null) {
            authenticated = true;
        }

        return authenticated;
    }

    /**
     * Forces authentication to occur.
     ** Add to JAXR 2.0??
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void authenticate() throws JAXRException {
        // Obtain a LoginContext, needed for authentication. Tell it 
        // to use the LoginModule implementation specified by the 
        // entry named "Sample" in the JAAS login configuration 
        // file and to also use the specified CallbackHandler.
        LoginContext lc = null;

        try {
            loginModuleMgr.createLoginConfigFile();

            String applicationName = loginModuleMgr.getApplicationName();
            handler = loginModuleMgr.getCallbackHandler();

            lc = new LoginContext(applicationName, handler);

            // attempt authentication
            lc.login();

            //Get the authenticated Subject.
            Subject subject = lc.getSubject();
            Set<Object> privateCredentials = subject.getPrivateCredentials();

            //Set credentials on JAXR Connections
            setCredentials(privateCredentials);

            log.info(JAXRResourceBundle.getInstance().getString("message.SetCredentialsOnConnection"));
        } catch (LoginException le) {
            String msg = le.getMessage();

            if ((msg != null) && (!(msg.equalsIgnoreCase("Login cancelled")))) {
                throw new JAXRException(le);
            }
        } catch (SecurityException se) {
            throw new JAXRException(se);
        }
    }

    /**
     * Logout current user if any.
     *
     */
    public void logoff() throws JAXRException {
        boolean authenticated = isAuthenticated();

        if (authenticated) {
        	
        	// enable support for SAML v2 assertion
        	assertion = null;
        	
            x500Cred = null;
            x509Cert = null;
            
            ((RegistryServiceImpl)getRegistryService()).setCredentialInfo(null);
        }
    }
}
