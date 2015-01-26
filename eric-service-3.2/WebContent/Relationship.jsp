<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="d" uri="/components" %>
<c:if test="${roCollection.relationshipBean.referencedPanelRendered == 'true'}">
<f:verbatim><hr><br></f:verbatim>
<h:form id="relationshipForm" target="_new">
 <h:panelGrid id="relationshipHelpPanel" columns="2" width="100%" columnClasses="leftAlign, rightAlign">
   
    <h:outputText id="relationshipOut" value="#{bundle.createRelationship}" styleClass="h2"/>
   
    <h:outputLink id="relationshipHelp"
        value="#{registryBrowser.relationshipHelp}" target="_new">
        <h:outputText id="relationshipHelpOut" value="#{bundle.relationshipHelp}"/>
    </h:outputLink>
 </h:panelGrid>
</h:form>
    <h:form id="RelationshipForm">
            <f:verbatim><br><br></f:verbatim>
            <h:panelGrid id="RelationshipPanel"  styleClass="tabPage">
               <h:outputText id="RelationshipTitle" value="#{bundle.targetObjectMessage}" styleClass="h2"/>
               <f:verbatim><br></f:verbatim>
                <h:column>    
                    <h:outputLabel id="referenceCodeMenuLabel" for="referenceCodeMenu" 
                        value="#{bundle.sourceLabel}:" styleClass="h2"/>
                    <f:verbatim>&nbsp;&nbsp;</f:verbatim>
                      <h:selectOneMenu id="referenceCodeMenu" 
                       value="#{roCollection.referenceSourceCode}" 
                       valueChangeListener="#{roCollection.changeSelectedSourceType}"
                       onchange="document.forms['searchResultsView:RelationshipForm'].submit(); return false;"
                        >
                        <f:selectItems value="#{roCollection.referenceScopeCodes}" id="refScopeCodesItems"/>
                        </h:selectOneMenu>
                    <h:outputText id="arrow" value="------------>"/>
                    <h:outputLabel id="selectedValueLabel" for="selectedValue" 
                        value="#{bundle.targetLabel}:" styleClass="h2"/>
                    <f:verbatim>&nbsp;&nbsp;</f:verbatim>
                    <h:inputText id="selectedValue" value="#{roCollection.referenceTargetCode}" disabled="false" 
                        readonly="true" size="40" style="border: 1px solid #000000;"/>
                </h:column>     
            </h:panelGrid>

    <f:verbatim><br><br></f:verbatim>
    <f:verbatim><hr><br></f:verbatim>
    <h:outputLabel id="relationshipRadioOut" for="relationshipRadio" 
        value="#{bundle.relationship}" styleClass="h2"/>
        <f:verbatim><br><br></f:verbatim>
        <h:panelGrid id="sourceTargetPanel" columns="3" styleClass="tabPage" >
            <h:selectOneRadio 
            title="radio button to select reference or association relationship type"
            id="relationshipRadio"
            border="0" 
            value="#{roCollection.relationshipType}" 
            valueChangeListener="#{roCollection.changeReferenceAssociationType}"
            onchange="document.forms['searchResultsView:RelationshipForm'].submit(); return false;"
            >
                <f:selectItem itemDisabled="#{!roCollection.relationshipBean.referencedValid}" itemValue="Reference" itemLabel="Reference" id="refSelectItem"/>
                <f:selectItem itemValue="Association" itemLabel="Association" id="assocSelectItem"/>
            </h:selectOneRadio>
        </h:panelGrid>
    
       <f:verbatim><br><br></f:verbatim>
       <c:choose>   
           <c:when test="${roCollection.relationshipType == 'Reference'}">
        <c:import url="/Reference.jsp"/>
    </c:when>
    </c:choose>
    </h:form>
    <c:choose>   
    <c:when test="${roCollection.relationshipType == 'Association'}">
        <c:import url="/Details.jsp"/>
    </c:when>
    </c:choose>
</c:if>


