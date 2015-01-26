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

package it.cnr.icar.eric.client.ui.thin.components.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.context.FacesContext;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.common.UIUtility;
import it.cnr.icar.eric.client.ui.thin.QueryBean;
import it.cnr.icar.eric.client.ui.thin.RegistryBrowser;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;
import it.cnr.icar.eric.common.CanonicalSchemes;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectOne;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectItems;
import javax.faces.component.UICommand;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.StateHolder;
import javax.faces.webapp.UIComponentTag;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import it.cnr.icar.eric.client.ui.thin.components.model.Graph;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;
import it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl;

import it.cnr.icar.eric.client.ui.common.conf.bindings.ConfigurationType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ParameterType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SlotType1;
import it.cnr.icar.eric.client.ui.common.conf.bindings.SlotListType;
//import it.cnr.icar.eric.client.ui.common.conf.bindings.Value;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ValueListType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ObjectRefType;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

/**
 * Component wrapping a {@link Graph} object that is pointed at by the
 * a value binding reference expression.  This component supports
 * the processing of a {@link ActionEvent} that will toggle the expanded
 * state of the specified {@link Node} in the {@link Graph}.
 */

public class QueryPanelComponent extends HtmlPanelGrid implements StateHolder {

    private static Log log = LogFactory.getLog(QueryPanelComponent.class);
    private static final String COMPONENT_RENDERER = "QueryPanel";
    private static final String COMPONENT_FAMILY = "QueryPanel";
    
    @SuppressWarnings("unused")
	private Object value = null;
    private boolean isInitialized = false;    
    private int currentComponentId = 0; 
    /* This map stores ClassificationSchemes by their UUID specified in the 
     * config.xml file.
     */
    private Map<String, RegistryObject> classSchemeRegistry = null;    
    private QueryBean currentQuery = null;
    private boolean menuDisplayTree = false;
    /*
     * This map stores the panels (HTMLPanelGrid) by using the query id as the key
     */
    private HashMap<String, HtmlPanelGrid> panelMap = null;
    private HashMap<String, AdhocQueryImpl> adhocQueryMap = null;
    private ArrayList<String> storeBasicQueryIds = null;
    private String storeBasicQueryId = ProviderProperties.getInstance()
                                                         .getProperty("jaxr-ebxml.thin.defaultQueryPanel",
                                                                      "urn:oasis:names:tc:ebxml-regrep:query:FindAllMyObjects");


    public QueryPanelComponent() {
        super();        
        
        setRendererType(COMPONENT_RENDERER);
    }
    
    /*
     * This method is used to construct the tree of query panels (HTMLPanelGrids)
     */
    public void init() throws Exception {
        getQueryPanels();
        getQueries();
    }
    
    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getRendererType() {
        return COMPONENT_RENDERER;
    }
   
  

    public Object saveState(FacesContext context) {

        Object values[] = new Object[10];
        values[0] = super.saveState(context);
        values[1] = panelMap;
        values[2] = new Integer(currentComponentId);
        values[3] = classSchemeRegistry;
        values[4] = currentQuery;
        values[5] = storeBasicQueryId;
        values[6] = storeBasicQueryIds;
        values[7] = new Boolean(menuDisplayTree);
        values[8] = adhocQueryMap;
        // Save additional state for this component
        // Into additional values in the array.
        //values[7] = saveAttachedState(context, validators);
        //values[8] = saveAttachedState(context, validatorBinding);
        //values[9] = saveAttachedState(context, valueChangeMethod);
        return (values);

    }


