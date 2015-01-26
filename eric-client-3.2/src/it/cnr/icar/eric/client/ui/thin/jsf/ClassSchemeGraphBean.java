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
package it.cnr.icar.eric.client.ui.thin.jsf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.event.ActionEvent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.RegistryObject;

import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.client.ui.thin.RegistryBrowser;
import it.cnr.icar.eric.client.ui.thin.SearchPanelBean;
import it.cnr.icar.eric.client.ui.thin.ParameterBean;
import it.cnr.icar.eric.client.ui.thin.components.components.GraphComponent;
import it.cnr.icar.eric.client.ui.thin.components.model.Graph;
import it.cnr.icar.eric.client.ui.thin.components.model.Node;
import it.cnr.icar.eric.client.ui.thin.components.model.RegistryObjectNode;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.common.BindingUtility;

/**
 * <p>Backing file bean for TreeControl demo.</p>
 */

public class ClassSchemeGraphBean {

    private static Log log = LogFactory.getLog(ClassSchemeGraphBean.class);
    private Graph treeGraph = null;
    private Node root = null;
    @SuppressWarnings("unused")
	private BusinessQueryManager bqm;
    private DeclarativeQueryManager dqm;
    private SearchPanelBean searchPanelBean = null;
    private String selectedPath;
   
    /** 
    * Since a node may have child nodes, but child nodes must be displayed
    * lazily (only when requested), a child placeholder node is used instead to
    * indicated the possible existence of child nodes. This string is passed to a 
    * <code>it.cnr.icar.eric.client.ui.thin.components.model.Node</code>'s 
    * constructor as the Name parameter to flag it as a child placeholder node.
    * When a subsequent request for a node's children is made, the Node with
    * this placeholder name will be removed and replaced with the retrieved
    * child nodes, if any.
    */
    public static final String PLACEHOLDER_CHILD_NODE = "PLACEHOLDER_CHILD_NODE";
    private String holdSelectedNode = null;

    public ClassSchemeGraphBean() throws Exception {
         bqm = RegistryBrowser.getBQM();
         dqm = RegistryBrowser.getDQM();
    }
    
    public ClassSchemeGraphBean(SearchPanelBean searchPanelBean) throws Exception {
        this();
        this.searchPanelBean = searchPanelBean;
    }

    public Graph getTreeGraph() throws JAXRException, Exception {
        // Construct a preconfigured Graph lazily.
        if (treeGraph == null) {
            String localizedClassScheme = WebUIResourceBundle.getInstance()
                                                             .getString("ClassificationSchemes");
            root = new Node("ClassificationSchemes", localizedClassScheme, 
                                 "/registry/thin/DiscoveryPanel.jsp", null, true, false);
            treeGraph = new Graph(root);
            root.setHasChild(true);
        }
        return treeGraph;
    }
    
    public void clearSelectedNodes() {
        
        if (root.getChildCount() > 0) {            
            internalClearSelectedNodes(root.getChildren());
        }
    }
    
    public Collection<Node> getSelectedNodes() {
        Collection<Node> nodes = new ArrayList<Node>();
        if (root.getChildCount() > 0) {
            internalGetSelectedNodes(root.getChildren(), nodes);
        }
        return nodes;
    }

    private void internalGetSelectedNodes(Iterator<?> children, Collection<Node> nodes) {
        while (children.hasNext()) {
            Node node = (Node)children.next();
            if (node.isSelected()) {
                nodes.add(node);
            }        
            if (node.getChildCount() > 0) {
                internalGetSelectedNodes(node.getChildren(), nodes);
            }
        }
    }
    
    private void internalClearSelectedNodes(Iterator<?> children) {
        while (children.hasNext()) {
            Node node = (Node)children.next();
            node.setSelected(false);
            if (node.getChildCount() > 0) {
                internalClearSelectedNodes(node.getChildren());
            }
        }
    }
    
    public void setTreeGraph(Graph newTreeGraph) {
        this.treeGraph = newTreeGraph;
    }    

