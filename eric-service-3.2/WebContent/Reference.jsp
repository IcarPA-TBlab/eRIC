<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="d" uri="/components" %>


 <f:verbatim><hr><br></f:verbatim>
 <h:panelGrid id="subReferencePanel"  styleClass="tabPage">
                <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
                <h:outputLabel id="CodeMenuLabel" for="CodeMenu" 
                      value="#{bundle.referenceAttribute}: " styleClass="h2" />
                <f:verbatim>&nbsp;&nbsp;</f:verbatim>
                <h:selectOneMenu 
                id="CodeMenu"
                value="#{roCollection.referenceObjectTypeCode}">
                    <f:selectItems value="#{roCollection.referenceAttributeCodes}"/>
                </h:selectOneMenu>
      
      <f:verbatim>&nbsp;&nbsp;</f:verbatim>
       <h:commandButton id="refrenceButton"
                        value="#{bundle.applyButtonText}"
                        action="#{roCollection.doApplyReference}"
                        styleClass="Btn2Mni" 
                        />
    </h:panelGrid>
