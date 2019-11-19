package ch.elexis.core.ui.views.textsystem;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.data.Mandant;

import ch.rgw.tools.StringTool;
public class TextTemplatePrintSettings {
	public static final String TXT_TEMPLATE_PREFIX_PUBLIC = "texttemplates/public/";
	public static final String TXT_TEMPLATE_PREFIX_PRIVATE = "texttemplates/private/";
	public static final String TXT_TEMPLATE_PRINTER_SUFFIX = "/printer";
	public static final String TXT_TEMPLATE_TRAY_SUFFIX = "/tray";
	
	private static final String SEPARATOR = StringTool.slash;
	private String printer;
	private String tray;
	
	public TextTemplatePrintSettings(String template, String mimeType){
		this(template, mimeType, null, null, ElexisEventDispatcher.getSelectedMandator());
	}
	
	public TextTemplatePrintSettings(String template, String mimeType,
		@Nullable String alternatePrintCfg, @Nullable String alternateTrayCfg){
		this(template, mimeType, alternatePrintCfg, alternateTrayCfg, ElexisEventDispatcher
			.getSelectedMandator());
	}
	
	public TextTemplatePrintSettings(String template, String mimeType,
		@Nullable String alternatePrintCfg, @Nullable String alternateTrayCfg, Mandant mandant){
		String type = MimeTypeUtil.getSimpleName(mimeType);
		
		// check specific entry for this mandant and template exists
		if (mandant != null) {
			String templateBase =
				TextTemplatePrintSettings.TXT_TEMPLATE_PREFIX_PRIVATE + mandant.getId() + SEPARATOR
					+ type + SEPARATOR + template;
			printer = getTemplatePrinterFromConfig(templateBase);
			tray = getTemplateTrayFromConfig(templateBase);
		}
		
		// try public template configuration
		if (printer == null) {
			printer =
				getTemplatePrinterFromConfig(TXT_TEMPLATE_PREFIX_PUBLIC + type + SEPARATOR
					+ template);
			tray =
				getTemplateTrayFromConfig(TXT_TEMPLATE_PREFIX_PUBLIC + type + SEPARATOR + template);
			
			// alternative config -> else keep neutral/null values for printer and tray
			if (printer == null && alternatePrintCfg != null && alternateTrayCfg != null) {
				printer = CoreHub.localCfg.get(alternatePrintCfg, null);
				tray = CoreHub.localCfg.get(alternateTrayCfg, null);
			}
		}
	}
	
	public String getPrinter(){
		return this.printer;
	}
	
	public String getTray(){
		return this.tray;
	}
	
	/**
	 * get the print settings for this template
	 * 
	 * @param template
	 *            name of the template
	 * @param mandant
	 *            logged in mandant
	 * @param alternatePrintCfg
	 *            alternative config entry to check for printer (may be null)
	 * @param alternateTrayCfg
	 *            alternative config entry to check for printer tray (may be null)
	 * @return Prio1: mandants template print settings if available, Prio2: public template print
	 *         settings, Prio3: settings form alternative configuration, Prio 4: Returns
	 *         {@code String[null,null]} if nothing was found (will result in using default
	 *         printer/tray)
	 */
	
	private String getTemplatePrinterFromConfig(String templateBase){
		return CoreHub.localCfg.get(templateBase + TXT_TEMPLATE_PRINTER_SUFFIX, null);
	}
	
	private String getTemplateTrayFromConfig(String templateBase){
		return CoreHub.localCfg.get(templateBase + TXT_TEMPLATE_TRAY_SUFFIX, null);
	}
	
}
