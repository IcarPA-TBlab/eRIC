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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Locale;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;


/**
 * A specialization of JList for JAXR Browser. List elements are RIM objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @version
 */
public class JBList extends JList<Object> implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7314940089536742726L;

	protected JavaUIResourceBundle resourceBundle =
		JavaUIResourceBundle.getInstance();

    private JPopupMenu popup = null;
    private JMenuItem editMenuItem = null;
    private JMenuItem insertMenuItem = null;
    private JMenuItem removeMenuItem = null;
    private MouseListener popupListener;
    private String interfaceName = null;
    protected boolean editable = RegistryBrowser.getInstance().isAuthenticated();

    //Not usable
    @SuppressWarnings("unused")
	private JBList() {
    }

    public JBList(String interfaceName) {
        super();

        JBList_initialize(interfaceName);
    }

    /** Creates new JBList */
    public JBList(String interfaceName, JBListModel model) {
        super(model);

        JBList_initialize(interfaceName);
    }

    private void JBList_initialize(String interfaceName) {
        this.interfaceName = interfaceName;

        this.setToolTipText(resourceBundle.getString("tip.jblist"));
        createPopup();

        //add listener for 'locale' bound property
		RegistryBrowser.getInstance().
			addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
									  this);
    }

    /** Create popup menu for List */
    private void createPopup() {
        popup = new JPopupMenu();

		if (editable) {
			editMenuItem = new JMenuItem(resourceBundle.getString("menu.edit"));
		} else {
			editMenuItem = new JMenuItem(resourceBundle.getString("menu.showDetails"));
		}

        popup.add(editMenuItem);

		insertMenuItem = new JMenuItem(resourceBundle.getString("menu.insert"));
		removeMenuItem = new JMenuItem(resourceBundle.getString("menu.remove"));

        if (editable) {
            popup.add(insertMenuItem);
            popup.add(removeMenuItem);
        }

        editMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    editAction();
                }
            });

        insertMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    insertAction();
                }
            });

        removeMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    removeAction();
                }
            });

        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    protected void editAction() {
        int[] selectedIndices = getSelectedIndices();

        if (selectedIndices.length == 1) {
            Object selectedObject = getSelectedValue();

            if (selectedObject != null) {
                JBEditorDialog.showObjectDetails(this, selectedObject, true,
                    editable);
            }
        } else {
            //Only show error if there is at least one item in list
            //Reason is that a user may inadvertantly double click
            //on an empty list and then get confused by the message.
            if (getModel().getSize() > 0) {
                RegistryBrowser.
					displayError(resourceBundle.getString("error.editAction"));
            } else {
                RegistryBrowser.
					displayInfo(resourceBundle.getString("message.selectAction"));
            }
        }
    }

    protected void insertAction() {
        int index = getMaxSelectionIndex();

        JBListModel model = (JBListModel) getModel();

        if (index < 0) {
            index = model.getSize();
        }

        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            Object elementModel = null;

            try {
                Class<?> cls = Class.forName(interfaceName);

                //Now create an instance of the cls
                elementModel = cls.newInstance();
            } catch (ClassNotFoundException e) {
                elementModel = lcm.createObject(interfaceName);
            } catch (InstantiationException e) {
                throw new JAXRException(e);
            } catch (IllegalAccessException e) {
                throw new JAXRException(e);
            }

            JBEditorDialog dialog = JBEditorDialog.showObjectDetails(this,
                    elementModel, true, editable);

            if (dialog.getStatus() == JBEditorDialog.OK_STATUS) {
                model.add(index, elementModel);
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    protected void removeAction() {
        int[] selectedIndices = getSelectedIndices();

        if (selectedIndices.length >= 1) {
            Object selectedObject = getSelectedValue();

            if (selectedObject != null) {
                JBListModel model = (JBListModel) getModel();

                model.removeElementAt(selectedIndices[0]);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.removeAction"));
        }
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        createPopup();
    }

    /**
     * Tells whether this dialog is read-only or editable.
     */
    public boolean isEditable() {
        return editable;
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() > 1) {
                editAction();
            } else {
                maybeShowPopup(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int[] selectedIndices = getSelectedIndices();

                if (selectedIndices.length == 0) {
                    editMenuItem.setEnabled(false);
                    insertMenuItem.setEnabled(true);
                    removeMenuItem.setEnabled(false);
                } else if (selectedIndices.length == 1) {
                    editMenuItem.setEnabled(true);
                    insertMenuItem.setEnabled(true);
                    removeMenuItem.setEnabled(true);
                } else if (selectedIndices.length > 1) {
                    editMenuItem.setEnabled(false);
                    insertMenuItem.setEnabled(true);
                    removeMenuItem.setEnabled(true);
                }

                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
        }
    }

	/**
	 * Processes a change in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		resourceBundle = JavaUIResourceBundle.getInstance();

		setLocale(newLocale);
		setDefaultLocale(newLocale);

		updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
	protected void updateUIText() {
        setToolTipText(resourceBundle.getString("tip.jblist"));

		if (editable) {
			editMenuItem.setText(resourceBundle.getString("menu.edit"));
		} else {
			editMenuItem.setText(resourceBundle.getString("menu.showDetails"));
		}

		insertMenuItem.setText(resourceBundle.getString("menu.insert"));
		removeMenuItem.setText(resourceBundle.getString("menu.remove"));
	}
}
