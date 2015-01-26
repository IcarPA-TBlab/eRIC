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

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;


/*
 * Based upon code from:
 * http://forum.java.sun.com/thread.jsp?thread=328882&forum=57&message=1337973
 *
 */
public class HyperLinkLabel extends JLabel implements HyperLinkContainer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8940355632901054818L;
	Color stdFG = Color.BLACK;
    Font stdFont;
    Font urlFont;
    Color urlNormalFG = Color.BLUE;
    Color urlHiliteFG = Color.RED;
    String url;
    MouseListener mouseListener;
    HyperLinkContainer linkContainer = null;

    public HyperLinkLabel() {
        this(null, null, 0);
    }

    public HyperLinkLabel(Icon image) {
        this(null, image, 0);
    }

    public HyperLinkLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
    }

    public HyperLinkLabel(String text) {
        this(text, null, 0);
    }

    public HyperLinkLabel(String text, int horizontalAlignment) {
        this(text, null, horizontalAlignment);
    }

    public HyperLinkLabel(String text, Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);

        stdFG = this.getForeground();
        stdFont = this.getFont();

        //Make urlFont be bold and underlined
        urlFont = stdFont.deriveFont(Font.BOLD);

        //java.util.HashMap textAttributes = new java.util.HashMap(stdFont.getAttributes());
        //textAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        //urlFont = urlFont.deriveFont(textAttributes);
        setText(text);

        //Create mouse listener
        mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinker.displayURL(_url);
                        }
                    }

                    public void mouseEntered(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinkLabel.this.setCursor(new Cursor(
                                    Cursor.HAND_CURSOR));
                            HyperLinkLabel.this.setForeground(urlHiliteFG);
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        String _url = getURL();

                        if ((_url != null) && (_url.length() > 0)) {
                            HyperLinkLabel.this.setForeground(urlNormalFG);
                            HyperLinkLabel.this.setCursor(new Cursor(
                                    Cursor.DEFAULT_CURSOR));
                        }
                    }
                };
    }

    public String getURL() {
        if (this.linkContainer != null) {
            url = this.linkContainer.getURL();
        }

        return url;
    }

    public void setURL(String urlString) throws MalformedURLException {
        removeMouseListener(mouseListener);
        this.url = null;
        linkContainer = null;

        setForeground(stdFG);

        //setFont(stdFont);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        //Check if URL is valid
        URL _url = null;

        if (urlString != null) {
            _url = new URL(urlString);
        }

        if (_url != null) {
            //Valid URL. Use it.
            this.url = urlString;
            linkContainer = null;

            setForeground(urlNormalFG);

            //setFont(urlFont);
            addMouseListener(mouseListener);
        }
    }

    public HyperLinkContainer getHyperLinkContainer() {
        HyperLinkContainer _linkContainer = linkContainer;

        if (_linkContainer == null) {
            _linkContainer = this;
        }

        return _linkContainer;
    }

    public void setHyperLinkContainer(HyperLinkContainer linkContainer) {
        this.linkContainer = linkContainer;

        setForeground(urlNormalFG);

        //setFont(urlFont);
        addMouseListener(mouseListener);
    }

    public void setText(String text) {
        super.setText(text);

        //Check if URL is valid
        @SuppressWarnings("unused")
		URL _url = null;

        try {
            _url = new URL(text);

            //setURL if text is a valid URL
            setURL(text);
        } catch (MalformedURLException e) {
            //No need to do anything. It is normal for tetx to not be a URL
        	_url = null;
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        JFrame f = new JFrame("HyperLinkLabel Tester");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Container c = f.getContentPane();
        HyperLinkLabel lb = new HyperLinkLabel("click me");
        lb.setURL("http://ebxmlrr.sourceforge.net");

        c.add(lb);
        f.pack();
        f.setVisible(true);
    }
}
