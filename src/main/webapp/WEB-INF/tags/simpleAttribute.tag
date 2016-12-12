<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="attributes" required="true" type="java.util.ArrayList" %>

<%--
    Input tag for dynamic attributes which support also multiple values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.SimpleAttribute objects.
        - fieldName must be the Stripes bean property name (actionBean.vocabularyFolder.testAttribute)
        - uniqueId id that is used as suffix for ids of different html elements
 --%>

<c:set var="attrMeta" value="${attributes[0]}"/>

<%-- Single value --%>
<c:if test="${not attrMeta.multiValue}">
    <input type="hidden" name="${fieldName}[0].attributeId" value="${attrMeta.attributeId}" />
    <c:if test="${attrMeta.inputType eq 'textarea'}">
        <stripes:textarea name="${fieldName}[0].value" rows="${attrMeta.height}" cols="${attrMeta.width}" class="smalltext" />
    </c:if>

    <c:if test="${attrMeta.inputType eq 'text'}">
        <stripes:text name="${fieldName}[0].value" size="${attrMeta.width}" class="smalltext" />
    </c:if>

</c:if>


<%-- Multiple values --%>
<c:if test="${attrMeta.multiValue}">

<script type="text/javascript">
// <![CDATA[
(function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(attributes)};

        $("#multiAdd${uniqueId}").click(function(event){
            var newInput = $("#newField${uniqueId}").clone(true);
            newInput.attr("id", "multyVal${uniqueId}-" + currentSize);
            if ('${attrMeta.inputType}' == 'text') {
                newInput.find("input[type='text']").attr("name", "${fieldName}[" + currentSize + "].value");
            }
            if ('${attrMeta.inputType}' == 'textarea') {
                newInput.find("textarea").attr("name", "${fieldName}[" + currentSize + "].value");
            }
            newInput.find("input[type='hidden']").attr("name", "${fieldName}[" + currentSize + "].attributeId");
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
        <input type="hidden" name="" value="${attrMeta.attributeId}" />
        <c:if test="${attrMeta.inputType eq 'text'}">
            <input class="smalltext" size="${attrMeta.width}" type="text">
        </c:if>
        <c:if test="${attrMeta.inputType eq 'textarea'}">
            <textarea class="smalltext" rows="${attrMeta.height}" cols="${attrMeta.width}"></textarea>
        </c:if>
        <a href="#" class="delLink deleteButton" title="Remove"></a>
    </div>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${attributes}" varStatus="innerLoop">
        <div id="multyVal${uniqueId}-${innerLoop.index}" class="delLinkWrapper">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].attributeId" value="${attr.attributeId}" />
            <c:if test="${attrMeta.inputType eq 'textarea'}">
                <stripes:textarea name="${fieldName}[${innerLoop.index}].value" rows="${attrMeta.height}" cols="${attrMeta.width}" class="smalltext" />
            </c:if>
            <c:if test="${attrMeta.inputType eq 'text'}">
                <stripes:text name="${fieldName}[${innerLoop.index}].value" size="${attrMeta.width}" class="smalltext" />
            </c:if>
            <a href="#" class="delLink deleteButton" title="Remove"></a>
        </div>
    </c:forEach>
</div>
<br />
<a href="#" id="multiAdd${uniqueId}">Add new</a>

</c:if>