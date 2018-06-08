package ch.elexis.core.jpa.entities.listener;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PrePersist;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.jpa.entities.Config;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;

public class KontaktEntityListener {

	private ElexisEntityManger entityManager;
	
	@PrePersist
	public void prePersist(Kontakt contact) {
		if (contact.isPatient() && contact.getCode() == null) {
			contact.setCode(Integer.toString(findAndIncrementPatientNr()));
		}
	}

	/**
	 * Finds the current patient number, checks for uniqueness, retrieves it and
	 * increments by one
	 * 
	 * @return
	 */
	private int findAndIncrementPatientNr(){
		int ret = 0;
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			Config patNr = em.find(Config.class, "PatientNummer");
			if (patNr == null) {
				Config patNrConfig = new Config();
				patNrConfig.setParam("PatientNummer");
				patNrConfig.setWert("1");
				em.persist(patNrConfig);
				ret = 1;
			} else {
				em.lock(patNr, LockModeType.PESSIMISTIC_WRITE);
				ret = Integer.parseInt(patNr.getWert());
				ret += 1;

				while (true) {
					@SuppressWarnings("rawtypes")
					List resultList = em.createQuery("SELECT k FROM Kontakt k WHERE k.code=" + ret).getResultList();
					if (resultList.size() == 0) {
						break;
					} else {
						ret += 1;
					}
				}
				patNr.setWert(Integer.toString(ret));
			}
			em.getTransaction().commit();
			return ret;
		} finally {
			em.close();
		}
	}
	
	private EntityManager getEntityManager(){
		if (entityManager == null) {
			// get ElexisEntityManger via osgi service reference
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			ServiceReference<ElexisEntityManger> ref =
				bundle.getBundleContext().getServiceReference(ElexisEntityManger.class);
			if (ref != null) {
				entityManager = bundle.getBundleContext().getService(ref);
			}
		}
		return entityManager.getEntityManager();
	}
}
