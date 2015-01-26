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

//import java.text.MessageFormat;

import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.LocalizedString;


/**
 * Panel to edit/inspect a LocalizedString.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *         <a href="mailto:diego.ballve@republica.fi">Diego Ballve</a>
 */
public class LocalizedStringPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1151233748837865310L;
	JComboBox<?> localeCombo = null;
    JTextField charsetText = null;
    JTextArea valueText = null;
	JLabel charsetLabel = null;
	JLabel localeLabel= null;
	JLabel valueLabel = null;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;

    /**
     * Creates new LocalizedStringsPanel
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public LocalizedStringPanel() {
        setBorder(BorderFactory.createTitledBorder("LocalizedString Details"));

        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);

        //The name Text
        localeLabel = new JLabel(resourceBundle.getString("label.locale"),
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
        gbl.setConstraints(localeLabel, c);
        add(localeLabel);

        localeCombo = new JComboBox();
        localeCombo.setModel(new DefaultComboBoxModel(
                Locale.getAvailableLocales()));
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(localeCombo, c);
        add(localeCombo);

        charsetLabel = new JLabel(resourceBundle.getString("label.charset"),
								  SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(charsetLabel, c);
        add(charsetLabel);

        charsetText = new JTextField();
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(charsetText, c);
        add(charsetText);

        valueLabel = new JLabel(resourceBundle.getString("label.value"),
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
        gbl.setConstraints(valueLabel, c);
        add(valueLabel);

        valueText = new JTextArea();
        valueText.setLineWrap(true);
        valueText.setRows(2);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(valueText, c);
        add(valueText);
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, LocalizedString.class);

        super.setModel(obj);

        LocalizedString lString = (LocalizedString) obj;

        try {
            if (lString != null) {
                Locale lStringLocale = lString.getLocale();
                localeCombo.setSelectedItem(lStringLocale);

                String lStringCharset = lString.getCharsetName();

                if (lStringCharset != null) {
                    charsetText.setText(lStringCharset);
                }

                String lStringValue = lString.getValue();

                if (lStringValue != null) {
                    valueText.setText(lStringValue);
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            LocalizedString lString = (LocalizedString) model;

            lString.setLocale((Locale) localeCombo.getSelectedItem());

            lString.setCharsetName(charsetText.getText());

            lString.setValue(valueText.getText());

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        if (localeCombo.getSelectedItem() == null) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringNullLocale"));
        }

        if (charsetText.getText() == null) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringNullCharset"));
        }

        if (valueText.getText().length() > 256) {
            throw new JAXRException(resourceBundle.getString("error.localizedStringValueLength"));
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        localeCombo.setSelectedItem(Locale.getDefault());
        charsetText.setText("");
        valueText.setText("");
    }
}
