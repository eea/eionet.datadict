<%@page %>

<%!

public String getSortedImg(int curColNr, Integer oSortCol, Integer oSortOrder){
    
    boolean sorted = oSortCol!=null && oSortCol.intValue()==curColNr;
    String sortedImg = "images/sortnot.gif";
    if (sorted && oSortOrder!=null)
        sortedImg = oSortOrder.intValue()>0 ? "images/sortup.gif" : "images/sortdown.gif";
    
    return sortedImg;
}

public String getSortedLink(int curColNr, Integer oSortCol, Integer oSortOrder){
    
    boolean sorted = oSortCol!=null && oSortCol.intValue()==curColNr;
    int newSortOrder = (sorted && oSortOrder!=null) ? 0-oSortOrder.intValue() : 1;
    
    StringBuffer sortLink = new StringBuffer("javascript:showSortedList(");
    sortLink.append(curColNr);
    sortLink.append(", ");
    sortLink.append(newSortOrder);
    sortLink.append(")");
    
    return sortLink.toString();
}

public String getSortedAlt(String sortedImg){
    
    if (sortedImg==null)
        return "";
    else if (sortedImg.endsWith("sortup.gif"))
        return "Sorted up";
    else if (sortedImg.endsWith("sortdown.gif"))
        return "Sorted down";
    else
        return "";
}
%>