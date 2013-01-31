(function(){
	/**
	 * Ricotta API object contains all the api functionalities
	 * @para url the backend url string
	 */
	var RicottaAPI = function(url) {
		var that = this;
		that.backendUrl = "/api/";
		if(url != undefined) {
			that.backendUrl = url;
		}
	};
	RicottaAPI.prototype = {
		/**
		 * Get user login 
		 * @param successCallback function to be called when the user already login
		 * @param failCallback function to be called when getting user failed or user not login
		 */
		me: function(successCallback, failCallback) {
			var that = this;
			//sending ajax rquest
			$.ajax({
//				async: false,
		        type: "GET",
		        url: that.backendUrl + "me/v10",
		        data: {
		            path: "/index.html"
		        },
		        dataType: "json",
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(jqXHR);
		        	}
		        }
		    });
		},
		getTemplates: function(successCallback, failCallback) {
			var that = this;
			$.ajax({
//				async: false,
		        type: "GET",
		        url: that.backendUrl + "template/v10",
		        dataType: "json",
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(jqXHR);
		        	}
		        }
		    });
		},
		createTemplate: function(data, successCallback, failCallback) {
			var that = this;
			$.ajax({
//				async: false,
		        type: "POST",
		        url: that.backendUrl + "template/v10",
		        dataType: "json",
		        data: data,
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(jqXHR);
		        	}
		        }
		    });
		},
		updateTemplate: function(name, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
//				async: false,
		        type: "POST",
		        url: that.backendUrl + "template/v10/"+name,
		        dataType: "json",
		        data: data,
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(jqXHR);
		        	}
		        }
		    });
		},
		getSubsets: function(projName, successCallback, failCallback) {
			var that = this;
			$.ajax({
				type: "GET",
				url: that.backendUrl + "project/v10/" + projName + "/subsets",
				dataType: "json",
				success: function(data) {
					if(typeof successCallback !== 'undefined') {
						successCallback(data);
					}
				},
				error: function() {
					if(typeof failCallback !== "undefined") {
						failCallback();
					}
				}
			});
		},
		createSubset: function(projName, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
				type: "POST",
				url: that.backendUrl + "project/v10/" + projName + "/subsets",
				data: data,
				dataType: "json",
				success: function(data) {
					if(typeof successCallback === 'function') {
						successCallback(data);
					}
				},
				error: function() {
					if(typeof failCallback === "function") {
						failCallback();
					}
				}
			});
		},
		/**
		 * Get the upload url for uploading images
		 */
		uploadContextImage: function(projectName, successCallback, failCallback) {
			var that = this;
			$.ajax({
				async: false,
				type: "GET",
				url: that.backendUrl + "blob/v10/"+projectName,
				crossDomain: true,
				dataType: "json",
				accept: "*", 
				success: function(blob){
					if(typeof successCallback === 'function') {
						successCallback(blob);
					}
				},
				error: function() {
					if(typeof failCallback === 'function') {
						failCallback();
					}
				}
			});
		},
		createContext: function(projName, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/" + projName +"/context",
				type: "POST",
				data: data,
				dataType: "json",
				success: function(context){
					if(typeof successCallback === 'function') {
						successCallback(context);
					}
				},
				error: function() {
					if(typeof failCallback === 'function'){
						faillCallback();
					}
				}
				
			});
		},
		getContexts: function(projName, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/" + projName +"/context",
				type: "GET",
				dataType: "json",
				success: function(contexts){
					if(typeof successCallback === 'function') {
						successCallback(context);
					}
				},
				error: function() {
					if(typeof failCallback === 'function'){
						faillCallback();
					}
				}
				
			});
			
		},
		/**
		 * Add user to the project
		 */
		saveUser: function(projName, id, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
		        type: "POST",
		        url: that.backendUrl + "project/v10/" + projName + "/user/" + id,
		        data: data,
		        dataType: "json",
		        success: function(data, textStatus, jqXHR){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(req, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(textStatus);
		        	}
		        }
		    });
		},
		/**
		 * Get all roles
		 */
		loadRoles: function(successCallback, failCallback) {
			var that = this;
			$.ajax({
		        async: false,
		        type: "GET",
		        url: that.backendUrl + "role/v10",
		        dataType: "json",
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(req, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(textStatus);
		        	}
		        }
		    });
		},
		/**
		 * Add the language to the project
		 */
		addProjectLanguage: function(projName, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
                async: false,
                type: "POST",
                url: that.backendUrl + "project/v10/" + projName + "/projLang",
                dataType: "json",
                data: data
                /*
                data: {
                    langCode: projLangCode.val(),
                    defaultLang: projLangDefault.val()
                }*/
            })
            .done(function(data){
            	if(typeof successCallback === 'function') {
            		successCallback(data);
            	}
            	/*
                $("#createProjLangDialog").dialog("close");
                loadProject(p.name);
                p = $("#headerProject").data("json");
                buildProjLangsTable(p);*/
            })
            .fail(function() {
            	if(typeof failCallback === 'function') {
            		failCallback(this.statusText);
            	}
                //updateTips(this.statusText);
            });
		},
		/**
		 * Load all the languages
		 */
		loadLanguages: function(callback) {
			var that = this;
			$.ajax({
		        async: false,
		        type: "GET",
		        url: that.backendUrl + "lang/v10",
		        dataType: "json",
		        success: function(data){
		        	if(typeof callback === 'function') {
		        		//populateLanguages(data);
		        		callback(data);
		        	}
		        },
		        error: function(req, textStatus, errorThrown) {}
		    });
		},
		/**
		 * Create new language
		 */
		createLanguage: function(data, successCallback, failCallback) {
			var that = this;
			$.ajax({
                async: false,
                type: "POST",
                url: that.backendUrl + "lang/v10",
                dataType: "json",
                data: data
            })
            .done(function(data){
            	if(typeof successCallback === 'function') {
            		successCallback(data);
            	}
             })
            .fail(function() {
            	if(typeof failCallback === 'function') {
        			if (this.status == 409) {
        				failCallback('name already taken');
        			} else {
        				failCallback(this.statusText);
        			}
            	}
            })
		},
		/**
		 * Create new project
		 * @param projectName the name of the new project
		 * @param successCallback function to be called when the new project created
		 * @param failCallback function to be called when creating project fail
		 */
		createProject: function(projectName, defaultLang, successCallback, failCallback) {
			var that = this;
			$.ajax({
                async: false,
                type: "POST",
                url: that.backendUrl + "project/v10",
                dataType: "json",
                data: {
                    name: projectName,
                    defaultLang: defaultLang
                }
            })
            .done(function(data){
            	if(typeof successCallback === 'function') {
            		successCallback(data);
            	}
            })
            .fail(function() {
            	if(typeof failCallback === 'function') {
            		if (this.status == 409) {
                        failCallback('name already taken');
                    } else {
                        failCallback(this.statusText);
                    }
            	}
            });
		},
		/**
		 * Get all created projects
		 * @param successCallback function to be called after retrieved all projects
		 */
		getProjects: function(successCallback) {
			var that = this;
			$.ajax({
		        async: false,
		        type: "GET",
		        url: that.backendUrl +"project/v10",
		        dataType: "json",
		        success: function(data){
		        	successCallback(data);
		        },
		        error: function(req, textStatus, errorThrown) {}
		    });
		},
		getProjectTokens: function(name, successCallback, failCallback) {
			var that = this;
			$.ajax({
		        async: false,
		        type: "GET",
		        url: that.backendUrl + "project/v10/" + name + "/token",
		        dataType: "json",
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		            hideToast();
		            $("#headerProject").data("json", data);
		            $("#projectTabs").show();
		        },
		        error: function(req, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(textStatus);
		        	}
		            showToast(textStatus);
		        }
		    });
		},
		/**
		 * @param projName the project contains the token
		 * @param tokenId the token id to be update
		 * @param data 
		 */
		saveToken: function(projName, tokenId, data, callback) {
			var that = this;
			$.ajax({
                async: false,
                type: "POST",
                url: that.backendUrl + "project/v10/" + projName + "/token/" + tokenId,
                dataType: "json",
                data: data
            })
            .done(function(data){
            	if(typeof callback === 'function') {
            		callback();
            	}
            })
            .fail(function() {});
		},
		/**
		 * Create token for a given project
		 * @param projName Project name
		 * @param data contains json data object
		 * @param successCallback function to be called after creating complete
		 * @param failCallback function to be called after any fail action occurred 
		 */
		createToken: function(projName, data, successCallback, failCallback) {
			var that = this;
			$.ajax({
                async: false,
                type: "POST",
                url: that.backendUrl + "project/v10/" + projName + "/token",
                dataType: "json",
                data: data
            })
            .done(function(data){
            	if(typeof successCallback === 'function') {
            		successCallback(data);
            	}
            })
           .fail(function() {
        	   if(typeof failCallback === 'function') {
        		   if (this.status == 409) {
        			   failCallback('name already taken');
        		   } else {
        			   failCallback(this.statusText);
        		   }
        	   }
           });
		}
	};
	if(typeof exports !== 'undefined') exports.RicottaAPI = RicottaAPI;
	else window.RicottaAPI = RicottaAPI;
})();

var ricottaAPI = new RicottaAPI();