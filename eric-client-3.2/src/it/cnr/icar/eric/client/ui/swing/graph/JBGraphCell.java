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

import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.GraphConstants;



import it.cnr.icar.eric.client.ui.swing.RegistryBrowser;
import it.cnr.icar.eric.common.CanonicalConstants;

import java.awt.Color;
import java.awt.Font;

import java.net.URL;

import javax.swing.ImageIcon;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;


/**
 * The model for a vertex in the JAXR Browser's Registry Object Graph.
 * Each cell represents a RegistryObject in the graph.
 *
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBGraphCell extends DefaultGraphCell {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7664955110105679376L;
	RegistryObject registryObject = null;

    @SuppressWarnings("unused")
	private JBGraphCell() {
    }

    public JBGraphCell(RegistryObject ro, boolean createIcon) {
        super(getLabel(ro));

        //super((ro.getClass().getName()).substring((ro.getClass().getName()).lastIndexOf('.')+1, (ro.getClass().getName()).length()-4));
        String objectType = CanonicalConstants.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject;

        try {
            objectType = ro.getObjectType().getValue();
        } catch (Exception e) {
            //e.printStackTrace();
        	objectType = null;
        }

        this.registryObject = ro;

        //Rectangle bounds = new Rectangle(50, 50);
        //GraphConstants.setBounds(attributes, bounds);
        GraphConstants.setOpaque(attributes, false);
        GraphConstants.setBorderColor(attributes, Color.black);
        GraphConstants.setAutoSize(attributes, true);

        //GraphConstants.setBorder(attributes, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setFontStyle(attributes, Font.BOLD);

        // Create Ports
        @SuppressWarnings("unused")
		int u = GraphConstants.PERCENT;

        // Floating Center Port (Child 0 is Default)
        DefaultPort port = new DefaultPort("Center");
        add(port);

        if (createIcon) {
            ImageIcon icon = getIcon(objectType);

            if (icon != null) {
                GraphConstants.setIcon(attributes, icon);
            }
        }
    }

    private ImageIcon getIcon(String objectType) {
        ImageIcon icon = null;
        String resourceName = "icons/rim/" + objectType + ".gif";
        URL url = this.getClass().getClassLoader().getResource(resourceName);

        if (url != null) {
            icon = new ImageIcon(url);
        } else {
            if (!objectType.equals("ExtrinsicObject")) {
                icon = getIcon("ExtrinsicObject");
            }
        }

        return icon;
    }

    private static String getLabel(RegistryObject ro) {
        String label = "";

        try {
            Connection connection = RegistryBrowser.getInstance().getClient()
                                                   .getConnection();
            RegistryService service = connection.getRegistryService();
            int registryLevel = service.getCapabilityProfile()
                                       .getCapabilityLevel();

            if (ro != null) {
                if (ro instanceof User) {
                    label = RegistryBrowser.getUserName((User) ro, registryLevel);
                } else {
                    label = RegistryBrowser.getName(ro);
                }

                if ((label == null) || (label.length() == 0)) {
                    if (ro instanceof Concept) {
                        label = ((Concept) ro).getValue();
                    } else if (ro instanceof Classification) {
                        label = ((Classification) ro).getValue();
                    }
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
        }

        return label;
    }

    RegistryObject getRegistryObject() {
        return registryObject;
    }
}
