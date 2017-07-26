<%@ include file="/pages/common/taglibs.jsp"%>

<script type="text/javascript">
    // <![CDATA[
    (function($) {
        $(document).ready(function() {
            $("#switchTypeLink").click(function() {
                $('#switchTypeDialog').dialog('open');
                return false;
            });

            $('#switchTypeDialog').dialog({
                autoOpen: false,
                width: 500,
                modal:true
            });

            $("#closeSwitchTypeDialog").click(function() {
                $('#switchTypeDialog').dialog("close");
                return true;
            });
        });
    })(jQuery);
    // ]]>
</script>

<div id="switchTypeDialog" title="Switch data element type">
    <p>
        Use the below buttons to switch the type of this element to something else.<br/>
        Please note that this might result in the loss of attributes that are<br/>
        found irrelevant for the new type.
    </p>

    <form id="switchTypeForm" method="post" action="<%=request.getContextPath()%>/dataelements" style="margin-top:1em">

        <%
            String curType = request.getParameter("curType");
            if (!"CH1".equals(curType)) { %>
        <input type="submit" name="newTypeCH1" value="Switch to fixed values" style="display:block;margin-top:5px"/> <%
        }

        if (!"CH2".equals(curType)) { %>
        <input type="submit" name="newTypeCH2" value="Switch to quantitative values" style="display:block;margin-top:5px"/> <%
        }

        if (!"CH3".equals(curType)) { %>
        <input type="submit" name="newTypeCH3" value="Switch to vocabulary values" style="display:block;margin-top:5px"/> <%
        }
    %>

        <input type="button" id="closeSwitchTypeDialog" name="closeButton" value="Cancel" style="display:block;margin-top:5px"/>

        <div style="display:none">
            <input type="hidden" name="delem_id" value="<%=request.getParameter("elemId")%>"/>
            <input type="hidden" name="mode" value="edit"/>
            <input type="hidden" name="switch_type" value="true"/>
        </div>

    </form>
</div>