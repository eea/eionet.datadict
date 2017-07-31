<%@page contentType="text/html; charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Attribute editor" currentSection="attributes">
    <%@ include file="/pages/attributes/attribute_scripts.jsp"%>
    <stripes:layout-component name="contents">
        <h1>Attribute definition</h1>
        <stripes:form beanclass="${actionBean['class']}">
            <table class="datatable results">
                <tr>
                    <th scope="row" class="scope-row">Short name</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif"/>" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty actionBean.attribute.id}">
                                <em><c:out value="${actionBean.attribute.shortName}"/></em>
                            </c:when>
                            <c:otherwise>
                                <stripes:text id="Short Name" class="mandatory_field" name="attribute.shortName"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Name</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td><stripes:text id="Name" class="mandatory_field" name="attribute.name"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Context</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <stripes:select name="attribute.namespace.id" value="${actionBean.attribute.namespace.id}">
                            <stripes:options-collection collection="${actionBean.namespaces}" value="id" label="fullName"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Definition</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:textarea class="small" rows="5" cols="52" name="attribute.definition"/></td>  
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Obligation</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <stripes:select name="attribute.obligationType" value="${actionBean.attribute.obligationType}">
                            <stripes:options-enumeration enum="eionet.datadict.model.Attribute$ObligationType" label="label"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display type</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:select id='select-display-type' name="attribute.displayType" value="${actionBean.attribute.displayType}" onchange="showOnChange(this,'${actionBean.attribute.displayType}');">
                            <stripes:option label="-Do not display at all-" value=""/>
                            <stripes:options-enumeration enum="eionet.datadict.model.Attribute$DisplayType" label="label"/>
                        </stripes:select>
                        <c:set var="vocab_display_style" value ="display: none"/>
                        <c:set var="select_display_style" value="display: none"/>
                        <c:if test="${not empty actionBean.attribute.id and actionBean.attribute.displayType == 'VOCABULARY'}">
                            <c:set var="vocab_display_style" value="display: inline"/>
                        </c:if>
                        <c:if test="${not empty actionBean.attribute.id and actionBean.attribute.displayType == 'SELECT'}">
                            <c:set var="select_display_style" value="display: inline"/>
                        </c:if>
                        <div id="vocabulary" style="${vocab_display_style}">
                            &nbsp;
                            <stripes:link href="${actionBean.contextPath}/vocabularies/selectVocabulary">
                                <stripes:param name="attrId" value="${actionBean.attribute.id}"/>
                                <img src="<stripes:url value="/images/edit.gif" />" alt="Edit vocabulary" name="Edit vocabulary" title="Edit vocabulary binding"/>
                            </stripes:link>
                            &nbsp;
                            <stripes:link beanclass="eionet.web.action.AttributeActionBean" event="removeVocabularyBinding">
                                <stripes:param name="attribute.id" value="${actionBean.attribute.id}"/>
                                <img src="<stripes:url value="/images/delete.gif" />" alt="Remove vocabulary" name="Remove vocabulary" title="Remove vocabulary binding"/>
                            </stripes:link>
                            <br />
                            <div class="smallfont">
                                Current:
                                <c:choose>
                                    <c:when test="${not empty actionBean.attribute.vocabulary}">
                                        <stripes:link href="${actionBean.contextPath}/vocabulary/${actionBean.attribute.vocabulary.folderLabel}/${actionBean.attribute.vocabulary.identifier}/view">
                                            <c:out value="${actionBean.attribute.vocabulary.label}"/>
                                        </stripes:link>
                                        <stripes:hidden name="attribute.vocabulary.folderLabel"/>
                                        <stripes:hidden name="attribute.vocabulary.identifier"/>
                                        <stripes:hidden name="attribute.vocabulary.label"/>
                                        <stripes:hidden name="attribute.vocabulary.id"/>
                                    </c:when>
                                    <c:otherwise>
                                        <em>None</em>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div id="select" style="${select_display_style}">
                            &nbsp;<span class="smallfont"><a href="${actionBean.contextPath}/fixedvalues/attr/${actionBean.attribute.id}/edit">
                                  <b>FIXED VALUES</b></a></span>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display multiple</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:checkbox  name="attribute.displayMultiple"  checked="${actionBean.attribute.displayMultiple}"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Inheritance</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <c:forEach var="inherit" items="${ddfn:getEnumValues('eionet.datadict.model.Attribute$ValueInheritanceMode')}">
                            <stripes:radio id="${inherit}" name="attribute.valueInheritanceMode" value="${inherit}" />
                            <label for="${fn:escapeXml(inherit)}">${fn:escapeXml(inherit.label)}<br/>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display order</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:text size="5" name="attribute.displayOrder"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display for</th>
                    <td><img src="<stripes:url value="/images/mandatory.gif" />" alt="Mandatory" name="Mandatory"/></td>
                    <td>
                        <c:forEach var="displayForType" items="${ddfn:getEnumValues('eionet.datadict.model.Attribute$TargetEntity')}"> 
                            <div>
                               <stripes:checkbox id="targetEntity-${displayForType.value}" name="attribute.targetEntities" value="${displayForType}" checked="${actionBean.attribute.targetEntities}"/>
                               <label for="targetEntity-${displayForType.value}">${fn:escapeXml(displayForType.label)}</label>
                            </div>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display width</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="attribute.displayWidth"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">Display height</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="attribute.displayHeight"/></td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">RDF property URI</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td>
                        <stripes:select name="attribute.rdfNamespace.id" value="${actionBean.attribute.rdfNamespace.id}">
                        <stripes:option/>
                            <stripes:options-collection collection="${actionBean.rdfNamespaces}" value="id" label="uri"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row">RDF property name</th>
                    <td><img src="<stripes:url value="/images/optional.gif" />" alt="Optional" name="Optional"/></td>
                    <td><stripes:text name="attribute.rdfPropertyName"/></td>
                </tr>
                <tr>
                    <th></th>
                    <td colspan="2">
                        <stripes:submit class="mediumbuttonb" name="save" value="Save" onclick="return validateMandatoryEditorFields()"/>
                        <c:if test="${not empty actionBean.attribute.id}">
                            <stripes:submit class="mediumbuttonb" name="reset" value="Reset"/>
                            <stripes:submit class="mediumbuttonb" name="confirmDelete" value="Delete"/>
                            <stripes:hidden name="attribute.shortName"/>
                        </c:if>
                    </td>
                </tr>
            </table>
            <stripes:hidden name="attribute.id"/>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>