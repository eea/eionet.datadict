function form_changed(form_name) {
    document.forms[form_name].elements["changed"].value="1";
}

function pop(link) {
    // it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
    // null will load into the opener if the opener itself has also been opened by another window!!!
    window.open(link,"","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}

function populateInput(formName, inputName, popValues) {
    if (document.forms[formName] == undefined) {
        return;
    }

    var elmsIdx;
    var elms = document.forms[formName].elements;
    
    for (elmsIdx = 0; elmsIdx < elms.length; elmsIdx++) {
        var elm = elms[elmsIdx];
        if (elm.name==inputName) {
            if (elm.type == "text" || elm.type == "textarea") {
                elm.value=popValues[0];
            } else if (elm.type == "select-one" || elm.type == "select-multiple") {
                var i;
                var done = new Array();
                var opts = elm.options;
                for (i = 0; i < opts.length; i++) {
                    var opt = opts[i];
                    if (arrayContains(popValues, opt.value)) {
                        opt.selected = true;
                        done[done.length] = opt.value;
                    }
                }

                var j;
                for (j = 0; j < popValues.length; j++) {
                    if (!arrayContains(done, popValues[j])) {
                        var opt = new Option();
                        opt.text = popValues[j];
                        opt.value = popValues[j];
                        elm.options.add(opt);
                    }
                }
            } else if (elm.type == "radio" || elm.type == "checkbox") {
                if (arrayContains(popValues, elm.value)) {
                    elm.checked = true;
                }
            }
        }
    }
}

function arrayContains(arr, str) {
    var i;
    for (i = 0; arr != null && i < arr.length; i++){
        if (arr[i] == str) {
            return true;
        }
    }
    return false;
}

function visibleInputsToQueryString(formName, skipByName) {
    if (document.forms[formName] == undefined) {
        return;
    }

    var result = "";
    var elmsIdx;
    var elms = document.forms[formName].elements;
    for (elmsIdx = 0; elmsIdx < elms.length; elmsIdx++) {
        var values = new Array();
        var elm = elms[elmsIdx];
        
        if (arrayContains(skipByName, elm.name)) {
            continue;
        }

        if (elm.style.display.toUpperCase()=="NONE") {
            continue;
        }

        if (elm.type == "text" || elm.type == "textarea") {
            values[0] = elm.value;
        } else if (elm.type == "select-one" || elm.type == "select-multiple") {
            var i;
            var iii = 0;
            var opts = elm.options;
            for (i=0; i < opts.length; i++) {
                var opt = opts[i];
                if (opt.selected == true) {
                    values[iii] = opt.value;
                    iii = iii + 1;
                }
            }
        } else if (elm.type == "radio" || elm.type == "checkbox") {
            if (elm.checked == true) {
                values[0] = elm.value;
            }
        }

        for (i = 0; i < values.length; i++) {
            var val = values[i];
            if (val != null && val.length>0) {
                if (result.length > 0) {
                    result = result + "&";
                }
                result = result + encodeURIComponent(elm.name) + "=" + encodeURIComponent(values[i]);
            }
        }
    }
    return result;
}

function getFirstChildElement(n) {
    var x = n.firstChild;
    while (x &&  x.nodeType!=1) {
        x = x.nextSibling;
    }
    return x;
}

function addMultiSelectRow(addValue, checkboxName, multiSelectDivName) {
    if (!addValue || addValue.length == 0 || !checkboxName || checkboxName.length == 0 
            || !multiSelectDivName || multiSelectDivName.length == 0) {
            return;
    }

    var div = document.getElementById(multiSelectDivName);
    if (!div) {
        return;
    }

    var label = document.createElement("label");
    label.setAttribute("style", "display:block");

    var input = document.createElement("input");
    input.setAttribute("type", "checkbox");
    input.setAttribute("name", checkboxName);
    input.setAttribute("value", addValue);
    input.setAttribute("checked", "checked");
    input.setAttribute("style", "margin-right:5px");
    label.appendChild(input);

    var text = document.createTextNode(addValue);
    label.appendChild(text);

    var firstChild = getFirstChildElement(div);

    if (firstChild) {
        div.insertBefore(label,firstChild);
    } else {
        div.appendChild(label);
    }
}

function applySearchToggle(searchFormId) {
    jQuery("a.searchSection").click(function() {
        jQuery("#" + searchFormId).slideToggle("slow");
        jQuery(this).parent("li.search").toggleClass("open");
        return false;
    });
}

var DropDownSlidesWatcher = (function () {
    var parentArrowIconClass = "li.expand";
    var slide = {
        id: null,
        elementClass: null,
        open: false
    };
    var isCurrentOpenSlide = function (slideId) {
        if (slide.id === slideId && slide.open === true) {
            return true;
        }
    }
    var isCurrentSlideClosed = function (slideId) {
        if (slide.id === slideId && slide.open === false) {
            return true;
        }
    }
    var isOtherSlideOpen = function (slideId) {
        if (slide.id !== null && slide.id !== slideId) {
            return true;
        }
    }
    return {
        getSlide: function () {
            return slide;
        },
        /**
         * 
         * @argument elementClass is used in accordance with its parent Class "li.expand" in order to alter the arrow symbol direction if present.
         */
        slideInteract: function (slideId,elementClass) {
            if (isCurrentOpenSlide(slideId)) {
                jQuery(slideId).slideToggle("slow");
                jQuery(elementClass).parent(parentArrowIconClass).toggleClass("active");
                slide.open = false;
                slide.id = null;
                slide.elementClass = null;
                return;
            }
            if (isCurrentSlideClosed(slideId)) {
                jQuery(slideId).slideToggle("slow");
                jQuery(elementClass).parent(parentArrowIconClass).toggleClass("active");
                slide.elementClass = elementClass;
                slide.open = true;
                return;
            }
            if (isOtherSlideOpen(slideId)) {
                jQuery(slide.id).slideToggle("slow");
                jQuery(slide.elementClass).parent(parentArrowIconClass).toggleClass("active");
                slide.id = slideId;
                jQuery(slideId).slideToggle("slow");
                slide.elementClass = elementClass;
                jQuery(elementClass).parent(parentArrowIconClass).toggleClass("active");
                slide.open = true;
                return;
            }
            slide.id = slideId;
            slide.open = true;
            slide.elementClass = elementClass;
            jQuery(slideId).slideToggle("slow");
            jQuery(elementClass).parent("li.expand").toggleClass("active");

        }
    }

})();



function applyExportOptionsToggle() {
    var $dropOperations = jQuery("#drop-operations ul");
    if ($dropOperations.length) {
        $dropOperations.append('<li class="expand"><a id="exportLink" href="#">Exports</a></li>');
    } else {
        jQuery("#form1").before('<div id="drop-operations"><ul><li class="expand"><a id="exportLink" href="#">Exports</a></li></ul></div>');
    }

    jQuery("a#exportLink").click(function() {
        DropDownSlidesWatcher.slideInteract("#createbox","a#exportLink");
        return false;
    });
}


function applyAdminToolsToggle() {
    var $dropOperations = jQuery("#drop-operations ul");
    if ($dropOperations.length) {
        $dropOperations.append('<li class="expand"><a id="adminToolsLink" href="#">Admin Tools</a></li>');
    } else {
        jQuery("#form1").before('<div id="drop-operations"><ul><li class="expandTools"><a id="adminToolsLink" href="#">Admin Tools</a></li></ul></div>');
    }

    jQuery("a#adminToolsLink").click(function() {
        DropDownSlidesWatcher.slideInteract("#createBoxAdminTools","a#adminToolsLink");
        return false;
    });
}

function setDatasetExcelXMLDownloadLinksVisibility(contextPath,datasetId) {
        var checked = $('#excelXMLDownloadOption').is(':checked');
        $.ajax({
            type: "GET",
            url: contextPath+'/v2/dataset/'+datasetId+'/allowExcelXmlDownload/'+checked,
            success: function (data) {
                alert('Value successfully updated.');
                window.location.reload(true);
            },
            error: function () {
            alert('An error occurred. Please try again later.');
            }
        });
}


function setDatasetMsAccessDownloadLinksVisibility(contextPath,datasetId) {
        var checked = $('#msAccessDownloadOption').is(':checked');
        $.ajax({
            type: "GET",
            url: contextPath+'/v2/dataset/'+datasetId+'/allowMsAccessDownload/'+checked,
            success: function (data) {
                alert('Value successfully updated.');
                window.location.reload(true);
            },
            error: function () {
                alert('An error occurred. Please try again later.');
            }
        });
}


function applySelectionStyle() {
    jQuery(".selectable").click(function() {
        if (jQuery(this).is(":checked")) {
            jQuery(this).parent("li").length ? jQuery(this).parent("li").addClass("selected") : jQuery(this).closest("tr").addClass("selected");
        } else {
            jQuery(this).parent("li").length ? jQuery(this).parent("li").removeClass("selected") : jQuery(this).closest("tr").removeClass("selected");
        }
    });
}

function applyBoundElementsFiltererInteractions(url, vocabularyFolderId) {
    jQuery("#addFilter").change(function() {
        if (jQuery(this).val()==="") {
            return;
        }

        var filterValue = jQuery(this).val();
        var $selectedOption = jQuery("option:selected", jQuery(this));
                            
        jQuery("#addFilterRow").before('<tr id="spinner-'+ filterValue + '"><td><div class="spinner-loader">Loading...</div></td></tr>');

        jQuery.ajax({
            url: url,
            data: { 
                'boundElementFilterId': filterValue,
                'boundElementFilterIndex': jQuery(".boundElementFilter").length,
                'vocabularyFolderId': vocabularyFolderId,
                '_eventName': 'constructBoundElementFilter'
            },
            success:function(data) {
                jQuery("tr#spinner-" + filterValue).remove();
                jQuery("#addFilterRow").before(data);
                $selectedOption.prop("disabled", true);
            },
            error: function() {
                jQuery("div.spinner-loader", "tr#spinner-" + filterValue).removeClass().addClass("ajaxError").text("Something went wrong. Please try again.");
                setTimeout(function() {
                    jQuery("tr#spinner-" + filterValue).remove();
                }, 2000);
            }
        });

        jQuery(this).val("");
    });

    jQuery("table.filter").delegate("a.deleteButton", "click", function() {
        var $filterItem = jQuery(this).closest("tr.boundElementFilter");
        var filterId = $filterItem.data("filterId");
        jQuery('#addFilter option[value=' + filterId +']').prop('disabled', false);
        $filterItem.remove();

        // recalculate bound element names
        jQuery('.boundElementFilterId').each(function(index) {
            jQuery(this).attr("name", "filter.boundElements[" + index + "].id");
        });
        jQuery('.boundElementFilterSelect').each(function(index) {
            jQuery(this).attr("name", "filter.boundElements[" + index + "].value");
        });

        return false;
    });
}

function applyConceptDefinitionBalloon() {
    jQuery(".conceptDefinition").balloon({
        css: {
            border: 'solid 1px #000',
            padding: '10px',
            backgroundColor: '#f6f6f6',
            color: '#000',
            width: "30%"
        }
    });
}

(function($) {
    $(document).ready(function() {
        $("th.sorted").addClass("selected");
        applySelectionStyle();
    });
})(jQuery);

 function closeAllActiveTabs(){
     //   var activeTabs = document.getElementsByClassName("active");
         var activeTabs = document.querySelector('.active') // Using a class instead, see note below.
        activeTabs.classList.toggle('active');
        var i;
  /**      for (i = 0; i < activeTabs.length; i++) {
            console.log(activeTabs[i]);
            if(activeTabs[i]!=null){
                activeTabs[i].classList.toggleClass("active");
            }
        }
        **/
    }

 
