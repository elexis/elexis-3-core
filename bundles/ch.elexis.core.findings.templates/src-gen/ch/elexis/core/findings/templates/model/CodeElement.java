/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Code Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.CodeElement#getCode <em>Code</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.CodeElement#getSystem <em>System</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.CodeElement#getDisplay <em>Display</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getCodeElement()
 * @model
 * @generated
 */
public interface CodeElement extends EObject {
	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see #setCode(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getCodeElement_Code()
	 * @model
	 * @generated
	 */
	String getCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.CodeElement#getCode <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code</em>' attribute.
	 * @see #getCode()
	 * @generated
	 */
	void setCode(String value);

	/**
	 * Returns the value of the '<em><b>System</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>System</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' attribute.
	 * @see #setSystem(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getCodeElement_System()
	 * @model
	 * @generated
	 */
	String getSystem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.CodeElement#getSystem <em>System</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' attribute.
	 * @see #getSystem()
	 * @generated
	 */
	void setSystem(String value);

	/**
	 * Returns the value of the '<em><b>Display</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Display</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Display</em>' attribute.
	 * @see #setDisplay(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getCodeElement_Display()
	 * @model
	 * @generated
	 */
	String getDisplay();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.CodeElement#getDisplay <em>Display</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Display</em>' attribute.
	 * @see #getDisplay()
	 * @generated
	 */
	void setDisplay(String value);

} // CodeElement
