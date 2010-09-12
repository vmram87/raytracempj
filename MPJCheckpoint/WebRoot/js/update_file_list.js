

function del_multiple_file_page(){
	if(select_file_map.size() == 0)
		dialog("Delete Files","text:Please first select the files or directories to be delete!","400px","200px","text");
	else
		dialog("Delete Files","iframe:delMultipleFilePage.action?folder.id=" + select_folder_id,"400px","200px","iframe");
	
}

function del_multiple_file(){
	$.post("delMultipleFile.action",{"selectFileIds":select_file_map.values()},
			function(responseText){		
				if(responseText.indexOf("Successed")!=-1){				
					open_view("fileList");
				}
				else{
					alert(responseText);
				}
				close_dialog();
			}
	);
}

function rename_file_for_list_page(){
	if(select_file_map.size() != 1)
		dialog("Rename File","text:Please select one and only one file to rename","400px","200px","text");
	else
		dialog("Rename File","iframe:renameFilePage.action?folder.id=" + select_file_map.values(),"400px","250px","iframe");
}

function rename_file_for_list(id, fileName){
	$.post("renameFile.action",{"folder.id":id, "folderName":fileName},
			function(responseText){		
				if(responseText.indexOf("Successed")!=-1){				
					open_view("fileList");
					select_file_map.clear();
				}
				else{
					alert(responseText);
				}
				close_dialog();
			}
	);
}


function move_multiple_file_page(){
	if(select_file_map == null || select_file_map.size() == 0 ){
		dialog("Move File","text:Please first select files to be moved!","400px","200px","text");
	}
	else
		dialog("Move File","iframe:moveMultipleFilePage.action","300px","360px","iframe");
}