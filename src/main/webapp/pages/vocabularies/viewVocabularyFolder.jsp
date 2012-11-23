<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Vocabulary">

    <stripes:layout-component name="contents">

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="edit">
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                        Edit vocabulary
                    </stripes:link>
                </li>
            </ul>
        </div>

        <h1>Vocabulary</h1>

        <!-- Vocabulary folder -->
        <div id="outerframe">
            <c:url var="mandatoryPic" value="/images/mandatory.gif" />
            <table class="datatable">
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Identifier
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.identifier}" />
                    </td>
                </tr>
                <tr>
                    <th scope="row" class="scope-row simple_attr_title">
                        Label
                    </th>
                    <td class="simple_attr_value">
                        <c:out value="${actionBean.vocabularyFolder.label}" />
                    </td>
                </tr>
            </table>
        </div>

        <!-- Vocabulary concepts -->

        <display:table name="${actionBean.vocabularyConcepts}" class="datatable" id="item" style="width:80%">
            <display:column title="Identifier" property="identifier" />
            <display:column title="Label" property="label" />
            <display:column title="Definition" property="definition" />
            <display:column title="Notation" property="notation" />
        </display:table>

    </stripes:layout-component>

</stripes:layout-render>