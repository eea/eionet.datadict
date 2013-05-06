<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="attributeId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="attributes" required="true" type="java.util.ArrayList" %>
<%@ attribute name="vocabularyConcepts" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldSize" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for related concepts which supports multiple values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.VocabularyConceptAttribute objects.
        - vocabularyConcepts is List of VocabularyConcept objects that can be related
        - attributeId numeric id of the attribute definition (in M_ATTRIBUTE table).
        - fieldName must be the Stripes bean property name (actionBean.vocabularyConcept.testAttribute)
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
<c:url var="delIcon" value="/images/button_remove.gif" />

<script type="text/javascript">
// <![CDATA[
( function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(attributes)};

        $("#multiAdd${uniqueId}").live("click", function(event){
            var newInput = $("#newField${uniqueId}").clone(true);
            newInput.attr("id", "multySpan${uniqueId}-" + currentSize);
            newInput.find("input[type='text']").attr("name", "${fieldName}[" + currentSize + "].linkText");
            newInput.find("select").attr("name", "${fieldName}[" + currentSize + "].relatedId");
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
    <div id="newField${uniqueId}">
        <input type="hidden" name="" value="${attributeId}" />
        <select name="">
            <option value=""></option>
            <c:forEach var="concept" items="${vocabularyConcepts}">
                <option value="${concept.id}"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
            </c:forEach>
        </select>
        <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
        <%--
        <fieldset>
            <legend>Link text</legend>
            <input name="" class="${fieldClass}" size="${fieldSize}" type="text">
        </fieldset>
         --%>
    </div>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${attributes}" varStatus="innerLoop">
        <c:if test="${attr.id != 0}">
        <div id="multySpan${uniqueId}-${innerLoop.index}">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
            <input type="hidden" name="${fieldName}[${innerLoop.index}].relatedId" value="${attr.relatedId}" />
            <select name="select-${fieldName}[${innerLoop.index}].relatedId" disabled="disabled">
                <option value=""></option>
                <c:forEach var="concept" items="${vocabularyConcepts}">
                    <c:choose>
                        <c:when test="${concept.id eq attr.relatedId}">
                            <option value="${concept.id}" selected="selected"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
                        </c:when>
                        <c:otherwise>
                            <option value="${concept.id}"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select>
            <a href='#' class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
            <%--
            <fieldset>
                <legend>Link text</legend>
                <input value="${attr.linkText}" name="${fieldName}[${innerLoop.index}].linkText" class="${fieldClass}" size="${fieldSize}" type="text">
            </fieldset>
            --%>
        </div>
        </c:if>
    </c:forEach>
</div>
<a href="#" id="multiAdd${uniqueId}">Add new</a>