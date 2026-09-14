package ch.elexis.core.text.docx.util;

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
 * Turns lines starting with a dash into {@code <ul>/<li>} lists on the way into a Word document.
 * Findings (diagnoses, anamnesis, risks, allergies) carry the dash as text, it is the bullet
 * convention of the plain text the rich text editor took over, and Word shows a bullet only for a
 * real list. The diagnose view does the same with a bullet character, see
 * {@code TextUtil#sanitizeHtmlForNebula}.
 */
public final class DashListConverter {

	/** A line starting with a dash, with or without a following space. */
	private static final Pattern LEADING_DASH = Pattern.compile("^[\\s\\u00A0]*-[ \\t\\u00A0]?");

	private static final String BLOCKS = "p,div,h1,h2,h3,h4,h5,h6";

	private DashListConverter() {
	}

	/**
	 * Converts every dash line into a list item, consecutive ones into the same list. The editor
	 * keeps a whole text in one block with {@code <br/>} between the lines, so the blocks are cut
	 * into one block per line first, and only where a dash line is involved. Markup without dash
	 * lines is returned unchanged.
	 *
	 * @param html the markup as the rich text editor stored it
	 * @return the markup with dash lines as {@code <ul>/<li>}
	 */
	public static String toHtmlLists(String html) {
		if (html == null || html.indexOf('-') < 0) {
			return html;
		}
		Document doc = Jsoup.parseBodyFragment(html);
		doc.outputSettings().prettyPrint(false).syntax(Syntax.xml).escapeMode(EscapeMode.xhtml);
		for (Element block : new ArrayList<>(doc.body().select(BLOCKS))) {
			splitIntoLines(block);
		}
		groupDashLines(doc.body());
		doc.body().select("li").forEach(DashListConverter::stripLeadingDash);
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

	private static boolean startsWithDash(String text) {
		return text != null && LEADING_DASH.matcher(text).find();
	}

	private static boolean isBlock(Element element) {
		String tag = element.tagName().toLowerCase();
		return "p".equals(tag) || "div".equals(tag)
				|| (tag.length() == 2 && tag.charAt(0) == 'h' && Character.isDigit(tag.charAt(1)));
	}

	private static boolean isBreak(Node node) {
		return node instanceof Element && "br".equalsIgnoreCase(((Element) node).tagName());
	}
}
