/**
 * Copyright (c) 2025 MEDEVIT <info@medelexis.ch>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDELEXIS <info@medelexis.ch> - initial API and implementation
 */
package ch.elexis.core.model;

import java.time.LocalDate;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>IOutputLog</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getCreatorId <em>Creator Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getOutputter <em>Outputter</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOutputLog#getOutputterStatus <em>Outputter Status</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIOutputLog()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IOutputLog extends Identifiable, Deleteable, WithExtInfo {

	/**
	 * Returns the value of the '<em><b>Object Type</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Object Type</em>' attribute.
	 * @see #setObjectType(String)
	 * @generated
	 */
	String getObjectType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOutputLog#getObjectType
	 * <em>Object Type</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Object Type</em>' attribute.
	 * @see #getObjectType()
	 * @generated
	 */
	void setObjectType(String value);

	/**
	 * Returns the value of the '<em><b>ObjectId</b></em>' attribute.
	 * 
	 * @return the value of the '<em>ObjectId</em>' attribute.
	 * @see #setObjectId(String)
	 * @generated
	 */
	String getObjectId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOutputLog#getObjectId
	 * <em>ObjectId</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Object Id</em>' attribute.
	 * @see #getObjectId()
	 * @generated
	 */
	void setObjectId(String value);

	/**
	 * Returns the value of the '<em><b>CreatorId</b></em>' attribute.
	 * 
	 * @return the value of the '<em>CreatorId</em>' attribute.
	 * @see #setCreatorId(String)
	 * @generated
	 */
	String getCreatorId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOutputLog#getCreatorId
	 * <em>Creator Id</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>CreatorId</em>' attribute.
	 * @see #getCreatorId()
	 * @generated
	 */
	void setCreatorId(String value);

	/**
	 * Returns the value of the '<em><b>Outputter</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Outputter</em>' attribute.
	 * @see #setOutputter(String)
	 * @generated
	 */
	String getOutputter();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOutputLog#getOutputter
	 * <em>Outputter</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>Outputter</em>' attribute.
	 * @see #getOutputter()
	 * @generated
	 */
	void setOutputter(String value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * 
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(String)
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOutputLog#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>OutputterStatus</b></em>' attribute.
	 * 
	 * @return the value of the '<em>OutputterStatus</em>' attribute.
	 * @see #setOutputterStatus(String)
	 * @generated
	 */
	String getOutputterStatus();

	/**
	 * Sets the value of the
	 * '{@link ch.elexis.core.model.IOutputLog#getOutputterStatus <em>OutputterStatus</em>}' attribute.
	 * 
	 * @param value the new value of the '<em>OutputterStatus</em>' attribute.
	 * @see #getOutputterStatus()
	 * @generated
	 */
	void setOutputterStatus(String value);
}
