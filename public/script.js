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

function selectRightOne(form_name,select_name,select_value){

	if (form_name==null || select_name==null || value==null)
		return;
	
	var o = document.forms[form_name].select_name;
	for (i=0; o!=null && i<o.options.length; i++){
		if (o.options[i].value == select_value){
			o.selectedIndex = i;
			break;
		}
	}
}

function openSource(url){
	window.open(url,null,"height=600,width=800,status=yes,toolbar=yes,scrollbars=yes,resizable=yes,menubar=yes,location=yes");
}

function openDCMES(){
	window.open("dcmes.html","DCMES","height=600,width=600,status=no,toolbar=no,scrollbars=yes,resizable=no,menubar=no,location=no");
}


function openType(){
	alert("Type specifies if a data element has quantitative values (Character 2), pre-fixed values (Character 1) or no values, " +
			"but sub-elements (Aggregate). For more information, please go to the start page.");
}

function openAttrType(){
	alert("An attribute can be of type SIMPLE or type COMPLEX. Simple attributes are simply name/value pairs. Complex attributes " +
			"consist of fields and each field is then a simple name/value pair.");
}

function openShortName(){
	alert("Short name is a shorter version (max 50 letters) of a data element's name that must have no white space. It is this name that will be used " +
			"to identify data elements in XML or whatever formatted data reporting!");
}

function openDataClass(){
	alert("Class is for specifying the class into which a data element belongs to. Classes could be for example 'Chemical', 'Parameter', etc. " +
			"This way one can get a picklist of all data elements that are for example chemicals by nature.");
}

function openAttrShortName(){
	alert("Short name is a shorter version (max 50 letters) of an attribute's name that must have no white space. This name is used to display " +
			"attribute titles in the user interface and there can be no attributes with the same short name!");
}

function openNsShortName(){
	alert("Short name is a short title (max 50 letters) for a namespace. It must not contain any white space and it is the abbreviation " +
			"used to identify data elements' namespaces in XML formatted data reporting!");
}

function openNamespace(){
	alert("Namespace defines a context in which a data element is seen. Each namespace is uinquely identified by its URL where one can " +
			"usually read a more detailed description of the context. Namespace plus short name form a unique ID of a data element! " +
			"Please go to Search/Namespaces to find out more about namespaces.");
}

function openAttrName(){
	alert("This is the full name of an attribute that can have a maximum of 255 characters (including white space) and must be " +
			"as self-explanatory as possible. This is where you put the official name of the attribute, according to the standard " +
			"where the attributes originates from.");
}

function openNsName(){
	alert("This is the full name of a namespace. It can have a maximum of 255 characters (including white space) and must be " +
			"as informative as possible. It will not be used XML formatted data reporting, it has only informative value.");
}

function openNsURL(){
	alert("This is the  f u l l  URL of a namespace. It is used as a unique identifier for namespaces and it would be nice to " +
			"have an actual page behind that URL, declaring and describing the namespace.");
}

function openAttrDefinition(){
	alert("This where you should write a short definition of what this attribute means and where and how to use it.");
}

function openAttrObligation(){
	alert("Tells if specifying this attribute for a data element is mandatory, optional or conditional. The latter is not supported for the time being.");
}

function openAttrObligation(){
	alert("Tells if specifying this attribute for a data element is mandatory, optional or conditional. The latter is not supported for the time being.");
}

function openNsDescr(){
	alert("This where you should write a short description of what this namespace stands for, what it covers, who's the owner of it and " +
			"other specifying information.");
}

function openSequenceHelp(){
	alert("Sequence is an ordered list of data elements. Each of its elements in XML document must appear in the specified order. " +
			"For each element it is possible to specify how many times it can occur at its specified location. This can vary from " +
			"zero to unlimited number of occurabces.");
}

function openChoiceHelp(){
	alert("Choice is a group of data elements from which  o n l y  o n e  can appear in the XML document! A choice is naturally not ordered " +
			"and it can contain as many elements as you like.");
}
function form_changed(form_name){
	document.forms[form_name].elements["changed"].value="1";		
}
function confirm_saving(){
	return confirm("You have unsaved data on the page! Are you sure you want to leave the page and loose the changes?");
}
function getDDVersionName(){
	return "Version 1.2 Test";
}