<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="elementId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dataElements" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldSize" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for text attribute which supports multiple values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.DataElement objects.
        - elementId numeric id of DataElement object.
        - dataElements must be the Stripes bean property name (actionBean.vocabularyConcept.dataElementValue)
        - uniqueId id that is used as suffix for ids of different html elements
        - fieldSize and fieldClass are optional with default values "30" and "smalltext"
 --%>

<c:if test="${empty fieldSize}">
    <c:set var="fieldSize" value="30" />
</c:if>
<c:if test="${empty fieldClass}">
    <c:set var="fieldClass" value="smalltext" />
</c:if>

<c:url var="addIcon" value="/images/button_plus.gif" />

<script type="text/javascript">
// <![CDATA[
(function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(dataElements)};

        $("#multiAdd${uniqueId}").live("click", function(event) {
            clearSysMsg();
            var newInput = $("#newField${uniqueId}").clone(true);
            newInput.attr("id", "multiDiv${uniqueId}-" + currentSize);
            newInput.find("input[type='text']").attr("name", "${fieldName}[" + currentSize + "].attributeValue");
            newInput.find("input[type='hidden']").attr("name", "${fieldName}[" + currentSize + "].id");
            newInput.find("input[type='hidden']").attr("value", "${elementId}");

            //newInput.find("select").attr("id", "lang${fieldName}-" + currentSize);
            newInput.find("select").attr("name", "${fieldName}[" + currentSize + "].attributeLanguage");
            newInput.appendTo("#multiDiv${uniqueId}");
            currentSize++;
            event.preventDefault();
        });
    });
})(jQuery);
// ]]>
</script>

<div style="display:none">
    <div id="newField${uniqueId}" class="delLinkWrapper">
        <input type="hidden" name="" value="${elementId}" />
        <input class="smalltext" size="${fieldSize}" type="text" />
        <dd:selectLang value="en" name="" />
        <a href="#" class="delLink deleteButton" title="Remove"></a>
    </div>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${dataElements}" varStatus="innerLoop">
        <c:if test="${!empty attr.attributeValue}">
            <div id="multiDiv${uniqueId}-${innerLoop.index}" class="delLinkWrapper">
                <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
                <input value="${attr.attributeValue}" name="${fieldName}[${innerLoop.index}].attributeValue" class="${fieldClass}" size="${fieldSize}" type="text" />
                <dd:selectLang value="${attr.attributeLanguage}" name="${fieldName}[${innerLoop.index}].attributeLanguage" />
                <a href="#" class="delLink deleteButton" title="Remove"></a>
            </div>
        </c:if>
    </c:forEach>
</div>
<br />
<a href="#" id="multiAdd${uniqueId}">Add new</a>