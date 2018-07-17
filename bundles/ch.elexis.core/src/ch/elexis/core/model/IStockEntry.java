package ch.elexis.core.model;

public interface IStockEntry {
	
	public IArticle getArticle();
	
	public int getMinimumStock();
	
	public void setMinimumStock(int minStock);
	
	public int getCurrentStock();
	
	public void setCurrentStock(int currentStock);
	
	public int getMaximumStock();
	
	public void setMaximumStock(int maxStock);
	
	public int getFractionUnits();

	public void setFractionUnits(int rest);

	public Object getProvider();
	
	public void setProvider(Object provider);
	
	public IStock getStock();
}
