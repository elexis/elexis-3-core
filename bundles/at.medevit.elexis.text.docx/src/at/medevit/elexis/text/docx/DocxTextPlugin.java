package at.medevit.elexis.text.docx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.LoggerFactory;

import at.medevit.elexis.text.docx.print.PrintProcess;
import at.medevit.elexis.text.docx.print.ScriptInitializer;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.views.textsystem.TextTemplatePrintSettings;
import ch.elexis.core.utils.CoreUtil;

public class DocxTextPlugin extends ch.elexis.core.text.docx.DocxTextPlugin implements ITextPlugin {

	private static final String DOCX_PREF = "textplugins/docx/";

	public static final String PRINT_PROCESS_TIMEOUT = DOCX_PREF + "printtimeout";

	public static final String PRINT_COMMAND_PREF = DOCX_PREF + "printcommand";
	public static final String PRINTTOPRINTER_COMMAND_PREF = DOCX_PREF + "printtoprintercommand";

	public static final String USE_PRINT_SCRIPT = DOCX_PREF + "printcommand";

	private static TextTemplatePrintSettings printSettings;

	private Composite composite;
	private Button openButton;
        private String testString;

	@Override
	public void setFocus() {
		if (composite != null && !composite.isDisposed()) {
			composite.setFocus();
		}
	}

	@Override
	public void dispose() {
		composite.dispose();
	}

	@Override
	public void showMenu(boolean b) {
		// ignore, no UI
	}

	@Override
	public void showToolbar(boolean b) {
		// ignore, no UI
	}

	@Override
	public void setSaveOnFocusLost(boolean bSave) {
		// ignore, no UI
	}

	@Override
	public boolean createEmptyDocument() {
		boolean ret = super.createEmptyDocument();
		if (ret && openButton != null && !openButton.isDisposed()) {
			openButton.setEnabled(true);
		}
		return ret;
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		if (checkTextPreferences()) {
			boolean ret = super.loadFromByteArray(bs, asTemplate);
			if (ret && openButton != null && !openButton.isDisposed()) {
				openButton.setEnabled(true);
			}
			return ret;
		}
		return false;
	}

	private boolean checkTextPreferences() {
		boolean editLocal = CoreHub.localCfg.get(ch.elexis.core.constants.Preferences.P_TEXT_EDIT_LOCAL, false);
		boolean externFile = ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false);

