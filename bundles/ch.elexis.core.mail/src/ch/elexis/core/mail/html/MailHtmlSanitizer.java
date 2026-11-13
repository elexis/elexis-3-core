package ch.elexis.core.mail.html;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * Sanitizes untrusted mail HTML and blocks remote images without fetching any resources.
 */
public final class MailHtmlSanitizer {

	private static final int MAX_HTML_LENGTH = 5_000_000;

	private static final String FORBIDDEN_ELEMENTS = "script,noscript,iframe,frame,object,embed,applet,form,"
			+ "input,button,select,textarea,meta,base,link,svg,math,template,portal,style";

	private final MailCssFilter cssFilter = new MailCssFilter();

	/**
	 * Returns a sanitized body fragment, without html, head or body wrappers.
	 *
	 * @param html the non-null HTML input
	 * @param cidUrlResolver trusted resolver from bare Content-ID to a local attachment URL;
	 *            a null result removes the image. The endpoint must validate raster bytes and the 10 MB limit.
	 * @return sanitized body HTML, or empty if the input exceeds 5,000,000 characters
	 */
	public Optional<String> sanitize(String html, Function<String, String> cidUrlResolver) {
		if (html.length() > MAX_HTML_LENGTH) {
			return Optional.empty();
		}
		Document source = Jsoup.parse(html);
		source.select(FORBIDDEN_ELEMENTS).remove();
		Safelist safelist = Safelist.relaxed().addAttributes(":all", "style")
				.removeAttributes("blockquote", "cite").removeAttributes("q", "cite")
				.removeProtocols("a", "href", "ftp", "http", "https", "mailto")
				.removeProtocols("img", "src", "http", "https");
		Document clean = new Cleaner(safelist).clean(source);
		clean.outputSettings().prettyPrint(false);
		for (Element element : clean.body().getAllElements()) {
			if (element.hasAttr("style")) {
				String style = cssFilter.filter(element.attr("style"));
				if (style.isEmpty()) {
					element.removeAttr("style");
				} else {
					element.attr("style", style);
				}
			}
			if (element.normalName().equals("a") && element.hasAttr("href")) {
				String href = element.attr("href").trim();
				String scheme = scheme(href);
				if (scheme.equals("http") || scheme.equals("https") || scheme.equals("mailto")) {
					element.attr("href", href).attr("target", "_blank").attr("rel", "noopener noreferrer nofollow");
				} else {
					element.removeAttr("href");
				}
			} else if (element.normalName().equals("img")) {
				filterImage(element, cidUrlResolver);
			}
		}
		return Optional.of(clean.body().html());
	}

	private String scheme(String url) {
		int colon = url.indexOf(':');
		return colon < 0 ? "" : url.substring(0, colon).toLowerCase(Locale.ROOT);
	}

	private void filterImage(Element image, Function<String, String> resolver) {
		String src = image.attr("src").trim();
		String scheme = scheme(src);
		if (scheme.equals("cid")) {
			String id = src.substring(4).trim();
			if (id.startsWith("<") && id.endsWith(">")) {
				id = id.substring(1, id.length() - 1);
			}
			String localUrl = resolver.apply(id);
			if (localUrl == null) {
				image.remove();
			} else {
				image.attr("src", localUrl);
			}
		} else if (scheme.equals("http") || scheme.equals("https")) {
			image.removeAttr("src");
			image.attr("data-mail-remote-src", src).addClass("mail-remote-image");
		} else if (scheme.equals("data") && isRasterData(src)) {
			image.attr("src", src);
		} else {
			image.remove();
		}
	}

	private boolean isRasterData(String src) {
		int semicolon = src.indexOf(';');
		int comma = src.indexOf(',');
		int end = semicolon >= 0 && semicolon < comma ? semicolon : comma;
		if (end < 0) {
			return false;
		}
		String mime = src.substring(0, end).toLowerCase(Locale.ROOT);
		return mime.equals("data:image/png") || mime.equals("data:image/jpeg") || mime.equals("data:image/gif")
				|| mime.equals("data:image/webp");
	}
}
