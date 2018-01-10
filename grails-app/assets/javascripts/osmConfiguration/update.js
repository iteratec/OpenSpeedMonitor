"use strict";

function update() {
    document.getElementsByTagName('form')[0].submit();
    window.location = '../../osmConfiguration/show/1'
}

function confirm() {
    var newSuffix = $('#globalUserAgentSuffix').val();
    if (newSuffix == '') {
        update();
    }
    else {
        var prompt = 'The configured user agent suffix will be used in all future wpt jobs. If you have a job that ' +
            'needs differing user agents settings, you will need to explicitly overwrite this global setting.\n' +
            'Are you sure you want to continue?';
        $.ajax({
            url: '/osmConfiguration/globalUserAgentSuffix/1', error: update, success: function (d) {
                var originalSuffix = d.globalUserAgentSuffix;
                if (originalSuffix == null) {
                    //Somethings wrong...
                    update();
                }
                else if (originalSuffix != '') {
                    update();
                }
                else if (modalDialog(prompt)) {
                    update();
                }
            }
        });
    }
}

function modalDialog(prompt) {
    // TODO: This function needs to prompt the user if he wants to continue.
    console.log(prompt);
    return true;
}
