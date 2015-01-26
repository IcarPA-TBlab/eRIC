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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import javax.xml.registry.JAXRException;


/**
 * Specialized JList for showing RegistryObjects.
 * Supports drag&drop of Classification objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsList extends JBList {
    /**
	 * 
	 */
	private static final long serialVersionUID = -233891512783229349L;

	public RegistryObjectsList(String interfaceName) {
        super(interfaceName);
    }

    /**
     *
     */
    public RegistryObjectsList(String interfaceName,
        RegistryObjectsListModel model) {
        super(interfaceName, model);

        //setDragEnabled(true);
        //setTransferHandler(new RegistryObjectsListTransferHandler());

        /*
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent)e.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY);
            }
        };
        addMouseListener(ml);
        */
    }

    /**
     * TransferHandler for RegistryObjectsList
     *
     */
    public class RegistryObjectsListTransferHandler extends TransferHandler {
        /**
		 * 
		 */
		private static final long serialVersionUID = 922642450313284566L;
		boolean isCut = false;

        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            return true;
        }

        protected final Transferable createTransferable(JComponent c) {
            if (c instanceof RegistryObjectsList) {
                RegistryObjectsList roList = (RegistryObjectsList) c;

                try {
                    ArrayList<?> registryObjects = ((RegistryObjectsListModel) roList.getModel()).getModels();

                    if ((registryObjects != null) &&
                            (registryObjects.size() > 0)) {
                        return create(registryObjects);
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            }

            return null;
        }

        @SuppressWarnings("rawtypes")
		protected RegistryObjectsTransferable create(ArrayList<?> registrObjects) {
            ArrayList<?> registryObjects = null;

            try {
                registryObjects = ((RegistryObjectsListModel) RegistryObjectsList.this.getModel()).getModels();
            } catch (JAXRException e) {
                registryObjects = new ArrayList();
                RegistryBrowser.displayError(e);
            }

            return new RegistryObjectsTransferable(registryObjects);
        }

        protected void exportDone(JComponent comp, Transferable data, int action) {
            if (comp instanceof RegistryObjectsList &&
                    data instanceof RegistryObjectsTransferable) {
                /*
                Object[]    cells = ((GraphTransferable) data).getCells();
                JGraph      graph = (JGraph) comp;
                Point       p = insertionLocation;

                if (p == null && action == TransferHandler.MOVE) {
                removeCells(graph, cells);
                }
                else if (p != null && handle != null) {
                int mod = (action == TransferHandler.COPY)
                      ? InputEvent.CTRL_MASK : 0;

                handle.mouseReleased(new MouseEvent(comp, 0, 0, mod, p.x, p.y,
                                                1, false));
                }
                insertionLocation = null;
                    */
            }
        }

        public void exportToClipboard(JComponent compo, Clipboard clip,
            int action) {
            isCut = (action == TransferHandler.MOVE);
            super.exportToClipboard(compo, clip, action);
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public boolean importData(JComponent comp, Transferable t) {
            try {
                if (comp instanceof RegistryObjectsList) {
                    @SuppressWarnings("unused")
					RegistryObjectsList list = (RegistryObjectsList) comp;

                    if (t.isDataFlavorSupported(
                                RegistryObjectsTransferable.dataFlavor)) {
                        Object obj = t.getTransferData(RegistryObjectsTransferable.dataFlavor);
                        RegistryObjectsTransferable roTransferrable = (RegistryObjectsTransferable) obj;

                        ((RegistryObjectsListModel) RegistryObjectsList.this.getModel()).setModels(roTransferrable.getRegistryObjects());

                        return true;
                    }
                }
            } catch (Exception exception) {
            } finally {
                isCut = false;
            }

            return false;
        }
    }
}
