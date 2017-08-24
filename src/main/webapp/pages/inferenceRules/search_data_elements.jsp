<%@ include file="/pages/common/taglibs.jsp"%>

    <script type="text/javascript">
        // <![CDATA[
        (function($) {
            $(document).ready(function() {
                $("#searchDataElementsLink").live("click", function() {
                    $('#filters').dialog('open');
                    return false;
                });

                $("#searchDataElements").dialog({
                    autoOpen: false,
                    width: 800,
                });

                $("#cancelButton").click(function() {
                    $("#searchDataElements").dialog("close");
                    return false;
                });

                $("#searchDataElementsButton").click(function() {
                    $.ajax({
                        url: "${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/search?pattern=" + $("#filterText").val()
                    }).done(function(result) {
                        $("#searchResultsSection").empty();
                        if (result.length) {
                            $("#searchResultsSection").append('<table id="searchResults" class="datatable results"><thead><th>Element</th><th>Actions</th></thead><tbody></tbody></table>');
                            for(i = 0; i < result.length; i++) {
                                $("#searchResults tbody").append('<tr><td><a target="_blank" href="${pageContext.request.contextPath}/dataelements/' + result[i].id + '">' + 
                                        result[i].shortName + '</a></td><td><a href="${pageContext.request.contextPath}/dataelements/' + result[i].id + '" data-element-id="' + result[i].id + '" class="selectDataElement">[Select]</a></td></tr>');
                            }
                            $(".selectDataElement").click(function() {
                                var elementId = $(this).data("elementId");
                                var elementInputId = "${param.elementInputId}";
                                $("#" + elementInputId).val(elementId);
                                $("#searchDataElements").dialog("close");
                                return false;
                            });
                        } else {
                            $("#searchResultsSection").append('<p class="not-found">No data elements found.</p>');
                        }
                   });
                });
                
            });
        })(jQuery);
        // ]]>
    </script>

    <div id="searchDataElements" title="Search data elements" style="display:none">
        <table id="searchDataElementsFilter" class="datatable results">
            <tr>
                <th scope="row" class="scope-row simple_attr_title" title="Vocabulary concept identifier or label ">
                    <label for="filterText">Data element:</label>
                </th>
                <td class="simple_attr_value">
                    <input class="smalltext" size="50" id="filterText" placeholder="Search by short name"/>
                </td>
            </tr>
            <tr>
                <th></th>
                <td>
                    <input type="submit" id="searchDataElementsButton" name="search" value="Search" class="mediumbuttonb"/>
                    <input id="cancelButton" name="cancelSearch" value="Cancel" class="mediumbuttonb" type="button">
                </td>
            </tr>
        </table>
        <p id="searchResultsSection"></p>
    </div>