package ch.elexis.core.ui.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.utils.Extensions;
import ch.elexis.data.Rechnung;

public class SendInvoiceAsMailHandler extends AbstractHandler {

	private IRnOutputter pdfOutputter;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IInvoice> selectedInvoice = ContextServiceHolder.get().getTyped(IInvoice.class);
		if (selectedInvoice.isPresent()) {
			// generate a pdf to send
			Optional<IRnOutputter> pdfOutputter = getPdfOutputter();
			if (pdfOutputter.isPresent()) {
				List<File> pdfs = getPdfs(selectedInvoice.get());
				
				if (pdfs.isEmpty()) {
					Properties outputProps = new Properties();
					outputProps.put(IRnOutputter.PROP_OUTPUT_MODIFY_INVOICESTATE, Boolean.toString(false));
					outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_ESR, Boolean.toString(true));
					outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_RECLAIM, Boolean.toString(true));
					outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_MAIL, Boolean.toString(false));

					pdfOutputter.get().doOutput(IRnOutputter.TYPE.ORIG, Collections.singletonList(
							(Rechnung) NoPoUtil.loadAsPersistentObject(selectedInvoice.get())), outputProps);
					pdfs = getPdfs(selectedInvoice.get());
					if (pdfs.isEmpty()) {
						MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
								"Konnte keine Pdfs für die Rechnung erstellen.");
						return Boolean.FALSE;
					}
				}

				if (!pdfs.isEmpty()) {
					ICommandService commandService = HandlerUtil.getActiveWorkbenchWindow(event)
							.getService(ICommandService.class);
					try {
						String attachmentsString = AttachmentsUtil.getAttachmentsString(pdfs);
						Command sendMailCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMail"); //$NON-NLS-1$

						HashMap<String, String> params = new HashMap<>();
						params.put("ch.elexis.core.mail.ui.sendMail.attachments", attachmentsString); //$NON-NLS-1$

						if (StringUtils.isNotBlank(selectedInvoice.get().getCoverage().getPatient().getEmail())) {
							params.put("ch.elexis.core.mail.ui.sendMail.to", //$NON-NLS-1$
									selectedInvoice.get().getCoverage().getPatient().getEmail());
						}

						params.put("ch.elexis.core.mail.ui.sendMail.subject", //$NON-NLS-1$
								"Rechnung " + selectedInvoice.get().getNumber()); //$NON-NLS-1$

						ParameterizedCommand parametrizedCommmand = ParameterizedCommand
								.generateCommand(sendMailCommand, params);
						PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(parametrizedCommmand,
								null);
					} catch (Exception ex) {
						throw new RuntimeException("ch.elexis.core.mail.ui.sendMail not found", ex); //$NON-NLS-1$
					}
				}
			} else {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler", "Keine pdf Ausgabe installiert.");
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private List<File> getPdfs(IInvoice iInvoice) {
		try {
			String pdfOutputDir = OutputterUtil.getPdfOutputDir("qrpdf-output/");
			File esrFile = VirtualFilesystemServiceHolder.get()
					.of(pdfOutputDir + File.separator + iInvoice.getNumber() + "_esr.pdf").toFile().orElse(null); //$NON-NLS-1$
			File rfFile = VirtualFilesystemServiceHolder.get()
					.of(pdfOutputDir + File.separator + iInvoice.getNumber() + "_rf.pdf").toFile().orElse(null); //$NON-NLS-1$
			if (esrFile.exists() && rfFile.exists()) {
				List<File> ret = new ArrayList<>();
				ret.add(esrFile);
				ret.add(rfFile);
				return ret;
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("Could not access qr pdf output", e); //$NON-NLS-1$
		}
		return Collections.emptyList();
	}

	private Optional<IRnOutputter> getPdfOutputter() {
		if (pdfOutputter != null) {
			return Optional.of(pdfOutputter);
		}
		@SuppressWarnings("unchecked")
		List<IRnOutputter> foundOutputters = Extensions.getClasses(ExtensionPointConstantsData.RECHNUNGS_MANAGER,
				"outputter"); //$NON-NLS-1$
		for (IRnOutputter outputter : foundOutputters) {
			if (outputter.getClass().getName().endsWith("pdfBills.QrRnOutputter")) { //$NON-NLS-1$
				pdfOutputter = outputter;
				return Optional.of(pdfOutputter);
			}
		}
		return Optional.empty();
	}

	/**
	 * Copy from pdf outputter to lookup output directories.
	 */
	private class OutputterUtil {

		public static final String CFG_ROOT = "pdf-outputter/"; //$NON-NLS-1$

		public static final String CFG_PRINT_GLOBALOUTPUTDIRS = CFG_ROOT + "global.output.dirs"; //$NON-NLS-1$

		public static final String CFG_PRINT_GLOBALPDFDIR = CFG_ROOT + "global.output.pdfdir"; //$NON-NLS-1$
		public static final String CFG_PRINT_GLOBALXMLDIR = CFG_ROOT + "global.output.xmldir"; //$NON-NLS-1$

		public static final String PDFDIR = "pdfdir"; //$NON-NLS-1$
		
		/**
		 * Test if global output directories should be used.
		 *
		 * @return
		 */
		public static boolean useGlobalOutputDirs() {
			return hasGlobalDirectories() && LocalConfigService.get(CFG_PRINT_GLOBALOUTPUTDIRS, true);
		}

		private static boolean hasGlobalDirectories() {
			return StringUtils
					.isNotBlank(PreferencesUtil.getOsSpecificPreference(CFG_PRINT_GLOBALPDFDIR, ConfigServiceHolder.get()))
					&& StringUtils.isNotBlank(
							PreferencesUtil.getOsSpecificPreference(CFG_PRINT_GLOBALXMLDIR, ConfigServiceHolder.get()));
		}

		public static String getPdfOutputDir(String configRoot) {
			if (useGlobalOutputDirs()) {
				return PreferencesUtil.getOsSpecificPreference(CFG_PRINT_GLOBALPDFDIR, ConfigServiceHolder.get());
			} else {
				return LocalConfigService.get(configRoot + PDFDIR, StringUtils.EMPTY);
			}
		}
	}
}
