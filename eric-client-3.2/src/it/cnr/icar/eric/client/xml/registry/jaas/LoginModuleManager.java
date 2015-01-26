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
package it.cnr.icar.eric.client.xml.registry.jaas;

import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.KeystoreUtil;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.common.CommonProperties;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.security.auth.login.ConfigFile;

/**
 *
 * @author  psterk
 */
public class LoginModuleManager {
    private static final int MAX_FIXED_ATTRIBUTES = 25;
    private static final int MAX_BACKUP_LOGIN_FILES = 10;
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator");
    @SuppressWarnings("unused")
	private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");

    /** Source for default properties. */
    private static ProviderProperties props = ProviderProperties.getInstance();
    
    // Default application name. This is used if a bundled authentication
    // application config file does not exist, or if the 
    // LoginModuleManager(String applicationName) was not called
    private static final String DEFAULT_APPLICATION_NAME = props.getProperty(
        "jaxr-ebxml.security.providerappname", "jaxr-ebxml-provider");
    @SuppressWarnings("unused")
	private static final String DEFAULT_LOGIN_MODULE_CLASSNAME = "com.sun.security.auth.module.KeyStoreLoginModule";
    
    private static final String  DEFAULT_KEYSTORE_FILENAME = getDefaultKeystoreName();
    private static final String ROOT_PROPERTY_NAME ="jaxr-ebxml";
        
    private static boolean createLoginFile = false;
    private static boolean createDefaultLoginFile = false;
    private static boolean getCallbackHandler = false;
    private static final Log log = LogFactory.getLog(LoginModuleManager.class);

    // cached objects
    private String applicationName;
    private CallbackHandler callbackHandler;
    private CallbackHandler defaultCallbackHandler;
    
    private String bundledCfgFileContents;
    private Properties securityProps;
    private Frame parentFrame;

    /**
     * Default constructor<br>
     * Uses jaxr-ebxml-provider as the default login config application name
     */
    public LoginModuleManager() {
        log.debug(JAXRResourceBundle.getInstance().
		  getString("message.LoginModuleManagerUsingFollowingKeystoreFile",
			    new Object[]{DEFAULT_KEYSTORE_FILENAME}));
    }

    /**
     * Alternative constructor<br>
     * The application name is configurable
     *
     * @param applicationName
     *  A String that contains the application name for the login config file
     */
    public LoginModuleManager(String applicationName) {
        if (applicationName == null || applicationName.trim().length() == 0) {
            this.applicationName = DEFAULT_APPLICATION_NAME;
        } else {
            this.applicationName = applicationName;
        }
    }

    private static String getDefaultKeystoreName() {
        String keystoreName = null;
        String ericHome = CommonProperties.getInstance().getProperty("eric.home");
        if (ericHome == null || ericHome.equals("")) {
            ericHome = "${eric.home}";
        }
        String keystore = ProviderProperties.getInstance()
                                            .getProperty("jaxr-ebxml.security.keystore");
        String keystoreType = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storetype", "JKS");
        String fileType = null;
        if (keystoreType.equalsIgnoreCase("JKS")) {
            fileType = ".jks";
        } else if (keystoreType.equalsIgnoreCase("PKCS12")) {
            fileType = ".p12";
        } else {
            fileType = "." + keystoreType.toLowerCase();
        }
        
        if (keystore == null || keystore.equals("")) {
            keystore = File.separatorChar + "security"+ File.separatorChar 
                + "keystore" + fileType;            
        }
        keystoreName = ericHome + File.separatorChar + "jaxr-ebxml" + 
            File.separatorChar + keystore;
        return keystoreName;
    }
    
    /**
     * This method is used to set the parent frame of this class. The
     * reference will be passed to the CallbackHandler implementation to
     * improve the GUI behavior.
     *
     * @param frame
     *   The parent frame used by the CallbackHandler implementation
     */
    public void setParentFrame(Frame frame) {
        parentFrame = frame;
    }

    /**
     * This method is used to get the parent frame of this class.
     *
     * @retrun
     *   The parent frame used by the CallbackHandler implementation
     */
    public Frame getParentFrame() {
        return parentFrame;
    }

