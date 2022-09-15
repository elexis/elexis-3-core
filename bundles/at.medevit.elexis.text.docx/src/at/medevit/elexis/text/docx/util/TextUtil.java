package at.medevit.elexis.text.docx.util;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;

public class TextUtil {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

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
		if (cursor instanceof R) {
			return replaceText(((R) cursor), text, styleInfo);
		} else if (cursor instanceof Text) {
			return replaceText(((Text) cursor).getParent(), text, align, styleInfo);
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
	private static Object replaceText(R cursor, String text, StyleInfo styleInfo) {
		R ret = null;

		boolean cursorInTbl = DocxUtil.getParentTbl(cursor) != null;
		List<String> newLineSplit = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < newLineSplit.size(); i++) {
			if (i == 0) {
				ret = insertLine(cursor, newLineSplit.get(i), styleInfo);
			} else {
				if (cursorInTbl) {
					P p = DocxUtil.appendParagraph(cursor);
					R r = DocxUtil.getOrCreateRun(p);
					r.setRPr(cursor.getRPr());
					insertLine(r, newLineSplit.get(i), styleInfo);
					cursor = r;
				} else {
					appendLine(cursor, newLineSplit.get(i), styleInfo);
				}
			}
		}

		return ret;
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

		List<String> newLineSplit = Arrays.asList(text.split(StringUtils.LF, -1));
		for (int i = 0; i < newLineSplit.size(); i++) {
			if (i == 0) {
				ret = DocxUtil.appendRun(cursor);
				insertLine(ret, newLineSplit.get(i), styleInfo);
				cursor = ret;
			} else {
				appendLine(cursor, newLineSplit.get(i), styleInfo);
			}
		}

		return ret;
	}

	/**
	 * Text of the line will be inserted into cursor. Only \t are allowed and
	 * handled.
	 *
	 * @param cursor
	 * @param line
	 * @param styleInfo
	 * @return
	 */
	private static R insertLine(R cursor, String line, StyleInfo styleInfo) {
		applyStyleInfo(cursor, styleInfo);

		Text text = DocxUtil.getOrCreateText(cursor);
		List<String> tabsSplit = Arrays.asList(line.split("\t", -1));
		if (tabsSplit.size() > 1) {
			for (int i = 0; i < tabsSplit.size(); i++) {
				if (i == 0) {
					text.setValue(tabsSplit.get(i));
				} else {
					text = DocxUtil.appendTabbedText(cursor);
					text.setValue(tabsSplit.get(i));
				}
			}
		} else {
			text.setValue(line);
		}
		return cursor;
	}

	private static R appendLine(R cursor, String line, StyleInfo styleInfo) {
		applyStyleInfo(cursor, styleInfo);

		Text text = DocxUtil.getNewLineText(cursor);
		List<String> tabsSplit = Arrays.asList(line.split("\t", -1));
		if (tabsSplit.size() > 1) {
			for (int i = 0; i < tabsSplit.size(); i++) {
				if (i == 0) {
					text.setValue(tabsSplit.get(i));
				} else {
					text = DocxUtil.appendTabbedText(cursor);
					text.setValue(tabsSplit.get(i));
				}
			}
		} else {
			text.setValue(line);
		}
		return cursor;
	}

	private static boolean applyStyleInfo(R r, StyleInfo styleInfo) {
		if (styleInfo.isStyleSet()) {
			// Create object for rPr
			RPr rpr = wmlObjectFactory.createRPr();
			styleInfo.applyTo(rpr);
			r.setRPr(rpr);
			return true;
		}
		return false;
	}

	public static boolean isMultiTextRun(R cursor) {
		List<Object> content = new ArrayList<Object>(cursor.getContent());
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
		List<Object> content = new ArrayList<Object>(from.getContent());
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
}
