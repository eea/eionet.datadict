<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp">
    <stripes:layout-component name="title">
        <title>${initParam.appDispName}</title>
    </stripes:layout-component>
    <stripes:layout-component name="bodylabel">
        <body  class="threecolumns">
    </stripes:layout-component>
    <stripes:layout-component name="news">
        <div id="rightcolumn" class="quickjumps">
            ${actionBean.helps}
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${empty actionBean.errorMessage}">
                <div id="outerframe">
                    <jsp:include page="/releasedItems.action" flush="true" />
                    <jsp:include page="/documentation">
                        <jsp:param name="event" value="show"/>
                    </jsp:include>
                    ${actionBean.support}
                </div>
            </c:when>
            <c:otherwise>
                <stripes:layout-render name="/pages/welcome/show_DD_ERR_MSG.jsp" errorMessage="${actionBean.errorMessage}" errorTrace="${actionbean.errorTrace}"/>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
