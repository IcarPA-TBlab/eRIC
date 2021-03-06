<%@ page contentType="text/html" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:view>
<html>
<head> 
<title>Registry Browser Login</title> 
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/ebxml.css" %>'>
<script language='javascript'  src='<%= request.getContextPath() + "/browser.js" %>'></script>
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
</head>
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.bundles.LocalizedText" var="bundle"/>
<body bgcolor='#ffffff'>
    <h:panelGrid bgcolor="#ffffff" cellspacing="0" cellpadding="0" width="100%">
        <h:panelGrid bgcolor="#ffffff" width="100%" columns="2" cellspacing="0" 
            cellpadding="0" columnClasses="leftColumn, rightColumn">           
            <c:import url="/Blank.jsp"/>
            <c:import url="/Banner.jsp"/>
        </h:panelGrid>
    </h:panelGrid>
        <f:verbatim><br><br><br><br><br><br><br></f:verbatim>
            <f:verbatim><br><br><DIV ALIGN="CENTER">
            <FORM ACTION="j_security_check" METHOD=POST NAME="LoginForm">
              <TABLE class="tabPage">
                <TR>
                    <TD colspan="2">
                        </f:verbatim>
                        <h:outputText value="#{bundle.pleaseLogin}"/>
                        <f:verbatim>
                    </TD>
                </TR>
                <TR>
                    <TD colspan="2">
                        <br>
                    </TD>
                </TR>
                <TR>
                    <TD>
                    <INPUT type=HIDDEN id='guestUserName' name='j_username' value='${guestPrincipalName}'>
                    <TD>
                    <INPUT type=HIDDEN id='guestPassword' name='j_password' value='${guestPrincipalName}'>
                    </TD>
                </TR>
                <TR>
                    <TD>
                    <INPUT type=RADIO name='userType' id='guestRB' 
                        value='Guest' checked onclick='enableFields()'>
                        </f:verbatim>
                        <h:outputText value="#{bundle.guest}"/>
                        <f:verbatim>
                    </TD>
                    <TD>
                    <INPUT type=RADIO name='userType' id='registeredUserRB' 
                        value='RegisteredUser' 
                        onclick='enableFields()'>
                        </f:verbatim>
                        <h:outputText value="#{bundle.registeredUser}"/>
                        <f:verbatim>
                    </TD>
                </TR>
                <TR>
                    <TD align="right">
                       </f:verbatim>
                       <h:outputText value="#{bundle.userName}"/>
                       <f:verbatim>
                    </TD>
                    <TD>
                        <INPUT disabled type=TEXT id='registeredUserName' 
                            NAME="j_username" VALUE="">
                    </TD>
                </TR>
                <TR>
                    <TD align="right">
                        </f:verbatim>
                        <h:outputText value="#{bundle.password}"/>
                        <f:verbatim>
                    </TD>
                    <TD>
                        <INPUT disabled type=PASSWORD id='registeredUserPassword' 
                            NAME="j_password" VALUE="">                      
                    </TD>
                </TR>
                <TR>
                    <TD colspan="2">
                        <br>
                    </TD>
                </TR>
                <TR>
                    <TD colspan="2" align="center">
                        <INPUT type=SUBMIT value="</f:verbatim><h:outputText value="#{bundle.login}"/><f:verbatim>">   
                        <INPUT type=RESET value="</f:verbatim><h:outputText value="#{bundle.clearButtonText}"/><f:verbatim>" 
                            onclick="resetFields()">
                    </TD>
                </TR>
              </TABLE>
            </FORM>
            </DIV>
            </f:verbatim>
        <f:verbatim><br><br><br><br><br><br><br><br></f:verbatim>
        <f:facet name="footer">
            <c:import url="/Footer.jsp"/>
        </f:facet>
    
</body>
</html>
</f:view> 
    
