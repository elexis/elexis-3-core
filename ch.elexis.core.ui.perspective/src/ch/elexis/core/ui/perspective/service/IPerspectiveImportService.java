package ch.elexis.core.ui.perspective.service;

import java.io.IOException;
import java.io.InputStream;
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
	
	/**
	 * Creates a legacy perspective from a given {@link MPerspective} Returns a list of fastview
	 * view ids.
	 * 
	 * @param path
	 * @param mPerspective
	 * @return
	 * @throws IOException
	 */
	public List<String> createPerspectiveFromLegacy(String path, MPerspective mPerspective)
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
	 * Loads a perspective from {@link InputStream}, the stream will be closed afterwards.
	 * 
	 * @param f
	 * @return
	 */
	public MPerspective loadPerspectiveFromStream(InputStream in) throws IOException;
	
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
	
	/**
	 * Returns the position of the perspective inside the stack of opened perspectives.
	 * 
	 * @param perspectiveId
	 * @return returns -1 if the perspective with the given id is not found, or the perspective is
	 *         not inside the stack.
	 */
	public int isPerspectiveInStack(String perspectiveId);
}
