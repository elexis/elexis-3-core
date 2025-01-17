package ch.elexis.core.services;

import java.io.File;
import java.util.Optional;

import ch.elexis.core.model.IDocument;

public interface IDocumentConverter {

	/**
	 * Convert the document to a temporary pdf file. The File should be deleted by
	 * the caller. Optional is empty if conversion failed.
	 *
	 * @param document
	 * @return
	 */
	public Optional<File> convertToPdf(IDocument document);

	/**
	 * Test if the converter is available.
	 *
	 * @return
	 */
	public boolean isAvailable();

	/**
	 * Check if the file format is supported for conversion.
	 *
	 * @param document
	 * @return true if supported, false otherwise
	 */
	public boolean isSupportedFile(IDocument document);

}
