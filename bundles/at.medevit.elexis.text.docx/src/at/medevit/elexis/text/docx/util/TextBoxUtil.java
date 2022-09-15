package at.medevit.elexis.text.docx.util;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTTextboxInfo;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualDrawingShapeProps;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTPresetTextShape;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.STShapeType;
import org.docx4j.dml.STTextAnchoringType;
import org.docx4j.dml.STTextHorzOverflowType;
import org.docx4j.dml.STTextShapeType;
import org.docx4j.dml.STTextVertOverflowType;
import org.docx4j.dml.STTextVerticalType;
import org.docx4j.dml.STTextWrappingType;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTEffectExtent;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.CTPosV;
import org.docx4j.dml.wordprocessingDrawing.STRelFromH;
import org.docx4j.dml.wordprocessingDrawing.STRelFromV;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;

public class TextBoxUtil {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	static org.docx4j.dml.wordprocessingDrawing.ObjectFactory dmlwordprocessingDrawingObjectFactory = new org.docx4j.dml.wordprocessingDrawing.ObjectFactory();
	static org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

	static org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.ObjectFactory mswordprocessingShapeObjectFactory = new org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.ObjectFactory();

	// start txbx id with probably not already used number
	private static int txbxId = 1000;

	private synchronized static int getNextTxBxId() {
		return ++txbxId;
	}

	/**
	 * Create a P containing a text box with absolute positioning information.
	 * Adding to P to a document is the responsibility of the caller.
	 *
	 * @param posx
	 * @param posy
	 * @param width
	 * @param height
	 * @param align
	 * @return
	 */
	public static P pTextBox(int posx, int posy, int width, int height, int align) {
		P p = wmlObjectFactory.createP();
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r.setRPr(rpr);
		// Create object for noProof
		Drawing drawing = wmlObjectFactory.createDrawing();
		JAXBElement<org.docx4j.wml.Drawing> drawingWrapped = wmlObjectFactory.createRDrawing(drawing);
		// Create object for anchor
		Anchor anchor = dmlwordprocessingDrawingObjectFactory.createAnchor();
		anchor.setLayoutInCell(true);
		anchor.setAllowOverlap(true);
		anchor.setSimplePosAttr(false);
		anchor.setRelativeHeight(251658240);

		// dummy simple position
		CTPoint2D point2d = dmlObjectFactory.createCTPoint2D();
		point2d.setX(0);
		point2d.setY(0);
		anchor.setSimplePos(point2d);

		// relative from margin and page position
		CTPosH posH = dmlwordprocessingDrawingObjectFactory.createCTPosH();
		posH.setRelativeFrom(STRelFromH.MARGIN);
		posH.setPosOffset((int) UnitsOfMeasurement.twipToEMU(posx));
		anchor.setPositionH(posH);
		CTPosV posV = dmlwordprocessingDrawingObjectFactory.createCTPosV();
		posV.setRelativeFrom(STRelFromV.PAGE);
		posV.setPosOffset((int) UnitsOfMeasurement.twipToEMU(posy));
		anchor.setPositionV(posV);

		// size of the text box
		CTPositiveSize2D extent = dmlObjectFactory.createCTPositiveSize2D();
		extent.setCx(UnitsOfMeasurement.twipToEMU(width));
		extent.setCy(UnitsOfMeasurement.twipToEMU(height));
		anchor.setExtent(extent);

		CTEffectExtent effectiveExtent = dmlwordprocessingDrawingObjectFactory.createCTEffectExtent();
		anchor.setEffectExtent(effectiveExtent);
		anchor.setWrapNone(dmlwordprocessingDrawingObjectFactory.createCTWrapNone());
		CTNonVisualDrawingProps docPr = dmlObjectFactory.createCTNonVisualDrawingProps();
		int nextTxBxId = getNextTxBxId();
		docPr.setId(nextTxBxId);
		docPr.setName("Textfeld " + nextTxBxId);
		anchor.setDocPr(docPr);
		anchor.setCNvGraphicFramePr(dmlObjectFactory.createCTNonVisualGraphicFrameProperties());
		drawing.getAnchorOrInline().add(anchor);

		Graphic graphic = dmlObjectFactory.createGraphic();
		anchor.setGraphic(graphic);

		GraphicData graphicdata = dmlObjectFactory.createGraphicData();
		graphic.setGraphicData(graphicdata);

		CTNonVisualDrawingShapeProps cNvSpPr = dmlObjectFactory.createCTNonVisualDrawingShapeProps();
		cNvSpPr.setTxBox(true);
		graphicdata.setUri("http://schemas.microsoft.com/office/word/2010/wordprocessingShape");
		CTWordprocessingShape wpsp = mswordprocessingShapeObjectFactory.createCTWordprocessingShape();
		JAXBElement<CTWordprocessingShape> wpspWrapped = mswordprocessingShapeObjectFactory.createWsp(wpsp);
		graphicdata.getAny().add(wpspWrapped);
		graphicdata.getWordprocessingShape().setCNvSpPr(cNvSpPr);

		CTShapeProperties spPr = dmlObjectFactory.createCTShapeProperties();
		CTTransform2D xfrm = dmlObjectFactory.createCTTransform2D();
		CTPoint2D off = dmlObjectFactory.createCTPoint2D();
		off.setX(0);
		off.setY(0);
		xfrm.setOff(off);
		CTPositiveSize2D ext = dmlObjectFactory.createCTPositiveSize2D();
		ext.setCx(UnitsOfMeasurement.twipToEMU(width));
		ext.setCy(UnitsOfMeasurement.twipToEMU(height));
		xfrm.setExt(ext);
		spPr.setXfrm(xfrm);
		CTNoFillProperties noFill = dmlObjectFactory.createCTNoFillProperties();
		spPr.setNoFill(noFill);
		CTLineProperties ln = dmlObjectFactory.createCTLineProperties();
		noFill = dmlObjectFactory.createCTNoFillProperties();
		ln.setNoFill(noFill);
		spPr.setLn(ln);
		CTPresetGeometry2D prstGeom = dmlObjectFactory.createCTPresetGeometry2D();
		prstGeom.setPrst(STShapeType.RECT);
		spPr.setPrstGeom(prstGeom);
		graphicdata.getWordprocessingShape().setSpPr(spPr);

		CTTextboxInfo txbx = mswordprocessingShapeObjectFactory.createCTTextboxInfo();
		CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
		// Create object for p
		P p2 = wmlObjectFactory.createP();
		txbxcontent.getContent().add(p2);
		// Create object for pPr
		PPr ppr2 = wmlObjectFactory.createPPr();
		DocxUtil.addAlign(ppr2, align);
		p2.setPPr(ppr2);
		// Create object for rPr
		ParaRPr pararpr2 = wmlObjectFactory.createParaRPr();
		ppr2.setRPr(pararpr2);
		// Create object for r
		R r2 = wmlObjectFactory.createR();
		p2.getContent().add(r2);
		// Create object for rPr
		RPr rpr2 = wmlObjectFactory.createRPr();
		r2.setRPr(rpr2);
		// Create object for t (wrapped in JAXBElement)
		Text text = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text);
		r2.getContent().add(textWrapped);

