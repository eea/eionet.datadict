<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Data elements">

    <stripes:layout-component name="contents">
    <h1>Data elements</h1>

    <c:if test="${empty actionBean.user}">
        <p class="advise-msg">
            Note: Elements from datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.
        </p>
    </c:if>

    <display:table name="actionBean.result.dataElements" class="sortable" id="item" requestURI="/searchelements/search/">
        <display:column title="Element" sortable="true" sortProperty="identifier">
            <c:choose>
                <c:when test="${item.released && empty actionBean.user}">
                    <stripes:link href="/dataelements/${item.id}">${item.identifier}</stripes:link>
                </c:when>
                <c:when test="${not empty actionBean.user}">
                    <stripes:link href="/dataelements/${item.id}">${item.identifier}</stripes:link>
                    <c:if test="${not empty item.workingUser}">
                        <span class="checkedout" title="${item.workingUser}">*</span>
                    </c:if>
                </c:when>
                <c:otherwise>
                    ${item.identifier}
                </c:otherwise>
            </c:choose>
        </display:column>
        <display:column title="Type" sortable="true">
            <c:if test="${item.type == 'CH1'}">Fixed values (code list)</c:if>
            <c:if test="${item.type == 'CH2'}">Quantitative</c:if>
            <c:if test="${item.type == 'CH3'}">Fixed Values (vocabulary)</c:if>
        </display:column>
        <c:if test="${!actionBean.result.commonElements}">
            <c:set var="statusLabel" value="Dataset status" />
            <display:column property="tableName" title="Table" sortable="true" />
            <display:column property="dataSetName" title="Dataset" sortable="true" />
        </c:if>
        <c:if test="${actionBean.result.commonElements}">
            <c:set var="statusLabel" value="Status" />
        </c:if>
        <display:column title="${statusLabel}" sortable="true">
            <c:url var="imgSrc" value="/images/${item.statusImage}" />
            <img src="${imgSrc}" border="0" title="${item.status}" />

            <c:if test="${item.released}">
                <fmt:setLocale value="en_GB" />
                <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
                <sup class="commonelm">${dateFormatted}</sup>
            </c:if>
        </display:column>
         <display:column title="Name" sortable="true" sortProperty="name">
            ${item.name}
        </display:column>
    </display:table>

    <c:if test="${not empty actionBean.result.dataElements}">
        Total results: ${actionBean.result.totalResults}
    </c:if>

    </stripes:layout-component>
</stripes:layout-render>