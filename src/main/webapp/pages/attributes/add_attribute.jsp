<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit Attribute">
    <stripes:layout-component name="contents">
        <c:set value="${actionBean.viewModel}" var="model"/>
        <h1>Add an attribute definition</h1>
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
                    <td><stripes:text name="viewModel.attributeDefinition.shortName"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Name</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.name"/></td>
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
                        <stripes:select name="viewModel.attributeDefinition.obligationLevel" value="eionet.datadict.model.enums.Enumerations$Obligation.M">
                            <stripes:options-enumeration enum="eionet.datadict.model.enums.Enumerations$Obligation" label="label"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display type</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:select name="viewModel.attributeDefinition.displayType" value="eionet.datadict.model.enums.Enumerations$AttributeDisplayType.TEXT">
                            <stripes:option label="-Do not display at all-" value=""/>
                            <stripes:options-enumeration enum="eionet.datadict.model.enums.Enumerations$AttributeDisplayType" label="displayLabel"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display multiple</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:checkbox  name="viewModel.attributeDefinition.displayMultiple"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Inheritance</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <c:forEach var="inherit" items="${model.allInherits}">
                            <c:choose>
                                <c:when test="${inherit.value eq '0'}">
                                    <stripes:radio name="viewModel.attributeDefinition.inherit" value="${inherit}" checked="${inherit}"/>${inherit.label}<br/>
                                </c:when>
                                <c:otherwise>
                                    <stripes:radio name="viewModel.attributeDefinition.inherit" value="${inherit}"/>${inherit.label}<br/>
                                </c:otherwise>
                            </c:choose>
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
                            <stripes:checkbox name="viewModel.displayForTypes" value="${displayForType}"/> ${displayForType.label}<br/>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display width</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.displayWidth" value=""/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display height</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="viewModel.attributeDefinition.displayHeight"></stripes:text></td>
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
                        <stripes:submit class="mediumbuttonb" name="saveAdd" value="Save"/>
                    </td>
                </tr>
            </table>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>