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
import it.cnr.icar.eric.client.xml.registry.ClientRequestContext;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.BindingUtility;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;


public class Import extends AbstractAdminFunction {
    @SuppressWarnings("unused")
	private HashMap<?, ?> attachments = new HashMap<Object,Object>();
    private String request;
    
    @SuppressWarnings("unused")
	public void execute(AdminFunctionContext context, String args)
    throws Exception {
	if (args != null) {
	    String[] tokens = args.split("\\s+");

	    int tIndex = 0;

	    for (tIndex = 0;
		 ((tIndex < tokens.length) && (tokens[tIndex].charAt(0) == '-'));
		 tIndex++) {
		String option = tokens[tIndex];

		if ((collator.compare(option, "-a") == 0) ||
                    (collator.compare(option, "--attach") == 0)) {
		    if (++tIndex == tokens.length) {
			context.printMessage(getUsage());

			return;
		    }

                    String attachData = tokens[tIndex++];
                    StringTokenizer tokenizer = new StringTokenizer(attachData,
								    ",");

                    String attachFileName = null;
                    String mimeType = null;
                    String attachId = "id";

                    int j = 0;

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();

                        if (j == 1) {
                            attachFileName = token;
                        } else if (j == 2) {
                            mimeType = token;
                        }

                        if (j == 3) {
                            attachId = token;
                        }

                        j++;
                    }
                    
                    if (attachFileName == null) {
                        context.printMessage(format(rb,"invalidArgument",
						    new Object[] { attachData }));
                        return;
                    }
                    
		} else {
		    context.printMessage(format(rb,"invalidArgument",
						new Object[] { option }));
		    return;
		}
            }
            
            if (tIndex == tokens.length - 1) {
                request = tokens[tIndex];
            } else {
                context.printMessage(format(rb,"argumentRequired"));
            
                return;
            }
        } else {
            context.printMessage(format(rb,"argumentRequired"));
            
            return;
        }
       
	Unmarshaller unmarshaller = BindingUtility.getInstance().getJAXBContext().createUnmarshaller();
                    
	SubmitObjectsRequest submitRequest = (SubmitObjectsRequest)unmarshaller.unmarshal(new File(request));
	HashMap<String, Object> attachMap = new HashMap<String, Object>();  //id to attachments map
                    
	//Look for special temporary Slot on ExtrinsicObjects to resolve to RepositoryItem
	//If a file in same directory is found with filename same as slot value
	//then assume it is the matching RepositoryItem
	List<JAXBElement<? extends IdentifiableType>> ros = submitRequest.getRegistryObjectList().getIdentifiable();
	Iterator<JAXBElement<? extends IdentifiableType>> iter=ros.iterator();
	while (iter.hasNext()) {
	    Object obj = iter.next();
	    if (obj instanceof ExtrinsicObjectType) {
		ExtrinsicObjectType eo = (ExtrinsicObjectType)obj;
		HashMap<String, Serializable> slotsMap = BindingUtility.getInstance().getSlotsFromRegistryObject(eo);
		@SuppressWarnings("static-access")
		String slotName = BindingUtility.getInstance().CANONICAL_SLOT_EXTRINSIC_OBJECT_REPOSITORYITEM_URL;
		if (slotsMap.containsKey(slotName)) {
		    String riURLStr = (String)slotsMap.get(slotName);
		    File riFile = new File(riURLStr);
		    DataHandler riDataHandler = new DataHandler(new FileDataSource(riFile));                                
		    attachMap.put(eo.getId(), riDataHandler);
                                
		    //Remove transient slot
		    slotsMap.remove(slotName);
		    eo.getSlot().clear();
		    BindingUtility.getInstance().addSlotsToRegistryObject(eo, slotsMap);
		}
	    }
	}
	ClientRequestContext requestContext = new ClientRequestContext("AdminTool:import", submitRequest);
	requestContext.setRepositoryItemsMap(attachMap);
	BulkResponse br = ((LifeCycleManagerImpl) context.getService().getLCM()).doSubmitObjectsRequest(requestContext);
	JAXRUtility.checkBulkResponse(br);
    }
    
    public String getUsage() {
        return format(rb, "usage.import");
    }
}
