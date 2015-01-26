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
import it.cnr.icar.eric.client.ui.common.conf.bindings.InternationalStringType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ObjectRefType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ParameterType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SlotListType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SlotType1;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ValueListType;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.QueryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.common.BindingUtility;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 *  The panel that displays UI for specifying parameters to a Ad hoc Query.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AdhocQuerySearchPanel extends QueryPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -321882255570416177L;
	private GridBagConstraints c = new GridBagConstraints();
    @SuppressWarnings("unused")
	private FindParamsPanel findParamsPanel;
    private ArrayList<QueryType> queries = new ArrayList<QueryType>();
    private JComboBox<?> selectQueryCombo;
    /** Label for the selectQueryCombo */
    private JLabel selectQueryLabel;
    private JTextField queryNameText;
    /** Label for the nameTextField */
    private JLabel queryNameLabel;
    private JTextArea queryDescText;
    /** Label for the description TextArea */
    private JLabel queryDescLabel;
    @SuppressWarnings("unused")
	private JPanel querySelectionPanel;
    private JPanel parentOfEntryPanel;
    private ArrayList<JComponent> paramComponents = new ArrayList<JComponent>();
    private QueryType query;
    private DeclarativeQueryManagerImpl dqm;
    private LifeCycleManagerImpl lcm;
    private HashMap<QueryType, JPanel> queryToParamEntryPanelMap = new HashMap<QueryType, JPanel>();
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private HashMap<QueryType, Comparable> queryToAdhocQueryMap = new HashMap();
    
    private static final Log log = LogFactory.getLog(AdhocQuerySearchPanel.class);

    /**
     * Class Constructor.
     */
    public AdhocQuerySearchPanel(final FindParamsPanel findParamsPanel, ConfigurationType cfg) {
        super(findParamsPanel, cfg);
        
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        
        // create QuerySelection and QueryParams panels
        JPanel querySelectionPanel = createQuerySelectionPanel();
        parentOfEntryPanel = createParentOfEntryPanels();
        
        JScrollPane querySelectionScrollPane = new JScrollPane(querySelectionPanel);
        JScrollPane parentOfEntryScrollPane = new JScrollPane(parentOfEntryPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
            querySelectionScrollPane, parentOfEntryScrollPane);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        //add listener for 'locale' bound property
        RegistryBrowser.getInstance().addPropertyChangeListener(
            RegistryBrowser.PROPERTY_LOCALE, this);
    }
    
    public void reloadModel() {
        final SwingWorker worker = new SwingWorker(this) {
            public Object doNonUILogic() {
                try {
                    // This might take a while. Contructor should be running as SwingWorker
                    JAXRClient client = RegistryBrowser.getInstance().getClient();
                    Connection connection = client.getConnection();
                    RegistryService service = connection.getRegistryService();
                    dqm = (it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();            
                    lcm = (it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl)service.getBusinessLifeCycleManager();            
                    processConfiguration();
                    // model!!!
                } catch (JAXRException e) {
                    throw new UndeclaredThrowableException(e);
                }
                return null;
            }
            @SuppressWarnings({ "unchecked", "rawtypes" })
			public void doUIUpdateLogic() {
                try {
                    selectQueryCombo.setModel(new DefaultComboBoxModel(getQueryNames()));
                    if (queries.size() > 0) {
                        setQuery(queries.get(0));
                    }
                } catch (JAXRException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };
        worker.start();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private JPanel createQuerySelectionPanel() {
        JPanel querySelectionPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        querySelectionPanel.setLayout(gbl);
        
        //The selectQueryCombo
        selectQueryLabel =
        new JLabel(resourceBundle.getString("title.selectQuery"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(selectQueryLabel, c);
        querySelectionPanel.add(selectQueryLabel);
        
        //TODO: SwingBoost: localize this:
        selectQueryCombo = new JComboBox(new String[] {"loading queries..."});
        selectQueryCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                RegistryBrowser.setWaitCursor();
                if (ev.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                
                @SuppressWarnings("unused")
				String item = (String) ev.getItem();
                int index = ((JComboBox) ev.getSource()).getSelectedIndex();
                
                QueryType uiQueryType =
                queries.get(index);
                
                try {
                    setQuery(uiQueryType);
                }
                catch (JAXRException e) {
                    RegistryBrowser.setDefaultCursor();
                    throw new UndeclaredThrowableException(e);
                }
                RegistryBrowser.setDefaultCursor();
            }
        });
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(selectQueryCombo, c);
        querySelectionPanel.add(selectQueryCombo);
        
        //The nameTextField
        queryNameLabel =
        new JLabel(resourceBundle.getString("title.name"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(queryNameLabel, c);
        querySelectionPanel.add(queryNameLabel);
        
        queryNameText = new JTextField();
        queryNameText.setEditable(false);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(queryNameText, c);
        querySelectionPanel.add(queryNameText);
        
        //The description TextArea
        queryDescLabel =
        new JLabel(resourceBundle.getString("title.description"),
        SwingConstants.LEADING);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(queryDescLabel, c);
        querySelectionPanel.add(queryDescLabel);
        
        queryDescText = new JTextArea(4, 25);
        queryDescText.setLineWrap(true);
        queryDescText.setEditable(false);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 4);
        gbl.setConstraints(queryDescText, c);
        querySelectionPanel.add(queryDescText);
        
        return querySelectionPanel;
    }
    
    private void setQuery(QueryType uiQueryType) throws JAXRException {
        this.query = uiQueryType;
        
        String name = getQueryName(uiQueryType);
        queryNameText.setText(name);
        queryDescText.setText(getQueryDescription(uiQueryType));
        
        JPanel parameterEntryPanel = getParameterEntryPanel(uiQueryType);
        parentOfEntryPanel.setVisible(false);
        parentOfEntryPanel.removeAll();
        parentOfEntryPanel.add(parameterEntryPanel);
        parentOfEntryPanel.setVisible(true);
    }
    
    private JPanel getParameterEntryPanel(QueryType uiQueryType) {
        JPanel panel = queryToParamEntryPanelMap.get(uiQueryType);
        if (panel == null) {
            panel = createParameterEntryPanel(uiQueryType);
            queryToParamEntryPanelMap.put(uiQueryType, panel);
        }
        
        return panel;
    }
    
    protected void processConfiguration()  {
        List<?> _queries = uiConfigurationType.getQuery();
        
        Iterator<?> iter = _queries.iterator();
        
        while (iter.hasNext()) {
            QueryType uiQueryType = (QueryType) iter.next();
            AdhocQueryImpl ahq = getAdhocQuery(uiQueryType);
            
            if (ahq != null) {
                queries.add(uiQueryType);
            }
        }
    }
    
    private String getQueryDescription(QueryType uiQueryType) throws JAXRException {
        String queryDesc = null;
        
        InternationalString desc = getAdhocQuery(uiQueryType).getDescription();
        queryDesc = getLocalizedValue(desc);
        
        return queryDesc;
    }
    
    private String getQueryName(QueryType uiQueryType) throws JAXRException {
        String queryName = null;
        
        InternationalString name = getAdhocQuery(uiQueryType).getName();
        queryName = getLocalizedValue(name);
        
        return queryName;
    }
    
    private String[] getQueryNames() throws JAXRException {
        String[] queryNames = new String[queries.size()];
        
        int i = 0;
        Iterator<QueryType> iter = queries.iterator();
        
        while (iter.hasNext()) {
            QueryType uiQueryType = iter.next();
            queryNames[i++] = getQueryName(uiQueryType);
        }
        
        return queryNames;
    }
    
    private AdhocQueryImpl getAdhocQuery(QueryType uiQueryType) {
        AdhocQueryImpl adhocQuery = null;
        ObjectRefType queryRef = uiQueryType.getAdhocQueryRef();
        String queryId = queryRef.getId();
        try {
            Object adhocQueryObj = queryToAdhocQueryMap.get(uiQueryType);
            if (adhocQueryObj == null) {
                adhocQuery = (AdhocQueryImpl)dqm.getRegistryObject(queryId); //, "AdhocQuery");
                if (adhocQuery == null) {
                    String msg = resourceBundle.getString(
                        "message.error.failedLoadingAdhocQuery.notFound",
                        new String[] {queryId});
                    log.warn(msg);
                    
                    queryToAdhocQueryMap.put(uiQueryType, Boolean.FALSE);
                } else {
                    queryToAdhocQueryMap.put(uiQueryType, adhocQuery);
                }                
            } else {            
                if (adhocQueryObj instanceof AdhocQueryImpl) {
                    adhocQuery = (AdhocQueryImpl)adhocQueryObj;
                }
            }                        
        } catch (JAXRException e) {
            String msg = resourceBundle.getString(
                "message.error.failedLoadingAdhocQuery.exception",
                new String[] {queryId, e.getLocalizedMessage()});
            log.warn(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        
        return adhocQuery;
    }
    
    
    private String getLocalizedValue(InternationalStringType ist) {
        String str = "";
        
        try {
            org.oasis.ebxml.registry.bindings.rim.InternationalStringType istConverted =  
                (org.oasis.ebxml.registry.bindings.rim.InternationalStringType)UIUtility.getInstance().convertToRimBinding(ist);
            it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl is =
                new it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl(lcm, istConverted);
            str = getLocalizedValue(is);
        } catch (Exception e) {
            String msg = resourceBundle.getString(
                "message.error.failedLoadingAdhocQuery.exception",
                new String[] {"", e.getLocalizedMessage()});
            log.warn(msg, e);
            RegistryBrowser.displayError(msg, e);
        }
        return str;
    }
    
    /*
    private String getLocalizedValue(InternationalStringType i18n) {
        String value = "";
        
        List localizedStrings = i18n.getLocalizedString();
        Iterator iter = localizedStrings.iterator();
        
        //?? This should use InternationalString.getClosestValue()
        while (iter.hasNext()) {
            LocalizedString ls = (LocalizedString) iter.next();
            value = ls.getValue();
        }
        
        return value;
    }
    */
    
    private String getLocalizedValue(InternationalString i18n) throws JAXRException {
        String value = ((InternationalStringImpl)i18n).getClosestValue();
        
        return value;
    }
    
    private JPanel createParentOfEntryPanels() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.
        createTitledBorder(resourceBundle.
        getString("title.adHocQueryParameters")));
        
        BorderLayout bl = new BorderLayout();
        panel.setLayout(bl);
        
        return panel;
    }
    
    private JPanel createParameterEntryPanel(QueryType uiQueryType) {
        JPanel parameterEntryPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        parameterEntryPanel.setLayout(gbl);
        
        paramComponents.clear();
        
        List<?> params = uiQueryType.getParameter();
        Iterator<?> iter = params.iterator();
        int i = 0;
        
        while (iter.hasNext()) {
            ParameterType param = (ParameterType) iter.next();
            String tooltip = getLocalizedValue(param.getDescription());
            JLabel label = new JLabel(getLocalizedValue(param.getName()) + ":",
            SwingConstants.LEADING);
            label.setToolTipText(tooltip);
            c.gridx = 0;
            c.gridy = i;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_END;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(label, c);
            parameterEntryPanel.add(label);
            
            //Get default value for Parameter if any
            String defaultValue = param.getDefaultValue();
            
            JComponent comp = getComponentForParameter(param, defaultValue);
            
            //comp.setToolTipText(tooltip);
            c.gridx = 1;
            c.gridy = i;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.5;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_END;
            c.insets = new Insets(4, 4, 4, 4);
            gbl.setConstraints(comp, c);
            parameterEntryPanel.add(comp);
            paramComponents.add(comp);
            
            i++;
        }
        
        return parameterEntryPanel;
    }
    
    private JComponent getComponentForParameter(ParameterType uiParameterType, String defaultValue) {
        JComponent comp = null;
        
        String type = uiParameterType.getDatatype();
        
        if (type.equals("boolean")) {
            boolean val = false;
            
            if (defaultValue.equalsIgnoreCase("true")) {
                val = true;
            }
            
            comp = new JCheckBox("", val);
        } else if (type.equals("taxonomyElement")) {
            String domainId = null;
            
            //Get the domain taxonomy element id
            SlotListType slotList = uiParameterType.getSlotList();
            List<?> slots = slotList.getSlot();
            Iterator<?> slotsIter = slots.iterator();
            
            while (slotsIter.hasNext()) {
                SlotType1 uiSlotType = (SlotType1) slotsIter.next();
                
                if (uiSlotType.getName().equalsIgnoreCase("domain")) {
                    ValueListType valList = uiSlotType.getValueList();
                    
                    if (valList != null) {
                        List<String> values = valList.getValue();
                        Iterator<String> valuesIter = values.iterator();
                        domainId = valuesIter.next();
                    }
                }
            }
            
            RegistryObject ro = null;
            
            try {
                ro = dqm.getRegistryObject(domainId);
            } catch (JAXRException e) {
                log.error(e);
                System.exit(-1);
            }
            
            ConceptsTreeModel domainTreeModel = new ConceptsTreeModel(true, ro);
            comp = new it.cnr.icar.eric.client.ui.swing.TreeCombo(domainTreeModel);
        } else {
            comp = new JTextField(defaultValue);
            ((JTextField) comp).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    RegistryBrowser.getInstance().findAction();
                }
            });
        }
        
        return comp;
    }
    
    /**
     * Execute the query using parameters defined by the fields in QueryPanel.
     */
    BulkResponse executeQuery() {
        BulkResponse resp = null;
        
        try {
            Connection connection = RegistryBrowser.client.getConnection();
            @SuppressWarnings("unused")
			RegistryService service = connection.getRegistryService();
            
            HashMap<String, String> parameters = getParameters();
            QueryImpl query = (QueryImpl)dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL);
            query.setFederated(isFederated());
            
            // make JAXR request
            resp = dqm.executeQuery(query, parameters);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
        
        return resp;
    }
    
    private HashMap<String, String> getParameters() throws JAXRException {
        HashMap<String, String> params = new HashMap<String, String>();
        
        params.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, getAdhocQuery(query).getKey().getId());
        
        //Get the value of each parameter from its UI component
        int paramCnt = paramComponents.size();
        
        for (int i=0; i<paramCnt; i++ ) {
            ParameterType uiParameterType = query.getParameter().get(i);
            Component comp = paramComponents.get(i);            
            String paramName = uiParameterType.getParameterName();
            String paramValue = getParameterValue(i, comp);
            if ((paramValue != null) && (paramValue.length() > 0)) {
                params.put(paramName, paramValue);
            }
        }
        
        
        return params;
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
        super.processLocaleChange(newLocale);
        
        updateUIText();
    }
    
    private String getParameterValue(int paramPosition,
    Component component) {
        String paramValue = null;
        
        try {
            if (component instanceof JTextComponent) {
                paramValue = ((JTextComponent) component).getText();
            } else if (component instanceof AbstractButton) {
                boolean selected = ((AbstractButton) component).isSelected();
                
                if (selected) {
                    paramValue = "1";
                } else {
                    paramValue = "0";
                }
            } else if (component instanceof it.cnr.icar.eric.client.ui.swing.TreeCombo) {
                Object conceptsTreeNode = ((it.cnr.icar.eric.client.ui.swing.TreeCombo) component).getSelectedItemsObject();
                Object nodeInfo = ((DefaultMutableTreeNode) conceptsTreeNode).getUserObject();
                
                if (nodeInfo instanceof it.cnr.icar.eric.client.ui.swing.NodeInfo) {
                    Object obj = ((it.cnr.icar.eric.client.ui.swing.NodeInfo) nodeInfo).obj;
                    
                    if (obj instanceof ClassificationScheme) {
                        paramValue = null;
                    } else if (obj instanceof Concept) {
                        paramValue = ((Concept) obj).getPath();
                    }
                }
            } else {
                RegistryBrowser.displayError(
                "Internal error: unsupported component class: " +
                component.getClass().getName());
            }
            
        } catch (JAXRException e) {
            log.error(e);
        }
        
        return paramValue;
    }
    
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        selectQueryLabel.
        setText(resourceBundle.getString("title.selectQuery"));
        queryNameLabel.
        setText(resourceBundle.getString("title.name"));
        queryDescLabel.
        setText(resourceBundle.getString("title.description"));
        ((TitledBorder) parentOfEntryPanel.getBorder()).setTitle(resourceBundle.
        getString("title.adHocQueryParameters"));
    }
    
    public void clear() throws JAXRException {
    }
    
}
