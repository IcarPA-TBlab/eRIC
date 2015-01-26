    <%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>    
    <jsp:useBean id="registrationInfo" class="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean" scope="session" />

    <f:subview id="regWizard4">
        <h:panelGrid id="regWizard4Panel" 
            bgcolor="#FFFFFF" cellspacing="0" cellpadding="5">
            <f:verbatim><br></f:verbatim>
            <h:outputText id="regHead4Out" value="#{bundle.registrationWizardHeader}" styleClass="h2"/>
            <h:outputText id="regMsg4Out" value="#{bundle.registrationWizardInfo_4}" styleClass="li"/> 
        </h:panelGrid>

        <h:panelGrid id="sucessMessage"> 
          <h:outputText id="sucessMessageOut" value="#{bundle.registrationSucceeded}" styleClass="h2"/>

          <%-- Success with generated key: download key option--%>
          <c:if test="${registrationInfo.generatePrivateKey == 'true'}">
            <h:panelGrid id="downloadKeyPanel" columns="1">
              <h:outputText id="downloadKeyInfo" value="#{bundle.downloadKeyInfo}"/>
                <h:outputLink id="keyFileDownload"
                 value="#{facesContext.externalContext.request.contextPath}/registry/thin/RegistrationFileDownload.jsp">
                  <h:outputText id="downloadButtonLabel" value="#{bundle.downloadButtonText}"/>
                </h:outputLink>                               
            </h:panelGrid>
          </c:if>
        </h:panelGrid>
                
        <%-- Help for different web browsers --%>
        <h:panelGrid id="regMessage" columns="1">
          <h:outputText id="registrationConcludedBrowserImportKeyHelpTextOut" value="#{bundle.registrationConcludedBrowserImportKeyHelpText}" styleClass="li"/> 
          <h:outputLink id="regHelpFirefox4Link" value="#{registryBrowser.userRegistrationHelp}" target="_new">
            <h:outputText id="regHelpFirefox4Out" value="Firefox"/>
          </h:outputLink>
          <h:outputLink id="regHelpIE4Link" value="#{registryBrowser.userRegistrationHelp}" target="_new">
            <h:outputText id="regHelpIE4Out" value="Microsoft Internet Explorer"/>
          </h:outputLink>
          <h:outputLink id="regHelpMozilla4Link" value="#{registryBrowser.userRegistrationHelp}" target="_new">
            <h:outputText id="regHelpMozilla4Out" value="Mozilla"/>
          </h:outputLink>
          <f:verbatim><br></f:verbatim>
          <h:outputText id="clickLoginOut" value="#{bundle.clickLogin}" />
        </h:panelGrid>
        
        <%-- 
          Buttons
        --%>
        <h:panelGrid id="regWizard4Buttons" columns="2" columnClasses="leftAlign, rightAlign">
          <h:panelGrid id="finishPanel" columns="1" columnClasses="rightAlign">
            <h:commandButton id="finishButton" 
                          value="#{bundle.finishButtonText}" 
                     styleClass="Btn2Mni"  
                         action="#{registrationInfo.doClear}" />          
          </h:panelGrid>
        </h:panelGrid>
        
    </f:subview>
