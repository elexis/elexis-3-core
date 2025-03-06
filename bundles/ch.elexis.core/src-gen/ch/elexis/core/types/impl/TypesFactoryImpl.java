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

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.InvoiceState.REJECTCODE;
import ch.elexis.core.model.LabOrderState;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.XidQuality;
import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.model.prescription.EntryType;
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
import ch.elexis.core.types.TextTemplateCategory;
import ch.elexis.core.types.TypesFactory;
import ch.elexis.core.types.TypesPackage;
import ch.elexis.core.types.VatInfo;
import ch.rgw.tools.Money;
import ch.rgw.tools.VersionedResource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TypesFactoryImpl extends EFactoryImpl implements TypesFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static TypesFactory init() {
		try {
			TypesFactory theTypesFactory = (TypesFactory)EPackage.Registry.INSTANCE.getEFactory(TypesPackage.eNS_URI);
			if (theTypesFactory != null) {
				return theTypesFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TypesFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypesFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case TypesPackage.CONTACT_GENDER:
				return createContactGenderFromString(eDataType, initialValue);
			case TypesPackage.RELATIONSHIP_TYPE:
				return createRelationshipTypeFromString(eDataType, initialValue);
			case TypesPackage.ADDRESS_TYPE:
				return createAddressTypeFromString(eDataType, initialValue);
			case TypesPackage.DOCUMENT_STATUS:
				return createDocumentStatusFromString(eDataType, initialValue);
			case TypesPackage.APPOINTMENT_TYPE:
				return createAppointmentTypeFromString(eDataType, initialValue);
			case TypesPackage.APPOINTMENT_STATE:
				return createAppointmentStateFromString(eDataType, initialValue);
			case TypesPackage.TEXT_TEMPLATE_CATEGORY:
				return createTextTemplateCategoryFromString(eDataType, initialValue);
			case TypesPackage.MONEY:
				return createMoneyFromString(eDataType, initialValue);
			case TypesPackage.GENDER:
				return createGenderFromString(eDataType, initialValue);
			case TypesPackage.LAB_ITEM_TYP:
				return createLabItemTypFromString(eDataType, initialValue);
			case TypesPackage.COUNTRY:
				return createCountryFromString(eDataType, initialValue);
			case TypesPackage.PATHOLOGIC_DESCRIPTION:
				return createPathologicDescriptionFromString(eDataType, initialValue);
			case TypesPackage.LOCAL_DATE_TIME:
				return createLocalDateTimeFromString(eDataType, initialValue);
			case TypesPackage.INPUT_STREAM:
				return createInputStreamFromString(eDataType, initialValue);
			case TypesPackage.OUTPUT_STREAM:
				return createOutputStreamFromString(eDataType, initialValue);
			case TypesPackage.LOCAL_DATE:
				return createLocalDateFromString(eDataType, initialValue);
			case TypesPackage.XID_QUALITY:
				return createXidQualityFromString(eDataType, initialValue);
			case TypesPackage.LAB_ORDER_STATE:
				return createLabOrderStateFromString(eDataType, initialValue);
			case TypesPackage.ARTICLE_TYP:
				return createArticleTypFromString(eDataType, initialValue);
			case TypesPackage.VAT_INFO:
				return createVatInfoFromString(eDataType, initialValue);
			case TypesPackage.ORDER_ENTRY_STATE:
				return createOrderEntryStateFromString(eDataType, initialValue);
			case TypesPackage.ARTICLE_SUB_TYP:
				return createArticleSubTypFromString(eDataType, initialValue);
			case TypesPackage.VERSIONED_RESOURCE:
				return createVersionedResourceFromString(eDataType, initialValue);
			case TypesPackage.ENTRY_TYPE:
				return createEntryTypeFromString(eDataType, initialValue);
			case TypesPackage.INVOICE_STATE:
				return createInvoiceStateFromString(eDataType, initialValue);
			case TypesPackage.CHRONO_UNIT:
				return createChronoUnitFromString(eDataType, initialValue);
			case TypesPackage.BILLING_LAW:
				return createBillingLawFromString(eDataType, initialValue);
			case TypesPackage.MARITAL_STATUS:
				return createMaritalStatusFromString(eDataType, initialValue);
			case TypesPackage.MIME_TYPE:
				return createMimeTypeFromString(eDataType, initialValue);
			case TypesPackage.INVOICE_REJECT_CODE:
				return createInvoiceRejectCodeFromString(eDataType, initialValue);
			case TypesPackage.OPTIONAL:
				return createOptionalFromString(eDataType, initialValue);
			case TypesPackage.CHAR_ARRAY:
				return createcharArrayFromString(eDataType, initialValue);
			case TypesPackage.SERIES_TYPE:
				return createSeriesTypeFromString(eDataType, initialValue);
			case TypesPackage.ENDING_TYPE:
				return createEndingTypeFromString(eDataType, initialValue);
			case TypesPackage.LOCAL_TIME:
				return createLocalTimeFromString(eDataType, initialValue);
			case TypesPackage.PROCESS_STATUS:
				return createProcessStatusFromString(eDataType, initialValue);
			case TypesPackage.VISIBILITY:
				return createVisibilityFromString(eDataType, initialValue);
			case TypesPackage.PRIORITY:
				return createPriorityFromString(eDataType, initialValue);
			case TypesPackage.TYPE:
				return createTypeFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case TypesPackage.CONTACT_GENDER:
				return convertContactGenderToString(eDataType, instanceValue);
			case TypesPackage.RELATIONSHIP_TYPE:
				return convertRelationshipTypeToString(eDataType, instanceValue);
			case TypesPackage.ADDRESS_TYPE:
				return convertAddressTypeToString(eDataType, instanceValue);
			case TypesPackage.DOCUMENT_STATUS:
				return convertDocumentStatusToString(eDataType, instanceValue);
			case TypesPackage.APPOINTMENT_TYPE:
				return convertAppointmentTypeToString(eDataType, instanceValue);
			case TypesPackage.APPOINTMENT_STATE:
				return convertAppointmentStateToString(eDataType, instanceValue);
			case TypesPackage.TEXT_TEMPLATE_CATEGORY:
				return convertTextTemplateCategoryToString(eDataType, instanceValue);
			case TypesPackage.MONEY:
				return convertMoneyToString(eDataType, instanceValue);
			case TypesPackage.GENDER:
				return convertGenderToString(eDataType, instanceValue);
			case TypesPackage.LAB_ITEM_TYP:
				return convertLabItemTypToString(eDataType, instanceValue);
			case TypesPackage.COUNTRY:
				return convertCountryToString(eDataType, instanceValue);
			case TypesPackage.PATHOLOGIC_DESCRIPTION:
				return convertPathologicDescriptionToString(eDataType, instanceValue);
			case TypesPackage.LOCAL_DATE_TIME:
				return convertLocalDateTimeToString(eDataType, instanceValue);
			case TypesPackage.INPUT_STREAM:
				return convertInputStreamToString(eDataType, instanceValue);
			case TypesPackage.OUTPUT_STREAM:
				return convertOutputStreamToString(eDataType, instanceValue);
			case TypesPackage.LOCAL_DATE:
				return convertLocalDateToString(eDataType, instanceValue);
			case TypesPackage.XID_QUALITY:
				return convertXidQualityToString(eDataType, instanceValue);
			case TypesPackage.LAB_ORDER_STATE:
				return convertLabOrderStateToString(eDataType, instanceValue);
			case TypesPackage.ARTICLE_TYP:
				return convertArticleTypToString(eDataType, instanceValue);
			case TypesPackage.VAT_INFO:
				return convertVatInfoToString(eDataType, instanceValue);
			case TypesPackage.ORDER_ENTRY_STATE:
				return convertOrderEntryStateToString(eDataType, instanceValue);
			case TypesPackage.ARTICLE_SUB_TYP:
				return convertArticleSubTypToString(eDataType, instanceValue);
			case TypesPackage.VERSIONED_RESOURCE:
				return convertVersionedResourceToString(eDataType, instanceValue);
			case TypesPackage.ENTRY_TYPE:
				return convertEntryTypeToString(eDataType, instanceValue);
			case TypesPackage.INVOICE_STATE:
				return convertInvoiceStateToString(eDataType, instanceValue);
			case TypesPackage.CHRONO_UNIT:
				return convertChronoUnitToString(eDataType, instanceValue);
			case TypesPackage.BILLING_LAW:
				return convertBillingLawToString(eDataType, instanceValue);
			case TypesPackage.MARITAL_STATUS:
				return convertMaritalStatusToString(eDataType, instanceValue);
			case TypesPackage.MIME_TYPE:
				return convertMimeTypeToString(eDataType, instanceValue);
			case TypesPackage.INVOICE_REJECT_CODE:
				return convertInvoiceRejectCodeToString(eDataType, instanceValue);
			case TypesPackage.OPTIONAL:
				return convertOptionalToString(eDataType, instanceValue);
			case TypesPackage.CHAR_ARRAY:
				return convertcharArrayToString(eDataType, instanceValue);
			case TypesPackage.SERIES_TYPE:
				return convertSeriesTypeToString(eDataType, instanceValue);
			case TypesPackage.ENDING_TYPE:
				return convertEndingTypeToString(eDataType, instanceValue);
			case TypesPackage.LOCAL_TIME:
				return convertLocalTimeToString(eDataType, instanceValue);
			case TypesPackage.PROCESS_STATUS:
				return convertProcessStatusToString(eDataType, instanceValue);
			case TypesPackage.VISIBILITY:
				return convertVisibilityToString(eDataType, instanceValue);
			case TypesPackage.PRIORITY:
				return convertPriorityToString(eDataType, instanceValue);
			case TypesPackage.TYPE:
				return convertTypeToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ContactGender createContactGenderFromString(EDataType eDataType, String initialValue) {
		ContactGender result = ContactGender.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertContactGenderToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelationshipType createRelationshipTypeFromString(EDataType eDataType, String initialValue) {
		RelationshipType result = RelationshipType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRelationshipTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AddressType createAddressTypeFromString(EDataType eDataType, String initialValue) {
		AddressType result = AddressType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertAddressTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DocumentStatus createDocumentStatusFromString(EDataType eDataType, String initialValue) {
		DocumentStatus result = DocumentStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertDocumentStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AppointmentType createAppointmentTypeFromString(EDataType eDataType, String initialValue) {
		AppointmentType result = AppointmentType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertAppointmentTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AppointmentState createAppointmentStateFromString(EDataType eDataType, String initialValue) {
		AppointmentState result = AppointmentState.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertAppointmentStateToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TextTemplateCategory createTextTemplateCategoryFromString(EDataType eDataType, String initialValue) {
		TextTemplateCategory result = TextTemplateCategory.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertTextTemplateCategoryToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Money createMoneyFromString(EDataType eDataType, String initialValue) {
		return (Money)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertMoneyToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Gender createGenderFromString(EDataType eDataType, String initialValue) {
		return (Gender)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertGenderToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LabItemTyp createLabItemTypFromString(EDataType eDataType, String initialValue) {
		return (LabItemTyp)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLabItemTypToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Country createCountryFromString(EDataType eDataType, String initialValue) {
		return (Country)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertCountryToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PathologicDescription createPathologicDescriptionFromString(EDataType eDataType, String initialValue) {
		return (PathologicDescription)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPathologicDescriptionToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalDateTime createLocalDateTimeFromString(EDataType eDataType, String initialValue) {
		return (LocalDateTime)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLocalDateTimeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InputStream createInputStreamFromString(EDataType eDataType, String initialValue) {
		return (InputStream)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertInputStreamToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OutputStream createOutputStreamFromString(EDataType eDataType, String initialValue) {
		return (OutputStream)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertOutputStreamToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalDate createLocalDateFromString(EDataType eDataType, String initialValue) {
		return (LocalDate)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLocalDateToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public XidQuality createXidQualityFromString(EDataType eDataType, String initialValue) {
		return (XidQuality)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertXidQualityToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LabOrderState createLabOrderStateFromString(EDataType eDataType, String initialValue) {
		return (LabOrderState)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLabOrderStateToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ArticleTyp createArticleTypFromString(EDataType eDataType, String initialValue) {
		return (ArticleTyp)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertArticleTypToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VatInfo createVatInfoFromString(EDataType eDataType, String initialValue) {
		return (VatInfo)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertVatInfoToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OrderEntryState createOrderEntryStateFromString(EDataType eDataType, String initialValue) {
		return (OrderEntryState)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertOrderEntryStateToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ArticleSubTyp createArticleSubTypFromString(EDataType eDataType, String initialValue) {
		return (ArticleSubTyp)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertArticleSubTypToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VersionedResource createVersionedResourceFromString(EDataType eDataType, String initialValue) {
		return (VersionedResource)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertVersionedResourceToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EntryType createEntryTypeFromString(EDataType eDataType, String initialValue) {
		return (EntryType)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEntryTypeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InvoiceState createInvoiceStateFromString(EDataType eDataType, String initialValue) {
		return (InvoiceState)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertInvoiceStateToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ChronoUnit createChronoUnitFromString(EDataType eDataType, String initialValue) {
		return (ChronoUnit)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertChronoUnitToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BillingLaw createBillingLawFromString(EDataType eDataType, String initialValue) {
		return (BillingLaw)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertBillingLawToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MaritalStatus createMaritalStatusFromString(EDataType eDataType, String initialValue) {
		return (MaritalStatus)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertMaritalStatusToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MimeType createMimeTypeFromString(EDataType eDataType, String initialValue) {
		return (MimeType)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertMimeTypeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public REJECTCODE createInvoiceRejectCodeFromString(EDataType eDataType, String initialValue) {
		return (REJECTCODE)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertInvoiceRejectCodeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Optional<?> createOptionalFromString(EDataType eDataType, String initialValue) {
		return (Optional<?>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertOptionalToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public char[] createcharArrayFromString(EDataType eDataType, String initialValue) {
		return (char[])super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertcharArrayToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SeriesType createSeriesTypeFromString(EDataType eDataType, String initialValue) {
		return (SeriesType)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSeriesTypeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EndingType createEndingTypeFromString(EDataType eDataType, String initialValue) {
		return (EndingType)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEndingTypeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LocalTime createLocalTimeFromString(EDataType eDataType, String initialValue) {
		return (LocalTime)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLocalTimeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProcessStatus createProcessStatusFromString(EDataType eDataType, String initialValue) {
		return (ProcessStatus)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertProcessStatusToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Visibility createVisibilityFromString(EDataType eDataType, String initialValue) {
		return (Visibility)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertVisibilityToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Priority createPriorityFromString(EDataType eDataType, String initialValue) {
		return (Priority)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPriorityToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Type createTypeFromString(EDataType eDataType, String initialValue) {
		return (Type)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertTypeToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypesPackage getTypesPackage() {
		return (TypesPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static TypesPackage getPackage() {
		return TypesPackage.eINSTANCE;
	}

} //TypesFactoryImpl
