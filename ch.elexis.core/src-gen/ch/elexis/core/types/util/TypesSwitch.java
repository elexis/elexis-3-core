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
package ch.elexis.core.types.util;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

import ch.elexis.core.types.TypesPackage;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance hierarchy. It supports the
 * call {@link #doSwitch(EObject) doSwitch(object)} to invoke the <code>caseXXX</code> method for
 * each class of the model, starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the result of the switch.
 * <!-- end-user-doc -->
 * 
 * @see ch.elexis.core.types.TypesPackage
 * @generated
 */
public class TypesSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected static TypesPackage modelPackage;
	
	/**
	 * Creates an instance of the switch. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypesSwitch(){
		if (modelPackage == null) {
			modelPackage = TypesPackage.eINSTANCE;
		}
	}
	
	/**
	 * Checks whether this is a switch for the given package. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage){
		return ePackage == modelPackage;
	}
	
	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result;
	 * it yields that result. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T1 doSwitch(int classifierID, EObject theEObject){
		switch (classifierID) {
		case TypesPackage.COMPARABLE: {
			Comparable<?> comparable = (Comparable<?>) theEObject;
			T1 result = caseComparable(comparable);
			if (result == null)
				result = defaultCase(theEObject);
			return result;
		}
		case TypesPackage.LIST: {
			List<?> list = (List<?>) theEObject;
			T1 result = caseList(list);
			if (result == null)
				result = defaultCase(theEObject);
			return result;
		}
		case TypesPackage.MAP: {
			Map<?, ?> map = (Map<?, ?>) theEObject;
			T1 result = caseMap(map);
			if (result == null)
				result = defaultCase(theEObject);
			return result;
		}
		default:
			return defaultCase(theEObject);
		}
	}
	
	/**
	 * Returns the result of interpreting the object as an instance of '<em>Comparable</em>'. <!--
	 * begin-user-doc --> This implementation returns null; returning a non-null result will
	 * terminate the switch. <!-- end-user-doc -->
	 * 
	 * @param object
	 *            the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Comparable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T> T1 caseComparable(Comparable<T> object){
		return null;
	}
	
	/**
	 * Returns the result of interpreting the object as an instance of '<em>List</em>'. <!--
	 * begin-user-doc --> This implementation returns null; returning a non-null result will
	 * terminate the switch. <!-- end-user-doc -->
	 * 
	 * @param object
	 *            the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>List</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <E> T1 caseList(List<E> object){
		return null;
	}
	
	/**
	 * Returns the result of interpreting the object as an instance of '<em>Map</em>'. <!--
	 * begin-user-doc --> This implementation returns null; returning a non-null result will
	 * terminate the switch. <!-- end-user-doc -->
	 * 
	 * @param object
	 *            the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Map</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <K, V> T1 caseMap(Map<K, V> object){
		return null;
	}
	
	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'. <!--
	 * begin-user-doc --> This implementation returns null; returning a non-null result will
	 * terminate the switch, but this is the last case anyway. <!-- end-user-doc -->
	 * 
	 * @param object
	 *            the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T1 defaultCase(EObject object){
		return null;
	}
	
} // TypesSwitch
