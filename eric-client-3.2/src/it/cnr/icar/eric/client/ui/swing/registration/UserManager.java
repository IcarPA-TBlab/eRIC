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
package it.cnr.icar.eric.client.ui.swing.registration;

import it.cnr.icar.eric.client.common.userModel.UserModel;
import it.cnr.icar.eric.client.ui.swing.JAXRClient;
import it.cnr.icar.eric.client.ui.swing.JavaUIResourceBundle;
import it.cnr.icar.eric.client.ui.swing.RegistryBrowser;
import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonNameImpl;
import it.cnr.icar.eric.client.xml.registry.util.CertificateUtil;
import it.cnr.icar.eric.client.xml.registry.util.UserRegistrationInfo;

import java.util.ArrayList;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * User registration tool.
 */
public class UserManager {
    
    /** Singleton instance */
    private static final UserManager instance = new UserManager();
    
    static JAXRClient client = null;
    static BusinessLifeCycleManager lcm = null;

    private static final Log log = LogFactory.getLog(UserManager.class);

    static {
        try {
            client = RegistryBrowser.getInstance().getClient();
            lcm = client.getBusinessLifeCycleManager();
        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new UserManager object.
     */
    private UserManager() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static UserManager getInstance() {
        return instance;
    }

    /*
     * Register a new user
     *
     */
    public void registerNewUser() throws Exception {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            
            //Make sure you are logged off when registering new user so new user is not owned by old user.
            ((ConnectionImpl)client.getConnection()).logoff();
            
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

            UserModel userModel = new UserModel(lcm.createUser());
            UserRegistrationPanel userRegPanel = new UserRegistrationPanel(userModel);
            UserRegistrationDialog dialog = new UserRegistrationDialog(userRegPanel,
                    userModel);

            dialog.setVisible(true);

            //Make sure you are logged off after registering new user as Java UI does not show authenticated UI state after user reg.
            ((ConnectionImpl)client.getConnection()).logoff();
            
            if (dialog.getStatus() != UserRegistrationDialog.OK_STATUS) {
                return;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /** First check if certificate already exists in client keystore. If it does,
      * use it. If not then create a self signed certificate for the user and use it to
      * authenticate with the ebxmlrr server.
      * If the authentication is sucessful, save the user model to the server.
      *
      * @throw Exception
      *     An exception could indicate either a communications problem or an
      *     authentication error.
      */
    public static void authenticateAndSaveUser(UserModel userModel)
        throws Exception 
    {
        @SuppressWarnings("unused")
		boolean generatedCert = false;
        UserRegistrationInfo userRegInfo = userModel.getUserRegistrationInfo();
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            RegistryServiceImpl rs = (RegistryServiceImpl) lcm.getRegistryService();
            ConnectionImpl connection = rs.getConnection();

            if (!userRegInfo.isCAIssuedCert()) {
                if (!CertificateUtil.certificateExists(userRegInfo.getAlias(), userRegInfo.getStorePassword())) {
                    CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
                }
            } else {
                try {
                    CertificateUtil.importCAIssuedCert(userRegInfo);
                } catch (Exception e) {
                    throw new JAXRException(JavaUIResourceBundle.getInstance().getString("error.importCertificateFailed"), e);
                }
            }

            // Force re-authentication in case credentials are already set
            connection.authenticate();

            RegistryBrowser.setWaitCursor();

            // Now save the User
            ArrayList<User> objects = new ArrayList<User>();
            objects.add(userModel.getUser());
            client.saveObjects(objects, false, false);

            // saveObjects uses XML-Security which overwrites the log4j
            // configuration and we never get to see this:
            log.info(JavaUIResourceBundle.getInstance().getString("message.SavedUserOnServer", new Object[]{((PersonNameImpl)(userModel.getUser().getPersonName())).getFormattedName()}));
        } 
        catch (Exception e) {
            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (userRegInfo != null) {
                    String alias = userRegInfo.getAlias();

                    if ((alias != null) && (!userRegInfo.isCAIssuedCert())) {
                        CertificateUtil.removeCertificate(alias,
                            userRegInfo.getStorePassword());
                    }
                }
            } catch (Exception removeCertException) {
                log.warn(JavaUIResourceBundle.getInstance().getString("message.FailedToRemoveTheCertificateFromTheKeystoreGenerated"),
                    removeCertException);
            }

            throw e;
        } finally {
            RegistryBrowser.setDefaultCursor();
        }
    }

}
