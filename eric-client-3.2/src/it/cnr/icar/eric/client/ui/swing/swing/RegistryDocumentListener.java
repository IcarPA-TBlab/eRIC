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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import javax.xml.registry.JAXRException;


/**
 * DocumentListener that factors out some commonly used code. This in an
 * implementation of the Strategy design pattern.
 *
 * The class implements the document listener methods and invokes an
 * abstract method setText. The setText method is intended to set the
 * entered text in the underlying model.
 *
 * @author Fabian Ritzmann
 */
public abstract class RegistryDocumentListener implements DocumentListener {
    protected final JPanel panel;
    protected String errorMessage;

    /**
     * Constructor.
     *
     * @param p Panel to which this listener belongs
     * @param error String that is prepended to error message if
     * setText throws an exception
     */
    public RegistryDocumentListener(JPanel p, String error) {
        this.panel = p;
        this.errorMessage = error;
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Invokes update.
     *
     * @see javax.swing.event.DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    /**
     * Gets the text from the document and calls setText. Displays a message
     * with an error if setText throws an exception.
     *
     * @param ev The event with changed text
     */
    protected void update(DocumentEvent ev) {
        Document doc = ev.getDocument();
        int docLength = doc.getLength();

        try {
            String text = doc.getText(0, docLength);
            setText(text);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.panel,
                this.errorMessage + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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

    /**
     * Should be implemented to set a new text string on the underlying
     * model.
     *
     * @param text The text in a document field
     *
     * @throws JAXRException Should be thrown if an exception in the
     * underlying model is triggered
     */
    protected abstract void setText(String text) throws JAXRException;
}
