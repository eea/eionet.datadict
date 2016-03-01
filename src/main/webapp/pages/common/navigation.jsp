<%@page import="eionet.util.SecurityUtil,eionet.meta.DDUser"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li ${currentSection eq 'documentation' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/documentation">Help and documentation</a></li>
        <li ${currentSection eq 'datasets' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/datasets.jsp">Datasets</a></li>
        <li ${currentSection eq 'tables' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/searchtables">Tables</a></li>
        <li ${currentSection eq 'dataElements' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/searchelements">Data elements</a></li>
        <%
        DDUser _user = SecurityUtil.getUser(request);
        if (_user!=null){
            %>
            <li ${currentSection eq 'checkouts' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/checkedout.jsp">Your checkouts</a></li>
            <li ${currentSection eq 'attributes' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/attributes.jsp">Attributes</a></li><%
        }

        if (SecurityUtil.userHasPerm(request, "/import", "x")){ %>
            <li ${currentSection eq 'import' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/import.jsp">Import datasets</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/cleanup", "x")){ %>
            <li ${currentSection eq 'cleanup' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/clean.jsp">Cleanup</a></li> <%
        }
        if (_user!=null){ %>
            <li ${currentSection eq 'subscribe' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/subscribe.jsp">Subscribe</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/schemasets", "v") || SecurityUtil.userHasPerm(request, "/schemas", "v")){ %>
          <li ${currentSection eq 'schemas' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/schemasets/browse/">Schemas</a></li> <%
        }
        %>
        <li ${currentSection eq 'vocabularies' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/vocabularies">Vocabularies</a></li>
        <li ${currentSection eq 'services' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/services/list">Services</a></li>
        <li ${currentSection eq 'namespaces' ? 'class="current"' : ''}><a href="${pageContext.request.contextPath}/namespaces">Namespaces</a></li>
    </ul>
</div>
