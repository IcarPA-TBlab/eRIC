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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
//import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The top level panel for discovery operations.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class DiscoveryPanel extends JPanel implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2699677991375138393L;
	private JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(DiscoveryPanel.class);

    private GridBagConstraints c = new GridBagConstraints();
    private RegistryObjectsTableModel registryObjectsTableModel;
    private RegistryObjectsTable registryObjectsTable;
    private JScrollPane registryObjectsTablePane;
    JPanel registryObjectsDetailPanel;
    JSplitPane splitPane2;
    FindParamsPanel findParamsPanel;
    JPanel registryObjectsTablePanel;
    TitledBorder registryObjectsTablePanelBorder;
    
    /**
     * Class Constructor.
     */
    public DiscoveryPanel() throws JAXRException {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        
        findParamsPanel = new FindParamsPanel(this);
        
        JPanel findResultsPanel = createFindResultsPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        findParamsPanel, findResultsPanel);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(splitPane, c);
        add(splitPane);
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().
        addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
        this);
    }
    
    private JPanel createFindResultsPanel() {
        //The registryObjectsTable
        registryObjectsTablePanel = new JPanel();
        registryObjectsTablePanelBorder =
        BorderFactory.createTitledBorder(resourceBundle.getString("title.searchResults"));
        registryObjectsTablePanel.setBorder(registryObjectsTablePanelBorder);
        
        registryObjectsTablePanel.setLayout(new BorderLayout());
        
        registryObjectsTableModel = new RegistryObjectsTableModel();
        
        TableSorter sorter = new TableSorter(registryObjectsTableModel);
        registryObjectsTable = new RegistryObjectsTable(sorter);
        sorter.addMouseListenerToHeaderInTable(registryObjectsTable);
        
        registryObjectsTablePane = new JScrollPane(registryObjectsTable);
        registryObjectsTablePanel.add(registryObjectsTablePane,
        BorderLayout.CENTER);
        
        return registryObjectsTablePanel;
    }
    
    void find() {
        if (RegistryBrowser.client.connection == null) {
            RegistryBrowser.displayUnconnectedError();
        } else {
            // hide scroll pane while querying, updating table model and data
            // so that table will not be repainted after each event
            registryObjectsTablePane.setVisible(false);
            // use SwingWorker to perform query
            final SwingWorker worker = new SwingWorker(this) {
                public Object doNonUILogic() {
                    //...code that might take a while to execute is here...
                    BulkResponse resp = findParamsPanel.executeQuery();
                    return resp;
                }
                public void doUIUpdateLogic() {
                    // updates to UI go here
                    registryObjectsTable.updateModel((BulkResponse)get());
                    registryObjectsTablePane.setVisible(true);
                }
            };
            worker.start();
        }
    }
    
    public void reloadModel() {
        findParamsPanel.reloadModel();
    }
    
    public void clear() throws JAXRException {
        registryObjectsTable.clearModel();
        findParamsPanel.clear();
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
        registryObjectsTablePanelBorder.
        setTitle(resourceBundle.getString("title.searchResults"));
    }
}
