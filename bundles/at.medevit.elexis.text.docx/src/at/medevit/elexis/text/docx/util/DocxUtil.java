package at.medevit.elexis.text.docx.util;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.R;
import org.docx4j.wml.R.Cr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.eclipse.swt.SWT;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.LoggerFactory;

public class DocxUtil {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	public static ContentAccessor getParentContentAccessor(Child child) {
		Object parent = child.getParent();
		if (parent instanceof ContentAccessor) {
			return (ContentAccessor) parent;
		} else if (parent instanceof Child) {
			return getParentContentAccessor((Child) parent);
		}
		return null;
	}

	public static P getParentP(Child child) {
		Object parent = child.getParent();
		if (parent instanceof P) {
			return (P) parent;
		} else if (parent instanceof Child) {
			return getParentP((Child) parent);
		}
		return null;
	}

	public static Tbl getParentTbl(Child child) {
		Object parent = child.getParent();
		if (parent instanceof Tbl) {
			return (Tbl) parent;
		} else if (parent instanceof Child) {
			return getParentTbl((Child) parent);
		}
		return null;
	}

	public static Text getOrCreateText(R r) {
		Text ret = getLastText(r);
		if (ret == null) {
			ret = wmlObjectFactory.createText();
			JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(ret);
			r.getContent().add(textWrapped);
			ret.setParent(r);
		}
		return ret;
	}

	public static Text getNewLineText(R r) {
		appendNewLine(r);
		return appendText(r);
	}

	public static Text appendTabbedText(R r) {
		appendTab(r);
		return appendText(r);
	}

	private static Text appendText(R r) {
		Text ret = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(ret);
		r.getContent().add(textWrapped);
		ret.setParent(r);
		return ret;
	}

	public static R appendRun(R cursor) {
		ContentAccessor parent = getParentContentAccessor(cursor);
		int index = parent.getContent().indexOf(cursor);
		// Create object for r
		R r = wmlObjectFactory.createR();
		if (index != -1) {
			parent.getContent().add(index + 1, r);
		} else {
			parent.getContent().add(r);
		}
		r.setParent(parent);
		r.setRPr(cursor.getRPr()); // point to cursor style
		return r;
	}

	public static P appendParagraph(R cursor) {
		P parentP = getParentP(cursor);
		ContentAccessor parent = getParentContentAccessor(parentP);
		int index = parent.getContent().indexOf(parentP);
		// add a new P
		P p = wmlObjectFactory.createP();
		p.setPPr(parentP.getPPr());
		// add spacing 0 if not already spacing defined
		if (p.getPPr() != null && p.getPPr().getSpacing() == null) {
			Spacing spacing = wmlObjectFactory.createPPrBaseSpacing();
			spacing.setAfter(BigInteger.ZERO);
			p.getPPr().setSpacing(spacing);
		}
		if (index != -1) {
			parent.getContent().add(index + 1, p);
		} else {
			parent.getContent().add(p);
		}
		p.setParent(parent);
		return p;
	}

	public static R getOrCreateRun(P p) {
		R ret = getLastRun(p);
		if (ret == null) {
			// Create object for r
			R r = wmlObjectFactory.createR();
			p.getContent().add(r);
			r.setParent(p);

			// Create object for rPr
			RPr rpr = wmlObjectFactory.createRPr();
			r.setRPr(rpr);
			rpr.setParent(r);

			ret = r;
		}
		return ret;
	}

	public static R createRunBefore(R cursor) {
		P parentP = getParentP(cursor);
		int index = parentP.getContent().indexOf(cursor);
		// Create object for r
		R r = wmlObjectFactory.createR();
		parentP.getContent().add(index, r);
		r.setParent(parentP);
		r.setRPr(cursor.getRPr()); // point to cursor style
		return r;
	}

	public static R getLastRun(P p) {
		R ret = null;
		List<Object> content = p.getContent();
		for (Object object : content) {
			if (object instanceof JAXBElement<?>) {
				object = ((JAXBElement<?>) object).getValue();
			}
			if (object instanceof R) {
				ret = (R) object;
			}
		}
		return ret;
	}

	private static void appendTab(R r) {
		List<Object> content = r.getContent();
		R.Tab rTab = wmlObjectFactory.createRTab();
		JAXBElement<org.docx4j.wml.R.Tab> rtabWrapped = wmlObjectFactory.createRTab(rTab);
		content.add(rtabWrapped);
		rTab.setParent(r);
	}

	private static void appendNewLine(R r) {
		List<Object> content = r.getContent();
		Cr rCr = wmlObjectFactory.createRCr();
		JAXBElement<org.docx4j.wml.R.Cr> rcrWrapped = wmlObjectFactory.createRCr(rCr);
		content.add(rcrWrapped);
		rCr.setParent(r);
	}

	public static Text getLastText(R r) {
		Text ret = null;
		List<Object> content = r.getContent();
		for (Object object : content) {
			if (object instanceof JAXBElement<?>) {
				object = ((JAXBElement<?>) object).getValue();
			}
			if (object instanceof Text) {
				ret = (Text) object;
			}
		}
		return ret;
	}

	/**
	 * Get the writable width of the document from the sections page dimensions. If
	 * no such information is found a default width is returned.
	 *
	 * @param document
	 * @return
	 */
	public static int getDocumentWidth(WordprocessingMLPackage document) {
		int ret = getWritableWidthTwips();
		if (!document.getDocumentModel().getSections().isEmpty()) {
			SectionWrapper section = document.getDocumentModel().getSections().get(0);
			if (section != null) {
				PageDimensions pageDimensions = section.getPageDimensions();
				if (pageDimensions != null) {
					return pageDimensions.getWritableWidthTwips();
				}
			}
		}
		return ret;
	}

	private static int getWritableWidthTwips() {
		try {
			WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
			SectionWrapper section = wordPackage.getDocumentModel().getSections().get(0);
			PageDimensions pageDimensions = section.getPageDimensions();
			return pageDimensions.getWritableWidthTwips();
		} catch (InvalidFormatException e) {
			LoggerFactory.getLogger(TableUtil.class).error("Error getting table width twips");
		}
		return 0;
	}

	public static void addAlign(PPr ppr, int align) {
		Jc jc = wmlObjectFactory.createJc();
		if (align == SWT.LEFT) {
			jc.setVal(JcEnumeration.LEFT);
		} else if (align == SWT.RIGHT) {
			jc.setVal(JcEnumeration.RIGHT);
		} else if (align == SWT.CENTER) {
			jc.setVal(JcEnumeration.CENTER);
		}
		ppr.setJc(jc);
	}
}