		txbx.setTxbxContent(txbxcontent);

		graphicdata.getWordprocessingShape().setTxbx(txbx);

		CTTextBodyProperties bodyPr = dmlObjectFactory.createCTTextBodyProperties();
		bodyPr.setRot(0);
		bodyPr.setSpcFirstLastPara(false);
		bodyPr.setVertOverflow(STTextVertOverflowType.OVERFLOW);
		bodyPr.setHorzOverflow(STTextHorzOverflowType.OVERFLOW);
		bodyPr.setVert(STTextVerticalType.HORZ);
		bodyPr.setWrap(STTextWrappingType.SQUARE);
		bodyPr.setLIns(0);
		bodyPr.setTIns(0);
		bodyPr.setRIns(0);
		bodyPr.setBIns(0);
		bodyPr.setNumCol(1);
		bodyPr.setSpcCol(0);
		bodyPr.setRtlCol(false);
		bodyPr.setFromWordArt(false);
		bodyPr.setAnchor(STTextAnchoringType.T);
		bodyPr.setAnchorCtr(false);
		bodyPr.setForceAA(false);
		bodyPr.setCompatLnSpc(true);

		CTPresetTextShape prstTxWarp = dmlObjectFactory.createCTPresetTextShape();
		prstTxWarp.setPrst(STTextShapeType.TEXT_NO_SHAPE);
		bodyPr.setPrstTxWarp(prstTxWarp);

		graphicdata.getWordprocessingShape().setBodyPr(bodyPr);

		r.getContent().add(drawingWrapped);
		return p;
	}

	private static Optional<Text> getTextFromBoxP(P textBox) {
		FindTextVisitor visitor = new FindTextVisitor();
		TraversalUtil.visit(textBox, visitor);
		List<Text> found = visitor.getFound();
		if (!found.isEmpty()) {
			return Optional.of(found.get(0));
		}
		return Optional.empty();
	}

	/**
	 * Create a text box at the specified absolute position with the specified
	 * dimensions and text content. Provided position and dimensions are expected in
	 * mm.
	 *
	 * @param currentDocument
	 * @param posx
	 * @param posy
	 * @param width
	 * @param height
	 * @param text
	 * @param align
	 * @param currentStyleInfo
	 * @return
	 */
	public static Object createTextBox(WordprocessingMLPackage currentDocument, int posx, int posy, int width,
			int height, String text, int align, StyleInfo currentStyleInfo) {

		posx = UnitsOfMeasurement.mmToTwip(posx);
		posy = UnitsOfMeasurement.mmToTwip(posy);
		width = UnitsOfMeasurement.mmToTwip(width);
		height = UnitsOfMeasurement.mmToTwip(height);

		P textBox = pTextBox(posx, posy, width, height, align);
		currentDocument.getMainDocumentPart().getContent().add(textBox);
		Optional<Text> textElement = getTextFromBoxP(textBox);
		if (textElement.isPresent()) {
			R r = (R) textElement.get().getParent();
			TextUtil.insertText(r, text, align, currentStyleInfo);
			return r;
		}
		throw new IllegalStateException("Created textbox without Text element");
	}
}
