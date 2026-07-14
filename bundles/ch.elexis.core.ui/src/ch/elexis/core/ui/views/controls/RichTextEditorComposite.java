/*******************************************************************************
 * Reusable rich text editor.
 *
 * Shows text WYSIWYG (real bold/italic/underline/strikethrough, font and
 * background colour, bullet and numbered lists), with a small toolbar. The model
 * representation is a plain string with inline markup: {@code <strong>},
 * {@code <em>}, {@code <u>}, {@code <s>}, {@code <span style="color:#RRGGBB">},
 * {@code <span style="background-color:#RRGGBB">} and {@code <ul>/<ol>/<li>} - the
 * same markup the docx text plugin and the diagnose view understand.
 *
 * Intended to be reused by DiagnoseListComposite, PersonalAnamnesisComposite,
 * FamilyAnamnesisComposite, RiskComposite, SocialAnamnesisComposite, ...
 ******************************************************************************/
package ch.elexis.core.ui.views.controls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.nebula.widgets.richtext.RichTextEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import com.equo.chromium.swt.Browser;
import com.equo.chromium.swt.BrowserFunction;

/**
 * Hosts a CKEditor instance inside an Equo Chromium browser widget. Reuses
 * resources from Nebula RichTextEditor while avoiding native IE limitations.
 */
public class RichTextEditorComposite extends Composite {

	private final Browser browser;
	private final List<ModifyListener> modifyListeners = new ArrayList<>();

	private static final String[] EXPECTED_BRIDGE_CALLBACKS = { "getAllOptions", "customizeToolbar", "focusIn",
			"focusOut", "keyPressed", "keyReleased", "textModified" };

	private static final String PARAGRAPH_SPACING = "2px";

	private static String customizationScript;

	private boolean editorLoaded;
	private String pendingText;

	public RichTextEditorComposite(Composite parent) {
		this(parent, SWT.NONE);
	}


	public RichTextEditorComposite(Composite parent, int style) {
		this(parent, style, false);
	}

	/**
	 * Creates a new rich text editor composite.
	 *
	 * @param parent       the parent composite
	 * @param style        the SWT style bits
	 * @param plainDisplay if {@code true} the toolbar is hidden and character formatting
	 *                     is not shown (still editable, structure preserved)
	 */
	public RichTextEditorComposite(Composite parent, int style, boolean plainDisplay) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		browser = new Browser(this, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.setJavascriptEnabled(true);

		registerBridge();

		addListener(SWT.Resize, e -> getDisplay().asyncExec(this::maximizeEditorHeight));

		browser.addProgressListener(new ProgressListener() {
			@Override
			public void changed(ProgressEvent event) {
				// nothing to do
			}

			@Override
			public void completed(ProgressEvent event) {
				if (editorLoaded) {
					return;
				}
				try {
					browser.execute("document.documentElement.style.visibility='hidden';");
					browser.execute("window.getAllOptions = function() { return " + editorOptionsJs() + "; };");
					browser.execute("window.customizeToolbar = function() {};");
					browser.execute("window.focusIn = window.focusIn || function() {};");
					browser.execute("window.focusOut = window.focusOut || function() {};");
					browser.execute("window.keyPressed = window.keyPressed || function() {};");
					browser.execute("window.keyReleased = window.keyReleased || function() {};");
					if (pendingText != null) {
						browser.execute("document.getElementById('editor').value = '"
								+ escape(prepareForEditor(pendingText)) + "';");
						pendingText = null;
					}

					verifyBridgeContract();

					browser.execute("window.elexisRichTextConfig = { paragraphSpacing: '" + PARAGRAPH_SPACING
							+ "', plainDisplay: " + plainDisplay + " };");
					browser.execute(getCustomizationScript());

					browser.evaluate("initEditor();");
					editorLoaded = true;
				} catch (Exception e) {
					LoggerFactory.getLogger(RichTextEditorComposite.class)
							.error("Could not initialize CKEditor", e);
				}
			}
		});

		String url = resolveTemplateUrl();
		if (url != null) {
			browser.setUrl(url);
		}
	}

	private static String resolveTemplateUrl() {
		URL url = RichTextEditor.class.getResource("resources/template.html");
		if (url == null) {
			LoggerFactory.getLogger(RichTextEditorComposite.class)
					.error("CKEditor template.html not found in nebula richtext bundle");
			return null;
		}
		try {
			return FileLocator.toFileURL(url).toString();
		} catch (IOException e) {
			LoggerFactory.getLogger(RichTextEditorComposite.class)
					.error("Could not resolve CKEditor template url", e);
			return null;
		}
	}

	private static final String CUSTOMIZATION_SCRIPT_PATH = "rsc/js/richtexteditor.js";

	private static synchronized String getCustomizationScript() {
		if (customizationScript == null) {
			URL entry = FrameworkUtil.getBundle(RichTextEditorComposite.class).getEntry(CUSTOMIZATION_SCRIPT_PATH);
			if (entry == null) {
				LoggerFactory.getLogger(RichTextEditorComposite.class)
						.error("{} not found in bundle", CUSTOMIZATION_SCRIPT_PATH);
				customizationScript = StringUtils.EMPTY;
				return customizationScript;
			}
			try (InputStream in = entry.openStream()) {
				customizationScript = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				LoggerFactory.getLogger(RichTextEditorComposite.class).error("Could not load {}",
						CUSTOMIZATION_SCRIPT_PATH, e);
				customizationScript = StringUtils.EMPTY;
			}
		}
		return customizationScript;
	}

