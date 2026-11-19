package ch.elexis.core.mail.html;

import java.util.Locale;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/** Applies the compose editor's output allow list without loading external resources. */
public final class OutgoingHtmlFilter {

	private static final Set<String> BLOCKS = Set.of("p", "div", "ul", "ol", "blockquote");
	/**
	 * Filters an editor body, retaining only the supported compose markup.
	 * @param html the editor HTML
	 * @return a safe body fragment
	 */
	public String filter(String html) {
		Document source = Jsoup.parse(html);
		source.select("script,style").remove();
		Safelist allowed = new Safelist().addTags("p", "br", "strong", "em", "u", "s", "ul", "ol", "li",
				"blockquote", "div", "span", "a", "img").addAttributes("span", "style")
				.addAttributes("div", "class").addAttributes("a", "href").addAttributes("img", "src", "alt");
		Document clean = new Cleaner(allowed).clean(source);
		clean.outputSettings().prettyPrint(false);
		MailCssFilter css = new MailCssFilter();
		for (Element element : clean.body().getAllElements()) {
			if (element.normalName().equals("span") && element.hasAttr("style")) {
				String style = colors(css.filter(element.attr("style")));
				element.removeAttr("style");
				if (!style.isEmpty()) {
					element.attr("style", style);
				}
			} else if (element.normalName().equals("div")) {
				boolean signature = element.hasClass("mail-signature");
				element.removeAttr("class");
				if (signature) {
					element.attr("class", "mail-signature");
				}
			} else if (element.normalName().equals("a") && !link(element.attr("href"))) {
				element.removeAttr("href");
			} else if (element.normalName().equals("img") && !image(element.attr("src"))) {
				element.remove();
			}
		}
		return clean.body().html();
	}

	private String colors(String filtered) {
		StringBuilder result = new StringBuilder();
		int start = 0;
		int depth = 0;
		char quote = 0;
		for (int i = 0; i <= filtered.length(); i++) {
			char value = i == filtered.length() ? ';' : filtered.charAt(i);
			if (quote != 0) {
				if (value == quote) {
					quote = 0;
				}
			} else if (value == '\'' || value == '"') {
				quote = value;
			} else if (value == '(' || value == '[' || value == '{') {
				depth++;
			} else if (value == ')' || value == ']' || value == '}') {
				depth--;
			} else if (value == ';' && depth == 0) {
				String declaration = filtered.substring(start, i);
				int colon = declaration.indexOf(':');
				if (colon > 0 && Set.of("color", "background-color").contains(declaration.substring(0, colon))) {
					if (!result.isEmpty()) {
						result.append(';');
					}
					result.append(declaration);
				}
				start = i + 1;
			}
		}
		return result.toString();
	}

	/**
	 * Produces the plain alternative, including paragraph, list and quote structure.
	 * @param html the editor HTML, filtered again before conversion
	 * @return plain text with LF line endings
	 */
	public String toPlainText(String html) {
		StringBuilder text = new StringBuilder();
		plain(Jsoup.parseBodyFragment(filter(html)).body(), text, 0);
		return text.toString().replaceAll("[ \\t]+\\n", "\n").strip();
	}

	private boolean link(String value) {
		String lower = value.trim().toLowerCase(Locale.ROOT);
		return lower.startsWith("http:") || lower.startsWith("https:") || lower.startsWith("mailto:");
	}

	private boolean image(String value) {
		String lower = value.trim().toLowerCase(Locale.ROOT);
		if (lower.startsWith("http:") || lower.startsWith("https:")) {
			return true;
		}
		int comma = lower.indexOf(',');
		if (comma < 0) {
			return false;
		}
		String type = lower.substring(0, comma).split(";", 2)[0];
		return Set.of("data:image/png", "data:image/jpeg", "data:image/gif", "data:image/webp").contains(type);
	}

	private void plain(Node node, StringBuilder text, int quote) {
		if (node instanceof TextNode value) {
			String content = value.getWholeText().replaceAll("\\s+", " ");
			if (!content.isBlank() || !text.isEmpty() && text.charAt(text.length() - 1) != '\n') {
				prefix(text, quote);
				text.append(content);
			}
			return;
		}
		String tag = node.nodeName();
		if (tag.equals("br")) {
			text.append('\n');
			return;
		}
		boolean block = BLOCKS.contains(tag) || tag.equals("li");
		if (block) {
			newline(text);
		}
		if (tag.equals("blockquote")) {
			quote++;
		}
		if (tag.equals("li")) {
			prefix(text, quote);
			text.append("- ");
		}
		for (Node child : node.childNodes()) {
			plain(child, text, quote);
		}
		if (block) {
			newline(text);
		}
	}

	private void prefix(StringBuilder text, int quote) {
		if (text.isEmpty() || text.charAt(text.length() - 1) == '\n') {
			text.append("> ".repeat(quote));
		}
	}

	private void newline(StringBuilder text) {
		if (!text.isEmpty() && text.charAt(text.length() - 1) != '\n') {
			text.append('\n');
		}
	}
}
