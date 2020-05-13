<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/pages/common/taglibs.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="../../headerinfo.jsp" %>
    <title>Error</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
    <style>
        .error-template {padding: 40px 15px;text-align: center;color: red;}
    </style>
</head>
<body>
<div id="container">
    <jsp:include page="../../nlocation.jsp" flush="true">
        <jsp:param name="name" value="Error page"/>
        <jsp:param name="helpscreen" value="error_page"/>
    </jsp:include>
    <c:set var="currentSection" value="errorPage" />
    <%@ include file="/pages/common/navigation.jsp" %>
    <div id="workarea">
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <div class="error-template">
                        <h1 style="color:red">${msgOne}</h1>
                        <div>Sorry, an error has occured!</div>
                    </div>
                </div>
            </div>
        </div>
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="../../footer.jsp" %>
</body>
</html>
