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
package it.cnr.icar.eric.client.ui.swing.registration;


import it.cnr.icar.eric.client.common.userModel.KeyModel;
import it.cnr.icar.eric.client.ui.swing.JBFileFilter;
import it.cnr.icar.eric.client.ui.swing.JBPanel;
import it.cnr.icar.eric.client.ui.swing.swing.RegistryDocumentListener;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.BorderFactory;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.xml.registry.JAXRException;




/**
 * Panel for gathering information of user's digital certificate for use in
 * user registration.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class CertificateInfoPanel extends JBPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7794581714483149796L;
	private static JBFileFilter pkcs12Filter;
    private static JBFileFilter pkcs12Filter2;

    static JFileChooser chooser = null;
    static {
        chooser = new JFileChooser();
        pkcs12Filter = new JBFileFilter("p12", resourceBundle.getString("message.pkcs12Keystore"));
        chooser.setFileFilter(pkcs12Filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        pkcs12Filter2 = new JBFileFilter("p12", resourceBundle.getString("message.pkcs12Keystore"));
    };
    private JRadioButton regCertButton;
    private JRadioButton caCertButton;
    private CardLayout cardLayout;
    private JPanel paramEntryParentPanel;
    private ButtonGroup buttonGroup;
    private TitledBorder digitalCertificateBorder;

    private RegistryIssuedCertificatePanel regIssuedCertPanel;
    private JLabel aliasLabel;
    private JLabel keyPassLabel;
    private JLabel certFileLabel;
    private JButton certFileButton;
    private AliasListener aliasListener;
    private KeyPassListener keyPassListener;
    private CertFileListener certFileListener;
    
    private CAIssuedCertificatePanel caIssuedCertPanel;
    private JLabel caAliasLabel;
    private JLabel caKeyPassLabel;
    private JLabel caCertFileLabel;
    private JButton caCertFileButton;
    private KeyPassListener caKeyPassListener;
    private CertFileListener caCertFileListener;
    
    KeyModel keyModel;

    /**
     * Used for displaying objects
     */
    public CertificateInfoPanel(KeyModel keyModel) {
        this.keyModel = keyModel;
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        cardLayout = new CardLayout();

        digitalCertificateBorder = BorderFactory.createTitledBorder(resourceBundle.getString("title.certificate"));
        this.setBorder(digitalCertificateBorder);
        
        JPanel certTypePanel = createCertificateTypePanel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(certTypePanel, c);
        add(certTypePanel);

        //Next is the panel that is the parent of both regIssuedCertPanel and
        //caIssuedCertPanel
        paramEntryParentPanel = createParamEntryParentPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4, 4, 0, 4);
        gbl.setConstraints(paramEntryParentPanel, c);
        add(paramEntryParentPanel);
        
        if (keyModel.isCAIssuedCert()) {
            cardLayout.show(paramEntryParentPanel, "caIssuedCertPanel");
        } else {
            cardLayout.show(paramEntryParentPanel, "regIssuedCertPanel");
        }
    }
    
    private JPanel createCertificateTypePanel() {
        //It just has two radio buttons that Selects from "CA Issue" and "Registry Issued"
        JPanel certTypePanel = new JPanel();
        
        regCertButton = new JRadioButton(resourceBundle.getString("button.regCertButton"));
        regCertButton.setMnemonic(KeyEvent.VK_R);
        regCertButton.setSelected(!keyModel.isCAIssuedCert());
        regCertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(paramEntryParentPanel, "regIssuedCertPanel");
                keyModel.setCAIssuedCert(false);
            }
        });
        
        caCertButton = new JRadioButton(resourceBundle.getString("button.caCertButton"));
        caCertButton.setMnemonic(KeyEvent.VK_C);
        caCertButton.setSelected(keyModel.isCAIssuedCert());
        caCertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(paramEntryParentPanel, "caIssuedCertPanel");
                keyModel.setCAIssuedCert(true);
            }
        });

        buttonGroup = new ButtonGroup();
        buttonGroup.add(regCertButton);
        buttonGroup.add(caCertButton);
        
        certTypePanel.add(regCertButton);
        certTypePanel.add(caCertButton);
        
        return certTypePanel;
    }
    
    private JPanel createParamEntryParentPanel() {
        JPanel paramEntryParentPanel = new JPanel();
        paramEntryParentPanel.setLayout(cardLayout);
                
        //The panel used for Registry Issued Cert Parameters
        regIssuedCertPanel = new RegistryIssuedCertificatePanel();
        paramEntryParentPanel.add(regIssuedCertPanel, "regIssuedCertPanel");
        
        //The panel used for CA Issued Cert parameters
        caIssuedCertPanel = new CAIssuedCertificatePanel();
        paramEntryParentPanel.add(caIssuedCertPanel, "caIssuedCertPanel");
        
        return paramEntryParentPanel;
    }
    
    class RegistryIssuedCertificatePanel extends JPanel {
        
        /**
		 * 
		 */
		private static final long serialVersionUID = 8297666441306587251L;
		private JTextField certFileText;
        
        RegistryIssuedCertificatePanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            RegistryIssuedCertificatePanel.this.setLayout(gbl);
            
            //aliasLabel
            aliasLabel = new JLabel(resourceBundle.getString("label.keystoreAlias"),
                    SwingConstants.LEADING);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(aliasLabel, c);
            RegistryIssuedCertificatePanel.this.add(aliasLabel);


            JTextField aliasText = new JTextField();
            aliasListener = new AliasListener(RegistryIssuedCertificatePanel.this);
            aliasText.getDocument().addDocumentListener(aliasListener);
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.70;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(aliasText, c);
            RegistryIssuedCertificatePanel.this.add(aliasText);

            keyPassLabel = new JLabel(resourceBundle.getString("label.privateKeyPassword"),
                    SwingConstants.LEFT);
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(keyPassLabel, c);
            RegistryIssuedCertificatePanel.this.add(keyPassLabel);

            JPasswordField keyPassText = new JPasswordField();
            keyPassListener = new KeyPassListener(RegistryIssuedCertificatePanel.this);
            keyPassText.getDocument().addDocumentListener(keyPassListener);
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.30;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(keyPassText, c);
            RegistryIssuedCertificatePanel.this.add(keyPassText);    
            
            certFileLabel = new JLabel(resourceBundle.getString("label.certFileTo"),
                    SwingConstants.LEFT);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(certFileLabel, c);
            RegistryIssuedCertificatePanel.this.add(certFileLabel);

            certFileText = new JTextField();
            certFileListener = new CertFileListener(RegistryIssuedCertificatePanel.this);
            certFileText.getDocument().addDocumentListener(certFileListener);
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.70;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(certFileText, c);
            RegistryIssuedCertificatePanel.this.add(certFileText);
            
            certFileButton = new JButton(resourceBundle.getString("button.chooseCertFileTo"));
            certFileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    File certFile = getCertificateFileForExport();
                    setCertFile(certFile.getAbsolutePath());
                }
            });
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 2;
            c.weightx = 0.30;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(certFileButton, c);
            RegistryIssuedCertificatePanel.this.add(certFileButton);
            
        }
        
        void setCertFile(String path) {
            if (!path.endsWith(".p12")) {
                path = path + ".p12";
            }
            certFileText.setText(path);
            keyModel.setP12File(path);
        }     
        
        File getCertificateFileForExport() {
            File certFile = null;

            //File currentDir = chooser.getCurrentDirectory();        

            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                certFile = chooser.getSelectedFile();
            }
            return certFile;
        }
        
    }
    
    class CAIssuedCertificatePanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2540864418032025568L;
		private JTextField certFileText;
        
        CAIssuedCertificatePanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            CAIssuedCertificatePanel.this.setLayout(gbl);
            
            //caAliasLabel
            caAliasLabel = new JLabel(resourceBundle.getString("label.keystoreAlias"),
                    SwingConstants.LEADING);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(caAliasLabel , c);
            CAIssuedCertificatePanel.this.add(caAliasLabel );


            JTextField aliasText = new JTextField();
            aliasText.getDocument().addDocumentListener(new AliasListener(CAIssuedCertificatePanel.this));
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.70;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(aliasText, c);
            CAIssuedCertificatePanel.this.add(aliasText);

            caKeyPassLabel = new JLabel(resourceBundle.getString("label.privateKeyPassword"),
                    SwingConstants.LEFT);
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(caKeyPassLabel, c);
            CAIssuedCertificatePanel.this.add(caKeyPassLabel);

            JPasswordField keyPassText = new JPasswordField();
            caKeyPassListener = new KeyPassListener(CAIssuedCertificatePanel.this);
            keyPassText.getDocument().addDocumentListener(caKeyPassListener);
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.30;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(keyPassText, c);
            CAIssuedCertificatePanel.this.add(keyPassText);    
            
            caCertFileLabel = new JLabel(resourceBundle.getString("label.certFileFrom"),
                    SwingConstants.LEFT);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(caCertFileLabel, c);
            CAIssuedCertificatePanel.this.add(caCertFileLabel);

            certFileText = new JTextField();
            caCertFileListener = new CertFileListener(CAIssuedCertificatePanel.this);
            certFileText.getDocument().addDocumentListener(caCertFileListener);
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.70;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(certFileText, c);
            CAIssuedCertificatePanel.this.add(certFileText);
            
            caCertFileButton = new JButton(resourceBundle.getString("button.chooseCertFileFrom"));
            caCertFileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    File certFile = getCertificateFileForImport();
                    setCertFile(certFile.getAbsolutePath());
                }
            });
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 2;
            c.weightx = 0.30;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(4, 4, 0, 4);
            gbl.setConstraints(caCertFileButton, c);
            CAIssuedCertificatePanel.this.add(caCertFileButton);
            
        }
        
        void setCertFile(String path) {
            certFileText.setText(path);
            keyModel.setP12File(path);
        }
        
        File getCertificateFileForImport() {
            File certFile = null;

            chooser.setFileFilter(pkcs12Filter2);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //File currentDir = chooser.getCurrentDirectory();        

            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                certFile = chooser.getSelectedFile();
            }
            return certFile;
        }
        
        class AliasListener extends RegistryDocumentListener {
            public AliasListener(JPanel panel) {
                super(panel, resourceBundle.getString("error.setAliasFailed"));
            }

            protected void setText(String text) throws JAXRException {
                keyModel.setAlias(text);
            }
        }
    }                

    /**
     * Sets whether this dialog is read-only or editable.
     */
    public void setEnabledCertificateTypeButtons(boolean enabled) {
        Enumeration<?> elements = buttonGroup.getElements();
        while (elements.hasMoreElements()) {
            JRadioButton button = (JRadioButton)elements.nextElement();
            button.setEnabled(enabled);
        }
    }
    
    /**
     * Processes a change in the bound property
     * RegistryBrowser.PROPERTY_LOCALE.
     */
    protected void processLocaleChange(Locale newLocale) {
        super.processLocaleChange(newLocale);

        updateUIText();
    }

    /**
     * Updates the UI strings based on the locale of the ResourceBundle.
     */
    protected void updateUIText() {
        super.updateUIText();
        
        pkcs12Filter.setDescription(resourceBundle.getString("message.pkcs12Keystore"));
        pkcs12Filter2.setDescription(resourceBundle.getString("message.pkcs12Keystore"));
        regCertButton.setText(resourceBundle.getString("button.regCertButton"));
        caCertButton.setText(resourceBundle.getString("button.caCertButton"));
        digitalCertificateBorder.setTitle(resourceBundle.getString("title.certificate"));

        aliasLabel.setText(resourceBundle.getString("label.keystoreAlias"));
        keyPassLabel.setText(resourceBundle.getString("label.privateKeyPassword"));
        certFileLabel.setText(resourceBundle.getString("label.certFileTo"));
        certFileButton.setText(resourceBundle.getString("button.chooseCertFileTo"));
        aliasListener.setError(resourceBundle.getString("error.setAliasFailed"));
        certFileListener.setError(resourceBundle.getString("error.setCertFileFailed"));
        keyPassListener.setError(resourceBundle.getString("error.setPKPassFailed"));
                
        caAliasLabel.setText(resourceBundle.getString("label.keystoreAlias"));
        caKeyPassLabel.setText(resourceBundle.getString("label.privateKeyPassword"));
        caCertFileLabel.setText(resourceBundle.getString("label.certFileTo"));
        caCertFileButton.setText(resourceBundle.getString("button.chooseCertFileFrom"));
        caCertFileListener.setError(resourceBundle.getString("error.setCertFileFailed"));
        caKeyPassListener.setError(resourceBundle.getString("error.setPKPassFailed"));

    }
    
    class AliasListener extends RegistryDocumentListener {
        public AliasListener(JPanel panel) {
            super(panel, resourceBundle.getString("error.setAliasFailed"));
        }

        protected void setText(String text) throws JAXRException {
            keyModel.setAlias(text);
            regIssuedCertPanel.setCertFile(System.getProperty("user.home", ".") + "/" + text);
        }
    }
    
    class KeyPassListener extends RegistryDocumentListener {
        public KeyPassListener(JPanel panel) {
            super(panel, resourceBundle.getString("error.setPKPassFailed"));
        }

        /**
         * The Swing documentation recommends to use char[] instead of String
         * for passwords for security reasons. That would probably mean that
         * we have to write our own document and document listener.
         */
        protected void setText(String text) throws JAXRException {
            keyModel.setKeyPassword(text.toCharArray());
        }
    }
    
    class CertFileListener extends RegistryDocumentListener {
        public CertFileListener(JPanel panel) {
            super(panel, resourceBundle.getString("error.setCertFileFailed"));
        }

        protected void setText(String text) throws JAXRException {
            keyModel.setP12File(text);
        }
    }
}
