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

import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.InvoiceState.REJECTCODE;
import ch.elexis.core.model.LabOrderState;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.XidQuality;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.model.prescription.EntryType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;
import ch.elexis.core.types.ArticleSubTyp;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.ContactGender;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.RelationshipType;
import ch.elexis.core.types.TypesFactory;
import ch.elexis.core.types.TypesPackage;
import ch.elexis.core.types.VatInfo;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Comparable;
import java.time.LocalDate;

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
	private EClass resultEClass = null;

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
	private EEnum appointmentTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum appointmentStateEEnum = null;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType localDateTimeEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType inputStreamEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType outputStreamEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType localDateEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType xidQualityEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType labOrderStateEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType articleTypEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType vatInfoEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType orderEntryStateEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType articleSubTypEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType versionedResourceEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType entryTypeEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType invoiceStateEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType chronoUnitEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType billingLawEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType maritalStatusEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType mimeTypeEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType invoiceRejectCodeEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType optionalEDataType = null;

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
		Object registeredTypesPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		TypesPackageImpl theTypesPackage = registeredTypesPackage instanceof TypesPackageImpl ? (TypesPackageImpl)registeredTypesPackage : new TypesPackageImpl();

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
	@Override
	public EClass getComparable() {
		return comparableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getList() {
		return listEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMap() {
		return mapEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getContactGender() {
		return contactGenderEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRelationshipType() {
		return relationshipTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAddressType() {
		return addressTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getDocumentStatus() {
		return documentStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAppointmentType() {
		return appointmentTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAppointmentState() {
		return appointmentStateEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getMoney() {
		return moneyEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getGender() {
		return genderEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getLabItemTyp() {
		return labItemTypEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getCountry() {
		return countryEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getPathologicDescription() {
		return pathologicDescriptionEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getLocalDateTime() {
		return localDateTimeEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getInputStream() {
		return inputStreamEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getOutputStream() {
		return outputStreamEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getLocalDate() {
		return localDateEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getXidQuality() {
		return xidQualityEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getLabOrderState() {
		return labOrderStateEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getArticleTyp() {
		return articleTypEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getVatInfo() {
		return vatInfoEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getOrderEntryState() {
		return orderEntryStateEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getArticleSubTyp() {
		return articleSubTypEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getVersionedResource() {
		return versionedResourceEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEntryType() {
		return entryTypeEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getInvoiceState() {
		return invoiceStateEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getChronoUnit() {
		return chronoUnitEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getBillingLaw() {
		return billingLawEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getMaritalStatus() {
		return maritalStatusEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getMimeType() {
		return mimeTypeEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getInvoiceRejectCode() {
		return invoiceRejectCodeEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getOptional() {
		return optionalEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getResult() {
		return resultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
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

		resultEClass = createEClass(RESULT);

		// Create enums
		contactGenderEEnum = createEEnum(CONTACT_GENDER);
		relationshipTypeEEnum = createEEnum(RELATIONSHIP_TYPE);
		addressTypeEEnum = createEEnum(ADDRESS_TYPE);
		documentStatusEEnum = createEEnum(DOCUMENT_STATUS);
		appointmentTypeEEnum = createEEnum(APPOINTMENT_TYPE);
		appointmentStateEEnum = createEEnum(APPOINTMENT_STATE);

		// Create data types
		moneyEDataType = createEDataType(MONEY);
		genderEDataType = createEDataType(GENDER);
		labItemTypEDataType = createEDataType(LAB_ITEM_TYP);
		countryEDataType = createEDataType(COUNTRY);
		pathologicDescriptionEDataType = createEDataType(PATHOLOGIC_DESCRIPTION);
		localDateTimeEDataType = createEDataType(LOCAL_DATE_TIME);
		inputStreamEDataType = createEDataType(INPUT_STREAM);
		outputStreamEDataType = createEDataType(OUTPUT_STREAM);
		localDateEDataType = createEDataType(LOCAL_DATE);
		xidQualityEDataType = createEDataType(XID_QUALITY);
		labOrderStateEDataType = createEDataType(LAB_ORDER_STATE);
		articleTypEDataType = createEDataType(ARTICLE_TYP);
		vatInfoEDataType = createEDataType(VAT_INFO);
		orderEntryStateEDataType = createEDataType(ORDER_ENTRY_STATE);
		articleSubTypEDataType = createEDataType(ARTICLE_SUB_TYP);
		versionedResourceEDataType = createEDataType(VERSIONED_RESOURCE);
		entryTypeEDataType = createEDataType(ENTRY_TYPE);
		invoiceStateEDataType = createEDataType(INVOICE_STATE);
		chronoUnitEDataType = createEDataType(CHRONO_UNIT);
		billingLawEDataType = createEDataType(BILLING_LAW);
		maritalStatusEDataType = createEDataType(MARITAL_STATUS);
		mimeTypeEDataType = createEDataType(MIME_TYPE);
		invoiceRejectCodeEDataType = createEDataType(INVOICE_REJECT_CODE);
		optionalEDataType = createEDataType(OPTIONAL);
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
		addETypeParameter(resultEClass, "T");
		addETypeParameter(optionalEDataType, "T");

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(comparableEClass, Comparable.class, "Comparable", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		initEClass(listEClass, List.class, "List", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		initEClass(mapEClass, Map.class, "Map", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		initEClass(resultEClass, Result.class, "Result", IS_ABSTRACT, IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(contactGenderEEnum, ContactGender.class, "ContactGender");
		addEEnumLiteral(contactGenderEEnum, ContactGender.MALE);
		addEEnumLiteral(contactGenderEEnum, ContactGender.FEMALE);
		addEEnumLiteral(contactGenderEEnum, ContactGender.UNDEFINED);
		addEEnumLiteral(contactGenderEEnum, ContactGender.UNKNOWN);

		initEEnum(relationshipTypeEEnum, RelationshipType.class, "RelationshipType");
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.AGENERIC);
		addEEnumLiteral(relationshipTypeEEnum, RelationshipType.AUNKNOWN);
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

		initEEnum(appointmentTypeEEnum, AppointmentType.class, "AppointmentType");
		addEEnumLiteral(appointmentTypeEEnum, AppointmentType.DEFAULT);
		addEEnumLiteral(appointmentTypeEEnum, AppointmentType.FREE);
		addEEnumLiteral(appointmentTypeEEnum, AppointmentType.BOOKED);

		initEEnum(appointmentStateEEnum, AppointmentState.class, "AppointmentState");
		addEEnumLiteral(appointmentStateEEnum, AppointmentState.EMPTY);
		addEEnumLiteral(appointmentStateEEnum, AppointmentState.DEFAULT);

		// Initialize data types
		initEDataType(moneyEDataType, Money.class, "Money", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(genderEDataType, Gender.class, "Gender", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(labItemTypEDataType, LabItemTyp.class, "LabItemTyp", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(countryEDataType, Country.class, "Country", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(pathologicDescriptionEDataType, PathologicDescription.class, "PathologicDescription", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(localDateTimeEDataType, LocalDateTime.class, "LocalDateTime", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(inputStreamEDataType, InputStream.class, "InputStream", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(outputStreamEDataType, OutputStream.class, "OutputStream", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(localDateEDataType, LocalDate.class, "LocalDate", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(xidQualityEDataType, XidQuality.class, "XidQuality", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(labOrderStateEDataType, LabOrderState.class, "LabOrderState", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(articleTypEDataType, ArticleTyp.class, "ArticleTyp", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(vatInfoEDataType, VatInfo.class, "VatInfo", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(orderEntryStateEDataType, OrderEntryState.class, "OrderEntryState", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(articleSubTypEDataType, ArticleSubTyp.class, "ArticleSubTyp", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(versionedResourceEDataType, VersionedResource.class, "VersionedResource", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(entryTypeEDataType, EntryType.class, "EntryType", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(invoiceStateEDataType, InvoiceState.class, "InvoiceState", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(chronoUnitEDataType, ChronoUnit.class, "ChronoUnit", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(billingLawEDataType, BillingLaw.class, "BillingLaw", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(maritalStatusEDataType, MaritalStatus.class, "MaritalStatus", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(mimeTypeEDataType, MimeType.class, "MimeType", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(invoiceRejectCodeEDataType, REJECTCODE.class, "InvoiceRejectCode", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(optionalEDataType, Optional.class, "Optional", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //TypesPackageImpl
