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

import it.cnr.icar.eric.common.BindingUtility;

import java.io.PrintStream;

import java.text.Collator;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;


public abstract class AbstractAdminFunction implements AdminFunction {
    public static final String ADMIN_FUNCTION_RESOURCES_PREFIX =
        "AdminFunctionResources.";

    protected static final BindingUtility bu = BindingUtility.getInstance();

    protected static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.IDENTICAL);
    }

    protected AdminResourceBundle rb = AdminResourceBundle.getInstance();

    protected AdminFunctionContext context;
    protected RegistryPackage      currentRP;
    protected boolean              debug;
    protected PrintStream          outStream;
    protected Collection<?>           registryObjects;
    protected boolean              verbose;

    public void execute(AdminFunctionContext context,
			String args) throws Exception {
	throw new AdminException(format(rb, "unimplemented"));
    }

    public void execute(AdminFunctionContext context,
			String[] args) throws Exception {
	throw new AdminException(format(rb, "unimplemented"));
    }

    public AdminFunctionContext getContext() {
	return context;
    }

    public String getUsage() {
	return null;
    }

    public void help(AdminFunctionContext context,
		     String args) throws Exception {
	context.printMessage(getUsage());
    }

    public void setContext(AdminFunctionContext context) {
	this.context = context;
    }

    public void setCurrentRegistryPackage(RegistryPackage currentRP) {
	this.currentRP = currentRP;
    }

    public void setDebug(boolean debug) {
	this.debug = debug;
    }

    public void setOutStream(PrintStream outStream) {
	this.outStream = outStream;
    }

    public void setProperties(Properties properties) {
	if (properties.containsKey("debug")) {
	    setDebug(Boolean.
		     valueOf(properties.getProperty("debug")).booleanValue());
	}

	if (properties.containsKey("verbose")) {
	    setVerbose(Boolean.
		       valueOf(properties.getProperty("verbose")).booleanValue());
	}
    }

    public void setRegistryObjects(Collection<?> registryObjects) {
	this.registryObjects = registryObjects;
    }

    public void setVerbose(boolean verbose) {
	this.verbose = verbose;
    }

    public void verifySettings() throws AdminException {
    }

    /**
     * Gets 'resourceName' key from the specified resource bundle and
     * formats the return using given 'formatArgs'.  First attempts
     * retrieval using the ADMIN_FUNCTION_RESOURCES_PREFIX.  If that key is
     * not found, uses ADMIN_SHELL_RESOURCES_PREFIX.  Falling back in this
     * fashion is not the result of an error.
     *
     * @param rb AdminResourceBundle in which key should be found
     * @param resourceName String key for message retrieval
     * @param formatArgs Array of arguments for message
     * @return Formatted String
     */
    public String format(AdminResourceBundle rb,
			 String resourceName,
			 Object[] formatArgs) {
	String ret;

	try {
	    ret = rb.getString(ADMIN_FUNCTION_RESOURCES_PREFIX + resourceName,
				formatArgs);
	} catch (java.util.MissingResourceException e) {
	    ret = rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       resourceName,
			       formatArgs);
	}
	return ret;
    }

    /**
     * Gets 'resourceName' key from the specified resource bundle.  First
     * attempts retrieval using the ADMIN_FUNCTION_RESOURCES_PREFIX.  If
     * that key is not found, uses ADMIN_SHELL_RESOURCES_PREFIX.  Falling
     * back in this fashion is not the result of an error.
     *
     * @param rb AdminResourceBundle in which key should be found
     * @param resourceName String key for message retrieval
     * @return Formatted String
     */
    public String format(AdminResourceBundle rb,
			 String resourceName) {
	String ret;

	try {
	    ret = rb.getString(ADMIN_FUNCTION_RESOURCES_PREFIX + resourceName);
	} catch (java.util.MissingResourceException e) {
	    ret = rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       resourceName);
	}
	return ret;
    }
}
