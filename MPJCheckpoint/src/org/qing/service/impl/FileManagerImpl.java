package org.qing.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.qing.dao.FileDao;
import org.qing.object.MyFile;
import org.qing.service.FileManager;
import org.qing.util.PropertyUtil;
import org.qing.util.SystemConfig;

public class FileManagerImpl implements FileManager {
	private FileDao fileDao;
	private String userDirectory;
	private String configFilePath;
	private Map openIds;
	private boolean isIncludeFiles;
	
	

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public void setUserDirectory(String userDirectory) {
		this.userDirectory = userDirectory;
	}

	public void setFileDao(FileDao fileDao) {
		this.fileDao = fileDao;
	}
	
	

	@Override
	public MyFile getFileById(Integer id) throws Exception {
		return fileDao.get(id);
	}

	@Override
	public boolean deleteFile(int fileId) throws Exception {
		MyFile f = fileDao.get(fileId);
		MyFile parent = f.getParentDirectory();
		fileDao.delete(fileId);

		String path = getRelativePathById(parent.getId());
		path = userDirectory + path + f.getFileName();
		File file = new File(path);
		if (file.exists())
			file.delete();
		
		return true;
	}

	@Override
	public List getFilesByDirectory(int directoryId) throws Exception {
		return fileDao.getByDirectory(fileDao.get(directoryId));
	}

	@Override
	public boolean moveto(int fileId, int directoryId) throws Exception {
		MyFile srcf = fileDao.get(fileId);
		MyFile dstf = fileDao.get(directoryId);
		
		if(checkFileExist(dstf, srcf.getFileName()) == true)
			return false;

		File srcfile = new File(userDirectory
				+ getRelativePathById(srcf.getId()));
		File dir = new File(userDirectory + getRelativePathById(dstf.getId())
				+ srcf.getFileName());

		srcf.setParentDirectory(dstf);
		
		fileDao.saveOrUpdate(srcf);

		return srcfile.renameTo(dir);
	}

	private boolean checkFileExist(MyFile directory, String fileName) {
		Set childSet = directory.getFiles();
		MyFile f = null;
		for(Object child : childSet){
			f = (MyFile) child;
			if(f.getFileName().equals(fileName))
				return true;
		}
		return false;
	}

	@Override
	public boolean renameFile(String fileName, int fileId) throws Exception {
		MyFile srcf = fileDao.get(fileId);
		MyFile dstf = srcf.getParentDirectory();
		
		if(fileDao.getByFileNameAndParent(fileName, dstf) != null){
			return false;
		}

		File srcfile = new File(userDirectory
				+ getRelativePathById(srcf.getId()));
		File dir = new File(userDirectory + getRelativePathById(dstf.getId())
				+ fileName);

		srcf.setFileName(fileName);
		fileDao.saveOrUpdate(srcf);

		return srcfile.renameTo(dir);

		/*
		 * if(srcf.getIsDirectory() == true){
		 * FileUtils.copyDirectoryToDirectory(srcfile, dir);
		 * FileUtils.deleteDirectory(srcfile); } else{
		 * FileUtils.copyFileToDirectory(srcfile, dir); srcfile.delete(); }
		 * 
		 * 
		 * return true;
		 */
	}

	@Override
	public boolean uploadFile(File file, String tName, int parentDirectoryId) throws Exception {
		MyFile p = fileDao.get(parentDirectoryId);
	
		String destPathString = userDirectory+ getRelativePathById(parentDirectoryId)+tName;
		String fileType = tName.substring(tName.lastIndexOf(".") + 1,tName.length()).toLowerCase();
		
		System.out.println("Temp save path:"+file.getPath()+"\n"+"Save Path:"+destPathString);
		MyFile f=fileDao.getByFileNameAndParent(tName, p);
		if(f == null){
			MyFile newFile = new MyFile();
			newFile.setFileName(tName);
			newFile.setFilePath("");
			newFile.setIsDirectory(false);		
			newFile.setParentDirectory(p);
			newFile.setFileType(fileType);
			newFile.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			fileDao.save(newFile);
		}
		else{
			f.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			fileDao.saveOrUpdate(f);
		}
			
		FileUtils.copyFile(file, new File(destPathString));
		
		return true;
	}

