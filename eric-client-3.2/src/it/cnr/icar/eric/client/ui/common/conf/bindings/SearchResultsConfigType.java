//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.20 at 09:38:53 AM CET 
//


package it.cnr.icar.eric.client.ui.common.conf.bindings;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;p xmlns="http://www.w3.org/2001/XMLSchema" xmlns:query="urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:tns="urn:oasis:names:tc:ebxml-regrep:config:xsd:3.0"&gt;
 *           Configures how the Search Results table in RegistryBrowser
 *           displays the results of a search that displays objects belonging to that ObjectType.
 *         &lt;/p&gt;
 * </pre>
 * 
 *         
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;h5 xmlns="http://www.w3.org/2001/XMLSchema" xmlns:query="urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:tns="urn:oasis:names:tc:ebxml-regrep:config:xsd:3.0"&gt;Element SearchResultsColumn:&lt;/h5&gt;
 * </pre>
 * 
 *         
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;p xmlns="http://www.w3.org/2001/XMLSchema" xmlns:query="urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:tns="urn:oasis:names:tc:ebxml-regrep:config:xsd:3.0"&gt;Configures a display column within the search results table of RegistryBrowser.&lt;/p&gt;
 * </pre>
 * 
 *       
 * 
 * <p>Java class for SearchResultsConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchResultsConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:config:xsd:3.0}SearchResultsColumn" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchResultsConfigType", namespace = "urn:oasis:names:tc:ebxml-regrep:config:xsd:3.0", propOrder = {
    "searchResultsColumn"
})
public class SearchResultsConfigType {

    @XmlElement(name = "SearchResultsColumn", required = true)
    protected List<SearchResultsColumnType> searchResultsColumn;

    /**
     * Gets the value of the searchResultsColumn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the searchResultsColumn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSearchResultsColumn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SearchResultsColumnType }
     * 
     * 
     */
    public List<SearchResultsColumnType> getSearchResultsColumn() {
        if (searchResultsColumn == null) {
            searchResultsColumn = new ArrayList<SearchResultsColumnType>();
        }
        return this.searchResultsColumn;
    }

}
