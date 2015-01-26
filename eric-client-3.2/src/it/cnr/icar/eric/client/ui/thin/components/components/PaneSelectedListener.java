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

package it.cnr.icar.eric.client.ui.thin.components.components;

import java.util.HashMap;
import java.util.Map;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.thin.RegistryBrowser;
import it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean;
import it.cnr.icar.eric.client.ui.thin.RegistryObjectBean;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

/**
 * <p>Faces Listener implementation which sets the selected tab
 * component;</p>
 */
public class PaneSelectedListener implements FacesListener, StateHolder {
    
    private static Log log = LogFactory.getLog(PaneSelectedListener.class);

    private static final String INITIALIZE_TO_FIRST_PANEL = "-1";

    public PaneSelectedListener() {
    }

    // process the event..

    public void processPaneSelectedEvent(FacesEvent event) {
        UIComponent source = event.getComponent();
        UIComponent currentPane = null;// Find the parent tab control so we can set all tabs
        // to "unselected";
        UIComponent tabControl = findParentForRendererType(source,
                                                           "Tabbed");            
        boolean supportsROB = true;
        String supportsROBStr = (String)tabControl.getAttributes().get("supportsROB");
        if (supportsROBStr != null && supportsROBStr.equalsIgnoreCase("false")) {
            supportsROB = false;
        }
        RegistryObjectCollectionBean rocBean = null;
        RegistryObjectBean rob = null;
        if (supportsROB) {
            Map<?, ?> sessionMap = FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getSessionMap();
            rocBean = (RegistryObjectCollectionBean)sessionMap.get("roCollection");
            rob = rocBean.getCurrentRegistryObjectBean();
            if (event instanceof PaneSelectedPreRequestEvent) {
                handlePaneSelectedPreRequestEvent((PaneSelectedPreRequestEvent)event);
                return;
            }
        }
        PaneSelectedEvent pevent = (PaneSelectedEvent) event;
        String id = pevent.getId();

        boolean paneSelected = false;

        int n = tabControl.getChildCount();
        for (int i = 0; i < n; i++) {
            PaneComponent pane = (PaneComponent) tabControl.getChildren()
                .get(i);
            // If id == INITIALIZE_TO_FIRST_PANEL it means to render the 
            // first child tab.  This is needed when doing multi drilldowns
            // on different members of the result set.
            if (id.equals(INITIALIZE_TO_FIRST_PANEL)) {
                if (pane.isFirstTab().equalsIgnoreCase("true")) {
                    pane.setRendered(true);
                    paneSelected = true;
                    currentPane = pane;
                } else {
                    pane.setRendered(false);
                }
            } else {
                if (pane.getId().equals(id)) {
                    pane.setRendered(true);
                    paneSelected = true;
                    currentPane = pane;
                    if (supportsROB) {
                        rob.setCurrentDetailsPaneId(id);
                    }
                } else {
                    pane.setRendered(false);
                }
            }
        }

        if (!paneSelected) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.CannotSelectPaneForId", new Object[]{id}));
            ((PaneComponent) tabControl.getChildren().get(0)).setRendered(
                true);
        }

        // set the selected RegistryObject as current RegistryObject
        if (currentPane != null && supportsROB) {
            // String idValue = (String)currentPane.getAttributes().get("registryObjectId")

            // This resets the state after any related object drilldowns. 
            rocBean.resetCurrentComposedRegistryObjectBean();

            // get the Collection of the RegistryObjects for the current type
            String relationshipName = (String)currentPane.getAttributes().get("relationshipName");
            if (relationshipName != null) {
                rocBean.setCurrentRelatedObjectsData(relationshipName);
            }
        } 

        if (currentPane != null && !supportsROB) {
            HashMap<Object, Object> selectedTabs = RegistryBrowser.getInstance().getSelectedTabs();
            selectedTabs.put(tabControl.getId(), currentPane.getId());
        }  
    }

    private void handlePaneSelectedPreRequestEvent(PaneSelectedPreRequestEvent event) {
        String id = event.getId();

        @SuppressWarnings("unused")
		boolean paneSelected = false;
        UIComponent source = event.getComponent();
        Map<?, ?> sessionMap = FacesContext.getCurrentInstance()
                                             .getExternalContext()
                                             .getSessionMap();
        RegistryObjectCollectionBean rocBean = 
            (RegistryObjectCollectionBean)sessionMap.get("roCollection");
        // Find the parent tab control so we can set all tabs
        // to "unselected";
        UIComponent tabControl = findParentForRendererType(source,
                                                           "Tabbed");
        int n = tabControl.getChildCount();
        for (int i = 0; i < n; i++) {
            PaneComponent pane = (PaneComponent) tabControl.getChildren()
                .get(i);
            // If id == INITIALIZE_TO_FIRST_PANEL it means to render the 
            // first child tab.  This is needed when doing multi drilldowns
            // on different members of the result set.
            if (id.equals(INITIALIZE_TO_FIRST_PANEL)) {
                if (pane.isFirstTab().equalsIgnoreCase("true")) {
                    rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(false);
                }
            } else {
                if (pane.getId().equals(id)) {
                    if (pane.isFirstTab().equalsIgnoreCase("true")) {
                        rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(false);
                    } else {
                        rocBean.getCurrentRegistryObjectBean().setFormUpdateIgnored(true);
                    }
                }
            }
        }
    }
    
    private UIComponent findParentForRendererType(UIComponent component, String rendererType) {
        @SuppressWarnings("unused")
		Object facetParent = null;
        UIComponent currentComponent = component;
        
        // Search for an ancestor that is the specified renderer type;
        // search includes the facets.
        while (null != (currentComponent = currentComponent.getParent())) {
            if (currentComponent.getRendererType().equals(rendererType)) {
                break;
            }
        }
        return currentComponent;
    }    

    // methods from StateHolder
    Object testState = "test-state";

    public Object saveState(FacesContext context) {
        return testState;
    }

    public void restoreState(FacesContext context, Object state) {
        testState = state;
    }

    boolean transientValue;
    public void setTransient(boolean newTransientValue) {
        this.transientValue = newTransientValue;
    }

    public boolean isTransient() {
        return transientValue;
    }
}
