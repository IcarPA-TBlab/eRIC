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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExternalIdentifierPanel extends RegistryObjectPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -453350459345195751L;
	JTextField schemeText = null;
    JTextField valueText = null;

    /**
     * Creates new ExternalIdentifierPanel
     */
    public ExternalIdentifierPanel() {
        setBorder(BorderFactory.createTitledBorder("ExternalIdentifier Details"));

        //ClassificationScheme
        JLabel schemeLabel = new JLabel("ClassificationScheme:",
                SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(schemeLabel, c);
        add(schemeLabel);

        schemeText = new JTextField();
        schemeText.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(schemeText, c);
        add(schemeText);

        //Select ClassificationScheme
        JButton showSchemeButton = new JButton("Select ClassificationScheme...");
        showSchemeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    getSelectedScheme();
                }
            });
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(showSchemeButton, c);
        add(showSchemeButton);

        //Value
        JLabel valueLabel = new JLabel("Value:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueLabel, c);
        add(valueLabel);

        valueText = new JTextField();
        valueText.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(valueText, c);
        add(valueText);
    }

    private void getSelectedScheme() {
        ConceptsTreeDialog dialog = ConceptsTreeDialog.showSchemes(this, true,
                true);

        if (dialog.getStatus() == JBDialog.OK_STATUS) {
            ArrayList<?> selectedObjects = dialog.getSelectedClassificationSchemes();

            int selectionCount = selectedObjects.size();

            if (selectionCount == 0) {
                RegistryBrowser.displayError(
                    "Must select a ClassificationScheme in ClassificationScheme dialog");
            } else {
                if (selectionCount > 1) {
                    RegistryBrowser.displayError(
                        "Only one ClassificationScheme selection is allowed in ClassificationScheme dialog. Using last selection");
                }

                ExternalIdentifier externalId = (ExternalIdentifier) model;
                Object obj = selectedObjects.get(selectionCount - 1);

                try {
                    @SuppressWarnings("unused")
					String conceptNameStr = null;
                    ClassificationScheme scheme = null;

                    if (obj instanceof ClassificationScheme) {
                        scheme = (ClassificationScheme) obj;
                        externalId.setIdentificationScheme(scheme);
                    }

                    String schemeName = RegistryBrowser.getName(scheme);

                    if ((schemeName == null) || (schemeName.length() == 0)) {
                        schemeName = scheme.getKey().getId();
                    }

                    schemeText.setText(schemeName);
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        }
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ExternalIdentifier.class);

        super.setModel(obj);

        ExternalIdentifier externalId = (ExternalIdentifier) obj;

        try {
            if (externalId != null) {
                ClassificationScheme scheme = externalId.getIdentificationScheme();

                if (scheme != null) {
                    String schemeName = RegistryBrowser.getName(scheme);

                    if ((schemeName == null) || (schemeName.length() == 0)) {
                        schemeName = scheme.getKey().getId();
                    }

                    schemeText.setText(schemeName);
                }

                valueText.setText(externalId.getValue());
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            ExternalIdentifier externalId = (ExternalIdentifier) model;

            String value = valueText.getText();
            externalId.setValue(value);

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        @SuppressWarnings("unused")
		ExternalIdentifier externalId = (ExternalIdentifier) model;

        String value = valueText.getText();

        if ((value == null) || (value.length() == 0)) {
            throw new JAXRException(
                "Error. ExternalIdentifier.value length must have a value");
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        schemeText.setText("");
        valueText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        valueText.setEditable(editable);
    }
}
