<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Inference Rule" currentSection="dataElements">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Edit Inference Rule for element <stripes:link href="/dataelements/${actionBean.parentElement.id}">${actionBean.parentElement.shortName}</stripes:link></h1>
            <div id="drop-operations">
                <ul>
                    <li class="back">
                        <stripes:link href="/inference_rules/${actionBean.parentElement.id}">Back to rules</stripes:link>
                    </li>
                </ul>
            </div>
            <stripes:form method="get" beanclass="${actionBean['class'].name}">
                <table class="datatable">
                    <tbody>
                        <tr>
                            <th>Rule</th>
                            <th>Target Element</th>
                        </tr>
                        <tr>
                            <td style="background-color:#D3D3D3;">${actionBean.type.name}</td>
                            <td style="background-color:#D3D3D3;">${actionBean.targetElementId}</td>
                        </tr>
                    </tbody>
                </table>
                <table class="datatable">
                    <tbody>
                        <stripes:hidden name="parentElementId" />
                        <stripes:hidden name="type" />
                        <stripes:hidden name="targetElementId" />
                        <tr>
                            <th>Rule</th>
                            <td>
                            <stripes:select name="newType">
                                <stripes:options-map map="<%=eionet.meta.dao.domain.InferenceRule.getAllRuleMappings()%>" label="key" value="value"/> 
                            </stripes:select>
                            </td>
                        </tr>
                        <tr>
                            <th>Target Element ID</th>
                            <td>
                                <stripes:text name="newTargetElementId"/>
                            </td>
                            <td>
                                <input id="search-element" type="button" value="Search by name" href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/search" >
                                <input name="element-name" type="text" />
                            </td>
                        </tr>
                    </tbody>
                </table>
                <stripes:submit name="editRule" value="Edit" class="mediumbuttonb" />
            </stripes:form>
            <div id="grep-elements" style="visibility:hidden">
                <div id="table-wrapper" style="max-height: 300px; overflow-y:auto">
                <table >
                    <thead>
                        <tr>
                            <th>Element Name</th>
                        </tr>
                    </thead>
                    <tbody cellpadding="15"></tbody>
                </table>
                </div>
            </div>
            <script type="text/javascript">
            (function() {
                    var context  = "${pageContext.servletContext.contextPath}";
                    var dialog;
                
                    jQuery("#search-element").click(function(){
                       jQuery.ajax({
                           url: jQuery(this).attr("href") + "?pattern=" + jQuery("input[name='element-name']").val()
                       }).done(function(result){
                           jQuery("#table-wrapper tbody").empty();
                           for(i=0; i<result.length; i++){
                               jQuery("#table-wrapper tbody").append("<tr><td><a target='_blank' href='" + context + "/dataelements/" + result[i].id + "'>" + result[i].shortName +  "</a></td><td>" + "<input elemID='" + result[i].id + "' class='selectElement' type='button' value='Select'>" + "</td></tr>");
                           }
                           dialog = jQuery("#table-wrapper").dialog({title:'Matched Elements', width: "35%"});
                           jQuery(".selectElement").click(function(){
                               var elementID = jQuery(this).attr("elemID");
                               jQuery("input[name='newTargetElementId']").val(elementID);
                               dialog.dialog("close");
                           });
                       });
                    });
            })();
            </script>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>