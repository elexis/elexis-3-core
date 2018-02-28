/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.types.impl;

import ch.elexis.core.types.AddressType;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import ch.elexis.core.types.ContactGender;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.RelationshipType;
import ch.elexis.core.types.TypesFactory;
import ch.elexis.core.types.TypesPackage;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import java.lang.Comparable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TypesPackageImpl extends EPackageImpl implements TypesPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass comparableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mapEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum contactTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum contactGenderEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum relationshipTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum addressTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum documentStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType moneyEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType timeToolEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType genderEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType labItemTypEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType countryEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType pathologicDescriptionEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see ch.elexis.core.types.TypesPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TypesPackageImpl() {
		super(eNS_URI, TypesFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link TypesPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TypesPackage init() {
		if (isInited) return (TypesPackage)EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI);

		// Obtain or create and register package
		TypesPackageImpl theTypesPackage = (TypesPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TypesPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TypesPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theTypesPackage.createPackageContents();

		// Initialize created meta-data
		theTypesPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTypesPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TypesPackage.eNS_URI, theTypesPackage);
		return theTypesPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getComparable() {
		return comparableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getList() {
		return listEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMap() {
		return mapEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getContactType() {
		return contactTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getContactGender() {
		return contactGenderEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getRelationshipType() {
		return relationshipTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getAddressType() {
		return addressTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDocumentStatus() {
		return documentStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getMoney() {
		return moneyEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getTimeTool() {
		return timeToolEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getGender() {
		return genderEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getLabItemTyp() {
		return labItemTypEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getCountry() {
		return countryEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getPathologicDescription() {
		return pathologicDescriptionEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypesFactory getTypesFactory() {
		return (TypesFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		comparableEClass = createEClass(COMPARABLE);

		listEClass = createEClass(LIST);

		mapEClass = createEClass(MAP);

		// Create enums
		contactTypeEEnum = createEEnum(CONTACT_TYPE);
		contactGenderEEnum = createEEnum(CONTACT_GENDER);
		relationshipTypeEEnum = createEEnum(RELATIONSHIP_TYPE);
		addressTypeEEnum = createEEnum(ADDRESS_TYPE);
		documentStatusEEnum = createEEnum(DOCUMENT_STATUS);

		// Create data types
		moneyEDataType = createEDataType(MONEY);
		timeToolEDataType = createEDataType(TIME_TOOL);
		genderEDataType = createEDataType(GENDER);
		labItemTypEDataType = createEDataType(LAB_ITEM_TYP);
		countryEDataType = createEDataType(COUNTRY);
		pathologicDescriptionEDataType = createEDataType(PATHOLOGIC_DESCRIPTION);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters
		addETypeParameter(comparableEClass, "T");
		addETypeParameter(listEClass, "E");
		addETypeParameter(mapEClass, "K");
		addETypeParameter(mapEClass, "V");

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(comparableEClass, Comparable.class, "Comparable", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		initEClass(listEClass, List.class, "List", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		initEClass(mapEClass, Map.class, "Map", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(contactTypeEEnum, ContactType.class, "ContactType");
		addEEnumLiteral(contactTypeEEnum, ContactType.PERSON);
		addEEnumLiteral(contactTypeEEnum, ContactType.ORGANIZATION);
		addEEnumLiteral(contactTypeEEnum, ContactType.MANDATOR);
		addEEnumLiteral(contactTypeEEnum, ContactType.LABORATORY);
		addEEnumLiteral(contactTypeEEnum, ContactType.PATIENT);
		addEEnumLiteral(contactTypeEEnum, ContactType.UNKNOWN);
		addEEnumLiteral(contactTypeEEnum, ContactType.USER);

		initEEnum(contactGenderEEnum, ContactGender.class, "ContactGender");
		addEEnumLiteral(contactGenderEEnum, ContactGender.MALE);
		addEEnumLiteral(contactGenderEEnum, ContactGender.FEMALE);
		addEEnumLiteral(contactGenderEEnum, ContactGender.UNDEFINED);
		addEEnumLiteral(contactGenderEEnum, ContactGender.UNKNOWN);

		initEEnum(relationshipTypeEEnum, RelationshipType.class, "RelationshipType");
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.AGENERIC);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.BUSINESS_EMPLOYER);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.BUSINESS_EMPLOYEE);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.FAMILY_PARENT);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.FAMILY_CHILD);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.FAMILY_GUARDIAN);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.FAMILY_ICE);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.FAMILY_ALTERNATIVE);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.WELFARE_GENERAL_PRACTITIONER);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.WELFARE_PATIENT);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.WELFARE_CONSULTANT);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.WELFARE_INSURER);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.WELFARE_INSUREE);

		initEEnum(addressTypeEEnum, AddressType.class, "AddressType");
		addEEnumLiteral(addressTypeEEnum, AddressType.PRINCIPAL_RESIDENCE);
		addEEnumLiteral(addressTypeEEnum, AddressType.SECONDARY_RESIDENCE);
		addEEnumLiteral(addressTypeEEnum, AddressType.HOLIDAY_HOME);
		addEEnumLiteral(addressTypeEEnum, AddressType.PLACE_OF_RESIDENCE);
		addEEnumLiteral(addressTypeEEnum, AddressType.EMPLOYER);
		addEEnumLiteral(addressTypeEEnum, AddressType.FAMILY_FRIENDS);
		addEEnumLiteral(addressTypeEEnum, AddressType.ATTACHMENT_FIGURE);
		addEEnumLiteral(addressTypeEEnum, AddressType.PRISON);
		addEEnumLiteral(addressTypeEEnum, AddressType.NURSING_HOME);
		addEEnumLiteral(addressTypeEEnum, AddressType.OTHER);

		initEEnum(documentStatusEEnum, DocumentStatus.class, "DocumentStatus");
		addEEnumLiteral(documentStatusEEnum, DocumentStatus.NEW);
		addEEnumLiteral(documentStatusEEnum, DocumentStatus.CHANGED);
		addEEnumLiteral(documentStatusEEnum, DocumentStatus.VALIDATED);
		addEEnumLiteral(documentStatusEEnum, DocumentStatus.SENT);
		addEEnumLiteral(documentStatusEEnum, DocumentStatus.RECIVED);

		// Initialize data types
		initEDataType(moneyEDataType, Money.class, "Money", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(timeToolEDataType, TimeTool.class, "TimeTool", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(genderEDataType, Gender.class, "Gender", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(labItemTypEDataType, LabItemTyp.class, "LabItemTyp", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(countryEDataType, Country.class, "Country", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(pathologicDescriptionEDataType, PathologicDescription.class, "PathologicDescription", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //TypesPackageImpl
