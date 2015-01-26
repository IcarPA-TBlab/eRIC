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

import java.beans.PropertyChangeEvent;

import java.util.Locale;

//import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ServiceBinding;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ServiceBindingPanel extends RegistryObjectPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5182662526949199475L;
	JTextField accessURIText = null;
	TitledBorder serviceBindingPanelBorder = null;
	HyperLinkLabel accessURILabel = null;

    /**
     * Creates new ServiceBindingPanel
     */
    public ServiceBindingPanel() {
        super();
        serviceBindingPanelBorder =
			javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("title.serviceBinding"));
        setBorder(serviceBindingPanelBorder);

		accessURILabel =
			new HyperLinkLabel(resourceBundle.getString("label.accessURI"),
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
        gbl.setConstraints(accessURILabel, c);
        add(accessURILabel);

        accessURIText = new JTextField();
        accessURIText.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(accessURIText, c);
        add(accessURIText);

        accessURILabel.setHyperLinkContainer(new HyperLinkContainer() {
                public String getURL() {
                    return (accessURIText.getText());
                }

                public void setURL(String url) {
                    accessURIText.setText(url);
                }
            });
    }

    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ServiceBinding.class);

        super.setModel(obj);

        ServiceBinding binding = (ServiceBinding) obj;

        try {
            if (binding != null) {
                String accessURIStr = binding.getAccessURI();

                if (accessURIStr != null) {
                    accessURIText.setText(accessURIStr);
                }
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            ServiceBinding serviceBinding = (ServiceBinding) model;

            String accessURITextStr = accessURIText.getText();
            serviceBinding.setAccessURI(accessURITextStr);

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    public void clear() throws JAXRException {
        super.clear();
        accessURIText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        accessURIText.setEditable(editable);
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

        serviceBindingPanelBorder.
			setTitle(resourceBundle.getString("title.serviceBinding"));

		accessURILabel.setText(resourceBundle.getString("label.accessURI"));
	}
}
