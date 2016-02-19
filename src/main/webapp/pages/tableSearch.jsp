<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search tables" currentSection="tables">

    <stripes:layout-component name="contents">
        
    <script type="text/javascript" src="<%=request.getContextPath()%>/helpPopup.js"></script>
        
    <h1>Search tables</h1>

    <stripes:form action="/tableSearch.action" method="post">
        <table width="auto" cellspacing="0" style="margin-top:10px">
            <tr style="vertical-align:top">
                <td align="right" style="padding-right:10">
                    <b>Short name</b>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:text id="shortName" name="tableFilter.shortName" class="smalltext" size="59" />
                </td>
            </tr>

            <tr style="vertical-align:top">
                <td align="right" style="padding-right:10">
                    <b>Identifier</b>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:text id="identifier" name="tableFilter.identifier" class="smalltext" size="59" />
                </td>
            </tr>

            <c:forEach var="attr" items="${actionBean.tableFilter.attributes}" varStatus="row">
            <tr style="vertical-align:top">
                <td align="right" style="padding-right:10">
                    <b><c:out value="${attr.name}" /></b>
                </td>
                <td>
                    <a class="helpButton" href="help.jsp?screen=dataset&amp;area=short_name">
                        <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:text id="attr${row.index}" name="tableFilter.attributes[${row.index}].value" class="smalltext" size="59" />
                    <stripes:hidden name="tableFilter.attributes[${row.index}].id" />
                    <stripes:hidden name="tableFilter.attributes[${row.index}].name" />
                </td>
            </tr>
            </c:forEach>
            <tr>
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    <br />
                    <stripes:submit name="search" value="Search" />
                </td>
            </tr>
        </table>

    </stripes:form>
    </stripes:layout-component>

</stripes:layout-render>