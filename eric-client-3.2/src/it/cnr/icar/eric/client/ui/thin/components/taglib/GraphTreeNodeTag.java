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

package it.cnr.icar.eric.client.ui.thin.components.taglib;

import it.cnr.icar.eric.client.ui.thin.components.model.Graph;
import it.cnr.icar.eric.client.ui.thin.components.model.Node;
import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;

import javax.xml.registry.JAXRException;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentBodyTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <B>GraphMenuNodeTag</B> builds the graph as the nodes are processed.
 * This tag creates a node with specified properties. Locates the parent of
 * this node by using the node name from its immediate parent tag of the
 * type GraphTreeNodeTag. If the parent could not be located, then the created
 * node is assumed to be root.
 */

public class GraphTreeNodeTag extends UIComponentBodyTag {

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables
    private static final Log log = LogFactory.getLog(GraphTreeNodeTag.class);
    private String name = null;
    private String icon = null;
    private String label = null;
    private String action = null;
    private boolean expanded;
    private boolean enabled = true;
   
    // Relationship Instance Variables

    //
    // Constructors and Initializers    
    //

    public GraphTreeNodeTag() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //
    /**
     * Name of the node
     */
    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return this.name;
    }


    /**
     * Should the node appear expanded by default
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }


    /**
     * Icon representing the node.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }


    /**
     * Label for the node.
     */
    public void setLabel(String label) {
        this.label = label;
    }


    /**
     * Should the node be enabled by default
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    /**
     * Link the node points to.
     */
    public void setAction(String action) {
        this.action = action;
    }


    public String getComponentType() {
        return null;
    }


    public String getRendererType() {
        return null;
    }


    //
    // Methods from FacesBodyTag
    //
    public int doStartTag() throws JspException {

        FacesContext context = FacesContext.getCurrentInstance();

        Graph graph = (Graph)
            ((Util.getValueBinding("#{sessionScope.graph_tree}").getValue(
                context)));
        // In the postback case, graph and the node exist already.So make sure
        // it doesn't created again.
        if (graph.findNodeByName(getName()) != null) {
            return BodyTag.EVAL_BODY_BUFFERED;
        }
        Node node = new Node(name, label, action, icon, enabled, expanded);
        
        // get the immediate ancestor/parent tag of this tag.
        GraphTreeNodeTag parentNode = null;
        try {
            parentNode = (GraphTreeNodeTag) TagSupport.findAncestorWithClass(
                this,
                GraphTreeNodeTag.class);
        } catch (Exception e) {
            System.out.println(
                "Exception while locating GraphTreeNodeTag.class");
        }    
        // if this tag has no parent that is a node tag,
        if (parentNode == null) {
            // then this should be root
            try {
                graph.setRoot(node);
            } catch (JAXRException ex) {
                log.error(ex);
            }
        } else {
            // add the node to its parent node.
            Node nodeToAdd = graph.findNodeByName(parentNode.getName());
            // this node should exist
            if (nodeToAdd != null) {
                try {
                    nodeToAdd.addChild(node);
                } catch (JAXRException ex) {
                    log.error(ex);
                }
            }
        }

        return BodyTag.EVAL_BODY_BUFFERED;
    }


    public int doEndTag() throws JspException {
        return (EVAL_PAGE);
    }

}
    

