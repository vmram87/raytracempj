package org.qing.action;

import java.util.List;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class FileTreeAction extends BaseActionInterface {
	private MyFile codeFolder;
	private MyFile libFolder;
	private boolean includeFiles;
	private MyFile folder;
	private String folderName;
	private int directoryId;
	private String path;
	private String tip;
	private List fileList;
		
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

	public boolean isIncludeFiles() {
		return includeFiles;
	}

	public void setIncludeFiles(boolean includeFiles) {
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

	@Override
	public String execute() throws Exception{
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		
		return SUCCESS;
	}

	public String folderList() throws Exception{
		fileList = fileMgr.getFolderListById(folder.getId(),includeFiles);		
		return SUCCESS;
	}
	
	public String addFolder() throws Exception{
		tip = null;
		if(fileMgr.newFolder(folderName, folder.getId())==false)
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
				tip = "Failed: can not rename!";
		}
		catch(Exception e){
			e.printStackTrace();
			tip = "Failed, Exception Occurs!";
		}
			
		return SUCCESS;

	}
}
