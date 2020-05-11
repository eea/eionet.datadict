<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/pages/common/taglibs.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="../../headerinfo.jsp" %>
    <title>Error</title>
    <style>
        #msgOne {
            float: left;
        }
        #msgCont {
            padding: 0px;
            color: red;
            text-align: center;
            border: 1px solid red;
            font-size: small;
        }
    </style>
</head>
<body>
<div id="container">
    <jsp:include page="../../nlocation.jsp" flush="true">
        <jsp:param name="name" value="Admin tools"/>
        <jsp:param name="helpscreen" value="groups_and_users"/>
    </jsp:include>
    <c:set var="currentSection" value="groups" />
    <%@ include file="/pages/common/navigation.jsp" %>
    <div id="workarea">
        <div id="msgCont">
            <h1 id="msgOne">${msgOne}</h1>
            <h1 id="msgTwo">${msgTwo}</h1>
        </div>
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="../../footer.jsp" %>
</body>
</html>
