<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%--
  SelfRegistration.jsp
  
  Display the Self Registration page.
  
  TO DO: 
  Include PersonName, EmailAddress, PostalAddress, etc pages. This will 
  require some sort of generalizing of the backing bean so that User.jsp 
  can us them also. Also, on this page, disabled="false", but for User.jsp,
  we need the components to have disabled="true". 
--%>
<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

    <f:subview id="selfRegistrationView">

      <h:form id="selfRegistrationForm"> 
        <h:panelGrid>
                       
          <c:import url="/NewUser.jsp" />
    
          <%-- 
            Buttons 
          --%>
          <h:panelGrid columns="2">
            <h:commandButton id="registerButton" 
                             value="#{bundle.registerButtonText}" 
                             action="#{registrationInfo.doRegister}" 
                             styleClass="Btn2Mni" 
                            />
            <h:commandButton id="clearButton" 
                             value="#{bundle.clearButtonText}" 
                             action="#{registrationInfo.doClear}" 
                             styleClass="Btn2Mni" 
                            />
          </h:panelGrid>
          
        </h:panelGrid>
      </h:form>
      
    </f:subview>

