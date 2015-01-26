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
package it.cnr.icar.eric.client.xml.registry.util;

import java.io.File;

import javax.xml.registry.JAXRException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides methods to check that jaxr-ebxml keystore properties are set
 * correctly and keystore file exists.
 *
 * @author Fabian Ritzmann
 */
public class KeystoreUtil {
    private static final Log log = LogFactory.getLog(KeystoreUtil.class);
    /**
     * Return location of keystore file defined in jaxr-ebxml properties
     *
     * @return Path to keystore file
     * @throws JAXRException Thrown if properties are not set
     */
    public static File getKeystoreFile() throws JAXRException {
        String jaxrHomeFileName = ProviderProperties.getInstance().getProperty("jaxr-ebxml.home");

        if ((jaxrHomeFileName == null) || (jaxrHomeFileName.length() == 0)) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.no.property.jaxr-ebxml.home"));
        }

        File jaxrHomeFile = new File(jaxrHomeFileName);

        String keystoreFileName = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keystore");

        if ((keystoreFileName == null) || (keystoreFileName.length() == 0)) {
            throw new JAXRException(
                JAXRResourceBundle.getInstance().getString("message.error.no.property.jaxr-ebxml.security.keystore"));
        }

        return new File(jaxrHomeFile.getAbsolutePath(), keystoreFileName);
    }

    /**
     * Returns if keystore file can be read, throws an exception otherwise
     *
     * @param keystoreFile Path to keystore file
     * @throws JAXRException Thrown if keystore file can not be read
     */
    public static void canReadKeystoreFile(File keystoreFile)
        throws JAXRException {
        try {
            if (!keystoreFile.exists()) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.keystore.file.not.exist", new Object[] {keystoreFile.getAbsolutePath()}));
            }

            if (!keystoreFile.canRead()) {
                throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.read.keysore.file", new Object[] {keystoreFile.getAbsolutePath()}));
            }
        } catch (SecurityException e) {
            log.error(e);
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.read.keysore.file", new Object[] {keystoreFile.getAbsolutePath()}));
        }
    }

    /**
     * Create keystore directory if it does not already exist
     *
     * @param keystoreFile Path to keystore file
     * @throws JAXRException Thrown if directory could not be created
     */
    public static void createKeystoreDirectory(File keystoreFile)
        throws JAXRException {
        File keystoreDir = keystoreFile.getParentFile();

        try {
            // Ignore return value of mkdirs, returns false if directories
            // already exist
            keystoreDir.mkdirs();
        } catch (SecurityException e) {
            log.error(e);
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.not.create.directory",new Object[] {keystoreDir.getAbsolutePath()}));
        }
    }
}
