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
package it.cnr.icar.eric.server.container;

/**
 * Interface for life cycle listener code specific to a type of database.
 *
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.1 $
 */
interface Helper {
    /**
     * Perform whatever initialization is necessary for this database type.
     * Should be called whether or not server will be started and shut down.
     *
     * @param configDirectory string identifying a directory in which
     * configuration files may be found.  Use {@code null} if container
     * provides a consistent current directory for such files.
     *
     * @param listener {@link GenericListener} from which {@link Helper}
     * may get parameters.
     */
    public void initialize(String configDirectory,
			   GenericListener listener);

    /**
     * Shut the database down smoothly.
     */
    public void shutdownDatabase();

    /**
     * Start database server, if relevant for this database type.
     *
     * @return boolean which is true if and only if a database server was
     * successfully started
     */
    public boolean startupServer();

    /**
     * Shut down database server, if any.
     */
    public void shutdownServer();
}
