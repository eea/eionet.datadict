<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="attributeId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="attributes" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldRows" required="false" %>
<%@ attribute name="fieldCols" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for text attribute which supports multiple values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.VocabularyConceptAttribute objects.
        - attributeId numeric id of the attribute definition (in M_ATTRIBUTE table).
        - fieldName must be the Stripes bean property name (actionBean.vocabularyConcept.testAttribute)
        - uniqueId id that is used as suffix for ids of different html elements
        - fieldRows, fieldCols and fieldClass are optional with default values "50", "3" and "smalltext"
 --%>

<c:if test="${empty fieldRows}">
    <c:set var="fieldRows" value="3" />
</c:if>
<c:if test="${empty fieldCols}">
    <c:set var="fieldCols" value="40" />
</c:if>
<c:if test="${empty fieldClass}">
    <c:set var="fieldClass" value="smalltext" />
</c:if>

<c:url var="addIcon" value="/images/button_plus.gif" />
<c:url var="delIcon" value="/images/button_remove.gif" />

<script type="text/javascript">
// <![CDATA[
( function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(attributes)};

        $("#multiAdd${uniqueId}").live("click", function(event){
            var newInput = $("#newField${uniqueId}").clone(true);
            newInput.attr("id", "multySpan${uniqueId}-" + currentSize);
            newInput.find("textarea").attr("name", "${fieldName}[" + currentSize + "].value");
            newInput.find("input[type='hidden']").attr("name", "${fieldName}[" + currentSize + "].attributeId");
            newInput.appendTo("#multiDiv${uniqueId}");
            currentSize++;
            event.preventDefault();
        });

    });
} ) ( jQuery );
// ]]>
</script>

<div style="display:none">
    <span id="newField${uniqueId}">
        <input type="hidden" name="" value="${attributeId}" />
        <textarea class="${fieldClass}" rows="${fieldRows}" cols="${fieldCols}"></textarea>
        <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
        <br/>
    </span>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${attributes}" varStatus="innerLoop">
        <c:if test="${!empty attr.value}">
        <span id="multySpan${uniqueId}-${innerLoop.index}">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].attributeId" value="${attr.attributeId}" />
            <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
            <textarea name="${fieldName}[${innerLoop.index}].value" class="${fieldClass}" rows="${fieldRows}" cols="${fieldCols}">${attr.value}</textarea>
            <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a><br>
        </span>
        </c:if>
    </c:forEach>
</div>
<a href="#" id="multiAdd${uniqueId}">Add new</a>