//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.20 at 09:38:53 AM CET 
//


package it.cnr.icar.eric.client.ui.common.conf.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QueryExpressionBranchType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryExpressionBranchType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0}BranchType">
 *       &lt;sequence>
 *         &lt;element name="QueryLanguageQuery" type="{urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0}ClassificationNodeQueryType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryExpressionBranchType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {
    "queryLanguageQuery"
})
public class QueryExpressionBranchType
    extends BranchType
{

    @XmlElement(name = "QueryLanguageQuery")
    protected ClassificationNodeQueryType queryLanguageQuery;

    /**
     * Gets the value of the queryLanguageQuery property.
     * 
     * @return
     *     possible object is
     *     {@link ClassificationNodeQueryType }
     *     
     */
    public ClassificationNodeQueryType getQueryLanguageQuery() {
        return queryLanguageQuery;
    }

    /**
     * Sets the value of the queryLanguageQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassificationNodeQueryType }
     *     
     */
    public void setQueryLanguageQuery(ClassificationNodeQueryType value) {
        this.queryLanguageQuery = value;
    }

}
