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

import it.cnr.icar.eric.client.common.userModel.KeyModel;
import it.cnr.icar.eric.client.ui.swing.JavaUIResourceBundle;
import it.cnr.icar.eric.client.ui.swing.RegistryBrowser;
import it.cnr.icar.eric.client.xml.registry.util.CertificateUtil;

//import it.cnr.icar.eric.client.xml.registry.util.UserRegistrationInfo;

//import java.util.ArrayList;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * User key registration tool.
 */
public class KeyManager {

    /** Singleton */
    private static final KeyManager instance = new KeyManager();

    /** Create a static reference to the logging service. */
    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(KeyManager.class);

    /**
     * Creates a new KeyManager object.
     */
    private KeyManager() {
    }

    /**
     * Singleton accessor
     *
     * @return KeyManager singleton instance.
     */
    public static KeyManager getInstance() {
        return instance;
    }

    /**
     * Shows dialog to register a new key
     */
    public void registerNewKey() throws Exception {
        try {

            KeyModel keyModel = new KeyModel();
            keyModel.setCAIssuedCert(true);

            CertificateInfoPanel certificateInfoPanel = new CertificateInfoPanel(keyModel);
            certificateInfoPanel.setEnabledCertificateTypeButtons(false);

            KeyRegistrationDialog dialog = new KeyRegistrationDialog(certificateInfoPanel, keyModel);
            dialog.setVisible(true);

            if (dialog.getStatus() != KeyRegistrationDialog.OK_STATUS) {
                return;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /**
     * Save a key to client keystore. 
     *
     * @throw Exception relate to keystore manipulation
     */
    public static void saveKey(KeyModel keyModel) throws Exception {
        try {
            if (CertificateUtil.certificateExists(keyModel.getAlias(), keyModel.getStorePassword())) {
                throw new JAXRException(JavaUIResourceBundle.getInstance().getString("error.keyAliasAlreadyExists", new Object[] {keyModel.getAlias()}));
            } else {
                CertificateUtil.importCAIssuedCert(keyModel);
                RegistryBrowser.displayInfo(JavaUIResourceBundle.getInstance().getString("message.certificateImported"));
            }
        } finally {
            RegistryBrowser.setDefaultCursor();
        }
    }

}
