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


import it.cnr.icar.eric.client.common.userModel.PostalAddressModel;
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
import javax.xml.registry.infomodel.PostalAddress;


/**
 * Panel for PostalAddress
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class PostalAddressPanel extends RegistryMappedPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5402088906528886674L;
	private final PostalAddressModel model;
    private final JTextField streetNumText = new JTextField();
    private final JTextField streetText = new JTextField();
    private final JTextField cityText = new JTextField();
    private final JTextField stateText = new JTextField();
    private final JTextField postalCodeText = new JTextField();
    private final JTextField countryText = new JTextField();
    private JLabel streetNumLabel;
    private JLabel streetLabel;
    private JLabel cityLabel;
    private JLabel stateLabel;
    private JLabel postalCodeLabel;
    private JLabel countryLabel;
    private JLabel typeLabel;
    private StreetNumListener streetNumListener;
    private StreetListener streetListener;
    private CityListener cityListener;
    private StateListener stateListener;
    private PostalCodeListener postalCodeListener;
    private CountryListener countryListener;
    private AddressTypeListener addressTypeListener;
            

    PostalAddressPanel(PostalAddressModel address) {
        super(address, resourceBundle.getString("error.displayPostalAddressFailed"));
        this.model = address;
        this.model.addObserver(this);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        streetNumLabel = new JLabel(resourceBundle.getString("label.streetNumber"), SwingConstants.LEFT);
        setConstraints(streetNumLabel, c, gbl, 0, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(streetNumLabel);

        streetNumListener = new StreetNumListener();
        streetNumText.getDocument().addDocumentListener(streetNumListener);
        setConstraints(streetNumText, c, gbl, 0, 1, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return streetNumText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStreetNumber();
                    }

                    return null;
                }
            });

        streetLabel = new JLabel(resourceBundle.getString("label.street"), SwingConstants.LEFT);
        setConstraints(streetLabel, c, gbl, 1, 0, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(streetLabel);

        streetListener = new StreetListener();
        streetText.getDocument().addDocumentListener(streetListener);
        setConstraints(streetText, c, gbl, 1, 1, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return streetText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStreet();
                    }

                    return null;
                }
            });

        cityLabel = new JLabel(resourceBundle.getString("label.city"), SwingConstants.LEFT);
        setConstraints(cityLabel, c, gbl, 0, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(cityLabel);

        cityListener = new CityListener();
        cityText.getDocument().addDocumentListener(cityListener);
        setConstraints(cityText, c, gbl, 0, 3, 1, 0.5, GridBagConstraints.BOTH,
            GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return cityText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getCity();
                    }

                    return null;
                }
            });

        stateLabel = new JLabel(resourceBundle.getString("label.stateProvince"), SwingConstants.LEFT);
        setConstraints(stateLabel, c, gbl, 1, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(stateLabel);

        stateListener = new StateListener();
        stateText.getDocument().addDocumentListener(stateListener);
        setConstraints(stateText, c, gbl, 1, 3, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return stateText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getStateOrProvince();
                    }

                    return null;
                }
            });

        postalCodeLabel = new JLabel(resourceBundle.getString("label.postalCode"), SwingConstants.LEFT);
        setConstraints(postalCodeLabel, c, gbl, 0, 4, 1, 0.0,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        add(postalCodeLabel);

        postalCodeListener = new PostalCodeListener();
        postalCodeText.getDocument().addDocumentListener(postalCodeListener);
        setConstraints(postalCodeText, c, gbl, 0, 5, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return postalCodeText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getPostalCode();
                    }

                    return null;
                }
            });

        countryLabel = new JLabel(resourceBundle.getString("label.country"), SwingConstants.LEFT);
        setConstraints(countryLabel, c, gbl, 1, 4, 1, 0.0,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(countryLabel);

        countryListener = new CountryListener();
        countryText.getDocument().addDocumentListener(countryListener);
        setConstraints(countryText, c, gbl, 1, 5, 1, 0.5,
            GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        addTextField(new TextField() {
                public JTextField getTextField() {
                    return countryText;
                }

                public String getText() throws JAXRException {
                    PostalAddress address = getPostalAddressModel().getAddress();

                    if (address != null) {
                        return address.getCountry();
                    }

                    return null;
                }
            });

        typeLabel = new JLabel(resourceBundle.getString("label.addressType"), SwingConstants.LEFT);
        setConstraints(typeLabel, c, gbl, 0, 6, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeLabel);

        @SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox typeCombo = new JComboBox(PostalAddressModel.ADDRESS_TYPES);
        typeCombo.setEditable(true);
        addressTypeListener = new AddressTypeListener();
        typeCombo.addActionListener(addressTypeListener);
        setConstraints(typeCombo, c, gbl, 0, 7, 1, 0.5,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        //add(typeCombo);   Removed due to bug where change in this field changed all other fields in panel and was not being stored.   

    }

    public PostalAddressModel getPostalAddressModel() {
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

        setError(resourceBundle.getString("error.displayPostalAddressFailed"));
        
        streetNumLabel.setText(resourceBundle.getString("label.streetNumber"));
        streetLabel.setText(resourceBundle.getString("label.street"));
        cityLabel.setText(resourceBundle.getString("label.city"));
        stateLabel.setText(resourceBundle.getString("label.stateProvince"));
        postalCodeLabel.setText(resourceBundle.getString("label.postalCode"));
        countryLabel.setText(resourceBundle.getString("label.country"));
        typeLabel.setText(resourceBundle.getString("label.addressType"));

        streetNumListener.setError(resourceBundle.getString("error.setStreetNumberFailed"));
        streetListener.setError(resourceBundle.getString("error.setStreetFailed"));
        cityListener.setError(resourceBundle.getString("error.setCityFailed"));
        stateListener.setError(resourceBundle.getString("error.setStateFailed"));
        postalCodeListener.setError(resourceBundle.getString("error.setPostalCodeFailed"));
        countryListener.setError(resourceBundle.getString("error.setCountryFailed"));
    }

    class StreetNumListener extends MappedDocumentListener {
        public StreetNumListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStreetNumberFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setStreetNum(text);
        }
    }

    class StreetListener extends MappedDocumentListener {
        public StreetListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStreetFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setStreet(text);
        }
    }

    class CityListener extends MappedDocumentListener {
        public CityListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setCityFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setCity(text);
        }
    }

    class StateListener extends MappedDocumentListener {
        public StateListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setStateFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setState(text);
        }
    }

    class PostalCodeListener extends MappedDocumentListener {
        public PostalCodeListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setPostalCodeFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setPostalCode(text);
        }
    }

    class CountryListener extends MappedDocumentListener {
        public CountryListener() {
            super(getRegistryMappedPanel(), resourceBundle.getString("error.setCountryFailed"));
        }

        protected void setText(String text) throws JAXRException {
            getPostalAddressModel().setCountry(text);
        }
    }

    class AddressTypeListener extends RegistryComboBoxListener {
        AddressTypeListener() {
            super(getPostalAddressModel(), getRegistryMappedPanel());
        }
    }
}
