<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<h:panelGrid columns="2" columnClasses="rightAlign,rightAlign" cellpadding="2" cellspacing="2">
    <h:commandButton id="composedAddButton"
                 value="#{bundle.addButtonText}" 
             immediate="true"
             onclick="window.open('NewComposedObject.jsp', 'NewComposedObject','toolbar=no, location=no, directories=no, statusbar=no, menubar=no, scrollbars=yes,resizable=yes, width=700, height=800')"
             styleClass="Btn2Mni" 
            />
    <h:commandButton id="composedDeleteButton"
             value="#{bundle.deleteButtonText}" 
            action="#{roCollection.doDeleteOnCurrentComposedROB}" 
        styleClass="Btn2Mni" 
       />
</h:panelGrid>
