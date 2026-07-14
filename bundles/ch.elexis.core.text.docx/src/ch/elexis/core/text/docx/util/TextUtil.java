package ch.elexis.core.text.docx.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.LoggerFactory;

import ch.elexis.core.text.RichTextMarker;

public class TextUtil {

	static ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	/**
	 * Insert text after the cursor as new runs (splitting on {@code \n}, handling tabs).
	 *
	 * @param cursor
	 * @param text
	 * @param align
	 * @param styleInfo
	 * @return
	 */
	public static Object insertText(Object cursor, String text, int align, StyleInfo styleInfo) {
		if (cursor instanceof R) {
			return addText(((R) cursor), text, styleInfo);
		} else if (cursor instanceof Text) {
			return insertText(((Text) cursor).getParent(), text, align, styleInfo);
		}
		throw new IllegalStateException("Unknown cursor type [" + cursor + "]");
	}

	/**
	 * Convenience {@link #replaceText(Object, String, int, StyleInfo, WordprocessingMLPackage)} without a package.
	 *
	 * @param cursor
	 * @param text
	 * @param align
	 * @param styleInfo
	 * @return
	 */
	public static Object replaceText(Object cursor, String text, int align, StyleInfo styleInfo) {
		return replaceText(cursor, text, align, styleInfo, null);
	}

	/**
	 * Replace the cursor text. Rich text (HTML markup explicitly marked with
	 * {@link RichTextMarker}) is rendered via ImportXHTML when {@code pkg} is
	 * given; all unmarked text always takes the standard plain text path.
	 *
	 * @param cursor
	 * @param text
	 * @param align
	 * @param styleInfo
	 * @param pkg
	 * @return
	 */
	public static Object replaceText(Object cursor, String text, int align, StyleInfo styleInfo,
			WordprocessingMLPackage pkg) {
		if (cursor instanceof R) {
			return replaceText(((R) cursor), text, styleInfo, pkg);
		} else if (cursor instanceof Text) {
			return replaceText(((Text) cursor).getParent(), text, align, styleInfo, pkg);
		}
		throw new IllegalStateException("Unknown cursor type [" + cursor + "]");
	}

