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
package it.cnr.icar.eric.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;


/**
 *
 * @author Paul Sterk
 */
public class JavaFileLocalizer {
    private static JavaFileLocalizer instance = null;
    
    public synchronized static JavaFileLocalizer getInstance() {
        if (instance == null) {
            instance = new JavaFileLocalizer();
        }
        return instance;
    }

    
    private static int getLogIndex(String line) {
        int index = line.indexOf("log.info(\"");
        if (index != -1) {
            index += "log.info(\"".length();
        } else {    
            index = line.indexOf("log.warn(\"");
            if (index != -1) {
                index += "log.warn(\"".length();
            } else {
                index = line.indexOf("log.error(\"");
                if (index != -1) {
                    index += "log.error(\"".length();
                } else {
                    index = line.indexOf("log.fatal(\"");
                    if (index != -1) {
                        index += "log.fatal(\"".length();
                    }
                }
            }
        }      
        return index;
    }
    
    public static String getRbHelperClass(String filename) {
        String rbHelperClass = null;
        int index = filename.indexOf("/eric/client/ui/thin");
        if (index != -1) {
            rbHelperClass = "WebUIResourceBundle";
        } else {
            index = filename.indexOf("/eric/client/ui/swing");
            if (index != -1) {
                rbHelperClass = "JavaUIResourceBundle";
            } else {
                index = filename.indexOf("/eric/client/admin");
                if (index != -1) {
                    rbHelperClass = "AdminResourceBundle";
                } else {
                    index = filename.indexOf("/eric/client/xml/registry");
                    if (index != -1) {
                        rbHelperClass = "JAXRResourceBundle";
                    } else {
                        index = filename.indexOf("/eric/common");
                        if (index != -1) {
                            rbHelperClass = "CommonResourceBundle";
                        } else {
                            index = filename.indexOf("/eric/server");
                            if (index != -1) {
                                rbHelperClass = "ServerResourceBundle";
                            }
                        }
                    }
                }
            }
        }
        return rbHelperClass;
    }
    
