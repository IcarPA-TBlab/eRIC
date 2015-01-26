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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;

import java.beans.PropertyChangeListener;

//import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javax.xml.registry.JAXRException;


/**
 * A JBDialog that has an object at its model and serves as a UI editor for that model object.
 */
public class JBEditorDialog extends JBDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = -12985402551231396L;
	JBPanel panel = null;

    public JBEditorDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        JBEditorDialog_initialize();
    }

    public JBEditorDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        JBEditorDialog_initialize();
    }

    private void JBEditorDialog_initialize() {
    }

    public Object getModel() throws JAXRException {
        Object model = null;

        if (panel != null) {
            model = panel.getModel();
        }

        return model;
    }

    public void setModel(Object model) throws JAXRException {
        status = JBDialog.CANCEL_STATUS;

        Class<?> oldClass = null;
        Class<?> newClass = null;

        Object oldModel = getModel();

        if ((model != null)) {
            if (oldModel != null) {
                oldClass = oldModel.getClass();
            }

            newClass = model.getClass();

            if (newClass != oldClass) {
                try {
                    setTitle(getDialogTitleFromModelClass(newClass));
                    
                    Class<?> panelClass = null;
                    
                    while (true) {
                        try {
                            //First see if panelClass defined for this modelClass
                            panelClass = getPanelClassFromModelClass(newClass);
                            break;
                        } catch (ClassNotFoundException e) {
                            newClass = newClass.getSuperclass();
                            if (newClass.getName().startsWith("java.")) {
                                throw e;
                            }
                        }
                    }

                    //Now create an instance of the panelClass
                    panel = (JBPanel) panelClass.newInstance();
                    panel.setModel(model);
                    panel.setEditable(editable);

                    //Add panel to dialog
                    JScrollPane scrPane = new JScrollPane(panel);
                    mainPanel.removeAll();
                    mainPanel.add(scrPane, BorderLayout.CENTER);
                    pack();
                } catch (ClassNotFoundException e) {
                    throw new JAXRException(e);
                } catch (InstantiationException e) {
                    throw new JAXRException(e);
                } catch (IllegalAccessException e) {
                    throw new JAXRException(e);
                }
            }
        }
    }
    
    private String getDialogTitleFromModelClass(Class<?> modelClass) {
        String title = "";
        
        String modelClassName = modelClass.getName();
        @SuppressWarnings("unused")
		String packagePrefix = modelClassName.substring(0,
                modelClassName.lastIndexOf(".") + 1);
        modelClassName = modelClassName.substring(modelClassName.lastIndexOf(
                    ".") + 1);

        if (modelClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            modelClassName = modelClassName.substring(0,
                    modelClassName.length() - 4);
        }

        title = modelClassName;
        return title;
    }
    
    private Class<?> getPanelClassFromModelClass(Class<?> modelClass) throws ClassNotFoundException {
        Class<?> panelClass = null;
        
        //Need to get <modelClass>Panel from <modelClass>.
        String modelClassName = modelClass.getName();
        String packagePrefix = modelClassName.substring(0,
                modelClassName.lastIndexOf(".") + 1);
        modelClassName = modelClassName.substring(modelClassName.lastIndexOf(
                    ".") + 1);

        if (modelClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            modelClassName = modelClassName.substring(0,
                    modelClassName.length() - 4);
        }

        String panelClassName = null;

        try {
            //First try same package as model class for panel class's package
                                    // I18N: Do not localize next statement.
            panelClassName = packagePrefix + modelClassName +
                "Panel";
            panelClass = Class.forName(panelClassName);
        } catch (ClassNotFoundException e) {
            //Next try default package as a fallback for panel class's package
                                    // I18N: Do not localize next statement.
            panelClassName = "it.cnr.icar.eric.client.ui.swing." +
                modelClassName + "Panel";
            panelClass = Class.forName(panelClassName);
        }
        
        return panelClass;
    }

    protected void okAction() {
        super.okAction();

        if (status == JBDialog.OK_STATUS) {
            try {
				// I18N: Do not localize next statement.
                firePropertyChange(PROPERTY_DIALOG_OK, "OK", getModel());
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }
    }

    public static JBEditorDialog showObjectDetails(Component parent,
        Object obj, boolean modal, boolean editable) {
        JBEditorDialog dialog = null;

        try {
            dialog = null;

            Window window = (Window) (SwingUtilities.getRoot(parent));

            if (window instanceof JFrame) {
                dialog = new JBEditorDialog((JFrame) window, modal);
            } else if (window instanceof JDialog) {
                dialog = new JBEditorDialog((JDialog) window, modal);
            }

            dialog.setModel(obj);
            dialog.setEditable(editable);

            if (obj instanceof PropertyChangeListener) {
                dialog.addPropertyChangeListener((PropertyChangeListener) obj);
            }

            dialog.pack();

			if (dialog.getComponentOrientation().isLeftToRight()) {
				dialog.setLocation((int) (window.getLocation().getX() + 30),
								   (int) (window.getLocation().getY() + 30));
			} else {
				dialog.setLocation((window.getWidth() -
										  dialog.getWidth() - 30),
								   (int) (window.getLocation().getY() + 30));
			}

            dialog.setVisible(true);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return dialog;
    }

    protected void validateInput() throws JAXRException {
        super.validateInput();

        if (panel != null) {
            panel.validateInput();
        }
    }

    public void clear() throws JAXRException {
        super.clear();

        if (panel != null) {
            panel.clear();
        }
    }

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEditable(boolean editable) {
        super.setEditable(editable);

        if (panel != null) {
            panel.setEditable(editable);
        }
    }
}
