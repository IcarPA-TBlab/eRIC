<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="d" uri="/components" %>

<f:subview id="exploreSubView">
  <h:panelGrid id="exploreSubPanel" cellpadding="8" cellspacing="0" width="100%" styleClass="tabPage">
    <h:outputLabel id="exploreSubLabel">
        <h:outputText id="exploreSubOut" value="#{bundle.registryContent}"/>
        <h:inputHidden id="expandTree" value="true"/>
            <d:exploreRegistryGraphMenutree id="exploreTree" 
                value="#{searchPanel.explorerGraphBean.treeGraph}"
                action="#{searchPanel.explorerGraphBean.doTreeDisplay}"
                actionListener="#{searchPanel.explorerGraphBean.processGraphEvent}"
                selectedValues="#{searchPanel.currentQuery.parameters.$classificationPath.listValue}"
                styleClass="tree-control"
                selectedClass="tree-control-selected"
                unselectedClass="tree-control-unselected"
                immediate="false"/>
    </h:outputLabel>
  </h:panelGrid>
</f:subview>
