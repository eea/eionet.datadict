<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error page">

    <stripes:layout-component name="contents">

       <h1>Unexpected error has occurred</h1>

       <div style="color:red; font-weight:bold;">
            <c:choose>
                <c:when test="${not empty actionBean.message}">
                    <c:out value="${actionBean.message}" />
                </c:when>
                <c:otherwise>
                    Unknown error.
                </c:otherwise>
            </c:choose>
       </div>
       <br />
       Find more information about the error in the log files. To continue your work, go <stripes:link href="/">here</stripes:link>.

    </stripes:layout-component>

</stripes:layout-render>