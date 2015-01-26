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


import it.cnr.icar.eric.client.ui.swing.graph.JBGraph;
import it.cnr.icar.eric.client.ui.swing.graph.JBGraphModel;
import it.cnr.icar.eric.client.ui.swing.graph.JBGraphPanel;
import it.cnr.icar.eric.common.CommonProperties;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.registry.JAXRException;

/**
 * The JTabbedPane for RegistryBrowser
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBTabbedPane extends JTabbedPane implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1534734880129691886L;
	static JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    DiscoveryPanel discoveryPanel;
    JPanel submissionPanel;
    boolean noUserRegRequired = false;
    
    /**
     * Class Constructor.
     *
     */
    public JBTabbedPane() throws JAXRException {
        noUserRegRequired = Boolean.valueOf(CommonProperties.getInstance().getProperty("eric.common.noUserRegistrationRequired", "false")).booleanValue();
        
        discoveryPanel = new DiscoveryPanel();
        addTab(resourceBundle.getString("tabbedPane.discovery"), discoveryPanel);
        
        submissionPanel = new JPanel();
        submissionPanel.setLayout(new BorderLayout());
        
        JBGraphPanel graphPanel = new JBGraphPanel();
        JBGraph graph = new JBGraph(new JBGraphModel());
        graphPanel.setModel(graph);
        graphPanel.setEnabled(true);
        submissionPanel.add(graphPanel, BorderLayout.CENTER);
        
        if (noUserRegRequired) {
            addTab(resourceBundle.getString("tabbedPane.submission"),
            submissionPanel);
        }
        
        
        setSelectedIndex(0);
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE, this);
        
        //add listener for 'authenticated' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_AUTHENTICATED,this);
    }

    public void reloadModel() {
        discoveryPanel.reloadModel();
    }
    
    /**
     * Action for the Find tool.
     */
    public void findAction() {
        discoveryPanel.find();
    }
    
    /**
     * Listens to property changes in the bound property RegistryBrowser.PROPERTY_AUTHENTICATED.
     * Hides certain menuItems when user is unAuthenticated.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();
            
            //Show submission pane only if authenticated
            if (authenticated || noUserRegRequired) {
                addTab(resourceBundle.getString("tabbedPane.submission"),
                submissionPanel);
            } else {
                remove(submissionPanel);
            }
            
            setSelectedIndex(0);
            
            //getRootPane().updateUI();
        } else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
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
        setTitleAt(indexOfComponent(discoveryPanel),
        resourceBundle.getString("tabbedPane.discovery"));
        // 'Submission' panel may not be added as a panel at the moment.
        if (indexOfComponent(submissionPanel) != -1) {
            setTitleAt(indexOfComponent(submissionPanel),
            resourceBundle.getString("tabbedPane.submission"));
        }
    }
}
