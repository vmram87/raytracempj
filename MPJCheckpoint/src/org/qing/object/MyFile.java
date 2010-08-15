package org.qing.object;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class MyFile implements Serializable {
	private Integer id;
	private String fileName;
	private String filePath;
	private String fileType;
	private boolean isDirectory;
	private Timestamp updateTime;
	
	private MyFile parentDirectory;
	private Set files = new HashSet();
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public boolean getIsDirectory() {
		return isDirectory;
	}
	public void setIsDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public MyFile getParentDirectory() {
		return parentDirectory;
	}
	public void setParentDirectory(MyFile parentDirectory) {
		this.parentDirectory = parentDirectory;
	}
	public Set getFiles() {
		return files;
	}
	public void setFiles(Set files) {
		this.files = files;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	
}
