/**
 */
package ch.elexis.core.findings.templates.model.impl;

import ch.elexis.core.findings.templates.model.DataType;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.ModelPackage;

import java.util.Collection;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Data Group Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.impl.InputDataGroupComponentImpl#getFindingsTemplates <em>Findings Templates</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.impl.InputDataGroupComponentImpl#getDataType <em>Data Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InputDataGroupComponentImpl extends MinimalEObjectImpl.Container implements InputDataGroupComponent {
	/**
	 * The cached value of the '{@link #getFindingsTemplates() <em>Findings Templates</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFindingsTemplates()
	 * @generated
	 * @ordered
	 */
	protected EList<FindingsTemplate> findingsTemplates;

	/**
	 * The default value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataType()
	 * @generated
	 * @ordered
	 */
	protected static final DataType DATA_TYPE_EDEFAULT = DataType.GROUP_COMPONENT;

	/**
	 * The cached value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataType()
	 * @generated
	 * @ordered
	 */
	protected DataType dataType = DATA_TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InputDataGroupComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.INPUT_DATA_GROUP_COMPONENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<FindingsTemplate> getFindingsTemplates() {
		if (findingsTemplates == null) {
			findingsTemplates = new EObjectResolvingEList<FindingsTemplate>(FindingsTemplate.class, this, ModelPackage.INPUT_DATA_GROUP_COMPONENT__FINDINGS_TEMPLATES);
		}
		return findingsTemplates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__FINDINGS_TEMPLATES:
				return getFindingsTemplates();
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__DATA_TYPE:
				return getDataType();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__FINDINGS_TEMPLATES:
				getFindingsTemplates().clear();
				getFindingsTemplates().addAll((Collection<? extends FindingsTemplate>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__FINDINGS_TEMPLATES:
				getFindingsTemplates().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__FINDINGS_TEMPLATES:
				return findingsTemplates != null && !findingsTemplates.isEmpty();
			case ModelPackage.INPUT_DATA_GROUP_COMPONENT__DATA_TYPE:
				return dataType != DATA_TYPE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (dataType: ");
		result.append(dataType);
		result.append(')');
		return result.toString();
	}

} //InputDataGroupComponentImpl
