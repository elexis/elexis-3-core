package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Component
public class StockService implements IStockService {

	private static final String PAT_STOCK_PREFIX = "P";

	private static Logger log = LoggerFactory.getLogger(StockService.class);

	@Inject
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	@Reference
	IConfigService configService;

	@Inject
	@Reference
	IStoreToStringService storeToStringService;

	@Override
	public Long getCumulatedStockForArticle(IArticle article) {
		INamedQuery<Long> query = PortableServiceLoader.getCoreModelService().getNamedQueryByName(Long.class,
				IStockEntry.class, "StockEntry_SumCurrentStock.articleId.articleType");
		Optional<String> storeToString = PortableServiceLoader.get(IStoreToStringService.class).storeToString(article);
		if (storeToString.isPresent()) {
			String[] parts = storeToString.get().split(IStoreToStringContribution.DOUBLECOLON);
			List<Long> results = query
					.executeWithParameters(query.getParameterMap("articleId", parts[1], "articleType", parts[0]));
			if (!results.isEmpty()) {
				return results.get(0);
			}
		}
		return null;
	}

	public void performSingleDisposal(IArticle article, int count) {
		Optional<IMandator> mandator = PortableServiceLoader.get(IContextService.class).getActiveMandator();
		performSingleDisposal(article, count, (mandator.isPresent()) ? mandator.get().getId() : null);
	}

