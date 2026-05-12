package ch.elexis.core.ui.medication;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IPrescription;

public interface IMedicationInteractionUi {

	/**
	 * Get interaction {@link Image} for the provided {@link IPrescription} which
	 * must be part of the current relevant {@link IPrescription}s.
	 * 
	 * @param iPrescription
	 * @return
	 */
	public Image getImage(IPrescription iPrescription);

	/**
	 * Get a short text describing the interaction for the provided
	 * {@link IPrescription} which must be part of the current relevant
	 * {@link IPrescription}s.
	 * 
	 * @param iPrescription
	 * @return
	 */
	public String getText(IPrescription iPrescription);

	/**
	 * Set the current {@link IPrescription}s relevant for interaction check.
	 * 
	 * @param input
	 */
	public void setPrescriptions(List<IPrescription> input);
}
