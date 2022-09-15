package at.medevit.elexis.text.docx.util;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.eclipse.swt.SWT;

public class StyleInfo {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	private String fontName;
	private int fontStyle = -1;
	private float fontSize = -1;

	private boolean styleSet = false;

	public void setFontStyle(int style) {
		styleSet = true;
		this.fontStyle = style;
	}

	public void setFontName(String name) {
		styleSet = true;
		this.fontName = name;
	}

	public void setFontSize(float size) {
		styleSet = true;
		this.fontSize = size;
	}

	public boolean isStyleSet() {
		return styleSet;
	}

	public void applyTo(RPr rpr) {
		if (fontName != null) {
			RFonts fonts = rpr.getRFonts();
			if (fonts == null) {
				fonts = wmlObjectFactory.createRFonts();
				fonts.setAscii(fontName);
				fonts.setHAnsi(fontName);
				fonts.setCs(fontName);
			}
			rpr.setRFonts(fonts);
		}
		if (fontSize != -1) {
			HpsMeasure measure = wmlObjectFactory.createHpsMeasure();
			measure.setVal(BigInteger.valueOf((long) fontSize * 2));
			rpr.setSz(measure);
			rpr.setSzCs(measure);
		}
		if (fontStyle != -1) {
			BooleanDefaultTrue bold = new BooleanDefaultTrue();
			if ((fontStyle & SWT.BOLD) != 0) {
				bold.setVal(true);
				rpr.setB(bold);
			} else {
				bold.setVal(false);
				rpr.setB(bold);
			}
		}
	}
}
