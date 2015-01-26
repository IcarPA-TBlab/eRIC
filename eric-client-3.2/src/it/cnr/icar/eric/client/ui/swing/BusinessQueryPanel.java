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


import it.cnr.icar.eric.client.ui.common.conf.bindings.ConfigurationType;
import it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.QueryImpl;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.BindingUtility;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The panel that displays UI for specifying parameters to a BusinessQuery.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class BusinessQueryPanel extends QueryPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2184595124785089653L;
	private GridBagConstraints c = new GridBagConstraints();
    private TreeCombo objectTypeCombo;
    private JTextField nameText;
    private JTextField descText;
    private JCheckBox caseSensitiveCheckBox;
    private ClassificationsList classificationsList;
    private ExternalIdentifiersList extIdsList;
    private ExternalLinksList linksList;
    private JLabel objectTypeLabel;
    /** The name Text */
    private JLabel nameLabel;
    /** The description text */
    private JLabel descLabel;
    /** Classifications text */
    private JLabel classificationsLabel;
    /** Identifiers text */
    private JLabel identifiersLabel;
    /** 'External Links' text */
    private JLabel linksLabel;
    
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(BusinessQueryPanel.class);
    
    /**
     * Class Constructor.
     */
    public BusinessQueryPanel(final FindParamsPanel findParamsPanel, ConfigurationType uiConfigurationType) throws JAXRException {
        super(findParamsPanel, uiConfigurationType);
        
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        objectTypeLabel = new JLabel(resourceBundle.getString("title.objectType"),
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
        gbl.setConstraints(objectTypeLabel, c);
        add(objectTypeLabel);

        //TODO: SwingBoost: Localize this
        TreeNode tempTreeNode = new DefaultMutableTreeNode("loading object types...");
        objectTypeCombo = new it.cnr.icar.eric.client.ui.swing.TreeCombo(
            new DefaultTreeModel(tempTreeNode));
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(objectTypeCombo, c);
        add(objectTypeCombo);
        objectTypeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                
                @SuppressWarnings("unused")
				String objectType = getObjectType().toString();
            }
        });
        
        //The caseSensitive CheckBox
        caseSensitiveCheckBox =
        new JCheckBox(resourceBundle.getString("title.caseSensitiveSearch"));
        caseSensitiveCheckBox.setSelected(false);
        caseSensitiveCheckBox.setEnabled(true);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(caseSensitiveCheckBox, c);
        add(caseSensitiveCheckBox);
        
        //The name Text
        nameLabel = new JLabel(resourceBundle.getString("title.name"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(nameLabel, c);
        add(nameLabel);
        
        nameText = new JTextField();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(nameText, c);
        add(nameText);
        
        nameText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findParamsPanel.find();
            }
        });
        
        //The description text
        descLabel = new JLabel(resourceBundle.getString("title.description"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(descLabel, c);
        add(descLabel);
        
        descText = new JTextField();
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(descText, c);
        add(descText);
        
        descText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findParamsPanel.find();
            }
        });
        
        //Classifications
        classificationsLabel =
        new JLabel(resourceBundle.getString("title.classifications"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 7;
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
        classificationsList.setEditable(true);
        classificationsList.setVisibleRowCount(3);
        
        JScrollPane classificationsListScrollPane = new JScrollPane(classificationsList);
        
        //Workaround for bug 740746 where very wide item resulted in too short a height
        classificationsListScrollPane.setMinimumSize(new Dimension(
        -1, 50));
        
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(classificationsListScrollPane, c);
        add(classificationsListScrollPane);
        
        //Identifiers
        identifiersLabel =
        new JLabel(resourceBundle.getString("title.externalIdentifiers"),
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
        gbl.setConstraints(identifiersLabel, c);
        add(identifiersLabel);
        
        extIdsList = new ExternalIdentifiersList();
        extIdsList.setEditable(true);
        extIdsList.setVisibleRowCount(3);
        
        JScrollPane extIdsListScrollPane = new JScrollPane(extIdsList);
        
        //Workaround for bug 740746 where very wide item resulted in too short a height
        extIdsListScrollPane.setMinimumSize(new Dimension(-1, 50));
        
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(extIdsListScrollPane, c);
        add(extIdsListScrollPane);
        
        //External Links
        linksLabel =
        new JLabel(resourceBundle.getString("title.externalLinks"),
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
        linksList.setEditable(true);
        linksList.setVisibleRowCount(3);
        
        JScrollPane linksListScrollPane = new JScrollPane(linksList);
        
        //Workaround for bug 740746 where very wide item resulted in too short a height
        linksListScrollPane.setMinimumSize(new Dimension(-1, 50));
        
        c.gridx = 0;
        c.gridy = 12;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(linksListScrollPane, c);
        add(linksListScrollPane);
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE, this);
    }

    public void reloadModel() {
        final SwingWorker worker = new SwingWorker(this) {
            public Object doNonUILogic() {
                ConceptsTreeModel objectTypesTreeModel = null;
                try {
                    objectTypesTreeModel = new ConceptsTreeModel(false);
                    //...code that might take a while to execute is here...
                    ClassificationScheme objectTypeScheme = null;
                    JAXRClient client = RegistryBrowser.getInstance().getClient();
                    BusinessQueryManager bqm = client.getBusinessQueryManager();
                    objectTypeScheme = (ClassificationScheme) bqm.getRegistryObject(
                            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType,
                            LifeCycleManager.CLASSIFICATION_SCHEME);
                    objectTypesTreeModel.setRootTaxonomyElem(objectTypeScheme);
                    objectTypesTreeModel.update();
                } catch (JAXRException j) {
                    //TODO: SwingBoost: give error
                	objectTypesTreeModel = null;
                }
                return objectTypesTreeModel;
            }
            public void doUIUpdateLogic() {
                ConceptsTreeModel objectTypesTreeModel = (ConceptsTreeModel)get();
                if (objectTypesTreeModel != null) {
                    objectTypeCombo.setModel(objectTypesTreeModel);
                    String selectedIndexStr = ProviderProperties.getInstance().getProperty(
                        "jaxr-ebxml.registryBrowser.objectTypeCombo.initialSelectionIndex", "1");
                    int index = Integer.parseInt(selectedIndexStr);
		    if (index < objectTypeCombo.getItemCount()) {
			objectTypeCombo.setSelectedIndex(index);
		    } else {
			objectTypeCombo.setSelectedIndex(-1);
		    }
                }
                setObjectTypesTreeModel(objectTypesTreeModel);                
            }
        };
        worker.start();
    }
    protected void processConfiguration() {
    }
    
    //Allows sharing with JBGraph. See ConceptsTreeModelSemaphore.
    private static void setObjectTypesTreeModel(ConceptsTreeModel objectTypesTreeModel) {
        ConceptsTreeModelSemaphore.setObjectTypesTreeModel(objectTypesTreeModel);
    }
    
    //Allows sharing with JBGraph. See ConceptsTreeModelSemaphore.
    public static ConceptsTreeModel getObjectTypesTreeModel() {
	return ConceptsTreeModelSemaphore.getObjectTypesTreeModel();
    }
    
    private Object getObjectType() {
        @SuppressWarnings("unused")
		String objectType = null;
        Object selectedItem = objectTypeCombo.getSelectedItemsObject();
        
        if (selectedItem instanceof DefaultMutableTreeNode) {
            Object nodeInfo = ((DefaultMutableTreeNode) selectedItem).getUserObject();

            if (nodeInfo instanceof it.cnr.icar.eric.client.ui.swing.NodeInfo) {
                Object obj = ((it.cnr.icar.eric.client.ui.swing.NodeInfo) nodeInfo).obj;

                if (obj instanceof RegistryObject) {
                    selectedItem = obj;
                }
            }
        }
        
        return selectedItem;
    }
    
    public static String[] getObjectTypes() {
        String[] array = null;
        ArrayList<String> objectTypes = new ArrayList<String>();
        
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (client.getBusinessQueryManager());
            Collection<?> concepts = bqm.findConceptsByPath(
            "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject%");
            
            int size = concepts.size();
            
            Iterator<?> iter = concepts.iterator();
            @SuppressWarnings("unused")
			int i = 0;
            
            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                String objectType = concept.getValue();
                
                if (objectType.equals("ClassificationNode")) {
                    objectType = "Concept";
                }
                
                objectTypes.add(objectType);
            }
            
            //Collections.sort(objectTypes);
            
            array = new String[size];
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return objectTypes.toArray(array);
    }
    
    /**
     * Execute the query using parameters defined by the fields in QueryPanel.
     */
    BulkResponse executeQuery() {
        BulkResponse resp = null;
        
        try {
            // create namePattern collection
            String nameStr = nameText.getText();
            String descStr = descText.getText();
            
            ArrayList<Object> classifications = ((ClassificationsListModel) (classificationsList.getModel())).getModels();
            ArrayList<?> extIds = ((ExternalIdentifiersListModel) (extIdsList.getModel())).getModels();
            ArrayList<?> extLinks = ((ExternalLinksListModel) (linksList.getModel())).getModels();
            
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            Connection connection = RegistryBrowser.client.getConnection();
            RegistryService service = connection.getRegistryService();
            @SuppressWarnings("unused")
			BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) service.getBusinessQueryManager();
            DeclarativeQueryManagerImpl dqm =
            (DeclarativeQueryManagerImpl) service.getDeclarativeQueryManager();
            
            Object objectTypeObj = getObjectType();
            if (!(objectTypeObj instanceof Concept)) {
                throw new JAXRException("Search not supported for objectType: " +
                objectTypeObj.toString());
            }
            
            Concept objectType = (Concept)objectTypeObj;
            if (!(objectType.getPath().startsWith("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType))) {
                throw new JAXRException("Search not supported for objectType: " +
                objectType.getPath());                
            }
            
            boolean isIntrinsic = isIntrinsicObjectType(objectType);
            boolean caseSensitive = caseSensitiveCheckBox.isSelected();
            
            // make declarative query
            String queryStr = "SELECT obj.* from ";
            
            if (isIntrinsic) {
                queryStr += (objectType.getValue() + " obj ");
            } else {
                //Following using RegistryObject as it could be an ExtrinsicObject
                //or an ExternalLink
                queryStr += "RegistryObject obj, ClassificationNode typeNode ";
            }
            
            //Add name to tables in join
            if ((nameStr != null) && (nameStr.length() != 0)) {
                queryStr += ", Name_ nm ";
            }
            
            //Add description to tables in join
            if ((descStr != null) && (descStr.length() != 0)) {
                queryStr += ", Description des ";
            }
            
            boolean addedPredicate = false;
            
            //Add objectType predicate
            if (!isIntrinsic) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += ("((obj.objectType = typeNode.id) AND " +
			     "(typeNode.path LIKE '" +
			     objectType.getPath() +
			     "' OR typeNode.path LIKE '" +
			     objectType.getPath() +
			     "/%'))");
            }
            
            //Add name predicate if needed
            if ((nameStr != null) && (nameStr.length() > 0)) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += ("((nm.parent = obj.id) AND (" +
                BusinessQueryManagerImpl.caseSensitise("nm.value",
                caseSensitive) + " LIKE " +
                BusinessQueryManagerImpl.caseSensitise("'" + nameStr + "'",
                caseSensitive) + ")) ");
            }
            
            //Add description predicate if needed
            if ((descStr != null) && (descStr.length() > 0)) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += ("((des.parent = obj.id) AND (" +
                BusinessQueryManagerImpl.caseSensitise("des.value",
                caseSensitive) + " LIKE " +
                BusinessQueryManagerImpl.caseSensitise("'" + descStr + "'",
                caseSensitive) + ")) ");
            }
            
            //Add nested query for Classifications if needed
            if (classifications.size() > 0) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += qu.getClassificationsPredicate(classifications, "obj.id", null);
            }
            
            //Add predicate for ExternalIdentifiers if needed
            if (extIds.size() > 0) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += qu.getExternalIdentifiersPredicate(extIds, "obj.id", null);
            }
            
            //Add nested query for ExternalLinks if needed
            if (extLinks.size() > 0) {
                if (!addedPredicate) {
                    queryStr += "WHERE ";
                    addedPredicate = true;
                } else {
                    queryStr += "AND ";
                }
                
                queryStr += qu.getExternalLinksPredicate(extLinks, "obj.id", null);
            }
            
            QueryImpl query = (QueryImpl)dqm.createQuery(Query.QUERY_TYPE_SQL,
                queryStr);
            query.setFederated(isFederated());
            
            // make JAXR request
            resp = dqm.executeQuery(query);
            
            client.checkBulkResponse(resp);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return resp;
    }
    
    public static boolean isIntrinsicObjectType(Concept objectType)
    throws JAXRException {
        boolean isIntrinsic = true;
        
        String path = objectType.getPath();
        if (path.startsWith("/" + 
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + 
            "/RegistryObject/ExtrinsicObject")) {
            isIntrinsic = false;
        }
        
        return isIntrinsic;
    }
    
    /**
     * Process a ConceptsTreeDialog.PROPERTY_SELECTED_CONCEPTS change event.
     */
    private void processSelectedConceptsChange(PropertyChangeEvent ev) {
        try {
            Connection connection =
            RegistryBrowser.client.getConnection();
            RegistryService service =
            connection.getRegistryService();
            LifeCycleManager lcm =
            service.getBusinessLifeCycleManager();
            
            ArrayList<?> selectedConcepts = (ArrayList<?>) ev.getNewValue();
            ArrayList<Classification> classifications = new ArrayList<Classification>();
            
            Iterator<?> iter = selectedConcepts.iterator();
            
            while (iter.hasNext()) {
                Concept concept = (Concept) iter.next();
                Classification classification = lcm.createClassification(concept);
                classifications.add(classification);
            }
            
            ((ClassificationsListModel) (classificationsList.getModel())).setModels(classifications);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        //System.err.println(ev);
        if (ev.getPropertyName().equals(ConceptsTreeDialog.PROPERTY_SELECTED_CONCEPTS)) {
            processSelectedConceptsChange(ev);
        } else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
            processLocaleChange((Locale) ev.getNewValue());
        }
    }
    
    /**
     * Clears or resets the UI.
     */
    public void clear() throws JAXRException {
        nameText.setText("");
        descText.setText("");
        
        if (classificationsList != null) {
            ((ClassificationsListModel) (classificationsList.getModel())).clear();
        }
        
        if (extIdsList != null) {
            ((ExternalIdentifiersListModel) (extIdsList.getModel())).clear();
        }
        
        if (linksList != null) {
            ((ExternalLinksListModel) (linksList.getModel())).clear();
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
        
        objectTypeLabel.setText(resourceBundle.getString("title.objectType"));
        caseSensitiveCheckBox.
        setText(resourceBundle.getString("title.caseSensitiveSearch"));
        nameLabel.setText(resourceBundle.getString("title.name"));
        descLabel.setText(resourceBundle.getString("title.description"));
        classificationsLabel.
        setText(resourceBundle.getString("title.classifications"));
        identifiersLabel.
        setText(resourceBundle.getString("title.externalIdentifiers"));
        linksLabel.
        setText(resourceBundle.getString("title.externalLinks"));
    }
}
