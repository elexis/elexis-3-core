package at.medevit.elexis.text.docx;

import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
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
import at.medevit.elexis.text.docx.stax.TextFindStAXHandler;
import at.medevit.elexis.text.docx.util.DocxUtil;
import at.medevit.elexis.text.docx.util.FindTextVisitor;
import at.medevit.elexis.text.docx.util.RegexTextVisitor;
import at.medevit.elexis.text.docx.util.StyleInfo;
import at.medevit.elexis.text.docx.util.TableUtil;
import at.medevit.elexis.text.docx.util.TextBoxUtil;
import at.medevit.elexis.text.docx.util.TextUtil;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.core.ui.views.textsystem.TextTemplatePrintSettings;
import ch.elexis.core.utils.CoreUtil;

public class DocxTextPlugin implements ITextPlugin {

	private static final String DOCX_PREF = "textplugins/docx/";

	public static final String PRINT_PROCESS_TIMEOUT = DOCX_PREF + "printtimeout";

	public static final String PRINT_COMMAND_PREF = DOCX_PREF + "printcommand";
	public static final String PRINTTOPRINTER_COMMAND_PREF = DOCX_PREF + "printtoprintercommand";

	public static final String USE_PRINT_SCRIPT = DOCX_PREF + "printcommand";

	private Parameter parameter;
	private static TextTemplatePrintSettings printSettings;

	private PageFormat format = ITextPlugin.PageFormat.USER;
	private WordprocessingMLPackage currentDocument;

	private Composite composite;
	private Button openButton;

	private StyleInfo currentStyleInfo;

	public DocxTextPlugin() {
		currentStyleInfo = new StyleInfo();
	}

	@Override
	public PageFormat getFormat() {
		return format;
	}

	@Override
	public void setFormat(PageFormat f) {
		format = f;
	}

