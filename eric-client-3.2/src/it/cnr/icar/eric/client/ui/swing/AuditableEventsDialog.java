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

import java.awt.BorderLayout;

import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.text.MessageFormat;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;


public class AuditableEventsDialog extends JBDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6367867677934742741L;
	private AuditableEventsTableModel auditableEventsTableModel = null;
    private AuditableEventsTable auditableEventsTable = null;

    public AuditableEventsDialog(JFrame parent, boolean modal,
        RegistryObject registryObject) {
        super(parent, modal);

        try {
			Object[] auditableEventsArgs = {RegistryBrowser.getName(registryObject)};
			MessageFormat form =
				new MessageFormat(resourceBundle.getString("title.auditableEvents"));
            setTitle(form.format(auditableEventsArgs));
            initialize(registryObject.getAuditTrail());
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void initialize(Collection<?> auditableEvents) {
        JPanel mainPanel = getMainPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel auditableEventsTablePanel = new JPanel();
        auditableEventsTablePanel.setBorder(BorderFactory.createTitledBorder(
                resourceBundle.getString("title.auditableEvents.border")));
        auditableEventsTablePanel.setLayout(new BorderLayout());

        auditableEventsTableModel = new AuditableEventsTableModel();
        auditableEventsTable = new AuditableEventsTable(auditableEventsTableModel);

        JScrollPane auditableEventsTablePane = new JScrollPane(auditableEventsTable);
        auditableEventsTablePanel.add(auditableEventsTablePane,
            BorderLayout.CENTER);

        //setBounds(new Rectangle(300, 300, 600, 300));
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);        
        setAuditableEvents(auditableEvents);

        auditableEventsTablePanel.setVisible(true);
        mainPanel.add(auditableEventsTablePanel);
        pack();
    }

    public void setAuditableEvents(Collection<?> auditableEvents) {
        auditableEventsTableModel.update(auditableEvents);
    }
}
