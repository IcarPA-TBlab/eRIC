<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="
java.io.File,
java.util.Enumeration,
java.io.FileOutputStream,
java.io.InputStream,
java.io.BufferedOutputStream,

javax.servlet.http.HttpServlet,
javax.servlet.http.HttpServletRequest,
javax.servlet.http.HttpServletResponse,
javax.faces.context.FacesContext,

javax.mail.internet.MimeMultipart,
javax.mail.internet.MimeBodyPart,
javax.mail.Part,

it.cnr.icar.eric.client.ui.thin.RegistryObjectCollectionBean,
it.cnr.icar.eric.client.ui.thin.RegistryBrowser,
it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle,
it.cnr.icar.eric.client.ui.thin.StreamDataSource
"%>
<f:view locale="#{userPreferencesBean.uiLocale}">
<f:loadBundle basename="it.cnr.icar.eric.client.ui.thin.ResourceBundle" var="bundle"/>
<jsp:useBean id="fileUploadBean" 
    class="it.cnr.icar.eric.client.ui.thin.FileUploadBean" 
    scope="session" />

<%
RegistryObjectCollectionBean roCollection = 
        (RegistryObjectCollectionBean)FacesContext.getCurrentInstance()
                                                  .getExternalContext()
                                                  .getSessionMap()
                                                  .get("roCollection");

RegistryBrowser registryBrowser = 
        (RegistryBrowser)FacesContext.getCurrentInstance()
                                     .getExternalContext()
                                     .getSessionMap()
                                     .get("registryBrowser");
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

        //delete file from temp location if any. 
        roCollection.doDeleteFile();
        
        //setting the file name in Registry object collection bean 
        //so file can be deleted after the save operation.
        roCollection.setFileName(filename);
        
        String status = roCollection.doUpload(fileUploadBean.getFile(),
        fileUploadBean.getContentType());

        if(fileUploadBean.isFileLengthMore()){
            out.println("<h4>"+WebUIResourceBundle.getInstance().
            getString("fileSizeBig")+"</h4>");
        }
        if(status.equals("success")){
            out.println("</br></br></br>");
            out.println("<table name=\"MutlipartProcessTable\" "+
            "class=\"tabPage\">");
            out.println(WebUIResourceBundle.getInstance().getString("uploadedFile", 
                                new Object[]{fileUploadBean.getFileName()}));
            out.println("<tr><td>"+WebUIResourceBundle.getInstance().getString("saveRepositoryItem")+
                        "</td></tr>");
            out.println("<tr><td>");
            out.println("<input type=\"button\" name=\"ok\" value="+
            WebUIResourceBundle.getInstance().getString("ok")+
            "  \" onclick=\"window.close()\"></td></table>");
        } else {
            ServletContext ctx = getServletConfig().getServletContext();
            RequestDispatcher rd = ctx.getRequestDispatcher("/FileUpload.jsp");
            rd.forward(request,response);
            roCollection.append(WebUIResourceBundle.getInstance().
            getString("problemDuringFileUpload"));
            file.delete();
        }
        roCollection.getCurrentRegistryObjectBean().setNewUpload(true);
    }catch (java.lang.OutOfMemoryError ex){
        roCollection.append(WebUIResourceBundle.getInstance().
            getString("javaHeapSpace"));
        ServletContext ctx = getServletConfig().getServletContext();
        RequestDispatcher rd = ctx.getRequestDispatcher("/FileUpload.jsp");
        rd.forward(request,response);
        return;
    }catch(Exception ex) {
        file.delete();
        roCollection.append(ex.getMessage());
        ServletContext ctx = getServletConfig().getServletContext();
        RequestDispatcher rd = ctx.getRequestDispatcher("/FileUpload.jsp");
        rd.forward(request,response);
        return;
    }
%>
        </form>
     
    </body>
</html>
</f:view>
