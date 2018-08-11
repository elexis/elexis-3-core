package ch.elexis.core.data.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.ConfigUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IStockService;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Stock;
import ch.elexis.data.StockEntry;


/**
 * Do not create an instance, use {@link CoreHub#getStockService()}.
 */
public class StockService implements IStockService {
	
	private static Logger log = LoggerFactory.getLogger(StockService.class);
	
	@Override
	public Integer getCumulatedStockForArticle(IArticle article){
		INamedQuery<Integer> query = CoreModelServiceHolder.get().getNamedQueryByName(Integer.class,
			IStockEntry.class, "StockEntry_SumCurrentStock.articleId.articleType");
		// TODO check if system name matches old article type (class name)
		List<Integer> results =
			query.executeWithParameters(CoreModelServiceHolder.get().getParameterMap("articleId",
				article.getId(), "articleType", article.getCodeSystemName()));
		if (!results.isEmpty()) {
			return results.get(0);
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
			
			return CoreHub.getStockCommissioningSystemService().performArticleOutlay(se, count,
				null);
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
	
	public void performSingleReturn(IArticle article, int count){
		Optional<IMandator> mandator =
			ContextServiceHolder.get().getRootContext().getActiveMandator();
		performSingleReturn(article, count, (mandator.isPresent()) ? mandator.get().getId() : null);
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
		INamedQuery<Integer> query = null;
		if (isTriggerStockAvailabilityOnBelow()) {
			query = CoreModelServiceHolder.get().getNamedQueryByName(Integer.class,
				IStockEntry.class, "StockEntry_AvailableCurrentBelowStock.articleId.articleType");
		} else {
			query = CoreModelServiceHolder.get().getNamedQueryByName(Integer.class,
				IStockEntry.class, "StockEntry_AvailableCurrentStock.articleId.articleType");
		}
		List<Integer> results = query.executeWithParameters(CoreModelServiceHolder.get()
			.getParameterMap("articleId", article.getId(), "articleType",
				article.getCodeSystemName()));
		if (!results.isEmpty()) {
			Integer value = results.get(0);
			if (value > 1) {
				return Availability.IN_STOCK;
			} else if (value == 1) {
				return Availability.CRITICAL_STOCK;
			}
			return Availability.OUT_OF_STOCK;
		}
		return null;
	}
	
	public static Availability determineAvailability(IStockEntry se){
		return IStockService.determineAvailability(se.getCurrentStock(), se.getMinimumStock(),
			isTriggerStockAvailabilityOnBelow());
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
	
	private IArtikel loadArticle(String article){
		if (article == null) {
			log.warn("performSingleReturn for null article", new Throwable("Diagnosis"));
			return null;
		}
		PersistentObject po = CoreHub.poFactory.createFromString(article);
		if (po != null && po instanceof Artikel) {
			return (Artikel) po;
		}
		return null;
	}
	
	public List<Stock> getAllStocks(boolean includeCommissioningSystems){
		Query<Stock> qbe = new Query<Stock>(Stock.class);
		if(!includeCommissioningSystems) {
			qbe.add(Stock.FLD_DRIVER_UUID, Query.EQUALS, null);
		}
		qbe.orderBy(false, Stock.FLD_PRIORITY);
		return qbe.execute();
	}
	
	public Availability getArticleAvailabilityForStock(IStock stock, String article){
		IStockEntry se = findStockEntryForArticleInStock(stock, article);
		return IStockService.determineAvailability(se.getCurrentStock(), se.getMinimumStock(),
			isTriggerStockAvailabilityOnBelow());
	}
	
	@Override
	public IStockEntry findStockEntryForArticleInStock(IStock iStock, String storeToString){
		Stock stock = (Stock) iStock;
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		Query<StockEntry> qre = new Query<StockEntry>(StockEntry.class);
		qre.add(StockEntry.FLD_STOCK, Query.EQUALS, stock.getId());
		qre.add(StockEntry.FLD_ARTICLE_TYPE, Query.EQUALS, vals[0]);
		qre.add(StockEntry.FLD_ARTICLE_ID, Query.EQUALS, vals[1]);
		List<StockEntry> qbe = qre.execute();
		if (qbe.isEmpty()) {
			return null;
		}
		return qbe.get(0);
	}
	
	@Override
	public IStockEntry storeArticleInStock(IStock stock, String article){
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			return stockEntry;
		}
		Artikel loadArticle = loadArticle(article);
		if (loadArticle == null) {
			return null;
		}
		StockEntry se = new StockEntry((Stock) stock, loadArticle);
		CoreHub.getLocalLockService().acquireLock(se);
		CoreHub.getLocalLockService().releaseLock(se);
		return se;
	}
	
	@Override
	public void unstoreArticleFromStock(IStock stock, String article){
		IStockEntry stockEntry = findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			LockResponse lr = CoreHub.getLocalLockService()
				.acquireLockBlocking((StockEntry) stockEntry, 1, new NullProgressMonitor());
			if (lr.isOk()) {
				((StockEntry) stockEntry).delete();
				CoreHub.getLocalLockService().releaseLock(((StockEntry) stockEntry));
			} else {
				log.warn("Could not unstore article [{}]", article);
			}
		}
	}
	
	@Override
	public List<? extends IStockEntry> findAllStockEntriesForArticle(String storeToString){
		String[] vals = storeToString.split(StringConstants.DOUBLECOLON);
		Query<StockEntry> qre = new Query<StockEntry>(StockEntry.class);
		qre.add(StockEntry.FLD_ARTICLE_TYPE, Query.EQUALS, vals[0]);
		qre.add(StockEntry.FLD_ARTICLE_ID, Query.EQUALS, vals[1]);
		return qre.execute();
	}
	
	@Override
	public List<? extends IStockEntry> findAllStockEntriesForStock(IStock stock){
		// TODO Auto-generated method stub
		return null;
	}
	
}
