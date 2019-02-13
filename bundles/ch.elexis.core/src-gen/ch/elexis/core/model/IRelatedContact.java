/**
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import ch.elexis.core.types.RelationshipType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IRelated Contact</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IRelatedContact#getMyContact <em>My Contact</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRelatedContact#getOtherContact <em>Other Contact</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRelatedContact#getRelationshipDescription <em>Relationship Description</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRelatedContact#getMyType <em>My Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRelatedContact#getOtherType <em>Other Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IRelatedContact extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>My Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>My Contact</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>My Contact</em>' reference.
	 * @see #setMyContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact_MyContact()
	 * @model required="true"
	 * @generated
	 */
	IContact getMyContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRelatedContact#getMyContact <em>My Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>My Contact</em>' reference.
	 * @see #getMyContact()
	 * @generated
	 */
	void setMyContact(IContact value);

	/**
	 * Returns the value of the '<em><b>Other Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Other Contact</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Other Contact</em>' reference.
	 * @see #setOtherContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact_OtherContact()
	 * @model
	 * @generated
	 */
	IContact getOtherContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRelatedContact#getOtherContact <em>Other Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Other Contact</em>' reference.
	 * @see #getOtherContact()
	 * @generated
	 */
	void setOtherContact(IContact value);

	/**
	 * Returns the value of the '<em><b>Relationship Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Relationship Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relationship Description</em>' attribute.
	 * @see #setRelationshipDescription(String)
	 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact_RelationshipDescription()
	 * @model
	 * @generated
	 */
	String getRelationshipDescription();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRelatedContact#getRelationshipDescription <em>Relationship Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relationship Description</em>' attribute.
	 * @see #getRelationshipDescription()
	 * @generated
	 */
	void setRelationshipDescription(String value);

	/**
	 * Returns the value of the '<em><b>My Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.types.RelationshipType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>My Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>My Type</em>' attribute.
	 * @see ch.elexis.core.types.RelationshipType
	 * @see #setMyType(RelationshipType)
	 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact_MyType()
	 * @model
	 * @generated
	 */
	RelationshipType getMyType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRelatedContact#getMyType <em>My Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>My Type</em>' attribute.
	 * @see ch.elexis.core.types.RelationshipType
	 * @see #getMyType()
	 * @generated
	 */
	void setMyType(RelationshipType value);

	/**
	 * Returns the value of the '<em><b>Other Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.types.RelationshipType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Other Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Other Type</em>' attribute.
	 * @see ch.elexis.core.types.RelationshipType
	 * @see #setOtherType(RelationshipType)
	 * @see ch.elexis.core.model.ModelPackage#getIRelatedContact_OtherType()
	 * @model
	 * @generated
	 */
	RelationshipType getOtherType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRelatedContact#getOtherType <em>Other Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Other Type</em>' attribute.
	 * @see ch.elexis.core.types.RelationshipType
	 * @see #getOtherType()
	 * @generated
	 */
	void setOtherType(RelationshipType value);

} // IRelatedContact
