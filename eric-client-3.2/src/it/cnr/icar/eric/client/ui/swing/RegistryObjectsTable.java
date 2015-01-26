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


import it.cnr.icar.eric.client.ui.swing.graph.JBGraphPanel;
import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;

/**
 * A JTable that lists
 *
 * @author Jim Glennon
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryObjectsTable extends JTable
implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4172387143473506093L;

	private static final Log log = LogFactory.getLog(RegistryObjectsTable.class);
    
    protected JavaUIResourceBundle resourceBundle =
    JavaUIResourceBundle.getInstance();
    
    /** DOCUMENT ME! */
    public static final String SELECTED_ROW_PROP = "selectedRow";
    
    /** DOCUMENT ME! */
    int selectedRow = -1;
    
    /** DOCUMENT ME! */
    JPopupMenu popup;
    
    /** DOCUMENT ME! */
    JMenuItem editMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem removeMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem saveMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem approveMenuItem;
    
    JMenuItem setStatusMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem deprecateMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem undeprecateMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem browseMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem exportMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem auditTrailMenuItem;
    
    /** DOCUMENT ME! */
    JMenuItem showRepositoryItemMenuItem;
    
    JMenuItem showRegistryObjectMenuItem;
    
    /** DOCUMENT ME! */
    MouseListener popupListener;
    private boolean editable = false;
    
    /** DOCUMENT ME! */
    final RegistryObjectsTableModel tableModel;
    int stdRowHeight = 0;
    
    /** Cached TableCellRenderer */
    private TableCellRenderer cachedTableCellRenderers[];
    
    /**
     * Class Constructor.
     *
     * @param model
     *
     * @see
     */
    public RegistryObjectsTable(TableModel model) {
        // Gives a TableColumnModel so that AutoCreateColumnsFromModel will be false.
        super(model, new DefaultTableColumnModel());
        
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        if (model instanceof RegistryObjectsTableModel) {
            tableModel = (RegistryObjectsTableModel) model;
        } else if (model instanceof TableSorter) {
            tableModel = (RegistryObjectsTableModel) (((TableSorter) model).getModel());
        } else {
            Object[] unexpectedTableModelArgs = {model};
            MessageFormat form =
            new MessageFormat(resourceBundle.
            getString("error.unexpectedTableModel"));
            throw new IllegalArgumentException(form.format(unexpectedTableModelArgs));
        }
        
        setToolTipText(resourceBundle.getString("tip.registryObjectsTable"));
        
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ListSelectionModel rowSM = getSelectionModel();
        stdRowHeight = getRowHeight();
        setRowHeight(stdRowHeight * 3);
        
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                
                if (!lsm.isSelectionEmpty()) {
                    setSelectedRow(lsm.getMinSelectionIndex());
                } else {
                    setSelectedRow(-1);
                }
            }
        });
        
        createPopup();
        
        addRenderers();
        
        // Add listener to self so that I can bring up popup menus on right mouse click
        popupListener = new PopupListener();
        addMouseListener(popupListener);
        
        //add listener for 'authenticated' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(RegistryBrowser.PROPERTY_AUTHENTICATED,
        this);
        
        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().
        addPropertyChangeListener(RegistryBrowser.PROPERTY_LOCALE,
        this);
    }
    
    @SuppressWarnings("unchecked")
	private void addRenderers() {
        try {
            setDefaultRenderer(Class.forName("java.lang.Object"),
            new JBDefaultTableCellRenderer());
            setDefaultRenderer(Class.forName("java.util.Collection"),
            new CollectionRenderer());
            
            final JList<Object> list = new JList<Object>();
            list.setVisibleRowCount(3);
            list.setOpaque(true);
            list.setCellRenderer(new ListDefaultRenderer());
            
            list.addListSelectionListener(new ListSelectionListener() {
                @SuppressWarnings({ "static-access", "unused" })
				public void valueChanged(
                ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        Object obj = list.getSelectedValue();
                        obj = RegistryObjectsTable.this.convertValue(obj);
                        
                        if (obj instanceof URL) {
                            HyperLinker.displayURL(obj.toString());
                        } else if (obj instanceof String) {
                            //Check if URL is valid
                            URL _url = null;
                            
                            try {
                                _url = new URL(obj.toString());
                                HyperLinker.displayURL(obj.toString());
                            } catch (MalformedURLException exc) {
                                //No need to do anything. It is normal for text to not be a URL
                            	_url = null;
                            }
                        }
                    }
                }
            });
            
            JScrollPane listPane = new JScrollPane(list);
            setDefaultEditor(Class.forName("java.util.Collection"),
            new JBDefaultCellEditor(listPane));
            
            HyperLinkLabel hyperLinkLabel = new HyperLinkLabel();
            hyperLinkLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            setDefaultEditor(Class.forName("java.lang.Object"),
            new JBDefaultCellEditor(hyperLinkLabel));
        } catch (ClassNotFoundException e) {
        	return;
        }
    }
    
    public Class<?> getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (cachedTableCellRenderers[column] != null) {
            return cachedTableCellRenderers[column];
        }
        TableCellRenderer renderer = null;
        Class<?> clazz = null;
        
        try {
            clazz = tableModel.getColumnClass(column);
            renderer = super.getCellRenderer(row, column);
            
            if (renderer == null) {
                Object[] unsupportedColumnClassArgs = {tableModel.getColumnName(column),
                clazz};
                MessageFormat form = new MessageFormat(resourceBundle.
                getString("error.unsupportedColumnClass"));
                RegistryBrowser.displayError(form.format(unsupportedColumnClassArgs));
            }
        } catch (Exception e) {
            Object[] unsupportedColumnClassArgs = {tableModel.getColumnName(column),
            clazz};
            MessageFormat form =
            new MessageFormat(resourceBundle.
            getString("error.unsupportedColumnClass"));
            RegistryBrowser.displayError(form.format(unsupportedColumnClassArgs),
            e);
        }
        
        if (renderer instanceof JLabel) {
            ((JLabel)renderer).setHorizontalAlignment(SwingConstants.LEFT);
        }
        cachedTableCellRenderers[column] = renderer;
        return renderer;
    }
    
    private void createPopup() {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            ConnectionImpl connection = (ConnectionImpl) client.getConnection();
            boolean authenticated = connection.isAuthenticated();
            
            // Create popup menu for table
            popup = new JPopupMenu();
            
            if (editable) {
                editMenuItem = new JMenuItem(resourceBundle.getString("menu.edit"));
            } else {
                editMenuItem = new JMenuItem(resourceBundle.getString("menu.showDetails"));
            }
            
            popup.add(editMenuItem);
            removeMenuItem = new JMenuItem(resourceBundle.getString("menu.remove"));
            popup.add(removeMenuItem);
            saveMenuItem = new JMenuItem(resourceBundle.getString("menu.save"));
            popup.add(saveMenuItem);
            
            setStatusMenuItem = new JMenu(resourceBundle.getString("menu.setStatus"));
            Collection<?> statusTypeConcepts = getStatusTypeConcepts();
            createMenuItemsForTaxonomyElements(setStatusMenuItem, statusTypeConcepts);
            popup.add(setStatusMenuItem);
            
            approveMenuItem = new JMenuItem(resourceBundle.getString("menu.approve"));
            popup.add(approveMenuItem);
                        
            deprecateMenuItem = new JMenuItem(resourceBundle.getString("menu.deprecate"));
            popup.add(deprecateMenuItem);
            undeprecateMenuItem = new JMenuItem(resourceBundle.getString("menu.undeprecate"));
            popup.add(undeprecateMenuItem);
            browseMenuItem = new JMenuItem(resourceBundle.getString("menu.browse"));
            popup.add(browseMenuItem);
            
            exportMenuItem = new JMenuItem(resourceBundle.getString("menu.export"));
            popup.add(exportMenuItem);
            
            auditTrailMenuItem = new JMenuItem(resourceBundle.getString("menu.showAuditTrail"));
            popup.add(auditTrailMenuItem);
            showRegistryObjectMenuItem =
            new JMenuItem(resourceBundle.getString("menu.showRegistryObject"));
            popup.add(showRegistryObjectMenuItem);
            showRepositoryItemMenuItem =
            new JMenuItem(resourceBundle.getString("menu.showRepositoryItem"));
            showRepositoryItemMenuItem.setVisible(false);
            popup.add(showRepositoryItemMenuItem);
            
            editMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    editAction();
                }
            });
            
            removeMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    removeAction();
                }
            });
            
            saveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    saveAction();
                }
            });
            
            approveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    approveAction();
                }
            });
                        
            deprecateMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    deprecateAction();
                }
            });
            
            undeprecateMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    undeprecateAction();
                }
            });
            
            browseMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    browseAction();
                }
            });
            
            exportMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    exportAction();
                }
            });
            
            auditTrailMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    auditTrailAction();
                }
            });
            
            showRegistryObjectMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    showRegistryObjectAction();
                }
            });
            
            showRepositoryItemMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    showRepositoryItemAction();
                }
            });
            
            // hide and disable, if not authenticated
            removeMenuItem.setVisible(authenticated);
            saveMenuItem.setVisible(authenticated);
            approveMenuItem.setVisible(authenticated);
            setStatusMenuItem.setVisible(authenticated);
            deprecateMenuItem.setVisible(authenticated);
            undeprecateMenuItem.setVisible(authenticated);
            removeMenuItem.setEnabled(authenticated);
            saveMenuItem.setEnabled(authenticated);
            approveMenuItem.setEnabled(authenticated);
            setStatusMenuItem.setEnabled(authenticated);
            deprecateMenuItem.setEnabled(authenticated);
            undeprecateMenuItem.setEnabled(authenticated);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }
    
    private Collection<?> getStatusTypeConcepts() throws JAXRException {
        Collection<?> statusTypeConcepts = new ArrayList<Object>();
        
        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessQueryManager bqm = client.getBusinessQueryManager();
        
        ClassificationScheme statusTypeScheme = (ClassificationScheme)bqm.getRegistryObject(
                CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_StatusType,
                LifeCycleManager.CLASSIFICATION_SCHEME);
        
        statusTypeConcepts = statusTypeScheme.getChildrenConcepts();
        return statusTypeConcepts;
    }
    
    
    /**
     * DOCUMENT ME!
     */
    protected void editAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length == 1) {
            showSelectedObjectDetails();
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.editDetailsAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    ArrayList<RegistryObject> getSelectedRegistryObjects() {
        ArrayList<RegistryObject> selectedObjects = new ArrayList<RegistryObject>();
        int[] selectedIndices = getSelectedRows();
        
        for (int i = 0; i < selectedIndices.length; i++) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[i],
            -1);
            selectedObjects.add(ro);
        }
        
        return selectedObjects;
    }
    
    /**
     * DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
	protected void removeAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            try {
                ArrayList<?> selectedObjects = getSelectedRegistryObjects();
                ArrayList<Key> removeKeys = new ArrayList<Key>();
                
                int size = selectedObjects.size();
                
                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    removeKeys.add(key);
                }
                
                // Confirm the remove
                boolean confirmRemoves = true;
                // I18N: Do not localize next statement.
                String confirmRemovesStr = ProviderProperties.getInstance()
                .getProperty("jaxr-ebxml.registryBrowser.confirmRemoves",
                "true");
                
                if (confirmRemovesStr.equalsIgnoreCase("false") ||
                confirmRemovesStr.toLowerCase().equals("off")) {
                    confirmRemoves = false;
                }
                
                if (confirmRemoves) {
                    int option =
                    JOptionPane.showConfirmDialog(null,
                    resourceBundle.getString("dialog.confirmRemove.text"),
                    resourceBundle.getString("dialog.confirmRemove.title"),
                    JOptionPane.YES_NO_OPTION);
                    
                    if (option == JOptionPane.NO_OPTION) {
                        RegistryBrowser.setDefaultCursor();
                        
                        return;
                    }
                }

                // cancels the cell editor, if any
                removeEditor();
                
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
                BulkResponse resp = lcm.deleteObjects(removeKeys);
                client.checkBulkResponse(resp);
                
                if (resp.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                    //Remove from UI model
                    @SuppressWarnings("rawtypes")
					ArrayList objects = (ArrayList) ((tableModel.getRegistryObjects()).clone());
                    size = selectedIndices.length;
                    
                    for (int i = size - 1; i >= 0; i--) {
                        RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[i],
                        -1);
                        objects.remove(ro);
                    }
                    
                    tableModel.setRegistryObjects(objects);
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.removeAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * The save action.
     */
    protected void saveAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            ArrayList<?> selectedObjects = getSelectedRegistryObjects();
            RegistryBrowser.showSaveDialog(selectedObjects);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.saveAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * The approve action
     */
    protected void approveAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            ArrayList<?> selectedObjects = getSelectedRegistryObjects();
            
            try {
                ArrayList<Key> keys = new ArrayList<Key>();
                
                int size = selectedObjects.size();
                
                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    keys.add(key);
                }
                
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)client.getBusinessLifeCycleManager();
                
                BulkResponse resp = lcm.approveObjects(keys);
                JAXRUtility.checkBulkResponse(resp);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.approveAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    
    /**
     * The deprecate action
     */
    protected void deprecateAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            ArrayList<?> selectedObjects = getSelectedRegistryObjects();
            
            try {
                ArrayList<Key> keys = new ArrayList<Key>();
                
                int size = selectedObjects.size();
                
                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    keys.add(key);
                }
                
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)client.getBusinessLifeCycleManager();
                BulkResponse resp = lcm.deprecateObjects(keys);
                JAXRUtility.checkBulkResponse(resp);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.deprecateAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * The undeprecate action
     */
    protected void undeprecateAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            ArrayList<?> selectedObjects = getSelectedRegistryObjects();
            
            try {
                ArrayList<Key> keys = new ArrayList<Key>();
                
                int size = selectedObjects.size();
                
                for (int i = size - 1; i >= 0; i--) {
                    RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                    Key key = obj.getKey();
                    keys.add(key);
                }
                
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)client.getBusinessLifeCycleManager();
                BulkResponse resp = lcm.unDeprecateObjects(keys);
                JAXRUtility.checkBulkResponse(resp);
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.undeprecateAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void browseAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length >= 1) {
            ArrayList<?> selectedObjects = getSelectedRegistryObjects();
            Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);
            
            if (parent instanceof JFrame) {
                JBGraphPanel.browseObjects((JFrame) parent, selectedObjects,
                editable);
            } else if (parent instanceof JDialog) {
                JBGraphPanel.browseObjects((JDialog) parent, selectedObjects,
                editable);
            }
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.browseAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    @SuppressWarnings("static-access")
	protected void exportAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedRow,
            -1);
            
            try {
                JAXRClient client = RegistryBrowser.getInstance().getClient();
                DeclarativeQueryManagerImpl dqm = client.getDeclarativeQueryManager();
                String queryId = CanonicalConstants.CANONICAL_QUERY_Export;
                Map<String, String> queryParams = new HashMap<String, String>();
                @SuppressWarnings("unused")
				String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
                queryParams.put("$schemaComponentId", ro.getKey().getId());
                
                queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, queryId);
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL);

                //Add response option to ensure that RepositoryItem is returned
                String returnType = ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM.value();
                queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);
                
                BulkResponse bResponse = dqm.executeQuery(query, queryParams);
                Collection<?> registryObjects = bResponse.getCollection();
                RegistryBrowser.getInstance().exportToFile(registryObjects);
                
            } catch (ObjectNotFoundException e) {
                RegistryBrowser.displayError(resourceBundle.getString("message.info.exportFeatureNotConfigured"));
            } catch (Exception e) {
                RegistryBrowser.displayError(e);
            }
            
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("message.error.cannotExportMultipleObjects"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void auditTrailAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedRow,
            -1);
            RegistryBrowser.showAuditTrail(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.auditTrail"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void showRegistryObjectAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[0],
            -1);
            @SuppressWarnings("unused")
			Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);
            RegistryBrowser.showRegistryObject(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.showRegistryObjectAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void showRepositoryItemAction() {
        RegistryBrowser.setWaitCursor();
        
        int[] selectedIndices = getSelectedRows();
        
        if (selectedIndices.length == 1) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedIndices[0],
            -1);
            @SuppressWarnings("unused")
			Component parent = SwingUtilities.getRoot(RegistryObjectsTable.this);
            RegistryBrowser.showRepositoryItem(ro);
        } else {
            RegistryBrowser.displayError(resourceBundle.getString("error.showRepositoryItemAction"));
        }
        
        RegistryBrowser.setDefaultCursor();
    }
    
    /**
     * Creates default columns for the table from
     * the data model using the <code>getColumnCount</code> method
     * defined in the <code>TableModel</code> interface.
     *
     * Clears any existing columns before creating the
     * new columns based on information from the model.
     *
     * Overrides base class behaviour by setting the column width as a % of the
     * viewport width.
     */
    public void createDefaultColumnsFromModel() {
        TableModel m = getModel();
        if (m != null) {
            // Remove any current columns
            TableColumnModel cm = getColumnModel();
            while (cm.getColumnCount() > 0) {
                cm.removeColumn(cm.getColumn(0));
            }
            
            // get parent width
            int parentWidth = 0;
            Component parent = getParent();
            if (parent != null) {
                parentWidth = parent.getWidth();
            }
            
            // Create new columns from the data model info
            int columnCount = m.getColumnCount();
            for (int i = 0; i < m.getColumnCount(); i++) {
                int width = tableModel.getColumnWidth(i);
                if (width == 0) {
                    width = parentWidth / columnCount;
                } else {
                    //Width is a % of the viewport width
                    width = (width * parentWidth) / 100;
                }
                TableColumn newColumn = new TableColumn(i);
                newColumn.setPreferredWidth(width);
                addColumn(newColumn);
            }
        }
    }
    
    /**
     * Sets the currently selected row in table Also does
     * firePropertyChange on property "selectedRow"
     *
     * @param index DOCUMENT ME!
     */
    private void setSelectedRow(int index) {
        Integer oldIndex = new Integer(selectedRow);
        
        selectedRow = index;
        firePropertyChange(SELECTED_ROW_PROP, oldIndex, new Integer(index));
    }
    
    /**
     * Method Declaration.
     *
     * @param makeVisible
     *
     * @see
     */
    public void setVisible(boolean makeVisible) {
        //jimbog        Log.print(Log.TRACE,1,"Destination table visible:" + makeVisible);
        if (makeVisible) {
        }
        
        super.setVisible(makeVisible);
    }
    
    /**
     * DOCUMENT ME!
     */
    private void showSelectedObjectDetails() {
        if (selectedRow >= 0) {
            RegistryObject ro = (RegistryObject) dataModel.getValueAt(selectedRow,
            -1);
            JBEditorDialog.showObjectDetails(this, ro, false, editable);
        }
    }
    
    /**
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_AUTHENTICATED.  Certain menuItems are
     * hidden when user is unAuthenticated.
     *
     * Listens to property changes in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.  Updates locale and UI strings
     * when the property changes.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_AUTHENTICATED)) {
            boolean authenticated = ((Boolean) ev.getNewValue()).booleanValue();
            
            setEditable(authenticated);
        } else if (ev.getPropertyName().equals(RegistryBrowser.PROPERTY_LOCALE)) {
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
        setToolTipText(resourceBundle.getString("tip.registryObjectsTable"));
        
        if (editable) {
            editMenuItem.setText(resourceBundle.getString("menu.edit"));
        } else {
            editMenuItem.setText(resourceBundle.getString("menu.showDetails"));
        }
        
        removeMenuItem.setText(resourceBundle.getString("menu.remove"));
        saveMenuItem.setText(resourceBundle.getString("menu.save"));
        browseMenuItem.setText(resourceBundle.getString("menu.browse"));
        exportMenuItem.setText(resourceBundle.getString("menu.export"));
        auditTrailMenuItem.setText(resourceBundle.getString("menu.showAuditTrail"));
        showRegistryObjectMenuItem.setText(resourceBundle.getString("menu.showRegistryObject"));
        showRepositoryItemMenuItem.setText(resourceBundle.getString("menu.showRepositoryItem"));
    }
    
    /**
     * Converts an Object value to a format suitable for display in JTable.
     */
    protected static Object convertValue(Object value) {
        //TODO: This method appears in some 4 different places in code.. why?
        Object finalValue = null;
        Locale selectedLocale = RegistryBrowser.getInstance().getSelectedLocale();
        
        try {
            if (value == null) {
                finalValue = value;
            } else if (value instanceof InternationalString) {
                finalValue = ((InternationalStringImpl) value).getClosestValue(selectedLocale);
            } else if (value instanceof ExternalLink) {
                finalValue = ((ExternalLink) value).getExternalURI();
                
                try {
                    URL url = new URL(((ExternalLink) value).getExternalURI());
                    finalValue = url;
                } catch (MalformedURLException e) {
                	finalValue = null;
                }
            } else if (value instanceof Collection) {
                //Converts elements of Collection
                Collection<?> c1 = (Collection<?>) value;
                Collection<Object> c2 = new ArrayList<Object>();
                Iterator<?> iter = c1.iterator();
                
                while (iter.hasNext()) {
                    c2.add(convertValue(iter.next()));
                }
                
                finalValue = c2;
            } else if (value instanceof Slot) {
                Collection<?> c = ((Slot) value).getValues();
                finalValue = c;
            } else if (value instanceof Concept) {
                finalValue = ((Concept) value).getValue();
            } else {
                finalValue = value;
            }
        } catch (JAXRException e) {
            log.error(e);
        }
        
        return finalValue;
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
    
    /**
     * Renderer used to render all Collection types.
     * Uses a JList to display the Collection.
     */
    class CollectionRenderer extends JScrollPane implements TableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 6101650408150517967L;
		JList<?> list;
        
        @SuppressWarnings("unchecked")
		public CollectionRenderer() {
            list = new JList<Object>();
            
            Font font = RegistryObjectsTable.this.getFont();
            list.setFont(font);
            list.setVisibleRowCount(3);
            list.setOpaque(true);
            
            ListDefaultRenderer renderer = new ListDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.TRAILING);
            list.setCellRenderer(renderer);
            list.setBorder(BorderFactory.createEmptyBorder());
            this.setBorder(BorderFactory.createEmptyBorder());
            
            this.setViewportView(list);
            
            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }
        
        @SuppressWarnings("unchecked")
		public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row,
        int column) {
            value = convertValue(value);
            
            if (isSelected) {
                list.setBackground(table.getSelectionBackground());
                list.setForeground(table.getSelectionForeground());
            } else {
                list.setBackground(table.getBackground());
                list.setForeground(table.getForeground());
            }
            
            @SuppressWarnings("rawtypes")
			DefaultListModel model = new DefaultListModel<Object>();
            Collection<?> c = (Collection<?>) value;
            
            if (c != null) {
                Iterator<?> iter = c.iterator();
                
                while (iter.hasNext()) {
                    model.addElement(iter.next());
                }
            }
            
            list.setModel(model);
            
            return this;
        }
    }
    
    /**
     * Editor used to edit all types.
     * Adds support for Collection and URL types to DefaultCellEditor
     */
    class JBDefaultCellEditor extends DefaultCellEditor {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8243309861597356086L;

		/**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final JScrollPane scrollPane) {
            super(new JTextField());
            editorComponent = scrollPane;
            
            this.clickCountToStart = 1;
            
            final Component comp = scrollPane.getViewport().getView();
            delegate = new javax.swing.DefaultCellEditor.EditorDelegate() {
                /**
				 * 
				 */
				private static final long serialVersionUID = -6214182787119287037L;

				@SuppressWarnings("unchecked")
				public void setValue(Object value) {
                    value = convertValue(value);
                    
                    if (comp instanceof JList) {
                        @SuppressWarnings({ "rawtypes" })
						JList<Object> list = (JList) comp;
                        @SuppressWarnings("rawtypes")
						DefaultListModel model = new DefaultListModel();
                        
                        if (value instanceof Collection) {
                            Collection<?> c = (Collection<?>) value;
                            Iterator<?> iter = c.iterator();
                            
                            while (iter.hasNext()) {
                                Object obj = iter.next();
                                model.addElement(obj);
                            }
                        }
                        
                        list.setModel(model);
                    }
                }
                
                @SuppressWarnings({ "rawtypes", "unchecked" })
				public Object getCellEditorValue() {
                    Object value = null;
                    
                    if (comp instanceof JList) {
                        Collection c = new ArrayList();
                        JList list = (JList) comp;
                        ListModel model = list.getModel();
                        
                        for (int i = 0; i < model.getSize(); i++) {
                            c.add(model.getElementAt(i));
                        }
                        
                        value = c;
                    }
                    
                    return value;
                }
            };
            
            //list.addActionListener(delegate);
        }
        
        /**
         * Constructs a <code>JBDefaultCellEditor</code> that uses a JList field.
         *
         * @param x  a <code>JList</code> object
         */
        public JBDefaultCellEditor(final HyperLinkLabel label) {
            super(new JTextField());
            
            //list.setDefaultRenderer(Class.forName("java.net.URL"), new URLRenderer());
            editorComponent = label;
            this.clickCountToStart = 1;
            delegate = new javax.swing.DefaultCellEditor.EditorDelegate() {
                /**
				 * 
				 */
				private static final long serialVersionUID = -6729318949726704623L;

				public void setValue(Object value) {
                    try {
                        label.setURL(null);
                    } catch (MalformedURLException e) {
                        //Do nothing as this will never be thrown here.
                    }
                    
                    label.setText(value.toString());
                }
                
                public Object getCellEditorValue() {
                    return label.getText();
                }
            };
            
            //list.addActionListener(delegate);
        }
    }
    
    @SuppressWarnings("rawtypes")
	class ListDefaultRenderer extends HyperLinkLabel implements ListCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 8573418795686395195L;

		public ListDefaultRenderer() {
            setOpaque(true);
            
            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
            Font font = RegistryObjectsTable.this.getFont();
            setFont(font);
            list.setFont(font);
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            value = RegistryObjectsTable.convertValue(value);
            setText(value.toString());
            
            return this;
        }
    }
    
    /**
     * Class Declaration.
     *
     * @author
     * @version 1.9, 03/29/00
     *
     * @see
     */
    class PopupListener extends MouseAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mousePressed(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int index = rowAtPoint(p);
            
            Object ro = dataModel.getValueAt(index, -1);
            
            if (ro instanceof ExtrinsicObject) {
                showRepositoryItemMenuItem.setVisible(true);
            } else {
                showRepositoryItemMenuItem.setVisible(false);
            }
            
            maybeShowPopup(e);
            
            if (e.getClickCount() > 1) {
                //showSelectedObjectDetails();
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                @SuppressWarnings("unused")
				int[] selectedIndices = getSelectedRows();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    void updateModel(BulkResponse response) {
        tableModel.update(response);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	void clearModel() {
        tableModel.setRegistryObjects(new ArrayList());
    }
    
    // ********************************************************************** //
    // Implementation for interface TableModelListener                        //
    // ********************************************************************** //
    
    /**
     * This fine grain notification tells listeners the exact range
     * of cells, rows, or columns that changed.
     *
     * Overrides base class behaviour by setting selection when first
     * row (destination) is added to model.
     */
    public void tableChanged(TableModelEvent e) {
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
            // The whole thing changed
            // This will effect invalidation of the JTable and JTableHeader.
            createDefaultColumnsFromModel();
            // Reset cachedTableCellRenderer
            cachedTableCellRenderers = new TableCellRenderer[getColumnCount()];
            return;
        } else {
            super.tableChanged(e);
        }
        // If no selectedRow, set selectedRow to firstRow
        if ((selectedRow == -1) && (e.getType() == TableModelEvent.INSERT)) {
            // Following will result in a software initiated selection
            // of the first row in table
            ListSelectionModel rowSM = getSelectionModel();
            rowSM.setSelectionInterval(0, 0);
        }
    }
    
    private void createMenuItemsForTaxonomyElements(JMenuItem parentMenuItem, Collection<?> taxonomyElements) throws JAXRException {
        Iterator<?> iter = taxonomyElements.iterator();
        while (iter.hasNext()) {
            Object taxonomyElementObj = iter.next();

            Collection<?> children = null;
            if (taxonomyElementObj instanceof ClassificationScheme) {
                children = ((ClassificationScheme)taxonomyElementObj).getChildrenConcepts();
            } else if (taxonomyElementObj instanceof Concept) {
                children = ((Concept)taxonomyElementObj).getChildrenConcepts();
            } else {
                throw new JAXRException(CommonResourceBundle.getInstance().getString("message.unexpectedObjectType",
                            new String[] {taxonomyElementObj.getClass().toString(), "javax.xml.registry.infomodel.ClassificationScheme, javax.xml.registry.infomodel.ClassificationScheme"}));
            }
            
            String childName = ((RegistryObjectImpl)taxonomyElementObj).getDisplayName();
            
            JMenuItem childMenuItem = null;
            
            //Need to handle intermediate nodes different from terminal nodes
            //Intermediate nodes: have a JMenu and JMenuItem children where first child represents themself
            //Leaf nodes: have a JMenuItem that represents themself
            if (children.size() > 0) {
                //Intermediate node
                childMenuItem = new JMenu(childName);
                
                //Add a first child that represents the intermediate node itself
                JMenuItem firstGrandChildMenuItem = new TaxonomyElementMenuItem(((RegistryObjectImpl)taxonomyElementObj));
                Action action = new SetStatusAction(((RegistryObjectImpl)taxonomyElementObj));
                firstGrandChildMenuItem.setAction(action);                        
                childMenuItem.add(firstGrandChildMenuItem);
                
                //Now add a separator
                JSeparator separator = new JSeparator();
                childMenuItem.add(separator);
                
                createMenuItemsForTaxonomyElements(childMenuItem, children);
            } else {
                //Leaf node
                childMenuItem = new TaxonomyElementMenuItem(((RegistryObjectImpl)taxonomyElementObj));
                Action action = new SetStatusAction(((RegistryObjectImpl)taxonomyElementObj));
                childMenuItem.setAction(action);                        
            }                        
            parentMenuItem.add(childMenuItem);                
        }
    }
    
    
    class TaxonomyElementMenuItem extends JMenuItem {
        /**
		 * 
		 */
		private static final long serialVersionUID = -7440266131605484746L;
		private RegistryObjectImpl taxonomyElement = null;
        
        public TaxonomyElementMenuItem(RegistryObjectImpl taxonomyElement) throws JAXRException {
            super(taxonomyElement.getDisplayName());
            TaxonomyElementMenuItem.this.taxonomyElement = taxonomyElement;
        }

        public RegistryObjectImpl getTaxonomyElement() {
            return taxonomyElement;
        }
    }
    
    class SetStatusAction extends AbstractAction {
        /**
		 * 
		 */
		private static final long serialVersionUID = 8185379220464844303L;
		public SetStatusAction(RegistryObjectImpl taxonomyElement) throws JAXRException {
            super(taxonomyElement.getDisplayName());            
        }
        public void actionPerformed(ActionEvent ae) {
            RegistryBrowser.setWaitCursor();

            int[] selectedIndices = getSelectedRows();

            if (selectedIndices.length >= 1) {
                ArrayList<?> selectedObjects = getSelectedRegistryObjects();

                try {
                    ArrayList<Key> keys = new ArrayList<Key>();

                    int size = selectedObjects.size();

                    for (int i = size - 1; i >= 0; i--) {
                        RegistryObject obj = (RegistryObject) selectedObjects.get(i);
                        Key key = obj.getKey();
                        keys.add(key);
                    }

                    JAXRClient client = RegistryBrowser.getInstance().getClient();
                    LifeCycleManagerImpl lcm = (LifeCycleManagerImpl)client.getBusinessLifeCycleManager();

                    Object source = ae.getSource();
                    String statusId = ((Concept)(((TaxonomyElementMenuItem)source).getTaxonomyElement())).getKey().getId();
                    BulkResponse resp = lcm.setStatusOnObjects(keys, statusId);
                    JAXRUtility.checkBulkResponse(resp);
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
            } else {
                RegistryBrowser.displayError(resourceBundle.getString("error.approveAction"));
            }

            RegistryBrowser.setDefaultCursor();
        }
    }        
    
}
