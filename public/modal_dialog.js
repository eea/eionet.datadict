var dialogWin = new Object();

/*
opens modal dialog window
works with NS and IE
parameters:
		url - dialog source url eg. yesno_dialog.html
		text - the question or explanation text displayd in dialog window
		returnFunc  - the main window function name, which is called after clicking buttons on dialog window
		height, width - dialog window height and width
	*/

function openDialog(url, text, returnFunc, height, width) {
	if (!dialogWin.win || (dialogWin.win && dialogWin.win.closed)) {
		// Initialize properties of the modal dialog object.
		dialogWin.text = text;
		dialogWin.returnFunc = returnFunc;
		
		// Generate the dialog and make sure it has focus.
		dialogWin.win = window.open(url, "", "height=" + height +",width="+width+",status=yes,toolbar=no,scrollbars=no,resizable=yes,menubar=no,location=no,modal=yes");
		window.onfocus = checkModal;
		dialogWin.win.focus()
	} else {
		dialogWin.win.focus()
	}
}

function checkModal() {
	if (dialogWin.win!=null && !dialogWin.win.closed) {
    	dialogWin.win.focus()
	}
}

