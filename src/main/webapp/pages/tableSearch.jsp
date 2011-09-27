<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search tables">

    <stripes:layout-component name="contents">
    <h1>Search tables</h1>

    <div class="form">
        <stripes:form action="/tableSearch.action" method="post">
        <fieldset>
            <label for="shortName">
                Sort name
                <a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="shortName" name="shortName" />
        </fieldset>
        <fieldset>
            <label for="identifier">
                Identifier
                <a href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="identifier" name="identifier" />
        </fieldset>
        <fieldset>
            <label for="name">
                Name
                <a href="help.jsp?attrid=1&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="name" name="name" />
        </fieldset>
        <fieldset>
            <label for="definition">
                Definition
                <a href="help.jsp?attrid=4&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="definition" name="definition" />
        </fieldset>
        <fieldset>
            <label for="keyword">
                Keyword
                <a href="help.jsp?attrid=5&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="keyword" name="keyword" />
        </fieldset>
        <fieldset>
            <label for="descriptiveImage">
                Descriptive image
                <a href="help.jsp?attrid=40&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="descriptiveImage" name="descriptiveImage" />
        </fieldset>
        <fieldset>
            <label for="eeaIssue">
                EEA issue
                <a href="help.jsp?attrid=37&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="eeaIssue" name="eeaIssue" />
        </fieldset>
        <fieldset>
            <label for="methodology">
                Methodology
                <a href="help.jsp?attrid=17&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="methodology" name="methodology" />
        </fieldset>
        <fieldset>
            <label for="shortDescription">
                Short description
                <a href="help.jsp?attrid=15&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                    <img style="border:0" src="images/info_icon.gif" width="16" height="16" alt=""/>
                </a>
            </label>
            <stripes:text id="shortDescription" name="shortDescription" />
        </fieldset>
        <fieldset>
            <div>
                <stripes:radio id="substringSearch" value="SUBSTRING_SEARCH" name="searchType" /><label for="substringSearch" style="float: none;">Substring search</label>
                <stripes:radio id="exactSearch" value="EXACT_SEARCH" name="searchType" /><label for="exactSearch" style="float: none;">Exact search</label>
            </div>
        </fieldset>
        <fieldset>
            <div>
                <stripes:submit name="search" value="Search" />
                <stripes:submit name="reset" value="Reset" />
            </div>
        </fieldset>
        </stripes:form>
    </div>
    </stripes:layout-component>

</stripes:layout-render>