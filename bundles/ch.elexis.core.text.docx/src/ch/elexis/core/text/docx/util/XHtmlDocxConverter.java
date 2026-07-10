package ch.elexis.core.text.docx.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts CKEditor HTML into WordprocessingML using
 * {@code docx4j-ImportXHTML}. Uses Jsoup to normalize raw HTML into well-formed
 * XHTML prior to conversion.
 */
public class XHtmlDocxConverter {

	private static final Logger log = LoggerFactory.getLogger(XHtmlDocxConverter.class);

	private XHtmlDocxConverter() {
		// utility
	}

	/**
	 * Unquoted style attribute up to the closing {@code >}, e.g.
	 * {@code style=color:#000; font-size:16px}. Without quotes the HTML tokenizer
	 * splits the value at whitespace, turning css declarations into attributes
	 * whose names are not valid XML names ({@code font-size:16px=""}), which makes
	 * the importer's XML parser fail.
	 */
	private static final Pattern UNQUOTED_STYLE = Pattern.compile("(?i)\\bstyle\\s*=\\s*(?![\"'])([^>]*?)\\s*(?=/?>)");

	/** Attribute name that is safe in a namespace unaware XML document. */
	private static final Pattern XML_ATTR_NAME = Pattern.compile("[A-Za-z_][\\w.-]*");

	/**
	 * Quotes unquoted style attribute values (up to the closing {@code >}), so
	 * multi-property values like {@code style=color:#000; font-size:16px} stay one
	 * attribute instead of being split into invalid ones by the HTML tokenizer.
	 */
	public static String quoteUnquotedStyles(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		return UNQUOTED_STYLE.matcher(html).replaceAll("style=\"$1\"");
	}

	/**
	 * Normalizes raw HTML into well-formed XHTML required by the importer.
	 *
	 * @param html the raw HTML markup to normalize
	 * @return well-formed XHTML string
	 */
	public static String normalizeToXhtml(String html) {
		String raw = html != null ? html : "";
		raw = raw.replace("&quot;", "\"").replace("&apos;", "'");
		raw = quoteUnquotedStyles(raw);
		Document doc = Jsoup.parse(raw);
		doc.outputSettings() //
				.syntax(Syntax.xml) //
				.escapeMode(EscapeMode.xhtml) //
				.charset(StandardCharsets.UTF_8) //
				.prettyPrint(false);
		removeInvalidAttributes(doc);
		return doc.html();
	}

	/**
	 * Safety net: drops attributes whose names are not valid XML names, so the
	 * importer can never fail on them. Names that look like css declarations
	 * (leftovers of an unquoted style value) are merged back into the style
	 * attribute instead.
	 */
	static void removeInvalidAttributes(Document doc) {
		for (Element element : doc.getAllElements()) {
			List<Attribute> invalid = null;
			for (Attribute attribute : element.attributes()) {
				if (!XML_ATTR_NAME.matcher(attribute.getKey()).matches()) {
					if (invalid == null) {
						invalid = new ArrayList<>();
					}
					invalid.add(attribute);
				}
			}
			if (invalid != null) {
				StringBuilder style = new StringBuilder(element.attr("style"));
				for (Attribute attribute : invalid) {
					if (attribute.getKey().contains(":")) {
						if (style.length() > 0 && !style.toString().trim().endsWith(";")) {
							style.append(';');
						}
						style.append(attribute.getKey()).append(attribute.getValue());
					}
					element.removeAttr(attribute.getKey());
				}
				if (style.length() > 0) {
					element.attr("style", style.toString());
				}
			}
			cleanStyleAttribute(element);
		}
	}

	/**
	 * Keeps only complete {@code name:value} declarations in the element's style
	 * attribute. Incomplete leftovers of broken markup (e.g. {@code color:} without
	 * a value, or empty segments) crash the Nebula RichTextPainter, which blindly
	 * does {@code split(":")[1]} per declaration.
	 */
	private static void cleanStyleAttribute(Element element) {
		String style = element.attr("style");
		if (style.isEmpty()) {
			return;
		}
		StringBuilder cleaned = new StringBuilder();
		for (String declaration : style.split(";")) {
			int colon = declaration.indexOf(':');
			if (colon <= 0) {
				continue;
			}
			String name = declaration.substring(0, colon).trim();
			String value = declaration.substring(colon + 1).trim();
			if (name.isEmpty() || value.isEmpty()) {
				continue;
			}
			if (cleaned.length() > 0) {
				cleaned.append(';');
			}
			cleaned.append(name).append(':').append(value);
		}
		if (cleaned.length() > 0) {
			element.attr("style", cleaned.toString());
		} else {
			element.removeAttr("style");
		}
	}

	/**
	 * Converts HTML markup into WordprocessingML content elements.
	 *
	 * @param pkg  the target document package providing style context
	 * @param html the raw HTML markup to convert
	 * @return list of converted WordprocessingML objects
	 * @throws Docx4JException if the import conversion fails
	 */
	public static List<Object> convert(WordprocessingMLPackage pkg, String html) throws Docx4JException {
		XHTMLImporterImpl importer = new XHTMLImporterImpl(pkg);
		String xhtml = normalizeToXhtml(html);
		return importer.convert(xhtml, null);
	}

	/**
	 * Converts HTML directly into a standalone {@code .docx} file.
	 *
	 * @param html   the raw HTML markup to convert
	 * @param target the target file to save the document to
	 */
	public static void htmlToDocxFile(String html, File target) {
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			pkg.getMainDocumentPart().getContent().addAll(convert(pkg, html));
			pkg.save(target);
		} catch (Exception e) {
			log.error("Failed to convert HTML to standalone docx file", e);
		}
	}
}
