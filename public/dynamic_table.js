	////////////////////////////////////////////////////////////////////////////
	// dynamic_table - constructor
	//
	// This method reads the table from html.
	// The paramater name should be table id in html and table should have tbody

function dynamic_table(name) {

	mytable= document.getElementById(name);
	mytableBodies = mytable.getElementsByTagName("tbody");

	//mytableBody = mytableBodies[3];
	mytableBody = mytableBodies.item(0);
	for (var i=1; i<=mytableBodies.length; i++){
		if (mytableBody.id=="tbl_body")
			break;
		else
			mytableBody = mytableBodies.item(i);
	}

	rows = mytableBody.getElementsByTagName("tr");
 	rows_len=rows.length;
 	sel_pos=0;  //to store selected row position
	other_click=false; //variable for understand the clicks on other objects (eg. checkbox)

	this.moveup = dt_moverowup;    // Method assignment.
 	this.movedown = dt_moverowdown;    // Method assignment.
	this.movefirst = dt_moverowfirst;    // Method assignment.
	this.movelast = dt_moverowlast;    // Method assignment.
 	this.selectRow = dt_selectrow;    // Method assignment.
	this.getRowPos = dt_getrowpos;
	this.clickOtherObject = dt_clickobject;
	this.insertNumbers = dt_setrownumbers
}


// This method moves the selected row up. 
function dt_moverowup(){
	if (sel_pos==0 || sel_pos==1) return;
			
	sel_row = rows.item(sel_pos-1);
	up_row = rows.item(sel_pos-2);
	mytableBody.insertBefore(sel_row, up_row);
	this.selectRow(sel_row);
}

// This method moves the selected row down. 
function dt_moverowdown(){
	if (sel_pos==0 || sel_pos==rows_len) return;
			
	sel_row = rows.item(sel_pos-1);
	if (sel_pos+1==rows_len)
	{
		mytableBody.appendChild(sel_row);
	}
	else{
		down_row = rows.item(sel_pos+1);
		mytableBody.insertBefore(sel_row, down_row);
	}
	this.selectRow(sel_row);
}

function dt_moverowfirst(){
	if (sel_pos==0 || sel_pos==1) return;
			
	sel_row = rows.item(sel_pos-1);
	first_row = rows.item(0);
	mytableBody.insertBefore(sel_row, first_row);
	this.selectRow(sel_row);
}

function dt_moverowlast(){
	if (sel_pos==0 || sel_pos==rows_len) return;
			
	sel_row = rows.item(sel_pos-1);
	mytableBody.appendChild(sel_row);
	this.selectRow(sel_row);
}

// This method sets the selected row position and changes the color.
// parameter row is TR object
function dt_selectrow(row){

	if (other_click==true)
	{
		other_click=false;
		return;
	}

	row_pos = this.getRowPos(row);

//	row_pos = 1;
	
	if (row_pos != sel_pos && eval(row_pos)>0)
	{
		sel_pos=row_pos;
	}
	else
	{
		sel_pos=0;
	}

	dt_setrowcolors();
}
// This method receives the selected row's position in table.
// parameter row is TR object
// NB! each row should have unique id
function dt_getrowpos(row){
	for (var i=1; i<=rows_len; i++){
		r = rows.item(i-1);
		if (r.id==row.id)
		{
			return i
		}

	}
	return 0;
}
// this method sets the row colors for each row
function dt_setrowcolors(){
	for (var i=1; i<=rows_len; i++){
		r = rows.item(i-1);
		if (i==sel_pos){
			r.style.backgroundColor="#97ABD7";
		}
		else{
			r.style.backgroundColor=(i % 2 != 0) ? "#f0f0f0":"#D3D3D3";
		}
	}
}
// this method should be used if there is another object in the row eg. checkbox
// Clicking the checkbox fires 2 onclick events (for row and checkbox)
// It's possible to avoid this click if you use this method and the row is not selected
function dt_clickobject(){
	other_click=true;
}

// this method inserts row numbers into inputs in each row
// parameter represents the start of the input name eg."pos_"
// NB! input id should end with row id
function dt_setrownumbers(input_prefix){

	if (input_prefix==null || input_prefix.length==0)
			input_prefix="pos_";

	for (var i=1; i<=rows_len; i++){
		r = rows.item(i-1);
		inputs=document.getElementsByTagName("input");
		for(j=0;j<inputs.length;j++) {
			inp = inputs.item(j);
			if (inp.name==input_prefix + r.id){
				inp.value=i;
			}
		}
	}
}