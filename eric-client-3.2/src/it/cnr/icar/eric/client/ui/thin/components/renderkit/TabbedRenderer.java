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


import it.cnr.icar.eric.client.ui.thin.components.components.PaneComponent;
import it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean;
import it.cnr.icar.eric.client.ui.thin.RegistryBrowser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import it.cnr.icar.eric.client.ui.thin.RegistryObjectBean;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

/**
 * <p>Render our associated {@link PaneComponent} as a tabbed control, with
 * each of its immediate child {@link PaneComponent}s representing a single
 * tab.  Measures are taken to ensure that exactly one of the child tabs is
 * selected, and only the selected child pane's contents will be rendered.
 * </p>
 */

public class TabbedRenderer extends BaseRenderer {


    private static Log log = LogFactory.getLog(TabbedRenderer.class);
    
    private String imageLocation = null;

    public void decode(FacesContext context, UIComponent component) {
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeBegin(" + component.getId() + ")");
        }

        // Render the outer border and tabs of our owning table
        String paneClass = (String) component.getAttributes().get("paneClass");
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<table");
        if (paneClass != null) {
            writer.write(" class=\"");
            writer.write(paneClass);
            writer.write("\"");
        }
        writer.write(" cellpadding=\"0\" cellspacing=\"0\">");        

    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeChildren(" + component.getId() + ")");
        }

    }


    @SuppressWarnings({ "unused", "unchecked" })
	public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeEnd(" + component.getId() + ")");
        }
        try {
            // Ensure that exactly one of our child PaneComponents is selected
            Iterator<?> kids = component.getChildren().iterator();
            PaneComponent firstPane = null;
            PaneComponent selectedPane = null;
            imageLocation = getImagesLocation(context);
            RegistryObjectBean rob = null;
            boolean supportsROB = true;
            String supportsROBStr = (String)component.getAttributes().get("supportsROB");
            if (supportsROBStr != null && supportsROBStr.equalsIgnoreCase("false")) {
                supportsROB = false;
            }
            String robCurrentPaneId = null;
            if (supportsROB) {
                Map<?, ?> sessionMap = FacesContext.getCurrentInstance()
                                                     .getExternalContext()
                                                     .getSessionMap();      
                RegistryObjectCollectionBean rocBean = 
                        (RegistryObjectCollectionBean)sessionMap.get("roCollection");
                rob = rocBean.getCurrentDrilldownRegistryObjectBean();               
                robCurrentPaneId = rob.getCurrentDetailsPaneId();
            }
            HashMap<Object, Object> selectedTabs = RegistryBrowser.getInstance().getSelectedTabs();
            String selectdTabId = (String)selectedTabs.get(component.getId());
            int n = 0;
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (!(kid instanceof PaneComponent)) {
                    continue;
                }
                PaneComponent pane = (PaneComponent) kid;
                n++;

                if (n==1) {
                    firstPane = pane;                  
                } 
                if (supportsROB && rob.isFirstAccess()) {
                    if (firstPane != null) {
                        firstPane.setRendered(true);
                        selectedPane = firstPane;
                        rob.setFirstAccess(false);
                        rob.setCurrentDetailsPaneId(null);
                        robCurrentPaneId = null;
                    }
                } else if (supportsROB && robCurrentPaneId != null) {
                    if (robCurrentPaneId.equals(pane.getId())) {
                        selectedPane = pane;
                        selectedPane.setRendered(true);
                    } else {
                        pane.setRendered(false);
                    }
                } else if (!supportsROB && selectdTabId != null) {                    
                    if(selectdTabId.equals(pane.getId())) {
                        selectedPane = pane;
                        selectedPane.setRendered(true);
                    }
                    else
                        pane.setRendered(false);
                } else if (pane.isRendered()) {
                    if (selectedPane == null) {
                        selectedPane = pane;
                    } else {
                        pane.setRendered(false);
                    }
                }
            }
            if (selectedPane == null) {
                firstPane.setRendered(true);
                selectedPane = firstPane;
            }

            // Render the labels for our tabs
            String selectedClass =
                (String) component.getAttributes().get("selectedClass");
            String unselectedClass =
                (String) component.getAttributes().get("unselectedClass");
            ResponseWriter writer = context.getResponseWriter();
            int percent;
            if (n > 0) {
                percent = 100 / n;
            } else {
                percent = 100;
            }

            String renderClass = unselectedClass;
            int count = 0;
            int wrapAfter = 4;
            try {
                if (supportsROB) {
                    wrapAfter = Integer.parseInt(ProviderProperties.getInstance().
                                        getProperty("eric.client.thinbrowser.wrapDetailTabAfter", "4"));
                }
            } catch(NumberFormatException nfe) {
                String message = WebUIResourceBundle.getInstance().getString("detailsTabWrapNotSet");
                log.error(message, nfe);
                message += " " + WebUIResourceBundle.getInstance().getString("registrySupport");
                FacesContext.getCurrentInstance()
                            .addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         message, 
                                                         null));
            }
            
            boolean selectedGroupInd = false;
            int startInd = 0;
            int endInd = wrapAfter - 1;
            int selectedStartWrap = startInd;
            int selectedEndWrap = endInd;

            ArrayList<PaneComponent> copyPane = new ArrayList<PaneComponent>();
            kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (!(kid instanceof PaneComponent)) {
                    continue;
                }


                copyPane.add((PaneComponent) kid);
                if (((PaneComponent) kid).isRendered()) {
                    selectedGroupInd = true;
                    selectedStartWrap = startInd;
                    selectedEndWrap = endInd;
                }
                
                count++;
                if ((count % wrapAfter) == 0) {
                    startInd += wrapAfter;
                    endInd += wrapAfter;
                }
            }
            
            startInd = selectedStartWrap;
            endInd = selectedEndWrap;
            ArrayList<PaneComponent> paneOrder = new ArrayList<PaneComponent>();
            if ((count-1) < endInd) {
                endInd = count -1;
            }
            
            for (int i=0; i < count; i++) {
                if (startInd <= i && i <= endInd) {
                    continue;
                }
                paneOrder.add(copyPane.get(i));
            }
            int lastWrapIndex = paneOrder.size();

            while (startInd <= endInd) {
                paneOrder.add(copyPane.get(startInd++));
            }

            String viewId = context.getViewRoot().getViewId();
            String actionURL = context.getApplication().
                                       getViewHandler().
                                       getActionURL(context, viewId);
            context.getExternalContext().encodeActionURL(actionURL);

            if (!supportsROB) {                    
                writer.write("<form id=\"TaskBarTabcontrol\" method=\"post\" action=\""); 
                writer.write(actionURL+"\" enctype=\"application/x-www-form-urlencoded\">");                                  
            }

            boolean wrap = true;
            count = 0;
            for (int i=0; i < paneOrder.size(); i++) {
                if ((i % wrapAfter) == 0 && i != 0 && wrap || i==lastWrapIndex) {
                    writer.write("</tr>\n");
                    writer.write("<tr><td width=\"100%\" class=\""+selectedClass+"\" colspan=\"");
                    writer.write("" + n);
                    writer.write("\" >");
                    writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" ");
                    writer.write("height=\"4\" width=\"1\">");
                    writer.write("</td></tr>\n");
                    writer.write("<tr><td width=\"100%\" bgcolor=\"#ffffff\" colspan=\"");
                    writer.write("" + n);
                    writer.write("\" >");
                    writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" ");
                    writer.write("height=\"4\" width=\"1\">");
                    writer.write("</td></tr>\n");
                    writer.write("<tr>\n");
                }
                if (i==lastWrapIndex) {
                    wrap = false;
                }

                PaneComponent pane = paneOrder.get(i);
                if (pane.isRendered() && (selectedClass != null)) {
                    renderClass = selectedClass;
                } else if (!pane.isRendered() && (unselectedClass != null)) {
                    renderClass = unselectedClass;
                }
                

                writer.write("<td width=\"");
                writer.write("" + percent);
                writer.write("%\"");
                writer.write(">");
                writer.write("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
                writer.write("<tbody>");
                writer.write("<tr>");
                writer.write("<td rowspan=\"3\">");
                writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" height=\"1\" width=\"4\">");
                writer.write("</td>");
                
                writer.write("<td rowspan=\"3\" valign=\"top\" class=\""+renderClass+"\">");
                
                writer.write("<img src=\""+imageLocation+"/tabNotch.gif\" alt=\"\">");
                writer.write("</td>");

		writer.write("<td class=\""+renderClass+"\">");

                writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" height=\"4\" width=\"1\">");
                writer.write("</td>");

                writer.write("<td class=\""+renderClass+"\" rowspan=\"3\">");

                writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" height=\"1\" width=\"8\">");
                writer.write("</td>");
                writer.write("</tr>");
                writer.write("<tr>");
                writer.write("<td class=\""+renderClass+"\" valign=\"bottom\" nowrap=\"nowrap\" width=\"100%\">");
                UIComponent facet = pane.getFacet("label");
                if (facet != null) {
                    if (pane.isRendered() && (selectedClass != null)) {
                        facet.getAttributes().put("paneTabLabelClass",
                                                  selectedClass);
                    } else if (!pane.isRendered() && (unselectedClass != null)) {
                        facet.getAttributes().put("paneTabLabelClass",
                                                  unselectedClass);
                    }
                    facet.encodeBegin(context);
                }
                writer.write("</td>");
                writer.write("</tr>");
                writer.write("</tbody>");
                writer.write("</table>");
                writer.write("</td>\n");
            }

            if (!supportsROB) {
                context.getApplication().getViewHandler().writeState(context);
                writer.write("</form>"); 
            }

            writer.write("<tr><td width=\"100%\" class=\""+selectedClass+"\" colspan=\"");
            writer.write("" + n);
            writer.write("\" >");
            writer.write("<img src=\""+imageLocation+"/nothing.gif\" alt=\"\" ");
            writer.write("height=\"4\" width=\"1\">");
            writer.write("</td></tr>\n");


            // Begin the containing element for the selected child pane
            String contentClass = (String) component.getAttributes().get(
                "contentClass");
            writer.write("<tr><td width=\"100%\" colspan=\"");
            writer.write("" + n);
            writer.write("\"");
            if (contentClass != null) {
                writer.write(" class=\"");
                writer.write(contentClass);
                writer.write("\"");
            }
            writer.write(">\n");

            // Render the selected child pane
            kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                PaneComponent kid = (PaneComponent) kids.next();
                kid.encodeBegin(context);
                if (kid.getRendersChildren()) {
                    kid.encodeChildren(context); // We know Pane does this
                }
                kid.encodeEnd(context);
            }

            // End the containing element for the selected child pane
            writer.write("\n</td></tr>\n");

            // Render the ending of our owning element and table
            writer.write("</table>\n");
        } catch (Throwable t) {
            String message = WebUIResourceBundle.getInstance().getString("detailsPanelNotInitialized");
            log.error(message, t);
            message += " " + WebUIResourceBundle.getInstance().getString("registrySupport");
            FacesContext.getCurrentInstance()
                        .addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                     message, 
                                                     null));
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
            images = "/images/tabs";
        }
        sb.append(images);
        return (sb.toString());
    }
}
