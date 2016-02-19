<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Documentation" currentSection="documentation">
    <stripes:layout-component name="contents">
        <c:set var="menuSection" value="documentation" scope="request" />
            <c:if test='${ddfn:userHasPermission(actionBean.user.userName, "/documentation", "u")}'>
    			<div id="tabbedmenu">
	                <ul>
	                	<c:choose>
	                        <c:when test="${not empty actionBean.pageId && actionBean.pageId != 'contents'}">
			                	<c:choose>
			                        <c:when test="${actionBean.event == 'edit'}">
			                        	<li><stripes:link href="/documentation/${actionBean.pageId}">View</stripes:link></li>
			                            <li id="currenttab"><stripes:link href="/documentation/${actionBean.pageId}/edit">Edit</stripes:link></li>
			                        </c:when>
			                        <c:otherwise>
			                        	<li id="currenttab"><stripes:link href="/documentation/${actionBean.pageId}">View</stripes:link></li>
			                        	<li><stripes:link href="/documentation/${actionBean.pageId}/edit">Edit</stripes:link></li>
			                        </c:otherwise>
			                    </c:choose>
	                    	</c:when>
	                    	<c:otherwise>
	                    		<c:choose>
			                        <c:when test="${actionBean.event == 'edit' || actionBean.event == 'add' || actionBean.pageId == 'contents'}">
			                        	<li><stripes:link href="/documentation">View</stripes:link></li>
			                            <li id="currenttab"><stripes:link href="/documentation/contents">Contents</stripes:link></li>
			                        </c:when>
			                        <c:otherwise>
			                        	<li id="currenttab"><stripes:link href="/documentation">View</stripes:link></li>
			                        	<li><stripes:link href="/documentation/contents">Contents</stripes:link></li>
			                        </c:otherwise>
			                    </c:choose>
	                    	</c:otherwise>
	                    </c:choose>
	                </ul>
            	</div>
            	<br style="clear:left" />
				<br style="clear:left" />
            </c:if>
			<c:choose>
           		<c:when test='${actionBean.event == "edit" && ddfn:userHasPermission(actionBean.user.userName, "/documentation", "u")}'>
		            <stripes:form action="/documentation" method="post">
		                <table cellpadding="3" style="width:100%">
				    <col style="width:10em"/>
				    <col />
		                    <tr>
		                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
		                        <td>
		                            ${actionBean.pageId}
		                            <stripes:hidden name="pageObject.pid" value="${actionBean.pageId}"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="page_title">Page title</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_title" name="pageObject.title" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="content_type">Content type</stripes:label></td>
		                        <td>
		                            <stripes:text id="content_type" name="pageObject.contentType" size="66"/>
		                        </td>
		                    </tr>
		                    <c:if test='${actionBean.pageObject.editableContent}'>
			                    <tr>
			                        <td valign="top"><stripes:label class="question" for="page_content">Content</stripes:label></td>
			                        <td>
			                            <stripes:textarea id="page_content" name="pageObject.content" cols="70" rows="20" style="width:100%"/>
			                        </td>
			                    </tr>
		                    </c:if>
		                    <tr>
		                        <td><stripes:label class="question" for="file">File</stripes:label></td>
		                        <td><stripes:file name="fileToSave" id="file" size="54" /></td>
		                    </tr>
		                    <tr>
		                        <td colspan="2" align="right">
		                            <stripes:submit name="editContent" value="Save" />
		                        </td>
		                    </tr>
		                </table>
		            </stripes:form>
           		</c:when>
           		<c:when test='${actionBean.event == "add" && ddfn:userHasPermission(actionBean.user.userName, "/documentation", "u")}'>
		            <stripes:form action="/documentation" method="post">
		                <table cellpadding="3">
		                    <tr>
		                        <td><stripes:label class="question" for="page_id">Page ID</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_id" name="pageObject.pid" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="page_title">Page title</stripes:label></td>
		                        <td>
		                            <stripes:text id="page_title" name="pageObject.title" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="content_type">Content type</stripes:label></td>
		                        <td>
		                            <stripes:text id="content_type" name="pageObject.contentType" size="66"/>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td><stripes:label class="question" for="file">File</stripes:label></td>
		                        <td><stripes:file name="fileToSave" id="file" size="54" /></td>
		                    </tr>
		                    <tr>
		                    	<td></td>
		                        <td>
		                            <stripes:checkbox name="pageObject.overwrite" id="overwrite"/>
		                            <stripes:label for="overwrite">Overwrite if file with the same name already exists</stripes:label>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td colspan="2" align="right">
		                            <stripes:submit name="addContent" value="Add" />
		                        </td>
		                    </tr>
		                </table>
		            </stripes:form>
           		</c:when>
           		<c:when test='${actionBean.pageId == "contents" && ddfn:userHasPermission(actionBean.user.userName, "/documentation", "u")}'>
           			<div id="operations">
	                    <ul>
	                        <li>
	                            <stripes:link href="/documentation">
	                            	Add new file
	                            	<stripes:param name="event" value="add"/>
	                            </stripes:link>
	                        </li>
	                    </ul>
	                </div>
           			<stripes:form action="/documentation" method="post">
	           			<table>
							<c:forEach var="doc" items="${actionBean.pageObject.docs}">
								<tr>
									<td>
										<stripes:checkbox name="pageObject.docIds" value="${doc.pageId}"/>
									</td>
									<td>
										<stripes:link href="/documentation/${doc.pageId}/edit">${doc.pageId} (${doc.title})</stripes:link>
									</td>
								</tr>
							</c:forEach>
						</table>
						<div style="padding-top: 1em">
							<stripes:submit name="delete" value="Delete" />
						</div>
					</stripes:form>
           		</c:when>
   				<c:otherwise>
   					<c:choose>
						<c:when test="${not empty actionBean.pageId}">
							<h1>${actionBean.pageObject.title}</h1>
							${actionBean.pageObject.content}
						</c:when>
                        <c:otherwise>
                        	<h1>Documentation</h1>
		   					<ul>
			   					<c:forEach var="doc" items="${actionBean.pageObject.docs}">
									<li>
										<c:set var="doctitle" value="${doc.title}"/>
										<c:if test="${empty doctitle}">
											<c:set var="doctitle" value="${doc.pageId}"/>
										</c:if>
										<stripes:link href="/documentation/${doc.pageId}">${doctitle}</stripes:link>
									</li>
								</c:forEach>
							</ul>
                        </c:otherwise>
                    </c:choose>
   				</c:otherwise>
   			</c:choose>
    </stripes:layout-component>
</stripes:layout-render>
