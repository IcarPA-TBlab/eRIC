<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="classificationSubview">
  <h:panelGrid id="classWrapperPanel">
  
   <c:import url="RegistryObject.jsp" />
 
    <%-- 
      Classification Scheme 
    --%>
    <h:panelGrid id="classPanel" columns="2"
                 columnClasses="leftAlign160,list-row-default">
      <h:outputLabel id="classInLabel" styleClass="h3" for="classIn" 
                value="#{bundle.classificationSchemeLabel}#{bundle.colon}" />
      <h:inputText id="classIn" value="#{roCollection.currentRegistryObjectBean.fields.classificationScheme.name}" 
        size="42" disabled="true"/>
    
    <%-- 
      Concept 
    --%>
      <h:outputLabel id="conceptInLabel" styleClass="h3" for="conceptIn" 
                value="#{bundle.conceptLabel}#{bundle.colon}" />
      <h:inputText id="conceptIn" value="#{roCollection.currentRegistryObjectBean.fields.concept.key.id}" 
         size="70" disabled="true"/>
    
    <%-- 
      Value 
    --%>
      <h:outputLabel id="classValInLabel" styleClass="h3" for="classValIn" 
                value="#{bundle.classificationValueLabel}#{bundle.colon}" />
      <h:inputText id="classValIn" value="#{roCollection.currentRegistryObjectBean.fields.value}" 
        size="42" disabled="#{! roCollection.currentRegistryObjectBean.fields.classificationScheme.external}" />
    </h:panelGrid>
    
    <%--
      Select Scheme or Concept
    --%>
    <h:commandButton id="classSchemeButton" 
                  value="#{bundle.selectClassSchemeOrNode}" 
                 immediate="true" 
             styleClass="Btn2Mni"  
                onclick="window.open('ClassSchemeNodeSelector.jsp', 'ClassSchemeNodeSelector','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"/>
        

  </h:panelGrid>

</f:subview>
