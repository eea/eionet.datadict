/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, TripleDev OÜ
 * Enriko Käsper, TripleDev OÜ
 * Risto Alt, TripleDev OÜ
 * Juhan Voolaid, TripleDev OÜ
 */

/**
 * Toggles the "select all" feature for ALL checkboxes found inside
 * the given form.
 *
 * Input parameters:
 *   formId - the id of the form object where these checkboxes are looked for
 *
 * Return value: none
 */
function toggleSelectAll(formId) {
    formobj = document.getElementById(formId);
    checkboxes = formobj.getElementsByTagName('input');
    var isAllSelected = (formobj.selectAll.value == "Select all") ? false : true;

    if (isAllSelected == null || isAllSelected == false) {
        for (i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox' && !checkboxes[i].disabled) {
                checkboxes[i].checked = true ;
            }
        }
        formobj.selectAll.value = "Deselect all";
    } else {
        for (i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox' && !checkboxes[i].disabled) {
                checkboxes[i].checked = false ;
            }
        }
        formobj.selectAll.value = "Select all";
    }
}

/**
 * Toggles the "select all" feature for given checkbox field.
 *
 * Input parameters:
 *   formId - the id of the form object where these checkboxes are looked for
 *   field - the name of the checkbox field
 *
 * Return value: none
 */
function toggleSelectAllForField(formId, fieldName) {
    formobj = document.getElementById(formId);
    checkboxes = formobj.getElementsByTagName('input');
    var isAllSelected = (formobj.selectAll.value == "Select all") ? false : true;

    if (isAllSelected == null || isAllSelected == false) {
        for (i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox' && checkboxes[i].name == fieldName && !checkboxes[i].disabled) {
                checkboxes[i].checked = true ;
            }
        }
        formobj.selectAll.value = "Deselect all";
    } else {
        for (i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox' && checkboxes[i].name == fieldName && !checkboxes[i].disabled) {
                checkboxes[i].checked = false ;
            }
        }
        formobj.selectAll.value = "Select all";
    }
}
