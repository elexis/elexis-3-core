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
	String eNS_URI = "http://ch.elexis.corel/model/model";

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
	int IDENTIFIABLE = 9;

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
	int ICONTACT = 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DELETED = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Contact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__CONTACT_TYPE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__MANDATOR = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__USER = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PATIENT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION1 = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION2 = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__DESCRIPTION3 = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__CODE = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__COUNTRY = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__ZIP = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__CITY = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__STREET = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PHONE1 = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__PHONE2 = IDENTIFIABLE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__FAX = IDENTIFIABLE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__EMAIL = IDENTIFIABLE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__WEBSITE = IDENTIFIABLE_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__MOBILE = IDENTIFIABLE_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT__COMMENT = IDENTIFIABLE_FEATURE_COUNT + 19;

	/**
	 * The number of structural features of the '<em>IContact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICONTACT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 20;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPersistentObject <em>IPersistent Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPersistentObject
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPersistentObject()
	 * @generated
	 */
	int IPERSISTENT_OBJECT = 1;

	/**
	 * The feature id for the '<em><b>Xid</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSISTENT_OBJECT__XID = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Xids</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSISTENT_OBJECT__XIDS = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IPersistent Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSISTENT_OBJECT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

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
	 * The feature id for the '<em><b>Xid</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__XID = IPERSISTENT_OBJECT__XID;

	/**
	 * The feature id for the '<em><b>Xids</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__XIDS = IPERSISTENT_OBJECT__XIDS;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN = IPERSISTENT_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Domain Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__DOMAIN_ID = IPERSISTENT_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Object</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__OBJECT = IPERSISTENT_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Quality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__QUALITY = IPERSISTENT_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>GUID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID__GUID = IPERSISTENT_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>IXid</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IXID_FEATURE_COUNT = IPERSISTENT_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICodeElement
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICodeElement()
	 * @generated
	 */
	int ICODE_ELEMENT = 3;

	/**
	 * The number of structural features of the '<em>ICode Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICODE_ELEMENT_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IChangeListener <em>IChange Listener</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IChangeListener
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIChangeListener()
	 * @generated
	 */
	int ICHANGE_LISTENER = 4;

	/**
	 * The number of structural features of the '<em>IChange Listener</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ICHANGE_LISTENER_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ISticker
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getISticker()
	 * @generated
	 */
	int ISTICKER = 5;

	/**
	 * The feature id for the '<em><b>Background</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__BACKGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Foreground</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__FOREGROUND = TypesPackage.COMPARABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER__VISIBLE = TypesPackage.COMPARABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>ISticker</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTICKER_FEATURE_COUNT = TypesPackage.COMPARABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPerson <em>IPerson</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPerson
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPerson()
	 * @generated
	 */
	int IPERSON = 6;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__DELETED = ICONTACT__DELETED;

	/**
	 * The feature id for the '<em><b>Contact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON__CONTACT_TYPE = ICONTACT__CONTACT_TYPE;

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
	 * The number of structural features of the '<em>IPerson</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERSON_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 4;

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
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__DELETED = IPERSON__DELETED;

	/**
	 * The feature id for the '<em><b>Contact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPATIENT__CONTACT_TYPE = IPERSON__CONTACT_TYPE;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IUser <em>IUser</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IUser
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIUser()
	 * @generated
	 */
	int IUSER = 8;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DELETED = ICONTACT__DELETED;

	/**
	 * The feature id for the '<em><b>Contact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__CONTACT_TYPE = ICONTACT__CONTACT_TYPE;

	/**
	 * The feature id for the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__MANDATOR = ICONTACT__MANDATOR;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__USER = ICONTACT__USER;

	/**
	 * The feature id for the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__PATIENT = ICONTACT__PATIENT;

	/**
	 * The feature id for the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DESCRIPTION1 = ICONTACT__DESCRIPTION1;

	/**
	 * The feature id for the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DESCRIPTION2 = ICONTACT__DESCRIPTION2;

	/**
	 * The feature id for the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__DESCRIPTION3 = ICONTACT__DESCRIPTION3;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__CODE = ICONTACT__CODE;

	/**
	 * The feature id for the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__COUNTRY = ICONTACT__COUNTRY;

	/**
	 * The feature id for the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__ZIP = ICONTACT__ZIP;

	/**
	 * The feature id for the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__CITY = ICONTACT__CITY;

	/**
	 * The feature id for the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__STREET = ICONTACT__STREET;

	/**
	 * The feature id for the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__PHONE1 = ICONTACT__PHONE1;

	/**
	 * The feature id for the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__PHONE2 = ICONTACT__PHONE2;

	/**
	 * The feature id for the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__FAX = ICONTACT__FAX;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__EMAIL = ICONTACT__EMAIL;

	/**
	 * The feature id for the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__WEBSITE = ICONTACT__WEBSITE;

	/**
	 * The feature id for the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__MOBILE = ICONTACT__MOBILE;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__COMMENT = ICONTACT__COMMENT;

	/**
	 * The feature id for the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__USERNAME = ICONTACT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER__PASSWORD = ICONTACT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IUser</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IUSER_FEATURE_COUNT = ICONTACT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.Deleteable <em>Deleteable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.Deleteable
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getDeleteable()
	 * @generated
	 */
	int DELETEABLE = 10;

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
	 * The meta object id for the '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ILabItem
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabItem()
	 * @generated
	 */
	int ILAB_ITEM = 11;

	/**
	 * The feature id for the '<em><b>Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__TYP = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Reference Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__REFERENCE_MALE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Reference Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__REFERENCE_FEMALE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__GROUP = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__PRIORITY = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__UNIT = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Kuerzel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__KUERZEL = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__NAME = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Digits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__DIGITS = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM__VISIBLE = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>ILab Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ITEM_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 10;

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
	 * The feature id for the '<em><b>Ref Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REF_MALE = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Ref Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__REF_FEMALE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__UNIT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Analyse Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ANALYSE_TIME = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Observation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__OBSERVATION_TIME = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Transmission Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__TRANSMISSION_TIME = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__RESULT = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Flags</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__FLAGS = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__COMMENT = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Origin Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ORIGIN_CONTACT = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__DATE = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__ITEM = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Pathologic Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT__PATHOLOGIC_DESCRIPTION = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The number of structural features of the '<em>ILab Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_RESULT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 13;

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
	 * The feature id for the '<em><b>Lab Result</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__LAB_RESULT = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Lab Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__LAB_ITEM = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Patient Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER__PATIENT_CONTACT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>ILab Order</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ILAB_ORDER_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IPeriod
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPeriod()
	 * @generated
	 */
	int IPERIOD = 14;

	/**
	 * The feature id for the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__START_TIME = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD__END_TIME = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>IPeriod</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IPERIOD_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

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
	 * The feature id for the '<em><b>Patient Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__PATIENT_ID = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Author Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__AUTHOR_ID = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__TITLE = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__DESCRIPTION = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__STATUS = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__CREATED = IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Lastchanged</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__LASTCHANGED = IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__MIME_TYPE = IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__CATEGORY = IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>History</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__HISTORY = IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Store Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__STORE_ID = IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__EXTENSION = IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Keywords</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT__KEYWORDS = IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The number of structural features of the '<em>IDocument</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDOCUMENT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 13;

	/**
	 * The meta object id for the '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.ICategory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getICategory()
	 * @generated
	 */
	int ICATEGORY = 16;

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
	 * The meta object id for the '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.IHistory
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIHistory()
	 * @generated
	 */
	int IHISTORY = 17;

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
	 * The meta object id for the '<em>String Array</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.impl.ModelPackageImpl#getStringArray()
	 * @generated
	 */
	int STRING_ARRAY = 18;


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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IContact#getContactType <em>Contact Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contact Type</em>'.
	 * @see ch.elexis.core.model.IContact#getContactType()
	 * @see #getIContact()
	 * @generated
	 */
	EAttribute getIContact_ContactType();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IPersistentObject <em>IPersistent Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IPersistent Object</em>'.
	 * @see ch.elexis.core.model.IPersistentObject
	 * @generated
	 */
	EClass getIPersistentObject();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IPersistentObject#getXid <em>Xid</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Xid</em>'.
	 * @see ch.elexis.core.model.IPersistentObject#getXid()
	 * @see #getIPersistentObject()
	 * @generated
	 */
	EReference getIPersistentObject_Xid();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.model.IPersistentObject#getXids <em>Xids</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Xids</em>'.
	 * @see ch.elexis.core.model.IPersistentObject#getXids()
	 * @see #getIPersistentObject()
	 * @generated
	 */
	EReference getIPersistentObject_Xids();

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
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.IXid#getObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Object</em>'.
	 * @see ch.elexis.core.model.IXid#getObject()
	 * @see #getIXid()
	 * @generated
	 */
	EReference getIXid_Object();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IXid#isGUID <em>GUID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>GUID</em>'.
	 * @see ch.elexis.core.model.IXid#isGUID()
	 * @see #getIXid()
	 * @generated
	 */
	EAttribute getIXid_GUID();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.IChangeListener <em>IChange Listener</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IChange Listener</em>'.
	 * @see ch.elexis.core.model.IChangeListener
	 * @generated
	 */
	EClass getIChangeListener();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabItem#getKuerzel <em>Kuerzel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kuerzel</em>'.
	 * @see ch.elexis.core.model.ILabItem#getKuerzel()
	 * @see #getILabItem()
	 * @generated
	 */
	EAttribute getILabItem_Kuerzel();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Result</em>'.
	 * @see ch.elexis.core.model.ILabResult
	 * @generated
	 */
	EClass getILabResult();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getRefMale <em>Ref Male</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ref Male</em>'.
	 * @see ch.elexis.core.model.ILabResult#getRefMale()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_RefMale();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getRefFemale <em>Ref Female</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ref Female</em>'.
	 * @see ch.elexis.core.model.ILabResult#getRefFemale()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_RefFemale();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.ILabResult#getFlags <em>Flags</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Flags</em>'.
	 * @see ch.elexis.core.model.ILabResult#getFlags()
	 * @see #getILabResult()
	 * @generated
	 */
	EAttribute getILabResult_Flags();

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
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabResult#getOriginContact <em>Origin Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Origin Contact</em>'.
	 * @see ch.elexis.core.model.ILabResult#getOriginContact()
	 * @see #getILabResult()
	 * @generated
	 */
	EReference getILabResult_OriginContact();

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
	 * Returns the meta object for class '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ILab Order</em>'.
	 * @see ch.elexis.core.model.ILabOrder
	 * @generated
	 */
	EClass getILabOrder();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getLabResult <em>Lab Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Lab Result</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getLabResult()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_LabResult();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getLabItem <em>Lab Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Lab Item</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getLabItem()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_LabItem();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.model.ILabOrder#getPatientContact <em>Patient Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Patient Contact</em>'.
	 * @see ch.elexis.core.model.ILabOrder#getPatientContact()
	 * @see #getILabOrder()
	 * @generated
	 */
	EReference getILabOrder_PatientContact();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getPatientId <em>Patient Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Patient Id</em>'.
	 * @see ch.elexis.core.model.IDocument#getPatientId()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_PatientId();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.model.IDocument#getAuthorId <em>Author Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Author Id</em>'.
	 * @see ch.elexis.core.model.IDocument#getAuthorId()
	 * @see #getIDocument()
	 * @generated
	 */
	EAttribute getIDocument_AuthorId();

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
	 * Returns the meta object for data type '<em>String Array</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>String Array</em>'.
	 * @model instanceClass="java.lang.String[]"
	 * @generated
	 */
	EDataType getStringArray();

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
		 * The meta object literal for the '<em><b>Contact Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ICONTACT__CONTACT_TYPE = eINSTANCE.getIContact_ContactType();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.IPersistentObject <em>IPersistent Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IPersistentObject
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIPersistentObject()
		 * @generated
		 */
		EClass IPERSISTENT_OBJECT = eINSTANCE.getIPersistentObject();

		/**
		 * The meta object literal for the '<em><b>Xid</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPERSISTENT_OBJECT__XID = eINSTANCE.getIPersistentObject_Xid();

		/**
		 * The meta object literal for the '<em><b>Xids</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IPERSISTENT_OBJECT__XIDS = eINSTANCE.getIPersistentObject_Xids();

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
		 * The meta object literal for the '<em><b>Object</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IXID__OBJECT = eINSTANCE.getIXid_Object();

		/**
		 * The meta object literal for the '<em><b>Quality</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__QUALITY = eINSTANCE.getIXid_Quality();

		/**
		 * The meta object literal for the '<em><b>GUID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IXID__GUID = eINSTANCE.getIXid_GUID();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.IChangeListener <em>IChange Listener</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.IChangeListener
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getIChangeListener()
		 * @generated
		 */
		EClass ICHANGE_LISTENER = eINSTANCE.getIChangeListener();

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
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__UNIT = eINSTANCE.getILabItem_Unit();

		/**
		 * The meta object literal for the '<em><b>Kuerzel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_ITEM__KUERZEL = eINSTANCE.getILabItem_Kuerzel();

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
		 * The meta object literal for the '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.ILabResult
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getILabResult()
		 * @generated
		 */
		EClass ILAB_RESULT = eINSTANCE.getILabResult();

		/**
		 * The meta object literal for the '<em><b>Ref Male</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__REF_MALE = eINSTANCE.getILabResult_RefMale();

		/**
		 * The meta object literal for the '<em><b>Ref Female</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__REF_FEMALE = eINSTANCE.getILabResult_RefFemale();

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
		 * The meta object literal for the '<em><b>Result</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__RESULT = eINSTANCE.getILabResult_Result();

		/**
		 * The meta object literal for the '<em><b>Flags</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__FLAGS = eINSTANCE.getILabResult_Flags();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__COMMENT = eINSTANCE.getILabResult_Comment();

		/**
		 * The meta object literal for the '<em><b>Origin Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_RESULT__ORIGIN_CONTACT = eINSTANCE.getILabResult_OriginContact();

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
		 * The meta object literal for the '<em><b>Pathologic Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ILAB_RESULT__PATHOLOGIC_DESCRIPTION = eINSTANCE.getILabResult_PathologicDescription();

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
		 * The meta object literal for the '<em><b>Lab Result</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__LAB_RESULT = eINSTANCE.getILabOrder_LabResult();

		/**
		 * The meta object literal for the '<em><b>Lab Item</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__LAB_ITEM = eINSTANCE.getILabOrder_LabItem();

		/**
		 * The meta object literal for the '<em><b>Patient Contact</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ILAB_ORDER__PATIENT_CONTACT = eINSTANCE.getILabOrder_PatientContact();

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
		 * The meta object literal for the '<em><b>Patient Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__PATIENT_ID = eINSTANCE.getIDocument_PatientId();

		/**
		 * The meta object literal for the '<em><b>Author Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDOCUMENT__AUTHOR_ID = eINSTANCE.getIDocument_AuthorId();

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
		 * The meta object literal for the '<em>String Array</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.impl.ModelPackageImpl#getStringArray()
		 * @generated
		 */
		EDataType STRING_ARRAY = eINSTANCE.getStringArray();

	}

} //ModelPackage
