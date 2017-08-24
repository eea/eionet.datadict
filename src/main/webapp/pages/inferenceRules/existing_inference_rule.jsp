<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Inference Rule" currentSection="dataElements">
    <stripes:layout-component name="contents">
        <c:if test="${empty actionBean.context.validationErrors}">
            <h1>Edit inference rule for element <stripes:link href="/dataelements/${actionBean.parentElement.id}">${actionBean.parentElement.shortName}</stripes:link></h1>
            <div id="drop-operations">
                <ul>
                    <li class="back">
                        <stripes:link href="/inference_rules/${actionBean.parentElement.id}">Back to rules</stripes:link>
                    </li>
                </ul>
            </div>
            <p>
                <stripes:form method="get" beanclass="${actionBean['class'].name}">
                    <table class="datatable results">
                        <tbody>
                            <stripes:hidden name="parentElementId" />
                            <stripes:hidden name="type" />
                            <stripes:hidden name="targetElementId" />
                            <tr>
                                <th class="scope-row"><label for="newType">Inference rule</label></th>
                                <td>
                                    <stripes:select id="newType" name="newType">
                                        <stripes:options-map map="<%=eionet.meta.dao.domain.InferenceRule.getAllRuleMappings()%>" label="key" value="value"/> 
                                    </stripes:select>
                                </td>
                            </tr>
                            <tr>
                                <th class="scope-row"><label for="newTargetElementId">Element id</label></th>
                                <td>
                                    <stripes:text id="newTargetElementId" name="newTargetElementId"/>
                                    <a class="search" href="#" id="searchDataElementsLink">Search...</a>
                                </td>
                            </tr>
                            <tr>
                                <th></th>
                                <td colspan="2">
                                    <stripes:submit name="editRule" value="Save" class="mediumbuttonb" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </stripes:form>
            </p>
            <jsp:include page="search_data_elements.jsp">
                <jsp:param name="elementInputId" value="newTargetElementId" />
            </jsp:include>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>