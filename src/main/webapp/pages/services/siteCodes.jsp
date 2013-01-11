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

        <c:if test="${actionBean.allocationRight}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="#" id="allocateSiteCodesLink">Allocate site codes</a></li>
            </ul>
        </div>
        </c:if>

        <h1>Site codes</h1>

        <%-- Site codes search --%>
        <stripes:form method="get" id="searchSiteCodesForm" beanclass="${actionBean.class.name}">
            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col />
                </colgroup>
                <tr>
                    <td class="simple_attr_title" title="Allocated country">
                        Country
                    </td>
                    <td class="simple_attr_value">
                        <stripes:select name="filter.countryCode">
                            <stripes:option label="All" value="" />
                            <stripes:options-collection collection="${actionBean.countries}" value="value" label="definition" />
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td class="simple_attr_title" title="Allocated country">
                        Status
                    </td>
                    <td class="simple_attr_value">
                        <stripes:select name="filter.status">
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
                    <td class="simple_attr_title" title="Allocated country">
                        Site code name
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="filter.siteName" />
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
        <display:table name="actionBean.siteCodeResult" class="datatable" id="siteCode" style="width:80%" requestURI="/services/siteCodes" >
            <display:setProperty name="basic.msg.empty_list" value="No site codes found." />

            <display:column title="Site code" property="identifier" escapeXml="true" class="number" style="width: 1%" />
            <display:column title="Site name" escapeXml="true" property="label" />
            <display:column title="Status" property="status" />
            <display:column title="Country" escapeXml="true" property="countryCode" />
            <display:column title="Allocated" escapeXml="true" property="dateAllocated" />
            <display:column title="User" escapeXml="true" property="userAllocated" />
        </display:table>

        <%-- Site codes allocation popup --%>
        <div id="allocateSiteCodesDiv" title="Allocate site codes">
            <div class="tip-msg">
                <strong>Tip</strong>
                <p>Site codes can be allocated by number or by list of site names where each name is on a new line.</p>
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
                        <td class="simple_attr_title" title="Number of site codes to allocate">
                            <label for="choiceAmount">Number of site codes</label>
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="5" name="amount" id="amountText"/>
                        </td>
                    </tr>
                    <tr><td colspan="4" style="padding-left: 10%">Or</td></tr>
                    <tr>
                        <td><stripes:radio name="choice" value="label" id="choiceLabel"/></td>
                        <td class="simple_attr_title" title="List of new site code names separated by new line">
                            <label for="choiceLabel">Site code names</label>
                        </td>
                        <td class="simple_attr_value">
                            <stripes:textarea class="smalltext" name="labels" id="labelsText" rows="5" cols="60"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td>
                            <stripes:submit name="allocate" value="Allocate site codes" />
                            <button type="button" id="closeAllocateLink">Cancel</button>
                        </td>
                    </tr>
                </table>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>