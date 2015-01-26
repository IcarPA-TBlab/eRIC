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

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;

import javax.xml.registry.JAXRException;


/**
 * Base class for all xxListModel classes
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBListModel extends DefaultListModel<Object> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6769339890303527515L;

	/**
     * Constructor
     */
    public JBListModel() {
        super();
    }

    public ArrayList<Object> getModels() throws JAXRException {
        ArrayList<Object> models = new ArrayList<Object>();

        Object[] objs = toArray();

        for (int i = 0; i < objs.length; i++) {
            models.add(objs[i]);
        }

        return models;
    }

    public void setModels(ArrayList<?> models) throws JAXRException {
        clear();

        if (models != null) {
            Iterator<?> iter = models.iterator();

            while (iter.hasNext()) {
                addElement(iter.next());
            }
        }

        fireContentsChanged(this, 0, size());
    }
}
