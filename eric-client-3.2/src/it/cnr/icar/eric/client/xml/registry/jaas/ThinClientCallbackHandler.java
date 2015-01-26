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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

import java.awt.Frame;

import java.util.ResourceBundle;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;




/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: *
 * <p>Company: </p>
 * @author Paul Sterk
 * @version 1.0
 *
 * This class is a CallbackHandler implementation that is suitable for a thi client server
 * logging into a server-side keystore on behalf of the user.
 * The handle(Callback[]) method has been customized such that it can only handle the login method
 * of LoginContext.
 */
public class ThinClientCallbackHandler implements CallbackHandler {
    /* The ResourceBundle for Sun's Auth package. */
    @SuppressWarnings("unused")
	private static final ResourceBundle authResBundle = ResourceBundle.getBundle(
            "sun.security.util.AuthResources");
    @SuppressWarnings("unused")
	private Frame ownerFrame;
    private static final Log log = LogFactory.getLog(ThinClientCallbackHandler.class);
    private boolean handleStorePass = true;
    
    /**
     * Default constructor
     */
    public ThinClientCallbackHandler() {
    }

    /** Implementation of the handle method specified by
     * <code> javax.security.auth.callback.CallbackHandler </code>
     * @param callbacks <code>Array of 
     * javax.security.auth.callback.CallbackHandler</code>
     *
     */
    public void handle(Callback[] callbacks)
        throws UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                // Ignore this section for now. This will be used when a generic callback handler
                // is being implemented. In our current implementation, we are only expecting the
                //login type callback handler.
            } else if (callbacks[i] instanceof NameCallback) {
                // For now hard-code the alias of the the RegistryOperator account
                NameCallback nc = (NameCallback) callbacks[i];
                String alias = ProviderProperties.getInstance().
                    getProperty("jaxr-ebxml.security.alias");
                if (alias == null) {
                    String message = "Error: the jaxr-ebxml.security.alias "+
                        "property must be set";
                    log.error(message);
                    System.err.println(message);
                    alias = "";
                }
                nc.setName(alias);
            } else if (callbacks[i] instanceof PasswordCallback) {
                // For now hard-code the password of the the RegistryOperator account
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                char[] password = null;
                if (handleStorePass) {
                    String storepass = ProviderProperties.getInstance().
                        getProperty("jaxr-ebxml.security.storepass");
                    if (storepass == null) {
                        storepass = "ebxmlrr";
                    }
                    password = storepass.toCharArray();
                    handleStorePass = false;
                } else {
                    String keypass = ProviderProperties.getInstance().
                        getProperty("jaxr-ebxml.security.keypass");
                    if (keypass == null) {
                        String message = "Error: the jaxr-ebxml.security.keypass "+
                            "property must be set";
                        log.error(message);
                        System.err.println(message);
                        keypass = "";
                    }
                    password = keypass.toCharArray();
                }
                pc.setPassword(password);
            } else if (callbacks[i] instanceof ConfirmationCallback) {
                ConfirmationCallback cc = (ConfirmationCallback) callbacks[i];
                cc.setSelectedIndex(ConfirmationCallback.OK);
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                    JAXRResourceBundle.getInstance().getString("message.error.unrecognized.callback"));
            }
        }
    }
}
