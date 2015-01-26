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


import it.cnr.icar.eric.client.common.userModel.TelephoneNumberModel;
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
import javax.xml.registry.infomodel.TelephoneNumber;


/**
 * Panel for TelephoneNumber
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class TelephoneNumberPanel extends RegistryMappedPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2867262424275675589L;
	private final TelephoneNumberModel model;
    private final JTextField countryCodeText = new JTextField();
    private final JTextField areaCodeText = new JTextField();
    private final JTextField numberText = new JTextField();
    private final JTextField extensionText = new JTextField();
    private final JTextField urlText = new JTextField();
    private JLabel countryCodeLabel;
    private JLabel areaCodeLabel;
    private JLabel numberLabel;
    private JLabel extensionLabel;
    private JLabel urlLabel;
    private JLabel typeLabel;
    private CountryCodeListener countryCodeListener;
    private AreaCodeListener areaCodeListener;
    private NumberListener numberListener;
    private ExtensionListener extensionListener;
    private URLListener urlListener;
    private PhoneTypeListener phoneTypeListener;

    TelephoneNumberPanel(TelephoneNumberModel number) {
        super(number, resourceBundle.getString("error.displayTelephoneNumberFailed"));
        this.model = number;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        countryCodeLabel = new JLabel(resourceBundle.getString("label.countryCode"), SwingConstants.LEFT);
        setConstraints(countryCodeLabel, c, gbl, 0, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(countryCodeLabel);

        countryCodeListener = new CountryCodeListener();
        countryCodeText.getDocument().addDocumentListener(countryCodeListener);
        setConstraints(countryCodeText, c, gbl, 0, 1, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return countryCodeText;
                }

                public String getText() throws JAXRException {
                    TelephoneNumber number = getTelephoneNumberModel()
                                                 .getNumber();

                    if (number != null) {
                        return number.getCountryCode();
                    }

                    return null;
                }
            });

        areaCodeLabel = new JLabel(resourceBundle.getString("label.areaCode"), SwingConstants.LEFT);
        setConstraints(areaCodeLabel, c, gbl, 1, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(areaCodeLabel);

        areaCodeListener = new AreaCodeListener();
        areaCodeText.getDocument().addDocumentListener(areaCodeListener);
        setConstraints(areaCodeText, c, gbl, 1, 1, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return areaCodeText;
                }

                public String getText() throws JAXRException {
                    TelephoneNumber number = getTelephoneNumberModel()
                                                 .getNumber();

                    if (number != null) {
                        return number.getAreaCode();
                    }

                    return null;
                }
            });

        numberLabel = new JLabel(resourceBundle.getString("label.number"), SwingConstants.LEFT);
        setConstraints(numberLabel, c, gbl, 0, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(numberLabel);

        numberListener = new NumberListener();
        numberText.getDocument().addDocumentListener(numberListener);
        setConstraints(numberText, c, gbl, 0, 3, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return numberText;
                }

                public String getText() throws JAXRException {
                    TelephoneNumber number = getTelephoneNumberModel()
                                                 .getNumber();

                    if (number != null) {
                        return number.getNumber();
                    }

                    return null;
                }
            });

        extensionLabel = new JLabel(resourceBundle.getString("label.extension"), SwingConstants.LEFT);
        setConstraints(extensionLabel, c, gbl, 1, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(extensionLabel);

        extensionListener = new ExtensionListener();
        extensionText.getDocument().addDocumentListener(extensionListener);
        setConstraints(extensionText, c, gbl, 1, 3, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return extensionText;
                }

                public String getText() throws JAXRException {
                    TelephoneNumber number = getTelephoneNumberModel()
                                                 .getNumber();

                    if (number != null) {
                        return number.getExtension();
                    }

                    return null;
                }
            });

        urlLabel = new JLabel(resourceBundle.getString("label.url"), SwingConstants.LEFT);
        setConstraints(urlLabel, c, gbl, 0, 4, 1, 0.0, GridBagConstraints.NONE,
            GridBagConstraints.WEST);
        //add(urlLabel);

        urlListener = new URLListener();
        urlText.getDocument().addDocumentListener(urlListener);
        setConstraints(urlText, c, gbl, 0, 5, 1, 0.5, GridBagConstraints.BOTH,
            GridBagConstraints.WEST);
        /*
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return urlText;
                }

                public String getText() throws JAXRException {
                    TelephoneNumber number = getTelephoneNumberModel()
                                                 .getNumber();

                    if (number != null) {
                        return number.getUrl();
                    }

                    return null;
                }
            });
        */
        typeLabel = new JLabel(resourceBundle.getString("label.phoneType"), SwingConstants.LEFT);
        setConstraints(typeLabel, c, gbl, 0, 6, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeLabel);

        @SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox typeCombo = new JComboBox(TelephoneNumberModel.PHONE_TYPES);
        phoneTypeListener = new PhoneTypeListener();
        typeCombo.addActionListener(phoneTypeListener);
        typeCombo.setEditable(true);
        setConstraints(typeCombo, c, gbl, 0, 7, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        //add(typeCombo);   Removed due to bug where change in this field changed all other fields in panel and was not being stored.   
    }

    public TelephoneNumberModel getTelephoneNumberModel() {
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
        
        setError(resourceBundle.getString("error.displayTelephoneNumberFailed"));
        
        countryCodeLabel.setText(resourceBundle.getString("label.countryCode"));
        areaCodeLabel.setText(resourceBundle.getString("label.areaCode"));
        numberLabel.setText(resourceBundle.getString("label.number"));
        extensionLabel.setText(resourceBundle.getString("label.extension"));
        urlLabel.setText(resourceBundle.getString("label.url"));
        typeLabel.setText(resourceBundle.getString("label.phoneType"));
        
        countryCodeListener.setError(resourceBundle.getString("error.setCountryCodeFailed"));
        areaCodeListener.setError(resourceBundle.getString("error.setAreaCodeFailed"));
        numberListener.setError(resourceBundle.getString("error.setNumberFailed"));
        extensionListener.setError(resourceBundle.getString("error.setExtensionFailed"));
        urlListener.setError(resourceBundle.getString("error.setUrlFailed"));
    }

    class CountryCodeListener extends MappedDocumentListener {
        public CountryCodeListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setCountryCodeFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getTelephoneNumberModel().setCountryCode(text);
        }
    }

    class AreaCodeListener extends MappedDocumentListener {
        public AreaCodeListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setAreaCodeFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getTelephoneNumberModel().setAreaCode(text);
        }
    }

    class NumberListener extends MappedDocumentListener {
        public NumberListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setNumberFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getTelephoneNumberModel().setNumber(text);
        }
    }

    class ExtensionListener extends MappedDocumentListener {
        public ExtensionListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setExtensionFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getTelephoneNumberModel().setExtension(text);
        }
    }

    class URLListener extends MappedDocumentListener {
        public URLListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setUrlFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getTelephoneNumberModel().setURL(text);
        }
    }

    class PhoneTypeListener extends RegistryComboBoxListener {
        public PhoneTypeListener() {
            super(getTelephoneNumberModel(), getRegistryMappedPanel());
        }
    }
}
