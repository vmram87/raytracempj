package org.qing.dao.impl;

import java.util.List;

import org.qing.dao.ContextDao;
import org.qing.object.Context;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContextDaoImpl extends HibernateDaoSupport implements ContextDao {

	@Override
	public void delAllPrevContextsByVersion(int versionId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public Context get(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Context> getAllPrevContextsByVersion(int versionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext(int rank, int processId, int versionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Context> getContextsByVersion(int versionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getLatestVersionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getNextLatestVersionId(int versionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveOrUpdate(Context context) {
		// TODO Auto-generated method stub

	}

}