    @SuppressWarnings("unused")
	private ArrayList<ClassificationScheme> getClassificationSchemes() {
        ArrayList<ClassificationScheme> schemes = new ArrayList<ClassificationScheme>();
        
        return schemes;
    }
    
    public String getSelectedPath() {
        return selectedPath;
    }
    String treeType = null;
    public void processCustomGraphEvent(ActionEvent event) throws 
            Exception{
        GraphComponent component = (GraphComponent) event.getSource();
        if (this.treeType == null){
            treeType = (String) component.getAttributes().get("treeType");
            this.processGraphEvent(event);
        }else{
            this.processGraphEvent(event);
        }
            
    }
    
    /*
     * Processes the event queued on the graph component when a particular
     * node in the tree control is to be expanded or collapsed.
     */
    public void processGraphEvent(ActionEvent event) 
        throws JAXRException, Exception {
        try {
        if (log.isTraceEnabled()) {
            log.trace("TRACE: GraphBean.processGraphEvent ");
        }
        Graph graph = null;
        GraphComponent component = (GraphComponent) event.getSource();
        String path = (String) component.getAttributes().get("path");
        
        selectedPath = path;

        // Acquire the root node of the graph representing the menu
        graph = (Graph) component.getValue();
        if (graph == null) {
            if (log.isErrorEnabled()) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ERRORGraphCouldNotLocatedInScope"));
            }
        }
        // Toggle the expanded state of this node
        Node node = graph.findNode(path);
        if (node == null) {
            if (log.isErrorEnabled()) {
                log.error(WebUIResourceBundle.getInstance().getString("message.ERRORNodeCouldNotBeLocated", new Object[]{path}));
            }
            return;
        }
        
        node.setGraph(graph);
        if (node.getParent() == null) {
            // If user clicks on top class scheme node and the node is already
            // expanded, clear any selected concept nodes
            if (node.isExpanded()) {
                clearQueryParams(component);
                clearSelectedNodes();
            } else {
                loadChildNodesFromClassSchemes(node);
            }
        } else {
            if (node.isExpanded()) {
                clearQueryParams(component);
                clearSelectedNodes();
            } else {
                if (node instanceof RegistryObjectNode) {
                    loadChildNodesFromConcepts((RegistryObjectNode)node, true);
                } else {
                    log.error("Expecting RegistryObjectNode. Got Node");
                }
            }
        }
        
        node.setAnchor(true);
        String expandTree = (String) component.getAttributes().get("expandTree");
        if (expandTree == null || expandTree.equals("")) {
            expandTree = "true";
        }
        if (Boolean.valueOf(expandTree).booleanValue()) {   
            setSelectedNodeDepth(graph, node);
            boolean current = node.isExpanded();
            node.setExpanded(!current);
            if (!current) {
                if (node.getChildCount() > 0) {
                    internalCloseNodes(node);
                }
            }
        } else { // if you don't expand the node, select it instead      
            if(this.holdSelectedNode != null){
                 graph.findNode(this.holdSelectedNode).setSelected(false);   
            }
            graph.setSelected(node);
            this.holdSelectedNode = node.getPath();
            String selectedValuesBinding = (String)component.getAttributes()
                                                            .get("selectedValues");
            if (selectedValuesBinding.indexOf('$') != -1) {
                String[] tokens = selectedValuesBinding.split("\\.");
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].charAt(0) == '$') {
                        String placeholder = tokens[i];
                        ParameterBean paramBean = (ParameterBean)searchPanelBean
                                                   .getCurrentQuery()
                                                   .getParameters().get(placeholder);
                        Collection<Node> selectedNodes = graph.getSelected();
                        List<String> selectedNodePaths = new ArrayList<String>(selectedNodes.size());
                        Iterator<Node> itr = selectedNodes.iterator();
                        while (itr.hasNext()) {
                            RegistryObjectNode selectedNode = (RegistryObjectNode)itr.next();
                            String selectedNodePath = selectedNode.getRegistryObjectPath();                            
                            selectedNodePaths.add(selectedNodePath);
                        }
                        paramBean.setListValue(selectedNodePaths);
                        break;
                    }
                }
            }
        }
        } catch (Throwable t) {                      
            log.error(t);
            String message = WebUIResourceBundle.getInstance()
                                                    .getString("excDispRegistryObjects");
            FacesContext.getCurrentInstance()
                        .addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                     message+": "+t.getMessage(), 
                                                     null));
        }
    }
    
    public void internalCloseNodes(Node rootNode) {
        Iterator<?> kids = rootNode.getChildren();
        while (kids.hasNext()) {
            Node kid = (Node)kids.next();            
            kid.setExpanded(false);
            kid.setSelected(false);
            if (kid.getChildCount() > 0) {
                internalCloseNodes(kid);
            }
        }
    }
    
    private void clearQueryParams(GraphComponent component) throws Exception {
        String selectedValuesBinding = (String)component.getAttributes()
                                                            .get("selectedValues");
        if (selectedValuesBinding.indexOf('$') != -1) {
            String[] tokens = selectedValuesBinding.split("\\.");
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].charAt(0) == '$') {
                    String placeholder = tokens[i];
                    ParameterBean paramBean = (ParameterBean)searchPanelBean
                                               .getCurrentQuery()
                                               .getParameters().get(placeholder);
                    paramBean.setListValue(null);
                    break;
                }
            }
        }
    }
    
    @SuppressWarnings({ "static-access", "unchecked" })
	public Collection<?> getClassSchemes()  {
        Collection<?> classSchemes = null;
        try {
            HashMap<String, String> queryParams = new HashMap<String, String>();
            queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, BindingUtility.getInstance().CANONICAL_QUERY_GetClassificationSchemesById);
            Query query = ((DeclarativeQueryManagerImpl)dqm).createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse bResponse = ((DeclarativeQueryManagerImpl)dqm).executeQuery(query, queryParams);

            Collection<?> exceptions = bResponse.getExceptions();                    
            //TO DO: forward exceptions to an error JSP
            if (exceptions != null) {
                Iterator<?> iter = exceptions.iterator();
                Exception exception = null;
                @SuppressWarnings("unused")
				StringBuffer sb2 = new StringBuffer(WebUIResourceBundle.getInstance().getString("errorExecQuery"));
                while (iter.hasNext())  {
                    exception = (Exception) iter.next();
                }
                log.error("\n"+exception.getMessage());
            }
            // Filter hidden schemes here.
            classSchemes = filterHiddenSchemes(bResponse.getCollection());
        } catch (Throwable t) {
            log.error(t.getMessage());
        }
        return classSchemes;
    }
    
    private Collection<ClassificationScheme> filterHiddenSchemes(Collection<ClassificationScheme> classSchemes)
        throws JAXRException {
        Collection<ClassificationScheme> filteredSchemes = null;
        String hiddenSchemesProp = "jaxr-ebxml.registryBrowser.ConceptsTreeModel.hiddenSchemesList";
        String hiddenSchemesStr = ProviderProperties.getInstance()
                                                    .getProperty(hiddenSchemesProp);
        if (hiddenSchemesStr == null) {
            filteredSchemes = classSchemes;
        } else {
            String[] tokens = hiddenSchemesStr.split("\\|");
            if (tokens.length > 0) {                
                filteredSchemes = new ArrayList<ClassificationScheme>(classSchemes);
                for (int i = 0; i < tokens.length; i++) {
                    String csIdToFilter = tokens[i];
                    Iterator<?> itr = classSchemes.iterator();
                    while (itr.hasNext()) {
                        ClassificationScheme cs = (ClassificationScheme)itr.next();
                        String csId = cs.getKey().getId();
                        if (csIdToFilter.equalsIgnoreCase(csId)) {
                            filteredSchemes.remove(cs);
                        }
                    }
                }
            }
        }
        return filteredSchemes;
    }
    
    public void loadChildNodesFromClassSchemes(Node parentNode) 
        throws JAXRException {
        Collection<?> classSchemes = getClassSchemes();
        parentNode.clearChildren();
        if(this.treeType != null && this.treeType.equals("ExtrinsicObject")){
            parentNode.addChild(loadExtrinsicNodeFromClassScheme(classSchemes));
        }else if(this.treeType != null && this.treeType.equals("ExternalIdentifier")){
            Iterator<?> itr = classSchemes.iterator();
            while (itr.hasNext()) {
                ClassificationScheme scheme = (ClassificationScheme)itr.next();
                Node node = new RegistryObjectNode(scheme);
                node.setHasChild(scheme.getChildConceptCount() > 0);
                if(!node.hasChild()){
                    parentNode.addChild(node);
                }
            }
        }else{
            Iterator<?> itr = classSchemes.iterator();
            while (itr.hasNext()) {
                ClassificationScheme scheme = (ClassificationScheme)itr.next();
                Node node = new RegistryObjectNode(scheme);
                node.setHasChild(scheme.getChildConceptCount() > 0);
                parentNode.addChild(node);
            }
        }
        parentNode.sortChildren();
    }
    
    public Node loadExtrinsicNodeFromClassScheme(Collection<?> classSchemes) 
        throws JAXRException{
        Node node1 = null;
        Iterator<?> itr = classSchemes.iterator();
        while (itr.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme)itr.next();
            Node node = new RegistryObjectNode(scheme);
            if(node.getId().endsWith(CanonicalSchemes.
                    CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType)) {
               node.setHasChild(scheme.getChildConceptCount() > 0);
               node1 = node;
        
            }
        }
        return node1;
    }
    
    public void loadChildNodesFromConcepts(RegistryObjectNode parentNode, 
                                           boolean isFirstLevel)
        throws JAXRException {
        RegistryObject ro = parentNode.getRegistryObject();
        if (ro == null) {
            return; // is this a valid case?
        }
        Iterator<?> itr = null;
        if (ro instanceof ClassificationScheme) {
            itr = ((ClassificationScheme)ro).getChildrenConcepts().iterator();
        } else if (ro instanceof Concept){
            itr = ((Concept)ro).getChildrenConcepts().iterator();
        } else {
            log.error("Expected object: ClassificationScheme or Node");
            return;
        }
        Node node = null;
        parentNode.clearChildren();
        if(this.treeType != null && this.treeType.equals("ExtrinsicObject") && 
                !parentNode.getId().endsWith(CanonicalSchemes.
                CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType)){
            parentNode.addChild(loadExtrinsicChildNodeFromConcept(itr));
        }else{
            while (itr.hasNext()) {
                Concept concept = (Concept)itr.next();
                node = new RegistryObjectNode(concept);
                parentNode.addChild(node);
                int numChildren = concept.getChildConceptCount();
                node.setHasChild(numChildren>0);
            }
        }
        parentNode.sortChildren();
    }
 
    public Node loadExtrinsicChildNodeFromConcept(Iterator<?> itr) 
        throws JAXRException{
        Node node1 = null;
        while (itr.hasNext()) {
            Concept concept = (Concept)itr.next();
            Node node = new RegistryObjectNode(concept);
            if (node.getId().endsWith(CanonicalSchemes.
                    CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject)){
               node.setHasChild(concept.getChildConceptCount() > 0);
               node1 = node;
               this.treeType ="completed";
             }
        }
        return node1;
    }    
    
    private void setSelectedNodeDepth(Graph graph, Node node) {
        
        int currentDepth = graph.getSelectedNodeDepth();
        int newDepth = node.getDepth();
        if (currentDepth > newDepth) {
            newDepth = currentDepth;
        }
        
        graph.setSelectedNodeDepth(newDepth);
    }
}
