<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- RegistryObject.xsl -->
<xsl:import href="http://localhost:8080/eric/registry/http?interface=QueryManager&amp;method=getRepositoryItem&amp;param-id=urn:uuid:0f4e8307-b208-4e91-97b6-aadd0134e3cd"/>

<xsl:template match="*" name="RegistryEntry">
        <xsl:call-template name="RegistryObject"/>
        <tr valign="top">
            <td width="24%">
            <p> <font size="-1" style="font-family: Arial,Helvetica,sans-serif;"><b>Status:</b></font> </p>
            </td>
            <td width="25%">
            <p><br></br>
            </p>
            </td>
            <td width="51%">
            <p> <font size="-1" style="font-family: Arial,Helvetica,sans-serif;"><b>Expiration:</b></font> </p>
            </td>
	</tr>
	<tr valign="top">
	    <td colspan="2" width="49%">				                                      
            <xsl:variable name="status" select="@status"/>
            <p><input type="text" name="TextBox4" size="50" value="{$status}"></input></p>
	    </td>
	    <td width="51%">				                                                 
            <xsl:variable name="expiration" select="@expiration"/>
            <p><input type="text" name="TextBox3" size="50" value="{$expiration}"></input></p>
	    </td>
	</tr>
	<tr valign="top">
	    <td width="24%">				                                                 
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Stability:</b></font></p>
	    </td>
  	    <td width="25%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	    <td width="51%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	</tr>
	<tr valign="top">
	    <td colspan="2" width="49%">				                                      
            <p>
            <xsl:variable name="stability" select="@stability"/>
            <xsl:choose>
            <xsl:when test="$stability">
            <input type="text" name="TextBox5" size="50" value="{$stability}"></input>
            </xsl:when>
            <xsl:otherwise>
            <input type="text" name="TextBox5" size="50" value="Dynamic"></input>
            </xsl:otherwise>
            </xsl:choose>
            </p>
	    </td>
	    <td width="51%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	</tr>
	<tr valign="top">
	    <td width="24%">				                                                 
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>User Version:</b></font></p>
	    </td>
	    <td width="25%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	    <td width="51%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	</tr>
	<tr valign="top">
	    <td colspan="2" width="49%">				                                      
            <p>
            <xsl:variable name="userVersion" select="@userVersion"/>
            <input type="text" name="TextBox7" size="50" value="{$userVersion}"></input>
            </p>
	    </td>
	    <td width="51%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	</tr>
</xsl:template>

</xsl:stylesheet>
