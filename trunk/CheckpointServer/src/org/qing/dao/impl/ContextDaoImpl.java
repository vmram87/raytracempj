package org.qing.dao.impl;

import java.util.List;

import org.qing.dao.ContextDao;
import org.qing.object.Context;

public class ContextDaoImpl implements ContextDao {

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
	public int getLastestVersionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNextLastestVersionId(int versionId) {
		// TODO Auto-generated method stub
		return 0;
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
