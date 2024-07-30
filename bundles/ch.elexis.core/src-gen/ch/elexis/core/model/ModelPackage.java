/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__LASTUPDATE = 0;

	/**
	 * The number of structural features of the '<em>Identifiable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_FEATURE_COUNT = 1;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IXid <em>IXid</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IXid
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIXid()
	 * @generated
	 */
	int IXID = 2;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Domain Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN_ID = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Quality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__QUALITY = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IXid</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__ADDRESS = IDENTIFIABLE_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__GROUP = IDENTIFIABLE_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__POSTAL_ADDRESS = IDENTIFIABLE_FEATURE_COUNT + 24;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__IMAGE = IDENTIFIABLE_FEATURE_COUNT + 25;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__RELATED_CONTACTS = IDENTIFIABLE_FEATURE_COUNT + 26;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DECEASED = IDENTIFIABLE_FEATURE_COUNT + 27;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__EMAIL2 = IDENTIFIABLE_FEATURE_COUNT + 28;

	/**
	 * The number of structural features of the '<em>IContact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 29;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__LASTUPDATE = ICONTACT__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__ADDRESS = ICONTACT__ADDRESS;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__GROUP = ICONTACT__GROUP;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__POSTAL_ADDRESS = ICONTACT__POSTAL_ADDRESS;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__IMAGE = ICONTACT__IMAGE;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__RELATED_CONTACTS = ICONTACT__RELATED_CONTACTS;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DECEASED = ICONTACT__DECEASED;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__EMAIL2 = ICONTACT__EMAIL2;

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
	 * The feature id for the '<em><b>Marital Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__MARITAL_STATUS = ICONTACT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Legal Guardian</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__LEGAL_GUARDIAN = ICONTACT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Date Of Death</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DATE_OF_DEATH = ICONTACT_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>IPerson</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 9;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__LASTUPDATE = ICONTACT__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__ADDRESS = ICONTACT__ADDRESS;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__GROUP = ICONTACT__GROUP;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__POSTAL_ADDRESS = ICONTACT__POSTAL_ADDRESS;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__IMAGE = ICONTACT__IMAGE;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__RELATED_CONTACTS = ICONTACT__RELATED_CONTACTS;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__DECEASED = ICONTACT__DECEASED;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__EMAIL2 = ICONTACT__EMAIL2;

	/**
	 * The feature id for the '<em><b>Insurance Xml Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__INSURANCE_XML_NAME = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Insurance Law Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION__INSURANCE_LAW_CODE = ICONTACT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IOrganization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORGANIZATION_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 2;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__LASTUPDATE = IORGANIZATION__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__ADDRESS = IORGANIZATION__ADDRESS;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__GROUP = IORGANIZATION__GROUP;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__POSTAL_ADDRESS = IORGANIZATION__POSTAL_ADDRESS;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__IMAGE = IORGANIZATION__IMAGE;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__RELATED_CONTACTS = IORGANIZATION__RELATED_CONTACTS;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__DECEASED = IORGANIZATION__DECEASED;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__EMAIL2 = IORGANIZATION__EMAIL2;

	/**
	 * The feature id for the '<em><b>Insurance Xml Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__INSURANCE_XML_NAME = IORGANIZATION__INSURANCE_XML_NAME;

	/**
	 * The feature id for the '<em><b>Insurance Law Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY__INSURANCE_LAW_CODE = IORGANIZATION__INSURANCE_LAW_CODE;

	/**
	 * The number of structural features of the '<em>ILaboratory</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILABORATORY_FEATURE_COUNT = IORGANIZATION_FEATURE_COUNT + 0;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__LASTUPDATE = IPERSON__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__ADDRESS = IPERSON__ADDRESS;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__GROUP = IPERSON__GROUP;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__POSTAL_ADDRESS = IPERSON__POSTAL_ADDRESS;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__IMAGE = IPERSON__IMAGE;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__RELATED_CONTACTS = IPERSON__RELATED_CONTACTS;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DECEASED = IPERSON__DECEASED;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__EMAIL2 = IPERSON__EMAIL2;

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
	 * The feature id for the '<em><b>Marital Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__MARITAL_STATUS = IPERSON__MARITAL_STATUS;

	/**
	 * The feature id for the '<em><b>Legal Guardian</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__LEGAL_GUARDIAN = IPERSON__LEGAL_GUARDIAN;

	/**
	 * The feature id for the '<em><b>Date Of Death</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DATE_OF_DEATH = IPERSON__DATE_OF_DEATH;

	/**
	 * The feature id for the '<em><b>Family Doctor</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__FAMILY_DOCTOR = IPERSON_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Diagnosen</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DIAGNOSEN = IPERSON_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Risk</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__RISK = IPERSON_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Family Anamnese</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__FAMILY_ANAMNESE = IPERSON_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Personal Anamnese</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__PERSONAL_ANAMNESE = IPERSON_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Allergies</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__ALLERGIES = IPERSON_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Coverages</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__COVERAGES = IPERSON_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>IPatient</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT_FEATURE_COUNT = IPERSON_FEATURE_COUNT + 7;

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
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__LASTUPDATE = ICONTACT__LASTUPDATE;

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
	 * The feature id for the '<em><b>Address</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__ADDRESS = ICONTACT__ADDRESS;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__GROUP = ICONTACT__GROUP;

	/**
	 * The feature id for the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__POSTAL_ADDRESS = ICONTACT__POSTAL_ADDRESS;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__IMAGE = ICONTACT__IMAGE;

	/**
	 * The feature id for the '<em><b>Related Contacts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__RELATED_CONTACTS = ICONTACT__RELATED_CONTACTS;

	/**
	 * The feature id for the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__DECEASED = ICONTACT__DECEASED;

	/**
	 * The feature id for the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__EMAIL2 = ICONTACT__EMAIL2;

	/**
	 * The feature id for the '<em><b>Biller</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__BILLER = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR__ACTIVE = ICONTACT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IMandator</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMANDATOR_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 2;

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
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__USERNAME = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Hashed Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__HASHED_PASSWORD = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Salt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__SALT = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Assigned Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ASSIGNED_CONTACT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Roles</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ROLES = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ACTIVE = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Allow External</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ALLOW_EXTERNAL = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Administrator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ADMINISTRATOR = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>IUser</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IUserGroup <em>IUser Group</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IUserGroup
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUserGroup()
	 * @generated
	 */
	int IUSER_GROUP = 10;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Users</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP__USERS = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Roles</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP__ROLES = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Groupname</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP__GROUPNAME = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IUser Group</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_GROUP_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabItem
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabItem()
	 * @generated
	 */
	int ILAB_ITEM = 11;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The feature id for the '<em><b>Export</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__EXPORT = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Mappings</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__MAPPINGS = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The number of structural features of the '<em>ILab Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 16;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabResult
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabResult()
	 * @generated
	 */
	int ILAB_RESULT = 12;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__RESULT = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__COMMENT = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Reference Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REFERENCE_MALE = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Reference Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REFERENCE_FEMALE = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__UNIT = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__DATE = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Observation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__OBSERVATION_TIME = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Analyse Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ANALYSE_TIME = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Transmission Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__TRANSMISSION_TIME = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Pathologic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATHOLOGIC = DELETEABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Pathologic Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATHOLOGIC_DESCRIPTION = DELETEABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ORIGIN = DELETEABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ITEM = DELETEABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATIENT = DELETEABLE_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>ILab Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 15;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabOrder
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabOrder()
	 * @generated
	 */
	int ILAB_ORDER = 13;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The feature id for the '<em><b>Group Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__GROUP_NAME = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>User Resolved</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__USER_RESOLVED = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>ILab Order</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ILabMapping <em>ILab Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabMapping
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabMapping()
	 * @generated
	 */
	int ILAB_MAPPING = 14;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Item Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ITEM_NAME = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ITEM = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__ORIGIN = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Charge</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING__CHARGE = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>ILab Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_MAPPING_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDocument <em>IDocument</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDocument
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocument()
	 * @generated
	 */
	int IDOCUMENT = 15;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The feature id for the '<em><b>Status</b></em>' attribute list.
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
	 * The meta object id for the '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IHistory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIHistory()
	 * @generated
	 */
	int IHISTORY = 16;

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
	 * The meta object id for the '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICategory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICategory()
	 * @generated
	 */
	int ICATEGORY = 17;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDocumentLetter
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentLetter()
	 * @generated
	 */
	int IDOCUMENT_LETTER = 18;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__LASTUPDATE = IDOCUMENT__LASTUPDATE;

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
	 * The feature id for the '<em><b>Status</b></em>' attribute list.
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
	 * The feature id for the '<em><b>Encounter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__ENCOUNTER = IDOCUMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Recipient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER__RECIPIENT = IDOCUMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IDocument Letter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_LETTER_FEATURE_COUNT = IDOCUMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDocumentTemplate <em>IDocument Template</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDocumentTemplate
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentTemplate()
	 * @generated
	 */
	int IDOCUMENT_TEMPLATE = 19;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__LASTUPDATE = IDOCUMENT__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__DELETED = IDOCUMENT__DELETED;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__TITLE = IDOCUMENT__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__DESCRIPTION = IDOCUMENT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__STATUS = IDOCUMENT__STATUS;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__CREATED = IDOCUMENT__CREATED;

	/**
	 * The feature id for the '<em><b>Lastchanged</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__LASTCHANGED = IDOCUMENT__LASTCHANGED;

	/**
	 * The feature id for the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__MIME_TYPE = IDOCUMENT__MIME_TYPE;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__CATEGORY = IDOCUMENT__CATEGORY;

	/**
	 * The feature id for the '<em><b>History</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__HISTORY = IDOCUMENT__HISTORY;

	/**
	 * The feature id for the '<em><b>Store Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__STORE_ID = IDOCUMENT__STORE_ID;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__EXTENSION = IDOCUMENT__EXTENSION;

	/**
	 * The feature id for the '<em><b>Keywords</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__KEYWORDS = IDOCUMENT__KEYWORDS;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__PATIENT = IDOCUMENT__PATIENT;

	/**
	 * The feature id for the '<em><b>Author</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__AUTHOR = IDOCUMENT__AUTHOR;

	/**
	 * The feature id for the '<em><b>Template Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__TEMPLATE_TYP = IDOCUMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__MANDATOR = IDOCUMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Ask For Addressee</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE__ASK_FOR_ADDRESSEE = IDOCUMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>IDocument Template</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_TEMPLATE_FEATURE_COUNT = IDOCUMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ISticker
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISticker()
	 * @generated
	 */
	int ISTICKER = 20;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__DELETED = TypesPackage.COMPARABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__LASTUPDATE = TypesPackage.COMPARABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Background</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__BACKGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Foreground</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__FOREGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__VISIBLE = TypesPackage.COMPARABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__NAME = TypesPackage.COMPARABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Importance</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__IMPORTANCE = TypesPackage.COMPARABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__IMAGE = TypesPackage.COMPARABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Attached To</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__ATTACHED_TO = TypesPackage.COMPARABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Attached To Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__ATTACHED_TO_DATA = TypesPackage.COMPARABLE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>ISticker</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER_FEATURE_COUNT = TypesPackage.COMPARABLE_FEATURE_COUNT + 10;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICodeElement
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElement()
	 * @generated
	 */
	int ICODE_ELEMENT = 21;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT__CODE = 0;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT__TEXT = 1;

	/**
	 * The number of structural features of the '<em>ICode Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICodeElementBlock <em>ICode Element Block</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICodeElementBlock
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElementBlock()
	 * @generated
	 */
	int ICODE_ELEMENT_BLOCK = 22;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__CODE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__TEXT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Elements</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__ELEMENTS = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Element References</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__ELEMENT_REFERENCES = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Macro</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK__MACRO = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>ICode Element Block</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_BLOCK_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillable <em>IBillable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillable
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillable()
	 * @generated
	 */
	int IBILLABLE = 23;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillableVerifier <em>IBillable Verifier</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillableVerifier
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillableVerifier()
	 * @generated
	 */
	int IBILLABLE_VERIFIER = 24;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillableOptifier <em>IBillable Optifier</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillableOptifier
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillableOptifier()
	 * @generated
	 */
	int IBILLABLE_OPTIFIER = 25;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IService <em>IService</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IService
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIService()
	 * @generated
	 */
	int ISERVICE = 26;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IArticle <em>IArticle</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IArticle
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIArticle()
	 * @generated
	 */
	int IARTICLE = 28;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE__CODE = ICODE_ELEMENT__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE__TEXT = ICODE_ELEMENT__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE__LASTUPDATE = ICODE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>IBillable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE_FEATURE_COUNT = ICODE_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IBillable Verifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE_VERIFIER_FEATURE_COUNT = 0;

	/**
	 * The number of structural features of the '<em>IBillable Optifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLABLE_OPTIFIER_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__CODE = IBILLABLE__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__TEXT = IBILLABLE__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__LASTUPDATE = IBILLABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__DELETED = IBILLABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__PRICE = IBILLABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Net Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__NET_PRICE = IBILLABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE__MINUTES = IBILLABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IService</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISERVICE_FEATURE_COUNT = IBILLABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICustomService <em>ICustom Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICustomService
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICustomService()
	 * @generated
	 */
	int ICUSTOM_SERVICE = 27;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__CODE = ISERVICE__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__TEXT = ISERVICE__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__LASTUPDATE = ISERVICE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__DELETED = ISERVICE__DELETED;

	/**
	 * The feature id for the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__PRICE = ISERVICE__PRICE;

	/**
	 * The feature id for the '<em><b>Net Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__NET_PRICE = ISERVICE__NET_PRICE;

	/**
	 * The feature id for the '<em><b>Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE__MINUTES = ISERVICE__MINUTES;

	/**
	 * The number of structural features of the '<em>ICustom Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_SERVICE_FEATURE_COUNT = ISERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__CODE = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__TEXT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__GTIN = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Atc Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__ATC_CODE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__NAME = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Selling Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__SELLING_SIZE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Package Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PACKAGE_SIZE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Package Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PACKAGE_UNIT = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Product</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PRODUCT = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Purchase Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PURCHASE_PRICE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Selling Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__SELLING_PRICE = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Obligation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__OBLIGATION = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__TYP = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Sub Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__SUB_TYP = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Package Size String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE__PACKAGE_SIZE_STRING = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The number of structural features of the '<em>IArticle</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 16;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IArticleDefaultSignature <em>IArticle Default Signature</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IArticleDefaultSignature
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIArticleDefaultSignature()
	 * @generated
	 */
	int IARTICLE_DEFAULT_SIGNATURE = 29;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Atc Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__ATC_CODE = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Morning</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__MORNING = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Noon</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__NOON = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Evening</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__EVENING = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Night</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__NIGHT = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__COMMENT = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Free Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__FREE_TEXT = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Medication Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__MEDICATION_TYPE = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Disposal Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__DISPOSAL_TYPE = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__END_DATE = DELETEABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE__START_DATE = DELETEABLE_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>IArticle Default Signature</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IARTICLE_DEFAULT_SIGNATURE_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDiagnosis <em>IDiagnosis</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDiagnosis
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosis()
	 * @generated
	 */
	int IDIAGNOSIS = 30;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS__CODE = ICODE_ELEMENT__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS__TEXT = ICODE_ELEMENT__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS__LASTUPDATE = ICODE_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS__DESCRIPTION = ICODE_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IDiagnosis</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_FEATURE_COUNT = ICODE_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IFreeTextDiagnosis <em>IFree Text Diagnosis</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IFreeTextDiagnosis
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIFreeTextDiagnosis()
	 * @generated
	 */
	int IFREE_TEXT_DIAGNOSIS = 31;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IFREE_TEXT_DIAGNOSIS__CODE = IDIAGNOSIS__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IFREE_TEXT_DIAGNOSIS__TEXT = IDIAGNOSIS__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IFREE_TEXT_DIAGNOSIS__LASTUPDATE = IDIAGNOSIS__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IFREE_TEXT_DIAGNOSIS__DESCRIPTION = IDIAGNOSIS__DESCRIPTION;

	/**
	 * The number of structural features of the '<em>IFree Text Diagnosis</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IFREE_TEXT_DIAGNOSIS_FEATURE_COUNT = IDIAGNOSIS_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDiagnosisReference <em>IDiagnosis Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDiagnosisReference
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosisReference()
	 * @generated
	 */
	int IDIAGNOSIS_REFERENCE = 32;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE__CODE = IDIAGNOSIS__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE__TEXT = IDIAGNOSIS__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE__LASTUPDATE = IDIAGNOSIS__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE__DESCRIPTION = IDIAGNOSIS__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Referred Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE__REFERRED_CLASS = IDIAGNOSIS_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>IDiagnosis Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_REFERENCE_FEATURE_COUNT = IDIAGNOSIS_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDiagnosisTree <em>IDiagnosis Tree</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDiagnosisTree
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosisTree()
	 * @generated
	 */
	int IDIAGNOSIS_TREE = 33;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__CODE = IDIAGNOSIS__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__TEXT = IDIAGNOSIS__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__LASTUPDATE = IDIAGNOSIS__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__DESCRIPTION = IDIAGNOSIS__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__PARENT = IDIAGNOSIS_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Children</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE__CHILDREN = IDIAGNOSIS_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IDiagnosis Tree</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDIAGNOSIS_TREE_FEATURE_COUNT = IDIAGNOSIS_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICustomDiagnosis <em>ICustom Diagnosis</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICustomDiagnosis
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICustomDiagnosis()
	 * @generated
	 */
	int ICUSTOM_DIAGNOSIS = 34;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__CODE = IDIAGNOSIS_TREE__CODE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__TEXT = IDIAGNOSIS_TREE__TEXT;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__LASTUPDATE = IDIAGNOSIS_TREE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__DESCRIPTION = IDIAGNOSIS_TREE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__PARENT = IDIAGNOSIS_TREE__PARENT;

	/**
	 * The feature id for the '<em><b>Children</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS__CHILDREN = IDIAGNOSIS_TREE__CHILDREN;

	/**
	 * The number of structural features of the '<em>ICustom Diagnosis</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICUSTOM_DIAGNOSIS_FEATURE_COUNT = IDIAGNOSIS_TREE_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICoverage <em>ICoverage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICoverage
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICoverage()
	 * @generated
	 */
	int ICOVERAGE = 35;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__PATIENT = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__DESCRIPTION = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__REASON = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Date From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__DATE_FROM = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Cost Bearer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__COST_BEARER = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Insurance Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__INSURANCE_NUMBER = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Date To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__DATE_TO = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Billing Proposal Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__BILLING_PROPOSAL_DATE = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Encounters</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__ENCOUNTERS = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Billing System</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__BILLING_SYSTEM = DELETEABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Guarantor</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE__GUARANTOR = DELETEABLE_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>ICoverage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICOVERAGE_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillingSystemFactor <em>IBilling System Factor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillingSystemFactor
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillingSystemFactor()
	 * @generated
	 */
	int IBILLING_SYSTEM_FACTOR = 36;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>System</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR__SYSTEM = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Factor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR__FACTOR = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Valid From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR__VALID_FROM = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Valid To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR__VALID_TO = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IBilling System Factor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FACTOR_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IConfig <em>IConfig</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IConfig
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIConfig()
	 * @generated
	 */
	int ICONFIG = 37;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONFIG__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IUserConfig <em>IUser Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IUserConfig
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUserConfig()
	 * @generated
	 */
	int IUSER_CONFIG = 38;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_CONFIG__LASTUPDATE = ICONFIG__LASTUPDATE;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPeriod
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPeriod()
	 * @generated
	 */
	int IPERIOD = 39;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The meta object id for the '{@link ch.elexis.core.model.WithExtInfo <em>With Ext Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.WithExtInfo
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getWithExtInfo()
	 * @generated
	 */
	int WITH_EXT_INFO = 40;

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
	int IROLE = 41;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IROLE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>System Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IROLE__SYSTEM_ROLE = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Assigned Rights</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IROLE__ASSIGNED_RIGHTS = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IRole</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IROLE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBlob <em>IBlob</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBlob
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBlob()
	 * @generated
	 */
	int IBLOB = 42;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IEncounter <em>IEncounter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IEncounter
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIEncounter()
	 * @generated
	 */
	int IENCOUNTER = 43;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Time Stamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__TIME_STAMP = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__DATE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Billable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__BILLABLE = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Billed</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__BILLED = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Diagnoses</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__DIAGNOSES = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__COVERAGE = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Versioned Entry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__VERSIONED_ENTRY = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER__INVOICE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The number of structural features of the '<em>IEncounter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IENCOUNTER_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBilled <em>IBilled</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBilled
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBilled()
	 * @generated
	 */
	int IBILLED = 44;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Billable</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__BILLABLE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Encounter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__ENCOUNTER = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__PRICE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Scaled Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__SCALED_PRICE = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Net Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__NET_PRICE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__TEXT = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__POINTS = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Factor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__FACTOR = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Primary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__PRIMARY_SCALE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Secondary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__SECONDARY_SCALE = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__CODE = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__TOTAL = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Biller</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED__BILLER = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>IBilled</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLED_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IInvoiceBilled <em>IInvoice Billed</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IInvoiceBilled
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIInvoiceBilled()
	 * @generated
	 */
	int IINVOICE_BILLED = 45;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__LASTUPDATE = IBILLED__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__DELETED = IBILLED__DELETED;

	/**
	 * The feature id for the '<em><b>Billable</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__BILLABLE = IBILLED__BILLABLE;

	/**
	 * The feature id for the '<em><b>Encounter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__ENCOUNTER = IBILLED__ENCOUNTER;

	/**
	 * The feature id for the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__AMOUNT = IBILLED__AMOUNT;

	/**
	 * The feature id for the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__PRICE = IBILLED__PRICE;

	/**
	 * The feature id for the '<em><b>Scaled Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__SCALED_PRICE = IBILLED__SCALED_PRICE;

	/**
	 * The feature id for the '<em><b>Net Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__NET_PRICE = IBILLED__NET_PRICE;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__TEXT = IBILLED__TEXT;

	/**
	 * The feature id for the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__POINTS = IBILLED__POINTS;

	/**
	 * The feature id for the '<em><b>Factor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__FACTOR = IBILLED__FACTOR;

	/**
	 * The feature id for the '<em><b>Primary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__PRIMARY_SCALE = IBILLED__PRIMARY_SCALE;

	/**
	 * The feature id for the '<em><b>Secondary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__SECONDARY_SCALE = IBILLED__SECONDARY_SCALE;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__CODE = IBILLED__CODE;

	/**
	 * The feature id for the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__TOTAL = IBILLED__TOTAL;

	/**
	 * The feature id for the '<em><b>Biller</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__BILLER = IBILLED__BILLER;

	/**
	 * The feature id for the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED__INVOICE = IBILLED_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>IInvoice Billed</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_BILLED_FEATURE_COUNT = IBILLED_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IInvoice <em>IInvoice</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IInvoice
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIInvoice()
	 * @generated
	 */
	int IINVOICE = 46;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__STATE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__NUMBER = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__COVERAGE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Encounters</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__ENCOUNTERS = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Billed</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__BILLED = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__DATE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Date From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__DATE_FROM = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Date To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__DATE_TO = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Total Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__TOTAL_AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Open Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__OPEN_AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Payed Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__PAYED_AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Demand Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__DEMAND_AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__REMARK = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>State Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__STATE_DATE = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Payments</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__PAYMENTS = IDENTIFIABLE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Transactions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__TRANSACTIONS = IDENTIFIABLE_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Attachments</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE__ATTACHMENTS = IDENTIFIABLE_FEATURE_COUNT + 18;

	/**
	 * The number of structural features of the '<em>IInvoice</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IINVOICE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 19;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IStock <em>IStock</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IStock
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIStock()
	 * @generated
	 */
	int ISTOCK = 47;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__CODE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Driver Uuid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__DRIVER_UUID = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Driver Config</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__DRIVER_CONFIG = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__PRIORITY = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__OWNER = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__LOCATION = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Responsible</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__RESPONSIBLE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Stock Entries</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK__STOCK_ENTRIES = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>IStock</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IStockEntry <em>IStock Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IStockEntry
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIStockEntry()
	 * @generated
	 */
	int ISTOCK_ENTRY = 48;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Minimum Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__MINIMUM_STOCK = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Current Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__CURRENT_STOCK = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Maximum Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__MAXIMUM_STOCK = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Fraction Units</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__FRACTION_UNITS = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Stock</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__STOCK = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__ARTICLE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Provider</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY__PROVIDER = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The number of structural features of the '<em>IStock Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTOCK_ENTRY_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IOrderEntry <em>IOrder Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IOrderEntry
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrderEntry()
	 * @generated
	 */
	int IORDER_ENTRY = 49;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Order</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__ORDER = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Stock</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__STOCK = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__ARTICLE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Provider</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__PROVIDER = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY__STATE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>IOrder Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_ENTRY_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IOrder <em>IOrder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IOrder
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrder()
	 * @generated
	 */
	int IORDER = 50;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Entries</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER__ENTRIES = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER__TIMESTAMP = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER__NAME = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IOrder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IORDER_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IAddress <em>IAddress</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IAddress
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAddress()
	 * @generated
	 */
	int IADDRESS = 51;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Street1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__STREET1 = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Street2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__STREET2 = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__ZIP = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__CITY = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__COUNTRY = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Written Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__WRITTEN_ADDRESS = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__TYPE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS__CONTACT = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>IAddress</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IADDRESS_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IImage <em>IImage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IImage
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIImage()
	 * @generated
	 */
	int IIMAGE = 52;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.WithAssignableId <em>With Assignable Id</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.WithAssignableId
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getWithAssignableId()
	 * @generated
	 */
	int WITH_ASSIGNABLE_ID = 53;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IAppointment <em>IAppointment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IAppointment
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAppointment()
	 * @generated
	 */
	int IAPPOINTMENT = 65;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__DATE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__PREFIX = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__TITLE = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Image</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__IMAGE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE__MIME_TYPE = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>IImage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IIMAGE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>With Assignable Id</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WITH_ASSIGNABLE_ID_FEATURE_COUNT = 0;


	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPrescription <em>IPrescription</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPrescription
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPrescription()
	 * @generated
	 */
	int IPRESCRIPTION = 54;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__ARTICLE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Date From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__DATE_FROM = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Date To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__DATE_TO = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Stop Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__STOP_REASON = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Dosage Instruction</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__DOSAGE_INSTRUCTION = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Disposal Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__DISPOSAL_COMMENT = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__REMARK = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Entry Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__ENTRY_TYPE = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__APPLIED = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Sort Order</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__SORT_ORDER = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Prescriptor</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__PRESCRIPTOR = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Recipe</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__RECIPE = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Billed</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION__BILLED = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>IPrescription</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPRESCRIPTION_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 15;


	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IRight <em>IRight</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IRight
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRight()
	 * @generated
	 */
	int IRIGHT = 55;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT__NAME = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Localized Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT__LOCALIZED_NAME = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT__PARENT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IRight</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRIGHT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBillingSystem <em>IBilling System</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBillingSystem
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillingSystem()
	 * @generated
	 */
	int IBILLING_SYSTEM = 56;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM__NAME = 0;

	/**
	 * The feature id for the '<em><b>Law</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM__LAW = 1;

	/**
	 * The number of structural features of the '<em>IBilling System</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBILLING_SYSTEM_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IRecipe <em>IRecipe</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IRecipe
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRecipe()
	 * @generated
	 */
	int IRECIPE = 57;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__DATE = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Prescriptions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__PRESCRIPTIONS = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Document</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE__DOCUMENT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>IRecipe</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRECIPE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IBlobSecondary <em>IBlob Secondary</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IBlobSecondary
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBlobSecondary()
	 * @generated
	 */
	int IBLOB_SECONDARY = 58;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_SECONDARY__LASTUPDATE = IBLOB__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_SECONDARY__DELETED = IBLOB__DELETED;

	/**
	 * The feature id for the '<em><b>Content</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_SECONDARY__CONTENT = IBLOB__CONTENT;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_SECONDARY__DATE = IBLOB__DATE;

	/**
	 * The number of structural features of the '<em>IBlob Secondary</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IBLOB_SECONDARY_FEATURE_COUNT = IBLOB_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IRelatedContact <em>IRelated Contact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IRelatedContact
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRelatedContact()
	 * @generated
	 */
	int IRELATED_CONTACT = 59;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>My Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__MY_CONTACT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Other Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__OTHER_CONTACT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Relationship Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__RELATIONSHIP_DESCRIPTION = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>My Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__MY_TYPE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Other Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT__OTHER_TYPE = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>IRelated Contact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IRELATED_CONTACT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPayment <em>IPayment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPayment
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPayment()
	 * @generated
	 */
	int IPAYMENT = 60;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__INVOICE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__REMARK = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT__DATE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>IPayment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPAYMENT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IAccountTransaction <em>IAccount Transaction</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IAccountTransaction
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAccountTransaction()
	 * @generated
	 */
	int IACCOUNT_TRANSACTION = 61;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__INVOICE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Payment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__PAYMENT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__AMOUNT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__REMARK = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__DATE = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Account</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION__ACCOUNT = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The number of structural features of the '<em>IAccount Transaction</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_TRANSACTION_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IAccount <em>IAccount</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IAccount
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAccount()
	 * @generated
	 */
	int IACCOUNT = 62;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Numeric</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT__NUMERIC = 1;

	/**
	 * The number of structural features of the '<em>IAccount</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IACCOUNT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IMessage <em>IMessage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IMessage
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIMessage()
	 * @generated
	 */
	int IMESSAGE = 63;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Sender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__SENDER = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Receiver</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__RECEIVER = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Sender Accepts Answer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__SENDER_ACCEPTS_ANSWER = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Create Date Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__CREATE_DATE_TIME = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Message Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__MESSAGE_TEXT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Message Codes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__MESSAGE_CODES = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Message Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__MESSAGE_PRIORITY = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Preferred Transporters</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE__PREFERRED_TRANSPORTERS = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>IMessage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMESSAGE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ITextTemplate <em>IText Template</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ITextTemplate
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getITextTemplate()
	 * @generated
	 */
	int ITEXT_TEMPLATE = 64;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__CATEGORY = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__NAME = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Template</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE__TEMPLATE = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>IText Template</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEXT_TEMPLATE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__LASTUPDATE = IPERIOD__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__DELETED = IPERIOD__DELETED;

	/**
	 * The feature id for the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__START_TIME = IPERIOD__START_TIME;

	/**
	 * The feature id for the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__END_TIME = IPERIOD__END_TIME;

	/**
	 * The feature id for the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__REASON = IPERIOD_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__STATE = IPERIOD_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__TYPE = IPERIOD_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Duration Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__DURATION_MINUTES = IPERIOD_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Schedule</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__SCHEDULE = IPERIOD_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Subject Or Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__SUBJECT_OR_PATIENT = IPERIOD_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__PRIORITY = IPERIOD_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Treatment Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__TREATMENT_REASON = IPERIOD_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Case Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__CASE_TYPE = IPERIOD_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Insurance Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__INSURANCE_TYPE = IPERIOD_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Linkgroup</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__LINKGROUP = IPERIOD_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__EXTENSION = IPERIOD_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__CREATED = IPERIOD_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Last Edit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__LAST_EDIT = IPERIOD_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>State History</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__STATE_HISTORY = IPERIOD_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Recurring</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__RECURRING = IPERIOD_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Created By</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT__CREATED_BY = IPERIOD_FEATURE_COUNT + 16;

	/**
	 * The number of structural features of the '<em>IAppointment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_FEATURE_COUNT = IPERIOD_FEATURE_COUNT + 17;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IAppointmentSeries <em>IAppointment Series</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IAppointmentSeries
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAppointmentSeries()
	 * @generated
	 */
	int IAPPOINTMENT_SERIES = 66;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__LASTUPDATE = IAPPOINTMENT__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__DELETED = IAPPOINTMENT__DELETED;

	/**
	 * The feature id for the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__START_TIME = IAPPOINTMENT__START_TIME;

	/**
	 * The feature id for the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__END_TIME = IAPPOINTMENT__END_TIME;

	/**
	 * The feature id for the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__REASON = IAPPOINTMENT__REASON;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__STATE = IAPPOINTMENT__STATE;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__TYPE = IAPPOINTMENT__TYPE;

	/**
	 * The feature id for the '<em><b>Duration Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__DURATION_MINUTES = IAPPOINTMENT__DURATION_MINUTES;

	/**
	 * The feature id for the '<em><b>Schedule</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SCHEDULE = IAPPOINTMENT__SCHEDULE;

	/**
	 * The feature id for the '<em><b>Subject Or Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SUBJECT_OR_PATIENT = IAPPOINTMENT__SUBJECT_OR_PATIENT;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__PRIORITY = IAPPOINTMENT__PRIORITY;

	/**
	 * The feature id for the '<em><b>Treatment Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__TREATMENT_REASON = IAPPOINTMENT__TREATMENT_REASON;

	/**
	 * The feature id for the '<em><b>Case Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__CASE_TYPE = IAPPOINTMENT__CASE_TYPE;

	/**
	 * The feature id for the '<em><b>Insurance Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__INSURANCE_TYPE = IAPPOINTMENT__INSURANCE_TYPE;

	/**
	 * The feature id for the '<em><b>Linkgroup</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__LINKGROUP = IAPPOINTMENT__LINKGROUP;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__EXTENSION = IAPPOINTMENT__EXTENSION;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__CREATED = IAPPOINTMENT__CREATED;

	/**
	 * The feature id for the '<em><b>Last Edit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__LAST_EDIT = IAPPOINTMENT__LAST_EDIT;

	/**
	 * The feature id for the '<em><b>State History</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__STATE_HISTORY = IAPPOINTMENT__STATE_HISTORY;

	/**
	 * The feature id for the '<em><b>Recurring</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__RECURRING = IAPPOINTMENT__RECURRING;

	/**
	 * The feature id for the '<em><b>Created By</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__CREATED_BY = IAPPOINTMENT__CREATED_BY;

	/**
	 * The feature id for the '<em><b>Series Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_TYPE = IAPPOINTMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Ending Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__ENDING_TYPE = IAPPOINTMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Series Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_START_DATE = IAPPOINTMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Series Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_START_TIME = IAPPOINTMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Series End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_END_DATE = IAPPOINTMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Series End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_END_TIME = IAPPOINTMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Series Pattern String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__SERIES_PATTERN_STRING = IAPPOINTMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Ending Pattern String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__ENDING_PATTERN_STRING = IAPPOINTMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Persistent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__PERSISTENT = IAPPOINTMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Root Appointment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES__ROOT_APPOINTMENT = IAPPOINTMENT_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>IAppointment Series</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IAPPOINTMENT_SERIES_FEATURE_COUNT = IAPPOINTMENT_FEATURE_COUNT + 10;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ISickCertificate <em>ISick Certificate</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ISickCertificate
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISickCertificate()
	 * @generated
	 */
	int ISICK_CERTIFICATE = 67;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__COVERAGE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Letter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__LETTER = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__PERCENT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__DATE = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__START = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>End</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__END = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__REASON = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Note</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE__NOTE = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>ISick Certificate</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISICK_CERTIFICATE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IDayMessage <em>IDay Message</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IDayMessage
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDayMessage()
	 * @generated
	 */
	int IDAY_MESSAGE = 68;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE__TITLE = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE__MESSAGE = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE__DATE = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>IDay Message</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDAY_MESSAGE_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IReminder <em>IReminder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IReminder
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIReminder()
	 * @generated
	 */
	int IREMINDER = 69;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__DELETED = DELETEABLE__DELETED;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__LASTUPDATE = DELETEABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__CREATOR = DELETEABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Responsible</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__RESPONSIBLE = DELETEABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__CONTACT = DELETEABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Due</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__DUE = DELETEABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__STATUS = DELETEABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Visibility</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__VISIBILITY = DELETEABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Subject</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__SUBJECT = DELETEABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__MESSAGE = DELETEABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__PRIORITY = DELETEABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__TYPE = DELETEABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Responsible All</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER__RESPONSIBLE_ALL = DELETEABLE_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>IReminder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_FEATURE_COUNT = DELETEABLE_FEATURE_COUNT + 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IReminderResponsibleLink <em>IReminder Responsible Link</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IReminderResponsibleLink
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIReminderResponsibleLink()
	 * @generated
	 */
	int IREMINDER_RESPONSIBLE_LINK = 70;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_RESPONSIBLE_LINK__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_RESPONSIBLE_LINK__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Reminder</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_RESPONSIBLE_LINK__REMINDER = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Responsible</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_RESPONSIBLE_LINK__RESPONSIBLE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>IReminder Responsible Link</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IREMINDER_RESPONSIBLE_LINK_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IVaccination <em>IVaccination</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IVaccination
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIVaccination()
	 * @generated
	 */
	int IVACCINATION = 71;

	/**
	 * The feature id for the '<em><b>Lastupdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__LASTUPDATE = IDENTIFIABLE__LASTUPDATE;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__ARTICLE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Article Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__ARTICLE_NAME = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Article Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__ARTICLE_GTIN = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Article Atc</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__ARTICLE_ATC = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Lot Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__LOT_NUMBER = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Date Of Administration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__DATE_OF_ADMINISTRATION = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Ingredients Atc</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__INGREDIENTS_ATC = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Performer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION__PERFORMER = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>IVaccination</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IVACCINATION_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 10;

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.Identifiable#getLastupdate <em>Lastupdate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lastupdate</em>'.
	 * @see ch.elexis.core.model.Identifiable#getLastupdate()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Lastupdate();

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
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IContact#getAddress <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Address</em>'.
	 * @see ch.elexis.core.model.IContact#getAddress()
	 * @see #getIContact()
	 * @generated
	 */
	EReference getIContact_Address();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getGroup <em>Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Group</em>'.
	 * @see ch.elexis.core.model.IContact#getGroup()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Group();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getPostalAddress <em>Postal Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Postal Address</em>'.
	 * @see ch.elexis.core.model.IContact#getPostalAddress()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_PostalAddress();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IContact#getImage <em>Image</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Image</em>'.
	 * @see ch.elexis.core.model.IContact#getImage()
	 * @see #getIContact()
	 * @generated
	 */
	EReference getIContact_Image();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IContact#getRelatedContacts <em>Related Contacts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Related Contacts</em>'.
	 * @see ch.elexis.core.model.IContact#getRelatedContacts()
	 * @see #getIContact()
	 * @generated
	 */
	EReference getIContact_RelatedContacts();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#isDeceased <em>Deceased</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Deceased</em>'.
	 * @see ch.elexis.core.model.IContact#isDeceased()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Deceased();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getEmail2 <em>Email2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Email2</em>'.
	 * @see ch.elexis.core.model.IContact#getEmail2()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_Email2();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getMaritalStatus <em>Marital Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Marital Status</em>'.
	 * @see ch.elexis.core.model.IPerson#getMaritalStatus()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_MaritalStatus();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPerson#getLegalGuardian <em>Legal Guardian</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Legal Guardian</em>'.
	 * @see ch.elexis.core.model.IPerson#getLegalGuardian()
	 * @see #getIPerson()
	 * @generated
	 */
	EReference getIPerson_LegalGuardian();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPerson#getDateOfDeath <em>Date Of Death</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date Of Death</em>'.
	 * @see ch.elexis.core.model.IPerson#getDateOfDeath()
	 * @see #getIPerson()
	 * @generated
	 */
	EAttribute getIPerson_DateOfDeath();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrganization#getInsuranceXmlName <em>Insurance Xml Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Insurance Xml Name</em>'.
	 * @see ch.elexis.core.model.IOrganization#getInsuranceXmlName()
	 * @see #getIOrganization()
	 * @generated
	 */
	EAttribute getIOrganization_InsuranceXmlName();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrganization#getInsuranceLawCode <em>Insurance Law Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Insurance Law Code</em>'.
	 * @see ch.elexis.core.model.IOrganization#getInsuranceLawCode()
	 * @see #getIOrganization()
	 * @generated
	 */
	EAttribute getIOrganization_InsuranceLawCode();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPatient</em>'.
	 * @see ch.elexis.core.model.IPatient
	 * @generated
	 */
	EClass getIPatient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPatient#getFamilyDoctor <em>Family Doctor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Family Doctor</em>'.
	 * @see ch.elexis.core.model.IPatient#getFamilyDoctor()
	 * @see #getIPatient()
	 * @generated
	 */
	EReference getIPatient_FamilyDoctor();

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
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IPatient#getCoverages <em>Coverages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Coverages</em>'.
	 * @see ch.elexis.core.model.IPatient#getCoverages()
	 * @see #getIPatient()
	 * @generated
	 */
	EReference getIPatient_Coverages();

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
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IMandator#getBiller <em>Biller</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Biller</em>'.
	 * @see ch.elexis.core.model.IMandator#getBiller()
	 * @see #getIMandator()
	 * @generated
	 */
	EReference getIMandator_Biller();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMandator#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see ch.elexis.core.model.IMandator#isActive()
	 * @see #getIMandator()
	 * @generated
	 */
	EAttribute getIMandator_Active();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#getHashedPassword <em>Hashed Password</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Hashed Password</em>'.
	 * @see ch.elexis.core.model.IUser#getHashedPassword()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_HashedPassword();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#getSalt <em>Salt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Salt</em>'.
	 * @see ch.elexis.core.model.IUser#getSalt()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_Salt();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see ch.elexis.core.model.IUser#isActive()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_Active();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#isAllowExternal <em>Allow External</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow External</em>'.
	 * @see ch.elexis.core.model.IUser#isAllowExternal()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_AllowExternal();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUser#isAdministrator <em>Administrator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Administrator</em>'.
	 * @see ch.elexis.core.model.IUser#isAdministrator()
	 * @see #getIUser()
	 * @generated
	 */
	EAttribute getIUser_Administrator();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IUserGroup <em>IUser Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IUser Group</em>'.
	 * @see ch.elexis.core.model.IUserGroup
	 * @generated
	 */
	EClass getIUserGroup();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IUserGroup#getUsers <em>Users</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Users</em>'.
	 * @see ch.elexis.core.model.IUserGroup#getUsers()
	 * @see #getIUserGroup()
	 * @generated
	 */
	EReference getIUserGroup_Users();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IUserGroup#getRoles <em>Roles</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Roles</em>'.
	 * @see ch.elexis.core.model.IUserGroup#getRoles()
	 * @see #getIUserGroup()
	 * @generated
	 */
	EReference getIUserGroup_Roles();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IUserGroup#getGroupname <em>Groupname</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Groupname</em>'.
	 * @see ch.elexis.core.model.IUserGroup#getGroupname()
	 * @see #getIUserGroup()
	 * @generated
	 */
	EAttribute getIUserGroup_Groupname();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getExport <em>Export</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Export</em>'.
	 * @see ch.elexis.core.model.ILabItem#getExport()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Export();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.ILabItem#getMappings <em>Mappings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Mappings</em>'.
	 * @see ch.elexis.core.model.ILabItem#getMappings()
	 * @see #getILabItem()
	 * @generated
	 */
	EReference getILabItem_Mappings();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#getGroupName <em>Group Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Group Name</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getGroupName()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_GroupName();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabOrder#isUserResolved <em>User Resolved</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User Resolved</em>'.
	 * @see ch.elexis.core.model.ILabOrder#isUserResolved()
	 * @see #getILabOrder()
	 * @generated
	 */
	EAttribute getILabOrder_UserResolved();

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
	 * Returns the meta object for the attribute list '{@link ch.elexis.core.model.IDocument#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Status</em>'.
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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDocument Letter</em>'.
	 * @see ch.elexis.core.model.IDocumentLetter
	 * @generated
	 */
	EClass getIDocumentLetter();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocumentLetter#getEncounter <em>Encounter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Encounter</em>'.
	 * @see ch.elexis.core.model.IDocumentLetter#getEncounter()
	 * @see #getIDocumentLetter()
	 * @generated
	 */
	EReference getIDocumentLetter_Encounter();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocumentLetter#getRecipient <em>Recipient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Recipient</em>'.
	 * @see ch.elexis.core.model.IDocumentLetter#getRecipient()
	 * @see #getIDocumentLetter()
	 * @generated
	 */
	EReference getIDocumentLetter_Recipient();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDocumentTemplate <em>IDocument Template</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDocument Template</em>'.
	 * @see ch.elexis.core.model.IDocumentTemplate
	 * @generated
	 */
	EClass getIDocumentTemplate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocumentTemplate#getTemplateTyp <em>Template Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Template Typ</em>'.
	 * @see ch.elexis.core.model.IDocumentTemplate#getTemplateTyp()
	 * @see #getIDocumentTemplate()
	 * @generated
	 */
	EAttribute getIDocumentTemplate_TemplateTyp();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDocumentTemplate#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.IDocumentTemplate#getMandator()
	 * @see #getIDocumentTemplate()
	 * @generated
	 */
	EReference getIDocumentTemplate_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocumentTemplate#isAskForAddressee <em>Ask For Addressee</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ask For Addressee</em>'.
	 * @see ch.elexis.core.model.IDocumentTemplate#isAskForAddressee()
	 * @see #getIDocumentTemplate()
	 * @generated
	 */
	EAttribute getIDocumentTemplate_AskForAddressee();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.ISticker#getName()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getImportance <em>Importance</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Importance</em>'.
	 * @see ch.elexis.core.model.ISticker#getImportance()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_Importance();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ISticker#getImage <em>Image</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Image</em>'.
	 * @see ch.elexis.core.model.ISticker#getImage()
	 * @see #getISticker()
	 * @generated
	 */
	EReference getISticker_Image();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ISticker#getAttachedTo <em>Attached To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Attached To</em>'.
	 * @see ch.elexis.core.model.ISticker#getAttachedTo()
	 * @see #getISticker()
	 * @generated
	 */
	EReference getISticker_AttachedTo();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISticker#getAttachedToData <em>Attached To Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Attached To Data</em>'.
	 * @see ch.elexis.core.model.ISticker#getAttachedToData()
	 * @see #getISticker()
	 * @generated
	 */
	EAttribute getISticker_AttachedToData();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICodeElement#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.model.ICodeElement#getCode()
	 * @see #getICodeElement()
	 * @generated
	 */
	EAttribute getICodeElement_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICodeElement#getText <em>Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text</em>'.
	 * @see ch.elexis.core.model.ICodeElement#getText()
	 * @see #getICodeElement()
	 * @generated
	 */
	EAttribute getICodeElement_Text();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICodeElementBlock <em>ICode Element Block</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICode Element Block</em>'.
	 * @see ch.elexis.core.model.ICodeElementBlock
	 * @generated
	 */
	EClass getICodeElementBlock();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.ICodeElementBlock#getElements <em>Elements</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Elements</em>'.
	 * @see ch.elexis.core.model.ICodeElementBlock#getElements()
	 * @see #getICodeElementBlock()
	 * @generated
	 */
	EReference getICodeElementBlock_Elements();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.ICodeElementBlock#getElementReferences <em>Element References</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Element References</em>'.
	 * @see ch.elexis.core.model.ICodeElementBlock#getElementReferences()
	 * @see #getICodeElementBlock()
	 * @generated
	 */
	EReference getICodeElementBlock_ElementReferences();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ICodeElementBlock#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.ICodeElementBlock#getMandator()
	 * @see #getICodeElementBlock()
	 * @generated
	 */
	EReference getICodeElementBlock_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICodeElementBlock#getMacro <em>Macro</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Macro</em>'.
	 * @see ch.elexis.core.model.ICodeElementBlock#getMacro()
	 * @see #getICodeElementBlock()
	 * @generated
	 */
	EAttribute getICodeElementBlock_Macro();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBillableVerifier <em>IBillable Verifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBillable Verifier</em>'.
	 * @see ch.elexis.core.model.IBillableVerifier
	 * @generated
	 */
	EClass getIBillableVerifier();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBillableOptifier <em>IBillable Optifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBillable Optifier</em>'.
	 * @see ch.elexis.core.model.IBillableOptifier
	 * @generated
	 */
	EClass getIBillableOptifier();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IService <em>IService</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IService</em>'.
	 * @see ch.elexis.core.model.IService
	 * @generated
	 */
	EClass getIService();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IService#getPrice <em>Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Price</em>'.
	 * @see ch.elexis.core.model.IService#getPrice()
	 * @see #getIService()
	 * @generated
	 */
	EAttribute getIService_Price();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IService#getNetPrice <em>Net Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Net Price</em>'.
	 * @see ch.elexis.core.model.IService#getNetPrice()
	 * @see #getIService()
	 * @generated
	 */
	EAttribute getIService_NetPrice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IService#getMinutes <em>Minutes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Minutes</em>'.
	 * @see ch.elexis.core.model.IService#getMinutes()
	 * @see #getIService()
	 * @generated
	 */
	EAttribute getIService_Minutes();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICustomService <em>ICustom Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICustom Service</em>'.
	 * @see ch.elexis.core.model.ICustomService
	 * @generated
	 */
	EClass getICustomService();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getAtcCode <em>Atc Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Atc Code</em>'.
	 * @see ch.elexis.core.model.IArticle#getAtcCode()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_AtcCode();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getSellingSize <em>Selling Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selling Size</em>'.
	 * @see ch.elexis.core.model.IArticle#getSellingSize()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_SellingSize();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getPackageSize <em>Package Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Package Size</em>'.
	 * @see ch.elexis.core.model.IArticle#getPackageSize()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_PackageSize();

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
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IArticle#getProduct <em>Product</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Product</em>'.
	 * @see ch.elexis.core.model.IArticle#getProduct()
	 * @see #getIArticle()
	 * @generated
	 */
	EReference getIArticle_Product();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getPurchasePrice <em>Purchase Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Purchase Price</em>'.
	 * @see ch.elexis.core.model.IArticle#getPurchasePrice()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_PurchasePrice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getSellingPrice <em>Selling Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selling Price</em>'.
	 * @see ch.elexis.core.model.IArticle#getSellingPrice()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_SellingPrice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#isObligation <em>Obligation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Obligation</em>'.
	 * @see ch.elexis.core.model.IArticle#isObligation()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_Obligation();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getTyp <em>Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Typ</em>'.
	 * @see ch.elexis.core.model.IArticle#getTyp()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_Typ();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getSubTyp <em>Sub Typ</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sub Typ</em>'.
	 * @see ch.elexis.core.model.IArticle#getSubTyp()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_SubTyp();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticle#getPackageSizeString <em>Package Size String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Package Size String</em>'.
	 * @see ch.elexis.core.model.IArticle#getPackageSizeString()
	 * @see #getIArticle()
	 * @generated
	 */
	EAttribute getIArticle_PackageSizeString();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IArticleDefaultSignature <em>IArticle Default Signature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IArticle Default Signature</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature
	 * @generated
	 */
	EClass getIArticleDefaultSignature();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getAtcCode <em>Atc Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Atc Code</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getAtcCode()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_AtcCode();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getMorning <em>Morning</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Morning</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getMorning()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_Morning();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getNoon <em>Noon</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Noon</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getNoon()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_Noon();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getEvening <em>Evening</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Evening</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getEvening()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_Evening();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getNight <em>Night</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Night</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getNight()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_Night();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getComment()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_Comment();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getFreeText <em>Free Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Free Text</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getFreeText()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_FreeText();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getMedicationType <em>Medication Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Medication Type</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getMedicationType()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_MedicationType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getDisposalType <em>Disposal Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Disposal Type</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getDisposalType()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_DisposalType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getEndDate <em>End Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>End Date</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getEndDate()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_EndDate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IArticleDefaultSignature#getStartDate <em>Start Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Date</em>'.
	 * @see ch.elexis.core.model.IArticleDefaultSignature#getStartDate()
	 * @see #getIArticleDefaultSignature()
	 * @generated
	 */
	EAttribute getIArticleDefaultSignature_StartDate();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDiagnosis <em>IDiagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDiagnosis</em>'.
	 * @see ch.elexis.core.model.IDiagnosis
	 * @generated
	 */
	EClass getIDiagnosis();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDiagnosis#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see ch.elexis.core.model.IDiagnosis#getDescription()
	 * @see #getIDiagnosis()
	 * @generated
	 */
	EAttribute getIDiagnosis_Description();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IFreeTextDiagnosis <em>IFree Text Diagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IFree Text Diagnosis</em>'.
	 * @see ch.elexis.core.model.IFreeTextDiagnosis
	 * @generated
	 */
	EClass getIFreeTextDiagnosis();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDiagnosisReference <em>IDiagnosis Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDiagnosis Reference</em>'.
	 * @see ch.elexis.core.model.IDiagnosisReference
	 * @generated
	 */
	EClass getIDiagnosisReference();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDiagnosisReference#getReferredClass <em>Referred Class</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Referred Class</em>'.
	 * @see ch.elexis.core.model.IDiagnosisReference#getReferredClass()
	 * @see #getIDiagnosisReference()
	 * @generated
	 */
	EAttribute getIDiagnosisReference_ReferredClass();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDiagnosisTree <em>IDiagnosis Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDiagnosis Tree</em>'.
	 * @see ch.elexis.core.model.IDiagnosisTree
	 * @generated
	 */
	EClass getIDiagnosisTree();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IDiagnosisTree#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Parent</em>'.
	 * @see ch.elexis.core.model.IDiagnosisTree#getParent()
	 * @see #getIDiagnosisTree()
	 * @generated
	 */
	EReference getIDiagnosisTree_Parent();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IDiagnosisTree#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Children</em>'.
	 * @see ch.elexis.core.model.IDiagnosisTree#getChildren()
	 * @see #getIDiagnosisTree()
	 * @generated
	 */
	EReference getIDiagnosisTree_Children();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ICustomDiagnosis <em>ICustom Diagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ICustom Diagnosis</em>'.
	 * @see ch.elexis.core.model.ICustomDiagnosis
	 * @generated
	 */
	EClass getICustomDiagnosis();

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
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ICoverage#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.ICoverage#getPatient()
	 * @see #getICoverage()
	 * @generated
	 */
	EReference getICoverage_Patient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see ch.elexis.core.model.ICoverage#getDescription()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_Description();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getReason <em>Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reason</em>'.
	 * @see ch.elexis.core.model.ICoverage#getReason()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_Reason();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getDateFrom <em>Date From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date From</em>'.
	 * @see ch.elexis.core.model.ICoverage#getDateFrom()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_DateFrom();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ICoverage#getBillingSystem <em>Billing System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Billing System</em>'.
	 * @see ch.elexis.core.model.ICoverage#getBillingSystem()
	 * @see #getICoverage()
	 * @generated
	 */
	EReference getICoverage_BillingSystem();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ICoverage#getGuarantor <em>Guarantor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Guarantor</em>'.
	 * @see ch.elexis.core.model.ICoverage#getGuarantor()
	 * @see #getICoverage()
	 * @generated
	 */
	EReference getICoverage_Guarantor();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ICoverage#getCostBearer <em>Cost Bearer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Cost Bearer</em>'.
	 * @see ch.elexis.core.model.ICoverage#getCostBearer()
	 * @see #getICoverage()
	 * @generated
	 */
	EReference getICoverage_CostBearer();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getInsuranceNumber <em>Insurance Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Insurance Number</em>'.
	 * @see ch.elexis.core.model.ICoverage#getInsuranceNumber()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_InsuranceNumber();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getDateTo <em>Date To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date To</em>'.
	 * @see ch.elexis.core.model.ICoverage#getDateTo()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_DateTo();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ICoverage#getBillingProposalDate <em>Billing Proposal Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Billing Proposal Date</em>'.
	 * @see ch.elexis.core.model.ICoverage#getBillingProposalDate()
	 * @see #getICoverage()
	 * @generated
	 */
	EAttribute getICoverage_BillingProposalDate();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.ICoverage#getEncounters <em>Encounters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Encounters</em>'.
	 * @see ch.elexis.core.model.ICoverage#getEncounters()
	 * @see #getICoverage()
	 * @generated
	 */
	EReference getICoverage_Encounters();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBillingSystemFactor <em>IBilling System Factor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBilling System Factor</em>'.
	 * @see ch.elexis.core.model.IBillingSystemFactor
	 * @generated
	 */
	EClass getIBillingSystemFactor();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystemFactor#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>System</em>'.
	 * @see ch.elexis.core.model.IBillingSystemFactor#getSystem()
	 * @see #getIBillingSystemFactor()
	 * @generated
	 */
	EAttribute getIBillingSystemFactor_System();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystemFactor#getFactor <em>Factor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Factor</em>'.
	 * @see ch.elexis.core.model.IBillingSystemFactor#getFactor()
	 * @see #getIBillingSystemFactor()
	 * @generated
	 */
	EAttribute getIBillingSystemFactor_Factor();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystemFactor#getValidFrom <em>Valid From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Valid From</em>'.
	 * @see ch.elexis.core.model.IBillingSystemFactor#getValidFrom()
	 * @see #getIBillingSystemFactor()
	 * @generated
	 */
	EAttribute getIBillingSystemFactor_ValidFrom();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystemFactor#getValidTo <em>Valid To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Valid To</em>'.
	 * @see ch.elexis.core.model.IBillingSystemFactor#getValidTo()
	 * @see #getIBillingSystemFactor()
	 * @generated
	 */
	EAttribute getIBillingSystemFactor_ValidTo();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRole#isSystemRole <em>System Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>System Role</em>'.
	 * @see ch.elexis.core.model.IRole#isSystemRole()
	 * @see #getIRole()
	 * @generated
	 */
	EAttribute getIRole_SystemRole();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IRole#getAssignedRights <em>Assigned Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Assigned Rights</em>'.
	 * @see ch.elexis.core.model.IRole#getAssignedRights()
	 * @see #getIRole()
	 * @generated
	 */
	EReference getIRole_AssignedRights();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IEncounter <em>IEncounter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IEncounter</em>'.
	 * @see ch.elexis.core.model.IEncounter
	 * @generated
	 */
	EClass getIEncounter();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IEncounter#getTimeStamp <em>Time Stamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time Stamp</em>'.
	 * @see ch.elexis.core.model.IEncounter#getTimeStamp()
	 * @see #getIEncounter()
	 * @generated
	 */
	EAttribute getIEncounter_TimeStamp();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IEncounter#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IEncounter#getPatient()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IEncounter#getCoverage <em>Coverage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Coverage</em>'.
	 * @see ch.elexis.core.model.IEncounter#getCoverage()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Coverage();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IEncounter#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.IEncounter#getMandator()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Mandator();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IEncounter#getBilled <em>Billed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Billed</em>'.
	 * @see ch.elexis.core.model.IEncounter#getBilled()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Billed();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IEncounter#getDiagnoses <em>Diagnoses</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Diagnoses</em>'.
	 * @see ch.elexis.core.model.IEncounter#getDiagnoses()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Diagnoses();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IEncounter#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IEncounter#getDate()
	 * @see #getIEncounter()
	 * @generated
	 */
	EAttribute getIEncounter_Date();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IEncounter#isBillable <em>Billable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Billable</em>'.
	 * @see ch.elexis.core.model.IEncounter#isBillable()
	 * @see #getIEncounter()
	 * @generated
	 */
	EAttribute getIEncounter_Billable();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IEncounter#getVersionedEntry <em>Versioned Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Versioned Entry</em>'.
	 * @see ch.elexis.core.model.IEncounter#getVersionedEntry()
	 * @see #getIEncounter()
	 * @generated
	 */
	EAttribute getIEncounter_VersionedEntry();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IEncounter#getInvoice <em>Invoice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Invoice</em>'.
	 * @see ch.elexis.core.model.IEncounter#getInvoice()
	 * @see #getIEncounter()
	 * @generated
	 */
	EReference getIEncounter_Invoice();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBilled <em>IBilled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBilled</em>'.
	 * @see ch.elexis.core.model.IBilled
	 * @generated
	 */
	EClass getIBilled();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IBilled#getBillable <em>Billable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Billable</em>'.
	 * @see ch.elexis.core.model.IBilled#getBillable()
	 * @see #getIBilled()
	 * @generated
	 */
	EReference getIBilled_Billable();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IBilled#getEncounter <em>Encounter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Encounter</em>'.
	 * @see ch.elexis.core.model.IBilled#getEncounter()
	 * @see #getIBilled()
	 * @generated
	 */
	EReference getIBilled_Encounter();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getAmount <em>Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Amount</em>'.
	 * @see ch.elexis.core.model.IBilled#getAmount()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Amount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getPrice <em>Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Price</em>'.
	 * @see ch.elexis.core.model.IBilled#getPrice()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Price();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getScaledPrice <em>Scaled Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scaled Price</em>'.
	 * @see ch.elexis.core.model.IBilled#getScaledPrice()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_ScaledPrice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getNetPrice <em>Net Price</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Net Price</em>'.
	 * @see ch.elexis.core.model.IBilled#getNetPrice()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_NetPrice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getText <em>Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text</em>'.
	 * @see ch.elexis.core.model.IBilled#getText()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Text();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getPoints <em>Points</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Points</em>'.
	 * @see ch.elexis.core.model.IBilled#getPoints()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Points();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getFactor <em>Factor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Factor</em>'.
	 * @see ch.elexis.core.model.IBilled#getFactor()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Factor();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getPrimaryScale <em>Primary Scale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Primary Scale</em>'.
	 * @see ch.elexis.core.model.IBilled#getPrimaryScale()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_PrimaryScale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getSecondaryScale <em>Secondary Scale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Secondary Scale</em>'.
	 * @see ch.elexis.core.model.IBilled#getSecondaryScale()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_SecondaryScale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.model.IBilled#getCode()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBilled#getTotal <em>Total</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total</em>'.
	 * @see ch.elexis.core.model.IBilled#getTotal()
	 * @see #getIBilled()
	 * @generated
	 */
	EAttribute getIBilled_Total();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IBilled#getBiller <em>Biller</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Biller</em>'.
	 * @see ch.elexis.core.model.IBilled#getBiller()
	 * @see #getIBilled()
	 * @generated
	 */
	EReference getIBilled_Biller();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IInvoiceBilled <em>IInvoice Billed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IInvoice Billed</em>'.
	 * @see ch.elexis.core.model.IInvoiceBilled
	 * @generated
	 */
	EClass getIInvoiceBilled();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IInvoiceBilled#getInvoice <em>Invoice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Invoice</em>'.
	 * @see ch.elexis.core.model.IInvoiceBilled#getInvoice()
	 * @see #getIInvoiceBilled()
	 * @generated
	 */
	EReference getIInvoiceBilled_Invoice();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IInvoice <em>IInvoice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IInvoice</em>'.
	 * @see ch.elexis.core.model.IInvoice
	 * @generated
	 */
	EClass getIInvoice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State</em>'.
	 * @see ch.elexis.core.model.IInvoice#getState()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_State();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getNumber <em>Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number</em>'.
	 * @see ch.elexis.core.model.IInvoice#getNumber()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_Number();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IInvoice#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.IInvoice#getMandator()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Mandator();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IInvoice#getCoverage <em>Coverage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Coverage</em>'.
	 * @see ch.elexis.core.model.IInvoice#getCoverage()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Coverage();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IInvoice#getEncounters <em>Encounters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Encounters</em>'.
	 * @see ch.elexis.core.model.IInvoice#getEncounters()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Encounters();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IInvoice#getBilled <em>Billed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Billed</em>'.
	 * @see ch.elexis.core.model.IInvoice#getBilled()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Billed();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IInvoice#getDate()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_Date();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getDateFrom <em>Date From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date From</em>'.
	 * @see ch.elexis.core.model.IInvoice#getDateFrom()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_DateFrom();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getDateTo <em>Date To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date To</em>'.
	 * @see ch.elexis.core.model.IInvoice#getDateTo()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_DateTo();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getTotalAmount <em>Total Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Amount</em>'.
	 * @see ch.elexis.core.model.IInvoice#getTotalAmount()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_TotalAmount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getOpenAmount <em>Open Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Open Amount</em>'.
	 * @see ch.elexis.core.model.IInvoice#getOpenAmount()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_OpenAmount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getPayedAmount <em>Payed Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Payed Amount</em>'.
	 * @see ch.elexis.core.model.IInvoice#getPayedAmount()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_PayedAmount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getDemandAmount <em>Demand Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Demand Amount</em>'.
	 * @see ch.elexis.core.model.IInvoice#getDemandAmount()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_DemandAmount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getRemark <em>Remark</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Remark</em>'.
	 * @see ch.elexis.core.model.IInvoice#getRemark()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_Remark();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IInvoice#getPayments <em>Payments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Payments</em>'.
	 * @see ch.elexis.core.model.IInvoice#getPayments()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Payments();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IInvoice#getTransactions <em>Transactions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Transactions</em>'.
	 * @see ch.elexis.core.model.IInvoice#getTransactions()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Transactions();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IInvoice#getAttachments <em>Attachments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Attachments</em>'.
	 * @see ch.elexis.core.model.IInvoice#getAttachments()
	 * @see #getIInvoice()
	 * @generated
	 */
	EReference getIInvoice_Attachments();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IInvoice#getStateDate <em>State Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State Date</em>'.
	 * @see ch.elexis.core.model.IInvoice#getStateDate()
	 * @see #getIInvoice()
	 * @generated
	 */
	EAttribute getIInvoice_StateDate();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IStock <em>IStock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IStock</em>'.
	 * @see ch.elexis.core.model.IStock
	 * @generated
	 */
	EClass getIStock();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStock#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.model.IStock#getCode()
	 * @see #getIStock()
	 * @generated
	 */
	EAttribute getIStock_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStock#getDriverUuid <em>Driver Uuid</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Driver Uuid</em>'.
	 * @see ch.elexis.core.model.IStock#getDriverUuid()
	 * @see #getIStock()
	 * @generated
	 */
	EAttribute getIStock_DriverUuid();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStock#getDriverConfig <em>Driver Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Driver Config</em>'.
	 * @see ch.elexis.core.model.IStock#getDriverConfig()
	 * @see #getIStock()
	 * @generated
	 */
	EAttribute getIStock_DriverConfig();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStock#getPriority <em>Priority</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Priority</em>'.
	 * @see ch.elexis.core.model.IStock#getPriority()
	 * @see #getIStock()
	 * @generated
	 */
	EAttribute getIStock_Priority();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IStock#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Owner</em>'.
	 * @see ch.elexis.core.model.IStock#getOwner()
	 * @see #getIStock()
	 * @generated
	 */
	EReference getIStock_Owner();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStock#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Location</em>'.
	 * @see ch.elexis.core.model.IStock#getLocation()
	 * @see #getIStock()
	 * @generated
	 */
	EAttribute getIStock_Location();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IStock#getResponsible <em>Responsible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Responsible</em>'.
	 * @see ch.elexis.core.model.IStock#getResponsible()
	 * @see #getIStock()
	 * @generated
	 */
	EReference getIStock_Responsible();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IStock#getStockEntries <em>Stock Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Stock Entries</em>'.
	 * @see ch.elexis.core.model.IStock#getStockEntries()
	 * @see #getIStock()
	 * @generated
	 */
	EReference getIStock_StockEntries();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IStockEntry <em>IStock Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IStock Entry</em>'.
	 * @see ch.elexis.core.model.IStockEntry
	 * @generated
	 */
	EClass getIStockEntry();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStockEntry#getMinimumStock <em>Minimum Stock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Minimum Stock</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getMinimumStock()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EAttribute getIStockEntry_MinimumStock();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStockEntry#getCurrentStock <em>Current Stock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Current Stock</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getCurrentStock()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EAttribute getIStockEntry_CurrentStock();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStockEntry#getMaximumStock <em>Maximum Stock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Maximum Stock</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getMaximumStock()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EAttribute getIStockEntry_MaximumStock();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IStockEntry#getFractionUnits <em>Fraction Units</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fraction Units</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getFractionUnits()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EAttribute getIStockEntry_FractionUnits();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IStockEntry#getStock <em>Stock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Stock</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getStock()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EReference getIStockEntry_Stock();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IStockEntry#getArticle <em>Article</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Article</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getArticle()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EReference getIStockEntry_Article();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IStockEntry#getProvider <em>Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Provider</em>'.
	 * @see ch.elexis.core.model.IStockEntry#getProvider()
	 * @see #getIStockEntry()
	 * @generated
	 */
	EReference getIStockEntry_Provider();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IOrderEntry <em>IOrder Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IOrder Entry</em>'.
	 * @see ch.elexis.core.model.IOrderEntry
	 * @generated
	 */
	EClass getIOrderEntry();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IOrderEntry#getOrder <em>Order</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Order</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getOrder()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EReference getIOrderEntry_Order();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IOrderEntry#getStock <em>Stock</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Stock</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getStock()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EReference getIOrderEntry_Stock();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrderEntry#getAmount <em>Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Amount</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getAmount()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EAttribute getIOrderEntry_Amount();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IOrderEntry#getArticle <em>Article</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Article</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getArticle()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EReference getIOrderEntry_Article();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IOrderEntry#getProvider <em>Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Provider</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getProvider()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EReference getIOrderEntry_Provider();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrderEntry#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State</em>'.
	 * @see ch.elexis.core.model.IOrderEntry#getState()
	 * @see #getIOrderEntry()
	 * @generated
	 */
	EAttribute getIOrderEntry_State();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IOrder <em>IOrder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IOrder</em>'.
	 * @see ch.elexis.core.model.IOrder
	 * @generated
	 */
	EClass getIOrder();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IOrder#getEntries <em>Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Entries</em>'.
	 * @see ch.elexis.core.model.IOrder#getEntries()
	 * @see #getIOrder()
	 * @generated
	 */
	EReference getIOrder_Entries();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrder#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see ch.elexis.core.model.IOrder#getTimestamp()
	 * @see #getIOrder()
	 * @generated
	 */
	EAttribute getIOrder_Timestamp();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IOrder#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.IOrder#getName()
	 * @see #getIOrder()
	 * @generated
	 */
	EAttribute getIOrder_Name();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IAddress <em>IAddress</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAddress</em>'.
	 * @see ch.elexis.core.model.IAddress
	 * @generated
	 */
	EClass getIAddress();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getStreet1 <em>Street1</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Street1</em>'.
	 * @see ch.elexis.core.model.IAddress#getStreet1()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_Street1();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getStreet2 <em>Street2</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Street2</em>'.
	 * @see ch.elexis.core.model.IAddress#getStreet2()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_Street2();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getZip <em>Zip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Zip</em>'.
	 * @see ch.elexis.core.model.IAddress#getZip()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_Zip();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getCity <em>City</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>City</em>'.
	 * @see ch.elexis.core.model.IAddress#getCity()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_City();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getCountry <em>Country</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Country</em>'.
	 * @see ch.elexis.core.model.IAddress#getCountry()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_Country();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getWrittenAddress <em>Written Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Written Address</em>'.
	 * @see ch.elexis.core.model.IAddress#getWrittenAddress()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_WrittenAddress();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAddress#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see ch.elexis.core.model.IAddress#getType()
	 * @see #getIAddress()
	 * @generated
	 */
	EAttribute getIAddress_Type();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAddress#getContact <em>Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Contact</em>'.
	 * @see ch.elexis.core.model.IAddress#getContact()
	 * @see #getIAddress()
	 * @generated
	 */
	EReference getIAddress_Contact();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IImage <em>IImage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IImage</em>'.
	 * @see ch.elexis.core.model.IImage
	 * @generated
	 */
	EClass getIImage();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IImage#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IImage#getDate()
	 * @see #getIImage()
	 * @generated
	 */
	EAttribute getIImage_Date();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IImage#getPrefix <em>Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefix</em>'.
	 * @see ch.elexis.core.model.IImage#getPrefix()
	 * @see #getIImage()
	 * @generated
	 */
	EAttribute getIImage_Prefix();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IImage#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.model.IImage#getTitle()
	 * @see #getIImage()
	 * @generated
	 */
	EAttribute getIImage_Title();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IImage#getImage <em>Image</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Image</em>'.
	 * @see ch.elexis.core.model.IImage#getImage()
	 * @see #getIImage()
	 * @generated
	 */
	EAttribute getIImage_Image();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IImage#getMimeType <em>Mime Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mime Type</em>'.
	 * @see ch.elexis.core.model.IImage#getMimeType()
	 * @see #getIImage()
	 * @generated
	 */
	EAttribute getIImage_MimeType();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.WithAssignableId <em>With Assignable Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>With Assignable Id</em>'.
	 * @see ch.elexis.core.model.WithAssignableId
	 * @generated
	 */
	EClass getWithAssignableId();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPrescription <em>IPrescription</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPrescription</em>'.
	 * @see ch.elexis.core.model.IPrescription
	 * @generated
	 */
	EClass getIPrescription();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPrescription#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IPrescription#getPatient()
	 * @see #getIPrescription()
	 * @generated
	 */
	EReference getIPrescription_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPrescription#getArticle <em>Article</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Article</em>'.
	 * @see ch.elexis.core.model.IPrescription#getArticle()
	 * @see #getIPrescription()
	 * @generated
	 */
	EReference getIPrescription_Article();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getDateFrom <em>Date From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date From</em>'.
	 * @see ch.elexis.core.model.IPrescription#getDateFrom()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_DateFrom();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getDateTo <em>Date To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date To</em>'.
	 * @see ch.elexis.core.model.IPrescription#getDateTo()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_DateTo();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getDosageInstruction <em>Dosage Instruction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dosage Instruction</em>'.
	 * @see ch.elexis.core.model.IPrescription#getDosageInstruction()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_DosageInstruction();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getRemark <em>Remark</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Remark</em>'.
	 * @see ch.elexis.core.model.IPrescription#getRemark()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_Remark();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getStopReason <em>Stop Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stop Reason</em>'.
	 * @see ch.elexis.core.model.IPrescription#getStopReason()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_StopReason();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getEntryType <em>Entry Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Entry Type</em>'.
	 * @see ch.elexis.core.model.IPrescription#getEntryType()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_EntryType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#isApplied <em>Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Applied</em>'.
	 * @see ch.elexis.core.model.IPrescription#isApplied()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_Applied();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getSortOrder <em>Sort Order</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sort Order</em>'.
	 * @see ch.elexis.core.model.IPrescription#getSortOrder()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_SortOrder();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPrescription#getDisposalComment <em>Disposal Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Disposal Comment</em>'.
	 * @see ch.elexis.core.model.IPrescription#getDisposalComment()
	 * @see #getIPrescription()
	 * @generated
	 */
	EAttribute getIPrescription_DisposalComment();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPrescription#getPrescriptor <em>Prescriptor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Prescriptor</em>'.
	 * @see ch.elexis.core.model.IPrescription#getPrescriptor()
	 * @see #getIPrescription()
	 * @generated
	 */
	EReference getIPrescription_Prescriptor();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPrescription#getRecipe <em>Recipe</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Recipe</em>'.
	 * @see ch.elexis.core.model.IPrescription#getRecipe()
	 * @see #getIPrescription()
	 * @generated
	 */
	EReference getIPrescription_Recipe();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPrescription#getBilled <em>Billed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Billed</em>'.
	 * @see ch.elexis.core.model.IPrescription#getBilled()
	 * @see #getIPrescription()
	 * @generated
	 */
	EReference getIPrescription_Billed();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IRight <em>IRight</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IRight</em>'.
	 * @see ch.elexis.core.model.IRight
	 * @generated
	 */
	EClass getIRight();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRight#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.IRight#getName()
	 * @see #getIRight()
	 * @generated
	 */
	EAttribute getIRight_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRight#getLocalizedName <em>Localized Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Name</em>'.
	 * @see ch.elexis.core.model.IRight#getLocalizedName()
	 * @see #getIRight()
	 * @generated
	 */
	EAttribute getIRight_LocalizedName();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRight#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Parent</em>'.
	 * @see ch.elexis.core.model.IRight#getParent()
	 * @see #getIRight()
	 * @generated
	 */
	EReference getIRight_Parent();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBillingSystem <em>IBilling System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBilling System</em>'.
	 * @see ch.elexis.core.model.IBillingSystem
	 * @generated
	 */
	EClass getIBillingSystem();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystem#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.IBillingSystem#getName()
	 * @see #getIBillingSystem()
	 * @generated
	 */
	EAttribute getIBillingSystem_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IBillingSystem#getLaw <em>Law</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Law</em>'.
	 * @see ch.elexis.core.model.IBillingSystem#getLaw()
	 * @see #getIBillingSystem()
	 * @generated
	 */
	EAttribute getIBillingSystem_Law();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IRecipe <em>IRecipe</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IRecipe</em>'.
	 * @see ch.elexis.core.model.IRecipe
	 * @generated
	 */
	EClass getIRecipe();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRecipe#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IRecipe#getPatient()
	 * @see #getIRecipe()
	 * @generated
	 */
	EReference getIRecipe_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRecipe#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.IRecipe#getMandator()
	 * @see #getIRecipe()
	 * @generated
	 */
	EReference getIRecipe_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRecipe#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IRecipe#getDate()
	 * @see #getIRecipe()
	 * @generated
	 */
	EAttribute getIRecipe_Date();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IRecipe#getPrescriptions <em>Prescriptions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Prescriptions</em>'.
	 * @see ch.elexis.core.model.IRecipe#getPrescriptions()
	 * @see #getIRecipe()
	 * @generated
	 */
	EReference getIRecipe_Prescriptions();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRecipe#getDocument <em>Document</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Document</em>'.
	 * @see ch.elexis.core.model.IRecipe#getDocument()
	 * @see #getIRecipe()
	 * @generated
	 */
	EReference getIRecipe_Document();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IBlobSecondary <em>IBlob Secondary</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IBlob Secondary</em>'.
	 * @see ch.elexis.core.model.IBlobSecondary
	 * @generated
	 */
	EClass getIBlobSecondary();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IRelatedContact <em>IRelated Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IRelated Contact</em>'.
	 * @see ch.elexis.core.model.IRelatedContact
	 * @generated
	 */
	EClass getIRelatedContact();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRelatedContact#getMyContact <em>My Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>My Contact</em>'.
	 * @see ch.elexis.core.model.IRelatedContact#getMyContact()
	 * @see #getIRelatedContact()
	 * @generated
	 */
	EReference getIRelatedContact_MyContact();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IRelatedContact#getOtherContact <em>Other Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Other Contact</em>'.
	 * @see ch.elexis.core.model.IRelatedContact#getOtherContact()
	 * @see #getIRelatedContact()
	 * @generated
	 */
	EReference getIRelatedContact_OtherContact();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRelatedContact#getRelationshipDescription <em>Relationship Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Relationship Description</em>'.
	 * @see ch.elexis.core.model.IRelatedContact#getRelationshipDescription()
	 * @see #getIRelatedContact()
	 * @generated
	 */
	EAttribute getIRelatedContact_RelationshipDescription();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRelatedContact#getMyType <em>My Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>My Type</em>'.
	 * @see ch.elexis.core.model.IRelatedContact#getMyType()
	 * @see #getIRelatedContact()
	 * @generated
	 */
	EAttribute getIRelatedContact_MyType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IRelatedContact#getOtherType <em>Other Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Other Type</em>'.
	 * @see ch.elexis.core.model.IRelatedContact#getOtherType()
	 * @see #getIRelatedContact()
	 * @generated
	 */
	EAttribute getIRelatedContact_OtherType();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPayment <em>IPayment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPayment</em>'.
	 * @see ch.elexis.core.model.IPayment
	 * @generated
	 */
	EClass getIPayment();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPayment#getInvoice <em>Invoice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Invoice</em>'.
	 * @see ch.elexis.core.model.IPayment#getInvoice()
	 * @see #getIPayment()
	 * @generated
	 */
	EReference getIPayment_Invoice();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPayment#getAmount <em>Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Amount</em>'.
	 * @see ch.elexis.core.model.IPayment#getAmount()
	 * @see #getIPayment()
	 * @generated
	 */
	EAttribute getIPayment_Amount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPayment#getRemark <em>Remark</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Remark</em>'.
	 * @see ch.elexis.core.model.IPayment#getRemark()
	 * @see #getIPayment()
	 * @generated
	 */
	EAttribute getIPayment_Remark();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IPayment#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IPayment#getDate()
	 * @see #getIPayment()
	 * @generated
	 */
	EAttribute getIPayment_Date();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IAccountTransaction <em>IAccount Transaction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAccount Transaction</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction
	 * @generated
	 */
	EClass getIAccountTransaction();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAccountTransaction#getInvoice <em>Invoice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Invoice</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getInvoice()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EReference getIAccountTransaction_Invoice();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAccountTransaction#getPayment <em>Payment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Payment</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getPayment()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EReference getIAccountTransaction_Payment();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAccountTransaction#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getPatient()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EReference getIAccountTransaction_Patient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAccountTransaction#getAmount <em>Amount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Amount</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getAmount()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EAttribute getIAccountTransaction_Amount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAccountTransaction#getRemark <em>Remark</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Remark</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getRemark()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EAttribute getIAccountTransaction_Remark();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAccountTransaction#getAccount <em>Account</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Account</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getAccount()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EReference getIAccountTransaction_Account();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IAccount <em>IAccount</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAccount</em>'.
	 * @see ch.elexis.core.model.IAccount
	 * @generated
	 */
	EClass getIAccount();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAccount#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.IAccount#getName()
	 * @see #getIAccount()
	 * @generated
	 */
	EAttribute getIAccount_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAccount#getNumeric <em>Numeric</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Numeric</em>'.
	 * @see ch.elexis.core.model.IAccount#getNumeric()
	 * @see #getIAccount()
	 * @generated
	 */
	EAttribute getIAccount_Numeric();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAccountTransaction#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IAccountTransaction#getDate()
	 * @see #getIAccountTransaction()
	 * @generated
	 */
	EAttribute getIAccountTransaction_Date();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IMessage <em>IMessage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IMessage</em>'.
	 * @see ch.elexis.core.model.IMessage
	 * @generated
	 */
	EClass getIMessage();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#getSender <em>Sender</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sender</em>'.
	 * @see ch.elexis.core.model.IMessage#getSender()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_Sender();

	/**
	 * Returns the meta object for the attribute list '{@link ch.elexis.core.model.IMessage#getReceiver <em>Receiver</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Receiver</em>'.
	 * @see ch.elexis.core.model.IMessage#getReceiver()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_Receiver();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#isSenderAcceptsAnswer <em>Sender Accepts Answer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sender Accepts Answer</em>'.
	 * @see ch.elexis.core.model.IMessage#isSenderAcceptsAnswer()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_SenderAcceptsAnswer();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#getCreateDateTime <em>Create Date Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Create Date Time</em>'.
	 * @see ch.elexis.core.model.IMessage#getCreateDateTime()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_CreateDateTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#getMessageText <em>Message Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message Text</em>'.
	 * @see ch.elexis.core.model.IMessage#getMessageText()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_MessageText();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#getMessageCodes <em>Message Codes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message Codes</em>'.
	 * @see ch.elexis.core.model.IMessage#getMessageCodes()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_MessageCodes();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IMessage#getMessagePriority <em>Message Priority</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message Priority</em>'.
	 * @see ch.elexis.core.model.IMessage#getMessagePriority()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_MessagePriority();

	/**
	 * Returns the meta object for the attribute list '{@link ch.elexis.core.model.IMessage#getPreferredTransporters <em>Preferred Transporters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Preferred Transporters</em>'.
	 * @see ch.elexis.core.model.IMessage#getPreferredTransporters()
	 * @see #getIMessage()
	 * @generated
	 */
	EAttribute getIMessage_PreferredTransporters();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ITextTemplate <em>IText Template</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IText Template</em>'.
	 * @see ch.elexis.core.model.ITextTemplate
	 * @generated
	 */
	EClass getITextTemplate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ITextTemplate#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Category</em>'.
	 * @see ch.elexis.core.model.ITextTemplate#getCategory()
	 * @see #getITextTemplate()
	 * @generated
	 */
	EAttribute getITextTemplate_Category();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ITextTemplate#getMandator <em>Mandator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Mandator</em>'.
	 * @see ch.elexis.core.model.ITextTemplate#getMandator()
	 * @see #getITextTemplate()
	 * @generated
	 */
	EReference getITextTemplate_Mandator();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ITextTemplate#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see ch.elexis.core.model.ITextTemplate#getName()
	 * @see #getITextTemplate()
	 * @generated
	 */
	EAttribute getITextTemplate_Name();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ITextTemplate#getTemplate <em>Template</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Template</em>'.
	 * @see ch.elexis.core.model.ITextTemplate#getTemplate()
	 * @see #getITextTemplate()
	 * @generated
	 */
	EAttribute getITextTemplate_Template();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IAppointmentSeries <em>IAppointment Series</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAppointment Series</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries
	 * @generated
	 */
	EClass getIAppointmentSeries();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesType <em>Series Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series Type</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesType()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getEndingType <em>Ending Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ending Type</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getEndingType()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_EndingType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartDate <em>Series Start Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series Start Date</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesStartDate()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesStartDate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartTime <em>Series Start Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series Start Time</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesStartTime()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesStartTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndDate <em>Series End Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series End Date</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesEndDate()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesEndDate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndTime <em>Series End Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series End Time</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesEndTime()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesEndTime();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesPatternString <em>Series Pattern String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Series Pattern String</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getSeriesPatternString()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_SeriesPatternString();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#getEndingPatternString <em>Ending Pattern String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ending Pattern String</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getEndingPatternString()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_EndingPatternString();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointmentSeries#isPersistent <em>Persistent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Persistent</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#isPersistent()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EAttribute getIAppointmentSeries_Persistent();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IAppointmentSeries#getRootAppointment <em>Root Appointment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Root Appointment</em>'.
	 * @see ch.elexis.core.model.IAppointmentSeries#getRootAppointment()
	 * @see #getIAppointmentSeries()
	 * @generated
	 */
	EReference getIAppointmentSeries_RootAppointment();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.ISickCertificate <em>ISick Certificate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ISick Certificate</em>'.
	 * @see ch.elexis.core.model.ISickCertificate
	 * @generated
	 */
	EClass getISickCertificate();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ISickCertificate#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getPatient()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EReference getISickCertificate_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ISickCertificate#getCoverage <em>Coverage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Coverage</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getCoverage()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EReference getISickCertificate_Coverage();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ISickCertificate#getLetter <em>Letter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Letter</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getLetter()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EReference getISickCertificate_Letter();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getPercent <em>Percent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Percent</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getPercent()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_Percent();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getDate()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_Date();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getStart <em>Start</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getStart()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_Start();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getEnd <em>End</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>End</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getEnd()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_End();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getReason <em>Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reason</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getReason()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_Reason();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ISickCertificate#getNote <em>Note</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Note</em>'.
	 * @see ch.elexis.core.model.ISickCertificate#getNote()
	 * @see #getISickCertificate()
	 * @generated
	 */
	EAttribute getISickCertificate_Note();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IDayMessage <em>IDay Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IDay Message</em>'.
	 * @see ch.elexis.core.model.IDayMessage
	 * @generated
	 */
	EClass getIDayMessage();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDayMessage#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.model.IDayMessage#getTitle()
	 * @see #getIDayMessage()
	 * @generated
	 */
	EAttribute getIDayMessage_Title();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDayMessage#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see ch.elexis.core.model.IDayMessage#getMessage()
	 * @see #getIDayMessage()
	 * @generated
	 */
	EAttribute getIDayMessage_Message();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDayMessage#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see ch.elexis.core.model.IDayMessage#getDate()
	 * @see #getIDayMessage()
	 * @generated
	 */
	EAttribute getIDayMessage_Date();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IReminder <em>IReminder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IReminder</em>'.
	 * @see ch.elexis.core.model.IReminder
	 * @generated
	 */
	EClass getIReminder();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IReminder#getCreator <em>Creator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Creator</em>'.
	 * @see ch.elexis.core.model.IReminder#getCreator()
	 * @see #getIReminder()
	 * @generated
	 */
	EReference getIReminder_Creator();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IReminder#getResponsible <em>Responsible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Responsible</em>'.
	 * @see ch.elexis.core.model.IReminder#getResponsible()
	 * @see #getIReminder()
	 * @generated
	 */
	EReference getIReminder_Responsible();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IReminder#getContact <em>Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Contact</em>'.
	 * @see ch.elexis.core.model.IReminder#getContact()
	 * @see #getIReminder()
	 * @generated
	 */
	EReference getIReminder_Contact();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getDue <em>Due</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Due</em>'.
	 * @see ch.elexis.core.model.IReminder#getDue()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Due();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see ch.elexis.core.model.IReminder#getStatus()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Status();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getVisibility <em>Visibility</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visibility</em>'.
	 * @see ch.elexis.core.model.IReminder#getVisibility()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Visibility();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getSubject <em>Subject</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Subject</em>'.
	 * @see ch.elexis.core.model.IReminder#getSubject()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Subject();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see ch.elexis.core.model.IReminder#getMessage()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Message();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getPriority <em>Priority</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Priority</em>'.
	 * @see ch.elexis.core.model.IReminder#getPriority()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Priority();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see ch.elexis.core.model.IReminder#getType()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_Type();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IReminder#isResponsibleAll <em>Responsible All</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Responsible All</em>'.
	 * @see ch.elexis.core.model.IReminder#isResponsibleAll()
	 * @see #getIReminder()
	 * @generated
	 */
	EAttribute getIReminder_ResponsibleAll();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IReminderResponsibleLink <em>IReminder Responsible Link</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IReminder Responsible Link</em>'.
	 * @see ch.elexis.core.model.IReminderResponsibleLink
	 * @generated
	 */
	EClass getIReminderResponsibleLink();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IReminderResponsibleLink#getReminder <em>Reminder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Reminder</em>'.
	 * @see ch.elexis.core.model.IReminderResponsibleLink#getReminder()
	 * @see #getIReminderResponsibleLink()
	 * @generated
	 */
	EReference getIReminderResponsibleLink_Reminder();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IReminderResponsibleLink#getResponsible <em>Responsible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Responsible</em>'.
	 * @see ch.elexis.core.model.IReminderResponsibleLink#getResponsible()
	 * @see #getIReminderResponsibleLink()
	 * @generated
	 */
	EReference getIReminderResponsibleLink_Responsible();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IVaccination <em>IVaccination</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IVaccination</em>'.
	 * @see ch.elexis.core.model.IVaccination
	 * @generated
	 */
	EClass getIVaccination();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IVaccination#getPatient <em>Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient</em>'.
	 * @see ch.elexis.core.model.IVaccination#getPatient()
	 * @see #getIVaccination()
	 * @generated
	 */
	EReference getIVaccination_Patient();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IVaccination#getArticle <em>Article</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Article</em>'.
	 * @see ch.elexis.core.model.IVaccination#getArticle()
	 * @see #getIVaccination()
	 * @generated
	 */
	EReference getIVaccination_Article();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getArticleName <em>Article Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Article Name</em>'.
	 * @see ch.elexis.core.model.IVaccination#getArticleName()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_ArticleName();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getArticleGtin <em>Article Gtin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Article Gtin</em>'.
	 * @see ch.elexis.core.model.IVaccination#getArticleGtin()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_ArticleGtin();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getArticleAtc <em>Article Atc</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Article Atc</em>'.
	 * @see ch.elexis.core.model.IVaccination#getArticleAtc()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_ArticleAtc();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getLotNumber <em>Lot Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lot Number</em>'.
	 * @see ch.elexis.core.model.IVaccination#getLotNumber()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_LotNumber();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getDateOfAdministration <em>Date Of Administration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date Of Administration</em>'.
	 * @see ch.elexis.core.model.IVaccination#getDateOfAdministration()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_DateOfAdministration();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IVaccination#getIngredientsAtc <em>Ingredients Atc</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ingredients Atc</em>'.
	 * @see ch.elexis.core.model.IVaccination#getIngredientsAtc()
	 * @see #getIVaccination()
	 * @generated
	 */
	EAttribute getIVaccination_IngredientsAtc();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IVaccination#getPerformer <em>Performer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Performer</em>'.
	 * @see ch.elexis.core.model.IVaccination#getPerformer()
	 * @see #getIVaccination()
	 * @generated
	 */
	EReference getIVaccination_Performer();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.model.IAppointment <em>IAppointment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IAppointment</em>'.
	 * @see ch.elexis.core.model.IAppointment
	 * @generated
	 */
	EClass getIAppointment();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getReason <em>Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reason</em>'.
	 * @see ch.elexis.core.model.IAppointment#getReason()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Reason();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State</em>'.
	 * @see ch.elexis.core.model.IAppointment#getState()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_State();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see ch.elexis.core.model.IAppointment#getType()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Type();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getDurationMinutes <em>Duration Minutes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Duration Minutes</em>'.
	 * @see ch.elexis.core.model.IAppointment#getDurationMinutes()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_DurationMinutes();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getSchedule <em>Schedule</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Schedule</em>'.
	 * @see ch.elexis.core.model.IAppointment#getSchedule()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Schedule();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getCreatedBy <em>Created By</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Created By</em>'.
	 * @see ch.elexis.core.model.IAppointment#getCreatedBy()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_CreatedBy();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getSubjectOrPatient <em>Subject Or Patient</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Subject Or Patient</em>'.
	 * @see ch.elexis.core.model.IAppointment#getSubjectOrPatient()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_SubjectOrPatient();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getPriority <em>Priority</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Priority</em>'.
	 * @see ch.elexis.core.model.IAppointment#getPriority()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Priority();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getTreatmentReason <em>Treatment Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Treatment Reason</em>'.
	 * @see ch.elexis.core.model.IAppointment#getTreatmentReason()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_TreatmentReason();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getCaseType <em>Case Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Case Type</em>'.
	 * @see ch.elexis.core.model.IAppointment#getCaseType()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_CaseType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getInsuranceType <em>Insurance Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Insurance Type</em>'.
	 * @see ch.elexis.core.model.IAppointment#getInsuranceType()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_InsuranceType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getLinkgroup <em>Linkgroup</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Linkgroup</em>'.
	 * @see ch.elexis.core.model.IAppointment#getLinkgroup()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Linkgroup();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getExtension <em>Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Extension</em>'.
	 * @see ch.elexis.core.model.IAppointment#getExtension()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Extension();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getCreated <em>Created</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Created</em>'.
	 * @see ch.elexis.core.model.IAppointment#getCreated()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Created();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getLastEdit <em>Last Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Edit</em>'.
	 * @see ch.elexis.core.model.IAppointment#getLastEdit()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_LastEdit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#getStateHistory <em>State History</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State History</em>'.
	 * @see ch.elexis.core.model.IAppointment#getStateHistory()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_StateHistory();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IAppointment#isRecurring <em>Recurring</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Recurring</em>'.
	 * @see ch.elexis.core.model.IAppointment#isRecurring()
	 * @see #getIAppointment()
	 * @generated
	 */
	EAttribute getIAppointment_Recurring();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.Identifiable <em>Identifiable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.Identifiable
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIdentifiable()
		 * @generated
		 */
		EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

		/**
		 * The meta object literal for the '<em><b>Lastupdate</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__LASTUPDATE = eINSTANCE.getIdentifiable_Lastupdate();

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
		 * The meta object literal for the '<em><b>Organization</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__ORGANIZATION = eINSTANCE.getIContact_Organization();

		/**
		 * The meta object literal for the '<em><b>Laboratory</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__LABORATORY = eINSTANCE.getIContact_Laboratory();

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
		 * The meta object literal for the '<em><b>Address</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICONTACT__ADDRESS = eINSTANCE.getIContact_Address();

		/**
		 * The meta object literal for the '<em><b>Group</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__GROUP = eINSTANCE.getIContact_Group();

		/**
		 * The meta object literal for the '<em><b>Postal Address</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__POSTAL_ADDRESS = eINSTANCE.getIContact_PostalAddress();

		/**
		 * The meta object literal for the '<em><b>Image</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICONTACT__IMAGE = eINSTANCE.getIContact_Image();

		/**
		 * The meta object literal for the '<em><b>Related Contacts</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICONTACT__RELATED_CONTACTS = eINSTANCE.getIContact_RelatedContacts();

		/**
		 * The meta object literal for the '<em><b>Deceased</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__DECEASED = eINSTANCE.getIContact_Deceased();

		/**
		 * The meta object literal for the '<em><b>Email2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__EMAIL2 = eINSTANCE.getIContact_Email2();

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
		 * The meta object literal for the '<em><b>Marital Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__MARITAL_STATUS = eINSTANCE.getIPerson_MaritalStatus();

		/**
		 * The meta object literal for the '<em><b>Legal Guardian</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPERSON__LEGAL_GUARDIAN = eINSTANCE.getIPerson_LegalGuardian();

		/**
		 * The meta object literal for the '<em><b>Date Of Death</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPERSON__DATE_OF_DEATH = eINSTANCE.getIPerson_DateOfDeath();

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
		 * The meta object literal for the '<em><b>Insurance Xml Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORGANIZATION__INSURANCE_XML_NAME = eINSTANCE.getIOrganization_InsuranceXmlName();

		/**
		 * The meta object literal for the '<em><b>Insurance Law Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORGANIZATION__INSURANCE_LAW_CODE = eINSTANCE.getIOrganization_InsuranceLawCode();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPatient
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPatient()
		 * @generated
		 */
		EClass IPATIENT = eINSTANCE.getIPatient();

		/**
		 * The meta object literal for the '<em><b>Family Doctor</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPATIENT__FAMILY_DOCTOR = eINSTANCE.getIPatient_FamilyDoctor();

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
		 * The meta object literal for the '<em><b>Coverages</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPATIENT__COVERAGES = eINSTANCE.getIPatient_Coverages();

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
		 * The meta object literal for the '<em><b>Biller</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IMANDATOR__BILLER = eINSTANCE.getIMandator_Biller();

		/**
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMANDATOR__ACTIVE = eINSTANCE.getIMandator_Active();

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
		 * The meta object literal for the '<em><b>Hashed Password</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__HASHED_PASSWORD = eINSTANCE.getIUser_HashedPassword();

		/**
		 * The meta object literal for the '<em><b>Salt</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__SALT = eINSTANCE.getIUser_Salt();

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
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__ACTIVE = eINSTANCE.getIUser_Active();

		/**
		 * The meta object literal for the '<em><b>Allow External</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__ALLOW_EXTERNAL = eINSTANCE.getIUser_AllowExternal();

		/**
		 * The meta object literal for the '<em><b>Administrator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER__ADMINISTRATOR = eINSTANCE.getIUser_Administrator();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IUserGroup <em>IUser Group</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IUserGroup
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUserGroup()
		 * @generated
		 */
		EClass IUSER_GROUP = eINSTANCE.getIUserGroup();

		/**
		 * The meta object literal for the '<em><b>Users</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IUSER_GROUP__USERS = eINSTANCE.getIUserGroup_Users();

		/**
		 * The meta object literal for the '<em><b>Roles</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IUSER_GROUP__ROLES = eINSTANCE.getIUserGroup_Roles();

		/**
		 * The meta object literal for the '<em><b>Groupname</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IUSER_GROUP__GROUPNAME = eINSTANCE.getIUserGroup_Groupname();

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
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__UNIT = eINSTANCE.getILabItem_Unit();

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
		 * The meta object literal for the '<em><b>Export</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__EXPORT = eINSTANCE.getILabItem_Export();

		/**
		 * The meta object literal for the '<em><b>Mappings</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ITEM__MAPPINGS = eINSTANCE.getILabItem_Mappings();

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
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__UNIT = eINSTANCE.getILabResult_Unit();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__DATE = eINSTANCE.getILabResult_Date();

		/**
		 * The meta object literal for the '<em><b>Observation Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__OBSERVATION_TIME = eINSTANCE.getILabResult_ObservationTime();

		/**
		 * The meta object literal for the '<em><b>Analyse Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__ANALYSE_TIME = eINSTANCE.getILabResult_AnalyseTime();

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
		 * The meta object literal for the '<em><b>Group Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__GROUP_NAME = eINSTANCE.getILabOrder_GroupName();

		/**
		 * The meta object literal for the '<em><b>User Resolved</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ORDER__USER_RESOLVED = eINSTANCE.getILabOrder_UserResolved();

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
		 * The meta object literal for the '<em><b>Status</b></em>' attribute list feature.
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
		 * The meta object literal for the '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDocumentLetter
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentLetter()
		 * @generated
		 */
		EClass IDOCUMENT_LETTER = eINSTANCE.getIDocumentLetter();

		/**
		 * The meta object literal for the '<em><b>Encounter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT_LETTER__ENCOUNTER = eINSTANCE.getIDocumentLetter_Encounter();

		/**
		 * The meta object literal for the '<em><b>Recipient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT_LETTER__RECIPIENT = eINSTANCE.getIDocumentLetter_Recipient();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDocumentTemplate <em>IDocument Template</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDocumentTemplate
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDocumentTemplate()
		 * @generated
		 */
		EClass IDOCUMENT_TEMPLATE = eINSTANCE.getIDocumentTemplate();

		/**
		 * The meta object literal for the '<em><b>Template Typ</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT_TEMPLATE__TEMPLATE_TYP = eINSTANCE.getIDocumentTemplate_TemplateTyp();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDOCUMENT_TEMPLATE__MANDATOR = eINSTANCE.getIDocumentTemplate_Mandator();

		/**
		 * The meta object literal for the '<em><b>Ask For Addressee</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT_TEMPLATE__ASK_FOR_ADDRESSEE = eINSTANCE.getIDocumentTemplate_AskForAddressee();

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
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__NAME = eINSTANCE.getISticker_Name();

		/**
		 * The meta object literal for the '<em><b>Importance</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__IMPORTANCE = eINSTANCE.getISticker_Importance();

		/**
		 * The meta object literal for the '<em><b>Image</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTICKER__IMAGE = eINSTANCE.getISticker_Image();

		/**
		 * The meta object literal for the '<em><b>Attached To</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTICKER__ATTACHED_TO = eINSTANCE.getISticker_AttachedTo();

		/**
		 * The meta object literal for the '<em><b>Attached To Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTICKER__ATTACHED_TO_DATA = eINSTANCE.getISticker_AttachedToData();

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
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICODE_ELEMENT__CODE = eINSTANCE.getICodeElement_Code();

		/**
		 * The meta object literal for the '<em><b>Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICODE_ELEMENT__TEXT = eINSTANCE.getICodeElement_Text();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICodeElementBlock <em>ICode Element Block</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICodeElementBlock
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElementBlock()
		 * @generated
		 */
		EClass ICODE_ELEMENT_BLOCK = eINSTANCE.getICodeElementBlock();

		/**
		 * The meta object literal for the '<em><b>Elements</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICODE_ELEMENT_BLOCK__ELEMENTS = eINSTANCE.getICodeElementBlock_Elements();

		/**
		 * The meta object literal for the '<em><b>Element References</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICODE_ELEMENT_BLOCK__ELEMENT_REFERENCES = eINSTANCE.getICodeElementBlock_ElementReferences();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICODE_ELEMENT_BLOCK__MANDATOR = eINSTANCE.getICodeElementBlock_Mandator();

		/**
		 * The meta object literal for the '<em><b>Macro</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICODE_ELEMENT_BLOCK__MACRO = eINSTANCE.getICodeElementBlock_Macro();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.IBillableVerifier <em>IBillable Verifier</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBillableVerifier
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillableVerifier()
		 * @generated
		 */
		EClass IBILLABLE_VERIFIER = eINSTANCE.getIBillableVerifier();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBillableOptifier <em>IBillable Optifier</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBillableOptifier
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillableOptifier()
		 * @generated
		 */
		EClass IBILLABLE_OPTIFIER = eINSTANCE.getIBillableOptifier();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IService <em>IService</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IService
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIService()
		 * @generated
		 */
		EClass ISERVICE = eINSTANCE.getIService();

		/**
		 * The meta object literal for the '<em><b>Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISERVICE__PRICE = eINSTANCE.getIService_Price();

		/**
		 * The meta object literal for the '<em><b>Net Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISERVICE__NET_PRICE = eINSTANCE.getIService_NetPrice();

		/**
		 * The meta object literal for the '<em><b>Minutes</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISERVICE__MINUTES = eINSTANCE.getIService_Minutes();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICustomService <em>ICustom Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICustomService
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICustomService()
		 * @generated
		 */
		EClass ICUSTOM_SERVICE = eINSTANCE.getICustomService();

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
		 * The meta object literal for the '<em><b>Atc Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__ATC_CODE = eINSTANCE.getIArticle_AtcCode();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__NAME = eINSTANCE.getIArticle_Name();

		/**
		 * The meta object literal for the '<em><b>Selling Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__SELLING_SIZE = eINSTANCE.getIArticle_SellingSize();

		/**
		 * The meta object literal for the '<em><b>Package Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PACKAGE_SIZE = eINSTANCE.getIArticle_PackageSize();

		/**
		 * The meta object literal for the '<em><b>Package Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PACKAGE_UNIT = eINSTANCE.getIArticle_PackageUnit();

		/**
		 * The meta object literal for the '<em><b>Product</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IARTICLE__PRODUCT = eINSTANCE.getIArticle_Product();

		/**
		 * The meta object literal for the '<em><b>Purchase Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PURCHASE_PRICE = eINSTANCE.getIArticle_PurchasePrice();

		/**
		 * The meta object literal for the '<em><b>Selling Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__SELLING_PRICE = eINSTANCE.getIArticle_SellingPrice();

		/**
		 * The meta object literal for the '<em><b>Obligation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__OBLIGATION = eINSTANCE.getIArticle_Obligation();

		/**
		 * The meta object literal for the '<em><b>Typ</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__TYP = eINSTANCE.getIArticle_Typ();

		/**
		 * The meta object literal for the '<em><b>Sub Typ</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__SUB_TYP = eINSTANCE.getIArticle_SubTyp();

		/**
		 * The meta object literal for the '<em><b>Package Size String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE__PACKAGE_SIZE_STRING = eINSTANCE.getIArticle_PackageSizeString();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IArticleDefaultSignature <em>IArticle Default Signature</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IArticleDefaultSignature
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIArticleDefaultSignature()
		 * @generated
		 */
		EClass IARTICLE_DEFAULT_SIGNATURE = eINSTANCE.getIArticleDefaultSignature();

		/**
		 * The meta object literal for the '<em><b>Atc Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__ATC_CODE = eINSTANCE.getIArticleDefaultSignature_AtcCode();

		/**
		 * The meta object literal for the '<em><b>Morning</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__MORNING = eINSTANCE.getIArticleDefaultSignature_Morning();

		/**
		 * The meta object literal for the '<em><b>Noon</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__NOON = eINSTANCE.getIArticleDefaultSignature_Noon();

		/**
		 * The meta object literal for the '<em><b>Evening</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__EVENING = eINSTANCE.getIArticleDefaultSignature_Evening();

		/**
		 * The meta object literal for the '<em><b>Night</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__NIGHT = eINSTANCE.getIArticleDefaultSignature_Night();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__COMMENT = eINSTANCE.getIArticleDefaultSignature_Comment();

		/**
		 * The meta object literal for the '<em><b>Free Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__FREE_TEXT = eINSTANCE.getIArticleDefaultSignature_FreeText();

		/**
		 * The meta object literal for the '<em><b>Medication Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__MEDICATION_TYPE = eINSTANCE.getIArticleDefaultSignature_MedicationType();

		/**
		 * The meta object literal for the '<em><b>Disposal Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__DISPOSAL_TYPE = eINSTANCE.getIArticleDefaultSignature_DisposalType();

		/**
		 * The meta object literal for the '<em><b>End Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__END_DATE = eINSTANCE.getIArticleDefaultSignature_EndDate();

		/**
		 * The meta object literal for the '<em><b>Start Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IARTICLE_DEFAULT_SIGNATURE__START_DATE = eINSTANCE.getIArticleDefaultSignature_StartDate();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDiagnosis <em>IDiagnosis</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDiagnosis
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosis()
		 * @generated
		 */
		EClass IDIAGNOSIS = eINSTANCE.getIDiagnosis();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDIAGNOSIS__DESCRIPTION = eINSTANCE.getIDiagnosis_Description();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IFreeTextDiagnosis <em>IFree Text Diagnosis</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IFreeTextDiagnosis
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIFreeTextDiagnosis()
		 * @generated
		 */
		EClass IFREE_TEXT_DIAGNOSIS = eINSTANCE.getIFreeTextDiagnosis();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDiagnosisReference <em>IDiagnosis Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDiagnosisReference
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosisReference()
		 * @generated
		 */
		EClass IDIAGNOSIS_REFERENCE = eINSTANCE.getIDiagnosisReference();

		/**
		 * The meta object literal for the '<em><b>Referred Class</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDIAGNOSIS_REFERENCE__REFERRED_CLASS = eINSTANCE.getIDiagnosisReference_ReferredClass();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDiagnosisTree <em>IDiagnosis Tree</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDiagnosisTree
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDiagnosisTree()
		 * @generated
		 */
		EClass IDIAGNOSIS_TREE = eINSTANCE.getIDiagnosisTree();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDIAGNOSIS_TREE__PARENT = eINSTANCE.getIDiagnosisTree_Parent();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IDIAGNOSIS_TREE__CHILDREN = eINSTANCE.getIDiagnosisTree_Children();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ICustomDiagnosis <em>ICustom Diagnosis</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ICustomDiagnosis
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICustomDiagnosis()
		 * @generated
		 */
		EClass ICUSTOM_DIAGNOSIS = eINSTANCE.getICustomDiagnosis();

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
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICOVERAGE__PATIENT = eINSTANCE.getICoverage_Patient();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__DESCRIPTION = eINSTANCE.getICoverage_Description();

		/**
		 * The meta object literal for the '<em><b>Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__REASON = eINSTANCE.getICoverage_Reason();

		/**
		 * The meta object literal for the '<em><b>Date From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__DATE_FROM = eINSTANCE.getICoverage_DateFrom();

		/**
		 * The meta object literal for the '<em><b>Billing System</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICOVERAGE__BILLING_SYSTEM = eINSTANCE.getICoverage_BillingSystem();

		/**
		 * The meta object literal for the '<em><b>Guarantor</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICOVERAGE__GUARANTOR = eINSTANCE.getICoverage_Guarantor();

		/**
		 * The meta object literal for the '<em><b>Cost Bearer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICOVERAGE__COST_BEARER = eINSTANCE.getICoverage_CostBearer();

		/**
		 * The meta object literal for the '<em><b>Insurance Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__INSURANCE_NUMBER = eINSTANCE.getICoverage_InsuranceNumber();

		/**
		 * The meta object literal for the '<em><b>Date To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__DATE_TO = eINSTANCE.getICoverage_DateTo();

		/**
		 * The meta object literal for the '<em><b>Billing Proposal Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICOVERAGE__BILLING_PROPOSAL_DATE = eINSTANCE.getICoverage_BillingProposalDate();

		/**
		 * The meta object literal for the '<em><b>Encounters</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ICOVERAGE__ENCOUNTERS = eINSTANCE.getICoverage_Encounters();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBillingSystemFactor <em>IBilling System Factor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBillingSystemFactor
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillingSystemFactor()
		 * @generated
		 */
		EClass IBILLING_SYSTEM_FACTOR = eINSTANCE.getIBillingSystemFactor();

		/**
		 * The meta object literal for the '<em><b>System</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM_FACTOR__SYSTEM = eINSTANCE.getIBillingSystemFactor_System();

		/**
		 * The meta object literal for the '<em><b>Factor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM_FACTOR__FACTOR = eINSTANCE.getIBillingSystemFactor_Factor();

		/**
		 * The meta object literal for the '<em><b>Valid From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM_FACTOR__VALID_FROM = eINSTANCE.getIBillingSystemFactor_ValidFrom();

		/**
		 * The meta object literal for the '<em><b>Valid To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM_FACTOR__VALID_TO = eINSTANCE.getIBillingSystemFactor_ValidTo();

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
		 * The meta object literal for the '<em><b>System Role</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IROLE__SYSTEM_ROLE = eINSTANCE.getIRole_SystemRole();

		/**
		 * The meta object literal for the '<em><b>Assigned Rights</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IROLE__ASSIGNED_RIGHTS = eINSTANCE.getIRole_AssignedRights();

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

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IEncounter <em>IEncounter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IEncounter
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIEncounter()
		 * @generated
		 */
		EClass IENCOUNTER = eINSTANCE.getIEncounter();

		/**
		 * The meta object literal for the '<em><b>Time Stamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IENCOUNTER__TIME_STAMP = eINSTANCE.getIEncounter_TimeStamp();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__PATIENT = eINSTANCE.getIEncounter_Patient();

		/**
		 * The meta object literal for the '<em><b>Coverage</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__COVERAGE = eINSTANCE.getIEncounter_Coverage();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__MANDATOR = eINSTANCE.getIEncounter_Mandator();

		/**
		 * The meta object literal for the '<em><b>Billed</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__BILLED = eINSTANCE.getIEncounter_Billed();

		/**
		 * The meta object literal for the '<em><b>Diagnoses</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__DIAGNOSES = eINSTANCE.getIEncounter_Diagnoses();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IENCOUNTER__DATE = eINSTANCE.getIEncounter_Date();

		/**
		 * The meta object literal for the '<em><b>Billable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IENCOUNTER__BILLABLE = eINSTANCE.getIEncounter_Billable();

		/**
		 * The meta object literal for the '<em><b>Versioned Entry</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IENCOUNTER__VERSIONED_ENTRY = eINSTANCE.getIEncounter_VersionedEntry();

		/**
		 * The meta object literal for the '<em><b>Invoice</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IENCOUNTER__INVOICE = eINSTANCE.getIEncounter_Invoice();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBilled <em>IBilled</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBilled
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBilled()
		 * @generated
		 */
		EClass IBILLED = eINSTANCE.getIBilled();

		/**
		 * The meta object literal for the '<em><b>Billable</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IBILLED__BILLABLE = eINSTANCE.getIBilled_Billable();

		/**
		 * The meta object literal for the '<em><b>Encounter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IBILLED__ENCOUNTER = eINSTANCE.getIBilled_Encounter();

		/**
		 * The meta object literal for the '<em><b>Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__AMOUNT = eINSTANCE.getIBilled_Amount();

		/**
		 * The meta object literal for the '<em><b>Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__PRICE = eINSTANCE.getIBilled_Price();

		/**
		 * The meta object literal for the '<em><b>Scaled Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__SCALED_PRICE = eINSTANCE.getIBilled_ScaledPrice();

		/**
		 * The meta object literal for the '<em><b>Net Price</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__NET_PRICE = eINSTANCE.getIBilled_NetPrice();

		/**
		 * The meta object literal for the '<em><b>Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__TEXT = eINSTANCE.getIBilled_Text();

		/**
		 * The meta object literal for the '<em><b>Points</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__POINTS = eINSTANCE.getIBilled_Points();

		/**
		 * The meta object literal for the '<em><b>Factor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__FACTOR = eINSTANCE.getIBilled_Factor();

		/**
		 * The meta object literal for the '<em><b>Primary Scale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__PRIMARY_SCALE = eINSTANCE.getIBilled_PrimaryScale();

		/**
		 * The meta object literal for the '<em><b>Secondary Scale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__SECONDARY_SCALE = eINSTANCE.getIBilled_SecondaryScale();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__CODE = eINSTANCE.getIBilled_Code();

		/**
		 * The meta object literal for the '<em><b>Total</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLED__TOTAL = eINSTANCE.getIBilled_Total();

		/**
		 * The meta object literal for the '<em><b>Biller</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IBILLED__BILLER = eINSTANCE.getIBilled_Biller();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IInvoiceBilled <em>IInvoice Billed</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IInvoiceBilled
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIInvoiceBilled()
		 * @generated
		 */
		EClass IINVOICE_BILLED = eINSTANCE.getIInvoiceBilled();

		/**
		 * The meta object literal for the '<em><b>Invoice</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE_BILLED__INVOICE = eINSTANCE.getIInvoiceBilled_Invoice();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IInvoice <em>IInvoice</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IInvoice
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIInvoice()
		 * @generated
		 */
		EClass IINVOICE = eINSTANCE.getIInvoice();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__STATE = eINSTANCE.getIInvoice_State();

		/**
		 * The meta object literal for the '<em><b>Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__NUMBER = eINSTANCE.getIInvoice_Number();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__MANDATOR = eINSTANCE.getIInvoice_Mandator();

		/**
		 * The meta object literal for the '<em><b>Coverage</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__COVERAGE = eINSTANCE.getIInvoice_Coverage();

		/**
		 * The meta object literal for the '<em><b>Encounters</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__ENCOUNTERS = eINSTANCE.getIInvoice_Encounters();

		/**
		 * The meta object literal for the '<em><b>Billed</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__BILLED = eINSTANCE.getIInvoice_Billed();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__DATE = eINSTANCE.getIInvoice_Date();

		/**
		 * The meta object literal for the '<em><b>Date From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__DATE_FROM = eINSTANCE.getIInvoice_DateFrom();

		/**
		 * The meta object literal for the '<em><b>Date To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__DATE_TO = eINSTANCE.getIInvoice_DateTo();

		/**
		 * The meta object literal for the '<em><b>Total Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__TOTAL_AMOUNT = eINSTANCE.getIInvoice_TotalAmount();

		/**
		 * The meta object literal for the '<em><b>Open Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__OPEN_AMOUNT = eINSTANCE.getIInvoice_OpenAmount();

		/**
		 * The meta object literal for the '<em><b>Payed Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__PAYED_AMOUNT = eINSTANCE.getIInvoice_PayedAmount();

		/**
		 * The meta object literal for the '<em><b>Demand Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__DEMAND_AMOUNT = eINSTANCE.getIInvoice_DemandAmount();

		/**
		 * The meta object literal for the '<em><b>Remark</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__REMARK = eINSTANCE.getIInvoice_Remark();

		/**
		 * The meta object literal for the '<em><b>Payments</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__PAYMENTS = eINSTANCE.getIInvoice_Payments();

		/**
		 * The meta object literal for the '<em><b>Transactions</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__TRANSACTIONS = eINSTANCE.getIInvoice_Transactions();

		/**
		 * The meta object literal for the '<em><b>Attachments</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IINVOICE__ATTACHMENTS = eINSTANCE.getIInvoice_Attachments();

		/**
		 * The meta object literal for the '<em><b>State Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IINVOICE__STATE_DATE = eINSTANCE.getIInvoice_StateDate();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IStock <em>IStock</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IStock
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIStock()
		 * @generated
		 */
		EClass ISTOCK = eINSTANCE.getIStock();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK__CODE = eINSTANCE.getIStock_Code();

		/**
		 * The meta object literal for the '<em><b>Driver Uuid</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK__DRIVER_UUID = eINSTANCE.getIStock_DriverUuid();

		/**
		 * The meta object literal for the '<em><b>Driver Config</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK__DRIVER_CONFIG = eINSTANCE.getIStock_DriverConfig();

		/**
		 * The meta object literal for the '<em><b>Priority</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK__PRIORITY = eINSTANCE.getIStock_Priority();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK__OWNER = eINSTANCE.getIStock_Owner();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK__LOCATION = eINSTANCE.getIStock_Location();

		/**
		 * The meta object literal for the '<em><b>Responsible</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK__RESPONSIBLE = eINSTANCE.getIStock_Responsible();

		/**
		 * The meta object literal for the '<em><b>Stock Entries</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK__STOCK_ENTRIES = eINSTANCE.getIStock_StockEntries();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IStockEntry <em>IStock Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IStockEntry
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIStockEntry()
		 * @generated
		 */
		EClass ISTOCK_ENTRY = eINSTANCE.getIStockEntry();

		/**
		 * The meta object literal for the '<em><b>Minimum Stock</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK_ENTRY__MINIMUM_STOCK = eINSTANCE.getIStockEntry_MinimumStock();

		/**
		 * The meta object literal for the '<em><b>Current Stock</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK_ENTRY__CURRENT_STOCK = eINSTANCE.getIStockEntry_CurrentStock();

		/**
		 * The meta object literal for the '<em><b>Maximum Stock</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK_ENTRY__MAXIMUM_STOCK = eINSTANCE.getIStockEntry_MaximumStock();

		/**
		 * The meta object literal for the '<em><b>Fraction Units</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTOCK_ENTRY__FRACTION_UNITS = eINSTANCE.getIStockEntry_FractionUnits();

		/**
		 * The meta object literal for the '<em><b>Stock</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK_ENTRY__STOCK = eINSTANCE.getIStockEntry_Stock();

		/**
		 * The meta object literal for the '<em><b>Article</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK_ENTRY__ARTICLE = eINSTANCE.getIStockEntry_Article();

		/**
		 * The meta object literal for the '<em><b>Provider</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISTOCK_ENTRY__PROVIDER = eINSTANCE.getIStockEntry_Provider();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IOrderEntry <em>IOrder Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IOrderEntry
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrderEntry()
		 * @generated
		 */
		EClass IORDER_ENTRY = eINSTANCE.getIOrderEntry();

		/**
		 * The meta object literal for the '<em><b>Order</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IORDER_ENTRY__ORDER = eINSTANCE.getIOrderEntry_Order();

		/**
		 * The meta object literal for the '<em><b>Stock</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IORDER_ENTRY__STOCK = eINSTANCE.getIOrderEntry_Stock();

		/**
		 * The meta object literal for the '<em><b>Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORDER_ENTRY__AMOUNT = eINSTANCE.getIOrderEntry_Amount();

		/**
		 * The meta object literal for the '<em><b>Article</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IORDER_ENTRY__ARTICLE = eINSTANCE.getIOrderEntry_Article();

		/**
		 * The meta object literal for the '<em><b>Provider</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IORDER_ENTRY__PROVIDER = eINSTANCE.getIOrderEntry_Provider();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORDER_ENTRY__STATE = eINSTANCE.getIOrderEntry_State();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IOrder <em>IOrder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IOrder
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIOrder()
		 * @generated
		 */
		EClass IORDER = eINSTANCE.getIOrder();

		/**
		 * The meta object literal for the '<em><b>Entries</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IORDER__ENTRIES = eINSTANCE.getIOrder_Entries();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORDER__TIMESTAMP = eINSTANCE.getIOrder_Timestamp();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IORDER__NAME = eINSTANCE.getIOrder_Name();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IAddress <em>IAddress</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IAddress
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAddress()
		 * @generated
		 */
		EClass IADDRESS = eINSTANCE.getIAddress();

		/**
		 * The meta object literal for the '<em><b>Street1</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__STREET1 = eINSTANCE.getIAddress_Street1();

		/**
		 * The meta object literal for the '<em><b>Street2</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__STREET2 = eINSTANCE.getIAddress_Street2();

		/**
		 * The meta object literal for the '<em><b>Zip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__ZIP = eINSTANCE.getIAddress_Zip();

		/**
		 * The meta object literal for the '<em><b>City</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__CITY = eINSTANCE.getIAddress_City();

		/**
		 * The meta object literal for the '<em><b>Country</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__COUNTRY = eINSTANCE.getIAddress_Country();

		/**
		 * The meta object literal for the '<em><b>Written Address</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__WRITTEN_ADDRESS = eINSTANCE.getIAddress_WrittenAddress();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IADDRESS__TYPE = eINSTANCE.getIAddress_Type();

		/**
		 * The meta object literal for the '<em><b>Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IADDRESS__CONTACT = eINSTANCE.getIAddress_Contact();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IImage <em>IImage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IImage
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIImage()
		 * @generated
		 */
		EClass IIMAGE = eINSTANCE.getIImage();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IIMAGE__DATE = eINSTANCE.getIImage_Date();

		/**
		 * The meta object literal for the '<em><b>Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IIMAGE__PREFIX = eINSTANCE.getIImage_Prefix();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IIMAGE__TITLE = eINSTANCE.getIImage_Title();

		/**
		 * The meta object literal for the '<em><b>Image</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IIMAGE__IMAGE = eINSTANCE.getIImage_Image();

		/**
		 * The meta object literal for the '<em><b>Mime Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IIMAGE__MIME_TYPE = eINSTANCE.getIImage_MimeType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.WithAssignableId <em>With Assignable Id</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.WithAssignableId
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getWithAssignableId()
		 * @generated
		 */
		EClass WITH_ASSIGNABLE_ID = eINSTANCE.getWithAssignableId();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IPrescription <em>IPrescription</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPrescription
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPrescription()
		 * @generated
		 */
		EClass IPRESCRIPTION = eINSTANCE.getIPrescription();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPRESCRIPTION__PATIENT = eINSTANCE.getIPrescription_Patient();

		/**
		 * The meta object literal for the '<em><b>Article</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPRESCRIPTION__ARTICLE = eINSTANCE.getIPrescription_Article();

		/**
		 * The meta object literal for the '<em><b>Date From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__DATE_FROM = eINSTANCE.getIPrescription_DateFrom();

		/**
		 * The meta object literal for the '<em><b>Date To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__DATE_TO = eINSTANCE.getIPrescription_DateTo();

		/**
		 * The meta object literal for the '<em><b>Dosage Instruction</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__DOSAGE_INSTRUCTION = eINSTANCE.getIPrescription_DosageInstruction();

		/**
		 * The meta object literal for the '<em><b>Remark</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__REMARK = eINSTANCE.getIPrescription_Remark();

		/**
		 * The meta object literal for the '<em><b>Stop Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__STOP_REASON = eINSTANCE.getIPrescription_StopReason();

		/**
		 * The meta object literal for the '<em><b>Entry Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__ENTRY_TYPE = eINSTANCE.getIPrescription_EntryType();

		/**
		 * The meta object literal for the '<em><b>Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__APPLIED = eINSTANCE.getIPrescription_Applied();

		/**
		 * The meta object literal for the '<em><b>Sort Order</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__SORT_ORDER = eINSTANCE.getIPrescription_SortOrder();

		/**
		 * The meta object literal for the '<em><b>Disposal Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPRESCRIPTION__DISPOSAL_COMMENT = eINSTANCE.getIPrescription_DisposalComment();

		/**
		 * The meta object literal for the '<em><b>Prescriptor</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPRESCRIPTION__PRESCRIPTOR = eINSTANCE.getIPrescription_Prescriptor();

		/**
		 * The meta object literal for the '<em><b>Recipe</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPRESCRIPTION__RECIPE = eINSTANCE.getIPrescription_Recipe();

		/**
		 * The meta object literal for the '<em><b>Billed</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPRESCRIPTION__BILLED = eINSTANCE.getIPrescription_Billed();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IRight <em>IRight</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IRight
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRight()
		 * @generated
		 */
		EClass IRIGHT = eINSTANCE.getIRight();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRIGHT__NAME = eINSTANCE.getIRight_Name();

		/**
		 * The meta object literal for the '<em><b>Localized Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRIGHT__LOCALIZED_NAME = eINSTANCE.getIRight_LocalizedName();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRIGHT__PARENT = eINSTANCE.getIRight_Parent();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBillingSystem <em>IBilling System</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBillingSystem
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBillingSystem()
		 * @generated
		 */
		EClass IBILLING_SYSTEM = eINSTANCE.getIBillingSystem();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM__NAME = eINSTANCE.getIBillingSystem_Name();

		/**
		 * The meta object literal for the '<em><b>Law</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IBILLING_SYSTEM__LAW = eINSTANCE.getIBillingSystem_Law();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IRecipe <em>IRecipe</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IRecipe
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRecipe()
		 * @generated
		 */
		EClass IRECIPE = eINSTANCE.getIRecipe();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRECIPE__PATIENT = eINSTANCE.getIRecipe_Patient();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRECIPE__MANDATOR = eINSTANCE.getIRecipe_Mandator();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRECIPE__DATE = eINSTANCE.getIRecipe_Date();

		/**
		 * The meta object literal for the '<em><b>Prescriptions</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRECIPE__PRESCRIPTIONS = eINSTANCE.getIRecipe_Prescriptions();

		/**
		 * The meta object literal for the '<em><b>Document</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRECIPE__DOCUMENT = eINSTANCE.getIRecipe_Document();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IBlobSecondary <em>IBlob Secondary</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IBlobSecondary
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIBlobSecondary()
		 * @generated
		 */
		EClass IBLOB_SECONDARY = eINSTANCE.getIBlobSecondary();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IRelatedContact <em>IRelated Contact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IRelatedContact
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIRelatedContact()
		 * @generated
		 */
		EClass IRELATED_CONTACT = eINSTANCE.getIRelatedContact();

		/**
		 * The meta object literal for the '<em><b>My Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRELATED_CONTACT__MY_CONTACT = eINSTANCE.getIRelatedContact_MyContact();

		/**
		 * The meta object literal for the '<em><b>Other Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IRELATED_CONTACT__OTHER_CONTACT = eINSTANCE.getIRelatedContact_OtherContact();

		/**
		 * The meta object literal for the '<em><b>Relationship Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRELATED_CONTACT__RELATIONSHIP_DESCRIPTION = eINSTANCE.getIRelatedContact_RelationshipDescription();

		/**
		 * The meta object literal for the '<em><b>My Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRELATED_CONTACT__MY_TYPE = eINSTANCE.getIRelatedContact_MyType();

		/**
		 * The meta object literal for the '<em><b>Other Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IRELATED_CONTACT__OTHER_TYPE = eINSTANCE.getIRelatedContact_OtherType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IPayment <em>IPayment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPayment
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPayment()
		 * @generated
		 */
		EClass IPAYMENT = eINSTANCE.getIPayment();

		/**
		 * The meta object literal for the '<em><b>Invoice</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPAYMENT__INVOICE = eINSTANCE.getIPayment_Invoice();

		/**
		 * The meta object literal for the '<em><b>Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPAYMENT__AMOUNT = eINSTANCE.getIPayment_Amount();

		/**
		 * The meta object literal for the '<em><b>Remark</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPAYMENT__REMARK = eINSTANCE.getIPayment_Remark();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IPAYMENT__DATE = eINSTANCE.getIPayment_Date();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IAccountTransaction <em>IAccount Transaction</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IAccountTransaction
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAccountTransaction()
		 * @generated
		 */
		EClass IACCOUNT_TRANSACTION = eINSTANCE.getIAccountTransaction();

		/**
		 * The meta object literal for the '<em><b>Invoice</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IACCOUNT_TRANSACTION__INVOICE = eINSTANCE.getIAccountTransaction_Invoice();

		/**
		 * The meta object literal for the '<em><b>Payment</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IACCOUNT_TRANSACTION__PAYMENT = eINSTANCE.getIAccountTransaction_Payment();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IACCOUNT_TRANSACTION__PATIENT = eINSTANCE.getIAccountTransaction_Patient();

		/**
		 * The meta object literal for the '<em><b>Amount</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IACCOUNT_TRANSACTION__AMOUNT = eINSTANCE.getIAccountTransaction_Amount();

		/**
		 * The meta object literal for the '<em><b>Remark</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IACCOUNT_TRANSACTION__REMARK = eINSTANCE.getIAccountTransaction_Remark();

		/**
		 * The meta object literal for the '<em><b>Account</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IACCOUNT_TRANSACTION__ACCOUNT = eINSTANCE.getIAccountTransaction_Account();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IAccount <em>IAccount</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IAccount
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAccount()
		 * @generated
		 */
		EClass IACCOUNT = eINSTANCE.getIAccount();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IACCOUNT__NAME = eINSTANCE.getIAccount_Name();

		/**
		 * The meta object literal for the '<em><b>Numeric</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IACCOUNT__NUMERIC = eINSTANCE.getIAccount_Numeric();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IACCOUNT_TRANSACTION__DATE = eINSTANCE.getIAccountTransaction_Date();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IMessage <em>IMessage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IMessage
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIMessage()
		 * @generated
		 */
		EClass IMESSAGE = eINSTANCE.getIMessage();

		/**
		 * The meta object literal for the '<em><b>Sender</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__SENDER = eINSTANCE.getIMessage_Sender();

		/**
		 * The meta object literal for the '<em><b>Receiver</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__RECEIVER = eINSTANCE.getIMessage_Receiver();

		/**
		 * The meta object literal for the '<em><b>Sender Accepts Answer</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__SENDER_ACCEPTS_ANSWER = eINSTANCE.getIMessage_SenderAcceptsAnswer();

		/**
		 * The meta object literal for the '<em><b>Create Date Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__CREATE_DATE_TIME = eINSTANCE.getIMessage_CreateDateTime();

		/**
		 * The meta object literal for the '<em><b>Message Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__MESSAGE_TEXT = eINSTANCE.getIMessage_MessageText();

		/**
		 * The meta object literal for the '<em><b>Message Codes</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__MESSAGE_CODES = eINSTANCE.getIMessage_MessageCodes();

		/**
		 * The meta object literal for the '<em><b>Message Priority</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__MESSAGE_PRIORITY = eINSTANCE.getIMessage_MessagePriority();

		/**
		 * The meta object literal for the '<em><b>Preferred Transporters</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMESSAGE__PREFERRED_TRANSPORTERS = eINSTANCE.getIMessage_PreferredTransporters();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ITextTemplate <em>IText Template</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ITextTemplate
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getITextTemplate()
		 * @generated
		 */
		EClass ITEXT_TEMPLATE = eINSTANCE.getITextTemplate();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEXT_TEMPLATE__CATEGORY = eINSTANCE.getITextTemplate_Category();

		/**
		 * The meta object literal for the '<em><b>Mandator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ITEXT_TEMPLATE__MANDATOR = eINSTANCE.getITextTemplate_Mandator();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEXT_TEMPLATE__NAME = eINSTANCE.getITextTemplate_Name();

		/**
		 * The meta object literal for the '<em><b>Template</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEXT_TEMPLATE__TEMPLATE = eINSTANCE.getITextTemplate_Template();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IAppointmentSeries <em>IAppointment Series</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IAppointmentSeries
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAppointmentSeries()
		 * @generated
		 */
		EClass IAPPOINTMENT_SERIES = eINSTANCE.getIAppointmentSeries();

		/**
		 * The meta object literal for the '<em><b>Series Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_TYPE = eINSTANCE.getIAppointmentSeries_SeriesType();

		/**
		 * The meta object literal for the '<em><b>Ending Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__ENDING_TYPE = eINSTANCE.getIAppointmentSeries_EndingType();

		/**
		 * The meta object literal for the '<em><b>Series Start Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_START_DATE = eINSTANCE.getIAppointmentSeries_SeriesStartDate();

		/**
		 * The meta object literal for the '<em><b>Series Start Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_START_TIME = eINSTANCE.getIAppointmentSeries_SeriesStartTime();

		/**
		 * The meta object literal for the '<em><b>Series End Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_END_DATE = eINSTANCE.getIAppointmentSeries_SeriesEndDate();

		/**
		 * The meta object literal for the '<em><b>Series End Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_END_TIME = eINSTANCE.getIAppointmentSeries_SeriesEndTime();

		/**
		 * The meta object literal for the '<em><b>Series Pattern String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__SERIES_PATTERN_STRING = eINSTANCE.getIAppointmentSeries_SeriesPatternString();

		/**
		 * The meta object literal for the '<em><b>Ending Pattern String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__ENDING_PATTERN_STRING = eINSTANCE.getIAppointmentSeries_EndingPatternString();

		/**
		 * The meta object literal for the '<em><b>Persistent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT_SERIES__PERSISTENT = eINSTANCE.getIAppointmentSeries_Persistent();

		/**
		 * The meta object literal for the '<em><b>Root Appointment</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IAPPOINTMENT_SERIES__ROOT_APPOINTMENT = eINSTANCE.getIAppointmentSeries_RootAppointment();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.ISickCertificate <em>ISick Certificate</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ISickCertificate
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISickCertificate()
		 * @generated
		 */
		EClass ISICK_CERTIFICATE = eINSTANCE.getISickCertificate();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISICK_CERTIFICATE__PATIENT = eINSTANCE.getISickCertificate_Patient();

		/**
		 * The meta object literal for the '<em><b>Coverage</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISICK_CERTIFICATE__COVERAGE = eINSTANCE.getISickCertificate_Coverage();

		/**
		 * The meta object literal for the '<em><b>Letter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ISICK_CERTIFICATE__LETTER = eINSTANCE.getISickCertificate_Letter();

		/**
		 * The meta object literal for the '<em><b>Percent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__PERCENT = eINSTANCE.getISickCertificate_Percent();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__DATE = eINSTANCE.getISickCertificate_Date();

		/**
		 * The meta object literal for the '<em><b>Start</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__START = eINSTANCE.getISickCertificate_Start();

		/**
		 * The meta object literal for the '<em><b>End</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__END = eINSTANCE.getISickCertificate_End();

		/**
		 * The meta object literal for the '<em><b>Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__REASON = eINSTANCE.getISickCertificate_Reason();

		/**
		 * The meta object literal for the '<em><b>Note</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISICK_CERTIFICATE__NOTE = eINSTANCE.getISickCertificate_Note();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IDayMessage <em>IDay Message</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IDayMessage
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIDayMessage()
		 * @generated
		 */
		EClass IDAY_MESSAGE = eINSTANCE.getIDayMessage();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDAY_MESSAGE__TITLE = eINSTANCE.getIDayMessage_Title();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDAY_MESSAGE__MESSAGE = eINSTANCE.getIDayMessage_Message();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDAY_MESSAGE__DATE = eINSTANCE.getIDayMessage_Date();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IReminder <em>IReminder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IReminder
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIReminder()
		 * @generated
		 */
		EClass IREMINDER = eINSTANCE.getIReminder();

		/**
		 * The meta object literal for the '<em><b>Creator</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IREMINDER__CREATOR = eINSTANCE.getIReminder_Creator();

		/**
		 * The meta object literal for the '<em><b>Responsible</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IREMINDER__RESPONSIBLE = eINSTANCE.getIReminder_Responsible();

		/**
		 * The meta object literal for the '<em><b>Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IREMINDER__CONTACT = eINSTANCE.getIReminder_Contact();

		/**
		 * The meta object literal for the '<em><b>Due</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__DUE = eINSTANCE.getIReminder_Due();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__STATUS = eINSTANCE.getIReminder_Status();

		/**
		 * The meta object literal for the '<em><b>Visibility</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__VISIBILITY = eINSTANCE.getIReminder_Visibility();

		/**
		 * The meta object literal for the '<em><b>Subject</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__SUBJECT = eINSTANCE.getIReminder_Subject();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__MESSAGE = eINSTANCE.getIReminder_Message();

		/**
		 * The meta object literal for the '<em><b>Priority</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__PRIORITY = eINSTANCE.getIReminder_Priority();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__TYPE = eINSTANCE.getIReminder_Type();

		/**
		 * The meta object literal for the '<em><b>Responsible All</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IREMINDER__RESPONSIBLE_ALL = eINSTANCE.getIReminder_ResponsibleAll();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IReminderResponsibleLink <em>IReminder Responsible Link</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IReminderResponsibleLink
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIReminderResponsibleLink()
		 * @generated
		 */
		EClass IREMINDER_RESPONSIBLE_LINK = eINSTANCE.getIReminderResponsibleLink();

		/**
		 * The meta object literal for the '<em><b>Reminder</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IREMINDER_RESPONSIBLE_LINK__REMINDER = eINSTANCE.getIReminderResponsibleLink_Reminder();

		/**
		 * The meta object literal for the '<em><b>Responsible</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IREMINDER_RESPONSIBLE_LINK__RESPONSIBLE = eINSTANCE.getIReminderResponsibleLink_Responsible();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IVaccination <em>IVaccination</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IVaccination
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIVaccination()
		 * @generated
		 */
		EClass IVACCINATION = eINSTANCE.getIVaccination();

		/**
		 * The meta object literal for the '<em><b>Patient</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IVACCINATION__PATIENT = eINSTANCE.getIVaccination_Patient();

		/**
		 * The meta object literal for the '<em><b>Article</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IVACCINATION__ARTICLE = eINSTANCE.getIVaccination_Article();

		/**
		 * The meta object literal for the '<em><b>Article Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__ARTICLE_NAME = eINSTANCE.getIVaccination_ArticleName();

		/**
		 * The meta object literal for the '<em><b>Article Gtin</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__ARTICLE_GTIN = eINSTANCE.getIVaccination_ArticleGtin();

		/**
		 * The meta object literal for the '<em><b>Article Atc</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__ARTICLE_ATC = eINSTANCE.getIVaccination_ArticleAtc();

		/**
		 * The meta object literal for the '<em><b>Lot Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__LOT_NUMBER = eINSTANCE.getIVaccination_LotNumber();

		/**
		 * The meta object literal for the '<em><b>Date Of Administration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__DATE_OF_ADMINISTRATION = eINSTANCE.getIVaccination_DateOfAdministration();

		/**
		 * The meta object literal for the '<em><b>Ingredients Atc</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IVACCINATION__INGREDIENTS_ATC = eINSTANCE.getIVaccination_IngredientsAtc();

		/**
		 * The meta object literal for the '<em><b>Performer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IVACCINATION__PERFORMER = eINSTANCE.getIVaccination_Performer();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.model.IAppointment <em>IAppointment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IAppointment
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIAppointment()
		 * @generated
		 */
		EClass IAPPOINTMENT = eINSTANCE.getIAppointment();

		/**
		 * The meta object literal for the '<em><b>Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__REASON = eINSTANCE.getIAppointment_Reason();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__STATE = eINSTANCE.getIAppointment_State();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__TYPE = eINSTANCE.getIAppointment_Type();

		/**
		 * The meta object literal for the '<em><b>Duration Minutes</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__DURATION_MINUTES = eINSTANCE.getIAppointment_DurationMinutes();

		/**
		 * The meta object literal for the '<em><b>Schedule</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__SCHEDULE = eINSTANCE.getIAppointment_Schedule();

		/**
		 * The meta object literal for the '<em><b>Created By</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__CREATED_BY = eINSTANCE.getIAppointment_CreatedBy();

		/**
		 * The meta object literal for the '<em><b>Subject Or Patient</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__SUBJECT_OR_PATIENT = eINSTANCE.getIAppointment_SubjectOrPatient();

		/**
		 * The meta object literal for the '<em><b>Priority</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__PRIORITY = eINSTANCE.getIAppointment_Priority();

		/**
		 * The meta object literal for the '<em><b>Treatment Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__TREATMENT_REASON = eINSTANCE.getIAppointment_TreatmentReason();

		/**
		 * The meta object literal for the '<em><b>Case Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__CASE_TYPE = eINSTANCE.getIAppointment_CaseType();

		/**
		 * The meta object literal for the '<em><b>Insurance Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__INSURANCE_TYPE = eINSTANCE.getIAppointment_InsuranceType();

		/**
		 * The meta object literal for the '<em><b>Linkgroup</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__LINKGROUP = eINSTANCE.getIAppointment_Linkgroup();

		/**
		 * The meta object literal for the '<em><b>Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__EXTENSION = eINSTANCE.getIAppointment_Extension();

		/**
		 * The meta object literal for the '<em><b>Created</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__CREATED = eINSTANCE.getIAppointment_Created();

		/**
		 * The meta object literal for the '<em><b>Last Edit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__LAST_EDIT = eINSTANCE.getIAppointment_LastEdit();

		/**
		 * The meta object literal for the '<em><b>State History</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__STATE_HISTORY = eINSTANCE.getIAppointment_StateHistory();

		/**
		 * The meta object literal for the '<em><b>Recurring</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IAPPOINTMENT__RECURRING = eINSTANCE.getIAppointment_Recurring();

	}

} //ModelPackage
