package ch.elexis.core.ui.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.InvoiceConstants;
import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.utils.Extensions;
import ch.elexis.data.Rechnung;

public class SendInvoiceAsMailHandler extends AbstractHandler {

	private IRnOutputter pdfOutputter;

	private List<File> movedExisting;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IInvoice> selectedInvoice = ContextServiceHolder.get().getTyped(IInvoice.class);
		if (selectedInvoice.isPresent()) {
			// generate a pdf to send
			Optional<IRnOutputter> pdfOutputter = getPdfOutputter();
			if (pdfOutputter.isPresent()) {
				moveExistingPfds(selectedInvoice.get());

				Properties outputProps = new Properties();
				outputProps.put(IRnOutputter.PROP_OUTPUT_NOUI, Boolean.toString(true));
				outputProps.put(IRnOutputter.PROP_OUTPUT_MODIFY_INVOICESTATE, Boolean.toString(false));
				outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_ESR, Boolean.toString(true));
				outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_RECLAIM, Boolean.toString(true));
				outputProps.put(IRnOutputter.PROP_OUTPUT_WITH_MAIL, Boolean.toString(false));

				pdfOutputter.get().doOutput(IRnOutputter.TYPE.COPY,
						Collections.singletonList((Rechnung) NoPoUtil.loadAsPersistentObject(selectedInvoice.get())),
						outputProps);
				List<File> pdfs = getPdfs(selectedInvoice.get());
				if (pdfs.isEmpty()) {
					restoreExistingPfds();
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
							"Konnte keine Rechnungskopie Pdfs erstellen.");
					return Boolean.FALSE;
				} else {
					pdfs = moveCopyPdfs(pdfs, selectedInvoice.get());
					restoreExistingPfds();
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
								"Rechnungskopie " + selectedInvoice.get().getNumber()); //$NON-NLS-1$

						ParameterizedCommand parametrizedCommmand = ParameterizedCommand
								.generateCommand(sendMailCommand, params);
						Object result = PlatformUI.getWorkbench().getService(IHandlerService.class)
								.executeCommand(parametrizedCommmand, null);
						if (Boolean.TRUE.equals(result)) {
							selectedInvoice.get().addTrace(InvoiceConstants.OUTPUT, "Rechnungskopie per Mail");
							CoreModelServiceHolder.get().save(selectedInvoice.get());
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, selectedInvoice.get());
						}
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

	private List<File> moveCopyPdfs(List<File> pdfs, IInvoice iInvoice) {
		List<File> movedFiles = new ArrayList<>();
		for (File file : pdfs) {
			try {
				if (file.getName().endsWith("_esr.pdf")) {
					Path destination = file.toPath()
							.resolveSibling(iInvoice.getNumber() + "_Rechnungskopie_Einzahlungsschein.pdf");
					if (destination.toFile().exists()) {
						destination.toFile().delete();
					}
					Files.move(file.toPath(), destination);
					movedFiles.add(destination.toFile());
				} else if (file.getName().endsWith("_rf.pdf")) {
					Path destination = file.toPath()
							.resolveSibling(iInvoice.getNumber() + "_Rechnungskopie_Formular.pdf");
					if (destination.toFile().exists()) {
						destination.toFile().delete();
					}
					Files.move(file.toPath(), destination);
					movedFiles.add(destination.toFile());
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error moving existing pdfs", e);
			}
		}
		return movedFiles;
	}

	private void moveExistingPfds(IInvoice iInvoice) {
		movedExisting = new ArrayList<>();
		List<File> pdfs = getPdfs(iInvoice);
		for (File file : pdfs) {
			try {
				Path destination = file.toPath().resolveSibling("existing_" + file.getName());
				if (destination.toFile().exists()) {
					destination.toFile().delete();
				}
				Files.move(file.toPath(), destination);
				movedExisting.add(destination.toFile());
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error moving existing pdfs", e);
			}
		}
	}

	private void restoreExistingPfds() {
		if (movedExisting != null) {
			for (File file : movedExisting) {
				try {
					Path destination = file.toPath().resolveSibling(file.getName().replace("existing_", ""));
					if (destination.toFile().exists()) {
						destination.toFile().delete();
					}
					Files.move(file.toPath(), destination);
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error restoring existing pdfs", e);
				}
			}
		}
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
