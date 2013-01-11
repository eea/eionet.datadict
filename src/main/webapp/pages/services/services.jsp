<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Services">

    <stripes:layout-component name="contents">

        <h1>Services</h1>

        <ul>
            <li><stripes:link href="/services/siteCodes">CDDA dataflow site codes</stripes:link></li>
        </ul>

    </stripes:layout-component>
</stripes:layout-render>