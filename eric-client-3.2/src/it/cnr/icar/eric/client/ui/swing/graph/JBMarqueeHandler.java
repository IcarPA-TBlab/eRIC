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
package it.cnr.icar.eric.client.ui.swing.graph;

import com.jgraph.graph.BasicMarqueeHandler;
import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.GraphConstants;
import com.jgraph.graph.Port;
import com.jgraph.graph.PortView;


import it.cnr.icar.eric.client.ui.swing.JavaUIResourceBundle;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/**
 * MarqueeHandler that Connects Vertices and Displays PopupMenus
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class JBMarqueeHandler extends BasicMarqueeHandler {
    protected JavaUIResourceBundle resourceBundle =
    JavaUIResourceBundle.getInstance();
    
    JBGraph graph = null;

    // Holds the Start and the Current Point
    protected Point start;
    protected Point current;

    // Holds the First and the Current Port
    protected PortView port;
    protected PortView firstPort;

    public JBMarqueeHandler(JBGraph graph) {
        this.graph = graph;
    }

    // Override to Gain Control (for PopupMenu and ConnectMode)
    public boolean isForceMarqueeEvent(MouseEvent e) {
        // If Right Mouse Button we want to Display the PopupMenu
        if (SwingUtilities.isRightMouseButton(e)) {
            // Return Immediately
            return true;
        }

        // Find and Remember Port
        port = getSourcePortAt(e.getPoint());

        // If Port Found and in ConnectMode (=Ports Visible)
        if ((port != null) && graph.isPortsVisible()) {
            return true;
        }

        // Else Call Superclass
        return super.isForceMarqueeEvent(e);
    }

    // Display PopupMenu or Remember Start Location and First Port
    public void mousePressed(final MouseEvent e) {
        // If Right Mouse Button
        if (SwingUtilities.isRightMouseButton(e)) {
            // Scale From Screen to Model
            Point loc = graph.fromScreen(e.getPoint());

            // Find Cell in Model Coordinates
            Object cell = graph.getFirstCellForLocation(loc.x, loc.y);

            //If DefaultGraphCell then it must be a Collection attribute
            //Get the JBGraphCell under it.
            if ((!(cell instanceof it.cnr.icar.eric.client.ui.swing.graph.JBGraphCell)) &&
                    (cell instanceof DefaultGraphCell)) {
                JBGraphCell _cell = graph.getJBGraphCellAt((DefaultGraphCell) cell,
                        loc.x, loc.y);

                if (_cell != null) {
                    cell = _cell;
                }
            }

            // Create PopupMenu for the Cell
            JPopupMenu menu = createPopupMenu(e.getPoint(), cell);

            // Display PopupMenu
            menu.show(graph, e.getX(), e.getY());

            // Else if in ConnectMode and Remembered Port is Valid
        } else if ((port != null) && !e.isConsumed() && graph.isPortsVisible()) {
            // Remember Start Location
            start = graph.toScreen(port.getLocation(null));

            // Remember First Port
            firstPort = port;

            // Consume Event
            e.consume();
        } else {
            // Call Superclass
            super.mousePressed(e);
        }
    }

    // Find Port under Mouse and Repaint Connector
    public void mouseDragged(MouseEvent e) {
        // If remembered Start Point is Valid
        if ((start != null) && !e.isConsumed()) {
            // Fetch Graphics from Graph
            Graphics g = graph.getGraphics();

            // Xor-Paint the old Connector (Hide old Connector)
            paintConnector(Color.black, graph.getBackground(), g);

            // Reset Remembered Port
            port = getTargetPortAt(e.getPoint());

            // If Port was found then Point to Port Location
            if (port != null) {
                current = graph.toScreen(port.getLocation(null));
            }
            // Else If no Port was found then Point to Mouse Location
            else {
                current = graph.snap(e.getPoint());
            }

            // Xor-Paint the new Connector
            paintConnector(graph.getBackground(), Color.black, g);

            // Consume Event
            e.consume();
        }

        // Call Superclass
        super.mouseDragged(e);
    }

    public PortView getSourcePortAt(Point point) {
        // Scale from Screen to Model
        Point tmp = graph.fromScreen(new Point(point));

        // Find a Port View in Model Coordinates and Remember
        return graph.getPortViewAt(tmp.x, tmp.y);
    }

    // Find a Cell at point and Return its first Port as a PortView
    protected PortView getTargetPortAt(Point point) {
        // Find Cell at point (No scaling needed here)
        Object cell = graph.getFirstCellForLocation(point.x, point.y);

        // Loop Children to find PortView
        for (int i = 0; i < graph.getModel().getChildCount(cell); i++) {
            // Get Child from Model
            Object tmp = graph.getModel().getChild(cell, i);

            // Get View for Child using the Graph's View as a Cell Mapper
            tmp = graph.getView().getMapping(tmp, false);

            // If Child View is a Port View and not equal to First Port
            if (tmp instanceof PortView && (tmp != firstPort)) {
                // Return as PortView
                return (PortView) tmp;
            }
        }

        // No Port View found
        return getSourcePortAt(point);
    }

    // Connect the First Port and the Current Port in the Graph or Repaint
    public void mouseReleased(MouseEvent e) {
        // If Valid Event, Current and First Port
        if ((e != null) && !e.isConsumed() && (port != null) &&
                (firstPort != null) && (firstPort != port)) {
            // Then Establish Connection
            graph.connect((Port) firstPort.getCell(), (Port) port.getCell());

            // Consume Event
            e.consume();

            // Else Repaint the Graph
        } else {
            graph.repaint();
        }

        // Reset Global Vars
        firstPort = port = null;
        start = current = null;

        // Call Superclass
        super.mouseReleased(e);
    }

    // Show Special Cursor if Over Port
    public void mouseMoved(MouseEvent e) {
        // Check Mode and Find Port
        if ((e != null) && (getSourcePortAt(e.getPoint()) != null) &&
                !e.isConsumed() && graph.isPortsVisible()) {
            // Set Cusor on Graph (Automatically Reset)
            graph.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Consume Event
            e.consume();
        }

        // Call Superclass
        super.mouseReleased(e);
    }

    // Use Xor-Mode on Graphics to Paint Connector
    protected void paintConnector(Color fg, Color bg, Graphics g) {
        // Set Foreground
        g.setColor(fg);

        // Set Xor-Mode Color
        g.setXORMode(bg);

        // Highlight the Current Port
        paintPort(graph.getGraphics());

        // If Valid First Port, Start and Current Point
        if ((firstPort != null) && (start != null) && (current != null)) {
            // Then Draw A Line From Start to Current Point
            g.drawLine(start.x, start.y, current.x, current.y);
        }
    }

    // Use the Preview Flag to Draw a Highlighted Port
    protected void paintPort(Graphics g) {
        // If Current Port is Valid
        if (port != null) {
            // If Not Floating Port...
            boolean o = (GraphConstants.getOffset(port.getAttributes()) != null);

            // ...Then use Parent's Bounds
            Rectangle r = (o) ? port.getBounds()
                              : port.getParentView().getBounds();

            // Scale from Model to Screen
            r = graph.toScreen(new Rectangle(r));

            // Add Space For the Highlight Border
            r.setBounds(r.x - 3, r.y - 3, r.width + 6, r.height + 6);

            // Paint Port in Preview (=Highlight) Mode
            graph.getUI().paintCell(g, port, r, true);
        }
    }

    //
    // PopupMenu
    //
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();
        boolean editable = graph.isEditable();

        if (cell != null) {
            if (cell instanceof it.cnr.icar.eric.client.ui.swing.graph.JBGraphCell) {
                // Edit
                String editLabel = null;

                if (editable) {
                    editLabel = resourceBundle.getString("menu.edit");
                } else {
                    editLabel = resourceBundle.getString("menu.showDetails");
                }

                menu.add(new AbstractAction(editLabel) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -7971072098388170354L;

						public void actionPerformed(ActionEvent e) {
                            graph.editCell((JBGraphCell) cell);
                        }
                    });

                // Show related objects
                menu.add(new AbstractAction(resourceBundle.getString("menu.graphPanel.showRelated")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -688260505324214754L;

						public void actionPerformed(ActionEvent e) {
                            graph.showRelatedObjects((JBGraphCell) cell);
                        }
                    });

                // Browse object in a new GraphPanel
                menu.add(new AbstractAction(resourceBundle.getString("menu.graphPanel.browseAsGraph")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -3034236101124246783L;

						public void actionPerformed(ActionEvent e) {
                            graph.browseObject((JBGraphCell) cell);
                        }
                    });

                // Show audit trail
                menu.add(new AbstractAction(resourceBundle.getString("menu.showAuditTrail")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -7303493556200490532L;

						public void actionPerformed(ActionEvent e) {
                            graph.showAuditTrail((JBGraphCell) cell);
                        }
                    });

                // Show RegistryObject
                menu.add(new AbstractAction(resourceBundle.getString("menu.showRegistryObject")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -1326028271088278256L;

						public void actionPerformed(ActionEvent e) {
                            graph.showRegistryObject((JBGraphCell) cell);
                        }
                    });
                    
                if (((JBGraphCell) cell).getRegistryObject() instanceof it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl) {
                    // Retrieve
                    menu.add(new AbstractAction(resourceBundle.getString("menu.showRepositoryItem")) {
                            /**
						 * 
						 */
						private static final long serialVersionUID = -8593544499293295688L;

							public void actionPerformed(ActionEvent e) {
                                graph.showRepositoryItem((JBGraphCell) cell);
                            }
                        });
                }
            }
        }

        // Remove
        if (!graph.isSelectionEmpty()) {
            menu.addSeparator();

            if (editable) {
                menu.add(new AbstractAction(resourceBundle.getString("menu.graphPanel.removeFromView")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -5808497140647589680L;

						public void actionPerformed(ActionEvent e) {
                            graph.remove.actionPerformed(e);
                        }
                    });
            }

            if (graph.getSelectionCount() > 1) {
                // ReLayout
                menu.add(new AbstractAction(resourceBundle.getString("menu.graphPanel.reLayout")) {
                        /**
					 * 
					 */
					private static final long serialVersionUID = -5796865397218862021L;

						public void actionPerformed(ActionEvent e) {
                            JBGraph.circleLayout(graph);
                        }
                    });
            }
        }

        menu.addSeparator();

        if (editable) {
            // Insert
            menu.add(new AbstractAction(resourceBundle.getString("menu.insert")) {
                    /**
				 * 
				 */
				private static final long serialVersionUID = 556677078992592257L;

					public void actionPerformed(ActionEvent ev) {
                        graph.insert(pt);
                    }
                });

            // Save
            menu.add(new AbstractAction(resourceBundle.getString("menu.save")) {
                    /**
				 * 
				 */
				private static final long serialVersionUID = 2839092145726318931L;

					public void actionPerformed(ActionEvent ev) {
                        graph.save();
                    }
                });
        }

        // Export
        /* Commented as Export is of dubious value in current state.
        menu.add(new AbstractAction(resourceBundle.getString("menu.export")) {
                public void actionPerformed(ActionEvent ev) {
                    graph.export();
                }
            });
        */
        return menu;
    }
}
