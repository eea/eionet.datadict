<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search data elements" currentSection="dataElements">

    <stripes:layout-component name="head">
        <script type="text/javascript" src="<%=request.getContextPath()%>/helpPopup.js"></script>
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

        ( function($) {
            $(document).ready(function(){

                /*
                 * Checks if common/non-common element radio button is selected and disables/enambles input fields accordingly.
                 */
                 var toggleDisabledFields = function() {
                     var nonCommonRadio = document.getElementById("nonCommonRadio");
                     var commonRadio = document.getElementById("commonRadio");
                     var regStatusSelect = document.getElementById("regStatusSelect");
                     var datasetSelect = document.getElementById("datasetSelect");

                     if (nonCommonRadio.checked) {
                         regStatusSelect.disable();
                         datasetSelect.enable();
                     }

                     if (commonRadio.checked) {
                         regStatusSelect.enable();
                         datasetSelect.disable();
                     }

                     regStatusSelect.value="";
                     datasetSelect.value="";
                 }

                toggleDisabledFields();

                $("#nonCommonRadio").click(function() {
                    toggleDisabledFields();
                });

                $("#commonRadio").click(function() {
                    toggleDisabledFields();
                });

            });
        } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
    <h1>Search data elements</h1>

    <c:if test="${actionBean.permissionToAdd}">
        <div id="drop-operations">
            <ul>
                <li class="new">
                    <stripes:link href="/dataelements/add/">
                        <stripes:param name="common" value="true" />
                        New common element
                    </stripes:link>
                </li>
            </ul>
        </div>
    </c:if>

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
                            <stripes:options-collection collection="${actionBean.regStatuses}"/>
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
                        <stripes:text name="filter.shortName" class="smalltext" size="59" id="txtShortName"/>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="txtIdentifier">Identifier</label>
                        <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier"></a>
                    </td>
                    <td class="input">
                        <stripes:text name="filter.identifier" class="smalltext" size="59" id="txtIdentifier"/>
                    </td>
                </tr>
                <c:forEach items="${actionBean.filter.attributes}" var="attr" varStatus="row">
                    <tr>
                        <td class="label">
                            <label for="txtFilterAttr_${attr.id}_${row.index}"><c:out value="${attr.shortName}" /></label>
                            <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <stripes:hidden name="filter.attributes[${row.index}].id" />
                            <stripes:hidden name="filter.attributes[${row.index}].name" />
                            <stripes:hidden name="filter.attributes[${row.index}].shortName" />
                            <stripes:text name="filter.attributes[${row.index}].value" class="smalltext" size="59" id="txtFilterAttr_${attr.id}_${row.index}"/>
                        </td>
                    </tr>
                </c:forEach>
                <c:forEach items="${actionBean.addedAttributes}" var="attr" varStatus="row">
                    <tr>
                        <td class="label">
                            <label for="txtAddedAttr_${attr.id}_${row.index}"><c:out value="${attr.name}" /></label>
                            <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                        </td>
                        <td class="input">
                            <stripes:hidden name="addedAttributes[${row.index}].id" />
                            <stripes:hidden name="addedAttributes[${row.index}].name" />
                            <stripes:hidden name="addedAttributes[${row.index}].shortName" />
                            <stripes:text name="addedAttributes[${row.index}].value" class="smalltext" size="59" id="txtAddedAttr_${attr.id}_${row.index}"/>
                            <a class="deleteButton" href="#" onclick="deleteAttribute(${attr.id})" title="Remove attribute from search criteria" /></a>
                        </td>
                    </tr>
                </c:forEach>
                <tr>
                    <td></td>
                    <td class="input">
                        <stripes:select name="addAttr" class="small" onchange="this.form.submit();" >
                            <stripes:option value="0" label="Add criteria" />
                            <stripes:options-collection collection="${actionBean.addableAttributes}" label="name" value="id"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="label">Class</td>
                    <td class="input">
                        <stripes:radio id="nonCommonRadio" name="filter.elementType" value="${actionBean.filter.nonCommonElementType}" checked="${actionBean.filter.nonCommonElementType}" /><label for="nonCommonRadio">Non-common elements</label>
                        <stripes:radio id="commonRadio" name="filter.elementType" value="${actionBean.filter.commonElementType}" /><label for="commonRadio">Common elements</label>
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        <label for="chkInclHistoricVersions">Include historic versions</label>
                    </td>
                    <td class="input">
                        <stripes:checkbox name="filter.includeHistoricVersions" id="chkInclHistoricVersions"/>
                        <label for="chkInclHistoricVersions" class="smallfont">Yes</label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <stripes:submit class="mediumbuttonb searchButton" name="search" value="Search"/>
                        <input class="mediumbuttonb" type="reset" value="Reset" />
                    </td>
                </tr>
            </table>
        </div>
    </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>