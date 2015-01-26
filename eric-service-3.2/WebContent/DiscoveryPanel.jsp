<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/components" prefix="d" %>

<f:subview id="DiscoveryView">
    <h:panelGrid id="discWelcomePanel"  cellspacing="0" cellpadding="0" 
        width="100%" rowClasses="wideColumn" styleClass="DicoveryPanel">

        <h:panelGrid cellspacing="0"  cellpadding="0" 
            width="100%" rowClasses="wideColumn" id="homeHelpPanel">
            <d:pane_tabbed id="tabcontrol2"
                    paneClass="tabbed-pane"
                 contentClass="tabbed-content"
                selectedClass="tabbed-selected"
              unselectedClass="tabbed-unselected"
                  supportsROB="false">

                <d:pane_tab id="discTasksPane" >
                    <f:facet name="label">
                        <d:pane_tablabel id="tasksPaneLabel" label="#{bundle.Tasks}" />
                    </f:facet>
                    <c:import url="/Tasks.jsp" /> 
                </d:pane_tab>
                <d:pane_tab id="discsearchPane" firstTab="true">
                    <f:facet name="label">
                        <d:pane_tablabel id="searchPaneLabel" label="#{bundle.Search}" />
                    </f:facet>
                    <c:import url="/SearchPanel.jsp" /> 
                </d:pane_tab>
                <d:pane_tab id="discexplorePane" >
                    <f:facet name="label">
                        <d:pane_tablabel id="explorePaneLabel" label="#{bundle.Explore}" />
                    </f:facet>
                    <c:import url="/ExplorePanel.jsp" />          
                </d:pane_tab>
            </d:pane_tabbed>
        </h:panelGrid>

    </h:panelGrid>
</f:subview>
