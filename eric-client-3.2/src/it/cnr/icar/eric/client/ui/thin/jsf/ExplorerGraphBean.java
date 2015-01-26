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
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.ClassificationScheme;

import javax.faces.event.ActionEvent;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.thin.RegistryBrowser;
import it.cnr.icar.eric.client.ui.thin.SearchPanelBean;
import it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean;
import it.cnr.icar.eric.client.ui.thin.components.components.GraphComponent;
import it.cnr.icar.eric.client.ui.thin.components.model.Graph;
import it.cnr.icar.eric.client.ui.thin.components.model.Node;
import it.cnr.icar.eric.client.ui.thin.components.model.RegistryObjectNode;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

/**
 * <p>Backing file bean for TreeControl demo.</p>
 */
///////////////////////////////////////////////////////////
public class ExplorerGraphBean {
    
    private BusinessQueryManager bqm;
    private DeclarativeQueryManager dqm;
    private ClassSchemeGraphBean classSchemeGraphBean;
    private RegistryPackage roPkg;
    private RegistryPackage csPkg;
    
    private static Log log = LogFactory.getLog(ExplorerGraphBean.class);
    Graph treeGraph = null;
    Collection<?> col=null;
    ArrayList<?> list=null;
    RegistryPackage pkg = null;
    RegistryObject ro = null;
    Node root=null;   
    private String expandTree = "true";
    private RegistryObject rootFolder = null;
    private String pathOfSelectedNode = null;
    
    public static final String PLACEHOLDER_CHILD_NODE="PLACEHOLDER_CHILD_NODE";
    
    
    public ExplorerGraphBean() throws Exception{
        bqm=RegistryBrowser.getBQM();
        dqm=RegistryBrowser.getDQM();
    }
    
    public Graph getTreeGraph()throws JAXRException {
        if (treeGraph == null) {
            try {
                rootFolder=bqm.getRegistryObject(CanonicalConstants.CANONICAL_ROOT_FOLDER_ID, "RegistryPackage");
            } catch (Throwable t) {
                log.error(t.getMessage());
                throw new JAXRException(t);
            }
            if(rootFolder instanceof RegistryPackage) {
                pkg=(RegistryPackage)rootFolder;
            }
            else {
                throw new JAXRException(WebUIResourceBundle.getInstance().getString("excRegistryPack")+ro.getClass());
            }
            root = new RegistryObjectNode(pkg);         
            
            treeGraph = new Graph(root);
            root.setHasChild(true);
        }
        return treeGraph;
    }
     
    public void setTreeGraph(Graph newTreeGraph) {
        this.treeGraph = newTreeGraph;
    }
    
    public String doTreeDisplay() {
        String outcome = null;
        RegistryBrowser.getInstance().setSessionExpired(false);
        if (expandTree == null || expandTree.equals("")) {
            expandTree = "true";
        }
        if (Boolean.valueOf(expandTree).booleanValue()) {
            outcome = "showExplorerPanel";
        } else {
            outcome = "searchSuccessful";
        }
        return outcome;
    }
    
    /*
     * Processes the event queued on the graph component when a particular
     * node in the tree control is to be expanded or collapsed.
     */
    
