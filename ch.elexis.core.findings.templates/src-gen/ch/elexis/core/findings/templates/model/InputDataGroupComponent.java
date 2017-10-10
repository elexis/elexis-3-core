/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Data Group Component</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataGroupComponent#getFindingsTemplates <em>Findings Templates</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataGroupComponent#getDataType <em>Data Type</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataGroupComponent#getTextSeparator <em>Text Separator</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroupComponent()
 * @model
 * @generated
 */
public interface InputDataGroupComponent extends InputData {
	/**
	 * Returns the value of the '<em><b>Findings Templates</b></em>' containment reference list.
	 * The list contents are of type {@link ch.elexis.core.findings.templates.model.FindingsTemplate}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Findings Templates</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Findings Templates</em>' containment reference list.
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroupComponent_FindingsTemplates()
	 * @model containment="true"
	 * @generated
	 */
	EList<FindingsTemplate> getFindingsTemplates();

	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * The default value is <code>"GROUP_COMPONENT"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.findings.templates.model.DataType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroupComponent_DataType()
	 * @model default="GROUP_COMPONENT" changeable="false"
	 * @generated
	 */
	DataType getDataType();

	/**
	 * Returns the value of the '<em><b>Text Separator</b></em>' attribute.
	 * The default value is <code>" "</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Text Separator</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Text Separator</em>' attribute.
	 * @see #setTextSeparator(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroupComponent_TextSeparator()
	 * @model default=" "
	 * @generated
	 */
	String getTextSeparator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.InputDataGroupComponent#getTextSeparator <em>Text Separator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Text Separator</em>' attribute.
	 * @see #getTextSeparator()
	 * @generated
	 */
	void setTextSeparator(String value);

} // InputDataGroupComponent
