package ch.elexis.core.ui.perspective.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.ui.IPerspectiveDescriptor;

public interface IPerspectiveImportService {
	
	/**
	 * Imports a perspective from a file or web uri
	 * 
	 * @param uri
	 * @param iStateHandle
	 * @param openPerspectiveIfAdded
	 * @return IPerspectiveDescriptor
	 */
	public IPerspectiveDescriptor importPerspective(String uri, IStateCallback iStateHandle,
		boolean openPerspectiveIfAdded);
	
	public List<String> createLegacyPerspective(String path, MPerspective mPerspective)
		throws IOException;
	
	/**
	 * Returns the active window
	 * 
	 * @return
	 */
	public MTrimmedWindow getActiveWindow();
	
	/**
	 * Opens a perspective
	 * 
	 * @param existingPerspectiveDescriptor
	 */
	public void openPerspective(IPerspectiveDescriptor existingPerspectiveDescriptor);
	
	/**
	 * Loads a perspective from file
	 * 
	 * @param f
	 * @return
	 */
	public MPerspective loadPerspectiveFromFile(File f);
	
	/**
	 * Deletes a perspective by perspective id
	 * 
	 * @param perspectiveId
	 * @return
	 */
	public int deletePerspective(String perspectiveId);
	
	/**
	 * Saves the perspective by id with the given name
	 * 
	 * @param perspectiveId
	 * @param newName
	 */
	public void savePerspectiveAs(String perspectiveId, String newName);
	
	/**
	 * Closes a perspective with the given descriptor
	 * 
	 * @param existingPerspectiveDescriptor
	 * @return
	 */
	public int closePerspective(IPerspectiveDescriptor existingPerspectiveDescriptor);
}