    /////////////////////////////////////////////////////
    public void processGraphEvent(ActionEvent event) 
        throws JAXRException, Exception {
        try {
            if (log.isTraceEnabled()) {
                log.trace("TRACE: GraphBean.processGraphEvent ");
            }
            Graph graph = null;
            GraphComponent component = (GraphComponent) event.getSource();
            String path = (String) component.getAttributes().get("path");
            
            // Acquire the root node of the graph representing the menu
            graph = (Graph) component.getValue();
            if (graph == null) {
                if (log.isErrorEnabled()) {
                    log.error("ERROR: Graph could not located in scope ");
                }
            }
            // Toggle the expanded state of this node
            RegistryObjectNode node = (RegistryObjectNode)graph.findNode(path);
            if (node == null) {
                if (log.isErrorEnabled()) {
                    log.error("ERROR: Node " + path + "could not be located. ");
                }
                return;
            }
            node.setGraph(graph);
            expandTree = (String) component.getAttributes().get("expandTree");
            if (expandTree == null || expandTree.equals("")) {
                expandTree = "true";
            }
            if (Boolean.valueOf(expandTree).booleanValue()) {
                // Expand the node and show any child nodes
                if (node.getRegistryObject() instanceof RegistryPackage) {
                    loadRegistryPackageChildNodes(node);                
                } else {           
                    refreshNode(node);
                    loadVirtualChildNodes(node);
                }
                boolean current = node.isExpanded();
                node.setExpanded(!current);
                if (!current) {
                    Node parent = node.getParent();
                    if (parent != null) {
                        if (parent != root) {
                            Iterator<?> kids = parent.getChildren();
                            while (kids.hasNext()) {
                                Node kid = (Node) kids.next();
                                if (kid != node) {
                                    kid.setExpanded(false);
                                }
                            }
                        }
                    }
                }
                node.setAnchor(true);       
            } else {
                pathOfSelectedNode = path;
                // Deselct all child nodes first to avoid multiple 
                // selections in the Explorer tree.
                graph.deselectAllChildNodes();                
                // If user clicks the node, render it bold.
                node.setSelected(true);
                // if you don't expand the node, execute a query to 
                // retrieve all RegistryObjects, except RegistryPackages, that are
                // associated with this ro
                loadRegistryObjects(node);
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
    
    /*
     * This method is used to refresh the selected node. This is needed in case
     * the ro has changed. When JAXR provider is notified of changes by the 
     * server, this refresh will not be needed.
     */
    private void refreshNode(RegistryObjectNode node) throws JAXRException {
        RegistryObject ro = node.getRegistryObject();
        String id = ro.getKey().getId();
        String type = null;
        if (ro instanceof ClassificationScheme) {
            type = "ClassificationScheme";
        } else if (ro instanceof Concept) {
            type = "ClassificationNode";
        } else if (ro instanceof RegistryPackage) {
            type = "RegistryPackage";
        }
        RegistryObject refreshedRo = null;
        if (type == null) {
            refreshedRo = dqm.getRegistryObject(id);
        } else {
            refreshedRo = dqm.getRegistryObject(id, type);
        }
        node.setRegistryObject(refreshedRo);
    }
    
    /*
     * What makes the node 'virtual' is that they are not RegistryPackages. They
     * are ClassSchemes and Concepts that are retrieved from the ClassSchemeGraphBean
     * This bean also displays classschemes and nodes in the Query Panels: so 
     * we gain code resuse by accomodating them in the Explorer.
     */
    private void loadVirtualChildNodes(RegistryObjectNode node) {
        try {
            getClassSchemeGraphBean().loadChildNodesFromConcepts(node, false);
        } catch (Throwable t) {
            log.error("Could not load child nodes", t);
        }
    }
    
    private ClassSchemeGraphBean getClassSchemeGraphBean() throws Exception {
        if (classSchemeGraphBean == null) {
            classSchemeGraphBean = new ClassSchemeGraphBean();
        }
        return classSchemeGraphBean;
    }

    private void loadRegistryPackageChildNodes(RegistryObjectNode parentNode) 
        throws JAXRException, Exception {
        parentNode.clearChildren();
        if (parentNode.getId().equals(CanonicalConstants.CANONICAL_ROOT_FOLDER_ID)) {
            // This method is used to load an in-memory RegistryPackage that 
            // contains all ClassificationSchemes
            loadClassificationSchemesRegistryPackage(parentNode);
        } else if (parentNode.getId().equals(CanonicalConstants.CANONICAL_USERDATA_FOLDER_ID)) {           
            // This method is used to load an in-memory RegistryPackage that
            // contains all RegistryObjects
            loadRegistryObjectsRegistryPackage(parentNode);
        }        
        if (parentNode.getRegistryObjectType() == null) {
            // If the RegistryObjectNode's registry object type is null, it means 
            // you are dealing with a RegistryPackage such as the top level
            // registry RP.
            if (parentNode.getId().equals("urn:oasis:names:tc:ebxml-regrep:RegistryPackage:ClassificationSchemes")) {
                // Load all ClassificationSchemes
                loadClassificationSchemes(parentNode);
            } else {
                // This method is used to load child RegistryPackages of the RP
                // contained in the parentNode
                loadChildRegistryPackages(parentNode);
            }
        } else {
            // This method is used to load all child nodes of a registry object 
            // type.
            loadRegistryObjectChildNodes(parentNode);
        }
    }
    
    /* This method is used to load an in-memory RegistryPackage that
     * contains all RegistryObjects
     */
    private void loadRegistryObjectsRegistryPackage(RegistryObjectNode parentNode) 
        throws JAXRException {
        if (roPkg == null) {
            // TODO: load into db using minDB target
            String localizedROsLabel = WebUIResourceBundle.getInstance()
                                          .getString("registryObjects");
            try {
                roPkg = RegistryBrowser.getBLCM().createRegistryPackage(localizedROsLabel);

                Key key = RegistryBrowser.getBLCM().createKey("urn:oasis:names:tc:ebxml-regrep:RegistryPackage:RegistryObject");
                (roPkg).setKey(key);          
        
            } catch (Exception ex) {
                throw new JAXRException(ex);
            }
 
        }
        Concept roClassNode = null;
        try {
            roClassNode = (Concept)RegistryBrowser.getDQM()
                                                  .getRegistryObject("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject", "ClassificationNode"); 
        } catch (Exception ex) {
            throw new JAXRException(ex);
        }                                                          
        RegistryObjectNode childNode = new RegistryObjectNode(roPkg, roClassNode);
        childNode.setHasChild(true);
        parentNode.addChild(childNode);
    }
    
    /*
     * This is a 'special' RegistryPackage that is a placeholder for
     * all ClassificationSchemes
     */
    private void loadClassificationSchemesRegistryPackage(RegistryObjectNode parentNode) 
        throws JAXRException {
        if (csPkg == null) {
            // TODO: load into db using minDB target?
            String localizedCSLabel = WebUIResourceBundle.getInstance()
                                                         .getString("ClassificationSchemes");
            try {
                csPkg = RegistryBrowser.getBLCM().createRegistryPackage(localizedCSLabel);

                Key key = RegistryBrowser.getBLCM().createKey("urn:oasis:names:tc:ebxml-regrep:RegistryPackage:ClassificationSchemes");
                (csPkg).setKey(key);
            } catch (Exception ex) {
                throw new JAXRException(ex);
            }
        }
        RegistryObjectNode childNode = new RegistryObjectNode(csPkg);            
        childNode.setHasChild(true);
        parentNode.addChild(childNode);
        if (childNode.getPath().equalsIgnoreCase(pathOfSelectedNode)) {
            childNode.setSelected(true);
        }
    }
    
    /* If the RegistryObjectNode's registry object type is not null, it means 
     * you are dealing with a RegistryPackage that is placeholder for
     * for a collection of ro's with a particular type. For example,
     * an RP with label 'Service' contains all ro's that have type 'Service'
     */
    private void loadRegistryObjectChildNodes(RegistryObjectNode parentNode) 
        throws JAXRException {
        Concept concept = parentNode.getRegistryObjectType();
        Iterator<?> itr = concept.getChildrenConcepts().iterator();
        RegistryObjectNode roNode = null;
        if (itr.hasNext()) {
            parentNode.clearChildren();
            RegistryPackage rOCPkg = null;
            while (itr.hasNext()) {
                Concept childConcept = (Concept)itr.next();
                try {
                    rOCPkg = RegistryBrowser.getBLCM()
                                            .createRegistryPackage(childConcept.getValue());
                } catch (Exception ex) {
                    throw new JAXRException(ex);
                }
                roNode = new RegistryObjectNode(rOCPkg, childConcept);
                parentNode.addChild(roNode);
                roNode.setHasChild(childConcept.getChildConceptCount()>0);
            }
            if (roNode != null) {
                roNode.setLast(true);
            }
        }
    }
    
    /*
     * This method is used to load all ClassificationSchemes under the 
     * ClassificationSchemes RegistryPackage. Note that this method leverages
     * existing code in the ClassSchemeGraphBean.
     */
    private void loadClassificationSchemes(RegistryObjectNode parentNode) {
        try {
            getClassSchemeGraphBean().loadChildNodesFromClassSchemes(parentNode);
        } catch (Exception ex) {
            log.error("Could not load class schemes", ex);
        }
    }

    /*
     * This method is used to load child RegistryPackages of the RP contained 
     * in the parentNode
     */
    private void loadChildRegistryPackages(RegistryObjectNode parentNode) 
        throws JAXRException {
        String id = parentNode.getId(); 

        String queryString = "SELECT child.* FROM RegistryPackage child, RegistryPackage parent, Association ass WHERE (parent.id = '" +
            id + "') AND (ass.associationType='" + CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
            "' AND ass.sourceObject = parent.id AND ass.targetObject = child.id) ";

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
        BulkResponse bResponse = dqm.executeQuery(query);

        Collection<?> exceptions = bResponse.getExceptions();                    
        //TO DO: forward exceptions to an error JSP       
        handleExceptions(exceptions);
        Collection<?> registryObjects = bResponse.getCollection();

        Node node = null;
        Iterator<?> itr = registryObjects.iterator();
        while (itr.hasNext()) {
            RegistryPackage childPackage = (RegistryPackage)itr.next();
            node = new RegistryObjectNode(childPackage);
            node.setHasChild(hasChildren(node.getId()));
            parentNode.addChild(node);
        }
        if (node != null) {
            node.setLast(true);            
            if (node.getPath().equalsIgnoreCase(pathOfSelectedNode)) {
                node.setSelected(true);
            }
        }
    }
    
    /*
     * This method is used to determine if a RegistryPackage has any child RPs
     * by quering for an assocation with type HasMember
     */
    private boolean hasChildren(String id) 
        throws JAXRException {
        boolean hasChildren = false;
        
        String queryString = "SELECT child.* FROM RegistryPackage child, RegistryPackage parent, Association ass WHERE (parent.id = '" +
            id + "') AND (ass.associationType='" + CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
            "' AND ass.sourceObject = parent.id AND ass.targetObject = child.id) ";
        
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
        BulkResponse bResponse = dqm.executeQuery(query);

        Collection<?> exceptions = bResponse.getExceptions();                    
        //TO DO: forward exceptions to an error JSP       
        handleExceptions(exceptions);
        Collection<?> registryObjects = bResponse.getCollection();
        if (id.equals(CanonicalConstants.CANONICAL_USERDATA_FOLDER_ID)) {
            hasChildren = true;
        } else {
            hasChildren = (registryObjects !=null && registryObjects.size() > 0);
        }
        return hasChildren;
    }

    /*
     * This method is used to obtain all the ROs associated with the 
     * RegistryPackage contained in the parentNode
     */
    public void loadRegistryObjects(RegistryObjectNode parentNode) 
        throws JAXRException, Exception {
        SearchPanelBean.getInstance().doClear();
        @SuppressWarnings("unused")
		int childCount = parentNode.getChildCount();
        String queryString = null;
        if (parentNode.getRegistryObject() instanceof RegistryPackage) {
            if ((parentNode).getRegistryObjectType() == null) {
                // If the registry object type is null, it means we need to look 
                // for child RegistryPackages. Note: this is a lower priority
                // candidate for server side caching
                String id = parentNode.getId();
                queryString = "SELECT ro.* FROM RegistryObject ro, RegistryPackage p, Association ass WHERE (p.id = '" +
                id + "') AND (ass.associationType='" + CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
                "' AND ass.sourceObject = p.id AND ass.targetObject = ro.id) ";
            } else {
                // If the registry object type is not null, it means we need to look 
                // query for all ROs with the type contained in the parent node
                String type = (parentNode).getRegistryObjectType().getPath();
                queryString = "SELECT ro.* FROM RegistryObject ro WHERE "+
                "objecttype IN "+
                "(SELECT id FROM ClassificationNode WHERE path LIKE '"+type+
                "' OR path LIKE '"+type+"/%')";
            }
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
            int maxResults = RegistryObjectCollectionBean.getInstance()
                                                             .getScrollerBean()
                                                             .getNumberOfSearchResults();
            maxResults = maxResults * 10;
            IterativeQueryParams iqParams = new IterativeQueryParams(0, maxResults);
            BulkResponse bResponse = 
                ((DeclarativeQueryManagerImpl)dqm).executeQuery(query, null, iqParams);

            Collection<?> exceptions = bResponse.getExceptions();                    
            //TO DO: forward exceptions to an error JSP
            handleExceptions(exceptions);
            RegistryObjectCollectionBean.getInstance()
                                        .handleRegistryObjects(bResponse);
        } else {
            String id = parentNode.getId();
            RegistryObject ro = null;
            if ((parentNode).getRegistryObject() instanceof ClassificationScheme) {
                ro = dqm.getRegistryObject(id, "ClassificationScheme");
            } else if((parentNode).getRegistryObject() instanceof Concept) {
                ro = dqm.getRegistryObject(id, "ClassificationNode");
            } else {
                ro = dqm.getRegistryObject(id);
            }
            Collection<RegistryObject> ros = new ArrayList<RegistryObject>();
            ros.add(ro);
            RegistryObjectCollectionBean.getInstance()
                                        .handleRegistryObjects(ros);
        }
        RegistryObjectCollectionBean.getInstance().setNode(parentNode);
    }
    
    @SuppressWarnings("unused")
	private void setSelectedNodeDepth(Graph graph, Node node) {
        
        int currentDepth = graph.getSelectedNodeDepth();
        int newDepth = node.getDepth();
        if (currentDepth > newDepth) {
            newDepth = currentDepth;
        }
        
        graph.setSelectedNodeDepth(newDepth);
    }
    
    private void handleExceptions(Collection<?> exceptions) throws JAXRException {
        if (exceptions != null) {
            Iterator<?> iter = exceptions.iterator();
            Exception exception = null;
            StringBuffer sb2 = new StringBuffer(WebUIResourceBundle.getInstance().getString("errorExecQuery"));
            while (iter.hasNext())  {
                exception = (Exception) iter.next();
            }      
            log.error("\n"+exception.getMessage());
            throw new JAXRException(sb2.toString());
        }
    }
    
}

