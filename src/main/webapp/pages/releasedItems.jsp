<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

    <h2>Recently Released Items</h2>

    <!-- TODO make icon looks nicer -->
    <c:url var="datasetIconUrl" value="/images/pdf.png" />
    <c:url var="vocabularyIconUrl" value="/images/rdf-icon.gif" />
    <c:url var="schemaIconUrl" value="/images/xsl.png" />
    <display:table name="${actionBean.results}" class="datatable" id="recentlyReleased" style="width:100%" >
        <display:column style="width:5%">
            <!-- TODO add actions -->
            <c:choose>
                <c:when test="${recentlyReleased.type eq 'VOCABULARY'}">
                    <img src="${vocabularyIconUrl}" alt="" />
                </c:when>
                <c:when test="${recentlyReleased.type eq 'DATASET'}">
                    <img src="${datasetIconUrl}" alt="" />
                </c:when>
                <c:when test="${recentlyReleased.type eq 'SCHEMA'}">
                    <img src="${schemaIconUrl}" alt="" />
                </c:when>
            </c:choose>
        </display:column>
        <display:column style="width:70%">
            <!-- TODO Add links -->
            <c:out value="${recentlyReleased.name}" />
        </display:column>
        <display:column style="width:25%">
            <fmt:formatDate value="${recentlyReleased.releasedDate}" type="DATE" dateStyle="LONG"/>
        </display:column>
    </display:table>
