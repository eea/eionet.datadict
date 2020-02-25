<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Generate JWT Token" currentSection="administration">

    <stripes:layout-component name="contents">
        <h1>Generate JWT Token</h1>
        <div id="drop-operations">
            <ul>
                <li class="back">
                    <a href="${pageContext.request.contextPath}/administration">Back to administration</a>
                </li>
            </ul>
        </div>
        <p>
            A valid JWT token will be generated in order for you to upload an rdf file for vocabulary update via the API.
        </p>

        <stripes:form beanclass="${actionBean['class'].name}">
            <p class="actions"><stripes:submit name="generateToken" value="Generate Token" /></p>
        </stripes:form>

        <c:if test="${not empty param.generated_token}" >
            <p>The generated JWT token is:</p>
            <p style="word-wrap:break-word;"><b><c:out value="${param.generated_token}" /></b></p>
        </c:if>

    </stripes:layout-component>

</stripes:layout-render>
