package org.qing.dao.impl;

import java.io.File;
import java.util.List;

import org.qing.dao.FileDao;
import org.qing.object.MyFile;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class FileDaoImpl extends HibernateDaoSupport implements FileDao {

	@Override
	public void delete(int id) throws Exception {
		this.delete(this.get(id));
	}

	@Override
	public void delete(MyFile file) throws Exception {
		getHibernateTemplate().delete(file);

	}

	@Override
	public MyFile get(int id) throws Exception {
		return (MyFile) getHibernateTemplate().get(MyFile.class, id);
	}

	@Override
	public List<MyFile> getByDirectory(MyFile directory) throws Exception {
		return getHibernateTemplate().find("from MyFile f where f.parentDirectory=? order by f.isDirectory", directory);
	}

	@Override
	public MyFile getUserFolder() throws Exception {
		List ul = getHibernateTemplate().find("from MyFile f where f.fileName='My_Class' and f.parentDirectory is null");;
		if(ul != null && ul.size() == 1)
			return (MyFile)ul.get(0);
		else
			return null;
	}

	@Override
	public MyFile getUserLib() throws Exception {
		List ul = getHibernateTemplate().find("from MyFile f where f.fileName='My_Lib' and f.parentDirectory is null");;
		if(ul != null && ul.size() == 1)
			return (MyFile)ul.get(0);
		else
			return null;
	}

	@Override
	public void save(MyFile file) throws Exception {
		getHibernateTemplate().save(file);
	}

	@Override
	public void saveOrUpdate(MyFile file) throws Exception {
		getHibernateTemplate().saveOrUpdate(file);
	}

	@Override
	public List getFolderListById(MyFile parent, boolean includeFiles)
			throws Exception {
		List ul = null;
		if(includeFiles)
			ul = getHibernateTemplate().find("from MyFile f where f.parentDirectory=? order by f.isDirectory desc, f.fileName asc", parent);
		else
			ul = getHibernateTemplate().find("from MyFile f where f.parentDirectory=? and f.isDirectory=1 order by f.fileName asc", parent);
			
		if(ul!=null && ul.size()>0)
			return ul;
		else
			return null;
	}

	@Override
	public MyFile getByFileNameAndParent(String newFolderName,
			MyFile parentFolder) throws Exception {
		Object[] args = {newFolderName, parentFolder};
		List ul = getHibernateTemplate().find("from MyFile f where f.fileName=? and f.parentDirectory=?",args);
		if(ul != null && ul.size() == 1)
			return (MyFile)ul.get(0);
		else
			return null;
	}

	
}
