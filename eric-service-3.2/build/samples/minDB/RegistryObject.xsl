<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="*" name="RegistryObject">
        <tr valign="top">
            <td width="24%"><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Name:</b>(en_US)</font>
            </td>
	    <td width="25%"><div align="right"><input type="button" name="PushButton" value="Details"></input></div>
	    </td>
	    <td width="51%"><br></br></td>
	</tr>
	<tr valign="top">
            <td colspan="2" width="49%">
            <p>
            <xsl:variable name="value" select="Name/LocalizedString/@value"/>
            <input type="text" name="TextBox" size="50" value="{$value}"> 
            </input>                                             
            </p>
	    </td>
	    <td width="51%">
            <p><br></br></p>
            </td>
	</tr>
	<tr valign="top">
    	    <td width="24%"><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Description:</b> (en_US)</font>
            </td>
            <td width="25%"><div align="right">					                           
            <input type="button" name="PushButton4" value="Details"></input>
            </div>
	    </td>
     	    <td width="51%">				                                                 
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Unique Identifier:</b></font></p>
	    </td>
	</tr>
	<tr valign="top">
	    <td rowspan="2" colspan="2" width="49%">				                          
            <textarea name="TextBox1" cols="37" rows="2"><xsl:value-of select="Description/LocalizedString/@value"/>
            </textarea>
            </td>
	    <td width="51%">				                                             
            <input type="text" name="TextBox2" size="50"></input>
            </td>
	</tr>
	<tr>
	    <td width="51%" valign="top">				                                    
            <p><br></br></p>
	    </td>
	</tr>
	<tr valign="top">
   	    <td width="24%">				                                                 
             <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
             <b>Classifications:</b></font></p>
	     </td>
	     <td width="25%"><p><br></br>
	     </p>
	     </td>
	     <td width="51%">				                                                 
             <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
             <b>External Identifiers:</b></font></p>
	     </td>
	</tr>
	<tr valign="top">
	     <td colspan="2" width="49%">				                                      
             <p>                                                        
             <xsl:choose>
             <xsl:when test="/*/Classification != ''">
             <select name="ListBox" size="2">
             <xsl:for-each select="/*/Classification">
             <option>
             <xsl:value-of select="Name/LocalizedString/@value" />
             </option>
             </xsl:for-each>
             </select>
             </xsl:when>
             <xsl:otherwise>
             <input type="text" name="ClassificationTextField" size="50"/>
             </xsl:otherwise>
             </xsl:choose>
             </p>
	     </td>
	     <td width="51%">				                                                 
             <p> 
             <xsl:choose>
             <xsl:when test="/*/ExternalIdentifier != ''">
             <select name="ListBox1" size="2">
             <xsl:for-each select="/*/ExternalIdentifier">
             <option>
             <xsl:value-of select="Name/LocalizedString/@value" />
             </option>
             </xsl:for-each>
             </select>
             </xsl:when>
             <xsl:otherwise>
             <input type="text" name="ExternalIdTextField" size="50"/>
             </xsl:otherwise>
             </xsl:choose>
             </p>
	     </td>
	</tr>
	<tr valign="top">
	    <td width="24%">				                                                 
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>External Links:</b></font></p>
	    </td>
	    <td width="25%">				                                                 
            <p><br></br>
	    </p>
	    </td>
	    <td width="51%">				                                                 
            <p><font style="font-family: Arial,Helvetica,sans-serif;" size="-1">
            <b>Slots:</b></font></p>
	    </td>
	</tr>
	<tr valign="top">
	    <td colspan="2" width="49%">				                                      
            <p> 
            <xsl:choose>
            <xsl:when test="/*/ExternalLink != ''">
            <select name="ListBox2" size="2">
            <xsl:for-each select="/*/ExternalLink">
            <option>
            <xsl:value-of select="Name/LocalizedString/@value" />
            </option>
            </xsl:for-each>
            </select>
            </xsl:when>
            <xsl:otherwise>
            <input type="text" name="ExternalLinkTextField" size="50"/>
            </xsl:otherwise>
            </xsl:choose>
            </p>
	    </td>
	    <td width="51%">				                                                 
            <p>                                                        
            <xsl:choose>
            <xsl:when test="/*/Slot != ''">
            <select name="ListBox3" size="2">
            <xsl:for-each select="/*/Slot">
            <option>
            name=<xsl:value-of select="@name" /> type= null values=<xsl:value-of select="ValueList/Value" />
            </option>
            </xsl:for-each>
            </select>
            </xsl:when>
            <xsl:otherwise>
            <input type="text" name="SlotTextField" size="50"/>
            </xsl:otherwise>
            </xsl:choose>
            </p>
	    </td>
	</tr>
</xsl:template>

</xsl:stylesheet>
