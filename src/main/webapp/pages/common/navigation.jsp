<%@page import="eionet.util.SecurityUtil,eionet.meta.DDUser"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li><a href="<%=request.getContextPath()%>/documentation">Help and documentation</a></li>
        <li><a href="<%=request.getContextPath()%>/datasets.jsp">Datasets</a></li>
        <li><a href="<%=request.getContextPath()%>/search_results_tbl.jsp">Tables</a></li>
        <li><a href="<%=request.getContextPath()%>/search.jsp">Data elements</a></li>
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
        if (SecurityUtil.userHasPerm(request, "/schemasets", "v")){ %>
          <li><a href="<%=request.getContextPath()%>/schemaSets.action">Schemas</a></li> <%
        }
        %>
    </ul>
</div>
