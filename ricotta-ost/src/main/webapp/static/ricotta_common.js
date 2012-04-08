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
