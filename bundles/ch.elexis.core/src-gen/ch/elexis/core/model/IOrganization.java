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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IOrganization</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IOrganization#getInsuranceXmlName <em>Insurance Xml Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrganization#getInsuranceLawCode <em>Insurance Law Code</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIOrganization()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IOrganization extends IContact {

	/**
	 * Returns the value of the '<em><b>Insurance Xml Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Insurance Xml Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Insurance Xml Name</em>' attribute.
	 * @see #setInsuranceXmlName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIOrganization_InsuranceXmlName()
	 * @model
	 * @generated
	 */
	String getInsuranceXmlName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrganization#getInsuranceXmlName <em>Insurance Xml Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Insurance Xml Name</em>' attribute.
	 * @see #getInsuranceXmlName()
	 * @generated
	 */
	void setInsuranceXmlName(String value);

	/**
	 * Returns the value of the '<em><b>Insurance Law Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Insurance Law Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Insurance Law Code</em>' attribute.
	 * @see #setInsuranceLawCode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIOrganization_InsuranceLawCode()
	 * @model
	 * @generated
	 */
	String getInsuranceLawCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrganization#getInsuranceLawCode <em>Insurance Law Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Insurance Law Code</em>' attribute.
	 * @see #getInsuranceLawCode()
	 * @generated
	 */
	void setInsuranceLawCode(String value);
} // IOrganization
