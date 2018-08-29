package ch.elexis.core.data.interfaces;

public interface IArticle {
	/**
	 * @return the number of dispensable items available within a package, or 0 if unknown
	 */
	public int getPackageUnit();
	
	/**
	 * @return the number of dispensable items to be dispensed during a default selling process, if
	 *         0 the whole package is dispensed
	 */
	public int getSellingUnit();
	
	public String getName();
	
	/**
	 * Determine whether this article is a product or a package. A product is the abstract
	 * definition of articles available as packages. Hence a product can not be billed, as it does
	 * not represent a tangible element.
	 */
	public boolean isProduct();
	
	public String getGTIN();
	
	public String getLabel();
	
	public String getId();
	
	public String getCodeSystemName();
}
