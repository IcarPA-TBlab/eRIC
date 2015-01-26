<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="classNodeSubview">
  <h:panelGrid id="classNodeWrapperPanel">
  
    <c:import url="RegistryObject.jsp" />
   
    <!-- 
      Classification Scheme 
    -->
    <h:panelGrid columns="1"
                 rowClasses="h3,list-row-default"
                 styleClass="list-background"
                 id="classNodeSchemeNameInPanel">
      <h:outputLabel id="schemeNameInLabel" for="schemeNameIn" 
                value="#{bundle.classificationSchemeLabel}#{bundle.colon}" />
      <h:inputText id="schemeNameIn"
                   value="#{roCollection.currentRegistryObjectBean.fields.classificationScheme.name}" 
                   size="70"
                   disabled="true" />
    
    <%-- 
      Parent id
    --%>
      <h:outputLabel id="parentIdInLabel" for="parentIdIn" 
                value="#{bundle.parentIdLabel}#{bundle.colon}" />
      <h:inputTextarea id="parentIdIn"
                   value="#{roCollection.currentRegistryObjectBean.fields.parent.id}" 
                   cols="70"
                   rows="2"
                   disabled="true"/>
    
    <%-- 
      Path 
    --%>
      <h:outputLabel id="pathInLabel" for="pathIn" 
                value="#{bundle.pathLabel}#{bundle.colon}" />
      <h:inputTextarea id="pathIn"
                   value="#{roCollection.currentRegistryObjectBean.fields.path}" 
                   cols="70"
                   rows="2"
                   disabled="true"/>
    
    <%-- 
      Value 
    --%>
      <h:outputLabel id="conceptValueInLabel" for="conceptValueIn" 
                value="#{bundle.conceptValueLabel}#{bundle.colon}" />
      <h:inputText id="conceptValueIn"
                   value="#{roCollection.currentRegistryObjectBean.fields.value}" 
                   size="70"
                   disabled="false"/>
    </h:panelGrid>
    
  </h:panelGrid>

</f:subview>

