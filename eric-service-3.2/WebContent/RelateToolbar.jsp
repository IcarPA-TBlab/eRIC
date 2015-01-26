<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle" %>


<h:panelGrid id="relatePanelWrapper" columns="2" columnClasses="leftAlign,rightAlign">
    <h:commandButton 
    id="removeBookmarkButton"
    value="#{bundle.removeBookmarkButtonText}"
    action="#{roCollection.doRemoveBookmark}"
    styleClass="Btn1Mni"  
    />            
    <h:outputText id="bookmarkObjectsOut" value="#{bundle.bookmarkedObjects}" styleClass="h2"/>    
</h:panelGrid>
