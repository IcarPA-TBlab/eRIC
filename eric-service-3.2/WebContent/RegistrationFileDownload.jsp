<%-- 
     NOTE: This is a replacement for a servlet to download the generated key.
     KeyStore needs an OutputStream to serialize itself so we need to call
     response.getOutputStream(), which will cause JSP to throw exception
     AFTER the file has been downloaded. The exception can be safelly ignored.
 --%>
<%@page contentType="application/x-pkcs12"%>
<%
  response.setHeader("Content-Disposition", "attachment; filename=\"generated-key.p12\"");
%>
<%@page import="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>

<jsp:useBean id="registrationInfo" class="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean" scope="session"/>

<c:if test="${registrationInfo.generatePrivateKey == 'true'}">
  <c:if test="${registrationInfo.keystore != null}">
<%
  RegistrationInfoBean regInfo = (RegistrationInfoBean)registrationInfo;
  regInfo.getKeystore().store(response.getOutputStream(), regInfo.getPassword().toCharArray());
  
  out.clear();
  out = pageContext.pushBody(); 
%>
  </c:if>
</c:if>
  
