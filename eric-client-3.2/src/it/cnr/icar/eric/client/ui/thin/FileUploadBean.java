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

import java.io.File;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;


/**
 *
 * @author Anand
 */

public class FileUploadBean implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1939804519267152073L;

	private final static long FILECHECKLENGTHBYTES = 30 * 1024; 
    
    private static Log log = LogFactory.getLog(FileUploadBean.class);

    private String fileName = null;
    private long fileSize = 0;
    private File file = null;
    private String contentType =  null;
    @SuppressWarnings("unused")
	private boolean isFileLengthMore = false;
    private String absolutePath = null;
    
    /** Creates a new instance of FileUploadBean */
    public FileUploadBean() {
    }
    
    public String getFileName(){
        return fileName;
    }
    public String getContentType(){
        return contentType;
    }
    
    public long getFileSize(){
        return fileSize;
    } 
    
    public File getFile(){
        return file;
    }
    
    public String getAbsolutePath(){
        return absolutePath;
    }
    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setFile(File file) {
        this.file =  file;
    }
    
    public void setAbsolutePath(String absolutePath){
        this.absolutePath = absolutePath;
    }
    
    @SuppressWarnings("static-access")
	public boolean isFileLengthMore(){

        if(file != null && fileSize > 0){
            if (fileSize > this.FILECHECKLENGTHBYTES){
                return true;
            }  
        }
        return false;
    }
   
    @SuppressWarnings("unchecked")
	public static FileUploadBean getInstance() {
        FileUploadBean fileUploadBean = 
                (FileUploadBean)FacesContext.getCurrentInstance()
                                                          .getExternalContext()
                                                          .getSessionMap()
                                                          .get("fileUploadBean");
        if (fileUploadBean == null) {
            fileUploadBean = new FileUploadBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("fileUploadBean", fileUploadBean);
        }
        return fileUploadBean;
    }
    
    /** 
     * Clear (reset) all the properties in this bean.
     */
    public void doClear() {
        if (log.isDebugEnabled()) {
            log.debug("doClear started");
        }
        doDeleteFile();
        fileName = null;
        fileSize = 0;
        file = null;
        contentType =  null;
        isFileLengthMore = false;
        absolutePath = null;
    }

    /** 
     * Deletes the file pointed by 'file' property.
     */
    public void doDeleteFile() {
        if (log.isDebugEnabled()) {
            log.debug("doDeleteFile started");
        }
        if (file != null) {
            file.delete();
        }
    }

}