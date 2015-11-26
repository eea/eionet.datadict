/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */


(function(document, $){
    $(document).ready(function() {
        $('.helpButton').bind("click", function(event) {
            event.preventDefault();
            // removes the <div> element with id="modalPopup" if it already exists
            $('#modalPopup').remove();
            // create <div> element with id="modalPopup"
            $('body').append('<div id="modalPopup">');
            // load the element with ID="workarea" from the HTML markup 
            // referenced by this.href into the modal div with ID="modalPopup"
            $('#modalPopup').load(this.href + " #workarea", function(){
                $('#modalPopup div').removeAttr('id');
            });
            // popup the dialog window with predifined dimensions and functionality concerning the close event
            $('#modalPopup').dialog({
                title: "Data Dictionary - Help",
                width: 800,
                height: 400,
                // when dialog is closed <div> element will be removed from DOM
                close: function () {
                    $('#modalPopup').remove();
                }
            });
        });
    });
})(document, jQuery);
