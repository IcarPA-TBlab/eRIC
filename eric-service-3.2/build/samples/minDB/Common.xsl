<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- RegistryObject.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:0f4e8307-b208-4e91-97b6-aadd0134e3cd"/>
<!-- Service.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:64275ff5-e943-491f-b1ad-f1242a9dd023"/>
<!-- ServiceBinding.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:8b7886ee-69eb-48a6-a254-830227cdcfc5"/>

<xsl:template match="/">
<html>
<head>
<script language="JavaScript">
<![CDATA[
function setupWindow() {
    var length = 750
    var height = 0
    var form = document.forms[0]
    if (form.elements.length == 17 || form.elements.length == 16) {
        height = 690
    } else if (form.elements.length == 15 || form.elements.length == 14) {
        height = 600
    } else if (form.elements.length == 13 || form.elements.length == 12 ) {
        height = 510
    } else if (form.elements.length == 11 || form.elements.length == 10 ) {
        height = 420
    }
    var listbox = document.forms[0].ListBox
    var text = null
    var maxStringLength = 16
    var listlength = 0
    if (listbox != null) {
        listlength = listbox.length
        for (var i = 0; i < listlength; i++) {
            text = listbox.options[i].text
            if (text.length > maxStringLength) {
                maxStringLength = text.length
            }
        }
    }
    listbox = document.forms[0].ListBox1
    if (listbox != null) {
        listlength = listbox.length
        for (var i = 0; i < listlength; i++) {
            text = listbox.options[i].text
            if (text.length > maxStringLength) {
                maxStringLength = text.length
            }
        }
    }
    listbox = document.forms[0].ListBox2
    if (listbox != null) {
        listlength = listbox.length
        for (var i = 0; i < listlength; i++) {
            text = listbox.options[i].text
            if (text.length > maxStringLength) {
                maxStringLength = text.length
            }
        }
    }
    listbox = document.forms[0].ListBox3
    if (listbox != null) {
        listlength = listbox.length
        for (var i = 0; i < listlength; i++) {
            text = listbox.options[i].text
            if (text.length > maxStringLength) {
                maxStringLength = text.length
            }
        }
    }
    if (maxStringLength > 16) {
        length = length + parseInt(maxStringLength-16)*2.5
    }
    var app = navigator.appName
    var version = navigator.appVersion
    if (app.indexOf("Netscape") != -1 && version.indexOf("5") != -1) {
        height = height - 20
        length = length - 30
    }
    if (app.indexOf("Netscape") != -1 && version.indexOf("4") != -1) {
        height = height - 10
        length = length + 135
    }
    window.resizeTo(length, height)
    window.moveTo(75,75)
    window.locationbar.visible=false
    window.menubar.visible=false
    window.personalbar.visible=false
    window.toolbar.visible=false
}
function dismissWindow() {
 window.close()
}
]]>
</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</meta>
<title></title>
</head>
<body lang="en-US" bgcolor="#ccccff" dir="ltr" onLoad="setupWindow()">
<xsl:apply-templates select="*"/>
</body>
</html>
</xsl:template>

<xsl:template match="*">
<form name="Standard">
  <table width="100" border="0" cellpadding="0" cellspacing="0">
  <tbody>
  <tr>
  <td>
  <fieldset><legend><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
      <b><xsl:value-of select="name(../*)"/> Details</b></font></legend>
      <table width="100%" border="0" cellpadding="2" cellspacing="0">
      <col width="25*"></col><col width="25*"></col><col width="50*"></col>
      <tbody>
      <xsl:variable name="object" select="name(../*)"/>
      <xsl:choose>
      <xsl:when test="$object = 'Service'">
          <xsl:call-template name="Service"/>
      </xsl:when>
      <xsl:when test="$object = 'ServiceBinding'">
          <xsl:call-template name="ServiceBinding"/>
      </xsl:when>
      <xsl:otherwise>
          <xsl:call-template name="RegistryObject"/>
      </xsl:otherwise>
      </xsl:choose>
      </tbody>
      </table>
  </fieldset>
  </td>
  </tr>
  <tr>
      <td>
      <div align="right">
      <input value=" Cancel " name="PushButton3" type="button" onClick="dismissWindow()"></input>
      </div>
      </td>
  </tr>  
  </tbody>
  </table>
</form>
</xsl:template>
</xsl:stylesheet>
