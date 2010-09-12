function addFolder(parent_id){
	var parent = document.getElementById(parent_id);
	
	var childNodes = parent.childNodes;
	for (var i=0;i<childNodes.length;i++)
	{
		if(childNodes[i].nodeName == "UL" || childNodes[i].nodeName == "ul"){
			parent.removeChild(childNodes[i]);
			break;
		}
	}
	
	var ul = document.createElement("ul");
	ul.className = "ajax";
	var li = document.createElement("li");
	li.id = 0 - parent_id;
	li.innerHTML = "{url:folderList.action?folder.id="+parent_id+"}";
	
	ul.appendChild(li);
	parent.appendChild(ul);
	parent.className="folder-close";

	
	simpleTreeCollection[0].setAjaxNodes(ul, parent.id);

}

function delFile(file_id){	
	simpleTreeCollection[0].delNode();
}

function moveToFolder(){
	location.reload();
}

function rename_file(id, fileName){
	var p = document.getElementById(id);
	if(p != null && p != undefined){
		var childNodes = p.childNodes;
		for(var i = 0; i < childNodes.length; i++){
			if(childNodes[i].nodeName == "SPAN" || childNodes[i].nodeName == "span"){
				childNodes[i].innerHTML = fileName;
				break;
			}
		}
	}
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