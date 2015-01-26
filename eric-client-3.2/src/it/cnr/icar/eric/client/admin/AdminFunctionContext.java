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

import java.io.File;
import java.io.PrintStream;

import java.util.Collection;
import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;


public class AdminFunctionContext {
    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(AdminFunctionContext.class.getName());
    private static AdminFunctionContext instance; //singleton instance

    private File localDir;
    private RegistryPackage currentRP;
    private boolean debug;
    private String editor;
    private String lastException;
    private PrintStream outStream;
    private Properties properties;
    private Collection<?> registryObjects;
    private JAXRService service;
    private String[] users;
    private boolean verbose;

    /** Creates a new instance of AdminFunctionContext */
    protected AdminFunctionContext() {
    }

    /**
     * Gets the singleton instance of
     * <code>AdminFunctionContext</code>.
     *
     * @return an <code>AdminFunctionContext</code> value
     */
    public synchronized static AdminFunctionContext getInstance() {
        if (instance == null) {
            instance = new AdminFunctionContext();
        }

        return instance;
    }

    /**
     * Gets the current value of currentRP.
     *
     * @return the current currentRP value.
     */
    public RegistryPackage getCurrentRP() {
        return currentRP;
    }

    /**
     * Sets the value of currentRP.
     *
     * @param currentRP the new value
     */
    public void setCurrentRP(RegistryPackage currentRP) {
        this.currentRP = currentRP;
    }

    /**
     * Gets the current value of debug.
     *
     * @return the current debug value.
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets the value of debug.
     *
     * @param debug the new value
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Gets the current value of editor.
     *
     * @return the current editor value.
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Sets the value of editor.
     *
     * @param editor the new value
     */
    public void setEditor(String editor) {
        this.editor = editor;
    }

    /**
     * Gets the current value of lastException.
     *
     * @return the current lastException value.
     */
    public String getLastException() {
        return lastException;
    }

    /**
     * Sets the value of lastException.
     *
     * @param lastException the new value
     */
    public void setLastException(String lastException) {
        this.lastException = lastException;
    }

    /**
     * Gets the current value of localDir.
     *
     * @return the current localDir value.
     */
    public File getLocalDir() {
        return localDir;
    }

    /**
     * Sets the value of localDir.
     *
     * @param localDir the new value
     */
    public void setLocalDir(File localDir) {
        this.localDir = localDir;
    }

    /**
     * Gets the current value of outStream.
     *
     * @return the current outStream value.
     */
    public PrintStream getOutStream() {
        return outStream;
    }

    /**
     * Sets the value of outStream.
     *
     * @param outStream the new value
     */
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    /**
     * Gets the current value of properties.
     *
     * @return the current properties value.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of properties.
     *
     * @param properties the new value
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Gets the current value of registryObjects.
     *
     * @return the current registryObjects value.
     */
    public Collection<?> getRegistryObjects() {
        return registryObjects;
    }

    /**
     * Sets the value of registryObjects.
     *
     * @param registryObjects the new value
     */
    public void setRegistryObjects(Collection<?> registryObjects) {
        this.registryObjects = registryObjects;
    }

    /**
     * Gets the current value of service.
     *
     * @return the current service value.
     */
    public JAXRService getService() {
        return service;
    }

    /**
     * Sets the value of service.
     *
     * @param service the new value
     */
    public void setService(JAXRService service) {
        this.service = service;
    }

    /**
     * Gets the current value of users.
     *
     * @return the current users value.
     */
    public String[] getUsers() {
        return users;
    }

    /**
     * Sets the value of users.
     *
     * @param users the new value
     */
    public void setUsers(String[] users) {
        this.users = users;
    }

    /**
     * Gets the current value of verbose.
     *
     * @return the current verbose value.
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Sets the value of verbose.
     *
     * @param verbose the new value
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Verifies that the current settings are correct and consistent.
     *
     * @exception AdminException if an error occurs
     */
    public void verifySettings() throws AdminException {
    }

    public void printMessage(String string) {
        outStream.println(string);
    }

    public void printMessage() {
        outStream.println();
    }
}
