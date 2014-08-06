<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="elementId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dataElements" required="true" type="java.util.ArrayList" %>
<%@ attribute name="vocabularyConcepts" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldSize" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for related concepts which supports multiple values.
    For the delete button to work, $(".delLink").click javaScript function must be included.
    Parameters:
        - dataElements must be List of eionet.meta.dao.domain.DataElement objects.
        - vocabularyConcepts is List of VocabularyConcept objects that can be related
        - elementId numeric id of the element definition (in DATAELEM table).
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
        var currentSize = ${fn:length(dataElements)};

        $("#multiAdd${uniqueId}").live("click", function(event){
          clearSysMsg();
            var newInput = $("#newField${uniqueId}").clone(true);
            newInput.attr("id", "multySpan${uniqueId}-" + currentSize);

            newInput.find("select").attr("name", "${fieldName}[" + currentSize + "].relatedConceptId");
            //newInput.find("select").attr("id", "elem${fieldName}[" + currentSize + "].relatedConceptId");

            newInput.find("input[id='elem-${uniqueId}.id']").attr("name", "${fieldName}[" + currentSize + "].id");
            newInput.find("input[id='elem-${uniqueId}.id']").attr("value", "${elementId}");

            newInput.find("input[id='identifier-${uniqueId}.identifier']").attr("name", "${fieldName}[" + currentSize + "].identifier");
            //newInput.find("input[id='identifier${uniqueId}].identifier']").attr("value", "${dataElements[0].identifier}");


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
        <input type="hidden" id="elem-${uniqueId}.id" name="" value="${elementId}" />
        <input type="hidden" id="identifier-${uniqueId}.identifier" name="" value="${dataElements[0].identifier}" />
        <select name="">
            <option value=""> </option>
            <c:forEach var="concept" items="${vocabularyConcepts}">
                <option value="${concept.id}"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
            </c:forEach>
        </select>
        <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove'/></a>
    </div>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${dataElements}" varStatus="innerLoop">
        <c:if test="${not empty attr.relatedConceptId && attr.relatedConceptId != 0}">
        <div id="multySpan${uniqueId}-${innerLoop.index}">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
            <input type="hidden" name="${fieldName}[${innerLoop.index}].vocabularyId" value="${attr.vocabularyId}" />
            <input type="hidden" name="${fieldName}[${innerLoop.index}].relatedConceptId" value="${attr.relatedConceptId}" />
            <input type="hidden" name="${fieldName}[${innerLoop.index}].identifier" value="${attr.identifier}" />
            <select name="select-${fieldName}[${innerLoop.index}].relatedConceptId" disabled="disabled">
                <option value=""> </option>
                <c:forEach var="concept" items="${vocabularyConcepts}">
                    <c:choose>
                        <c:when test="${concept.id eq attr.relatedConceptId}">
                            <option value="${concept.id}" selected="selected"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
                        </c:when>
                        <c:otherwise>
                            <option value="${concept.id}"><c:out value="${concept.identifier}" /> (<c:out value="${concept.label}" />)</option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select>
            <a href='#' class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
        </div>
        </c:if>
    </c:forEach>
</div>
<a href="#" id="multiAdd${uniqueId}">Add new</a>