	@Override
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

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
		try {
			currentDocument = WordprocessingMLPackage.createPackage();
			if (openButton != null && !openButton.isDisposed()) {
				openButton.setEnabled(true);
			}
		} catch (InvalidFormatException e) {
			LoggerFactory.getLogger(getClass()).error("Erro creating document", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		if (checkTextPreferences()) {
			try {
				currentDocument = WordprocessingMLPackage.load(new ByteArrayInputStream(bs));
				if (openButton != null && !openButton.isDisposed()) {
					openButton.setEnabled(true);
				}
			} catch (Docx4JException e) {
				LoggerFactory.getLogger(getClass())
						.error("Error loading from byte array [" + bs + "] size [" + bs.length + "]");
				return false;
			}
			return true;
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
		try {
			currentDocument = WordprocessingMLPackage.load(is);
			if (openButton != null && !openButton.isDisposed()) {
				openButton.setEnabled(true);
			}
		} catch (Docx4JException e) {
			LoggerFactory.getLogger(getClass()).error("Error loading from stream [" + is + "]");
			return false;
		}
		return true;
	}

	@Override
	public byte[] storeToByteArray() {
		if (currentDocument != null) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				Docx4J.save(currentDocument, out, Docx4J.FLAG_SAVE_ZIP_FILE);
				return out.toByteArray();
			} catch (IOException | Docx4JException e) {
				LoggerFactory.getLogger(getClass()).error("Error writing to byte array");
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean insertTable(String text, int properties, String[][] contents, int[] columnSizes) {
		if (currentDocument != null) {
			FindTextVisitor visitor = new FindTextVisitor(text);
			TraversalUtil.visit(currentDocument.getMainDocumentPart(), visitor);
			List<Text> found = visitor.getFound();
			if (!found.isEmpty()) {
				for (Text foundText : found) {
					foundText.setValue(StringUtils.EMPTY);
					R r = (R) foundText.getParent();
					// do not insert table with no content
					if (contents.length > 0) {
						Tbl table = TableUtil.insertTable(r, properties, contents, columnSizes,
								DocxUtil.getDocumentWidth(currentDocument), true);
						TableUtil.addBorders(table, 1);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public Object insertTextAt(int posx, int posy, int width, int height, String text, int align) {
		if (currentDocument != null) {
			return TextBoxUtil.createTextBox(currentDocument, posx - 3, posy, width, height, text, align,
					currentStyleInfo);
		}
		return null;
	}

	@Override
	public boolean setFont(String name, int style, float size) {
		currentStyleInfo.setFontName(name);
		currentStyleInfo.setFontStyle(style);
		currentStyleInfo.setFontSize(size);
		return true;
	}

	@Override
	public boolean setStyle(int style) {
		currentStyleInfo.setFontStyle(style);
		return true;
	}

	@Override
	public Object insertText(String marke, String text, int align) {
		if (currentDocument != null) {
			FindTextVisitor visitor = new FindTextVisitor(marke);
			TraversalUtil.visit(currentDocument.getMainDocumentPart(), visitor);
			List<Text> found = visitor.getFound();
			if (!found.isEmpty()) {
				Object ret = null;
				for (Text foundText : found) {
					foundText.setValue(StringUtils.EMPTY);
					R r = (R) foundText.getParent();
					ret = TextUtil.insertText(r, text, align, currentStyleInfo);
				}
				return ret;
			}
		}
		return null;
	}

	@Override
	public Object insertText(Object pos, String text, int align) {
		return TextUtil.insertText(pos, text, align, currentStyleInfo);
	}

	@Override
	public boolean clear() {
		// not implemented
		return false;
	}

	@Override
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		if (currentDocument != null) {
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
		if (currentDocument != null) {
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
			Docx4J.save(currentDocument, tempFile, Docx4J.FLAG_SAVE_ZIP_FILE);
			return Optional.of(tempFile);
		} catch (IOException | Docx4JException e) {
			LoggerFactory.getLogger(getClass()).error("Error saving docx temp file", e);
			return Optional.empty();
		}
	}

	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_MSWORD;
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
	public boolean findOrReplace(String pattern, ReplaceCallback callBack) {
		if (currentDocument != null) {
			prepare();

			MainDocumentPart documentPart = currentDocument.getMainDocumentPart();

			RegexTextVisitor visitor = new RegexTextVisitor(currentDocument, pattern);
			TraversalUtil.visit(documentPart, visitor);
			visitor.replaceMatchingTexts(callBack);

			// replace header and footer
			RelationshipsPart relationshipPart = documentPart.getRelationshipsPart();
			List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
			for (Relationship relationship : relationships) {
				if (relationship.getType().equals(Namespaces.HEADER)
						|| relationship.getType().equals(Namespaces.FOOTER)) {
					JaxbXmlPart part = (JaxbXmlPart) relationshipPart.getPart(relationship);
					RegexTextVisitor partVisitor = new RegexTextVisitor(currentDocument, pattern);
					TraversalUtil.visit(part, partVisitor);
					partVisitor.replaceMatchingTexts(callBack);
				}
			}
		}
		return false;
	}

	protected int findTextCount(String text) {
		if (currentDocument != null) {
			try {
				prepare();

				TextFindStAXHandler stAXHAndler = new TextFindStAXHandler(text);
				MainDocumentPart documentPart = currentDocument.getMainDocumentPart();
				// find header and footer
				RelationshipsPart relationshipPart = documentPart.getRelationshipsPart();
				List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
				for (Relationship relationship : relationships) {
					if (relationship.getType().equals(Namespaces.HEADER)
							|| relationship.getType().equals(Namespaces.FOOTER)) {
						JaxbXmlPart part = (JaxbXmlPart) relationshipPart.getPart(relationship);
						part.pipe(stAXHAndler);
					}
				}
				// find main document
				documentPart.pipe(stAXHAndler);
				return stAXHAndler.getCount();
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Error finding text [" + text + "]", e);
			}
		}
		return 0;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// ignore
	}

	public WordprocessingMLPackage getCurrentDocument() {
		return currentDocument;
	}

	protected void prepare() {
		if (currentDocument != null) {
			try {
				VariablePrepare.prepare(currentDocument);
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Error preparing document", e);
			}
		}
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
