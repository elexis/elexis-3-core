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

import ch.elexis.core.model.prescription.EntryType;
import java.time.LocalDate;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IArticle Default Signature</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getAtcCode <em>Atc Code</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getMorning <em>Morning</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getNoon <em>Noon</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getEvening <em>Evening</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getNight <em>Night</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getComment <em>Comment</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getFreeText <em>Free Text</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getMedicationType <em>Medication Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getDisposalType <em>Disposal Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticleDefaultSignature#getEndDate <em>End Date</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IArticleDefaultSignature extends Deleteable, Identifiable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Atc Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Atc Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Atc Code</em>' attribute.
	 * @see #setAtcCode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_AtcCode()
	 * @model
	 * @generated
	 */
	String getAtcCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getAtcCode <em>Atc Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Atc Code</em>' attribute.
	 * @see #getAtcCode()
	 * @generated
	 */
	void setAtcCode(String value);

	/**
	 * Returns the value of the '<em><b>Morning</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Morning</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Morning</em>' attribute.
	 * @see #setMorning(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_Morning()
	 * @model
	 * @generated
	 */
	String getMorning();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getMorning <em>Morning</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Morning</em>' attribute.
	 * @see #getMorning()
	 * @generated
	 */
	void setMorning(String value);

	/**
	 * Returns the value of the '<em><b>Noon</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Noon</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Noon</em>' attribute.
	 * @see #setNoon(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_Noon()
	 * @model
	 * @generated
	 */
	String getNoon();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getNoon <em>Noon</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Noon</em>' attribute.
	 * @see #getNoon()
	 * @generated
	 */
	void setNoon(String value);

	/**
	 * Returns the value of the '<em><b>Evening</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Evening</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Evening</em>' attribute.
	 * @see #setEvening(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_Evening()
	 * @model
	 * @generated
	 */
	String getEvening();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getEvening <em>Evening</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Evening</em>' attribute.
	 * @see #getEvening()
	 * @generated
	 */
	void setEvening(String value);

	/**
	 * Returns the value of the '<em><b>Night</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Night</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Night</em>' attribute.
	 * @see #setNight(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_Night()
	 * @model
	 * @generated
	 */
	String getNight();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getNight <em>Night</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Night</em>' attribute.
	 * @see #getNight()
	 * @generated
	 */
	void setNight(String value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Free Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Free Text</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Free Text</em>' attribute.
	 * @see #setFreeText(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_FreeText()
	 * @model
	 * @generated
	 */
	String getFreeText();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getFreeText <em>Free Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Free Text</em>' attribute.
	 * @see #getFreeText()
	 * @generated
	 */
	void setFreeText(String value);

	/**
	 * Returns the value of the '<em><b>Medication Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Medication Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Medication Type</em>' attribute.
	 * @see #setMedicationType(EntryType)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_MedicationType()
	 * @model dataType="ch.elexis.core.types.EntryType"
	 * @generated
	 */
	EntryType getMedicationType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getMedicationType <em>Medication Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Medication Type</em>' attribute.
	 * @see #getMedicationType()
	 * @generated
	 */
	void setMedicationType(EntryType value);

	/**
	 * Returns the value of the '<em><b>Disposal Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Disposal Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Disposal Type</em>' attribute.
	 * @see #setDisposalType(EntryType)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_DisposalType()
	 * @model dataType="ch.elexis.core.types.EntryType"
	 * @generated
	 */
	EntryType getDisposalType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getDisposalType <em>Disposal Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Disposal Type</em>' attribute.
	 * @see #getDisposalType()
	 * @generated
	 */
	void setDisposalType(EntryType value);

	/**
	 * Returns the value of the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Date</em>' attribute.
	 * @see #setEndDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIArticleDefaultSignature_EndDate()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getEndDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticleDefaultSignature#getEndDate <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Date</em>' attribute.
	 * @see #getEndDate()
	 * @generated
	 */
	void setEndDate(LocalDate value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setArticle(IArticle article);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isAtc();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getSignatureAsDosisString();

} // IArticleDefaultSignature
