package ch.elexis.core.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * The dash bullet convention of the findings (diagnoses, anamnesis, risks, allergies) in one place.
 * A line starting with a dash is an enumeration item: that is how the text was written before the
 * rich text editor, and the users kept writing it that way. Every output has to turn it into its
 * own kind of bullet.
 * <ul>
 * <li>{@link #toHtmlLists(String)} for Word, which shows a bullet only for a real list</li>
 * <li>{@link #toBulletChars(String)} and {@link #flattenLists(String)} for the diagnose view, whose
 * painter has no list support and needs a bullet character per line</li>
 * </ul>
 */
public final class BulletConverter {

	/** A line starting with a dash, with or without a following space. */
	private static final Pattern LEADING_DASH = Pattern.compile("^[\\s\\u00A0]*-[ \\t\\u00A0]?");

	/** The tags the rich text editor writes, tells its markup from plain text of earlier versions. */
	private static final Pattern HTML_TAG = Pattern
			.compile("(?i)</?(?:p|br|div|ul|ol|li|strong|b|em|i|u|s|strike|del|span|font|h[1-6]|sub|sup)"
					+ "(?:\\s[^>]*)?\\s*/?>");

	private static final String BLOCKS = "p,div,h1,h2,h3,h4,h5,h6";

	/** Whitespace and opening inline tags that may sit between the start of a line and its dash. */
	private static final String LINE_START = "(?:\\s|&nbsp;|\\u00A0|<(?:span|strong|b|em|i|u|s|strike|del|sub|sup|font|a)"
			+ "(?:\\s[^>]*)?>)*";

	/** A dash line of the markup: start of text, after a line break or at the start of a block. */
	private static final Pattern LINE_DASH = Pattern
			.compile("(?i)(^|<br\\s*/?>|<(?:p|div)(?:\\s[^>]*)?>|</(?:p|div)>)(" + LINE_START + ")-[ \\t]?");

	/** A dash the writer left at the start of a real list item, where the list brings its own bullet. */
	private static final Pattern ITEM_DASH = Pattern
			.compile("(?i)(<li(?:\\s[^>]*)?>)(" + LINE_START + ")-[ \\t]?");

	/** Four non-breaking spaces per nesting level, so the view painter keeps the indent. */
	private static final String LIST_INDENT = "&#160;&#160;&#160;&#160;";

	private BulletConverter() {
	}

	/**
	 * Turns every dash line into a list item, consecutive ones into the same list. Takes both the
	 * markup of the rich text editor and the plain text stored by earlier versions. Text without
	 * dash lines keeps its structure.
	 *
	 * @param textOrHtml the stored finding text
	 * @return block markup with the dash lines as {@code <ul>/<li>}
	 */
	public static String toHtmlLists(String textOrHtml) {
		if (StringUtils.isBlank(textOrHtml)) {
			return textOrHtml;
		}
		if (HTML_TAG.matcher(textOrHtml).find()) {
			return markupToLists(textOrHtml);
		}
		return plainTextToLists(textOrHtml);
	}

	/** Builds the block markup from plain text, one block per line. */
	private static String plainTextToLists(String text) {
		StringBuilder html = new StringBuilder();
		boolean inList = false;
		for (String line : text.split("\\r?\\n")) {
			String content = line.trim();
			boolean item = content.startsWith("-");
			if (item) {
				content = content.substring(1).trim();
			}
			if (content.isEmpty()) {
				continue;
			}
			if (item != inList) {
				html.append(item ? "<ul>" : "</ul>");
				inList = item;
			}
			html.append(item ? "<li>" : "<p>").append(escapeHtml(content)).append(item ? "</li>" : "</p>");
		}
		if (inList) {
			html.append("</ul>");
		}
		return html.toString();
	}

	/**
	 * Converts the dash lines of editor markup. The editor keeps a whole text in one block with
	 * {@code <br/>} between the lines, so the blocks are cut into one block per line first, and
	 * only where a dash line is involved.
	 */
	private static String markupToLists(String html) {
		if (html.indexOf('-') < 0) {
			return html;
		}
		Document doc = Jsoup.parseBodyFragment(html);
		doc.outputSettings().prettyPrint(false).syntax(Syntax.xml).escapeMode(EscapeMode.xhtml);
		for (Element block : new ArrayList<>(doc.body().select(BLOCKS))) {
			splitIntoLines(block);
		}
		groupDashLines(doc.body());
		doc.body().select("li").forEach(BulletConverter::stripLeadingDash);
		return doc.body().html();
	}

	/** Replaces a block holding {@code <br/>} separated lines with one block per line. */
	private static void splitIntoLines(Element block) {
		List<List<Node>> lines = splitOnBr(new ArrayList<>(block.childNodes()));
		if (lines.size() < 2 || lines.stream().noneMatch(line -> startsWithDash(lineText(line)))) {
			return;
		}
		for (List<Node> line : lines) {
			Element element = new Element(block.tagName());
			block.attributes().forEach(attribute -> element.attr(attribute.getKey(), attribute.getValue()));
			line.forEach(element::appendChild);
			block.before(element);
		}
		block.remove();
	}

	/**
	 * Splits the nodes into lines at every {@code <br/>}, also inside inline markup: a break within
	 * a coloured {@code <span>} ends the line, and the rest of the span continues the next one.
	 */
	private static List<List<Node>> splitOnBr(List<Node> nodes) {
		List<List<Node>> lines = new ArrayList<>();
		List<Node> line = new ArrayList<>();
		for (Node node : nodes) {
			if (isBreak(node)) {
				lines.add(line);
				line = new ArrayList<>();
			} else if (node instanceof Element && !((Element) node).select("br").isEmpty()) {
				List<List<Node>> inner = splitOnBr(new ArrayList<>(node.childNodes()));
				for (int i = 0; i < inner.size(); i++) {
					if (i > 0) {
						lines.add(line);
						line = new ArrayList<>();
					}
					if (!inner.get(i).isEmpty()) {
						Element wrapper = ((Element) node).shallowClone();
						inner.get(i).forEach(wrapper::appendChild);
						line.add(wrapper);
					}
				}
			} else {
				line.add(node);
			}
		}
		lines.add(line);
		return lines;
	}

	/** Moves consecutive dash lines into a single {@code <ul>}. */
	private static void groupDashLines(Element body) {
		Element list = null;
		for (Element block : new ArrayList<>(body.children())) {
			if (isBlock(block) && startsWithDash(block.text())) {
				if (list == null) {
					list = new Element("ul");
					block.before(list);
				}
				Element item = new Element("li");
				new ArrayList<>(block.childNodes()).forEach(item::appendChild);
				list.appendChild(item);
				block.remove();
			} else {
				list = null;
			}
		}
	}

	/** Removes the dash from the item's first text, wherever inline markup put it. */
	private static boolean stripLeadingDash(Element element) {
		for (Node node : element.childNodes()) {
			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				String value = textNode.getWholeText();
				if (value.isBlank()) {
					continue;
				}
				String stripped = LEADING_DASH.matcher(value).replaceFirst(StringUtils.EMPTY);
				if (stripped.equals(value)) {
					return false;
				}
				textNode.text(stripped);
				return true;
			} else if (node instanceof Element && stripLeadingDash((Element) node)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Turns a line that starts with a dash, with or without a following space, into a bullet line
	 * ("&#8226; "), so text typed with a leading dash gets a real bullet - the same bullet the
	 * flattened {@code <ul>} lists use. Only matches the dash at the very start of a line - start
	 * of string, right after a {@code <br/>}, or at the start/end of a block element ({@code <p>},
	 * {@code <div>}) - so ranges like "start - end" inside a line are kept untouched. Inline markup
	 * around the line is stepped over and the bullet placed before it, so a struck through line does
	 * not strike its bullet as well. In a real list item the dash is only removed, because the
	 * list itself already brings a bullet.
	 *
	 * @param html the markup to render in the view
	 * @return the markup with a bullet character per dash line
	 */
	public static String toBulletChars(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		String withoutItemDashes = ITEM_DASH.matcher(html).replaceAll("$1$2");
		return LINE_DASH.matcher(withoutItemDashes).replaceAll("$1&#8226; $2");
	}

	/**
	 * Flattens {@code <ul>}/{@code <ol>} into indented text lines for a painter without list
	 * support: nested lists are indented, ordered lists numbered continuously, empty items
	 * dropped; inline markup is kept.
	 *
	 * @param html the markup to render in the view
	 * @return the markup with the lists as text lines
	 */
	public static String flattenLists(String html) {
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
			clone.children().stream().filter(BulletConverter::isListElement).forEach(Element::remove);
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

	private static String lineText(List<Node> line) {
		StringBuilder text = new StringBuilder();
		for (Node node : line) {
			if (node instanceof TextNode) {
				text.append(((TextNode) node).text());
			} else if (node instanceof Element) {
				text.append(((Element) node).text());
			}
		}
		return text.toString();
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static boolean startsWithDash(String text) {
		return text != null && LEADING_DASH.matcher(text).find();
	}

	private static boolean isBlock(Element element) {
		String tag = element.tagName().toLowerCase();
		return "p".equals(tag) || "div".equals(tag)
				|| (tag.length() == 2 && tag.charAt(0) == 'h' && Character.isDigit(tag.charAt(1)));
	}

	private static boolean isListElement(Element element) {
		return "ul".equalsIgnoreCase(element.tagName()) || "ol".equalsIgnoreCase(element.tagName());
	}

	private static boolean isBreak(Node node) {
		return node instanceof Element && "br".equalsIgnoreCase(((Element) node).tagName());
	}
}