    @SuppressWarnings("unchecked")
	public void restoreState(FacesContext context, Object state) {

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        panelMap = (HashMap<String, HtmlPanelGrid>)values[1];
        currentComponentId = ((Integer)values[2]).intValue();
        classSchemeRegistry = (Map<String, RegistryObject>)values[3];
        currentQuery = (QueryBean)values[4];
        storeBasicQueryId = (String)values[5];
        storeBasicQueryIds = (ArrayList<String>)values[6];
        menuDisplayTree = ((Boolean)values[7]).booleanValue();
        adhocQueryMap = (HashMap<String, AdhocQueryImpl>)values[8];
        ValueBinding vb = 
                context.getApplication().createValueBinding("#{searchPanel.queryComponent}");
        vb.setValue(context, this);
    }
    
  
    /**
     * This method determines if this component has been initialized by 
     * inspecting the this.value reference.  If it is null, this method returns
     * false. If the reference is not null, it returns true.  Since this 
     * component cannot create its children until the value binding 
     * expression has been obtained from the tag handler, this component's 
     * rendered needs to know if it should render the children explicitly.
     * If this method returns false, the rendered will call encodeChildren on 
     * all the child components of this class.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * <p>Sets the <code>value</code> property of the <code>UICommand</code>.
     * This is most often rendered as a label.</p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /*
     * This method is used to get the web ui's query panels. Instantiate the
     * default or configured default panel only to improve preformance.
     */
    public void getQueryPanels() throws Exception {
        createQueryPanel(getCurrentQuery().getQueryId());
        renderQueryPanel(getCurrentQuery().getQueryId());
    }
    
    @SuppressWarnings({ "unused", "unchecked" })
	public void createQueryPanel(String queryId) throws Exception {
        ConfigurationType cfg = UIUtility.getInstance().getConfigurationType();
        List<QueryType> configuredQueries = cfg.getQuery();
        Iterator<QueryType> iter = configuredQueries.iterator();
        StringBuffer whereClause = null;
        for (int i = 1; iter.hasNext(); i++) {
            int numConfiguredQueries = configuredQueries.size();
            it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query = 
                iter.next();
            
            if (query.getAdhocQueryRef().getId().equalsIgnoreCase(queryId)) {
                HtmlPanelGrid childPanel = createQueryPanel(query);
                getChildren().add(childPanel);
                break;
            }
        }
    }
    
    @SuppressWarnings({ "unused", "unchecked" })
	private HtmlPanelGrid createQueryPanels() {
        HtmlPanelGrid panel = new HtmlPanelGrid();
        panel.setId(getCurrentComponentId());
        ConfigurationType cfg = UIUtility.getInstance().getConfigurationType();
        List<QueryType> configuredQueries = cfg.getQuery();
        Iterator<QueryType> iter = configuredQueries.iterator();
        StringBuffer whereClause = null;
        for (int i = 1; iter.hasNext(); i++) {
            int numConfiguredQueries = configuredQueries.size();
            it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query = 
                iter.next();
            HtmlPanelGrid childPanel = createQueryPanel(query);
            panel.getChildren().add(childPanel);
        }
        return panel;
    }
    
    @SuppressWarnings("unchecked")
	private HtmlPanelGrid createQueryPanel(it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query) {        
        // Look for cached panel first
        HtmlPanelGrid panel = getQueryPanel(query);
        if (panel == null) {
            panel = new HtmlPanelGrid();
            panel.setId(getCurrentComponentId());
            registerQueryPanel(query, panel);
            panel.setRendered(false);
            String queryId = query.getAdhocQueryRef().getId();
            Iterator<ParameterType> paramIter = query.getParameter().iterator();
            HtmlInputHidden classificationSchemeHidden = new  HtmlInputHidden();
            while (paramIter.hasNext()) {
                ParameterType parameter = paramIter.next();
                String dataType = parameter.getDatatype();
                UIComponentBase uiBase = null;
                if (dataType.equalsIgnoreCase("string")) {
                    uiBase = initializeString(panel, parameter, queryId);
                } else if (dataType.equalsIgnoreCase("taxonomyElement")) {
                    uiBase = initializeTaxonomyElement(panel, parameter, queryId);
                } else if (dataType.equalsIgnoreCase("boolean") && parameter.
                        getParameterName().equals("$generateClassificationLink")) {
                        classificationSchemeHidden.setId("ClassificationLink"+queryId.
                                substring(queryId.lastIndexOf(':')+1));
                        classificationSchemeHidden.setValue(parameter.getDefaultValue());
                        panel.getChildren().add(1,classificationSchemeHidden);
                } else if (dataType.equalsIgnoreCase("boolean")) {
                    uiBase = initializeBoolean(panel, parameter, queryId);
                }
                if (uiBase != null) {
                    HtmlOutputLabel label = initializeLabel(panel, parameter, uiBase);
                    HtmlOutputText breakAdd = initializeBreak();
                    panel.getChildren().add(label);     
                    panel.getChildren().add(uiBase);
                    panel.getChildren().add(breakAdd);
                }
            }
        }
        return panel;
    }
    
