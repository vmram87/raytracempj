package org.qing.action;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class FileListAction extends BaseActionInterface {
	private MyFile codeFolder;
	private MyFile libFolder;
	private boolean includeFiles;
	private MyFile folder;
	private String path;
	private Integer[] selectFileIds;
	private Integer directoryId;
	private String tip;
	
	
	
	public Integer getDirectoryId() {
		return directoryId;
	}

	public void setDirectoryId(Integer directoryId) {
		this.directoryId = directoryId;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

	public Integer[] getSelectFileIds() {
		return selectFileIds;
	}

	public void setSelectFileIds(Integer[] selectFileIds) {
		this.selectFileIds = selectFileIds;
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


	public String getFolderList() throws Exception{
		codeFolder = fileMgr.getUserFolder();
		libFolder = fileMgr.getUserLib();
		
		return SUCCESS;
	}
	
	
	
	public String getFolderPath() throws Exception{
		path = fileMgr.getRelativePathById(folder.getId());		
		return SUCCESS;
	}
	
	public String delMultipleFile() throws Exception{
		fileMgr.delMultipleFiles(selectFileIds);
		return SUCCESS;
	}
	
	public String moveMultipleFile() throws Exception{
		fileMgr.moveMultipleFiles(directoryId, selectFileIds);
		
		return SUCCESS;
	}
}
