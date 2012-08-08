function getFragmentByName(name) {

    var match = RegExp('[&#]' + name + '=([^&]*)')
                    .exec(window.location.hash);

    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));

}

function getParameterByName(name) {

    var match = RegExp('[?&]' + name + '=([^&]*)')
                    .exec(window.location.search);

    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));

}

function getSafe(s) {
    return s.replace(/[ &]/gi, "_");
}

function getShort(s, len) {
    if (s.length <= len) {
        return s;
    }
    return s.substring(0,len-3) + '...';
}

function updateTips( t ) {
    var tips = $( ".validateTips" );
        tips
                .text( t )
                .addClass( "ui-state-highlight" );
        setTimeout(function() {
                tips.removeClass( "ui-state-highlight", 1500 );
        }, 500 );
}

function checkLength( o, n, min, max ) {
        if ( o.val().length > max || o.val().length < min ) {
                o.addClass( "ui-state-error" );
                updateTips( "Length of " + n + " must be between " +
                        min + " and " + max + "." );
                return false;
        } else {
                return true;
        }
}

function checkRegexp( o, regexp, n ) {
        if ( !( regexp.test( o.val() ) ) ) {
                o.addClass( "ui-state-error" );
                updateTips( n );
                return false;
        } else {
                return true;
        }
}
