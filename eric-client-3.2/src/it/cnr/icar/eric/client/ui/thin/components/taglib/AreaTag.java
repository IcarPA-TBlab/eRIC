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

import it.cnr.icar.eric.client.ui.thin.components.components.AreaComponent;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.webapp.UIComponentTag;


/**
 * <p>{@link UIComponentTag} for an image map hotspot.</p>
 */

public class AreaTag extends UIComponentTag {


    private String alt = null;


    public void setAlt(String alt) {
        this.alt = alt;
    }


    private String targetImage = null;


    public void setTargetImage(String targetImage) {
        this.targetImage = targetImage;
    }


    private String coords = null;


    public void setCoords(String coords) {
        this.coords = coords;
    }


    private String onmouseout = null;


    public void setOnmouseout(String newonmouseout) {
        onmouseout = newonmouseout;
    }


    private String onmouseover = null;


    public void setOnmouseover(String newonmouseover) {
        onmouseover = newonmouseover;
    }


    private String shape = null;


    public void setShape(String shape) {
        this.shape = shape;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    private String value = null;


    public void setValue(String newValue) {
        value = newValue;
    }


    public String getComponentType() {
        return ("DemoArea");
    }


    public String getRendererType() {
        return ("DemoArea");
    }


    public void release() {
        super.release();
        this.alt = null;
        this.coords = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.shape = null;
        this.styleClass = null;
        this.value = null;
    }


    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {
        super.setProperties(component);
        AreaComponent area = (AreaComponent) component;
        if (alt != null) {
            if (isValueReference(alt)) {
                area.setValueBinding("alt", Util.getValueBinding(alt));
            } else {
                area.getAttributes().put("alt", alt);
            }
        }
        if (coords != null) {
            if (isValueReference(coords)) {
                area.setValueBinding("coords", Util.getValueBinding(coords));
            } else {
                area.getAttributes().put("coords", coords);
            }
        }
        if (onmouseout != null) {
            if (isValueReference(onmouseout)) {
                area.setValueBinding("onmouseout",
                                     Util.getValueBinding(onmouseout));
            } else {
                area.getAttributes().put("onmouseout", onmouseout);
            }
        }
        if (onmouseover != null) {
            if (isValueReference(onmouseover)) {
                area.setValueBinding("onmouseover",
                                     Util.getValueBinding(onmouseover));
            } else {
                area.getAttributes().put("onmouseover", onmouseover);
            }
        }
        if (shape != null) {
            if (isValueReference(shape)) {
                area.setValueBinding("shape", Util.getValueBinding(shape));
            } else {
                area.getAttributes().put("shape", shape);
            }
        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                area.setValueBinding("styleClass",
                                     Util.getValueBinding(styleClass));
            } else {
                area.getAttributes().put("styleClass", styleClass);
            }
        }

        ValueHolder valueHolder = (ValueHolder) component;
        if (value != null) {
            if (isValueReference(value)) {
                area.setValueBinding("value", Util.getValueBinding(value));
            } else {
                valueHolder.setValue(value);
            }
        }
 
        // target image is required
        area.setTargetImage(targetImage);
    }
}
