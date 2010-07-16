package org.qing.dao;


import java.util.List;

import org.qing.object.Context;

public interface ContextDao {

	Context get(int id);
	void save(Context context);	
	void saveOrUpdate(Context context);
	void delete(int id);
	void delete(Context context);
	
	Integer getLatestVersionId();
	Integer getNextLatestVersionId(int versionId);
	Context getContext(int rank, int processId, int versionId);
	List<Context> getContextsByVersion(int versionId);
	List<Context> getAllPrevContextsByVersion(int versionId);
	
}
