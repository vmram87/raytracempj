package org.qing.service;

import java.io.File;
import java.util.List;

import org.qing.object.MyFile;

public interface FileManager {
	public void uploadFile(File file, int parentDirectoryId) throws Exception;
	public void uploadFiles(File[] files,String[] fileName, int parentDirectoryId) throws Exception;
	public void deleteFile(int fileId) throws Exception;
	public boolean renameFile(String fileName, int fileId) throws Exception;
	public boolean moveto(int fileId, int directoryId) throws Exception;
	public List getFilesByDirectory(int directoryId) throws Exception;
	public MyFile getUserFolder() throws Exception;
	public MyFile getUserLib() throws Exception;
}