	@Override
	public boolean uploadFiles(File[] files, String[] fileName,
			int parentDirectoryId) throws Exception {
		MyFile pf = fileDao.get(parentDirectoryId);
		String savePath = pf.getFilePath();
		for (int i = 0; i < files.length; i++) {
			MyFile f = new MyFile();
			f.setIsDirectory(false);
			f.setFileName(fileName[i]);
			f.setFilePath(savePath + File.separator + fileName[i]);
			f.setParentDirectory(pf);
			f.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			fileDao.save(f);

			FileOutputStream fos = new FileOutputStream(savePath
					+ File.separator + fileName[i]);
			FileInputStream fis = new FileInputStream(files[i]);

			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
		
		return true;

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
		if (myfile.getIsDirectory())
			path = path + "/";
		MyFile parent = myfile.getParentDirectory();
		while (parent != null) {
			path = parent.getFileName() + "/" + path;
			parent = parent.getParentDirectory();
		}
		return path;
	}

	@Override
	public boolean newFolder(String newFolderName, Integer parentFileId)
			throws Exception {
		MyFile newFolder = new MyFile();
		MyFile parentFolder = fileDao.get(parentFileId);
		if (fileDao.getByFileNameAndParent(newFolderName, parentFolder) != null) {
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
		return fileDao.getFolderListById(parent, includeFiles);
	}

	@Override
	public List getFileTreeList(Integer folder_id, Map openDirectoryIds,
			boolean includeFiles) throws Exception {
		List finalFileList = new ArrayList();
		MyFile root = fileDao.get(folder_id);
		MyFile temp = root;
		finalFileList.add(root);
		openIds = openDirectoryIds;
		isIncludeFiles = includeFiles;

		if (openDirectoryIds.get(temp.getId().intValue()) == null)
			return finalFileList;

		getCompleteList(finalFileList, root);
		return finalFileList;
	}

	private List getCompleteList(List fileList, MyFile root) throws Exception {
		List childList = fileDao.getFolderListById(root, isIncludeFiles);
		if (childList != null) {
			for (int i = 0; i < childList.size(); i++) {
				MyFile child = (MyFile) childList.get(i);
				if (openIds.get(child.getId()) == null)
					fileList.add(child);
				else {
					List expandList = new ArrayList();
					expandList.add(child);
					getCompleteList(expandList, child);
					fileList.add(expandList);
				}
			}
		}
		return childList;
	}

	@Override
	public InputStream getFileInputStreamById(Integer id) throws Exception {
		MyFile myFile = fileDao.get(id);
		if (myFile == null)
			return null;

		File file = new File(userDirectory
				+ getRelativePathById(myFile.getId()));
		return new FileInputStream(file);
	}

	@Override
	public String getDestPath(Integer id) throws Exception {
		return userDirectory + getRelativePathById(id);
	}

	@Override
	public void delMultipleFiles(Integer[] selectFileIds) throws Exception {
		for(int i=0; i < selectFileIds.length; i++){
			File file = new File(getDestPath(selectFileIds[i]));
			fileDao.delete(selectFileIds[i]);
			
			try{
				if(file.exists())
					file.delete();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public boolean moveMultipleFiles(Integer directoryId, Integer[] selectFileIds)
			throws Exception {
		for(int i = 0; i < selectFileIds.length; i++){
			if(moveto(selectFileIds[i], directoryId) == false)
				return false;
		}
		
		return true;
	}

	@Override
	public SystemConfig getConfig() throws Exception {
		SystemConfig config = new SystemConfig();
		config.setRunType(PropertyUtil.readValue(configFilePath, "runType"));
		config.setRunFile(PropertyUtil.readValue(configFilePath, "runFile"));
		config.setNproc(Integer.parseInt(PropertyUtil.readValue(configFilePath, "nproc")));
		ArrayList outputFile = new ArrayList();
		outputFile.add(PropertyUtil.readValue(configFilePath, "outputFile"));
		config.setOutputFile(outputFile);
		return config;
	}

	@Override
	public void saveConfig(SystemConfig config) throws Exception {
		PropertyUtil.writeProperties(configFilePath, "runType", config.getRunType());
		PropertyUtil.writeProperties(configFilePath, "runFile", config.getRunFile());
		PropertyUtil.writeProperties(configFilePath, "nproc", config.getNproc().toString());
		PropertyUtil.writeProperties(configFilePath, "outputFile", config.getOutputFile().get(0).toString());
		
	}
	
	

	
}