    /**
     * This method is used to write the login configuration file required
     * by the LoginContext.  It searches for the java.login.config file in
     * the classpath and writes it to the filesystem.  The LoginContext class
     * will read this file, and instantiate and configure the correct
     * JAAS LoginModules. If the java.login.config file cannot be found, it
     * defaults to the current KeystoreLoginModule.
     *
     * @throws JAXRException
     *  This exception is thrown if the bundled config file is different from
     *  the user config file, and cannot be written to the filesystem. If there
     *  is no bundled config file, this exception is thrown if there is a
     *  problem writing the default config file to the filesystem
     */
    public void createLoginConfigFile() throws JAXRException {
        log.trace("start creating login config file");

        // first look for java.login.config in the classpath
        String bundledCfgFileContents = getBundledCfgFileContents();

        // check if  keystore file is there
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.canReadKeystoreFile(keystoreFile);

        // if java.login.config does not exist, 
        // call createDefaultLoginConfigFile()
        if (bundledCfgFileContents == null) {
            createDefaultLoginConfigFile();
        } else {
            createLoginConfigFileInternal(bundledCfgFileContents);
        }

        log.trace("finish creating login config file");
    }

    /**
     * This method is used to get the application name from the bundled config
     * file.  If this file does not exist, it defaults to 'jaxr-ebxml-provider'
     *
     * @return
     *  A String containing the application name
     */
    public String getApplicationName() {
        log.trace("start getting application name");

        if (applicationName == null) {
            try { // try to get application from bundled config file             

                String bundledCfgFileContents = getBundledCfgFileContents();

                if (bundledCfgFileContents != null) {
                    applicationName = getLoginName(bundledCfgFileContents);
                }

                log.info(JAXRResourceBundle.getInstance().getString("message.authenticationApplicationName", new Object[]{applicationName}));
            } catch (Throwable t) {
                log.warn(JAXRResourceBundle.getInstance().getString("message.problemReadingBundledLoginConfigFileUseDefault", new Object[]{DEFAULT_APPLICATION_NAME}), t);
            }

            // if bundled config does not exist or there is a problem loading it
            // use default
            if (applicationName == null) {
                log.warn(JAXRResourceBundle.getInstance().getString("message.problemReadingBundledLoginConfigFileUseDefault", new Object[]{DEFAULT_APPLICATION_NAME}));
                applicationName = DEFAULT_APPLICATION_NAME;
            }
        }

        log.trace("finish getting application name");

        return applicationName;
    }

    /**
     * This method is used to set the default CallbackHandler. If the
     * jaxr-ebxml.security.jaas.callbackHandlerClassName property is not set,
     * this default CallbackHandler will be used.
     *
     * @param handler
     *  A javax.security.auth.callback.CallbackHandler implementation
     *  provided by the user
     */
    public void setDefaultCallbackHandler(CallbackHandler handler) {
        defaultCallbackHandler = handler;
    }

    /**
     * This method is used to get the CallbackHandler from the bundled
     * properties file.  It reads the
     * jaxr-ebxml.security.jaas.callbackHandlerClassName property.
     * If this file or property does not exist, it defaults to
     * com.sun.xml.registry.client.jaas.DialogAuthenticationCallbackHandler.
     *
     * @return
     *  An instance of the CallbackHandler interface
     */
    public CallbackHandler getCallbackHandler() throws JAXRException {
        log.trace("start getting CallbackHandler name");

        if (callbackHandler == null) {
            Properties properties = JAXRUtility.getBundledClientProperties();
            String callbackHandlerClassName = properties.getProperty(ROOT_PROPERTY_NAME +
                    ".security.jaas.callbackHandlerClassName");
            
            // over ride with system properties. The system property is used
            // by the thick client. The file property is used by the thin client
            String callbackHandlerClassNameFromSystem = System.getProperty(ROOT_PROPERTY_NAME +
                    ".security.jaas.callbackHandlerClassName");
            
            if (callbackHandlerClassNameFromSystem != null) {
                callbackHandlerClassName = callbackHandlerClassNameFromSystem;
            }
            
            if (callbackHandlerClassName != null) {
                Class<?> clazz = null;

                try {
                    clazz = Class.forName(callbackHandlerClassName);
                } catch (ClassNotFoundException ex) {
                    log.error(ex);
                    throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.instantiate.CallbackHandler",new Object[] {callbackHandlerClassName}));
                }

                Class<?>[] clazzes = new Class[1];
                clazzes[0] = java.awt.Frame.class;

                Constructor<?> constructor = null;

                try {
                    constructor = clazz.getDeclaredConstructor(clazzes);

                    Object[] objs = new Object[1];
		    Frame frame = getParentFrame();
                    if (frame != null) {
                        objs[0] = frame;
                    }
                    callbackHandler = (CallbackHandler) constructor.newInstance(objs);
                } catch (NoSuchMethodException ex) {
                    log.debug("Could not find constructor that takes a Frame " +
			      "parameter. Trying default constructor");

                    // use default constructor instead
                    try {
                        callbackHandler = (CallbackHandler) Class.forName(callbackHandlerClassName)
                                                                  .newInstance();
                    } catch (Throwable t) {
                        log.error(t);
                        throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.instantiate.CallbackHandler.classpath", new Object[] {callbackHandlerClassName}));
                    }
                } catch (Throwable t) {
                    log.error(t.getMessage());
                    throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.instantiate.CallbackHandler", new Object[] {callbackHandlerClassName}));
                }
            }
            if ( callbackHandler == null) {
                log.info(JAXRResourceBundle.getInstance().getString("message.UsingDefaultCallbackHandler"));

                // If set, user default CallbackHandler provided by user
                if ( defaultCallbackHandler != null) {
                    callbackHandler = defaultCallbackHandler;
                } else // Use default CallbackHandler that loads credentials
                    // from client-side keystore file.
                 {
                    callbackHandler = new ThinClientCallbackHandler();
                }
            }

            log.info(JAXRResourceBundle.getInstance().getString("message.CallbackHandlerName", new Object[]{callbackHandler.getClass().getName()}));
        }

        log.trace("finish getting CallbackHandler name");

        return callbackHandler;
    }

