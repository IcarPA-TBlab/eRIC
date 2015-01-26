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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;

import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;


/**
 * It is the importer for ISO3166 taxonomy. The MiddleEast node/tree will be a
 * sub-node/tree of Asia node.        The Australia node will be moved from Other node
 * to Australia node next to New Zealand. The remaining nodes remain the same
 * relationship as the original input ISO3166 taxonomy.
 * @author Adrian Chong
 */
public class ISO3166Importer {
    private StreamTokenizer tokenizer;
    private int maxNumOfCountries;
    private UUIDFactory uUIDFactory;

    /**
     *         Constructor.
     *         @param fileName The file path and name of the ISO3166 taxonomy file.
     *         @param uUIDFactory The UUIDFactory
     */
    public ISO3166Importer(String fileName, UUIDFactory uUIDFactory)
        throws FileNotFoundException {
        tokenizer = new StreamTokenizer(new FileReader(fileName));
        tokenizer.eolIsSignificant(false);
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.whitespaceChars(',', ',');
        this.uUIDFactory = uUIDFactory;
    }

    /**
     *         Set the file path of the ISO3166 taxonomy file.
     *         @param fileName The file path of the ISO3166 taxonomy file
     */
    public void setFile(String fileName) throws FileNotFoundException {
        tokenizer = new StreamTokenizer(new FileReader(fileName));
    }

    /**
     *         Set the maximum number of entries should be handled. Setting it to 0 means
     *         that the importer will handle unlimited number of entries.
     *         @param maxNum The maximum number of entries the importer will handle
     */
    public void setMaxNumOfCountries(int maxNum) {
        maxNumOfCountries = maxNum;
    }

    /**
     *         Get the ClassificationScheme for ISO3166 taxonomy
     *         @return the ClassificationScheme
     */
    public ClassificationSchemeType getClassificationScheme()
        throws IOException, JAXBException, JAXRException {
        int count = 0;

        ClassificationSchemeType ebClassificationSchemeType = BindingUtility.getInstance().rimFac.createClassificationSchemeType();
        
        ebClassificationSchemeType.setId("urn:uuid:" + uUIDFactory.newUUID().toString());
        ebClassificationSchemeType.setIsInternal(true);
        ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_UniqueCode);
        ebClassificationSchemeType.setDescription(BindingUtility.getInstance().getDescription("This is the classification scheme for ISO3166"));
        ebClassificationSchemeType.setName(BindingUtility.getInstance().getName("ISO 3166"));

        HashMap<String, ClassificationNodeType> continentToNodeMap = new HashMap<String, ClassificationNodeType>();

        while (true) {
            if (((maxNumOfCountries != 0) && (count > maxNumOfCountries)) ||
                    (tokenizer.nextToken() == StreamTokenizer.TT_EOF)) {
                break;
            }

            //System.out.println(tokenizer.sval);
            String code = tokenizer.sval;
            tokenizer.nextToken();

            //System.out.println(tokenizer.sval);
            String name = tokenizer.sval;
            tokenizer.nextToken();

            //System.out.println(tokenizer.sval);
            String continent = tokenizer.sval;

            ClassificationNodeType ebParentClassificationNodeType = null;

            if ((ebParentClassificationNodeType = continentToNodeMap.get(
                            continent)) == null) {
                // Create root concept for a continent
                String continentId = uUIDFactory.newUUID().toString();

                // id attribute
                ebParentClassificationNodeType = BindingUtility.getInstance().rimFac.createClassificationNodeType();
                ebParentClassificationNodeType.setId("urn:uuid:" + continentId);

                /*
                Add the continent concept to the Classification Scheme,
                and set parent attribite
                 */
                ebClassificationSchemeType.getClassificationNode().add(ebParentClassificationNodeType);

                // code attribute
                ebParentClassificationNodeType.setCode(continent);

                // name
                ebParentClassificationNodeType.setName(BindingUtility.getInstance()
                                                               .getName(continent));
                continentToNodeMap.put(continent, ebParentClassificationNodeType);
            }

            //Generate new ClassificationNode
            String countryId = uUIDFactory.newUUID().toString();

            // id attribute
            ClassificationNodeType node = BindingUtility.getInstance().rimFac.createClassificationNodeType();
            node.setId("urn:uuid:" + countryId);

            /*
            Add the concept to the continent parent and set the parent attribute
             */
            ebParentClassificationNodeType.getClassificationNode().add(node);

            // code attribute
            node.setCode(code);

            // name attribute
            node.setName(BindingUtility.getInstance().getName(name));
            count++;
        }

        return fixNodes(ebClassificationSchemeType);
    }

    /**
     *         Make the MiddleEast node/tree be a sub-node/tree of Asia node.
     *         Move Australia from Other node to Australia node next to New Zealand.
     */
    private ClassificationSchemeType fixNodes(ClassificationSchemeType classScheme) {
        List<ClassificationNodeType> nodes = classScheme.getClassificationNode();
        ClassificationNodeType asiaNode = null;
        ClassificationNodeType middleEastNode = null;
        ClassificationNodeType otherNode = null;
        ClassificationNodeType australiaNode = null;

        for (int i = 0; i < nodes.size(); i++) {
            ClassificationNodeType _node = nodes.get(i);

            if (_node.getCode().equalsIgnoreCase("Asia")) {
                asiaNode = _node;
            } else if (_node.getCode().equalsIgnoreCase("Other")) {
                otherNode = _node;
            } else if (_node.getCode().equalsIgnoreCase("Middle East")) {
                middleEastNode = _node;
            } else if (_node.getCode().equalsIgnoreCase("Australia")) {
                australiaNode = _node;
            }
        }

        ClassificationNodeType australiaInOtherNode = null;
        List<ClassificationNodeType> otherNodeChildren = otherNode.getClassificationNode();

        for (int i = 0; i < otherNodeChildren.size(); i++) {
            ClassificationNodeType _node = otherNodeChildren.get(i);

            if (_node.getCode().equalsIgnoreCase("AU")) {
                australiaInOtherNode = _node;
            }
        }

        if ((middleEastNode != null) && (asiaNode != null)) {
            classScheme.getClassificationNode().remove(middleEastNode);
            asiaNode.getClassificationNode().add(middleEastNode);
        }

        if ((otherNode != null) && (australiaNode != null) &&
                (australiaInOtherNode != null)) {
            otherNode.getClassificationNode().remove(australiaInOtherNode);
            australiaNode.getClassificationNode().add(australiaInOtherNode);
        }

        return classScheme;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: [input file path] [output file path]");

            return;
        }

        UUIDFactory uUIDFactory = UUIDFactory.getInstance();
        ISO3166Importer importer = new ISO3166Importer(args[0], uUIDFactory);
        ClassificationSchemeType ebClassificationSchemeType = importer.getClassificationScheme();
        FileWriter outputWriter = new FileWriter(args[1]);
        BindingUtility.getInstance().getJAXBContext().createMarshaller()
                      .marshal(ebClassificationSchemeType, outputWriter);
    }
}
