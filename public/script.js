Net=1;
if ((navigator.appName.substring(0,5) == "Netsc"
  && navigator.appVersion.charAt(0) > 2)
  || (navigator.appName.substring(0,5) == "Micro"
  && navigator.appVersion.charAt(0) > 3)) {
 Net=0;

 over = new Image;
 out = new Image;
 gammel = new Image;

 over.src = "images/on.gif";
 out.src = "images/off.gif";
 
 gTarget = 'img1';
}

function Click(Target) {
 if (Net != 1){
  if (Target != gTarget) {
   document[Target].src = over.src;
   document[gTarget].src = out.src;
   gTarget = Target;
   gammel.src = document[Target].src;
  }
 }
}

function Over(Target) {
 if (Net != 1){
  gammel.src = document[Target].src;
  document[Target].src = over.src;
 }
}

function Out(Target) {
 if (Net != 1){
  document[Target].src = gammel.src;
 }
}

var browser = document.all ? 'E' : 'N';

function login() {
	window.open("login.html","login","height=200,width=300,status=no,toolbar=no,scrollbars=no,resizable=no,menubar=no,location=no");
}
function logout() {
	window.open("logout.html","login","height=200,width=300,status=no,toolbar=no,scrollbars=no,resizable=no,menubar=no,location=no");
}

function form_changed(form_name){
	document.forms[form_name].elements["changed"].value="1";		
}
function getDDVersionName(){
	return "Version 2.1";
}

function pop(link) {
	// it is VERY IMOPRTANT to give the popup window a name (even if its an empty string), because setting it to
	// null will load into the opener if the opener itself has also been opened by another window!!!
	var newWin = window.open(link,"","status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=no");
	// it is VERY IMOPRTANT to set the window's event return value to FALSE, because this will override the browser's
	// default behaviour which would ruin the whole thing
	window.event.returnValue = false;
}