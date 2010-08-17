package org.qing.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.qing.object.MyFile;
import org.qing.util.SystemConfig;

public interface FileManager {
	public boolean uploadFile(File file, String tName, int parentDirectoryId) throws Exception;
	public boolean uploadFiles(File[] files,String[] fileName, int parentDirectoryId) throws Exception;
	public boolean deleteFile(int fileId) throws Exception;
	public boolean renameFile(String fileName, int fileId) throws Exception;
	public boolean moveto(int fileId, int directoryId) throws Exception;
	public List getFilesByDirectory(int directoryId) throws Exception;
	public MyFile getUserFolder() throws Exception;
	public MyFile getUserLib() throws Exception;
	public String getRelativePathById(Integer id) throws Exception;
	public boolean newFolder(String newFolderName, Integer parentFileId) throws Exception;
	public List getFolderListById(Integer id, boolean includeFiles) throws Exception;
	public List getFileTreeList(Integer id, Map openDirectoryIds, boolean includeFiles) throws Exception;
	public InputStream getFileInputStreamById(Integer id) throws Exception;
	public String getDestPath(Integer id) throws Exception;
	public MyFile getFileById(Integer id) throws Exception;
	public void delMultipleFiles(Integer[] selectFileIds)throws Exception;
	public boolean moveMultipleFiles(Integer directoryId, Integer[] selectFileIds)throws Exception;
	public SystemConfig getConfig()throws Exception;
	public void saveConfig(SystemConfig config)throws Exception;
}
