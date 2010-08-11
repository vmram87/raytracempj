package org.qing.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.qing.dao.FileDao;
import org.qing.object.MyFile;
import org.qing.service.FileManager;

public class FileManagerImpl implements FileManager {
	private FileDao fileDao;
	private String userDirectory;
	
	public void setUserDirectory(String userDirectory) {
		this.userDirectory = userDirectory;
	}

	public void setFileDao(FileDao fileDao) {
		this.fileDao = fileDao;
	}

	@Override
	public void deleteFile(int fileId) throws Exception {
		MyFile f = fileDao.get(fileId);
		MyFile parent = f.getParentDirectory();
		fileDao.delete(fileId);
		
		String path = getRelativePathById(parent.getId());
		path = userDirectory + path + f.getFileName();
		File file = new File(path);
		if(file.exists())
			file.delete();
	}

	@Override
	public List getFilesByDirectory(int directoryId) throws Exception {
		return fileDao.getByDirectory(fileDao.get(directoryId));
	}

	@Override
	public boolean moveto(int fileId, int directoryId) throws Exception {
		MyFile srcf = fileDao.get(fileId);
		MyFile dstf = fileDao.get(directoryId);
		
		File srcfile = new File(userDirectory + getRelativePathById(srcf.getId()));
		File dir = new File(userDirectory +  getRelativePathById(dstf.getId()) + srcf.getFileName());
		
		
		srcf.setParentDirectory(dstf);
		fileDao.saveOrUpdate(srcf);
		
		return srcfile.renameTo(dir);
	}

	@Override
	public boolean renameFile(String fileName, int fileId) throws Exception {		
		MyFile srcf = fileDao.get(fileId);
		MyFile dstf = srcf.getParentDirectory();
	
		File srcfile = new File(userDirectory + getRelativePathById(srcf.getId()));
		File dir = new File(userDirectory +  getRelativePathById(dstf.getId()) + fileName);
		
		srcf.setFileName(fileName);
		fileDao.saveOrUpdate(srcf);
		
		return srcfile.renameTo(dir);
		
		/*
		if(srcf.getIsDirectory() == true){
			FileUtils.copyDirectoryToDirectory(srcfile, dir);
			FileUtils.deleteDirectory(srcfile);
		}
		else{
			FileUtils.copyFileToDirectory(srcfile, dir);
			srcfile.delete();
		}
		
		
		return true;
		*/
	}

	@Override
	public void uploadFile(File file, int parentDirectoryId) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadFiles(File[] files,String[] fileName, int parentDirectoryId)
			throws Exception {
		MyFile pf = fileDao.get(parentDirectoryId);
		String savePath = pf.getFilePath();
		for(int i = 0; i < files.length; i++){
			MyFile f = new MyFile();
			f.setIsDirectory(false);
			f.setFileName(fileName[i]);
			f.setFilePath(savePath + File.separator + fileName[i]);
			f.setParentDirectory(pf);
			f.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			fileDao.save(f);
			
			FileOutputStream fos = new FileOutputStream(savePath + File.separator + fileName[i]);
			FileInputStream fis = new FileInputStream(files[i]);
			
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len = fis.read(buffer)) > 0){
				fos.write(buffer, 0, len);
			}
		}

	}

	@Override
	public MyFile getUserFolder() throws Exception {
		return fileDao.getUserFolder();
	}

	@Override
	public MyFile getUserLib() throws Exception {
		// TODO Auto-generated method stub
		return fileDao.getUserLib();
	}

	@Override
	public String getRelativePathById(Integer id) throws Exception {
		MyFile myfile = fileDao.get(id);
		String path = myfile.getFileName();
		if(myfile.getIsDirectory())
			path = path + "/";
		MyFile parent = myfile.getParentDirectory();
		while(parent != null){
			path = parent.getFileName() + "/" + path;
			parent = parent.getParentDirectory();
		}
		return path;
	}

	@Override
	public boolean newFolder(String newFolderName, Integer parentFileId) throws Exception {
		MyFile newFolder = new MyFile();
		MyFile parentFolder = fileDao.get(parentFileId);
		if(fileDao.getByFileNameAndParent(newFolderName, parentFolder) != null){
			return false;
		}
		
		newFolder.setFileName(newFolderName);
		newFolder.setIsDirectory(true);
		newFolder.setParentDirectory(parentFolder);
		newFolder.setFilePath("");
		fileDao.save(newFolder);
		
		
		String path = getRelativePathById(parentFileId);
		path = userDirectory + path + newFolderName;
		boolean success = (new File(path)).mkdir();
		
		return success;
	}

	@Override
	public List getFolderListById(Integer id, boolean includeFiles)
			throws Exception {
		MyFile parent = fileDao.get(id);
		return fileDao.getFolderListById(parent,includeFiles);
	}
	
	
	
	

}
