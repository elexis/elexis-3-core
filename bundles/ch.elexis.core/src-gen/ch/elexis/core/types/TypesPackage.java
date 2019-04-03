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
package ch.elexis.core.types;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ch.elexis.core.types.TypesFactory
 * @model kind="package"
 * @generated
 */
public interface TypesPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "types";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://ch.elexis.core/model/types";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ch.elexis.core.types";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	TypesPackage eINSTANCE = ch.elexis.core.types.impl.TypesPackageImpl.init();

	/**
	 * The meta object id for the '{@link java.lang.Comparable <em>Comparable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.lang.Comparable
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getComparable()
	 * @generated
	 */
	int COMPARABLE = 0;

	/**
	 * The number of structural features of the '<em>Comparable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPARABLE_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link java.util.List <em>List</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.List
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getList()
	 * @generated
	 */
	int LIST = 1;

	/**
	 * The number of structural features of the '<em>List</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LIST_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link java.util.Map <em>Map</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Map
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMap()
	 * @generated
	 */
	int MAP = 2;

	/**
	 * The number of structural features of the '<em>Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MAP_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.types.ContactGender <em>Contact Gender</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.ContactGender
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getContactGender()
	 * @generated
	 */
	int CONTACT_GENDER = 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.types.RelationshipType <em>Relationship Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.RelationshipType
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getRelationshipType()
	 * @generated
	 */
	int RELATIONSHIP_TYPE = 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.types.AddressType <em>Address Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.AddressType
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getAddressType()
	 * @generated
	 */
	int ADDRESS_TYPE = 6;

	/**
	 * The meta object id for the '{@link ch.elexis.core.types.DocumentStatus <em>Document Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.DocumentStatus
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getDocumentStatus()
	 * @generated
	 */
	int DOCUMENT_STATUS = 7;

	/**
	 * The meta object id for the '<em>Money</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.rgw.tools.Money
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMoney()
	 * @generated
	 */
	int MONEY = 8;

	/**
	 * The meta object id for the '<em>Gender</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.Gender
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getGender()
	 * @generated
	 */
	int GENDER = 9;


	/**
	 * The meta object id for the '<em>Lab Item Typ</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.LabItemTyp
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLabItemTyp()
	 * @generated
	 */
	int LAB_ITEM_TYP = 10;


	/**
	 * The meta object id for the '<em>Country</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.Country
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getCountry()
	 * @generated
	 */
	int COUNTRY = 11;


	/**
	 * The meta object id for the '<em>Pathologic Description</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.PathologicDescription
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getPathologicDescription()
	 * @generated
	 */
	int PATHOLOGIC_DESCRIPTION = 12;


	/**
	 * The meta object id for the '<em>Local Date Time</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.LocalDateTime
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLocalDateTime()
	 * @generated
	 */
	int LOCAL_DATE_TIME = 13;


	/**
	 * The meta object id for the '<em>Input Stream</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.InputStream
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInputStream()
	 * @generated
	 */
	int INPUT_STREAM = 14;

	/**
	 * The meta object id for the '<em>Output Stream</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.OutputStream
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOutputStream()
	 * @generated
	 */
	int OUTPUT_STREAM = 15;


	/**
	 * The meta object id for the '<em>Local Date</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.LocalDate
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLocalDate()
	 * @generated
	 */
	int LOCAL_DATE = 16;


	/**
	 * The meta object id for the '<em>Xid Quality</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.XidQuality
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getXidQuality()
	 * @generated
	 */
	int XID_QUALITY = 17;


	/**
	 * The meta object id for the '<em>Lab Order State</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.LabOrderState
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLabOrderState()
	 * @generated
	 */
	int LAB_ORDER_STATE = 18;


	/**
	 * The meta object id for the '<em>Article Typ</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.ArticleTyp
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getArticleTyp()
	 * @generated
	 */
	int ARTICLE_TYP = 19;


	/**
	 * The meta object id for the '<em>Vat Info</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.VatInfo
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getVatInfo()
	 * @generated
	 */
	int VAT_INFO = 20;


	/**
	 * The meta object id for the '<em>Order Entry State</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.OrderEntryState
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOrderEntryState()
	 * @generated
	 */
	int ORDER_ENTRY_STATE = 21;

	/**
	 * The meta object id for the '<em>Article Sub Typ</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.ArticleSubTyp
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getArticleSubTyp()
	 * @generated
	 */
	int ARTICLE_SUB_TYP = 22;

	/**
	 * The meta object id for the '<em>Versioned Resource</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.rgw.tools.VersionedResource
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getVersionedResource()
	 * @generated
	 */
	int VERSIONED_RESOURCE = 23;

	/**
	 * The meta object id for the '<em>Entry Type</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.prescription.EntryType
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getEntryType()
	 * @generated
	 */
	int ENTRY_TYPE = 24;

	/**
	 * The meta object id for the '<em>Invoice State</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.InvoiceState
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInvoiceState()
	 * @generated
	 */
	int INVOICE_STATE = 25;

	/**
	 * The meta object id for the '<em>Chrono Unit</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.temporal.ChronoUnit
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getChronoUnit()
	 * @generated
	 */
	int CHRONO_UNIT = 26;

	/**
	 * The meta object id for the '<em>Billing Law</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ch.BillingLaw
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getBillingLaw()
	 * @generated
	 */
	int BILLING_LAW = 27;

	/**
	 * The meta object id for the '<em>Marital Status</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.MaritalStatus
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMaritalStatus()
	 * @generated
	 */
	int MARITAL_STATUS = 28;

	/**
	 * The meta object id for the '<em>Mime Type</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.MimeType
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMimeType()
	 * @generated
	 */
	int MIME_TYPE = 29;

	/**
	 * The meta object id for the '<em>Invoice Reject Code</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.InvoiceState.REJECTCODE
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInvoiceRejectCode()
	 * @generated
	 */
	int INVOICE_REJECT_CODE = 30;

	/**
	 * The meta object id for the '<em>Optional</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Optional
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOptional()
	 * @generated
	 */
	int OPTIONAL = 31;

	/**
	 * The meta object id for the '{@link ch.rgw.tools.Result <em>Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.rgw.tools.Result
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getResult()
	 * @generated
	 */
	int RESULT = 3;


	/**
	 * The number of structural features of the '<em>Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESULT_FEATURE_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link java.lang.Comparable <em>Comparable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Comparable</em>'.
	 * @see java.lang.Comparable
	 * @model instanceClass="java.lang.Comparable" typeParameters="T"
	 * @generated
	 */
	EClass getComparable();

	/**
	 * Returns the meta object for class '{@link java.util.List <em>List</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>List</em>'.
	 * @see java.util.List
	 * @model instanceClass="java.util.List" typeParameters="E"
	 * @generated
	 */
	EClass getList();

	/**
	 * Returns the meta object for class '{@link java.util.Map <em>Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Map</em>'.
	 * @see java.util.Map
	 * @model instanceClass="java.util.Map" typeParameters="K V"
	 * @generated
	 */
	EClass getMap();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.types.ContactGender <em>Contact Gender</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Contact Gender</em>'.
	 * @see ch.elexis.core.types.ContactGender
	 * @generated
	 */
	EEnum getContactGender();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.types.RelationshipType <em>Relationship Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Relationship Type</em>'.
	 * @see ch.elexis.core.types.RelationshipType
	 * @generated
	 */
	EEnum getRelationshipType();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.types.AddressType <em>Address Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Address Type</em>'.
	 * @see ch.elexis.core.types.AddressType
	 * @generated
	 */
	EEnum getAddressType();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.types.DocumentStatus <em>Document Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Document Status</em>'.
	 * @see ch.elexis.core.types.DocumentStatus
	 * @generated
	 */
	EEnum getDocumentStatus();

	/**
	 * Returns the meta object for data type '{@link ch.rgw.tools.Money <em>Money</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Money</em>'.
	 * @see ch.rgw.tools.Money
	 * @model instanceClass="ch.rgw.tools.Money"
	 * @generated
	 */
	EDataType getMoney();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.Gender <em>Gender</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Gender</em>'.
	 * @see ch.elexis.core.types.Gender
	 * @model instanceClass="ch.elexis.core.types.Gender"
	 * @generated
	 */
	EDataType getGender();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.LabItemTyp <em>Lab Item Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Lab Item Typ</em>'.
	 * @see ch.elexis.core.types.LabItemTyp
	 * @model instanceClass="ch.elexis.core.types.LabItemTyp"
	 * @generated
	 */
	EDataType getLabItemTyp();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.Country <em>Country</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Country</em>'.
	 * @see ch.elexis.core.types.Country
	 * @model instanceClass="ch.elexis.core.types.Country"
	 * @generated
	 */
	EDataType getCountry();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.PathologicDescription <em>Pathologic Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Pathologic Description</em>'.
	 * @see ch.elexis.core.types.PathologicDescription
	 * @model instanceClass="ch.elexis.core.types.PathologicDescription"
	 * @generated
	 */
	EDataType getPathologicDescription();

	/**
	 * Returns the meta object for data type '{@link java.time.LocalDateTime <em>Local Date Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Local Date Time</em>'.
	 * @see java.time.LocalDateTime
	 * @model instanceClass="java.time.LocalDateTime"
	 * @generated
	 */
	EDataType getLocalDateTime();

	/**
	 * Returns the meta object for data type '{@link java.io.InputStream <em>Input Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Input Stream</em>'.
	 * @see java.io.InputStream
	 * @model instanceClass="java.io.InputStream"
	 * @generated
	 */
	EDataType getInputStream();

	/**
	 * Returns the meta object for data type '{@link java.io.OutputStream <em>Output Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Output Stream</em>'.
	 * @see java.io.OutputStream
	 * @model instanceClass="java.io.OutputStream"
	 * @generated
	 */
	EDataType getOutputStream();

	/**
	 * Returns the meta object for data type '{@link java.time.LocalDate <em>Local Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Local Date</em>'.
	 * @see java.time.LocalDate
	 * @model instanceClass="java.time.LocalDate"
	 * @generated
	 */
	EDataType getLocalDate();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.XidQuality <em>Xid Quality</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Xid Quality</em>'.
	 * @see ch.elexis.core.model.XidQuality
	 * @model instanceClass="ch.elexis.core.model.XidQuality"
	 * @generated
	 */
	EDataType getXidQuality();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.LabOrderState <em>Lab Order State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Lab Order State</em>'.
	 * @see ch.elexis.core.model.LabOrderState
	 * @model instanceClass="ch.elexis.core.model.LabOrderState"
	 * @generated
	 */
	EDataType getLabOrderState();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.ArticleTyp <em>Article Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Article Typ</em>'.
	 * @see ch.elexis.core.types.ArticleTyp
	 * @model instanceClass="ch.elexis.core.types.ArticleTyp"
	 * @generated
	 */
	EDataType getArticleTyp();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.VatInfo <em>Vat Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Vat Info</em>'.
	 * @see ch.elexis.core.types.VatInfo
	 * @model instanceClass="ch.elexis.core.types.VatInfo"
	 * @generated
	 */
	EDataType getVatInfo();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.OrderEntryState <em>Order Entry State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Order Entry State</em>'.
	 * @see ch.elexis.core.model.OrderEntryState
	 * @model instanceClass="ch.elexis.core.model.OrderEntryState"
	 * @generated
	 */
	EDataType getOrderEntryState();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.types.ArticleSubTyp <em>Article Sub Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Article Sub Typ</em>'.
	 * @see ch.elexis.core.types.ArticleSubTyp
	 * @model instanceClass="ch.elexis.core.types.ArticleSubTyp"
	 * @generated
	 */
	EDataType getArticleSubTyp();

	/**
	 * Returns the meta object for data type '{@link ch.rgw.tools.VersionedResource <em>Versioned Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Versioned Resource</em>'.
	 * @see ch.rgw.tools.VersionedResource
	 * @model instanceClass="ch.rgw.tools.VersionedResource"
	 * @generated
	 */
	EDataType getVersionedResource();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.prescription.EntryType <em>Entry Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Entry Type</em>'.
	 * @see ch.elexis.core.model.prescription.EntryType
	 * @model instanceClass="ch.elexis.core.model.prescription.EntryType"
	 * @generated
	 */
	EDataType getEntryType();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.InvoiceState <em>Invoice State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Invoice State</em>'.
	 * @see ch.elexis.core.model.InvoiceState
	 * @model instanceClass="ch.elexis.core.model.InvoiceState"
	 * @generated
	 */
	EDataType getInvoiceState();

	/**
	 * Returns the meta object for data type '{@link java.time.temporal.ChronoUnit <em>Chrono Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Chrono Unit</em>'.
	 * @see java.time.temporal.ChronoUnit
	 * @model instanceClass="java.time.temporal.ChronoUnit"
	 * @generated
	 */
	EDataType getChronoUnit();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.ch.BillingLaw <em>Billing Law</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Billing Law</em>'.
	 * @see ch.elexis.core.model.ch.BillingLaw
	 * @model instanceClass="ch.elexis.core.model.ch.BillingLaw"
	 * @generated
	 */
	EDataType getBillingLaw();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.MaritalStatus <em>Marital Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Marital Status</em>'.
	 * @see ch.elexis.core.model.MaritalStatus
	 * @model instanceClass="ch.elexis.core.model.MaritalStatus"
	 * @generated
	 */
	EDataType getMaritalStatus();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.MimeType <em>Mime Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Mime Type</em>'.
	 * @see ch.elexis.core.model.MimeType
	 * @model instanceClass="ch.elexis.core.model.MimeType"
	 * @generated
	 */
	EDataType getMimeType();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.InvoiceState.REJECTCODE <em>Invoice Reject Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Invoice Reject Code</em>'.
	 * @see ch.elexis.core.model.InvoiceState.REJECTCODE
	 * @model instanceClass="ch.elexis.core.model.InvoiceState.REJECTCODE"
	 * @generated
	 */
	EDataType getInvoiceRejectCode();

	/**
	 * Returns the meta object for data type '{@link java.util.Optional <em>Optional</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Optional</em>'.
	 * @see java.util.Optional
	 * @model instanceClass="java.util.Optional" typeParameters="T"
	 * @generated
	 */
	EDataType getOptional();

	/**
	 * Returns the meta object for class '{@link ch.rgw.tools.Result <em>Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Result</em>'.
	 * @see ch.rgw.tools.Result
	 * @model instanceClass="ch.rgw.tools.Result" typeParameters="T"
	 * @generated
	 */
	EClass getResult();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	TypesFactory getTypesFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link java.lang.Comparable <em>Comparable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.lang.Comparable
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getComparable()
		 * @generated
		 */
		EClass COMPARABLE = eINSTANCE.getComparable();

		/**
		 * The meta object literal for the '{@link java.util.List <em>List</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.List
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getList()
		 * @generated
		 */
		EClass LIST = eINSTANCE.getList();

		/**
		 * The meta object literal for the '{@link java.util.Map <em>Map</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Map
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMap()
		 * @generated
		 */
		EClass MAP = eINSTANCE.getMap();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.types.ContactGender <em>Contact Gender</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.ContactGender
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getContactGender()
		 * @generated
		 */
		EEnum CONTACT_GENDER = eINSTANCE.getContactGender();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.types.RelationshipType <em>Relationship Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.RelationshipType
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getRelationshipType()
		 * @generated
		 */
		EEnum RELATIONSHIP_TYPE = eINSTANCE.getRelationshipType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.types.AddressType <em>Address Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.AddressType
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getAddressType()
		 * @generated
		 */
		EEnum ADDRESS_TYPE = eINSTANCE.getAddressType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.types.DocumentStatus <em>Document Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.DocumentStatus
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getDocumentStatus()
		 * @generated
		 */
		EEnum DOCUMENT_STATUS = eINSTANCE.getDocumentStatus();

		/**
		 * The meta object literal for the '<em>Money</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.rgw.tools.Money
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMoney()
		 * @generated
		 */
		EDataType MONEY = eINSTANCE.getMoney();

		/**
		 * The meta object literal for the '<em>Gender</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.Gender
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getGender()
		 * @generated
		 */
		EDataType GENDER = eINSTANCE.getGender();

		/**
		 * The meta object literal for the '<em>Lab Item Typ</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.LabItemTyp
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLabItemTyp()
		 * @generated
		 */
		EDataType LAB_ITEM_TYP = eINSTANCE.getLabItemTyp();

		/**
		 * The meta object literal for the '<em>Country</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.Country
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getCountry()
		 * @generated
		 */
		EDataType COUNTRY = eINSTANCE.getCountry();

		/**
		 * The meta object literal for the '<em>Pathologic Description</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.PathologicDescription
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getPathologicDescription()
		 * @generated
		 */
		EDataType PATHOLOGIC_DESCRIPTION = eINSTANCE.getPathologicDescription();

		/**
		 * The meta object literal for the '<em>Local Date Time</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.LocalDateTime
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLocalDateTime()
		 * @generated
		 */
		EDataType LOCAL_DATE_TIME = eINSTANCE.getLocalDateTime();

		/**
		 * The meta object literal for the '<em>Input Stream</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.InputStream
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInputStream()
		 * @generated
		 */
		EDataType INPUT_STREAM = eINSTANCE.getInputStream();

		/**
		 * The meta object literal for the '<em>Output Stream</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.OutputStream
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOutputStream()
		 * @generated
		 */
		EDataType OUTPUT_STREAM = eINSTANCE.getOutputStream();

		/**
		 * The meta object literal for the '<em>Local Date</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.LocalDate
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLocalDate()
		 * @generated
		 */
		EDataType LOCAL_DATE = eINSTANCE.getLocalDate();

		/**
		 * The meta object literal for the '<em>Xid Quality</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.XidQuality
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getXidQuality()
		 * @generated
		 */
		EDataType XID_QUALITY = eINSTANCE.getXidQuality();

		/**
		 * The meta object literal for the '<em>Lab Order State</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.LabOrderState
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLabOrderState()
		 * @generated
		 */
		EDataType LAB_ORDER_STATE = eINSTANCE.getLabOrderState();

		/**
		 * The meta object literal for the '<em>Article Typ</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.ArticleTyp
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getArticleTyp()
		 * @generated
		 */
		EDataType ARTICLE_TYP = eINSTANCE.getArticleTyp();

		/**
		 * The meta object literal for the '<em>Vat Info</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.VatInfo
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getVatInfo()
		 * @generated
		 */
		EDataType VAT_INFO = eINSTANCE.getVatInfo();

		/**
		 * The meta object literal for the '<em>Order Entry State</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.OrderEntryState
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOrderEntryState()
		 * @generated
		 */
		EDataType ORDER_ENTRY_STATE = eINSTANCE.getOrderEntryState();

		/**
		 * The meta object literal for the '<em>Article Sub Typ</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.ArticleSubTyp
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getArticleSubTyp()
		 * @generated
		 */
		EDataType ARTICLE_SUB_TYP = eINSTANCE.getArticleSubTyp();

		/**
		 * The meta object literal for the '<em>Versioned Resource</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.rgw.tools.VersionedResource
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getVersionedResource()
		 * @generated
		 */
		EDataType VERSIONED_RESOURCE = eINSTANCE.getVersionedResource();

		/**
		 * The meta object literal for the '<em>Entry Type</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.prescription.EntryType
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getEntryType()
		 * @generated
		 */
		EDataType ENTRY_TYPE = eINSTANCE.getEntryType();

		/**
		 * The meta object literal for the '<em>Invoice State</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.InvoiceState
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInvoiceState()
		 * @generated
		 */
		EDataType INVOICE_STATE = eINSTANCE.getInvoiceState();

		/**
		 * The meta object literal for the '<em>Chrono Unit</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.temporal.ChronoUnit
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getChronoUnit()
		 * @generated
		 */
		EDataType CHRONO_UNIT = eINSTANCE.getChronoUnit();

		/**
		 * The meta object literal for the '<em>Billing Law</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ch.BillingLaw
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getBillingLaw()
		 * @generated
		 */
		EDataType BILLING_LAW = eINSTANCE.getBillingLaw();

		/**
		 * The meta object literal for the '<em>Marital Status</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.MaritalStatus
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMaritalStatus()
		 * @generated
		 */
		EDataType MARITAL_STATUS = eINSTANCE.getMaritalStatus();

		/**
		 * The meta object literal for the '<em>Mime Type</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.MimeType
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getMimeType()
		 * @generated
		 */
		EDataType MIME_TYPE = eINSTANCE.getMimeType();

		/**
		 * The meta object literal for the '<em>Invoice Reject Code</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.InvoiceState.REJECTCODE
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getInvoiceRejectCode()
		 * @generated
		 */
		EDataType INVOICE_REJECT_CODE = eINSTANCE.getInvoiceRejectCode();

		/**
		 * The meta object literal for the '<em>Optional</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Optional
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getOptional()
		 * @generated
		 */
		EDataType OPTIONAL = eINSTANCE.getOptional();

		/**
		 * The meta object literal for the '{@link ch.rgw.tools.Result <em>Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.rgw.tools.Result
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getResult()
		 * @generated
		 */
		EClass RESULT = eINSTANCE.getResult();

	}

} //TypesPackage
