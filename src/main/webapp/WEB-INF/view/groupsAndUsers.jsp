<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ include file="/pages/common/taglibs.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="../../headerinfo.jsp" %>
    <title>Admin tools</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    <script src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
    <script>
        $(function() {
            $('#groupsAndUsers').DataTable({
                "order": [[1, "asc"]]
            });

            $('.dataTables_length').addClass('bs-select');


            $('#myBtn').click(function(){
                $('#myForm').toggle(500);
            });
        });
    </script>
</head>
<body>
<div id="container">
    <jsp:include page="../../nlocation.jsp" flush="true">
        <jsp:param name="name" value="Admin tools"/>
        <jsp:param name="helpscreen" value="admin_tools"/>
    </jsp:include>
    <c:set var="currentSection" value="administration" />
    <%@ include file="/pages/common/navigation.jsp" %>
    <div id="workarea">
        <table id="groupsAndUsers" class="table border">
            <thead>
            <tr>
                <th>User</th>
                <th>DD Group</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ddGroup" items="${ddGroups}" varStatus="loop">
                <c:forEach var="member" items="${ddGroupsAndUsers.get(ddGroup)}">
                    <c:url var="removeUser" value="/v2/admintools/removeUser">
                        <c:param name="ddGroupName" value="${ddGroup}" />
                        <c:param name="memberName" value="${member}" />
                    </c:url>
                    <tr>
                        <td>${member}</td>
                        <td>${ddGroup}</td>
                        <td style="cursor: pointer;"><a class="text-info" style="text-decoration: underline" href="${removeUser}">Remove</a></td>
                    </tr>
                </c:forEach>
            </c:forEach>
            </tbody>
        </table>
        <button id="myBtn" class="btn btn-info" style="margin-bottom:5px">Add User</button></br></br>
        <form:form id="myForm" style="display:none" action="${pageContext.request.contextPath}/v2/admintools/addUser" modelAttribute="groupDetails" method="post">
            <form:select path="group" items="${ddGroups}"/>
            <form:input path="userName" placeholder="Enter user name"/>
            <input type="submit" class="btn btn-info" name="submit" value="Submit"/>
        </form:form>
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="../../footer.jsp" %>
<script>

</script>
</body>
</html>
