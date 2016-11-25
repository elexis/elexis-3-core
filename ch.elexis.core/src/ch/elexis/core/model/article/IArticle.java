package ch.elexis.core.model.article;

/**
 * @since 3.2
 */
public interface IArticle {
	
	public int getPackageUnit();
	
	public int getSellingUnit();
	
	public String getName();
	
	/**
	 * Determine whether this article is a product or a package. A product is the abstract
	 * definition of articles available as packages. Hence a product can not be billed, as it does
	 * not represent a tangible element.
	 */
	public boolean isProduct();
	
	public String getGTIN();
}
