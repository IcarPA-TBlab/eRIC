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
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Based on grammar by Kevin (mailto:kevinh@empower.com.au) at: http://www.cobase.cs.ucla.edu/pub/javacc/sql-oracle.jj
 */
class SQLParser implements SQLParserConstants {

        PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();

        private ResponseOptionType responseOption = null;
        public String firstTableName = null;
        public HashMap<String, String> aliasToTableNameMap = new HashMap<String, String>();
        public String selectColName = null;
        public String selectColAlias = null;
        public String selectColAndAlias = null;

        private static HashMap<String, ?> collectionAttributeMap = new HashMap<String, Object>();
        private boolean inINClause = false;

        @SuppressWarnings("unused")
		private boolean beginAssocQuery = false;
        @SuppressWarnings("unused")
		private UserType user = null;

        static {
                //FROM RIM: All attributes made lower case for case insensitive comparison
                collectionAttributeMap.put("associatedobjects", null);
                collectionAttributeMap.put("audittrail", null);
                collectionAttributeMap.put("classificationnodes", null);
                collectionAttributeMap.put("externallinks", null);
                collectionAttributeMap.put("packages", null);
                collectionAttributeMap.put("memberobjects", null);
                collectionAttributeMap.put("classifiedobjects", null);
                collectionAttributeMap.put("permissions", null);
                collectionAttributeMap.put("privileges", null);
                collectionAttributeMap.put("privilegeattributes", null);
                collectionAttributeMap.put("groups", null);
                collectionAttributeMap.put("identities", null);
                collectionAttributeMap.put("roles", null);
        }

        private String getSelectColumn(String idName) {
                String selectCol = idName;

                int index = idName.indexOf('.');

                if ((!inINClause) && (index == -1)  ) {

                        if (responseOption.getReturnType() == ReturnType.OBJECT_REF) {
                                selectCol = "id";
                        }
                        else {
                                selectCol = "*";
                        }
                }

                return selectCol;
        }

        private String getAttributeNameFromColRef(String colRef) {
                String attributeName = null;

                //Get the last path element
                int lastPeriodIndex = colRef.lastIndexOf('.');
                attributeName = colRef.substring(lastPeriodIndex+1);

                return attributeName.toLowerCase();
        }

        private boolean isPrimitiveAttribute(String colRef) {
                boolean primitiveAttribute = true;

                String attributeName = getAttributeNameFromColRef(colRef);

                if (collectionAttributeMap.containsKey(attributeName)) {
                        primitiveAttribute = false;
                }

                //System.err.println("attributeName='" + attributeName + "' isPrimitiveAttribute=" + primitiveAttribute);

                return primitiveAttribute;
        }

        private String mapCollectionAttributeToIdList(String attributeName) {
                String idList = "1, 2, 3, 4, 5";



                return idList;
        }

