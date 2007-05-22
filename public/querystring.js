/* Client-side access to querystring name=value pairs
	Version 1.2.3
	22 Jun 2005
	Adam Vandenberg

	improved by Jaanus Heinlaid
*/
function Querystring(qs) { // optionally pass a querystring to parse
	this.params = new Object()
	this.get=Querystring_get
	this.getValues=Querystring_get_values
	this.remove = Querystring_remove
	this.removeAll = Querystring_remove_all
	this.toString = Querystring_to_string
	this.setValues_ = Querystring_set_values_
	this.hasKey = Querystring_has_key
		
	if (qs == null)
		qs=location.search.substring(1,location.search.length)

	if (qs.length == 0) return

// Turn <plus> back to <space>
// See: http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1
	qs = qs.replace(/\+/g, ' ')
	var args = qs.split('&') // parse out name/value pairs separated via &
	
// split out each name=value pair
	for (var i=0;i<args.length;i++) {
		var value;
		var pair = args[i].split('=')
		var name = unescape(pair[0])

		if (pair.length == 2)
			value = unescape(pair[1])
		else
			value = name
		
		var valuesArr = this.params[name];
		if (valuesArr==null || valuesArr.length==0)
			valuesArr = new Array();
		valuesArr[valuesArr.length] = value;

		this.params[name] = valuesArr
	}
}

function Querystring_get(key, default_) {
	// This silly looking line changes UNDEFINED to NULL
	if (default_ == null)
		default_ = null;

	var result = default_;
	var valuesArr=this.params[key]
	if (valuesArr!=null && valuesArr.length>0)
		result = valuesArr[0];
	
	return result;
}

function Querystring_get_values(key) {
	return this.params[key];
}

function Querystring_remove(key){
	if (typeof(this.params[key]) != 'undefined') {
		delete this.params[key];
	}
}

function Querystring_remove_all(srcQS){

	if (srcQS==null)
		return;

	if (this.params!=null){
		var key;
		var toBeDeleted = new Array();
		for (key in this.params){
			if (srcQS.hasKey(key))
				toBeDeleted[toBeDeleted.length] = key;				
		}
		var j;
		for (j=0; j<toBeDeleted.length; j++){
			delete this.params[toBeDeleted[j]];
		}
	}
}

function Querystring_to_string(){

	var result = "";
	if (this.params!=null){
		var key;
		for (key in this.params){
			var values = this.params[key];
//			alert("__1: " + values);
			if (values!=null && values.length>0){
				var j;
//				alert("__2: " + values);
//				alert("__3 length: " + values.length);
				for (j=0; j<values.length; j++){
					if (result.length>0)
						result = result + "&";
//					alert("__2: " + values[j]);
					result = result + key + "=" + escape(values[j]);
				}
			}
		}
	}
	
	return result;
}

function Querystring_set_values_(key, values){
	if (key==null || values==null)
		return;

	if (typeof(this.params[key]) != 'undefined')
		delete this.params[key];

	this.params[key] = values;
}

function Querystring_has_key(key){
	return typeof(this.params[key]) != "undefined";
}
