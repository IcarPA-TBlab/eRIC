<%-- 
     NOTE: This is a copy of FileUpload.jsp, but points to 
     MultipartRequestHandler2.jsp for independence of roCollection bean.
     Once that dependence is cleared, the pages can be unified again.
 --%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle,
    javax.faces.context.FacesContext,
    it.cnr.icar.eric.client.ui.thin.RegistryBrowser
"%>
<f:view locale="#{userPreferencesBean.uiLocale}">
<%
RegistryBrowser registryBrowser = 
        (RegistryBrowser)FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getSessionMap()
                                     .get("registryBrowser");
%>
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><%= registryBrowser.getCssFile()%>'>
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
<html>
<head> 
<title><%= WebUIResourceBundle.getInstance().getString("fileUpload") %></title>
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<script type="text/javascript">
<!--//
    function enableUploadButton(){
        check = false;
        filepath = "";
        for(i=0; i<document.forms[0].elements.length; i++){
            if(document.forms[0].elements[i].name =="uploadFileButton2"){
                document.forms[0].elements[i].disabled = false ;
            }
            if(document.forms[0].elements[i].name =="uploadfile"){
                filepath = document.forms[0].elements[i].value;
            }
        }

        for(i=0; i<document.forms[0].elements.length; i++){
            if(document.forms[0].elements[i].name =="filepath"){
                document.forms[0].elements[i].value = filepath ;
             }
        }
 
    }
    function checkFileForUpload(){
        for(i=0; i<document.forms[0].elements.length; i++){
            if(document.forms[0].elements[i].name =="uploadfile"){
                if(document.forms[0].elements[i].value == "") {
                    check = true;
                    <%= "alert(\"" + WebUIResourceBundle.getInstance().getString("selectFileToUpload") + "\");" %>
                }
            }
         }
         if (check == true){
            for(i=0; i<document.forms[0].elements.length; i++){
                if(document.forms[0].elements[i].name =="uploadFileButton2"){
                    document.forms[0].elements[i].disabled = false ;
                    return false;
                }
            }
         }
    }
    function clearFormHiddenParams(curFormName) {
      var curForm = document.forms[curFormName];
    }
//-->
</script>
<noscript>
    <h2>
        <%=WebUIResourceBundle.getInstance().getString("noscript")%>        
    </h2>
</noscript>     
</head>
    <body bgcolor="#FFFFFF" text="#000000">
            <h:outputText id="FileUpload2" value="#{bundle.fileUpload}" styleClass="h2"/>
            <f:verbatim></br></f:verbatim>
            <h:messages id="fileUp2Messages" globalOnly="true"/> 
                <form method="post" action=<%= request.getContextPath() + "/registry/thin/MultipartRequestHandler2.jsp" %> enctype='multipart/form-data'>
                <h:panelGrid id="FileUpload2MainPanel" columns="1" styleClass="tabPage">
                    <h:panelGrid id="classNodeFile2UploadPanel" columns="1" styleClass="h2">
                    <h:outputText id="Pleaseselectthefiletoupload2" value="#{bundle.pleaseselectthefiletoupload}" styleClass="h4"/>
                    <f:verbatim></br></f:verbatim>
                        <f:verbatim><input type="file" name="uploadfile" size="80" onchange="enableUploadButton()"></f:verbatim>
                </h:panelGrid>
                <f:verbatim><br></f:verbatim>
                <h:panelGrid id="classNodeFile2SelectButtonTable" columns="3" >
                <h:commandButton id="uploadFileButton2"
                                 action="uploaded"
                                 value="#{bundle.uploadFile}"
                                 disabled = "false"
                                 onclick="return checkFileForUpload()"
                                 styleClass="Btn2Mni" 
                                />
                    <f:verbatim>
                    <input type="button" id="classNodeFileSelectClose" value="<%=WebUIResourceBundle.getInstance().getString("cancel")%>" 
                                     onclick="window.close()" class="Btn2Mni"/>
                    &nbsp;&nbsp;</f:verbatim>
                    <input type=hidden name="filepath" value="">
               </h:panelGrid>          
             </h:panelGrid>        
         </form>            
    </body>
</html>
</f:view>
