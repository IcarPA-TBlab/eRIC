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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;

import java.util.Locale;

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
 */
public class EmailAddressPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2306870454160570639L;
	JTextField addressText = null;
    String[] emailTypes = { resourceBundle.getString("type.officeEmail"),
			    resourceBundle.getString("type.homeEmail") };
    JComboBox<?> typeCombo = null;
	JLabel addressLabel = null;
	JLabel typeLabel = null;

    /**
     * Used for displaying objects
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public EmailAddressPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

		addressLabel = new JLabel(resourceBundle.getString("label.emailAddress"),
								  SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addressLabel, c);
        add(addressLabel);

        addressText = new JTextField();
        addressText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addressText, c);
        add(addressText);

		typeLabel = new JLabel(resourceBundle.getString("label.addressType"),
							   SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeLabel, c);
        add(typeLabel);

        typeCombo = new JComboBox(emailTypes);
        typeCombo.setEditable(true);
        typeCombo.setEnabled(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeCombo, c);
        add(typeCombo);
    }

    public EmailAddress getEmailAddress() throws JAXRException {
        EmailAddress email = null;

        if (model != null) {
            email = (EmailAddress) getModel();
        }

        return email;
    }

    public void setEmailAddress(EmailAddress email) throws JAXRException {
        setModel(email);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, EmailAddress.class);

        super.setModel(obj);

        EmailAddress email = (EmailAddress) obj;

        try {
            if (email != null) {
                addressText.setText(email.getAddress());
                typeCombo.setSelectedItem(email.getType());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public void setEmailAddress(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, EmailAddress.class);

        super.setModel(obj);

        EmailAddress email = (EmailAddress) obj;

        try {
            if (email != null) {
                addressText.setText(email.getAddress());
                typeCombo.setSelectedItem(email.getType());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            EmailAddress emailAddress = (EmailAddress) model;

            emailAddress.setAddress(addressText.getText());

            emailAddress.setType((String) typeCombo.getSelectedItem());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        @SuppressWarnings("unused")
		EmailAddress emailAddress = (EmailAddress) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        addressText.setText("");
        typeCombo.setSelectedIndex(0);
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        addressText.setEditable(editable);
        typeCombo.setEnabled(editable);
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
        }
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

		addressLabel.setText(resourceBundle.getString("label.emailAddress"));
		typeLabel.setText(resourceBundle.getString("label.addressType"));
		emailTypes = new String[] { resourceBundle.getString("type.officeEmail"),
					    resourceBundle.getString("type.homeEmail") };
	}
}
