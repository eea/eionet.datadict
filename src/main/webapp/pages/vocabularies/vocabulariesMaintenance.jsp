<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabularies">

    <stripes:layout-component name="head">
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h1>Maintain vocabularies</h1>

        <h3>Populate Empty Base URI</h3>
        <p class="advise-msg">
            Populates empty base uris with <em>site_prefix/vocabulary_set_identifier/vocabulary_identifier</em>.
        </p>
        <p>
            Site prefix for current instance: ${actionBean.sitePrefix}
        </p>

        <stripes:form id="vocabulariesPopulateEmptyBaseUrisForm" beanclass="${actionBean.class.name}" method="post" style="margin-top:1em">
            <stripes:submit id="populateBtn" name="populate" value="Populate" />
        </stripes:form>

        <h3>Change Site Prefix in Base URIs</h3>
        <p class="advise-msg">
            If data is gathered from another source or site prefix has changed, you can use this functionality to change
            site prefix in all vocabularies (which starts with old prefix)
        </p>


    </stripes:layout-component>
</stripes:layout-render>
