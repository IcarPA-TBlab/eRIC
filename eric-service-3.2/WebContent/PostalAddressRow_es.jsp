<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalViewRow_es">        
        <h:panelGrid id="PostalPanelWrapperRow_es" columns="2" columnClasses="rightAlign160, list-row-default">
            <h:outputLabel id="PostalStreetInLabelRow_es" for="PostalStreetInRow_es" value="#{bundle.streetLabel}" />
            <h:inputText id="PostalStreetInRow_es" value="#{result.street}" disabled="false"/>
            <h:outputLabel id="PostalNumInLabelRow_es" for="PostalNumInRow_es" value="#{bundle.streetNumberLabel}" />         
            <h:inputText id="PostalNumInRow_es" value="#{result.streetNumber}" disabled="false"/>
            <h:outputLabel id="PostalCodeInLabelRow_es" for="PostalCodeInRow_es" value="#{bundle.postalCodeLabel}" />
            <h:inputText id="PostalCodeInRow_es" value="#{result.postalCode}" disabled="false"/>
            <h:message id="PostalCityInRow_es_message" for="PostalCityInRow_es" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
            <h:outputLabel id="PostalCityInLabelRow_es" for="PostalCityInRow_es" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
            <h:inputText id="PostalCityInRow_es" value="#{result.city}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
            <h:message id="PostalCountryyInRow_es_message" for="PostalCountryInLabelRow_es" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
            <h:outputLabel id="PostalCountryInLabelRow_es" for="PostalCountryInRow_es" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
            <h:inputText id="PostalCountryInRow_es" value="#{result.country}" maxlength="2" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>
