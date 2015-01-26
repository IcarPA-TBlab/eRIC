<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalViewRow_ja">        
        <h:panelGrid id="PostalPanelWrapperRow_ja" columns="2" columnClasses="rightAlign160, list-row-default">
                <h:outputLabel id="PostalCodeInLabelRow_ja" for="PostalCodeInRow_ja" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeInRow_ja" value="#{result.postalCode}" disabled="false"/>
                <h:message id="PostalCountryInRow_ja_message" for="PostalCountryInRow_ja" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabelRow_ja" for="PostalCountryInRow_ja" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />
                <h:inputText id="PostalCountryInRow_ja" value="#{result.country}" maxlength="2" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalStateInRow_ja_message" for="PostalStateInRow_ja" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabelRow_ja" for="PostalStateInRow_ja" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.stateOrProvinceLabel}" />
                <h:inputText id="PostalStateInRow_ja" value="#{result.stateOrProvince}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalCityInRow_ja_message" for="PostalCityInRow_ja" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCityInLabelRow_ja" for="PostalCityInRow_ja" value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
                <h:inputText id="PostalCityInRow_ja" value="#{result.city}" disabled="false" required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:outputLabel id="PostalStreetInLabelRow_ja" for="PostalStreetInRow_ja" value="#{bundle.streetLabel}" />
                <h:inputText id="PostalStreetInRow_ja" value="#{result.street}" disabled="false"/>
                <h:outputLabel id="PostalNumInLabelRow_ja" for="PostalNumInRow_ja" value="#{bundle.streetNumberLabel}" />         
                <h:inputText id="PostalNumInRow_ja" value="#{result.streetNumber}" disabled="false"/>
        
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>
