<%@page import="eionet.util.SecurityUtil,eionet.meta.DDUser"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li><a href="<%=request.getContextPath()%>/documentation">Help and documentation</a></li>
        <li><a href="<%=request.getContextPath()%>/datasets.jsp">Datasets</a></li>
        <li><a href="<%=request.getContextPath()%>/tableSearch.action">Tables</a></li>
        <li><a href="<%=request.getContextPath()%>/searchelements">Data elements</a></li>
        <%
        DDUser _user = SecurityUtil.getUser(request);
        if (_user!=null){
            %>
            <li><a href="<%=request.getContextPath()%>/checkedout.jsp">Your checkouts</a></li>
            <li><a href="<%=request.getContextPath()%>/attributes.jsp">Attributes</a></li><%
        }

        if (SecurityUtil.userHasPerm(request, "/import", "x")){ %>
            <li><a href="<%=request.getContextPath()%>/import.jsp">Import datasets</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/cleanup", "x")){ %>
            <li><a href="<%=request.getContextPath()%>/clean.jsp">Cleanup</a></li> <%
        }
        if (_user!=null){ %>
            <li><a href="<%=request.getContextPath()%>/subscribe.jsp">Subscribe</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/schemasets", "v") || SecurityUtil.userHasPerm(request, "/schemas", "v")){ %>
          <li><a href="<%=request.getContextPath()%>/schemasets/browse/">Schemas</a></li> <%
        }
        %>
        <li><a href="<%=request.getContextPath()%>/vocabularies">Vocabularies</a></li>
        <%
        if (_user!=null){
            %>
            <li><a href="<%=request.getContextPath()%>/services/list">Services</a></li>
            <%
        }
        %>
    </ul>
</div>
