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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ClassificationSchemePanel extends RegistryObjectPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8558373848649312248L;
	JCheckBox externalCheckBox = null;
    String[] valueTypes = { "Embedded Path", "Non-Unique", "Unique" };
    JComboBox<?> valueTypeCombo = null;

    /**
     * Creates new ServicePanel
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public ClassificationSchemePanel() {
        setBorder(BorderFactory.createTitledBorder("ClassificationScheme"));

        externalCheckBox = new JCheckBox("External ClassificationScheme");
        externalCheckBox.setSelected(false);
        externalCheckBox.setEnabled(false);
        c.gridx = 0;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(externalCheckBox, c);
        add(externalCheckBox);

        JLabel valueTypeLabel = new JLabel("Value Type:", SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueTypeLabel, c);
        add(valueTypeLabel);

        valueTypeCombo = new JComboBox(valueTypes);
        valueTypeCombo.setEditable(false);
        c.gridx = 0;
        c.gridy = row + 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueTypeCombo, c);
        add(valueTypeCombo);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ClassificationScheme.class);

        super.setModel(obj);

        ClassificationScheme classificationScheme = (ClassificationScheme) obj;

        try {
            if (classificationScheme != null) {
                externalCheckBox.setSelected(classificationScheme.isExternal());
            }
            
            int valueType = classificationScheme.getValueType();
            
            if (valueType == ClassificationScheme.VALUE_TYPE_EMBEDDED_PATH) {
                valueTypeCombo.setSelectedIndex(0);
            } else if (valueType == ClassificationScheme.VALUE_TYPE_NON_UNIQUE) {
                valueTypeCombo.setSelectedIndex(1);
            } else if (valueType == ClassificationScheme.VALUE_TYPE_UNIQUE) {
                valueTypeCombo.setSelectedIndex(2);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();
        
        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        @SuppressWarnings("unused")
		ClassificationScheme classificationScheme = (ClassificationScheme) model;
    }

    public void clear() throws JAXRException {
        super.clear();
        externalCheckBox.setSelected(false);
    }
}
