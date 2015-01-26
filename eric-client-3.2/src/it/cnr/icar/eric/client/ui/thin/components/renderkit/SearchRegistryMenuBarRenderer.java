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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a menu bar, where the children of the root node are treated as individual
 * menus, and grandchildren of the root node are the items on the main menus.
 * A real application would display things as links to expand and contract
 * items, including recursive submenus.</p>
 */

public class SearchRegistryMenuBarRenderer extends BaseRenderer {

    public static final String URL_PREFIX = "/faces";
    public final static String FORM_NUMBER_ATTR =
        "com.sun.faces.FormNumber";

    private static final Log log = LogFactory.getLog(SearchRegistryMenuBarRenderer.class);

    protected String treeClass = null;
    protected String selectedClass = null;
    protected String unselectedClass = null;
    protected String clientId = null;
    protected UIComponent component = null;
    protected FacesContext context = null;
    protected boolean showNoConcept = true;
    protected String treeSelect = null;


    @SuppressWarnings("unchecked")
	public void decode(FacesContext context, UIComponent component) {

        @SuppressWarnings("unused")
		Graph graph = null;
  
        // if a node was clicked queue an ActionEvent.
        Map<?, ?> requestParameterMap = context.getExternalContext().
            getRequestParameterMap();
        String path = (String) requestParameterMap.
            get(component.getClientId(context));
        String clientId = component.getClientId(context);
        String expandTreeId = clientId.substring(0, clientId.lastIndexOf(":"));
        expandTreeId = expandTreeId + ":expandTree";
        String expandTree = (String) requestParameterMap.get(expandTreeId);
        if (path != null && path.length() != 0) {
            component.getAttributes().put("path", path);
            component.getAttributes().put("expandTree", expandTree);
            component.queueEvent(new ActionEvent(component));
            if (log.isTraceEnabled()) {
                log.trace("ActionEvent queued on Graph component for " + path);
            }
        }
      
    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
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

        treeClass = (String) component.getAttributes().get("menuClass");
        selectedClass = (String) component.getAttributes().get("selectedClass");
        unselectedClass =
            (String) component.getAttributes().get("unselectedClass");
        treeSelect = (String)component.getAttributes().get("treeSelect");
        
        // Render the menu bar for this graph
        Iterator<?> menus = null;
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\"");
        if (treeClass != null) {
            writer.write(" class=\"");
            writer.write(treeClass);
            writer.write("\"");
        }
        writer.write(">");
        writer.write("\n");
        writer.write("<tr>"); // For top level menu bar
        menus = root.getChildren();
        while (menus.hasNext()) {
            Node menu = (Node) menus.next();
            writer.write("<th bgcolor=\"silver\" align=\"left\">");
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            if (menu.isEnabled()) {
                writer.write("<a href=\"");
                try {
                    writer.write(getSubmitScript(menu.getPath(), context, true));
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
                writer.write(menu.getLabel());
                writer.write("</a>");
            } else {
                writer.write(menu.getLabel());
            }
            writer.write("</th>");
        }
        writer.write("</tr>");

        writer.write("<tr>"); // For any expanded menu(s)
        menus = root.getChildren();
        while (menus.hasNext()) {
            Node menu = (Node) menus.next();
            writer.write(
                "<td bgcolor=\"silver\" align=\"left\" valign=\"top\">");

            if (menu.isExpanded()) {
                writer.write("<ul>");
                Iterator<?> items = menu.getChildren();
                while (items.hasNext()) {
                    Node node = (Node) items.next();
                    writer.write("<li>");
                    // Render the label for this node (if any) as a
                    // link is the node is enabled. 
                    if (node.getLabel() != null) {
                        writer.write("   ");
                        String labelStyle = null;
                        if (node.isSelected() && (selectedClass != null)) {
                            labelStyle = selectedClass;
                        } else if (!node.isSelected() &&
                            (unselectedClass != null)) {
                            labelStyle = unselectedClass;
                        }
                        if (node.isEnabled()) {
                            // writer.write("<a href=\"");
                            // Note: we assume that the links do not act as
                            // command button, meaning they do not cause the
                            // form to be submitted.
                            //writer.write(href(node.getAction()));
                            //writer.write("\"");
                            writer.write("<a href=\"");
                            try {
                                writer.write(getSubmitScript(menu.getPath(), context, false));
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
                        if (node.getLabel() != null) {
                            writer.write("</a>");
                        } else if (labelStyle != null) {
                            writer.write("</span>");
                        }
                    }
                    writer.write("</li>");
                    // PENDING - marker for submenu
                    // PENDING - expanded submenu?
                }
                writer.write("</ul>");
            } else {
                writer.write("&nbsp;");
            }
            writer.write("</td>");
        }
        writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
        writer.write("</table>");

    }


    /**
     * Returns a string that is rendered as the value of
     * onmousedown attribute. onmousedown event handler is used
     * the track the node that was clicked using a hidden field, then submits
     * the form so that we have the state information to reconstitute the tree.
     */
    protected String getSubmitScript(String path, FacesContext context) {
        UIForm uiform = getMyForm();
        String formClientId = uiform.getClientId(context);
        StringBuffer sb = new StringBuffer();
        sb.append("#\" onclick=\"document.forms['" + formClientId + "']['" +
                  clientId + "'].value='" + path +
                  "';document.forms['" + formClientId + "'].submit(); return false;\"");
        return sb.toString();
    }
    
    protected String getSubmitScript(String path, FacesContext context,
        boolean expandTree) {
        UIForm uiform = getMyForm();
        String formClientId = uiform.getClientId(context);
        StringBuffer sb = new StringBuffer();
        sb.append("#\" onclick=\"document.forms['" + formClientId + "']['" +
                  clientId + "'].value='" + path);
        sb.append("';document.forms['" + formClientId + "'][");
        StringBuffer sb4 = new StringBuffer("'");
        sb4.append(clientId.substring(0, clientId.lastIndexOf(":")));
        sb4.append(":expandTree'].value='"); 
        sb.append(sb4.toString());
        
        sb.append(expandTree);
        sb.append("';document.forms['" + formClientId + "'].submit(); return false;\"");
        return sb.toString();
    }


    /**
     * Returns the parent form of graph component.
     */
    protected UIForm getMyForm() {
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }
        return (UIForm) parent;
    }


    /**
     * Returns a string that is rendered as the value of
     * href attribute after prepending the contextPath if necessary.
     */
    protected String href(String action) {
        // if action does not start with a "/", it is considered an absolute
        // URL and hence don't prepend contextPath.
        if (action != null && (action.charAt(0) != '/')) {
            return action;
        }

        StringBuffer sb = new StringBuffer();
        if (action != null) {
            if (action.charAt(0) == '/') {
                sb.append(context.getExternalContext().getRequestContextPath());
            }
            sb.append(action);
        }
        return (sb.toString());
    }

}
