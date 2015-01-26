<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:subview id="TelephoneNumbersView">

    <h:panelGrid id="phoneNumsPanel" columns="1" headerClass="header" 
           rowClasses="h3,list-row-default,list-row-default,"
           styleClass="list-background">
        <h:outputText id="phoneNumsOut" value="#{bundle.telephoneNumbers}" />
        <h:dataTable id="phoneNumsTable" columnClasses=""
            rowClasses="list-row-default"
            styleClass="list-background"
            rows="20"
            value="#{roCollection.currentRegistryObjectBean.fields.telephoneNumbers}"
            var="result">
            <h:column id="phoneNumsCol" >
                <c:import url="/TelephoneNumberRow.jsp"/>
            </h:column>
        </h:dataTable>
    </h:panelGrid>
</f:subview>
