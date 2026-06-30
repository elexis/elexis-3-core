package ch.elexis.core.text.docx.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Color;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STShd;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.jvnet.jaxb2_commons.ppp.Child;

import jakarta.xml.bind.JAXBElement;

public class TextUtil {

	static ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	/**
	 * Insert text after the cursor. Always creates new open xml elements for the
	 * text.
	 *
	 * @param cursor
	 * @param text
	 * @param align
	 * @param currentStyleInfo
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
	 * Replace the text at cursor. If the text contains \t or \n new open xml
	 * elements will be created.
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
	 * Text is replaces Text of cursor. If text contains \n new P and R with text
	 * are created.
	 *
	 * @param cursor
	 * @param text
	 * @param styleInfo
	 * @return
	 */
	private static Object replaceText(R cursor, String text, StyleInfo styleInfo, WordprocessingMLPackage pkg) {
		if (pkg != null && containsListMarkup(text)) {
			return renderWithLists(cursor, text, styleInfo, pkg);
		}

		R first = null;
		RPr baseRPr = cursor.getRPr() != null ? (RPr) XmlUtils.deepCopy(cursor.getRPr()) : null;

		boolean cursorInTbl = DocxUtil.getParentTbl(cursor) != null;
		List<String> newLineSplit = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < newLineSplit.size(); i++) {
			if (i == 0) {
				first = cursor;
				cursor = insertLine(cursor, newLineSplit.get(i), styleInfo, baseRPr);
			} else {
				if (cursorInTbl) {
					P p = DocxUtil.appendParagraph(cursor);
					R r = DocxUtil.getOrCreateRun(p);
					r.setRPr(cursor.getRPr());
					cursor = insertLine(r, newLineSplit.get(i), styleInfo, baseRPr);
				} else {
					cursor = appendLine(cursor, newLineSplit.get(i), styleInfo, baseRPr);
				}
			}
		}

