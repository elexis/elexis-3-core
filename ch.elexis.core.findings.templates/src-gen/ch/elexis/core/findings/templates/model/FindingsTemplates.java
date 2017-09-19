/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Findings Templates</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getFindingsTemplates <em>Findings Templates</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getId <em>Id</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getTitle <em>Title</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplates()
 * @model
 * @generated
 */
public interface FindingsTemplates extends EObject {
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
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplates_FindingsTemplates()
	 * @model containment="true"
	 * @generated
	 */
	EList<FindingsTemplate> getFindingsTemplates();

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplates_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Title</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Title</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' attribute.
	 * @see #setTitle(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getFindingsTemplates_Title()
	 * @model default=""
	 * @generated
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

} // FindingsTemplates
