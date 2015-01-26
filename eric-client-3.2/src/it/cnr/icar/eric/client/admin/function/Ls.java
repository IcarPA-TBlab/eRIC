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
import it.cnr.icar.eric.common.Utility;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.infomodel.RegistryObject;

import org.apache.tools.ant.types.selectors.SelectorUtils;


public class Ls extends AbstractAdminFunction {
	@SuppressWarnings("static-access")
	public void execute(AdminFunctionContext context,
						String args) throws Exception {
		String queryStr;

		if (context.getCurrentRP() == null) {

			queryStr =
				"SELECT ro.* from RegistryObject ro WHERE " +
				"ro.id NOT IN (SELECT targetObject FROM Association) OR " +
				"ro.id IN (SELECT DISTINCT targetObject FROM Association " +
				"WHERE associationType != '" +
				bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
				"')";
		} else {
			queryStr =
				"SELECT DISTINCT ro.* " +
				"FROM RegistryObject ro, RegistryPackage p, " +
				"Association ass WHERE ((p.id = '" +
				context.getCurrentRP().getKey().getId() +
				"') AND (ass.associationType='"
				+ bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
				"' AND ass.sourceObject = p.id AND ass.targetObject = ro.id)) ";
		}

		javax.xml.registry.Query query =
			context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
													  queryStr);
		// make JAXR request
		javax.xml.registry.BulkResponse resp =
			context.getService().getDQM().executeQuery(query);

		Collection<?> registryObjects = resp.getCollection();
		Iterator<?> iter = registryObjects.iterator();

		// Number of objects listed by command.
		int objectCount = 0;
		if (args != null) {
			// If there are any args, match every object in RegistryPackage against
			// each arg (with the arg evaluated as a pattern)
			String[] tokens = args.split("\\s+");
			while (iter.hasNext()) {
				RegistryObject ro = (RegistryObject) iter.next();
				String roName = ro.getName().getValue();

				for(int index = 0; index < tokens.length; index++) {
					String pattern = tokens[index];

					// Match against pattern as either UR or globbing pattern.
					// Currently can't match against null names.
					if ((Utility.getInstance().isValidURN(pattern) &&
						 ro.getKey().getId().equals(pattern)) ||
						(!Utility.getInstance().isValidURN(pattern) &&
						 roName != null &&
						 SelectorUtils.match(pattern, roName))) {
						context.printMessage(ro.getKey().getId() +
											 "  " +
											 ro.getName());

						objectCount++;
						continue;
					}
				}
			}
		} else {
			// If no args, list every RegistryObject in RegistryPackage.
			while (iter.hasNext()) {
				RegistryObject ro = (RegistryObject) iter.next();
				context.printMessage(ro.getKey().getId() +
									 "  " +
									 ro.getName());
				objectCount++;
			}
		}

		context.printMessage(format(rb,"objectsFound",
					    new Object[] {
						    new Integer(objectCount)
					    }));
	}

	public String getUsage() {
		return format(rb, "usage.ls");
	}
}
