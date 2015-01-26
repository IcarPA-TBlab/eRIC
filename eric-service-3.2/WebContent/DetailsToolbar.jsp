<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>

    <script language="text/javascript">
<!--//
        function confirmDelete(){
            if (confirm("<%= WebUIResourceBundle.getInstance().getString("confirmRemove") %>")==true) {
                return true;
            } else {
                return false;
            }
        }
//-->
    </script>
    <noscript>
        <h2>
            <%=WebUIResourceBundle.getInstance().getString("noscript")%>        
        </h2>
    </noscript>     
<f:subview id="detailsToolbarSubview">
    <h:panelGrid id="detailsToolbarPanel" 
            columns="2" 
      columnClasses="leftAlign, rightAlign, leftAlign, rightAlign, leftAlign">
      <h:column id="detailsToolbarPanel1">
        <h:commandButton id="applyButton" 
                      value="#{bundle.applyButtonText}" 
                     action="#{roCollection.doApplyOnCurrentROB}"
                      title="#{bundle.applyTip}"
                   disabled="#{(roCollection.currentRegistryObjectBean.withdrawn)}"
                 styleClass="Btn1Mni"  
                />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>               
        <h:commandButton id="saveButton" 
                      value="#{bundle.saveButtonText}" 
                     action="#{roCollection.doSaveOnCurrentROB}"
                      title="#{bundle.saveTip}"
                   disabled="#{(roCollection.currentRegistryObjectBean.withdrawn)}"
                 styleClass="Btn1Mni"  
                />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>               
         <h:commandButton id="cancelDetailsButton" 
                       value="#{bundle.cancel}" 
                      action="#{roCollection.doCancelOnCurrentROB}"
                   disabled="#{(roCollection.currentRegistryObjectBean.withdrawn)}"
                 styleClass="Btn1Mni"
                />
      </h:column>
      <h:column id="detailsToolbarPanel2">
        <h:commandButton id="approveButton" 
                      value="#{bundle.approveButtonText}" 
                     action="#{roCollection.doApproveOnCurrentROB}"
                   disabled="#{(roCollection.currentRegistryObjectBean.approved)}"
                 styleClass="Btn1Mni"
                />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>               
        <h:commandButton id="deprecateButton" 
                      value="#{bundle.deprecateButtonText}" 
                     action="#{roCollection.doDeprecateOnCurrentROB}"
                   disabled="#{(roCollection.currentRegistryObjectBean.deprecated)}"
                 styleClass="Btn1Mni"
                />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>               
        <h:commandButton id="undeprecateButton" 
                      value="#{bundle.undeprecateButtonText}" 
                     action="#{roCollection.doUndeprecateOnCurrentROB}"
                   disabled="#{(roCollection.currentRegistryObjectBean.unDeprecated)}"
                 styleClass="Btn1Mni"
                />
            <f:verbatim>&nbsp;&nbsp;</f:verbatim>               
        <h:commandButton id="removeButton" 
                      value="#{bundle.deleteButtonText}" 
                     action="#{roCollection.doDeleteOnCurrentROB}"
                   disabled="#{roCollection.currentRegistryObjectBean.fields.new}"
                  styleClass="Btn1Mni"  
                    onclick="return confirmDelete();"
                />
      </h:column>
    </h:panelGrid>
    <h:panelGrid id="detailsToolbarStatusPanel" 
            columns="1" 
      columnClasses="leftAlign">
      <h:column id="detailsToolbarStatusPanelColumn">
        <h:commandButton id="statusTypeButton" 
                      value="#{bundle.statusTypeButtonText}" 
                     action="#{roCollection.doStatusOnCurrentROB}"
                     disabled="#{roCollection.currentRegistryObjectBean.fields.new}"
                  styleClass="Btn1Mni"
                />

        <h:selectOneMenu id="statusTypeMenu" 
                      value="#{roCollection.currentRegistryObjectBeanStatusTypeConcept}" 
                   disabled="#{roCollection.currentRegistryObjectBean.fields.new}"
                        >
            <f:selectItems value="#{roCollection.statusType_SelectItems}" id="statusTypeConcepts"/>
        </h:selectOneMenu>
      </h:column>
    </h:panelGrid>
</f:subview>
