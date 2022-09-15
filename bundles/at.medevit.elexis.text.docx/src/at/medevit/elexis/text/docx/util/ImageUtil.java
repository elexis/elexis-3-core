package at.medevit.elexis.text.docx.util;

import java.io.IOException;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.R;

import ch.elexis.core.model.IImage;

public class ImageUtil {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	private static int imageCounter = 0;

	public static void insertImage(R run, IImage replacement, WordprocessingMLPackage currentDocument)
			throws IOException, Exception {

		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(currentDocument,
				replacement.getImage());

		Inline inline = imagePart.createImageInline(null, null, ++imageCounter, ++imageCounter, false, 0);

		Drawing drawing = wmlObjectFactory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);
	}
}
