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

//import it.cnr.icar.eric.client.ui.swing.JavaUIResourceBundle;

import it.cnr.icar.eric.client.common.userModel.UserModel;
import it.cnr.icar.eric.client.ui.swing.I18nPanel;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryDocumentListener;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryMappedPanel;
import it.cnr.icar.eric.client.xml.registry.infomodel.UserImpl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.xml.registry.JAXRException;



/**
 * Panel for User
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Fabian Ritzmann
 */
public class UserRegistrationPanel extends I18nPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3740541530124081455L;
	private final JPanel panel;
    private final UserModel model;
    private TitledBorder userDetailsBorder;
    private TitledBorder personNameBorder;
    private TitledBorder emailAddressBorder;
    private TitledBorder postalAddressBorder;
    private TitledBorder telephoneNumberBorder;
    private TitledBorder digitalCertificateBorder;
    private JTextField idText;

    /**
     * Extends UserPanel to add fields to get alias and password
     */
    public UserRegistrationPanel(UserModel user) throws JAXRException {
        super();
        this.panel = this;
        this.model = user;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        userDetailsBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.userDetails"));
        setBorder(userDetailsBorder);

        //userId field
        JLabel idLabel = new JLabel(resourceBundle.getString("label.uniqueIdentifier"),
            SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idLabel, c);
        add(idLabel);
        
        idText = new JTextField();
        idText.setEditable(true);
        idText.getDocument().addDocumentListener(new IDListener(UserRegistrationPanel.this));
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idText, c);
        add(idText);
        idText.setText(user.getUser().getKey().getId());
        
        //PersonNamePanel
        PersonNamePanel personNamePanel = new PersonNamePanel(user.getPersonNameModel());
        personNameBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.name"));
        personNamePanel.setBorder(personNameBorder);
        RegistryMappedPanel.setConstraints(personNamePanel, c, gbl, 0, 2, 1,
            0.5, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(personNamePanel);
        
        //EmailAddressPanel
        EmailAddressPanel emailPanel = new EmailAddressPanel(user.getEmailAddressModel());
        emailAddressBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.emailAddress"));
        emailPanel.setBorder(emailAddressBorder);
        RegistryMappedPanel.setConstraints(emailPanel, c, gbl, 1, 2, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(emailPanel);

        //PostalAddressPanel
        PostalAddressPanel addressPanel = new PostalAddressPanel(user.getPostalAddressModel());
        postalAddressBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.postalAddress"));
        addressPanel.setBorder(postalAddressBorder);
        RegistryMappedPanel.setConstraints(addressPanel, c, gbl, 0, 3, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(addressPanel);

        //TelephoneNumberPanel
        TelephoneNumberPanel phonePanel = new TelephoneNumberPanel(user.getTelephoneNumberModel());
        telephoneNumberBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.telephoneNumber"));
        phonePanel.setBorder(telephoneNumberBorder);
        RegistryMappedPanel.setConstraints(phonePanel, c, gbl, 1, 3, 1, 0.5,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        add(phonePanel);
        
        CertificateInfoPanel certPanel = new CertificateInfoPanel(user.getUserRegistrationInfo());
        digitalCertificateBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.certificate"));
        certPanel.setBorder(digitalCertificateBorder);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(certPanel, c);
        add(certPanel);
        
    }
    
    class IDListener extends RegistryDocumentListener {
        public IDListener(JPanel panel) {
            super(panel, resourceBundle.getString("message.error.CantSetUniqueId"));
        }

        protected void setText(String text) throws JAXRException {
            UserImpl user = (UserImpl)model.getUser();
            user.getKey().setId(text);
            user.setLid(text);
        }
    }    
    
    
    public UserModel getUserModel() {
        return this.model;
    }

    public JPanel getPanel() {
        return this.panel;
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

        userDetailsBorder.setTitle(resourceBundle.getString("title.userDetails"));
        personNameBorder.setTitle(resourceBundle.getString("title.name"));
        emailAddressBorder.setTitle(resourceBundle.getString("title.emailAddress"));
        postalAddressBorder.setTitle(resourceBundle.getString("title.postalAddress"));
        telephoneNumberBorder.setTitle(resourceBundle.getString("title.telephoneNumber"));
        digitalCertificateBorder.setTitle(resourceBundle.getString("title.certificate"));
    }
    
}
