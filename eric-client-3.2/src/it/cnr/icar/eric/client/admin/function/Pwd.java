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
package it.cnr.icar.eric.client.admin.function;


import it.cnr.icar.eric.client.admin.AbstractAdminFunction;
import it.cnr.icar.eric.client.admin.AdminFunctionContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import javax.xml.registry.infomodel.RegistryPackage;


public class Pwd extends AbstractAdminFunction {
    public class Subpath {
        private RegistryPackage rp;
        private String subpath;

        protected Subpath() {
        }

        public Subpath(RegistryPackage rp, String subpath) {
            this.rp = rp;
            this.subpath = subpath;
        }

        public RegistryPackage getRP() {
            return rp;
        }

        public String getSubpath() {
            return subpath;
        }
    }

    public void execute(AdminFunctionContext context, String[] args)
        throws Exception {
        RegistryPackage currentRP = context.getCurrentRP();

        // if not in a RegistryPackage, then it's the root.
        if (currentRP == null) {
            context.printMessage("/");
        } else {
            LinkedList<Subpath> subpathList = new LinkedList<Subpath>();
            subpathList.add(new Subpath(currentRP,
                    currentRP.getName().toString()));

            while (!subpathList.isEmpty()) {
                Subpath currSubpath = subpathList.removeFirst();

                Collection<?> sourceRPs = context.getService()
                                              .getSourceRegistryPackages(currSubpath.getRP());

                if (sourceRPs.size() == 0) {
                    context.printMessage("(" + Locale.getDefault() + ") /" +
                        currSubpath.getSubpath());
                } else {
                    Iterator<?> rpIter = sourceRPs.iterator();

                    while (rpIter.hasNext()) {
                        RegistryPackage sourceRP = (RegistryPackage) rpIter.next();
                        String sourceRPName = sourceRP.getName().toString();

                        subpathList.add(new Subpath(sourceRP,
                                sourceRPName + "/" + currSubpath.getSubpath()));
                    }
                }
            }
        }
    }

    public String getUsage() {
        return "pwd";
    }
}
