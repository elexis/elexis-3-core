package ch.elexis.core.ui.perspective.service;

import java.io.IOException;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public interface IPerspectiveExportService {

	/**
	 * Exports a perspective to a specific path from file system
	 *
	 * @param pathToExport
	 * @throws IOException
	 */
	public void exportPerspective(String pathToExport, String code, String newLabel) throws IOException;

	/**
	 * Exports a perspective to a specific {@link IVirtualFilesystemHandle}
	 * 
	 * @param sharedPerspectivePath
	 * @param code
	 * @param newLabel
	 */
	public void exportPerspective(IVirtualFilesystemHandle sharedPerspectivePath, String code, String newLabel)
			throws IOException;

}
