package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;

public class MsoDao {
	
	public Mso save(Mso mso) {
		if (mso == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(mso);
		mso = pm.detachCopy(mso);
		pm.close();		
		return mso;
	}
	
	public Mso findByName(String name) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Mso.class);
		name = name.toLowerCase();
		query.setFilter("nameSearch == '" + name + "'");
		@SuppressWarnings("unchecked")
		List<Mso> results = (List<Mso>) query.execute(name);
		Mso detached = null; 
		if (results.size() > 0) {
			detached = pm.detachCopy(results.get(0));
		}
		pm.close();	
		return detached;
	}
	
	public List<Mso> findByType(short type) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Mso.class);
		query.setFilter("type == " + type);
		@SuppressWarnings("unchecked")
		List<Mso> results = (List<Mso>) query.execute(type);
		results = (List<Mso>)pm.detachCopyAll(results);
		pm.close();
		return results;
	}
	
}
