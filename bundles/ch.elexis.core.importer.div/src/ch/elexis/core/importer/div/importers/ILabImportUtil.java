package ch.elexis.core.importer.div.importers;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IXid;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.TimeTool;

/**
 * Definition of a utility class to help handling laboratory imports.
 * 
 * @author thomas
 *
 */
public interface ILabImportUtil {

	/**
	 * Resolve a {@link ch.elexis.core.model.ILabItem} from the {@link ILaboratory} by its code.
	 * 
	 * @param code
	 * @param labor
	 * @return
	 */
	public ILabItem getLabItem(String code, ILaboratory labor);

	/**
	 * Create a new {@link ILabItem} with the provided properties.
	 * 
	 * @param code
	 * @param name
	 * @param labor
	 * @param male
	 * @param female
	 * @param unit
	 * @param typ
	 * @param testGroupName
	 * @param nextTestGroupSequence
	 * @return
	 */
	public ILabItem createLabItem(String code, String name, ILaboratory origin, String male,
		String female, String unit, LabItemTyp typ, String group, String priority);

	/**
	 * Create a new {@link ILabResult} with the provided properties.
	 * 
	 * @param patient
	 * @param date
	 * @param labItem
	 * @param result
	 * @param comment
	 * @param refVal
	 * @param origin
	 * @param subId
	 * @param labOrder
	 * @param orderId
	 * @param mandantId
	 * @param observationTime
	 * @return
	 */
	public ILabResult createLabResult(IPatient patient, TimeTool date, ILabItem labItem,
		String result, String comment, String refVal, ILaboratory laboratory, String subId,
		ILabOrder labOrder, String orderId, IMandator mandator, TimeTool observationTime);
	
	/**
	 * Get a {@link ILabItem} with matching properties, and typ {@link LabItemTyp#DOCUMENT}.
	 * 
	 * @param liShort
	 * @param liName
	 * @param labor
	 * @return
	 */
	public Optional<ILabItem> getDocumentLabItem(String liShort, String liName, ILaboratory labor);

	/**
	 * Get a {@link ILabItem} with matching properties.
	 * 
	 * @param liShort
	 * @param liName
	 * @param labor
	 * @return
	 */
	public Optional<ILabItem> getLabItem(String liShort, String liName, ILaboratory labor);

	/**
	 * Get existing {@link ILabResult} matching the parameters.
	 * 
	 * @param patient
	 * @param item
	 * @param date
	 * @param analyseTime
	 * @param observationTime
	 * @return
	 */
	public List<ILabResult> getLabResults(IPatient patient, ILabItem item, TimeTool date,
		TimeTool analyseTime, TimeTool observationTime);
	
	String importLabResults(List<TransientLabResult> results, ImportHandler importHandler);

	void createDocumentManagerEntry(String title, String lab, byte[] data, String mimeType, TimeTool date, IPatient pat);

	void updateLabResult(ILabResult iLabResult, TransientLabResult transientLabResult);
	
	/**
	 * Find or set the {@link ILaboratory} this identifier is linked to via an {@link IXid}.
	 * 
	 * @param identifier
	 * @param contactResolver
	 * @return
	 */
	public ILaboratory getLinkLabor(String identifier,
		IContactResolver<ILaboratory> labContactResolver);
	
	/**
	 * Load a model object of type clazz with the id string.
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> loadCoreModel(String id, Class<T> clazz);
	
	public ILaboratory getOrCreateLabor(String identifier);
	
	public Optional<ILabItem> getLabItem(String shortname, String name, LabItemTyp document);
	
}
