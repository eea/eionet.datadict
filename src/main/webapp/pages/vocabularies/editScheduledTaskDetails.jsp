<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit scheduled synchronization of vocabulary" currentSection="vocabularies">
    <stripes:layout-component name="head">
        <script type="text/javascript">
            (function ($) {
                function isValidEmail(email) {
                    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
                    return regex.test(email);
                }

                $(document).ready(function () {
                    $('#emails').tagsInput({
                        'defaultText': 'add Email',
                        'onAddTag': function (tag) {
                            console.log("on Add Tag" + tag);
                            if (!isValidEmail(tag)) {
                                $('.email').each(function ()
                                {
                                    if ($(this).text().trim() === tag) {
                                        $(this).parent().css('background', '#FBD8DB').css('color', '#90111A').css('border-color', '#FBD8DB');
                                    }
                                });
                            }
                        }
                    });
                    $('.numbersOnly').keyup(function () {
                        this.value = this.value.replace(/[^0-9\.]/g, '');
                    });
                });
            })(jQuery);

        </script>
        <script>jQuery.noConflict();</script>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Edit scheduled synchronization of vocabulary ${actionBean.vocabularyFolder.identifier}</h1>
        <div id="drop-operations">
            <ul>
                <li class="back">
                    <stripes:link beanclass="eionet.web.action.VocabularyFolderActionBean" event="view">
                        <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                        <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                        Back to vocabulary
                    </stripes:link>
                </li>
            </ul>
        </div>

        <stripes:form id="scheduleVocabularySync" method="post" beanclass="${actionBean['class'].name}" style="padding-top:20px">
            <div id="outerframe">
                <stripes:param name="vocabularyFolder.folderName" value="${actionBean.vocabularyFolder.folderName}" />
                <stripes:param name="vocabularyFolder.id" value="${actionBean.vocabularyFolder.id}" />
                <stripes:param name="vocabularyFolder.identifier" value="${actionBean.vocabularyFolder.identifier}" />
                <stripes:param name="vocabularyFolder.workingCopy" value="${actionBean.vocabularyFolder.workingCopy}" />
                <table class="datatable results">
                    <colgroup>
                        <col style="width:26%"/>
                        <col style="width:4%"/>
                        <col />
                    </colgroup>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Vocabulary RDF URL
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="60" name="vocabularyRdfUrl" id="vocabularyRdfUrl" value="${actionBean.vocabularyRdfUrl}"/>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Emails
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text class="smalltext" size="60" name="emails" id="emails" value="${actionBean.emails}"/>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            Schedule interval
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <stripes:text id="interval" name="scheduleInterval" size="10" value="${actionBean.scheduleInterval}" class="numbersOnly" />
                            <stripes:select name="schedulingIntervalUnit">
                                <stripes:options-enumeration enum="eionet.datadict.model.enums.Enumerations$SchedulingIntervalUnit" label="label" />
                            </stripes:select>
                        </td>
                    </tr>

                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            How to handle existing concepts
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <div>
                                <stripes:radio id="rdfDontPurge" name="rdfPurgeOption" value="1" />
                                <label for="rdfDontPurge" class="question">Don't purge vocabulary data</label>
                                <div class="elaboration">
                                    In this case, existing vocabulary information will be updated with information from imported concepts.
                                </div>
                            </div>
                            <div>
                                <stripes:radio id="rdfPurgePerPredicate" name="rdfPurgeOption" value="2"/>
                                <label for="rdfPurgePerPredicate" class="question">Purge Per Predicate</label>
                                <div class="elaboration">
                                    In this case, predicates of existing concepts will be replaced with the imported predicates.
                                </div>
                            </div>
                            <div>
                                <stripes:radio id="rdfPurgeVocabularyData" name="rdfPurgeOption" value="3"/>
                                <label for="rdfPurgeVocabularyData" class="question">Purge All Vocabulary Data</label>
                                <div class="elaboration">
                                    In this case, all existing concepts will be removed and the imported concepts will be added.
                                </div>
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <th scope="row" class="scope-row simple_attr_title">
                            How to handle concepts missing from import
                        </th>
                        <td class="simple_attr_help">
                            <dd:mandatoryIcon />
                        </td>
                        <td class="simple_attr_value">
                            <div class="strategies">
                                <div class="strategy-ignore">
                                    <stripes:radio id="strategy-ignore" name="missingConceptsAction" value="keep" checked="keep"/>
                                    <label for="strategy-ignore" class="question">Maintain as is, ignore</label>
                                </div>
                                <div class="strategy-remove">
                                    <stripes:radio id="strategy-remove" name="missingConceptsAction" value="remove"/>
                                    <label for="strategy-remove" class="question">Remove</label>
                                </div>
                                <div class="strategy-status-invalid">
                                    <stripes:radio id="strategy-status-invalid" name="missingConceptsAction" value="invalid"/>
                                    <label for="strategy-status-invalid" class="question">Maintain, but update status to "Invalid"</label>
                                </div>
                                <div class="strategy-status-deprecated">
                                    <stripes:radio id="strategy-status-deprecated" name="missingConceptsAction" value="deprecated"/>
                                    <label for="strategy-status-deprecated" class="question">Maintain, but update status to "Deprecated"</label>
                                </div>
                                <div class="strategy-status-deprecated-retired">
                                    <stripes:radio id="strategy-status-deprecated-retired" name="missingConceptsAction" value="retired"/>
                                    <label for="strategy-status-deprecated-retired" class="question">Maintain, but update status to "Deprecated-Retired"</label>
                                </div>
                                <div class="strategy-status-deprecated-superseded">
                                    <stripes:radio id="strategy-status-deprecated-superseded" name="missingConceptsAction" value="superseded"/>
                                    <label for="strategy-status-deprecated-superseded" class="question">Maintain, but update status to "Deprecated-Superseded"</label>
                                </div>
                            </div>

                        </td>
                    </tr>
                    <tr>
                        <th>&nbsp;</th>
                        <td colspan="2">
                            <stripes:param name="scheduledTaskId" value="${actionBean.scheduledTaskId}" />
                            <stripes:submit name="updateScheduledJob" value="Update" class="mediumbuttonb"/>
                            <stripes:submit name="cancelSave" value="Cancel" class="mediumbuttonb"/>
                        </td>
                    </tr>
                </table>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