    public HashMap<String, HtmlPanelGrid> getPanelRegistry() {
        if (panelMap == null) {
            panelMap = new HashMap<String, HtmlPanelGrid>();
        }
        return panelMap;
    }
    
    public HashMap<String, AdhocQueryImpl> getAdhocQueryRegistry() {
        if (this.adhocQueryMap == null) {
            adhocQueryMap = new HashMap<String, AdhocQueryImpl>();
        }
        return adhocQueryMap;
    }
    
    public boolean getMenuDisplayTree() {
        return menuDisplayTree;
    }
    
    private ArrayList<String> getIdsForQueryPanelsThatDisplayClassSchemeTree() {
        if (storeBasicQueryIds == null) {
            storeBasicQueryIds = new ArrayList<String>();
        }
        return storeBasicQueryIds;
    }
    
    @SuppressWarnings("unchecked")
	public boolean renderQueryPanel(String queryId) {
        getChildren().clear();
        boolean panelRendered = false;
        menuDisplayTree = false;
        Iterator<Entry<String, HtmlPanelGrid>> panelItr = getPanelRegistry().entrySet().iterator();
        while (panelItr.hasNext()) {
            Entry<String, HtmlPanelGrid> entry = panelItr.next();
            String key = entry.getKey();
            HtmlPanelGrid panel = entry.getValue();
            if (key.equals(queryId)) {
                panel.setRendered(true);
                getChildren().add(panel);
                panelRendered = true;
                if (!panel.getChildren().isEmpty()){
                    if (panel.getChildren().get(1) instanceof HtmlInputHidden) {
                        this.menuDisplayTree = Boolean.valueOf(((String)((HtmlInputHidden)panel.getChildren().get(1)).getValue())).booleanValue();
                    }
                }                
            } else {
                panel.setRendered(false);
            }
        }
        return panelRendered;
    }
        
    private void setInitialQuery() throws Exception {
        // This UUID is for the default BusinessQuery:
        // urn:freebxml:registry:query:BusinessQuery
        String keyString = ProviderProperties.getInstance()
                                              .getProperty("jaxr-ebxml.thin.defaultQueryPanel",
                                                  "urn:freebxml:registry:query:BusinessQuery");
        
        currentQuery = getQueries().get(keyString);

        if (currentQuery == null) {
            // if the jaxr-ebxml.thin.defaultQueryPanel property is not set, and
            // the default BusinessQuery does not exist, load first adhoc query
            Collection<QueryBean> values = getQueries().values();
            currentQuery = values.iterator().next();            
        }
    }
        
    private HtmlPanelGrid getQueryPanel(it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query) {
        String id = query.getAdhocQueryRef().getId();
        HtmlPanelGrid panel = getPanelRegistry().get(id);
        return panel;
    }
    
    private void registerQueryPanel(it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query, HtmlPanelGrid panel) {
        ObjectRefType ref = query.getAdhocQueryRef();
        String id = ref.getId();
        getPanelRegistry().put(id, panel);
        panel.setRendered(false);
    }
    
