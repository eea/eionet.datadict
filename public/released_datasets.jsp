<%@page contentType="text/html;charset=UTF-8" import="java.io.*,java.util.*,eionet.meta.*,eionet.util.*,com.tee.xmlserver.*"%>

<%
request.setCharacterEncoding("UTF-8");
Vector releasedDatasets = (Vector)request.getAttribute("rlsd_datasets");
%>

<h2>Latest released data definitions</h2>
<!-- <table border="0" width="100%" cellspacing="0" cellpadding="3" style="bordercolorlight:#C0C0C0; bordercolordark:#C0C0C0; border: 1px solid #FF9900"> -->
<table width="100%">
		<%
		for (int i=0; releasedDatasets!=null && i<releasedDatasets.size(); i++){
			Dataset dst = (Dataset)releasedDatasets.get(i);
			
			String name = dst.getName();
			if (name==null) name = dst.getShortName();
			if (name==null) name = dst.getIdentifier();
			
			String date = dst.getDate();
			date = date==null ? "" : eionet.util.Util.releasedDate(Long.parseLong(date));
			%>
			<tr>				
				<td style="width:70%;vertical-align:top">
					<a href="dataset.jsp?mode=view&amp;ds_id=<%=dst.getID()%>">
						<%=Util.replaceTags(name)%>
					</a>
				</td>
				<td style="width:23%;vertical-align:top">
					<%=date%>
				</td>
				<td style="width:7%;vertical-align:top;text-align:center">
					<a href="GetPrintout?format=PDF&amp;obj_type=DST&amp;obj_id=<%=dst.getID()%>&amp;out_type=GDLN">
						<img src="images/icon_pdf.jpg" style="border:0" width="17" height="18" alt="PDF" title="Definition as a PDF file"/>
					</a>
				</td>
			</tr>
			<%
		}
		
		if (releasedDatasets.size()==0){
			%>
			<tr>
				<td style="width:100%;vertical-align:top" colspan="3">
					No released dataset definitions found at the moment!
				</td>
			</tr>
			<%
		}
		else{
			%>
			<tr>
				<td style="width:100%;vertical-align:top;text-align:right" colspan="3">
					[<a href="datasets.jsp?SearchType=SEARCH">More...</a>]
				</td>
			</tr>
			<%
		}
		%>
		
</table>
