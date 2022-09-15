package at.medevit.elexis.text.docx.util;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.model.IImage;

public class RegexTextVisitor extends TraversalUtilVisitor<Text> {

	private List<Text> foundElements = new ArrayList<>();
	private Pattern pattern;

	private StyleInfo styleInfo;

	private WordprocessingMLPackage currentDocument;

	public RegexTextVisitor(WordprocessingMLPackage currentDocument, String regex) {
		pattern = Pattern.compile(regex);
		styleInfo = new StyleInfo();
		this.currentDocument = currentDocument;
	}

	public RegexTextVisitor() {
		// no value
	}

	@Override
	public void apply(Text element) {
		if (pattern != null) {
			if (patternMatchesText(element.getValue())) {
				foundElements.add(element);
			}
		} else {
			foundElements.add(element);
		}
	}

	private boolean patternMatchesText(String text) {
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	public List<Text> getFound() {
		return foundElements;
	}

	public void replaceMatchingTexts(ReplaceCallback replaceCallback) {
		for (Text text : foundElements) {
			Object cursor = getTextCursor(text);
			// make sure each Run contains only 1 text
			if (TextUtil.isMultiTextRun((R) cursor)) {
				TextUtil.convertToSingleTextRun((R) cursor);
				cursor = text.getParent();
			}
			String origText = text.getValue();

			String replaced = origText;
			Matcher matcher = pattern.matcher(origText);
			Object replacement;
			while (matcher.find()) {
				replacement = replaceCallback.replace(replaced.substring(matcher.start(), matcher.end()));
				if (replacement instanceof String) {
					replaced = matcher.replaceFirst(Matcher.quoteReplacement((String) replacement));
				} else if (replacement instanceof String[][]) {
					// insert a table
					Tbl table = TableUtil.insertTable((R) cursor, 0, (String[][]) replacement, null,
							DocxUtil.getDocumentWidth(currentDocument), true);
					TableUtil.addBorders(table, 1);
					replaced = StringUtils.EMPTY;
				} else if (replacement instanceof IImage) {
					try {
						ImageUtil.insertImage((R) cursor, (IImage) replacement, currentDocument);
					} catch (Exception e) {
						LoggerFactory.getLogger(getClass()).error("Error inserting image", e);
					}
					replaced = StringUtils.EMPTY;
				} else if (replacement == null) {
					replaced = matcher.replaceFirst(Matcher.quoteReplacement("??Auswahl??"));
				}
				matcher = pattern.matcher(replaced);
			}
			cursor = TextUtil.replaceText(cursor, replaced, 1 << 14, styleInfo);
		}
	}

	/**
	 * Fix [17413] for some docx documents the found {@link Text} has an R element
	 * as parent, that is not the R Element which is in the content of the P. This
	 * seems to be some wired behavior of the docx4j {@link TraversalUtilVisitor}.
	 * This workaround again searches for the {@link Text} with the parent P as root
	 * node.
	 *
	 * @param text
	 * @return
	 */
	private Object getTextCursor(Text text) {
		if (text.getParent() instanceof R && ((R) text.getParent()).getParent() instanceof P) {
			P parentP = (P) ((R) text.getParent()).getParent();
			List<Object> pContents = parentP.getContent();
			for (Object pContent : pContents) {
				if (pContent instanceof R) {
					R contentR = (R) pContent;
					List<Object> rContents = contentR.getContent();
					for (Object rContent : rContents) {
						if (rContent instanceof JAXBElement<?>) {
							JAXBElement<?> jaxbElement = (JAXBElement<?>) rContent;
							if (jaxbElement.getValue() == text) {
								return contentR;
							}
						}
					}
				}
			}
		}
		return text.getParent();
	}
}
