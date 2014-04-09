<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Tables">

    <stripes:layout-component name="contents">

        <h1>Namespaces from Data Dictionary</h1>

        <h3>RDF Namespaces</h3>

        <display:table name="${actionBean.rdfNamespaceResult}" class="sortable" id="rdfNamespace" sort="list" >
            <display:setProperty name="basic.msg.empty_list" value="No RDF namespaces found." />
            <display:setProperty name="paging.banner.item_name" value="RDF namespace" />
            <display:setProperty name="paging.banner.items_name" value="RDF namespaces" />
            <display:column title="Prefix" sortable="true">
                <c:out value="${rdfNamespace.prefix}" />
            </display:column>
            <display:column title="URI" sortable="true">
                <c:out value="${rdfNamespace.uri}" />
            </display:column>
        </display:table>

        <h3>Data Dictionary Namespaces</h3>

        <display:table name="${actionBean.namespaceResult}" class="sortable" id="datasetNamespace" sort="list" requestURI="/namespaces">
            <display:setProperty name="paging.banner.placement" value="both" />
            <display:setProperty name="basic.msg.empty_list" value="No namespaces found." />
            <display:setProperty name="paging.banner.item_name" value="namespace" />
            <display:setProperty name="paging.banner.items_name" value="namespaces" />
            <display:column title="Prefix" sortable="true">
                <c:out value="${actionBean.sitePrefix}" />
            </display:column>
            <display:column title="Url" sortable="true">
                <c:out value="${datasetNamespace.url}" />
            </display:column>
            <display:column title="Short name" sortable="true">
                <c:out value="${datasetNamespace.shortName}" />
            </display:column>
            <display:column title="Full name" sortable="false">
                <c:out value="${datasetNamespace.fullName}" />
            </display:column>
        </display:table>

    </stripes:layout-component>

</stripes:layout-render>
