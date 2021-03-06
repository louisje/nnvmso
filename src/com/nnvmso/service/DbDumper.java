package com.nnvmso.service;

import java.util.Collection;
import java.util.List;
import javax.jdo.PersistenceManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;

@Service
public class DbDumper {
		
	public void save(Object o) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(o);
		pm.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void save(Collection objs) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistentAll(objs);
		pm.close();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object findByKey(Class c, Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Object o = pm.getObjectById(c, key);
		pm.close();
		return o;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List findAll(Class c, String order) {
		  PersistenceManager pm = PMF.get().getPersistenceManager();
		  //String query = "select from " + c.getName() + " order by "+ order;
		  String query = "select from " + c.getName();
		  List list = (List) pm.newQuery(query).execute();
		  List detached = (List) pm.detachCopyAll(list);
		  pm.close();
		  return detached;	
	}
	
	@SuppressWarnings({ "rawtypes"})
	public void deleteAll(Class c, Collection list) {
	    PersistenceManager pm = PMF.get().getPersistenceManager();
	    pm.deletePersistentAll(list);
	    pm.close();
	}
}

