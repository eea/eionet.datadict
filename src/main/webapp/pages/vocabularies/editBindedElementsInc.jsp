<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
<!--
( function($) {
    $(document).ready(function() {

        // Open add concept dialog
        $("#addElementsLink").click(function() {
            $("#addElementsDiv").dialog('open');
            return false;
        });

        // Add concept dialog setup
        $("#addElementsDiv").dialog({
            autoOpen: false,
            width: 800
        });

        // Close add concept dialog
        $("#closeAddElementsButton").click(function() {
            $("#addElementsDiv").dialog("close");
            return false;
        });

        <c:if test="${not empty actionBean.editDivId}">
            openPopup("#${actionBean.editDivId}");
        </c:if>

    });
} ) ( jQuery );
//-->
</script>

<h2>Bound elements for concepts</h2>

<display:table name="actionBean.bindedElements" class="sortable" id="item"
    requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/edit">
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
        <c:if test="${item.type == 'CH1'}">Fixed values</c:if>
        <c:if test="${item.type == 'CH2'}">Quantitative</c:if>
    </display:column>
    <display:column title="Status" sortable="true">
        <c:url var="imgSrc" value="/images/${item.statusImage}" />
        <img src="${imgSrc}" border="0" title="${item.status}" />

        <c:if test="${item.released}">
            <fmt:setLocale value="en_GB" />
            <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
            <sup class="commonelm">${dateFormatted}</sup>
        </c:if>
    </display:column>
    <display:column>
        <stripes:link beanclass="${actionBean.class.name}" event="removeDataElement">
            <stripes:param name="elementId" value="${item.id}" />
            <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
            <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
            <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
            <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
            <img src="<c:url value='/images/delete.gif'/>" alt="Remove" border="0" />
        </stripes:link>
    </display:column>
</display:table>

<br />
<a href="#" id="addElementsLink">Add new data elements</a>
<br />
<br />

<div id="addElementsDiv" title="Add new data element">
    <stripes:form method="post" beanclass="${actionBean.class.name}">
        <div>
        <stripes:hidden name="vocabularyFolder.folderName" />
        <stripes:hidden name="vocabularyFolder.identifier" />
        <stripes:hidden name="vocabularyFolder.workingCopy" />
        </div>
        <table class="datatable">
            <colgroup>
                <col style="width:10em;"/>
                <col />
                <col />
            </colgroup>
            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Data element name">
                    <label for="filterText"><span style="white-space:nowrap;">Data element</span></label>
                </th>
                <td class="simple_attr_value">
                    <stripes:text class="smalltext" size="30" name="elementsFilter.identifier" id="filterText"/>
                </td>
                <td>
                    <stripes:submit name="searchDataElements" value="Search" class="mediumbuttonb"/>
                </td>
            </tr>
        </table>
    </stripes:form>

    <display:table name="actionBean.elementsResult.dataElements" class="sortable" id="item" pagesize="20"
        requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/searchDataElements">
        <display:column title="Element" sortable="true" sortProperty="identifier">
            <c:choose>
                <c:when test="${item.released}">
                    <stripes:link beanclass="${actionBean.class.name}" event="addDataElement">
                        <stripes:param name="elementId" value="${item.id}" />
                        <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        ${item.identifier}
                    </stripes:link>
                    <c:if test="${not empty item.workingUser}">
                        <span class="checkedout" title="${item.workingUser}">*</span>
                    </c:if>
                </c:when>
                <c:otherwise>
                    ${item.identifier}
                    <c:if test="${not empty item.workingUser}">
                        <span class="checkedout" title="${item.workingUser}">*</span>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </display:column>
        <display:column title="Type" sortable="true">
            <c:if test="${item.type == 'CH1'}">Fixed values</c:if>
            <c:if test="${item.type == 'CH2'}">Quantitative</c:if>
        </display:column>
        <display:column title="Status" sortable="true">
            <c:url var="imgSrc" value="/images/${item.statusImage}" />
            <img src="${imgSrc}" border="0" title="${item.status}" />

            <c:if test="${item.released}">
                <fmt:setLocale value="en_GB" />
                <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
                <sup class="commonelm">${dateFormatted}</sup>
            </c:if>
        </display:column>
    </display:table>
</div>