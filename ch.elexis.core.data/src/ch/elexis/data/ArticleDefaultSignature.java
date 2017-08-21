package ch.elexis.data;

import java.util.List;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.prescription.EntryType;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

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
	
	public static final String EXT_FLD_MEDICATIONTYPE = "medicationType";
	public static final String EXT_FLD_DISPOSALTYPE = "disposalType";
	public static final String EXT_FLD_FREETEXT = "textSignature";
	
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
		StringBuilder sb = new StringBuilder();
		String atcCode = get(FLD_ATC_CODE);
		Artikel article = getArticle();
		if (atcCode != null && !atcCode.isEmpty()) {
			sb.append("ATC [" + atcCode + "] ");
		} else if (article != null) {
			sb.append("ARTICLE [" + article.getLabel() + "] ");
		}
		sb.append(getSignatureMorning()).append("-").append(getSignatureNoon()).append("-")
			.append(getSignatureEvening()).append("-").append(getSignatureNight()).append(" ")
			.append(getSignatureComment());
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
	public static @Nullable ArticleDefaultSignature getDefaultsignatureForArticle(@Nullable Artikel artikel){
		if(artikel==null) return null;
		Query<ArticleDefaultSignature> qbe =
			new Query<ArticleDefaultSignature>(ArticleDefaultSignature.class);
		qbe.add(FLD_ARTICLE, Query.LIKE, "%" + artikel.storeToString());
		List<ArticleDefaultSignature> resultArticle = qbe.execute();
		if (resultArticle.size() > 0) {
			return resultArticle.get(0);
		}
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
	
	public EntryType getMedicationType(){
		String typeNumber = (String) getExtInfoStoredObjectByKey(EXT_FLD_MEDICATIONTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}
	
	public void setMedicationType(EntryType type){
		setExtInfoStoredObjectByKey(EXT_FLD_MEDICATIONTYPE, Integer.toString(type.numericValue()));
	}
	
	public EntryType getDisposalType(){
		String typeNumber = (String) getExtInfoStoredObjectByKey(EXT_FLD_DISPOSALTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}
	
	public void setDisposalType(EntryType type){
		setExtInfoStoredObjectByKey(EXT_FLD_DISPOSALTYPE, Integer.toString(type.numericValue()));
	}
	
	public Artikel getArticle(){
		String articleString = get(FLD_ARTICLE);
		if (articleString != null && !articleString.isEmpty()) {
			String[] parts = articleString.split("\\$");
			if (parts.length == 3) {
				PersistentObjectFactory factory = new PersistentObjectFactory();
				PersistentObject ret = factory.createFromString(parts[2]);
				if (ret instanceof Artikel) {
					return (Artikel) ret;
				}
			}
		}
		return null;
	}
	
	public String getAtcCode(){
		return get(FLD_ATC_CODE);
	}
	
	public String getSignatureFreeText(){
		return (String) getExtInfoStoredObjectByKey(EXT_FLD_FREETEXT);
	}
	
	public void setSignatureFreeText(String text){
		if (text == null) {
			text = "";
		}
		setExtInfoStoredObjectByKey(EXT_FLD_FREETEXT, text);
	}
	
	public static class ArticleSignature {
		
		private ArticleDefaultSignature defaultSignature;
		
		private Artikel article;
		private String atcCode;
		
		private String morning;
		private String noon;
		private String evening;
		private String night;
		
		private String freeText;
		
		private String comment;
		
		private EntryType medicationType;
		private EntryType disposalType;
		
		private TimeTool endDate;
		
		public static ArticleSignature fromDefault(ArticleDefaultSignature defaultSignature){
			ArticleSignature signature =
				new ArticleSignature(defaultSignature.getArticle(), defaultSignature.getAtcCode());
			
			signature.setMorning(defaultSignature.getSignatureMorning());
			signature.setNoon(defaultSignature.getSignatureNoon());
			signature.setEvening(defaultSignature.getSignatureEvening());
			signature.setNight(defaultSignature.getSignatureNight());
			
			signature.setFreeText(defaultSignature.getSignatureFreeText());
			
			signature.setComment(defaultSignature.getSignatureComment());
			
			signature.setMedicationType(defaultSignature.getMedicationType());
			signature.setDisposalType(defaultSignature.getDisposalType());
			
			signature.defaultSignature = defaultSignature;
			
			return signature;
		}
		
		public ArticleSignature(Artikel article, String atcCode){
			this.article = article;
			this.atcCode = atcCode;
		}
		
		public ArticleDefaultSignature toDefault(){
			if (defaultSignature != null) {
				defaultSignature.setSignatureMorning(morning);
				defaultSignature.setSignatureNoon(noon);
				defaultSignature.setSignatureEvening(evening);
				defaultSignature.setSignatureNight(night);
				
				defaultSignature.setSignatureFreeText(freeText);
				
				defaultSignature.setSignatureComment(comment);
				
				if (medicationType != null) {
					defaultSignature.setMedicationType(medicationType);
				}
				if (disposalType != null) {
					defaultSignature.setDisposalType(disposalType);
				}
				if (atcCode != null && !atcCode.isEmpty()) {
					defaultSignature.set(FLD_ATC_CODE, atcCode);
					defaultSignature.set(FLD_ARTICLE, null);
				} else if (article != null) {
					defaultSignature.set(FLD_ATC_CODE, null);
					defaultSignature.set(FLD_ARTICLE, article.storeToString());
				}
			}
			return defaultSignature;
		}
		
		public void delete(){
			if (defaultSignature != null) {
				defaultSignature.delete();
			}
		}
		
		public String getFreeText(){
			return freeText;
		}
		
		public void setFreeText(String text){
			this.freeText = text;
		}
		
		public String getMorning(){
			return morning;
		}
		
		public void setMorning(String morning){
			this.morning = morning;
		}
		
		public String getNoon(){
			return noon;
		}
		
		public void setNoon(String noon){
			this.noon = noon;
		}
		
		public String getEvening(){
			return evening;
		}
		
		public void setEvening(String evening){
			this.evening = evening;
		}
		
		public String getNight(){
			return night;
		}
		
		public void setNight(String night){
			this.night = night;
		}
		
		public String getSignatureAsDosisString(){
			String freeText = getFreeText();
			if (freeText != null && !freeText.isEmpty()) {
				return freeText;
			}
			
			String[] values = new String[] {
				morning, noon, evening, night
			};
			
			StringBuilder sb = new StringBuilder();
			if (signatureInfoExists(values)) {
				for (int i = 0; i < values.length; i++) {
					String string = values[i] == null || values[i].isEmpty() ? "0" : values[i];
					
					if (i > 0) {
						sb.append("-");
					}
					sb.append(string);
				}
			}
			return sb.toString();
		}
		
		private boolean signatureInfoExists(String[] values){
			for (String val : values) {
				if (val != null && !val.isEmpty()) {
					return true;
				}
			}
			return false;
		}
		
		public String getComment(){
			return comment;
		}
		
		public void setComment(String comment){
			this.comment = comment;
		}
		
		public EntryType getMedicationType(){
			return medicationType;
		}
		
		public void setMedicationType(EntryType medicationType){
			this.medicationType = medicationType;
		}
		
		public EntryType getDisposalType(){
			return disposalType;
		}
		
		public void setDisposalType(EntryType disposalType){
			this.disposalType = disposalType;
		}
		
		public void setAtcCode(String code){
			this.atcCode = code;
			this.article = null;
		}
		
		public void setArticle(Artikel article){
			this.article = article;
			this.atcCode = null;
		}
		
		public boolean isAtc(){
			return (atcCode != null && !atcCode.isEmpty());
		}
		
		public boolean isPersistent(){
			return defaultSignature != null;
		}
		
		public void createPersistent(){
			if (isPersistent()) {
				return;
			}
			if (isAtc()) {
				defaultSignature = new ArticleDefaultSignature(null, atcCode);
			} else {
				defaultSignature = new ArticleDefaultSignature(article, null);
			}
		}
		
		public TimeTool getEndDate(){
			return endDate;
		}
		
		public void setEndDate(TimeTool endDate){
			this.endDate = endDate;
		}
	}
}
