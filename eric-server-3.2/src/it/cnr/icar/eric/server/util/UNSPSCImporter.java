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
package it.cnr.icar.eric.server.util;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.UUIDFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import javax.xml.bind.JAXBException;

import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;


/**
 * It is the importer for UNSPSC. It will automatically skip the first line in the
 * input file.
 *
 * @author Adrian Chong
*/
public class UNSPSCImporter {
    private static final int SEGMENT = 1;
    private static final int FAMILY = 2;
    private static final int CLASS = 3;
    private static final int COMMODITY = 4;
    private static final int BUSINESS_FUNCTION = 5;
    private StreamTokenizer tokenizer;
    private int maxNumOfEntries;
    private UUIDFactory uUIDFactory;

    /**
    *         Constructor.
    *         @param fileName The file path and name of the UNSPSC taxonomy file.
    *         @param uUIDFactory The UUIDFactory
    */
    public UNSPSCImporter(String fileName, UUIDFactory uUIDFactory)
        throws IOException {
        setFile(fileName);
        tokenizer.eolIsSignificant(false);
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.wordChars(' ', '/');
        tokenizer.whitespaceChars(',', ',');
        tokenizer.parseNumbers();
        this.uUIDFactory = uUIDFactory;
    }

    /**
    *         Set the file path of the UNSPSC taxonomy file.
    *         @param fileName The file path of the UNSPSC taxonomy file
        */
    public void setFile(String fileName) throws IOException {
        tokenizer = new StreamTokenizer(new FileReader(fileName));
        tokenizer.eolIsSignificant(true);

        while ((tokenizer.nextToken() != StreamTokenizer.TT_EOF) &&
                (tokenizer.lineno() < 2)) {
        	continue;
        }

        tokenizer.eolIsSignificant(false);
    }

    /**
    *         Set the maximum number of entries should be handled. Setting it to 0 means
    *         that the importer will handle unlimited number of entries.
    *         @param maxNum The maximum number of entries the importer will handle
        */
    public void setMaxNumOfEntries(int maxNum) {
        maxNumOfEntries = maxNum;
    }

    /**
    *         Get the ClassificationScheme for NAICS taxonomy
    *         @return the ClassificationScheme
        */
    public ClassificationSchemeType getClassificationScheme()
        throws IOException, JAXBException {
        // Create the classification scheme
        ClassificationSchemeType ebClassificationSchemeType = BindingUtility.getInstance().rimFac.createClassificationSchemeType();
        ebClassificationSchemeType.setId("urn:uuid:" + uUIDFactory.newUUID().toString());
        ebClassificationSchemeType.setIsInternal(true);
        ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_UniqueCode);
        ebClassificationSchemeType.setDescription(BindingUtility.getInstance().getDescription("This is the classification scheme for UNSPSC"));
        ebClassificationSchemeType.setName(BindingUtility.getInstance().getName("UNSPSC"));

        ClassificationNodeType parentSegment = null;
        ClassificationNodeType parentFamily = null;
        ClassificationNodeType parentClass = null;
        ClassificationNodeType parentCommodity = null;
        ClassificationNodeType businessFunction = null;

        int count = 0;

        while (true) {
            if (((maxNumOfEntries != 0) && (count > maxNumOfEntries)) ||
                    (tokenizer.nextToken() == StreamTokenizer.TT_EOF)) {
                break;
            }

            int code = 0;
            String name = null;
            code = (int) tokenizer.nval;

            //System.out.println("a number:" + (int)tokenizer.nval);
            tokenizer.nextToken();
            name = tokenizer.sval;

            //System.out.println(tokenizer.sval);
            // Create the classification node by the type
            String id = uUIDFactory.newUUID().toString();

            if (getType(code) == SEGMENT) {
                parentSegment = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentSegment.setId("urn:uuid:" + id);
                ebClassificationSchemeType.getClassificationNode().add(parentSegment);

                // code attribute 
                parentSegment.setCode(code + "");

                // name 
                parentSegment.setName(BindingUtility.getInstance().getName(name));
            } else if (getType(code) == FAMILY) {
                parentFamily = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentFamily.setId("urn:uuid:" + id);
                parentSegment.getClassificationNode().add(parentFamily);

                // code attribute 
                parentFamily.setCode(code + "");

                // name 
                parentFamily.setName(BindingUtility.getInstance().getName(name));
            } else if (getType(code) == CLASS) {
                parentClass = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentClass.setId("urn:uuid:" + id);
                parentFamily.getClassificationNode().add(parentClass);

                // code attribute 
                parentClass.setCode(code + "");

                // name 
                parentClass.setName(BindingUtility.getInstance().getName(name));
            } else if (getType(code) == COMMODITY) {
                parentCommodity = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentCommodity.setId("urn:uuid:" + id);
                parentClass.getClassificationNode().add(parentCommodity);

                // code attribute 
                parentCommodity.setCode(code + "");

                // name 
                parentCommodity.setName(BindingUtility.getInstance().getName(name));
            } else if (getType(code) == BUSINESS_FUNCTION) {
                businessFunction = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                businessFunction.setId("urn:uuid:" + id);
                parentCommodity.getClassificationNode().add(businessFunction);

                // code attribute 
                businessFunction.setCode(code + "");

                // name 
                businessFunction.setName(BindingUtility.getInstance().getName(name));
            }

            count++;
        }

        return ebClassificationSchemeType;
    }

    /**
    *         Get the type (i.e. Segment, Family, etc.)
    */
    private int getType(int code) {
        String codeStr = code + "";

        if (!codeStr.substring(0, 2).equals("00") &&
                codeStr.substring(2, 4).equals("00")) {
            return SEGMENT;
        } else if (!codeStr.substring(2, 4).equals("00") &&
                codeStr.substring(4, 6).equals("00")) {
            return FAMILY;
        } else if (!codeStr.substring(4, 6).equals("00") &&
                codeStr.substring(6, 8).equals("00")) {
            return CLASS;
        } else if (!codeStr.substring(6, 8).equals("00") &&
                ((codeStr.length() <= 8) ||
                codeStr.substring(8, 10).equals("00"))) {
            return COMMODITY;
        } else if ((codeStr.length() > 8) &&
                !codeStr.substring(8, 10).equals("00")) {
            return BUSINESS_FUNCTION;
        } else {
            throw new RuntimeException(ServerResourceBundle.getInstance().getString("message.unknownTypeFound"));
        }
    }
}
