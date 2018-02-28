package ch.elexis.core.ui.perspective.service;

import java.io.IOException;

public interface IPerspectiveExportService {
	
	/**
	 * Exports a perspective to a specific path from file system
	 * 
	 * @param pathToExport
	 * @throws IOException
	 */
	public void exportPerspective(String pathToExport, String code, String newLabel)
		throws IOException;
	
}
