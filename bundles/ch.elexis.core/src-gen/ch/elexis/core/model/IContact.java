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

import ch.elexis.core.types.Country;
import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IContact</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IContact#isMandator <em>Mandator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isUser <em>User</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isPerson <em>Person</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isOrganization <em>Organization</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isLaboratory <em>Laboratory</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getDescription1 <em>Description1</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getDescription2 <em>Description2</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getDescription3 <em>Description3</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getCode <em>Code</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getCountry <em>Country</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getZip <em>Zip</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getCity <em>City</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getStreet <em>Street</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getPhone1 <em>Phone1</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getPhone2 <em>Phone2</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getFax <em>Fax</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getEmail <em>Email</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getWebsite <em>Website</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getMobile <em>Mobile</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getComment <em>Comment</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getAddress <em>Address</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getGroup <em>Group</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getPostalAddress <em>Postal Address</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getImage <em>Image</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getRelatedContacts <em>Related Contacts</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#isDeceased <em>Deceased</em>}</li>
 *   <li>{@link ch.elexis.core.model.IContact#getEmail2 <em>Email2</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIContact()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IContact extends Identifiable, Deleteable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Mandator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mandator</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mandator</em>' attribute.
	 * @see #setMandator(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Mandator()
	 * @model
	 * @generated
	 */
	boolean isMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isMandator <em>Mandator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mandator</em>' attribute.
	 * @see #isMandator()
	 * @generated
	 */
	void setMandator(boolean value);

	/**
	 * Returns the value of the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' attribute.
	 * @see #setUser(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_User()
	 * @model
	 * @generated
	 */
	boolean isUser();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User</em>' attribute.
	 * @see #isUser()
	 * @generated
	 */
	void setUser(boolean value);

	/**
	 * Returns the value of the '<em><b>Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' attribute.
	 * @see #setPatient(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Patient()
	 * @model
	 * @generated
	 */
	boolean isPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isPatient <em>Patient</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' attribute.
	 * @see #isPatient()
	 * @generated
	 */
	void setPatient(boolean value);

	/**
	 * Returns the value of the '<em><b>Person</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Person</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Person</em>' attribute.
	 * @see #setPerson(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Person()
	 * @model
	 * @generated
	 */
	boolean isPerson();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isPerson <em>Person</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Person</em>' attribute.
	 * @see #isPerson()
	 * @generated
	 */
	void setPerson(boolean value);

	/**
	 * Returns the value of the '<em><b>Laboratory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Laboratory</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Laboratory</em>' attribute.
	 * @see #setLaboratory(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Laboratory()
	 * @model
	 * @generated
	 */
	boolean isLaboratory();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isLaboratory <em>Laboratory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Laboratory</em>' attribute.
	 * @see #isLaboratory()
	 * @generated
	 */
	void setLaboratory(boolean value);

	/**
	 * Returns the value of the '<em><b>Organization</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Organization</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Organization</em>' attribute.
	 * @see #setOrganization(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Organization()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='organisation'"
	 * @generated
	 */
	boolean isOrganization();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isOrganization <em>Organization</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Organization</em>' attribute.
	 * @see #isOrganization()
	 * @generated
	 */
	void setOrganization(boolean value);

	/**
	 * Returns the value of the '<em><b>Description1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description1</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description1</em>' attribute.
	 * @see #setDescription1(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Description1()
	 * @model
	 * @generated
	 */
	String getDescription1();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getDescription1 <em>Description1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description1</em>' attribute.
	 * @see #getDescription1()
	 * @generated
	 */
	void setDescription1(String value);

	/**
	 * Returns the value of the '<em><b>Description2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description2</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description2</em>' attribute.
	 * @see #setDescription2(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Description2()
	 * @model
	 * @generated
	 */
	String getDescription2();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getDescription2 <em>Description2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description2</em>' attribute.
	 * @see #getDescription2()
	 * @generated
	 */
	void setDescription2(String value);

	/**
	 * Returns the value of the '<em><b>Description3</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description3</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description3</em>' attribute.
	 * @see #setDescription3(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Description3()
	 * @model
	 * @generated
	 */
	String getDescription3();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getDescription3 <em>Description3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description3</em>' attribute.
	 * @see #getDescription3()
	 * @generated
	 */
	void setDescription3(String value);

	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see #setCode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Code()
	 * @model
	 * @generated
	 */
	String getCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getCode <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code</em>' attribute.
	 * @see #getCode()
	 * @generated
	 */
	void setCode(String value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Country()
	 * @model dataType="ch.elexis.core.types.Country"
	 * @generated
	 */
	Country getCountry();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getCountry <em>Country</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Country</em>' attribute.
	 * @see #getCountry()
	 * @generated
	 */
	void setCountry(Country value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Zip()
	 * @model
	 * @generated
	 */
	String getZip();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getZip <em>Zip</em>}' attribute.
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
	 * @see ch.elexis.core.model.ModelPackage#getIContact_City()
	 * @model
	 * @generated
	 */
	String getCity();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getCity <em>City</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>City</em>' attribute.
	 * @see #getCity()
	 * @generated
	 */
	void setCity(String value);

	/**
	 * Returns the value of the '<em><b>Street</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Street</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Street</em>' attribute.
	 * @see #setStreet(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Street()
	 * @model
	 * @generated
	 */
	String getStreet();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getStreet <em>Street</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Street</em>' attribute.
	 * @see #getStreet()
	 * @generated
	 */
	void setStreet(String value);

	/**
	 * Returns the value of the '<em><b>Phone1</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Phone1</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Phone1</em>' attribute.
	 * @see #setPhone1(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Phone1()
	 * @model
	 * @generated
	 */
	String getPhone1();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getPhone1 <em>Phone1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Phone1</em>' attribute.
	 * @see #getPhone1()
	 * @generated
	 */
	void setPhone1(String value);

	/**
	 * Returns the value of the '<em><b>Phone2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Phone2</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Phone2</em>' attribute.
	 * @see #setPhone2(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Phone2()
	 * @model
	 * @generated
	 */
	String getPhone2();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getPhone2 <em>Phone2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Phone2</em>' attribute.
	 * @see #getPhone2()
	 * @generated
	 */
	void setPhone2(String value);

	/**
	 * Returns the value of the '<em><b>Fax</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Fax</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fax</em>' attribute.
	 * @see #setFax(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Fax()
	 * @model
	 * @generated
	 */
	String getFax();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getFax <em>Fax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fax</em>' attribute.
	 * @see #getFax()
	 * @generated
	 */
	void setFax(String value);

	/**
	 * Returns the value of the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Email</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Email()
	 * @model
	 * @generated
	 */
	String getEmail();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getEmail <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email</em>' attribute.
	 * @see #getEmail()
	 * @generated
	 */
	void setEmail(String value);

	/**
	 * Returns the value of the '<em><b>Website</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Website</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Website</em>' attribute.
	 * @see #setWebsite(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Website()
	 * @model
	 * @generated
	 */
	String getWebsite();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getWebsite <em>Website</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Website</em>' attribute.
	 * @see #getWebsite()
	 * @generated
	 */
	void setWebsite(String value);

	/**
	 * Returns the value of the '<em><b>Mobile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mobile</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mobile</em>' attribute.
	 * @see #setMobile(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Mobile()
	 * @model
	 * @generated
	 */
	String getMobile();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getMobile <em>Mobile</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mobile</em>' attribute.
	 * @see #getMobile()
	 * @generated
	 */
	void setMobile(String value);

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Address</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IAddress}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Address</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Address</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Address()
	 * @model
	 * @generated
	 */
	List<IAddress> getAddress();

	/**
	 * Returns the value of the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group</em>' attribute.
	 * @see #setGroup(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Group()
	 * @model
	 * @generated
	 */
	String getGroup();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getGroup <em>Group</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group</em>' attribute.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(String value);

	/**
	 * Returns the value of the '<em><b>Postal Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Postal Address</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Postal Address</em>' attribute.
	 * @see #setPostalAddress(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_PostalAddress()
	 * @model
	 * @generated
	 */
	String getPostalAddress();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getPostalAddress <em>Postal Address</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Postal Address</em>' attribute.
	 * @see #getPostalAddress()
	 * @generated
	 */
	void setPostalAddress(String value);

	/**
	 * Returns the value of the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image</em>' reference.
	 * @see #setImage(IImage)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Image()
	 * @model
	 * @generated
	 */
	IImage getImage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getImage <em>Image</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image</em>' reference.
	 * @see #getImage()
	 * @generated
	 */
	void setImage(IImage value);

	/**
	 * Returns the value of the '<em><b>Related Contacts</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IRelatedContact}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Related Contacts</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Related Contacts</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIContact_RelatedContacts()
	 * @model
	 * @generated
	 */
	List<IRelatedContact> getRelatedContacts();

	/**
	 * Returns the value of the '<em><b>Deceased</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Deceased</em>' attribute.
	 * @see #setDeceased(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Deceased()
	 * @model
	 * @generated
	 */
	boolean isDeceased();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#isDeceased <em>Deceased</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Deceased</em>' attribute.
	 * @see #isDeceased()
	 * @generated
	 */
	void setDeceased(boolean value);

	/**
	 * Returns the value of the '<em><b>Email2</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email2</em>' attribute.
	 * @see #setEmail2(String)
	 * @see ch.elexis.core.model.ModelPackage#getIContact_Email2()
	 * @model
	 * @generated
	 */
	String getEmail2();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IContact#getEmail2 <em>Email2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email2</em>' attribute.
	 * @see #getEmail2()
	 * @generated
	 */
	void setEmail2(String value);

} // IContact
