function addFolder(folderName,parent_id){
	var parent = document.getElementById(parent_id);
	
	var li = document.createElement("li");
	li.id=parent_id;
	
	var span = document.createElement("span");
	span.className = "text";
	span.innerHTML = folderName;
	
	
	
	
	parent.insertBefore(newChild, parent.firstChild);
	close_dialog();
}

function get_nextSibling(n)
{
	if (n.nextSibling){
		y=n.nextSibling;
		while (y.nodeType!=1)
		{
			if(y.nextSibling)
		  		y=y.nextSibling;
			else
				return null;
		}
		return y;
	}
	else
		return null;
}