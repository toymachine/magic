jQuery(document).ready(function($) {
    var load_page = function(url) {
                $.ajax({ 
                    url: url,
                    success: function(data) {
                        $('#page-body').html(data);
                    }
                });
            }

    load_page('/index');

});
