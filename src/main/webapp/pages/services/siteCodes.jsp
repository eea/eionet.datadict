<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Site codes">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {

                var sampleNames = "site name 1, site name 2, site name 3";

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

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="#" id="allocateSiteCodesLink">Allocate site codes</a></li>
            </ul>
        </div>

        <h1>Site codes</h1>

        <div id="allocateSiteCodesDiv" title="Allocate site codes">
            <div class="tip-msg">
                <strong>Tip</strong>
                <p>Country codes can be allocated by number or by list of comma separated site code names.</p>
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
                            Country
                        </td>
                        <td class="simple_attr_value">
                            <stripes:select name="country">
                                <stripes:options-collection collection="${actionBean.countries}" value="value" label="definition" />
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><stripes:radio name="choice" value="amount" /></td>
                        <td class="simple_attr_title" title="Number of site codes to allocate">
                            Number of site codes
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="5" name="amount" id="amountText"/>
                        </td>
                    </tr>
                    <tr><td colspan="4" style="padding-left: 10%">Or</td></tr>
                    <tr>
                        <td><stripes:radio name="choice" value="label" /></td>
                        <td class="simple_attr_title" title="List of new site code names separated by comma">
                            Site code names
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