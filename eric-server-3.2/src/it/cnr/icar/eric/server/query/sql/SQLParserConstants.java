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
package it.cnr.icar.eric.server.query.sql;

public interface SQLParserConstants {

  int EOF = 0;
  int COMMENT_LINE = 5;
  int COMMENT_BLOCK = 6;
  int ALL = 7;
  int AND = 8;
  int ASC = 9;
  int BETWEEN = 10;
  int BY = 11;
  int DESC = 12;
  int DISTINCT = 13;
  int EXISTS = 14;
  int FROM = 15;
  int GROUP = 16;
  int HAVING = 17;
  int IN = 18;
  int IS = 19;
  int LIKE = 20;
  int NOT = 21;
  int NULL = 22;
  int OR = 23;
  int ORDER = 24;
  int SELECT = 25;
  int SPACES = 26;
  int SUM = 27;
  int UNION = 28;
  int WHERE = 29;
  int ZERO = 30;
  int ZEROS = 31;
  int INTEGER_LITERAL = 32;
  int FLOATING_POINT_LITERAL = 33;
  int EXPONENT = 34;
  int STRING_LITERAL = 35;
  int ID = 36;
  int VARIABLE = 37;
  int LETTER = 38;
  int DIGIT = 39;
  int SEMICOLON = 40;
  int DOT = 41;
  int LESS = 42;
  int LESSEQUAL = 43;
  int GREATER = 44;
  int GREATEREQUAL = 45;
  int EQUAL = 46;
  int NOTEQUAL = 47;
  int NOTEQUAL2 = 48;
  int OPENPAREN = 49;
  int CLOSEPAREN = 50;
  int ASTERISK = 51;
  int SLASH = 52;
  int PLUS = 53;
  int MINUS = 54;
  int QUESTIONMARK = 55;
  int PIPES = 56;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\t\"",
    "<COMMENT_LINE>",
    "<COMMENT_BLOCK>",
    "\"all\"",
    "\"and\"",
    "\"asc\"",
    "\"between\"",
    "\"by\"",
    "\"desc\"",
    "\"distinct\"",
    "\"exists\"",
    "\"from\"",
    "\"group\"",
    "\"having\"",
    "\"in\"",
    "\"is\"",
    "\"like\"",
    "\"not\"",
    "\"null\"",
    "\"or\"",
    "\"order\"",
    "\"select\"",
    "\"spaces\"",
    "\"sum\"",
    "\"union\"",
    "\"where\"",
    "\"zero\"",
    "\"zeros\"",
    "<INTEGER_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<ID>",
    "<VARIABLE>",
    "<LETTER>",
    "<DIGIT>",
    "\";\"",
    "\".\"",
    "\"<\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"=\"",
    "\"!=\"",
    "\"<>\"",
    "\"(\"",
    "\")\"",
    "\"*\"",
    "\"/\"",
    "\"+\"",
    "\"-\"",
    "\"?\"",
    "\"||\"",
    "\",\"",
  };

}
