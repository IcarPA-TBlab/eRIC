<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<Country>
<PostalCode>
<State or Province>
<City><Street><Street Number>
<last name> <first name>

    <f:subview id="PostalView_ja">        
        <h:panelGrid id="PostalAddressWrapper_ja" columns="1" headerClass="header" 
            rowClasses="h3,list-row-default"
            styleClass="list-background">
            <h:outputText id="postalAddressOut_ja" value="#{bundle.postalAddress}" />

            <h:panelGrid id="PostalCountryPanel_ja" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:outputLabel id="PostalCodeInLabel_ja" for="PostalCodeIn_ja" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.postalCode}" disabled="false"/>
                <h:message id="PostalCountryIn_ja_message" for="PostalCountryIn_ja" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabel_ja" for="PostalCountryIn_ja" value="#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.country}" disabled="false" required="false"/>
                <h:message id="PostalStateIn_ja_message" for="PostalStateIn_ja" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabel_ja" for="PostalStateIn_ja" value="#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.stateOrProvince}" disabled="false" required="false"/>
                    <h:message id="PostalCityIn_ja_message" for="PostalCityIn_ja" style="color: red"/>
                    <f:verbatim>
                        <td></td>
                    </f:verbatim>
                    <h:outputLabel id="PostalCityInLabel_ja" for="PostalCityIn_ja" value="#{bundle.cityLabel}" />
                    <h:inputText id="PostalCityIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.city}" disabled="false" required="false"/>
                    <h:outputLabel id="PostalStreetInLabel_ja" for="PostalStreetIn_ja" value="#{bundle.streetLabel}" />
                    <h:inputText id="PostalStreetIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.street}" disabled="false"/>
                    <h:outputLabel id="PostalNumInLabel_ja" for="PostalNumIn_ja" value="#{bundle.streetNumberLabel}" />
                    <h:inputText id="PostalNumIn_ja" value="#{roCollection.currentRegistryObjectBean.fields.streetNumber}" disabled="false"/>
            </h:panelGrid>
            <f:verbatim><br></f:verbatim>
        </h:panelGrid>
    </f:subview>
