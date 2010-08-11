package org.qing.action;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class FileListAction extends BaseActionInterface {
	private MyFile codeFolder;
	private MyFile libFolder;
	private boolean includeFiles;
	private MyFile folder;
	private String path;
		
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
}
