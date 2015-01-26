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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.BulkResponse;

import it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl;
import it.cnr.icar.eric.client.ui.common.ReferenceAssociation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Anand
 */

public class RelationshipBean implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = -6138567648376112011L;
	private static final Log log = LogFactory.getLog(RelationshipBean.class);
    private boolean isReferencedPanelRendered = false;
    private boolean isAssociationPanelRendered = false;
    private RegistryObject sourceRegistryObject = null;
    private RegistryObject targetRegistryObject = null;
    private RegistryObject ro1 = null;
    private RegistryObject ro2 = null;
    private ReferenceAssociation refAss = null;
    private String sourceType = null;
    private String objectType = null;
    private boolean isReferencedValid = false;	
    public String sourceRegistryObjectName = null;
    public String targetRegistryObjectName = null;
    private String refAttribute = null;
   
   
    public boolean isReferencedPanelRendered(){
        return isReferencedPanelRendered; 
    } 
    public void setReferencedPanelRendered(boolean isReferencedPanelRendered) {
        this.isReferencedPanelRendered = isReferencedPanelRendered;
    }

    public boolean isAssociationPanelRendered(){
        return isAssociationPanelRendered; 
    } 
    public void setAssociationPanelRendered(boolean isAssociationPanelRendered) {
        this.isAssociationPanelRendered = isAssociationPanelRendered;
    }

    /**
    * Getter method for Source RegistryObject which is used for Relationship 
    * operation between two RegistryObject  
    * @return RegistryObject
    */ 
    public RegistryObject getSourceRegistryObject(){
        return sourceRegistryObject;
    } 

   /**
    * Setter method for Source RegistryObject which is used for Relation 
    * @param RelationObject
    */ 
    public void setSourceRegistryObject(RegistryObject sourceRegistryObject){
        this.sourceRegistryObject = sourceRegistryObject;
    } 

   /**
    * Getter method for target RegistryObject which is used for Relationship 
    * operation between two RegistryObject  
    * @return RegistryObject
    */ 
    public RegistryObject getTargetRegistryObject(){
        return targetRegistryObject;
    } 

   /**
    * Setter method for target RegistryObject which is used for Relationship
    * between two RegistryObject 
    * @param RelationObject
    */ 
    public void setTargetRegistryObject(RegistryObject targetRegistryObject){
        this.targetRegistryObject = targetRegistryObject;
    } 

   /**
    * This method return boolean if Relation between two RegistryObjects 
    * are valid for Reference. 
    * @return RelationObject
    */ 
    public boolean isReferencedValid(){
       return isReferencedValid;
    } 

   /**
    * This method set boolean for Relationship operation between 
    * two RegistryObjects are valid for Reference. 
    * @return RelationObject
    */ 
    public void setIsReferencedValid(boolean isReferencedValid){
       this.isReferencedValid = isReferencedValid;
    } 

    /**
     * Create new instance of ReferenceAssociation Object
     * @return ReferenceAssociation
     */
    private void createReferenceAssociation(){
             refAss = new ReferenceAssociation(sourceRegistryObject,targetRegistryObject);
        }

    /**
     * get instance of ReferenceAssociation Object
     * @return ReferenceAssociation
     */
     private ReferenceAssociation getReferenceAssociation(){
	 return refAss;
     }
     
    public void setFirstRegistryObject(RegistryObject ro1){
        this.ro1 = ro1;
    } 

    public RegistryObject getFirstRegistryObject(){
        return ro1;
    } 
    
    public void setSecondRegistryObject(RegistryObject ro2){
        this.ro2 = ro2;
    } 

    public RegistryObject getSecondRegistryObject(){
        return ro2;
    } 

    
    public String getSourceRegistryObjectName(){
        return sourceRegistryObjectName;
    }
    
    public void setSourceRegistryObjectName(RegistryObject ro){
        try{
        sourceRegistryObjectName = ro.getObjectType().getValue();
            }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileSettingSourceRegistryObjectNameOperation"),ex);
        }   
    }
    
    public String getTargetRegistryObjectName(){
        return targetRegistryObjectName;
    }
    
    public void setTargetRegistryObjectName(RegistryObject ro){
        try{
            targetRegistryObjectName = ro.getObjectType().getValue();
        }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileSettingTargetRegistryObjectNameOperation"),ex);
        }   
    }

    

   /**
    * This method return boolean if Relation between two RegistryObjects 
    * are valid for Reference. 
    * @return RelationObject
    */ 
    public boolean checkReferenced(String sourceType,String objectType){
        boolean referenceStatus = false;
        String relationStatus = null;
        this.sourceType = sourceType;
        this.objectType = objectType;
        this.setSourceTargetObject(sourceType);
        
	if(refAss == null){
            this.createReferenceAssociation();		
	}
        relationStatus = this.getReferenceAssociation().getReferenceStatus();

        if (relationStatus.equals("Reference")) {
            referenceStatus=true;
            this.isReferencedValid = true;
        }
        refAss = null;
        return referenceStatus;
    }
    @SuppressWarnings("unused")
	private void switchROs(){
        RegistryObject switchRO = null;
        switchRO =  this.sourceRegistryObject;
        this.sourceRegistryObject = this.targetRegistryObject;
        this.targetRegistryObject = switchRO;
    }

   /**
    * This method return String if Relation between two RegistryObjects 
    * happend sucessfully
    * @return String
    */ 
    public String doApplyReference(){
        String status = "failure";
        ArrayList<RegistryObject> roList = new ArrayList<RegistryObject>();
        BulkResponse br = null;
        try{    
            @SuppressWarnings("static-access")
			BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getInstance().getBLCM();
            this.setSourceTargetObject(sourceType);
	    if(refAss == null){
               this.createReferenceAssociation();		
	    }
            if (refAttribute == null) {
                refAss.setReferenceAttribute(this.objectType);
            } else {
                refAss.setReferenceAttribute(this.refAttribute);
            }
            refAss.setReferenceAttributeOnSourceObject();
            roList.add(sourceRegistryObject);
            roList.add(targetRegistryObject);
            br  = blcm.saveObjects(roList);
            if (br.getStatus() == 0){
                status = "relationSuccessful";
            }
        }catch(Exception ex){
            log.error(WebUIResourceBundle.getInstance().getString("message.errorOccuredWhileDoingDoSaveRefrenceOperation"),ex);
        }   
        return status;
    }
    
    public void setSourceTargetObject(String sourceType){
        if(sourceType.equals("source")){
            sourceRegistryObject = this.getFirstRegistryObject();
            targetRegistryObject = this.getSecondRegistryObject();
            this.setSourceRegistryObjectName(sourceRegistryObject);
            this.setTargetRegistryObjectName(targetRegistryObject);
        }
        else {
            sourceRegistryObject = this.getSecondRegistryObject();
            targetRegistryObject = this.getFirstRegistryObject();
            this.setSourceRegistryObjectName(sourceRegistryObject);
            this.setTargetRegistryObjectName(targetRegistryObject);
        }
    }
    
    public List<String> getRefAttributes(String sourceType,String targetType) {
        String refAttributes[] = null;
        String relationStatus = null;
        List<String> refList = null;

        this.sourceType = sourceType;
        this.objectType = targetType;
        this.setSourceTargetObject(sourceType);
        
	if(refAss == null){
            this.createReferenceAssociation();		
	}
        relationStatus = this.getReferenceAssociation().getReferenceStatus();

        if (relationStatus.equals("Reference")) {
            this.isReferencedValid = true;
            refAttributes = refAss.getReferenceAttributes();
        }
        refAss = null;
        if (refAttributes != null) {
            refList = java.util.Arrays.asList(refAttributes);
        }
        
        return refList;
    }

    public void setRefAttribute(String refAttribute) {
        this.refAttribute = refAttribute;
    }

 }
