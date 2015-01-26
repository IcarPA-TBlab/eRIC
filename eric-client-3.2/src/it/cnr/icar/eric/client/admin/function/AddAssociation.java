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
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.Utility;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;


public class AddAssociation extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        String type = null;

        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String[] tokens = args.split("\\s+");

        int tIndex = 0;

        for (tIndex = 0;
                ((tIndex < tokens.length) && (tokens[tIndex].charAt(0) == '-'));
                tIndex++) {
            String option = tokens[tIndex];

            if (collator.compare(option, "-type") == 0) {
                if (++tIndex == tokens.length) {
                    context.printMessage(getUsage());
                }

                type = tokens[tIndex];
            } else {
                context.printMessage(format(rb,"invalidArgument",
					    new Object[] { tokens[tIndex] }));

                return;
            }
        }

        if (type == null) {
            context.printMessage(format(rb,"associationTypeRequired"));

            return;
        }

        if (tIndex != (tokens.length - 2)) {
            context.printMessage(format(rb,"invalidArgument",
					new Object[] { tokens[tIndex] }));

            return;
        }

        String sourceID = tokens[tIndex++];

        if (!Utility.getInstance().isValidURN(sourceID)) {
            context.printMessage(format(rb,"urnRequired",
					new Object[] { sourceID }));

            return;
        }

        String targetID = tokens[tIndex];

        if (!Utility.getInstance().isValidURN(targetID)) {
            context.printMessage(format(rb,"urnRequired",
					new Object[] { targetID }));

            return;
        }

        // Check source and target aren't identical
        if (sourceID.equals(targetID)) {
            context.printMessage(format(rb,"sourceIsTarget",
					new Object[] { sourceID }));

            return;
        }

        // Check that the source exists
        String queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro WHERE ro.id = '" + sourceID + "'";

        javax.xml.registry.Query query = context.getService().getDQM()
                                                .createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        javax.xml.registry.BulkResponse resp = context.getService().getDQM()
                                                      .executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        Collection<?> registryObjects = resp.getCollection();

        if (registryObjects.size() == 0) {
            context.printMessage(format(rb,"doesNotExist",
					new Object[] { sourceID }));

            return;
        } else if (registryObjects.size() > 1) {
            context.printMessage(format(rb,"multipleExist",
					new Object[] { sourceID }));

            return;
        }

        RegistryObject sourceRO = (RegistryObject) registryObjects.iterator()
                                                                  .next();

        // Check that the target exists
        queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro WHERE ro.id = '" + targetID + "'";

        query = context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        resp = context.getService().getDQM().executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        registryObjects = resp.getCollection();

        if (registryObjects.size() == 0) {
            context.printMessage(format(rb,"doesNotExist",
					new Object[] { targetID }));

            return;
        } else if (registryObjects.size() > 1) {
            context.printMessage(format(rb,"multipleExist",
					new Object[] { targetID }));

            return;
        }

        RegistryObject targetRO = (RegistryObject) registryObjects.iterator()
                                                                  .next();

        // Check that an association does not already exist
        queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro, Association ass " +
            "WHERE (ass.sourceObject = '" + sourceID +
            "' AND ass.targetObject = '" + targetID +
            "') OR (ass.sourceObject = '" + targetID +
            "' AND ass.targetObject = '" + sourceID + "')";

        query = context.getService().getDQM().createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);
        resp = context.getService().getDQM().executeQuery(query);

        JAXRUtility.checkBulkResponse(resp);

        registryObjects = resp.getCollection();

        if (registryObjects.size() != 0) {
            context.printMessage(format(rb,"associationExists",
					new Object[] { sourceID, targetID }));
        }

        Concept assocConcept = context.getService().getBQM().findConceptByPath("/AssociationType/" +
                type);

        if (assocConcept == null) {
            context.printMessage(format(rb,"noConceptForType",
					new Object[] { type }));

            return;
        }

        Association assoc = context.getService().getLCM().createAssociation(targetRO,
                assocConcept);

        sourceRO.addAssociation(assoc);

        HashSet<Association> assocColl = new HashSet<Association>();

        assocColl.add(assoc);

        context.getService().getLCM().saveAssociations(assocColl, true);

        if (context.getVerbose() || context.getDebug()) {
            context.printMessage(assoc.getKey().getId());
        }
    }

    public String getUsage() {
        return format(rb, "usage.addAssoc");
    }
}
