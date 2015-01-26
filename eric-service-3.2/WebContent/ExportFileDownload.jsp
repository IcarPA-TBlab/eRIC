<%-- 
     NOTE: This is a replacement for a servlet to download the generated key.
     KeyStore needs an OutputStream to serialize itself so we need to call
     response.getOutputStream(), which will cause JSP to throw exception
     AFTER the file has been downloaded. The exception can be safelly ignored.
 --%>
<%@page contentType="application/zip"%>
<%
  response.setHeader("Content-Disposition", "attachment; filename=\"exportedContent.zip\"");
%>
<%@page import="it.cnr.icar.eric.client.ui.thin.RegistrationInfoBean"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>

<jsp:useBean id="exportBean" class="it.cnr.icar.eric.client.ui.thin.ExportBean" scope="session"/>

<%
  exportBean.doWriteZipFile(response.getOutputStream());
  
  out.clear();
  out = pageContext.pushBody(); 
%>
  
