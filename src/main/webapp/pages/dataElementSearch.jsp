<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search data elements" currentSection="dataElements">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[

            /*
            * Adds hidden parameter "delAttr" to form and submits it, so the attribute field gets removed after the submit.
            */
            function deleteAttribute(id) {
                var input = document.createElement("input");
                input.setAttribute("type", "hidden");
                input.setAttribute("name", "delAttr");
                input.setAttribute("value", id);

                document.getElementById("searchForm").appendChild(input);
                document.getElementById("searchForm").submit();
            }

            (function($) {
                $(document).ready(function() {
                    // Checks if common/non-common element radio button is selected and disables/enambles input fields accordingly.
                    var toggleDisabledFields = function() {
                        var nonCommonRadio = document.getElementById("nonCommonRadio");
                        var commonRadio = document.getElementById("commonRadio");
                        var regStatusSelect = document.getElementById("regStatusSelect");
                        var datasetSelect = document.getElementById("datasetSelect");

                        if (nonCommonRadio.checked) {
                            regStatusSelect.disable();
                            datasetSelect.enable();
                            regStatusSelect.value = "";
                        }

                        if (commonRadio.checked) {
                            regStatusSelect.enable();
                            datasetSelect.disable();
                            datasetSelect.value = "";
                        }
                    }

                    toggleDisabledFields();

                    $("#nonCommonRadio").click(function() {
                        toggleDisabledFields();
                    });

                    $("#commonRadio").click(function() {
                        toggleDisabledFields();
                    });

                    applySearchToggle("searchForm");
                });
            })(jQuery);
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
    <h1>Data elements</h1>
    <c:if test="${empty actionBean.user}">
        <p class="advise-msg">
            Note: Elements from datasets NOT in <em>Recorded</em> or <em>Released</em> status are inaccessible for anonymous users.
        </p>
    </c:if>

    <div id="drop-operations">
        <ul>
            <li class="search open"><a class="searchSection" href="#" title="Search data elements">Search</a></li>
            <c:if test="${actionBean.permissionToAdd}">
                <li class="new">
                    <stripes:link href="/dataelements/add/">
                        <stripes:param name="common" value="true" />
                        New common element
                    </stripes:link>
                </li>
            </c:if>
        </ul>
    </div>

    <stripes:form id="searchForm" beanclass="eionet.web.action.SearchDataElementsActionBean" method="get">
        <div id="filters">
            <table class="filter">
                <tr>
                    <td class="label">
                        <label for="regStatusSelect">Registration status</label>
                        <a class="helpButton" href="help.jsp?screen=dataset&area=regstatus"></a>
                    </td>
                    <td class="input">
                        <stripes:select id="regStatusSelect" name="filter.regStatus" class="small">
                            <stripes:option value="" label="All" />
                            <stripes:options-collection collection="${actionBean.regStatuses}" />
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="datasetSelect">Dataset</label>
                        <a class="helpButton" href="help.jsp?screen=search_element&amp;area=dataset"></a>
                    </td>
                    <td class="input">
                        <stripes:select id="datasetSelect" name="filter.dataSet" class="small">
                            <stripes:option value="" label="All" />
                            <stripes:options-collection collection="${actionBean.dataSets}" value="identifier" label="shortName" />
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="typeSelect">Type</label>
                        <a class="helpButton" href="help.jsp?screen=element&amp;area=type"></a>
                    </td>
                    <td class="input">
                        <stripes:select name="filter.type" class="small" id="typeSelect">
                            <stripes:option value="" label="All" />
                            <stripes:option value="CH1" label="Data element with fixed values (codes)" />
                            <stripes:option value="CH2" label="Data element with quantitative values (e.g. measurements)" />
                            <stripes:option value="CH3" label="Data element with fixed values (vocabulary)" />
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="txtShortName">Short name</label>
                        <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name"></a>
                    </td>
                    <td class="input">
                        <stripes:text name="filter.shortName" class="smalltext" size="59" id="txtShortName" />
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="txtIdentifier">Identifier</label>
                        <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier"></a>
                    </td>
                    <td class="input">
                        <stripes:text name="filter.identifier" class="smalltext" size="59" id="txtIdentifier" />
                    </td>
                </tr>
                <c:forEach items="${actionBean.filter.defaultAttributes}" var="attr" varStatus="row">
                    <tr>
                        <td class="label">
                            <label for="txtFilterAttr_${attr.id}_${row.index}"><c:out value="${attr.shortName}" /></label>
                            <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <stripes:hidden name="filter.defaultAttributes[${row.index}].id" />
                            <stripes:hidden name="filter.defaultAttributes[${row.index}].name" />
                            <stripes:hidden name="filter.defaultAttributes[${row.index}].shortName" />
                            <stripes:text name="filter.defaultAttributes[${row.index}].value" class="smalltext" size="59" id="txtFilterAttr_${attr.id}_${row.index}" />
                        </td>
                    </tr>
                </c:forEach>
                <c:forEach items="${actionBean.filter.addedAttributes}" var="attr" varStatus="row">
                    <tr>
                        <td class="label">
                            <label for="txtAddedAttr_${attr.id}_${row.index}"><c:out value="${attr.name}" /></label>
                            <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <stripes:hidden name="filter.addedAttributes[${row.index}].id" />
                            <stripes:hidden name="filter.addedAttributes[${row.index}].name" />
                            <stripes:hidden name="filter.addedAttributes[${row.index}].shortName" />
                            <stripes:text name="filter.addedAttributes[${row.index}].value" class="smalltext" size="59" id="txtAddedAttr_${attr.id}_${row.index}" />
                            <a class="deleteButton" href="#" onclick="deleteAttribute(${attr.id})" title="Remove attribute from search criteria" /></a>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <td></td>
                    <td class="input">
                        <stripes:select name="addAttr" class="small" onchange="this.form.submit();" >
                            <stripes:option value="0" label="Add criteria" />
                            <stripes:options-collection collection="${actionBean.addableAttributes}" label="name" value="id" />
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="label">Class</td>
                    <td class="input bordered">
                        <stripes:radio id="nonCommonRadio" name="filter.elementType" value="${actionBean.filter.nonCommonElementType}" checked="${actionBean.filter.nonCommonElementType}" /><label for="nonCommonRadio">Non-common elements</label>
                        <stripes:radio id="commonRadio" name="filter.elementType" value="${actionBean.filter.commonElementType}" /><label for="commonRadio">Common elements</label>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="chkInclHistoricVersions">Include historic versions</label>
                    </td>
                    <td class="input bordered">
                        <stripes:checkbox name="filter.includeHistoricVersions" id="chkInclHistoricVersions"/>
                        <label for="chkInclHistoricVersions" class="smallfont">Yes</label>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="pageSize">Page size</label>
                    </td>
                    <td class="input">
                        <stripes:select id="pageSize" name="filter.pageSize" class="small">
                            <stripes:options-collection collection="${actionBean.filter.possibleResultsPerPage}" />
                        </stripes:select>
                    </td>
                </tr>
            </table>
            <p class="actions">
                <stripes:submit class="mediumbuttonb searchButton" name="search" value="Search"/>
                <input class="mediumbuttonb" type="reset" value="Reset" />  
            </p>
        </div>
    </stripes:form>

    <display:table name="${actionBean.result}" class="datatable results" id="item" requestURI="/searchelements">
        <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No data elements found.</p>" />
        <display:column title="Element" sortable="true" sortProperty="IDENTIFIER">
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
        <display:column title="Type" sortable="true" sortProperty="TYPE">
            <c:if test="${item.type == 'CH1'}">Fixed values (code list)</c:if>
            <c:if test="${item.type == 'CH2'}">Quantitative</c:if>
            <c:if test="${item.type == 'CH3'}">Fixed Values (vocabulary)</c:if>
        </display:column>
        <c:if test="${actionBean.filter.elementType=='nonCommon'}">
            <c:set var="statusLabel" value="Dataset status" />
            <display:column property="tableName" title="Table" sortable="true" sortProperty="TABLE_NAME" />
            <display:column property="dataSetName" title="Dataset" sortable="true" sortProperty="DATASET_NAME" />
        </c:if>
        <c:if test="${actionBean.filter.elementType=='common'}">
            <c:set var="statusLabel" value="Status" />
        </c:if>
        <display:column title="${statusLabel}" sortable="true" sortProperty="STATUS">
            <dd:datasetRegStatus value="${item.status}" />

            <c:if test="${item.released}">
                <fmt:setLocale value="en_GB" />
                <fmt:formatDate pattern="dd MMM yyyy" value="${item.modified}" var="dateFormatted"/>
                <sup class="commonelm">${dateFormatted}</sup>
            </c:if>
        </display:column>
         <display:column title="Name" sortable="true" sortProperty="NAME">
            ${item.name}
        </display:column>
    </display:table>

    </stripes:layout-component>

</stripes:layout-render>