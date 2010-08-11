package org.qing.dao;

import java.io.File;
import java.util.List;

import org.qing.object.MyFile;

public interface FileDao {
	MyFile get(int id) throws Exception;
	void save(MyFile file) throws Exception;	
	void saveOrUpdate(MyFile file) throws Exception;
	void delete(int id) throws Exception;
	void delete(MyFile file) throws Exception;
	
	List<MyFile> getByDirectory(MyFile directory) throws Exception;
	public MyFile getUserFolder() throws Exception;
	public MyFile getUserLib() throws Exception;
	List getFolderListById(MyFile parent, boolean includeFiles)throws Exception;
	MyFile getByFileNameAndParent(String newFolderName, MyFile parentFolder) throws Exception;
}
