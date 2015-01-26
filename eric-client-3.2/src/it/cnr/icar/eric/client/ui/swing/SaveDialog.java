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

//import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//import java.beans.PropertyChangeEvent;

import javax.swing.ButtonGroup;
//import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
//import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JTextField;


/**
 * The Save Dialog allows configuration of save properties such as Versioning.
 *
 * @author  <a href="mailto:farrukh.najmi@sun.com">Farrukh Najmi</a> / Sun Microsystems
 */
public class SaveDialog extends JBDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5736389578763587475L;
	private JCheckBox jchbVersionMetadata;
    private JCheckBox jchbVersionContent;
    private JPanel jPanel1;
    @SuppressWarnings("unused")
	private ButtonGroup bgSelectionMode;
    
    /** Determine if versioning is on or off */
    @SuppressWarnings("unused")
	private boolean versionMetadata = true;
    @SuppressWarnings("unused")
	private boolean versionContent = true;
    
    
    /** Creates new form SaveDialog */
    public SaveDialog(JFrame parent,
            boolean modal) {
        super(parent, modal, true);
        SaveDialog_initialize();
    }
    
    /** Creates new form SaveDialog */
    public SaveDialog(JDialog parent,
            boolean modal) {
        super(parent, modal, true);
        SaveDialog_initialize();
    }
    
    private void SaveDialog_initialize() {
        setEditable(true);
        setTitle(resourceBundle.getString("dialog.save.title"));
        
        jPanel1 = getMainPanel();
        jPanel1.setLayout(new java.awt.GridBagLayout());
        
        GridBagConstraints gridBagConstraints;
        
        bgSelectionMode = new javax.swing.ButtonGroup();
        jchbVersionMetadata = new javax.swing.JCheckBox();
        jchbVersionContent = new javax.swing.JCheckBox();
        
        jchbVersionMetadata.setText(resourceBundle.getString("dialog.save.versionMetaData"));
        jchbVersionMetadata.setEnabled(true);
        jchbVersionMetadata.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jchbVersionMetadataActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbVersionMetadata, gridBagConstraints);
        
        jchbVersionContent.setText(resourceBundle.getString("dialog.save.versionContent"));
        jchbVersionContent.setEnabled(true);
        jchbVersionContent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jchbVersionContentActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 12, 0, 4);
        jPanel1.add(jchbVersionContent, gridBagConstraints);
        
        pack();
    }
    
    private void jchbVersionMetadataActionPerformed(ActionEvent evt) {
        //If not version metadata then MUST NOT version content
        if (!jchbVersionMetadata.isSelected()) {
            jchbVersionContent.setSelected(false);
        }
    }
    
    private void jchbVersionContentActionPerformed(ActionEvent evt) {
        //If versioning content then MUST version metadata
        if (jchbVersionContent.isSelected()) {
            jchbVersionMetadata.setSelected(true);
        }
    }
    
    protected void okAction() {
        super.okAction();                
        
        dispose();
    }
    
    public boolean versionMetadata() {
        return jchbVersionMetadata.isSelected();
    }
    
    public boolean versionContent() {
        return jchbVersionContent.isSelected();
    }
    
    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        setTitle(resourceBundle.getString("dialog.save.title"));
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        SaveDialog saveDialog = new SaveDialog(frame, true);
        saveDialog.setVisible(true);
        frame.dispose();
    }
}
