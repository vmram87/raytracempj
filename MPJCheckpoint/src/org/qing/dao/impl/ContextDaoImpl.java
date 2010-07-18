package org.qing.dao.impl;

import java.util.List;

import org.qing.dao.ContextDao;
import org.qing.object.Context;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContextDaoImpl extends HibernateDaoSupport implements ContextDao {

	@Override
	public void delete(int id) {
		getHibernateTemplate().delete(get(id));

	}

	@Override
	public void delete(Context context) {
		getHibernateTemplate().delete(context);

	}

	@Override
	public Context get(int id) {
		return (Context)getHibernateTemplate().get(Context.class, id);
	}

	@Override
	public List<Context> getAllPrevContextsByVersion(int versionId) {
		return getHibernateTemplate().find("from Context c where c.versionId<?",versionId);
	}

	@Override
	public Context getContext(int rank, int processId, int versionId) {
		Object args[] = { rank, processId, versionId };
		List ul= getHibernateTemplate().find("from Context c where c.rank=? and c.processId=? and c.versionId=?",args);
		if( ul== null || ul.size() == 0)
			return null;
		else if(ul.size() == 1)
			return (Context)ul.get(0);
		else{
			System.out.println("Impossible");
			return null;
		}
			
	}

	@Override
	public List<Context> getContextsByVersion(int versionId) {
		return getHibernateTemplate().find("from Context c where c.versionId=?",versionId);
	}

	@Override
	public Integer getLatestVersionId() {
		List ul = getHibernateTemplate().find("select max(c.versionId) from Context c");
		if(ul == null || ul.size() == 0)
			return null;
		else 
			return (Integer)ul.get(0);
	}

	@Override
	public Integer getNextLatestVersionId(int versionId) {
		List ul = getHibernateTemplate().find("select max(c.versionId) from Context c where c.versionId<?", versionId);
		if(ul == null || ul.size() == 0)
			return null;
		else 
			return (Integer)ul.get(0);
	}

	@Override
	public void save(Context context) {
		getHibernateTemplate().save(context);
	}

	@Override
	public void saveOrUpdate(Context context) {
		getHibernateTemplate().saveOrUpdate(context);
	}

}
