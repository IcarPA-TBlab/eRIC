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


import it.cnr.icar.eric.client.ui.common.UIUtility;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ConfigurationType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.RegistryURIListType;
import it.cnr.icar.eric.client.ui.swing.metal.BigContrastMetalTheme;
import it.cnr.icar.eric.client.ui.swing.metal.ContrastMetalTheme;
import it.cnr.icar.eric.client.ui.swing.metal.DemoMetalTheme;
import it.cnr.icar.eric.client.ui.swing.metal.MetalThemeMenu;
import it.cnr.icar.eric.client.ui.swing.metal.UISwitchListener;
import it.cnr.icar.eric.client.ui.swing.registration.KeyManager;
import it.cnr.icar.eric.client.ui.swing.registration.UserManager;
import it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.ClientRequestContext;
import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AuditableEventImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CommonResourceBundle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * The ebXML Registry Browser
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RegistryBrowser extends JFrame implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7784164802384782869L;
	// TODO: Need to make this get its value from dist.version
	public static final String BROWSER_VERSION = "3.2";
	static boolean localCall = Boolean.valueOf(
			ProviderProperties.getInstance().getProperty("it.cnr.icar.eric.client.xml.registry.localCall", "false"))
			.booleanValue();

	/** DOCUMENT ME! */
	private static final Log log = LogFactory.getLog(RegistryBrowser.class);
	static JAXRClient client = new JAXRClient();
	protected static JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();

	/** Bound Properties. */
	public static String PROPERTY_AUTHENTICATED = "PROPERTY_AUTHENTICATED";
	public static String PROPERTY_LOCALE = "locale";

	/** DOCUMENT ME! */
	static String selectAnItem = resourceBundle.getString("listBox.enterURL");
	static RegistryBrowser instance;

	// The baseURL to registry currently connected to.
	static String baseURL;

	/** DOCUMENT ME! */
	public ClassLoader classLoader;

	/** DOCUMENT ME! */
	Color buttonBackground;

	/** DOCUMENT ME! */
	ConceptsTreeDialog conceptsTreeDialog;

	/** A dialog for selecting a Locale for RegistryBrowser. */
	LocaleSelectorDialog localeSelectorDialog;

	/** DOCUMENT ME! */
	JMenuBar menuBar;

	/** File menu */
	JMenu fileMenu;

	/** Edit menu */
	JMenu editMenu;

	/** View menu */
	JMenu viewMenu;

	/** Theme menu */
	JMenu themeMenu;

	/** An array of themes */
	MetalTheme[] themes = { new DefaultMetalTheme(), new DemoMetalTheme(), new ContrastMetalTheme(),
			new BigContrastMetalTheme(), };

	/** Help menu */
	JMenu helpMenu;

	/** DOCUMENT ME! */
	JMenuItem newItem;

	/**
	 * Imports RegistryObjects defined in a SubmitObjectsRequest file into
	 * registry.
	 */
	JMenuItem importItem;

	/** DOCUMENT ME! */
	JMenuItem saveItem;

	/** DOCUMENT ME! */
	JMenuItem saveAsItem;

	/** DOCUMENT ME! */
	JMenuItem exitItem;

	/** DOCUMENT ME! */
	JMenuItem cutItem;

	/** DOCUMENT ME! */
	JMenuItem copyItem;

	/** DOCUMENT ME! */
	JMenuItem pasteItem;

	/** DOCUMENT ME! */
	JMenuItem aboutItem;

	// move inside constructor later

	/** DOCUMENT ME! */
	FileDialog saveFileDialog = new FileDialog(this);

	/** DOCUMENT ME! */
	JFileChooser fileChooser = new JFileChooser();

	/** DOCUMENT ME! */
	JPanel tabbedPaneParent = new JPanel();

	/** The tabbed pane */
	JBTabbedPane tabbedPane;

	/** DOCUMENT ME! */
	JPanel topPanel = new JPanel();

	/** Button for selecting search function. */
	JButton findButton;

	/** Button for selecting scheme. */
	JButton showSchemesButton;

	/** Button for logging in. */
	JButton authenticateButton;

	/** Button for logging out. */
	JButton logoutButton;

	/** Button for registering a user key. */
	JButton keyRegButton;

	/** Button for registering a user. */
	JButton userRegButton;

	/** Button for selecting locale. */
	JButton localeSelButton;

	/** Label for registryCombo */
	JLabel locationLabel;

	/** DOCUMENT ME! */
	@SuppressWarnings("rawtypes")
	JComboBox registryCombo = new JComboBox();

	/** TextField to show user name for currently authenticated user */
	JTextField currentUserText = new JTextField();

	/** DOCUMENT ME! */
	JPanel toolbarPanel = new JPanel();

	/** DOCUMENT ME! */
	JToolBar discoveryToolBar;

	/** DOCUMENT ME! */
	JPanel registryObjectsPanel = new JPanel();

	private class ItemText {
		private String text;

		ItemText(String text) {
			this.text = text;
		}

		public String toString() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	ItemText selectAnItemText;

	/**
	 * Creates a new RegistryBrowser object.
	 */
	@SuppressWarnings("unchecked")
	private RegistryBrowser() {
		instance = this;

		classLoader = getClass().getClassLoader(); // new
													// JAXRBrowserClassLoader(getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);

		/*
		 * try { classLoader.loadClass("javax.xml.soap.SOAPMessage"); } catch
		 * (ClassNotFoundException e) {
		 * log.error("Could not find class javax.xml.soap.SOAPMessage", e); }
		 */

		UIManager.addPropertyChangeListener(new UISwitchListener(getRootPane()));

		// add listener for 'locale' bound property
		addPropertyChangeListener(PROPERTY_LOCALE, this);

		menuBar = new JMenuBar();
		fileMenu = new JMenu();
		editMenu = new JMenu();
		viewMenu = new JMenu();
		helpMenu = new JMenu();

		JSeparator JSeparator1 = new JSeparator();
		newItem = new JMenuItem();
		importItem = new JMenuItem();
		saveItem = new JMenuItem();
		saveAsItem = new JMenuItem();
		exitItem = new JMenuItem();
		cutItem = new JMenuItem();
		copyItem = new JMenuItem();
		pasteItem = new JMenuItem();
		aboutItem = new JMenuItem();
		setJMenuBar(menuBar);
		setTitle(resourceBundle.getString("title.registryBrowser.java"));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		// Scale window to be centered using 70% of screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		setBounds((int) (dim.getWidth() * .15), (int) (dim.getHeight() * .1), (int) (dim.getWidth() * .7),
				(int) (dim.getHeight() * .75));
		setVisible(false);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle(resourceBundle.getString("dialog.save.title"));

		GridBagLayout gb = new GridBagLayout();

		topPanel.setLayout(gb);
		getContentPane().add("North", topPanel);

		GridBagConstraints c = new GridBagConstraints();
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		toolbarPanel.setBounds(0, 0, 488, 29);

		discoveryToolBar = createDiscoveryToolBar();
		toolbarPanel.add(discoveryToolBar);

		// c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 0, 0, 0);
		gb.setConstraints(toolbarPanel, c);
		topPanel.add(toolbarPanel);

		// Panel containing context info like registry location and user context
		JPanel contextPanel = new JPanel();
		GridBagLayout gb1 = new GridBagLayout();
		contextPanel.setLayout(gb1);

		locationLabel = new JLabel(resourceBundle.getString("label.registryLocation"));

		// locationLabel.setPreferredSize(new Dimension(80, 23));
		// c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 0, 0);
		gb1.setConstraints(locationLabel, c);

		// contextPanel.setBackground(Color.green);
		contextPanel.add(locationLabel);

		selectAnItemText = new ItemText(selectAnItem);
		registryCombo.addItem(selectAnItemText.toString());

		ConfigurationType uiConfigurationType = UIUtility.getInstance().getConfigurationType();
		RegistryURIListType urlList = uiConfigurationType.getRegistryURIList();

		List<String> urls = urlList.getRegistryURI();
		Iterator<String> urlsIter = urls.iterator();
		while (urlsIter.hasNext()) {
			ItemText url = new ItemText(urlsIter.next());
			registryCombo.addItem(url.toString());
		}

		registryCombo.setEditable(true);
		registryCombo.setEnabled(true);
		registryCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String url = (String) registryCombo.getSelectedItem();
				if ((url == null) || (url.equals(selectAnItem))) {
					return;
				}

				// Clean tabbedPaneParent. Will create new content
				tabbedPaneParent.removeAll();
				conceptsTreeDialog = null;
				ConceptsTreeDialog.clearCache();

				// design:
				// 1. connect and construct tabbedPane in a now swing thread
				// 2. add tabbedPane in swing thread
				// 3. call reloadModel that should use WingWorkers
				final SwingWorker worker1 = new SwingWorker(RegistryBrowser.this) {
					public Object doNonUILogic() {
						try {
							// Try to connect
							if (connectToRegistry(url)) {
								return new JBTabbedPane();
							}
						} catch (JAXRException e1) {
							displayError(e1);
						}
						return null;
					}

					public void doUIUpdateLogic() {
						tabbedPane = (JBTabbedPane) get();
						if (tabbedPane != null) {
							tabbedPaneParent.add(tabbedPane, BorderLayout.CENTER);
							tabbedPane.reloadModel();
							try {
								// DBH 1/30/04 - Add the submissions panel if
								// the user is authenticated.
								ConnectionImpl connection = RegistryBrowser.client.connection;
								boolean newValue = connection.isAuthenticated();
								firePropertyChange(PROPERTY_AUTHENTICATED, false, newValue);
								getRootPane().updateUI();
							} catch (JAXRException e1) {
								displayError(e1);
							}
						}
					}
				};
				worker1.start();
			}
		});
		// c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.9;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 5, 0);
		gb1.setConstraints(registryCombo, c);
		contextPanel.add(registryCombo);

		JLabel currentUserLabel = new JLabel(resourceBundle.getString("label.currentUser"), SwingConstants.TRAILING);
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 5, 5, 0);
		gb1.setConstraints(currentUserLabel, c);

		// contextPanel.add(currentUserLabel);
		currentUserText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				@SuppressWarnings("unused")
				String text = currentUserText.getText();
			}
		});

		currentUserText.setEditable(false);
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.9;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 0, 5, 5);
		gb1.setConstraints(currentUserText, c);

		// contextPanel.add(currentUserText);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.9;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 0);
		gb.setConstraints(contextPanel, c);
		topPanel.add(contextPanel, c);

		tabbedPaneParent.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		tabbedPaneParent.setLayout(new BorderLayout());
		tabbedPaneParent.setToolTipText(resourceBundle.getString("tabbedPane.tip"));

		getContentPane().add("Center", tabbedPaneParent);

		fileMenu.setText(resourceBundle.getString("menu.file"));
		fileMenu.setActionCommand("File");
		fileMenu.setMnemonic((int) 'F');
		menuBar.add(fileMenu);

		saveItem.setHorizontalTextPosition(SwingConstants.TRAILING);
		saveItem.setText(resourceBundle.getString("menu.save"));
		saveItem.setActionCommand("Save");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		saveItem.setMnemonic((int) 'S');

		// fileMenu.add(saveItem);
		fileMenu.add(JSeparator1);
		importItem.setText(resourceBundle.getString("menu.import"));
		importItem.setActionCommand("Import");
		importItem.setMnemonic((int) 'I');
		importItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RegistryBrowser.setWaitCursor();
				importFromFile();
				RegistryBrowser.setDefaultCursor();
			}
		});
		fileMenu.add(importItem);

		exitItem.setText(resourceBundle.getString("menu.exit"));
		exitItem.setActionCommand("Exit");
		exitItem.setMnemonic((int) 'X');
		fileMenu.add(exitItem);

		editMenu.setText(resourceBundle.getString("menu.edit"));
		editMenu.setActionCommand("Edit");
		editMenu.setMnemonic((int) 'E');

		// menuBar.add(editMenu);
		cutItem.setHorizontalTextPosition(SwingConstants.TRAILING);
		cutItem.setText(resourceBundle.getString("menu.cut"));
		cutItem.setActionCommand("Cut");
		cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		cutItem.setMnemonic((int) 'T');
		editMenu.add(cutItem);

		copyItem.setHorizontalTextPosition(SwingConstants.TRAILING);
		copyItem.setText(resourceBundle.getString("menu.copy"));
		copyItem.setActionCommand("Copy");
		copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
		copyItem.setMnemonic((int) 'C');
		editMenu.add(copyItem);

		pasteItem.setHorizontalTextPosition(SwingConstants.TRAILING);
		pasteItem.setText(resourceBundle.getString("menu.paste"));
		pasteItem.setActionCommand("Paste");
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
		pasteItem.setMnemonic((int) 'P');
		editMenu.add(pasteItem);

		viewMenu.setText(resourceBundle.getString("menu.view"));
		viewMenu.setActionCommand("view");
		viewMenu.setMnemonic((int) 'V');

		themeMenu = new MetalThemeMenu(resourceBundle.getString("menu.theme"), themes);
		viewMenu.add(themeMenu);
		menuBar.add(viewMenu);

		helpMenu.setText(resourceBundle.getString("menu.help"));
		helpMenu.setActionCommand("Help");
		helpMenu.setMnemonic((int) 'H');
		menuBar.add(helpMenu);
		aboutItem.setHorizontalTextPosition(SwingConstants.TRAILING);
		aboutItem.setText(resourceBundle.getString("menu.about"));
		aboutItem.setActionCommand("About...");
		aboutItem.setMnemonic((int) 'A');

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] aboutArgs = { BROWSER_VERSION };
				MessageFormat form = new MessageFormat(resourceBundle.getString("dialog.about.text"));
				JOptionPane.showMessageDialog(RegistryBrowser.this, form.format(aboutArgs),
						resourceBundle.getString("dialog.about.title"), JOptionPane.INFORMATION_MESSAGE);
			}
		});

		helpMenu.add(aboutItem);

		// REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();

		this.addWindowListener(aSymWindow);

		SymAction lSymAction = new SymAction();

		saveItem.addActionListener(lSymAction);
		exitItem.addActionListener(lSymAction);

		SwingUtilities.updateComponentTreeUI(getContentPane());
		SwingUtilities.updateComponentTreeUI(menuBar);
		SwingUtilities.updateComponentTreeUI(fileChooser);

		// Auto select the registry that is configured to connect to by default
		String selectedIndexStr = ProviderProperties.getInstance().getProperty(
				"jaxr-ebxml.registryBrowser.registryLocationCombo.initialSelectionIndex", "0");

		int index = Integer.parseInt(selectedIndexStr);

		try {
			registryCombo.setSelectedIndex(index);
		} catch (IllegalArgumentException e) {
			Object[] invalidIndexArguments = { new Integer(index) };
			MessageFormat form = new MessageFormat(resourceBundle.getString("message.error.invalidIndex"));
			displayError(form.format(invalidIndexArguments), e);
		}
	}

	// UserRegistrationWizardAction userRegAction = new
	// UserRegistrationWizardAction();
	public static RegistryBrowser getInstance() {
		if (instance == null) {
			instance = new RegistryBrowser();
		}

		return instance;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JAXRClient getClient() {
		return client;
	}

	/**
	 * Action for the Find tool.
	 */
	public void findAction() {
		if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
		} else {
			tabbedPane.findAction();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JToolBar createDiscoveryToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		// Find
		URL findUrl = getClass().getClassLoader().getResource("icons/find.gif");
		ImageIcon findIcon = new ImageIcon(findUrl);
		findButton = toolBar.add(new AbstractAction("", findIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3307493586585435411L;

			public void actionPerformed(ActionEvent e) {
				findAction();
			}
		});

		findButton.setToolTipText(resourceBundle.getString("button.find"));

		// showSchemes
		URL showSchemesUrl = getClass().getClassLoader().getResource("icons/schemeViewer.gif");
		ImageIcon showSchemesIcon = new ImageIcon(showSchemesUrl);
		showSchemesButton = toolBar.add(new AbstractAction("", showSchemesIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1899451223510883277L;

			public void actionPerformed(ActionEvent e) {
				if (RegistryBrowser.client.connection == null) {
					displayUnconnectedError();
				} else {
					ConceptsTreeDialog.showSchemes(RegistryBrowser.getInstance(), false, isAuthenticated());
				}
			}
		});

		showSchemesButton.setToolTipText(resourceBundle.getString("button.showSchemes"));

		// Re-authenticate
		URL authenticateUrl = getClass().getClassLoader().getResource("icons/authenticate.gif");
		ImageIcon authenticateIcon = new ImageIcon(authenticateUrl);
		authenticateButton = toolBar.add(new AbstractAction("", authenticateIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3469608949024981381L;

			public void actionPerformed(ActionEvent e) {
				authenticate();
			}
		});

		authenticateButton.setToolTipText(resourceBundle.getString("button.authenticate"));

		// Logout
		URL logoutUrl = getClass().getClassLoader().getResource("icons/logoff.gif");
		ImageIcon logoutIcon = new ImageIcon(logoutUrl);
		logoutButton = toolBar.add(new AbstractAction("", logoutIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -293987897100997408L;

			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});

		logoutButton.setToolTipText(resourceBundle.getString("button.logout"));
		logoutButton.setEnabled(false);

		// key registration
		URL keyRegUrl = getClass().getClassLoader().getResource("icons/keyReg.gif");
		ImageIcon keyRegIcon = new ImageIcon(keyRegUrl);
		keyRegButton = toolBar.add(new AbstractAction("", keyRegIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8988435962749097387L;

			public void actionPerformed(ActionEvent e) {
				RegistryBrowser.setWaitCursor();

				// showKeyRegistrationWizard();
				KeyManager keyMgr = KeyManager.getInstance();

				try {
					keyMgr.registerNewKey();
				} catch (Exception er) {
					RegistryBrowser.displayError(er);
				}

				RegistryBrowser.setDefaultCursor();
			}
		});

		keyRegButton.setToolTipText(resourceBundle.getString("button.keyReg"));

		// user registration
		URL userRegUrl = getClass().getClassLoader().getResource("icons/userReg.gif");
		ImageIcon userRegIcon = new ImageIcon(userRegUrl);
		userRegButton = toolBar.add(new AbstractAction("", userRegIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8890984621456210702L;

			public void actionPerformed(ActionEvent e) {
				RegistryBrowser.setWaitCursor();

				// showUserRegistrationWizard();
				if (RegistryBrowser.client.connection == null) {
					displayUnconnectedError();
				} else {
					UserManager userMgr = UserManager.getInstance();

					try {
						// Make sure you are logged off when registering new
						// user so new user is not owned by old user.
						logout();
						userMgr.registerNewUser();
						logout();
					} catch (Exception er) {
						RegistryBrowser.displayError(er);
					}
				}

				RegistryBrowser.setDefaultCursor();
			}
		});

		userRegButton.setToolTipText(resourceBundle.getString("button.userReg"));

		// locale selection
		URL localeSelUrl = getClass().getClassLoader().getResource("icons/localeSel.gif");
		ImageIcon localeSelIcon = new ImageIcon(localeSelUrl);
		localeSelButton = toolBar.add(new AbstractAction("", localeSelIcon) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6304340858289330717L;

			public void actionPerformed(ActionEvent e) {
				RegistryBrowser.setWaitCursor();

				LocaleSelectorDialog dialog = getLocaleSelectorDialog();

				@SuppressWarnings("unused")
				Locale oldSelectedLocale = getSelectedLocale();

				dialog.setVisible(true);

				Locale selectedLocale = getSelectedLocale();

				System.out.println(getLocale());

				setLocale(selectedLocale);

				RegistryBrowser.setDefaultCursor();
			}
		});

		localeSelButton.setToolTipText(resourceBundle.getString("button.localeSel"));

		return toolBar;
	}

	/**
	 * Listens to property changes in the bound property
	 * RegistryBrowser.PROPERTY_LOCALE.
	 */
	public void propertyChange(PropertyChangeEvent ev) {
		if (ev.getPropertyName().equals(PROPERTY_LOCALE)) {
			processLocaleChange((Locale) ev.getNewValue());
		}
	}

	/**
	 * Processes a change in the bound property RegistryBrowser.PROPERTY_LOCALE.
	 */
	protected void processLocaleChange(Locale newLocale) {
		setComponentOrientation(ComponentOrientation.getOrientation(newLocale));
		updateUIText();
	}

	/**
	 * Updates the UI strings based on the locale of the ResourceBundle.
	 */
	protected void updateUIText() {
		/* Frame */
		setTitle(resourceBundle.getString("title.registryBrowser.java"));

		/* Dialog boxes */
		saveFileDialog.setTitle(resourceBundle.getString("dialog.save.title"));

		/* Menus and submenus */
		fileMenu.setText(resourceBundle.getString("menu.file"));
		editMenu.setText(resourceBundle.getString("menu.edit"));
		viewMenu.setText(resourceBundle.getString("menu.view"));
		themeMenu.setText(resourceBundle.getString("menu.theme"));
		helpMenu.setText(resourceBundle.getString("menu.help"));

		/* Menu items */
		importItem.setText(resourceBundle.getString("menu.import"));
		saveItem.setText(resourceBundle.getString("menu.save"));
		exitItem.setText(resourceBundle.getString("menu.exit"));
		cutItem.setText(resourceBundle.getString("menu.cut"));
		copyItem.setText(resourceBundle.getString("menu.copy"));
		pasteItem.setText(resourceBundle.getString("menu.paste"));
		aboutItem.setText(resourceBundle.getString("menu.about"));

		/* Buttons */
		findButton.setToolTipText(resourceBundle.getString("button.find"));
		showSchemesButton.setToolTipText(resourceBundle.getString("button.showSchemes"));
		authenticateButton.setToolTipText(resourceBundle.getString("button.authenticate"));
		logoutButton.setToolTipText(resourceBundle.getString("button.logout"));
		keyRegButton.setToolTipText(resourceBundle.getString("button.keyReg"));
		userRegButton.setToolTipText(resourceBundle.getString("button.userReg"));
		localeSelButton.setToolTipText(resourceBundle.getString("button.localeSel"));

		/* Registry combo */
		locationLabel.setText(resourceBundle.getString("label.registryLocation"));
		selectAnItemText.setText(resourceBundle.getString("listBox.enterURL"));

		/* Tabbed pane parent */
		tabbedPaneParent.setToolTipText(resourceBundle.getString("tabbedPane.tip"));
	}

	/**
	 * Getter for property localeSelectorDialog. Instantiates a new
	 * LocaleSelectorDialog with Locale.getDefault() if property is null.
	 * 
	 * @return value of property localeSelectorDialog.
	 */
	public LocaleSelectorDialog getLocaleSelectorDialog() {
		if (localeSelectorDialog == null) {
			RegistryBrowser.setWaitCursor();
			localeSelectorDialog = new LocaleSelectorDialog(Locale.getDefault(), RegistryBrowser.getInstance(), true);
			RegistryBrowser.setDefaultCursor();
		}

		SwingUtilities.updateComponentTreeUI(localeSelectorDialog);
		localeSelectorDialog.pack();
		return localeSelectorDialog;
	}

	/**
	 * Getter for RegistryBrowser's current Locale. Intended to be used when
	 * displaying InternationalString values.
	 * 
	 * @return The currently selected Locale.
	 */
	public Locale getSelectedLocale() {
		// getLocaleSelectorDialog() has become to expensive! Do not call it.
		if (localeSelectorDialog == null) {
			getLocaleSelectorDialog();
		}
		return localeSelectorDialog.getSelectedLocale();
	}

	public void setLocale(Locale locale) {
		super.setLocale(locale);

		Locale oldLocale = Locale.getDefault();

		Locale.setDefault(locale);

		resourceBundle = JavaUIResourceBundle.getInstance(locale);

		firePropertyChange(PROPERTY_LOCALE, oldLocale, locale);

		applyComponentOrientation(ComponentOrientation.getOrientation(getLocale()));

		SwingUtilities.updateComponentTreeUI(this);

		// Setting the look and feel is seemingly the only way to get
		// the JOptionPane.showConfirmDialog in exitApplication() to
		// use the correct button text for the new locale.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}
	}

	/**
	 * Determine whether the user has already authenticated and setCredentials
	 * on the Connection or not. Add to JAXR 2.0??
	 * 
	 * @param handler
	 *            DOCUMENT ME!
	 */
	public boolean isAuthenticated() {
		boolean authenticated = false;

		if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
		} else {
			try {
				ConnectionImpl connection = (RegistryBrowser.client.connection);
				authenticated = connection.isAuthenticated();
			} catch (JAXRException e) {
				displayError(e);
			}
		}

		return authenticated;
	}

	/**
	 * Forces authentication to occur.
	 * 
	 */
	public void authenticate() {
		RegistryBrowser.setWaitCursor();

		if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
		} else {
			try {
				ConnectionImpl connection = (RegistryBrowser.client.connection);
				boolean oldValue = connection.isAuthenticated();

				connection.authenticate();

				boolean newValue = connection.isAuthenticated();

				authenticateButton.setEnabled(!newValue);
				logoutButton.setEnabled(newValue);

				// Notify listeners of this bound property that it has changed.
				firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
			} catch (JAXRException e) {
				displayError(e);
			}
		}

		RegistryBrowser.setDefaultCursor();
	}

	/**
	 * Handles logout action from toolbar and logs current user out.
	 */
	public void logout() {
		RegistryBrowser.setWaitCursor();

		if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
		} else {
			try {
				ConnectionImpl connection = (RegistryBrowser.client.connection);
				boolean oldValue = connection.isAuthenticated();

				connection.logoff();

				boolean newValue = connection.isAuthenticated();
				authenticateButton.setEnabled(!newValue);
				logoutButton.setEnabled(newValue);

				// Notify listeners of this bound property that it has changed.
				firePropertyChange(PROPERTY_AUTHENTICATED, oldValue, newValue);
			} catch (JAXRException e) {
				displayError(e);
			}
		}

		RegistryBrowser.setDefaultCursor();
	}

	/**
	 * DOCUMENT ME!
	 */
	void showUserRegistrationWizard() {
		RegistryBrowser.setWaitCursor();

		if (RegistryBrowser.client.connection == null) {
			displayUnconnectedError();
		} else {
			// userRegAction.performAction();
		}

		RegistryBrowser.setDefaultCursor();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param url
	 *            DOCUMENT ME!
	 */
	public boolean connectToRegistry(String url) throws JAXRException {
		// Force logout when connecting to a new registry.
		if (RegistryBrowser.client.connection != null) {
			this.logout();
		}

		baseURL = url;
		boolean connected = client.createConnection(url);
		UIUtility.getInstance().setConnection(client.getConnection());
		return connected;
	}

	/**
	 * Helper method to let browser subcomponents set a wait cursor while
	 * performing long operations.
	 */
	public static void setWaitCursor() {
		instance.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * Helper method for browser subcomponents to set the cursor back to its
	 * default version.
	 */
	public static void setDefaultCursor() {
		instance.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param message
	 *            DOCUMENT ME!
	 */
	public static void displayInfo(String message) {
		log.info(message);
		JOptionPane.showMessageDialog(RegistryBrowser.getInstance(), message,
				resourceBundle.getString("message.information.label"), JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Display common error message about not being connected to the server.
	 * 
	 * @param message
	 *            the message to display
	 */
	public static void displayUnconnectedError() {
		displayError(resourceBundle.getString("message.error.noConnection"));
	}

	/**
	 * Display an error message.
	 * 
	 * @param message
	 *            the message to display
	 */
	public static void displayError(String message) {
		log.error(message);
		JOptionPane.showMessageDialog(RegistryBrowser.getInstance(), message,
				resourceBundle.getString("message.error.label"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Method Declaration.
	 * 
	 * @param message
	 * @param t
	 * 
	 * @see
	 */
	public static void displayError(String message, Throwable t) {
		t.printStackTrace();
		log.error(message, t);

		String msg = t.getMessage();

		if (msg != null && msg.length() > 200) {
			msg = msg.substring(0, 200);
			msg += resourceBundle.getString("message.seeStderr");
		}

		displayError((message + "\n" + msg));
	}

	/**
	 * Method Declaration.
	 * 
	 * @param t
	 * 
	 * @see
	 */
	public static void displayError(Throwable t) {
		t.printStackTrace();
		log.error(t);

		String msg = t.getMessage();

		if ((msg != null) && (msg.length() > 200)) {
			msg = msg.substring(0, 200);
			msg += resourceBundle.getString("message.seeStderr");
		}

		displayError(msg);
	}

	/**
	 * The entry point for this application. Sets the Look and Feel to the
	 * System Look and Feel. Creates a new RegistryBrowser and makes it visible.
	 * 
	 * @param args
	 *            DOCUMENT ME!
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		// Pre-load configuration and props (singleton) using this thread.
		UIUtility.getInstance().getConfigurationType();
		ProviderProperties.getInstance();
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		try {
			String callbackHandlerClassName = ProviderProperties.getInstance().getProperty(
					"jaxr-ebxml.security.jaas.callbackHandlerClassName",
					System.getProperty("jaxr-ebxml.security.jaas.callbackHandlerClassName"));

			if ((callbackHandlerClassName == null) || (callbackHandlerClassName.length() == 0)) {
				System.setProperty("jaxr-ebxml.security.jaas.callbackHandlerClassName",
						"it.cnr.icar.eric.client.xml.registry.jaas.DialogAuthenticationCallbackHandler");
			}

			// By default JDialog and JFrame will not follow theme changes.
			// JDialog.setDefaultLookAndFeelDecorated(true);
			// JFrame.setDefaultLookAndFeelDecorated(true);
			// I18N: Do not localize next statement.
			System.setProperty("sun.awt.noerasebackground", "true");
			MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
			// I18N: Do not localize next statement.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// Create a new instance of our application's frame, and make it
			// visible.
			RegistryBrowser browser = getInstance();

			browser.pack();

			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

			browser.setBounds(0, 0, (int) (dim.getWidth()), (int) (dim.getHeight()));

			browser.setVisible(true);
		} catch (Throwable t) {
			log.fatal(t);
			t.printStackTrace();

			// Ensure the application exits with an error condition.
			System.exit(1);
		}
	}

	/**
	 * Method Declaration.
	 * 
	 * @param doConfirm
	 * @param exitStatus
	 */
	void exitApplication(boolean doConfirm, int exitStatus) {
		boolean doExit = true;

		if (doConfirm) {
			try {
				// Show a confirmation dialog
				int reply = JOptionPane.showConfirmDialog(this, resourceBundle.getString("message.confirmExit"),
						resourceBundle.getString("title.registryBrowser.java"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				// If the confirmation was affirmative, handle exiting.
				if (reply == JOptionPane.YES_OPTION) {
					this.setVisible(false); // hide the Frame
					this.dispose(); // free the system resources
					exitStatus = 0;
				} else {
					doExit = false;
				}
			} catch (Exception e) {
			}
		}

		if (doExit) {
			System.exit(exitStatus);
		}
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void RegistryBrowser_windowClosing(WindowEvent event) {
		// to do: code goes here.
		RegistryBrowser_windowClosing_Interaction1(event);
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void RegistryBrowser_windowClosing_Interaction1(WindowEvent event) {
		try {
			this.exitApplication(true, 0);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void saveItem_actionPerformed(ActionEvent event) {
		// to do: code goes here.
		if (RegistryBrowser.client.connection != null) {
			try {
				((BusinessLifeCycleManagerImpl) (RegistryBrowser.client.getBusinessLifeCycleManager()))
						.saveAllObjects();
			} catch (JAXRException e) {
				displayError(e);
			}
		}
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void exitItem_actionPerformed(ActionEvent event) {
		// to do: code goes here.
		exitItem_actionPerformed_Interaction1(event);
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void exitItem_actionPerformed_Interaction1(ActionEvent event) {
		try {
			this.exitApplication(true, 0);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void saveButton_actionPerformed(ActionEvent event) {
		// to do: code goes here.
		saveButton_actionPerformed_Interaction1(event);
	}

	/**
	 * Method Declaration.
	 * 
	 * @param event
	 * 
	 * @see
	 */
	void saveButton_actionPerformed_Interaction1(ActionEvent event) {
		try {
			// saveFileDialog Show the FileDialog
			saveFileDialog.setVisible(true);
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param ro
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws JAXRException
	 *             DOCUMENT ME!
	 */
	public static String getName(RegistryObject ro) throws JAXRException {
		try {
			return ((InternationalStringImpl) ro.getName()).getClosestValue();
		} catch (NullPointerException npe) {
			return "";
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param ro
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws JAXRException
	 *             DOCUMENT ME!
	 */
	public static String getDescription(RegistryObject ro) throws JAXRException {
		try {
			return ((InternationalStringImpl) ro.getDescription()).getClosestValue();
		} catch (NullPointerException npe) {
			return "";
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param registryObject
	 *            DOCUMENT ME!
	 */
	public static void showAuditTrail(RegistryObject registryObject) {
		AuditableEventsDialog dialog = new AuditableEventsDialog(RegistryBrowser.getInstance(), false,
				registryObject);

		try {
			@SuppressWarnings("rawtypes")
			Collection auditTrail = registryObject.getAuditTrail();
			if (auditTrail.size() > 0) {
				dialog.setVisible(true);
			}
		} catch (JAXRException e) {
			RegistryBrowser.displayError(e);
		}
	}

	public static void showSaveDialog(@SuppressWarnings("rawtypes") Collection registryObjects) {
		// Remove SaveDialog steps until versioning feature is enabled
		// after versioning bugs are fixed.

		// SaveDialog dialog = new
		// SaveDialog((JFrame)RegistryBrowser.getInstance(), true);

		try {
			// dialog.setVisible(true);
			// if (dialog.getStatus() == JBDialog.OK_STATUS) {
			boolean versionMetadata = false; // dialog.versionMetadata();
			boolean versionContent = false; // dialog.versionContent();
			JAXRClient client = RegistryBrowser.getInstance().getClient();
			BulkResponse resp = client.saveObjects(registryObjects, versionMetadata, versionContent);
			JAXRUtility.checkBulkResponse(resp);
			// }
		} catch (JAXRException e) {
			RegistryBrowser.displayError(e);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param user
	 *            DOCUMENT ME!
	 * @param registryLevel
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws JAXRException
	 *             DOCUMENT ME!
	 */
	public static String getUserName(User user, int registryLevel) throws JAXRException {
		String userName = "";

		if ((user != null) && (user.getPersonName() != null)) {
			PersonName personName = user.getPersonName();
			if (registryLevel == 0) {
				userName = personName.getFullName();
			} else {
				String firstName = personName.getFirstName();
				String middleName = personName.getMiddleName();
				String lastName = personName.getLastName();

				if (firstName != null) {
					userName = firstName;
				}

				if (middleName != null) {
					userName += (" " + middleName);
				}

				if (lastName != null) {
					userName += (" " + lastName);
				}
			}
		}

		return userName;
	}

	// util method -> should be implemented as any other predefined enumeration
	// and moved to proper place
	public static String getEventTypeAsString(int eventType) throws JAXRException {
		if (eventType == AuditableEvent.EVENT_TYPE_CREATED) {
			return (resourceBundle.getString("eventType.created"));
		} else if (eventType == AuditableEventImpl.EVENT_TYPE_APPROVED) {
			return (resourceBundle.getString("eventType.approved"));
		} else if (eventType == AuditableEvent.EVENT_TYPE_DELETED) {
			return (resourceBundle.getString("eventType.deleted"));
		} else if (eventType == AuditableEvent.EVENT_TYPE_DEPRECATED) {
			return (resourceBundle.getString("eventType.deprecated"));
		} else if (eventType == AuditableEvent.EVENT_TYPE_UNDEPRECATED) {
			return (resourceBundle.getString("eventType.undeprecated"));
		} else if (eventType == AuditableEvent.EVENT_TYPE_UPDATED) {
			return (resourceBundle.getString("eventType.updated"));
		} else if (eventType == AuditableEvent.EVENT_TYPE_VERSIONED) {
			return (resourceBundle.getString("eventType.versioned"));
		} else if (eventType == AuditableEventImpl.EVENT_TYPE_RELOCATED) { // Relocated
			return (resourceBundle.getString("eventType.relocated"));
		} else {
			return (null);
		}
	}

	/**
	 * Utility method that checks if obj is an instance of targetType.
	 * 
	 * @param obj
	 *            Object to check
	 * @param targetType
	 *            Class type for which to check
	 * 
	 * @return true if obj is an instance of targetType
	 * 
	 * @throws InvalidRequestException
	 *             if obj is not an instance of targetType.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isInstanceOf(Object obj, Class targetType) throws InvalidRequestException {
		if (targetType.isInstance(obj)) {
			return true;
		} else {
			Object[] notInstanceOfArgs = { targetType.getName(), obj.getClass().getName() };
			MessageFormat form = new MessageFormat(resourceBundle.getString("error.notInstanceOf"));
			throw new InvalidRequestException(form.format(notInstanceOfArgs));
		}
	}

	/**
	 * Shows the specified RepositoryItem for the RegistryObject in a Web
	 * Browser
	 * 
	 * @param registryObject
	 *            DOCUMENT ME!
	 */
	public static void showRepositoryItem(RegistryObject registryObject) {

		DataHandler repositoryItem = null;
		@SuppressWarnings("unused")
		File defaultItemFile = null;

		try {
			repositoryItem = ((ExtrinsicObject) registryObject).getRepositoryItem();
			String url = null;
			if (repositoryItem == null) {
				// I18N??
				// displayInfo("There is no repository item for this object");
				Slot contentLocatorSlot = registryObject.getSlot(CanonicalConstants.CANONICAL_SLOT_CONTENT_LOCATOR);
				if (contentLocatorSlot != null) {
					@SuppressWarnings("rawtypes")
					Collection values = contentLocatorSlot.getValues();
					String contentLocator = null;
					if (values.size() > 0) {
						contentLocator = (String) (values.toArray())[0];
					}

					if (isExternalURL(contentLocator)) {
						url = contentLocator;
					}
				}
			} else {
				url = baseURL.substring(0, baseURL.length() - 4)
						+ "http?interface=QueryManager&method=getRepositoryItem&param-id="
						+ URLEncoder.encode(registryObject.getKey().getId(), "utf-8");
			}

			if (url != null) {
				HyperLinker.displayURL(url);
			} else {
				displayInfo(resourceBundle.getString("message.info.noRepositoryItemOrURL"));
			}
		} catch (Exception e) {
			displayError(e);
		}
	}

	private static boolean isExternalURL(String urlStr) {
		boolean isExternal = false;

		// TODO: Generalize this to make sure that other protocols are supported
		// TODO: Avoid using "Magic Numbers"
		// (http://en.wikipedia.org/wiki/Magic_number_(programming)#Magic_numbers_in_code)
		if ((urlStr.startsWith("http://")) || (urlStr.startsWith("jar:"))) {
			isExternal = true;
		}

		return isExternal;
	}

	/**
	 * Shows the specified RegistryObject in a Web Browser
	 * 
	 * @param registryObject
	 *            DOCUMENT ME!
	 */
	public static void showRegistryObject(RegistryObject registryObject) {

		@SuppressWarnings("unused")
		DataHandler repositoryItem = null;
		@SuppressWarnings("unused")
		File defaultItemFile = null;

		try {
			String url = baseURL.substring(0, baseURL.length() - 4)
					+ "http?interface=QueryManager&method=getRegistryObject&param-id="
					+ URLEncoder.encode(registryObject.getKey().getId(), "utf-8");
			HyperLinker.displayURL(url);
		} catch (Exception e) {
			displayError(e);
		}
	}

	/**
	 * Import RegistryObjects defined in an XML file within a ebRS
	 * SubmitObjectsRequest and publish them to the registry user current user
	 * context.
	 */
	private void importFromFile() {
		if (isAuthenticated()) {
			try {
				int returnVal = fileChooser.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File requestFile = fileChooser.getSelectedFile();
					File parentDirectory = requestFile.getParentFile();
					Unmarshaller unmarshaller = BindingUtility.getInstance().getJAXBContext().createUnmarshaller();

					SubmitObjectsRequest submitRequest = (SubmitObjectsRequest) unmarshaller.unmarshal(requestFile);
					HashMap<String, Object> attachMap = new HashMap<String, Object>(); // id
																						// to
																						// attachments
																						// map

					// Look for special temporary Slot on ExtrinsicObjects to
					// resolve to RepositoryItem
					// If a file in same directory is found with filename same
					// as slot value
					// then assume it is the matching RepositoryItem

					// List ros =
					// submitRequest.getRegistryObjectList().getIdentifiable();
					//List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) BindingUtility
					//		.getInstance().getIdentifiableTypeList(submitRequest.getRegistryObjectList());

					// Iterator iter=ros.iterator();
					//Iterator<IdentifiableType> iter = ebIdentifiableTypeList.iterator();
					
					List<?> ros = submitRequest.getRegistryObjectList().getIdentifiable();
					Iterator<?> iter=ros.iterator();
					
					while (iter.hasNext()) {
						Object obj = iter.next();
						if (obj instanceof ExtrinsicObjectType) {
							ExtrinsicObjectType eo = (ExtrinsicObjectType) obj;
							@SuppressWarnings("rawtypes")
							HashMap slotsMap = BindingUtility.getInstance().getSlotsFromRegistryObject(eo);
							@SuppressWarnings("static-access")
							String slotName = BindingUtility.getInstance().CANONICAL_SLOT_EXTRINSIC_OBJECT_REPOSITORYITEM_URL;
							String riURLStr = null;
							if (slotsMap.containsKey(slotName)) {
								riURLStr = (String) slotsMap.get(slotName);

								// Remove transient slot
								slotsMap.remove(slotName);
								eo.getSlot().clear();
								BindingUtility.getInstance().addSlotsToRegistryObject(eo, slotsMap);
							} else if (slotsMap.containsKey(BindingUtility.CANONICAL_SLOT_CONTENT_LOCATOR)) {
								riURLStr = (String) slotsMap.get(BindingUtility.CANONICAL_SLOT_CONTENT_LOCATOR);
								if (isExternalURL(riURLStr)) {
									// Dont import a repository item if URL is
									// external
									riURLStr = null;
								}
							}

							if (riURLStr != null) {
								File riFile = new File(parentDirectory, riURLStr);
								DataHandler riDataHandler = new DataHandler(new FileDataSource(riFile));
								attachMap.put(eo.getId(), riDataHandler);
							}
						}
					}
					LifeCycleManagerImpl lcm = (LifeCycleManagerImpl) (client.getBusinessLifeCycleManager());
					ClientRequestContext context = new ClientRequestContext("RegistryBrowser:importFromFile",
							submitRequest);
					context.setRepositoryItemsMap(attachMap);
					BulkResponse br = lcm.doSubmitObjectsRequest(context);
					JAXRUtility.checkBulkResponse(br);
					displayInfo(resourceBundle.getString("message.info.ImportSuccessful"));
				}
			} catch (JAXBException e) {
				RegistryBrowser.displayError(resourceBundle.getString("message.error.InvalidEbRRSyntax"), e);
			} catch (Exception e) {
				RegistryBrowser.displayError(e);
			}
		} else {
			RegistryBrowser.displayError(resourceBundle.getString("message.error.mustBeLoggedIn"));
		}
	}

	/**
	 * Import RegistryObjects defined in an XML file within a ebRS
	 * SubmitObjectsRequest and publish them to the registry user current user
	 * context.
	 */
	@SuppressWarnings("rawtypes")
	public void exportToFile(Collection registryObjects) {
		FileOutputStream fos = null;
		File zipFile = null;
		try {
			// For now we only handle the case where a single RO with zip RI is
			// being exported.
			if (registryObjects.size() == 0) {
				displayError(resourceBundle.getString("message.info.nothingToExport"));
				return;
			}

			if (registryObjects.size() != 1) {
				// ??I18N
				displayError(resourceBundle.getString("message.error.exactlyOneObjectMustBeSelectedForExport"));
				return;
			}

			Object obj = registryObjects.toArray()[0];

			if (!(obj instanceof ExtrinsicObject)) {
				displayError(CommonResourceBundle.getInstance().getString("message.unexpectedObjectType",
						new Object[] { "javax.xml.registry.infomodel.ExtrinsicObject", obj.getClass().getName() }));
				return;
			}

			ExtrinsicObject eo = (ExtrinsicObject) obj;

			// TODO: Replace with canonical constant
			if (!(eo.getMimeType().equalsIgnoreCase("application/zip"))) {
				// TODO: Add new message that is mimeType specific
				displayError(CommonResourceBundle.getInstance().getString("message.unexpectedObjectType",
						new Object[] { "application/zip", eo.getMimeType() }));
			}

			zipFile = File.createTempFile("JavaUIExportAction", ".zip");
			zipFile.deleteOnExit();
			fos = new FileOutputStream(zipFile);

			DataHandler ri = eo.getRepositoryItem();
			InputStream is = ri.getInputStream();

			// Copy is to fos
			int n;
			byte[] buffer = new byte[1024];
			while ((n = is.read(buffer)) > -1) {
				fos.write(buffer, 0, n);
			}
		} catch (Exception e) {
			displayError(e.getMessage(), e);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				displayError(e);
			}
		}
		displayInfo(resourceBundle.getString("message.info.ExportSuccessful",
				new Object[] { zipFile.getAbsolutePath() }));

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @author $author$
	 * @version $Revision: 1.55 $
	 */
	class SymWindow extends WindowAdapter {
		/**
		 * DOCUMENT ME!
		 * 
		 * @param event
		 *            DOCUMENT ME!
		 */
		public void windowClosing(WindowEvent event) {
			Object object = event.getSource();

			if (object == RegistryBrowser.this) {
				RegistryBrowser_windowClosing(event);
			}
		}
	}

	/**
	 * Class Declaration.
	 * 
	 * @author
	 * @version 1.17, 03/29/00
	 * 
	 * @see
	 */
	class SymAction implements ActionListener {
		/**
		 * Method Declaration.
		 * 
		 * @param event
		 * 
		 * @see
		 */
		public void actionPerformed(ActionEvent event) {
			Object object = event.getSource();

			if (object == saveItem) {
				saveItem_actionPerformed(event);
			} else if (object == exitItem) {
				exitItem_actionPerformed(event);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @author $author$
	 * @version $Revision: 1.55 $
	 */
	class JAXRBrowserClassLoader extends ClassLoader {
		/**
		 * Creates a new JAXRBrowserClassLoader object.
		 * 
		 * @param parent
		 *            DOCUMENT ME!
		 */
		JAXRBrowserClassLoader(ClassLoader parent) {
			log.info(JavaUIResourceBundle.getInstance().getString(
					"message.JAXRBrowserClassLoaderUsingParentClassloader", new Object[] { parent }));
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param className
		 *            DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 * 
		 * @throws ClassNotFoundException
		 *             DOCUMENT ME!
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected Class findClass(String className) throws ClassNotFoundException {
			log.info(JavaUIResourceBundle.getInstance().getString("message.findClass", new Object[] { className }));

			return super.findClass(className);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param className
		 *            DOCUMENT ME!
		 * @param resolve
		 *            DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 * 
		 * @throws ClassNotFoundException
		 *             DOCUMENT ME!
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
			log.info(JavaUIResourceBundle.getInstance().getString("message.loadClassResolve",
					new Object[] { className, new Boolean(resolve) }));

			return super.loadClass(className, resolve);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param className
		 *            DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 * 
		 * @throws ClassNotFoundException
		 *             DOCUMENT ME!
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Class loadClass(String className) throws ClassNotFoundException {
			log.info(JavaUIResourceBundle.getInstance().getString("message.loadClass", new Object[] { className }));

			Class clazz;

			try {
				clazz = super.loadClass(className);
			} catch (ClassNotFoundException e) {
				log.error(e);
				clazz = getParent().loadClass(className);
			}

			return clazz;
		}
	}

}
