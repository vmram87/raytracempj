package org.qing.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.List;

import org.qing.dao.FileDao;
import org.qing.object.MyFile;
import org.qing.service.FileManager;

public class FileManagerImpl implements FileManager {
	private FileDao fileDao;
	
	

	public void setFileDao(FileDao fileDao) {
		this.fileDao = fileDao;
	}

	@Override
	public void deleteFile(int fileId) throws Exception {
		MyFile f = fileDao.get(fileId);
		fileDao.delete(fileId);
		File file = new File(f.getFilePath());
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
		
		File srcfile = new File(srcf.getFilePath());
		File dir = new File(dstf.getFilePath() + File.separator + srcf.getFileName());
		
		boolean success = srcfile.renameTo(new File(dir, srcfile.getName()));
		return success;
	}

	@Override
	public boolean renameFile(String fileName, int fileId) throws Exception {
		MyFile srcf = fileDao.get(fileId);
		MyFile dstf = srcf.getParentDirectory();
		
		File srcfile = new File(srcf.getFilePath());
		File dir = new File(dstf.getFilePath() + File.separator + fileName);
		
		boolean success = srcfile.renameTo(new File(dir, srcfile.getName()));
		return success;
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
			f.setDirectory(false);
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
	
	

}
