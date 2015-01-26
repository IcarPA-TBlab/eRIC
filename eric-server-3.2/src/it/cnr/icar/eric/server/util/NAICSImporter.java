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
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;

import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;


/**
 * It is the importer for NAICS taxonomy. It expects the input file is of
 * version 2002. It will automatically skip the first four lines in the input file.
 *
 * @author Adrian Chong
*/
public class NAICSImporter {
    private static final int SECTOR = 2;
    private static final int SUBSECTOR = 3;
    private static final int INDUSTRY_GROUP = 4;
    private static final int INDUSTRY = 5;
    private static final int NATIONAL = 6;
    private StreamTokenizer tokenizer;
    private int maxNumOfEntries;
    private UUIDFactory uUIDFactory;

    /**
    *         Constructor.
    *         @param fileName The file path and name of the NAICS taxonomy file.
    *         @param uUIDFactory The UUIDFactory
    */
    public NAICSImporter(String fileName, UUIDFactory uUIDFactory)
        throws IOException {
        setFile(fileName);
        tokenizer.eolIsSignificant(false);
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.wordChars(' ', ' ');
        tokenizer.wordChars(',', ',');
        this.uUIDFactory = uUIDFactory;
    }

    /**
    *         Set the file path of the ISO3166 taxonomy file.
    *         @param fileName The file path of the NAICS taxonomy file
        */
    public void setFile(String fileName) throws IOException {
        tokenizer = new StreamTokenizer(new FileReader(fileName));
        tokenizer.eolIsSignificant(true);

        int lineCount = 0;

        while ((tokenizer.nextToken() != StreamTokenizer.TT_EOF) &&
                (lineCount < 3)) {
            if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
                lineCount++;
            }
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
        throws IOException, JAXBException, JAXRException {
        // Create the classification scheme
        ClassificationSchemeType ebClassificationSchemeType = BindingUtility.getInstance().rimFac.createClassificationSchemeType();
        ebClassificationSchemeType.setId("urn:uuid:" + uUIDFactory.newUUID().toString());
        ebClassificationSchemeType.setIsInternal(true);
        ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_UniqueCode);
        ebClassificationSchemeType.setDescription(BindingUtility.getInstance().getDescription("This is the classification scheme for NAICS version 2002"));
        ebClassificationSchemeType.setName(BindingUtility.getInstance().getName("ntis-gov:naics"));

        ClassificationNodeType parentSector = null;
        ClassificationNodeType parentSubSector = null;
        ClassificationNodeType parentIndustryGroup = null;
        ClassificationNodeType parentIndustry = null;
        ClassificationNodeType national = null;

        int count = 0;

        while (true) {
            if (((maxNumOfEntries != 0) && (count > maxNumOfEntries)) ||
                    (tokenizer.nextToken() == StreamTokenizer.TT_EOF)) {
                break;
            }

            // code
            String code = "";

            if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                code = ((int) tokenizer.nval) + "";
            }

            // It is to check whether the code is a range  
            tokenizer.nextToken();

            if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                code += (int) tokenizer.nval;
                tokenizer.nextToken();
            }

            //System.out.println(code);
            //System.out.println(getType(code));
            String name = tokenizer.sval;

            //System.out.println(name);
            String id = "urn:uuid:" + uUIDFactory.newUUID().toString();

            if (getType(code) == SECTOR) {
                parentSector = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentSector.setId(id);
                parentSector.setName(BindingUtility.getInstance().getName(name));
                parentSector.setCode(code);
                ebClassificationSchemeType.getClassificationNode().add(parentSector);
            } else if (getType(code) == SUBSECTOR) {
                parentSubSector = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentSubSector.setId(id);
                parentSubSector.setName(BindingUtility.getInstance().getName(name));
                parentSubSector.setCode(code);
                parentSector.getClassificationNode().add(parentSubSector);
            } else if (getType(code) == INDUSTRY_GROUP) {
                parentIndustryGroup = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentIndustryGroup.setId(id);
                parentIndustryGroup.setName(BindingUtility.getInstance()
                                                          .getName(name));
                parentIndustryGroup.setCode(code);
                parentSubSector.getClassificationNode().add(parentIndustryGroup);
            } else if (getType(code) == INDUSTRY) {
                parentIndustry = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                parentIndustry.setId(id);
                parentIndustry.setName(BindingUtility.getInstance().getName(name));
                parentIndustry.setCode(code);
                parentIndustryGroup.getClassificationNode().add(parentIndustry);
            } else if (getType(code) == NATIONAL) {
                national = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                national.setId(id);
                national.setName(BindingUtility.getInstance().getName(name));
                national.setCode(code);
                parentIndustry.getClassificationNode().add(national);
            }

            count++;
        }

        return ebClassificationSchemeType;
    }

    /**
    *         Get the type (i.e. Sector, SubSector, etc.) by inspecting the code length.
    *         A code range 's type is specified by the number of digits.
    */
    private int getType(String code) {
        if (code.indexOf('-') == -1) {
            return code.length();
        } else {
            StringTokenizer sTokenizer = new StringTokenizer(code);

            return sTokenizer.nextToken("-").length();
        }
    }
}
