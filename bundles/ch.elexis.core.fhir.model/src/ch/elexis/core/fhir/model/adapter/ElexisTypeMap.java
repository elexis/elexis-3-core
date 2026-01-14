package ch.elexis.core.fhir.model.adapter;

import java.util.HashMap;

import ch.elexis.core.fhir.model.impl.AbstractFhirModelAdapter;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.services.IModelService;

/**
 * Map type names from new {@link AbstractDBObjectIdDeleted} subclasses to
 * PersistentObject legacy type names. Use by
 * {@link IModelService#loadFromString(String)} and
 * {@link IModelService#storeToString(ch.elexis.core.model.Identifiable)}.
 *
 * @author thomas
 *
 */
public class ElexisTypeMap {

	private static final HashMap<Class<?>, String> classToStsMap;

	public static final String TYPE_BRIEF = "ch.elexis.data.Brief";
	public static final String TYPE_FALL = "ch.elexis.data.Fall";
	public static final String TYPE_KONSULTATION = "ch.elexis.data.Konsultation";
	public static final String TYPE_KONTAKT = "ch.elexis.data.Kontakt";
	public static final String TYPE_LABORATORY = "ch.elexis.data.Labor";
	public static final String TYPE_LABITEM = "ch.elexis.data.LabItem";
	public static final String TYPE_LABRESULT = "ch.elexis.data.LabResult";
	public static final String TYPE_ORGANISATION = "ch.elexis.data.Organisation";
	public static final String TYPE_PATIENT = "ch.elexis.data.Patient";
	public static final String TYPE_PERSON = "ch.elexis.data.Person";
	public static final String TYPE_RECHNUNG = "ch.elexis.data.Rechnung";
	public static final String TYPE_REMINDER = "ch.elexis.data.Reminder";
	public static final String TYPE_PRESCRIPTION = "ch.elexis.data.Prescription";
	public static final String TYPE_STOCK_ENTRY = "ch.elexis.data.StockEntry";
	public static final String TYPE_TERMIN = "ch.elexis.agenda.data.Termin";
	public static final String TYPE_USER = "ch.elexis.data.User";
	public static final String TYPE_VERRECHNET = "ch.elexis.data.Verrechnet";
	public static final String TYPE_MANDANT = "ch.elexis.data.Mandant";
	public static final String TYPE_BESTELLUNG = "ch.elexis.data.Bestellung";
	public static final String TYPE_BESTELLUNGENTRY = "ch.elexis.data.BestellungEntry";
	public static final String TYPE_AUF = "ch.elexis.data.AUF";
	public static final String TYPE_STICKER = "ch.elexis.data.Sticker";
	public static final String TYPE_REZEPT = "ch.elexis.data.Rezept";
	public static final String TYPE_VACCINATION = "at.medevit.elexis.impfplan.model.po.Vaccination";

	static {
		classToStsMap = new HashMap<>();

		// bi-directional mappable
		classToStsMap.put(IDocumentLetter.class, TYPE_BRIEF);
		classToStsMap.put(ICoverage.class, TYPE_FALL);
		classToStsMap.put(ILabItem.class, TYPE_LABITEM);
		classToStsMap.put(ILabResult.class, TYPE_LABRESULT);
		classToStsMap.put(IEncounter.class, TYPE_KONSULTATION);
		classToStsMap.put(IPrescription.class, TYPE_PRESCRIPTION);
		classToStsMap.put(IInvoice.class, TYPE_RECHNUNG);
		classToStsMap.put(IReminder.class, TYPE_REMINDER);
		classToStsMap.put(IStockEntry.class, TYPE_STOCK_ENTRY);
		classToStsMap.put(IAppointment.class, TYPE_TERMIN);
		classToStsMap.put(IUser.class, TYPE_USER);
		classToStsMap.put(IBilled.class, TYPE_VERRECHNET);
		classToStsMap.put(IOrder.class, TYPE_BESTELLUNG);
		classToStsMap.put(IOrderEntry.class, TYPE_BESTELLUNGENTRY);
		classToStsMap.put(ISickCertificate.class, TYPE_AUF);
		classToStsMap.put(ISticker.class, TYPE_STICKER);
		classToStsMap.put(IRecipe.class, TYPE_REZEPT);
		classToStsMap.put(IVaccination.class, TYPE_VACCINATION);

		classToStsMap.put(IPatient.class, TYPE_PATIENT);
		classToStsMap.put(IPerson.class, TYPE_PERSON);
		classToStsMap.put(IContact.class, TYPE_KONTAKT);

		classToStsMap.put(IUserGroup.class, "ch.elexis.core.model.UserGroup");
	}

	/**
	 *
	 * @param obj
	 * @return <code>null</code> if not resolvable, else the resp. Entity Type
	 */
	public static String getKeyForObject(AbstractFhirModelAdapter<?, ?> obj) {
		if (obj != null) {
			return classToStsMap.get(obj.getModelType());
		}

		return null;
	}
}