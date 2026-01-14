package ch.elexis.core.fhir.model.interfaces;

import java.util.Set;

/**
 * Mixin interface. If an object is an instance of this interface, it originates
 * from a FHIR request.
 */
public interface IFhirBased {

	/**
	 * @return whether this object is fully loaded, or only partial as defined in
	 *         https://hl7.org/fhir/search.html#_summary. If it is subsetted, it may
	 *         only serve as a "selection representation" as it only provides meta,
	 *         narrative and text
	 */
	public boolean isSubsetted();

	/**
	 * if {@link #isSubsetted()} loads the full object<br>
	 * MUST SUPPORT VREAD OR - what if this version does not exist anymore?
	 */
	public void load();

	/**
	 * This information is available for every fhir based object, even if
	 * {@link #isSubsetted()}. It should be used to represent the object in a
	 * selector
	 * 
	 * @return
	 */
	public String getNarrativeLabel();

	/**
	 * The narrative text carries tags that can be used for image or label providing
	 * 
	 * @return
	 */
	public Set<String> getNarrativeTags();

}
