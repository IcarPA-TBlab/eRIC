<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>
<%@ taglib prefix="d" uri="/components" %>

<f:view locale="#{userPreferencesBean.uiLocale}">
<html>
<head> 
<title>Extrinsic Object Classification Node Selector</title> 
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><h:outputText value="#{registryBrowser.cssFile}"/>'>
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
</head>
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
<body bgcolor='#ffffff'>
    <h:form id="extObjClassNodeSelectForm" >
    <h:panelGrid id="extObjClassNodeSelectTableWrapper">
        <f:verbatim><br></f:verbatim>
        <h:outputText id="extObjClassNodeSelectOut" value="#{bundle.selectConceptForObjectType}" 
            styleClass="h2"/>
        <f:verbatim><br></f:verbatim>
        <h:outputText id="openClassSchemeNotesOut_EO" value="#{bundle.openClassSchemeNotes}" />
        <h:outputText id="extObjSelectConceptNotesOut" value="#{bundle.selectConceptNotes}" />
	<h:outputText id="selectExtrinsicObjectNodeOut" value="#{bundle.selectExtrinsicObjectNode}" />
        <f:verbatim><br></f:verbatim>
        <f:verbatim><a href="#gotobuttons"><img src="images/a.gif" width="1" height="2" 
             border="0" alt="Skip to Buttons"></a>
        </f:verbatim>
        <h:panelGrid id="extObjClassNodeSelectorTable" styleClass="tabPage">
            <h:inputHidden id="expandTree" value="true"/>
            <d:searchRegistryGraphMenutree id="extObjClassNodeTreeSelector" 
                value="#{searchPanel.classSchemeSelector.treeGraph}"
                action="showSearchPanel"
                actionListener="#{searchPanel.classSchemeSelector.processCustomGraphEvent}"
                selectedValues=""
                styleClass="tree-control"
                selectedClass="tree-control-selected"
                unselectedClass="tree-control-unselected"
                treeSelect="CN"
                treeType="ExtrinsicObject"
                immediate="true"/>
        </h:panelGrid>
        <f:verbatim><a name="gotobuttons"></a></f:verbatim>
            <h:panelGrid id="extObjClassNodeSelectorButtonTable" columns="3">
            <h:commandButton id="extObjClassNodeSelectOk" 
                          value="#{bundle.ok}"
                      styleClass="Btn2Mni"  
                         action="#{roCollection.doSetClassNode}" />            
                <f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandButton id="classNodeEOSelectOkClose" 
                          value="#{bundle.cancel}" 
                     styleClass="Btn2Mni"  
                         action="#{roCollection.doCancelClassSchemeOrNode}"/>
        </h:panelGrid>
    </h:panelGrid>
    </h:form>
</body>
</html>
</f:view>
