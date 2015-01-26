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
package it.cnr.icar.eric.client.ui.thin.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.security.KeyStore;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.x500.X500PrivateCredential;
import java.security.cert.X509Certificate;
import javax.servlet.ServletRequest;
import javax.faces.context.FacesContext;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.cnr.icar.eric.client.xml.registry.util.KeystoreUtil;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;
import it.cnr.icar.eric.common.CommonProperties;


/**
 *
 * Some utility methods related to XML security
 *
 * $Header: /cvsroot/ebxmlrr/eric/src/java/it/cnr/eric/client/ui/thin/security/SecurityUtil.java,v 1.19 2006/02/08 18:38:46 farrukh_najmi Exp $
 *
 */
public class SecurityUtil {
    
    private static SecurityUtil instance = null;
    private static final Log log = LogFactory.getLog(SecurityUtil.class);

    protected SecurityUtil() throws JAXRException {        
    }

    /** 
     * The purpose of this method to determine if a <code>java.util.Set</code>
     * of credentials needs to be set on the <code>javax.registry.Connection
     * </code> object. This determination considers whether or not the web
     * container or policy is providing authentication services.
     * 
     * @param principal
     *    A <code>java.security.Principal</code> object
     * @param connection
     *    A <code>java.security.Connection</code> object
     */    
    public void handleCredentials(Principal principal, Connection connection)
        throws JAXRException {
        boolean isAuthenticated = (principal != null);
        if (isAuthenticated) {
            // This servlet is protected by the web contianer or policy
            // agent. To have gotten here, the user must have logged in
            // successfully.
            String principalName = principal.getName();
            String guestPrincipalName = ProviderProperties.getInstance().getProperty
                ("jaxr-ebxml.security.guestPrincipalName");
            if (! principalName.equals(guestPrincipalName)) {
                Set<Object> credentials = getCredentials(principalName);
                if ((credentials == null) || credentials.isEmpty()) {
                    credentials = generateCredentials(principalName);
                }
                setCredentials(credentials, connection);
            }
        }
        else {
            // This servlet is not being protected by the web container
            // or policy agent.
            String principalName = CommonProperties.getInstance().getProperty
                ("eric.security.anonymousUserPrincipalName");
            if (principalName != null) {
                Set<Object> credentials = getCredentials(principalName);
                if ((credentials != null) && !credentials.isEmpty()) {
                    setCredentials(credentials, connection);
                }
            } else {
                // obtain credentials from client certificate
                Set<Object> credentials = getX509CertFromRequest();
                if ((credentials != null) && !credentials.isEmpty()) {
                    setCredentials(credentials, connection);
                }
            }
        }
    }
    
