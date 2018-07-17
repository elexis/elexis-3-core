package ch.elexis.core.jpa.entities.listener;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PrePersist;
import javax.persistence.TypedQuery;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.jpa.entities.LabOrder;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;

public class LabOrderEntityListener {

	private ElexisEntityManger entityManager;
	
	@PrePersist
	public void prePersist(LabOrder labOrder){
		if (labOrder.getOrderid() == null) {
			labOrder.setOrderid(Integer.toString(findAndIncrementLabOrderId()));
		}
	}

	/**
	 * Finds the current lab order number, checks for uniqueness, retrieves it and increments by one
	 * 
	 * @return
	 */
	private int findAndIncrementLabOrderId(){
		int ret = 0;
		EntityManager em = getEntityManager();
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
					TypedQuery<LabOrder> query =
						em.createNamedQuery("LabOrder.getByOrderId", LabOrder.class);
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
