<%-- 
     NOTE: This is a copy of MultipartRequestHandler2.jsp, for independence
     of roCollection bean. Once that dependence is cleared, the pages can be
     unified again.
 --%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="
java.io.InputStream,
java.io.File,
java.io.FileOutputStream,

javax.faces.context.FacesContext,

javax.mail.internet.MimeMultipart,
javax.mail.internet.MimeBodyPart,
javax.mail.Part,

it.cnr.icar.eric.client.ui.thin.FileUploadBean,
it.cnr.icar.eric.client.ui.thin.RegistryBrowser,
it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle,
it.cnr.icar.eric.client.ui.thin.StreamDataSource
"%>
<f:view locale="#{userPreferencesBean.uiLocale}">
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>

<jsp:useBean id="fileUploadBean" class="it.cnr.icar.eric.client.ui.thin.FileUploadBean" scope="session"/>
<jsp:useBean id="registryBrowser" class="it.cnr.icar.eric.client.ui.thin.RegistryBrowser" scope="session"/>

<%
fileUploadBean.doClear();
%>

<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><%= registryBrowser.getCssFile()%>'>

<html>
<head><title><%= WebUIResourceBundle.getInstance().getString("fileUpload") %></title></head>

     <body>
     
        <form method="post"  name="multipartrequesthandlerform">
<%
    String name = null;
    String filename = null;
    String type = null;
    File dir = null;
    File file = null;
    long fileSize = 0L;
    int STREAM_BUFFER_SIZE = 10 * 1024; // 10KB
    try {
        //create DataStream object to retrive part of file 
        // which provide actual file and related info like filename,
        // contentType others....
        
        StreamDataSource sds = new StreamDataSource(request);
        MimeMultipart multi = new MimeMultipart(sds);

        for (int i=0, n=multi.getCount(); i<n; i++) {

            Part part = multi.getBodyPart(i);
            filename = part.getFileName();
            MimeBodyPart mbp = (MimeBodyPart)part;
            type = mbp.getContentType();

            if (filename != null) {

                dir = new File(System.getProperty("java.io.tmpdir"));
                if (!dir.isDirectory()){
                    dir.mkdir();
                }
                name = System.getProperty("java.io.tmpdir");
                //creating file in temp location on the server
                file  =  new File(name,filename);

                InputStream inputStream = part.getInputStream(); 
                //writing file to the temp location.
                FileOutputStream fos = new FileOutputStream(file);
                int read = 0;
                byte buffer[] = new byte[STREAM_BUFFER_SIZE];
                while ((read = inputStream.read(buffer, 0, STREAM_BUFFER_SIZE)) != -1) {
                    fos.write(buffer, 0, read);
                }

                fos.close();                    
                inputStream.close();

                if (file != null) {
                    fileSize = file.length();
                }
                i=n;
            }
        }

        //set the value in fileUploadBean which
        //will be used for file Upload Operation.
        fileUploadBean.setFileName(filename);
        fileUploadBean.setContentType(type);
        fileUploadBean.setFile(file);
        fileUploadBean.setFileSize(fileSize);

        if (fileUploadBean.isFileLengthMore()){
            out.println("<h4>"+WebUIResourceBundle.getInstance().
            getString("fileSizeBig")+"</h4>");
        }

        out.println("</br></br></br>");
        out.println("<table name=\"MutlipartProcessTable\" "+
        "class=\"tabPage\">");
        out.println(WebUIResourceBundle.getInstance().getString("uploadedFile", 
                    new Object[]{fileUploadBean.getFileName()}));
        out.println("<tr><td>");
        out.println("<input type=\"button\" name=\"ok\" value=\" "+
        WebUIResourceBundle.getInstance().getString("ok")+
        "  \" onclick=\"window.opener.location=window.opener."+
        "location;window.close()\"></td></table>");
    } catch (java.lang.OutOfMemoryError ex){
        //TODO: proper error reporting
        out.println(WebUIResourceBundle.getInstance().getString("javaHeapSpace"));
        ServletContext ctx = getServletConfig().getServletContext();
        RequestDispatcher rd = ctx.getRequestDispatcher("/FileUpload2.jsp");
        rd.forward(request,response);
        if (file != null) {
            file.delete();
        }
        return;
    } catch(Exception ex) {
        //TODO: proper error reporting
        out.println(ex.getClass().getName() + ": " + ex.getMessage());
        ServletContext ctx = getServletConfig().getServletContext();
        RequestDispatcher rd = ctx.getRequestDispatcher("/FileUpload2.jsp");
        rd.forward(request,response);
        if (file != null) {
            file.delete();
        }
    }
%>
        </form>
     
    </body>
</html>
</f:view>
