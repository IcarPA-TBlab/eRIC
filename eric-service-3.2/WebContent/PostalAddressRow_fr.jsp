<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


    <f:subview id="PostalViewRow_fr">        
        <h:panelGrid id="PostalPanelWrapperRow_fr" columns="2" columnClasses="rightAlign160, list-row-default">

                    <h:outputLabel id="PostalNumInLabelRow_fr" for="PostalNumInRow_fr" value="#{bundle.streetNumberLabel}" />         
                    <h:inputText id="PostalNumInRow_fr" value="#{result.streetNumber}" disabled="false"/>
                    <h:outputLabel id="PostalStreetInLabelRow_fr" for="PostalStreetInRow_fr" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetInRow_fr" value="#{result.street}" disabled="false"/>
                    <h:outputLabel id="PostalCodeInLabelRow_fr" for="PostalCodeInRow_fr" value="#{bundle.postalCodeLabel}" />
                    <h:inputText id="PostalCodeInRow_fr" value="#{result.postalCode}" disabled="false"/>
                    <h:message id="PostalCityInRow_fr_message1" for="PostalCityInRow_fr" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabelRow_fr" for="PostalCityInRow_fr" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityInRow_fr" value="#{result.city}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                    <h:message id="PostalCityInRow_fr_message2" for="PostalCountryInRow_fr" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                <h:outputLabel id="PostalCountryInLabelRow_fr" for="PostalCountryInRow_fr" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryInRow_fr" value="#{result.country}" maxlength="2" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>