    /**
     * This method is used to create the default login configuration file.
     * Currently, the default file is for the
     * com.sun.security.auth.module.KeystoreLoginModule
     *
     * @throws JAXRException
     *  This is thrown if there is a problem writing the default login config
     *  file to the filesystem
     */
    public void createDefaultLoginConfigFile() throws JAXRException {
        log.trace("start creation of default login config file");

        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.canReadKeystoreFile(keystoreFile);

        // This property should always be set by java
        String userHomeFileName = System.getProperty("user.home");

        if ((userHomeFileName == null) || (userHomeFileName.length() == 0)) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.find.system.property"));
        }

        File configFile;
        // Login config filename might be define as system property
        String configFileName = System.getProperty("java.security.auth.login.config");
        if (configFileName != null) {
            configFile = new File(configFileName);
        } else {
            configFile = new File(userHomeFileName, ".java.login.config");
        }

        if (configFile.exists()) {
            if (configFile.canRead()) {
                Configuration config = ConfigFile.getConfiguration();
                String appName = getApplicationName();
                AppConfigurationEntry[] defaultAppConfigEntries = getReloadedAppConfigurationEntries(config,
                        configFile.getPath() + ".tmp",
                        getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME +
                            ".tmp"), appName + ".tmp");
                AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(appName);

                //TODO: Paul to verify this!! What if one of the Entries is null??
                boolean isCorrect;
                if (defaultAppConfigEntries == null && userAppConfigEntries == null) {
                    // this will happen when using constructor LoginModuleManager(String applicationName)
                    // and not having an entry for 'applicationName' in .java.login.config
                    isCorrect = true;
                } else if (defaultAppConfigEntries != null && userAppConfigEntries == null) {
                    // force add default to existing cfg file
                    isCorrect = false;
                } else {
                    isCorrect = checkLoginModules(userAppConfigEntries,
                        defaultAppConfigEntries);
                }

                // if the user has a login config file with the same app name
                // as the default, but the login modules are different, rename
                // the existing user login config file and write the default
                // config file in place of the existing
                if (!isCorrect) {
                    String userCfgFileName = configFile.getPath();
                    String userCfgFileContent = getUserCfgFileContents(userCfgFileName);
                    log.warn(JAXRResourceBundle.getInstance().getString("message.UserLoginConfigFileDoesNotHaveTheSameLoginModulesAsTheDefault"));
                    renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
                    writeCfgFile(configFile, userCfgFileContent + LINE_SEPARATOR
                        + getDefaultConfigFileContents(), false);
                    config.refresh();
                    log.info(JAXRResourceBundle.getInstance().getString("message.createdNewLoginConfigFile", new Object[]{configFile.getName()}));
                } else {
                    log.info(JAXRResourceBundle.getInstance().getString("message.usingExistingConfigFile", new Object[]{configFile.getName()}));

                    return;
                }
            } else {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.file.not.readable", new Object[] {configFile.getAbsolutePath()}));
            }
        } else {
            writeCfgFile(configFile, getDefaultConfigFileContents(), false);
            log.info(JAXRResourceBundle.getInstance().getString("message.createdNewLoginConfigFile", new Object[]{configFile.getName()}));
        }

        log.trace("finish creation of default login config file");
    }

    /*************************************************************************
     * private methods
     *************************************************************************/
    @SuppressWarnings("unused")
	private boolean getDebugSetting() {
        boolean debug = false;
        Configuration config = ConfigFile.getConfiguration();
        AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(getApplicationName());

        for (int i = 0; i < userAppConfigEntries.length; i++) {
            Map<String, ?> options = userAppConfigEntries[i].getOptions();
            String debugStr = (String) options.get("debug");

            if (debugStr != null) {
                if (debugStr.equalsIgnoreCase("true")) {
                    debug = true;
                }

                break;
            }
        }

        return debug;
    }

    @SuppressWarnings("unused")
	private String getBundledCfgFilename() {
        URL url = this.getClass().getClassLoader().getResource("java.login.config");
        String fileName = url.getFile();

        return fileName;
    }

    private String getBundledCfgFileContents() {
        if ( bundledCfgFileContents == null) {
            BufferedReader in = null;

            try {
                InputStream cfgFileInputStream = this.getClass().getClassLoader()
                                                     .getResourceAsStream("it/cnr/icar/eric/client/xml/registry/util/jaxr.java.login.config");

                if (cfgFileInputStream != null) {
                    log.info(JAXRResourceBundle.getInstance().getString("message.foundLoginConfigFile"));

                    StringBuffer sb = new StringBuffer();
                    String line = null;
                    in = new BufferedReader(new InputStreamReader(
                                cfgFileInputStream));

                    while ((line = in.readLine()) != null) {
                        sb.append(line).append(LINE_SEPARATOR);
                    }

                    bundledCfgFileContents = sb.toString();
                    checkKeystoreOption();
                }
            } catch (IOException ex) {
                log.warn(JAXRResourceBundle.getInstance().getString("message.problemReadingBundledLoginConfigFileUseDefault1"), ex);
                bundledCfgFileContents = null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore
                	in = null;
                }
            }
        }

        return bundledCfgFileContents;
    }

    private void createLoginConfigFileInternal(String bundledFileContents)
        throws JAXRException {
        log.trace("start creating login config file - internal");

        try {
            String userCfgFileName = getUserCfgFileName();
            log.info(JAXRResourceBundle.getInstance().getString("message.UsersConfigFileName", new Object[]{userCfgFileName}));

            String userCfgContents = getUserCfgFileContents(userCfgFileName);

            // if the user doesn't have an existing login cfg file, write the
            // bundled one
            if (userCfgContents == null) {
                writeCfgFile(userCfgFileName, bundledFileContents, false);
            } else {
                // the user has an existing login cfg file. Use this method
                // to compare both files and take appropriate actions
                checkUserCfgFile(userCfgFileName, userCfgContents,
                    bundledFileContents);
            }
        } catch (Throwable t) {
            log.warn(JAXRResourceBundle.getInstance().getString("message.problemReadingConfigFileUsingDefault"), t);

            // problem reading config file.  Use default
            createDefaultLoginConfigFile();
        }

        log.trace("finish creating login config file - internal");
    }

    private void checkUserCfgFile(String userCfgFileName,
        String userCfgContents, String bundledFileContents)
        throws JAXRException {
        String userLoginName = getLoginName(userCfgContents);
        String bundledLoginName = getLoginName(bundledFileContents);

        // if the login names are the same, check attributes
        if (userLoginName.equalsIgnoreCase(bundledLoginName)) {
            // this method checks that any required attributes are present and
            // that fixed attributes are set according to the settings in the
            // bundled jaxr-ebxml.properties file.            
            Configuration config = ConfigFile.getConfiguration();
            String appName = getApplicationName();
            AppConfigurationEntry[] bundledAppConfigEntries = getReloadedAppConfigurationEntries(config,
                    userCfgFileName + ".tmp", bundledFileContents, appName);
            AppConfigurationEntry[] userAppConfigEntries = config.getAppConfigurationEntry(appName);
            boolean isCorrect = areUserCfgFileAttributesCorrect(userAppConfigEntries,
                    bundledAppConfigEntries);

            // if the user cfg content has changed, write it to the user cfg
            // file
            if (!isCorrect) {
                log.warn(JAXRResourceBundle.getInstance().getString("message.UserLoginConfigFileIsNotCorrectUsingBundledConfigFileInstead"));
                renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
                log.info(JAXRResourceBundle.getInstance().getString("message.RenamedToBakFile", new Object[]{userCfgFileName}));
                writeCfgFile(userCfgFileName, bundledFileContents, false);
                ConfigFile.getConfiguration().refresh();
                log.info(JAXRResourceBundle.getInstance().getString("message.createdNewLoginFile", new Object[]{userCfgFileName}));
            } else {
                // if the user has a different keystore file in the 
                // jaxr-ebxml.properties file, update the user's config file
                // automatically.
                //TODO: check that it will not delete other entries
                updateUserCfgContents(userAppConfigEntries, userCfgContents,
                    userCfgFileName);
            }
        } else {
            // the existing login name in different than the bundled. So, move 
            // the existing user cfg file to a backup file
            renameCfgFile(userCfgFileName, userCfgFileName + ".bak");
            writeCfgFile(userCfgFileName, bundledFileContents, false);
        }
    }

    private void renameCfgFile(String fileName, String renamedFileName)
        throws JAXRException {
        try {
            File file = new File(fileName);
            File renamedFile = new File(renamedFileName);

            if (renamedFile.exists()) {
                for (int i = 2; i <= MAX_BACKUP_LOGIN_FILES; i++) {
                    String tempFileName = renamedFileName + i;
                    File tempFile = new File(tempFileName);

                    if (!tempFile.exists()) {
                        file.renameTo(tempFile);
                        log.debug("renaming config file " + fileName + " to " +
                            renamedFileName);

                        break;
                    }
                }
            } else {
                file.renameTo(renamedFile);
            }
        } catch (SecurityException ex) {
            throw new JAXRException(ex);
        }
    }

    private boolean areUserCfgFileAttributesCorrect(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries)
        throws JAXRException {
        boolean isCorrect = false;
        isCorrect = checkLoginModules(userAppConfigEntries,
                bundledAppConfigEntries);

        if (!isCorrect) {
            return isCorrect;
        }

        isCorrect = checkControlFlag(userAppConfigEntries,
                bundledAppConfigEntries);

        if (!isCorrect) {
            return isCorrect;
        }

        isCorrect = checkLoginModuleOptions(userAppConfigEntries,
                bundledAppConfigEntries);

        return isCorrect;
    }

    private AppConfigurationEntry[] getReloadedAppConfigurationEntries(
        Configuration config, String cfgFileName, String cfgFileContents,
        String appConfigName) throws JAXRException {
        AppConfigurationEntry[] appConfigEntries = null;

        // if there is an IOException, we do not have permission to write
        // to the local filesystem.  Without this permission, we cannot
        // control the authentication.  In this case, throw new 
        // JAXRException to notify the user to give us permission
        try {
            File file = new File(cfgFileName);
            writeCfgFile(file, cfgFileContents, false);
        } catch (Throwable t) {
            log.error(t);
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.permission.wirte.local.filesystem"));
        }

        String javaSecLoginCfg = System.getProperty(
                "java.security.auth.login.config");
        String userCfgFileName = getUserCfgFileName();
        System.setProperty("java.security.auth.login.config", cfgFileName);
        config.refresh();
        appConfigEntries = config.getAppConfigurationEntry(appConfigName);

        try {
            deleteCfgFile(cfgFileName);
        } catch (Throwable t) {
            log.warn(JAXRResourceBundle.getInstance().getString("message.problemDeletingConfigFile"), t);
        } finally {
            if (javaSecLoginCfg != null) {
                System.setProperty("java.security.auth.login.config",
                    javaSecLoginCfg);
            } else {
                System.setProperty("java.security.auth.login.config",
                    userCfgFileName);
            }

            config.refresh();
        }

        return appConfigEntries;
    }

    /*
     * The login module names in the user's config file must appear in the
     * same order as the bundled config file
     */
    private boolean checkLoginModules(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = false;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            isCorrect = true;

            try { // user login modules must appear in the same order as the
                  // bundled ones

                String bundledLoginModuleName = bundledAppConfigEntries[i].getLoginModuleName();
                String userLoginModuleName = userAppConfigEntries[i].getLoginModuleName();

                if (!bundledLoginModuleName.equals(userLoginModuleName)) {
                    isCorrect = false;

                    break;
                }
            } catch (Throwable t) {
                // If the user config file has missing login module, it will
                // be caught here
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug(
                "login module(s) in the existing login config file are ok");
        } else {
            log.warn(JAXRResourceBundle.getInstance().getString("message.theLoginModuleContainedExistingLoginConfigFile"));
        }

        return isCorrect;
    }

    private boolean checkControlFlag(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = true;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            try {
                AppConfigurationEntry.LoginModuleControlFlag bundledFlag = bundledAppConfigEntries[i].getControlFlag();
                String bundledFlagStr = bundledFlag.toString();
                AppConfigurationEntry.LoginModuleControlFlag userFlag = userAppConfigEntries[i].getControlFlag();
                String userFlagStr = userFlag.toString();

                if (!bundledFlagStr.equals(userFlagStr)) {
                    isCorrect = false;

                    break;
                }
            } catch (Throwable t) {
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug(
                "control flag(s) in the existing login config file are ok");
        } else {
            log.warn(JAXRResourceBundle.getInstance().getString("message.theControlFlagsDoNotMatch"));
        }

        return isCorrect;
    }

    private boolean checkLoginModuleOptions(
        AppConfigurationEntry[] userAppConfigEntries,
        AppConfigurationEntry[] bundledAppConfigEntries) {
        boolean isCorrect = true;

        for (int i = 0; i < bundledAppConfigEntries.length; i++) {
            Map<String, ?> userOptions = userAppConfigEntries[i].getOptions();
            Map<String, ?> bundledOptions = bundledAppConfigEntries[i].getOptions();
            isCorrect = doAllUserOptionExist(userOptions, bundledOptions);

            if (!isCorrect) {
                break; // problem with options; stop checking
            }

            String loginModuleName = bundledAppConfigEntries[i].getLoginModuleName();
            isCorrect = areAllUserOptionsSetToCorrectValues(userOptions,
                    loginModuleName);

            if (!isCorrect) {
                break; // problem with options; stop checking
            }
        }

        return isCorrect;
    }

    private boolean doAllUserOptionExist(Map<String, ?> userOptions, Map<String, ?> bundledOptions) {
        boolean isCorrect = true;
        Iterator<String> bundledOptionsIter = bundledOptions.keySet().iterator();

        while (bundledOptionsIter.hasNext()) {
            String bundledOption = bundledOptionsIter.next();
            String userOption = (String) userOptions.get(bundledOption);

            if (userOption == null) {
                log.warn(JAXRResourceBundle.getInstance().
			 getString("message.TheFollowingOptionIsMissingInTheExistingLoginConfigFile",
				   new Object[]{bundledOption}));
                isCorrect = false;

                break;
            }
        }

        if (isCorrect) {
            log.debug("All options exist in the existing login config file");
        }

        return isCorrect;
    }

    private boolean areAllUserOptionsSetToCorrectValues(Map<String, ?> userOptions,
        String loginModuleFullName) {
        boolean isCorrect = true;
        int lastPeriodIndex = loginModuleFullName.lastIndexOf('.');
        String loginModuleName = loginModuleFullName.substring(lastPeriodIndex +
                1, loginModuleFullName.length());
        Properties properties = JAXRUtility.getBundledClientProperties();

        if (properties == null) {
            return isCorrect;
        }

        String partialAttributeKey = ROOT_PROPERTY_NAME + ".security.jaas." +
            loginModuleName + ".attribute.";
        String partialFixedKey = ROOT_PROPERTY_NAME + ".security.jaas." +
            loginModuleName + ".fixedValue.";

        for (int j = 1; j <= MAX_FIXED_ATTRIBUTES; j++) {
            String attributeKey = partialAttributeKey + j;
            String attributeValue = null;

            try {
                attributeValue = properties.getProperty(attributeKey);
            } catch (MissingResourceException ex) {
                // ignore - try to load attribute.1 through
                // attribute.MAX_FIXED_ATTRIBUTES
            }

            if (attributeValue != null) {
                String fixedKey = partialFixedKey + j;
                String fixedValue = null;

                try {
                    fixedValue = properties.getProperty(fixedKey);
                } catch (MissingResourceException ex) {
                    // ignore - try to load fixedValue.1 through
                    // fixedValue.MAX_FIXED_ATTRIBUTES
                }

                if (fixedValue != null) {
                    String optionValue = (String) userOptions.get(attributeValue);

                    if ((optionValue == null) ||
                            !optionValue.equalsIgnoreCase(fixedValue)) {
                        // integrity check has failed
                        // break and return 'false'
                        log.warn(JAXRResourceBundle.getInstance().getString("message.TheFollowingOptionIsNotSetProperly", new Object[]{attributeValue}));
                        log.warn(JAXRResourceBundle.getInstance().getString("message.ItIsSetTo", new Object[]{optionValue}));
                        log.warn(JAXRResourceBundle.getInstance().getString("message.ItShouldBeSetTo", new Object[]{fixedValue}));
                        isCorrect = false;

                        break;
                    }
                }
            }
        }

        if (isCorrect) {
            log.debug("all user config file options are set properly");
        }

        return isCorrect;
    }

    private Properties getSecurityProperties() {
        if ( securityProps == null) {
            securityProps = new Properties();

            BufferedInputStream bis = null;
            String fileName = null;

            try {
                String javaHome = System.getProperty("java.home");
                String dirSep = System.getProperty("file.separator");
                StringBuffer sb = new StringBuffer(javaHome);
                sb.append(dirSep).append("lib").append(dirSep);
                sb.append("security").append(dirSep);
                sb.append("java.security");
                fileName = sb.toString();
                log.info(JAXRResourceBundle.getInstance().getString("message.FoundJavasecurityProperties", new Object[]{fileName}));

                File file = new File(fileName);
                bis = new BufferedInputStream(new FileInputStream(file));
                securityProps.load(bis);
            } catch (IOException ex) {
                log.warn(JAXRResourceBundle.getInstance().getString("message.couldNotOpenJavaSecurityFile"),
                    ex);
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException ex) {
                    // ignore
                	bis = null;
                }
            }
        }

        return securityProps;
    }

    private String getLoginName(String cfgFileContents) {
        int firstSpaceIndex = cfgFileContents.indexOf(' ');

        return (cfgFileContents.substring(0, firstSpaceIndex));
    }

    private void deleteCfgFile(String cfgFile) throws JAXRException {
        try {
            File file = new File(cfgFile);
            boolean isDeleted = file.delete();

            if (!isDeleted) {
                System.out.println("warning: could not delete tmp file");
            }
        } catch (Throwable t) {
            throw new JAXRException(t);
        }
    }

    private void writeCfgFile(File configFile, String cfgFileContents, boolean append)
        throws JAXRException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(configFile, append));
            writer.write(cfgFileContents, 0, cfgFileContents.length());
            writer.flush();
        } catch (IOException ex) {
            throw new JAXRException(ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    // ignore
                	writer = null;
                }
            }
        }
    }

    private void writeCfgFile(String userCfgFileName, String fileContents, boolean append)
        throws JAXRException {
        File file = new File(userCfgFileName);
        writeCfgFile(file, fileContents, append);
    }

    private String getUserCfgFileName() {
        String userCfgFileName = null;

        try {
            @SuppressWarnings("unused")
			Properties securityProps = getSecurityProperties();

            // Ignore the login.configuration.provider property in this
            // release. Alternative provider implementations can be supported
            // in subsequent releases.
            // We do not support login.config.url.1 in this release
            userCfgFileName = System.getProperty(
                    "java.security.auth.login.config");

            if ((userCfgFileName == null) || userCfgFileName.equals("")) {
                userCfgFileName = getDefaultUserCfgFileName();
            }
        } catch (Throwable t) {
            log.warn(JAXRResourceBundle.getInstance().getString("message.problemGettingUserConfigFile"), t);
            userCfgFileName = getDefaultUserCfgFileName();
        }

        return userCfgFileName;
    }

    private String getDefaultUserCfgFileName() {
        StringBuffer sb = new StringBuffer(System.getProperty("user.home"));
        sb.append(System.getProperty("file.separator"));
        sb.append(".java.login.config");

        return (sb.toString());
    }

    private String getUserCfgFileContents(String userHomeFileName)
        throws JAXRException {
        String fileContents = null;
        BufferedReader in = null;

        try {
            File configFile = new File(userHomeFileName);

            if (configFile.exists()) {
                StringBuffer sb2 = new StringBuffer();
                String line = null;
                in = new BufferedReader(new FileReader(configFile));

                while ((line = in.readLine()) != null) {
                    sb2.append(line).append(LINE_SEPARATOR);
                }

                fileContents = sb2.toString();
            }

            log.debug("user config file contents: " + fileContents);

            return fileContents;
        } catch (IOException ex) {
            throw new JAXRException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore
            	in = null;
            }
        }
    }

    private String getDefaultConfigFileContents() {
        return getDefaultConfigFileContents(DEFAULT_APPLICATION_NAME);
    }

    private String getDefaultConfigFileContents(String appName) {
        String defaultConfigFileContents = null;
        String keystoreType = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storetype", "JKS");

        
        if (appName == null) {
            appName = DEFAULT_APPLICATION_NAME;
        }

        String keystoreFileName = DEFAULT_KEYSTORE_FILENAME;

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);
            keystoreFileName = keystoreFile.toURI().toURL().getFile();
        } catch (Throwable ex) {
            // Since keystoreFileName is already set to default, ignore
        }

        StringBuffer sb = new StringBuffer(appName);
        sb.append(" { " + LINE_SEPARATOR);
        sb.append("    com.sun.security.auth.module.KeyStoreLoginModule ");
        sb.append("required ").append(LINE_SEPARATOR);
        sb.append("    keyStoreType=\"" + keystoreType + "\"" + LINE_SEPARATOR);
        sb.append("    debug=true keyStoreURL=\"file:");
        sb.append(keystoreFileName).append("\";");
        sb.append(LINE_SEPARATOR).append("};").append(LINE_SEPARATOR);
        defaultConfigFileContents = sb.toString();
        log.debug("Default config file contents: " + defaultConfigFileContents);

        return defaultConfigFileContents;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                String argFlag = args[i];

                if (argFlag.equals("-d")) {
                    createDefaultLoginFile = true;
                } else if (argFlag.equals("-l")) {
                    createLoginFile = true;
                } else if (argFlag.equals("-c")) {
                    getCallbackHandler = true;
                } else { // if no flags execute all methods
                    createDefaultLoginFile = true;
                    createLoginFile = true;
                    getCallbackHandler = true;
                }
            } catch (RuntimeException ex) {
                // use defaults
            	continue;
            }
        }

        if (args.length == 0) {
            createLoginFile = true;
        }

        LoginModuleManager loginModuleMgr = new LoginModuleManager();

        try {
            if (createDefaultLoginFile) {
                log.info(JAXRResourceBundle.getInstance().getString("message.startingCreateDefaultLoginConfigFile"));
                loginModuleMgr.createDefaultLoginConfigFile();
            }

            if (createLoginFile) {
                log.info(JAXRResourceBundle.getInstance().getString("message.startingCreateLoginConfigFile"));
                loginModuleMgr.createLoginConfigFile();
            }

            if (getCallbackHandler) {
                log.info(JAXRResourceBundle.getInstance().getString("message.startingGetCallbackHandler"));
                loginModuleMgr.getCallbackHandler();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void updateUserCfgContents(
        AppConfigurationEntry[] userAppConfigEntries, String userCfgContents,
        String cfgFileName) {
        for (int i = 0; i < userAppConfigEntries.length; i++) {
            Map<String, ?> userOptions = userAppConfigEntries[i].getOptions();
            String userKeystoreFile = (String) userOptions.get("keyStoreURL");

            if (userKeystoreFile != null) {
                try {
                    File keystoreFile = KeystoreUtil.getKeystoreFile();
                    KeystoreUtil.canReadKeystoreFile(keystoreFile);

                    String keystoreFileInPropFile = keystoreFile.toURI().toURL()
                                                                .getFile();

                    if (keystoreFileInPropFile != null) {
                        // strip the substring "file:" in userKeystoreFile 
                        // before doing comparison.
                        int searchstrindex = userKeystoreFile.indexOf("file:");
                        if (searchstrindex == -1) {
                            searchstrindex = 0;
                        } 
                        else {
                            searchstrindex +=5;
                        }
                        String userKeystoreFilePath = userKeystoreFile.substring(searchstrindex);
                        if (!userKeystoreFilePath.equals(keystoreFileInPropFile)) {
                            String keyStoreURL = "keyStoreURL=\"file:";
                            int keyStartIndex = userCfgContents.indexOf(keyStoreURL);
                            keyStartIndex += keyStoreURL.length();

                            int keyEndIndex = userCfgContents.indexOf("\";",
                                    keyStartIndex);

                            String firstPart = userCfgContents.substring(0,
                                    keyStartIndex);
                            String lastPart = userCfgContents.substring(keyEndIndex);

                            // combine all parts
                            StringBuffer sb = new StringBuffer(firstPart);
                            sb.append(keystoreFileInPropFile).append(lastPart);
                            userCfgContents = sb.toString();
                            writeCfgFile(cfgFileName, userCfgContents, true);
                        }
                    }
                } catch (Throwable t) {
                    // ignore
                	continue;
                }
            }
        }
    }

    private void checkKeystoreOption() {
        String keyStoreURL = "keyStoreURL=\"file:";
        int keyStartIndex = bundledCfgFileContents.indexOf(keyStoreURL);
        keyStartIndex += keyStoreURL.length();

        int keyEndIndex = bundledCfgFileContents.indexOf("\";", keyStartIndex);

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            // KeystoreUtil.canReadKeystoreFile(keystoreFile);

            String firstPart = bundledCfgFileContents.substring(0,
                    keyStartIndex);
            String keystoreFileName = keystoreFile.toURI().toURL().getFile();
            String lastPart = bundledCfgFileContents.substring(keyEndIndex);

            // combine all parts
            StringBuffer sb = new StringBuffer(firstPart);
            sb.append(keystoreFileName).append(lastPart);
            bundledCfgFileContents = sb.toString();
            KeystoreUtil.canReadKeystoreFile(keystoreFile);
        } catch (Throwable ex) {
            // ignore - use existing bundledCfgFileContents
        	return;
        }
    }
}
