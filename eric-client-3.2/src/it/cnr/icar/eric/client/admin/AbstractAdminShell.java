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
package it.cnr.icar.eric.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.cnr.icar.eric.common.BindingUtility;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import java.text.Collator;

import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public abstract class AbstractAdminShell implements AdminShell {
    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(AbstractAdminShell.class.getName());

    static final BindingUtility bu = BindingUtility.getInstance();
    static final Collator collator;
    static AdminFunctionContext context;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);

        context = AdminFunctionContext.getInstance();

        String editor = System.getProperty("EDITOR");

        if ((editor == null) || editor.equals("")) {
            String platform = System.getProperty("os.name");

            if (platform.startsWith("Windows")) {
                editor = "notepad.exe";
            } else {
                // assume some flavor of Unix with vi editor
                editor = "/bin/vi";
            }
        }

        context.setEditor(editor);
    }

    AdminResourceBundle rb = AdminResourceBundle.getInstance();
    ResourceBundle functions = ResourceBundle.getBundle(ADMIN_SHELL_FUNCTIONS);

    RegistryPackage currentRP;
    boolean debug;
    File localDir;
    Properties properties;
    RegistryObject[] registryObjects;
    JAXRService service;
    boolean verbose;

    public void run(InputStream inStream, PrintStream outStream)
        throws AdminException {
        throw new AdminException(rb.getString(ADMIN_SHELL_RESOURCES_PREFIX +
					      "unimplemented"));
    }

    public void setLocalDir(File localDir) {
	this.localDir = localDir;
	context.setLocalDir(localDir);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        context.setDebug(debug);
    }

    public void setProperties(Properties properties) {
        if (properties.containsKey("debug")) {
            setDebug(Boolean.valueOf(properties.getProperty("debug"))
                            .booleanValue());
        }

        if (properties.containsKey("verbose")) {
            setVerbose(Boolean.valueOf(properties.getProperty("verbose"))
                              .booleanValue());
        }

        context.setProperties(properties);
    }

    public void setRoot(RegistryPackage rootRP) {
        this.currentRP = rootRP;
        context.setCurrentRP(rootRP);
    }

    public void setService(JAXRService service) {
        if (service == null) {
            throw new NullPointerException();
        }

        this.service = service;
        context.setService(service);
    }

    public void setSQLSelect(String sqlQuery) throws AdminException {
        if (sqlQuery == null) {
            throw new NullPointerException();
        }

        //assert context.getService() != null;
        try {
            Collection<?> coll = context.getService().doSQLQuery(sqlQuery);
            context.setRegistryObjects(coll);
        } catch (Exception e) {
            throw new AdminException(e);
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        context.setVerbose(verbose);
    }

    public void verifySettings() throws AdminException {
    }
}
