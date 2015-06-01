package ch.elexis.core.ui.views.textsystem.model;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;

public class TextTemplate {
	private enum UPDATE_TYPE {
		MANDANT, ADDRESS, SYS_TEMPLATE
	}
	
	public static final String DEFAULT_MANDANT = "Alle";
	
	private String name;
	private String description;
	private String mimeType;
	private String mimeTypePrintname;
	private String mandantId;
	private String templateId;
	private boolean exists;
	private boolean systemTemplate;
	private boolean askForAddress;
	
	public TextTemplate(String name, String description, String mimeType){
		this(name, description, mimeType, null, null, false, false, false);
	}
	
	public TextTemplate(String name, String description, String mimeType, boolean systemTemplate){
		this(name, description, mimeType, null, null, false, systemTemplate, false);
	}
	
	public TextTemplate(String name, String description, String mimeType, String mandantId,
		String templateId, boolean exists, boolean systemTemplate, boolean askForAddress){
		this.name = name;
		this.description = description;
		this.mandantId = mandantId;
		this.exists = exists;
		this.systemTemplate = systemTemplate;
		this.askForAddress = askForAddress;
		this.templateId = templateId;
		setMimeType(mimeType);
	}
	
	private void addTemplateReference(Brief template, boolean systemTemplate){
		// set Brief id
		this.templateId = template.getId();
		this.exists = true;
		setSystemTemplate(systemTemplate);
		
		//set mandant
		Kontakt adressat = template.getAdressat();
		if (adressat != null) {
			setMandant(template.getAdressat().getId());
		}
		
		// set askForAddress flag
		ISticker sticker = template.getSticker();
		if (sticker != null && sticker.getLabel().equals(Brief.DONT_ASK_FOR_ADDRESS_STICKER)) {
			setAskForAddress(false);
		} else {
			setAskForAddress(true);
		}
	}
	
	public void addFormTemplateReference(Brief template){
		addTemplateReference(template, false);
	}
	
	public void addSystemTemplateReference(Brief template){
		addTemplateReference(template, true);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getMimeType(){
		return mimeType;
	}
	
	/**
	 * sets mime type and mimeType print name
	 * 
	 * @param mimeType
	 */
	public void setMimeType(String mimeType){
		this.mimeType = mimeType;
		setMimeTypePrintname(mimeType);
	}
	
	public String getMimeTypePrintname(){
		return mimeTypePrintname;
	}
	
	public void setMimeTypePrintname(String rawMimeType){
		this.mimeTypePrintname = MimeTypeUtil.getPrettyPrintName(rawMimeType);
	}
	
	public void setMandant(String mandantId){
		if (mandantId != null && mandantId.isEmpty()) {
			mandantId = null;
		}
		this.mandantId = mandantId;
		updateTemplateReference(UPDATE_TYPE.MANDANT);
	}
	
	public void setMandant(Mandant mandant){
		setMandant(mandant.getId());
	}
	
	public Mandant getMandant(){
		return mandantId == null ? null : Mandant.load(mandantId);
	}
	
	public String getMandantLabel(){
		Mandant mandant = getMandant();
		if (mandant == null || !mandant.isValid()) {
			return DEFAULT_MANDANT;
		}
		return mandant.get(Mandant.FLD_NAME3);
	}
	
	public Brief getTemplate(){
		return templateId == null ? null : Brief.load(templateId);
	}
	
	public String getTemplateId(){
		return templateId;
	}
	
	public void setTemplateId(String templateId){
		this.templateId = templateId;
	}
	
	public boolean exists(){
		return exists;
	}
	
	public void setExists(boolean exists){
		this.exists = exists;
	}
	
	public boolean isSystemTemplate(){
		return systemTemplate;
	}
	
	public void setSystemTemplate(boolean systemTemplate){
		this.systemTemplate = systemTemplate;
		updateTemplateReference(UPDATE_TYPE.SYS_TEMPLATE);
	}
	
	public boolean askForAddress(){
		return askForAddress;
	}
	
	public void setAskForAddress(boolean askForAddress){
		this.askForAddress = askForAddress;
		updateTemplateReference(UPDATE_TYPE.ADDRESS);
	}
	
	private void updateTemplateReference(UPDATE_TYPE updateType){
		Brief bt = getTemplate();
		if (bt != null) {
			switch (updateType) {
			case MANDANT:
				bt.setAdressat(mandantId);
				break;
			case ADDRESS:
				DocumentSelectDialog.setDontAskForAddresseeForThisTemplate(bt, askForAddress);
				break;
			case SYS_TEMPLATE:
				String sysTemplate = systemTemplate ? Brief.SYS_TEMPLATE : "";
				bt.set(Brief.FLD_KONSULTATION_ID, sysTemplate);
				break;
			default:
				break;
			}
		}
	}
}
