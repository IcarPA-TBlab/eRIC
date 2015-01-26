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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a tab button control on the tab pane.
 */
public class PaneTabLabelTag extends UIComponentTag {

    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(PaneTabLabelTag.class);


    private String commandName = null;
    @SuppressWarnings("unused")
	private UIComponent currentComponent = null;


    public void setCommandName(String newCommandName) {
        commandName = newCommandName;
    }


    private String image = null;


    public void setImage(String newImage) {
        image = newImage;
    }


    private String label = null;


    public void setLabel(String newLabel) {
        label = newLabel;
        /*
        if (currentComponent != null) {
            UIComponent comp = null;
            try {
                comp = findComponent(FacesContext.getCurrentInstance());
                if (comp != null) {
                    if (isValueReference(label)) {
                        ValueBinding vb =
                            getFacesContext().getApplication().
                            createValueBinding(label);
                        comp.setValueBinding("label", vb);
                    } else {
                        comp.getAttributes().put("label", label);
                    }
                }
            } catch (Throwable ex) {
                
            }
        }
         */
    }

    private String onClick = null;


    public void setOnClick(String newOnClick) {
        onClick = newOnClick;
    }

    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("TabLabel");
    }


    public void release() {
        super.release();
        this.commandName = null;
        this.image = null;
        this.label = null;
        this.onClick = null;
    }


    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {

        currentComponent = component;
        
        super.setProperties(component);

        if (commandName != null) {
            if (isValueReference(commandName)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(commandName);
                component.setValueBinding("commandName", vb);
            } else {
                component.getAttributes().put("commandName", commandName);
            }
        }

        if (image != null) {
            if (isValueReference(image)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(image);
                component.setValueBinding("image", vb);
            } else {
                component.getAttributes().put("image", image);
            }
        }

        if (label != null) {
            if (isValueReference(label)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(label);
                component.setValueBinding("label", vb);
            } else {
                component.getAttributes().put("label", label);
            }
        }
        
        if (onClick != null) {
            if (isValueReference(onClick)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(onClick);
                component.setValueBinding("onClick", vb);
            } else {
                component.getAttributes().put("onClick", onClick);
            }
        }
    }


}
