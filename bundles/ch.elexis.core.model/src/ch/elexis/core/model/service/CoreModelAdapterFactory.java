package ch.elexis.core.model.service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.model.Contact;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Laboratory;
import ch.elexis.core.model.Organization;
import ch.elexis.core.model.Patient;
import ch.elexis.core.model.Person;

public class CoreModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private Map<Class<? extends AbstractIdDeleteModelAdapter<?>>, List<MappingEntry>> adapterToEntryMap;
	private Map<Class<? extends AbstractDBObjectIdDeleted>, List<MappingEntry>> entityToEntryMap;
	private Map<Class<?>, List<MappingEntry>> interfaceToEntryMap;
	private Map<Class<? extends AbstractIdDeleteModelAdapter<?>>, Constructor<?>> adapterConstructorMap;
	
	@Override
	protected void initializeMappings(){
		adapterToEntryMap = new HashMap<>();
		entityToEntryMap = new HashMap<>();
		interfaceToEntryMap = new HashMap<>();
		
		addMapping(new MappingEntry(IContact.class, Contact.class, Kontakt.class));
		addMapping(new MappingEntry(IPatient.class, Patient.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isPatient()));
		addMapping(new MappingEntry(IPerson.class, Person.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isPerson()));
		addMapping(new MappingEntry(IOrganization.class, Organization.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isOrganisation()));
		addMapping(new MappingEntry(ILaboratory.class, Laboratory.class, Kontakt.class)
			.adapterPreCondition(adapter -> ((Kontakt) adapter.getEntity()).isOrganisation()));
		
		initializeAdapterContructors();
	}
	
	private void addMapping(MappingEntry entry){
		List<MappingEntry> list = interfaceToEntryMap.get(entry.getInterfaceClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		interfaceToEntryMap.put(entry.getInterfaceClass(), list);
		
		list = adapterToEntryMap.get(entry.getAdapterClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		adapterToEntryMap.put(entry.getAdapterClass(), list);
		
		list = entityToEntryMap.get(entry.getEntityClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		entityToEntryMap.put(entry.getEntityClass(), list);
	}
	
	private void initializeAdapterContructors(){
		adapterConstructorMap = new HashMap<>();
		for (Class<? extends AbstractIdDeleteModelAdapter<?>> adapterClass : adapterToEntryMap
			.keySet()) {
			List<MappingEntry> entries = adapterToEntryMap.get(adapterClass);
			for (MappingEntry interfaceAdapterEntityEntry : entries) {
				adapterConstructorMap.put(adapterClass, getAdapterConstructor(adapterClass,
					interfaceAdapterEntityEntry.getEntityClass()));
			}
		}
	}
	
	@Override
	protected Constructor<?> getAdapterConstructor(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapter){
		return adapterConstructorMap.get(adapter);
	}
	
	private MappingEntry getSingleEntry(List<MappingEntry> entryList){
		if (entryList != null && !entryList.isEmpty()) {
			return entryList.get(0);
		}
		return null;
	}
	
	private MappingEntry getSingleEntry(List<MappingEntry> entryList,
		Predicate<MappingEntry> matcher){
		if (entryList != null && !entryList.isEmpty()) {
			for (MappingEntry mappingEntry : entryList) {
				if (matcher.test(mappingEntry)) {
					return mappingEntry;
				}
			}
			throw new IllegalStateException(
				"Ambiguous adapter mapping for [" + entryList.get(0).getAdapterClass() + "]");
		}
		return null;
	}
	
	@Override
	protected MappingEntry getMappingForInterface(Class<?> clazz){
		List<MappingEntry> entryList = interfaceToEntryMap.get(clazz);
		return getSingleEntry(entryList);
	}
	
	@Override
	protected MappingEntry getMappingForAdapter(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapter){
		List<MappingEntry> entryList = adapterToEntryMap.get(adapter);
		return getSingleEntry(entryList);
	}
	
	@Override
	protected MappingEntry getMappingEntity(Class<? extends AbstractDBObjectIdDeleted> entity,
		Class<?> interfaceClass){
		List<MappingEntry> entryList = entityToEntryMap.get(entity);
		if (interfaceClass != null) {
			return getSingleEntry(entryList, e -> e.getInterfaceClass() == interfaceClass);
		} else {
			return getSingleEntry(entryList);
		}
	}
}
