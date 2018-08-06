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
package ch.elexis.core.model;

import ch.elexis.core.types.TypesPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

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
 * @see ch.elexis.core.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://ch.elexis.core/model/model";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ch.elexis.core.model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = ch.elexis.core.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.Identifiable <em>Identifiable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.Identifiable
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIdentifiable()
	 * @generated
	 */
	int IDENTIFIABLE = 0;

	/**
	 * The number of structural features of the '<em>Identifiable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IContact <em>IContact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IContact
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIContact()
	 * @generated
	 */
	int ICONTACT = 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IXid <em>IXid</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IXid
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIXid()
	 * @generated
	 */
	int IXID = 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICodeElement
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElement()
	 * @generated
	 */
	int ICODE_ELEMENT = 17;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ISticker
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISticker()
	 * @generated
	 */
	int ISTICKER = 16;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPerson <em>IPerson</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPerson
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPerson()
	 * @generated
	 */
	int IPERSON = 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPatient
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPatient()
	 * @generated
	 */
	int IPATIENT = 7;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IUser <em>IUser</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IUser
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUser()
	 * @generated
	 */
	int IUSER = 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.Deleteable <em>Deleteable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.Deleteable
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getDeleteable()
	 * @generated
	 */
	int DELETEABLE = 1;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETEABLE__DELETED = 0;

	/**
	 * The number of structural features of the '<em>Deleteable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETEABLE_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Domain Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN_ID = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Quality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__QUALITY = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__OBJECT_ID = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IXid</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__USER = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PERSON = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__ORGANIZATION = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__LABORATORY = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION1 = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION2 = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION3 = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__CODE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__COUNTRY = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__ZIP = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__CITY = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__STREET = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PHONE1 = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PHONE2 = IDENTIFIABLE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__FAX = IDENTIFIABLE_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__EMAIL = IDENTIFIABLE_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__WEBSITE = IDENTIFIABLE_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__MOBILE = IDENTIFIABLE_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__COMMENT = IDENTIFIABLE_FEATURE_COUNT + 21;

	/**
	 * The number of structural features of the '<em>IContact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DELETED = ICONTACT__DELETED;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__MANDATOR = ICONTACT__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__USER = ICONTACT__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__PATIENT = ICONTACT__PATIENT;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__PERSON = ICONTACT__PERSON;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__ORGANIZATION = ICONTACT__ORGANIZATION;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__LABORATORY = ICONTACT__LABORATORY;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DESCRIPTION1 = ICONTACT__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DESCRIPTION2 = ICONTACT__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DESCRIPTION3 = ICONTACT__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__CODE = ICONTACT__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__COUNTRY = ICONTACT__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__ZIP = ICONTACT__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__CITY = ICONTACT__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__STREET = ICONTACT__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__PHONE1 = ICONTACT__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__PHONE2 = ICONTACT__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__FAX = ICONTACT__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__EMAIL = ICONTACT__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__WEBSITE = ICONTACT__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__MOBILE = ICONTACT__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__COMMENT = ICONTACT__COMMENT;

	/**
	 * The feature id for the '<em><b>Date Of Birth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DATE_OF_BIRTH = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Gender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__GENDER = ICONTACT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Titel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__TITEL = ICONTACT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Titel Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__TITEL_SUFFIX = ICONTACT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>First Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__FIRST_NAME = ICONTACT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__LAST_NAME = ICONTACT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>IPerson</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabItem
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabItem()
	 * @generated
	 */
	int ILAB_ITEM = 10;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabResult
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabResult()
	 * @generated
	 */
	int ILAB_RESULT = 11;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabOrder
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabOrder()
	 * @generated
	 */
	int ILAB_ORDER = 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabMapping <em>ILab Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabMapping
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabMapping()
	 * @generated
	 */
	int ILAB_MAPPING = 13;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPeriod
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPeriod()
	 * @generated
	 */
	int IPERIOD = 25;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDocument <em>IDocument</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDocument
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocument()
	 * @generated
	 */
	int IDOCUMENT = 14;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICategory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICategory()
	 * @generated
	 */
	int ICATEGORY = 18;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IHistory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIHistory()
	 * @generated
	 */
	int IHISTORY = 19;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDiagnose <em>IDiagnose</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDiagnose
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnose()
	 * @generated
	 */
	int IDIAGNOSE = 20;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillable <em>IBillable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillable
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillable()
	 * @generated
	 */
	int IBILLABLE = 21;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICoverage <em>ICoverage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICoverage
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICoverage()
	 * @generated
	 */
	int ICOVERAGE = 22;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IOrganization <em>IOrganization</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IOrganization
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrganization()
	 * @generated
	 */
	int IORGANIZATION = 5;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__DELETED = ICONTACT__DELETED;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__MANDATOR = ICONTACT__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__USER = ICONTACT__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__PATIENT = ICONTACT__PATIENT;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__PERSON = ICONTACT__PERSON;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__ORGANIZATION = ICONTACT__ORGANIZATION;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__LABORATORY = ICONTACT__LABORATORY;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__DESCRIPTION1 = ICONTACT__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__DESCRIPTION2 = ICONTACT__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__DESCRIPTION3 = ICONTACT__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__CODE = ICONTACT__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__COUNTRY = ICONTACT__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__ZIP = ICONTACT__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__CITY = ICONTACT__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__STREET = ICONTACT__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__PHONE1 = ICONTACT__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__PHONE2 = ICONTACT__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__FAX = ICONTACT__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__EMAIL = ICONTACT__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__WEBSITE = ICONTACT__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__MOBILE = ICONTACT__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__COMMENT = ICONTACT__COMMENT;

	/**
	 * The number of structural features of the '<em>IOrganization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILaboratory <em>ILaboratory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILaboratory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILaboratory()
	 * @generated
	 */
	int ILABORATORY = 6;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__DELETED = IORGANIZATION__DELETED;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__MANDATOR = IORGANIZATION__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__USER = IORGANIZATION__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__PATIENT = IORGANIZATION__PATIENT;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__PERSON = IORGANIZATION__PERSON;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__ORGANIZATION = IORGANIZATION__ORGANIZATION;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__LABORATORY = IORGANIZATION__LABORATORY;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__DESCRIPTION1 = IORGANIZATION__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__DESCRIPTION2 = IORGANIZATION__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__DESCRIPTION3 = IORGANIZATION__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__CODE = IORGANIZATION__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__COUNTRY = IORGANIZATION__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__ZIP = IORGANIZATION__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__CITY = IORGANIZATION__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__STREET = IORGANIZATION__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__PHONE1 = IORGANIZATION__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__PHONE2 = IORGANIZATION__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__FAX = IORGANIZATION__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__EMAIL = IORGANIZATION__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__WEBSITE = IORGANIZATION__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__MOBILE = IORGANIZATION__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__COMMENT = IORGANIZATION__COMMENT;

	/**
	 * The number of structural features of the '<em>ILaboratory</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY_FEATURE_COUNT = IORGANIZATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DELETED = IPERSON__DELETED;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__MANDATOR = IPERSON__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__USER = IPERSON__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PATIENT = IPERSON__PATIENT;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PERSON = IPERSON__PERSON;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__ORGANIZATION = IPERSON__ORGANIZATION;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__LABORATORY = IPERSON__LABORATORY;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DESCRIPTION1 = IPERSON__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DESCRIPTION2 = IPERSON__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DESCRIPTION3 = IPERSON__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__CODE = IPERSON__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__COUNTRY = IPERSON__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__ZIP = IPERSON__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__CITY = IPERSON__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__STREET = IPERSON__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PHONE1 = IPERSON__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PHONE2 = IPERSON__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__FAX = IPERSON__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__EMAIL = IPERSON__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__WEBSITE = IPERSON__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__MOBILE = IPERSON__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__COMMENT = IPERSON__COMMENT;

	/**
	 * The feature id for the '<em><b>Date Of Birth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DATE_OF_BIRTH = IPERSON__DATE_OF_BIRTH;

	/**
	 * The feature id for the '<em><b>Gender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__GENDER = IPERSON__GENDER;

	/**
	 * The feature id for the '<em><b>Titel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__TITEL = IPERSON__TITEL;

	/**
	 * The feature id for the '<em><b>Titel Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__TITEL_SUFFIX = IPERSON__TITEL_SUFFIX;

	/**
	 * The feature id for the '<em><b>First Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__FIRST_NAME = IPERSON__FIRST_NAME;

	/**
	 * The feature id for the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__LAST_NAME = IPERSON__LAST_NAME;

	/**
	 * The feature id for the '<em><b>Diagnosen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DIAGNOSEN = IPERSON_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Risk</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__RISK = IPERSON_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Family Anamnese</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__FAMILY_ANAMNESE = IPERSON_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Personal Anamnese</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PERSONAL_ANAMNESE = IPERSON_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Allergies</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__ALLERGIES = IPERSON_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>IPatient</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT_FEATURE_COUNT = IPERSON_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDocumentLetter
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentLetter()
	 * @generated
	 */
	int IDOCUMENT_LETTER = 15;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IConfig <em>IConfig</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IConfig
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIConfig()
	 * @generated
	 */
	int ICONFIG = 23;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IUserConfig <em>IUser Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IUserConfig
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUserConfig()
	 * @generated
	 */
	int IUSER_CONFIG = 24;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IMandator <em>IMandator</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IMandator
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIMandator()
	 * @generated
	 */
	int IMANDATOR = 8;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__DELETED = ICONTACT__DELETED;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__MANDATOR = ICONTACT__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__USER = ICONTACT__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__PATIENT = ICONTACT__PATIENT;

	/**
	 * The feature id for the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__PERSON = ICONTACT__PERSON;

	/**
	 * The feature id for the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__ORGANIZATION = ICONTACT__ORGANIZATION;

	/**
	 * The feature id for the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__LABORATORY = ICONTACT__LABORATORY;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__DESCRIPTION1 = ICONTACT__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__DESCRIPTION2 = ICONTACT__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__DESCRIPTION3 = ICONTACT__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__CODE = ICONTACT__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__COUNTRY = ICONTACT__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__ZIP = ICONTACT__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__CITY = ICONTACT__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__STREET = ICONTACT__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__PHONE1 = ICONTACT__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__PHONE2 = ICONTACT__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__FAX = ICONTACT__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__EMAIL = ICONTACT__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__WEBSITE = ICONTACT__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__MOBILE = ICONTACT__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__COMMENT = ICONTACT__COMMENT;

	/**
	 * The number of structural features of the '<em>IMandator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__USERNAME = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__PASSWORD = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Assigned Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ASSIGNED_CONTACT = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Roles</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ROLES = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IUser</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__TYP = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Reference Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__REFERENCE_MALE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Reference Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__REFERENCE_FEMALE = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__UNIT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__GROUP = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__PRIORITY = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__CODE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__NAME = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Digits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__DIGITS = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__VISIBLE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Formula</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__FORMULA = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Loinc Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__LOINC_CODE = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Billing Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__BILLING_CODE = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The number of structural features of the '<em>ILab Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__RESULT = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__COMMENT = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Reference Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REFERENCE_MALE = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Reference Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REFERENCE_FEMALE = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__UNIT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__DATE = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Observation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__OBSERVATION_TIME = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Analyse Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ANALYSE_TIME = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Transmission Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__TRANSMISSION_TIME = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Pathologic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATHOLOGIC = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Pathologic Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATHOLOGIC_DESCRIPTION = DELETEABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ORIGIN = DELETEABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ITEM = DELETEABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATIENT = DELETEABLE_FEATURE_COUNT + 13;

	/**
	 * The number of structural features of the '<em>ILab Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Result</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__RESULT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__ITEM = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Time Stamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__TIME_STAMP = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Observation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__OBSERVATION_TIME = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>User</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__USER = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Order Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__ORDER_ID = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__STATE = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>ILab Order</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Item Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ITEM_NAME = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ITEM = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ORIGIN = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Charge</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__CHARGE = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>ILab Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__TITLE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__DESCRIPTION = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__STATUS = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__CREATED = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Lastchanged</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__LASTCHANGED = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__MIME_TYPE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__CATEGORY = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>History</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__HISTORY = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Store Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__STORE_ID = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__EXTENSION = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Keywords</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__KEYWORDS = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Author</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__AUTHOR = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The number of structural features of the '<em>IDocument</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__DELETED = IDOCUMENT__DELETED;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__TITLE = IDOCUMENT__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__DESCRIPTION = IDOCUMENT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__STATUS = IDOCUMENT__STATUS;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__CREATED = IDOCUMENT__CREATED;

	/**
	 * The feature id for the '<em><b>Lastchanged</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__LASTCHANGED = IDOCUMENT__LASTCHANGED;

	/**
	 * The feature id for the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__MIME_TYPE = IDOCUMENT__MIME_TYPE;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__CATEGORY = IDOCUMENT__CATEGORY;

	/**
	 * The feature id for the '<em><b>History</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__HISTORY = IDOCUMENT__HISTORY;

	/**
	 * The feature id for the '<em><b>Store Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__STORE_ID = IDOCUMENT__STORE_ID;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__EXTENSION = IDOCUMENT__EXTENSION;

	/**
	 * The feature id for the '<em><b>Keywords</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__KEYWORDS = IDOCUMENT__KEYWORDS;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__PATIENT = IDOCUMENT__PATIENT;

	/**
	 * The feature id for the '<em><b>Author</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__AUTHOR = IDOCUMENT__AUTHOR;

	/**
	 * The number of structural features of the '<em>IDocument Letter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER_FEATURE_COUNT = IDOCUMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__DELETED = TypesPackage.COMPARABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Background</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__BACKGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Foreground</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__FOREGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__VISIBLE = TypesPackage.COMPARABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__VALUE = TypesPackage.COMPARABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>ISticker</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER_FEATURE_COUNT = TypesPackage.COMPARABLE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>ICode Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICATEGORY__NAME = 0;

	/**
	 * The number of structural features of the '<em>ICategory</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICATEGORY_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IHISTORY__DATE = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IHISTORY__STATUS = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IHISTORY__DESCRIPTION = 2;

	/**
	 * The number of structural features of the '<em>IHistory</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IHISTORY_FEATURE_COUNT = 3;

	/**
	 * The number of structural features of the '<em>IDiagnose</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSE_FEATURE_COUNT = ICODE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>IBillable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE_FEATURE_COUNT = ICODE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__DELETED = DELETEABLE__DELETED;

	/**
	 * The number of structural features of the '<em>ICoverage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONFIG__KEY = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONFIG__VALUE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IConfig</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONFIG_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_CONFIG__KEY = ICONFIG__KEY;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_CONFIG__VALUE = ICONFIG__VALUE;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_CONFIG__OWNER = ICONFIG_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>IUser Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_CONFIG_FEATURE_COUNT = ICONFIG_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__START_TIME = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__END_TIME = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>IPeriod</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IArticle <em>IArticle</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IArticle
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIArticle()
	 * @generated
	 */
	int IARTICLE = 26;

	/**
	 * The feature id for the '<em><b>Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__GTIN = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__NAME = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Selling Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__SELLING_UNIT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Package Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PACKAGE_UNIT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Product</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PRODUCT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>IArticle</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.WithExtInfo <em>With Ext Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.WithExtInfo
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getWithExtInfo()
	 * @generated
	 */
	int WITH_EXT_INFO = 27;

	/**
	 * The number of structural features of the '<em>With Ext Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WITH_EXT_INFO_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IRole <em>IRole</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IRole
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRole()
	 * @generated
	 */
	int IROLE = 28;

	/**
	 * The number of structural features of the '<em>IRole</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IROLE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBlob <em>IBlob</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBlob
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBlob()
	 * @generated
	 */
	int IBLOB = 29;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Content</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB__CONTENT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB__DATE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>IBlob</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IContact <em>IContact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IContact</em>'.
	 * @see ch.elexis.core.model.IContact
	 * @generated
	 */
	EClass getIContact();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.IContact#isMandator()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isUser <em>User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User</em>'.
	 * @see ch.elexis.core.model.IContact#isUser()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_User();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IContact#isPatient()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Patient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isPerson <em>Person</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Person</em>'.
	 * @see ch.elexis.core.model.IContact#isPerson()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Person();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isLaboratory <em>Laboratory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Laboratory</em>'.
	 * @see ch.elexis.core.model.IContact#isLaboratory()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Laboratory();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isOrganization <em>Organization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Organization</em>'.
	 * @see ch.elexis.core.model.IContact#isOrganization()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Organization();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getDescription1 <em>Description1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description1</em>'.
	 * @see ch.elexis.core.model.IContact#getDescription1()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Description1();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getDescription2 <em>Description2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description2</em>'.
	 * @see ch.elexis.core.model.IContact#getDescription2()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Description2();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getDescription3 <em>Description3</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description3</em>'.
	 * @see ch.elexis.core.model.IContact#getDescription3()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Description3();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.model.IContact#getCode()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getCountry <em>Country</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Country</em>'.
	 * @see ch.elexis.core.model.IContact#getCountry()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Country();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getZip <em>Zip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Zip</em>'.
	 * @see ch.elexis.core.model.IContact#getZip()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Zip();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getCity <em>City</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>City</em>'.
	 * @see ch.elexis.core.model.IContact#getCity()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_City();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getStreet <em>Street</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Street</em>'.
	 * @see ch.elexis.core.model.IContact#getStreet()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Street();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getPhone1 <em>Phone1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Phone1</em>'.
	 * @see ch.elexis.core.model.IContact#getPhone1()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Phone1();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getPhone2 <em>Phone2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Phone2</em>'.
	 * @see ch.elexis.core.model.IContact#getPhone2()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Phone2();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getFax <em>Fax</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fax</em>'.
	 * @see ch.elexis.core.model.IContact#getFax()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Fax();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getEmail <em>Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Email</em>'.
	 * @see ch.elexis.core.model.IContact#getEmail()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Email();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getWebsite <em>Website</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Website</em>'.
	 * @see ch.elexis.core.model.IContact#getWebsite()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Website();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getMobile <em>Mobile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mobile</em>'.
	 * @see ch.elexis.core.model.IContact#getMobile()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Mobile();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see ch.elexis.core.model.IContact#getComment()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Comment();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IXid <em>IXid</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IXid</em>'.
	 * @see ch.elexis.core.model.IXid
	 * @generated
	 */
	EClass getIXid();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IXid#getDomain <em>Domain</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Domain</em>'.
	 * @see ch.elexis.core.model.IXid#getDomain()
	 * @see #getIXid()
	 * @generated
	 */
	EAttribute getIXid_Domain();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IXid#getDomainId <em>Domain Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Domain Id</em>'.
	 * @see ch.elexis.core.model.IXid#getDomainId()
	 * @see #getIXid()
	 * @generated
	 */
	EAttribute getIXid_DomainId();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IXid#getQuality <em>Quality</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Quality</em>'.
	 * @see ch.elexis.core.model.IXid#getQuality()
	 * @see #getIXid()
	 * @generated
	 */
	EAttribute getIXid_Quality();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IXid#getObjectId <em>Object Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Id</em>'.
	 * @see ch.elexis.core.model.IXid#getObjectId()
	 * @see #getIXid()
	 * @generated
	 */
	EAttribute getIXid_ObjectId();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICode Element</em>'.
	 * @see ch.elexis.core.model.ICodeElement
	 * @generated
	 */
	EClass getICodeElement();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ISticker</em>'.
	 * @see ch.elexis.core.model.ISticker
	 * @generated
	 */
	EClass getISticker();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getBackground <em>Background</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Background</em>'.
	 * @see ch.elexis.core.model.ISticker#getBackground()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Background();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getForeground <em>Foreground</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Foreground</em>'.
	 * @see ch.elexis.core.model.ISticker#getForeground()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Foreground();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see ch.elexis.core.model.ISticker#isVisible()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Visible();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see ch.elexis.core.model.ISticker#getValue()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Value();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPerson <em>IPerson</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPerson</em>'.
	 * @see ch.elexis.core.model.IPerson
	 * @generated
	 */
	EClass getIPerson();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getDateOfBirth <em>Date Of Birth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date Of Birth</em>'.
	 * @see ch.elexis.core.model.IPerson#getDateOfBirth()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_DateOfBirth();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getGender <em>Gender</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gender</em>'.
	 * @see ch.elexis.core.model.IPerson#getGender()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_Gender();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getTitel <em>Titel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Titel</em>'.
	 * @see ch.elexis.core.model.IPerson#getTitel()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_Titel();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getTitelSuffix <em>Titel Suffix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Titel Suffix</em>'.
	 * @see ch.elexis.core.model.IPerson#getTitelSuffix()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_TitelSuffix();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getFirstName <em>First Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>First Name</em>'.
	 * @see ch.elexis.core.model.IPerson#getFirstName()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_FirstName();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getLastName <em>Last Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Name</em>'.
	 * @see ch.elexis.core.model.IPerson#getLastName()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_LastName();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPatient</em>'.
	 * @see ch.elexis.core.model.IPatient
	 * @generated
	 */
	EClass getIPatient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPatient#getDiagnosen <em>Diagnosen</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Diagnosen</em>'.
	 * @see ch.elexis.core.model.IPatient#getDiagnosen()
	 * @see #getIPatient()
	 * @generated
	 */
	EAttribute getIPatient_Diagnosen();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPatient#getRisk <em>Risk</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Risk</em>'.
	 * @see ch.elexis.core.model.IPatient#getRisk()
	 * @see #getIPatient()
	 * @generated
	 */
	EAttribute getIPatient_Risk();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPatient#getFamilyAnamnese <em>Family Anamnese</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Family Anamnese</em>'.
	 * @see ch.elexis.core.model.IPatient#getFamilyAnamnese()
	 * @see #getIPatient()
	 * @generated
	 */
	EAttribute getIPatient_FamilyAnamnese();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPatient#getPersonalAnamnese <em>Personal Anamnese</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Personal Anamnese</em>'.
	 * @see ch.elexis.core.model.IPatient#getPersonalAnamnese()
	 * @see #getIPatient()
	 * @generated
	 */
	EAttribute getIPatient_PersonalAnamnese();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPatient#getAllergies <em>Allergies</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allergies</em>'.
	 * @see ch.elexis.core.model.IPatient#getAllergies()
	 * @see #getIPatient()
	 * @generated
	 */
	EAttribute getIPatient_Allergies();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IUser <em>IUser</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IUser</em>'.
	 * @see ch.elexis.core.model.IUser
	 * @generated
	 */
	EClass getIUser();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#getUsername <em>Username</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Username</em>'.
	 * @see ch.elexis.core.model.IUser#getUsername()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_Username();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#getPassword <em>Password</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Password</em>'.
	 * @see ch.elexis.core.model.IUser#getPassword()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_Password();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IUser#getAssignedContact <em>Assigned Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Assigned Contact</em>'.
	 * @see ch.elexis.core.model.IUser#getAssignedContact()
	 * @see #getIUser()
	 * @generated
	 */
	EReference getIUser_AssignedContact();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IUser#getRoles <em>Roles</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Roles</em>'.
	 * @see ch.elexis.core.model.IUser#getRoles()
	 * @see #getIUser()
	 * @generated
	 */
	EReference getIUser_Roles();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identifiable</em>'.
	 * @see ch.elexis.core.model.Identifiable
	 * @generated
	 */
	EClass getIdentifiable();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.Deleteable <em>Deleteable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Deleteable</em>'.
	 * @see ch.elexis.core.model.Deleteable
	 * @generated
	 */
	EClass getDeleteable();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.Deleteable#isDeleted <em>Deleted</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Deleted</em>'.
	 * @see ch.elexis.core.model.Deleteable#isDeleted()
	 * @see #getDeleteable()
	 * @generated
	 */
	EAttribute getDeleteable_Deleted();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Item</em>'.
	 * @see ch.elexis.core.model.ILabItem
	 * @generated
	 */
	EClass getILabItem();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getTyp <em>Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Typ</em>'.
	 * @see ch.elexis.core.model.ILabItem#getTyp()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Typ();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getReferenceMale <em>Reference Male</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Male</em>'.
	 * @see ch.elexis.core.model.ILabItem#getReferenceMale()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_ReferenceMale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getReferenceFemale <em>Reference Female</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Female</em>'.
	 * @see ch.elexis.core.model.ILabItem#getReferenceFemale()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_ReferenceFemale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getGroup <em>Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Group</em>'.
	 * @see ch.elexis.core.model.ILabItem#getGroup()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Group();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getPriority <em>Priority</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Priority</em>'.
	 * @see ch.elexis.core.model.ILabItem#getPriority()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Priority();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.model.ILabItem#getCode()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit</em>'.
	 * @see ch.elexis.core.model.ILabItem#getUnit()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Unit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.ILabItem#getName()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getDigits <em>Digits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Digits</em>'.
	 * @see ch.elexis.core.model.ILabItem#getDigits()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Digits();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see ch.elexis.core.model.ILabItem#isVisible()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Visible();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getFormula <em>Formula</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Formula</em>'.
	 * @see ch.elexis.core.model.ILabItem#getFormula()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Formula();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getLoincCode <em>Loinc Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Loinc Code</em>'.
	 * @see ch.elexis.core.model.ILabItem#getLoincCode()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_LoincCode();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getBillingCode <em>Billing Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Billing Code</em>'.
	 * @see ch.elexis.core.model.ILabItem#getBillingCode()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_BillingCode();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Result</em>'.
	 * @see ch.elexis.core.model.ILabResult
	 * @generated
	 */
	EClass getILabResult();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit</em>'.
	 * @see ch.elexis.core.model.ILabResult#getUnit()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Unit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getAnalyseTime <em>Analyse Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Analyse Time</em>'.
	 * @see ch.elexis.core.model.ILabResult#getAnalyseTime()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_AnalyseTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getObservationTime <em>Observation Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observation Time</em>'.
	 * @see ch.elexis.core.model.ILabResult#getObservationTime()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_ObservationTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getTransmissionTime <em>Transmission Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Transmission Time</em>'.
	 * @see ch.elexis.core.model.ILabResult#getTransmissionTime()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_TransmissionTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#isPathologic <em>Pathologic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pathologic</em>'.
	 * @see ch.elexis.core.model.ILabResult#isPathologic()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Pathologic();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getResult <em>Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Result</em>'.
	 * @see ch.elexis.core.model.ILabResult#getResult()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Result();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see ch.elexis.core.model.ILabResult#getComment()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Comment();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getReferenceMale <em>Reference Male</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Male</em>'.
	 * @see ch.elexis.core.model.ILabResult#getReferenceMale()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_ReferenceMale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getReferenceFemale <em>Reference Female</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Female</em>'.
	 * @see ch.elexis.core.model.ILabResult#getReferenceFemale()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_ReferenceFemale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.ILabResult#getDate()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Date();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabResult#getItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Item</em>'.
	 * @see ch.elexis.core.model.ILabResult#getItem()
	 * @see #getILabResult()
	 * @generated
	 */
	EReference getILabResult_Item();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabResult#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.ILabResult#getPatient()
	 * @see #getILabResult()
	 * @generated
	 */
	EReference getILabResult_Patient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getPathologicDescription <em>Pathologic Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pathologic Description</em>'.
	 * @see ch.elexis.core.model.ILabResult#getPathologicDescription()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_PathologicDescription();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabResult#getOrigin <em>Origin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Origin</em>'.
	 * @see ch.elexis.core.model.ILabResult#getOrigin()
	 * @see #getILabResult()
	 * @generated
	 */
	EReference getILabResult_Origin();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Order</em>'.
	 * @see ch.elexis.core.model.ILabOrder
	 * @generated
	 */
	EClass getILabOrder();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getResult <em>Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Result</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getResult()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_Result();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Item</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getItem()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_Item();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getPatient()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_Patient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#getTimeStamp <em>Time Stamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time Stamp</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getTimeStamp()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_TimeStamp();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#getObservationTime <em>Observation Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observation Time</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getObservationTime()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_ObservationTime();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getUser <em>User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>User</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getUser()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_User();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getMandator()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#getOrderId <em>Order Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Order Id</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getOrderId()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_OrderId();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getState()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_State();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabMapping <em>ILab Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Mapping</em>'.
	 * @see ch.elexis.core.model.ILabMapping
	 * @generated
	 */
	EClass getILabMapping();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabMapping#getItemName <em>Item Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Item Name</em>'.
	 * @see ch.elexis.core.model.ILabMapping#getItemName()
	 * @see #getILabMapping()
	 * @generated
	 */
	EAttribute getILabMapping_ItemName();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabMapping#getItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Item</em>'.
	 * @see ch.elexis.core.model.ILabMapping#getItem()
	 * @see #getILabMapping()
	 * @generated
	 */
	EReference getILabMapping_Item();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabMapping#getOrigin <em>Origin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Origin</em>'.
	 * @see ch.elexis.core.model.ILabMapping#getOrigin()
	 * @see #getILabMapping()
	 * @generated
	 */
	EReference getILabMapping_Origin();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabMapping#isCharge <em>Charge</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Charge</em>'.
	 * @see ch.elexis.core.model.ILabMapping#isCharge()
	 * @see #getILabMapping()
	 * @generated
	 */
	EAttribute getILabMapping_Charge();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPeriod</em>'.
	 * @see ch.elexis.core.model.IPeriod
	 * @generated
	 */
	EClass getIPeriod();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPeriod#getStartTime <em>Start Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Time</em>'.
	 * @see ch.elexis.core.model.IPeriod#getStartTime()
	 * @see #getIPeriod()
	 * @generated
	 */
	EAttribute getIPeriod_StartTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPeriod#getEndTime <em>End Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>End Time</em>'.
	 * @see ch.elexis.core.model.IPeriod#getEndTime()
	 * @see #getIPeriod()
	 * @generated
	 */
	EAttribute getIPeriod_EndTime();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDocument <em>IDocument</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDocument</em>'.
	 * @see ch.elexis.core.model.IDocument
	 * @generated
	 */
	EClass getIDocument();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.model.IDocument#getTitle()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Title();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see ch.elexis.core.model.IDocument#getDescription()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Description();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see ch.elexis.core.model.IDocument#getStatus()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Status();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getCreated <em>Created</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Created</em>'.
	 * @see ch.elexis.core.model.IDocument#getCreated()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Created();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getLastchanged <em>Lastchanged</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lastchanged</em>'.
	 * @see ch.elexis.core.model.IDocument#getLastchanged()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Lastchanged();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getMimeType <em>Mime Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mime Type</em>'.
	 * @see ch.elexis.core.model.IDocument#getMimeType()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_MimeType();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocument#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Category</em>'.
	 * @see ch.elexis.core.model.IDocument#getCategory()
	 * @see #getIDocument()
	 * @generated
	 */
	EReference getIDocument_Category();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IDocument#getHistory <em>History</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>History</em>'.
	 * @see ch.elexis.core.model.IDocument#getHistory()
	 * @see #getIDocument()
	 * @generated
	 */
	EReference getIDocument_History();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getStoreId <em>Store Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Store Id</em>'.
	 * @see ch.elexis.core.model.IDocument#getStoreId()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_StoreId();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getExtension <em>Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extension</em>'.
	 * @see ch.elexis.core.model.IDocument#getExtension()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Extension();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getKeywords <em>Keywords</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Keywords</em>'.
	 * @see ch.elexis.core.model.IDocument#getKeywords()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_Keywords();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocument#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IDocument#getPatient()
	 * @see #getIDocument()
	 * @generated
	 */
	EReference getIDocument_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocument#getAuthor <em>Author</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Author</em>'.
	 * @see ch.elexis.core.model.IDocument#getAuthor()
	 * @see #getIDocument()
	 * @generated
	 */
	EReference getIDocument_Author();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICategory</em>'.
	 * @see ch.elexis.core.model.ICategory
	 * @generated
	 */
	EClass getICategory();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICategory#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.ICategory#getName()
	 * @see #getICategory()
	 * @generated
	 */
	EAttribute getICategory_Name();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IHistory</em>'.
	 * @see ch.elexis.core.model.IHistory
	 * @generated
	 */
	EClass getIHistory();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IHistory#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IHistory#getDate()
	 * @see #getIHistory()
	 * @generated
	 */
	EAttribute getIHistory_Date();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IHistory#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see ch.elexis.core.model.IHistory#getStatus()
	 * @see #getIHistory()
	 * @generated
	 */
	EAttribute getIHistory_Status();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IHistory#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see ch.elexis.core.model.IHistory#getDescription()
	 * @see #getIHistory()
	 * @generated
	 */
	EAttribute getIHistory_Description();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDiagnose <em>IDiagnose</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDiagnose</em>'.
	 * @see ch.elexis.core.model.IDiagnose
	 * @generated
	 */
	EClass getIDiagnose();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBillable <em>IBillable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBillable</em>'.
	 * @see ch.elexis.core.model.IBillable
	 * @generated
	 */
	EClass getIBillable();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICoverage <em>ICoverage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICoverage</em>'.
	 * @see ch.elexis.core.model.ICoverage
	 * @generated
	 */
	EClass getICoverage();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IOrganization <em>IOrganization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IOrganization</em>'.
	 * @see ch.elexis.core.model.IOrganization
	 * @generated
	 */
	EClass getIOrganization();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILaboratory <em>ILaboratory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILaboratory</em>'.
	 * @see ch.elexis.core.model.ILaboratory
	 * @generated
	 */
	EClass getILaboratory();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDocument Letter</em>'.
	 * @see ch.elexis.core.model.IDocumentLetter
	 * @generated
	 */
	EClass getIDocumentLetter();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IConfig <em>IConfig</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IConfig</em>'.
	 * @see ch.elexis.core.model.IConfig
	 * @generated
	 */
	EClass getIConfig();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IConfig#getKey <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see ch.elexis.core.model.IConfig#getKey()
	 * @see #getIConfig()
	 * @generated
	 */
	EAttribute getIConfig_Key();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IConfig#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see ch.elexis.core.model.IConfig#getValue()
	 * @see #getIConfig()
	 * @generated
	 */
	EAttribute getIConfig_Value();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IUserConfig <em>IUser Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IUser Config</em>'.
	 * @see ch.elexis.core.model.IUserConfig
	 * @generated
	 */
	EClass getIUserConfig();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IUserConfig#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Owner</em>'.
	 * @see ch.elexis.core.model.IUserConfig#getOwner()
	 * @see #getIUserConfig()
	 * @generated
	 */
	EReference getIUserConfig_Owner();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IMandator <em>IMandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IMandator</em>'.
	 * @see ch.elexis.core.model.IMandator
	 * @generated
	 */
	EClass getIMandator();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IArticle <em>IArticle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IArticle</em>'.
	 * @see ch.elexis.core.model.IArticle
	 * @generated
	 */
	EClass getIArticle();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getGtin <em>Gtin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Gtin</em>'.
	 * @see ch.elexis.core.model.IArticle#getGtin()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_Gtin();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.IArticle#getName()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getSellingUnit <em>Selling Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selling Unit</em>'.
	 * @see ch.elexis.core.model.IArticle#getSellingUnit()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_SellingUnit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getPackageUnit <em>Package Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Package Unit</em>'.
	 * @see ch.elexis.core.model.IArticle#getPackageUnit()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_PackageUnit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#isProduct <em>Product</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Product</em>'.
	 * @see ch.elexis.core.model.IArticle#isProduct()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_Product();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.WithExtInfo <em>With Ext Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>With Ext Info</em>'.
	 * @see ch.elexis.core.model.WithExtInfo
	 * @generated
	 */
	EClass getWithExtInfo();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IRole <em>IRole</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IRole</em>'.
	 * @see ch.elexis.core.model.IRole
	 * @generated
	 */
	EClass getIRole();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBlob <em>IBlob</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBlob</em>'.
	 * @see ch.elexis.core.model.IBlob
	 * @generated
	 */
	EClass getIBlob();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBlob#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content</em>'.
	 * @see ch.elexis.core.model.IBlob#getContent()
	 * @see #getIBlob()
	 * @generated
	 */
	EAttribute getIBlob_Content();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBlob#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IBlob#getDate()
	 * @see #getIBlob()
	 * @generated
	 */
	EAttribute getIBlob_Date();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.IContact <em>IContact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IContact
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIContact()
		 * @generated
		 */
		EClass ICONTACT = eINSTANCE.getIContact();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__MANDATOR = eINSTANCE.getIContact_Mandator();

		/**
		 * The meta object literal for the '<em><b>User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__USER = eINSTANCE.getIContact_User();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__PATIENT = eINSTANCE.getIContact_Patient();

		/**
		 * The meta object literal for the '<em><b>Person</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__PERSON = eINSTANCE.getIContact_Person();

		/**
		 * The meta object literal for the '<em><b>Laboratory</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__LABORATORY = eINSTANCE.getIContact_Laboratory();

		/**
		 * The meta object literal for the '<em><b>Organization</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__ORGANIZATION = eINSTANCE.getIContact_Organization();

		/**
		 * The meta object literal for the '<em><b>Description1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__DESCRIPTION1 = eINSTANCE.getIContact_Description1();

		/**
		 * The meta object literal for the '<em><b>Description2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__DESCRIPTION2 = eINSTANCE.getIContact_Description2();

		/**
		 * The meta object literal for the '<em><b>Description3</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__DESCRIPTION3 = eINSTANCE.getIContact_Description3();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__CODE = eINSTANCE.getIContact_Code();

		/**
		 * The meta object literal for the '<em><b>Country</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__COUNTRY = eINSTANCE.getIContact_Country();

		/**
		 * The meta object literal for the '<em><b>Zip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__ZIP = eINSTANCE.getIContact_Zip();

		/**
		 * The meta object literal for the '<em><b>City</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__CITY = eINSTANCE.getIContact_City();

		/**
		 * The meta object literal for the '<em><b>Street</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__STREET = eINSTANCE.getIContact_Street();

		/**
		 * The meta object literal for the '<em><b>Phone1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__PHONE1 = eINSTANCE.getIContact_Phone1();

		/**
		 * The meta object literal for the '<em><b>Phone2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__PHONE2 = eINSTANCE.getIContact_Phone2();

		/**
		 * The meta object literal for the '<em><b>Fax</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__FAX = eINSTANCE.getIContact_Fax();

		/**
		 * The meta object literal for the '<em><b>Email</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__EMAIL = eINSTANCE.getIContact_Email();

		/**
		 * The meta object literal for the '<em><b>Website</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__WEBSITE = eINSTANCE.getIContact_Website();

		/**
		 * The meta object literal for the '<em><b>Mobile</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__MOBILE = eINSTANCE.getIContact_Mobile();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__COMMENT = eINSTANCE.getIContact_Comment();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IXid <em>IXid</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IXid
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIXid()
		 * @generated
		 */
		EClass IXID = eINSTANCE.getIXid();

		/**
		 * The meta object literal for the '<em><b>Domain</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__DOMAIN = eINSTANCE.getIXid_Domain();

		/**
		 * The meta object literal for the '<em><b>Domain Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__DOMAIN_ID = eINSTANCE.getIXid_DomainId();

		/**
		 * The meta object literal for the '<em><b>Quality</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__QUALITY = eINSTANCE.getIXid_Quality();

		/**
		 * The meta object literal for the '<em><b>Object Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__OBJECT_ID = eINSTANCE.getIXid_ObjectId();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICodeElement
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElement()
		 * @generated
		 */
		EClass ICODE_ELEMENT = eINSTANCE.getICodeElement();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ISticker
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISticker()
		 * @generated
		 */
		EClass ISTICKER = eINSTANCE.getISticker();

		/**
		 * The meta object literal for the '<em><b>Background</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__BACKGROUND = eINSTANCE.getISticker_Background();

		/**
		 * The meta object literal for the '<em><b>Foreground</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__FOREGROUND = eINSTANCE.getISticker_Foreground();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__VISIBLE = eINSTANCE.getISticker_Visible();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__VALUE = eINSTANCE.getISticker_Value();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IPerson <em>IPerson</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPerson
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPerson()
		 * @generated
		 */
		EClass IPERSON = eINSTANCE.getIPerson();

		/**
		 * The meta object literal for the '<em><b>Date Of Birth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__DATE_OF_BIRTH = eINSTANCE.getIPerson_DateOfBirth();

		/**
		 * The meta object literal for the '<em><b>Gender</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__GENDER = eINSTANCE.getIPerson_Gender();

		/**
		 * The meta object literal for the '<em><b>Titel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__TITEL = eINSTANCE.getIPerson_Titel();

		/**
		 * The meta object literal for the '<em><b>Titel Suffix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__TITEL_SUFFIX = eINSTANCE.getIPerson_TitelSuffix();

		/**
		 * The meta object literal for the '<em><b>First Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__FIRST_NAME = eINSTANCE.getIPerson_FirstName();

		/**
		 * The meta object literal for the '<em><b>Last Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__LAST_NAME = eINSTANCE.getIPerson_LastName();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPatient
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPatient()
		 * @generated
		 */
		EClass IPATIENT = eINSTANCE.getIPatient();

		/**
		 * The meta object literal for the '<em><b>Diagnosen</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPATIENT__DIAGNOSEN = eINSTANCE.getIPatient_Diagnosen();

		/**
		 * The meta object literal for the '<em><b>Risk</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPATIENT__RISK = eINSTANCE.getIPatient_Risk();

		/**
		 * The meta object literal for the '<em><b>Family Anamnese</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPATIENT__FAMILY_ANAMNESE = eINSTANCE.getIPatient_FamilyAnamnese();

		/**
		 * The meta object literal for the '<em><b>Personal Anamnese</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPATIENT__PERSONAL_ANAMNESE = eINSTANCE.getIPatient_PersonalAnamnese();

		/**
		 * The meta object literal for the '<em><b>Allergies</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPATIENT__ALLERGIES = eINSTANCE.getIPatient_Allergies();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IUser <em>IUser</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IUser
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUser()
		 * @generated
		 */
		EClass IUSER = eINSTANCE.getIUser();

		/**
		 * The meta object literal for the '<em><b>Username</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__USERNAME = eINSTANCE.getIUser_Username();

		/**
		 * The meta object literal for the '<em><b>Password</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__PASSWORD = eINSTANCE.getIUser_Password();

		/**
		 * The meta object literal for the '<em><b>Assigned Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IUSER__ASSIGNED_CONTACT = eINSTANCE.getIUser_AssignedContact();

		/**
		 * The meta object literal for the '<em><b>Roles</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IUSER__ROLES = eINSTANCE.getIUser_Roles();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.Identifiable <em>Identifiable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.Identifiable
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIdentifiable()
		 * @generated
		 */
		EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.Deleteable <em>Deleteable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.Deleteable
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getDeleteable()
		 * @generated
		 */
		EClass DELETEABLE = eINSTANCE.getDeleteable();

		/**
		 * The meta object literal for the '<em><b>Deleted</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DELETEABLE__DELETED = eINSTANCE.getDeleteable_Deleted();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILabItem
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabItem()
		 * @generated
		 */
		EClass ILAB_ITEM = eINSTANCE.getILabItem();

		/**
		 * The meta object literal for the '<em><b>Typ</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__TYP = eINSTANCE.getILabItem_Typ();

		/**
		 * The meta object literal for the '<em><b>Reference Male</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__REFERENCE_MALE = eINSTANCE.getILabItem_ReferenceMale();

		/**
		 * The meta object literal for the '<em><b>Reference Female</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__REFERENCE_FEMALE = eINSTANCE.getILabItem_ReferenceFemale();

		/**
		 * The meta object literal for the '<em><b>Group</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__GROUP = eINSTANCE.getILabItem_Group();

		/**
		 * The meta object literal for the '<em><b>Priority</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__PRIORITY = eINSTANCE.getILabItem_Priority();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__CODE = eINSTANCE.getILabItem_Code();

		/**
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__UNIT = eINSTANCE.getILabItem_Unit();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__NAME = eINSTANCE.getILabItem_Name();

		/**
		 * The meta object literal for the '<em><b>Digits</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__DIGITS = eINSTANCE.getILabItem_Digits();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__VISIBLE = eINSTANCE.getILabItem_Visible();

		/**
		 * The meta object literal for the '<em><b>Formula</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__FORMULA = eINSTANCE.getILabItem_Formula();

		/**
		 * The meta object literal for the '<em><b>Loinc Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__LOINC_CODE = eINSTANCE.getILabItem_LoincCode();

		/**
		 * The meta object literal for the '<em><b>Billing Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__BILLING_CODE = eINSTANCE.getILabItem_BillingCode();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILabResult
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabResult()
		 * @generated
		 */
		EClass ILAB_RESULT = eINSTANCE.getILabResult();

		/**
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__UNIT = eINSTANCE.getILabResult_Unit();

		/**
		 * The meta object literal for the '<em><b>Analyse Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__ANALYSE_TIME = eINSTANCE.getILabResult_AnalyseTime();

		/**
		 * The meta object literal for the '<em><b>Observation Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__OBSERVATION_TIME = eINSTANCE.getILabResult_ObservationTime();

		/**
		 * The meta object literal for the '<em><b>Transmission Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__TRANSMISSION_TIME = eINSTANCE.getILabResult_TransmissionTime();

		/**
		 * The meta object literal for the '<em><b>Pathologic</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__PATHOLOGIC = eINSTANCE.getILabResult_Pathologic();

		/**
		 * The meta object literal for the '<em><b>Result</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__RESULT = eINSTANCE.getILabResult_Result();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__COMMENT = eINSTANCE.getILabResult_Comment();

		/**
		 * The meta object literal for the '<em><b>Reference Male</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__REFERENCE_MALE = eINSTANCE.getILabResult_ReferenceMale();

		/**
		 * The meta object literal for the '<em><b>Reference Female</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__REFERENCE_FEMALE = eINSTANCE.getILabResult_ReferenceFemale();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__DATE = eINSTANCE.getILabResult_Date();

		/**
		 * The meta object literal for the '<em><b>Item</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_RESULT__ITEM = eINSTANCE.getILabResult_Item();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_RESULT__PATIENT = eINSTANCE.getILabResult_Patient();

		/**
		 * The meta object literal for the '<em><b>Pathologic Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__PATHOLOGIC_DESCRIPTION = eINSTANCE.getILabResult_PathologicDescription();

		/**
		 * The meta object literal for the '<em><b>Origin</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_RESULT__ORIGIN = eINSTANCE.getILabResult_Origin();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILabOrder
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabOrder()
		 * @generated
		 */
		EClass ILAB_ORDER = eINSTANCE.getILabOrder();

		/**
		 * The meta object literal for the '<em><b>Result</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__RESULT = eINSTANCE.getILabOrder_Result();

		/**
		 * The meta object literal for the '<em><b>Item</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__ITEM = eINSTANCE.getILabOrder_Item();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__PATIENT = eINSTANCE.getILabOrder_Patient();

		/**
		 * The meta object literal for the '<em><b>Time Stamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__TIME_STAMP = eINSTANCE.getILabOrder_TimeStamp();

		/**
		 * The meta object literal for the '<em><b>Observation Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__OBSERVATION_TIME = eINSTANCE.getILabOrder_ObservationTime();

		/**
		 * The meta object literal for the '<em><b>User</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__USER = eINSTANCE.getILabOrder_User();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__MANDATOR = eINSTANCE.getILabOrder_Mandator();

		/**
		 * The meta object literal for the '<em><b>Order Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__ORDER_ID = eINSTANCE.getILabOrder_OrderId();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__STATE = eINSTANCE.getILabOrder_State();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ILabMapping <em>ILab Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILabMapping
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabMapping()
		 * @generated
		 */
		EClass ILAB_MAPPING = eINSTANCE.getILabMapping();

		/**
		 * The meta object literal for the '<em><b>Item Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_MAPPING__ITEM_NAME = eINSTANCE.getILabMapping_ItemName();

		/**
		 * The meta object literal for the '<em><b>Item</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_MAPPING__ITEM = eINSTANCE.getILabMapping_Item();

		/**
		 * The meta object literal for the '<em><b>Origin</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_MAPPING__ORIGIN = eINSTANCE.getILabMapping_Origin();

		/**
		 * The meta object literal for the '<em><b>Charge</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_MAPPING__CHARGE = eINSTANCE.getILabMapping_Charge();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPeriod
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPeriod()
		 * @generated
		 */
		EClass IPERIOD = eINSTANCE.getIPeriod();

		/**
		 * The meta object literal for the '<em><b>Start Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERIOD__START_TIME = eINSTANCE.getIPeriod_StartTime();

		/**
		 * The meta object literal for the '<em><b>End Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERIOD__END_TIME = eINSTANCE.getIPeriod_EndTime();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDocument <em>IDocument</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDocument
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocument()
		 * @generated
		 */
		EClass IDOCUMENT = eINSTANCE.getIDocument();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__TITLE = eINSTANCE.getIDocument_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__DESCRIPTION = eINSTANCE.getIDocument_Description();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__STATUS = eINSTANCE.getIDocument_Status();

		/**
		 * The meta object literal for the '<em><b>Created</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__CREATED = eINSTANCE.getIDocument_Created();

		/**
		 * The meta object literal for the '<em><b>Lastchanged</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__LASTCHANGED = eINSTANCE.getIDocument_Lastchanged();

		/**
		 * The meta object literal for the '<em><b>Mime Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__MIME_TYPE = eINSTANCE.getIDocument_MimeType();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT__CATEGORY = eINSTANCE.getIDocument_Category();

		/**
		 * The meta object literal for the '<em><b>History</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT__HISTORY = eINSTANCE.getIDocument_History();

		/**
		 * The meta object literal for the '<em><b>Store Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__STORE_ID = eINSTANCE.getIDocument_StoreId();

		/**
		 * The meta object literal for the '<em><b>Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__EXTENSION = eINSTANCE.getIDocument_Extension();

		/**
		 * The meta object literal for the '<em><b>Keywords</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__KEYWORDS = eINSTANCE.getIDocument_Keywords();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT__PATIENT = eINSTANCE.getIDocument_Patient();

		/**
		 * The meta object literal for the '<em><b>Author</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT__AUTHOR = eINSTANCE.getIDocument_Author();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICategory
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICategory()
		 * @generated
		 */
		EClass ICATEGORY = eINSTANCE.getICategory();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICATEGORY__NAME = eINSTANCE.getICategory_Name();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IHistory
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIHistory()
		 * @generated
		 */
		EClass IHISTORY = eINSTANCE.getIHistory();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IHISTORY__DATE = eINSTANCE.getIHistory_Date();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IHISTORY__STATUS = eINSTANCE.getIHistory_Status();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IHISTORY__DESCRIPTION = eINSTANCE.getIHistory_Description();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDiagnose <em>IDiagnose</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDiagnose
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnose()
		 * @generated
		 */
		EClass IDIAGNOSE = eINSTANCE.getIDiagnose();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBillable <em>IBillable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBillable
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillable()
		 * @generated
		 */
		EClass IBILLABLE = eINSTANCE.getIBillable();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICoverage <em>ICoverage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICoverage
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICoverage()
		 * @generated
		 */
		EClass ICOVERAGE = eINSTANCE.getICoverage();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IOrganization <em>IOrganization</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IOrganization
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrganization()
		 * @generated
		 */
		EClass IORGANIZATION = eINSTANCE.getIOrganization();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ILaboratory <em>ILaboratory</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILaboratory
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILaboratory()
		 * @generated
		 */
		EClass ILABORATORY = eINSTANCE.getILaboratory();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDocumentLetter
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentLetter()
		 * @generated
		 */
		EClass IDOCUMENT_LETTER = eINSTANCE.getIDocumentLetter();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IConfig <em>IConfig</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IConfig
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIConfig()
		 * @generated
		 */
		EClass ICONFIG = eINSTANCE.getIConfig();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONFIG__KEY = eINSTANCE.getIConfig_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONFIG__VALUE = eINSTANCE.getIConfig_Value();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IUserConfig <em>IUser Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IUserConfig
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUserConfig()
		 * @generated
		 */
		EClass IUSER_CONFIG = eINSTANCE.getIUserConfig();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IUSER_CONFIG__OWNER = eINSTANCE.getIUserConfig_Owner();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IMandator <em>IMandator</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IMandator
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIMandator()
		 * @generated
		 */
		EClass IMANDATOR = eINSTANCE.getIMandator();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IArticle <em>IArticle</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IArticle
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIArticle()
		 * @generated
		 */
		EClass IARTICLE = eINSTANCE.getIArticle();

		/**
		 * The meta object literal for the '<em><b>Gtin</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__GTIN = eINSTANCE.getIArticle_Gtin();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__NAME = eINSTANCE.getIArticle_Name();

		/**
		 * The meta object literal for the '<em><b>Selling Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__SELLING_UNIT = eINSTANCE.getIArticle_SellingUnit();

		/**
		 * The meta object literal for the '<em><b>Package Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PACKAGE_UNIT = eINSTANCE.getIArticle_PackageUnit();

		/**
		 * The meta object literal for the '<em><b>Product</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PRODUCT = eINSTANCE.getIArticle_Product();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.WithExtInfo <em>With Ext Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.WithExtInfo
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getWithExtInfo()
		 * @generated
		 */
		EClass WITH_EXT_INFO = eINSTANCE.getWithExtInfo();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IRole <em>IRole</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IRole
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRole()
		 * @generated
		 */
		EClass IROLE = eINSTANCE.getIRole();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBlob <em>IBlob</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBlob
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBlob()
		 * @generated
		 */
		EClass IBLOB = eINSTANCE.getIBlob();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBLOB__CONTENT = eINSTANCE.getIBlob_Content();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBLOB__DATE = eINSTANCE.getIBlob_Date();

	}

} //ModelPackage
