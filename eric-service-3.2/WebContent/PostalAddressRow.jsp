<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


    <c:if test="${userPreferencesBean.uiLocale.language == 'de'}">
        <c:import url="/PostalAddressRow_de.jsp"/>
    </c:if>

    <c:if test="${userPreferencesBean.uiLocale.language == 'es'}">
        <c:import url="/PostalAddressRow_es.jsp"/>
    </c:if>

    <c:if test="${userPreferencesBean.uiLocale.language == 'fr'}">
        <c:import url="/PostalAddressRow_fr.jsp"/>
    </c:if>

    <c:if test="${userPreferencesBean.uiLocale.language == 'ja'}">
        <c:import url="/PostalAddressRow_ja.jsp"/>
    </c:if>

    <c:if test="${userPreferencesBean.uiLocale.language == 'ko'}">
        <c:import url="/PostalAddressRow_ko.jsp"/>
    </c:if>

    <c:if test="${userPreferencesBean.uiLocale.language == 'zh'}">
        <c:import url="/PostalAddressRow_zh.jsp"/>
    </c:if>
    <!-- Use en by default -->
    <c:if test="${userPreferencesBean.uiLocale.language != 'de' &&
                userPreferencesBean.uiLocale.language != 'es' &&
                userPreferencesBean.uiLocale.language != 'fr' &&
                userPreferencesBean.uiLocale.language != 'ja' &&
                userPreferencesBean.uiLocale.language != 'ko' &&
                userPreferencesBean.uiLocale.language != 'zh'}">
        <c:import url="/PostalAddressRow_en.jsp"/>
    </c:if>
    
