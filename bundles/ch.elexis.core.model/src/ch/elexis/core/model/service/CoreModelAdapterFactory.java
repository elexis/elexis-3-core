package ch.elexis.core.model.service;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Brief;
import ch.elexis.core.jpa.entities.DbImage;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.entities.VKPreis;
import ch.elexis.core.jpa.entities.ZusatzAdresse;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.model.Address;
import ch.elexis.core.model.BillingSystemFactor;
import ch.elexis.core.model.Config;
import ch.elexis.core.model.Contact;
import ch.elexis.core.model.Coverage;
import ch.elexis.core.model.DocumentBrief;
import ch.elexis.core.model.Encounter;
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Image;
import ch.elexis.core.model.Laboratory;
import ch.elexis.core.model.Mandator;
import ch.elexis.core.model.Organization;
import ch.elexis.core.model.Patient;
import ch.elexis.core.model.Person;
import ch.elexis.core.model.UserConfig;

public class CoreModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private static CoreModelAdapterFactory INSTANCE;
	
	public static synchronized CoreModelAdapterFactory getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new CoreModelAdapterFactory();
		}
		return INSTANCE;
	}
	
	private CoreModelAdapterFactory(){
		super();
	}
	
	@Override
	protected void initializeMappings(){
		addMapping(new MappingEntry(IConfig.class, Config.class,
			ch.elexis.core.jpa.entities.Config.class));
		addMapping(new MappingEntry(IUserConfig.class, UserConfig.class, Userconfig.class));
		
		addMapping(new MappingEntry(IUser.class, ch.elexis.core.model.User.class,
			ch.elexis.core.jpa.entities.User.class));
		
		addMapping(new MappingEntry(IXid.class, ch.elexis.core.model.Xid.class,
			ch.elexis.core.jpa.entities.Xid.class));
		addMapping(new MappingEntry(IBlob.class, ch.elexis.core.model.Blob.class,
			ch.elexis.core.jpa.entities.Heap.class));
		
		addMapping(new MappingEntry(IContact.class, Contact.class, Kontakt.class));
		addMapping(new MappingEntry(IPatient.class, Patient.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isPatient()
				&& ((Kontakt) adapter.getEntity()).isPerson())
			.adapterInitializer(adapter -> {
				((Patient) adapter).setPatient(true);
				((Patient) adapter).setPerson(true);
			}));
		addMapping(new MappingEntry(IPerson.class, Person.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isPerson())
			.adapterInitializer(adapter -> ((Person) adapter).setPerson(true)));
		addMapping(new MappingEntry(IOrganization.class, Organization.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isOrganisation())
			.adapterInitializer(adapter -> ((Organization) adapter).setOrganization(true)));
		addMapping(new MappingEntry(ILaboratory.class, Laboratory.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isLaboratory())
			.adapterInitializer(adapter -> ((Laboratory) adapter).setLaboratory(true)));
		addMapping(new MappingEntry(IMandator.class, Mandator.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isMandator())
			.adapterInitializer(adapter -> ((Mandator) adapter).setMandator(true)));
		
		addMapping(new MappingEntry(ICoverage.class, Coverage.class, Fall.class));
		
		addMapping(
			new MappingEntry(IBillingSystemFactor.class, BillingSystemFactor.class, VKPreis.class));
		
		addMapping(new MappingEntry(IEncounter.class, Encounter.class, Behandlung.class));
		
		addMapping(new MappingEntry(IAddress.class, Address.class, ZusatzAdresse.class));
		
		addMapping(new MappingEntry(IDocumentLetter.class, DocumentBrief.class, Brief.class));
		
		addMapping(new MappingEntry(ILabItem.class, ch.elexis.core.model.LabItem.class,
			ch.elexis.core.jpa.entities.LabItem.class));
		addMapping(new MappingEntry(ILabResult.class, ch.elexis.core.model.LabResult.class,
			ch.elexis.core.jpa.entities.LabResult.class));
		addMapping(new MappingEntry(ILabOrder.class, ch.elexis.core.model.LabOrder.class,
			ch.elexis.core.jpa.entities.LabOrder.class));
		addMapping(new MappingEntry(ILabMapping.class, ch.elexis.core.model.LabMapping.class,
			ch.elexis.core.jpa.entities.LabMapping.class));
		
		addMapping(new MappingEntry(IArticle.class, ch.elexis.core.model.TypedArticle.class,
			ch.elexis.core.jpa.entities.Artikel.class));
		
		addMapping(new MappingEntry(IStock.class, ch.elexis.core.model.Stock.class,
			ch.elexis.core.jpa.entities.Stock.class));
		addMapping(new MappingEntry(IStockEntry.class, ch.elexis.core.model.StockEntry.class,
			ch.elexis.core.jpa.entities.StockEntry.class));
		
		addMapping(new MappingEntry(IOrder.class, ch.elexis.core.model.Order.class,
			ch.elexis.core.jpa.entities.Bestellung.class));
		addMapping(new MappingEntry(IOrderEntry.class, ch.elexis.core.model.OrderEntry.class,
			ch.elexis.core.jpa.entities.BestellungEntry.class));
		
		addMapping(new MappingEntry(IImage.class, Image.class, DbImage.class));
	}
}
