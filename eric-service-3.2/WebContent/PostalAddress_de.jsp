<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalView_de">        
        <h:panelGrid id="PostalAddressWrapper_de" columns="1" headerClass="header" 
            columnClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressOut_de" value="#{bundle.postalAddress}" />

            <h:panelGrid id="PostalAddressPanel_de" columns="2" columnClasses="rightAlign160, list-row-default">
                    <h:outputLabel id="PostalStreetInLabel_de" for="PostalStreetIn_de" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_de" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:outputLabel id="PostalNumInLabel_de" for="PostalNumIn_de" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_de" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
                    <h:outputLabel id="PostalCodeInLabel_de" for="PostalCodeIn_de" value="#{bundle.postalCodeLabel}" />
                    <h:inputText id="PostalCodeIn_de" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
                    <h:message id="PostalCityIn_de_message" for="PostalCityIn_de" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabel_de" for="PostalCityIn_de" value="#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityIn_de" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:message id="PostalCountryIn_de_message" for="PostalCountryIn_de" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_de" for="PostalCountryIn_de" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_de" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
      </h:panelGrid>
    </f:subview>
