function format ( row, tr ) {
    var d = row.data();
    //ajax call to get data by ldapGroup
    var ldapGroup = d[0];
    $.ajax({
        async: false,
        type: "POST",
        url: 'roleUsers/' + ldapGroup,
        contentType : 'application/json; charset=utf-8',
        success: function (result) {
           var additionalInfo = '<table>' +
               '<tr>'+
               '<td style="float: left;border: none;width: 100%;overflow-wrap: break-word;word-break: normal">' +
                result+'</td>'+
               '<td style="border:none"></td>'+
               '<td style="border:none"></td>'+
               '</tr>'+
               '</table>';

            //show the row
            row.child(additionalInfo).show();
            tr.addClass('shown');
        },
        error: function () {
            alert('An error occurred.');
        }
    });
}

$(document).ready( function () {
    var table = $('#groupsAndUsers').DataTable({
        "order": [[1, "asc"]]
    });

    $('.dataTables_length').addClass('bs-select');

    $('#groupsAndUsers tbody').on('click', 'td span.details-control', function () {
        var tr = $(this).closest('tr');
        var row = table.row( tr );

        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Call function to fill the table and show it
            format(row, tr);
        }
    } );
});