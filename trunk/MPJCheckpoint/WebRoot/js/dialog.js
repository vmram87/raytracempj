// Lee dialog 1.0 http://www.xij.cn/blog/?p=68

var dialogFirst=true;
function dialog(title,content,width,height,cssName){
if(dialogFirst==true){
  var temp_float=new String;
  temp_float="<div id=\"floatBoxBg\" style=\"height:"+$(document).height()+"px;filter:alpha(opacity=0);opacity:0;\"></div>";
  temp_float+="<div id=\"boxBg\"></div><div id=\"floatBox\" class=\"floatBox\">";
  temp_float+="<div class=\"title\"><h4></h4><div class=\"close_span\"></div></div>";
  temp_float+="<div class=\"content\"></div>";
  temp_float+="</div>";
  $("body").append(temp_float);
  dialogFirst=false;
}

$("#floatBox .title .close_span").click(function(){
	
	
	$("#DivShim").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
	$("#boxBg").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
 	 $("#floatBoxBg").animate({opacity:"0"},"normal",function(){$(this).hide();});
 	 $("#floatBox").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
});

$("#floatBox .title h4").html(title);
contentType=content.substring(0,content.indexOf(":"));
content=content.substring(content.indexOf(":")+1,content.length);
switch(contentType){
  case "url":
  var content_array=content.split("?");
  $("#floatBox .content").ajaxStart(function(){
    $(this).html("loading...");
  });
  $.ajax({
    type:content_array[0],
    url:content_array[1],
    data:content_array[2],
	error:function(){
	  $("#floatBox .content").html("error...");
	},
    success:function(html){
      $("#floatBox .content").html(html);
    }
  });
  break;
  case "text":
  $("#floatBox .content").html(content);
  break;
  case "id":
  $("#floatBox .content").html($("#"+content+"").html());
  break;
  case "iframe":
  $("#floatBox .content").html("<iframe src=\""+content+"\" width=\"100%\" height=\""+(parseInt(height)-40)+"px"+"\" scrolling=\"no\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\"></iframe>");
}

$("#floatBox").attr("class","floatBox "+cssName);
$("#floatBox").css({left:(($(document).width())/2-(parseInt(width)/2))+"px",top:($(document).scrollTop()+30)+"px",width:width,height:height});
$("#floatBoxBg").animate({opacity:"0.3"},"normal",function(){$(this).show();});
$("#floatBox").animate({opacity:"1"},"normal",function(){$(this).show();}); 
$("#boxBg").css({left:(($(document).width())/2-(parseInt(width)/2))+"px",top:($(document).scrollTop()+30)+"px",width:width,height:height,zIndex:499});
$("#boxBg").animate({opacity:"1"},"normal",function(){$(this).show();});

var DivRef = document.getElementById('boxBg');
var IfrRef = document.getElementById('DivShim');
IfrRef.style.width = DivRef.style.width;
IfrRef.style.height = DivRef.style.height;
IfrRef.style.top = DivRef.style.top;
IfrRef.style.left = DivRef.style.left;
IfrRef.style.zIndex = DivRef.style.zIndex - 1;
$("#DivShim").animate({opacity:"1"},"normal",function(){$(this).show();});
}

function close_dialog(){
	$("#DivShim").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
	$("#boxBg").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
 	 $("#floatBoxBg").animate({opacity:"0"},"normal",function(){$(this).hide();});
 	 $("#floatBox").animate({opacity:"0"},"normal",function(){$(this).hide();}); 
}