package com.nnvmso.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Captcha;

public class CaptchaDao extends GenericDao<Captcha> {
	protected static final Logger logger = Logger.getLogger(CaptchaDao.class.getName());
	
	public CaptchaDao() {
		super(Captcha.class);
	}
	
	public void saveAll(List<Captcha> list) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistentAll(list);
		} finally {
			pm.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Captcha getRandom() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Captcha c = null;
		double random = Math.random();
		try {
			Query q = pm.newQuery(Captcha.class);
			q.setFilter("random > randomParam");
			q.declareParameters("double randomParam");
			q.setRange(1, 2);
			q.setOrdering("random");
			List<Captcha> list = (List<Captcha>) q.execute(random);			
			if (list.size() == 0) {
				q.setFilter("random < randomParam");
				q.setRange(1, 2);
				q.setOrdering("random");
				q.declareParameters("String randomParam");
				list = (List<Captcha>) q.execute(random);
			}
			if (list.size() > 0)
				c = pm.detachCopy(list.get(0));			
		} finally {
			pm.close();
		}
		return c;
	}
	
}
