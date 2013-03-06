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
		addProjectUser: function(projName, email, role, successCallback, failCallBack){
			var that = this;
			$.ajax({
				type: "POST",
				url: that.backendUrl + "project/v10/" + projName + "/user",
				data: {
					"email": email,
					"role": role
				},
				dataType: "json",
				success: function(data) {
					if(typeof successCallback === 'function') {
						successCallback(data);
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					if(typeof failCallBack === 'function') {
						if(jqXHR.status == 409) {
							failCallBack("User " + email + " already added.");
						}
					}
				}
			})
		},
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
		getExportUrl: function(projName) {
			var that = this;
			return that.backendUrl + "project/v10/"+projName + "/export";
		},
		deleteProj: function(projName, successCallback, failCallback) {
			var that = this;
			$.ajax({
		        async: false,
		        type: "POST",
		        url: that.backendUrl + "project/v10/"+projName + "/delete",
		        dataType: "json",
		        success: function(data){
		        	if(typeof successCallback === 'function') {
		        		successCallback(data);
		        	}
		        },
		        error: function(req, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(req.status);
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
            })
            .done(function(data){
            	if(typeof successCallback === 'function') {
            		successCallback(data);
            	}
            })
            .fail(function() {
            	if(typeof failCallback === 'function') {
            		failCallback(this.statusText);
            	}
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
                data: data,
                success: function(data) {
                	if(typeof successCallback === 'function') {
                		successCallback(data);
                	}
                },
                error: function(jqXR, statusText, errorThrown) {
                	if(typeof failCallback === 'function') {
                		if (jqXR.status == 409) {
                			failCallback('Name already taken');
                		} else {
                			failCallback(errorThrow);
                		}
                	}
                }
            });

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
                },
                success: function(data) {
                	if(typeof successCallback === 'function') {
                		successCallback(data);
                	}
                },
                error: function(jqXR, statusText, errorThrown) {
                	if(typeof failCallback === 'function') {
                		if (jqXR.status == 409) {
                			failCallback('<strong>Error!</strong> Name already taken');
                		} else {
                			failCallback(errorThrown);
                		}
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
		        },
		        error: function(req, textStatus, errorThrown) {
		        	if(typeof failCallback === 'function') {
		        		failCallback(textStatus);
		        	}
		            showToast(textStatus);
		        }
		    });
		},
		getToken: function(tokenId, projName, successCallback, failCallback) {
			var that =  this;
			$.ajax({
				url: that.backendUrl + "project/v10/" + projName + "/token/" + tokenId,
				type: "GET",
				dataType: "json",
				success: function(token) {
					if(typeof successCallback === 'function') {
						successCallback(token);
					}
				},
				error: function(jqXR, testStatus, errorThrown) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
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
		},
		updateTranslation: function(langCode, langVal, projName, tokenId, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/" + projName + "/token/" + tokenId,
				type: "POST",
				dataType: "json",
				data: {
					"langCode": langCode,
					"value": langVal
				},
				success: function(token) {
					if(typeof successCallback === 'function') {
						successCallback(token);
					}
				},
				error: function(jqXR, textStatus, errorThrown) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
				}
			});
		},
		deleteToken: function(tokenId, projName, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+ projName +"/token/" + tokenId,
				type: "DELETE",
				dataType: 'json',
				success: function() {
					if(typeof successCallback === 'function') {
						successCallback();
					}
				},
				error: function() {
					if(typeof failCallback === 'function') {
						failCallback();
					}
				}
			});
		},
		deleteUser: function(projName, email, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+ projName + "/user?email="+email ,
				type: "DELETE",
				dataType: "json",
				success: function() {
					if(typeof successCallback === 'function') {
						successCallback();
					}
				},
				error: function(jqXR) {
					if(typeof failCallback === 'function'){
						failCallback(jqXR);
					}
				}
			});
		},
		deleteProjLang: function(projName, langCode, successCallback, failCallBack) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+projName+"/lang?langCode="+ langCode,
				type: "DELETE",
				dataType: "json",
				success: function() {
					if(typeof successCallback === 'function') {
						successCallback();
					} 
				},
				error: function(jqXR) {
					if(typeof failCallBack === 'function') {
						failCallBack(jqXR);
					}
				}
			});
		},
		updateUser: function(projectName, role, keyString, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+ projectName +"/user/" + keyString,
				type: "POST",
				dataType: "json",
				data: {
					"role": role,
				},
				success: function(projUser) {
					if(typeof successCallback === 'function') {
						successCallback();
					}
				},
				error: function(jqXR) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
				}
			});
		},
		deleteContext: function(projName, keyString, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+ projName +"/context?keyString="+keyString,
				type:"DELETE",
				dataType: "json",
				success: function(projUser) {
					if(typeof successCallback === 'function') {
						successCallback();
					}
				},
				error: function(jqXR) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
				}
			});
		},
		updateContext: function(projName, contextName, description, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/" + projName +"/context/" + contextName,
				type: "POST",
				dataType: "json",
				data: {
					description: description,
				},
				success: function(ctx) {
					if(typeof successCallback === 'function') {
						successCallback(ctx);
					}
				},
				error: function(jqXR) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
				}
			});
		},
		deleteSubset: function(projName, keyString, successCallback, failCallback) {
			var that = this;
			$.ajax({
				url: that.backendUrl + "project/v10/"+ projName +"/subset?keyString="+keyString,
				type:"DELETE",
				dataType: "json",
				success: function(projUser) {
					if(typeof successCallback === 'function') {
						successCallback();
					}
				},
				error: function(jqXR) {
					if(typeof failCallback === 'function') {
						failCallback(jqXR);
					}
				}
			});
		}
	};
	if(typeof exports !== 'undefined') exports.RicottaAPI = RicottaAPI;
	else window.RicottaAPI = RicottaAPI;
})();

var ricottaAPI = new RicottaAPI();