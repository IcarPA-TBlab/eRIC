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

import it.cnr.icar.eric.client.ui.thin.components.components.GraphComponent;
import it.cnr.icar.eric.client.ui.thin.components.model.Graph;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentBodyTag;
import javax.faces.component.UICommand;

import it.cnr.icar.eric.client.ui.thin.components.renderkit.Util;


/**
 * This class creates a <code>Graph</code> instance if there is no modelReference
 * attribute specified on the component, represented by this tag and
 * stores it against the attribute name "graph_tree" in session scope.
 */
public class GraphMenuTreeTag extends UIComponentBodyTag {

    protected String action = null;
    protected String actionListener = null;
    protected String styleClass = null;
    protected String selectedClass = null;
    protected String unselectedClass = null;
    protected String value = null;
    protected String immediate = null;
    protected String showNoConcept = null;
    protected String selectedValues = null;
    protected String treeSelect = null;
    protected String treeType = null;

   /**
     * method reference to handle actions
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * method reference to handle menu expansion and contraction events
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Value Binding reference expression that points to a Graph in scoped
     * namespace.
     */
    public void setValue(String newValue) {
        value = newValue;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of selected nodes. This can be value or a value binding reference
     * expression.
     */
    public void setSelectedClass(String styleSelected) {
        this.selectedClass = styleSelected;
    }


    /**
     * The CSS style <code>class</code> to be applied to the text
     * of unselected nodes. This can be value or a value binding reference
     * expression.
     */
    public void setUnselectedClass(String styleUnselected) {
        this.unselectedClass = styleUnselected;
    }


    /**
     * The CSS style <code>class</code> to be applied to the entire menu.
     * This can be value or a value binding reference
     * expression.
     */
    public void setStyleClass(String style) {
        this.styleClass = style;
    }


    /**
     * A flag indicating that the default ActionListener should execute
     * immediately (that is, during the Apply Request Values phase of the
     * request processing lifecycle, instead of waiting for Invoke
     * Application phase). The default value of this property must be false.
     * This can be value or a value binding reference expression.
     */
    public void setImmediate(java.lang.String immediate) {
        this.immediate = immediate;
    }
    
    /**
     * An expression indicating what method binding to bind for selected nodes
     */
    public void setSelectedValues(java.lang.String selectedValues) {
        this.selectedValues = selectedValues;
    }


    public String getComponentType() {
        return ("Graph");
    }


    public String getRendererType() {
        return ("MenuTree");
    }
    
    public String getSelectedValues() {
        return selectedValues;
    }
    
    public void setShowNoConcept(java.lang.String showNoConcept) {
        this.showNoConcept = showNoConcept;
    }
    
    public void setTreeSelect(String treeSelect) {
        this.treeSelect = treeSelect;
    }
    
    public void setTreeType(String treeType) {
        this.treeType = treeType;
    }
    @SuppressWarnings("unchecked")
	protected void setProperties(UIComponent component) {
        super.setProperties(component);
        
        FacesContext context = FacesContext.getCurrentInstance();
        ValueBinding vb = null;

        GraphComponent graphComponent = (GraphComponent) component;

        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class<?> args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                graphComponent.setActionListener(mb);
            } else {
                @SuppressWarnings("unused")
				Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }

        // if the attributes are values set them directly on the component, if
        // not set the ValueBinding reference so that the expressions can be
        // evaluated lazily.
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                vb = context.getApplication().createValueBinding(styleClass);
                graphComponent.setValueBinding("styleClass", vb);
            } else {
                graphComponent.getAttributes().put("styleClass", styleClass);
            }
        }
        if (selectedClass != null) {
            if (isValueReference(selectedClass)) {
                vb =
                    context.getApplication().createValueBinding(selectedClass);
                graphComponent.setValueBinding("selectedClass", vb);
            } else {
                graphComponent.getAttributes().put("selectedClass",
                                                   selectedClass);
            }
        }
        if (unselectedClass != null) {
            if (isValueReference(unselectedClass)) {
                vb =
                    context.getApplication().createValueBinding(
                        unselectedClass);
                graphComponent.setValueBinding("unselectedClass", vb);
            } else {
                graphComponent.getAttributes().put("unselectedClass",
                                                   unselectedClass);
            }
        }

        if (immediate != null) {
            if (isValueReference(immediate)) {
                vb = context.getApplication().createValueBinding(immediate);
                graphComponent.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                graphComponent.setImmediate(_immediate);
            }
        }
        
        if (showNoConcept != null) {
            if (isValueReference(showNoConcept)) {
                vb = context.getApplication().createValueBinding(showNoConcept);
                graphComponent.setValueBinding("showNoConcept", vb);
            } else {
                graphComponent.getAttributes().put("showNoConcept",showNoConcept);
            }
        }

        if (treeSelect != null) {
            if (isValueReference(treeSelect)) {
                vb = context.getApplication().createValueBinding(treeSelect);
                graphComponent.setValueBinding("treeSelect", vb);
            } else {
                graphComponent.getAttributes().put("treeSelect",treeSelect);
            }
        }
        
        if (treeType != null) {
            if (isValueReference(treeType)) {
                vb = context.getApplication().createValueBinding(treeType);
                graphComponent.setValueBinding("treeType", vb);
            } else {
                graphComponent.getAttributes().put("treeType",treeType);
            }
        }
        if (value != null) {
            // if the value is not value reference expression, we need
            // to build the graph using the node tags.
            if (isValueReference(value)) {
                vb = context.getApplication().createValueBinding(value);
                component.setValueBinding("value", vb);
            }
        }
        
        // if there is no valueRef attribute set on this tag, then
        // we need to build the graph.
        if (value == null) {
            vb =
                context.getApplication().createValueBinding(
                    "#{sessionScope.graph_tree}");
            component.setValueBinding("value", vb); 
           
            // In the postback case, graph exists already. So make sure
            // it doesn't created again.
            Graph graph = (Graph) ((GraphComponent) component).getValue();
            if (graph == null) {
                graph = new Graph();
                vb.setValue(context, graph);
            }
        }
        
        if (selectedValues != null) {
            graphComponent.getAttributes().put("selectedValues", selectedValues);
        }
        
        if (action != null) {
            if (action != null) {
                UICommand command = (UICommand)component;
                if (isValueReference(action)) {
                    MethodBinding mb = FacesContext.getCurrentInstance().getApplication().createMethodBinding(action, null);
                    command.setAction(mb);
                }else {
                    @SuppressWarnings("unused")
					final String outcome = action;
                    MethodBinding mb = Util.createConstantMethodBinding(action);
                    command.setAction(mb);
                }
            }
        }
         
    }


}
