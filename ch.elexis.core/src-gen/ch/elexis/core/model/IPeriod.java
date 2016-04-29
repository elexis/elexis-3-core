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

import ch.rgw.tools.TimeTool;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IPeriod</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IPeriod#getStartTime <em>Start Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPeriod#getEndTime <em>End Time</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIPeriod()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPeriod extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Time</em>' attribute.
	 * @see #setStartTime(TimeTool)
	 * @see ch.elexis.core.model.ModelPackage#getIPeriod_StartTime()
	 * @model dataType="ch.elexis.core.types.TimeTool"
	 * @generated
	 */
	TimeTool getStartTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPeriod#getStartTime <em>Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Time</em>' attribute.
	 * @see #getStartTime()
	 * @generated
	 */
	void setStartTime(TimeTool value);

	/**
	 * Returns the value of the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Time</em>' attribute.
	 * @see #setEndTime(TimeTool)
	 * @see ch.elexis.core.model.ModelPackage#getIPeriod_EndTime()
	 * @model dataType="ch.elexis.core.types.TimeTool"
	 * @generated
	 */
	TimeTool getEndTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPeriod#getEndTime <em>End Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Time</em>' attribute.
	 * @see #getEndTime()
	 * @generated
	 */
	void setEndTime(TimeTool value);

} // IPeriod
