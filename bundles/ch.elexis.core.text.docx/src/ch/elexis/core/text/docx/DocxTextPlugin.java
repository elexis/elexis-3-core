package ch.elexis.core.text.docx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.text.MimeTypeUtil;
import ch.elexis.core.text.ReplaceCallback;
import ch.elexis.core.text.docx.stax.TextFindStAXHandler;
import ch.elexis.core.text.docx.util.DocxUtil;
import ch.elexis.core.text.docx.util.FindTextVisitor;
import ch.elexis.core.text.docx.util.RegexTextVisitor;
import ch.elexis.core.text.docx.util.StyleInfo;
import ch.elexis.core.text.docx.util.TableUtil;
import ch.elexis.core.text.docx.util.TextBoxUtil;
import ch.elexis.core.text.docx.util.TextUtil;

public class DocxTextPlugin implements ITextPlugin {

	private PageFormat format = ITextPlugin.PageFormat.USER;

	private Parameter parameter;

	private StyleInfo currentStyleInfo;

	private WordprocessingMLPackage currentDocument;

	public DocxTextPlugin() {
		currentStyleInfo = new StyleInfo();
	}

	@Override
	public PageFormat getFormat() {
		return format;
	}

	@Override
	public void setFormat(PageFormat f) {
		format = f;
	}

	@Override
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public boolean createEmptyDocument() {
		try {
			currentDocument = WordprocessingMLPackage.createPackage();
//			if (openButton != null && !openButton.isDisposed()) {
//				openButton.setEnabled(true);
//			}
		} catch (InvalidFormatException e) {
			LoggerFactory.getLogger(getClass()).error("Erro creating document", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
			try {
				currentDocument = WordprocessingMLPackage.load(new ByteArrayInputStream(bs));
//				if (openButton != null && !openButton.isDisposed()) {
//					openButton.setEnabled(true);
//				}
			} catch (Docx4JException e) {
				LoggerFactory.getLogger(getClass())
						.error("Error loading from byte array [" + bs + "] size [" + bs.length + "]");
				return false;
			}
			return true;
	}

	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		try {
			currentDocument = WordprocessingMLPackage.load(is);
//			if (openButton != null && !openButton.isDisposed()) {
//				openButton.setEnabled(true);
//			}
		} catch (Docx4JException e) {
			LoggerFactory.getLogger(getClass()).error("Error loading from stream [" + is + "]");
			return false;
		}
		return true;
	}

	@Override
	public boolean findOrReplace(String pattern, ReplaceCallback callBack) {
		if (getCurrentDocument() != null) {
			prepare();

			MainDocumentPart documentPart = currentDocument.getMainDocumentPart();

			RegexTextVisitor visitor = new RegexTextVisitor(currentDocument, pattern);
			TraversalUtil.visit(documentPart, visitor);
			visitor.replaceMatchingTexts(callBack);

			// replace header and footer
			RelationshipsPart relationshipPart = documentPart.getRelationshipsPart();
			List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
			for (Relationship relationship : relationships) {
				if (relationship.getType().equals(Namespaces.HEADER)
						|| relationship.getType().equals(Namespaces.FOOTER)) {
					JaxbXmlPart part = (JaxbXmlPart) relationshipPart.getPart(relationship);
					RegexTextVisitor partVisitor = new RegexTextVisitor(currentDocument, pattern);
					TraversalUtil.visit(part, partVisitor);
					partVisitor.replaceMatchingTexts(callBack);
				}
			}
		}
		return false;
	}

	public int findTextCount(String text) {
		if (currentDocument != null) {
			try {
				prepare();

				TextFindStAXHandler stAXHAndler = new TextFindStAXHandler(text);
				MainDocumentPart documentPart = currentDocument.getMainDocumentPart();
				// find header and footer
				RelationshipsPart relationshipPart = documentPart.getRelationshipsPart();
				List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
				for (Relationship relationship : relationships) {
					if (relationship.getType().equals(Namespaces.HEADER)
							|| relationship.getType().equals(Namespaces.FOOTER)) {
						JaxbXmlPart part = (JaxbXmlPart) relationshipPart.getPart(relationship);
						part.pipe(stAXHAndler);
					}
				}
				// find main document
				documentPart.pipe(stAXHAndler);
				return stAXHAndler.getCount();
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Error finding text [" + text + "]", e);
			}
		}
		return 0;
	}

	protected void prepare() {
		if (currentDocument != null) {
			try {
				VariablePrepare.prepare(currentDocument);
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Error preparing document", e);
			}
		}
	}

	@Override
	public byte[] storeToByteArray() {
		if (currentDocument != null) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				Docx4J.save(currentDocument, out, Docx4J.FLAG_SAVE_ZIP_FILE);
				return out.toByteArray();
			} catch (IOException | Docx4JException e) {
				LoggerFactory.getLogger(getClass()).error("Error writing to byte array");
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean insertTable(String text, int properties, String[][] contents, int[] columnSizes) {
		if (currentDocument != null) {
			FindTextVisitor visitor = new FindTextVisitor(text);
			TraversalUtil.visit(currentDocument.getMainDocumentPart(), visitor);
			List<Text> found = visitor.getFound();
			if (!found.isEmpty()) {
				for (Text foundText : found) {
					foundText.setValue(StringUtils.EMPTY);
					R r = (R) foundText.getParent();
					// do not insert table with no content
					if (contents.length > 0) {
						Tbl table = TableUtil.insertTable(r, properties, contents, columnSizes,
								DocxUtil.getDocumentWidth(currentDocument), true);
						TableUtil.addBorders(table, 1);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public Object insertTextAt(int posx, int posy, int width, int height, String text, int align) {
		if (currentDocument != null) {
			return TextBoxUtil.createTextBox(currentDocument, posx - 3, posy, width, height, text, align,
					currentStyleInfo);
		}
		return null;
	}

	@Override
	public boolean setFont(String name, int style, float size) {
		currentStyleInfo.setFontName(name);
		currentStyleInfo.setFontStyle(style);
		currentStyleInfo.setFontSize(size);
		return true;
	}

	@Override
	public boolean setStyle(int style) {
		currentStyleInfo.setFontStyle(style);
		return true;
	}

	@Override
	public Object insertText(String marke, String text, int align) {
		if (currentDocument != null) {
			FindTextVisitor visitor = new FindTextVisitor(marke);
			TraversalUtil.visit(currentDocument.getMainDocumentPart(), visitor);
			List<Text> found = visitor.getFound();
			if (!found.isEmpty()) {
				Object ret = null;
				for (Text foundText : found) {
					foundText.setValue(StringUtils.EMPTY);
					R r = (R) foundText.getParent();
					ret = TextUtil.insertText(r, text, align, currentStyleInfo);
				}
				return ret;
			}
		}
		return null;
	}

	@Override
	public Object insertText(Object pos, String text, int align) {
		return TextUtil.insertText(pos, text, align, currentStyleInfo);
	}

	@Override
	public boolean clear() {
		// not implemented
		return false;
	}


	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_MSWORD;
	}

	@Override
	public Object getCurrentDocument() {
		return currentDocument;
	}
}
