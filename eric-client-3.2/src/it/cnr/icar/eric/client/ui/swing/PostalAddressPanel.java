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
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.PostalAddress;


/**
 * Panel for PostalAddress
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class PostalAddressPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4566060785463749782L;
	JTextField streetNumText = null;
    JTextField streetText = null;
    JTextField cityText = null;
    JTextField stateText = null;
    JTextField postalCodeText = null;
    JTextField countryText = null;
    String[] addressTypes = { "Home", "Office", "Vacation", "Temporary" };
    @SuppressWarnings("rawtypes")
	JComboBox typeCombo = null;
    JTextField postalSchemeText = null;
	JLabel streetNumLabel = null;
	JLabel streetLabel = null;
	JLabel cityLabel = null;
	JLabel stateLabel = null;
	JLabel postalCodeLabel = null;
	JLabel countryLabel = null;
	JLabel typeLabel = null;
	JLabel postalSchemeLabel = null;

    /**
     * Used for displaying objects
     */
    public PostalAddressPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        streetNumLabel = new JLabel(resourceBundle.getString("label.streetNumber"),
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
        gbl.setConstraints(streetNumLabel, c);
        add(streetNumLabel);

        streetNumText = new JTextField();
        streetNumText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(streetNumText, c);
        add(streetNumText);

        streetLabel = new JLabel(resourceBundle.getString("label.street"),
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
        gbl.setConstraints(streetLabel, c);
        add(streetLabel);

        streetText = new JTextField();
        streetText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(streetText, c);
        add(streetText);

        cityLabel = new JLabel(resourceBundle.getString("label.city"),
							   SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(cityLabel, c);
        add(cityLabel);

        cityText = new JTextField();
        cityText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(cityText, c);
        add(cityText);

		stateLabel = new JLabel(resourceBundle.getString("label.stateProvince"),
								SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(stateLabel, c);
        add(stateLabel);

        stateText = new JTextField();
        stateText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(stateText, c);
        add(stateText);

		postalCodeLabel = new JLabel(resourceBundle.getString("label.postalCode"),
									 SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(postalCodeLabel, c);
        add(postalCodeLabel);

        postalCodeText = new JTextField();
        postalCodeText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(postalCodeText, c);
        add(postalCodeText);

		countryLabel = new JLabel(resourceBundle.getString("label.country"),
								  SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(countryLabel, c);
        add(countryLabel);

        countryText = new JTextField();
        countryText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(countryText, c);
        add(countryText);

        typeLabel = new JLabel(resourceBundle.getString("label.addressType"),
							   SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeLabel, c);

        //add(typeLabel);
        typeCombo = new JComboBox<Object>(addressTypes);
        typeCombo.setEditable(true);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(typeCombo, c);

        //add(typeCombo);
        postalSchemeLabel =
			new JLabel(resourceBundle.getString("label.postalAddressScheme"),
					   SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(postalSchemeLabel, c);

        //add(postalSchemeLabel);

        /*
        postalSchemeText = new JTextField();
        countryText.setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent comp) {
                try {
                    String text = countryText.getText();
                    postalAddress.setCountry(text);
                    RegistryBrowser.getInstance().getRootPane().updateUI();
                }
                catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
                return true;
            }
        });
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(postalSchemeText, c);
        add(postalSchemeText);
         */
    }

    public PostalAddress getPostalAddress() throws JAXRException {
        PostalAddress postalAddress = null;

        if (model != null) {
            postalAddress = (PostalAddress) getModel();
        }

        return postalAddress;
    }

    public void setPostalAddress(PostalAddress addr) throws JAXRException {
        setModel(addr);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, PostalAddress.class);

        super.setModel(obj);

        PostalAddress addr = (PostalAddress) obj;

        try {
            if (addr != null) {
                streetNumText.setText(addr.getStreetNumber());
                streetText.setText(addr.getStreet());
                cityText.setText(addr.getCity());
                stateText.setText(addr.getStateOrProvince());
                postalCodeText.setText(addr.getPostalCode());
                countryText.setText(addr.getCountry());

                //typeCombo.setText(addr.getType());
                ClassificationScheme scheme = addr.getPostalScheme();

                if (scheme != null) {
                    String schemeName = RegistryBrowser.getName(scheme);

                    if ((schemeName != null) && (schemeName.length() > 0)) {
                        postalSchemeText.setText(schemeName);
                    }
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            PostalAddress postalAddress = (PostalAddress) model;

            postalAddress.setStreetNumber(streetNumText.getText());
            postalAddress.setStreet(streetText.getText());
            postalAddress.setCity(cityText.getText());
            postalAddress.setStateOrProvince(stateText.getText());
            postalAddress.setPostalCode(postalCodeText.getText());
            postalAddress.setCountry(countryText.getText());
            postalAddress.setType((String) (typeCombo.getSelectedItem()));

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    @SuppressWarnings("unused")
	protected void validateInput() throws JAXRException {
        super.validateInput();

        PostalAddress postalAddress = (PostalAddress) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        streetNumText.setText("");
        streetText.setText("");
        cityText.setText("");
        stateText.setText("");
        postalCodeText.setText("");
        countryText.setText("");

        //typeCombo.setText("Office");
        if (postalSchemeText != null) {
            postalSchemeText.setText("");
        }
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        streetNumText.setEditable(editable);
        streetText.setEditable(editable);
        cityText.setEditable(editable);
        stateText.setEditable(editable);
        postalCodeText.setEditable(editable);
        countryText.setEditable(editable);
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

        streetNumLabel.setText(resourceBundle.getString("label.streetNumber"));
        streetLabel.setText(resourceBundle.getString("label.street"));
        cityLabel.setText(resourceBundle.getString("label.city"));
		stateLabel.setText(resourceBundle.getString("label.stateProvince"));
		postalCodeLabel.setText(resourceBundle.getString("label.postalCode"));
		countryLabel.setText(resourceBundle.getString("label.country"));
        typeLabel.setText(resourceBundle.getString("label.addressType"));
        postalSchemeLabel.setText(resourceBundle.getString("label.postalAddressScheme"));
	}
}
