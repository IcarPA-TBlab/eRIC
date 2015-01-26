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
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


/**
 * Panel used in a popup dialog that holds the information
 * about an organization such as its registry object and
 * registry entry information
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class OrganizationPanel extends RegistryObjectPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1888054048993175305L;
	RegistryObjectPanel roPanel = null;
    JBList phonesList = null;
    JBList addrsList = null;
    JTextField primContactText = null;
    JBEditorDialog postalAddressDialog = null;
    JBEditorDialog primaryContactDialog = null;

    /**
     * Creates new OrganizationPanel
     */
    public OrganizationPanel() {
        super();

        setBorder(BorderFactory.createTitledBorder("Organization Details"));

        JLabel addrLabel = new JLabel("Postal Address:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addrLabel, c);
        add(addrLabel);

        addrsList = new JBList(LifeCycleManager.POSTAL_ADDRESS,
                new JBListModel());
        addrsList.setVisibleRowCount(3);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(addrsList, c);
        add(addrsList);

        //Telephone Number
        JLabel phonesLabel = new JLabel("Telephone Numbers:",
                SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(phonesLabel, c);
        add(phonesLabel);

        phonesList = new JBList(LifeCycleManager.TELEPHONE_NUMBER,
                new JBListModel());
        phonesList.setVisibleRowCount(2);

        JScrollPane phonesListScrollPane = new JScrollPane(phonesList);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(phonesListScrollPane, c);
        add(phonesListScrollPane);

        //Primary contact
        JLabel primContactLabel = new JLabel("Primary Contact:",
                SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactLabel, c);
        add(primContactLabel);

        primContactText = new JTextField();
        primContactText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactText, c);
        add(primContactText);

        JButton primContactButton = new JButton("Contact Details...");
        primContactButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        getPrimaryContactDialog().setVisible(true);
                    } catch (JAXRException e) {
                        RegistryBrowser.displayError(e);
                    }
                }
            });
        c.gridx = 1;
        c.gridy = row + 5;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(primContactButton, c);
        add(primContactButton);
    }

    @SuppressWarnings("unused")
	private JBEditorDialog getPostalAddressDialog() throws JAXRException {
        if (postalAddressDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof JFrame) {
                postalAddressDialog = new JBEditorDialog((JFrame) window, false);
            } else if (window instanceof JDialog) {
                postalAddressDialog = new JBEditorDialog((JDialog) window, false);
            }

            postalAddressDialog.setLocation(50, 50);

            if (model != null) {
                postalAddressDialog.setModel(getOrganization().getPostalAddress());
            }

            postalAddressDialog.setEditable(editable);
        }

        return postalAddressDialog;
    }

    private JBEditorDialog getPrimaryContactDialog() throws JAXRException {
        if (primaryContactDialog == null) {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof JFrame) {
                primaryContactDialog = new JBEditorDialog((JFrame) window, false);
            } else if (window instanceof JDialog) {
                primaryContactDialog = new JBEditorDialog((JDialog) window,
                        false);
            }

            primaryContactDialog.setLocation(50, 50);

            //if (model != null) {
            primaryContactDialog.setModel(getOrganization().getPrimaryContact());

            //}
            primaryContactDialog.setEditable(editable);
        }

        return primaryContactDialog;
    }

    Organization getOrganization() throws JAXRException {
        Organization organization = null;

        if (model != null) {
            organization = (Organization) getModel();
        }

        return organization;
    }

    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, Organization.class);

        super.setModel(obj);

        Organization org = (Organization) obj;

        try {
            int registryLevel = RegistryBrowser.client.getCapabilityProfile()
                                                      .getCapabilityLevel();

            DefaultListModel<TelephoneNumber> phonesModel = (DefaultListModel) phonesList.getModel();
            DefaultListModel<PostalAddress> addrsModel = (DefaultListModel) addrsList.getModel();

            if (org != null) {
                PostalAddress addr = org.getPostalAddress();
                String addrStr = "";

                if (addr != null) {
                    addrsModel.addElement(addr);
                }

                Collection phones = org.getTelephoneNumbers(null);
                Iterator iter = phones.iterator();

                while (iter.hasNext()) {
                    TelephoneNumber phone = (TelephoneNumber) iter.next();
                    phonesModel.addElement(phone);
                }

                User primaryContact = org.getPrimaryContact();
                String userName = RegistryBrowser.getUserName(primaryContact,
                        registryLevel);
                primContactText.setText(userName);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        Organization org = (Organization) model;

        JBListModel phonesModel = (JBListModel) phonesList.getModel();
        org.setTelephoneNumbers(phonesModel.getModels());

        /*
        try {
           concept.setValue(valueText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }
        catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        */
    }

    @SuppressWarnings("rawtypes")
	public void clear() throws JAXRException {
        DefaultListModel phonesModel = (DefaultListModel) phonesList.getModel();
        phonesModel.clear();

        DefaultListModel addrsModel = (DefaultListModel) addrsList.getModel();
        addrsModel.clear();
        primContactText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        phonesList.setEditable(editable);
        addrsList.setEnabled(editable);
    }
}
