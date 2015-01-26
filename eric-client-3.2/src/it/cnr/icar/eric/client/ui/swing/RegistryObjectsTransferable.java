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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.Serializable;

import java.util.ArrayList;


/**
 * An object that represents the clipboard contents for a ArrayList of RegistryObjects selection.
 *
 * The object has two representations:
 * <p>
 * 1. Richer: Object representtaion
 * 2. Plain: plain text representation.
 *
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *
 */
public class RegistryObjectsTransferable
    extends com.jgraph.plaf.basic.BasicTransferable implements Serializable,
        ClipboardOwner {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5776386366122351984L;
	/** Local Machine Reference Data Flavor. */
    public static DataFlavor dataFlavor;

    /* Local Machine Reference Data Flavor. */
    static {
        DataFlavor localDataFlavor;

        try {
            localDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                    "; class=it.cnr.icar.eric.client.ui.swing.RegistryObjectsTransferable");
        } catch (ClassNotFoundException cnfe) {
            localDataFlavor = null;
        }

        dataFlavor = localDataFlavor;
    }

    private ArrayList<?> registryObjects = null;

    /**
     * Constructs a new transferable selection for <code>cells</code>,
     * <code>cs</code>and <code>attrMap</code>.
     */
    public RegistryObjectsTransferable(ArrayList<?> registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * Returns the <code>registryObjects</code> that represent the selection.
     */
    public ArrayList<?> getRegistryObjects() {
        return registryObjects;
    }

    // from ClipboardOwner
    public void lostOwnership(Clipboard clip, Transferable contents) {
        // do nothing
    }

    // --- Richer ----------------------------------------------------------

    /**
     * Returns the jvm-localreference flavors of the transferable.
     */
    public DataFlavor[] getRicherFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    /**
     * Fetch the data in a jvm-localreference format.
     */
    public Object getRicherData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if (flavor.equals(dataFlavor)) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    // --- Plain ----------------------------------------------------------

    /**
     * Returns true if the transferable support a text/plain format.
     */
    public boolean isPlainSupported() {
        return true;
    }

    /**
     * Fetch the data in a text/plain format.
     */
    public String getPlainData() {
        return "have'nt implemented this yet";
    }
}
