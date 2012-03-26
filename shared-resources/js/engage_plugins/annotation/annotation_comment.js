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
 * Global object constructors
 */

/**
 * @memberOf .
 * @description Scrubber Comment Object 
 */
function ScrubberComment(username, id, text, inpoint){
	var creator = username;
	var commentId = id;
	var commentText = text;
	var commentInpoint = inpoint;
	var self = this;
	
	this.getCreator = (function(){
		return creator;
	});
	this.getID = (function(){
		return commentId;
	});
	this.getText = (function(){
		return commentText;
	});
	this.getInpoint = (function(){
		return commentInpoint;
	});
}
/**
 * @memberOf .
 * @description Slide Comment Object 
 */
function SlideComment(username, id, text, slide, relPos){
	var creator = username;
	var commentId = id;
	var commentText = text;
	var commentOnSlide = slide;
	var slidePosition = relPos;
	var self = this;
	
	this.getCreator = (function(){
		return creator;
	});
	this.getID = (function(){
		return commentId;
	});
	this.getText = (function(){
		return commentText;
	});
	this.getSlideNr = (function(){
		return commentOnSlide;
	});
	this.getX = (function(){
		return slidePosition.x;
	});
	this.getY = (function(){
		return slidePosition.y;
	});
}
/**
 * @memberOf .
 * @description Reply Comment Object 
 */
function ReplyComment(username, id, text, replyId){
	var creator = username;
	var commentId = id;
	var commentText = text;
	var commentReplyId = replyId;
	var self = this;
	
	this.getCreator = (function(){
		return creator;
	});
	this.getID = (function(){
		return commentId;
	});
	this.getText = (function(){
		return commentText;
	});
	this.getResponseTo = (function(){
		return commentReplyId;
	});
}

function ReplyMap(){
    var reply_map = new Object();
    
    //Adds a reply to the map
    this.addReplyToComment = (function(reply){
        if(reply_map[reply.getResponseTo()] !== undefined){
            var a_end = reply_map[reply.getResponseTo()].length;
            reply_map[reply.getResponseTo()][a_end] = reply;
        }else{
            reply_map[reply.getResponseTo()] = new Array();
            reply_map[reply.getResponseTo()][0] = reply;
        }
    });
    //Returns array with comments
    this.getReplysToComment = (function(cId){
       return reply_map[cId]; 
    });
    //Removes a reply by given reply ID, returns true if found and removed
    this.removeReplyByID = (function(rId){
    	var found = false;
    	//for each comments
        for(i in reply_map){
        	if(found===true)
        		break;
        	//for each replys to this comment
        	$(reply_map[i]).each(function(j){
        		if(found===true)
        			return;
        		if(reply_map[i][j].getID() === rId){
        			//remove found item
        			reply_map[i].splice(j,1);
        			found = true;
        			return;
        		}
        	});
        }
        return found;
    });
    //Removes all replys to a given comment ID, returns true if found and removed
    this.removeReplysByCID = (function(cId){
        if(reply_map[cId] !== undefined){
        	delete reply_map[cId];
        	return true;
        }else{
        	return false;
        }
    });    
    
}

/**
 * @namespace the global Opencast namespace Annotation_Comment
 */
