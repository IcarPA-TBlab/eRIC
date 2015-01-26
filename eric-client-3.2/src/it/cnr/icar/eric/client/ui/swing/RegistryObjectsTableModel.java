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


import it.cnr.icar.eric.client.ui.common.UIUtility;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ConfigurationType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.MethodParameterType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ObjectTypeConfigType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SearchResultsColumnType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SearchResultsConfigType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A JTable that lists
 *
 * @author Jim Glennon
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsTableModel extends AbstractTableModel
implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8965927208117446163L;

	private static final Log log = LogFactory.getLog(RegistryObjectsTableModel.class);

    protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();

    ArrayList<Object> registryObjects = new ArrayList<Object>();
    RegistryBrowser registryBrowser;
    HashMap<String, ObjectTypeConfigType> objectTypeToConfigMap = new HashMap<String, ObjectTypeConfigType>();

    //Current configuration. Changes for each search
    ObjectTypeConfigType otCfg;
    SearchResultsConfigType srCfg;
    List<?> srCols;

    // column caches
    String cachedColumnNames[];
    Class<?> cachedColumnClasses[];

    /**
     * Default Constructor
     */
    public RegistryObjectsTableModel() {
        registryBrowser = RegistryBrowser.getInstance();
        loadConfiguration();
    }

    private void loadConfiguration() {
        ConfigurationType uiConfigurationType = UIUtility.getInstance().getConfigurationType();

        List<?> otCfgs = uiConfigurationType.getObjectTypeConfig();
        Iterator<?> iter = otCfgs.iterator();

        while (iter.hasNext()) {
            ObjectTypeConfigType otCfg = (ObjectTypeConfigType) iter.next();
            String id = otCfg.getId();
            objectTypeToConfigMap.put(id, otCfg);
        }
    }

    /**
     * Get the number of columns in model
     *
     * @return number of columns
     *
     * @see
     */
    public int getColumnCount() {
        if (srCols != null) {
            return srCols.size();
        } else {
            return 0;
        }
    }

    /**
     * Get the width of column 'col', from cfg.
     *
     * @param col The column index.
     * @return int The column width.
     */
    public int getColumnWidth(int col) {
        SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
        int width = (srCol.getColumnWidth()).intValue();
        @SuppressWarnings("unused")
		int cols = getColumnCount();
        return width;
    }

    /**
     * Gets the number of rows in the table model.
     *
     */
    public int getRowCount() {
        return registryObjects.size();
    }

    /**
     * Gets the Class for specified column based upon dynamic configuration.
     */
    public Class<?> getColumnClass(int col) {
        if (cachedColumnClasses[col] != null) {
            return cachedColumnClasses[col];
        }

        Class<?> clazz = null;

        try {
            SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
            String columnClass = srCol.getColumnClass();
            if (columnClass == null) {
                columnClass = "java.lang.Object";
            }
            clazz = Class.forName(columnClass);
        } catch (ClassNotFoundException e) {
            RegistryBrowser.displayError(e);
        }

        cachedColumnClasses[col] = clazz;
        return clazz;
    }

    /*
     * Gets whether a cell in table is editable or not.
     */
    public boolean isCellEditable(int row, int col) {
        boolean editable = false;

        SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
        editable = srCol.isEditable();

        return editable;
    }

    /**
     * Get value at specified cell in table.
     *
     * @param row the row for cell
     * @param col the column for cell
     *
     * @return value for cell
     *
     */
    @SuppressWarnings("rawtypes")
	public Object getValueAt(int row, int col) {
        RegistryObject ro = (RegistryObject) registryObjects.get(row);
        Object value = null;

        try {
            switch (col) {
                case -1:

                    //Special invisible column that contains the actual object
                    value = ro;

                    break;

                default:

                    SearchResultsColumnType srCol = (SearchResultsColumnType) srCols.get(col);
                    String className = otCfg.getClassName();
                    String methodName = srCol.getMethod();
                    Class<?> clazz = Class.forName(className);

                    List<?> params = srCol.getMethodParameter();
                    int numParams = params.size();
                    Class[] parameterTypes = new Class[numParams];
                    Object[] parameterValues = new Object[numParams];
                    Iterator paramsIter = params.iterator();
                    int i = 0;

                    //Setup parameterTypes
                    while (paramsIter.hasNext()) {
                        MethodParameterType uiMethodParameterType = (MethodParameterType) paramsIter.next();
                        String paramTypeName = uiMethodParameterType.getType();
                        parameterTypes[i] = Class.forName(paramTypeName);

                        String paramValue = uiMethodParameterType.getValue();
                        parameterValues[i] = paramValue;

                        i++;
                    }

                    Method method = clazz.getMethod(methodName, parameterTypes);
                    //System.err.println("row=" + row + " col=" + col + " class=" + clazz + " methodName=" + methodName);

                    //Invoke method to get Value as object. Convert the object to a format suitable for display
                    value = method.invoke(ro, parameterValues);
            }
        } catch (ClassNotFoundException e) {
            RegistryBrowser.displayError(resourceBundle.getString("error.classNotFound"),
            e);
        } catch (NoSuchMethodException e) {
            RegistryBrowser.displayError(resourceBundle.getString("error.noSuchMethod"),
            e);
        } catch (IllegalArgumentException e) {
            RegistryBrowser.displayError(e);
        } catch (IllegalAccessException e) {
            RegistryBrowser.displayError(e);
        } catch (InvocationTargetException e) {
            //Commented because we were getting weird swing NPEs
            //RegistryBrowser.displayError(e.getCause());
        } catch (ExceptionInInitializerError e) {
            RegistryBrowser.displayError(e);
        }

        return value;
    }

    /**
     * Method Declaration.
     *
     *
     * @param col
     *
     * @return
     *
     * @see
     */
    public String getColumnName(int col) {
        if (cachedColumnNames[col] != null) {
            return cachedColumnNames[col];
        }

        String columnName = null;
        String localizedColumnName = null;

        try {
            columnName = ((SearchResultsColumnType) srCols.get(col)).getColumnHeader();
            localizedColumnName = resourceBundle.getString("columnName." +
            columnName.replaceAll(" ", ""));
        } catch (MissingResourceException ex) {
            System.out.println("Missed: " + columnName);

            localizedColumnName = columnName;
        } catch (IndexOutOfBoundsException ex) {
            log.warn(resourceBundle.getString("error.outOfBounds") + ex.getMessage());
        }

        cachedColumnNames[col] = localizedColumnName;
        return localizedColumnName;
    }

    @SuppressWarnings("unchecked")
	void update(BulkResponse response) {
        Collection<Object> registryObjects = new ArrayList<Object>();
        try {
            // check for errors
            Collection<?> exceptions = response.getExceptions();
            if (exceptions != null) {
                String errMsg = resourceBundle.getString("error.registryRequest");
                Iterator<?> iter = exceptions.iterator();
                Exception exception = null;
                while (iter.hasNext()) {
                    exception = (Exception) iter.next();
                    RegistryBrowser.displayError(errMsg, exception);
                }
            }

            // check for objects
            // collection may be empty if there were errors
            registryObjects.addAll(response.getCollection());
            //Get the most specific object type that is common to all RegistryObjects
            Concept commonObjectType = UIUtility.getInstance().getCommonObjectType(registryObjects);
            //Dynamically update model configuration basd upon objectType
            updateConfiguration(commonObjectType);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        if (registryObjects.isEmpty()) {
            JOptionPane.showMessageDialog(null,
            resourceBundle.getString("message.noObjects"),
            resourceBundle.getString("title.registryBrowser.java"),
            JOptionPane.INFORMATION_MESSAGE);
        }

        setRegistryObjects(registryObjects);
    }

    /*
     * used by table inside action listener
     */

    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    ArrayList<Object> getRegistryObjects() {
        return registryObjects;
    }

    /**
     * Method Declaration.
     *
     *
     * @param objs
     *
     * @see
     */
    void setRegistryObjects(Collection<Object> objs) {
        registryObjects.clear();
        registryObjects.addAll(objs);
        fireTableDataChanged();
    }

    /**
     * Update the objectType specific configuration based upon specified objectType.
     * Dynamically called each time a update is called.
     */
    private void updateConfiguration(Concept objectType)
    throws JAXRException {
        // reset object list
        setRegistryObjects(new ArrayList<Object>());

        //Get the most specific ObjectTypeConfig for the most specific commonObjectType
        otCfg = UIUtility.getInstance().getObjectTypeConfig(objectType);
        srCfg = otCfg.getSearchResultsConfig();
        srCols = srCfg.getSearchResultsColumn();

        // reset cached column names and types
        int numCols = getColumnCount();
        cachedColumnNames = new String[numCols];
        cachedColumnClasses = new Class[numCols];

        fireTableStructureChanged();
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
        // reset cached column names
        int numCols = getColumnCount();
        cachedColumnNames = new String[numCols];
    }
}
