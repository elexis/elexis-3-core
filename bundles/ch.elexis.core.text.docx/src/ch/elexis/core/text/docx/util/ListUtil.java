package ch.elexis.core.text.docx.util;

import java.math.BigInteger;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.slf4j.LoggerFactory;

/**
 * Creates real Word numbering definitions (bullet and decimal) and list-item
 * paragraphs, so that {@code <ul>}/{@code <ol>}/{@code <li>} markup in a
 * replacement text can be rendered as proper Word lists.
 */
public class ListUtil {

	static final org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

	private static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	public static final class ListNumbering {
		private final Numbering numbering;
		private final long decimalAbstractId;
		private final long bulletNumId;
		private long nextNumId;

		private ListNumbering(Numbering numbering, long decimalAbstractId, long bulletNumId, long nextNumId) {
			this.numbering = numbering;
			this.decimalAbstractId = decimalAbstractId;
			this.bulletNumId = bulletNumId;
			this.nextNumId = nextNumId;
		}

		public long getBulletNumId() {
			return bulletNumId;
		}
	}

	/**
	 * Ensure the document has a numbering part with one bullet and one decimal
	 * abstract numbering. New ids are chosen above any existing ones so existing
	 * lists in a template are not disturbed.
	 *
	 * @return the created {@link ListNumbering}, or {@code null} on error
	 */
	public static ListNumbering ensure(WordprocessingMLPackage pkg) {
		try {
			MainDocumentPart mdp = pkg.getMainDocumentPart();
			NumberingDefinitionsPart ndp = mdp.getNumberingDefinitionsPart();
			Numbering numbering;
			if (ndp == null) {
				ndp = new NumberingDefinitionsPart();
				numbering = factory.createNumbering();
				ndp.setJaxbElement(numbering);
				mdp.addTargetPart(ndp);
			} else {
				numbering = ndp.getJaxbElement();
				if (numbering == null) {
					numbering = factory.createNumbering();
					ndp.setJaxbElement(numbering);
				}
			}

			long maxAbstract = 0;
			for (Numbering.AbstractNum abstractNum : numbering.getAbstractNum()) {
				if (abstractNum.getAbstractNumId() != null) {
					maxAbstract = Math.max(maxAbstract, abstractNum.getAbstractNumId().longValue());
				}
			}
			long maxNum = 0;
			for (Numbering.Num num : numbering.getNum()) {
				if (num.getNumId() != null) {
					maxNum = Math.max(maxNum, num.getNumId().longValue());
				}
			}

			long bulletAbstractId = maxAbstract + 1;
			long decimalAbstractId = maxAbstract + 2;
			long bulletNumId = maxNum + 1;

			Numbering created = (Numbering) XmlUtils
					.unmarshalString(buildNumberingXml(bulletAbstractId, decimalAbstractId, bulletNumId));
			numbering.getAbstractNum().addAll(created.getAbstractNum());
			numbering.getNum().addAll(created.getNum());

			return new ListNumbering(numbering, decimalAbstractId, bulletNumId, maxNum + 2);
		} catch (Exception e) {
			LoggerFactory.getLogger(ListUtil.class).error("Error preparing numbering definitions", e);
			return null;
		}
	}

	/**
	 * Create a fresh decimal numbering instance so an ordered list restarts at 1.
	 *
	 * @return the new numId
	 */
	public static long newDecimalNum(ListNumbering ctx) {
		long id = ctx.nextNumId++;
		try {
			String xml = "<w:numbering xmlns:w=\"" + W_NS + "\"><w:num w:numId=\"" + id
					+ "\"><w:abstractNumId w:val=\"" + ctx.decimalAbstractId + "\"/></w:num></w:numbering>";
			Numbering created = (Numbering) XmlUtils.unmarshalString(xml);
			ctx.numbering.getNum().addAll(created.getNum());
		} catch (Exception e) {
			LoggerFactory.getLogger(ListUtil.class).error("Error adding decimal numbering", e);
		}
		return id;
	}

	private static String buildNumberingXml(long bulletAbstractId, long decimalAbstractId, long bulletNumId) {
		StringBuilder sb = new StringBuilder();
		sb.append("<w:numbering xmlns:w=\"").append(W_NS).append("\">");
		sb.append("<w:abstractNum w:abstractNumId=\"").append(bulletAbstractId).append("\">");
		sb.append("<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/>");
		sb.append("<w:lvlText w:val=\"•\"/><w:lvlJc w:val=\"left\"/>");
		sb.append("<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>");
		sb.append("<w:abstractNum w:abstractNumId=\"").append(decimalAbstractId).append("\">");
		sb.append("<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>");
		sb.append("<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>");
		sb.append("<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>");
		sb.append("<w:num w:numId=\"").append(bulletNumId).append("\"><w:abstractNumId w:val=\"")
				.append(bulletAbstractId).append("\"/></w:num>");
		sb.append("</w:numbering>");
		return sb.toString();
	}

	/**
	 * Create a new paragraph carrying the given numbering, inserted directly after
	 * {@code afterP}.
	 */
	public static P createListParagraph(P afterP, long numId) {
		ContentAccessor parent = DocxUtil.getParentContentAccessor(afterP);
		P p = factory.createP();
		applyNumbering(p, numId);
		insertAfter(parent, afterP, p);
		return p;
	}

	public static P createPlainParagraph(P afterP, PPr templatePPr) {
		ContentAccessor parent = DocxUtil.getParentContentAccessor(afterP);
		P p = factory.createP();
		if (templatePPr != null) {
			PPr ppr = (PPr) XmlUtils.deepCopy(templatePPr);
			p.setPPr(ppr);
			ppr.setParent(p);
		}
		insertAfter(parent, afterP, p);
		return p;
	}

	public static void applyNumbering(P p, long numId) {
		PPr ppr = p.getPPr();
		if (ppr == null) {
			ppr = factory.createPPr();
			p.setPPr(ppr);
			ppr.setParent(p);
		}
		NumPr numPr = factory.createPPrBaseNumPr();
		NumPr.NumId nid = factory.createPPrBaseNumPrNumId();
		nid.setVal(BigInteger.valueOf(numId));
		numPr.setNumId(nid);
		NumPr.Ilvl ilvl = factory.createPPrBaseNumPrIlvl();
		ilvl.setVal(BigInteger.ZERO);
		numPr.setIlvl(ilvl);
		ppr.setNumPr(numPr);
		numPr.setParent(ppr);
	}

	public static R createRun(P p, RPr templateRPr) {
		R r = factory.createR();
		if (templateRPr != null) {
			RPr rpr = (RPr) XmlUtils.deepCopy(templateRPr);
			r.setRPr(rpr);
			rpr.setParent(r);
		}
		p.getContent().add(r);
		r.setParent(p);
		return r;
	}

	private static void insertAfter(ContentAccessor parent, P afterP, P p) {
		int index = parent.getContent().indexOf(afterP);
		if (index != -1) {
			parent.getContent().add(index + 1, p);
		} else {
			parent.getContent().add(p);
		}
		p.setParent(parent);
	}
}
