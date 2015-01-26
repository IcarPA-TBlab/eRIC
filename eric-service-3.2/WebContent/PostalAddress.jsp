<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

    <c:choose>
    <c:when test="${userPreferencesBean.uiLocale.language == 'de'}">
          <c:import url="/PostalAddress_de.jsp"/>
    </c:when>
    <c:when test="${userPreferencesBean.uiLocale.language == 'es'}">
          <c:import url="/PostalAddress_es.jsp"/>
    </c:when>
    <c:when test="${userPreferencesBean.uiLocale.language == 'fr'}">
          <c:import url="/PostalAddress_fr.jsp"/>
    </c:when>
    <c:when test="${userPreferencesBean.uiLocale.language == 'ja'}">
          <c:import url="/PostalAddress_ja.jsp"/>
    </c:when>
    <c:when test="${userPreferencesBean.uiLocale.language == 'ko'}">
          <c:import url="/PostalAddress_ko.jsp"/>
    </c:when>
    <c:when test="${userPreferencesBean.uiLocale.language == 'zh'}">
          <c:import url="/PostalAddress_zh.jsp"/>
    </c:when>
    <c:otherwise>
          <c:import url="/PostalAddress_en.jsp"/>
    </c:otherwise>
    </c:choose>
