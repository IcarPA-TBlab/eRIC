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

//import it.cnr.icar.eric.client.ui.swing.I18nPanel;

import it.cnr.icar.eric.client.common.userModel.EmailAddressModel;
import it.cnr.icar.eric.client.ui.swing.swing.MappedDocumentListener;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryComboBoxListener;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryMappedPanel;
import it.cnr.icar.eric.client.ui.swing.swing.TextField;

import java.util.Locale;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;


/**
 * Panel for EmailAddress
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class EmailAddressPanel extends RegistryMappedPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3577584775873506853L;
	private final EmailAddressModel model;
    private final JTextField addressText = new JTextField();
    private JLabel addressLabel;
    private JLabel typeLabel;
    private AddressListener addressListener;
    private EmailTypeListener emailTypeListener;

    public EmailAddressPanel(EmailAddressModel email) {
        super(email, resourceBundle.getString("error.displayEmailAddressFailed"));
        this.model = email;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        addressLabel = new JLabel(resourceBundle.getString("label.emailAddress"), SwingConstants.LEFT);
        setConstraints(addressLabel, c, gbl, 0, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(addressLabel);

        addressListener = new AddressListener();
        this.addressText.getDocument().addDocumentListener(addressListener);
        setConstraints(addressText, c, gbl, 0, 1, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return addressText;
                }

                public String getText() throws JAXRException {
                    EmailAddress address = getEmailAddressModel().getAddress();

                    if (address != null) {
                        return address.getAddress();
                    }

                    return null;
                }
            });

        typeLabel = new JLabel(resourceBundle.getString("label.addressType"), SwingConstants.LEFT);
        setConstraints(typeLabel, c, gbl, 1, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeLabel);

        @SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox<?> typeCombo = new JComboBox(EmailAddressModel.EMAIL_TYPES);
        emailTypeListener = new EmailTypeListener();
        typeCombo.addActionListener(emailTypeListener);
        typeCombo.setEditable(true);
        setConstraints(typeCombo, c, gbl, 1, 1, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        //add(typeCombo);     Removed due to bug where change in this field changed all other fields in panel and was not being stored.   
    }

    public EmailAddressModel getEmailAddressModel() {
        return this.model;
    }

    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        super.processLocaleChange(newLocale);
        updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        setError(resourceBundle.getString("error.displayEmailAddressFailed"));
        
        addressLabel.setText(resourceBundle.getString("label.emailAddress"));
        typeLabel.setText(resourceBundle.getString("label.addressType"));
        
        addressListener.setError(resourceBundle.getString("error.setEmailAddressFailed"));
    }

    class AddressListener extends MappedDocumentListener {
        AddressListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setEmailAddressFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getEmailAddressModel().setAddress(text);
        }
    }

    class EmailTypeListener extends RegistryComboBoxListener {
        EmailTypeListener() {
            super(getEmailAddressModel(), getRegistryMappedPanel());
        }
    }
    
}
