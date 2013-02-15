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

function updateTips(t) {
    var tips = $(".validateTips");
        tips.html(t);
        console.log(tips.parent());
        if(tips.parent().hasClass("hide")) {
        	tips.parent().addClass("show");
        }
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

function refreshPageContent(page) {
	if($("."+page).length > 0) {
		$("."+page).load(page + ".html");
	}
}

function refreshProjectContent(page) {
	$.get(page + ".html", function(content) {
		$("#projectSetting").append(content);
	});
}

function refreshMenu() {
	$(".nav-list").children().remove();
	$(".tab-items li").each(function(){
		$(this).filter(".first-tab").addClass("active");
		$(".nav-list").append($(this));
	});
}

function loadPage(page) {
	showToast("Loading...");
	$("#contents .show").removeClass("show").addClass("hide");
	if($("."+page).length == 0) {
		$.get(page + ".html", function(content) {
			$("#contents").append(content);
			$("."+page).addClass("show");
			hideToast();
		});
	}
	else { 
		$("."+page).removeClass("hide").addClass("show");
		hideToast();
	}
}

function loadProjectContent(pageContent) {
	//showToast()
	$("#projectSetting .show").removeClass("show").addClass("hide");
	if($("."+pageContent).length == 0) {
		$.get(pageContent + ".html", function(content) {
			$("#projectSetting").append(content);
			$("."+pageContent).addClass("show");
			hideToast();
		});
	}
	else { 
		$("."+pageContent).removeClass("hide").addClass("show");
		hideToast();
	}
}

function replaceClass(obj, oldClass, newClass) {
	obj.removeClass(oldClass);
	obj.addClass(newClass);
}