    public void handleCredentials(X509Certificate x509Cert, Connection connection)
        throws JAXRException {
        HashSet<Object> x509CertSet = new HashSet<Object>();
        x509CertSet.add(x509Cert);
        setCredentials(x509CertSet, connection);
    }
    
    
    public Set<Object> getX509CertFromRequest() {
        Set<Object> x509CertSet = null;
        ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance()
                                                             .getExternalContext()
                                                             .getRequest();
        Object certObj = request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certObj != null) {
            try {
                java.security.cert.Certificate[] certs = 
                    (java.security.cert.Certificate[])certObj;
                X509Certificate x509Cert = (X509Certificate)certs[0];
                x509CertSet = new HashSet<Object>();
                x509CertSet.add(x509Cert);
            } catch (ClassCastException t) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.TheFollowingCertificateTypeIsNotSupported", new Object[]{certObj.getClass().getName()}));
            }
        }
        return x509CertSet;
    }
    
     /** 
      * The purpose of this method is to determine the return status based on 
      * the principal.  The return status is a <code>java.lang.String</code>.
      * Web app frameworks, such as JSF, use the status to determine page
      * navigation. 
      *
      * @param principal
      *     A <code>java.security.Principal</code> object
      * @return
      *     A <code>java.lang.String</code> representing the status.  Typically,
      *     the status is 'success' or 'failure', but it can have other values.
      * @deprecated
      */
    public String getStatus(Principal principal) throws JAXRException {
        String status = null;
        return status;    
    }
    
     /** Determine if the user needs to self register.
      */
    public boolean isRegistrationNeeded(Connection connection, Principal principal) throws JAXRException {
        boolean isRegistrationNeeded = true;
        boolean isAuthenticated = (principal != null);
        if (isAuthenticated) {
            // This servlet is protected by the web contianer or policy
            // agent. To have gotten here, the user must have logged in
            // successfully.
            String principalName = principal.getName();
            String guestPrincipalName = ProviderProperties.getInstance().getProperty
                ("jaxr-ebxml.security.guestPrincipalName");
            if (principalName.equals(guestPrincipalName)) {
                isRegistrationNeeded = false;
            } else {
                try {
                    isRegistrationNeeded = (findUserByPrincipalName(connection, principalName) == null);
                } catch (JAXRException e) {
                    // TODO: there is an issue with thin client self-registration
                    // The AuthenticationServiceImpl throws an exception when
                    // a self-registered user cert is sent, but is not included
                    // in the server keystore (because the registration has
                    // not been completed yet). This issue is under 
                    // investigation. This is a workaround for now
                    isRegistrationNeeded = true;
                }
            }
        } 
        else {
            // The application does not have security enabled, so self-registration
            // is not possible. 
            isRegistrationNeeded = false;
        }
        
        return isRegistrationNeeded;    
    }
    
     /**
      *
      * @param principalName
      * @throws JAXRException
      * @return
      */    
    public User findUserByPrincipalName(Connection connection, String principalName) throws JAXRException {
        User user = null;
        DeclarativeQueryManager dqm = connection.getRegistryService().getDeclarativeQueryManager();
        String queryString = 
            "SELECT * " + 
            "FROM user_ u, slot s " +
            "WHERE u.id = s.parent AND s.name_='" + CanonicalConstants.CANONICAL_PRINCIPAL_NAME_URI + "' AND value='" + principalName + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
        BulkResponse br = dqm.executeQuery(query);
        Iterator<?> results = br.getCollection().iterator();
        while (results.hasNext()) {
            user = (User)results.next();
            break;
        }
        return user;
    }
    
     /** Get the credentials for the specified principal.
      *
      * @param alias
      *     The principal of the user making the request on the registry.
      *     This value will be obtained from the web container hosting the
      *     registry client by calling HttpServletRequest.getUserPrincipal().
      *     If this value is <code>null</code>, or if <code>principal.getName()</code>
      *     is <code>null</code>, then the default principal name, as specified
      *     by the <i>jaxr-ebxml.security.defaultPrincipalName</i> property
      *     will be used. If this property is not set, then an empty Set will
      *     be returned.
      * @return
      *     A Set of X500PrivateCredential objects representing the user's
      *     credentials. If this set is empty or null, no credentials will
      *     be passed to the registry with the request. The registry treats
      *     such requests as coming from the Registry Guest user.
      * @throws JAXRException
      *     Thrown if an error occurs while trying to map the principal
      *     to its credentials. An exception should not be thrown if there are
      *     no credentials associated with the principal. In this case, an
      *     empty Set should be returned.
      */
    public Set<Object> getCredentials(String alias) throws JAXRException {

        HashSet<Object> credentials = new HashSet<Object>();
        
        if (alias == null) {
            return credentials;
        }
        
        log.debug("Getting credentials for '" + alias + "'");

        try {
            credentials.add(it.cnr.icar.eric.client.xml.registry.util.SecurityUtil.
                getInstance().aliasToX500PrivateCredential(alias));
        }
        catch (JAXRException je) {
            // aliasToX500PrivateCredential() throws an exception if no certificate
            // can be found for the specified alias. For our purposes, this
            // is not an exception, so we just ignore such exceptions and
            // propogate all others.
            if (je.getMessage().equals("Alias unknown in keystore") ||
                je.getMessage().startsWith("KeyStore file not found") || 
                je.getMessage().startsWith("Failed to find an entry with the alias")) 
            {
                log.warn(WebUIResourceBundle.getInstance().getString("message.FailedToGetCredentialsForException", new Object[]{alias}), je);
                throw je;
            }
            else {
                throw je;
            }
        }

        return credentials;
    }
    
     /** Wrapper for ConnectionImpl.setCredentials() that ignores null or
      * empty credential sets.
      */
    public void setCredentials(Set<Object> credentials, Connection connection) throws JAXRException {
        if ((credentials != null) && !credentials.isEmpty()) {
            connection.setCredentials(credentials);
        }
    }
    
    /** Generate a key pair and add it to the keystore.
      *
      * @param alias
      * @return
      *     A HashSet of X500PrivateCredential objects.
      * @throws Exception
      */    
    private Set<Object> generateCredentials(String alias) throws JAXRException {
        
        try {
            HashSet<Object> credentials = new HashSet<Object>();

            // The keystore file is at ${jaxr-ebxml.home}/security/keystore.jks. If
            // the 'jaxr-ebxml.home' property is not set, ${user.home}/jaxr-ebxml/ is
            // used.
            File keyStoreFile = KeystoreUtil.getKeystoreFile();
            String storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass", "ebxmlrr");
            String keypass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keypass");
            if (keypass == null) {
                // keytool utility requires a six character minimum password.
                // pad passwords with < six chars
                if (alias.length() >= 6) {
                    keypass = alias;
                } else if (alias.length() == 5) {
                    keypass = alias+"1";
                } else if (alias.length() == 4) {
                    keypass = alias+"12";
                } else if (alias.length() == 3) {
                    keypass = alias+"123";
                }
                // alias should have at least 3 chars
            }
            log.debug("Generating key pair for '" + alias + "' in '" + keyStoreFile.getAbsolutePath() + "'");

// When run in S1WS 6.0, this caused some native library errors. It appears that S1WS
// uses different encryption spis than those in the jdk. 
//            String[] args = {
//                "-genkey", "-alias", uid, "-keypass", "keypass",
//                "-keystore", keyStoreFile.getAbsolutePath(), "-storepass",
//                new String(storepass), "-dname", "uid=" + uid + ",ou=People,dc=sun,dc=com"
//            };
//            KeyTool keytool = new KeyTool();
//            ByteArrayOutputStream keytoolOutput = new ByteArrayOutputStream();
//            try {
//                keytool.run(args, new PrintStream(keytoolOutput));
//            }
//            finally {
//                log.info(keytoolOutput.toString());
//            }
// To work around this problem, generate the key pair using keytool (which executes
// in its own vm. Note that all the parameters must be specified, or keytool prompts
// for their values and this 'hangs'
            String[] cmdarray = {
                "keytool", 
                "-genkey", "-alias", alias, "-keypass", keypass,
                "-keystore", keyStoreFile.getAbsolutePath(), 
                "-storepass", storepass, "-dname", "cn=" + alias
            };
            Process keytool = Runtime.getRuntime().exec(cmdarray);
            try {
                keytool.waitFor();
            }
            catch (InterruptedException ie) {
            }
            if (keytool.exitValue() != 0) {
                log.error(WebUIResourceBundle.getInstance().getString("message.keytoolCommandFailedDetails"));
                Reader reader = new InputStreamReader(keytool.getErrorStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                while (bufferedReader.ready()) {
                    log.error(bufferedReader.readLine());
                }
                throw new JAXRException(WebUIResourceBundle.getInstance().getString("excKeyToolCommandFail") + 
                    keytool.exitValue());
            }
            log.debug("Key pair generated successfully.");

            // After generating the keypair in the keystore file, we have to reload
            // SecurityUtil's KeyStore object.
            KeyStore keyStore = it.cnr.icar.eric.client.xml.registry.util.SecurityUtil.
                getInstance().getKeyStore();
            keyStore.load(new FileInputStream(keyStoreFile), storepass.toCharArray());

            credentials.add(it.cnr.icar.eric.client.xml.registry.util.SecurityUtil.
                getInstance().aliasToX500PrivateCredential(alias));

            return credentials;
        }
        catch (Exception e) {
            if (e instanceof JAXRException) {
                throw (JAXRException)e;
            }
            else {
                throw new JAXRException(e);
            }
        }
    }
    
    public String getAliasFromCredentials(Set<?> credentials) {
        String alias = null;
        Iterator<?> itr = credentials.iterator();
        while (itr.hasNext()) {
            try {
                Object obj = itr.next();
                if (obj instanceof X500PrivateCredential) {
                    X500PrivateCredential credential = (X500PrivateCredential)obj;
                    alias = it.cnr.icar.eric.client.xml.registry.util.SecurityUtil.
                           getInstance().x500PrivateCredentialToAlias(credential);
                    break;
                } else if (obj instanceof X509Certificate) {
                    X509Certificate credential = (X509Certificate)obj;
                    alias = credential.getSubjectDN().getName();
                } else {
                    log.warn(WebUIResourceBundle.getInstance().getString("message.CouldGetAliasFromCredentials", new Object[]{obj}));
                }
            } catch (Throwable t) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.CouldGetAliasFromCredentials1"), t);
            }
        }
        return alias;
    }

    /**
     * Method main
     *
     * @param unused
     * @throws Exception
     */
    public static void main(String[] unused) throws Exception {
    }

    public synchronized static SecurityUtil getInstance() throws JAXRException {
        if (instance == null) {
            instance = new SecurityUtil();
        }

        return instance;
    }
}
