package org.qing.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.qing.dao.ContextDao;
import org.qing.object.Context;

public class ContextDaoImpl implements ContextDao {
	SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
	
	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		session.delete(this.get(id));
		  
		t.commit();
		session.close();

	}

	@Override
	public void delete(Context context) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		session.delete(context);
		  
		t.commit();
		session.close();

	}

	@Override
	public Context get(int id) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		Context c = (Context)session.get(Context.class, id);
		  
		t.commit();
		session.close();
		return c;
	}

	@Override
	public Context getContext(int rank, int processId, int versionId) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		Context c = (Context)session.createQuery("from Context c where c.rank=? " +
				"and c.processId=? and c.versionId=?")
			.setInteger(0, rank).setInteger(1, processId)
			.setInteger(2, versionId).uniqueResult();
		  
		t.commit();
		session.close();
		return c;
	}

	@Override
	public List<Context> getContextsByVersion(int versionId) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		List l = session.createQuery("from Context c where c.versionId=?")
			.setInteger(0, versionId).list();
		  
		t.commit();
		session.close();
		return l;
	}
	
	@Override
	public List<Context> getAllPrevContextsByVersion(int versionId) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		List l = session.createQuery("from Context c where c.versionId<?")
			.setInteger(0, versionId).list();
		  
		t.commit();
		session.close();
		return l;
	}
	
	@Override
	public void delAllPrevContextsByVersion(int versionId) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		
		List<Context> l = session.createQuery("from Context c where c.versionId<?")
			.setInteger(0, versionId).list();
		
		for(int i = 0; i < l.size(); i++){
			session.delete(l.get(i));
		}
		  
		t.commit();
		session.close();

	}

	@Override
	public Integer getLatestVersionId() {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		Integer v = (Integer)session.createQuery("select max(c.versionId) from Context c")
			.uniqueResult();
		  
		t.commit();
		session.close();
		return v;
	}

	@Override
	public Integer getNextLatestVersionId(int versionId) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		Integer v = (Integer)session.createQuery("select max(c.versionId) from Context c" +
				" where c.versionId < ?").setInteger(0, versionId).uniqueResult();
		  
		t.commit();
		session.close();
		return v;
	}

	@Override
	public void save(Context context) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		session.save(context);
		  
		t.commit();
		session.close();

	}

	@Override
	public void saveOrUpdate(Context context) {
		Session session = sessionFactory.openSession();
		Transaction t =session.beginTransaction();
		  
		session.saveOrUpdate(context);
		  
		t.commit();
		session.close();
	}

}
