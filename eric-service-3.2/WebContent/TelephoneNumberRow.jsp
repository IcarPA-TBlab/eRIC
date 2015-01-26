<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

            <f:subview id="TelephoneNumberRowView"> 
                <h:panelGrid id="phonePanelRowWrapper" columns="2" columnClasses="rightAlign160, list-row-default">
                    <h:outputLabel id="phoneTypeRowMenuLabel" for="phoneTypeRowMenu" 
                            value="#{bundle.phoneTypeLabel}"/>
                    <h:selectOneMenu id="phoneTypeRowMenu" 
                                      value="#{result.type}">
                        <f:selectItems value="#{roCollection.phoneTypes}"
                               id="phoneTypeItems"/>
                    </h:selectOneMenu>
                    <h:outputLabel id="phoneCountryInRowLabel" for="phoneCountryRowIn" value="#{bundle.countryCodeLabel}"/>
                    <h:inputText id="phoneCountryRowIn" value="#{result.countryCode}" maxlength="8" disabled="false"/>
                    <h:outputLabel id="phoneAreaInRowLabel" for="phoneAreaRowIn" value="#{bundle.areaCodeLabel}"/>
                    <h:inputText id="phoneAreaRowIn" value="#{result.areaCode}" maxlength="8" disabled="false"/>
                    <h:outputLabel id="phoneNumInRowLabel" for="phoneNumRowIn" value="#{bundle.phoneNumberLabel}"/>
                    <h:inputText id="phoneNumRowIn" value="#{result.number}" maxlength="16" disabled="false"/>
                    <h:outputLabel id="phoneExtInRowLabel" for="phoneExtRowIn" value="#{bundle.phoneExtensionLabel}"/>
                    <h:inputText id="phoneExtRowIn" value="#{result.extension}" maxlength="8" disabled="false"/>
                </h:panelGrid>
                <f:verbatim><br></f:verbatim>
            </f:subview>

