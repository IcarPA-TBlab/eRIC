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


import it.cnr.icar.eric.client.common.userModel.PersonNameModel;
import it.cnr.icar.eric.client.ui.swing.I18nPanel;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryDocumentListener;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryMappedPanel;

import java.util.Locale;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;


/**
 * Panel for PersonName
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class PersonNamePanel extends I18nPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6510741778068621520L;
	private final PersonNameModel model;
    private JLabel firstNameLabel;
    private JLabel middleNameLabel;
    private JLabel lastNameLabel;
    private FirstNameListener firstNameListener;
    private MiddleNameListener middleNameListener;
    private LastNameListener lastNameListener;

    PersonNamePanel(PersonNameModel person) {
        super();
        this.model = person;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        firstNameLabel = new JLabel(resourceBundle.getString("label.firstName"),
                SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(firstNameLabel, c, gbl, 0, 0, 1,
            0.0, GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(firstNameLabel);

        JTextField firstNameText = new JTextField();
        firstNameListener = new FirstNameListener();
        firstNameText.getDocument().addDocumentListener(firstNameListener);
        RegistryMappedPanel.setConstraints(firstNameText, c, gbl, 0, 1, 1,
            0.75, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(firstNameText);

        middleNameLabel = new JLabel(resourceBundle.getString("label.middleName"), SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(middleNameLabel, c, gbl, 1, 0, 1,
            0.0, GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(middleNameLabel);

        JTextField middleNameText = new JTextField();
        middleNameListener = new MiddleNameListener();
        middleNameText.getDocument().addDocumentListener(middleNameListener);
        RegistryMappedPanel.setConstraints(middleNameText, c, gbl, 1, 1, 1,
            0.25, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        add(middleNameText);

        lastNameLabel = new JLabel(resourceBundle.getString("label.lastName"),
                SwingConstants.LEFT);
        RegistryMappedPanel.setConstraints(lastNameLabel, c, gbl, 0, 2, 1, 0.0,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        add(lastNameLabel);

        JTextField lastNameText = new JTextField();
        lastNameListener = new LastNameListener();
        lastNameText.getDocument().addDocumentListener(lastNameListener);
        RegistryMappedPanel.setConstraints(lastNameText, c, gbl, 0, 3, 2, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(lastNameText);
    }

    public PersonNameModel getPersonNameModel() {
        return this.model;
    }

    public PersonNamePanel getPersonNamePanel() {
        return this;
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
        
        firstNameLabel.setText(resourceBundle.getString("label.firstName"));
        middleNameLabel.setText(resourceBundle.getString("label.middleName"));
        lastNameLabel.setText(resourceBundle.getString("label.lastName"));
        
        firstNameListener.setError(resourceBundle.getString("error.setFirstName"));
        middleNameListener.setError(resourceBundle.getString("error.setMiddleName"));
        lastNameListener.setError(resourceBundle.getString("error.setLastName"));

    }
    
    class FirstNameListener extends RegistryDocumentListener {
        public FirstNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setFirstName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setFirstName(text);
        }
    }

    class MiddleNameListener extends RegistryDocumentListener {
        public MiddleNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setMiddleName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setMiddleName(text);
        }
    }

    class LastNameListener extends RegistryDocumentListener {
        LastNameListener() {
            super(getPersonNamePanel(), resourceBundle.getString("error.setLastName"));
        }

        protected void setText(String text) throws JAXRException {
            getPersonNameModel().setLastName(text);
        }
    }
}
