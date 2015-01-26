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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;


/**
 * Panel to edit/inspect an InternationalString.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *         <a href="mailto:diego.ballve@republica.fi">Diego Ballve</a>
 */
public class InternationalStringPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -9107793811785731440L;
	LocalizedStringsList localizedStringsList = null;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;
	TitledBorder internationalStringPanelBorder = null;
	JLabel localizedStringsLabel = null;

    /**
     * Creates new LocalizedStringsPanel
     */
    public InternationalStringPanel() {
		super();

        internationalStringPanelBorder =
			BorderFactory.createTitledBorder(resourceBundle.getString("title.internationalStringDetails"));
        setBorder(internationalStringPanelBorder);

        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);

        //LocalizedStrings
        localizedStringsLabel =
			new JLabel(resourceBundle.getString("label.localizedStrings"),
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
        gbl.setConstraints(localizedStringsLabel, c);
        add(localizedStringsLabel);

        localizedStringsList = new LocalizedStringsList();
        localizedStringsList.setVisibleRowCount(3);

        JScrollPane localizedStringsListScrollPane =
			new JScrollPane(localizedStringsList);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(localizedStringsListScrollPane, c);
        add(localizedStringsListScrollPane);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, InternationalString.class);

        super.setModel(obj);

        InternationalString iString = (InternationalString) obj;

        try {
            LocalizedStringsListModel localizedStringsListModel = (LocalizedStringsListModel) localizedStringsList.getModel();

            if (iString != null) {
                Collection<?> c = iString.getLocalizedStrings();
                localizedStringsListModel.setModels(new ArrayList(c));
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            InternationalString iString = (InternationalString) model;

            LocalizedStringsListModel localizedStringsListModel = (LocalizedStringsListModel) localizedStringsList.getModel();
            iString.addLocalizedStrings(Arrays.asList(
                    localizedStringsListModel.toArray()));

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
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
		resourceBundle = JavaUIResourceBundle.getInstance();

		setLocale(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
        internationalStringPanelBorder.
			setTitle(resourceBundle.getString("title.internationalStringDetails"));
        localizedStringsLabel.
			setText(resourceBundle.getString("label.localizedStrings"));
	}
}
