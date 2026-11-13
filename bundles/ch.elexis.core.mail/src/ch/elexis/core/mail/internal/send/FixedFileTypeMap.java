package ch.elexis.core.mail.internal.send;

import java.io.File;

import jakarta.activation.FileTypeMap;

final class FixedFileTypeMap extends FileTypeMap {

	private final String contentType;

	FixedFileTypeMap(String contentType) {
		this.contentType = contentType;
	}

	/** {@inheritDoc} */
	@Override
	public String getContentType(File file) {
		return contentType;
	}

	/** {@inheritDoc} */
	@Override
	public String getContentType(String fileName) {
		return contentType;
	}
}
