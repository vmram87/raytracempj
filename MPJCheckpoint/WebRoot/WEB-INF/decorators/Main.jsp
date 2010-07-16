<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><decorator:title default="Fault Tolerance Java Parralel Computing System"/></title>


<script type="text/javascript">


//截取中英字符長度
function cutstr(str,len)
{
   var str_length = 0;
   var str_len = 0;
      str_cut = new String();
      str_len = str.length;
      for(var i = 0;i<str_len;i++)
     {
        a = str.charAt(i);
        str_length++;
        if(escape(a).length > 4)
        {
         //中文字符的长度经编码之后大于4
         str_length++;
         }
         str_cut = str_cut.concat(a);

         if(str_length>=len)
         {
         str_cut = str_cut.concat("...");
         document.write(str_cut);
         return;
         }
    }
    //如果给定字符串小于指定长度，则返回源字符串；
    if(str_length<len){
    	document.write(str);
    }
}
</script>

<decorator:head/>
<style>


</style>

</head>

<body>


<div id="mainContentContainer_area">

	<decorator:body/>


</div><!-- mainContentContainer end -->


</body>
</html>
