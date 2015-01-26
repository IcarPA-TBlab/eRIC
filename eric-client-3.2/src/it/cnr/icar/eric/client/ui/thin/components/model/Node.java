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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;
import javax.xml.registry.JAXRException;

/**
 * <p>Node is a JavaBean representing a node in a tree control or menu.</p>
 */

public class Node {

    private String type = null;
    private boolean hasChild = false;
    // ----------------------------------------------------------- Constructors


    // No-args constructor
    public Node() {
        super();
    }


    // Full-up constructor
    public Node(String id, String label, String action, String icon,
                boolean enabled, boolean expanded) {
        setId(id);
        setLabel(label);
        setAction(action);
        setIcon(icon);
        setEnabled(enabled);
        setExpanded(expanded);
    }
    
    public Node(String id, String label, String action, String icon,
                boolean enabled, boolean expanded, String type) {
        this(id, label, action, icon, enabled, expanded);
        this.type = type;
    }
    
    public String getType() throws JAXRException {
        return type;
    }
    
    
    // ----------------------------------------------------- Instance Variables

    /**
     * Maintains a list of all the child nodes of this node.
     */
    private ArrayList<Node> children = new ArrayList<Node>();


    // ------------------------------------------------------------- Properties


    /**
     * The <code>Graph</code> instance representing the
     * entire tree.
     */
    protected Graph graph = null;


    public void setGraph(Graph graph) {
        this.graph = graph;
    }


    public Graph getGraph() {
        return graph;
    }

    public void clearChildren() {
        children.clear();
    }

    /*
     * Node action (context-relative URL triggered when node selected)
     */
    private String action = null;


    public String getAction() {
        return (this.action);
    }


    public void setAction(String action) {
        this.action = action;
    }


    private String icon = null;


    /*
     * Icon for this node if any.
     */
    public String getIcon() {
        return (this.icon);
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    /*
     * Returns the number of children of this Node.
     */
    public int getChildCount() {
        return (children.size());
    }


    /*
     * The nesting depth of this Node
     */
    private int depth = 1;


    public int getDepth() {
        return (this.depth);
    }


    /*
     * Is this node currently enabled (available for use by the user)?
     */
    private boolean enabled = false;


    public boolean isEnabled() {
        return (this.enabled);
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /*
     * Is this node currently expanded (in a tree control) or open (in a menu)?
     */
    private boolean expanded = false;


    public boolean isExpanded() {
        return (this.expanded);
    }


    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }


    /*
     * Node label (visible representation)
     */
    private String label = null;


    public String getLabel() {
        return (this.label);
    }


    public void setLabel(String label) {
        this.label = label;
    }


    /*
     * Node id
     */
    private String id = null;


    public String getId() throws JAXRException {
        return (this.id);
    }


    public void setId(String id) {
        this.id = id;
    }


    /*
     * The parent Node
     */
    private Node parent = null;


    public Node getParent() {
        return (this.parent);
    }


    void setParent(Node parent) {
        this.parent = parent;
        if (parent == null) {
            depth = 1;
        } else {
            depth = parent.getDepth() + 1;
        }
    }


    /*
     * Returns the absolute path of this node
     */
    public String getPath() throws JAXRException {

        Node parent = getParent();
        if (parent == null) {
            return ("/");
        }

        ArrayList<String> list = new ArrayList<String>();
        list.add(getId());
        while (parent != null) {
            list.add(0, parent.getId());
            parent = parent.getParent();
        }

        StringBuffer sb = new StringBuffer();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            if (i != 1) {
                sb.append('/');
            }
            if (i > 0) {
                sb.append(list.get(i));
            }
        }
        return (sb.toString());

    }


    /*
     * Is this node the currently selected one in the entire tree?
     */
    private boolean selected = false;


    public boolean isSelected() {
        return (this.selected);
    }


    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    /**
     * Is this the last node in the set of children for our parent node?
     */
    protected boolean last = false;


    public boolean isLast() {
        return (this.last);
    }


