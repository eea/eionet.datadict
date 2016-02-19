<%@page import="eionet.util.SecurityUtil,eionet.meta.DDUser"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li ${currentSection eq 'documentation' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/documentation">Help and documentation</a></li>
        <li ${currentSection eq 'datasets' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/datasets.jsp">Datasets</a></li>
        <li ${currentSection eq 'tables' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/tableSearch.action">Tables</a></li>
        <li ${currentSection eq 'dataElements' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/searchelements">Data elements</a></li>
        <%
        DDUser _user = SecurityUtil.getUser(request);
        if (_user!=null){
            %>
            <li ${currentSection eq 'checkouts' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/checkedout.jsp">Your checkouts</a></li>
            <li ${currentSection eq 'attributes' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/attributes.jsp">Attributes</a></li><%
        }

        if (SecurityUtil.userHasPerm(request, "/import", "x")){ %>
            <li ${currentSection eq 'import' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/import.jsp">Import datasets</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/cleanup", "x")){ %>
            <li ${currentSection eq 'cleanup' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/clean.jsp">Cleanup</a></li> <%
        }
        if (_user!=null){ %>
            <li ${currentSection eq 'subscribe' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/subscribe.jsp">Subscribe</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/schemasets", "v") || SecurityUtil.userHasPerm(request, "/schemas", "v")){ %>
          <li ${currentSection eq 'schemas' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/schemasets/browse/">Schemas</a></li> <%
        }
        %>
        <li ${currentSection eq 'vocabularies' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/vocabularies">Vocabularies</a></li>
        <li ${currentSection eq 'services' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/services/list">Services</a></li>
        <li ${currentSection eq 'namespaces' ? 'class="current"' : ''}><a href="<%=request.getContextPath()%>/namespaces">Namespaces</a></li>
    </ul>
</div>
