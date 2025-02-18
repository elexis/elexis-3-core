package ch.elexis.core.jpa.entities.listener;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.TypedQuery;

import ch.elexis.core.jpa.entities.LabOrder;
import ch.elexis.core.jpa.entities.entitymanager.ElexisEntityManagerServiceHolder;

public class LabOrderEntityListener {

	@PrePersist
	public void prePersist(LabOrder labOrder) {
		if (labOrder.getOrderid() == null) {
			labOrder.setOrderid(Integer.toString(findAndIncrementLabOrderId()));
		}
	}

	/**
	 * Finds the current lab order number, checks for uniqueness, retrieves it and
	 * increments by one
	 *
	 * @return
	 */
	private int findAndIncrementLabOrderId() {
		int ret = 0;
		EntityManager em = (EntityManager) ElexisEntityManagerServiceHolder.getEntityManager().getEntityManager(false);
		try {
			em.getTransaction().begin();
			LabOrder version = em.find(LabOrder.class, "VERSION");
			if (version == null) {
				version = new LabOrder();
				version.setId("VERSION");
				version.setOrderid("1");
				version.setDeleted(true);
				em.persist(version);
				ret = 1;
			} else {
				em.lock(version, LockModeType.PESSIMISTIC_WRITE);
				ret = Integer.parseInt(version.getOrderid());
				ret += 1;

				while (true) {
					TypedQuery<LabOrder> query = em.createNamedQuery("LabOrder.orderid", LabOrder.class);
					query.setParameter("orderid", Integer.toString(ret));
					List<LabOrder> results = query.getResultList();
					if (results.isEmpty()) {
						break;
					} else {
						ret += 1;
					}
				}
				version.setOrderid(Integer.toString(ret));
			}
			em.getTransaction().commit();
			return ret;
		} finally {
			ElexisEntityManagerServiceHolder.getEntityManager().closeEntityManager(em);
		}
	}
}
