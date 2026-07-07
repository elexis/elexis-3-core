package ch.elexis.core.text.docx.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
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
	 * Normalizes raw HTML into well-formed XHTML required by the importer.
	 *
	 * @param html the raw HTML markup to normalize
	 * @return well-formed XHTML string
	 */
	public static String normalizeToXhtml(String html) {
		String raw = html != null ? html : "";
		raw = raw.replace("&quot;", "\"").replace("&apos;", "'");
		Document doc = Jsoup.parse(raw);
		doc.outputSettings() //
				.syntax(Syntax.xml) //
				.escapeMode(EscapeMode.xhtml) //
				.charset(StandardCharsets.UTF_8) //
				.prettyPrint(false);
		return doc.html();
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
