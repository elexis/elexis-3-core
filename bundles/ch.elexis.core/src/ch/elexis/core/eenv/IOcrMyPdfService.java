package ch.elexis.core.eenv;

import java.io.IOException;

import ch.elexis.core.jdt.Nullable;

public interface IOcrMyPdfService {
	
	/**
	 * Send the provided application/pdf byte array to the ocrMyPdf service. If the pdf is already
	 * containing text, input is directly returned.
	 * 
	 * @param input
	 * @param parameters
	 *            to pass to the OcrMyPdf service, defaults to <code>-l deu</code>
	 * @return
	 * @throws IOException
	 * @see https://github.com/jbarlow83/OCRmyPDF/blob/master/misc/webservice.py
	 */
	public byte[] performOcr(byte[] input, @Nullable String parameters) throws IOException;
}
