<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="errorMessageView">

<c:choose>
<c:when test="${registryBrowser.sessionExpired}">
  <h:panelGrid id="errorSessionTimeoutPanel">
      <f:verbatim><br><br></f:verbatim>
      <h:outputText id="errorSessionTimeout" value="#{bundle.sessionTimeout}"/>
  </h:panelGrid>
</c:when>
<c:otherwise>
    <h:panelGrid cellpadding="3" cellspacing="0" width="100%" styleClass="tabPage"
           id="errorMessagePanel" rendered="#{! empty registryBrowser.errorMessage}">
           <h:outputText id="errorOut" value="#{bundle.error}" styleClass="h2" rendered="#{! empty registryBrowser.errorMessage}" />
           <h:outputText id="errorOccurOut" value="#{bundle.errorOccurred}" rendered="#{! empty registryBrowser.errorMessage}" />
           <h:outputText id="errorMessageOut" value="#{registryBrowser.errorMessage}" rendered="#{! empty registryBrowser.errorMessage}" />
           <h:messages id="errorMessages" globalOnly="true"/>
           <c:set var="helpLink" value="${registryBrowser.helpLink}"/>
           <c:if test="${helpLink != null}" >
                <h:outputLink id="errorHelpLink"
                    value="#{facesContext.externalContext.request.contextPath}/..#{registryBrowser.helpLink}" 
                        target="_new">
                    <h:outputText id="helpLinkOut" value="#{bundle.helpLink}"/>
                </h:outputLink>
           </c:if>
    </h:panelGrid>
</c:otherwise>
</c:choose>
    
</f:subview>

