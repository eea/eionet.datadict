<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Inference Rule">
    <stripes:layout-component name="contents">
        <c:if test="${(empty actionBean.context.validationErrors) || (not empty actionBean.parentElement)}">
            <h1>New Inference Rule for element <stripes:link href="/dataelements/${actionBean.parentElement.id}">${actionBean.parentElement.shortName}</stripes:link></h1>
            <div id="operations">
                <ul>
                    <li>
                        <stripes:link href="/inference_rules/${actionBean.parentElement.id}">back to rules</stripes:link>
                    </li>
                </ul>
            </div>
            <stripes:form method="get" beanclass="${actionBean.class.name}">
                <table class="datatable">
                    <tbody>
                        <stripes:hidden name="parentElementId" />
                        <tr>
                            <th>Rule</th>
                            <td>
                            <stripes:select name="type">
                                <stripes:options-map map="<%=eionet.meta.dao.domain.InferenceRule.getAllRuleMappings()%>" label="key" value="value"/> 
                            </stripes:select>
                            </td>
                        </tr>
                        <tr>
                            <th>Target Element ID</th>
                            <td>
                                <stripes:text name="targetElementId"/>
                            </td>
                            <td>
                                <input id="search-element" type="button" value="Search by name" href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/search" >
                                <input name="element-name" type="text" />
                            </td>
                        </tr>
                    </tbody>
                </table>
                <stripes:submit name="addRule" value="Add" class="mediumbuttonb" />
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
                               jQuery("input[name='targetElementId']").val(elementID);
                               dialog.dialog("close");
                           });
                       });
                    });
            })();
            </script>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>