     /*
      * Get the list of QueryBeans representing stored queries in the registry.
      */
    @SuppressWarnings("unchecked")
	public Map<String, QueryBean> getQueries() throws Exception {
        // Look for cached queries map in the http request context
        Map<String, QueryBean> queries = (Map<String, QueryBean>)FacesContext.getCurrentInstance()
                                       .getExternalContext()
                                       .getRequestMap()
                                       .get("adhocqueries");
        if (queries == null) {
            queries = new TreeMap<String, QueryBean>();          
            Iterator<QueryType> itr = UIUtility.getInstance().getConfigurationType().getQuery().iterator();            
            while (itr.hasNext()) {
                it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query = 
                    itr.next();
                AdhocQueryImpl ahq = getAdhocQuery(query);
                // TODO throw exception if ahq is null?
                if (ahq != null) {
                    ObjectRefType ref = null;
                    try {
                        QueryBean bean = new QueryBean(query, ahq);
                        ref = query.getAdhocQueryRef();
                        queries.put(ref.getId(), bean);
                    } catch (Throwable t) {
                        log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotCreateQueryBeanWithId", new Object[]{ref.getId()}));
                    }
                }
            }     
            if (queries.size() == 0) {
                String message = WebUIResourceBundle.getInstance().getString("message.error.noAdHocQueriesFound");
                QueryBean queryBean = new QueryBean(message, null, "Blank", "$dummy");
                queries.put(message, queryBean);
            }
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestMap()
                        .put("adhocqueries", queries);
        }
        return queries;
    } 
    
    /*
     * Get the currently selected query.
     */
    public QueryBean getCurrentQuery() throws Exception {
        if (currentQuery == null) {
            setInitialQuery();
        }
        return currentQuery;
    }
    
    public void setCurrentQuery(QueryBean currentQuery) {
        this.currentQuery = currentQuery;
    }
        
