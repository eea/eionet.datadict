<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="elementId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dataElements" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fixedValues" required="true" type="java.util.ArrayList" %>

<%--
    Input tag for text attribute which supports multiple fixed values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.DataElement objects.
        - elementId numeric id of DataElement object.
        - dataElements must be the Stripes bean property name (actionBean.vocabularyConcept.dataElementValue)
        - uniqueId id that is used as suffix for ids of different html elements
        - fixedValues is list of fixed values to be shown in a dropdown

 --%>

<c:url var="addIcon" value="/images/button_plus.gif" />
<c:url var="delIcon" value="/images/button_remove.gif" />

<script type="text/javascript">
// <![CDATA[
( function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(dataElements)};

        $("#multiAddFixed${uniqueId}").live("click", function(event){

          var newSelect = $("#newFixedField${uniqueId}").clone(true);

          newSelect.find("select").attr("name", "${fieldName}[" + currentSize + "].attributeValue");
          newSelect.find("input[type='hidden']").attr("value", "${elementId}");
          newSelect.find("input[type='hidden']").attr("name",  "${fieldName}[" + currentSize + "].id");
          newSelect.find("select").attr("id", "elem${fieldName}[" + currentSize + "]");

          newSelect.appendTo("#multiDiv${uniqueId}");
          currentSize++;
          event.preventDefault();
        });

    });
} ) ( jQuery );
// ]]>
</script>

<div style="display:none">
    <span id="newFixedField${uniqueId}">
        <input type="hidden" name="" value="${attributeId}" />
        <dd:selectFixedValue fixedValues="${fixedValues}" value="" id="elem${uniqueId}" ></dd:selectFixedValue>
        <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
        <br/>
    </span>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${dataElements}" varStatus="innerLoop">
        <c:if test="${!empty attr.attributeValue}">
        <span id="multySpan${uniqueId}-${innerLoop.index}">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
            <dd:selectFixedValue fixedValues="${attr.fixedValues}" value="${attr.attributeValue}" name="${fieldName}[${innerLoop.index}].attributeValue" id="elem${fieldName}[${innerLoop.index}]" ></dd:selectFixedValue>
            <a href='#' class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a><br>
        </span>
        </c:if>

    </c:forEach>
</div>
<a href="#" id="multiAddFixed${uniqueId}">Add new</a>
