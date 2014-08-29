<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

    <h2>RECENTLY RELEASED ITEMS</h2>

    <display:table name="${actionBean.dataSets}" class="sortable" id="recenltyReleased" sort="list" >
        <display:column title="Shortname">
            <c:out value="${recenltyReleased.name}" />
        </display:column>
        <display:column title="Released Date">
            <c:out value="${recenltyReleased.dateString}"/>
        </display:column>
    </display:table>


    <display:table name="${actionBean.schemas}" class="sortable" id="recenltyReleased" sort="list" >
        <display:column title="Shortname">
            <c:out value="${recenltyReleased.nameAttribute}"/>
        </display:column>
        <display:column title="Released Date">
            <fmt:formatDate value="${recenltyReleased.dateModified}" type="DATE" dateStyle="LONG"/>
        </display:column>
    </display:table>

    <display:table name="${actionBean.vocabularies}" class="sortable" id="recenltyReleased" sort="list" >
        <display:column title="Shortname">
            <c:out value="${recenltyReleased.label}"/>
        </display:column>
        <display:column title="Released Date">
            <fmt:formatDate value="${recenltyReleased.dateModified}" type="DATE" dateStyle="LONG"/>
        </display:column>
    </display:table>
