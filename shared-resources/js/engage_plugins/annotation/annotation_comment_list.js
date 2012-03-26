/**
 *  Copyright 2009-2011 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
 
var Opencast = Opencast || {};

/**
 * @namespace the global Opencast namespace Annotation_Comment_List
 */
Opencast.Annotation_Comment_List = (function ()
{
    var mediaPackageId;
    var COMMENTNOHIDE = "Comments",
    COMMENTSHIDE = "Hide Comments",
    defaultText = "Type Your Comment Here",
    annotationType = "comment",
    isOpening = false,
    isOpen = false;
    
    var replyTemplate =  '<tr class="oc-comment-list-reply-row" id="comment-row-reply-form"  >' +
                            '<td class="oc-comment-list-border" style="cursor:pointer;cursor:hand;">' +
                            	'<div id="oc-comment-list-reply-wrapper" style="display:none">'+
                            		'<p style="width:65px;float: left;"></p> ' +                           	                         
	                            	'<div class="oc-comment-list-left-reply-row" align="left" style="cursor:pointer;cursor:hand;">' +
	                            	    '<div class="oc-comment-list-user-icon-reply"><img src="/engage/ui/img/annotation/user.png" width="50" height="50"></div>' +
	                            	'</div>' +
                            		'<div class="oc-comment-list-reply-form">' + 
                            			'<div id="oc-comments-list-reply-addbox">' +
											'<div>' +
												'<input id="oc-comments-list-reply-namebox" name="Name" type="text">' +
											'</div>' +
											'<textarea id="oc-comments-list-reply-textbox" ></textarea>' +
											'<input id="oc-comments-list-reply-cancel" value="Cancel" role="button" type="button" />' +
											'<input id="oc-comments-list-reply-submit" value="Add" role="button" type="button" />' +
										'</div>' +
                            		'</div>' +
                            	'</div>' +
                            '</td>' +                            
                        '</tr>';
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Initializes the segments view
     */
    function initialize()
    {
	var reg = Opencast.Plugin_Controller.registerPlugin(Opencast.Annotation_Comment_List);
	$.log("Opencast.Annotation_Comment_List registered: " + reg);

        $.log("Comment List Plugin init");
        
        // //Add Comment Form // //
        $("#oc-comments-list-textbox").val(defaultText);
  		$("#oc-comments-list-namebox").val(Opencast.Annotation_Comment.getUsername());
        $("#oc-comments-list-submit").click(function(){        
            submitComment(false);         
        });
        $("#oc-comments-list-submit-timed").click(function(){        
            submitComment(true);         
        });
        
        // Handler keypress CTRL+Enter on textbox
        $("#oc-comments-list-textbox").keyup(function (event)
        {
            if (event.ctrlKey === true)
            {
                if (event.which === 13)
                {
                    submitComment();
                }

            }
        });
        
        // //
        
        //focus and mark text by click on textbox
        $("#oc-comments-list-textbox").click(function(){        
            $("#oc-comments-list-textbox").focus();
            $("#oc-comments-list-textbox").select();          
        });

        $("#oc-comments-list-namebox").click(function(){        
            $("#oc-comments-list-namebox").focus();
            $("#oc-comments-list-namebox").select();          
        });
        
        //
        $.log("init list bindings");

        $('#scrubber').bind('changePosition', function (event, ui)         
        {
        	$("#oc-comments-list-submit-timed").val("Add Comment At "+Opencast.Player.getCurrentTime());
        });

        $('#draggable').bind('dragstop', function (event, ui)         
        {
        	$("#oc-comments-list-submit-timed").val("Add Comment At "+Opencast.Player.getCurrentTime());
        });
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description submitComment
     */
    function submitComment(isTimed)
    { 
		var textBoxValue = $("#oc-comments-list-textbox").val();
		textBoxValue = textBoxValue.replace(/<>/g, "");
		textBoxValue = textBoxValue.replace(/'/g, "`");
		textBoxValue = textBoxValue.replace(/"/g, "`");
		var nameBoxValue = $("#oc-comments-list-namebox").val();
		nameBoxValue = nameBoxValue.replace(/<>/g, "");
		nameBoxValue = nameBoxValue.replace(/'/g, "`");
		nameBoxValue = nameBoxValue.replace(/"/g, "`");
		$.log("click submit " + textBoxValue + " " + nameBoxValue);
		if(textBoxValue !== defaultText && nameBoxValue !== Opencast.Annotation_Comment.getDefaultUsername()) {
			if(isTimed) {
				//pause player
				Opencast.Player.doPause();
				addComment(textBoxValue, nameBoxValue, "scrubber", Math.round(Opencast.Player.getCurrentPosition()));
			} else {
				addComment(textBoxValue, nameBoxValue, "normal");
			}

			$("#oc-comments-list-textbox").val(defaultText);
			$("#oc-comments-list-namebox").val(Opencast.Annotation_Comment.getUsername());
		} else if(textBoxValue === defaultText) {
			$("#oc-comments-list-textbox").focus();
			$("#oc-comments-list-textbox").select();
		} else if(nameBoxValue === Opencast.Annotation_Comment.getUsername()) {
			$("#oc-comments-list-namebox").focus();
			$("#oc-comments-list-namebox").select();
		} else {
			$.log("Opencast.Annotation_Comment_List: illegal input state");
		}

     
    }    

    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Refresh the username in the UI
     */
    function refreshUIUsername()
    {
    	$("#oc-comments-list-namebox").val(Opencast.Annotation_Comment.getUsername());
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(value,user,type,pos,replyID)
    {      
        /* // Get user by system
        var user = "Anonymous";
        if(Opencast.Player.getUserId() !== null){
            user = Opencast.Player.getUserId();
        }
        */
            
        //Set Username
        Opencast.Annotation_Comment.setUsername(user);
       	var timePos = 0;
        if(type === "reply"){
        	//comment data [user]<>[text]<>[type]<>[replyID]
        	if(replyID !== undefined){
        		data = user+"<>"+value+"<>"+type+"<>"+replyID;
        		
        	}else{
        		$.log("Opencast.Annotation_Comment_List: illegal add comment parameters");
        		return;
        	}
        }else{
	        //comment data [user]<>[text]<>[type]
	        data = user+"<>"+value+"<>"+type;
	        if(pos !== undefined)
	        	  timePos = pos;      
        }
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+timePos+"&value="+data+"&out="+0,
            dataType: 'xml',
            success: function (xml)
            {
                $.log("Add_Comment success");
                show(); //show list
                //show scrubber comments if a scrubber comment was added
                if(type === "scrubber"){
                	if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === false){
            			$("#oc_checkbox-annotation-comment").attr('checked', true);
        			}
        			Opencast.Annotation_Comment.show();
                }                           
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                $.log("Add_Comment error: "+textStatus);
            }
        });
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Show the Annotation_Comment_List
     */
    function show()
    {
	if(!isOpen && !isOpening)
	{
	    isOpening = true;
            // Hide other Tabs
	    Opencast.Plugin_Controller.hideAll(Opencast.Annotation_Comment_List);
            // Change Tab Caption
            $('#oc_btn-comments-tab').attr(
		{
		    title: COMMENTSHIDE
		});
            $('#oc_btn-comments-tab').html(COMMENTSHIDE);
            $("#oc_btn-comments-tab").attr('aria-pressed', 'true');
            // Show a loading Image
            $('#oc_comments-list-wrapper').show();
            $('#oc_comments-list-loading').show();
            $('#oc-comments-list-header').hide();
            $('#oc-comments-list').hide();
            $('#oc-comments-list-add-form').hide();
            
            // Request JSONP data
            $.ajax(
		{
		    url: Opencast.Watch.getAnnotationURL(),
		    data: "episode=" + mediaPackageId+"&type="+annotationType+"&limit=1000",
		    dataType: 'json',
		    jsonp: 'jsonp',
		    success: function (data)
		    {
			$.log("Annotation AJAX call: Requesting data succeeded");
			
			var count = 0;
			var slideCount = 0;
			var scrubberCount = 0;
			var normalCount = 0;
			var replyCount = 0;
			
			if ((data === undefined) || (data['annotations'] === undefined) || (data['annotations'].annotation === undefined))
			{
			    $.log("Annotation AJAX call: Data not available");
			    //show nothing
			    $('#oc-comments-list').html("");
			    //displayNoAnnotationsAvailable("No data defined");
			    isOpening = false;
			}
			else
			{
			    $.log("Annotation AJAX call: Data available");
			    
			    var commentData = new Object();                  
			    var commentArray = new Array();
			    var replyArray = new Array();
			    
			    if(data['annotations'].total > 1){
				$(data['annotations'].annotation).each(function (i)
								       {
									   //split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
									   //OR split data by <> [user]<>[text]<>[type]<>[replyID]
									   var dataArray = data['annotations'].annotation[i].value.split("<>");
									   var comment = new Object();
									   
									   comment.id = data['annotations'].annotation[i].annotationId;
									   comment.inpoint = data['annotations'].annotation[i].inpoint;
									   var created = data['annotations'].annotation[i].created;         
									   var dateCr = $.dateStringToDate(created);
									   comment.created = $.getDateString(dateCr) + " " + $.getTimeString(dateCr);
									   comment.user = dataArray[0];
									   comment.text = dataArray[1];
									   comment.type = dataArray[2];                            
									   if(dataArray[2] === "slide"){
									       comment.slide = dataArray[5];
									       slideCount++;                                    
									   }else if(dataArray[2] === "scrubber"){
									       scrubberCount++;
									   }else if(dataArray[2] === "normal"){
									       normalCount++;
									   }else if(dataArray[2] === "reply"){
                            						       comment.replyID = dataArray[3];
									   }
									   if(dataArray[2] !== "reply"){
                            						       commentArray[count] = comment;
                            						       count++; 
									   }else{
                            						       replyArray[replyCount] = comment;
                            						       replyCount++;
									   }                                            
								       });
				//last comments on top
				commentArray.reverse();
				replyArray.reverse();
			    }
			    else if(data['annotations'].total !== 0)
			    {
				//split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
				//OR split data by <> [user]<>[text]<>[type]<>[replyID]
				var dataArray = data['annotations'].annotation.value.split("<>");
				var comment = new Object();
				comment.id = data['annotations'].annotation.annotationId;
				comment.inpoint = data['annotations'].annotation.inpoint;
				var created = data['annotations'].annotation.created;         
				var dateCr = $.dateStringToDate(created);
				comment.created = $.getDateString(dateCr) + " " + $.getTimeString(dateCr);
				comment.user = dataArray[0];
				comment.text = dataArray[1];
				comment.type = dataArray[2];                            
				if(dataArray[2] === "slide"){
                                    comment.slide = dataArray[5];
                                    slideCount++;                                    
				}else if(dataArray[2] === "scrubber"){
                                    scrubberCount++;
				}else if(dataArray[2] === "normal"){
                                    normalCount++;
				}else if(dataArray[2] === "reply"){
                            	    comment.replyID = dataArray[3];
				}
				if(dataArray[2] !== "reply"){
                            	    commentArray[0] = comment;
                            	    count++; 
				}else{
                            	    replyArray[0] = comment;
                            	    replyCount++;
				}    
			    }
			    
			    commentData.comment = commentArray;
			    commentData.replys = replyArray;
			    
			    $.log("commentList template process");
			    // Create Trimpath Template
			    var commentListSet = Opencast.Annotation_Comment_List_Plugin.addAsPlugin($('#oc-comments-list'), commentData);
			    
			    if (!commentListSet)
			    {
				$.log("No commentList template processed");
			    }        
			}
			
			//process header
			if(count === 1){
			    $("#oc-comments-list-header-top").html(count+" Comment");
			}else{
			    $("#oc-comments-list-header-top").html(count+" Comments");
			}
			var line = "";
			if(slideCount === 1){
			    line += slideCount+ " slide comment, ";
			}else{
			    line += slideCount+ " slide comments, ";
			}
			if(scrubberCount === 1){
			    line += scrubberCount+ " timed comment, ";
			}else{
			    line += scrubberCount+ " timed comments, ";
			}
			if(normalCount === 1){
			    line += normalCount+ " regular comment and ";
			}else{
			    line += normalCount+ " regular comments and ";
			}
			
			if(replyCount === 1){
			    line += replyCount+ " reply";
			}else{
			    line += replyCount+ " replys";
			}
			$("#oc-comments-list-header-bottom").html(line);
			
			$('#oc_comments-list-loading').hide();
			$('#oc-comments-list-header').show();
			$('#oc-comments-list').show();
			$('#oc-comments-list-add-form').show();
			//scroll down
			$(window).scrollTop( $('#oc_btn-comments-tab').offset().top - 10 );
			isOpening = false;
			isOpen = true;
		    },
		    // If no data comes back
		    error: function (xhr, ajaxOptions, thrownError)
		    {
			$.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
			isOpening = false;
		    }
		});
	} 
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Hide the Annotation_Comment_List
     */
    function hide()
    {
	if(isOpen)
	{
            // Change Tab Caption
            $('#oc_btn-comments-tab').attr(
		{
		    title: COMMENTNOHIDE
		});
            $('#oc_btn-comments-tab').html(COMMENTNOHIDE);
            $("#oc_btn-comments-tab").attr('aria-pressed', 'false');
            $('#oc_comments-list-wrapper').hide();
	    isOpen = false;
	}
    }
    
    /**
     @memberOf Opencast.Annotation_Comment_List
     @description Toggle the Annotation_Comment_List
     */
    function doToggle()
    {
        if (!isOpen)
        {
	    Opencast.Plugin_Controller.hideAll(Opencast.Annotation_Comment_List);
            show();
        }
        else
        {
            hide();
        }
    }

    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description handle click in the comment list
     * @param commentID, commentValue, commentTime, commentSlide, userId, type
     */
    function clickCommentList(commentID, commentValue, commentTime, commentSlide, userId, type)
    {
        goToComment(commentID, commentValue, commentTime, commentSlide, userId, type);
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description handle hover out the comment list
     */
    function hoverOutCommentList()
    {
        $('#oc-comments-list-item-tooltip').hide();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description deletes comment
     */
    function deleteComment(commentID)
    {
        // ajax DELETE Request
        $.ajax(
        {
            type: 'DELETE',
            url: "../../annotation/"+commentID,
            statusCode: {
                200: function() {
                    $.log("Comment DELETE Ajax call: Request success");
                    show();
                    if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === true){
                        Opencast.Annotation_Comment.show();
                    }
                }
            },
            complete:
                function(jqXHR, textStatus){
                    $.log("Comment DELETE Ajax call: Request success");
                    show();
                    if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === true){
                        Opencast.Annotation_Comment.show();
                    }
                }
            
        });
    }   
    
    /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description go to a comment
     * @param commentID, commentValue, commentTime, commentSlide, userId, type
     */
    function goToComment(commentID, commentValue, commentTime, commentSlide, userId, type)
    {
        if(Opencast.Annotation_Comment.getAnnotationCommentDisplayed() === false){
            $("#oc_checkbox-annotation-comment").attr('checked', true);
            Opencast.Annotation_Comment.show();
        }
        
        //click on comment
        if(type === "scrubber"){
            //seek player to comment
        	Opencast.Watch.seekSegment(parseInt(commentTime)-2);
            //scroll to comment
            $(window).scrollTop( 0 );
        }else if(type === "slide"){
        	//Seek to slide
        	$("#segment"+commentSlide).click();
        	//scroll to comment
            $(window).scrollTop( 0 );
        }      
    }
    
        /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
     /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description 
     * @param String commentID
     */
    function replyComment(commentID)
    {
    	//Only one reply form allowed
    	cancelReply(function(){
	 		$("#comment-row-"+commentID).after(replyTemplate);
			$("#oc-comment-list-reply-wrapper").slideDown(600);
			//Submit Button
			$("#oc-comments-list-reply-submit").click(function(){
				var textBoxValue = $("#oc-comments-list-reply-textbox").val();
				textBoxValue = textBoxValue.replace(/<>/g, "");
				textBoxValue = textBoxValue.replace(/'/g, "`");
				textBoxValue = textBoxValue.replace(/"/g, "`");
				var nameBoxValue = $("#oc-comments-list-reply-namebox").val();
				nameBoxValue = nameBoxValue.replace(/<>/g, "");
				nameBoxValue = nameBoxValue.replace(/'/g, "`");
				nameBoxValue = nameBoxValue.replace(/"/g, "`");
				$.log("click submit " + textBoxValue + " " + nameBoxValue);
				if(textBoxValue !== defaultText && nameBoxValue !== Opencast.Annotation_Comment.getDefaultUsername()) {
					addComment(textBoxValue, nameBoxValue, "reply",0, commentID);
					cancelReply();
				} else if(textBoxValue === defaultText) {
					$("#oc-comments-list-reply-textbox").focus();
					$("#oc-comments-list-reply-textbox").select();
				} else if(nameBoxValue === Opencast.Annotation_Comment.getUsername()) {
					$("#oc-comments-list-reply-namebox").focus();
					$("#oc-comments-list-reply-namebox").select();
				} else {
					$.log("Opencast.Annotation_Comment_List: illegal input state");
				}			
			});
			//Cancel Button
			$("#oc-comments-list-reply-cancel").click(function(){
				cancelReply();
			});
			//Default Text and Name
			$("#oc-comments-list-reply-textbox").val(defaultText);
	  		$("#oc-comments-list-reply-namebox").val(Opencast.Annotation_Comment.getUsername());
	  		// Handler keypress CTRL+Enter on textbox
	        $("#oc-comments-list-reply-textbox").keyup(function (event)
	        {
	            if (event.ctrlKey === true)
	            {
	                if (event.which === 13)
	                {
	                    $("#oc-comments-list-reply-submit").click();
	                }
	
	           	}
        	});  		
    	});
    }
    
     /**
     * @memberOf Opencast.Annotation_Comment_List
     * @description removes the reply form from the DOM
     */
    function cancelReply(cancel_callback)
    {
    	if($("#oc-comment-list-reply-wrapper")[0] !== undefined){
			//Delete Reply Form
			$("#oc-comment-list-reply-wrapper").slideUp(500,function(){
				$("#comment-row-reply-form").remove();
				if(cancel_callback !== undefined)
					cancel_callback();
			});
		}else{
			if(cancel_callback !== undefined)
				cancel_callback();
		}	
    }
    
    return {
        initialize: initialize,
        show: show,
        hide: hide,
        refreshUIUsername: refreshUIUsername,
        clickCommentList: clickCommentList,
        deleteComment: deleteComment,
        replyComment: replyComment,
        cancelReply: cancelReply,
        hoverOutCommentList: hoverOutCommentList,
        goToComment: goToComment,
        doToggle: doToggle,
        setMediaPackageId: setMediaPackageId
    };
}());
