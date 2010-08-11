var xmlrequest;

function createXMLHttpRequest()
{
	if(window.XMLHttpRequest)
	{
		xmlrequest=new XMLHttpRequest();
	}
	else if(window.ActiveXObject)
	{
		try
		{
			xmlrequest=new ActiveXObject("Msxml2.XMLHTTP");
		}catch(e)
		{
			try{
				xmlrequest=new ActiveXObject("Microsoft.XMLHTTP");
			}catch(e){
				
			}
		}
	}
}

createXMLHttpRequest();