package ch.elexis.core.jpa.entities.listener;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.TypedQuery;

import ch.elexis.core.jpa.entities.Config;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.entitymanager.ElexisEntityManagerServiceHolder;

public class KontaktEntityListener {

	@PrePersist
	public void prePersist(Kontakt contact) {
		if (contact.isPatient() && contact.getCode() == null) {
			contact.setCode(Integer.toString(findAndIncrementPatientNr()));
		}
	}

	@PreUpdate
	public void preUpdate(Kontakt contact) {
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
	private int findAndIncrementPatientNr() {
		int ret = 0;
		EntityManager em = (EntityManager) ElexisEntityManagerServiceHolder.getEntityManager().getEntityManager(false);
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
					TypedQuery<Kontakt> query = em.createNamedQuery("Kontakt.code", Kontakt.class);
					query.setParameter("code", Integer.toString(ret));
					List<Kontakt> results = query.getResultList();
					if (results.isEmpty()) {
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
			ElexisEntityManagerServiceHolder.getEntityManager().closeEntityManager(em);
		}
	}
}
