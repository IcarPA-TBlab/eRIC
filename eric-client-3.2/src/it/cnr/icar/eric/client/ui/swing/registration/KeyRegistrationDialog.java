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



import it.cnr.icar.eric.client.common.Model;
import it.cnr.icar.eric.client.common.userModel.KeyModel;
import it.cnr.icar.eric.client.ui.swing.JBDialog;
import it.cnr.icar.eric.client.ui.swing.RegistryBrowser;

import java.awt.BorderLayout;

/**
 * A dialog for key registration.
 *
 * @author Diego Ballve / Digital Artefacts
 */
public class KeyRegistrationDialog extends JBDialog {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 7112305465760518766L;
	/** The underlying model */
    private final Model model;

    /**
     * Creates a new KeyRegistrationDialog object.
     *
     * @param panel The main panel
     * @param m The underlying model
     */
    public KeyRegistrationDialog(CertificateInfoPanel panel, Model m) {
        super(RegistryBrowser.getInstance(), true);
        getMainPanel().add(panel, BorderLayout.CENTER);
        pack();
        setLocation(100, 20);
        this.model = m;
        setTitle(resourceBundle.getString("dialog.keyreg.title"));
        setEditable(true);
    }

    /** Action performed when the OK button is pressed. Validate the dialog's
     * contents and register the user's key with the client keystore.
     */
    protected void okAction() {
        try {
            this.model.validate();
            KeyManager.saveKey((KeyModel)model);
            status = OK_STATUS;
            dispose();
        } catch (Exception e) {
            RegistryBrowser.displayError(e);
        }
    }
}
