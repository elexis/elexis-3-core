package ch.elexis.data;

import java.util.List;

import ch.rgw.tools.JdbcLink;

/**
 * Stores the default signature for an ATC code or a specific article
 *
 */
public class ArticleDefaultSignature extends PersistentObject {
	
	public static final String FLD_ATC_CODE = "atccode";
	public static final String FLD_ARTICLE = "article";
	public static final String FLD_TYPE = "type";
	
	public static final String FLD_SIG_MORNING = "morning";
	public static final String FLD_SIG_NOON = "noon";
	public static final String FLD_SIG_EVENING = "evening";
	public static final String FLD_SIG_NIGHT = "night";
	public static final String FLD_SIG_COMMENT = "comment";
	
	public static final String TABLENAME = "default_signatures";
	private static final String VERSION_ENTRY_ID = "VERSION";
	private static final String VERSION = "1.0.0";
	
	//@formatter:off
	/** Definition of the database table */
	static final String createDB = "CREATE TABLE " + TABLENAME 
		+ "("
		+ "ID VARCHAR(25) primary key," 
		+ "lastupdate BIGINT," + "deleted CHAR(1) default '0',"
		+ FLD_ATC_CODE + " CHAR(10)," 
		+ FLD_ARTICLE + " VARCHAR(255)," // contains EAN%Pharmacode$Article_StoreToString
		+ FLD_TYPE + " VARCHAR(255),"
		+ FLD_SIG_MORNING + " CHAR(10)," // id(VERSION) contains cummulated N dataset vers
		+ FLD_SIG_NOON + " CHAR(10)," 
		+ FLD_SIG_EVENING + " CHAR(10)," 
		+ FLD_SIG_NIGHT + " CHAR(10),"
		+ FLD_SIG_COMMENT + " TEXT," 
		+ PersistentObject.FLD_EXTINFO + " BLOB"
		+ "); " 
		+ "CREATE INDEX idxATCCode ON " + TABLENAME + " (" + FLD_ATC_CODE + "); "
		+ "INSERT INTO " + TABLENAME + " (ID," + FLD_ATC_CODE + ") VALUES ('VERSION', "
		+ JdbcLink.wrap(VERSION) + ")";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_ATC_CODE, FLD_ARTICLE, FLD_TYPE, FLD_SIG_MORNING, FLD_SIG_NOON,
			FLD_SIG_EVENING, FLD_SIG_NIGHT, FLD_SIG_COMMENT, FLD_EXTINFO);
		ArticleDefaultSignature version = load(VERSION_ENTRY_ID);
		if (!version.exists()) {
			createOrModifyTable(createDB);
		}
	}
	
	protected ArticleDefaultSignature(){}
	
	protected ArticleDefaultSignature(final String id){
		super(id);
	}
	
	/**
	 * Create a new default signature for an article. The value can be bound to a specific article
	 * (where the following article info is stored: <code>EAN$PharmaCode$Elexis_internal_ID</code>)
	 * or an ATC Code.
	 * 
	 * @param article
	 * @param atcCode
	 */
	public ArticleDefaultSignature(Artikel article, String atcCode){
		create(null);
		if (article != null)
			set(FLD_ARTICLE,
				article.getEAN() + "$" + article.getPharmaCode() + "$" + article.storeToString());
		if (atcCode != null)
			set(FLD_ATC_CODE, atcCode);
	}
	
	public static ArticleDefaultSignature load(String id){
		return new ArticleDefaultSignature(id);
	}
	
	@Override
	public String getLabel(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	/**
	 * 
	 * @param artikel
	 * @return the default signature for the specific article or the specific ATC code (in order),
	 *         <code>null</code> if either not available
	 */
	public static ArticleDefaultSignature getDefaultsignatureForArticle(Artikel artikel){
		Query<ArticleDefaultSignature> qbe =
			new Query<ArticleDefaultSignature>(ArticleDefaultSignature.class);
		qbe.add(FLD_ARTICLE, Query.LIKE, "%" + artikel.storeToString());
		List<ArticleDefaultSignature> resultArticle = qbe.execute();
		if (resultArticle.size() > 0)
			return resultArticle.get(0);
		return ArticleDefaultSignature.getDefaultSignatureForATCCode(artikel.getATC_code());
	}
	
	/**
	 * 
	 * @param atcCode
	 * @return the default signature or <code>null</code> if no corresponding entry
	 */
	public static ArticleDefaultSignature getDefaultSignatureForATCCode(String atcCode){
		Query<ArticleDefaultSignature> qbe =
			new Query<ArticleDefaultSignature>(ArticleDefaultSignature.class);
		qbe.add(FLD_ATC_CODE, Query.LIKE, atcCode);
		List<ArticleDefaultSignature> execute = qbe.execute();
		return (execute.size() > 0) ? execute.get(0) : null;
	}
	
	public String getSignatureAsDosisString(){
		String[] values = new String[4];
		get(new String[] {
			FLD_SIG_MORNING, FLD_SIG_NOON, FLD_SIG_EVENING, FLD_SIG_NIGHT
		}, values);
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			String string = values[i];
			if (string.length()>0) {
				if (i > 0) {
					sb.append("-");
				}
				sb.append(string);
			}
		}
		return sb.toString();
	}
	
	public String getSignatureMorning(){
		return checkNull(get(FLD_SIG_MORNING));
	}
	
	public void setSignatureMorning(String sm){
		set(FLD_SIG_MORNING, sm);
	}
	
	public String getSignatureNoon(){
		return checkNull(get(FLD_SIG_NOON));
	}
	
	public void setSignatureNoon(String sm){
		set(FLD_SIG_NOON, sm);
	}
	
	public String getSignatureEvening(){
		return checkNull(get(FLD_SIG_EVENING));
	}
	
	public void setSignatureEvening(String sm){
		set(FLD_SIG_EVENING, sm);
	}
	
	public String getSignatureNight(){
		return checkNull(get(FLD_SIG_NIGHT));
	}
	
	public void setSignatureNight(String sm){
		set(FLD_SIG_NIGHT, sm);
	}
	
	public String getSignatureComment(){
		return checkNull(get(FLD_SIG_COMMENT));
	}
	
	public void setSignatureComment(String sm){
		set(FLD_SIG_COMMENT, sm);
	}
}
