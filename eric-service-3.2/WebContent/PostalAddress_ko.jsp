<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalView_ko">        
        <h:panelGrid id="phoneNumsPanel_ko" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressesOut_ko" value="#{bundle.postalAddress}" />
            <h:panelGrid id="PostalCountryPanel_ko" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:message id="PostalCountryIn_ko_message" for="PostalCountryIn_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_ko" for="PostalCountryIn_ko" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
                <h:message id="PostalStateIn_ko_message" for="PostalStateIn_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabel_ko" for="PostalStateIn_ko" value="#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.stateOrProvince}" disabled="false"/>
                <h:outputLabel id="PostalCodeInLabel_ko" for="PostalCodeIn_ko" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false" required="false"/>
                <h:message id="PostalCityIn_ko_message" for="PostalCityIn_ko" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCityInLabel_ko" for="PostalCityIn_ko" value="#{bundle.cityLabel}" />
                <h:inputText id="PostalCityIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:outputLabel id="PostalStreetInLabel_ko" for="PostalStreetIn_ko" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:outputLabel id="PostalNumInLabel_ko" for="PostalNumIn_ko" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_ko" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>