    public void setLast(boolean last) {
        this.last = last;
    }


    /**
     * Is this a "leaf" node (i.e. one with no children)?
     */
    public boolean isLeaf() {
        synchronized (children) {
            return (children.size() < 1);
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Adds the specified node as a child of this node and sets this node
     * as its parent.
     */
    public void addChild(Node child) throws JAXRException {
        if (child.getParent() != null) {
            throw new IllegalArgumentException(WebUIResourceBundle.getInstance().getString("excChildAlreadyHasParent"));
        }
        // if graph is created after all the nodes are created, then
        // graph will be null.
        if (graph != null) {
            graph.addNode(child);
        }
        synchronized (children) {
            @SuppressWarnings("unused")
			boolean hasChanged = children.add(child);
        }
        child.setParent(this);

    }


    /**
     * Adds the specified node as a child of this node at the
     * specifed offset and sets this node as its parent.
     */
    public void addChild(int offset, Node child) throws JAXRException {
        if (child.getParent() != null) {
            throw new IllegalArgumentException(WebUIResourceBundle.getInstance().getString("excChildAlreadyHasParent"));
        }
        if (graph != null) {
            graph.addNode(child);
        }
        synchronized (children) {
            children.add(offset, child);
        }
        child.setParent(this);
    }


    /**
     * Returns the node with the specified path by looking up
     * by child list. If node is not found returns <code>null</code>
     */
    public Node findChild(String path) throws JAXRException {
        int n = children.size();
        for (int i = 0; i < n; i++) {
            Node kid = children.get(i);
            if (path.equals(kid.getId())) {
                return (kid);
            }
        }
        return (null);
    }


    /**
     * Returns and <code>iterator</code> over the children of this node.
     */
    public Iterator<Node> getChildren() {
        return (children.iterator());
    }


    /**
     * Removes the specified node from the child list of this node.
     */
    public void removeChild(Node child) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException(
                WebUIResourceBundle.getInstance().getString("excChildNotRelated"));
        }
        synchronized (children) {
            children.remove(child);
        }
        if (graph != null) {
            graph.removeNode(child);
        }
        child.setParent(null);
    }


    /**
     * Returns a string representing a description of this node.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Node[name=");
        sb.append(id);
        if (label != null) {
            sb.append(",label=");
            sb.append(label);
        }
        if (action != null) {
            sb.append(",action=");
            sb.append(action);
        }
        sb.append(",enabled=");
        sb.append(enabled);
        sb.append(",expanded=");
        sb.append(expanded);
        sb.append(']');
        return sb.toString();
    }

    /*
     * To move focus of page to selected node.
     */
    private boolean anchor = false;


    public boolean isAnchored() {
        return (this.anchor);
    }


    public void setAnchor(boolean anchor) {
        this.anchor = anchor;
    }

    public boolean hasChild() {
        if (hasChild) {           
            // this could be set to 'true' without any loaded children
            return hasChild;
        } else  {
            // Check if there are any loaded children
            return hasChild = children.size()>0;
        }
    }
    
    /*
     * This method can be used to indicate that there are child nodes without
     * having to front load them.
     */
    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }
    
    /**
     * Sorts the children nodes by node label.
     */
    public void sortChildren() {
        // sort
        Collections.sort(children, nodeLabelComparator);

        // update 'last' flag
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (i == children.size() - 1) {
                node.setLast(true);
            } else {
                node.setLast(false);
            }
        }
    }
    
    // Comparator used for sorting classification schemes
    private Comparator<Node> nodeLabelComparator = new NodeLabelComparator();

    private class NodeLabelComparator implements Comparator<Node> {

        public int compare(Node o1, Node o2) {
            int result;
            
            Node node1 = o1;
            Node node2 = o2;

            String label1 = node1.getLabel();
            String label2 = node2.getLabel();
            result = label1.compareTo(label2);

            if (result == 0) {
                result = label1.hashCode() - label2.hashCode();
            }
            
            return result;
        }
    }    

}