	@Override
	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId) {
		if (count < 0) {
			throw new IllegalArgumentException();
		}
		if (article == null) {
			return new Status(Status.ERROR, "ch.elexis.core.services", "Article is null");
		}

		IStockEntry se = findPreferredStockEntryForArticle(StoreToStringServiceHolder.getStoreToString(article),
				mandatorId);
		if (se == null) {
			return new Status(Status.WARNING, "ch.elexis.core.services", "No stock entry for article found");
		}

		if (se.getStock().isCommissioningSystem()) {

			boolean suspendOutlay = configService.getLocal(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY,
					Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY_DEFAULT);
			if (suspendOutlay) {
				return Status.OK_STATUS;
			}

			int sellingUnit = article.getSellingSize();
			boolean isPartialUnitOutput = (sellingUnit > 0 && sellingUnit < article.getPackageSize());
			if (isPartialUnitOutput) {
				boolean performPartialOutlay = configService.get(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
						Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
				if (!performPartialOutlay) {
					return Status.OK_STATUS;
				}
			}

			Optional<IStockCommissioningSystemService> scs = PortableServiceLoader
					.getOptional(IStockCommissioningSystemService.class);
			if (scs.isPresent()) {
				return scs.get().performArticleOutlay(se, count, null);
			} else {
				return Status.error("Could not get IStockCommissioningSystemService to outlay " + article);
			}

		} else {
			LockResponse lr = PortableServiceLoader.get(ILocalLockService.class).acquireLockBlocking(se, 1,
					new NullProgressMonitor());
			if (lr.isOk()) {
				int fractionUnits = se.getFractionUnits();
				int ve = article.getSellingSize();
				int vk = article.getPackageSize();

				if (vk == 0) {
					if (ve != 0) {
						vk = ve;
					}
				}
				if (ve == 0) {
					if (vk != 0) {
						ve = vk;
					}
				}
				int num = count * ve;
				int cs = se.getCurrentStock();
				if (vk == ve) {
					se.setCurrentStock(cs - count);

				} else {
					int rest = fractionUnits - num;
					while (rest < 0) {
						rest = rest + vk;
						se.setCurrentStock(cs - 1);
					}
					se.setFractionUnits(rest);
				}
				coreModelService.save(se);
				PortableServiceLoader.get(ILocalLockService.class).releaseLock(se);
				return Status.OK_STATUS;
			}
		}

		return new Status(Status.WARNING, "ch.elexis.core.services", "Could not acquire lock");
	}

	@Override
	public IStatus performSingleReturn(IArticle article, int count, String mandatorId) {
		if (count < 0) {
			throw new IllegalArgumentException();
		}
		if (article == null) {
			return new Status(Status.ERROR, "ch.elexis.core.services", "Article is null");
		}

		IStockEntry se = findPreferredStockEntryForArticle(StoreToStringServiceHolder.getStoreToString(article),
				mandatorId);
		if (se == null) {
			return new Status(Status.WARNING, "ch.elexis.core.services", "No stock entry for article found");
		}

		if (se.getStock().isCommissioningSystem()) {
			// updates must happen via manual inputs in the machine
			return Status.OK_STATUS;
		}

		LockResponse lr = PortableServiceLoader.get(ILocalLockService.class).acquireLockBlocking(se, 1,
				new NullProgressMonitor());
		if (lr.isOk()) {
			int fractionUnits = se.getFractionUnits();
			int ve = article.getSellingSize();
			int vk = article.getPackageSize();

			if (vk == 0) {
				if (ve != 0) {
					vk = ve;
				}
			}
			if (ve == 0) {
				if (vk != 0) {
					ve = vk;
				}
			}
			int num = count * ve;
			int cs = se.getCurrentStock();
			if (vk == ve) {
				se.setCurrentStock(cs + count);
			} else {
				int rest = fractionUnits + num;
				while (rest > vk) {
					rest = rest - vk;
					se.setCurrentStock(cs + 1);
				}
				se.setFractionUnits(rest);
			}
			coreModelService.save(se);
			PortableServiceLoader.get(ILocalLockService.class).releaseLock(se);
			return Status.OK_STATUS;
		}
		return new Status(Status.WARNING, "ch.elexis.core.services", "Could not acquire lock");
	}

	private boolean isTriggerStockAvailabilityOnBelow() {
		int trigger = configService.get(ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER,
				ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
		return trigger == ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_BELOW;
	}

	@Override
	public Availability getCumulatedAvailabilityForArticle(IArticle article) {
		INamedQuery<Integer> query = null;
		if (isTriggerStockAvailabilityOnBelow()) {
			query = PortableServiceLoader.getCoreModelService().getNamedQueryByName(Integer.class, IStockEntry.class,
					"StockEntry_AvailableCurrentBelowStock.articleId.articleType");
		} else {
			query = PortableServiceLoader.getCoreModelService().getNamedQueryByName(Integer.class, IStockEntry.class,
					"StockEntry_AvailableCurrentStock.articleId.articleType");
		}
		Optional<String> storeToString = PortableServiceLoader.get(IStoreToStringService.class).storeToString(article);
		if (storeToString.isPresent()) {
			String[] parts = storeToString.get().split(IStoreToStringContribution.DOUBLECOLON);
			List<Integer> results = query
					.executeWithParameters(query.getParameterMap("articleId", parts[1], "articleType", parts[0]));
			if (!results.isEmpty()) {
				Integer value = results.get(0);
				if (value > 1) {
					return Availability.IN_STOCK;
				} else if (value == 1) {
					return Availability.CRITICAL_STOCK;
				}
				return Availability.OUT_OF_STOCK;
			}
		}
		return null;
	}

	@Override
	public List<IStockEntry> getAllStockEntries(boolean includePatientStockEntries) {
		IQuery<IStock> stockQuery = PortableServiceLoader.getCoreModelService().getQuery(IStock.class);
		stockQuery.and("ID", COMPARATOR.LIKE, "PatientStock-%");
		List<IStock> lStock = stockQuery.execute();

		IQuery<IStockEntry> query = PortableServiceLoader.getCoreModelService().getQuery(IStockEntry.class);
		if (!includePatientStockEntries) {
			for (IStock stock : lStock) {
				query.and(ModelPackage.Literals.ISTOCK_ENTRY__STOCK, COMPARATOR.NOT_EQUALS, stock);
			}
		}
		return query.execute();
	}

	@Override
	public IStockEntry findPreferredStockEntryForArticle(String storeToString, String mandatorId) {
		List<? extends IStockEntry> entries = findAllStockEntriesForArticle(storeToString);
		int val = Integer.MAX_VALUE;
		IStockEntry ret = null;
		for (IStockEntry iStockEntry : entries) {
			IStock stock = iStockEntry.getStock();
			Integer priority = stock.getPriority();
			if (priority < val) {
				val = priority;
				ret = iStockEntry;
			}
			if (mandatorId != null) {
				IPerson owner = stock.getOwner();
				if (owner != null && owner.getId().equals(mandatorId)) {
					return iStockEntry;
				}
			}
		}
		return ret;
	}

	private IArticle loadArticle(String article) {
		if (article == null) {
			log.warn("performSingleReturn for null article", new Throwable("Diagnosis"));
			return null;
		}
		Optional<Identifiable> loaded = PortableServiceLoader.get(IStoreToStringService.class).loadFromString(article);
		if (loaded.isPresent() && loaded.get() instanceof IArticle) {
			return (IArticle) loaded.get();
		}
		return null;
	}

	@Override
	public List<IStock> getAllStocks(boolean includeCommissioningSystems, boolean includePatientStocks) {
		IQuery<IStock> query = PortableServiceLoader.getCoreModelService().getQuery(IStock.class);
		if (!includeCommissioningSystems) {
			query.and("driverConfig", COMPARATOR.EQUALS, null);
		}
		if (!includePatientStocks) {
			query.and("id", COMPARATOR.NOT_LIKE, "PatientStock-%");
		}
		query.orderBy("PRIORITY", ORDER.ASC);
		return query.execute();
	}

	@Override
	public IStock getDefaultStock() {
		IQuery<IStock> query = PortableServiceLoader.getCoreModelService().getQuery(IStock.class, true, false);
		query.and(ModelPackage.Literals.ISTOCK__CODE, COMPARATOR.EQUALS, "STD");
		List<IStock> existing = query.execute();
		if (!existing.isEmpty()) {
			return existing.get(0);
		} else {
			IStock stock = PortableServiceLoader.getCoreModelService().create(IStock.class);
			stock.setCode("STD");
			PortableServiceLoader.getCoreModelService().save(stock);
			return stock;
		}
	}

	@Override
	public IStock getMandatorDefaultStock(String mandatorId) {
		IMandator mandator = coreModelService.load(mandatorId, IMandator.class).orElse(null);
		if (mandator != null) {
			IQuery<IStock> query = PortableServiceLoader.getCoreModelService().getQuery(IStock.class, true, false);
			query.and(ModelPackage.Literals.ISTOCK__OWNER, COMPARATOR.EQUALS, mandator);
			query.orderBy("PRIORITY", ORDER.DESC);
			List<IStock> result = query.execute();
			if (!result.isEmpty()) {
				return result.get(0);
			}
		}
		return getDefaultStock();
	}

	@Override
	public Availability getArticleAvailabilityForStock(IStock stock, String article) {
		IStockEntry se = findStockEntryForArticleInStock(stock, article);
		return determineAvailability(se.getCurrentStock(), se.getMinimumStock(), isTriggerStockAvailabilityOnBelow());
	}

	@Override
	public IStockEntry findStockEntryForArticleInStock(IStock stock, IArticle article) {
		String articleSts = storeToStringService.storeToString(article).orElse(null);
		return findStockEntryForArticleInStock(stock, articleSts);
	}

	@Override
	public IStockEntry findStockEntryForArticleInStock(IStock iStock, String storeToString) {
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		INamedQuery<IStockEntry> query = PortableServiceLoader.getCoreModelService().getNamedQuery(IStockEntry.class,
				"articleId", "articleType");
		List<IStockEntry> entries = query
				.executeWithParameters(query.getParameterMap("articleId", vals[1], "articleType", vals[0]));
		if (entries != null && !entries.isEmpty()) {
			for (IStockEntry iStockEntry : entries) {
				if (iStockEntry.getStock().equals(iStock)) {
					return iStockEntry;
				}
			}
		}
		return null;
	}

	@Override
	public IStockEntry storeArticleInStock(IStock stock, IArticle article) {
		String articleSts = storeToStringService.storeToString(article).orElse(null);
		return storeArticleInStock(stock, articleSts);
	}

	@Override
	public IStockEntry storeArticleInStock(IStock stock, String article) {
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			stockEntry.setCurrentStock(stockEntry.getCurrentStock() + 1);
		} else {
			IArticle loadArticle = loadArticle(article);
			if (loadArticle == null) {
				return null;
			}
			stockEntry = PortableServiceLoader.getCoreModelService().create(IStockEntry.class);
			stockEntry.setStock(stock);
			stockEntry.setCurrentStock(1);
			stockEntry.setArticle(loadArticle);

		}
		PortableServiceLoader.getCoreModelService().save(stockEntry);
		PortableServiceLoader.get(ILocalLockService.class).acquireLock(stockEntry);
		PortableServiceLoader.get(ILocalLockService.class).releaseLock(stockEntry);
		return stockEntry;
	}

	@Override
	public void unstoreArticleFromStock(IStock stock, String article) {
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			LockResponse lr = PortableServiceLoader.get(ILocalLockService.class).acquireLockBlocking(stockEntry, 1,
					new NullProgressMonitor());
			if (lr.isOk()) {
				PortableServiceLoader.getCoreModelService().remove(stockEntry);
				PortableServiceLoader.get(ILocalLockService.class).releaseLock((stockEntry));
			} else {
				log.warn("Could not unstore article [{}]", article);
			}
		}
	}

	@Override
	public List<IStockEntry> findAllStockEntriesForArticle(String storeToString) {
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		INamedQuery<IStockEntry> query = PortableServiceLoader.getCoreModelService().getNamedQuery(IStockEntry.class,
				"articleId", "articleType");
		return query.executeWithParameters(query.getParameterMap("articleId", vals[1], "articleType", vals[0]));
	}

	@Override
	public List<IStockEntry> findAllStockEntriesForStock(IStock stock) {
		IQuery<IStockEntry> query = PortableServiceLoader.getCoreModelService().getQuery(IStockEntry.class);
		query.and("stock", COMPARATOR.EQUALS, stock);
		return query.execute();
	}

	@Override
	public IStatus performSingleDisposal(String articleStoreToString, int count, String mandatorId) {
		Optional<Identifiable> article = PortableServiceLoader.get(IStoreToStringService.class)
				.loadFromString(articleStoreToString);
		if (article.isPresent()) {
			return performSingleDisposal((IArticle) article.get(), count, mandatorId);
		}
		return new Status(Status.WARNING, "ch.elexis.core.services", "No article found [" + articleStoreToString + "]");
	}

	@Override
	public IStatus performSingleReturn(String articleStoreToString, int count, String mandatorId) {
		Optional<Identifiable> article = PortableServiceLoader.get(IStoreToStringService.class)
				.loadFromString(articleStoreToString);
		if (article.isPresent()) {
			return performSingleReturn((IArticle) article.get(), count, mandatorId);
		}
		return new Status(Status.WARNING, "ch.elexis.core.services", "No article found [" + articleStoreToString + "]");
	}

	@Override
	public Optional<IStock> getPatientStock(IPatient patient) {
		if (patient == null) {
			return Optional.empty();
		}
		return coreModelService.load("PatientStock-" + patient.getPatientNr(), IStock.class);
	}

	@Override
	public void setEnablePatientStock(IPatient patient, boolean stockState) {
		IStock patientStock = getPatientStock(patient).orElse(null);
		if (stockState) {
			if (patientStock == null) {
				patientStock = coreModelService.create(IStock.class);
				patientStock.setId("PatientStock-" + patient.getPatientNr());
				patientStock.setPriority(0);
				patientStock.setCode(PAT_STOCK_PREFIX + patient.getPatientNr());
				patientStock.setDescription(patient.getDescription1() + " " + patient.getDescription2());
				patientStock.setOwner(patient);
				patientStock.setLocation("Patient");
				coreModelService.save(patientStock);
			}
		} else if (patientStock != null) {
			List<IStockEntry> entries = findAllStockEntriesForStock(patientStock);
			entries.forEach(coreModelService::remove);
			coreModelService.remove(patientStock);
		}
	}

	@Override
	public IStock getOrCreatePatientStock(IPatient patient) {
		return getPatientStock(patient).orElseGet(() -> {
			setEnablePatientStock(patient, true);
			return getPatientStock(patient).orElse(null);
		});
	}

	@Override
	public List<IStock> getAllPatientStock() {
		IQuery<IStock> query = coreModelService.getQuery(IStock.class);
		query.and("id", COMPARATOR.LIKE, "PatientStock-%");
		return query.execute();
	}

	@Override
	public void removePatientStock(IStock patientStock) {
		patientStock.getStockEntries().forEach(entry -> unstoreArticleFromStock(patientStock,
				storeToStringService.storeToString(entry.getArticle()).get()));
		coreModelService.remove(patientStock);
	}

}
