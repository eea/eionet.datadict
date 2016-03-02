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
            <h2>Operations:</h2>
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
        <table width="auto" cellspacing="0" style="clear:right">
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <label for="regStatusSelect" class="question">Registration status</label>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&area=regstatus"></a>
                </td>
                <td colspan="2">
                    <stripes:select id="regStatusSelect" name="filter.regStatus" class="small">
                        <stripes:option value="" label="All" />
                        <stripes:options-collection collection="${actionBean.regStatuses}"/>
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <label for="datasetSelect" class="question">Dataset</label>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=search_element&amp;area=dataset"></a>
                </td>
                <td colspan="2">
                    <stripes:select id="datasetSelect" name="filter.dataSet" class="small">
                        <stripes:option value="" label="All" />
                        <stripes:options-collection collection="${actionBean.dataSets}" value="identifier" label="shortName" />
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <label for="typeSelect" class="question">Type</label>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=element&amp;area=type"></a>
                </td>
                <td colspan="2">
                    <stripes:select name="filter.type" class="small" id="typeSelect">
                        <stripes:option value="" label="All" />
                        <stripes:option value="CH1" label="Data element with fixed values (codes)" />
                        <stripes:option value="CH2" label="Data element with quantitative values (e.g. measurements)" />
                        <stripes:option value="CH3" label="Data element with fixed values (vocabulary)" />
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <label for="txtShortName" class="question">Short name</label>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name"></a>
                </td>
                <td colspan="2">
                    <stripes:text name="filter.shortName" class="smalltext" size="59" id="txtShortName"/>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <label for="txtIdentifier" class="question">Identifier</label>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&amp;area=identifier"></a>
                </td>
                <td colspan="2">
                    <stripes:text name="filter.identifier" class="smalltext" size="59" id="txtIdentifier"/>
                </td>
            </tr>
            <c:forEach items="${actionBean.filter.attributes}" var="attr" varStatus="row">
                <tr valign="top">
                    <td align="right" style="padding-right:10">
                        <label for="txtFilterAttr_${attr.id}_${row.index}" class="question"><c:out value="${attr.shortName}" /></label>
                    </td>
                    <td>
                        <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                    </td>
                    <td colspan="2">
                        <stripes:hidden name="filter.attributes[${row.index}].id" />
                        <stripes:hidden name="filter.attributes[${row.index}].name" />
                        <stripes:hidden name="filter.attributes[${row.index}].shortName" />
                        <stripes:text name="filter.attributes[${row.index}].value" class="smalltext" size="59" id="txtFilterAttr_${attr.id}_${row.index}"/>
                    </td>
                </tr>
            </c:forEach>
            <c:forEach items="${actionBean.addedAttributes}" var="attr" varStatus="row">
                <tr valign="top">
                    <td align="right" style="padding-right:10">
                        <label for="txtAddedAttr_${attr.id}_${row.index}" class="question"><c:out value="${attr.name}" /></label>
                    </td>
                    <td>
                        <a class="helpButton" href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE"></a>
                    </td>
                    <td colspan="2">
                        <stripes:hidden name="addedAttributes[${row.index}].id" />
                        <stripes:hidden name="addedAttributes[${row.index}].name" />
                        <stripes:hidden name="addedAttributes[${row.index}].shortName" />
                        <stripes:text name="addedAttributes[${row.index}].value" class="smalltext" size="59" id="txtAddedAttr_${attr.id}_${row.index}"/>
                        <a class="deleteButton" href="#" onclick="deleteAttribute(${attr.id})" title="Remove attribute from search criteria" /></a>
                    </td>
                </tr>
            </c:forEach>
            <tr valign="top">
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    <stripes:radio id="nonCommonRadio" name="filter.elementType" value="${actionBean.filter.nonCommonElementType}" checked="${actionBean.filter.nonCommonElementType}" /><label for="nonCommonRadio" class="question">Non-common elements</label>
                    <stripes:radio id="commonRadio" name="filter.elementType" value="${actionBean.filter.commonElementType}" /><label for="commonRadio" class="question">Common elements</label>
                </td>
            </tr>
            <tr valign="top">
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    <stripes:checkbox name="filter.includeHistoricVersions" id="chkInclHistoricVersions"/><label for="chkInclHistoricVersions" class="question">Include historic versions</label>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    Add criteria:
                    <stripes:select name="addAttr" class="small" onchange="this.form.submit();" >
                        <stripes:option value="0" label="" />
                        <stripes:options-collection collection="${actionBean.addableAttributes}" label="name" value="id"/>
                    </stripes:select>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
                <td colspan="2"><br /><stripes:submit name="search" value="Search"/></td>
            </tr>
        </table>
    </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>