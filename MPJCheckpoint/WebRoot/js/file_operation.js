var select_folder_id = null;
var delete_file_id = null;
var moveto_file_id = null;
//click new folder
function create_new_folder(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("New Folder","text:Please first select the destination folder in the left bottom corner!","400px","200px","text");
	else
		dialog("New Folder","iframe:newFolder.action?folder.id=" + select_folder_id,"400px","250px","iframe");
}

function addFolder(parent_id){
	window.frames["fileFrame"].addFolder(parent_id);
	close_dialog();
}

function delete_file(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("Delete File","text:Please first select the destination folder or file in the left bottom corner!","400px","200px","text");
	else
		dialog("Delete File","iframe:delFilePage.action?folder.id=" + select_folder_id,"400px","250px","iframe");
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

function rename_file_for_tree_page(){
	if(select_folder_id == null || select_folder_id == undefined)
		dialog("Rename File","text:Please first select the destination folder or file in the left bottom corner!","400px","200px","text");
	else
		dialog("Rename File","iframe:renameFilePage.action?folder.id=" + select_folder_id,"400px","250px","iframe");
}

function rename_file_for_tree(id, fileName){
	window.frames["fileFrame"].rename_file(id, fileName);
	open_view("fileList");
	close_dialog();
}

function click_moveto_node(){
}


//for select area
var isSelectBtnDown = false
function selectOptionButtonDown(b){
	if(isSelectBtnDown == false){
		isSelectBtnDown = true;
		if(b.hasChildNodes()){
			var x = b.childNodes;
			for(i=0;i<x.length;i++){
				if(x[i].className == "button_inner_border"){
					x[i].style.backgroundPosition="left -19px";				
				}
				
			}
			
		}

		$("#select_option_area").show();
	}
	else{
		isSelectBtnDown = false;
		if(b.hasChildNodes()){
			var x = b.childNodes;
			for(i=0;i<x.length;i++){
				if(x[i].className == "button_inner_border"){
					x[i].style.backgroundPosition="left top";
				}
			}
		}
		
		$("#select_option_area").hide();
	}
}

function select_all(){
	window.frames["listFrame"].select_all();
	if(isSelectBtnDown == true){
		selectOptionButtonDown(document.getElementById("select_option_button_item"));
	}
	$("#select_option_area").hide();
}

function select_none(){
	window.frames["listFrame"].select_none();
	if(isSelectBtnDown == true){
		selectOptionButtonDown(document.getElementById("select_option_button_item"));
	}
	$("#select_option_area").hide();
}

