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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


//import org.oasis.ebxml.registry.bindings.rim.*;

/**
 * A panel that serves as a manager for a card panel.
 * It provides toggle buttons to control which card in card panel is showing.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class CardManagerPanel extends JBPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4110525965398802029L;
	private GridBagConstraints c = new GridBagConstraints();
    private CardLayout cardLayout = null;
    protected JPanel selectorPanel = null;
    protected JPanel cardsPanel = null;
    protected String[] cards = null;
    protected JPanel[] cardPanels = null;
    ButtonGroup buttonGroup = null;
    HashMap<String, JRadioButton> cardToButtonMap = new HashMap<String, JRadioButton>();

    protected CardManagerPanel() {
    }

    public CardManagerPanel(String[] cards, JPanel[] cardPanels) {
        super();

        this.cards = cards;
        this.cardPanels = cardPanels;

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        //The upper panel with radio buttons to select a card in card layout
        selectorPanel = createSelectorPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(selectorPanel, c);
        add(selectorPanel);

        //Next is the panel containing all the cards
        cardsPanel = createCardsPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(cardsPanel, c);
        add(cardsPanel);
    }

    private JPanel createSelectorPanel() {
        //It just has radio buttons that select from among throws cards
        JPanel selectorPanel = new JPanel();
        buttonGroup = new ButtonGroup();

        for (int i = 0; i < cards.length; i++) {
            final String card = cards[i];
            JRadioButton radioButton = new JRadioButton(card);

            if (i == 0) {
                radioButton.setSelected(true);
            }

            radioButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showCardAction(card);
                    }
                });
            buttonGroup.add(radioButton);
            selectorPanel.add(radioButton);
            cardToButtonMap.put(card, radioButton);
        }

        return selectorPanel;
    }

    public void showCard(String card) {
        JRadioButton radioButton = cardToButtonMap.get(card);
        radioButton.doClick();
    }

    protected void showCardAction(String card) {
        cardLayout.show(cardsPanel, card);
    }

    private JPanel createCardsPanel() {
        JPanel cardsPanel = new JPanel();
        cardLayout = new CardLayout();
        cardsPanel.setLayout(cardLayout);

        for (int i = 0; i < cardPanels.length; i++) {
            cardsPanel.add(cardPanels[i], cards[i]);
        }

        return cardsPanel;
    }
}
