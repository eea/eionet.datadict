<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="eionet.meta.dao.domain.SchemaSet"%>
<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Add vocabulary">

    <stripes:layout-component name="contents">

        <h1>New vocabulary</h1>

        <stripes:form id="form" method="post" beanclass="${actionBean.class.name}" style="padding-top:20px">
        <stripes:hidden name="copyId" />
        <div id="outerframe">
            <c:url var="mandatoryPic" value="/images/mandatory.gif" />
            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col />
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${mandatoryPic}" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="vocabularyFolder.identifier"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_help">
                        <img style="border:0" src="${mandatoryPic}" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text name="vocabularyFolder.label" style="width: 500px;" class="smalltext"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Base URI
                    </th>
                    <td class="simple_attr_help">
                        <dd:optionalIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text name="vocabularyFolder.baseUri" style="width: 500px;" class="smalltext"/>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Type
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.type.label}" />
                        <stripes:select name="vocabularyFolder.type">
                            <stripes:options-enumeration enum="eionet.meta.dao.domain.VocabularyType" label="label"/>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Numeric concept identifiers
                    </th>
                    <td class="simple_attr_help">
                        <dd:mandatoryIcon />
                    </td>
                    <td class="simple_attr_value">
                        <stripes:checkbox name="vocabularyFolder.numericConceptIdentifiers" />
                    </td>
                </tr>
                <tr>
                    <th>&nbsp;</th>
                    <td colspan="2">
                        <stripes:submit name="saveFolder" value="Add" class="mediumbuttonb"/>
                        <stripes:submit name="cancelAdd" value="Cancel" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
        </div>

        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>