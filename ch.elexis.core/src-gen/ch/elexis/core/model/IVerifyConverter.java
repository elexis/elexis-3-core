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

import java.util.Optional;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IVerify Converter</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getIVerifyConverter()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVerifyConverter {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model dataType="ch.elexis.core.types.Optional<ch.elexis.core.model.IVerify>"
	 * @generated
	 */
	Optional<IVerify> convert(IBillable iBillable);

} // IVerifyConverter
