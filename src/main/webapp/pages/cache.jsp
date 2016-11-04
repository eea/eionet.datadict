<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Cache" helpScreen="cache" currentSection="${fn:escapeXml(actionBean.cacheTypeConfig.viewSection)}">

    <stripes:layout-component name="contents">
        <h1>Cached articles for ${fn:escapeXml(actionBean.cacheTypeConfig.objectType.title)}: <em>${fn:escapeXml(actionBean.identifier)}</em></h1>
        <stripes:form beanclass="${actionBean['class'].name}">
            <table class="datatable results">
                <thead>
                    <tr>
                        <th></th>
                        <th>Article</th>
                        <th>Created</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="cacheEntry" varStatus="row" items="${actionBean.cacheEntries}">
                        <tr class="${(row.index + 1) % 2 != 0 ? 'odd' : 'even'}">
                            <td class="center">
                                <stripes:checkbox name="articleTypeKeys" class="selectable" value="${fn:escapeXml(cacheEntry.articleType.key)}" />
                            </td>
                            <td>
                                ${fn:escapeXml(cacheEntry.articleType.title)}
                            </td>
                            <td>
                                ${fn:escapeXml(cacheEntry.creationDate)}
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <p class="actions">
                <stripes:hidden name="objectId" />
                <stripes:hidden name="objectTypeKey" />
                <stripes:submit name="update" value="Update selected" />
                <stripes:submit name="delete" value="Remove selected" />
            </p>
        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>
