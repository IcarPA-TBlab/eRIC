<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Example of how to create extended JSPs for extended object types (this example: CPP) --%>
<f:subview id="extendedDetailsSubview">
  
  <%-- Import the original ExtrinsicObject details --%>
  <c:import url="ExtrinsicObject.jsp" />
  
  <!-- CPP Details -->
  <f:verbatim><br></f:verbatim>
  <%-- Add here the properties from our special type --%>
  <h:panelGrid id="cppDetails" columns="1" headerClass="tableHeader"
             rowClasses="list-row-first-left,list-row-default,list-row-default"
             styleClass="list-background">
    <h:outputText id="cppRole" styleClass="tableHeader" value="Role" />

    <h:inputText value="#{roCollection.currentRegistryObjectBean.fields.Role}" 
       id="cppRoleIn" disabled="false"/>
  </h:panelGrid>

</f:subview>
