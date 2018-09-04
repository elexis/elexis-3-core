package ch.elexis.core.data.service.internal;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StockCommissioningServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.data.util.ConfigUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.elexis.data.StockEntry;

@Component
public class StockService implements IStockService {
	
	private static Logger log = LoggerFactory.getLogger(StockService.class);
	
	@Override
	public Long getCumulatedStockForArticle(IArticle article){
		INamedQuery<Long> query = CoreModelServiceHolder.get().getNamedQueryByName(Long.class,
			IStockEntry.class, "StockEntry_SumCurrentStock.articleId.articleType");
		Optional<String> storeToString = StoreToStringServiceHolder.get().storeToString(article);
		if (storeToString.isPresent()) {
			String[] parts = storeToString.get().split(IStoreToStringContribution.DOUBLECOLON);
			List<Long> results =
				query.executeWithParameters(CoreModelServiceHolder.get().getParameterMap(
					"articleId", parts[1], "articleType", parts[0]));
			if (!results.isEmpty()) {
				return results.get(0);
			}
		}
		return null;
	}
	
	public void performSingleDisposal(IArticle article, int count){
		Optional<IMandator> mandator =
			ContextServiceHolder.get().getRootContext().getActiveMandator();
		performSingleDisposal(article, count,
			(mandator.isPresent()) ? mandator.get().getId() : null);
	}
	
