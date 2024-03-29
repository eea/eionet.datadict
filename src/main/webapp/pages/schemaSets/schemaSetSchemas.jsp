<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema set schemas" currentSection="schemas">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            (function($) {
                $(document).ready(function() {
                    $("#toggleSelectAll").click(function() {
                        toggleSelectAll('schemasForm');
                        $(this).val() === "Select all" ? $("tr", "#schema").removeClass("selected") : $("tr", "#schema").addClass("selected");
                        return false;
                    });

                    $("#uploadSchemaLink").click(function() {
                        $('#uploadSchemaDialog').dialog('open');
                        return false;
                    });

                    $('#uploadSchemaDialog').dialog({
                        autoOpen: false,
                        width: 500
                    });

                    $("#closeUploadSchemaDialog").click(function() {
                        $('#uploadSchemaDialog').dialog("close");
                        return true;
                    });
                });
            })(jQuery);
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h1>Edit schemas of schema set <a href="${fn:escapeXml(schemaSetUrl)}">${actionBean.schemaSet.identifier}</a></h1>

        <c:if test="${actionBean.schemaSet.deprecatedStatus}">
            <div class="system-msg">
                <strong>Note</strong>
                <p>This schema set is deprecated. It is not valid anymore!</p>
            </div>
        </c:if>

        <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link beanclass="${actionBean['class'].name}">Back to schema set
                        <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
                        <stripes:param name="workingCopy" value="${actionBean.schemaSet.workingCopy}"/>
                    </stripes:link>
                </li>
                <li class="upload">
                    <a href="#" id="uploadSchemaLink">Upload schema</a>
                </li>
            </ul>
        </div>

        <stripes:url var="schemaSetUrl" beanclass="${actionBean['class'].name}">
            <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
            <stripes:param name="workingCopy" value="${actionBean.schemaSet.workingCopy}"/>
        </stripes:url>

        <c:if test="${not empty actionBean.schemas}">
            <stripes:form id="schemasForm" method="post" beanclass="${actionBean['class'].name}" style="padding-top:20px">
                <stripes:hidden name="schemaSet.id"/>
                <display:table name="${actionBean.schemas}" class="datatable results" id="schema">
                    <display:column>
                        <stripes:checkbox class="selectable" name="schemaIds" value="${schema.id}" />
                    </display:column>
                    <display:column title="File name">
                        <stripes:link beanclass="eionet.web.action.SchemaActionBean" title="Open schema details">
                            <stripes:param name="schemaSet.identifier" value="${actionBean.schemaSet.identifier}"/>
                            <stripes:param name="schema.fileName" value="${schema.fileName}"/>
                            <stripes:param name="workingCopy" value="true"/>
                            <c:out value="${schema.fileName}"/>
                        </stripes:link>
                    </display:column>
                    <display:column title="Name"><c:out value="${ddfn:join(schema.attributeValues['Name'],',')}"/></display:column>
                </display:table>

                <stripes:submit name="deleteSchemas" value="Delete" />
                <input type="button" id="toggleSelectAll" value="Select all" name="selectAll">

            </stripes:form>
        </c:if>

        <c:if test="${empty actionBean.schemas}">
            <p class='not-found'>No schemas defined for this schema set yet! Use operations menu to upload one.</p>
        </c:if>

        <div id="uploadSchemaDialog" title="Upload schema">
            <stripes:form beanclass="${actionBean['class'].name}" method="post">

                <label for="fileToUpload">File to upload*:</label>
                <stripes:file name="uploadedFile" id="fileToUpload" size="40"/>
                <c:if test="${not empty actionBean.mandatorySchemaAttributes}">
                    <c:forEach items="${actionBean.mandatorySchemaAttributes}" var="mandatoryAttr">
                        <br/>
                        <label for="attr_${mandatoryAttr.ID}_text"><c:out value="${mandatoryAttr.shortName}"/>*:</label>
                        <input type="text" name="attr_${mandatoryAttr.ID}" id="attr_${mandatoryAttr.ID}_text" class="smalltext" style="width:100%"/>
                    </c:forEach>
                </c:if>
                <br/><br/>
                <stripes:submit name="uploadSchema" value="Upload"/>
                <input type="button" id="closeUploadSchemaDialog" value="Cancel"/>

                <div style="display:none">
                    <stripes:hidden name="schemaSet.id"/>
                    <stripes:hidden name="schemaSet.identifier"/>
                </div>
            </stripes:form>
        </div>
    </stripes:layout-component>

</stripes:layout-render>
