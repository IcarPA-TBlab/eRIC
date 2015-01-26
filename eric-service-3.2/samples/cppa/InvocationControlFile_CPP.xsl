<?xml version = "1.0" encoding = "UTF-8"?>

<!--$Header: /cvsroot/ebxmlrr/omar/misc/samples/cppa/InvocationControlFile_CPP.xsl,v 1.11 2007/01/04 20:20:39 farrukh_najmi Exp $-->
<!--$Revision: 1.11 $-->
<xsl:stylesheet 
    xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" 
    xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:rim = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
    xmlns:rs = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0" 
    xmlns:tp = "http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd" 
    xmlns:xlink = "http://www.w3.org/1999/xlink" version = "1.0">
    
    <xsl:output method = "xml" indent = "yes"/>
    <xsl:strip-space elements = "*"/>
    <xsl:param name="repositoryItem"></xsl:param>
    
    <xsl:template match = "/">
        
        <!-- create the list -->  
        <rim:RegistryObjectList xsi:schemaLocation = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1 http://www.oasis-open.org/committees/regrep/documents/2.1/schema/rs.xsd urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1 http://www.oasis-open.org/committees/regrep/documents/2.1/schema/rim.xsd">  
            <!-- process all the rim:ExtrinsicObjects -->			    
            <!--xsl:message>
                <xsl:value-of select="name(*[1])"/> is what we found
            </xsl:message-->

            <xsl:apply-templates select = "//rim:ExtrinsicObject"/>  
        </rim:RegistryObjectList>
        
        
    </xsl:template>
    
    <xsl:template match = "rim:ExtrinsicObject">  
        <!-- store CPP in variable -->	  
        <xsl:variable name = "cpp" select = "document($repositoryItem, .)"/> 
        
        <!-- store PartyInfo for convenience -->	  
        <xsl:variable name = "PartyInfo" select = "$cpp/tp:CollaborationProtocolProfile/tp:PartyInfo"/>  
        
        <!-- another convenience... -->			    
        <xsl:variable name = "crole" select = "$PartyInfo/tp:CollaborationRole"/>    
        
        
        <!-- Make an ExtrinsicObject in the output -->		  
        <rim:ExtrinsicObject id = "{@id}" objectType = "{@objectType}">    
            <xsl:if test="@mimeType">
                <xsl:attribute name="mimeType">
                    <xsl:value-of select="@mimeType"/>
                </xsl:attribute>
            </xsl:if>
            <!-- Role Slot -->
            <xsl:call-template name="slot-list">
                <xsl:with-param name="name" select="'Role'"/>
                <xsl:with-param name="values" select="$crole/tp:Role/@tp:name"/>
            </xsl:call-template>
            
            <!--rim:Classification classifiedObject = "{@id}" classificationScheme = "idForCPPAScheme" nodeRepresentation = "{$crole/tp:Role/@tp:name}"/-->    

            <!-- Process Slot -->
            <xsl:call-template name="slot-list">
                <xsl:with-param name="name" select="'ProcessSpecification'"/>
                <xsl:with-param name="values" select="$crole/tp:ProcessSpecification/@tp:name"/>
            </xsl:call-template>
            <!--rim:Classification classifiedObject = "{@id}" classificationScheme = "idForProcessScheme" nodeRepresentation = "{$crole/tp:ProcessSpecification/@tp:name}"/-->    

            <!-- The name is derived from the tp:partyName attribute on the party? -->		    
            <rim:Name>      
                <rim:LocalizedString value = "{$PartyInfo/@tp:partyName}"/>    
            </rim:Name>
            
            <!-- Copy the description straight through -->			    
            <xsl:copy-of select = "rim:Description"/>
            
            <!-- Copy the versionInfo straight through -->			    
            <xsl:copy-of select = "rim:versionInfo"/>
            
            <!-- Get DUNS id only if PartyId type is DUNS -->
            <xsl:if test='$PartyInfo/tp:PartyId/@tp:type="urn:oasis:names:tc:ebxml-cppa:partyid-type:duns"'>
                <!-- DUNS identifier -->
                <rim:ExternalIdentifier identificationScheme = "urn:freebxml:registry:demoDB:classificationScheme:DUNS" value = "{$PartyInfo/tp:PartyId}" registryObject="{@id}" id="duns"/> 
            </xsl:if>
            
            <!-- Copy the contentVersionInfo straight through -->			    
            <xsl:copy-of select = "rim:contentVersionInfo"/>
            
        </rim:ExtrinsicObject>  
        
        <!-- ExternalLink for PartyRef/@xlink:href -->	  
        <rim:ExternalLink id = "partyRefId" externalURI = "{$PartyInfo/tp:PartyRef/@xlink:href}"/>						  
        <rim:Association id = "partyRefId-CPAId" associationType = "urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks" sourceObject = "partyRefId" targetObject = "{@id}"/>    
        
        <!-- ExternalLink for ProcessSpecification/@xlink:href -->
        <rim:ExternalLink id = "processHRefId" externalURI = "{$crole/tp:ProcessSpecification/@xlink:href}"/>
        <rim:Association id = "processHRefId-CPAId" associationType = "urn:oasis:names:tc:ebxml-regrep:AssociationType:ExternallyLinks" sourceObject = "processHRefId" targetObject = "{@id}"/>    
        
        <!--existing DUNS scheme. ObjctRef needed to satisfy validation -->
        <rim:ObjectRef id = "urn:freebxml:registry:demoDB:classificationScheme:DUNS"/>
        
        <!--existing CPPA scheme. ObjctRef needed to satisfy validation -->
        <!--rim:ObjectRef id = "idForCPPAScheme"/-->

        <!--existing ProcessSpecification@name scheme. ObjctRef needed to satisfy validation -->
        <!--rim:ObjectRef id = "idForProcessScheme"/-->
                                                

    </xsl:template>
    
    <xsl:template name="slot-list">
        <xsl:param name="name"/>
        <xsl:param name="values"/>
        <rim:Slot name = "{$name}">
            <rim:ValueList>
                <xsl:for-each select="$values">
                    <rim:Value><xsl:value-of select="."/></rim:Value>
                </xsl:for-each>
            </rim:ValueList>
        </rim:Slot>
    </xsl:template>
    
</xsl:stylesheet>
