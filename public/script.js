function login() {
	window.open("login.html","login","height=200,width=300,status=no,toolbar=no,scrollbars=no,resizable=no,menubar=no,location=no");
}

function form_changed(form_name){
	document.forms[form_name].elements["changed"].value="1";		
}

function popNovr(link) {
	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	var newWin = window.open(link,"","status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}

function pop(link) {

	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	window.open(link,"","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
}