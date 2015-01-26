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

//import it.cnr.icar.eric.client.ui.swing.TreeCombo.ListEntry;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TreeCombo extends JComboBox<Object> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4355824897884323096L;

	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(TreeCombo.class);
    
    static final int OFFSET = 16;
    static Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
    TreeModel treeModel = null;

    @SuppressWarnings("unchecked")
	public TreeCombo(TreeModel aTreeModel) {
        super();

        treeModel = aTreeModel;
        setModel(new TreeToListModel(aTreeModel));
        setRenderer(new ListEntryRenderer());

        addActionListener(new TreeComboActionListener());
    }
    
    @SuppressWarnings("unchecked")
	public void setModel(TreeModel aTreeModel) {
        treeModel = aTreeModel;
        setModel(new TreeToListModel(aTreeModel));
    }    

    public Object getSelectedItemsObject() {
        return ((ListEntry) getSelectedItem()).object();
    }

    @SuppressWarnings("rawtypes")
	class TreeToListModel extends AbstractListModel implements ComboBoxModel,
        TreeModelListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2690065371874578183L;
		TreeModel source;
        boolean invalid = true;
        Object currentValue;
        Vector<ListEntry> cache = new Vector<ListEntry>();

        public TreeToListModel(TreeModel aTreeModel) {
            source = aTreeModel;
            aTreeModel.addTreeModelListener(this);
            setRenderer(new ListEntryRenderer());
        }

        public void setSelectedItem(Object anObject) {
            currentValue = anObject;

            if (anObject == null) {
                return;
            }

            fireContentsChanged(this, -1, -1);
        }

        public Object getSelectedItem() {
            return currentValue;
        }

        public int getSize() {
            validate();

            return cache.size();
        }

        public Object getElementAt(int index) {
            return cache.elementAt(index);
        }

        public void treeNodesChanged(TreeModelEvent e) {
            invalid = true;
        }

        public void treeNodesInserted(TreeModelEvent e) {
            invalid = true;
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            invalid = true;
        }

        public void treeStructureChanged(TreeModelEvent e) {
            invalid = true;
        }

        void validate() {
            if (invalid) {
                cache = new Vector<ListEntry>();
                cacheTree(source.getRoot(), 0);

                if ((cache.size() > 0) && (currentValue == null)) {
                    currentValue = cache.elementAt(0);
                }

                invalid = false;
                fireContentsChanged(this, 0, 0);
            }
        }

        void cacheTree(Object anObject, int level) {
            if (source.isLeaf(anObject)) {
                addListEntry(anObject, level, false);
            } else {
                int c = source.getChildCount(anObject);
                int i;
                Object child;

                addListEntry(anObject, level, true);
                level++;

                for (i = 0; i < c; i++) {
                    child = source.getChild(anObject, i);
                    cacheTree(child, level);
                }

                level--;
            }
        }

        void addListEntry(Object anObject, int level, boolean isNode) {
            cache.addElement(new ListEntry(anObject, level, isNode));
        }
    }

    class ListEntry {
        Object object;
        int level;
        boolean isNode;

        public ListEntry(Object anObject, int aLevel, boolean isNode) {
            object = anObject;
            level = aLevel;
            this.isNode = isNode;
        }

        public Object object() {
            return object;
        }

        public int level() {
            return level;
        }

        public boolean isNode() {
            return isNode;
        }

        public String toString() {
            return object.toString();
        }
    }

    public class TreeComboActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (treeModel instanceof ConceptsTreeModel) {
                @SuppressWarnings("unused")
				TreeCombo cb = (TreeCombo) e.getSource();
                Object anObject = getSelectedItem();

                if (anObject == null) {
                    return;
                }

                if (anObject instanceof ListEntry) {
                    ListEntry listEntry = (ListEntry) anObject;

                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) listEntry.object();

                    int depth = 1;

                    if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                        depth = -1;
                    }
                    
                    // use a SwingWorker to expand the tree
                    final int finalDepth = depth;
                    final ConceptsTreeModel conceptsTreeModel = (ConceptsTreeModel) treeModel;
                    final SwingWorker worker = new SwingWorker(TreeCombo.this) {
                        public Object doNonUILogic() {
                            conceptsTreeModel.expandTree(node, finalDepth);
                            return null;
                        }
                        public void doUIUpdateLogic() {
                        }
                    };
                    worker.start();
                }
            }
        }
    }

    class ListEntryRenderer extends JLabel implements ListCellRenderer<Object> {
        /**
		 * 
		 */
		private static final long serialVersionUID = -5029269104075629812L;
		URL leafUrl = getClass().getClassLoader().getResource("icons/document.gif");
        ImageIcon leafIcon = new ImageIcon(leafUrl);
        URL nodeUrl = getClass().getClassLoader().getResource("icons/folder.gif");
        ImageIcon nodeIcon = new ImageIcon(nodeUrl);

        public ListEntryRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList<?> listbox,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ListEntry listEntry = (ListEntry) value;

            if (listEntry != null) {
                Border border;
                setText(listEntry.object().toString());
                setIcon(listEntry.isNode() ? nodeIcon : leafIcon);

                if (index != -1) {
                    // Padding both left and right sies is simplistic
                    // way of handling both LR and RL orientations.
                    border = new EmptyBorder(0, OFFSET * listEntry.level(),
			0, OFFSET * listEntry.level());
                } else {
                    border = emptyBorder;
                }

                if (UIManager.getLookAndFeel().getName().equals("CDE/Motif")) {
                    if (index == -1) {
                        setOpaque(false);
                    } else {
                        setOpaque(true);
                    }
                } else {
                    setOpaque(true);
                }

                setBorder(border);

                if (isSelected) {
                    setBackground(UIManager.getColor(
                            "ComboBox.selectionBackground"));
                    setForeground(UIManager.getColor(
                            "ComboBox.selectionForeground"));
                } else {
                    setBackground(UIManager.getColor("ComboBox.background"));
                    setForeground(UIManager.getColor("ComboBox.foreground"));
                }
            } else {
                setText("");
            }

            return this;
        }
    }
}
