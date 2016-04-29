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
package ch.elexis.core.model.util;

import ch.elexis.core.model.*;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.model.ModelPackage
 * @generated
 */
public class ModelSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelSwitch() {
		if (modelPackage == null) {
			modelPackage = ModelPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T1 doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ModelPackage.ICONTACT: {
				IContact iContact = (IContact)theEObject;
				T1 result = caseIContact(iContact);
				if (result == null) result = caseIdentifiable(iContact);
				if (result == null) result = caseDeleteable(iContact);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPERSISTENT_OBJECT: {
				IPersistentObject iPersistentObject = (IPersistentObject)theEObject;
				T1 result = caseIPersistentObject(iPersistentObject);
				if (result == null) result = caseIdentifiable(iPersistentObject);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IXID: {
				IXid iXid = (IXid)theEObject;
				T1 result = caseIXid(iXid);
				if (result == null) result = caseIPersistentObject(iXid);
				if (result == null) result = caseIdentifiable(iXid);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICODE_ELEMENT: {
				ICodeElement iCodeElement = (ICodeElement)theEObject;
				T1 result = caseICodeElement(iCodeElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ICHANGE_LISTENER: {
				IChangeListener iChangeListener = (IChangeListener)theEObject;
				T1 result = caseIChangeListener(iChangeListener);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ISTICKER: {
				ISticker iSticker = (ISticker)theEObject;
				T1 result = caseISticker(iSticker);
				if (result == null) result = caseComparable(iSticker);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPERSON: {
				IPerson iPerson = (IPerson)theEObject;
				T1 result = caseIPerson(iPerson);
				if (result == null) result = caseIContact(iPerson);
				if (result == null) result = caseIdentifiable(iPerson);
				if (result == null) result = caseDeleteable(iPerson);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPATIENT: {
				IPatient iPatient = (IPatient)theEObject;
				T1 result = caseIPatient(iPatient);
				if (result == null) result = caseIPerson(iPatient);
				if (result == null) result = caseIContact(iPatient);
				if (result == null) result = caseIdentifiable(iPatient);
				if (result == null) result = caseDeleteable(iPatient);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IUSER: {
				IUser iUser = (IUser)theEObject;
				T1 result = caseIUser(iUser);
				if (result == null) result = caseIContact(iUser);
				if (result == null) result = caseIdentifiable(iUser);
				if (result == null) result = caseDeleteable(iUser);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IDENTIFIABLE: {
				Identifiable identifiable = (Identifiable)theEObject;
				T1 result = caseIdentifiable(identifiable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.DELETEABLE: {
				Deleteable deleteable = (Deleteable)theEObject;
				T1 result = caseDeleteable(deleteable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_ITEM: {
				ILabItem iLabItem = (ILabItem)theEObject;
				T1 result = caseILabItem(iLabItem);
				if (result == null) result = caseIdentifiable(iLabItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_RESULT: {
				ILabResult iLabResult = (ILabResult)theEObject;
				T1 result = caseILabResult(iLabResult);
				if (result == null) result = caseIdentifiable(iLabResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.ILAB_ORDER: {
				ILabOrder iLabOrder = (ILabOrder)theEObject;
				T1 result = caseILabOrder(iLabOrder);
				if (result == null) result = caseIdentifiable(iLabOrder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ModelPackage.IPERIOD: {
				IPeriod iPeriod = (IPeriod)theEObject;
				T1 result = caseIPeriod(iPeriod);
				if (result == null) result = caseIdentifiable(iPeriod);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IContact</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IContact</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIContact(IContact object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPersistent Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPersistent Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPersistentObject(IPersistentObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IXid</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IXid</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIXid(IXid object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ICode Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ICode Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseICodeElement(ICodeElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IChange Listener</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IChange Listener</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIChangeListener(IChangeListener object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ISticker</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ISticker</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseISticker(ISticker object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPerson</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPerson</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPerson(IPerson object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPatient</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPatient</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPatient(IPatient object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IUser</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IUser</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIUser(IUser object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIdentifiable(Identifiable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Deleteable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Deleteable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDeleteable(Deleteable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabItem(ILabItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabResult(ILabResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ILab Order</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ILab Order</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseILabOrder(ILabOrder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>IPeriod</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>IPeriod</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseIPeriod(IPeriod object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Comparable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Comparable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T> T1 caseComparable(Comparable<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T1 defaultCase(EObject object) {
		return null;
	}

} //ModelSwitch
