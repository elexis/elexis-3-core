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

import java.util.List;
import org.eclipse.emf.ecore.EObject;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ICode Element</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getICodeElement()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ICodeElement {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getCodeSystemName();
	
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	 String getCodeSystemCode();
	
	 /**
	 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getId();
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getCode();
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getText();


	/**
	 * <!-- begin-user-doc -->
	 * 
	 * Kontext-Aktionen für dieses Code-Element
	 * 
	 * @param kontext
	 * @return a list castable to IAction using <code>(Iterable<IAction>)(Iterable<?>)</code>
	 * @deprecated
	 * @since 3.0.0 method is bound for removal, please refactor
	 *
	 * <!-- end-user-doc -->
	 * @model
	 * @generated not
	 */
	List<Object> getActions(Object context);


} // ICodeElement
