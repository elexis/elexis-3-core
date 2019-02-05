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
 * A representation of the model object '<em><b>ICode Element Block</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ICodeElementBlock#getElements <em>Elements</em>}</li>
 *   <li>{@link ch.elexis.core.model.ICodeElementBlock#getElementReferences <em>Element References</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getICodeElementBlock()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ICodeElementBlock extends Identifiable, Deleteable, ICodeElement {
	/**
	 * Returns the value of the '<em><b>Elements</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.ICodeElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Elements</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Elements</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getICodeElementBlock_Elements()
	 * @model
	 * @generated
	 */
	List<ICodeElement> getElements();

	/**
	 * Returns the value of the '<em><b>Element References</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.ICodeElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Element References</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Element References</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getICodeElementBlock_ElementReferences()
	 * @model
	 * @generated
	 */
	List<ICodeElement> getElementReferences();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model type="ch.elexis.core.types.List&lt;ch.elexis.core.model.ICodeElement&gt;" many="false" elementsType="ch.elexis.core.types.List&lt;ch.elexis.core.model.ICodeElement&gt;" elementsMany="false"
	 * @generated
	 */
	List<ICodeElement> getDiffToReferences(List<ICodeElement> elements);

} // ICodeElementBlock
