package org.qing.dao;


import java.util.List;

import org.qing.object.Context;

public interface ContextDao {

	Context get(int id) throws Exception;
	void save(Context context) throws Exception;	
	void saveOrUpdate(Context context) throws Exception;
	void delete(int id) throws Exception;
	void delete(Context context) throws Exception;
	
	Integer getLatestVersionId() throws Exception;
	Integer getNextLatestVersionId(int versionId) throws Exception;
	Context getContext(int rank, int processId, int versionId) throws Exception;
	List<Context> getContextsByVersion(int versionId) throws Exception;
	List<Context> getAllPrevContextsByVersion(int versionId) throws Exception;
	
}
