<%@ page contentType="text/html; charset=UTF-8" language="java" %>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
    <jsp:useBean id="userPreferencesBean" 
            class="it.cnr.icar.eric.client.ui.thin.UserPreferencesBean" 
            scope="session" />
    <f:subview id="customMessageView">
        <h:panelGrid id="customMessagePanel" 
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5" width="100%">
            <f:verbatim><br></f:verbatim>
            <h:outputText id="customHeadOut" value="#{bundle.customizeHeader}" styleClass="h2"/>
            <h:outputText id="custDefaultLangOut" value="#{bundle.customizeDefaultLanguage}" />
            <h:outputText id="custChangeDefaultLangOut" value="#{bundle.changeDefaultLanguage}" />
            
            <f:verbatim><ol></f:verbatim>
            <h:panelGrid id="custTypeTable" columns="2">
                    <f:verbatim><li></f:verbatim>
                    <h:outputText id="custWebConsoleLabelsOut" value="#{bundle.webConsoleLabels}" />
                    <f:verbatim><li></f:verbatim>
                    <h:outputText id="custRegistryContentOut" value="#{bundle.registryContent}" />
            </h:panelGrid>
            <f:verbatim></ol></f:verbatim>
            
            <h:outputText id="custChangingDefaultOut" value="#{bundle.changingDefaultLanguageLabels}" 
                styleClass="h2"/>
            <h:outputText id="labelsMessagesCanAppearOut" value="#{bundle.labelsMessagesCanAppear}"/>
            
            <f:verbatim><ol></f:verbatim>
            <h:dataTable id="localeTable"
                     rows="#{userPreferencesBean.numSupportedUiLocales}"
                    value="#{userPreferencesBean.supportedUiLocalesDisplayNames}"
                      var="locale">
               <h:column>
                  <f:verbatim><li></f:verbatim>
                  <h:outputText id="localeDisplayOut" value="#{locale}" />
               </h:column>
              
            </h:dataTable>
            <f:verbatim></ol></f:verbatim>
            
            <f:subview id="customizeLangLabels">
                <h:outputText id="setLangDetail2Out" value="#{bundle.languagePreferenceDetails2}"/>
                <f:verbatim><ol><li></f:verbatim>
                <h:outputText id="setLangDetail3Out" value="#{bundle.languagePreferenceDetails3}"/>
                <f:verbatim><li></f:verbatim>
                <h:outputText id="setLangDetail4Out" value="#{bundle.languagePreferenceDetails4}"/>
                <f:verbatim></ol></f:verbatim>
            </f:subview>
            
            <h:outputText id="changingDefaultLangContentOut" value="#{bundle.changingDefaultLangContent}" 
                styleClass="h2"/>
            <h:outputText id="changingDefaultLangContent1Out" 
                value="#{bundle.changingDefaultLangContentDetails1}"/>
            <h:outputText id="changingDefaultLangContent2Out" 
                value="#{bundle.changingDefaultLangContentDetails2}"/>
        </h:panelGrid>
    </f:subview>
