package ch.elexis.core.findings.templates.service;

import java.io.IOException;
import java.util.Optional;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.model.DataType;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.Type;
import ch.elexis.data.Patient;

public interface IFindingsTemplateService {
	
	/**
	 * Create a {@link IFinding} for a {@link Patient} using the provided {@link FindingsTemplate}.
	 * 
	 * @param load
	 * @param template
	 * @return
	 * @throws ElexisException
	 */
	public IFinding createFinding(Patient load, FindingsTemplate template) throws ElexisException;
	
	/**
	 * Get a findings templates model.
	 * 
	 * @param string
	 * @return
	 */
	public FindingsTemplates getFindingsTemplates(String string);
	
	/**
	 * Serializes and saves the model.
	 * 
	 * @param model
	 *            the serialized model
	 * @return
	 */
	public String saveFindingsTemplates(Optional<FindingsTemplates> model);
	
	/**
	 * Checks if there is a cycle in the references of the provided {@link FindingsTemplate}. If a
	 * cycle is detected an {@link ElexisException} is thrown.
	 * 
	 * @param findingsTemplate
	 * @param depth
	 *            current depth, most likely 0
	 * @param maxDepth
	 *            value of depth on which a cycle is assumed
	 * @throws ElexisException
	 */
	public void validateCycleDetection(FindingsTemplate findingsTemplate, int depth, int maxDepth)
		throws ElexisException;
	
	/**
	 * Import a {@link FindingsTemplates} model from a file.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public FindingsTemplates importTemplateFromFile(String path) throws IOException;
	
	/**
	 * Export a {@link FindingsTemplates} model to a file.
	 * 
	 * @param model
	 * @param path
	 * @throws IOException
	 */
	public void exportTemplateToFile(FindingsTemplates model, String path) throws IOException;
	
	/**
	 * Get a probably translated human readable text for the {@link DataType}.
	 * 
	 * @param element
	 * @return
	 */
	public String getDataTypeAsText(DataType element);
	
	/**
	 * Get a probably translated human readable text for the {@link Type}.
	 * 
	 * @param element
	 * @return
	 */
	public String getTypeAsText(Type element);
}