	/**
	 * @param cursor
	 * @param text
	 * @param styleInfo
	 * @param pkg
	 * @return
	 */
	private static Object replaceText(R cursor, String text, StyleInfo styleInfo, WordprocessingMLPackage pkg) {
		if (RichTextMarker.isMarked(text)) {
			if (pkg != null) {
				return renderWithImportXhtml(cursor, RichTextMarker.unwrap(text), pkg);
			}
			// no package to render into: insert the markup as plain text
			text = RichTextMarker.unwrap(text);
		}
		R ret = null;
		boolean cursorInTbl = DocxUtil.getParentTbl(cursor) != null;
		List<String> lines = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < lines.size(); i++) {
			if (i == 0) {
				ret = insertLine(cursor, lines.get(i), styleInfo);
			} else if (cursorInTbl) {
				P p = DocxUtil.appendParagraph(cursor);
				R r = DocxUtil.getOrCreateRun(p);
				r.setRPr(cursor.getRPr());
				insertLine(r, lines.get(i), styleInfo);
				cursor = r;
			} else {
				appendLine(cursor, lines.get(i), styleInfo);
			}
		}
		return ret;
	}

	/**
	 * @param cursor
	 * @param text
	 * @param styleInfo
	 * @return
	 */
	private static R addText(R cursor, String text, StyleInfo styleInfo) {
		R ret = null;
		List<String> lines = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < lines.size(); i++) {
			if (i == 0) {
				ret = DocxUtil.appendRun(cursor);
				insertLine(ret, lines.get(i), styleInfo);
				cursor = ret;
			} else {
				appendLine(cursor, lines.get(i), styleInfo);
			}
		}
		return ret;
	}

	/**
	 * @param cursor
	 * @param line
	 * @param styleInfo
	 * @return
	 */
	private static R insertLine(R cursor, String line, StyleInfo styleInfo) {
		applyStyleInfo(cursor, styleInfo);
		writeTabbedLine(cursor, DocxUtil.getOrCreateText(cursor), line);
		return cursor;
	}

	private static R appendLine(R cursor, String line, StyleInfo styleInfo) {
		applyStyleInfo(cursor, styleInfo);
		writeTabbedLine(cursor, DocxUtil.getNewLineText(cursor), line);
		return cursor;
	}

	/** Writes the line into the given text element, turning {@code \t} into tab elements. */
	private static void writeTabbedLine(R cursor, Text text, String line) {
		List<String> tabsSplit = Arrays.asList(line.split("\t", -1));
		for (int i = 0; i < tabsSplit.size(); i++) {
			if (i > 0) {
				text = DocxUtil.appendTabbedText(cursor);
			}
			text.setValue(tabsSplit.get(i));
		}
	}

	private static boolean applyStyleInfo(R r, StyleInfo styleInfo) {
		if (styleInfo.isStyleSet()) {
			RPr rpr = wmlObjectFactory.createRPr();
			styleInfo.applyTo(rpr);
			r.setRPr(rpr);
			return true;
		}
		return false;
	}

	// --- CKEditor markup -> WordprocessingML via docx4j-ImportXHTML ------------------------------

	/**
	 * Converts CKEditor markup (nested lists, sub/superscript, inline formatting) to
	 * WordprocessingML via {@code docx4j-ImportXHTML} and splices the result in place of
	 * the cursor's placeholder paragraph. {@link #applyTemplateFont} adapts the font.
	 * There is no fallback: on error the placeholder is cleared.
	 */
	private static Object renderWithImportXhtml(R cursor, String html, WordprocessingMLPackage pkg) {
		P originalP = DocxUtil.getParentP(cursor);
		ContentAccessor parent = originalP != null ? DocxUtil.getParentContentAccessor(originalP) : null;
		if (originalP == null || parent == null) {
			return cursor;
		}
		RPr baseRPr = cursor.getRPr() != null ? (RPr) XmlUtils.deepCopy(cursor.getRPr()) : null;
		List<Object> converted;
		try {
			converted = XHtmlDocxConverter.convert(pkg, html);
		} catch (Exception e) {
			LoggerFactory.getLogger(TextUtil.class).error("ImportXHTML conversion failed", e);
			return cursor;
		}
		if (converted == null || converted.isEmpty()) {
			DocxUtil.getOrCreateText(cursor).setValue(StringUtils.EMPTY);
			return cursor;
		}
		applyTemplateFont(converted, baseRPr);
		for (Object block : converted) {
			if (block instanceof Child) {
				((Child) block).setParent(parent);
			}
		}
		List<Object> content = parent.getContent();
		int index = content.indexOf(originalP);
		if (index < 0) {
			content.addAll(converted);
		} else {
			content.addAll(index + 1, converted);
			content.remove(originalP);
		}
		return converted.get(0);
	}

	/**
	 * Adapts ImportXHTML output to the template: overrides the font family on every run
	 * (CKEditor sets none, so ImportXHTML's default is never intentional) and snaps the
	 * body baseline size (the most frequent run size) to the template size, while keeping
	 * deliberately different sizes and all character formatting (bold, colour, ...).
	 */
	private static void applyTemplateFont(List<Object> converted, RPr baseRPr) {
		if (baseRPr == null) {
			return;
		}
		List<R> runs = new ArrayList<>();
		for (Object block : converted) {
			collectRuns(block, runs);
		}
		if (runs.isEmpty()) {
			return;
		}
		BigInteger baselineSize = modalRunSize(runs);
		RFonts templateFonts = baseRPr.getRFonts();
		HpsMeasure templateSz = baseRPr.getSz();
		HpsMeasure templateSzCs = baseRPr.getSzCs();
		for (R run : runs) {
			RPr rpr = run.getRPr();
			if (rpr == null) {
				rpr = wmlObjectFactory.createRPr();
				run.setRPr(rpr);
			}
			if (templateFonts != null) {
				rpr.setRFonts((RFonts) XmlUtils.deepCopy(templateFonts));
			}
			if (templateSz != null) {
				HpsMeasure currentSz = rpr.getSz();
				boolean isBaseline = currentSz == null
						|| (baselineSize != null && baselineSize.equals(currentSz.getVal()));
				if (isBaseline) {
					rpr.setSz((HpsMeasure) XmlUtils.deepCopy(templateSz));
					if (templateSzCs != null) {
						rpr.setSzCs((HpsMeasure) XmlUtils.deepCopy(templateSzCs));
					}
				}
			}
		}
	}

	private static void collectRuns(Object node, List<R> runs) {
		Object value = XmlUtils.unwrap(node);
		if (value instanceof R) {
			runs.add((R) value);
		}
		if (value instanceof ContentAccessor) {
			for (Object child : ((ContentAccessor) value).getContent()) {
				collectRuns(child, runs);
			}
		}
	}

	/** The most frequent explicit run size among the runs, or {@code null} if none set one. */
	private static BigInteger modalRunSize(List<R> runs) {
		Map<BigInteger, Integer> counts = new HashMap<>();
		for (R run : runs) {
			if (run.getRPr() != null && run.getRPr().getSz() != null && run.getRPr().getSz().getVal() != null) {
				counts.merge(run.getRPr().getSz().getVal(), 1, Integer::sum);
			}
		}
		BigInteger modal = null;
		int best = 0;
		for (Map.Entry<BigInteger, Integer> entry : counts.entrySet()) {
			if (entry.getValue() > best) {
				best = entry.getValue();
				modal = entry.getKey();
			}
		}
		return modal;
	}

	// --- Diagnose view: flatten lists for the Nebula rich text painter ---------------------------

	/**
	 * Converts the editor's block HTML into single {@code <br/>}-separated lines for the Nebula
	 * rich text painter, which cannot render block elements. CKEditor's {@code getData()} puts a
	 * {@code <p>...</p>} per paragraph and pretty-prints them with {@code \n\n} in between; a naive
	 * newline-to-{@code <br/>} conversion would turn that into {@code <br/><br/>} and show a blank
	 * line between every paragraph. This drops the formatting newlines and turns each closing
	 * paragraph/div/heading into exactly one {@code <br/>}. Lists ({@code <ul>/<ol>}) are left
	 * intact for {@link #flattenHtmlLists}.
	 */
	public static String blocksToNebulaBreaks(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		String s = html;
		// each block closer becomes exactly one line break, opener is dropped
		s = s.replaceAll("(?i)</(?:p|div|h[1-6])>", "<br/>");
		s = s.replaceAll("(?i)<(?:p|div|h[1-6])(?:\\s[^>]*)?>", "");
		// drop pretty-print newlines/indentation that sit directly next to a tag (formatting only)
		s = s.replaceAll(">[ \\t]*[\\r\\n]+[ \\t]*", ">");
		s = s.replaceAll("[\\r\\n]+[ \\t]*<", "<");
		// any newline left is a real in-text line break
		s = s.replaceAll("[\\r\\n]+", "<br/>");
		// collapse stacked breaks and trim leading/trailing ones
		s = s.replaceAll("(?i)(?:<br\\s*/?>\\s*){2,}", "<br/>");
		s = s.replaceAll("(?i)^(?:<br\\s*/?>)+", "");
		s = s.replaceAll("(?i)(?:<br\\s*/?>)+$", "");
		return s;
	}

	/**
	 * Removes character-formatting tags (bold, italic, underline, strikethrough, coloured/font
	 * {@code <span>}, {@code <font>}) but keeps structure: lists ({@code <ul>/<ol>/<li>}), line
	 * breaks and paragraphs. Used for the plain (non-alternative) diagnose rendering so it only
	 * shows text, lists and numbering - mirroring the editor's plain display mode.
	 */
	public static String stripInlineFormatting(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		return html.replaceAll("(?i)</?(?:strong|b|em|i|u|s|strike|del|span|font)(?:\\s[^>]*)?>", "");
	}

	/** Prepares stored condition HTML for the Nebula rich text painter (decode quotes, flatten lists). */
	public static String sanitizeHtmlForNebula(String rawHtml) {
		if (rawHtml == null || rawHtml.isEmpty()) {
			return rawHtml;
		}
		String sanitized = rawHtml.replace("&quot;", "\"").replace("&apos;", "'");
		sanitized = XHtmlDocxConverter.quoteUnquotedStyles(sanitized);
		sanitized = dropInvalidAttributes(sanitized);
		sanitized = bulletizeDashLines(sanitized);
		return flattenHtmlLists(sanitized);
	}

	/**
	 * Safety net for broken legacy markup (e.g. {@code <span style="color:#000;"
	 * font-size:16px="">} persisted by earlier versions): drops attributes whose
	 * names are not valid XML names, so the Nebula painter's XML parser can never
	 * fail on them. Css-like leftovers are merged back into the style attribute.
	 */
	private static String dropInvalidAttributes(String html) {
		if (html == null || html.indexOf('<') < 0) {
			return html;
		}
		Document doc = Jsoup.parseBodyFragment(html);
		doc.outputSettings().prettyPrint(false).syntax(Syntax.xml).escapeMode(EscapeMode.xhtml);
		XHtmlDocxConverter.removeInvalidAttributes(doc);
		return doc.body().html();
	}

	/**
	 * Turns a line that starts with a "- " (dash + space) into a bullet line ("&#8226; "),
	 * so notes/text typed with a leading dash are shown with a real bullet - the same bullet
	 * the flattened {@code <ul>} lists use. Only matches the dash at the very start of a line -
	 * start of string, right after a {@code <br/>}, or at the start/end of a block element
	 * ({@code <p>}, {@code <div>}, {@code <li>}) - so ranges like "start - end" inside a line
	 * are kept untouched.
	 */
	private static String bulletizeDashLines(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		return html.replaceAll(
				"(?i)(^|<br\\s*/?>|<(?:p|div|li)(?:\\s[^>]*)?>|</(?:p|div|li)>)([ \\t\\u00A0]*)-[ \\t]",
				"$1$2&#8226; ");
	}

	/** Four non-breaking spaces per nesting level, so the Nebula painter keeps the indent. */
	private static final String LIST_INDENT = "&#160;&#160;&#160;&#160;";

	/**
	 * Flattens {@code <ul>}/{@code <ol>} into indented text lines for the Nebula painter
	 * (which has no list support): nested lists are indented, ordered lists numbered
	 * continuously, empty items dropped; inline markup is kept.
	 */
	private static String flattenHtmlLists(String html) {
		if (html == null) {
			return null;
		}
		String lower = html.toLowerCase();
		if (!lower.contains("<ul") && !lower.contains("<ol")) {
			return html;
		}
		Document doc = Jsoup.parseBodyFragment(html);
		doc.outputSettings().prettyPrint(false).syntax(Syntax.xml).escapeMode(EscapeMode.xhtml);
		StringBuilder sb = new StringBuilder();
		for (Node node : doc.body().childNodes()) {
			if (node instanceof Element && isListElement((Element) node)) {
				renderList((Element) node, sb, 0);
			} else {
				sb.append(node.outerHtml());
			}
		}
		return sb.toString();
	}

	private static boolean isListElement(Element element) {
		return "ul".equalsIgnoreCase(element.tagName()) || "ol".equalsIgnoreCase(element.tagName());
	}

	/** Renders a list into indented text lines; indentation follows the nesting depth only. */
	private static void renderList(Element list, StringBuilder sb, int parentIndent) {
		int indent = parentIndent + 1;
		boolean ordered = "ol".equalsIgnoreCase(list.tagName());
		int counter = 1;
		for (Element li : list.children()) {
			if (!"li".equalsIgnoreCase(li.tagName())) {
				continue;
			}
			Element clone = li.clone();
			clone.children().stream().filter(TextUtil::isListElement).forEach(Element::remove);
			String itemText = clone.html().trim();
			if (!itemText.isEmpty()) {
				sb.append(LIST_INDENT.repeat(indent)) //
						.append(ordered ? (counter + ". ") : "&#8226; ") //
						.append(itemText).append("<br/>");
				counter++;
			}
			for (Element child : li.children()) {
				if (isListElement(child)) {
					renderList(child, sb, indent);
				}
			}
		}
	}

	// --- run helpers used by RegexTextVisitor ----------------------------------------------------

	public static boolean isMultiTextRun(R cursor) {
		int textCount = 0;
		for (Object object : new ArrayList<>(cursor.getContent())) {
			if (XmlUtils.unwrap(object) instanceof Text && ++textCount > 1) {
				return true;
			}
		}
		return false;
	}

	public static void convertToSingleTextRun(R cursor) {
		while (isMultiTextRun(cursor)) {
			transferFirstText(cursor, DocxUtil.createRunBefore(cursor));
		}
	}

	private static void transferFirstText(R from, R to) {
		for (Object object : new ArrayList<>(from.getContent())) {
			Object value = XmlUtils.unwrap(object);
			if (value instanceof Text) {
				to.getContent().add(value);
				((Text) value).setParent(to);
				from.getContent().remove(object);
				break;
			} else if (value instanceof Child) {
				to.getContent().add(value);
				((Child) value).setParent(to);
				from.getContent().remove(object);
			}
		}
	}
}
