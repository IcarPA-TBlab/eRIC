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

package it.cnr.icar.eric.client.ui.thin.components.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

/**
 * <p>Graph is a JavaBean representing a complete graph of {@link Node}s.</p>
 */

public class Graph {

    // ----------------------------------------------------------- Constructors

    // No-args constructor
    public Graph() {
        super();
    }


    // Constructor with specified root
    public Graph(Node root) throws JAXRException {
        setRoot(root);
    }
    
    /**
     * Constructor with Node and non-Select Node Depth
     *
     * @param root
     *   A root Node of this tree
     * @param nonSelectNodeDepth
     *   An int that specifies how many levels down from the root cannot be 
     *   selected by the user.  For example, a '2' means the user cannot 
     *   select nodes from the first two levels.  One reason could be that 
     *   these levels contain descriptive content that cannot be used by the
     *   Registry for querying.  The default is '0'.
     */
    public Graph(Node root, int nonSelectNodeDepth) throws JAXRException {
        setRoot(root);
        this.nonSelectNodeDepth = nonSelectNodeDepth;
    }


    // ------------------------------------------------------------- Properties


    /**
     * The collection of nodes that represent this hierarchy, keyed by name.
     */
    protected HashMap<String, Node> registry = new HashMap<String, Node>();

    // The root node
    private Node root = null;

    private int selectedNodeDepth = 0;
    
    private int nonSelectNodeDepth = 0;
    
    private static Log log = LogFactory.getLog(Graph.class);

    public Node getRoot() {
        return (this.root);
    }


    public void setRoot(Node root) throws JAXRException {
        setSelected(null);
        if (this.root != null) {
            removeNode(this.root);
        }
        if (root != null) {
            addNode(root);
        }
        root.setLast(true);
        this.root = root;
    }

    // support multiple selected nodes
    public Collection<Node> getSelected() {
        List<Node> selectedNodes = new ArrayList<Node>();
        Set<String> keys = registry.keySet();
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            Node node = registry.get(key);
            if (node.isSelected()) {
                selectedNodes.add(node);
            }
        }
        return selectedNodes;
    }

    public void setSelectedNodeDepth(int selectedNodeDepth) {
        this.selectedNodeDepth = selectedNodeDepth;
    }
    
    public int getSelectedNodeDepth() {
        return selectedNodeDepth;
    }

    public void setSelected(Node selected) throws JAXRException {
        if (!canNodeBeSelected(selected)) {
            return;
        }
        if (selected == root) {
            return;
        }
        if (selected == null) {
            clearSelectedNodes();
        } else {
            Node selectedNode = registry.get(selected.getId());
            if (selectedNode != null) {
                // toggle selected node off
                if (selectedNode.isSelected()) {
                    selectedNode.setSelected(false);
                } else { // toggle selected node on
                    selectedNode.setSelected(true);
                    // deselect all parent nodes in the selected node's path
                    Node node = selectedNode;
                    while (true) {
                        node = node.getParent();
                        if (node == null) {
                            break;
                        }
                        if (node.isSelected()) {
                            node.setSelected(false);
                        }
                    }
                    // deselect selected node, if any, in child node hierarchy
                    deselectChildNodes(selectedNode);
                }   
            }
        }
    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Find and return the named {@link Node} if it exists; otherwise,
     * return <code>null</code>.  The search expression must start with a
     * slash character ('/'), and the name of each intervening node is
     * separated by a slash.</p>
     *
     * @param path Absolute path to the requested node
     */
    public Node findNode(String path) throws JAXRException {

        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException(path);
        }
        Node node = getRoot();
        
        path = path.substring(1);
        while (path.length() > 0) {
            String name = null;
            int slash = path.indexOf("/");
            if (slash < 0) {
                name = path;
                path = "";
            } else {
                name = path.substring(0, slash);
                path = path.substring(slash + 1);
            }
            node = node.findChild(name);
            if (node == null) {
                return (null);
            }
        }
        return (node);

    }


    /**
     * Register the specified node in our registry of the complete tree.
     *
     * @param node The <code>Node</code> to be registered
     *
     * @throws IllegalArgumentException if the name of this node
     *                                  is not unique
     */
    protected void addNode(Node node) throws JAXRException, IllegalArgumentException {

        synchronized (registry) {
            String id = node.getId();
            if (registry.containsKey(id)) {
                /*
                throw new IllegalArgumentException("Name '" + name +
                                                   "' is not unique");
                 */
                log.trace(WebUIResourceBundle.getInstance().getString("message.NodeNameIsNotUniqueItWillBeIgnored", new Object[]{id}));
                return;
            }
            node.setGraph(this);
            registry.put(id, node);
        }

    }


    /**
     * Deregister the specified node, as well as all child nodes of this
     * node, from our registry of the complete tree.  If this node is not
     * present, no action is taken.
     *
     * @param node The <code>Node</code> to be deregistered
     */
    void removeNode(Node node) {

        synchronized (registry) {
            Iterator<?> nodeItr = node.getChildren();
            while (nodeItr.hasNext()) {
                removeNode((Node) nodeItr.next());
            }
            node.setParent(null);
            node.setGraph(null);
            if (node == this.root) {
                this.root = null;
            }
        }

    }


    /**
     * Return <code>Node</code> by looking up the node registry.
     *
     * @param nodename Name of the <code>Node</code> to look up.
     */
    public Node findNodeByName(String nodename) {

        synchronized (registry) {
            return (registry.get(nodename));
        }
    }
    
    private boolean canNodeBeSelected(Node selectedNode) {
    	return (selectedNode != null && selectedNode.getDepth() > nonSelectNodeDepth);
    	/*
        if (selectedNode != null && selectedNode.getDepth() > nonSelectNodeDepth) {
            return true;
        } else {
            return false;
        }
        */
    }
    
    public void deselectAllChildNodes() {
        root.setSelected(false);
        deselectChildNodes(root);
    }
    
    private void clearSelectedNodes() {
        @SuppressWarnings("unused")
		List<Object> selectedNodes = new ArrayList<Object>();
        Set<String> keys = registry.keySet();
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            Node node = registry.get(key);
            if (node.isSelected()) {
                node.setSelected(false);
            }
        }
    }
    
    
    private void deselectChildNodes(Node selectedNode) {
        Iterator<?> childItr = selectedNode.getChildren();
        while (childItr.hasNext()) {
            Node node = (Node)childItr.next();
            if (node.isSelected()) {
                node.setSelected(false);
            }
            deselectChildNodes(node);
        }
    }
    
    
}
