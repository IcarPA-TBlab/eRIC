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
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Concept;


/**
 * Panel to edit/inspect a Service.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExternalLinkPanel extends RegistryObjectPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6005566930387249346L;
	HyperLinkLabel externalURILabel = null;
    JTextField externalURIText = null;
    JTextField objectTypeText = null;


    /**
     * Creates new ExternalLinkPanel
     */
    public ExternalLinkPanel() {
        setBorder(BorderFactory.createTitledBorder("External Link"));

        HyperLinkLabel externalURILabel = new HyperLinkLabel("External URI:",
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
        gbl.setConstraints(externalURILabel, c);
        add(externalURILabel);

        externalURIText = new JTextField();
        externalURIText.setEditable(editable);
        c.gridx = 0;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(externalURIText, c);
        add(externalURIText);

        externalURILabel.setHyperLinkContainer(new HyperLinkContainer() {
                public String getURL() {
                    return (externalURIText.getText());
                }

                public void setURL(String url) {
                    externalURIText.setText(url);
                }
            });
            
        //Concept
        JLabel objectTypeLabel =
		  new JLabel(resourceBundle.getString("label.objectType"),
			     SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = row + 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(objectTypeLabel, c);
        add(objectTypeLabel);
        
        objectTypeText = new JTextField();
        objectTypeText.setEditable(false);
        c.gridx = 1;
        c.gridy = row + 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(objectTypeText, c);
        add(objectTypeText);
        
        //Select ClassificationScheme or Concept
        JButton showSchemeButton =
	    new JButton(resourceBundle.getString("button.selectConcept"));
        showSchemeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                getSelectedConcept();
            }
        });
        c.gridx = 1;
        c.gridy = row + 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(showSchemeButton, c);
        add(showSchemeButton);
            
    }

    private void getSelectedConcept() {
        ConceptsTreeDialog dialog =
	    ConceptsTreeDialog.showSchemes(this, true, true);

        if (dialog.getStatus() == JBDialog.OK_STATUS) {
            ArrayList<?> selectedObjects = dialog.getSelectedObjects();
            
            int selectionCount = selectedObjects.size();
            
            if (selectionCount == 0) {
                RegistryBrowser.displayError(
                resourceBundle.getString("error.mustSelectConcept"));
            } else {
                if (selectionCount > 1) {
                    RegistryBrowser.displayError(
                    resourceBundle.getString("error.onlyOneSelection"));
                }
                
                ExternalLink el = (ExternalLink) model;
                Object obj = selectedObjects.get(selectionCount - 1);
                
                try {
                    if (obj instanceof Concept) {
                        Concept concept = (Concept) obj;
                        ((it.cnr.icar.eric.client.xml.registry.infomodel.ExternalLinkImpl) el).setObjectType(concept);
                        objectTypeText.setText(concept.getValue());
                    } else {
                        throw new JAXRException(resourceBundle.getString("error.mustSelectConceptOnly"));
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }
        }
    }
    
    public void setModel(Object obj) throws JAXRException {
        RegistryBrowser.isInstanceOf(obj, ExternalLink.class);

        super.setModel(obj);

        ExternalLink externalLink = (ExternalLink) obj;

        try {
            if (externalLink != null) {
                String externalURIStr = externalLink.getExternalURI();

                if (externalURIStr != null) {
                    externalURIText.setText(externalURIStr);
                }
                
                Concept objectType = externalLink.getObjectType();
                
                if (objectType != null) {
                    objectTypeText.setText(objectType.getValue());
                }
                
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    public Object getModel() throws JAXRException {
        super.getModel();

        if (model != null) {
            ExternalLink externalLink = (ExternalLink) model;

            String externalURIStr = externalURIText.getText().trim();
            externalLink.setExternalURI(externalURIStr);

            RegistryBrowser.getInstance().getRootPane().updateUI();
        }

        return model;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        String externalURIStr = externalURIText.getText().trim();

        if (externalURIStr.length() == 0) {
            throw new JAXRException(
                "Error. externalURI attribute must not be empty.");
        }
    }

    public void clear() throws JAXRException {
        super.clear();
        externalURIText.setText("");
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        externalURIText.setEditable(editable);
        externalURIText.setEditable(editable);
    }
}
