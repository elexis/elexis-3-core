package ch.elexis.core.ui.views.textsystem.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.core.ui.views.textsystem.TextTemplatePrintSettings;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

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
	private String printer;
	private String tray;
	private boolean exists;
	private boolean systemTemplate;
	private boolean askForAddress;
	
	private String cfgTemplateBase;
	
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
		updateConfigTemplateBase();
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
		
		// set printer and tray info
		updateConfigTemplateBase();
		printer =
			CoreHub.localCfg.get(cfgTemplateBase
				+ TextTemplatePrintSettings.TXT_TEMPLATE_PRINTER_SUFFIX, null);
		tray =
			CoreHub.localCfg.get(cfgTemplateBase
				+ TextTemplatePrintSettings.TXT_TEMPLATE_TRAY_SUFFIX, null);
	}
	
	public void removeTemplateReference(){
		templateId = null;
		exists = false;
		removePrintSettings();
	}
	
	private void removePrintSettings(){
		CoreHub.localCfg.remove(cfgTemplateBase
			+ TextTemplatePrintSettings.TXT_TEMPLATE_PRINTER_SUFFIX);
		CoreHub.localCfg.remove(cfgTemplateBase
			+ TextTemplatePrintSettings.TXT_TEMPLATE_TRAY_SUFFIX);
		CoreHub.localCfg.flush();
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
		if (mandantId == null) {
			mandantId = "";
		}
		this.mandantId = mandantId;
		updateTemplateReference(UPDATE_TYPE.MANDANT);
		// update printConfigBase if for new mandant
		updateConfigTemplateBase();
	}
	
	public void setMandant(Mandant mandant){
		if (mandant == null) {
			setMandant("");
		} else {
			setMandant(mandant.getId());
		}
	}
	
	public Mandant getMandant(){
		Mandant ret = (mandantId == "" ? null : Mandant.load(mandantId));
		if (ret != null && ret.exists()) {
			return ret;
		}
		return null;
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
	
	public String getPrinter(){
		return printer;
	}
	
	public void setPrinter(String printer){
		this.printer = printer;
		CoreHub.localCfg.set(cfgTemplateBase
			+ TextTemplatePrintSettings.TXT_TEMPLATE_PRINTER_SUFFIX, printer);
		CoreHub.localCfg.flush();
	}
	
	public String getTray(){
		return tray;
	}
	
	public void setTray(String tray){
		this.tray = tray;
		CoreHub.localCfg.set(cfgTemplateBase + TextTemplatePrintSettings.TXT_TEMPLATE_TRAY_SUFFIX,
			tray);
		CoreHub.localCfg.flush();
	}
	
	public String getCfgTemplateBase(){
		return cfgTemplateBase;
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
				DocumentSelectDialog.setDontAskForAddresseeForThisTemplate(bt, !askForAddress);
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
	
	private void updateConfigTemplateBase(){
		String type = MimeTypeUtil.getSimpleName(mimeType);
		
		// don't keep old print setting refs
		if (printer != null) {
			removePrintSettings();
		}
		
		if (systemTemplate || mandantId == null || mandantId.isEmpty()) {
			cfgTemplateBase =
				TextTemplatePrintSettings.TXT_TEMPLATE_PREFIX_PUBLIC + type + "/" + name;
		} else {
			cfgTemplateBase =
				TextTemplatePrintSettings.TXT_TEMPLATE_PREFIX_PRIVATE + mandantId + "/" + type
					+ "/" + name;
		}
		
		if (printer != null)
			setPrinter(printer);
		
		if (tray != null)
			setTray(tray);
		
	}
	
	/**
	 * Find an existing template with matching name. If the mandantId is provided, and a template
	 * with matching mandantId is found it is preferred to templates without mandantId.
	 * 
	 * @param name
	 * @param mandantId
	 * @return
	 */
	public static Brief findExistingTemplate(String name, String mandantId){
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
		qbe.and();
		qbe.add(Brief.FLD_SUBJECT, Query.EQUALS, name);
		qbe.startGroup();
		if (mandantId != null) {
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, mandantId);
			qbe.or();
		}
		qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, StringTool.leer);
		qbe.endGroup();
		List<Brief> list = qbe.execute();
		Brief ret = null;
		if (list != null && !list.isEmpty()) {
			ret = list.get(0);
			if (list.size() > 1) {
				if (mandantId != null) {
					for (Brief brief : list) {
						if (mandantId.equals(brief.get(Brief.FLD_DESTINATION_ID))) {
							ret = brief;
							break;
						} else if (ret == null) {
							if (StringUtils.isEmpty(brief.get(Brief.FLD_DESTINATION_ID))) {
								ret = brief;
							}
						}
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Find existing templates matching the provided parameters.
	 * 
	 * @param isSysTemplate
	 * @param name
	 * @param mimeType
	 * @param mandantId
	 * @return
	 */
	public static List<Brief> findExistingTemplates(boolean isSysTemplate, String name,
		String mimeType, String mandantId){
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add(Brief.FLD_SUBJECT, Query.EQUALS, name);
		qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
		if (mimeType != null) {
			qbe.add(Brief.FLD_MIME_TYPE, Query.EQUALS, mimeType);
		}
		if (isSysTemplate) {
			qbe.add(Brief.FLD_KONSULTATION_ID, Query.EQUALS, Brief.SYS_TEMPLATE);
		} else {
			qbe.add(Brief.FLD_KONSULTATION_ID, Query.NOT_EQUAL, Brief.SYS_TEMPLATE);
		}
		if (mandantId != null) {
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, mandantId);
		} else {
			qbe.startGroup();
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, StringTool.leer);
			qbe.or();
			qbe.addToken(Brief.FLD_DESTINATION_ID + " is NULL");
			qbe.endGroup();
		}
		return qbe.execute();
	}
}
