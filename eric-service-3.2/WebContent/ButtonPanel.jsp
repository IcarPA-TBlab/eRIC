<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:subview id="buttonPanel">

  
    <h:panelGrid width="100%" columns="2" columnClasses="rightAlign">
    
      <h:panelGrid>   
        <h:form id="logoutForm" target="_parent">
        <h:panelGrid columnClasses="centerColumn">
          <h:commandButton id="logoutButton" 
                           image="images/find.gif"
                           styleClass="Btn1Mni"  
                           action="#{registryBrowser.doLogout}"/>
          <h:outputLabel for="logoutButton">
            <h:outputText value="#{bundle.Logout}"/>
          </h:outputLabel>
        </h:panelGrid>     
        </h:form>
        
        <h:form id="logoutForm_new" target="_new">  
           <h:panelGrid columnClasses="centerColumn">
            
            <h:commandButton id="userGuide"
                             action="showUserGuide"
                             styleClass="Btn1Mni">
              <h:outputText value="#{bundle.userGuide}"/>
            </h:commandButton>
            
        </h:panelGrid>
        </h:form>
      </h:panelGrid>
      
    </h:panelGrid>
  

</f:subview>
