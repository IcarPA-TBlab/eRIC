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


import it.cnr.icar.eric.client.ui.thin.components.model.ImageArea;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

import java.io.IOException;


/**
 * <p>{@link AreaComponent} is a JavaServer Faces component that represents
 * a particular hotspot in a client-side image map defined by our parent
 * {@link MapComponent}.  The <code>valueRef</code> property (if present)
 * must point at a JavaBean of type <code>components.model.ImageArea</code>;
 * if not present, an <code>ImageArea</code> instance will be synthesized
 * from the values of the <code>alt</code>, <code>coords</code>, and
 * <code>shape</code> properties, and assigned to the <code>value</code>
 * property.</p>
 */

public class AreaComponent extends UIOutput {


    // ------------------------------------------------------ Instance Variables


    private String alt = null;
    private String coords = null;
    private String shape = null;
    private String targetImage = null;



    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the alternate text for our synthesized {@link ImageArea}.</p>
     */
    public String getAlt() {
        return (this.alt);
    }


    /**
     * <p>Set the alternate text for our synthesized {@link ImageArea}.</p>
     *
     * @param alt The new alternate text
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }


    /**
     * <p>Return the hotspot coordinates for our synthesized {@link ImageArea}.
     * </p>
     */
    public String getCoords() {
        return (this.coords);
    }


    /**
     * <p>Set the hotspot coordinates for our synthesized {@link ImageArea}.</p>
     *
     * @param coords The new coordinates
     */
    public void setCoords(String coords) {
        this.coords = coords;
    }


    /**
     * <p>Return the shape for our synthesized {@link ImageArea}.</p>
     */
    public String getShape() {
        return (this.shape);
    }


    /**
     * <p>Set the shape for our synthesized {@link ImageArea}.</p>
     *
     * @param shape The new shape (default, rect, circle, poly)
     */
    public void setShape(String shape) {
        this.shape = shape;
    }


    /**
     * <p>Set the image that is the target of this <code>AreaComponent</code>.</p>
     *
     * @return the target image of this area component.
     */
    public String getTargetImage() {
        return targetImage;
    }


    /**
     * <p>Set the image that is the target of this <code>AreaComponent</code>.</p>
     *
     * @param targetImage the ID of the target of this <code>AreaComponent</code>
     */
    public void setTargetImage(String targetImage) {
        this.targetImage = targetImage;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Area");

    }

    // -------------------------------------------------------- UIOutput Methods


    /**
     * <p>Synthesize and return an {@link ImageArea} bean for this hotspot,
     * if there is no <code>valueRef</code> property on this component.</p>
     */
    public Object getValue() {

        if (super.getValue() == null) {
            setValue(new ImageArea(getAlt(), getCoords(), getShape()));
        }
        return (super.getValue());

    }


    // ----------------------------------------------------- StateHolder Methods


    /**
     * <p>Return the state to be saved for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */

    public Object saveState(FacesContext context) {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = alt;
        values[2] = coords;
        values[3] = shape;
        values[4] = targetImage;
        return (values);
    }


    /**
     * <p>Restore the state for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state   State to be restored
     *
     * @throws IOException if an input/output error occurs
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        alt = (String) values[1];
        coords = (String) values[2];
        shape = (String) values[3];
        targetImage = (String) values[4];
    }


}
