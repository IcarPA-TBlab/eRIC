<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <f:subview id="PostalViewRow_en">        
        <h:panelGrid id="PostalPanelWrapperRow_en" columns="2" columnClasses="rightAlign160, list-row-default">

                <h:outputLabel id="PostalNumInLabelRow_en" for="PostalNumInRow_en" value="#{bundle.streetNumberLabel}" />         
                <h:inputText id="PostalNumInRow_en" value="#{result.streetNumber}" disabled="false"/>
                <h:outputLabel id="PostalStreetInLabelRow_en" for="PostalStreetInRow_en" value="#{bundle.streetLabel}" />
                <h:inputText id="PostalStreetInRow_en" value="#{result.street}" disabled="false"/>
                <h:message id="PostalCityInRow_en_message" for="PostalCityInRow_en" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCityInLabelRow_en" for="PostalCityInRow_en" 
                               value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.cityLabel}" />
                <h:inputText id="PostalCityInRow_en" value="#{result.city}" disabled="false" 
                             required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
                <h:message id="PostalStateInRow_en_message" for="PostalStateInRow_en" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalStateInLabelRow_en" for="PostalStateInRow_en" 
                               value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.stateOrProvinceLabel}" />                
                <h:inputText id="PostalStateInRow_en" value="#{result.stateOrProvince}" disabled="false" 
                             required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>           
                <h:outputLabel id="PostalCodeInLabelRow_en" for="PostalCodeInRow_en" value="#{bundle.postalCodeLabel}" />
                <h:inputText id="PostalCodeInRow_en" value="#{result.postalCode}" disabled="false"/>
                <h:message id="PostalCountryInRow_en_message" for="PostalCountryInRow_en" style="color: red"/>
                <f:verbatim>
                    <td></td>
                </f:verbatim>
                <h:outputLabel id="PostalCountryInLabelRow_en" for="PostalCountryInRow_en" 
                               value="#{roCollection.currentRegistryObjectBean.requiredFieldFlag}#{bundle.countryCodeLabel}" />   
                <h:inputText id="PostalCountryInRow_en" value="#{result.country}" maxlength="2" disabled="false" 
                             required="#{roCollection.currentRegistryObjectBean.fieldRequired}"/>
        </h:panelGrid>
        <f:verbatim><br></f:verbatim>
    </f:subview>
