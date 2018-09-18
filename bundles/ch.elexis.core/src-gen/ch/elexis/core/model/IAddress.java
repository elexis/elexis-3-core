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

import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IAddress</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IAddress#getStreet1 <em>Street1</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getStreet2 <em>Street2</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getZip <em>Zip</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getCity <em>City</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getCountry <em>Country</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getWrittenAddress <em>Written Address</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getType <em>Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAddress#getContact <em>Contact</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIAddress()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IAddress extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Street1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Street1</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Street1</em>' attribute.
	 * @see #setStreet1(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Street1()
	 * @model
	 * @generated
	 */
	String getStreet1();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getStreet1 <em>Street1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Street1</em>' attribute.
	 * @see #getStreet1()
	 * @generated
	 */
	void setStreet1(String value);

	/**
	 * Returns the value of the '<em><b>Street2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Street2</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Street2</em>' attribute.
	 * @see #setStreet2(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Street2()
	 * @model
	 * @generated
	 */
	String getStreet2();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getStreet2 <em>Street2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Street2</em>' attribute.
	 * @see #getStreet2()
	 * @generated
	 */
	void setStreet2(String value);

	/**
	 * Returns the value of the '<em><b>Zip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zip</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zip</em>' attribute.
	 * @see #setZip(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Zip()
	 * @model
	 * @generated
	 */
	String getZip();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getZip <em>Zip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Zip</em>' attribute.
	 * @see #getZip()
	 * @generated
	 */
	void setZip(String value);

	/**
	 * Returns the value of the '<em><b>City</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>City</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>City</em>' attribute.
	 * @see #setCity(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_City()
	 * @model
	 * @generated
	 */
	String getCity();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getCity <em>City</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>City</em>' attribute.
	 * @see #getCity()
	 * @generated
	 */
	void setCity(String value);

	/**
	 * Returns the value of the '<em><b>Country</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Country</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Country</em>' attribute.
	 * @see #setCountry(Country)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Country()
	 * @model dataType="ch.elexis.core.types.Country"
	 * @generated
	 */
	Country getCountry();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getCountry <em>Country</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Country</em>' attribute.
	 * @see #getCountry()
	 * @generated
	 */
	void setCountry(Country value);

	/**
	 * Returns the value of the '<em><b>Written Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Written Address</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Written Address</em>' attribute.
	 * @see #setWrittenAddress(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_WrittenAddress()
	 * @model
	 * @generated
	 */
	String getWrittenAddress();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getWrittenAddress <em>Written Address</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Written Address</em>' attribute.
	 * @see #getWrittenAddress()
	 * @generated
	 */
	void setWrittenAddress(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.types.AddressType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see ch.elexis.core.types.AddressType
	 * @see #setType(AddressType)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Type()
	 * @model required="true"
	 * @generated
	 */
	AddressType getType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see ch.elexis.core.types.AddressType
	 * @see #getType()
	 * @generated
	 */
	void setType(AddressType value);

	/**
	 * Returns the value of the '<em><b>Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Contact</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contact</em>' reference.
	 * @see #setContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIAddress_Contact()
	 * @model required="true"
	 * @generated
	 */
	IContact getContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAddress#getContact <em>Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contact</em>' reference.
	 * @see #getContact()
	 * @generated
	 */
	void setContact(IContact value);

} // IAddress
