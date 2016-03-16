///
function form_changed(form_name){
	document.forms[form_name].elements["changed"].value="1";
}

///
function pop(link) {

	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	window.open(link,"","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}

///
function populateInput(formName, inputName, popValues){

	if (document.forms[formName]==undefined)
		return;

	var elmsIdx;
	var elms = document.forms[formName].elements;
	for (elmsIdx=0; elmsIdx<elms.length; elmsIdx++){

		var elm = elms[elmsIdx];
		if (elm.name==inputName){

			if (elm.type=="text" || elm.type=="textarea")
				elm.value=popValues[0];
			else if (elm.type=="select-one" || elm.type=="select-multiple"){
				var i;
				var done = new Array()
				var opts = elm.options;
				for (i=0; i<opts.length; i++){
					var opt = opts[i];
					if (arrayContains(popValues, opt.value)){
						opt.selected = true;
						done[done.length] = opt.value;
					}
				}
				var j;
				for (j=0; j<popValues.length; j++){
					if (!arrayContains(done, popValues[j])){
						var opt = new Option();
						opt.text = popValues[j];
						opt.value = popValues[j];
						elm.options.add(opt);
					}
				}
			}
			else if (elm.type=="radio" || elm.type=="checkbox"){
				if (arrayContains(popValues, elm.value))
					elm.checked = true;
			}
		}
	}
}

///
function arrayContains(arr, str){
	var i;
	for (i=0; arr!=null && i<arr.length; i++){
		if (arr[i]==str)
			return true;
	}
	return false;
}

///
function visibleInputsToQueryString(formName, skipByName){

	if (document.forms[formName]==undefined)
		return;

	var result = "";
	var elmsIdx;
	var elms = document.forms[formName].elements;
	for (elmsIdx=0; elmsIdx<elms.length; elmsIdx++){

		var values = new Array();
		var elm = elms[elmsIdx];
		if (arrayContains(skipByName, elm.name))
			continue;

		if (elm.style.display.toUpperCase()=="NONE")
			continue;

		if (elm.type=="text" || elm.type=="textarea")
			values[0] = elm.value;
		else if (elm.type=="select-one" || elm.type=="select-multiple"){
			var i;
			var iii = 0;
			var opts = elm.options;
			for (i=0; i<opts.length; i++){
				var opt = opts[i];
				if (opt.selected == true){
					values[iii] = opt.value;
					iii = iii + 1;
				}
			}
		}
		else if (elm.type=="radio" || elm.type=="checkbox"){
			if (elm.checked == true)
				values[0] = elm.value;
		}

		for (i=0; i<values.length; i++){
			var val = values[i];
			if (val!=null && val.length>0){
				if (result.length > 0)
					result = result + "&";
				result = result + encodeURIComponent(elm.name) + "=" + encodeURIComponent(values[i]);
			}
		}
	}

	return result;
}

function getFirstChildElement(n){
	var x = n.firstChild;
    while (x &&  x.nodeType!=1) {
        x=x.nextSibling;
    }
	return x;
}

function addMultiSelectRow(addValue, checkboxName, multiSelectDivName){

	if (!addValue || addValue.length==0 || !checkboxName || checkboxName.length==0 || !multiSelectDivName || multiSelectDivName.length==0)
		return;

	var div = document.getElementById(multiSelectDivName);
	if (!div)
		return;

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

    if (firstChild){
        div.insertBefore(label,firstChild);
    }
    else{
        div.appendChild(label);
    }
}

(function($) {
    $(document).ready(function() {
        $("th.sorted").addClass("selected");
    });
})(jQuery);