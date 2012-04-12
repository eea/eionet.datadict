<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add schema set">

    <stripes:layout-component name="contents">
    <h1>Create schema set</h1>

    <stripes:form method="post" beanclass="eionet.web.action.SchemaSetActionBean">
        <div id="outerframe">
            <table class="datatable">
                <colgroup>
                    <col style="width:26%"/>
                    <col style="width:4%"/>
                    <col style="width:4%"/>
                    <col style="width:62%"/>
                </colgroup>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_help">
                        <a href="/help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                            <img style="border:0" src="<%=request.getContextPath()%>/images/info_icon.gif" width="16" height="16" alt="help"/>
                        </a>
                    </td>
                    <td class="simple_attr_help">
                        <img style="border:0" src="<%=request.getContextPath()%>/images/mandatory.gif" width="16" height="16" alt=""/>
                    </td>
                    <td class="simple_attr_value">
                        <stripes:text class="smalltext" size="30" name="schemaSet.identifier"/>
                    </td>
                </tr>
                <tr>
                    <th>&nbsp;</th>
                    <td colspan="3">
                        <stripes:submit name="add" value="Add" class="mediumbuttonb"/>
                    </td>
                </tr>
            </table>
            <fieldset style="display:none">
                <stripes:hidden name="schemaSet.workingCopy" value="true"/>
            </fieldset>
        </div>
    </stripes:form>
    </stripes:layout-component>

</stripes:layout-render>