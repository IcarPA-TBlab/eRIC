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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Properties;

import javax.xml.registry.infomodel.RegistryPackage;

public interface AdminShell {
    public static final String ADMIN_SHELL_RESOURCE_BASE =
	"it.cnr.icar.eric.client.admin";

    public static final String ADMIN_SHELL_FUNCTIONS =
	ADMIN_SHELL_RESOURCE_BASE + ".AdminShellFunctions";

    public static final String ADMIN_SHELL_RESOURCES_PREFIX =
            "AdminShellResources.";

    public void run(InputStream inStream, PrintStream outStream) throws AdminException;

    public void run(String command, PrintStream outStream) throws AdminException;

    public void setDebug(boolean debug);

    public void setLocalDir(File baseDir);

    public void setProperties(Properties properties);

    public void setRoot(RegistryPackage rootRP);

    public void setService(JAXRService service);

    public void setSQLSelect(String sqlQuery) throws AdminException;

    public void setVerbose(boolean verbose);

    public void verifySettings() throws AdminException;
}
