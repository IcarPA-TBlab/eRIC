    <%@ page contentType="text/html; charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
    <f:subview id="regWizardView">
      <%-- Extension point: give a custom extension of RegistrationInfoBean. --%>
      <jsp:useBean id="registrationInfo" class="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean" scope="session" />
      <h:panelGrid id="regWizardPanel" bgcolor="#ffffff" cellspacing="0" cellpadding="0">
        <h:form id="regWizSelfRegistrationForm">
          <c:if test="${registrationInfo.currentStep == 1}">
              <c:import url="/RegistrationWizard_1.jsp"/>
          </c:if>
          <c:if test="${registrationInfo.currentStep == 2}">
              <c:import url="/RegistrationWizard_2.jsp"/>
          </c:if>
          <c:if test="${registrationInfo.currentStep == 3}">
              <c:import url="/RegistrationWizard_3.jsp"/>
          </c:if>
          <c:if test="${registrationInfo.currentStep == 4}">
              <c:import url="/RegistrationWizard_4.jsp"/>
          </c:if>
          <c:if test="${registrationInfo.currentStep < 1 or registrationInfo.currentStep > 4}">
              <c:import url="/RegisterMessage.jsp"/>
          </c:if>
        </h:form>
      </h:panelGrid>
    </f:subview>