Opencast.Annotation_Comment = (function ()
{
    var mediaPackageId, duration;
    var annotationCommentDisplayed = false;
    var ANNOTATION_COMMENT = "Annotation",
        ANNOTATION_COMMENTHIDE = "Annotation off";
    var annotationType = "comment";
    var oldSlideId = 0;
    var relativeSlideCommentPosition;
    var clickedOnHoverBar = false;
    var clickedOnComment = false;
    var hoverInfoBox = false;
    var infoTime = "";
    var commentAtInSeconds;
    var cookieName = "oc_comment_username";
    var default_name = "Your Name!";
    var cm_username;
    var defaul_comment_text = "Type Your Comment Here!"
    var comments_cache;
    var time_offset = 3;
    var reply_map;
    var modus = "private";
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Initializes Annotation Comment
     */

    function initialize()
    {
    	
    	$("Comment Plugin init");
    	
    	if(modus === "public"){
        	//Read Cookie for default Name
    		cm_username = default_name;
    		var nameEQ = cookieName + "=";
    		var ca = document.cookie.split(';');
    		for(var i = 0; i < ca.length; i++) {
    			var c = ca[i];
    			while(c.charAt(0) == ' ')
    			c = c.substring(1, c.length);
    			if(c.indexOf(nameEQ) == 0)
    				cm_username = c.substring(nameEQ.length, c.length);
    		}
		}else if(modus === "private"){
		    //set username
		    loggedUser();
		    //disable username input
		}else{
		    //TODO: error deactivate plugin
		}
		//if user logged in use his username
		
		
		
		$.log("Comment Plugin set username to: "+cm_username);
    	
    	
       	// Handler keypress ALT+CTRL+a
        $(document).keyup(function (event)
        {
            if (event.altKey === true && event.ctrlKey === true)
            {
                if (event.which === 65)
                {
                    if(annotationCommentDisplayed === true){
				    	$("#oc_btn-add-comment").click();            	
                    }

                }

            }
        });
        
         //// UI ////
        //add scrubber comment handler
        $("#oc_btn-add-comment").click(function(){
        	//pause player
        	Opencast.Player.doPause();
        	
        	//exit shown infos
        	$(".oc-comment-exit").click();
        	
        	clickedOnHoverBar = true;
    	    //hide other slide comments
        	$('div[id^="scComment"]').hide();			
			//process position and set comment info box
			var left = $("#scrubber").offset().left + ($("#scrubber").width() / 2) ;
			var top = $("#data").offset().top - 136;
			$("#comment-Info").css("left", left+"px");
			$("#comment-Info").css("top", top+"px");
			//show info
			$("#comment-Info").show();
			$("#cm-info-box").show();
			//process current time
            var curTime;
            if(parseInt(Opencast.Player.getCurrentPosition()) > time_offset)
            	curTime = $.formatSeconds((parseInt(Opencast.Player.getCurrentPosition()) - time_offset));
            else
            	curTime = Opencast.Player.getCurrentTime();
			//set top header info
			$("#oc-comment-info-header-text").html("Comments at "+curTime);
			
			//process comment input form
			if(modus === "private"){
                $("#oc-comment-info-value-wrapper").html(
                    '<div id="oc-comment-info-header-1" class="oc-comment-info-cm-header">'+
                        '<input id="oc-comment-add-submit" class="oc-comment-submit" value="Add" role="button" type="button" />'+           
                        '<input id="oc-comment-add-namebox" class="oc-comment-namebox" type="text" value="'+cm_username+'" disabled="disabled">'+
                        '<div id="oc-comment-info-header-text-1" class="oc-comment-info-header-text"> at '+curTime+'</div>'+
                    '</div>'+
                    '<textarea id="oc-comment-add-textbox" class="oc-comment-textbox">Type Your Comment Here</textarea>'            
                );			    
			}else if(modus === "public"){
                $("#oc-comment-info-value-wrapper").html(
                    '<div id="oc-comment-info-header-1" class="oc-comment-info-cm-header">'+
                        '<input id="oc-comment-add-submit" class="oc-comment-submit" value="Add" role="button" type="button" />'+           
                        '<input id="oc-comment-add-namebox" class="oc-comment-namebox" type="text" value="'+cm_username+'">'+
                        '<div id="oc-comment-info-header-text-1" class="oc-comment-info-header-text"> at '+curTime+'</div>'+
                    '</div>'+
                    '<textarea id="oc-comment-add-textbox" class="oc-comment-textbox">Type Your Comment Here</textarea>'            
                );			    
			}

			//submit comment btn click handler
			$("#oc-comment-add-submit").click(function(){
				submitCommentHandler();
			});
			
			// Handler keypress CTRL+enter to submit comment
        	$("#oc-comment-add-textbox").keyup(function (event){
		        if (event.ctrlKey === true){
		            if (event.keyCode == 13){
		                submitCommentHandler();
		            }
		        }
        	});
        
			$('#oc-comment-info-header').attr(
            {
                title: "Add timed comment"
            });
            
            $("#oc-comment-add-namebox").focus();
            $("#oc-comment-add-namebox").select(); 
        });

        //double click handler on slide comment box
        $("#oc_slide-comments").dblclick(function(event){
        	
        	//exit shown infos
        	$(".oc-comment-exit").click();
        	
        	//hide doubleclick info
        	$("#oc_dbclick-info").hide();
        	
        	//pause player
    		Opencast.Player.doPause();
        	
        	//hide other slide comments
        	$('canvas[id^="slideComment"]').hide();
           
            var mPos = new Object();
            mPos.x = event.pageX - $('#oc_slide-comments').offset().left - 10;
            mPos.y = event.pageY - $('#oc_slide-comments').offset().top - 18;
            
            var relPos = new Object();
            if($('#oc_slide-comments').width() > 0){
                relPos.x = ( mPos.x / $('#oc_slide-comments').width() ) * 100;
            }else{
                relPos.x = 0;
            }
            if($('#oc_slide-comments').height() > 0){
                relPos.y = ( mPos.y / $('#oc_slide-comments').height() ) * 100;
            }else{
                relPos.y = 0;
            }    
            // set global variable
            relativeSlideCommentPosition = relPos;
            $('#oc-comment-info-header').attr(
            {
                title: "Add slide comment"
            });
            var ciLeft = event.pageX;
            var ciTop = event.pageY-137;

            $("#comment-Info").css("left", ciLeft+"px");
            $("#comment-Info").css("top", ciTop+"px");
            $("#comment-Info").show();
            $("#cm-info-box").show();           
                     
            //header info text
            var curSlide = Opencast.segments.getCurrentSlideId() + 1;
            var allSlides = Opencast.segments.getNumberOfSegments();
            var infoText = "Slide " + curSlide + " of " + allSlides;    
            $("#oc-comment-info-header-text").html(infoText);
            
            //process comment input form
			$("#oc-comment-info-value-wrapper").html(
	            '<div id="oc-comment-info-header-1" class="oc-comment-info-cm-header">'+
	            	'<input id="oc-comment-add-submit" class="oc-comment-submit" value="Add" role="button" type="button" />'+       	
	            	'<input id="oc-comment-add-namebox" class="oc-comment-namebox" type="text" value="Your name">'+
	            	'<div id="oc-comment-info-header-text-1" class="oc-comment-info-header-text"> at Slide '+curSlide+'</div>'+
	            '</div>'+
            	'<textarea id="oc-comment-add-textbox" class="oc-comment-textbox">Type Your Comment Here</textarea>'			
			);
			//submit comment btn click handler
			$("#oc-comment-add-submit").click(function(){
				submitCommentHandler();
			});
			
			// Handler keypress CTRL+enter to submit comment
        	$("#oc-comment-add-textbox").keyup(function (event){
		        if (event.ctrlKey === true){
		            if (event.keyCode == 13){
		                submitCommentHandler();
		            }
		        }
        	});
            
            $("#oc-comment-add-namebox").focus();
            $("#oc-comment-add-namebox").select();               
       });
        
        
        // resize handler
        $('#oc_flash-player').bind('doResize', function(e) {
           
           //hideAnnotation_Comment();
           
			//positioning of the slide comment box
			var flashWidth = $('#oc_flash-player').width() / 2;
			var flashHeight = $('#oc_flash-player').height()-10;
			var flashTop = $('#oc_flash-player').offset().top;
			var flashLeft = $('#oc_flash-player').offset().left;
			
			
			var scHeight = 0;
			var scWidth = 0;
			var scLeft = 0;
			var scTop = 0;
			
			if(((flashWidth - 5) / flashHeight) < (4/3) ){
				scHeight = (flashWidth - 5) / (4/3);
				scWidth = (4/3)*scHeight;
				scLeft = flashWidth;
				scTop = flashHeight - scHeight + 4;
			}else{
				scWidth = (4/3) * flashHeight;
				scHeight = scWidth / (4/3);
				scLeft = flashWidth;
				scTop = 5;
			}
			
			$("#oc_slide-comments").css("position","absolute");
			$("#oc_slide-comments").css("height",scHeight+"px");
			$("#oc_slide-comments").css("width",scWidth+"px");
			$("#oc_slide-comments").css("left",scLeft+"px");
			$("#oc_slide-comments").css("top",scTop+"px");
			/*
			if(annotationCommentDisplayed){
				$.log("BLUB");
			    //Workaround: 500ms after resize repaint comments and marks
			    window.setTimeout(function() {
			    	$.log("after resize and 500ms show annotations");  
					Opencast.Annotation_Comment.showAnnotation_Comment();
				}, 500);            	
			}*/
			//Opencast.Annotation_Comment.showAnnotation_Comment();
 
            
            
        });      
        
        $(".oc-comment-exit").click(function(){
            // hide info box
            $("#comment-Info").hide();
            clickedOnHoverBar = false;
            clickedOnComment = false;
			//show other slide comments
			$('canvas[id^="slideComment"]').show();
			$('div[id^="scComment"]').show();
			$('#oc-comment-info-header').attr(
            {
                title: ""
            });
        });
        
        // Handler keypress Enter on textbox
        $("#oc-comment-add-textbox").keyup(function (event)
        {
            if (event.which === 13)
            {
                $("#oc-comment-add-submit").click();
            }
        });
        // Handler keypress Enter on namebox
        $("#oc-comment-add-namebox").keyup(function (event)
        {
            if (event.which === 13)
            {
                $("#oc-comment-add-submit-name").click();
            }
        });
        
        //// UI END ////
        
        // change scrubber position handler
        $('#scrubber').bind('changePosition', function(e) {
        	//Check weather comments are on the current slide 
            if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                    //exit shown infos
        			$(".oc-comment-exit").click();
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }
            //Check weather comments are on the current time
            $('div[id^="scComment"]').each(function(i){
            	if((parseInt(Opencast.Player.getCurrentPosition())+1) === parseInt($(this).attr("inpoint"))){
            		if(clickedOnComment === false){
	            		//show comment info for 3 seconds
	            		$(this).mouseover();
	            		window.setTimeout(function() {  
	    					Opencast.Annotation_Comment.hoverOutComment();
						}, 3000); 
            		}
            	}
            });
            
        });
        $('#draggable').bind('dragstop', function (event, ui){
        	//Check wether comments on the current slide 
             if(Opencast.segments.getCurrentSlideId() !== oldSlideId){
                if(annotationCommentDisplayed){
                    showAnnotation_Comment();
                    //exit shown infos
        			$(".oc-comment-exit").click();                    
                }                   
                oldSlideId = Opencast.segments.getCurrentSlideId();
            }                
        });
        

       
       $("#oc_slide-comments").mouseenter(function(){
       	
       		var sl_left = $('#oc_slide-comments').offset().left + $('#oc_slide-comments').width() + 2;
       		var sl_top = $('#oc_slide-comments').offset().top + $('#oc_slide-comments').height() - 40;
       		
            $("#oc_dbclick-info").css("left", sl_left+"px");
            $("#oc_dbclick-info").css("top", sl_top+"px");
            
            $("#oc_dbclick-info").show();     	
       });
       
       $("#oc_slide-comments").mouseleave(function(){
       		$("#oc_dbclick-info").hide();
       });
        
        // Display the controls
        $('#oc_checkbox-annotation-comment').show();    // checkbox
        $('#oc_label-annotation-comment').show();       // 
        $('#oc_video-view').show();                     // slide comments
        //$("#oc_ui_tabs").tabs('enable', 3);             // comment tab

    }

    /**
     * @memberOf Opencast.Annotation_Comment
     * @description handler for submit btn
     */
     function loggedUser(){
        $.ajax(
        {
            url: "../../info/me.json",
            data: "",
            dataType: 'json',
            jsonp: 'jsonp',
            success: function (data)
            {
                if ((data !== undefined) || (data['username'] !== undefined))
                {   
                     if(data.username === "anonymous"){
                         //TODO: what is to do if user not logged in, example: deactivate feature
                     }else{
                         cm_username = data.username;
                     }
                }  
            }
        });     
     
     }
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description handler for submit btn
     */
     function submitCommentHandler(){
        	if($("#oc-comment-add-textbox").val() !== defaul_comment_text || $("#oc-comment-add-namebox").val() !== default_name){
		    	// hide comment info box
				$("#comment-Info").hide();
				$("#cm-info-box").hide();
		        clickedOnHoverBar = false;
		        var commentValue = $("#oc-comment-add-textbox").val();
		        commentValue = commentValue.replace(/<>/g,"");
		        commentValue = commentValue.replace(/'/g,"`");
		        commentValue = commentValue.replace(/"/g,"`");
		        commentValue = commentValue.replace(/\n/,"");	        
		        var nameValue = $("#oc-comment-add-namebox").val();
		        nameValue = nameValue.replace(/<>/g,"");       
		        nameValue = nameValue.replace(/'/g,"`"); 
		        nameValue = nameValue.replace(/"/g,"`");  
				//show other slide comments
				$('canvas[id^="slideComment"]').show();
				$('div[id^="scComment"]').show(); 
		        if($('#oc-comment-info-header').attr("title") === "Add timed comment"){
		        	var curTime;
					if(parseInt(Opencast.Player.getCurrentPosition()) > time_offset)
						curTime = parseInt(Opencast.Player.getCurrentPosition()) - time_offset;
					else
						curTime = parseInt(Opencast.Player.getCurrentPosition());
		            //add scrubber comment
		            addComment(nameValue,curTime,commentValue,"scrubber");                
		        }else if($('#oc-comment-info-header').attr("title") === "Add slide comment"){
		            //add slide comment
		            addComment(nameValue,
								parseInt(Opencast.Player.getCurrentPosition()),
								commentValue,
								"slide",
								relativeSlideCommentPosition.x,
								relativeSlideCommentPosition.y,
								Opencast.segments.getCurrentSlideId()
		                      );                              
		        }
				$('#oc-comment-info-header').attr(
		        {
		            title: ""
		        });
		    }
        }

    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Set username
     * @param String username
     */
    function setUsername(user)
    {
    	//Create cookie with username
        document.cookie = cookieName+"="+user+"; path=/engage/ui/";
    	cm_username = user;
    	
    	//Refresh UI
    	Opencast.Annotation_Comment_List.refreshUIUsername();
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Get username
     */
    function getUsername()
    {
    	return cm_username;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Get default username
     */
    function getDefaultUsername()
    {
    	return default_name;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Add a comment
     * @param Int position, String value
     */
    function addComment(user,curPosition,value,type,xPos,yPos,segId)
    {
        //var user = "Anonymous";
        //if(Opencast.Player.getUserId() !== null){
        //    user = Opencast.Player.getUserId();
        //}
        
        //Set username
        setUsername(user);
        
        //comment data [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
        var data = "";
        if(xPos !== undefined && yPos !== undefined){
            data = user+"<>"+value+"<>"+type+"<>"+xPos+"<>"+yPos+"<>"+segId;
            //var markdiv = "<div style='height:100%; width:5px; background-color: #A72123; float: right;'> </div>";
            //$("#segment"+segId).html(markdiv);
        }else{
            data = user+"<>"+value+"<>"+type;        
        }
        
        $.ajax(
        {
            type: 'PUT',
            url: "../../annotation/",
            data: "episode="+mediaPackageId+"&type="+annotationType+"&in="+curPosition+"&value="+data+"&out="+curPosition,
            dataType: 'xml',
            success: function (xml)
            {
                $.log("add comment success");
                //erase cache
                comments_cache = undefined;
                //show new comments
                showAnnotation_Comment();
                //check checkbox
                $('#oc_checkbox-annotation-comment').attr('checked', true);
                
                var comment_list_show = $('#oc_btn-comments-tab').attr("title");
                if(comment_list_show == "Hide Comments"){
                    Opencast.Annotation_Comment_List.showComments();
                }                    
            },
            error: function (jqXHR, textStatus, errorThrown)
            {
                $.log("Add_Comment error: "+textStatus);
            }
        });
    }
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Show Annotation_Comment
     */
    function showAnnotation_Comment()
    {
        annotationCommentDisplayed = true;
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
                
                //demark segements              
                for(var slidesNr = Opencast.segments.getNumberOfSegments()-1 ; slidesNr >= 0 ; slidesNr--){
                    $("#segment"+slidesNr).html("");
                }
                
                if ((data === undefined) || (data['annotations'] === undefined) || (data['annotations'].annotation === undefined))
                {
                    $.log("Annotation AJAX call: Data not available");
                    //show nothing
                    $('#oc-comment-scrubber-box').html("");
                    $('#oc_slide-comments').html("");
                }
                else
                {
                    $.log("Annotation AJAX call: Data available");
                    data['annotations'].duration = duration; // duration is in seconds
                    data['annotations'].nrOfSegments = Opencast.segments.getNumberOfSegments();
                    
                    var scrubberData = new Object();
                    var slideData = new Object();
                    
                    var scrubberArray = new Array();
                    var slideArray = new Array();
                    var replyArray = new Array();
                    var toMarkSlidesArray = new Array();

                    scrubberData.duration = duration;
                    scrubberData.type = "scrubber";
                    slideData.type = "slide";
                    
                    reply_map = new ReplyMap();
                    
                    if(data['annotations'].total > 1){
                        var scCount = 0;
                        var slCount = 0;
                        var replyCount = 0;
                        $(data['annotations'].annotation).each(function (i)
                        {
                            //split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
                            //OR split data by <> [user]<>[text]<>[type]<>[replyID]
                            var dataArray = data['annotations'].annotation[i].value.split("<>");
                            //found scrubber comment
                            if(dataArray[2] === "scrubber"){
                            	comment = new ScrubberComment(
                            		dataArray[0], //username
                            		data['annotations'].annotation[i].annotationId, //ID
                            		dataArray[1], //text
                            		data['annotations'].annotation[i].inpoint //inpoint
                            	);                                                                                   
                                scrubberArray[scCount] = comment;
                                scCount++;
                            //found slide comment on current slide
                            }else if(dataArray[2] === "slide" && dataArray[5] == Opencast.segments.getCurrentSlideId()){
                            	var relPos = {x:dataArray[3],y:dataArray[4]};
                            	comment = new SlideComment(
                            		dataArray[0], //username
                            		data['annotations'].annotation[i].annotationId, //ID
                            		dataArray[1], //text
                            		dataArray[5], //slide nr
                            		relPos //relative position on the slide
                            	);              
                                slideArray[slCount] = comment;
                                slCount++;
                                var slideFound = false;
                                for (i in toMarkSlidesArray) {
       								if (toMarkSlidesArray[i] === dataArray[5]) {
       									slideFound = true;
       								}
   								}
   								if(slideFound === false){
   									toMarkSlidesArray[toMarkSlidesArray.length] = dataArray[5];
   								}
   							//found slide comment                               
                            }else if(dataArray[2] === "slide"){
                                var slideFound = false;
                                for (i in toMarkSlidesArray) {
       								if (toMarkSlidesArray[i] === dataArray[5]) {
       									slideFound = true;
       								}
   								}
   								if(slideFound === false){
   									toMarkSlidesArray[toMarkSlidesArray.length] = dataArray[5];
   								}                             	
                            }else if(dataArray[2] === "reply"){
                            	comment = new ReplyComment(
                            		dataArray[0], //username
                            		data['annotations'].annotation[i].annotationId, //ID
                            		dataArray[1], //text
                            		dataArray[3] //Reply ID
                            	);
                            	reply_map.addReplyToComment(comment);                           		
                            }                  
                            
                        });                       
                    }else if(data['annotations'].total !== 0){
                            //split data by <> [user]<>[text]<>[type]<>[xPos]<>[yPos]<>[segId]
                            var dataArray = data['annotations'].annotation.value.split("<>");
                            var comment = new Object();
                            comment.id = data['annotations'].annotation.annotationId;
                            comment.user = dataArray[0];
                            comment.text = dataArray[1];
                            if(dataArray[2] === "scrubber"){                              
                                comment.inpoint = data['annotations'].annotation.inpoint;
                                scrubberArray[0] = comment;
                            }else if(dataArray[2] === "slide" && dataArray[5] == Opencast.segments.getCurrentSlideId()){
                                comment.slideNr = dataArray[5];
                                comment.relPos = new Object();
                                comment.relPos.x = dataArray[3];
                                comment.relPos.y = dataArray[4];
                                comment.text = dataArray[1];
                                slideArray[0] = comment;
                                toMarkSlidesArray[0] = dataArray[5];                     
                            }else if(dataArray[2] === "slide"){
                            	toMarkSlidesArray[0] = dataArray[5];
                            }
                    }
                    
                    scrubberData.comment = scrubberArray;
                    slideData.comment = slideArray;

                    // Create Trimpath Template
                    var scrubberCommentSet = Opencast.Scrubber_CommentPlugin.addAsPlugin($('#oc-comment-scrubber-box'), scrubberData);
                    var slideCommentSet = Opencast.Slide_CommentPlugin.addAsPlugin($('#oc_slide-comments'), slideData);
                    if (!scrubberCommentSet)
                    {
                        $.log("No scrubberComment template processed");
                        //$("#oc-comment-scrubber-box").html("");
                    }
                    else
                    {                                                
                        //$("#oc-comment-scrubber-box").show();
                    }
                    
                    if (!slideCommentSet)
                    {
                        $.log("No slideComment template processed");
                        $("#oc_slide-comments").html("");
                    }
                    else
                    {                        
                        //$("#oc_slide-comments").show();
                    }
     
                    //mark segments
                    if(toMarkSlidesArray.length > 0){
                        $.log("Slide Comments available");
                        $(toMarkSlidesArray).each(function (i){
                        	$.log("Mark Slide: "+toMarkSlidesArray[i]);
                            var markdiv = "<div id='oc-comment-segmark_"+ toMarkSlidesArray[i] +"' style='width:6px; float: left;'> </div>";
                            $("#segment"+toMarkSlidesArray[i]).html(markdiv);
                            $("#oc-comment-segmark_"+ toMarkSlidesArray[i]).corner("cc:#000000 bevel bl 6px");
                        });
                    }
                                        
                    
                }
                $("#oc_slide-comments").show();
                $("#oc-comment-scrubber-box").show();
            },
            // If no data comes back
            error: function (xhr, ajaxOptions, thrownError)
            {
                $.log("Comment Ajax call: Requesting data failed "+xhr+" "+ ajaxOptions+" "+ thrownError);
            }
        });
    }
    
     /**
     * @memberOf Opencast.annotation_comment
     * @description hide scrubber comment on timeline
     */
    function hideScrubberComment()
    {
		$("#comment-Info").hide();
		$("#cm-info-hover").hide();
		$("#cm-info-box").hide();
		// back to default
		$("#oc-comment-add-textbox").val(defaul_comment_text);
		$("#oc-comment-add-namebox").val(cm_username);    	
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description shows given scrubber comment on timeline
     * @param commentId, commentValue, commentTime, userId
     */
    function showScrubberComment(commentId, commentValue, commentTime, userId)
    {
    	//process position and set comment info box
        var left = $("#scComment" + commentId).offset().left + 3;
        var top = $("#data").offset().top - 136;
        $("#comment-Info").css("left", left+"px");
        $("#comment-Info").css("top", top+"px");
        //show info, hide input forms
        $("#comment-Info").show();
        $("#cm-add-box").hide();
        $("#cm-info-box").show();
        $("#cm-info-hover").hide();
        //set top header info
        $("#oc-comment-info-header-text").html("Comments at "+$.formatSeconds(commentTime));
        //process html for comments
        //$("#oc-comment-info-textbox").html(commentValue);
        $("#oc-comment-info-value-wrapper").html(
        		"<div id='oc-comment-info-scComment"+commentId+"'>"+
		            "<div class='oc-comment-info-cm-header'>"+
		            	"<input onclick='Opencast.Annotation_Comment.deleteComment("+commentId+",\"scrubber\")' class='oc-comment-info-cm-btn oc-comment-info-cm-delbtn' type='image' src='/engage/ui/img/misc/space.png' name='Delete' alt='Delete' title='Delete' value='Delete'>"+
		            	"<input onclick='Opencast.Annotation_Comment.replyComment("+commentId+")' class='oc-comment-info-cm-btn oc-comment-info-cm-repbtn' type='image' src='/engage/ui/img/misc/space.png' name='Reply' alt='Reply' title='Reply' value='Reply'>"+
		            	"<input onclick='Opencast.Annotation_Comment.clickComment("+commentTime+")' class='oc-comment-info-cm-btn oc-comment-info-cm-gotobtn' type='image' src='/engage/ui/img/misc/space.png' name='Go To' alt='Go To' title='Go To' value='Go To'>"+
		            	"<div class='oc-comment-info-header-text'>"+userId+" at "+$.formatSeconds(commentTime)+"</div>"+
		            "</div>"+
		            "<p id='oc-comment-cm-textbox-"+commentId+"' class='oc-comment-cm-textbox'>"+commentValue+"</p>"
	            );
        //process html for replys
        $(reply_map.getReplysToComment(commentId)).each(function(i){
        	$("#oc-comment-info-value-wrapper").append(
        		"<div id='oc-comment-info-scComment"+reply_map.getReplysToComment(commentId)[i].getID()+"'>"+
	    			"<div class='oc-comment-info-reply-header'>"+
	            		"<input onclick='Opencast.Annotation_Comment.deleteComment("+reply_map.getReplysToComment(commentId)[i].getID()+",\"reply\")' class='oc-comment-info-cm-btn oc-comment-info-cm-delbtn' type='image' src='/engage/ui/img/misc/space.png' name='Delete' alt='Delete' title='Delete' value='Delete'>"+
	            		"<div class='oc-comment-info-header-text'>"+reply_map.getReplysToComment(commentId)[i].getCreator()+"</div>"+		            	
		            "</div>"+
	        		"<p class='oc-comment-reply-textbox'>"+reply_map.getReplysToComment(commentId)[i].getText()+"</p>"+
	        	"</div>"
            );
        });
        //close first comment tag
        $("#oc-comment-info-value-wrapper").append("</div>");    	
    }    

    /**
     * @memberOf Opencast.annotation_comment
     * @description open reply form and give possibilty to reply to given comment id
     * @param commentId
     */
    function replyComment(commentId)
    {
		//process comment input form
		if(modus === "private"){
            $("#oc-comment-cm-textbox-"+commentId).after(
                '<div id="oc-comment-reply-form" style="display:none;"'+
                    '<div id="oc-comment-info-header-reply" class="oc-comment-info-reply-header">'+
                        '<input id="oc-comment-add-submit" class="oc-comment-submit" value="Add" role="button" type="button"  />'+           
                        '<input id="oc-comment-add-namebox" class="oc-comment-namebox" type="text" value="'+cm_username+'" disabled="disabled">'+
                    '</div>'+
                    '<textarea id="oc-comment-add-reply-textbox" class="oc-comment-textbox">Type Your Comment Here</textarea>'+
                '</div>'            
            );		    
		}else if(modus === "public"){
            $("#oc-comment-cm-textbox-"+commentId).after(
                '<div id="oc-comment-reply-form" style="display:none;"'+
                    '<div id="oc-comment-info-header-reply" class="oc-comment-info-reply-header">'+
                        '<input id="oc-comment-add-submit" class="oc-comment-submit" value="Add" role="button" type="button" />'+           
                        '<input id="oc-comment-add-namebox" class="oc-comment-namebox" type="text" value="'+cm_username+'">'+
                    '</div>'+
                    '<textarea id="oc-comment-add-reply-textbox" class="oc-comment-textbox">Type Your Comment Here</textarea>'+
                '</div>'            
            );		    
		}

		$("#oc-comment-reply-form").slideDown(500);
		
		//submit comment btn click handler
		$("#oc-comment-add-submit").click(function(){
			submitCommentHandler();
		});
		
		// Handler keypress CTRL+enter to submit comment
    	$("#oc-comment-add-textbox").keyup(function (event){
	        if (event.ctrlKey === true){
	            if (event.keyCode == 13){
	                submitCommentHandler();
	            }
	        }
    	});    	
    }

    /**
     * @memberOf Opencast.Annotation_Comment
     * @description deletes comment
     */
    function deleteComment(commentID, type)
    {
    	var del_local = function(cID, t){
			if(type === "reply"){
				//Remove from local reply map
				reply_map.removeReplyByID(commentID);
				//Hide Comment and remove it from the DOM
            	$("#oc-comment-info-scComment"+commentID).slideUp(500,function(){
            		$("#oc-comment-info-scComment"+commentID).remove();
            	});
			}else if(type === "scrubber"){
				//Remove from  local reply map
				reply_map.removeReplysByCID(commentID);
				//TODO Check weather comment is the last in this balloon
				//Remove comment info from DOM, hide Comment balloon, remove comment point from scrubber
            	$("#oc-comment-info-scComment"+commentID).remove();
            	$("#scComment"+commentID).remove();
            	$(".oc-comment-exit").click();
            }   		
    	}
    	
        // ajax DELETE Request
        $.ajax(
        {
            type: 'DELETE',
            url: "../../annotation/"+commentID,
            complete: function ()
            {
            	    $.log("Comment DELETE Ajax call: Request success");
					del_local(commentID,type);
            },
            statusCode: {
                200: function() {
                    //$.log("Comment DELETE Ajax call: Request 200 success");
					//del_local(commentID,type);
   				}
            },
            statusCode: {
                404: function() {
                    //$.log("Comment DELETE Ajax call: Request success but Comment not found");
					//del_local(commentID,type);
                }
            }  
        });
    }    

    /**
     * @memberOf Opencast.annotation_comment
     * @description click event comment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function clickComment(commentTime)
    {
    	clickedOnComment = true;
		//show comment on timeline
		//showScrubberComment(commentId,commentValue,commentTime,userId);
        //seek player to comment
        Opencast.Watch.seekSegment(parseInt(commentTime));
        
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description clickSlideComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function clickSlideComment(commentId, commentValue, userId, slideNr)
    {
        //hide double click info
        $("#oc_dbclick-info").hide();
        
        clickedOnComment = true;
        var left = $("#" + commentId).offset().left + 8;
        var top = $("#" + commentId).offset().top - 100;
        $("#comment-Info").css("left", left+"px");
        $("#comment-Info").css("top", top+"px");
        $("#comment-Info").show();
        $("#cm-add-box").hide();
        $("#cm-info-box").show();
        $("#cm-info-hover").hide();
        var slNr = parseInt(slideNr) + 1;
        $("#oc-comment-info-header-text").html(userId + " at slide "+slNr);
        $("#oc-comment-info-textbox").html(commentValue);
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverComment(commentId, commentValue, commentTime, userId)
    {
        if(clickedOnHoverBar === false & clickedOnComment === false){
        	clickedOnHoverBar = true;
        	clickedOnComment = true;
    		$("#cm-info-box").hover(function(){
	    		//enter info box
	    		hoverInfoBox = true;
	    	},function(){
	    		//leave info box
	    		hoverInfoBox = false;
	    		window.setTimeout(function() {
			    	if(hoverInfoBox === false){
			    		clickedOnComment = false;
			    		//hideScrubberComment();		    		
		    		}
				}, 1000); 
	    	});
			//show comment on timeline
			showScrubberComment(commentId,commentValue,commentTime,userId);
	    }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverSlideComment
     * @param commentId id of the comment
     * @param commentValue comment value
     */
    function hoverSlideComment(commentId, commentValue, userId, slideNr)
    {
        //hide double click info
        $("#oc_dbclick-info").hide();
        
        if(clickedOnHoverBar === false){
            var left = $("#" + commentId).offset().left + 8;
            var top = $("#" + commentId).offset().top - 137;
            $("#comment-Info").css("left", left+"px");
            $("#comment-Info").css("top", top+"px");
            clickedOnHoverBar = true;
            $("#comment-Info").show();
            $("#cm-add-box").hide();
            $("#cm-info-box").show();
            $("#cm-info-hover").hide();
            var slNr = parseInt(slideNr) + 1;
            $("#oc-comment-info-header-text").html(userId + " at slide "+slNr);
            $("#oc-comment-info-textbox").html(commentValue);
            
        }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverOutSlideComment
     * @param commentId the id of the comment
     */
    function hoverOutSlideComment()
    {
    	if(clickedOnComment === false){
	        //show dblick info
	        $("#oc_dbclick-info").show();
	        
	        clickedOnHoverBar = false;
	        $("#comment-Info").hide();
	        $("#cm-info-hover").hide();
	        $("#cm-info-box").hide();
	        $("#oc-comment-add-textbox").val(defaul_comment_text);
	        $("#oc-comment-add-namebox").val(cm_username);
       }
    }
    
    /**
     * @memberOf Opencast.annotation_comment
     * @description hoverOutComment
     * @param commentId the id of the comment
     */
    function hoverOutComment()
    {		
		//start show timer 1sec
	    window.setTimeout(function() {
	    	if(hoverInfoBox === false){
	    		clickedOnComment = false;
	    		clickedOnHoverBar = false;
	    		hideScrubberComment(); 		
	    	}
		}, 1500); 
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Hide the Annotation
     */
    function hideAnnotation_Comment()
    {
    	//remove segment marks
    	$('div[id^="oc-comment-segmark_"]').remove();
        $("#oc-comment-scrubber-box").hide();
        $('canvas[id^="slideComment"]').hide();
        annotationCommentDisplayed = false;
    }

    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Toggle Analytics
     */
    function doToggleAnnotation_Comment()
    {
        if (!annotationCommentDisplayed)
        {
            showAnnotation_Comment();
        }
        else
        {
            hideAnnotation_Comment();
        }
        return true;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Set the mediaPackageId
     * @param String mediaPackageId
     */
    function setMediaPackageId(id)
    {
        mediaPackageId = id;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Set the duration
     * @param int duration
     */
    function setDuration(val)
    {
        duration = val;
    }
    
    /**
     * @memberOf Opencast.Annotation_Comment
     * @description Gets status of annotations are shown
     */
    function getAnnotationCommentDisplayed()
    {
        return annotationCommentDisplayed;
    }
    
    
    
    return {
        initialize: initialize,
        hideAnnotation_Comment: hideAnnotation_Comment,
        showAnnotation_Comment: showAnnotation_Comment,
        getAnnotationCommentDisplayed: getAnnotationCommentDisplayed,
        setUsername: setUsername,
        getUsername: getUsername,
        getDefaultUsername: getDefaultUsername,
        setDuration: setDuration,
        setMediaPackageId: setMediaPackageId,
        clickComment: clickComment,
        replyComment: replyComment,
        clickSlideComment: clickSlideComment,
        deleteComment: deleteComment,
        hoverComment: hoverComment,
        hoverOutComment: hoverOutComment,
        hoverSlideComment: hoverSlideComment,
        hoverOutSlideComment: hoverOutSlideComment,
        doToggleAnnotation_Comment: doToggleAnnotation_Comment
    };
}());
