<%@page import="java.util.*"%>


<%!
private String changeParamInUrl(String sUrl, String sName, String sValue){
	int i;
	int j;
	String sBeg;
	String sEnd;
	String sStr;

	i = sUrl.indexOf(sName + "=");
	if (i > 0) {
		sBeg=sUrl.substring(0, i); 
		sStr=sUrl.substring(i);
		j = sStr.indexOf("&");
		if (j > 0)
		   sEnd = sStr.substring(j);
		else
		   sEnd= "";
			sUrl=sBeg + sName + "=" + sValue + sEnd ;
			}
	else	
		{
			j = sUrl.indexOf("?");
		if (j>0)
			sUrl = sUrl + "&" + sName + "=" + sValue;
		else
			sUrl = sUrl + "?" + sName + "=" + sValue;
		}
	return sUrl ;
}
%>
<%        
		int iTotal;		
		int iCurrPage;
		int iPageLen;
		int	iMaxNumPagesInSelector = 10;

		try{
			iTotal= Integer.parseInt(request.getParameter("total"));
		}
		catch(Exception e){
			iTotal = 0;
		}
		try{
	        iCurrPage= Integer.parseInt(request.getParameter("curr_page"));
		}
		catch(Exception e){
			iCurrPage=0;
		}
		try{
	        iPageLen= Integer.parseInt(request.getParameter("page_len"));
		}
		catch(Exception e){
			iPageLen=10;
		}
	    if (iCurrPage<0) iCurrPage=0;

		String qryString = request.getQueryString();
		String sUrl = qryString==null ? request.getRequestURI() : request.getRequestURI() + "?" + qryString;
		%>
			<P>Total results: <%=iTotal%></P>
		<%
            int iPageCount=iTotal/iPageLen;
            if (iTotal%iPageLen>0)
                iPageCount++;
            if (iCurrPage>=iPageCount)
                iCurrPage=iPageCount-1;
            int iBeginPage=0;
            int iEndPage=iPageCount-1;
            
            if (iPageCount>1) {
                if (iPageCount>iMaxNumPagesInSelector) {
                    int iHalfNums=iMaxNumPagesInSelector/2;
                    iBeginPage=iCurrPage-iHalfNums;
                    iEndPage=iCurrPage+iHalfNums-1;
                    if (iEndPage>iPageCount) {
                        iEndPage=iPageCount-1;
                        iBeginPage=iEndPage-iMaxNumPagesInSelector;
                    }
                    if (iBeginPage<0) {
                        iBeginPage=0;
                        iEndPage=iMaxNumPagesInSelector-1;
                    }
                }
				%>
                <BR>
				<!--  paging is not needed currently
                <CENTER>Result Pages:
                    <TABLE>
                        <TR>
                            <%if (iCurrPage>0) {	%>					
                                <TD><A href='<%=changeParamInUrl(sUrl, "page_number", Integer.toString(iCurrPage-1))%>'>Previous</A></TD>
                            <%}%>
	                            <%for (int i=iBeginPage;i<=iEndPage;i++) {
	                                if (i!=iCurrPage) {%>
	                                    <TD><A href='<%=changeParamInUrl(sUrl, "page_number", Integer.toString(i))%>'><%=i+1%></A></TD>
	                                <%}else{%>
	                                    <TD><%=i+1%></TD>
	                                <%}
	                              }
	                            if (iCurrPage<iPageCount-1) {%>
	                                <TD><A href='<%=changeParamInUrl(sUrl, "page_number", Integer.toString(iCurrPage+1))%>'>Next</A></TD>
	                            <%}%>
	                        </TR>
	                    </TABLE>
	                </CENTER>
					-->
	          <%}
		%>