    public void loadTypes(Collection<RegistryObject> concepts, List<SelectItem> types, String indent) {
        Iterator<RegistryObject> itr = concepts.iterator();
        while (itr.hasNext()) {
            Concept concept = (Concept)itr.next();
            SelectItem item = loadConceptItem(concept, indent);
            if (item != null) {
                types.add(item);
            }
            try {
                if (concept.getChildConceptCount() > 0) {
                    Collection<RegistryObject> childConcepts = 
                        sortChildConceptsByName(concept.getChildrenConcepts());
                    loadTypes(childConcepts, 
                              types, 
                              indent+"...");
                }
            } catch (JAXRException ex) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ErrorLoadingObjectTypeChildConcept"), ex);
            }
        }
    }
        
    private AdhocQueryImpl getAdhocQuery(it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query) 
        throws Exception {
        AdhocQueryImpl adhocQuery = null;
        ObjectRefType queryRef = query.getAdhocQueryRef();
        String queryId = queryRef.getId();
        adhocQuery = getAdhocQueryRegistry().get(queryId);
        if (adhocQuery == null) {
            adhocQuery = (AdhocQueryImpl)RegistryBrowser.getDQM()
                                                        .getRegistryObject(queryId, "AdhocQuery");
            getAdhocQueryRegistry().put(queryId, adhocQuery);
        }
        // TODO: create adhocQuery lookup table
        if (adhocQuery == null) {
            log.warn(WebUIResourceBundle.getInstance().getString("message.error.failedLoadingAdhocQuery.notFound",new String[] {queryId}));
        }
        return adhocQuery;
    }
        
    @SuppressWarnings("unchecked")
	private HtmlOutputLabel initializeLabel(HtmlPanelGrid panel, ParameterType parameter, UIComponentBase base) {
        String localizedName = null;
        try {
            Object obj = UIUtility.getInstance().convertToRimBinding(parameter.getName());
            InternationalStringImpl i18nStr = new InternationalStringImpl(RegistryBrowser.getBLCM(),
                          (org.oasis.ebxml.registry.bindings.rim.InternationalStringType)obj);
            localizedName = i18nStr.getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
                                                    
        } catch (Exception ex) {
            localizedName = "Unknown";
        }
        HtmlOutputLabel output = (HtmlOutputLabel)FacesContext.getCurrentInstance()
                                                              .getApplication()
                                                              .createComponent("javax.faces.HtmlOutputLabel");
        output.setId(getCurrentComponentId());
        output.setRendererType("javax.faces.Label");
        output.setValue(localizedName + WebUIResourceBundle.getInstance()
                                                            .getString("colon"));
        String baseId = base.getId();
        output.setFor(baseId);
        output.getAttributes().put("for", baseId);
        output.setId(baseId+"Label");
        output.setStyleClass("h3");
        return output;
    }
    
    @SuppressWarnings("unused")
	private UIOutput initializeDescription(HtmlPanelGrid panel, it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query) 
        throws Exception {

        UIOutput output = (UIOutput)FacesContext.getCurrentInstance()
                                                .getApplication()
                                                .createComponent("javax.faces.Output");
        output.setId(getCurrentComponentId());
        output.setRendererType("javax.faces.Label");
        String id = query.getAdhocQueryRef().getId();
        QueryBean bean = getQueries().get(id);
        output.setValue(bean.getDescription());
        return output;
    }
            
    private UIInput initializeString(HtmlPanelGrid panel, 
                                     ParameterType parameter, 
                                     String queryId) {
        UIInput input = (UIInput)FacesContext.getCurrentInstance()
                                             .getApplication()
                                             .createComponent("javax.faces.Input");
        input.setId(getCurrentComponentId());
        input.setRendererType("javax.faces.Text");
        String placeholderName = parameter.getParameterName();
        placeholderName = placeholderName.replace('.', '_');
        String value = "#{" + "searchPanel.queryComponent" + 
            ".currentQuery.parameters." + placeholderName + ".textValue}";
        ValueBinding vb = FacesContext.getCurrentInstance()
                                      .getApplication()
                                      .createValueBinding(value);
        input.setValueBinding("value", vb);
        String defaultValue = parameter.getDefaultValue();
        if (defaultValue != null) {
            input.setValue(defaultValue);
        }
        input.setId(it.cnr.icar.eric.common.Utility.fixIdentifier(queryId+placeholderName));
        return input;
    }
    
    private UISelectBoolean initializeBoolean(HtmlPanelGrid panel, 
                                              ParameterType parameter, 
                                              String queryId) {
        UISelectBoolean input = (UISelectBoolean)FacesContext.getCurrentInstance()
                                                             .getApplication()
                                                             .createComponent("javax.faces.SelectBoolean");
        input.setId(getCurrentComponentId());
        input.setRendererType("javax.faces.Checkbox");
        String placeholderName = parameter.getParameterName();
        placeholderName = placeholderName.replace('.', '_');
        String value = "#{" + "searchPanel.queryComponent" + 
            ".currentQuery.parameters." + placeholderName + ".booleanValue}";
        ValueBinding vb = FacesContext.getCurrentInstance()
                                      .getApplication()
                                      .createValueBinding(value);
        input.setValueBinding("value", vb);
        String defaultValue = parameter.getDefaultValue();
        if ("true".equalsIgnoreCase(defaultValue)) {
            input.setSelected(true);
        } else {
            // default to false
            input.setSelected(false);
        }
        input.setId(it.cnr.icar.eric.common.Utility.fixIdentifier(queryId+placeholderName));
        return input;
    }
 
    private UIComponentBase initializeTaxonomyElement(HtmlPanelGrid panel, 
                                                      ParameterType parameter,
                                                      String queryId) {
        UIComponentBase base = null;
        String domainId = getDomainId(parameter);
        if (domainId.equals(CanonicalSchemes.CANONICAL_OBJECT_TYPE_LID_ClassificationScheme)) {
            getIdsForQueryPanelsThatDisplayClassSchemeTree().add(queryId);
        } else {
            RegistryObject ro = null;
            try { 
                if (classSchemeRegistry == null) {
                    classSchemeRegistry = getClassSchemeRegistry();
                }
                ro = classSchemeRegistry.get(domainId);
                // If ro is a Concept, create drop down list box
                if (ro != null) {
                    base = initializeListbox(panel, parameter, ro, queryId);
                }                
            } catch (Throwable t) {
                log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotLoadObjectTypeClassScheme"), t);
            }            
        }
        return base;
    }
    
    @SuppressWarnings("unused")
	private Map<String, RegistryObject> getClassSchemeRegistry() {
        Map<String, RegistryObject> classSchemeRegistry = new HashMap<String, RegistryObject>();
        ConfigurationType cfg = UIUtility.getInstance().getConfigurationType();
        List<QueryType> configuredQueries = cfg.getQuery();
        Iterator<QueryType> iter = configuredQueries.iterator();
        for (int i = 1; iter.hasNext(); i++) {
            it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType query = 
                iter.next();
            Iterator<ParameterType> paramIter = query.getParameter().iterator();
            while (paramIter.hasNext()) {
                ParameterType parameter = paramIter.next();
                String dataType = parameter.getDatatype();
                if (dataType.equalsIgnoreCase("taxonomyElement")) {
                    String domainId = getDomainId(parameter);
                    if (! domainId.equals(CanonicalSchemes.CANONICAL_OBJECT_TYPE_LID_ClassificationScheme)) {
                        RegistryObject ro = classSchemeRegistry.get(domainId);
                        if (ro == null) {
                            try {
                                ro = RegistryBrowser.getDQM().getRegistryObject(domainId);
                            } catch (Throwable t) {
                                log.error(WebUIResourceBundle.getInstance().getString("couldNotLoadCachedData"), t);
                            }
                            classSchemeRegistry.put(domainId, ro);
                        }
                    }
                }
            }
        }
        return classSchemeRegistry;
    }
    
    private String getDomainId(ParameterType parameter) {
        String domainId = null;
        //Get the domain taxonomy element id
        SlotListType slotList = parameter.getSlotList();
        List<SlotType1> slots = slotList.getSlot();
        Iterator<SlotType1> slotsIter = slots.iterator();

        while (slotsIter.hasNext()) {
            SlotType1 slot = slotsIter.next();

            if (slot.getName().equalsIgnoreCase("domain")) {

                ValueListType valList = slot.getValueList();

                if (valList != null) {
                    List<String> values = valList.getValue();
                    Iterator<String> valuesIter = values.iterator();
                    //domainId = ((Value) valuesIter.next()).getValue();
                    domainId = valuesIter.next();
                    break;
                }
            }
        }
        return domainId;
    }

    @SuppressWarnings("unchecked")
	private UISelectOne initializeListbox(HtmlPanelGrid panel, 
                                   ParameterType parameter,
                                   RegistryObject ro,
                                   String queryId) 
        {
        UISelectOne input = (UISelectOne)FacesContext.getCurrentInstance()
                                                     .getApplication()
                                                     .createComponent("javax.faces.SelectOne");
        input.setId(getCurrentComponentId());
        input.setRendererType("javax.faces.Menu");
        List<SelectItem> selectItems = getSelectItems(ro);
        UISelectItems items = new UISelectItems();
        items.setId(getCurrentComponentId());
        items.setValue(selectItems);
        input.getChildren().add(items);
        String placeholderName = parameter.getParameterName();
        placeholderName = placeholderName.replace('.', '_');        
        String value = "#{" + "searchPanel.queryComponent" + 
            ".currentQuery.parameters." + placeholderName + ".textValue}";
        ValueBinding vb = FacesContext.getCurrentInstance()
                                      .getApplication()
                                      .createValueBinding(value);
        input.setValueBinding("value", vb);
        String defaultValue = parameter.getDefaultValue();
        if (defaultValue != null) {
            Iterator<SelectItem> itr = selectItems.iterator();
            while (itr.hasNext()) {
                SelectItem item = itr.next();
                String itemValue = (String)item.getValue();
                if (itemValue.indexOf(defaultValue) != -1) {
                    input.setValue(itemValue);
                    break;
                }
            }            
        }
        input.setId(it.cnr.icar.eric.common.Utility.fixIdentifier(queryId+placeholderName));
        return input;
    }
        
    private List<SelectItem> getSelectItems(RegistryObject ro) {
        List<SelectItem> types = null;
        String objectId = null;
        try {
            objectId = ro.getKey().getId();
            types = new ArrayList<SelectItem>();
            if (ro instanceof ClassificationScheme) {
                Object obj = " ";
                String label = ro.getName().getValue();
                if (label == null) {
                    String message = WebUIResourceBundle.getInstance()
                                                        .getString("valueNotFound",
                                                                   new Object[]{ro.getKey().getId()});
                    log.error(message);
                    label = message;
                }
                types.add(new SelectItem(obj, label));
                Collection<RegistryObject> concepts = getConcepts(ro);
                loadTypes(concepts, types, "...");
            } else {
                Collection<RegistryObject> concepts = getConcepts(ro);
                loadTypes(concepts, types, "");
            }                       
        } catch (Throwable t) {
            log.error(WebUIResourceBundle.getInstance().getString("message.CouldNotLoadClassificationNodesWithId", new Object[]{objectId}), t);
        }   
        return types;
    }
    
    private Collection<RegistryObject> sortChildConceptsByName(Collection<?> childConcepts) {
        TreeMap<String, RegistryObject> treeMap = new TreeMap<String, RegistryObject>();
        Iterator<?> itr = childConcepts.iterator();
        while (itr.hasNext()) {
            Concept concept = (Concept)itr.next();
            try {
                treeMap.put(concept.getValue(), concept);
            } catch (JAXRException ex) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.CouldNotGetNameFromConcept"), ex);
            }
        }
        return treeMap.values();
        
    }
    
    private SelectItem loadConceptItem(Concept concept, String indent) {
        SelectItem item = null;
        try {
            String name = indent + ((InternationalStringImpl)concept.getName()).getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
            if (name == null || name.indexOf("null") != -1) {
                name = indent + concept.getValue();
            }

            Object path = concept.getPath();
            item = new SelectItem(path, name);
        } catch (JAXRException ex) {
            log.error(WebUIResourceBundle.getInstance().getString("message.ErrorGettingDataFromConcept"), ex);
        }
        return item;
    }

    @SuppressWarnings("unchecked")
	private Collection<RegistryObject> getConcepts(RegistryObject ro) throws JAXRException {
        Collection<RegistryObject> concepts = null;
        if (ro instanceof ClassificationScheme) {
            ClassificationScheme scheme = (ClassificationScheme) ro;
            concepts = scheme.getChildrenConcepts();
        } if (ro instanceof Concept) {
            @SuppressWarnings("unused")
			Concept concept = (Concept) ro;
            concepts = new ArrayList<RegistryObject>();
            concepts.add(ro);
        }

        return concepts;
    }
        
    @SuppressWarnings({ "unused", "unchecked" })
	private GraphComponent initializeGraphComponent(HtmlPanelGrid panel, 
                                                    ParameterType parameter,
                                                    String queryId) {
        UIInput hidden = (UIInput)FacesContext.getCurrentInstance()
                                              .getApplication()
                                              .createComponent("javax.faces.Input");
        hidden.setId(getCurrentComponentId());
        hidden.setRendererType("javax.faces.Hidden");
        hidden.setId("expandTree");
        hidden.setValue("true");
        panel.getChildren().add(hidden);
        GraphComponent graphComponent = new GraphComponent();
        graphComponent.setRendererType("SearchRegistryMenuTree");
        graphComponent.setId(getCurrentComponentId());
        FacesContext context = FacesContext.getCurrentInstance();
        
        ValueBinding vb = null;
        String actionListener = "#{" + "searchPanel" + 
            ".classSchemeGraphBean.processGraphEvent}";
        if (UIComponentTag.isValueReference(actionListener)) {
            Class<?> args[] = {ActionEvent.class};
            MethodBinding mb = FacesContext.getCurrentInstance()
                                           .getApplication()
                                           .createMethodBinding(actionListener, args);
            graphComponent.setActionListener(mb);
        } else {
            Object params [] = {actionListener};
            throw new javax.faces.FacesException();
        }

        // if the attributes are values set them directly on the component, if
        // not set the ValueBinding reference so that the expressions can be
        // evaluated lazily.
        String styleClass = "tree-control";
        if (styleClass != null) {
            if (UIComponentTag.isValueReference(styleClass)) {
                vb = FacesContext.getCurrentInstance()
                                 .getApplication()
                                 .createValueBinding(styleClass);
                graphComponent.setValueBinding("styleClass", vb);
            } else {
                graphComponent.getAttributes().put("styleClass", styleClass);
            }
        }
        String selectedClass = "tree-control-selected";
        if (selectedClass != null) {
            if (UIComponentTag.isValueReference(selectedClass)) {
                vb = FacesContext.getCurrentInstance()
                                 .getApplication()
                                 .createValueBinding(selectedClass);
                graphComponent.setValueBinding("selectedClass", vb);
            } else {
                graphComponent.getAttributes().put("selectedClass",
                                                   selectedClass);
            }
        }
        String unselectedClass = "tree-control-unselected";
        if (unselectedClass != null) {
            if (UIComponentTag.isValueReference(unselectedClass)) {
                vb = FacesContext.getCurrentInstance()
                                 .getApplication()
                                 .createValueBinding(unselectedClass);
                graphComponent.setValueBinding("unselectedClass", vb);
            } else {
                graphComponent.getAttributes().put("unselectedClass",
                                                   unselectedClass);
            }
        }

        String immediate = "false";
        if (immediate != null) {
            if (UIComponentTag.isValueReference(immediate)) {
                vb = FacesContext.getCurrentInstance()
                                 .getApplication()
                                 .createValueBinding(immediate);
                graphComponent.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                graphComponent.setImmediate(_immediate);
            }
        }

        String value = "#{" + "searchPanel" + 
            ".classSchemeGraphBean.treeGraph}";
        if (value != null) {
            // if the value is not value reference expression, we need
            // to build the graph using the node tags.
            if (UIComponentTag.isValueReference(value)) {
                vb = FacesContext.getCurrentInstance()
                                 .getApplication()
                                 .createValueBinding(value);
                graphComponent.setValueBinding("value", vb);
            }
        }
        
        // if there is no valueRef attribute set on this tag, then
        // we need to build the graph.
        if (value == null) {
            vb = FacesContext.getCurrentInstance()
                             .getApplication()
                             .createValueBinding("#{sessionScope.graph_tree}");
            graphComponent.setValueBinding("value", vb); 
           
            // In the postback case, graph exists already. So make sure
            // it doesn't created again.
            Graph graph = (Graph) (graphComponent).getValue();
            if (graph == null) {
                graph = new Graph();
                vb.setValue(context, graph);
            }
        }
        String placeholderName = parameter.getParameterName();
        placeholderName = placeholderName.replace('.', '_');
        String selectedValues = "#{" + "searchPanel.queryComponent" + 
            ".currentQuery.parameters." + placeholderName + ".listValue}";
        if (selectedValues != null) {
            graphComponent.getAttributes().put("selectedValues", selectedValues);
        }
        
        String action = "showSearchPanel";
        if (action != null) {
            if (action != null) {
                UICommand command = graphComponent;
                if (UIComponentTag.isValueReference(action)) {
                    MethodBinding mb = FacesContext.getCurrentInstance()
                                                   .getApplication()
                                                   .createMethodBinding(action, null);
                    command.setAction(mb);
                }else {
                    final String outcome = action;
                    MethodBinding mb = Util.createConstantMethodBinding(action);
                    command.setAction(mb);
                }
            }
        }
        graphComponent.setId(it.cnr.icar.eric.common.Utility.fixIdentifier(queryId+placeholderName));
        return graphComponent;
    }

    private HtmlOutputText initializeBreak () {
        HtmlOutputText output = new HtmlOutputText();
        output.setId(getCurrentComponentId());
        output.setValue(" ");
        return output;
    }
    
    private String getCurrentComponentId() {
        StringBuffer sb = new StringBuffer("SearchPanelBeanComponent_");
        sb.append(currentComponentId);
        currentComponentId++;
        return sb.toString();
    }
    
}
