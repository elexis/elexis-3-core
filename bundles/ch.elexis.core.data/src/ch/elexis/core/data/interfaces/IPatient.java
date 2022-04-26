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
package ch.elexis.core.data.interfaces;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>IPatient</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link ch.elexis.core.model.IPatient#getDiagnosen
 * <em>Diagnosen</em>}</li>
 * <li>{@link ch.elexis.core.model.IPatient#getRisk <em>Risk</em>}</li>
 * <li>{@link ch.elexis.core.model.IPatient#getFamilyAnamnese <em>Family
 * Anamnese</em>}</li>
 * <li>{@link ch.elexis.core.model.IPatient#getPersonalAnamnese <em>Personal
 * Anamnese</em>}</li>
 * <li>{@link ch.elexis.core.model.IPatient#getAllergies
 * <em>Allergies</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIPatient()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPatient extends IPerson {
	/**
	 * Returns the value of the '<em><b>Diagnosen</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Diagnosen</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Diagnosen</em>' attribute.
	 * @see #setDiagnosen(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPatient_Diagnosen()
	 * @model
	 * @generated
	 */
	String getDiagnosen();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPatient#getDiagnosen
	 * <em>Diagnosen</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value the new value of the '<em>Diagnosen</em>' attribute.
	 * @see #getDiagnosen()
	 * @generated
	 */
	void setDiagnosen(String value);

	/**
	 * Returns the value of the '<em><b>Risk</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Risk</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Risk</em>' attribute.
	 * @see #setRisk(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPatient_Risk()
	 * @model
	 * @generated
	 */
	String getRisk();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPatient#getRisk
	 * <em>Risk</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value the new value of the '<em>Risk</em>' attribute.
	 * @see #getRisk()
	 * @generated
	 */
	void setRisk(String value);

	/**
	 * Returns the value of the '<em><b>Family Anamnese</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Family Anamnese</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Family Anamnese</em>' attribute.
	 * @see #setFamilyAnamnese(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPatient_FamilyAnamnese()
	 * @model
	 * @generated
	 */
	String getFamilyAnamnese();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPatient#getFamilyAnamnese
	 * <em>Family Anamnese</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @param value the new value of the '<em>Family Anamnese</em>' attribute.
	 * @see #getFamilyAnamnese()
	 * @generated
	 */
	void setFamilyAnamnese(String value);

	/**
	 * Returns the value of the '<em><b>Personal Anamnese</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Personal Anamnese</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Personal Anamnese</em>' attribute.
	 * @see #setPersonalAnamnese(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPatient_PersonalAnamnese()
	 * @model
	 * @generated
	 */
	String getPersonalAnamnese();

	/**
	 * Sets the value of the
	 * '{@link ch.elexis.core.model.IPatient#getPersonalAnamnese <em>Personal
	 * Anamnese</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value the new value of the '<em>Personal Anamnese</em>' attribute.
	 * @see #getPersonalAnamnese()
	 * @generated
	 */
	void setPersonalAnamnese(String value);

	/**
	 * Returns the value of the '<em><b>Allergies</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Allergies</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Allergies</em>' attribute.
	 * @see #setAllergies(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPatient_Allergies()
	 * @model
	 * @generated
	 */
	String getAllergies();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPatient#getAllergies
	 * <em>Allergies</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value the new value of the '<em>Allergies</em>' attribute.
	 * @see #getAllergies()
	 * @generated
	 */
	void setAllergies(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient Nr</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @model kind="operation"
	 * @generated
	 */
	String getPatientNr();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @model
	 * @generated
	 */
	void setPatientNr(String patientNr);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @model kind="operation"
	 * @generated
	 */
	String getPatientLabel();

} // IPatient
