<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
(function($) {
    $(document).ready(function() {
        // Add concept dialog setup
        $("#addElementsDiv").dialog({
            autoOpen: false,
            width: 800
        });

        // Open add concept dialog
        $("#addElementsLink").click(function() {
            $("#addElementsDiv").dialog('open');
            return false;
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
})(jQuery);
</script>

<h2>Bound elements for concepts</h2>

<display:table name="actionBean.boundElements" class="datatable results" id="item" 
    requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/edit">
    <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No bound elements found.</p>" />
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
        <c:if test="${item.type == 'CH3'}">Vocabulary</c:if>
    </display:column>
    <display:column title="Status" sortable="true">
        <dd:datasetRegStatus value="${item.status}" />
        <c:if test="${item.released}">
            <fmt:setLocale value="en_GB" />
            <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
            <sup class="commonelm">${dateFormatted}</sup>
        </c:if>
    </display:column>
    <c:if test="${param.editMode eq 'true'}">
        <display:column>
            <stripes:form beanclass="${actionBean['class'].name}" onclick="return confirm('Are you sure you want to remove the bound element?');" >
                <stripes:param name="elementId" value="${item.id}" />
                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                <stripes:submit name="removeDataElement" style="color:red;background:none;border:none;font-weight:bold;font-size:large;" value="X" />
            </stripes:form>
        </display:column>
    </c:if>
</display:table>

<c:if test="${param.editMode eq 'true'}">
    <br />
    <a href="#" id="addElementsLink">Add new data elements</a>
    <br />
    <br />

    <div id="addElementsDiv" title="Add new data element">
        <stripes:form method="post" beanclass="${actionBean['class'].name}">
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
                        <label for="elemFilterText"><span style="white-space:nowrap;">Data element</span></label>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="elementsFilter.identifier" id="elemFilterText"/>
                    </td>
                    <td>
                        <stripes:submit name="searchDataElements" value="Search" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </stripes:form>

        <c:if test="${not empty actionBean.elementsResult}">
            <display:table name="actionBean.elementsResult.list" class="datatable results" id="item" pagesize="10"
                requestURI="/vocabulary/${actionBean.vocabularyFolder.folderName}/${actionBean.vocabularyFolder.identifier}/searchDataElements">
                <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No data elements found.</p>" />
                <display:column title="Element">
                    <c:choose>
                        <c:when test="${item.released}">
                            <stripes:link beanclass="${actionBean['class'].name}" event="addDataElement">
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
                <display:column title="Type">
                    <c:if test="${item.type == 'CH1'}">Fixed values</c:if>
                    <c:if test="${item.type == 'CH2'}">Quantitative</c:if>
                    <c:if test="${item.type == 'CH3'}">Vocabulary</c:if>
                </display:column>
                <display:column title="Status">
                    <dd:datasetRegStatus value="${item.status}" />
                    <c:if test="${item.released}">
                        <fmt:setLocale value="en_GB" />
                        <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
                        <sup class="commonelm">${dateFormatted}</sup>
                    </c:if>
                </display:column>
            </display:table>
        </c:if>
    </div>
</c:if>