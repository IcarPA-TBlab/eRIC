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
package it.cnr.icar.eric.client.ui.swing.graph;



import it.cnr.icar.eric.client.ui.swing.JBEditorDialog;
import it.cnr.icar.eric.client.ui.swing.JBPanel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Window;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Panel to show RegistryObjects as a Graph
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraphPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5786645704869746206L;

	// The width of this component in pixel
    @SuppressWarnings("unused")
	private static final int panelWidth = 1600;

    // The height of this component in pixel
    @SuppressWarnings("unused")
	private static final int panelHeight = 2000;

    public JBGraphPanel() {
        setLayout(new BorderLayout());
    }

    @SuppressWarnings("unused")
	public static void browseObject(Window parent, RegistryObject ro,
        boolean editable) {
        JBGraph graph = new JBGraph(new JBGraphModel());
        graph.addRegistryObject(ro, new Rectangle(200, 200, 50, 50), true);

        JBEditorDialog dialog = JBEditorDialog.showObjectDetails(parent, graph,
                false, editable);
    }

    public static void browseObjects(Window parent, Collection<?> objs,
        boolean editable) {
        JBGraph graph = new JBGraph(new JBGraphModel());

        int x = 200;
        int y = 200;
        Iterator<?> iter = objs.iterator();

        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject) iter.next();
            x += 100;
            y += 100;
            graph.addRegistryObject(ro, new Rectangle(x, y, 50, 50), true);
        }

        JBGraph.circleLayout(graph);

        JBEditorDialog dialog = JBEditorDialog.showObjectDetails(parent, graph,
                false, editable);
        dialog.setTitle(resourceBundle.getString("menu.browse"));
    }

    public void setModel(Object obj) throws JAXRException {
        if (!(obj instanceof JBGraph)) {
            throw new InvalidRequestException("Expecting a JBGRaph. Got a " +
                obj.getClass().getName());
        }

        super.setModel(obj);

        JBGraph graph = (JBGraph) obj;
        JScrollPane graphSP = new JScrollPane(graph); //(new JGraph());

        add(graph.getToolBar(), BorderLayout.NORTH);

        add(graphSP, BorderLayout.CENTER);
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        ((JBGraph) model).setEditable(editable);
    }

    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return ((JBGraph) model).isEditable();
    }
}
