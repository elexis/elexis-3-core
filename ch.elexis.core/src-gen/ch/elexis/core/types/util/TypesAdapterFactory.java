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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

import ch.elexis.core.types.TypesPackage;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides an adapter
 * <code>createXXX</code> method for each class of the model. <!-- end-user-doc -->
 * 
 * @see ch.elexis.core.types.TypesPackage
 * @generated
 */
public class TypesAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected static TypesPackage modelPackage;
	
	/**
	 * Creates an instance of the adapter factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TypesAdapterFactory(){
		if (modelPackage == null) {
			modelPackage = TypesPackage.eINSTANCE;
		}
	}
	
	/**
	 * Returns whether this factory is applicable for the type of the object. <!-- begin-user-doc
	 * --> This implementation returns <code>true</code> if the object is either the model's package
	 * or is an instance object of the model. <!-- end-user-doc -->
	 * 
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object){
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject) object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}
	
	/**
	 * The switch that delegates to the <code>createXXX</code> methods. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected TypesSwitch<Adapter> modelSwitch = new TypesSwitch<Adapter>() {
		@Override
		public <T> Adapter caseComparable(Comparable<T> object){
			return createComparableAdapter();
		}
		
		@Override
		public <E> Adapter caseList(List<E> object){
			return createListAdapter();
		}
		
		@Override
		public <K, V> Adapter caseMap(Map<K, V> object){
			return createMapAdapter();
		}
		
		@Override
		public Adapter defaultCase(EObject object){
			return createEObjectAdapter();
		}
	};
	
	/**
	 * Creates an adapter for the <code>target</code>. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param target
	 *            the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target){
		return modelSwitch.doSwitch((EObject) target);
	}
	
	/**
	 * Creates a new adapter for an object of class '{@link java.lang.Comparable
	 * <em>Comparable</em>}'. <!-- begin-user-doc --> This default implementation returns null so
	 * that we can easily ignore cases; it's useful to ignore a case when inheritance will catch all
	 * the cases anyway. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see java.lang.Comparable
	 * @generated
	 */
	public Adapter createComparableAdapter(){
		return null;
	}
	
	/**
	 * Creates a new adapter for an object of class '{@link java.util.List <em>List</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore
	 * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
	 * end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see java.util.List
	 * @generated
	 */
	public Adapter createListAdapter(){
		return null;
	}
	
	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map <em>Map</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore
	 * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
	 * end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see java.util.Map
	 * @generated
	 */
	public Adapter createMapAdapter(){
		return null;
	}
	
	/**
	 * Creates a new adapter for the default case. <!-- begin-user-doc --> This default
	 * implementation returns null. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter(){
		return null;
	}
	
} // TypesAdapterFactory
