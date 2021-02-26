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

import ch.rgw.tools.Result;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IBillable
 * Optifier</b></em>'. <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getIBillableOptifier()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBillableOptifier<T extends IBillable> {

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Add amount of a {@link IBillable} to the encounter resulting in an {@link IBilled}. If adding
	 * was not successful the {@link Result} is not ok, and should contain the reason as message.
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @model type="ch.elexis.core.types.Result&lt;ch.elexis.core.model.IBilled&gt;"
	 * @generated NOT
	 */
	default Result<IBilled> add(T billable, IEncounter encounter, double amount){
		return add(billable, encounter, amount, true);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Add amount of a {@link IBillable} to the encounter resulting in an {@link IBilled}. If adding
	 * was not successful the {@link Result} is not ok, and should contain the reason as message.
	 * <br />
	 * <br />
	 * If the save parameter is false, the changes are not persisted but a correct {@link IBillable}
	 * is returned. But there is no guarantee that correct validation is performed.
	 * </p>
	 * <!-- end-user-doc -->
	 * @model type="ch.elexis.core.types.Result&lt;ch.elexis.core.model.IBilled&gt;"
	 * @generated
	 */
	Result<IBilled> add(T billable, IEncounter encounter, double amount, boolean save);

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Add an object to the context of the {@link IBillableOptifier} implementation. If a object for the
	 * provided key already exists, the value is replaced.
	 * </p>
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void putContext(String key, Object value);
	
	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Add an implementation specific context object. If a object for the provided key already
	 * exists, the value is replaced.
	 * </p>
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void clearContext();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model type="ch.elexis.core.types.Result&lt;ch.elexis.core.model.IBilled&gt;"
	 * @generated
	 */
	Result<IBilled> remove(IBilled billed, IEncounter encounter);
	
} // IBillableOptifier