	private void maximizeEditorHeight() {
		if (!editorLoaded || browser == null || browser.isDisposed()) {
			return;
		}
		try {
			browser.execute("if (typeof maximizeEditorHeight === 'function') { maximizeEditorHeight(); }");
		} catch (Exception e) {
			LoggerFactory.getLogger(RichTextEditorComposite.class).warn("Could not maximize editor height", e);
		}
	}

	private void verifyBridgeContract() {
		if (browser == null || browser.isDisposed()) {
			return;
		}
		try {
			StringBuilder js = new StringBuilder("var __missing=[];");
			for (String name : EXPECTED_BRIDGE_CALLBACKS) {
				js.append("if(typeof window.").append(name).append("!=='function'){__missing.push('").append(name)
						.append("');}");
			}
			js.append("return __missing.join(',');");
			Object result = browser.evaluate(js.toString());
			String missing = result != null ? result.toString() : "";
			if (!missing.isEmpty()) {
				LoggerFactory.getLogger(RichTextEditorComposite.class).warn(
						"CKEditor bridge contract incomplete - callbacks missing before initEditor(): {}.",
						missing);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(RichTextEditorComposite.class).warn("Could not verify CKEditor bridge contract", e);
		}
	}

	private void registerBridge() {
		new BrowserFunction(browser, "textModified") {
			@Override
			public Object function(Object[] arguments) {
				fireModified();
				return null;
			}
		};
	}

	private String editorOptionsJs() {
		String language = Locale.getDefault().getLanguage();
		if (language == null || language.isEmpty()) {
			language = "de";
		}
		String removeButtons = "Save,NewPage,Link,Unlink,Anchor,"
				+ "Form,Checkbox,Radio,TextField,Textarea,Select,Button,ImageButton,HiddenField,"
				+ "Image,Flash,Table,HorizontalRule,Smiley,SpecialChar,PageBreak,Iframe,CreateDiv,"
				+ "BidiLtr,BidiRtl,Language,Maximize,ShowBlocks,About,Blockquote,Styles,Templates,"
				+ "Paste,PasteText,PasteFromWord";
		return "[" + "'allowedContent', true, "
				+ "'removePlugins', 'scayt,wsc,contextmenu,liststyle,tabletools,tableselection,magicline', "
				+ "'disableNativeSpellChecker', true, " + "'contentsLanguage', '" + language + "', "
				+ "'removeButtons', '" + removeButtons + "'"
				+ "]";
	}

	public String getText() {
		if (!editorLoaded || browser == null || browser.isDisposed()) {
			return stripZeroWidth(pendingText);
		}
		try {
			Object result = browser.evaluate("return getText()");
			return result != null ? stripZeroWidth(result.toString()) : null;
		} catch (Exception e) {
			LoggerFactory.getLogger(RichTextEditorComposite.class).warn("Could not read editor text", e);
			return stripZeroWidth(pendingText);
		}
	}

	private static String stripZeroWidth(String text) {
		if (text == null) {
			return null;
		}
		return text.replaceAll("[\\u200B\\u200C\\u200D\\uFEFF]", "");
	}

	public void setText(String markup) {
		if (editorLoaded) {
			applyText(markup);
		} else {
			pendingText = markup;
		}
	}

	private void applyText(String markup) {
		if (browser == null || browser.isDisposed()) {
			return;
		}
		try {
			browser.evaluate("setText('" + escape(prepareForEditor(markup != null ? markup : "")) + "')");
		} catch (Exception e) {
			LoggerFactory.getLogger(RichTextEditorComposite.class).warn("Could not set editor text", e);
		}
	}

	public void setEditable(boolean editable) {
		if (editorLoaded && browser != null && !browser.isDisposed()) {
			try {
				browser.evaluate("setReadOnly(" + !editable + ")");
			} catch (Exception e) {
				LoggerFactory.getLogger(RichTextEditorComposite.class).warn("Could not change editable state", e);
			}
		}
	}

	public void addModifyListener(ModifyListener listener) {
		if (listener != null) {
			modifyListeners.add(listener);
		}
	}

	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	private void fireModified() {
		if (modifyListeners.isEmpty() || isDisposed()) {
			return;
		}
		Event event = new Event();
		event.widget = this;
		ModifyEvent modifyEvent = new ModifyEvent(event);
		for (ModifyListener listener : new ArrayList<>(modifyListeners)) {
			listener.modifyText(modifyEvent);
		}
	}

	private static String prepareForEditor(String markup) {
		if (markup == null || markup.isEmpty()) {
			return markup;
		}
		String sanitized = markup.replace("&quot;", "\"").replace("&apos;", "'");
		// quote unquoted style values up to the closing '>', so multi-property
		// values (style=color:#000; font-size:16px) are not split by the parser
		sanitized = sanitized.replaceAll("(?i)\\bstyle\\s*=\\s*(?![\"'])([^>]*?)\\s*(?=/?>)", "style=\"$1\"");
		return sanitized;
	}

	private static String escape(String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
	}
}
