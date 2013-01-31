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
    if (s == null || s == undefined) {
        return '';
    }
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

function checkEmpty(listObj) {
	var valid = true;
	$.map(listObj, function(obj, index){
		if($(obj).val().length == 0) {
			$(obj).addClass("input_error");
			if(listObj.length > 1) updateTips("The hightlighted fields are mandatory");
			else updateTips("The hightlighted field is mandatory");
			var tips = $(".alert-error");
			tips.removeClass("hide").addClass("show");
			valid = false;
		}
	});
	return valid;
}

function setActiveMenu(context, elm) {
	context.children().each(function(){
		$(this).removeClass("active");
	});
	elm.addClass("active");
}

function refreshContent(page) {
	if($("."+page).length > 0) {
		$("."+page).load(page + ".html");
	}
}

function refreshMenu() {
	$(".nav-list").children().remove();
	$(".tab-items li").each(function(){
		$(this).filter(".first-tab").addClass("active");
		$(".nav-list").append($(this));
	});
}

function loadContents(page) {
	$("#contents .show").removeClass("show").addClass("hide");
	if($("."+page).length == 0) {
		$.get(page + ".html", function(content) {
			$("#contents").append(content);
			$("."+page).addClass("show");
		});
	}
	else { 
		$("."+page).removeClass("hide").addClass("show");
	}
}

window.addEventListener("DOMContentLoaded", function(){
    $(".nav-list li").live("click", function(){
    	var page = $(this).data("target");//get the link page
    	setActiveMenu($(".nav-list"), $(this));
    	loadContents(page);
    	//$("#tabContents").load(page);
    });
});