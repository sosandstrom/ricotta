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

function validName(st) {
	var reg = /^[a-zA-Z0-9-_]*$/;
	return reg.test(st);
}

function updateTips(t) {
    var tips = $(".validateTips");
        tips.html(t);
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
		if(ajaxCalled != null) ajaxCalled.abort();//abort the last called
		ajaxCalled = $.get(page + ".html", function(content) {
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
	$("#ajaxLoader").show();
	$("#projectSetting .show").removeClass("show").addClass("hide");
	if($("."+pageContent).length == 0) {
		if(ajaxCalled != null) ajaxCalled.abort(); //abort the last call to avoid the duplicate content
		ajaxCalled = $.get(pageContent + ".html", function(content) {
			$("#projectSetting").append(content);
			$("."+pageContent).addClass("show");
			//hideToast();
			$("#ajaxLoader").hide();
		});
	}
	else { 
		$("."+pageContent).removeClass("hide").addClass("show");
		$("#ajaxLoader").hide();
	}
}

function replaceClass(obj, oldClass, newClass) {
	obj.removeClass(oldClass);
	obj.addClass(newClass);
}

function clearLastText() {
	$(".modal-body .alert").removeClass("show").addClass("hide");
	$(".modal-body input[type='text']").removeClass("input_error");
	$(".modal-body input[type='text']").val("");
	$(".modal-body input[type='email']").removeClass("input_error");
	$(".modal-body input[type='email']").val("");
	$(".modal-body input[type='file']").val("");
	$(".modal-body textarea").removeClass("input_error");
	$(".modal-body textarea").val("");
}

/**
 * validate the input email. Normally it should used in the add user dialog
 * @param email
 * @returns {Boolean}
 */
function validateEmail(email) {
	//console.log("validate: "+  email);
	var mailformat = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,4})+$/;
	if(mailformat.test(email)) {
		return true;  
	}
	return false;
}