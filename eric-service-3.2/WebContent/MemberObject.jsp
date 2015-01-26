<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/components" prefix="d" %>
<f:view locale="#{userPreferencesBean.uiLocale}">

    <html>
    <head> 
    <title>Add RegistryObject Panel</title> 
    <link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/ebxml.css" %>'>
    <META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
    <META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
    </head>
    <f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
    <body>
    <h:form id="memObjAddRoToRpForm">
    <h:panelGrid id="memObjTopPanel">   
        <h:outputText id="memObjMessageOut" value="#{bundle.addROToRPMessage}"/>
        <f:verbatim><br></f:verbatim>
        <h:messages id="memObjDetailsMessages" globalOnly="true" />
    </h:panelGrid>
    <h:panelGrid id="memObjRoIdPanelWrapper" columns="1" headerClass="tableHeader"
               rowClasses="list-row-first-left,list-row-default,list-row-first-left,list-row-default"
               styleClass="list-background">
        <h:outputText id="memObjIdHeaderOut" value="#{bundle.uniqueIdentifier}"/>
        <h:inputText value="#{roCollection.currentDrilldownRegistryObjectBean.memberObjectId}" 
            id="memObjIdIn" disabled="false" size="70"/>
    </h:panelGrid>

  
    <f:verbatim><br><br><br><br><br><hr></f:verbatim>
    <h:panelGrid id="memObjToolbarPanel" columns="2">
    <h:commandButton id="memObjSave" 
                     value="#{bundle.addButtonText}"
                     action="#{roCollection.doAddRoToRegistryPackage}" 
                     styleClass="Btn2Mni" 
                    />
    <h:commandButton id="memObjClose" 
                     value="#{bundle.cancel}" 
                     action="#{roCollection.doCancelAddRoToRegistryPackage}"
                     styleClass="Btn2Mni" 
                    />
    </h:panelGrid>
    </h:form>
    </body>
</f:view>

