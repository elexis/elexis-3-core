package ch.elexis.core.importer.div.importers.internal;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.importer.div.importers.IContactResolver;
import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.LabOrderState;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.hl7.model.OrcMessage;
import ch.rgw.tools.TimeTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Utility class that provides basic functionality a Lab importer implementation
 * needs. Lab importers should use this class!
 *
 * @author thomashu
 *
 */
@ApplicationScoped
@Component
public class LabImportUtil implements ILabImportUtil {

	private static Logger logger = LoggerFactory.getLogger(LabImportUtil.class);

	@Inject
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService modelService;

	@Inject
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, target = "(storeid=ch.elexis.data.store.omnivore)")
	private IDocumentStore documentStore;

	@Inject
	@Reference
	IEncounterService encounterService;

	/**
	 * Searches for a Labor matching the identifier as part of the Kuerzel or Name
	 * attribute. If no matching Labor is found, a new Labor is created with
	 * identifier as Kuerzel.
	 *
	 * @param identifier
	 * @return
	 */
	public ILaboratory getOrCreateLabor(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		ILaboratory ret = null;
		IQuery<ILaboratory> query = modelService.getQuery(ILaboratory.class);
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%" + identifier + "%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + identifier + "%");
		List<ILaboratory> results = query.execute();
		if (results.isEmpty()) {
			ret = modelService.create(ILaboratory.class);
			ret.setCode(identifier);
			ret.setDescription1("Labor " + identifier);
			modelService.save(ret);
			logger.warn("Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
		} else {
			ret = results.get(0);
			if (results.size() > 1) {
				logger.warn("Found more than one Labor for identifier [" + identifier
						+ "]. This can cause problems when importing results.");
			}
		}
		return ret;
	}

	public ILaboratory getLinkLabor(String identifier, IContactResolver<ILaboratory> contactResolver) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		ILaboratory ret = null;
		// check if there is a connection to an XID
		IQuery<IXid> query = modelService.getQuery(IXid.class);
		query.and(ModelPackage.Literals.IXID__DOMAIN, COMPARATOR.EQUALS, XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY);
		query.and(ModelPackage.Literals.IXID__DOMAIN_ID, COMPARATOR.EQUALS, identifier);
		List<IXid> xids = query.execute();
		if (!xids.isEmpty()) {
			if (xids.size() > 1) {
				LoggerFactory.getLogger(getClass()).error(xids.size() + " Laboratories found with xid ["
						+ XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY + "] [" + identifier + "] using first");
			}
			return xids.get(0).getObject(ILaboratory.class);
		} else if (contactResolver != null) {
			ret = contactResolver.getContact(Messages.Core_Select_Laboratory + " [" + identifier + "]");
			if (ret != null) {
				ret.addXid(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, identifier, true);
			}
		}
		return ret;
	}

	/**
	 * Searches for a LabItem with an existing LabMapping for the identifier and the
	 * labor. If there is no LabMapping, for backwards compatibility the LaborId and
	 * Kürzel attributes of all LabItems will be used to find a match.
	 *
	 * @param identifier
	 * @param labor
	 * @return
	 */
	public ILabItem getLabItem(String identifier, ILaboratory labor) {
		ILabMapping mapping = getLabMapping(labor, identifier).orElse(null);
		if (mapping != null) {
			return mapping.getItem();
		}

		ILabItem ret = null;
		IQuery<ILabItem> query = modelService.getQuery(ILabItem.class);
		query.and(ModelPackage.Literals.ILAB_ITEM__CODE, COMPARATOR.EQUALS, identifier);
		List<ILabItem> list = query.execute();
		if (!list.isEmpty()) {
			ret = list.get(0);
			if (list.size() > 1) {
				logger.warn("Found more than one LabItem for identifier [" + identifier + "] and Labor [" + labor
						+ "]. This can cause problems when importing results.");
			}
		}
		return ret;
	}

	/**
	 * Get a {@link ILabMapping} matching the {@link ILaboratory} and the itemName.
	 *
	 * @param laboratory
	 * @param itemName
	 * @return
	 */
	public Optional<ILabMapping> getLabMapping(ILaboratory labor, String itemName) {
		List<ILabMapping> mappings = getLabMappings(labor, itemName);
		if (!mappings.isEmpty()) {
			if (mappings.size() > 1) {
				throw new IllegalArgumentException(
						String.format("Found more then 1 mapping for origin id [%s] - [%s]", labor, itemName)); //$NON-NLS-1$
			}
			return Optional.of(mappings.get(0));
		}
		return Optional.empty();
	}

	/**
	 * Search for a LabResult with matching patient, item and timestamps. The
	 * timestamp attributes can be null if not relevant for the search, but at least
	 * one timestamp has to be specified.
	 *
	 * @param patient
	 * @param timestamp
	 * @param item
	 * @return
	 */
	@Override
	public List<ILabResult> getLabResults(IPatient patient, ILabItem item, TimeTool date, TimeTool analyseTime,
			TimeTool observationTime) {

		if (date == null && analyseTime == null && observationTime == null) {
			throw new IllegalArgumentException("No timestamp specified.");
		}

		IQuery<ILabResult> query = modelService.getQuery(ILabResult.class, true, false);
		query.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient);
		query.and(ModelPackage.Literals.ILAB_RESULT__ITEM, COMPARATOR.EQUALS, item);
		if (date != null) {
			query.and(ModelPackage.Literals.ILAB_RESULT__DATE, COMPARATOR.EQUALS, date.toLocalDate());
		}
		if (analyseTime != null) {
			query.and(ModelPackage.Literals.ILAB_RESULT__ANALYSE_TIME, COMPARATOR.EQUALS,
					analyseTime.toLocalDateTime());
		}
		if (observationTime != null) {
			query.and(ModelPackage.Literals.ILAB_RESULT__OBSERVATION_TIME, COMPARATOR.EQUALS,
					observationTime.toLocalDateTime());
		}
		return query.execute();
	}

	/**
	 * Import a list of TransientLabResults. Create LabOrder objects for new
	 * results.
	 */
	public String importLabResults(List<TransientLabResult> results, ImportHandler importHandler) {
		boolean overWriteAll = false;
		IMandator mandator = findMandatorForLabResults(results);
		boolean newResult = false;
		String orderId = null;
		for (TransientLabResult transientLabResult : results) {
			List<ILabResult> existing = getExistingResults(transientLabResult);
			if (existing.isEmpty()) {
				ILabResult labResult = createLabResult(transientLabResult, orderId, mandator);
				newResult = true;
				// use first created orderId for the rest of the import
				if (orderId == null) {
					ILabOrder createdOrder = labResult.getLabOrder();
					if (createdOrder != null) {
						orderId = createdOrder.getOrderId();
					}
				}
				LocalLockServiceHolder.get().acquireLock(labResult);
				LocalLockServiceHolder.get().releaseLock(labResult);
			} else {
				for (ILabResult labResult : existing) {
					if (overWriteAll) {
						LocalLockServiceHolder.get().acquireLock(labResult);
						transientLabResult.overwriteExisting(labResult);
						LocalLockServiceHolder.get().releaseLock(labResult);
						continue;
					}
					// dont bother user if result has the same value
					if (transientLabResult.isSameResult(labResult)) {
						logger.info("Result " + labResult.toString() + " already exists.");
						continue;
					}

					ImportHandler.OverwriteState retVal = importHandler.askOverwrite(transientLabResult.getPatient(),
							labResult, transientLabResult);

					if (retVal == ImportHandler.OverwriteState.OVERWRITE) {
						LocalLockServiceHolder.get().acquireLock(labResult);
						transientLabResult.overwriteExisting(labResult);
						LocalLockServiceHolder.get().releaseLock(labResult);
						continue;
					} else if (retVal == ImportHandler.OverwriteState.OVERWRITEALL) {
						overWriteAll = true;
						LocalLockServiceHolder.get().acquireLock(labResult);
						transientLabResult.overwriteExisting(labResult);
						LocalLockServiceHolder.get().releaseLock(labResult);
						continue;
					} else {
						logger.info("Will not overwrite labResult [" + labResult.getId() + "] due to user decision.");
					}
				}
			}
		}
		// if no result was created, no laborder was created, lookup existing orderid
		// with 1st result
		if (!newResult && !results.isEmpty()) {
			List<ILabResult> existing = getExistingResults(results.get(0));
			for (ILabResult iLabResult : existing) {
				ILabOrder existingOrder = iLabResult.getLabOrder();
				if (existingOrder != null) {
					orderId = existingOrder.getOrderId();
				}
			}
		}
		modelService.postEvent(ElexisEventTopics.EVENT_RELOAD, ILabResult.class);

		return orderId;
	}

	/**
	 * Tries to find the {@link Mandant} Id for given {@link TransientLabResult}
	 *
	 * @param results
	 * @return
	 */
	private IMandator findMandatorForLabResults(List<TransientLabResult> results) {
		if (results != null && !results.isEmpty()) {
			TransientLabResult transientLabResult = results.get(0);
			OrcMessage orcMessage = transientLabResult.getOrcMessage();
			// case 1 try to find mandant via orc message
			if (orcMessage != null && !orcMessage.getNames().isEmpty()) {
				for (String name : orcMessage.getNames()) {
					String[] splitNames = name.split(StringUtils.SPACE);
					int size = splitNames.length;
					if (size > 1) {
						IQuery<IMandator> query = modelService.getQuery(IMandator.class);
						query.startGroup();
						query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, splitNames[0], true);
						query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, splitNames[1], true);
						query.startGroup();
						query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, splitNames[1], true);
						query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, splitNames[0], true);
						query.orJoinGroups();
						List<IMandator> result = query.execute();
						if (result.size() == 1) {
							logger.debug("labimport - mandantor [" + result.get(0) + "] found with orc name db match");
							return result.get(0);
						}
					}
				}
				logger.warn("labimport - " + orcMessage.getNames().toString()
						+ " not found or not unique in db - try to find mandantor via last konsultation");
			}

			// case 2 try to find mandant via last consultation for patient
			IPatient iPatient = transientLabResult.getPatient();
			if (iPatient != null) {
				Optional<IPatient> patient = CoreModelServiceHolder.get().load(iPatient.getId(), IPatient.class);
				if (patient.isPresent()) {
					Optional<IEncounter> konsultation = encounterService.getLatestEncounter(patient.get());
					if (konsultation.isPresent()) {
						IMandator mandant = konsultation.get().getMandator();
						if (mandant != null && mandant.getId() != null) {
							logger.debug(
									"labimport - mandantor found [" + mandant.getId() + "] with last konsultation");
							return modelService.load(mandant.getId(), IMandator.class).get();
						}
					}
				}
			}
		}

		// case 3 use the current mandant
		Optional<IMandator> mandant = ContextServiceHolder.get().getActiveMandator();
		if (mandant.isPresent()) {
			logger.debug("labimport - use the active selected mandantor [" + mandant.get().getId() + "]");
			return modelService.load(mandant.get().getId(), IMandator.class).get();
		}
		throw new RuntimeException("No mandantor found!"); // should not happen!
	}

	/**
	 * Match for existing result with same item and date. Matching dates are checked
	 * for validity (not same as transmission date).
	 *
	 * @param transientLabResult
	 * @return
	 */
	private List<ILabResult> getExistingResults(TransientLabResult transientLabResult) {
		List<ILabResult> ret = Collections.emptyList();

		// don't overwrite documents
		if (!transientLabResult.getLabItem().getTyp().equals(LabItemTyp.DOCUMENT)) {
			if (transientLabResult.isObservationTime()) {
				ret = getLabResults(transientLabResult.getPatient(), transientLabResult.getLabItem(), null, null,
						transientLabResult.getObservationTime());
			} else if (transientLabResult.isAnalyseTime()) {
				ret = getLabResults(transientLabResult.getPatient(), transientLabResult.getLabItem(), null,
						transientLabResult.getAnalyseTime(), null);
			} else {
				ret = getLabResults(transientLabResult.getPatient(), transientLabResult.getLabItem(),
						transientLabResult.getDate(), null, null);
			}

			// filter by subid
			if (transientLabResult.getSubId() != null) {
				Iterator<ILabResult> it = ret.iterator();
				while (it.hasNext()) {
					ILabResult result = it.next();
					String subId = (String) result.getExtInfo(LabResultConstants.EXTINFO_HL7_SUBID);
					if (subId != null && !transientLabResult.getSubId().equals(subId)) {
						it.remove();
					}
				}
			}
		}
		return ret;
	}

	@Override
	public ILabItem createLabItem(String code, String name, ILaboratory origin, String male, String female, String unit,
			LabItemTyp typ, String group, String priority) {
		ILabItem ret = modelService.create(ILabItem.class);
		ret.setCode(code);
		ret.setName(name);
		ret.setReferenceMale(male);
		ret.setReferenceFemale(female);
		ret.setUnit(unit);
		ret.setTyp(typ);
		ret.setGroup(group);
		ret.setPriority(priority);
		modelService.save(ret);

		Optional<ILabMapping> existingMapping = getLabMapping(origin, code);
		if (!existingMapping.isPresent()) {
			ILabMapping mapping = modelService.create(ILabMapping.class);
			mapping.setItem(ret);
			mapping.setOrigin(origin);
			mapping.setItemName(code);
			modelService.save(mapping);
		}
		return ret;
	}

	private List<ILabMapping> getLabMappings(ILaboratory labor, String itemname) {
		INamedQuery<ILabMapping> query = modelService.getNamedQuery(ILabMapping.class, "origin", "itemname");
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("origin", labor);
		parameters.put("itemname", itemname);
		return query.executeWithParameters(parameters);
	}

	private List<ILabItem> getLabItems(String code, String name, LabItemTyp typ) {
		INamedQuery<ILabItem> query = modelService.getNamedQuery(ILabItem.class, "code", "name", "typ");
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("code", code);
		parameters.put("name", name);
		parameters.put("typ", typ);
		return query.executeWithParameters(parameters);
	}

	private List<ILabItem> getLabItems(String code, String name) {
		INamedQuery<ILabItem> query = modelService.getNamedQuery(ILabItem.class, "code", "name");
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("code", code);
		parameters.put("name", name);
		return query.executeWithParameters(parameters);
	}

	@Override
	public Optional<ILabItem> getDocumentLabItem(String shortname, String name, ILaboratory labor) {
		// lookup using mapping first
		List<ILabMapping> mappings = getLabMappings(labor, shortname);
		if (!mappings.isEmpty()) {
			for (ILabMapping iLabMapping : mappings) {
				if (iLabMapping.getItem().getTyp() == LabItemTyp.DOCUMENT) {
					return Optional.of(iLabMapping.getItem());
				}
			}
		}
		// try with name and typ
		List<ILabItem> existing = getLabItems(shortname, name, LabItemTyp.DOCUMENT);
		if (!existing.isEmpty()) {
			return Optional.of(existing.get(0));
		}
		return Optional.empty();
	}

	public Optional<ILabItem> getLabItem(String shortname, String name, ILaboratory labor) {
		// lookup using mapping first
		List<ILabMapping> mappings = getLabMappings(labor, shortname);
		if (!mappings.isEmpty()) {
			for (ILabMapping iLabMapping : mappings) {
				if (iLabMapping.getItem().getTyp() == LabItemTyp.DOCUMENT) {
					return Optional.of(iLabMapping.getItem());
				}
			}
		}
		// try with name ...
		List<ILabItem> existing = getLabItems(shortname, name);
		if (!existing.isEmpty()) {
			return Optional.of(existing.get(0));
		}
		return Optional.empty();
	}

	@Override
	public Optional<ILabItem> getLabItem(String shortname, String name, LabItemTyp typ) {
		List<ILabItem> existing = getLabItems(shortname, name, typ);
		if (!existing.isEmpty()) {
			return Optional.of(existing.get(0));
		}
		return Optional.empty();
	}

	@Override
	public void createDocumentManagerEntry(String title, String lab, byte[] data, String mimeType, TimeTool date,
			IPatient pat) {
		if (documentStore != null) {
			IDocument document = documentStore.createDocument(pat.getId(), title, lab);
			document.setCreated(date.getTime());
			document.setMimeType(mimeType);
			try {
				documentStore.saveDocument(document, new ByteArrayInputStream(data));
			} catch (ElexisException e) {
				logger.error("Error saving document", e);
			}
		} else {
			logger.warn("No IDocumentStore available, document [" + title + "] not created");
		}
	}

	@Override
	public ILabResult createLabResult(IPatient patient, TimeTool date, ILabItem labItem, String result, String comment,
			String refVal, ILaboratory laboratory, String subId, ILabOrder labOrder, String orderId, IMandator mandator,
			TimeTool observationTime, String groupName, boolean userResolved) {

		logger.info("Creating result with patient [" + patient.getId() + "] labitem [" + labItem + "] origin ["
				+ laboratory + "] observationTime [" + observationTime + "] labOrder [" + labOrder + "]");

		ILabResult labResult = modelService.create(ILabResult.class);
		labResult.setPatient(patient);
		labResult.setDate(date.toLocalDate());
		labResult.setItem(labItem);
		labResult.setOrigin(laboratory);
		if (patient.getGender() == Gender.FEMALE) {
			labResult.setReferenceFemale(refVal);
		} else {
			labResult.setReferenceMale(refVal);
		}
		labResult.setResult(result);
		labResult.setComment(comment);

		// create new ILabOrder or set result in existing
		if (labOrder == null) {
			if (observationTime == null) {
				logger.warn("Could not resolve observation time and time for ILabResult [{}], defaulting to now.",
						labResult.getId());
				observationTime = new TimeTool();
			}
			ILabOrder order = modelService.create(ILabOrder.class);
			order.setItem(labItem);
			order.setPatient(patient);
			order.setResult(labResult);
			order.setTimeStamp(LocalDateTime.now());
			order.setObservationTime(observationTime.toLocalDateTime());
			order.setMandator(mandator);
			order.setGroupName(groupName);
			order.setUserResolved(userResolved);
			if (orderId != null) {
				order.setOrderId(orderId);
			}
			ContextServiceHolder.get().getActiveUserContact().ifPresent(uc -> order.setUser(uc));
			labOrder = order;

		} else {
			labOrder.setResult(labResult);
		}

		if (subId != null) {
			labResult.setExtInfo(LabResultConstants.EXTINFO_HL7_SUBID, subId);
		}

		modelService.save(labResult);
		modelService.save(labOrder);
		return labResult;
	}

	public ILabResult createLabResult(TransientLabResult transientLabResult, String orderId, IMandator mandantor) {
		ILabResult labResult = null;

		List<ILabOrder> existing = getLabOrders(transientLabResult.getPatient(), transientLabResult.getLabItem(),
				LabOrderState.ORDERED);
		ILabOrder labOrder = null;
		if (existing == null || existing.isEmpty()) {

			TimeTool time = transientLabResult.getObservationTime();
			if (time == null) {
				time = transientLabResult.getDate();
			}

			labResult = transientLabResult.persist(null, orderId, mandantor, time, "Import");
			labOrder = (ILabOrder) labResult.getLabOrder();

		} else {
			// TODO for multiple entries we could check on which one the observationtime
			// matches
			labOrder = existing.get(0);
			labResult = transientLabResult.persist(labOrder, null, null, null, null);
		}

		labOrder.setState(LabOrderState.DONE_IMPORT);
		modelService.save(labOrder);

		return labResult;
	}

	private List<ILabOrder> getLabOrders(IPatient patient, ILabItem labItem, LabOrderState state) {
		INamedQuery<ILabOrder> query = modelService.getNamedQuery(ILabOrder.class, "item", "patient", "state");
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("item", labItem);
		parameters.put("patient", patient);
		parameters.put("state", state);
		return query.executeWithParameters(parameters);
	}

	@Override
	public void updateLabResult(ILabResult iLabResult, TransientLabResult transientLabResult) {
		if (iLabResult != null) {
			iLabResult.setExtInfo(LabResultConstants.EXTINFO_HL7_SUBID, transientLabResult.getSubId());
		}
	}

	@Override
	public <T> Optional<T> loadCoreModel(String id, Class<T> clazz) {
		return modelService.load(id, clazz);
	}

	@Override
	public Optional<IPatient> getPatientByCode(String code) {
		if (code != null) {
			INamedQuery<IPatient> namedQuery = CoreModelServiceHolder.get().getNamedQuery(IPatient.class, "code");
			List<IPatient> found = namedQuery.executeWithParameters(namedQuery.getParameterMap("code", code));
			if (!found.isEmpty()) {
				if (found.size() > 1) {
					logger.warn("Found " + found.size() + " patients with code [" + code + "] using first");
				}
				return Optional.of(found.get(0));
			}
		}
		return Optional.empty();
	}
}