		return first;
	}

	private static boolean containsListMarkup(String text) {
		if (text == null) {
			return false;
		}
		String lower = text.toLowerCase();
		return lower.contains("<ul") || lower.contains("<ol");
	}

	/**
	 * Render a replacement text that contains
	 * <ul>
	 * /
	 * <ol>
	 * /
	 * <li>markup. Text outside list blocks is rendered into paragraphs as usual
	 * (inline character markup applies); list items become real Word list
	 * paragraphs.
	 */
	private static Object renderWithLists(R cursor, String text, StyleInfo styleInfo, WordprocessingMLPackage pkg) {
		RPr baseRunRPr = cursor.getRPr() != null ? (RPr) XmlUtils.deepCopy(cursor.getRPr()) : null;
		P originalP = DocxUtil.getParentP(cursor);
		PPr templatePPr = (originalP != null && originalP.getPPr() != null) ? (PPr) XmlUtils.deepCopy(originalP.getPPr())
				: null;

		writeText(DocxUtil.getOrCreateText(cursor), StringUtils.EMPTY);

		List<Block> blocks = parseBlocks(text);
		boolean firstRendered = false;
		P lastP = originalP;
		R lastR = cursor;
		ListUtil.ListNumbering numbering = null;

		for (Block block : blocks) {
			if (block.list) {
				if (block.items.isEmpty()) {
					continue;
				}
				if (numbering == null) {
					numbering = ListUtil.ensure(pkg);
				}
				if (numbering == null) {
					StringBuilder fallback = new StringBuilder();
					for (String item : block.items) {
						fallback.append("•\t").append(item).append(StringUtils.LF);
					}
					if (!firstRendered) {
						lastR = renderLines(cursor, fallback.toString(), styleInfo);
						firstRendered = true;
					} else {
						R r = ListUtil.createRun(ListUtil.createPlainParagraph(lastP, templatePPr), baseRunRPr);
						lastR = renderLines(r, fallback.toString(), styleInfo);
					}
					lastP = DocxUtil.getParentP(lastR);
					continue;
				}

				long numId = block.ordered ? ListUtil.newDecimalNum(numbering) : numbering.getBulletNumId();
				if (block.ordered && pkg != null) {
					try {
						org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp = pkg
								.getMainDocumentPart().getNumberingDefinitionsPart();
						if (ndp != null) {
							numId = ndp.restart(numId, 0, 1);
						}
					} catch (Exception e) {
						// Fehler beim Neustarten ignorieren (Rückfall auf fortlaufende Nummerierung)
					}
				}

				int startIndex = 0;
				if (!firstRendered) {
					ListUtil.applyNumbering(originalP, numId);
					buildLineRuns(cursor, block.items.get(0), true, baseRunRPr);
					lastP = originalP;
					lastR = cursor;
					startIndex = 1;
					firstRendered = true;
				}
				for (int i = startIndex; i < block.items.size(); i++) {
					P p = ListUtil.createListParagraph(lastP, numId);
					R r = ListUtil.createRun(p, baseRunRPr);
					buildLineRuns(r, block.items.get(i), true, baseRunRPr);
					lastP = p;
					lastR = r;
				}
			} else {
				if (!firstRendered) {
					lastR = renderLines(cursor, block.text, styleInfo);
					lastP = DocxUtil.getParentP(lastR);
					firstRendered = true;
				} else {
					if (StringUtils.isBlank(block.text)) {
						continue;
					}
					R r = ListUtil.createRun(ListUtil.createPlainParagraph(lastP, templatePPr), baseRunRPr);
					lastR = renderLines(r, block.text, styleInfo);
					lastP = DocxUtil.getParentP(lastR);
				}
			}
		}

		return cursor;
	}
	/**
	 * Render text (possibly multi-line) into the given anchor run / its paragraph,
	 * using {@link #insertLine}/{@link #appendLine}. No table handling.
	 */
	private static R renderLines(R cursor, String text, StyleInfo styleInfo) {
		RPr baseRPr = cursor.getRPr() != null ? (RPr) XmlUtils.deepCopy(cursor.getRPr()) : null;
		R current = cursor;
		List<String> newLineSplit = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < newLineSplit.size(); i++) {
			if (i == 0) {
				current = insertLine(cursor, newLineSplit.get(i), styleInfo, baseRPr);
			} else {
				current = appendLine(current, newLineSplit.get(i), styleInfo, baseRPr);
			}
		}
		return current;
	}

	private static final Pattern LIST_BLOCK = Pattern.compile("<(ul|ol)\\s*>(.*?)</\\1\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern LIST_ITEM = Pattern.compile("<li\\s*>(.*?)</li\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static List<Block> parseBlocks(String text) {
		List<Block> blocks = new ArrayList<>();
		Matcher m = LIST_BLOCK.matcher(text);
		int last = 0;
		while (m.find()) {
			if (m.start() > last) {
				blocks.add(Block.text(text.substring(last, m.start())));
			}
			boolean ordered = "ol".equalsIgnoreCase(m.group(1));
			List<String> items = new ArrayList<>();
			Matcher im = LIST_ITEM.matcher(m.group(2));
			while (im.find()) {
				items.add(im.group(1).trim());
			}
			blocks.add(Block.list(ordered, items));
			last = m.end();
		}
		if (last < text.length()) {
			blocks.add(Block.text(text.substring(last)));
		}
		return blocks;
	}

	private static final class Block {
		final boolean list;
		final boolean ordered;
		final String text;
		final List<String> items;

		private Block(boolean list, boolean ordered, String text, List<String> items) {
			this.list = list;
			this.ordered = ordered;
			this.text = text;
			this.items = items;
		}

		static Block text(String text) {
			return new Block(false, false, text, null);
		}

		static Block list(boolean ordered, List<String> items) {
			return new Block(true, ordered, null, items);
		}
	}

	/**
	 * A new R is appended to cursor and text is inserted in the new R. If text
	 * contains \n new P and R with text are created.
	 *
	 * @param cursor
	 * @param text
	 * @param styleInfo
	 * @return
	 */
	private static R addText(R cursor, String text, StyleInfo styleInfo) {
		R ret = null;
		RPr baseRPr = null;

		List<String> newLineSplit = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < newLineSplit.size(); i++) {
			if (i == 0) {
				ret = DocxUtil.appendRun(cursor);
				baseRPr = ret.getRPr() != null ? (RPr) XmlUtils.deepCopy(ret.getRPr()) : null;
				cursor = insertLine(ret, newLineSplit.get(i), styleInfo, baseRPr);
			} else {
				cursor = appendLine(cursor, newLineSplit.get(i), styleInfo, baseRPr);
			}
		}

		return ret;
	}

	/**
	 * Text of the line will be inserted into cursor. Only \t are allowed and
	 * handled. Inline bold markup ({@code <b>}/{@code <strong>}) is interpreted and
	 * produces additional bold runs.
	 *
	 * @param cursor
	 * @param line
	 * @param styleInfo
	 * @return the last run that received content
	 */
	private static R insertLine(R cursor, String line, StyleInfo styleInfo, RPr baseRPr) {
		applyStyleInfo(cursor, styleInfo);
		return buildLineRuns(cursor, line, true, baseRPr);
	}

	/**
	 * Append a line break after cursor and write the line content into new runs.
	 * Inline bold markup is interpreted.
	 *
	 * @param cursor
	 * @param line
	 * @param styleInfo
	 * @return the last run that received content
	 */
	private static R appendLine(R cursor, String line, StyleInfo styleInfo, RPr baseRPr) {
		applyStyleInfo(cursor, styleInfo);
		DocxUtil.appendNewLine(cursor);
		return buildLineRuns(cursor, line, false, baseRPr);
	}

	private static final Pattern STYLE_TAG = Pattern.compile(
			"</?(b|strong|i|em|u|s|strike|del)\\s*/?>"
					+ "|<span\\s+style\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))\\s*>" + "|</span\\s*>",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Build the runs for a single line (no \n). The line is split into segments at
	 * inline markup boundaries ({@code <b>/<strong>}, {@code <i>/<em>}, {@code <u>},
	 * {@code <s>/<strike>/<del>}); each style change starts a new run carrying a
	 * copy of the anchor run style with the active formats applied. {@code \t}
	 * becomes a tab element.
	 *
	 * @param anchor      run to start from. If {@code reuseAnchor} is true the
	 *                    anchor's existing text element is reused for the first
	 *                    text segment, otherwise a new run is appended after anchor
	 * @param line        the line text, may contain markup and tabs
	 * @param reuseAnchor whether the anchor run may be reused for content
	 * @return the last run that received content
	 */
	private static R buildLineRuns(R anchor, String line, boolean reuseAnchor, RPr baseRPr) {
		List<Part> parts = parseParts(line);

		R current = anchor;
		Style currentStyle = null;
		boolean anchorTextSlotFree = reuseAnchor;

		for (Part part : parts) {
			boolean onAnchor = (current == anchor) && reuseAnchor && (currentStyle == null);
			if (onAnchor) {
				currentStyle = part.style;
				if (!part.style.isPlain()) {
					applyRunStyle(current, null, part.style, true);
				}
			} else if (currentStyle == null || !currentStyle.matches(part.style)) {
				current = DocxUtil.appendRun(current);
				applyRunStyle(current, baseRPr, part.style, false);
				currentStyle = part.style;
			}

			if (part.tab) {
				if (current == anchor && anchorTextSlotFree) {
					writeText(DocxUtil.getOrCreateText(current), StringUtils.EMPTY);
					anchorTextSlotFree = false;
				}
				DocxUtil.appendTab(current);
			} else {
				if (current == anchor && anchorTextSlotFree) {
					writeText(DocxUtil.getOrCreateText(current), part.text);
					anchorTextSlotFree = false;
				} else {
					writeText(DocxUtil.appendText(current), part.text);
				}
			}
		}

		return current;
	}

	private static void writeText(Text text, String value) {
		text.setValue(value);
		text.setSpace("preserve");
	}

	/**
	 * Split a line into parts at inline markup boundaries and tabs. The markup tags
	 * themselves are removed; the text between an opening and closing tag carries
	 * the corresponding format(s). Nesting is supported.
	 */
	private static List<Part> parseParts(String line) {
		List<Part> parts = new ArrayList<>();
		Matcher m = STYLE_TAG.matcher(line);
		int last = 0;
		int bold = 0;
		int italic = 0;
		int underline = 0;
		int strike = 0;
		Deque<String> fg = new ArrayDeque<>();
		Deque<String> bg = new ArrayDeque<>();
		Deque<boolean[]> spans = new ArrayDeque<>();
		while (m.find()) {
			if (m.start() > last) {
				addTextParts(parts, line.substring(last, m.start()),
						new Style(bold > 0, italic > 0, underline > 0, strike > 0, fg.peek(), bg.peek()));
			}
			if (m.group(1) != null) {
				boolean closing = line.charAt(m.start() + 1) == '/';
				int delta = closing ? -1 : 1;
				switch (m.group(1).toLowerCase()) {
				case "b":
				case "strong":
					bold = Math.max(0, bold + delta);
					break;
				case "i":
				case "em":
					italic = Math.max(0, italic + delta);
					break;
				case "u":
					underline = Math.max(0, underline + delta);
					break;
				case "s":
				case "strike":
				case "del":
					strike = Math.max(0, strike + delta);
					break;
				default:
					break;
				}
			} else if (m.group(2) != null || m.group(3) != null || m.group(4) != null) {
				String styleAttribute = m.group(2) != null ? m.group(2)
						: (m.group(3) != null ? m.group(3) : m.group(4));

				String foregroundColor = extractColor(styleAttribute, "color");
				String backgroundColor = extractColor(styleAttribute, "background-color");

				boolean setForeground = foregroundColor != null;
				boolean setBackground = backgroundColor != null;

				if (setForeground) {
					fg.push(foregroundColor);
				}
				if (setBackground) {
					bg.push(backgroundColor);
				}
				spans.push(new boolean[] { setForeground, setBackground });
			} else if (!spans.isEmpty()) {
				boolean[] rec = spans.pop();
				if (rec[0] && !fg.isEmpty()) {
					fg.pop();
				}
				if (rec[1] && !bg.isEmpty()) {
					bg.pop();
				}
			}
			last = m.end();
		}
		if (last < line.length()) {
			addTextParts(parts, line.substring(last),
					new Style(bold > 0, italic > 0, underline > 0, strike > 0, fg.peek(), bg.peek()));
		}
		if (parts.isEmpty()) {
			parts.add(Part.text(StringUtils.EMPTY, Style.PLAIN));
		}
		return parts;
	}

	/**
	 * Extract a colour value (returned as {@code RRGGBB}, no leading #) for the
	 * given CSS property from a {@code style} attribute, or {@code null}.
	 */
	private static String extractColor(String style, String key) {
		if (style == null) {
			return null;
		}
		style = style.replace("&quot;", "").replace("&apos;", "").replace("\"", "").replace("'", "");

		for (String declaration : style.split(";")) {
			int colon = declaration.indexOf(':');
			if (colon < 0) {
				continue;
			}
			if (declaration.substring(0, colon).trim().equalsIgnoreCase(key)) {
				String value = declaration.substring(colon + 1).trim();
				if (value.startsWith("#")) {
					value = value.substring(1);
				}
				if (value.matches("[0-9a-fA-F]{6}")) {
					return value.toUpperCase();
				}
			}
		}
		return null;
	}

	private static void addTextParts(List<Part> parts, String text, Style style) {
		List<String> tabsSplit = Arrays.asList(text.split("\t", -1));
		for (int i = 0; i < tabsSplit.size(); i++) {
			if (i > 0) {
				parts.add(Part.tab());
			}
			if (!tabsSplit.get(i).isEmpty()) {
				parts.add(Part.text(tabsSplit.get(i), style));
			}
		}
	}

	/**
	 * Apply the active character formats to a run. When {@code mutateExisting} is
	 * true the run's existing {@link RPr} is extended (used for the reused anchor
	 * run); otherwise a fresh copy of {@code baseRPr} (the pristine template style)
	 * is used so formats never leak between runs. Only active formats are set, so
	 * any styling inherited from the template is preserved.
	 */
	private static void applyRunStyle(R r, RPr baseRPr, Style style, boolean mutateExisting) {
		RPr rpr;
		if (mutateExisting) {
			rpr = r.getRPr();
			if (rpr == null) {
				rpr = wmlObjectFactory.createRPr();
				r.setRPr(rpr);
				rpr.setParent(r);
			}
		} else {
			rpr = baseRPr != null ? (RPr) XmlUtils.deepCopy(baseRPr) : wmlObjectFactory.createRPr();
			r.setRPr(rpr);
			rpr.setParent(r);
		}
		if (style.bold) {
			BooleanDefaultTrue b = new BooleanDefaultTrue();
			b.setVal(true);
			rpr.setB(b);
		}
		if (style.italic) {
			BooleanDefaultTrue i = new BooleanDefaultTrue();
			i.setVal(true);
			rpr.setI(i);
		}
		if (style.strike) {
			BooleanDefaultTrue s = new BooleanDefaultTrue();
			s.setVal(true);
			rpr.setStrike(s);
		}
		if (style.underline) {
			U u = wmlObjectFactory.createU();
			u.setVal(UnderlineEnumeration.SINGLE);
			rpr.setU(u);
		}
		if (style.fg != null) {
			Color color = wmlObjectFactory.createColor();
			color.setVal(style.fg);
			rpr.setColor(color);
		}
		if (style.bg != null) {
			CTShd shd = wmlObjectFactory.createCTShd();
			shd.setVal(STShd.CLEAR);
			shd.setFill(style.bg);
			rpr.setShd(shd);
		}
	}

	private static final class Style {
		static final Style PLAIN = new Style(false, false, false, false, null, null);

		final boolean bold;
		final boolean italic;
		final boolean underline;
		final boolean strike;
		final String fg;
		final String bg;

		Style(boolean bold, boolean italic, boolean underline, boolean strike, String fg, String bg) {
			this.bold = bold;
			this.italic = italic;
			this.underline = underline;
			this.strike = strike;
			this.fg = fg;
			this.bg = bg;
		}

		boolean isPlain() {
			return !bold && !italic && !underline && !strike && fg == null && bg == null;
		}

		boolean matches(Style other) {
			return other != null && bold == other.bold && italic == other.italic && underline == other.underline
					&& strike == other.strike && java.util.Objects.equals(fg, other.fg)
					&& java.util.Objects.equals(bg, other.bg);
		}
	}

	private static final class Part {
		final String text;
		final Style style;
		final boolean tab;

		private Part(String text, Style style, boolean tab) {
			this.text = text;
			this.style = style;
			this.tab = tab;
		}

		static Part text(String text, Style style) {
			return new Part(text, style, false);
		}

		static Part tab() {
			return new Part(null, Style.PLAIN, true);
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

	public static boolean isMultiTextRun(R cursor) {
		List<Object> content = new ArrayList<>(cursor.getContent());
		int textCount = 0;
		for (Object object : content) {
			Object nonJaxBObject = object;
			if (object instanceof JAXBElement<?>) {
				nonJaxBObject = ((JAXBElement<?>) object).getValue();
			}
			if (nonJaxBObject instanceof Text) {
				textCount++;
				if (textCount > 1) {
					return true;
				}
			}
		}
		return false;
	}

	public static void convertToSingleTextRun(R cursor) {
		while (isMultiTextRun(cursor)) {
			R newRun = DocxUtil.createRunBefore(cursor);
			transferFirstText(cursor, newRun);
		}
	}

	private static void transferFirstText(R from, R to) {
		List<Object> content = new ArrayList<>(from.getContent());
		for (Object object : content) {
			Object nonJaxBObject = object;
			if (object instanceof JAXBElement<?>) {
				nonJaxBObject = ((JAXBElement<?>) object).getValue();
			}
			if (nonJaxBObject instanceof Text) {
				Text text = (Text) nonJaxBObject;
				to.getContent().add(text);
				text.setParent(to);

				from.getContent().remove(object);

				break;
			} else if (nonJaxBObject instanceof Child) {
				to.getContent().add(nonJaxBObject);
				((Child) nonJaxBObject).setParent(to);

				from.getContent().remove(object);
			}
		}
	}

	public static String sanitizeHtmlForNebula(String rawHtml) {
		if (rawHtml == null || rawHtml.isEmpty()) {
			return rawHtml;
		}
		String sanitized = rawHtml.replace("&quot;", "\"").replace("&apos;", "'");
		sanitized = sanitized.replaceAll("<span\\s+style\\s*=\\s*([^\"'>\\s]+)\\s*>", "<span style=\"$1\">");

		return flattenHtmlLists(sanitized);
	}

	private static String flattenHtmlLists(String html) {
		if (html == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		Matcher m = Pattern.compile("(?i)<(ul|ol)[^>]*>(.*?)</\\1>", Pattern.DOTALL).matcher(html);
		while (m.find()) {
			String tag = m.group(1).toLowerCase();
			String content = m.group(2);

			StringBuffer listContent = new StringBuffer();
			Matcher liMatcher = Pattern.compile("(?i)<li[^>]*>(.*?)</li>", Pattern.DOTALL).matcher(content);
			int counter = 1;
			while (liMatcher.find()) {
				String item = liMatcher.group(1);
				if (tag.equals("ol")) {
					listContent.append(counter).append(". ").append(item).append("<br/>");
					counter++;
				} else {
					listContent.append("&#8226; ").append(item).append("<br/>");
				}
			}

			if (listContent.length() == 0) {
				m.appendReplacement(sb, Matcher.quoteReplacement(content));
			} else {
				m.appendReplacement(sb, Matcher.quoteReplacement(listContent.toString()));
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
