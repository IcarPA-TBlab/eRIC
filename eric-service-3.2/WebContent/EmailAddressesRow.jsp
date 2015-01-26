<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<f:subview id="EmailAddressesRowSubview">
          <h:panelGrid id="emailAddressesRowPanel" columns="2" columnClasses="rightAlign160, list-row-default">
              <h:outputLabel id="emailAddressesRowTypeMenuLabel" for="emailTypeMenu" 
                      value="#{bundle.emailAddressTypeLabel}"/>
              <h:selectOneMenu id="emailAddressesRowTypeMenu"
                              value="#{result.type}">
                  <f:selectItems value="#{roCollection.emailTypes}" id="emailTypesRowItems"/>
              </h:selectOneMenu>
              <h:outputLabel id="emailAddressesRowInLabel" for="emailAddressesRowIn" 
                value="#{bundle.emailAddressLabel}" />
              <h:inputText id="emailAddressesRowIn" value="#{result.address}" size="80" 
                disabled="false"/>
          </h:panelGrid>
          <f:verbatim><br></f:verbatim>
</f:subview>