	@Override
	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId){
		if (article == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "Article is null");
		}
		
		IStockEntry se = findPreferredStockEntryForArticle(
			StoreToStringServiceHolder.getStoreToString(article), mandatorId);
		if (se == null) {
			return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
				"No stock entry for article found");
		}
		
		if (se.getStock().isCommissioningSystem()) {
			int sellingUnit = article.getSellingSize();
			boolean isPartialUnitOutput =
				(sellingUnit > 0 && sellingUnit < article.getPackageSize());
			if (isPartialUnitOutput) {
				boolean performPartialOutlay =
					ConfigUtil.isGlobalConfig(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
						Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
					if (!performPartialOutlay) {
						return Status.OK_STATUS;
					}
			}
			return StockCommissioningServiceHolder.get().performArticleOutlay(se, count, null);
		} else {
			LockResponse lr = CoreHub.getLocalLockService().acquireLockBlocking(se, 1,
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
				
				CoreHub.getLocalLockService().releaseLock(se);
				return Status.OK_STATUS;
			}
		}
		
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID, "Could not acquire lock");
	}
	
	@Override
	public IStatus performSingleReturn(IArticle article, int count, String mandatorId){
		if (article == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "Article is null");
		}
		
		IStockEntry se =
			findPreferredStockEntryForArticle(StoreToStringServiceHolder.getStoreToString(article),
				null);
		if (se == null) {
			return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
				"No stock entry for article found");
		}
		
		if (se.getStock().isCommissioningSystem()) {
			// updates must happen via manual inputs in the machine
			return Status.OK_STATUS;
		}
		
		LockResponse lr = CoreHub.getLocalLockService().acquireLockBlocking(se, 1,
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
			CoreHub.getLocalLockService().releaseLock(se);
			return Status.OK_STATUS;
		}
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID, "Could not acquire lock");
	}
	
	private static boolean isTriggerStockAvailabilityOnBelow(){
		int trigger =
			ConfigUtil.getGlobalConfig(ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER,
				ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
		return trigger == ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_BELOW;
	}
	
	@Override
	public Availability getCumulatedAvailabilityForArticle(IArticle article){
		INamedQuery<Long> query = null;
		if (isTriggerStockAvailabilityOnBelow()) {
			query = CoreModelServiceHolder.get().getNamedQueryByName(Long.class,
				IStockEntry.class, "StockEntry_AvailableCurrentBelowStock.articleId.articleType");
		} else {
			query = CoreModelServiceHolder.get().getNamedQueryByName(Long.class,
				IStockEntry.class, "StockEntry_AvailableCurrentStock.articleId.articleType");
		}
		List<Long> results = query.executeWithParameters(CoreModelServiceHolder.get()
			.getParameterMap("articleId", article.getId(), "articleType",
				article.getCodeSystemName()));
		if (!results.isEmpty()) {
			Long value = results.get(0);
			if (value > 1) {
				return Availability.IN_STOCK;
			} else if (value == 1) {
				return Availability.CRITICAL_STOCK;
			}
			return Availability.OUT_OF_STOCK;
		}
		return null;
	}
	
	public List<IStockEntry> getAllStockEntries(){
		return CoreModelServiceHolder.get().getQuery(IStockEntry.class).execute();
	}
	
	@Override
	public IStockEntry findPreferredStockEntryForArticle(String storeToString, String mandatorId){
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
				IMandator owner = stock.getOwner();
				if (owner != null && owner.getId().equals(mandatorId)) {
					return iStockEntry;
				}
			}
		}
		return ret;
	}
	
	private IArticle loadArticle(String article){
		if (article == null) {
			log.warn("performSingleReturn for null article", new Throwable("Diagnosis"));
			return null;
		}
		Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(article);
		if (loaded.isPresent() && loaded.get() instanceof IArticle) {
			return (IArticle) loaded.get();
		}
		return null;
	}
	
	public List<IStock> getAllStocks(boolean includeCommissioningSystems){
		IQuery<IStock> query = CoreModelServiceHolder.get().getQuery(IStock.class);
		if(!includeCommissioningSystems) {
			query.and("DRIVER_UUID", COMPARATOR.EQUALS, null);
		}
		query.orderBy("PRIORITY", ORDER.ASC);
		return query.execute();
	}
	
	@Override
	public IStock getDefaultStock(){
		IQuery<IStock> query = CoreModelServiceHolder.get().getQuery(IStock.class, true, false);
		query.and(ModelPackage.Literals.ISTOCK__CODE, COMPARATOR.EQUALS, "STD");
		List<IStock> existing = query.execute();
		if(!existing.isEmpty()) {
			return existing.get(0);
		} else {
			IStock stock = CoreModelServiceHolder.get().create(IStock.class);
			stock.setCode("STD");
			CoreModelServiceHolder.get().save(stock);
			return stock;
		}
	}
	
	public Availability getArticleAvailabilityForStock(IStock stock, String article){
		IStockEntry se = findStockEntryForArticleInStock(stock, article);
		return determineAvailability(se.getCurrentStock(), se.getMinimumStock(),
			isTriggerStockAvailabilityOnBelow());
	}
	
	@Override
	public IStockEntry findStockEntryForArticleInStock(IStock iStock, String storeToString){
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		INamedQuery<IStockEntry> query = CoreModelServiceHolder.get()
			.getNamedQuery(IStockEntry.class,
			"articleId", "articleType");
		List<IStockEntry> entries = query.executeWithParameters(CoreModelServiceHolder.get()
			.getParameterMap("articleId", vals[1], "articleType", vals[0]));
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
	public IStockEntry storeArticleInStock(IStock stock, String article){
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			return stockEntry;
		}
		IArticle loadArticle = loadArticle(article);
		if (loadArticle == null) {
			return null;
		}
		IStockEntry entry = CoreModelServiceHolder.get().create(IStockEntry.class);
		entry.setStock(stock);
		entry.setArticle(loadArticle);
		CoreModelServiceHolder.get().save((Identifiable) entry);
		CoreHub.getLocalLockService().acquireLock(entry);
		CoreHub.getLocalLockService().releaseLock(entry);
		return entry;
	}
	
	@Override
	public void unstoreArticleFromStock(IStock stock, String article){
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			LockResponse lr = CoreHub.getLocalLockService()
				.acquireLockBlocking(stockEntry, 1, new NullProgressMonitor());
			if (lr.isOk()) {
				CoreModelServiceHolder.get().delete(stockEntry);
				CoreHub.getLocalLockService().releaseLock(((StockEntry) stockEntry));
			} else {
				log.warn("Could not unstore article [{}]", article);
			}
		}
	}
	
	@Override
	public List<? extends IStockEntry> findAllStockEntriesForArticle(String storeToString){
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		INamedQuery<IStockEntry> query = CoreModelServiceHolder.get()
			.getNamedQuery(IStockEntry.class, "articleId", "articleType");
		return query.executeWithParameters(CoreModelServiceHolder.get().getParameterMap("articleId",
			vals[1], "articleType", vals[0]));
	}
	
	@Override
	public List<? extends IStockEntry> findAllStockEntriesForStock(IStock stock){
		IQuery<IStockEntry> query = CoreModelServiceHolder.get().getQuery(IStockEntry.class);
		query.and("stock", COMPARATOR.EQUALS, stock);
		return query.execute();
	}
	
	@Override
	public IStatus performSingleDisposal(String articleStoreToString, int count, String mandatorId){
		Optional<Identifiable> article =
			StoreToStringServiceHolder.get().loadFromString(articleStoreToString);
		if (article.isPresent()) {
			return performSingleDisposal((IArticle) article.get(), count, mandatorId);
		}
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
			"No article found [" + articleStoreToString + "]");
	}
	
	@Override
	public IStatus performSingleReturn(String articleStoreToString, int count, String mandatorId){
		Optional<Identifiable> article =
			StoreToStringServiceHolder.get().loadFromString(articleStoreToString);
		if (article.isPresent()) {
			return performSingleReturn((IArticle) article.get(), count, mandatorId);
		}
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
			"No article found [" + articleStoreToString + "]");
	}
}
