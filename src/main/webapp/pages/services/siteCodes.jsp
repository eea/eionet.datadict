<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Site codes">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

                var sampleNames = "site name 1\nsite name 2\nsite name 3\n...";

                // Open allocate site codes dialog
                $("#allocateSiteCodesLink").click(function() {
                    $("#allocateSiteCodesDiv").dialog('open');
                    return false;
                });
                $("#allocateSiteCodesLink2").click(function() {
                    $("#allocateSiteCodesDiv").dialog('open');
                    return false;
                });

                // Close allocate site codes dialog
                $("#closeAllocateLink").click(function() {
                    $("#allocateSiteCodesDiv").dialog('close');
                    return false;
                });

                // Allocate site codes dialog setup
                $("#allocateSiteCodesDiv").dialog({
                    autoOpen: false,
                    width: 800
                });

                // Reserve site codes dialog
                $("#reserveSiteCodesDialog").dialog({
                    autoOpen: false,
                    width: 600
                });

                // Doggle fields based on the radio button
                $("input:radio[name=choice]").click(function() {
                    var value = $(this).val();
                    if (value == "amount") {
                        $("#amountText").prop('disabled', false);
                        $("#labelsText").prop('disabled', true);
                        $("#labelsText").val(sampleNames);
                    } else {
                        $("#amountText").prop('disabled', true);
                        $("#labelsText").prop('disabled', false);
                        $("#labelsText").val("");
                    }
                });

                // Function for opening popups
                openPopup = function(divId) {
                    $(divId).dialog('open');
                    return false;
                }

                // Function for closing popups
                closePopup = function(divId) {
                    $(divId).dialog("close");
                    return false;
                }


                // First disable both fields
                var disableAmountAndLabelsInput = new function() {
                    var value = $("input:radio[name=choice]:checked").val();

                    if (value == "amount") {
                        $("#amountText").prop('disabled', false);
                        $("#labelsText").prop('disabled', true);
                        $("#labelsText").val(sampleNames);
                    } else if (value == "labels") {
                        $("#amountText").prop('disabled', true);
                        $("#labelsText").prop('disabled', false);
                        $("#labelsText").val("");
                    } else {
                        $("#amountText").prop('disabled', true);
                        $("#labelsText").prop('disabled', true);
                        $("#labelsText").val(sampleNames);
                    }
                }

                disableAmountAndLabelsInput();
            });

        } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <c:if test="${actionBean.allocateRight || actionBean.createRight}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <c:if test="${actionBean.createRight}">
                    <li><a href="#" onClick="openPopup('#reserveSiteCodesDialog')">Add new site codes</a></li>
                </c:if>
                <c:if test="${actionBean.allocateRight}">
                    <li><a href="#" id="allocateSiteCodesLink">Allocate site codes</a></li>
                </c:if>
            </ul>
        </div>
        </c:if>

        <h1>Site codes</h1>

        <%-- Info text --%>
        <c:if test="${actionBean.context.eventName == 'view'}">

            <p>
            Site code is a unique identifier of site records in the
            <a href=http://dd.eionet.europa.eu/datasets/latest/CDDA>Common database of designated areas</a>
            which is annually updated in one of the EEA's priority dataflows.
            </p>

            <p>
            CDDA, as the main European inventory of protected areas, provides the data for
            <a href="http://www.wdpa.org/">World Database on Protected Areas (WDPA)</a>.
            In order to keep identification of the CDDA site records compatible with the WDPA,
            the EEA agreed to use for the identification the code list maintained by the WDPA.
            Whenever it is needed the WDPA provides free codes from the codelist to the EEA.
            These free codes are then distributed on demand to the individual countries who assign
            the codes to new national sites during the update of the CDDA data.
            </p>

            <p>
            In the past the distribution of the free codes as well as maintenance of the European codelists
            has been performed manually by the ETC/BD. This service automatizes the process of the code distribution.
            </p>

            <p>
            Appointed Reportnet users, which are all Eionet <a href="http://www.eionet.europa.eu/ldap-roles/?role_id=eionet-nfp">NFPs</a>
            and <a href="http://www.eionet.europa.eu/ldap-roles/?role_id=eionet-nrc-nature">NRCs for Nature and Biodiversity</a>,
            can reserve a set of new site codes for their new sites after logging into the service. The process is called allocation of site codes.
            </p>

            <div class="advice-msg">
                Number of available Site codes in the system: <strong><c:out value="${actionBean.unallocatedSiteCodes}" /></strong>
            </div>

            <c:if test="${empty actionBean.user}">
            <div class="note-msg">
                <strong>Note</strong>
                <p>
                Please <a href="https://sso.eionet.europa.eu/login?service=http%3A%2F%2Fdd.eionet.europa.eu%2Flogin">log-in</a> with your Eionet
                user name and password in order to allocate new site codes or see the list of codes already allocated.
                </p>
            </div>
            </c:if>
        </c:if>
        <%--Allocated user countries --%>
        <c:if test="${not empty actionBean.allocations}">
            <c:forEach items="${actionBean.allocations}" var="allocations">
                <div class="important-msg">
                    <strong>Country: ${allocations.country.definition}</strong>
                    <p>Number of allocated, used codes: <strong>${allocations.usedCodes}</strong>
                            <stripes:link beanclass="${actionBean.class.name}" event="search">
                                <stripes:param name="filter.countryCode" value="${allocations.country.value}" />
                                <stripes:param name="filter.allocatedUsedStatuses" value="true" />
                                See the list
                            </stripes:link>
                    </p>
                    <c:choose>
                        <c:when test="${allocations.unusedCodes > 0}">
                            <p style="color:red">Number of allocated, unused codes: <strong>${allocations.unusedCodes}</strong>
                            <stripes:link beanclass="${actionBean.class.name}" event="search">
                                <stripes:param name="filter.countryCode" value="${allocations.country.value}" />
                                <stripes:param name="filter.status" value="${actionBean.allocatedStatus}" />
                                See the list
                            </stripes:link>
                            </p>
                            <p>Please check the list and consider using the codes before requesting any new ones.</p>
                        </c:when>
                        <c:otherwise>
                            <p>Number of allocated, unused codes: <strong>${allocations.unusedCodes}</strong>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${actionBean.allocateRight}">
                        <p><input type="button" name="allocateCodes" value="Allocate site codes"  id="allocateSiteCodesLink2"/></p>
                    </c:if>
                </div>
            </c:forEach>
        </c:if>

        <%-- Site codes search --%>
        <stripes:form method="get" id="searchSiteCodesForm" beanclass="${actionBean.class.name}">
            <h2>Search site codes</h2>
            <table class="datatable">
                <colgroup>
                    <col style="width:30 em;"/>
                    <col />
                </colgroup>
                <tr>
                    <th title="Allocated country">
                        <label for="countryFilter">Country</label>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:select name="filter.countryCode" id="countryFilter">
                            <stripes:option label="All" value="" />
                            <stripes:options-collection collection="${actionBean.countries}" value="value" label="definition" />
                        </stripes:select>
                    </td>
                    <th title="Allocated country">
                        <label for="statusFilter">Status</label>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:select name="filter.status" id="statusFilter">
                            <stripes:option label="All" value="" />
                            <c:choose>
                                <c:when test="${not empty actionBean.user}">
                                    <stripes:options-enumeration enum="eionet.meta.dao.domain.SiteCodeStatus" label="label" />
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${actionBean.publicStatuses}" var="status">
                                        <stripes:option label="${status.label}" value="${status}"/>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th class="simple_attr_title" title="Site code number">
                        <label for="siteCodeFilter">Site code</label>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="20" name="filter.identifier" id="siteCodeFilter"/>
                    </thd>
                    <th class="simple_attr_title" title="Site code name">
                        <label for="siteNameFilter">Site name</label>
                    </th>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="filter.siteName"  id="siteNameFilter"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <stripes:submit name="search" value="Search" />
                    </td>
                </tr>
           </table>
        </stripes:form>


        <%-- Site codes table --%>
        <c:if test="${actionBean.context.eventName == 'search'}">
        <%--
        <c:if test="${actionBean.siteCodeResult.totalItems > 0}">
            <stripes:link beanclass="${actionBean.class.name}" event="exportCsv">
                <stripes:param name="userAllocated" value="${actionBean.userAllocated}" />
                <stripes:param name="dateAllocated" value="${actionBean.dateAllocated}" />
                Export CSV
            </stripes:link>
            <br />
        </c:if>
        --%>
        <display:table name="actionBean.siteCodeResult" class="datatable" id="siteCode" style="width:80%"
        requestURI="/services/siteCodes/search" export="true">
            <display:setProperty name="basic.msg.empty_list" value="No site codes found." />

            <display:column title="Site code" property="identifier" escapeXml="true" class="number" style="width: 1%" />
            <display:column title="Site name" escapeXml="true" property="label" />
            <display:column title="Status">
                <c:out value="${siteCode.status.label}" />
            </display:column>
            <display:column title="Country" escapeXml="true" property="countryCode" />
            <display:column title="Allocated">
                <fmt:formatDate value="${siteCode.dateAllocated}" pattern="yyyy-MM-dd HH:mm:ss" />
            </display:column>
            <display:column title="User" escapeXml="true" property="userAllocated" />
            <c:if test="${actionBean.filter.status == actionBean.allocatedStatus}">
                <display:column title="Preliminary site name/identifier" escapeXml="true" property="initialSiteName" />
            </c:if>
            <display:setProperty name="export.excel.filename" value="CDDASiteCodes.xls"/>
            <display:setProperty name="export.excel" value="false" />
            <display:setProperty name="export.csv.filename" value="CDDASiteCodes.csv"/>
            <display:setProperty name="export.csv" value="true" />
            <display:setProperty name="export.xml" value="false" />
        </display:table>
        </c:if>
        <%-- Site codes allocation popup --%>
        <div id="allocateSiteCodesDiv" title="Allocate site codes">
            <div class="tip-msg">
                <strong>Tip</strong>
                <p>You have the following two options how to allocate new global site codes for your sites.</p>
            </div>

            <stripes:form method="post" id="allocateSiteCodesForm" beanclass="${actionBean.class.name}">
                <table class="datatable">
                    <colgroup>
                        <col style="width:1%" />
                        <col style="width:26%"/>
                        <col />
                    </colgroup>
                    <tr>
                        <td>&nbsp;</td>
                        <td class="simple_attr_title" title="Country to allocate to">
                            <label for="country">Country</label>
                        </td>
                        <td class="simple_attr_value">
                            <stripes:select name="country" id="country">
                                <stripes:options-collection collection="${actionBean.userCountries}" value="value" label="definition" />
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><stripes:radio name="choice" value="amount" id="choiceAmount" checked="checked"/></td>
                        <td class="simple_attr_title" title="Number of site codes to allocate" colspan="2">
                            <label for="choiceAmount">Enter the number of new site codes and press OK button</label>
                            <stripes:text class="smalltext" size="5" name="amount" id="amountText"/>
                        </td>
                    </tr>
                    <tr><td colspan="4" style="padding-left: 10%">Or</td></tr>
                    <tr>
                        <td><stripes:radio name="choice" value="label" id="choiceLabel"/></td>
                        <td class="simple_attr_title" title="List of new site code names separated by new line" colspan="2">
                            <label for="choiceLabel">Copy a list of new sites (their names, national codes or other identifiers
                             of your choice, or their combinations - anything that help you to remember which sites you have
                             allocated the codes for) and paste them into the area below. Please assure that each site occupy one line.
                             The result will be displayed for your reference and sent to you by email.</label>
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td class="simple_attr_value" colspan="2">
                            <stripes:textarea class="smalltext" name="labels" id="labelsText" rows="7" cols="70"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td>
                            <stripes:submit name="allocate" value="OK" />
                            <button type="button" id="closeAllocateLink">Cancel</button>
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </div>

        <%-- Reserve site codes range --%>
        <div id="reserveSiteCodesDialog"  title="Add new site codes">
            <stripes:form method="post" id="reserveFreeSiteCodesForm" beanclass="${actionBean.class.name}">
                <stripes:hidden name="siteCodeFolderId" />

                <div class="tip-msg">
                    <strong>Tip</strong>
                    <p>Insert the range of new site codes. All the new site codes falling inclusively between range start and range end will be availabel for countries to be allocated.
                    </p>
                </div>
                <table class="datatable">
                    <colgroup>
                        <col style="width:26%"/>
                        <col style="width:4%"/>
                        <col />
                    </colgroup>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title" title="Lowest value of the inserted unallocated site codes">
                            <label for="rangeStart">Range start</label>
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="startIdentifier" id="rangeStart" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title" title="Highest value of the inserted unallocated site codes">
                            <label for="rangeEnd">Range end</label>
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="30" name="endIdentifier" id="rangeEnd" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td>
                            <stripes:submit name="reserveNewSiteCodes" value="OK" />
                            <button type="button" onClick="closePopup('#reserveSiteCodesDialog')">Cancel</button>
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>