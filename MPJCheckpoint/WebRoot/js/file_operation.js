var select_folder_id = null;
var delete_file_id = null;
//click new folder
function create_new_folder(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("New Folder","text:Please first select the destination folder in the left bottom corner!","400px","200px","text");
	else
		dialog("New Folder","iframe:newFolder.action?folder.id=" + select_folder_id,"400px","200px","iframe");
}

function addFolder(parent_id){
	window.frames["fileFrame"].addFolder(parent_id);
	close_dialog();
}

function delete_file(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("Delete File","text:Please first select the destination folder or file in the left bottom corner!","400px","200px","text");
	else
		dialog("Delete File","iframe:delFilePage.action?folder.id=" + select_folder_id,"400px","200px","iframe");
}

function delFile(file_id){
	window.frames["fileFrame"].delFile(file_id);
	close_dialog();
}

function move_to_folder_page(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("Move File","text:Please first select the destination folder or file in the left bottom corner!","400px","200px","text");
	else
		dialog("Move File","iframe:moveFilePage.action?folder.id=" + select_folder_id,"300px","360px","iframe");
}

function move_file(file_id){
	window.frames["fileFrame"].moveToFolder();
	close_dialog();
}

function rename_file_page(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("Rename File","text:Please first select the destination folder or file in the left bottom corner!","400px","200px","text");
	else
		dialog("Rename File","iframe:renameFilePage.action?folder.id=" + select_folder_id,"400px","200px","iframe");
}

function rename_file(id, fileName){
	window.frames["fileFrame"].rename_file(id, fileName);
	close_dialog();
}

