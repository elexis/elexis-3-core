/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Data Group</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataGroup#getFindingsTemplates <em>Findings Templates</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataGroup#getDataType <em>Data Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroup()
 * @model
 * @generated
 */
public interface InputDataGroup extends InputData {
	/**
	 * Returns the value of the '<em><b>Findings Templates</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.findings.templates.model.FindingsTemplate}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Findings Templates</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Findings Templates</em>' reference list.
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroup_FindingsTemplates()
	 * @model
	 * @generated
	 */
	EList<FindingsTemplate> getFindingsTemplates();

	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * The default value is <code>"GROUP"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.findings.templates.model.DataType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataGroup_DataType()
	 * @model default="GROUP" changeable="false"
	 * @generated
	 */
	DataType getDataType();

} // InputDataGroup
