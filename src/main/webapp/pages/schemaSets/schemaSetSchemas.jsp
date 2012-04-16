<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Schema set schemas">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

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
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>
    
    <stripes:layout-component name="contents">
    
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <a href="#" id="uploadSchemaLink">Upload schema</a>
                </li>
            </ul>
        </div>
        
        <c:choose>
            <c:when test="${actionBean.context.eventName=='view'}">
                <h1>View schema set</h1>
            </c:when>
            <c:otherwise>
                <h1>Edit schema set</h1>
            </c:otherwise>
        </c:choose>
        
        <div id="tabbedmenu">
            <ul>
                <c:forEach items="${actionBean.tabs}" var="tab">
                    <li <c:if test="${tab.selected}">id="currenttab"</c:if>>
                        <stripes:link href="${tab.href}" title="${tab.hint}"><c:out value="${tab.title}"/></stripes:link>
                    </li>
                </c:forEach>
            </ul>
        </div>
    
        <c:if test="${not empty actionBean.schemas}">
            <stripes:form id="schemasForm" method="post" beanclass="${actionBean.class.name}" style="padding-top:20px">
            
                <display:table name="${actionBean.schemas}" class="datatable" id="schema" style="width:80%">
                    <display:column>
                        <stripes:checkbox name="schemaIds" value="${schema.id}" />
                    </display:column>
                    <display:column title="File name">
                        <stripes:link href="schema.action" title="Open schema details">
                            <stripes:param name="schema.id" value="${schema.id}"/>
                            <c:out value="${schema.fileName}"/>
                        </stripes:link>
                    </display:column>
                </display:table>
                
                <stripes:submit name="deleteSchemas" value="Delete" />
                <input type="button" onclick="toggleSelectAll('schemasForm');return false" value="Select all" name="selectAll">
                
            </stripes:form>
        </c:if>
        
        <c:if test="${empty actionBean.schemas}">
            <div style="margin-top:3em">No schemas found! Use operations menu to add one.</div>
        </c:if>
        
        <div id="uploadSchemaDialog" title="Upload schema">
            <stripes:form beanclass="${actionBean.class.name}" method="post">
                
                <fieldset style="border: 0px;">
                    <label for="fileToUpload" style="width: 200px; float: left;">File to upload*:</label>
                    <stripes:file name="uploadedFile" id="fileToUpload" size="80"/>
                </fieldset>
                <stripes:submit name="uploadSchema" value="Upload"/>
                <button id="closeUploadSchemaDialog">Cancel</button>
                
                <div style="display:none">
                    <stripes:hidden name="schemaSet.id"/>
                    <stripes:hidden name="schemaSet.identifier"/>
                </div>
            </stripes:form>
        </div>
    </stripes:layout-component>

</stripes:layout-render>