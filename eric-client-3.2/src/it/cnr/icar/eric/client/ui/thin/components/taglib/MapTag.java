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


import it.cnr.icar.eric.client.ui.thin.components.components.MapComponent;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;


/**
 * <p>{@link UIComponentTag} for an image map.</p>
 */

public class MapTag extends UIComponentTag {


    @SuppressWarnings("unused")
	private String current = null;


    public void setCurrent(String current) {
        this.current = current;
    }


    private String actionListener = null;


    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }


    private String action = null;


    public void setAction(String action) {
        this.action = action;
    }


    private String immediate = null;


    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    public String getComponentType() {
        return ("DemoMap");
    }


    public String getRendererType() {
        return ("DemoMap");
    }


    public void release() {
        super.release();
        current = null;
        styleClass = null;
        actionListener = null;
        action = null;
        immediate = null;
        styleClass = null;
    }


    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {
        super.setProperties(component);
        MapComponent map = (MapComponent) component;
        //        if (current != null) {
        //            map.setCurrent(current);
        //        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(styleClass);
                map.setValueBinding("styleClass", vb);
            } else {
                map.getAttributes().put("styleClass", styleClass);
            }
        }
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class<?> args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                map.setActionListener(mb);
            } else {
                @SuppressWarnings("unused")
				Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }

        if (action != null) {
            if (isValueReference(action)) {
                MethodBinding vb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(action, null);
                map.setAction(vb);
            } else {
                map.setAction(Util.createConstantMethodBinding(action));
            }
        }
        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(immediate);
                map.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                map.setImmediate(_immediate);
            }
        }

    }


}
