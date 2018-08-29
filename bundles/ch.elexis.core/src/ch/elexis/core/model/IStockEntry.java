package ch.elexis.core.model;

public interface IStockEntry extends Identifiable {
	
	public IArticle getArticle();
	
	public int getMinimumStock();
	
	public void setMinimumStock(int minStock);
	
	public int getCurrentStock();
	
	public void setCurrentStock(int currentStock);
	
	public int getMaximumStock();
	
	public void setMaximumStock(int maxStock);
	
	public int getFractionUnits();

	public void setFractionUnits(int rest);

	public IContact getProvider();
	
	public void setProvider(IContact provider);
	
	public IStock getStock();
	
	public void setStock(IStock stock);
	
	public void setArticle(IArticle loadArticle);
}
