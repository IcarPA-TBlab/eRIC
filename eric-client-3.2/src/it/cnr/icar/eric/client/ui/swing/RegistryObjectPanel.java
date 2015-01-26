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


import it.cnr.icar.eric.client.ui.swing.swing.RegistryDocumentListener;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.common.BindingUtility;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.RegistryObject;

import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;

/**
 * Currently only used for Organizations.
 * Holds organization informattion and a ClassificationsPanel.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6258286810373680083L;
	JTextField idText = null;
    JTextField lidText = null;
    JTextField versionNameText = null;
    JTextField commentText = null;
    JButton showNameInternationalStringButton = null;
    JTextField nameText = null;
    JTextArea descriptionText = null;
    JTextField nameLocaleText = null;
    JTextField descriptionLocaleText = null;
    JButton showDescriptionInternationalStringButton = null;
    ClassificationsList classificationsList = null;
    ExternalIdentifiersList extIdsList = null;
    ExternalLinksList linksList = null;
    SlotsList slotsList = null;
    JButton submitterButton = null;
    // Labels for text fields, etc.
    JLabel classificationsLabel = null;
    JLabel descriptionLabel = null;
    JLabel extIdsLabel = null;
    JLabel statusLabel = null;
    JLabel idLabel = null;
    JLabel lidLabel = null;
    JLabel versionNameLabel = null;
    JLabel commentLabel = null;
    JLabel linksLabel = null;
    JLabel nameLabel = null;
    JLabel slotsLabel = null;
    protected int row = 0;
    protected GridBagConstraints c = null;
    protected GridBagLayout gbl = null;
    
    /** The Locale used to get/set LocalizedStrings from/to model. */
    private Locale localeOnSetModel = null;
    private Locale nameLocaleOnSetModel = null;
    private Locale descriptionLocaleOnSetModel = null;
    
    /**
     * Used for displaying objects
     */
    public RegistryObjectPanel() {
        gbl = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gbl);
        
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());
        namePanel.setComponentOrientation(getComponentOrientation());
        
        //System.out.println("RegistryObjectPanel:: locale: " + getLocale());
        //System.out.println("RegistryObjectPanel:: isLeftToRight: " + getComponentOrientation().isLeftToRight());
        
        //The name Text
        nameLabel = new JLabel(resourceBundle.getString("label.name"),
        SwingConstants.LEADING);
        namePanel.add(nameLabel, BorderLayout.LINE_START);
        
        nameLocaleText = new JTextField();
        nameLocaleText.setEditable(false);
        nameLocaleText.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        nameLocaleText.setAlignmentX(JTextField.CENTER_ALIGNMENT);
        nameLocaleText.setHorizontalAlignment(JTextField.LEADING);
        namePanel.add(nameLocaleText, BorderLayout.CENTER);
        
        showNameInternationalStringButton =
        new JButton(resourceBundle.getString("button.details"));
        showNameInternationalStringButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showNameInternationalString();
            }
        });
        namePanel.add(showNameInternationalStringButton, BorderLayout.LINE_END);
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(namePanel, c);
        add(namePanel);
        
        nameText = new JTextField();
        nameText.setEditable(editable);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(nameText, c);
        add(nameText);
        
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BorderLayout());
        descriptionPanel.setComponentOrientation(getComponentOrientation());
        
        //The description Text
        descriptionLabel =
        new JLabel(resourceBundle.getString("label.description"),
        SwingConstants.TRAILING);
        descriptionPanel.add(descriptionLabel, BorderLayout.LINE_START);
        
        descriptionLocaleText = new JTextField();
        descriptionLocaleText.setEditable(false);
        descriptionLocaleText.setBorder(BorderFactory.createEmptyBorder(0, 5,
        0, 5));
        descriptionLocaleText.setAlignmentX(JTextField.CENTER_ALIGNMENT);
        descriptionPanel.add(descriptionLocaleText, BorderLayout.CENTER);
        
        showDescriptionInternationalStringButton =
        new JButton(resourceBundle.getString("button.details"));
        showDescriptionInternationalStringButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showDescriptionInternationalString();
            }
        });
        descriptionPanel.add(showDescriptionInternationalStringButton,
        BorderLayout.LINE_END);
        
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(descriptionPanel, c);
        add(descriptionPanel);
        
        descriptionText = new JTextArea();
        descriptionText.setEditable(editable);
        descriptionText.setLineWrap(true);
        descriptionText.setRows(2);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(descriptionText, c);
        add(descriptionText);
        
        statusLabel = new JLabel(resourceBundle.getString("label.status"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(statusLabel, c);
        add(statusLabel);
        
        lidLabel = new JLabel(resourceBundle.getString("label.logicalIdentifier"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(lidLabel, c);
        add(lidLabel);
        
        lidText = new JTextField();
        lidText.setEditable(editable);
        lidText.getDocument().addDocumentListener(new LIDListener(RegistryObjectPanel.this));
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(lidText, c);
        add(lidText);
        
        idLabel = new JLabel(resourceBundle.getString("label.uniqueIdentifier"),
        SwingConstants.LEADING);
        c.gridx = 1;
        c.gridy = 4;
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
        idText.setEditable(false);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(idText, c);
        add(idText);

        //versionName and comment rows
        versionNameLabel = new JLabel(resourceBundle.getString("label.versionName"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(versionNameLabel, c);
        add(versionNameLabel);
        
        versionNameText = new JTextField();
        versionNameText.setEditable(false);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(versionNameText, c);
        add(versionNameText);
        
        commentLabel = new JLabel(resourceBundle.getString("label.versionComment"),
        SwingConstants.LEADING);
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(commentLabel, c);
        add(commentLabel);
        
        commentText = new JTextField();
        commentText.setEditable(editable);
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(commentText, c);
        add(commentText);        
        
        submitterButton = new JButton(resourceBundle.getString("button.submitter"));
        
        //classificationsButton.setVerticalTextPosition(AbstractButton.BOTTOM);
        //classificationsButton.setHorizontalTextPosition(AbstractButton.CENTER);
        //classificationsButton.setMnemonic(KeyEvent.VK_M);
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(submitterButton, c);
        
        //add(submitterButton);
        //Classifications
        classificationsLabel =
        new JLabel(resourceBundle.getString("label.classifications"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(classificationsLabel, c);
        add(classificationsLabel);
        
        classificationsList = new ClassificationsList();
        classificationsList.setEditable(editable);
        classificationsList.setVisibleRowCount(3);
        
        JScrollPane classificationsListScrollPane = new JScrollPane(classificationsList);
        
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(classificationsListScrollPane, c);
        add(classificationsListScrollPane);
        
        //External Ids
        extIdsLabel =
        new JLabel(resourceBundle.getString("label.externalIdentifiers"),
        SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(extIdsLabel, c);
        add(extIdsLabel);
        
        extIdsList = new ExternalIdentifiersList();
        extIdsList.setEditable(editable);
        extIdsList.setVisibleRowCount(3);
        
        JScrollPane extIdsListScrollPane = new JScrollPane(extIdsList);
        
        c.gridx = 1;
        c.gridy = 10;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(extIdsListScrollPane, c);
        add(extIdsListScrollPane);
        
        //External Links
        linksLabel = new JLabel(resourceBundle.getString("label.externalLinks"),
        SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(linksLabel, c);
        add(linksLabel);
        
        linksList = new ExternalLinksList();
        linksList.setEditable(editable);
        linksList.setVisibleRowCount(3);
        
        JScrollPane linksListScrollPane = new JScrollPane(linksList);
        
        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(linksListScrollPane, c);
        add(linksListScrollPane);
        
        //Slots
        slotsLabel = new JLabel(resourceBundle.getString("label.slots"),
        SwingConstants.TRAILING);
        c.gridx = 1;
        c.gridy = 11;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(slotsLabel, c);
        add(slotsLabel);
        
        slotsList = new SlotsList();
        slotsList.setEditable(editable);
        slotsList.setVisibleRowCount(3);
        
        JScrollPane slotsListScrollPane = new JScrollPane(slotsList);
        
        c.gridx = 1;
        c.gridy = 12;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(slotsListScrollPane, c);
        add(slotsListScrollPane);
        
        row = 13;
    }
    
    class LIDListener extends RegistryDocumentListener {
        public LIDListener(JPanel panel) {
            super(panel, resourceBundle.getString("message.error.CantSetLid"));
        }

        protected void setText(String text) throws JAXRException {
            idText.setText(text);
        }
    }    
    
    public void showNameInternationalString() {
        try {
            InternationalString iName = null;
            
            if (model != null) {
                iName = ((RegistryObject) model).getName();
            }
            
            JBEditorDialog.showObjectDetails(this, iName, true, editable);
            updateName((InternationalStringImpl) iName);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    public void showDescriptionInternationalString() {
        try {
            InternationalString iDescription = null;
            
            if (model != null) {
                iDescription = ((RegistryObject) model).getDescription();
            }
            
            JBEditorDialog.showObjectDetails(this, iDescription, true, editable);
            
            if (model != null) {
                updateDescription((InternationalStringImpl) iDescription);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    private void updateName(InternationalStringImpl name)
    throws JAXRException {
        if (name != null) {
            LocalizedString lName = name.getClosestLocalizedString(localeOnSetModel,
            null);
            
            if (lName == null) {
                nameText.setText("");
                nameLocaleOnSetModel = localeOnSetModel;
            } else {
                nameText.setText(lName.getValue());
                nameLocaleOnSetModel = lName.getLocale();
            }
            
            nameLocaleText.setText(resourceBundle.getString("bracket.open") +
            nameLocaleOnSetModel +
            resourceBundle.getString("bracket.close"));
            nameLocaleText.setToolTipText(nameLocaleOnSetModel.
            getDisplayName(getLocale()));
        }
    }
    
    private void updateDescription(InternationalStringImpl description)
    throws JAXRException {
        if (description != null) {
            LocalizedString lDescription = description.getClosestLocalizedString(localeOnSetModel,
            null);
            
            if (lDescription == null) {
                descriptionText.setText("");
                descriptionLocaleOnSetModel = localeOnSetModel;
            } else {
                descriptionText.setText(lDescription.getValue());
                descriptionLocaleOnSetModel = lDescription.getLocale();
            }
            
            descriptionLocaleText.setText(resourceBundle.getString("bracket.open") +
            descriptionLocaleOnSetModel +
            resourceBundle.getString("bracket.close"));
            descriptionLocaleText.setToolTipText(descriptionLocaleOnSetModel.
            getDisplayName(getLocale()));
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void setModel(Object obj) throws JAXRException {
        localeOnSetModel = RegistryBrowser.getInstance().getSelectedLocale();
        
        RegistryBrowser.isInstanceOf(obj, RegistryObject.class);
        
        super.setModel(obj);
        
        RegistryObjectImpl ro = (RegistryObjectImpl) obj;
        
        try {
            ClassificationsListModel classificationsListModel = (ClassificationsListModel) classificationsList.getModel();
            ExternalIdentifiersListModel extIdsListModel = (ExternalIdentifiersListModel) extIdsList.getModel();
            ExternalLinksListModel linksListModel = (ExternalLinksListModel) linksList.getModel();
            SlotsListModel slotsListModel = (SlotsListModel) slotsList.getModel();
            
            if (ro != null) {
                // TO DO: Review implementation specific restriction
                updateName((InternationalStringImpl) ro.getName());
                
                // TO DO: Review implementation specific restriction
                updateDescription((InternationalStringImpl) ro.getDescription());
                
                statusLabel.setText(resourceBundle.getString("label.status") + " " + ro.getStatusAsString());
                lidText.setText(ro.getLid());
                
                if (ro.getKey() != null) {
                    idText.setText(ro.getKey().getId());
                }
                
                VersionInfoType versionInfo = ro.getVersionInfo();
                if (versionInfo != null) {
                    String versionName = versionInfo.getVersionName();
                    versionNameText.setText(versionName);
                    
                    String comment = versionInfo.getComment();
                    commentText.setText(comment);
                }
                
                Collection c = ro.getClassifications();
                classificationsListModel.setModels(new ArrayList(c));
                
                Collection e = ro.getExternalIdentifiers();
                extIdsListModel.setModels(new ArrayList(e));
                
                Collection links = ro.getExternalLinks();
                linksListModel.setModels(new ArrayList(links));
                
                Collection slots = ro.getSlots();
                slotsListModel.setModels(new ArrayList(slots));
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    public Object getModel() throws JAXRException {
        super.getModel();
        
        if (model != null) {
		    it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl registryObject =
		    (it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl) model;

		    String nameStr = nameText.getText();

		    InternationalString name = registryObject.getName();

		    if (name != null) {
		        name.setValue(nameLocaleOnSetModel, nameStr);
		    } else {
		        registryObject.setName(registryObject.getLifeCycleManager()
		        .createInternationalString(nameLocaleOnSetModel,
		        nameStr));
		    }

		    InternationalString description = registryObject.getDescription();

		    if (description != null) {
		        description.setValue(descriptionLocaleOnSetModel,
		        descriptionText.getText());
		    } else {
		        registryObject.setDescription(registryObject.getLifeCycleManager()
		        .createInternationalString(descriptionLocaleOnSetModel,
		        descriptionText.getText()));
		    }

		    String id = idText.getText();
		    if (id != null && id.length() > 0 && !id.equals(registryObject.getKey().getId())) {
		        registryObject.setKey(registryObject.getLifeCycleManager().createKey(id));
		    } else {
		        // no op, do not touch ID
		    }
		    
		    String lid = lidText.getText();
		    if (lid != null && lid.length() > 0) {
		        registryObject.setLid(lid);
		    } else {
		        registryObject.setLid(null);
		    }

		    VersionInfoType versionInfo = registryObject.getVersionInfo();
		    if (versionInfo == null) {
		        versionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
		    }
		    String versionName = versionNameText.getText();
		    String comment = commentText.getText();
		    
		    if (versionName != null && versionName.length() > 0) {
		        versionInfo.setVersionName(versionName);
		    } else {
		        versionInfo.setVersionName(versionName);
		    }

		    if (comment != null && comment.length() > 0) {
		        versionInfo.setComment(comment);
		    } else {
		        versionInfo.setComment(comment);
		    }
		    registryObject.setVersionInfo(versionInfo);

		    ClassificationsListModel classificationsListModel = (ClassificationsListModel) classificationsList.getModel();
		    registryObject.setClassifications(Arrays.asList(
		    classificationsListModel.toArray()));

		    ExternalIdentifiersListModel extIdsListModel = (ExternalIdentifiersListModel) extIdsList.getModel();
		    registryObject.setExternalIdentifiers(Arrays.asList(
		    extIdsListModel.toArray()));

		    ExternalLinksListModel linksListModel = (ExternalLinksListModel) linksList.getModel();
		    registryObject.setExternalLinks(Arrays.asList(
		    linksListModel.toArray()));

		    SlotsListModel slotsListModel = (SlotsListModel) slotsList.getModel();
		    registryObject.setSlots(Arrays.asList(slotsListModel.toArray()));

		    RegistryBrowser.getInstance().getRootPane().updateUI();
		}
        
        return model;
    }
    
    protected void validateInput() throws JAXRException {
        super.validateInput();
        
        @SuppressWarnings("unused")
		RegistryObject registryObject = (RegistryObject) model;
        
        String nameStr = nameText.getText();
        
        if (nameStr.length() > 1024) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectNameLength"));
        }
        
        String descStr = descriptionText.getText();
        
        if (descStr.length() > 1024) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectDescriptionLength"));
        }
        
        String versionName = versionNameText.getText();        
        if (versionName.length() > 16) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectVersionLength"));
        }
        
        String comment = commentText.getText();        
        if (comment.length() > 256) {
            throw new JAXRException(resourceBundle.getString("error.registryObjectVersionCommentLength"));
        }
    }
    
    public void clear() throws JAXRException {
        super.clear();
        nameText.setText("");
        descriptionText.setText("");
        idText.setText("");
        lidText.setText("");
        versionNameText.setText("");
        commentText.setText("");
    }
    
    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        
        nameText.setEditable(editable);
        descriptionText.setEditable(editable);
        
        boolean isNew = ((RegistryObjectImpl) model).isNew();
        idText.setEditable(isNew && editable);
        lidText.setEditable(isNew && editable);
        versionNameText.setEditable(false); //Only modified by registry
        commentText.setEditable(editable);
        
        classificationsList.setEditable(editable);
        extIdsList.setEditable(editable);
        linksList.setEditable(editable);
        slotsList.setEditable(editable);
    }
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        super.processLocaleChange(newLocale);
        
        localeOnSetModel = newLocale;
        
        updateUIText();
        updateUI();
        revalidate();
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        nameLabel.setText(resourceBundle.getString("label.name"));
        
        try {
            InternationalString iName = null;
            
            if (model != null) {
                iName = ((RegistryObject) model).getName();
            }
            updateName((InternationalStringImpl) iName);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        showNameInternationalStringButton.
        setText(resourceBundle.getString("button.details"));
        
        descriptionLabel.setText(resourceBundle.getString("label.description"));
        try {
            InternationalString iDescription = null;
            
            if (model != null) {
                iDescription = ((RegistryObject) model).getDescription();
            }
            updateDescription((InternationalStringImpl) iDescription);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        showDescriptionInternationalStringButton.
        setText(resourceBundle.getString("button.details"));
        
        idLabel.setText(resourceBundle.getString("label.uniqueIdentifier"));
        versionNameLabel.setText(resourceBundle.getString("label.versionName"));
        commentLabel.setText(resourceBundle.getString("label.versionComment"));
        
        submitterButton.setText(resourceBundle.getString("button.submitter"));
        
        classificationsLabel.setText(resourceBundle.getString("label.classifications"));
        
        extIdsLabel.setText(resourceBundle.getString("label.externalIdentifiers"));
        
        linksLabel.setText(resourceBundle.getString("label.externalLinks"));
        
        slotsLabel.setText(resourceBundle.getString("label.slots"));
    }
}
