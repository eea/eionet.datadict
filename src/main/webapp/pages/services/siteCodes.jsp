<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Site codes" currentSection="services">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        (function($) {
            $(document).ready(function() {
                var sampleNames = "site name 1\nsite name 2\nsite name 3\n...";
                var allocated = false;

                // Open allocate site codes dialog
                $("#allocateSiteCodesLink").click(function() {
                    $("#allocateSiteCodesDiv").dialog('open');
                    return false;
                });
                $("#allocateSiteCodesLink2").click(function() {
                    $("#allocateSiteCodesDiv").dialog('open');
                    return false;
                });
                $("#allocateButton").click(function() {
                    // prevents double submits
                    if (!allocated) {
                        allocated = true;
                        return true;
                    }
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
                //show available codes only for all countries
                $("#statusFilter").change(function() {
                    var value = $(this).val();
                    if (value == "AVAILABLE") {
                        $("#countryFilter").val("");
                        $("#countryFilter").prop('disabled', true);
                    } else {
                        $("#countryFilter").prop('disabled', false);
                    }
                });

                //if country is selected hide status: available
                $("#countryFilter").change(function() {
                    var countryValue = $(this).val();
                    if (countryValue != "") {
                        $("#statusFilter option[value='AVAILABLE']").remove();
                    } else {
                        $('#statusFilter option:first').after($('<option />', { "value": 'AVAILABLE', text: 'Available'}));
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

                applySearchToggle("searchSiteCodesForm");
            });
        })(jQuery);
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Site codes</h1>

        <%-- Info text --%>
        <c:if test="${actionBean.userLoggedIn and not actionBean.allocateRight}">
        <div class="system-msg">
            <strong>Note</strong>
            <p>
              You are not authorized to allocate site codes. Please contact responsible NRC or NFP if this is needed.
            </p>
        </div>
        </c:if>
        <p>
        Site code is a unique identifier of site records in the
        <a href="https://dd.eionet.europa.eu/datasets/latest/CDDA">Common database of designated areas (CDDA)</a>
        which is annually updated in one of the EEA's priority dataflows.
        </p>

        <p>
        CDDA is main European inventory of nationally designated protected areas and it provides data for
        <a href="https://www.wdpa.org/">World Database on Protected Areas (WDPA)</a>.
        In order to keep identification of the CDDA site records compatible with the WDPA,
        the EEA agreed to use the site identifier code list maintained by the WDPA.
        Whenever it is needed the WDPA provides free codes from the codelist to the EEA.
        These free codes are then distributed to the individual countries on demand.
        The countries then assign the codes to their new national sites during the update of the CDDA data.
        </p>

        <p>
        In the past the distribution of the free codes, as well as maintenance of the European codelist,
        has been performed manually by the ETC/BD. This service automates the process of the code distribution.
        </p>

        <p>
        Appointed Reportnet users, which are all Eionet <a href="https://www.eionet.europa.eu/ldap-roles/?role_id=eionet-nfp">NFPs</a>
        and <a href="https://www.eionet.europa.eu/ldap-roles/?role_id=eionet-nrc-biodivdata">NRCs for Biodiversity data and information</a>,
        can reserve a set of new site codes for their new sites after logging into the service. The process is called allocation of site codes.
        </p>

        <p>
            Detailed instructions on how to use the site code allocation service can be found in this <a href="<%=request.getContextPath()%>/documentation/service_cdda_sitecode_guide.doc">User guide</a>.
        </p>

        <div class="advice-msg">
            Number of available Site codes in the system: <strong><c:out value="${actionBean.unallocatedSiteCodes}" /></strong>
        </div>

        <c:if test="${empty actionBean.user}">
        <div class="system-msg">
            <strong>Note</strong>
            <p>
            Please <a href="https://sso.eionet.europa.eu/login?service=http%3A%2F%2Fdd.eionet.europa.eu%2Flogin">log-in</a> with your Eionet
            user name and password in order to allocate new site codes or see the list of codes already allocated.
            </p>
        </div>
        </c:if>
        <%--Allocated user countries --%>
        <c:if test="${not empty actionBean.allocations}">
            <c:forEach items="${actionBean.allocations}" var="allocations">
                <div class="important-msg">
                    <strong>Country: ${allocations.country.definition}</strong>
                    <p>Number of allocated, used codes: <strong>${allocations.usedCodes}</strong>
                            <stripes:link beanclass="${actionBean['class'].name}" event="search">
                                <stripes:param name="filter.countryCode" value="${allocations.country.value}" />
                                <stripes:param name="filter.allocatedUsedStatuses" value="true" />
                                See the list
                            </stripes:link>
                    </p>
                    <c:choose>
                        <c:when test="${allocations.unusedCodes > 0}">
                            <p style="color:red">Number of allocated, unused codes: <strong>${allocations.unusedCodes}</strong>
                            <stripes:link beanclass="${actionBean['class'].name}" event="search">
                                <stripes:param name="filter.countryCode" value="${allocations.country.value}" />
                                <stripes:param name="filter.status" value="${actionBean.allocatedStatus}" />
                                See the list
                            </stripes:link>
                            </p>
                            <p style="color:red">Please check the list and consider using the codes before requesting any new ones.</p>
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

        <div id="drop-operations">
            <ul>
                <li class="search open"><a class="searchSection" href="#" title="Search site codes">Search</a></li>
                <c:if test="${actionBean.createRight}">
                    <li class="add"><a href="#" onClick="openPopup('#reserveSiteCodesDialog')">Add new site codes</a></li>
                </c:if>
                <c:if test="${actionBean.allocateRight}">
                    <li class="allocate"><a href="#" id="allocateSiteCodesLink">Allocate site codes</a></li>
                </c:if>
            </ul>
        </div>

        <%-- Site codes search --%>
        <stripes:form method="get" id="searchSiteCodesForm" beanclass="${actionBean['class'].name}">
            <div id="filters">
                <table class="filter">
                    <tr>
                        <td class="label">
                            <label title="Allocated country" for="countryFilter">Country</label>
                        </td>
                        <td class="input">
                            <stripes:select name="filter.countryCode" id="countryFilter">
                                <stripes:option label="All" value="" />
                                <stripes:options-collection collection="${actionBean.countries}" value="value" label="definition" />
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label title="Status" for="statusFilter">Status</label>
                        </td>
                        <td class="input">
                            <stripes:select name="filter.status" id="statusFilter">
                                <stripes:option label="All" value="" />
                                <stripes:options-enumeration enum="eionet.meta.dao.domain.SiteCodeStatus" label="label" />
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label title="Site code number" for="siteCodeFilter">Site code</label>
                        </td>
                        <td class="input">
                            <stripes:text class="smalltext" size="20" name="filter.identifier" id="siteCodeFilter"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label title="Site code name" for="siteNameFilter">Site name</label>
                        </td>
                        <td class="input">
                            <stripes:text class="smalltext" size="30" name="filter.siteName" id="siteNameFilter"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="label" >
                            <label title="Result page size" for="pageSize">Page size</label>
                        </td>
                        <td class="input">
                            <stripes:select id="pageSize" name="filter.pageSize">
                                <stripes:options-collection collection="${actionBean.filter.possibleResultsPerPage}" />
                            </stripes:select>
                        </td>
                    </tr>
                </table>
                <p class="actions">
                    <stripes:submit name="search" value="Search" class="mediumbuttonb searchButton" />
                    <input class="mediumbuttonb" type="reset" value="Reset" />
                </p>
            </div>
        </stripes:form>

        <%-- Site codes table --%>
        <c:if test="${actionBean.context.eventName == 'search'}">
            <%-- Site codes search results and export option --%>
            <stripes:form method="get" id="exportSiteCodesCSV" beanclass="${actionBean['class'].name}" action="/services/siteCodes/exportToCsv">
                <input type="hidden" id="selectedCountryCode" name="selectedCountryCode" value="${actionBean.selectedCountryCode}">
                <input type="hidden" id="selectedStatus" name="selectedStatus" value="${actionBean.selectedStatus}">
                <input type="hidden" id="selectedSiteCode" name="selectedSiteCode" value="${actionBean.selectedSiteCode}">
                <input type="hidden" id="selectedSiteName" name="selectedSiteName" value="${actionBean.selectedSiteName}">

                <display:table name="actionBean.siteCodeResult" class="datatable results" id="siteCode" style="width:100%" requestURI="/services/siteCodes/search">
                    <display:setProperty name="basic.msg.empty_list" value="<p class='not-found'>No site codes found.</p>" />

                    <display:column title="Site code" class="number" sortable="true" sortProperty="identifier">
                        <stripes:link href="/vocabularyconcept/cdda/cddasites/${siteCode.identifier}/view" title="${siteCode.identifier}">
                            <stripes:param name="facet" value="HTML Representation"/> <!-- Discourage people from copy-paste of the link -->
                            <dd:attributeValue attrValue="${siteCode.identifier}"/>
                        </stripes:link>
                    </display:column>
                    <display:column title="Site name" escapeXml="true" property="label" sortable="true" sortProperty="label" />
                    <display:column title="Status" sortable="true" sortProperty="status">
                        <c:out value="${siteCode.siteCodeStatus.label}" />
                    </display:column>
                    <display:column title="Country" escapeXml="true" property="countryCode" sortable="true" sortProperty="cc_iso2" />
                    <display:column title="Allocated" sortable="true" sortProperty="date_allocated">
                        <fmt:formatDate value="${siteCode.dateAllocated}" pattern="yyyy-MM-dd HH:mm:ss" />
                    </display:column>
                    <display:column title="User" escapeXml="true" property="userAllocated" sortable="true" sortProperty="user_allocated" />
                    <c:if test="${actionBean.filter.status == actionBean.allocatedStatus}">
                        <display:column title="Preliminary site name/identifier" escapeXml="true" property="initialSiteName" sortable="true" sortProperty="initial_site_name" />
                    </c:if>
                </display:table>
                <stripes:submit name="export" value="Download all results as: CSV"/>
            </stripes:form>
        </c:if>
        <%-- Site codes allocation popup --%>
        <div id="allocateSiteCodesDiv" title="Allocate site codes">
            <c:choose>
                <c:when test="${actionBean.updateRight ||
                    (actionBean.allocateRightAsCountry && fn:length(actionBean.allocations) > 0 &&
                    actionBean.allocations[0].unusedCodes < actionBean.maxAllocateAmount)}">
                    <div class="system-msg">
                        <strong>Tip</strong>
                        <p>You have the following two options how to allocate new global site codes for your sites.</p>
                    </div>

                    <stripes:form method="post" id="allocateSiteCodesForm" beanclass="${actionBean['class'].name}">
                        <table class="datatable">
                            <colgroup>
                                <col style="width:1%" />
                                <col style="width:1%"/>
                                <col style="width:98%"/>
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
                                <td style="position:relative;">
                                    <div style="position:absolute;top:0px;left:0px;right:0px;bottom:0px;margin-top:8px">
                                        <stripes:radio name="choice" value="amount" id="choiceAmount" checked="checked"/>
                                    </div>
                                </td>
                                <td class="simple_attr_title" title="Number of site codes to allocate" colspan="2">
                                    <label for="choiceAmount">Enter the number of new site codes and press OK button</label>
                                    <stripes:text class="smalltext" size="5" name="amount" id="amountText"/>
                                    <c:if test="${actionBean.allocateRightAsCountry && fn:length(actionBean.allocations) > 0}">
                                        <c:choose>
                                            <c:when test="${actionBean.allocations[0].unusedCodesWithoutSiteNames < actionBean.maxAllocateAmountWithoutNames}">
                                                <p style="margin-bottom:0px;">Allocation limit for this option is <c:out value="${actionBean.maxAllocateAmountWithoutNames}"/> codes.
                                                You can still allocate up to <span style="color:red;">
                                                <c:choose>
                                                    <c:when test="${(actionBean.maxAllocateAmount - actionBean.allocations[0].unusedCodes) < (actionBean.maxAllocateAmountWithoutNames - actionBean.allocations[0].unusedCodesWithoutSiteNames)}">
                                                        <c:out value="${actionBean.maxAllocateAmount - actionBean.allocations[0].unusedCodes}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${actionBean.maxAllocateAmountWithoutNames - actionBean.allocations[0].unusedCodesWithoutSiteNames}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                                </span>
                                                site codes by this option. Please use the second option if you need more.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <p style="margin-bottom:0px;">This option can’t be used! Its allocation limit is <c:out value="${actionBean.maxAllocateAmountWithoutNames}"/> codes.
                                                <span style="color:red;"><c:out value="${actionBean.allocations[0].unusedCodesWithoutSiteNames}"/></span>
                                                site codes have already been allocated by this option. Please use the second option if you need to allocate more.</p>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </td>
                            </tr>
                            <tr><td colspan="4" style="padding-left: 10%;">Or</td></tr>
                            <tr>
                                <td style="position:relative;min-width:20px">
                                    <div style="position:absolute;top:0px;left:0px;right:0px;bottom:0px;margin-top:3px;">
                                        <stripes:radio name="choice" value="label" id="choiceLabel"/>
                                    </div>
                                </td>
                                <td class="simple_attr_title" title="List of new site code names separated by new line" colspan="2">
                                    <label for="choiceLabel">Copy a list of new sites (their names, national codes or other identifiers
                                    of your choice, or their combinations - anything that help you to remember which sites you have
                                    allocated the codes for) and paste them into the area below. Please assure that each site occupy one line.
                                    The result will be displayed for your reference and sent to you by email.</label>
                                    <c:if test="${actionBean.allocateRightAsCountry && fn:length(actionBean.allocations) > 0}">
                                        <p>Limit for self-made allocations is <c:out value="${actionBean.maxAllocateAmount}"/> codes.
                                        You can still allocate up to <span style="color:red;"><c:out value="${actionBean.maxAllocateAmount - actionBean.allocations[0].unusedCodes}"/></span>
                                        site codes. Please contact <a href="mailto:cdda.helpdesk@eionet.europa.eu">cdda.helpdesk@eionet.europa.eu</a> if you need more.</p>
                                    </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td class="simple_attr_value" colspan="2">
                                    <stripes:textarea class="smalltext" name="labels" id="labelsText" rows="7" cols="70"/>
                                </td>
                            </tr>
                            <tr>
                                <td >&nbsp;</td>
                                <td colspan="2" style="text-align:center">
                                    <stripes:submit name="allocate" value="OK" id="allocateButton" class="siteCodeOkButton" />
                                    <button type="button" id="closeAllocateLink">Cancel</button>
                                </td>
                            </tr>
                        </table>
                    </stripes:form>
                </c:when>
                <c:when test="${actionBean.allocateRightAsCountry && fn:length(actionBean.allocations) > 0 &&
                    actionBean.allocations[0].unusedCodes >= actionBean.maxAllocateAmount}">
                    <div class="warning-msg">
                        <strong>Warning</strong>
                        <p>You have already allocated <strong><c:out value="${actionBean.allocations[0].unusedCodes}"/></strong> site codes.
                        You can’t allocate more by yourself.
                        If you need more codes please contact <a href="mailto:cdda.helpdesk@eionet.europa.eu">cdda.helpdesk@eionet.europa.eu</a>
                        with explanation. </p>
                    </div>
                    <button type="button" id="closeAllocateLink" style="margin-left:15em;">OK</button>
                </c:when>
                <c:otherwise></c:otherwise>
            </c:choose>
        </div>

        <%-- Reserve site codes range --%>
        <div id="reserveSiteCodesDialog"  title="Add new site codes">
            <stripes:form method="post" id="reserveFreeSiteCodesForm" beanclass="${actionBean['class'].name}">
                <stripes:hidden name="siteCodeFolderId" />

                <div class="system-msg">
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
                            <stripes:submit name="reserveNewSiteCodes" value="OK" class="siteCodeOkButton"/>
                            <button type="button" onClick="closePopup('#reserveSiteCodesDialog')">Cancel</button>
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