		if (editLocal && externFile) {
			return true;
		} else {
			StringBuilder missingOptionText = new StringBuilder();
			if (!editLocal && !externFile) {
				missingOptionText.append("Es sind aktuell beide Optionen nicht aktiviert.");
			} else {
				missingOptionText.append("Die Option [");
				if (!editLocal) {
					missingOptionText.append(ch.elexis.core.ui.preferences.Messages.Texterstellung_texteditlocaldesc);
				} else if (!externFile) {
					missingOptionText.append("Brief extern speichern");
				}
				missingOptionText.append("] ist aktuell nicht aktiviert.");
			}
			return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Warnung",
					"Bei Benutzung der Docx-Document Textausgabe wird empfohlen unter Einstellungen > Textverarbeitung folgende Optionen zu aktivieren.\n\n"
							+ "* " + ch.elexis.core.ui.preferences.Messages.Texterstellung_texteditlocaldesc
							+ StringUtils.LF + "* " + "Brief extern speichern" + "\n\n" + missingOptionText.toString()
							+ StringUtils.LF + "Wollen Sie trotzdem weiter machen?");
		}
	}

	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		boolean ret = super.loadFromStream(is, asTemplate);
		if (ret && openButton != null && !openButton.isDisposed()) {
			openButton.setEnabled(true);
		}
		return ret;
	}

	@Override
	public boolean clear() {
		// not implemented
		return false;
	}

	@Override
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		if (getCurrentDocument() != null) {
			// check if script initialization for windows should be performed
			if (CoreUtil.isWindows() && ConfigServiceHolder.getGlobal(USE_PRINT_SCRIPT, false)
					&& !isScriptWinInitialized()) {
				initializeScriptWin();
			}
			if (printSettings != null) {
				toPrinter = printSettings.getPrinter() == null ? toPrinter : printSettings.getPrinter();
				toTray = printSettings.getTray() == null ? toTray : printSettings.getTray();
			}
			String printCommand = null;
			if (toPrinter != null && !toPrinter.isEmpty()) {
				printCommand = getPrintPrinterCommand();
			} else {
				printCommand = getPrintNoPrinterCommand();
			}
			if (printCommand != null) {
				Optional<File> tempFile = getCurrentDocumentTempFile();
				if (tempFile.isPresent()) {
					PrintProcess process = new PrintProcess(printCommand);
					process.setPrinter(toPrinter);
					process.setTray(toTray);
					process.setFilename(tempFile.get().getAbsolutePath());
					return process.execute();
				}
			} else {
				// just save and open in registered application
				openCurrentDocument();
			}
			return true;
		}
		return false;
	}

	@Override
	public void initTemplatePrintSettings(String template) {
		printSettings = new TextTemplatePrintSettings(template, getMimeType());
	}

	private String getPrintNoPrinterCommand() {
		if (CoreUtil.isWindows() && ConfigServiceHolder.getGlobal(USE_PRINT_SCRIPT, false)) {
			Properties commandsProperties = ScriptInitializer
					.getPrintCommands("/rsc/script/win/printcommands.properties");
			if (commandsProperties != null && commandsProperties.get("noprinter") != null) {
				return (String) commandsProperties.get("noprinter");
			}
		}
		return CoreHub.localCfg.get(PRINT_COMMAND_PREF, null);
	}

	private String getPrintPrinterCommand() {
		if (CoreUtil.isWindows() && ConfigServiceHolder.getGlobal(USE_PRINT_SCRIPT, false)) {
			Properties commandsProperties = ScriptInitializer
					.getPrintCommands("/rsc/script/win/printcommands.properties");
			if (commandsProperties != null && commandsProperties.get("printer") != null) {
				return (String) commandsProperties.get("printer");
			}
		}
		return CoreHub.localCfg.get(PRINTTOPRINTER_COMMAND_PREF, null);
	}

	private void openCurrentDocument() {
		if (getCurrentDocument() != null) {
			Optional<File> tempFile = getCurrentDocumentTempFile();
			tempFile.ifPresent(f -> {
				LoggerFactory.getLogger(getClass()).debug("Open temporary document from [" + f.getAbsolutePath() + "]");
				Program.launch(f.getAbsolutePath());
			});
		}
	}

	private Optional<File> getCurrentDocumentTempFile() {
		try {
			File tempFile = File.createTempFile("dtp_", "_" + System.currentTimeMillis() + ".docx");
			tempFile.deleteOnExit();
			Docx4J.save((WordprocessingMLPackage) getCurrentDocument(), tempFile, Docx4J.FLAG_SAVE_ZIP_FILE);
			return Optional.of(tempFile);
		} catch (IOException | Docx4JException e) {
			LoggerFactory.getLogger(getClass()).error("Error saving docx temp file", e);
			return Optional.empty();
		}
	}

	@Override
	public boolean isDirectOutput() {
		return false;
	}

	@Override
	public Composite createContainer(Composite parent, ICallback handler) {
		if (composite == null) {
			composite = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout(SWT.VERTICAL);
			layout.wrap = true;
			layout.fill = false;
			layout.justify = false;
			layout.marginBottom = 9;
			composite.setLayout(layout);

			RowData data = new RowData();
			Label label = new Label(composite, SWT.PUSH);
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			label.setText("docx Dokumente Generator ohne Editor\n");
			label.setLayoutData(data);
			data.width = 400;
			openButton = new Button(composite, SWT.PUSH);
			openButton.setText("Dokument öffnen");
			openButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					openCurrentDocument();
				}
			});
			data = new RowData();
			openButton.setLayoutData(data);
			openButton.setEnabled(false);

			composite.pack();
		}
		return composite;
	}



	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// ignore
	}

	private boolean isScriptWinInitialized() {
		ScriptInitializer initializer = new ScriptInitializer("/rsc/script/win/doc2sumatraprint.ps1");
		if (!initializer.existsInScriptFolder()) {
			return false;
		}
		if (!initializer.matchingFileSize()) {
			return false;
		}
		initializer = new ScriptInitializer("/rsc/script/win/SumatraPDF.exe");
		if (!initializer.existsInScriptFolder()) {
			return false;
		}
		if (!initializer.matchingFileSize()) {
			return false;
		}
		return true;
	}

	private void initializeScriptWin() {
		ScriptInitializer initializer = new ScriptInitializer("/rsc/script/win/doc2sumatraprint.ps1");
		if (initializer.existsInScriptFolder()) {
			if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Script existiert bereits", "Script ["
					+ initializer.getFilename() + "] existiert bereits, soll die Datei überschrieben werden?")) {
				initializer.init();
			}
		} else {
			initializer.init();
		}
		initializer = new ScriptInitializer("/rsc/script/win/SumatraPDF.exe");
		if (initializer.existsInScriptFolder()) {
			if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Script existiert bereits", "Script ["
					+ initializer.getFilename() + "] existiert bereits, soll die Datei überschrieben werden?")) {
				initializer.init();
			}
		} else {
			initializer.init();
		}
	}

	// public boolean insertImage(String text, InputStream image){
	// if (currentDocument != null) {
	// FindTextVisitor visitor = new FindTextVisitor(text);
	// TraversalUtil.visit(currentDocument.getMainDocumentPart(), visitor);
	// List<Text> found = visitor.getFound();
	// if (!found.isEmpty()) {
	// try {
	// for (Text foundText : found) {
	// foundText.setValue(StringUtils.EMPTY);
	// R r = (R) foundText.getParent();
	// ImageUtil.insertImage(r, image, currentDocument);
	// }
	// return true;
	// } catch (Exception e) {
	// LoggerFactory.getLogger(getClass()).error("Error inserting image", e);
	// }
	// }
	// }
	// return false;
	// }
}
