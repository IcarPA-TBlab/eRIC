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

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * <p>DataRepeaterTag is the tag handler class for a <code>UIData</code>
 * component associated with a <code>RepeaterRenderer</code>.</p>
 */

public class DataRepeaterTag extends UIComponentTag {


    // -------------------------------------------------------------- Attributes


    private String first = null;


    public void setFirst(String first) {
        this.first = first;
    }


    private String rows = null;


    public void setRows(String rows) {
        this.rows = rows;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    private String value = null;


    public void setValue(String value) {
        this.value = value;
    }


    private String var = null;


    public void setVar(String var) {
        this.var = var;
    }


    // -------------------------------------------------- UIComponentTag Methods


    public String getComponentType() {
        return ("javax.faces.Data");
    }


    public String getRendererType() {
        return ("Repeater");
    }


    public void release() {
        super.release();
        first = null;
        rows = null;
        styleClass = null;
        value = null;
        var = null;
    }


    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (first != null) {
            if (isValueReference(first)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(first);
                component.setValueBinding("first", vb);
            } else {
                ((UIData) component).setFirst(Integer.parseInt(first));
            }
        }

        if (rows != null) {
            if (isValueReference(rows)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(rows);
                component.setValueBinding("rows", vb);
            } else {
                ((UIData) component).setRows(Integer.parseInt(rows));
            }
        }

        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(styleClass);
                component.setValueBinding("styleClass", vb);
            } else {
                component.getAttributes().put("styleClass", styleClass);
            }
        }

        if (value != null) {
            if (isValueReference(value)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(value);
                component.setValueBinding("value", vb);
            } else {
                ((UIData) component).setValue(value);
            }
        }

        if (var != null) {
            if (isValueReference(var)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(var);
                component.setValueBinding("var", vb);
            } else {
                ((UIData) component).setVar(var);
            }
        }

    }


}
