<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="elementId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dataElements" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldSize" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for related reference concepts which supports multiple values.
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
    <c:set var="fieldSize" value="68" />
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

            $("#referenceAdd${uniqueId}").live("click", function(event){
                $('#add-value${uniqueId}').dialog('open');
                return false;
            });


            $("#add-value${uniqueId}").dialog({
                autoOpen: false,
                resizable: false,
                maxHeight: 300,
                width: 500,
                modal: true,
                buttons: {
                    "Yes, find concept in DD" : function() {
                        clearSysMsg();
                        var action = "saveConcept";
                        $('#txtEditDivId').attr('value', 'findVocabularyDiv');
                        //submit data to not loose changes, TODO: handle response
                        $("#editForm").attr('action', action).ajaxSubmit({type: 'post'});
                        var elementId=${elementId};

                        //if CH3 - skip vocabulary search as vocabulary is related to the element
                        if ("${dataElements[0].type}" == "CH3") {
                            document.getElementById('txtEditDivId').value='addConceptDiv';
                            document.getElementById('txtConceptElementId').value=elementId;
                            document.getElementById('txtVocabularyId').value="${dataElements[0].vocabularyId}";
                            document.getElementById('nonCh3Div').style.display = "none";
                            openPopup("#addConceptDiv");
                        } else {
                            document.getElementById('nonCh3Div').style.display = "block";
                            openVocabularySearch(elementId);
                        }
                        $(this).dialog("close");
                        return false;
                    },
                    "No, enter the URL": function() {
                        clearSysMsg();
                        var newInput = $("#newField${uniqueId}").clone(true);
                        newInput.attr("id", "multySpan${uniqueId}-" + currentSize);
                        newInput.find("input[id='elem-${uniqueId}.id']").attr("name", "${fieldName}[" + currentSize + "].id");
                        newInput.find("input[id='elem-${uniqueId}.id']").attr("value", "${elementId}");
                        newInput.find("input[type='text']").attr("name", "${fieldName}[" + currentSize + "].attributeValue");

                        newInput.appendTo("#multiDiv${uniqueId}");
                        currentSize++;

                        $(this).dialog("close");
                        newInput.find("input[type='text']").focus();
                    }
                }
            });

        });
    }) ( jQuery );



    // ]]>
</script>
<!--  text field for adding regular URL -->
<div style="display:none">
    <div id="newField${uniqueId}">
        <input type="hidden" id="elem-${uniqueId}.id" name="" value="${elementId}" />
        <input type="hidden" id="identifier-${uniqueId}.identifier" name="" value="${dataElements[0].identifier}" />
        <input  type="text"  name="${fieldName}[${uniqueId}].attributeValue" value="" class="${fieldClass}" size="${fieldSize}"/>
        <a href="#" class="delLink"><img style='border:0' src='${delIcon}' alt='Remove' /></a>
    </div>
</div>

<div id="multiDiv${uniqueId}">
    <c:forEach var="attr" items="${dataElements}" varStatus="innerLoop">
        <c:if test="${not empty attr.attributeValue or (not empty attr.relatedConceptId && attr.relatedConceptId != 0)}">
            <div id="multySpan${uniqueId}-${innerLoop.index}">
                <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
                <input type="hidden" name="${fieldName}[${innerLoop.index}].identifier" value="${attr.identifier}" />
                <stripes:hidden name="elementId" value="${elementId}"/>

                <c:choose>
                    <c:when test="${attr.relationalElement}">
                        <stripes:hidden name="${fieldName}[${innerLoop.index}].relatedConceptId" value="${attr.relatedConceptId}" />

                        <a href="${actionBean.conceptViewPrefix}${attr.relatedConceptRelativePath}/view"><c:out value="${attr.relatedConceptIdentifier}" />
                            <c:if test="${not empty attr.relatedConceptLabel}">
                                (<c:out value="${attr.relatedConceptLabel}" />)
                            </c:if>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <input name="${fieldName}[${innerLoop.index}].attributeValue" value="${attr.attributeValue}" class="${fieldClass}" size="${fieldSize}" type="text"/>
                    </c:otherwise>
                </c:choose>

                <a href='#' class="delLink"><img style='border:0' src='${delIcon}' alt='Remove'/></a>
            </div>
        </c:if>
    </c:forEach>
</div>

<div id="add-value${uniqueId}" title="Add reference to concept" style="display:none">
    <p>Do you want to add a concept maintained in another Data Dictionary vocabulary?</p>
</div>
<br/>
<a href="#" id="referenceAdd${uniqueId}">Add new</a>

