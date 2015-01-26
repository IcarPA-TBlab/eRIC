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

import javax.swing.SwingUtilities;

/**
 * Helper class used to hold a ConceptsTreeModel 'objectTypesTreeModel',
 * synchronizing its initialization and access to it.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
class ConceptsTreeModelSemaphore {
    /** The instance, used for synchronization. */
    private static ConceptsTreeModelSemaphore instance;
    /** The object we want to store. */
    private static ConceptsTreeModel objectTypesTreeModel;

    /** The Constructor, private. */
    private ConceptsTreeModelSemaphore() {
    }

    /** Getter for instance, initializes it on first call. */
    private synchronized static ConceptsTreeModelSemaphore getInstance() {
        if (instance == null) instance = new ConceptsTreeModelSemaphore();
        return instance;
    }

    /** Setter for 'objectTypesTreeModel'. Notifies all waiting threads. */
    public static void setObjectTypesTreeModel(ConceptsTreeModel
					       objectTypesTreeModel) {
        ConceptsTreeModelSemaphore.objectTypesTreeModel = objectTypesTreeModel;
        getInstance().doNotifyAll();
    }

    /** Getter for 'objectTypesTreeModel'. Put thread on wait() if not set yet. */
    public static ConceptsTreeModel getObjectTypesTreeModel() {
        // Do not hold Event Dispatch Thread!
        if (ConceptsTreeModelSemaphore.objectTypesTreeModel == null &&
            !SwingUtilities.isEventDispatchThread()) {
            getInstance().doWait();
        }
        return ConceptsTreeModelSemaphore.objectTypesTreeModel;
    }

    /** Synchronized method to put the calling thread on wait. */
    private synchronized void doWait() {
        try {
            wait();
        } catch (InterruptedException e) {
        	return;
        }
    }

    /** Synchronized method to notify all waiting threads. */
    private synchronized void doNotifyAll() {
        notifyAll();
    }
}