    public static void main(String[] args) {
        PrintWriter newRBfile = null;
        PrintWriter newJavafile = null;
        BufferedReader in = null;
        try {           
            String rbHelperClass = null;
            String ericWorkspace = args[0];
            ericWorkspace += "/";
            System.out.println("eric workspace:" + ericWorkspace);
            String filename = args[1];
            System.out.println("java file to localize: " + filename);
            rbHelperClass = getRbHelperClass(filename);
            String fullFileName = ericWorkspace + filename;
            System.out.println("java full filename to localize: " + fullFileName);
            
            int startIndex = filename.lastIndexOf('/');
            int endIndex = filename.lastIndexOf('.');
            String fname = filename.substring(startIndex+1, endIndex);
            
            in = new BufferedReader(new FileReader(fullFileName));
            
            String rbFileName = ericWorkspace + 
                 "src/resources/ResourceBundle.log."+fname+".properties";
            System.out.println("full resource bundle file: "+rbFileName);
            //BufferedReader propin = new BufferedReader(new FileReader(propFileName));
            newRBfile = new PrintWriter(new FileOutputStream(new File(rbFileName)));
            newJavafile = new PrintWriter(new FileOutputStream(new File(fullFileName+".new")));
            String origLine = null;
            @SuppressWarnings("unused")
			int keyNumber = 1;
            while((origLine = in.readLine()) != null) {
                int startLogMsgIndex = getLogIndex(origLine);
                // if you find a line with log.info log.error or log.fatal
                if (startLogMsgIndex != -1) {
                    int endLogMsgIndex = origLine.indexOf(';');
                    if (endLogMsgIndex == -1) {
                        endLogMsgIndex = origLine.lastIndexOf('+') + 1;
                    }
                    if (endLogMsgIndex != -1) {
                        // logging takes up just 1 line
                        boolean checkForEndMsg = false;
                        boolean checkForNextArg = false;
                        String msg = origLine.substring(startLogMsgIndex,  
                                                        endLogMsgIndex);
                        String msgPrefix = origLine.substring(0, startLogMsgIndex-1);
                        
                        char[] msgArray = msg.toCharArray();
                        char[] keyArray = new char[1024];
                        char[] iMsgArray = new char[1024];
                        char[][] argsArray = new char[10][100];
                        String iMsg = null;
                        String key = null;
                        int endMsgIndex = 0;
                        int keyIndex = 0;
                        int placeHolderIndex = -1;
                        int argCharIndex = 0;
                        boolean toUpper = false;
                        boolean newArg = true;
                        boolean workingOnArg = false;
                        int iIndex = 0;
                        int msgIndex = 0;
                        for ( ; msgIndex < msgArray.length; msgIndex++) {
                            char ch = msgArray[msgIndex];
                            if (!checkForEndMsg) {                                
                                if (ch == '"') {
                                    // this may be the end of the message
                                    checkForEndMsg = true;
                                } else {
                                    iMsgArray[iIndex] = ch;
                                    iIndex++;
                                    // not a space, key char to the key
                                    if (ch == ' ') { 
                                        toUpper = true;
                                    } else if (ch != '.' && ch != ':') {          
                                        if (toUpper) {
                                            keyArray[keyIndex] = Character.toUpperCase(ch);
                                            toUpper = false;
                                        } else {
                                            keyArray[keyIndex] = ch;
                                        }
                                        keyIndex++;
                                    }
                                }    
                            } else {
                                // look for a ) or a + sign
                                if (checkForNextArg) {
                                    if (ch == '"') {
                                        // we have another string. stop checking
                                        // for end or next arg
                                        checkForNextArg = false;
                                        checkForEndMsg = false;
                                    } else if (ch != ',' && ch != ':' 
                                               && ch != '+') {
                                            
                                        if ((ch == ')' && iMsgArray[iIndex-1] != '(')
                                            || (ch == ' ' && workingOnArg)) {
                                            checkForNextArg = false;
                                            //checkForEndMsg = false;
                                            workingOnArg = false;
                                            // end of message
                                            iMsg = new String(iMsgArray);
                                            iMsg = iMsg.trim();
                                            key = new String(keyArray);
                                            key = key.trim();
                                        }
                                        // get the name of the arg
                                        else if (ch != ' ') {
                                                                                   // we have another char, but it is not
                                            // a string. It must be an argument
                                            // add placeholder to key
                                            if (newArg) {                                                                              
                                                placeHolderIndex++;
                                                newArg = false;

                                                iMsgArray[iIndex] = '{';
                                                iIndex++;
                                                iMsgArray[iIndex] = String.valueOf(placeHolderIndex).charAt(0);
                                                iIndex++;
                                                iMsgArray[iIndex] = '}';
                                                iIndex++;
                                            }                                          
                                            argsArray[placeHolderIndex][argCharIndex] = ch;
                                            argCharIndex++;
                                            workingOnArg = true;
                                        } 
                                    }// else if (ch == ' ') {
                                        // stop checking
                                     //   checkForNextArg = false;
                                    //}
                                } else {
                                    if (ch == ')' || ch == ';') {
                                        // end of message
                                        iMsg = new String(iMsgArray);
                                        iMsg = iMsg.trim();
                                        key = new String(keyArray);
                                        key = key.trim();
                                    } else if (ch == '+') {
                                        // we could have an argument or a new string
                                        checkForNextArg = true;
                                        newArg = true;
                                        argCharIndex = 0;
                                        // at end of line with a plus sign
                                        // read next line
                                        if (msgIndex == msg.length()-1) {
                                            origLine = in.readLine();
                                            endLogMsgIndex = origLine.indexOf(';');
                                            if (endLogMsgIndex != -1) {                               
                                                startLogMsgIndex = origLine.indexOf('"');
                                                if (startLogMsgIndex == -1) {
                                                    msg = origLine.trim();
                                                    checkForNextArg = true;                                                   
                                                    checkForEndMsg = true;
                                                } else {
                                                    msg = " " + origLine.substring(startLogMsgIndex+1, endLogMsgIndex);                                               
                                                    checkForNextArg = false;
                                                    checkForEndMsg = false;
                                                }
                                                msgArray = msg.toCharArray();
                                                msgIndex = 0;
                                            }
                                        }
                                    }
                                }
                            }                                                       
                            endMsgIndex = msgIndex;
                        }
                        int commaIndex = origLine.indexOf(',');
                        String msgSuffix = null;
                        if (commaIndex > -1) {
                            msgSuffix = origLine.substring(commaIndex);
                        } else {
                            msgSuffix = origLine.substring(startLogMsgIndex+endMsgIndex);
                        }
                     
                        String newLine = msgPrefix + rbHelperClass +
                            ".getInstance().getString(\"message." + key;
                        if (placeHolderIndex > -1) {
                            newLine += "\", new Object[]{";
                            for (int j = 0; j <= placeHolderIndex; j++) {
                                if (j > 0) {
                                    newLine += ", ";
                                }
                                String arg = new String(argsArray[j]);
                                arg = arg.trim();
                                newLine += arg;
                            }
                            newLine += "})";
                            placeHolderIndex = -1;
                            newLine += msgSuffix;
                        } else {
                            newLine += "\")" + msgSuffix;
                        }
                        newRBfile.println("message." + key + "=" + iMsg);
                        newJavafile.println(newLine);

                    } else {
                        // logging takes up > 1 line
                    }
                } else {
                    newJavafile.println(origLine);
                }
            }
        } catch (Exception e){
            System.out.println(e);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            	in = null;
            }
            try {
                newJavafile.close();
            } catch (Throwable ex) {
            	newJavafile = null;
            }
            try {
                newRBfile.close();
            } catch (Throwable ex) {
            	newRBfile = null;
            }
        }
    }
    
    
    static class LocalizerClassLoader extends ClassLoader {
        protected URL findResource(String name) {
            URL resourceUrl = null;
            try {
                resourceUrl = new URL("file", "localhost", name);
            } catch (Exception ex) {
            	resourceUrl = null;
            }
            return resourceUrl;
        }
    }
}
