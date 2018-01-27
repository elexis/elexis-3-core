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
	 * The meta object id for the '{@link ch.elexis.core.types.ContactType <em>Contact Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.ContactType
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getContactType()
	 * @generated
	 */
	int CONTACT_TYPE = 3;

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
	 * The meta object id for the '<em>Time Tool</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.rgw.tools.TimeTool
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getTimeTool()
	 * @generated
	 */
	int TIME_TOOL = 9;

	/**
	 * The meta object id for the '<em>Gender</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.Gender
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getGender()
	 * @generated
	 */
	int GENDER = 10;


	/**
	 * The meta object id for the '<em>Lab Item Typ</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.LabItemTyp
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getLabItemTyp()
	 * @generated
	 */
	int LAB_ITEM_TYP = 11;


	/**
	 * The meta object id for the '<em>Country</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.Country
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getCountry()
	 * @generated
	 */
	int COUNTRY = 12;


	/**
	 * The meta object id for the '<em>Pathologic Description</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.types.PathologicDescription
	 * @see ch.elexis.core.types.impl.TypesPackageImpl#getPathologicDescription()
	 * @generated
	 */
	int PATHOLOGIC_DESCRIPTION = 13;


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
	 * Returns the meta object for enum '{@link ch.elexis.core.types.ContactType <em>Contact Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Contact Type</em>'.
	 * @see ch.elexis.core.types.ContactType
	 * @generated
	 */
	EEnum getContactType();

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
	 * Returns the meta object for data type '{@link ch.rgw.tools.TimeTool <em>Time Tool</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Time Tool</em>'.
	 * @see ch.rgw.tools.TimeTool
	 * @model instanceClass="ch.rgw.tools.TimeTool"
	 * @generated
	 */
	EDataType getTimeTool();

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
		 * The meta object literal for the '{@link ch.elexis.core.types.ContactType <em>Contact Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.types.ContactType
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getContactType()
		 * @generated
		 */
		EEnum CONTACT_TYPE = eINSTANCE.getContactType();

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
		 * The meta object literal for the '<em>Time Tool</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.rgw.tools.TimeTool
		 * @see ch.elexis.core.types.impl.TypesPackageImpl#getTimeTool()
		 * @generated
		 */
		EDataType TIME_TOOL = eINSTANCE.getTimeTool();

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

	}

} //TypesPackage
