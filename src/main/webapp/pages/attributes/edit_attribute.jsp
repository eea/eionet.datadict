<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit Attribute" currentSection="attributes">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <c:set value="${actionBean.viewModel}" var="model"/>
        <h1>Edit attribute definition</h1>
        <stripes:form beanclass="${model.submitActionBeanName}">
            <table class="datatable" style="clear:right">
                <col style="width:9em"/>
                <col style="width:2em"/>
                <col style="width:35em"/>
                <tr>
                    <th scope="row" class="scope-row">Type</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif"/>" alt="Mandatory" name="Mandatory"/></td>
                    <td> <b>SIMPLE ATTRIBUTE</b></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Short name</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif"/>" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <em>${model.attributeDefinition.shortName}</em>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Name</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.name"/></td>
                <tr>
                    <th scope="row" class="scope-row">Context</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <stripes:select name="viewModel.attributeDefinition.namespace.namespaceID" value="${model.attributeDefinition.namespace.namespaceID}">
                            <stripes:options-collection collection="${model.namespaces}" value="namespaceID" label="fullName"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Definition</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:textarea class="small" rows="5" cols="52" name="viewModel.attributeDefinition.definition"/></td>  
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Obligation</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <stripes:select name="viewModel.attributeDefinition.obligationLevel" value="${model.attributeDefinition.obligationLevel}">
                            <stripes:options-enumeration enum="${model.obligationClass}" label="label"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display type</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:select id='select-display-type' name="viewModel.attributeDefinition.displayType" value="${model.attributeDefinition.displayType}" onchange="showOnChange(this,'${model.attributeDefinition.displayType}')">
                            <stripes:option label="-Do not display at all-" value=""/>
                            <stripes:options-enumeration enum="${model.displayTypeClass}" label="displayLabel"/>
                        </stripes:select>
                        <c:set var="vocab_display_style" value ="display: none"/>
                        <c:set var="select_display_style" value="display: none"/>
                        <c:if test="${model.attributeDefinition.displayType == 'VOCABULARY'}">
                            <c:set var="vocab_display_style" value="display: inline"/>
                        </c:if>
                        <c:if test="${model.attributeDefinition.displayType == 'SELECT'}">
                            <c:set var="select_display_style" value="display: inline"/>
                        </c:if>
                        <div id="vocabulary" style="${vocab_display_style}">
                            &emsp;
                            <stripes:link href="${actionBean.contextPath}/vocabularies/selectVocabulary">
                                <stripes:param name="attrId" value="${model.attributeDefinition.id}"/>
                                ATTACH NEW VOCABULARY
                            </stripes:link>
                            &emsp;
                            <div class="smallfont">
                                (Current: 
                                <c:choose>
                                    <c:when test="${not empty model.attributeDefinition.vocabulary}">
                                        <stripes:link href="${actionBean.contextPath}/vocabulary/${model.attributeDefinition.vocabulary.folderLabel}/${model.attributeDefinition.vocabulary.identifier}/view">
                                            ${model.attributeDefinition.vocabulary.label}
                                        </stripes:link>
                                    </c:when>
                                    <c:otherwise>
                                        <em>None</em>
                                    </c:otherwise>
                                </c:choose>
                                )
                            </div>
                        </div>
                        <div id="select" style="${select_display_style}">
                            &nbsp;<span class="smallfont"><a href="${actionBean.contextPath}/fixedvalues/attr/${model.attributeDefinition.id}/edit">
                                    <b>FIXED VALUES</b></a></span>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display multiple</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:checkbox  name="viewModel.attributeDefinition.displayMultiple"  checked="${model.attributeDefinition.displayMultiple}"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Inheritance</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <c:forEach var="inherit" items="${model.allInherits}">
                            <stripes:radio name="viewModel.attributeDefinition.inherit" value="${inherit}"/>${inherit.label}<br/>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display order</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:text size="5" name="viewModel.displayOrder"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display for</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <c:forEach var="displayForType" items="${model.allDisplayForTypes}"> 
                            <stripes:checkbox name="viewModel.displayForTypes" value="${displayForType}" checked="${model.displayForTypes}"/> ${displayForType.label}<br/>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display width</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.displayWidth"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display height</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.displayHeight"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">RDF property URI</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:select name="viewModel.rdfNamespaceId">
                            <stripes:option/>
                            <stripes:options-collection collection="${model.allRdfNamespaces}" value="id" label="uri"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">RDF property name</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.rdfPropertyName"/></td>
                </tr>
                <tr>
                    <td colspan="3" style="text-align:center">
                        <stripes:submit class="mediumbuttonb" name="saveEdit" value="Save"/>
                        <stripes:submit class="mediumbuttonb" name="delete" value="Delete"/>
                    </td>
                </tr>
            </table>
            <stripes:hidden name="attrId"/>
            <stripes:hidden name="viewModel.attributeDefinition.id"/>      
            <stripes:hidden name="viewModel"/>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>