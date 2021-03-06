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

import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;


/**
 * Specialized JList for showing ExternalIdentifiers.
 * Supports drag&drop of ExternalIdentifier objects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class ExternalIdentifiersList extends RegistryObjectsList {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1759859153221391669L;

	/**
     * Default constructor
     */
    public ExternalIdentifiersList() {
        this(new ExternalIdentifiersListModel());
    }

    /**
     * Constructor
     */
    public ExternalIdentifiersList(ExternalIdentifiersListModel model) {
        super(LifeCycleManager.EXTERNAL_IDENTIFIER, model);

        setCellRenderer(new ExternalIdentifierRenderer());
    }

    class ExternalIdentifierRenderer extends JLabel implements ListCellRenderer<Object> {
        /**
		 * 
		 */
		private static final long serialVersionUID = 5356021503055315776L;

		public ExternalIdentifierRenderer() {
            setOpaque(true);

            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            try {
                ExternalIdentifiersListModel model = (ExternalIdentifiersListModel) list.getModel();
                ExternalIdentifier extId = (ExternalIdentifier) (model.elementAt(index));
                ClassificationScheme scheme = extId.getIdentificationScheme();
                String schemeName = RegistryBrowser.getName(scheme);
                String cvalue = extId.getValue();

                Object[] listCellArgs = {schemeName, cvalue};
                MessageFormat form =
                    new MessageFormat(resourceBundle.getString("format.classificationCell"));
          
                setText(form.format(listCellArgs));
                
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }

            return this;
        }
    }
}
