<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/pages/common/taglibs.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="../../headerinfo.jsp" %>
    <title>Admin tools</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
    <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    <script src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
    <script>
        $(function() {
            $("#ch" ).autocomplete({
                source: "ldapOptions"
            });
        });

        $(document).ready(function(){
            $('#myBtn').click(function(){
                $('#myForm').toggle(500);
            });
        });

        $(document).ready(function(){
            $('#mySecondBtn').click(function(){
                $('#mySecondForm').toggle(500);
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
    <c:set var="currentSection" value="groups" />
    <%@ include file="/pages/common/navigation.jsp" %>
    <div id="workarea">
        <table id="tab" class="table border">
            <thead>
            <tr>
                <th>Member</th>
                <th>LDAP_Group</th>
                <th>DD_Group</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody id="myTable">
            <c:forEach var="ddGroup" items="${ddGroups}" varStatus="loop">
                <c:forEach var="member" items="${ddGroupsAndUsers.get(ddGroup)}">
                    <c:url var="removeUser" value="/v2/admintools/removeUser">
                        <c:param name="ddGroupName" value="${ddGroup}" />
                        <c:param name="memberName" value="${member}" />
                    </c:url>
                    <c:choose>
                        <c:when test="${not empty memberLdapGroups.get(member)}">
                            <c:forEach var="memberLdapGroup" items="${memberLdapGroups.get(member)}">
                                <tr>
                                    <td>${member}</td>
                                    <td>${memberLdapGroup}</td>
                                    <td>${ddGroup}</td>
                                    <td style="cursor: pointer;"><a href="${removeUser}">Remove</a></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>${member}</td>
                                <td>-</td>
                                <td>${ddGroup}</td>
                                <td style="cursor: pointer;"><a href="${removeUser}">Remove</a></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </c:forEach>
            </tbody>
        </table>
        <button id="myBtn" class="btn btn-info" style="margin-bottom:5px">Add User</button></br></br>
        <form:form id="myForm" style="display:none" action="addUser" modelAttribute="groupDetails" method="post">
            <form:select path="groupNameOptionOne" items="${ddGroups}"/>
            <form:input path="userName" placeholder="Enter user name"/>
            <input type="submit" class="btn btn-info" name="submit" value="Submit"/>
        </form:form>
        <button id="mySecondBtn" class="btn btn-info" style="margin-bottom:5px">Add LDAP Group</button></br></br>
        <form:form id="mySecondForm" style="display:none" action="addUser" modelAttribute="groupDetails" method="post">
            <form:select path="groupNameOptionTwo" items="${ddGroups}"/>
            <form:input id="ch" path="ldapGroupName" placeholder="Enter LDAP group name"/>
            <input type="submit" class="btn btn-info" name="submit" value="Submit"/>
        </form:form>
    </div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="../../footer.jsp" %>
<script>
    $(document).ready( function () {
        $('#tab').DataTable({
            "order": [[2, "asc"]]
        });
        $('.dataTables_length').addClass('bs-select');
    });
</script>
</body>
</html>
