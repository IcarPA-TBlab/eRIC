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
package it.cnr.icar.eric.client.ui.thin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
  *
  * @author  dhilder
  */
public class ParameterBean implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 924405390855601056L;
	private String name;
    private String textValue = null;    
    private Boolean booleanValue = null;    
    private ArrayList<Object> listValue = null;    

    
    public ParameterBean(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getTextValue() {
        return textValue;
    }
    
    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
    
    public Boolean getBooleanValue() {
        return booleanValue;
    }
    
    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
    
    public java.lang.Object[] getListValue() {
        if (listValue == null) {
            listValue = new ArrayList<Object>();
        }
        return listValue.toArray();
    }
    
    public void setObjectArrayValue(java.lang.Object[] newListValue) {
        listValue = new ArrayList<Object>(Arrays.asList(newListValue));
    }
    
    public void setListValue(Collection<?> newListValue) {
        if (newListValue == null) {
            listValue = null;
        } else {
            listValue = new ArrayList<Object>(newListValue);
        }
    }
    
    public void addQueryParameters(Map<String, String> queryParameters) {
        if (textValue != null && textValue.length() > 0) {
            // skip empty string parameters
            textValue = textValue.trim();
            if (textValue.length() > 0) {
                queryParameters.put(name, textValue);
            }
        }
        else if (booleanValue != null) {
            String booleanStringValue = null;
            if (booleanValue.booleanValue()) {
                booleanStringValue = "1";
            } else {
                booleanStringValue = "0";
            }
            queryParameters.put(name, booleanStringValue);
        }
        else if (listValue != null && !listValue.isEmpty()) {
            int i = 1;
            Iterator<Object> values = listValue.iterator();
            while (values.hasNext()) {
                queryParameters.put(name + String.valueOf(i), values.next().toString());
                i++;
            }
        }
    }
}
