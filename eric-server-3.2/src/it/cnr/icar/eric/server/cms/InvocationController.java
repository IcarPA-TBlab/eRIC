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
package it.cnr.icar.eric.server.cms;

/**
 * Information to control an invocation of a Content Management
 * Service.  Also includes sufficient information to be able to
 * meaningfully sort instances of <code>InvocationController</code> so
 * services can be invoked in consistent order.
 *
 * @author  tkg
 */
public class InvocationController {
    /** Id of the 'InvocationControlFileFor' association subtype of
     * this invocation controller. */
    String controlFileForAssocId;

    /** Id of the corresponding ExtrinsicObject (and RepositoryItem)
     * in the registry/repository. */
    String eoId;

    /** Creates a new instance of InvocationController */
    public InvocationController(String controlFileForAssocId, String eoId) {
        this.controlFileForAssocId = controlFileForAssocId;
        this.eoId = eoId;
    }

    /**
     * Get the ControlFileForAssocId value.
     * @return the ControlFileForAssocId value.
     */
    public String getControlFileForAssocId() {
        return controlFileForAssocId;
    }

    /**
     * Set the ControlFileForAssocId value.
     * @param newControlFileForAssocId The new ControlFileForAssocId value.
     */
    public void setControlFileForAssocId(String newControlFileForAssocId) {
        this.controlFileForAssocId = newControlFileForAssocId;
    }

    /**
     * Gets the Id of the ExtrinsicObject for the InvocationControlFile.
     * @return the eoId value.
     */
    public String getEoId() {
        return eoId;
    }

    /**
     * Sets eoId value.  This is the Id of the ExtrinsicObject for the
     * InvocationControlFile.
     * @param newEoId The new eoId value.
     */
    public void setEoId(String newEoId) {
        this.eoId = newEoId;
    }
}
