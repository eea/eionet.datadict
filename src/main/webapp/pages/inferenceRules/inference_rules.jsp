<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Inference Rule" currentSection="dataElements">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Inference Rules of <stripes:link href="/dataelements/${actionBean.parentElement.id}">${actionBean.parentElement.shortName}</stripes:link> element </h1>
            <div style="margin:10px">
                <input id="add-rule" type="button" value="Add new Rule" href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/newRule" >
            </div>
            <c:choose>
                <c:when test="${empty actionBean.rules}">
                    There are no rules for this element
                </c:when>
                <c:otherwise>
                    <table class="datatable">
                        <tr>
                            <th>Rule</th>
                            <th>Target Element</th>
                        </tr>
                        <c:forEach items="${actionBean.rules}" var="rule">
                            <tr>
                                <td style="background-color:#D3D3D3;">${rule.type.name}</td>
                                <td style="background-color:#D3D3D3;"><stripes:link href="/dataelements/${rule.targetDElement.id}">${rule.targetDElement.id}</stripes:link></td>
                                <td style="background-color:#D3D3D3;"><input class="edit-rule" type="button" value="Edit" href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/existingRule/${rule.type}/${rule.targetDElement.id}" ></td>
                                <td style="background-color:#D3D3D3;"><input class="delete-rule" type="button" value="Delete" href="${pageContext.servletContext.contextPath}/inference_rules/${actionBean.parentElement.id}/deleteRule/${rule.type}/${rule.targetDElement.id}" ></td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
            <script type="text/javascript">
             (function() {
                jQuery("#add-rule, .edit-rule").click(function() {
                    location.href = jQuery(this).attr("href");
                });

                jQuery(".delete-rule").click(function(){
                    var response = confirm("This rule will be deleted. Are you sure ?");
                    if(response == true)
                        location.href = jQuery(this).attr("href");
                });
            })();
            </script>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>