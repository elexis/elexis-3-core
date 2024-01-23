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

import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IStock</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IStock#getCode <em>Code</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getDriverUuid <em>Driver Uuid</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getDriverConfig <em>Driver Config</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getPriority <em>Priority</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getOwner <em>Owner</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getLocation <em>Location</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getResponsible <em>Responsible</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStock#getStockEntries <em>Stock Entries</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIStock()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IStock extends Identifiable, Deleteable, WithAssignableId {
	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A unique code to define a specific stock
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see #setCode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIStock_Code()
	 * @model
	 * @generated
	 */
	String getCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStock#getCode <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code</em>' attribute.
	 * @see #getCode()
	 * @generated
	 */
	void setCode(String value);

	/**
	 * Returns the value of the '<em><b>Driver Uuid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Driver Uuid</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Driver Uuid</em>' attribute.
	 * @see #setDriverUuid(String)
	 * @see ch.elexis.core.model.ModelPackage#getIStock_DriverUuid()
	 * @model
	 * @generated
	 */
	String getDriverUuid();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStock#getDriverUuid <em>Driver Uuid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Driver Uuid</em>' attribute.
	 * @see #getDriverUuid()
	 * @generated
	 */
	void setDriverUuid(String value);

	/**
	 * Returns the value of the '<em><b>Driver Config</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Driver Config</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Driver Config</em>' attribute.
	 * @see #setDriverConfig(String)
	 * @see ch.elexis.core.model.ModelPackage#getIStock_DriverConfig()
	 * @model
	 * @generated
	 */
	String getDriverConfig();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStock#getDriverConfig <em>Driver Config</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Driver Config</em>' attribute.
	 * @see #getDriverConfig()
	 * @generated
	 */
	void setDriverConfig(String value);

	/**
	 * Returns the value of the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Priority</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Priority</em>' attribute.
	 * @see #setPriority(int)
	 * @see ch.elexis.core.model.ModelPackage#getIStock_Priority()
	 * @model
	 * @generated
	 */
	int getPriority();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStock#getPriority <em>Priority</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Priority</em>' attribute.
	 * @see #getPriority()
	 * @generated
	 */
	void setPriority(int value);

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owner</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The person the contents of this stock (ultimately) belong to.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Owner</em>' reference.
	 * @see #setOwner(IPerson)
	 * @see ch.elexis.core.model.ModelPackage#getIStock_Owner()
	 * @model
	 * @generated
	 */
	IPerson getOwner();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStock#getOwner <em>Owner</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' reference.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(IPerson value);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated not
	 */
	default boolean isCommissioningSystem(){
		String driverUuid = getDriverUuid();
		return (driverUuid != null && driverUuid.length() > 0);
	}

	/**
	 * 
	 * @param value
	 */
	void setDescription(String value);

	/**
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * 
	 * @return
	 */
	String getLocation();

	/**
	 * 
	 * @param value
	 */
	void setLocation(String value);

	/**
	 * 
	 * @return
	 */
	IContact getResponsible();

	/**
	 * 
	 * @param value
	 */
	void setResponsible(IContact value);

	/**
	 * Returns the value of the '<em><b>Stock Entries</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IStockEntry}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stock Entries</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIStock_StockEntries()
	 * @model changeable="false"
	 * @generated
	 */
	List<IStockEntry> getStockEntries();

} // IStock