        private String pruneUnusedTableRefs(String oldStr) {
            String newStr = oldStr;

            Set<String> aliases = aliasToTableNameMap.keySet();
            Iterator<String> iter = aliases.iterator();
            while (iter.hasNext()) {
                String alias = iter.next();
                String tableName = aliasToTableNameMap.get(alias);

                if (!(tableName.equals(firstTableName))) {

                    //See if "alias." missing in oldStr. If so need to prune tableref
                    boolean keepTableRef = oldStr.matches(".*"+alias+ "\\s*\\..*");

                    if (!keepTableRef) {
                        if (oldStr.matches(".*"+tableName+"\\s*" + alias + "\\s*\\,.*")) {
                            newStr = newStr.replaceAll(tableName+"\\s*" + alias + "\\s*\\,", " ");
                        } else if (oldStr.matches(".*"+tableName+"\\s*" + alias + "\\s*.*")) {
                            newStr = newStr.replaceAll(tableName+"\\s*" + alias + "\\s*", " ");
                        }
                    }
                }
            }

            //Handle case where trailing "," left before WHERE
            newStr = newStr.replaceAll(",\\s*WHERE", " WHERE ");

            return newStr;
        }

/*******************************************************************
 * The Registry Query (Subset of SQL-92) grammar starts here
 *******************************************************************/
  @SuppressWarnings("unused")
final public String processQuery(UserType user, ResponseOptionType responseOption) throws ParseException {
        this.responseOption = responseOption;
        String rs = "";
        this.user = user;
    rs = query_exp();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SEMICOLON:
      jj_consume_token(SEMICOLON);
      break;
    default:
      jj_la1[0] = jj_gen;
    }
    jj_consume_token(0);
            {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String query_exp() throws ParseException {
  String rs = "", ts;
    ts = query_term();
                     rs = rs + ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case UNION:
      jj_consume_token(UNION);
             rs = rs + "UNION";
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ALL:
        jj_consume_token(ALL);
                                           rs = rs + "ALL";
        break;
      default:
        jj_la1[1] = jj_gen;
      }
      ts = query_term();
                                                                                  rs = rs + ts;
      break;
    default:
      jj_la1[2] = jj_gen;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String query_term() throws ParseException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SELECT:
      ts = SQLSelect();
                     rs = rs + ts;
      break;
    case OPENPAREN:
      jj_consume_token(OPENPAREN);
          rs = rs + " ( ";
      ts = query_exp();
                                               rs = rs + ts;
      jj_consume_token(CLOSEPAREN);
                                                                     rs = rs + " ) ";
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLSelect() throws ParseException {
  String rs = "", ts;
    jj_consume_token(SELECT);
        rs = " SELECT ";
    ts = SQLSelectCols();
        rs = rs + ts;
    jj_consume_token(FROM);
        rs = rs + " FROM ";
    ts = SQLTableList();
                        rs = rs + ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case WHERE:
      ts = SQLWhere();
                        rs = rs + ts;
      break;
    default:
      jj_la1[4] = jj_gen;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ORDER:
      ts = SQLOrderBy();
                          rs = rs + ts;
      break;
    default:
      jj_la1[5] = jj_gen;
    }
        rs = pruneUnusedTableRefs(rs);
        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLSelectCols() throws ParseException {
  String idName=null;
  String rs = "";
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ALL:
      case DISTINCT:
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ALL:
        jj_consume_token(ALL);
            rs = rs + " ALL ";
        break;
      case DISTINCT:
        jj_consume_token(DISTINCT);
                 rs = rs + " DISTINCT ";
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASTERISK:
      jj_consume_token(ASTERISK);
                idName = "*";
      break;
    default:
      jj_la1[8] = jj_gen;
      if (jj_2_1(2)) {
        idName = SQLFunction();
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ID:
        case VARIABLE:
        case QUESTIONMARK:
          idName = SQLLvalueTerm();
          break;
        default:
          jj_la1[9] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
                selectColAndAlias = idName;
                int index = idName.indexOf('.');
                if (index == -1) {
                        selectColName = idName;
                        selectColAlias = null;
                }
                else {
                        selectColName = idName.substring(index+1, idName.length());
                        selectColAlias = idName.substring(0, index);
                }
                /*
		if ((!(selectColName.equalsIgnoreCase("id") || (selectColName.equalsIgnoreCase("*")) ) {
			throw new ParseException("Invalid select column '" + selectColName + "'. The only select columns allowed is 'id' or '*'.");
		}
		*/

        String selectCol = getSelectColumn(idName);
        rs = rs + selectCol + " ";

        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLTableList() throws ParseException {
  String rs = "", ts;
    ts = SQLTableRef();
                       rs = rs + ts;
    label_2:
    while (true) {
      if (jj_2_2(2)) {
      } else {
        break label_2;
      }
      jj_consume_token(57);
                       rs = rs + ", ";
      ts = SQLTableRef();
                       rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLTableRef() throws ParseException {
        Token tableName;
        Token aliasName;
        String rs = "";

        String tableNameStr=null;
        String aliasNameStr=null;
    tableName = jj_consume_token(ID);
        tableNameStr = tableName.image;

        //Be forgiving to those that use the infomodel class name rather than the
        //SQL table name which had to be different due to the name being a reserved word in SQL
        tableNameStr = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(tableNameStr);

        //Special case processing for extramural Associations
        if (tableNameStr.equalsIgnoreCase("Association")) {
                //??beginAssocQuery = true;
        }

        rs = rs + tableNameStr + " ";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      aliasName = jj_consume_token(ID);
        aliasNameStr = aliasName.image;
        rs = rs + aliasName.image + " ";
      break;
    default:
      jj_la1[10] = jj_gen;
    }
        if (firstTableName == null) {
                firstTableName = tableNameStr;
        }
        if ((aliasNameStr != null) && (aliasNameStr.length() > 0)) {
                //System.err.println("Adding aliasNameStr=" + aliasNameStr + " tableNameStr=" + tableNameStr);
                aliasToTableNameMap.put(aliasNameStr, tableNameStr);
        }
        {if (true) return rs.trim();}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLWhere() throws ParseException {
  String rs = "", ts;
    jj_consume_token(WHERE);
        rs = rs + " WHERE ";
    ts = SQLOrExpr();
        rs = rs + ts;
        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLOrExpr() throws ParseException {
  String rs = "", ts;
    ts = SQLAndExpr();
                      rs = rs + ts;
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_3;
      }
      jj_consume_token(OR);
          rs = rs + " OR ";
      ts = SQLAndExpr();
        rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLAndExpr() throws ParseException {
  String rs = "", ts;
    try {
      ts = SQLNotExpr();
            rs = rs + ts;
            //System.err.println("    SQLNotExpr()=" + ts);

    } catch (PrunePredicateException e) {
        // TODO: This module is missing a logger...
        e.printStackTrace();    //Should not happen

    }
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        break;
      default:
        jj_la1[12] = jj_gen;
        break label_4;
      }
      try {
        jj_consume_token(AND);
        ts = SQLNotExpr();
                rs = rs + " AND " + ts;
                //System.err.println("<AND> SQLNotExpr()= AND " + ts);

      } catch (PrunePredicateException e) {

      }
    }
    //System.err.println("SQLAndExpr()=" + rs);
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLNotExpr() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
          rs = rs + " NOT ";
      break;
    default:
      jj_la1[13] = jj_gen;
    }
    ts = SQLCompareExpr();
        rs = rs + ts;
        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLCompareExpr() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    if (jj_2_3(2147483647)) {
      ts = SQLIsClause();
                                                     rs = rs + ts;
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
      case STRING_LITERAL:
      case ID:
      case VARIABLE:
      case OPENPAREN:
      case PLUS:
      case MINUS:
      case QUESTIONMARK:
        ts = SQLSumExpr();
                        rs = rs + ts;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IN:
        case LIKE:
        case NOT:
        case LESS:
        case LESSEQUAL:
        case GREATER:
        case GREATEREQUAL:
        case EQUAL:
        case NOTEQUAL:
        case NOTEQUAL2:
          ts = SQLCompareExprRight();
                                                                       rs = rs + ts;
          break;
        default:
          jj_la1[14] = jj_gen;
        }
        break;
      default:
        jj_la1[15] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLCompareExprRight() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    if (jj_2_4(2)) {
      ts = SQLLikeClause();
                                        rs = rs + ts;
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IN:
      case NOT:
        ts = SQLInClause();
                         rs = rs + ts;
        break;
      case LESS:
      case LESSEQUAL:
      case GREATER:
      case GREATEREQUAL:
      case EQUAL:
      case NOTEQUAL:
      case NOTEQUAL2:
        ts = SQLCompareOp();
                          rs = rs + ts;
        ts = SQLSumExpr();
                        rs = rs + ts;
        break;
      default:
        jj_la1[16] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
          {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLCompareOp() throws ParseException {
  String rs = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EQUAL:
      jj_consume_token(EQUAL);
                     rs = rs + " = ";
      break;
    case NOTEQUAL:
      jj_consume_token(NOTEQUAL);
                     rs = rs + " <> ";
      break;
    case NOTEQUAL2:
      jj_consume_token(NOTEQUAL2);
                     rs = rs + " <> ";
      break;
    case GREATER:
      jj_consume_token(GREATER);
                     rs = rs + " > ";
      break;
    case GREATEREQUAL:
      jj_consume_token(GREATEREQUAL);
                     rs = rs + " >= ";
      break;
    case LESS:
      jj_consume_token(LESS);
                     rs = rs + " < ";
      break;
    case LESSEQUAL:
      jj_consume_token(LESSEQUAL);
                     rs = rs + " <= ";
      break;
    default:
      jj_la1[17] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLFunction() throws ParseException {
Token functionName;
String rs = "", ts;
    functionName = jj_consume_token(ID);
                            rs = rs + " " + functionName.image;
    try {
      ts = SQLFunctionArgs();
                                 rs = rs + ts;
    } catch (PrunePredicateException e) {
        // TODO: This module is missing a logger...
        e.printStackTrace(); //Should not happen

    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLFunctionArgs() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    jj_consume_token(OPENPAREN);
                rs = rs + "( ";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case STRING_LITERAL:
    case ID:
    case VARIABLE:
    case OPENPAREN:
    case PLUS:
    case MINUS:
    case QUESTIONMARK:
      ts = SQLSumExpr();
                        rs = rs + ts;
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 57:
          break;
        default:
          jj_la1[18] = jj_gen;
          break label_5;
        }
        jj_consume_token(57);
            rs = rs + ", ";
        ts = SQLSumExpr();
                              rs = rs + ts;
      }
      break;
    default:
      jj_la1[19] = jj_gen;
    }
    jj_consume_token(CLOSEPAREN);
                 rs = rs + " ) "; {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLInClause() throws ParseException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
            rs = rs + " NOT ";
      break;
    default:
      jj_la1[20] = jj_gen;
    }
    jj_consume_token(IN);
         rs = rs + " IN ";
    jj_consume_token(OPENPAREN);
        rs = rs + "( "; inINClause = true;
    ts = SQLLValueListOrProcedureCall();
                                        rs = rs + ts;
    jj_consume_token(CLOSEPAREN);
        rs = rs + ") "; inINClause = true;
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLValueListOrProcedureCall() throws ParseException {
  String rs = "", ts;
    if (jj_2_5(2)) {
      ts = ProcedureCall();
                                      rs = rs + ts;
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NULL:
      case SELECT:
      case INTEGER_LITERAL:
      case FLOATING_POINT_LITERAL:
      case STRING_LITERAL:
      case ID:
      case VARIABLE:
      case OPENPAREN:
      case PLUS:
      case MINUS:
      case QUESTIONMARK:
        ts = SQLLValueList();
                           rs = rs + ts;
        break;
      default:
        jj_la1[21] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String ProcedureCall() throws ParseException {
String rs = "";
Token procName = null;
Token param = null;
    procName = jj_consume_token(ID);
    jj_consume_token(OPENPAREN);
    param = jj_consume_token(STRING_LITERAL);
    jj_consume_token(CLOSEPAREN);

        String id = param.image.substring(1, param.image.length()-1);

        String sqlQuery = null;
        String tableName = null;

        if (procName.image.equalsIgnoreCase("RegistryObject_auditTrail")) {
                sqlQuery = "SELECT id FROM AuditableEvent ae, AffectedObject ao WHERE ao.id = '" + id + "' AND ao.eventId = ae.id ";
                tableName = "AuditableEvent";
        }
        else if (procName.image.equalsIgnoreCase("RegistryObject_externalLinks")) {
                sqlQuery = "SELECT sourceObject FROM Association WHERE associationType = 'ExternallyLinks' AND targetObject = '" + id + "' ";
                tableName = "Association";
        }
        else if (procName.image.equalsIgnoreCase("RegistryObject_externalIdentifiers")) {
                sqlQuery = "SELECT id FROM ExternalIdentifier WHERE registryObject = '" + id + "' ";
                tableName = "ExternalIdentifier";
        }
        else if (procName.image.equalsIgnoreCase("RegistryObject_classifications")) {
                sqlQuery = "SELECT id FROM Classification WHERE classifiedObject = '" + id + "' ";
                tableName = "Classification";
        }
        else if (procName.image.equalsIgnoreCase("RegistryObject_registryPackages")) {
                sqlQuery = "SELECT sourceObject FROM Association WHERE associationType = 'HasMember' AND targetObject = '" + id + "' ";
                tableName = "Association";
        }
        else if (procName.image.equalsIgnoreCase("RegistryPackage_memberObjects")) {
                sqlQuery = "SELECT targetObject FROM Association WHERE associationType = 'HasMember' AND sourceObject = '" + id + "' ";
                tableName = "Association";
        }
        else if (procName.image.equalsIgnoreCase("ExternalLink_linkedObjects")) {
                sqlQuery = "SELECT targetObject FROM Association WHERE associationType = 'ExternallyLinks' AND sourceObject = '" + id + "' ";
                tableName = "Association";
        }
        else if (procName.image.equalsIgnoreCase("ClassificationNode_classifiedObjects")) {
                sqlQuery = "SELECT classifiedObject FROM ClassificationNode WHERE id = '" + id + "' ";
                tableName = "Association";
        }
        else {
                {if (true) throw new ParseException("Invalid stored procedure name " + procName.image);}
        }

        /*
	ResponseOption responseOption = new ResponseOption();
	responseOption.setReturnType(ReturnType.OBJECT_REF);
	responseOption.setReturnComposedObjects(false);

	ArrayList ids = null;
	try {
		ids = it.cnr.icar.eric.server.common.Utility.getInstance().executeSQLQuery(sqlQuery, responseOption, tableName);
	}
	catch (RegistryException e) {
		// TODO: This module is missing a logger...
		e.printStackTrace();
		throw new ParseException(e.toString());
	}

	Iterator iter = ids.iterator();

	while(iter.hasNext()) {
		ObjectRef or = (ObjectRef)iter.next();
		String id_ = or.getId();

		if (iter.hasNext()) {
			rs = rs + "'" + id_ + "', ";
		}
		else {
			rs = rs + "'" + id_ + "' ";
		}
	}
	*/

        rs = sqlQuery;

        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLValueList() throws ParseException {
  String rs = "", ts;
    ts = SQLLValueElement();
                            rs = rs + ts;
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 57:
        break;
      default:
        jj_la1[22] = jj_gen;
        break label_6;
      }
      jj_consume_token(57);
          rs = rs + ", ";
      ts = SQLLValueElement();
                              rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLValueElement() throws ParseException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NULL:
      jj_consume_token(NULL);
             rs = rs + " NULL ";
      break;
    case INTEGER_LITERAL:
    case FLOATING_POINT_LITERAL:
    case STRING_LITERAL:
    case ID:
    case VARIABLE:
    case OPENPAREN:
    case PLUS:
    case MINUS:
    case QUESTIONMARK:
      try {
        ts = SQLSumExpr();
                            rs = rs + ts;
      } catch (PrunePredicateException e) {
        // TODO: This module is missing a logger...
        e.printStackTrace(); //Should not happen

      }
      break;
    case SELECT:
      ts = SQLSelect();
                       rs = rs + ts;
      break;
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLIsClause() throws ParseException {
  String rs = "", ts;
    ts = SQLColRef();
        rs = rs + ts;
    jj_consume_token(IS);
         rs = rs + " IS ";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
            rs = rs + " NOT ";
      break;
    default:
      jj_la1[24] = jj_gen;
    }
    jj_consume_token(NULL);
           rs = rs + " NULL ";
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLikeClause() throws ParseException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
            rs = rs + " NOT ";
      break;
    default:
      jj_la1[25] = jj_gen;
    }
    jj_consume_token(LIKE);
           rs = rs + " LIKE ";
    ts = SQLPattern();
                      rs = rs + ts;
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLPattern() throws ParseException {
  String rs = "", ts;
    if (jj_2_6(2)) {
      ts = SQLFunction();
                                    rs = rs + ts;
    } else if (jj_2_7(2)) {
      ts = stringLiteralOrConcatenatedString();
                                                            rs = rs + ts;
    } else if (jj_2_8(2)) {
      ts = SQLLvalue();
                                    rs = rs + ts;
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLColRef() throws ParseException {
  String s, rs = "";
    s = SQLLvalue();
        //Be forgiving to those that use the infomodel attribute name rather than the
        //SQL column name which had to be different due to the name being a reserved word in SQL
        s = it.cnr.icar.eric.common.Utility.getInstance().mapColumnName(s);
        rs = (rs + s).trim();

        if ((!inINClause) && (!isPrimitiveAttribute(rs))) {
                {if (true) throw new ParseException("Invalid Query: Collection attribute " + rs + " is only valid in an IN clause");}
        }
        else if ((inINClause) && (!isPrimitiveAttribute(rs))) {
                //Need to map collection attribute to a list of IDs
                rs = mapCollectionAttributeToIdList(rs);
        }

        {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLvalue() throws ParseException {
  String s = "", t;
    t = SQLLvalueTerm();
          s = s + t + " ";
          {if (true) return s;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLvalueTerm() throws ParseException {
  Token x;
  String s = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      x = jj_consume_token(ID);
              s = x.image + " ";
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case DOT:
          break;
        default:
          jj_la1[26] = jj_gen;
          break label_7;
        }
        jj_consume_token(DOT);
        ts = idOrStar();
                      s = s.trim() + ".";
                      s = s + it.cnr.icar.eric.common.Utility.getInstance().mapColumnName(ts) + " ";
      }
      break;
    case QUESTIONMARK:
      jj_consume_token(QUESTIONMARK);
                       s = s + "?";
      break;
    case VARIABLE:
      x = jj_consume_token(VARIABLE);
                       s = x.image + " ";
      break;
    default:
      jj_la1[27] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return s;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String idOrStar() throws ParseException {
  Token x;
  String s = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      x = jj_consume_token(ID);
             s = x.image + " ";
      break;
    case ASTERISK:
      jj_consume_token(ASTERISK);
          s = "* ";
      break;
    default:
      jj_la1[28] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return s;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLSumExpr() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    ts = SQLProductExpr();
                            rs = rs + ts;
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        break;
      default:
        jj_la1[29] = jj_gen;
        break label_8;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
              rs = rs + "+ ";
        break;
      case MINUS:
        jj_consume_token(MINUS);
                rs = rs + "- ";
        break;
      default:
        jj_la1[30] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      ts = SQLProductExpr();
                              rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLProductExpr() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    ts = SQLUnaryExpr();
                        rs = rs + ts;
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASTERISK:
      case SLASH:
        break;
      default:
        jj_la1[31] = jj_gen;
        break label_9;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASTERISK:
        jj_consume_token(ASTERISK);
            rs = rs + "* ";
        break;
      case SLASH:
        jj_consume_token(SLASH);
            rs = rs + "/ ";
        break;
      default:
        jj_la1[32] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      ts = SQLUnaryExpr();
                            rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLUnaryExpr() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
    case MINUS:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
            rs = rs + "+ ";
        break;
      case MINUS:
        jj_consume_token(MINUS);
            rs = rs + "- ";
        break;
      default:
        jj_la1[33] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[34] = jj_gen;
    }
    ts = SQLTerm();
                     rs = rs + ts;
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLTerm() throws ParseException, PrunePredicateException {
  String rs = "", ts;
    if (jj_2_9(2)) {
      ts = SQLFunction();
                                       rs = rs + ts;
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OPENPAREN:
        jj_consume_token(OPENPAREN);
           rs = rs + "( ";
        ts = SQLOrExpr();
                       rs = rs + ts;
        jj_consume_token(CLOSEPAREN);
        //For parameterized query, do predicate pruning in case a predicate parameter has not been supplied
        if (ts.indexOf('$') != -1) {
            //System.err.println("Pruning SQLTerm = " + rs + ") ");
            {if (true) throw new PrunePredicateException();}
        } else {
            rs = rs + ") ";
            //System.err.println("SQLTerm = " + rs);
        }
        break;
      default:
        jj_la1[35] = jj_gen;
        if (jj_2_10(2)) {
          ts = SQLColRef();
                                    rs = rs + ts;
        } else {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case INTEGER_LITERAL:
          case FLOATING_POINT_LITERAL:
          case STRING_LITERAL:
          case VARIABLE:
          case QUESTIONMARK:
            ts = SQLLiteralOrVar();
                             rs = rs + ts;
            break;
          default:
            jj_la1[36] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
        }
      }
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLiteral() throws ParseException {
 Token x;
 String rs = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING_LITERAL:
      x = jj_consume_token(STRING_LITERAL);
                           rs = rs + x.image + " ";
      break;
    case INTEGER_LITERAL:
      x = jj_consume_token(INTEGER_LITERAL);
                            rs = rs + x.image + " ";
      break;
    case FLOATING_POINT_LITERAL:
      x = jj_consume_token(FLOATING_POINT_LITERAL);
                                   rs = rs + x.image + " ";
      break;
    default:
      jj_la1[37] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String stringLiteralOrConcatenatedString() throws ParseException {
  String rs = "", ts;
    /* Though concatenation has a higher precedence than (for example) LIKE,
       * some databases (HSQLDB) get a bit confused.  Add parens to help out.
       */
      ts = concatenatedString();
                              rs = rs + "(" + ts + ") ";
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String concatenatedString() throws ParseException {
  String rs = "", ts;
    ts = StringLiteralOrVar();
                              rs = rs + ts + " ";
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PIPES:
        break;
      default:
        jj_la1[38] = jj_gen;
        break label_10;
      }
      jj_consume_token(PIPES);
              rs = rs + " || ";
      ts = StringLiteralOrVar();
                                rs = rs + ts + " ";
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String StringLiteralOrVar() throws ParseException {
 Token x;
 String rs = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING_LITERAL:
      x = jj_consume_token(STRING_LITERAL);
                         rs = rs + x.image + " ";
      break;
    case VARIABLE:
      x = jj_consume_token(VARIABLE);
                     rs = rs + x.image + " ";
      break;
    case QUESTIONMARK:
      jj_consume_token(QUESTIONMARK);
                     rs = rs + " ? ";
      break;
    default:
      jj_la1[39] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLLiteralOrVar() throws ParseException {
 Token x;
 String rs = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING_LITERAL:
      x = jj_consume_token(STRING_LITERAL);
                         rs = rs + x.image + " ";
      break;
    case INTEGER_LITERAL:
      x = jj_consume_token(INTEGER_LITERAL);
                            rs = rs + x.image + " ";
      break;
    case FLOATING_POINT_LITERAL:
      x = jj_consume_token(FLOATING_POINT_LITERAL);
                                   rs = rs + x.image + " ";
      break;
    case VARIABLE:
      x = jj_consume_token(VARIABLE);
                     rs = rs + x.image + " ";
      break;
    case QUESTIONMARK:
      jj_consume_token(QUESTIONMARK);
                     rs = rs + " ? ";
      break;
    default:
      jj_la1[40] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLOrderBy() throws ParseException {
  String rs = "", ts;
    jj_consume_token(ORDER);
    jj_consume_token(BY);
                 rs = rs + " ORDER BY ";
    ts = SQLOrderByList();
                          rs = rs + ts;
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLOrderByElem() throws ParseException {
  String rs = "", ts;
    ts = SQLColRef();
                     rs = rs + ts;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASC:
    case DESC:
      ts = SQLOrderDirection();
                               rs = rs + ts;
      break;
    default:
      jj_la1[41] = jj_gen;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLOrderByList() throws ParseException {
  String rs = "", ts;
    ts = SQLOrderByElem();
                          rs = rs + ts;
    label_11:
    while (true) {
      if (jj_2_11(2)) {
      } else {
        break label_11;
      }
      jj_consume_token(57);
          rs = rs + ", ";
      ts = SQLOrderByElem();
                            rs = rs + ts;
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  @SuppressWarnings("unused")
final public String SQLOrderDirection() throws ParseException {
  String rs = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASC:
      jj_consume_token(ASC);
            rs = rs + " ASC";
      break;
    case DESC:
      jj_consume_token(DESC);
             rs = rs + " DESC";
      break;
    default:
      jj_la1[42] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rs;}
    throw new Error("Missing return statement in function");
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  final private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  final private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  final private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  final private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  final private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  final private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  final private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  final private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  final private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  final private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  final private boolean jj_3R_20() {
    if (jj_scan_token(OPENPAREN)) return true;
    return false;
  }

  final private boolean jj_3R_14() {
    if (jj_3R_18()) return true;
    return false;
  }

  final private boolean jj_3_2() {
    if (jj_scan_token(57)) return true;
    if (jj_3R_13()) return true;
    return false;
  }

  final private boolean jj_3_8() {
    if (jj_3R_18()) return true;
    return false;
  }

  final private boolean jj_3_7() {
    if (jj_3R_17()) return true;
    return false;
  }

  final private boolean jj_3_6() {
    if (jj_3R_12()) return true;
    return false;
  }

  final private boolean jj_3R_12() {
    if (jj_scan_token(ID)) return true;
    if (jj_3R_20()) return true;
    return false;
  }

  final private boolean jj_3R_22() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_6()) {
    jj_scanpos = xsp;
    if (jj_3_7()) {
    jj_scanpos = xsp;
    if (jj_3_8()) return true;
    }
    }
    return false;
  }

  final private boolean jj_3R_32() {
    if (jj_scan_token(QUESTIONMARK)) return true;
    return false;
  }

  final private boolean jj_3R_21() {
    if (jj_scan_token(NOT)) return true;
    return false;
  }

  final private boolean jj_3R_31() {
    if (jj_scan_token(VARIABLE)) return true;
    return false;
  }

  final private boolean jj_3R_15() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_21()) jj_scanpos = xsp;
    if (jj_scan_token(LIKE)) return true;
    if (jj_3R_22()) return true;
    return false;
  }

  final private boolean jj_3R_30() {
    if (jj_scan_token(STRING_LITERAL)) return true;
    return false;
  }

  final private boolean jj_3R_25() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_30()) {
    jj_scanpos = xsp;
    if (jj_3R_31()) {
    jj_scanpos = xsp;
    if (jj_3R_32()) return true;
    }
    }
    return false;
  }

  final private boolean jj_3_1() {
    if (jj_3R_12()) return true;
    return false;
  }

  final private boolean jj_3R_36() {
    if (jj_scan_token(ASTERISK)) return true;
    return false;
  }

  final private boolean jj_3R_35() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  final private boolean jj_3R_34() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_35()) {
    jj_scanpos = xsp;
    if (jj_3R_36()) return true;
    }
    return false;
  }

  final private boolean jj_3R_26() {
    if (jj_scan_token(PIPES)) return true;
    return false;
  }

  final private boolean jj_3R_23() {
    if (jj_3R_25()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_26()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  final private boolean jj_3_4() {
    if (jj_3R_15()) return true;
    return false;
  }

  final private boolean jj_3R_29() {
    if (jj_scan_token(VARIABLE)) return true;
    return false;
  }

  final private boolean jj_3_3() {
    if (jj_3R_14()) return true;
    if (jj_scan_token(IS)) return true;
    return false;
  }

  final private boolean jj_3R_28() {
    if (jj_scan_token(QUESTIONMARK)) return true;
    return false;
  }

  final private boolean jj_3R_17() {
    if (jj_3R_23()) return true;
    return false;
  }

  final private boolean jj_3R_33() {
    if (jj_scan_token(DOT)) return true;
    if (jj_3R_34()) return true;
    return false;
  }

  final private boolean jj_3R_16() {
    if (jj_scan_token(ID)) return true;
    if (jj_scan_token(OPENPAREN)) return true;
    return false;
  }

  final private boolean jj_3R_27() {
    if (jj_scan_token(ID)) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_33()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  final private boolean jj_3R_24() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3R_28()) {
    jj_scanpos = xsp;
    if (jj_3R_29()) return true;
    }
    }
    return false;
  }

  final private boolean jj_3_5() {
    if (jj_3R_16()) return true;
    return false;
  }

  final private boolean jj_3_10() {
    if (jj_3R_14()) return true;
    return false;
  }

  final private boolean jj_3R_18() {
    if (jj_3R_24()) return true;
    return false;
  }

  final private boolean jj_3_11() {
    if (jj_scan_token(57)) return true;
    if (jj_3R_19()) return true;
    return false;
  }

  final private boolean jj_3R_13() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  final private boolean jj_3_9() {
    if (jj_3R_12()) return true;
    return false;
  }

  final private boolean jj_3R_19() {
    if (jj_3R_14()) return true;
    return false;
  }

  public SQLParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  @SuppressWarnings("unused")
private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[43];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x0,0x80,0x10000000,0x2000000,0x20000000,0x1000000,0x2080,0x2080,0x0,0x0,0x0,0x800000,0x100,0x200000,0x340000,0x0,0x240000,0x0,0x0,0x0,0x200000,0x2400000,0x0,0x2400000,0x200000,0x200000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x1200,0x1200,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x100,0x0,0x0,0x20000,0x0,0x0,0x0,0x0,0x80000,0x800030,0x10,0x0,0x0,0x0,0x1fc00,0xe2003b,0x1fc00,0x1fc00,0x2000000,0xe2003b,0x0,0xe2003b,0x2000000,0xe2003b,0x0,0x0,0x200,0x800030,0x80010,0x600000,0x600000,0x180000,0x180000,0x600000,0x600000,0x20000,0x80002b,0xb,0x1000000,0x800028,0x80002b,0x0,0x0,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[11];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public SQLParser(java.io.InputStream stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new SQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SQLParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new SQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SQLParser(SQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(SQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 43; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = -362958012772974426L; }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (@SuppressWarnings("rawtypes")
	java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[58];
    for (int i = 0; i < 58; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 43; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 58; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 11; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
