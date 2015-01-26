<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>

    <script language="javascript">
        function confirmRemove(){
            if (confirm("<%= WebUIResourceBundle.getInstance().getString("confirmRemove") %>")==true) {
                return true;
            } else {
                return false;
            }
        }
    </script>
     <noscript>
        <h2>
            <%=WebUIResourceBundle.getInstance().getString("noscript")%>        
        </h2>
    </noscript>           
    <h:panelGrid id="summaryPanelWrapper" columns="10">
            <f:facet name="header">
                <f:verbatim>
                </f:verbatim>
            </f:facet>
            <h:commandButton 
               id="applySummaryButton"
               value="#{bundle.applyButtonText}"
               action="#{roCollection.doApply}"
               styleClass="Btn1Mni"  
               />
            <h:commandButton 
               id="approveSummaryButton"
               value="#{bundle.approveButtonText}"
               action="#{roCollection.doApprove}"
               styleClass="Btn1Mni"  
               />
            <h:commandButton 
               id="deprecateSummaryButton"
               value="#{bundle.deprecateButtonText}"
               action="#{roCollection.doDeprecate}"
               styleClass="Btn1Mni"  
               />
            <h:commandButton 
               id="undeprecateSummaryButton"
               value="#{bundle.undeprecateButtonText}"
               action="#{roCollection.doUndeprecate}"
               styleClass="Btn1Mni"  
                />
	    <h:commandButton 
               id="bookmarkSummaryButton"
               value="#{bundle.bookmarkButtonText}"
               action="#{roCollection.doBookmark}"
               styleClass="Btn1Mni"  
               />	
            <h:commandButton 
               id="relateSummaryButton"
               value="#{bundle.relateButtonText}"
               action="#{roCollection.doRelate}"
               styleClass="Btn1Mni"  
               />            
            <h:commandButton 
               id="exportSummaryButton"
               value="#{bundle.export}"
               action="showExportPage"
               immediate="false"
               styleClass="Btn1Mni"
               />            
            <h:commandButton 
                id="removeSummaryButton"
                value="#{bundle.deleteButtonText}"
                action="#{roCollection.doDelete}"
                styleClass="Btn1Mni"  
                onclick="return confirmRemove()"
                />
            <h:outputLabel id="statusCodeMenuLabel" for="statusCodeMenu" 
                value="#{bundle.deletionScopeCodes}: " />
            <h:selectOneMenu 
                id="statusCodeMenu"
                value="#{registryBrowser.deletionScopeCode}">
                <f:selectItems id="deleteScopeCode" value="#{registryBrowser.deletionScopeCodes}"/>
            </h:selectOneMenu>
    </h:panelGrid>
    <h:panelGrid id="summaryPanelStatusWrapper" columns="1">
        <h:column id="summaryPanelStatusWrapperColumn">
            <h:commandButton id="statusTypeSummaryButton" 
                          value="#{bundle.statusTypeButtonText}" 
                         action="#{roCollection.doSetStatus}"
                      styleClass="Btn1Mni"
                    />
            <h:selectOneMenu id="statusTypeSummaryMenu" 
                          value="#{roCollection.statusTypeConcept}" 
                       disabled="false"
                            >
                <f:selectItems value="#{roCollection.statusType_SelectItems}" id="summaryStatusTypeConcepts"/>
            </h:selectOneMenu>
        </h:column>
    </h:panelGrid>
