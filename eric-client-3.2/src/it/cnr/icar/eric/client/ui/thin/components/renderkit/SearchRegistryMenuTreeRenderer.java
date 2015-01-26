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

package it.cnr.icar.eric.client.ui.thin.components.renderkit;


import it.cnr.icar.eric.client.ui.thin.components.components.GraphComponent;
import it.cnr.icar.eric.client.ui.thin.components.model.Graph;
import it.cnr.icar.eric.client.ui.thin.components.model.Node;
import it.cnr.icar.eric.client.ui.thin.components.model.RegistryObjectNode;
import it.cnr.icar.eric.client.xml.registry.infomodel.ClassificationSchemeImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ConceptImpl;

import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a tree control, with individual nodes expanded or contracted based on
 * their current status.</p>
 */

public class SearchRegistryMenuTreeRenderer extends SearchRegistryMenuBarRenderer {

    /**
     * The names of tree state images that we need.
     */
    public static final String IMAGE_HANDLE_DOWN_LAST = "handledownlast.gif";
    public static final String IMAGE_HANDLE_DOWN_MIDDLE = "handledownmiddle.gif";
    public static final String IMAGE_HANDLE_RIGHT_LAST = "handlerightlast.gif";
    public static final String IMAGE_HANDLE_RIGHT_MIDDLE = "handlerightmiddle.gif";
    public static final String IMAGE_LINE_LAST = "linelastnode.gif";
    public static final String IMAGE_LINE_MIDDLE = "linemiddlenode.gif";
    public static final String IMAGE_LINE_VERTICAL = "linevertical.gif";
    public static final String IMAGE_DOCUMENT = "document.gif";
    public static final String IMAGE_FOLDER = "folder.gif";
 
    String imageLocation = null;

    private static Log log = LogFactory.getLog(SearchRegistryMenuTreeRenderer.class);

    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        try {
            Graph graph = null;
            // Acquire the root node of the graph representing the menu
            graph = (Graph) ((GraphComponent) component).getValue();
            if (graph == null) {
                throw new FacesException(WebUIResourceBundle.getInstance().getString("excGraphNotLocated"));
            }
            Node root = graph.getRoot();
            if (root == null) {
                throw new FacesException(WebUIResourceBundle.getInstance().getString("excGraphNoRootNode"));
            }
            if (!root.hasChild()) {
                return; // Nothing to render
            }
            this.component = component;
            this.context = context;
            clientId = component.getClientId(context);
            imageLocation = getImagesLocation(context);

            treeClass = (String) component.getAttributes().get("graphClass");
            selectedClass = (String) component.getAttributes().get("selectedClass");
            unselectedClass =
                (String) component.getAttributes().get("unselectedClass");
            treeSelect = (String)component.getAttributes().get("treeSelect");

            showNoConcept = new Boolean(((String) component.getAttributes().get("showNoConcept"))).booleanValue();
            ResponseWriter writer = context.getResponseWriter();

            writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"");
            if (treeClass != null) {
                writer.write(" class=\"");
                writer.write(treeClass);
                writer.write("\"");
            }
            writer.write(">");
            writer.write("\n");

            int level = 0;
            encodeNode(writer, root, level, graph.getSelectedNodeDepth(), true);
            writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
            writer.write("</table>");
            writer.write("\n");
        } catch (Throwable t) {
            String message = WebUIResourceBundle.getInstance().getString("searchPanelNotInitialized");
            log.error(message, t);
            message += " " + WebUIResourceBundle.getInstance().getString("registrySupport");
            FacesContext.getCurrentInstance()
                        .addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                     message, 
                                                     null));
        }
    }


    protected void encodeNode(ResponseWriter writer, Node node,
                              int level, int width, boolean last)
        throws IOException {

        boolean showLink = false;
        if (node instanceof RegistryObjectNode) {
            if (((RegistryObjectNode)node).getRegistryObject() instanceof ClassificationSchemeImpl && "CS".equals(treeSelect) ||
                ((RegistryObjectNode)node).getRegistryObject() instanceof ConceptImpl && "CN".equals(treeSelect) || "CSANDCN".equals(treeSelect)) {
                showLink = true;
            }
        }
        // Render the beginning of this node
        writer.write("  <tr valign=\"middle\">");
      
        // Create the appropriate number of indents
        for (int i = 0; i < level; i++) {
            int levels = level - i;
            Node parent = node;
            for (int j = 1; j <= levels; j++)
                parent = parent.getParent();
            if (parent.isLast())
                writer.write("    <td></td>");
            else {
                writer.write("    <td><img src=\"");
                writer.write(imageLocation);
                writer.write("/");
                writer.write(IMAGE_LINE_VERTICAL);
                writer.write("\"");
                writer.write("alt=\"");
                writer.write("\" ");
                writer.write("border=\"0\"></td>");
            }
            writer.write("\n");
        }

        // Render the tree state image for this node. use the "onmousedown" event 
        // handler to track which node was clicked. The images are rendered
        // as links.
        writer.write("    <td>");
        if (node.hasChild()) {
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            writer.write("<a href=\"");
            try {
                writer.write(getSubmitScript(node.getPath(), context, true));
            } catch (Exception ex) {      
                String message = WebUIResourceBundle.getInstance()
                                                    .getString("message.errorGettingNodePath");
                FacesContext.getCurrentInstance()
                            .addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         message, 
                                                         null));
            }
            writer.write(" >");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isExpanded()) {
                writer.write(IMAGE_HANDLE_DOWN_LAST);
            } else {
                writer.write(IMAGE_HANDLE_RIGHT_LAST);
            }
            writer.write("\" border=\"0\">");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isExpanded() || node.hasChild()) {
                writer.write(IMAGE_FOLDER);
            } 
            writer.write("\" border=\"0\">");
            writer.write("</a>");
            writer.write("&nbsp;</td>");
        } else {
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLast()) {
                writer.write(IMAGE_LINE_LAST);
            } else {
                writer.write(IMAGE_LINE_MIDDLE);
            }
            writer.write("\" border=\"0\">");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLast() || node.isLeaf()) {
                writer.write(IMAGE_DOCUMENT);
            } else {
                writer.write(IMAGE_FOLDER);
            }	    
            writer.write("\" border=\"0\">");
            writer.write("&nbsp;</td>");
        }
        // Render the icon for this node (if any)
        writer.write("    <td colspan=\"");
