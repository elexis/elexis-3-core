/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Findings Template</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getType <em>Type</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getTitle <em>Title</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getInputData <em>Input Data</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getCodeElement <em>Code Element</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplate()
 * @model
 * @generated
 */
public interface FindingsTemplate extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.findings.templates.model.Type}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.Type
	 * @see #setType(Type)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplate_Type()
	 * @model
	 * @generated
	 */
	Type getType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.Type
	 * @see #getType()
	 * @generated
	 */
	void setType(Type value);

	/**
	 * Returns the value of the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Title</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' attribute.
	 * @see #setTitle(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplate_Title()
	 * @model
	 * @generated
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

	/**
	 * Returns the value of the '<em><b>Input Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input Data</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input Data</em>' containment reference.
	 * @see #setInputData(InputData)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplate_InputData()
	 * @model containment="true"
	 * @generated
	 */
	InputData getInputData();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getInputData <em>Input Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input Data</em>' containment reference.
	 * @see #getInputData()
	 * @generated
	 */
	void setInputData(InputData value);

	/**
	 * Returns the value of the '<em><b>Code Element</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Code Element</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code Element</em>' containment reference.
	 * @see #setCodeElement(CodeElement)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplate_CodeElement()
	 * @model containment="true"
	 * @generated
	 */
	CodeElement getCodeElement();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getCodeElement <em>Code Element</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code Element</em>' containment reference.
	 * @see #getCodeElement()
	 * @generated
	 */
	void setCodeElement(CodeElement value);

} // FindingsTemplate
