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

import it.cnr.icar.eric.client.ui.thin.components.components.ScrollerComponent;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;

/**
 * ScrollerTag is the tag handler class for <code>ScrollerComponent.</code>
 */
public class ScrollerTag extends UIComponentTag {

    protected String action = null;
    protected String actionListener = null;
    protected String navFacetOrientation = null;
    protected String forValue = null;


    /**
     * method reference to handle an action event generated as a result of
     * clicking on a link that points a particular page in the result-set.
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }
    
    /**
     * method reference to handle an action event generated as a result of
     * clicking on a link that points a particular page in the result-set.
     */
    public void setAction(String action) {
        this.action = action;
    }


    /*
     * When rendering a widget representing "page navigation" where
     * should the facet markup be rendered in relation to the page
     * navigation widget?  Values are "NORTH", "SOUTH", "EAST", "WEST".
     * Case insensitive. This can be value or a value binding 
     * reference expression.
     */
    public void setNavFacetOrientation(String navFacetOrientation) {
        this.navFacetOrientation = navFacetOrientation;
    }


    /*
     * The data grid component for which this acts as a scroller.
     * This can be value or a value binding reference expression.
     */
    public void setFor(String newForValue) {
        forValue = newForValue;
    }


    public String getComponentType() {
        return ("Scroller");
    }


    public String getRendererType() {
        return (null);
    }


    public void release() {
        super.release();
        this.navFacetOrientation = null;
    }


    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {
        super.setProperties(component);
        FacesContext context = FacesContext.getCurrentInstance();
        ValueBinding vb = null;

        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class<?> args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                ((ScrollerComponent) component).setActionListener(mb);
            } else {
                @SuppressWarnings("unused")
				Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }
        
       if (action != null) {
            if (isValueReference(action)) {
                MethodBinding vb2 = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(action, null);
                ((ScrollerComponent) component).setAction(vb2);
            } else {
                ((ScrollerComponent) component).setAction(Util.createConstantMethodBinding(action));
            }
        }

        // if the attributes are values set them directly on the component, if
        // not set the ValueBinding reference so that the expressions can be
        // evaluated lazily.
        if (navFacetOrientation != null) {
            if (isValueReference(navFacetOrientation)) {
                vb =
                    context.getApplication().createValueBinding(
                        navFacetOrientation);
                component.setValueBinding("navFacetOrientation", vb);
            } else {
                component.getAttributes().put("navFacetOrientation",
                                              navFacetOrientation);
            }
        }

        if (forValue != null) {
            if (isValueReference(forValue)) {
                vb = context.getApplication().createValueBinding(forValue);
                component.setValueBinding("for", vb);
            } else {
                component.getAttributes().put("for", forValue);
            }
        }
    }
}
