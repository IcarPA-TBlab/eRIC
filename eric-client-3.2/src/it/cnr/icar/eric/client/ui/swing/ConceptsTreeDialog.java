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
package it.cnr.icar.eric.client.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 * Base class for all JAXR Browser dialogs.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ConceptsTreeDialog extends JBDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4175217193598240733L;
	public static final String PROPERTY_SELECTED_CONCEPTS = "selectedConcepts";
    private ConceptsTree conceptsTree = null;

    public ConceptsTreeDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        ConceptsTreeDialog_initialize();
    }

    public ConceptsTreeDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        ConceptsTreeDialog_initialize();
    }

    void ConceptsTreeDialog_initialize() {
        setTitle("Classification Schemes");

        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout gbl1 = new GridBagLayout();

        //Top level panel
        JPanel panel = mainPanel;
        panel.setLayout(gbl1);

        //The conceptsTree
        conceptsTree = new ConceptsTree(true);

        JScrollPane conceptsTreePane = new JScrollPane(conceptsTree);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(4, 4, 0, 4);
        gbl1.setConstraints(conceptsTreePane, c);
        panel.add(conceptsTreePane);

        pack();
    }

    ConceptsTree getModel() {
        return conceptsTree;
    }

    protected void okAction() {
        super.okAction();

        if (conceptsTree != null) {
            String test = new String();
            firePropertyChange(PROPERTY_SELECTED_CONCEPTS, test,
                getSelectedObjects());

            //System.err.println("firePropertyChange");
        } else {
            //System.err.println("Missed firePropertyChange " + conceptsTree);
        }

        dispose();
    }

    public ArrayList<?> getSelectedObjects() {
        ArrayList<?> selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedObjects();
        }

        return selectedObjects;
    }

    public ArrayList<?> getSelectedConcepts() {
        ArrayList<?> selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedConcepts();
        }

        return selectedObjects;
    }

    public ArrayList<?> getSelectedClassificationSchemes() {
        ArrayList<?> selectedObjects = null;

        if (conceptsTree != null) {
            selectedObjects = conceptsTree.getSelectedClassificationSchemes();
        }

        return selectedObjects;
    }

    public static ConceptsTreeDialog showSchemes(Component parent,
        boolean modal, boolean editable) {
        ConceptsTreeDialog dialog = null;
        RegistryBrowser.setWaitCursor();

        Window window = (Window) (SwingUtilities.getRoot(parent));

        if (window instanceof JFrame) {
            dialog = new ConceptsTreeDialog((JFrame) window, modal);
        } else if (window instanceof JDialog) {
            dialog = new ConceptsTreeDialog((JDialog) window, modal);
        }

        dialog.setEditable(editable);
        dialog.setLocation((int) (window.getLocation().getX() + 30),
            (int) (window.getLocation().getY() + 30));
        dialog.setVisible(true);

        RegistryBrowser.setDefaultCursor();

        return dialog;
    }

    public static void clearCache() {
        ConceptsTree.clearCache();
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            super.setEditable(editable);
            conceptsTree.setEditable(editable);
        }
    }
}
