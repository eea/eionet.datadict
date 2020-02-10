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

        <stripes:form beanclass="eionet.web.action.JWTActionBean">
            <p class="actions"><stripes:submit name="generateJWTToken" value="Generate Token" /></p>
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>
