package ch.elexis.core.model.service;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Brief;
import ch.elexis.core.jpa.entities.BriefVorlage;
import ch.elexis.core.jpa.entities.DbImage;
import ch.elexis.core.jpa.entities.Diagnosis;
import ch.elexis.core.jpa.entities.Eigenleistung;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.KontaktAdressJoint;
import ch.elexis.core.jpa.entities.Leistungsblock;
import ch.elexis.core.jpa.entities.TagesNachricht;
import ch.elexis.core.jpa.entities.Termin;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.entities.VKPreis;
import ch.elexis.core.jpa.entities.Verrechnet;
import ch.elexis.core.jpa.entities.VerrechnetCopy;
import ch.elexis.core.jpa.entities.Zahlung;
import ch.elexis.core.jpa.entities.ZusatzAdresse;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.model.AccountTransaction;
import ch.elexis.core.model.Address;
import ch.elexis.core.model.Appointment;
import ch.elexis.core.model.Billed;
import ch.elexis.core.model.BillingSystemFactor;
import ch.elexis.core.model.CodeElementBlock;
import ch.elexis.core.model.Config;
import ch.elexis.core.model.Contact;
import ch.elexis.core.model.Coverage;
import ch.elexis.core.model.CustomService;
import ch.elexis.core.model.DayMessage;
import ch.elexis.core.model.DiagnosisReference;
import ch.elexis.core.model.DocumentLetter;
import ch.elexis.core.model.DocumentTemplate;
import ch.elexis.core.model.Encounter;
import ch.elexis.core.model.FreeTextDiagnosis;
import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IBlobSecondary;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IDayMessage;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.IRight;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Image;
import ch.elexis.core.model.Invoice;
import ch.elexis.core.model.InvoiceBilled;
import ch.elexis.core.model.Laboratory;
import ch.elexis.core.model.Mandator;
import ch.elexis.core.model.Message;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.Organization;
import ch.elexis.core.model.Patient;
import ch.elexis.core.model.Payment;
import ch.elexis.core.model.Person;
import ch.elexis.core.model.Prescription;
import ch.elexis.core.model.Recipe;
import ch.elexis.core.model.RelatedContact;
import ch.elexis.core.model.Reminder;
import ch.elexis.core.model.ReminderResponsibleLink;
import ch.elexis.core.model.Right;
import ch.elexis.core.model.Role;
import ch.elexis.core.model.SickCertificate;
import ch.elexis.core.model.TextTemplate;
import ch.elexis.core.model.UserConfig;
import ch.elexis.core.model.Vaccination;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class CoreModelAdapterFactory extends AbstractModelAdapterFactory {

	private static CoreModelAdapterFactory INSTANCE;

	public static synchronized CoreModelAdapterFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CoreModelAdapterFactory();
		}
		return INSTANCE;
	}

	private CoreModelAdapterFactory() {
		super();
	}

	@Override
	protected void initializeMappings() {
		addMapping(new MappingEntry(IAppointment.class, Appointment.class, Termin.class));
		addMapping(new MappingEntry(IDayMessage.class, DayMessage.class, TagesNachricht.class));

		addMapping(new MappingEntry(IReminder.class, Reminder.class, ch.elexis.core.jpa.entities.Reminder.class));
		addMapping(new MappingEntry(IReminderResponsibleLink.class, ReminderResponsibleLink.class,
				ch.elexis.core.jpa.entities.ReminderResponsibleLink.class));

		addMapping(new MappingEntry(IConfig.class, Config.class, ch.elexis.core.jpa.entities.Config.class));
		addMapping(new MappingEntry(IUserConfig.class, UserConfig.class, Userconfig.class));

		addMapping(
				new MappingEntry(IUser.class, ch.elexis.core.model.User.class, ch.elexis.core.jpa.entities.User.class));
		addMapping(new MappingEntry(IUserGroup.class, ch.elexis.core.model.UserGroup.class,
				ch.elexis.core.jpa.entities.UserGroup.class));

		addMapping(new MappingEntry(ISticker.class, ch.elexis.core.model.Sticker.class,
				ch.elexis.core.jpa.entities.Sticker.class));

		addMapping(new MappingEntry(IXid.class, ch.elexis.core.model.Xid.class, ch.elexis.core.jpa.entities.Xid.class));
		addMapping(
				new MappingEntry(IBlob.class, ch.elexis.core.model.Blob.class, ch.elexis.core.jpa.entities.Heap.class));
		addMapping(new MappingEntry(IBlobSecondary.class, ch.elexis.core.model.BlobSecondary.class,
				ch.elexis.core.jpa.entities.Heap2.class));

		addMapping(new MappingEntry(IContact.class, Contact.class, Kontakt.class)
				.adapterInitializer(this::setContactDiscriminator));
		addMapping(new MappingEntry(IPatient.class, Patient.class, Kontakt.class).adapterPreCondition(
				adapter -> ((Kontakt) adapter.getEntity()).isPatient() && ((Kontakt) adapter.getEntity()).isPerson())
				.queryPreCondition(query -> {
					query.and(ModelPackage.Literals.ICONTACT__PERSON, COMPARATOR.EQUALS, true);
					query.and(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, true);
				}).adapterInitializer(this::setContactDiscriminator));
		addMapping(new MappingEntry(IPerson.class, Person.class, Kontakt.class)
				.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isPerson()).queryPreCondition(query -> {
					query.and(ModelPackage.Literals.ICONTACT__PERSON, COMPARATOR.EQUALS, true);
				}).adapterInitializer(this::setContactDiscriminator));
		addMapping(new MappingEntry(IOrganization.class, Organization.class, Kontakt.class)
				.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isOrganisation())
				.queryPreCondition(query -> {
					query.and(ModelPackage.Literals.ICONTACT__ORGANIZATION, COMPARATOR.EQUALS, true);
					query.and(ModelPackage.Literals.ICONTACT__PERSON, COMPARATOR.EQUALS, false);
				}).adapterInitializer(this::setContactDiscriminator));
		addMapping(new MappingEntry(ILaboratory.class, Laboratory.class, Kontakt.class)
				.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isLaboratory())
				.queryPreCondition(query -> {
					query.and(ModelPackage.Literals.ICONTACT__LABORATORY, COMPARATOR.EQUALS, true);
				}).adapterInitializer(this::setContactDiscriminator));
		addMapping(new MappingEntry(IMandator.class, Mandator.class, Kontakt.class)
				.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isMandator())
				.queryPreCondition(query -> {
					query.and(ModelPackage.Literals.ICONTACT__MANDATOR, COMPARATOR.EQUALS, true);
				}).adapterInitializer(this::setContactDiscriminator));

		addMapping(new MappingEntry(ICoverage.class, Coverage.class, Fall.class));
		addMapping(new MappingEntry(IEncounter.class, Encounter.class, Behandlung.class));

		addMapping(new MappingEntry(IInvoice.class, Invoice.class, ch.elexis.core.jpa.entities.Invoice.class));
		addMapping(new MappingEntry(IInvoiceBilled.class, InvoiceBilled.class, VerrechnetCopy.class));

		addMapping(new MappingEntry(IAccountTransaction.class, AccountTransaction.class,
				ch.elexis.core.jpa.entities.AccountTransaction.class));
		addMapping(new MappingEntry(IPayment.class, Payment.class, Zahlung.class));

		addMapping(new MappingEntry(IBillingSystemFactor.class, BillingSystemFactor.class, VKPreis.class));
		addMapping(new MappingEntry(IBilled.class, Billed.class, Verrechnet.class));
		addMapping(new MappingEntry(IArticle.class, ch.elexis.core.model.TypedArticle.class,
				ch.elexis.core.jpa.entities.Artikel.class));
		addMapping(new MappingEntry(ICustomService.class, CustomService.class, Eigenleistung.class));
		addMapping(new MappingEntry(ICodeElementBlock.class, CodeElementBlock.class, Leistungsblock.class));

		addMapping(new MappingEntry(IArticleDefaultSignature.class, ch.elexis.core.model.ArticleDefaultSignature.class,
				ch.elexis.core.jpa.entities.DefaultSignature.class));

		addMapping(new MappingEntry(IDiagnosisReference.class, DiagnosisReference.class, Diagnosis.class));
		addMapping(new MappingEntry(IFreeTextDiagnosis.class, FreeTextDiagnosis.class,
				ch.elexis.core.jpa.entities.FreeTextDiagnosis.class));

		addMapping(new MappingEntry(IAddress.class, Address.class, ZusatzAdresse.class));

		addMapping(new MappingEntry(IRelatedContact.class, RelatedContact.class, KontaktAdressJoint.class));

		addMapping(new MappingEntry(IDocumentLetter.class, DocumentLetter.class, Brief.class));
		addMapping(new MappingEntry(IDocumentTemplate.class, DocumentTemplate.class, BriefVorlage.class));

		addMapping(new MappingEntry(IPrescription.class, Prescription.class,
				ch.elexis.core.jpa.entities.Prescription.class));
		addMapping(new MappingEntry(IRecipe.class, Recipe.class, ch.elexis.core.jpa.entities.Rezept.class));
		addMapping(new MappingEntry(IVaccination.class, Vaccination.class,
				ch.elexis.core.jpa.entities.Vaccination.class));

		addMapping(new MappingEntry(IRole.class, Role.class, ch.elexis.core.jpa.entities.Role.class));
		addMapping(new MappingEntry(IRight.class, Right.class, ch.elexis.core.jpa.entities.Right.class));

		addMapping(new MappingEntry(ILabItem.class, ch.elexis.core.model.LabItem.class,
				ch.elexis.core.jpa.entities.LabItem.class));
		addMapping(new MappingEntry(ILabResult.class, ch.elexis.core.model.LabResult.class,
				ch.elexis.core.jpa.entities.LabResult.class));
		addMapping(new MappingEntry(ILabOrder.class, ch.elexis.core.model.LabOrder.class,
				ch.elexis.core.jpa.entities.LabOrder.class));
		addMapping(new MappingEntry(ILabMapping.class, ch.elexis.core.model.LabMapping.class,
				ch.elexis.core.jpa.entities.LabMapping.class));

		addMapping(new MappingEntry(IStock.class, ch.elexis.core.model.Stock.class,
				ch.elexis.core.jpa.entities.Stock.class));
		addMapping(new MappingEntry(IStockEntry.class, ch.elexis.core.model.StockEntry.class,
				ch.elexis.core.jpa.entities.StockEntry.class));

		addMapping(new MappingEntry(IOrder.class, ch.elexis.core.model.Order.class,
				ch.elexis.core.jpa.entities.Bestellung.class));
		addMapping(new MappingEntry(IOrderEntry.class, ch.elexis.core.model.OrderEntry.class,
				ch.elexis.core.jpa.entities.BestellungEntry.class));

		addMapping(new MappingEntry(IImage.class, Image.class, DbImage.class));

		addMapping(new MappingEntry(IMessage.class, Message.class, ch.elexis.core.jpa.entities.Message.class));

		addMapping(new MappingEntry(ITextTemplate.class, TextTemplate.class,
				ch.elexis.core.jpa.entities.TextTemplate.class));

		addMapping(
				new MappingEntry(ISickCertificate.class, SickCertificate.class, ch.elexis.core.jpa.entities.AUF.class));
	}

	private Object setContactDiscriminator(AbstractIdModelAdapter<?> adapter) {
		if (adapter instanceof IContact) {
			IContact contact = (IContact) adapter;
			contact.setPerson(adapter instanceof IPerson);
			contact.setPatient(adapter instanceof IPatient);
			contact.setOrganization(adapter instanceof IOrganization);
			contact.setLaboratory(adapter instanceof ILaboratory);
			contact.setMandator(adapter instanceof IMandator);
		}
		return null;
	}
}
