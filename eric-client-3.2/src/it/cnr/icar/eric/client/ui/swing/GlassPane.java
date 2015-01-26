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
package it.cnr.icar.eric.client.ui.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * This is the glass pane class that intercepts screen interactions during system busy states.
 *
 * @author Yexin Chen
 */
public class GlassPane extends JComponent implements AWTEventListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7540172572783462226L;
	private Window theWindow;
    private Component activeComponent;
    
    /**
     * GlassPane constructor comment.
     * @param Container a
     */
    protected GlassPane(Component activeComponent) {
        // add adapters that do nothing for keyboard and mouse actions
        addMouseListener(new MouseAdapter() {
        });
        
        addKeyListener(new KeyAdapter() {
        });
        
        setActiveComponent(activeComponent);
    }
    
    /**
     * Receives all key events in the AWT and processes the ones that originated from the
     * current window with the glass pane.
     *
     * @param event the AWTEvent that was fired
     */
    public void eventDispatched(AWTEvent event) {
        Object source = event.getSource();
        
        // discard the event if its source is not from the correct type
        boolean sourceIsComponent = (event.getSource() instanceof Component);
        
        if ((event instanceof KeyEvent) && sourceIsComponent) {
            // If the event originated from the window w/glass pane, consume the event
            if ((SwingUtilities.windowForComponent((Component) source) == theWindow)) {
                ((KeyEvent) event).consume();
            }
        }
    }
    
    /**
     * Finds the glass pane that is related to the specified component.
     *
     * @param startComponent the component used to start the search for the glass pane
     * @param create a flag whether to create a glass pane if one does not exist
     * @return GlassPane
     */
    public synchronized static GlassPane mount(Component startComponent, boolean create) {
        RootPaneContainer aContainer = null;
        Component aComponent = startComponent;
        
        // Climb the component hierarchy until a RootPaneContainer is found or until the very top
        while ((aComponent.getParent() != null) && !(aComponent instanceof RootPaneContainer)) {
            aComponent = aComponent.getParent();
        }
        
        // Guard against error conditions if climb search wasn't successful
        if (aComponent instanceof RootPaneContainer) {
            aContainer = (RootPaneContainer) aComponent;
        }
        
        if (aContainer != null) {
            // Retrieve an existing GlassPane if old one already exist or create a new one, otherwise return null
            if ((aContainer.getGlassPane() != null) && (aContainer.getGlassPane() instanceof GlassPane)) {
                return (GlassPane) aContainer.getGlassPane();
            } else if (create) {
                GlassPane aGlassPane = new GlassPane(startComponent);
                aContainer.setGlassPane(aGlassPane);
                
                return aGlassPane;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Set the component that ordered-up the glass pane.
     *
     * @param aComponent the UI component that asked for the glass pane
     */
    private void setActiveComponent(Component aComponent) {
        activeComponent = aComponent;
    }
    
    /**
     * Sets the glass pane as visible or invisible. The mouse cursor will be set accordingly.
     */
    public void setVisible(boolean value) {
        if (value) {
            // keep track of the visible window associated w/the component
            // useful during event filtering
            if (theWindow == null) {
                theWindow = SwingUtilities.windowForComponent(activeComponent);
                if (theWindow == null) {
                    if (activeComponent instanceof Window) {
                        theWindow = (Window) activeComponent;
                    }
                }
            }
            
            // Sets the mouse cursor to hourglass mode
            getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            activeComponent = theWindow.getFocusOwner();
            
            // Start receiving all events and consume them if necessary
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
            
            this.requestFocus();
            
            // Activate the glass pane capabilities
            super.setVisible(value);
        } else {
            // Stop receiving all events
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            
            // Deactivate the glass pane capabilities
            super.setVisible(value);
            
            // Sets the mouse cursor back to the regular pointer
            if (getTopLevelAncestor() != null) {
                getTopLevelAncestor().setCursor(null);
            }
        }
    }
}