//        writer.write(String.valueOf(width - level + 1));
        writer.write(String.valueOf(width + 1));
        writer.write("\">");
        if (node.getIcon() != null) {
            // Label and action link
            // Note: we assume that the links do not act as command button,
            // meaning they do not cause the form to be submitted.
            if (node.getAction() != null) {
                writer.write("<a href=\"");
                writer.write(href(node.getAction()));
                writer.write("\">");
            }
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            writer.write(node.getIcon());
            writer.write("\" ");
            
            writer.write("alt=\"");
            writer.write("\" ");
            
            writer.write(" border=\"0\">");
            if (node.getAction() != null) {
                writer.write("</a>");
            }
        }
        // Render the label for this node (if any) as link.
        if (node.getLabel() != null) {
            writer.write("   ");
            String labelStyle = null;
            if (node.isSelected() && (selectedClass != null)) {
                labelStyle = selectedClass;
            } else if (!node.isSelected() && (unselectedClass != null)) {
                labelStyle = unselectedClass;
            }
            if (node.isEnabled() && showLink) {
                // Note: we assume that the links do not act as command button,
                // meaning they do not cause the form to be submitted.
                // writer.write("<a href=\"");
                // writer.write(href(node.getAction()));
                // writer.write("\"");
                writer.write("<a href=\"");
                try {
                    writer.write(getSubmitScript(node.getPath(), context, false));
                } catch (Exception ex) {      
                    String message = WebUIResourceBundle.getInstance()
                                                        .getString("message.errorGettingNodePath");
                    FacesContext.getCurrentInstance()
                                .addMessage(null, 
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                             message, 
                                                             null));
                }
                if (labelStyle != null) {
                    writer.write(" class=\"");
                    writer.write(labelStyle);
                    writer.write("\"");
                }
                writer.write(">");
            } else if (labelStyle != null) {
                writer.write("<span class=\"");
                writer.write(labelStyle);
                writer.write("\">");
            }
            writer.write(node.getLabel());
            if (node.getLabel() != null && showLink) {
                writer.write("</a>");
            } else if (labelStyle != null) {
                writer.write("</span>");
            }
            if (node.isAnchored()) {
                writer.write("<a name=\"here\"></a>");
                node.setAnchor(false);
            }
        }
        writer.write("</td>");
        writer.write("\n");

        // Render the end of this node
        writer.write("  </tr>");
        writer.write("\n");

        // Render the children of this node
        if (node.isExpanded()) {
            Iterator<?> children = node.getChildren();
            @SuppressWarnings("unused")
			int lastIndex = (node.getChildCount()) - 1;
            int newLevel = level + 1;
            while (children.hasNext()) {
                Node nextChild = (Node) children.next();
                boolean lastNode = nextChild.isLast();
                if (showNoConcept && newLevel > 1){
                    break;
                } else {
                    encodeNode(writer, nextChild, newLevel, width, lastNode);
                }
            }
        }
    }


    /**
     * Returns the location of images by looking up the servlet context
     * init parameter. If parameter is not found, default to "/images".
     * Image location can be configured by setting this property.
     */
    protected String getImagesLocation(FacesContext context) {
        StringBuffer sb = new StringBuffer();

        // First, add the context path
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        sb.append(contextPath);

        // Next, add the images directory path
        Map<?, ?> initParameterMap = context.getExternalContext()
            .getInitParameterMap();
        String images = (String) initParameterMap.get("tree.control.images");
        if (images == null) {
            images = "/images/tree";
        }
        sb.append(images);
        return (sb.toString());
    }


}
