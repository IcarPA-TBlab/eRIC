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
package it.cnr.icar.eric.client.ui.swing.swing;



import it.cnr.icar.eric.client.common.RegistryMappedModel;
import it.cnr.icar.eric.client.ui.swing.I18nPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.Document;


/**
 * Implements several methods commonly used by panels that have an
 * underlying MappedModel.
 *
 * @author Fabian Ritzmann
 */
public abstract class RegistryMappedPanel extends I18nPanel implements Observer,
    MappedPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6882895737626597563L;
	private boolean mappingIsChanging = false;
    private final List<TextField> textFields = new ArrayList<TextField>();
    private String errorMessage;

    /**
     * Sets initial values. Registers the panel as an observer with the
     * underlying model.
     *
     * @param model The underlying MappedModel
     * @param updateError Prepended to exception text if model update
     * triggered an exception
     */
    protected RegistryMappedPanel(RegistryMappedModel model, String updateError) {
        this.errorMessage = updateError;
        model.addObserver(this);
    }

    /**
     * Returns a reference to this object.
     *
     * @return RegistryMappedPanel A reference to this object
     */
    public final RegistryMappedPanel getRegistryMappedPanel() {
        return this;
    }

    /**
     * Iterates through the text fields associated with this panel,
     * clears the field and sets the new text to be displayed. Displays
     * an error dialog if an exception occurs.
     *
     * For this to work it is necessary that all text fields implement
     * the TextField interface and are added with addTextField.
     *
     * @see java.util.Observer#update(Observable, Object)
     *
     * @param o ignored
     * @param arg ignored
     */
    public void update(Observable o, Object arg) {
        TextField field = null;
        Document doc = null;
        Iterator<TextField> i = this.textFields.iterator();

        while (i.hasNext()) {
            field = i.next();

            try {
                doc = field.getTextField().getDocument();
                doc.remove(0, doc.getLength());

                String text = field.getText();

                if (text != null) {
                    doc.insertString(0, text, null);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    this.errorMessage + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Sets a flag that the mapping is changing.
     *
     * @see it.cnr.icar.eric.client.ui.swing.swing.ChangeableType#setChangingType(boolean)
     */
    public void setMappingIsChanging(boolean changing) {
        this.mappingIsChanging = changing;
    }

    /**
     * Returns the value of a flag.
     *
     * @see it.cnr.icar.eric.client.ui.swing.swing.ChangeableType#isChangingType()
     */
    public boolean mappingIsChanging() {
        return this.mappingIsChanging;
    }

    /**
     * Adds the text field to an internal list. All text fields in the list
     * are updated when the underlying model reports a change.
     */
    public void addTextField(TextField field) {
        add(field.getTextField());
        this.textFields.add(field);
    }

    /**
     * Utility method that sets constraints on a layout.
     *
     * @param component Component that is affected
     * @param c Container for constraints
     * @param gbl Container for layout
     * @param gridx value to set on constraints
     * @param gridy value to set on constraints
     * @param gridwidth value to set on constrains
     * @param weightx value to set on constraints
     * @param fill value to set on constrains
     * @param anchor value to set on constraints
     */
    public static void setConstraints(JComponent component,
        GridBagConstraints c, GridBagLayout gbl, int gridx, int gridy,
        int gridwidth, double weightx, int fill, int anchor) {
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.gridheight = 1;
        c.weightx = weightx;
        c.weighty = 0.0;
        c.fill = fill;
        c.anchor = anchor;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(component, c);
    }

    /**
     * Setter for error message prefix, to be used with updateUIText.
     *
     * @param error String that is prepended to error message if
     * setText throws an exception
     */
    public void setError(String error) {
        this.errorMessage = error;
    }
}
