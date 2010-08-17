package org.qing.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class FileTreeAction extends BaseActionInterface {
	private static MyFile codeFolder;
	private MyFile libFolder;
	private Boolean includeFiles;
	private MyFile folder;
	private String folderName;
	private int directoryId;
	private String path;
	private String tip;
	private List fileList;
	private Integer[] openDirectoryIds;
	
	private List libList;
		
	public Integer[] getOpenDirectoryIds() {
		return openDirectoryIds;
	}

	public void setOpenDirectoryIds(Integer[] openDirectoryIds) {
		this.openDirectoryIds = openDirectoryIds;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public MyFile getCodeFolder() {
		return codeFolder;
	}

	public void setCodeFolder(MyFile codeFolder) {
		this.codeFolder = codeFolder;
	}

	public MyFile getLibFolder() {
		return libFolder;
	}

	public void setLibFolder(MyFile libFolder) {
		this.libFolder = libFolder;
	}

	public Boolean isIncludeFiles() {
		return includeFiles;
	}

	public void setIncludeFiles(Boolean includeFiles) {
		this.includeFiles = includeFiles;
	}
	
	public MyFile getFolder() {
		return folder;
	}

	public void setFolder(MyFile folder) {
		this.folder = folder;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}	

	public List getFileList() {
		return fileList;
	}

	public String getTip() {
		return tip;
	}
	
	

	public int getDirectoryId() {
		return directoryId;
	}

	public void setDirectoryId(int directoryId) {
		this.directoryId = directoryId;
	}
	

	public List getLibList() {
		return libList;
	}

	public void setLibList(List libList) {
		this.libList = libList;
	}

	@Override
	public String execute() throws Exception{
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		
		return SUCCESS;
	}

	public String folderList() throws Exception{
		if(folder == null){
			folder = fileMgr.getUserFolder();
		}
		if(includeFiles == null)
			includeFiles = true;
			
		fileList = fileMgr.getFolderListById(folder.getId(),includeFiles);		
		
		return SUCCESS;
	}
	
	public String addFolder() throws Exception{
		tip = null;
		libFolder = fileMgr.getUserLib();
		if(folder.getId().intValue() == libFolder.getId().intValue()){
			tip = "Failed: Can not add folder to My_Lib folder";
			return SUCCESS;
		}
		if(fileMgr.newFolder(folderName, folder.getId()) == false)
			tip = "Failed, Check file name to see whether it exists!";
		return SUCCESS;

	}
	
	public String getFolderPath() throws Exception{
		path = fileMgr.getRelativePathById(folder.getId());		
		return SUCCESS;
	}
	
	public String delFile() throws Exception{
		tip = null;
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		if(folder.getId().intValue() == codeFolder.getId().intValue() || 
				folder.getId().intValue() == libFolder.getId().intValue()){
			tip = "Failed: Can not delete the My_Class or My_Lib folder";
			return SUCCESS;
		}
		
		try{
			fileMgr.deleteFile(folder.getId());
		}
		catch(Exception e){
			e.printStackTrace();
			tip = "Failed, Exception Occurs!";
		}
			
		return SUCCESS;

	}
	
	public String renameFile() throws Exception{
		tip = null;
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		if(folder.getId().intValue() == codeFolder.getId().intValue() || 
				folder.getId().intValue() == libFolder.getId().intValue()){
			tip = "Failed: Can not rename the My_Class or My_Lib folder";
			return SUCCESS;
		}
		
		try{
			if(fileMgr.renameFile(folderName, folder.getId()) == false)
				tip = "Failed: can not rename!";
		}
		catch(Exception e){
			e.printStackTrace();
			tip = "Failed, Exception Occurs!";
		}
			
		return SUCCESS;

	}
	
	public String moveFile() throws Exception{
		tip = null;
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		if(folder.getId().intValue() == codeFolder.getId().intValue() || 
				folder.getId().intValue() == libFolder.getId().intValue()){
			tip = "Failed: Can not mvoe the My_Class or My_Lib folder";
			return SUCCESS;
		}
		
		try{
			if(fileMgr.moveto(folder.getId(), directoryId) == false)
				tip = "Failed: can not move file!";
		}
		catch(Exception e){
			e.printStackTrace();
			tip = "Failed, Exception Occurs!";
		}
			
		return SUCCESS;

	}
	
	public String updateTree() throws Exception{
		tip = null;
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		HashMap<Integer, Integer> openIds = new HashMap<Integer, Integer>();
		if(openDirectoryIds!=null){
			for( Integer id : openDirectoryIds){
				openIds.put(id, id);
			}
		}
		tip = "";
		
		if(includeFiles == null)
			includeFiles = false;
		
		List treeList = fileMgr.getFileTreeList(codeFolder.getId(), openIds, includeFiles);
		if(treeList.size() > 1){
			tip = generateTreeFromList(treeList, tip, openIds);
		}
		
		libFolder = fileMgr.getUserLib();
		if(openIds.get(libFolder.getId()) != null){
			libList = fileMgr.getFolderListById(libFolder.getId(), true);			
		}
		
		
		return SUCCESS;
	}
	
	private static String generateTreeFromList(List tree, String treeHTML, Map openIds){
		//get root node
		Object node = tree.get(0);
		MyFile closeNode, openNode, rootNode;		
		rootNode = (MyFile) node;
		
		if(rootNode.getId() != codeFolder.getId()){//if the first node is not the My_Class folder
			treeHTML += "<li " + (openIds.get(rootNode.getId())==null?"":"class='open'") + " id='" +
					rootNode.getId() + "'><span class='text'>" + rootNode.getFileName() + "</span>";
			
			if(rootNode.getIsDirectory())
				treeHTML += "<ul>";
		}
		
		
		for(int i = 1; i < tree.size(); i++){
			node = tree.get(i);
			if(node instanceof MyFile){
				closeNode = (MyFile) node;
				treeHTML += "<li id='" + closeNode.getId() + "'><span class='text'>" + closeNode.getFileName() 
				+ "</span>";
				
				if(closeNode.getIsDirectory())
					treeHTML += "<ul class='ajax'><li id='"+ (-closeNode.getId().intValue()) + 
						"'>{url:folderList.action?folder.id=" + closeNode.getId() + 
						"&includeFiles=false}</li></ul>";
				
				treeHTML += "</li>";
				
			}
			else
			{// if it is an open directory
				List expandTreeList = (List)node;
				treeHTML = generateTreeFromList(expandTreeList, treeHTML, openIds);
			}
			
		}
		
		if(rootNode.getId() != codeFolder.getId()){//if the first node is not the My_Class folder
			treeHTML += "</ul></li>";
		}
		
		return treeHTML;
	}
	
}
