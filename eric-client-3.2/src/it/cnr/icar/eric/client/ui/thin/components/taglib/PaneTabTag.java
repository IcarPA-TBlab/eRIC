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

package it.cnr.icar.eric.client.ui.thin.components.taglib;

import it.cnr.icar.eric.client.ui.thin.components.components.PaneComponent;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents an individual tab on the overall control.
 */
public class PaneTabTag extends UIComponentTag {


    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(PaneTabTag.class);
    protected String registryObjectId = null;
    protected String relationshipName = null;
    protected String actionListener = null;
    protected String firstTab = "false";

    /**
     * method reference to handle an action event generated as a result of
     * clicking on a link that points a particular page in the result-set.
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }
    
    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tab");
    }

    public void setRegistryObjectId(String registryObjectId) {
        this.registryObjectId = registryObjectId;
    }
    
    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }
    
    public void setFirstTab(String firstTab) {
        this.firstTab = firstTab;
    }

    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {
        super.setProperties(component);        
        @SuppressWarnings("unused")
		FacesContext context = FacesContext.getCurrentInstance();
        PaneComponent pane = (PaneComponent)component;
        ValueBinding vb = null;
        
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class<?> args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                pane.setActionListener(mb);
            } else {
                @SuppressWarnings("unused")
				Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }

        if (registryObjectId != null) {
            if (isValueReference(registryObjectId)) {
                pane.setValueBinding("registryObjectId", Util.getValueBinding(registryObjectId));
                // save the value of the id to attributes collection
                vb = pane.getValueBinding("registryObjectId");
                String idValue = (String)vb.getValue(FacesContext.getCurrentInstance());
                pane.getAttributes().put("registryObjectId", idValue);
            } else {
                pane.getAttributes().put("registryObjectId",
                                              registryObjectId);
            }
        }

        if (relationshipName != null) {
            if (isValueReference(relationshipName)) {
                pane.setValueBinding("relationshipName", Util.getValueBinding(relationshipName));
                // save the value of the related object name  to attributes collection
                vb = pane.getValueBinding("relationshipName");
                String rName = (String)vb.getValue(FacesContext.getCurrentInstance());
                pane.getAttributes().put("relationshipName", rName);
            } else {
                pane.getAttributes().put("relationshipName",
                                              relationshipName);
            }
        }
        
        if (firstTab != null) {
            if (isValueReference(firstTab)) {
                pane.setValueBinding("firstTab", Util.getValueBinding(firstTab));
                // save the value of the related object name  to attributes collection
                vb = pane.getValueBinding("firstTab");
                String rName = (String)vb.getValue(FacesContext.getCurrentInstance());
                pane.getAttributes().put("firstTab", rName);
            } else {
                pane.getAttributes().put("firstTab",
                                              firstTab);
            }
        }
    }
}
