Net=1;
if ((navigator.appName.substring(0,5) == "Netsc"
  && navigator.appVersion.charAt(0) > 2)
  || (navigator.appName.substring(0,5) == "Micro"
  && navigator.appVersion.charAt(0) > 3)) {
 Net=0;

 over = new Image;
 out = new Image;
 gammel = new Image;

 over.src = "../images/on.gif";
 out.src = "../images/off.gif";
 
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
function confirm_saving(){
	return confirm("You have unsaved data on the page! Are you sure you want to leave the page and loose the changes?");
}
function getDDVersionName(){
	return "Version 2.0";
}
