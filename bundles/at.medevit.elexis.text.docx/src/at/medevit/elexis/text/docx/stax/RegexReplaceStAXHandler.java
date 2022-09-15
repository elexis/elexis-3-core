package at.medevit.elexis.text.docx.stax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.openpackaging.parts.StAXHandlerAbstract;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;

public class RegexReplaceStAXHandler extends StAXHandlerAbstract {

	private ReplaceCallback replaceCallback;
	private Pattern pattern;

	public RegexReplaceStAXHandler(String regex, ReplaceCallback replaceCallback) {
		this.replaceCallback = replaceCallback;
		pattern = Pattern.compile(regex);
	}

	@Override
	public void handleCharacters(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {
		StringBuilder sb = new StringBuilder();
		sb.append(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());

		String wmlString = replace(sb.toString(), new StringBuilder()).toString();

		char[] charOut = wmlString.toCharArray();
		writer.writeCharacters(charOut, 0, charOut.length);
	}

	private StringBuilder replace(String wmlTemplateString, StringBuilder strB) {
		if (patternMatchesText(wmlTemplateString)) {
			return strB.append(replaceAll(wmlTemplateString));
		} else {
			return strB.append(wmlTemplateString);
		}
	}

	private boolean patternMatchesText(String text) {
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	private String replaceAll(String text) {
		String replaced = text;
		Matcher matcher = pattern.matcher(text);
		Object replacement;
		while (matcher.find()) {
			replacement = replaceCallback.replace(replaced.substring(matcher.start(), matcher.end()));
			if (replacement instanceof String) {
				replaced = matcher.replaceFirst((String) replacement);
			}
			matcher = pattern.matcher(replaced);
		}
		return replaced;
	}
}
