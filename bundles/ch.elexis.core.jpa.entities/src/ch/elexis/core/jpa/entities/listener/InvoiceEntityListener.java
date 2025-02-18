package ch.elexis.core.jpa.entities.listener;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.TypedQuery;

import ch.elexis.core.jpa.entities.Config;
import ch.elexis.core.jpa.entities.Invoice;
import ch.elexis.core.jpa.entities.entitymanager.ElexisEntityManagerServiceHolder;

public class InvoiceEntityListener {

	@PrePersist
	public void prePersist(Invoice invoice) {
		if (invoice.getNumber() == null) {
			invoice.setNumber(Integer.toString(findAndIncrementInvoiceNumber()));
		}
	}

	/**
	 * Finds the current invoice number, checks for uniqueness, retrieves it and
	 * increments by one
	 *
	 * @return
	 */
	private int findAndIncrementInvoiceNumber() {
		int ret = 0;
		EntityManager em = (EntityManager) ElexisEntityManagerServiceHolder.getEntityManager().getEntityManager(false);
		try {
			em.getTransaction().begin();
			Config invoiceNr = em.find(Config.class, "RechnungsNr");
			if (invoiceNr == null) {
				Config invoiceNrConfig = new Config();
				invoiceNrConfig.setParam("RechnungsNr");
				invoiceNrConfig.setWert("1");
				em.persist(invoiceNrConfig);
				ret = 1;
			} else {
				em.lock(invoiceNr, LockModeType.PESSIMISTIC_WRITE);
				ret = Integer.parseInt(invoiceNr.getWert());
				ret += 1;

				while (true) {
					TypedQuery<Invoice> query = em.createNamedQuery("Invoice.number", Invoice.class);
					query.setParameter("number", Integer.toString(ret));
					List<Invoice> results = query.getResultList();
					if (results.isEmpty()) {
						break;
					} else {
						ret += 1;
					}
				}
				invoiceNr.setWert(Integer.toString(ret));
			}
			em.getTransaction().commit();
			return ret;
		} finally {
			ElexisEntityManagerServiceHolder.getEntityManager().closeEntityManager(em);
		}
	}
}
