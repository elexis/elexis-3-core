package ch.elexis.core.text.docx.stax;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.openpackaging.parts.StAXHandlerAbstract;

public class RegexFindStAXHandler extends StAXHandlerAbstract {

	private Pattern pattern;
	private int foundCount;
	private List<String> foundList;

	public RegexFindStAXHandler(String regex) {
		this.pattern = Pattern.compile(regex);
		this.foundList = new ArrayList<>();
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
			countAll(wmlTemplateString);
		}
		return strB.append(wmlTemplateString);
	}

	private boolean patternMatchesText(String text) {
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	private void countAll(String text) {
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			foundCount++;
			foundList.add(text.substring(matcher.start(), matcher.end()));
		}
	}

	public int getCount() {
		return foundCount;
	}

	public List<String> getFoundList() {
		return foundList;
	}
}
