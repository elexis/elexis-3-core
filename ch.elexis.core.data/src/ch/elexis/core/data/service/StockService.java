package ch.elexis.core.data.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.services.IStockService;
import ch.elexis.data.Artikel;
import ch.elexis.data.DBConnection;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Stock;
import ch.elexis.data.StockEntry;
import ch.rgw.tools.ExHandler;

/**
 * Do not create an instance, use {@link CoreHub#getStockService()}.
 */
public class StockService implements IStockService {
	
	private static Logger log = LoggerFactory.getLogger(StockService.class);
	
	private static final String PS_SUM_CURRENT =
		"SELECT SUM(CURRENT) FROM STOCK_ENTRY WHERE ARTICLE_ID = ? AND ARTICLE_TYPE = ? AND DELETED = '0'";
	
	private static final String PS_AVAIL_CURRENT =
		"SELECT MAX(CASE WHEN CURRENT <= 0 THEN 0 WHEN (ABS(MIN)-CURRENT) >=0 THEN 1 ELSE 2 END) FROM STOCK_ENTRY WHERE ARTICLE_ID = ? AND ARTICLE_TYPE = ? AND DELETED = '0'";
	
	@Override
	public Integer getCumulatedStockForArticle(IArticle article){
		Artikel art = (Artikel) article;
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		PreparedStatement ps = dbConnection.getPreparedStatement(PS_SUM_CURRENT);
		try {
			ps.setString(1, art.getId());
			ps.setString(2, art.getClass().getName());
			ResultSet res = ps.executeQuery();
			Integer ret = null;
			if (res.next()) {
				Object object = res.getObject(1);
				if (object != null) {
					ret = res.getInt(1);
				}
			}
			res.close();
			return ret;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			dbConnection.releasePreparedStatement(ps);
		}
	}
	
	public void performSingleDisposal(IArticle article, int count){
		Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
		performSingleDisposal(article, count, (mandator != null) ? mandator.getId() : null);
	}
	
	@Override
	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId){
		if (article == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "Article is null");
		}
		
		IStockEntry se =
			findPreferredStockEntryForArticle(((Artikel) article).storeToString(), mandatorId);
		if (se == null) {
			return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
				"No stock entry for article found");
		}
		
		if (se.getStock().isCommissioningSystem()) {
			int sellingUnit = article.getSellingUnit();
			boolean isPartialUnitOutput =
				(sellingUnit > 0 && sellingUnit < article.getPackageUnit());
			if (isPartialUnitOutput) {
					boolean performPartialOutlay =
						CoreHub.globalCfg.get(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
							Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
					if (!performPartialOutlay) {
						return Status.OK_STATUS;
					}
			}
			
			return CoreHub.getStockCommissioningSystemService().performArticleOutlay(se, count,
				null);
		} else {
			LockResponse lr = CoreHub.getLocalLockService().acquireLockBlocking((StockEntry) se, 1,
				new NullProgressMonitor());
			if (lr.isOk()) {
				int fractionUnits = se.getFractionUnits();
				int ve = article.getSellingUnit();
				int vk = article.getPackageUnit();
				
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
				
				CoreHub.getLocalLockService().releaseLock((StockEntry) se);
				return Status.OK_STATUS;
			}
		}
		
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID, "Could not acquire lock");
	}
	
	public void performSingleReturn(IArticle article, int count){
		Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
		performSingleReturn(article, count, (mandator != null) ? mandator.getId() : null);
	}
	
	@Override
	public IStatus performSingleReturn(IArticle article, int count, String mandatorId){
		if (article == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "Article is null");
		}
		
		IStockEntry se =
			findPreferredStockEntryForArticle(((Artikel) article).storeToString(), null);
		if (se == null) {
			return new Status(Status.WARNING, CoreHub.PLUGIN_ID,
				"No stock entry for article found");
		}
		
		if (se.getStock().isCommissioningSystem()) {
			// updates must happen via manual inputs in the machine
			return Status.OK_STATUS;
		}
		
		LockResponse lr = CoreHub.getLocalLockService().acquireLockBlocking((StockEntry) se, 1,
			new NullProgressMonitor());
		if (lr.isOk()) {
			int fractionUnits = se.getFractionUnits();
			int ve = article.getSellingUnit();
			int vk = article.getPackageUnit();
			
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
			CoreHub.getLocalLockService().releaseLock((StockEntry) se);
			return Status.OK_STATUS;
		}
		return new Status(Status.WARNING, CoreHub.PLUGIN_ID, "Could not acquire lock");
	}
	
	@Override
	public Availability getCumulatedAvailabilityForArticle(IArticle article){
		Artikel art = (Artikel) article;
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		PreparedStatement ps = dbConnection.getPreparedStatement(PS_AVAIL_CURRENT);
		try {
			ps.setString(1, art.getId());
			ps.setString(2, art.getClass().getName());
			ResultSet res = ps.executeQuery();
			if (res.next()) {
				Object object = res.getObject(1);
				if (object != null) {
					int value = res.getInt(1);
					if (value > 1) {
						return Availability.IN_STOCK;
					} else if (value == 1) {
						return Availability.CRITICAL_STOCK;
					}
					return Availability.OUT_OF_STOCK;
				}
			}
			res.close();
			return null;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {}
			dbConnection.releasePreparedStatement(ps);
		}
	}
	
	public static Availability determineAvailability(IStockEntry se){
		String[] values = ((StockEntry) se).get(false, StockEntry.FLD_MIN, StockEntry.FLD_CURRENT);
		int min = Integer.valueOf(values[0]);
		int current = Integer.valueOf(values[1]);
		
		return IStockService.determineAvailability(current, min);
	}
	
	public List<StockEntry> getAllStockEntries(){
		return new Query<StockEntry>(StockEntry.class).execute();
	}
	
	@Override
	public IStockEntry findPreferredStockEntryForArticle(String storeToString, String mandatorId){
		List<? extends IStockEntry> entries = findAllStockEntriesForArticle(storeToString);
		int val = Integer.MAX_VALUE;
		IStockEntry ret = null;
		for (IStockEntry iStockEntry : entries) {
			Stock stock = (Stock) iStockEntry.getStock();
			Integer priority = stock.getPriority();
			if (priority < val) {
				val = priority;
				ret = iStockEntry;
			}
			if (mandatorId != null) {
				Mandant owner = stock.getOwner();
				if (owner != null && owner.getId().equals(mandatorId)) {
					return iStockEntry;
				}
			}
		}
		return ret;
	}
	
	private Artikel loadArticle(String article){
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
		return IStockService.determineAvailability(se.getCurrentStock(), se.getMinimumStock());
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
		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, article);
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
		IStockEntry stockEntry = findStockEntryForArticleInStock((Stock) stock, article);
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
