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
import it.cnr.icar.eric.client.xml.registry.util.QueryUtil;

import java.awt.ComponentOrientation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;


/**
 * Base class for all Query related panels.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class QueryPanel extends JPanel
implements PropertyChangeListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5104194425246550965L;

	protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    
    private TitledBorder border;
    private FindParamsPanel findParamsPanel;
    protected ConfigurationType uiConfigurationType;
    protected QueryUtil qu = QueryUtil.getInstance();
    
    /**
     * Class Constructor.
     */
    public QueryPanel(final FindParamsPanel findParamsPanel, ConfigurationType cfg) {
        this.uiConfigurationType = cfg;
        this.findParamsPanel = findParamsPanel;
        border = BorderFactory.createTitledBorder(resourceBundle.getString("title.searchCriteria"));
        this.setBorder(border);        
    }
    
    protected abstract void processConfiguration();
    
    /**
     * Clears or resets the UI.
     */
    public abstract void clear() throws JAXRException;
    
    /**
     * Invoke the find action. Delegates to parent.
     */
    void find() {
        findParamsPanel.find();
    }
    
    boolean isFederated() {
        return findParamsPanel.isFederated();
    }    
    
    /**
     * Execute the query using parameters defined by the fields in QueryPanel.
     */
    abstract BulkResponse executeQuery();
    
    public abstract void propertyChange(PropertyChangeEvent ev);
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        resourceBundle = JavaUIResourceBundle.getInstance();
        
        setLocale(newLocale);
        applyComponentOrientation(ComponentOrientation.getOrientation(newLocale));
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        border.setTitle(resourceBundle.getString("title.searchCriteria"));
    }
}
