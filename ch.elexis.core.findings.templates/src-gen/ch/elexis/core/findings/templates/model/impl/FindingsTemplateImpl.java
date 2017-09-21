/**
 */
package ch.elexis.core.findings.templates.model.impl;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.InputData;
import ch.elexis.core.findings.templates.model.ModelPackage;
import ch.elexis.core.findings.templates.model.Type;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Findings Template</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl#getType <em>Type</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl#getTitle <em>Title</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl#getInputData <em>Input Data</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FindingsTemplateImpl extends MinimalEObjectImpl.Container implements FindingsTemplate {
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final Type TYPE_EDEFAULT = Type.OBSERVATION;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected Type type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected static final String TITLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected String title = TITLE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getInputData() <em>Input Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputData()
	 * @generated
	 * @ordered
	 */
	protected InputData inputData;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FindingsTemplateImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.FINDINGS_TEMPLATE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Type getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(Type newType) {
		Type oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FINDINGS_TEMPLATE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTitle(String newTitle) {
		String oldTitle = title;
		title = newTitle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FINDINGS_TEMPLATE__TITLE, oldTitle, title));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InputData getInputData() {
		return inputData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInputData(InputData newInputData, NotificationChain msgs) {
		InputData oldInputData = inputData;
		inputData = newInputData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA, oldInputData, newInputData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputData(InputData newInputData) {
		if (newInputData != inputData) {
			NotificationChain msgs = null;
			if (inputData != null)
				msgs = ((InternalEObject)inputData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA, null, msgs);
			if (newInputData != null)
				msgs = ((InternalEObject)newInputData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA, null, msgs);
			msgs = basicSetInputData(newInputData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA, newInputData, newInputData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA:
				return basicSetInputData(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.FINDINGS_TEMPLATE__TYPE:
				return getType();
			case ModelPackage.FINDINGS_TEMPLATE__TITLE:
				return getTitle();
			case ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA:
				return getInputData();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.FINDINGS_TEMPLATE__TYPE:
				setType((Type)newValue);
				return;
			case ModelPackage.FINDINGS_TEMPLATE__TITLE:
				setTitle((String)newValue);
				return;
			case ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA:
				setInputData((InputData)newValue);
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
			case ModelPackage.FINDINGS_TEMPLATE__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case ModelPackage.FINDINGS_TEMPLATE__TITLE:
				setTitle(TITLE_EDEFAULT);
				return;
			case ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA:
				setInputData((InputData)null);
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
			case ModelPackage.FINDINGS_TEMPLATE__TYPE:
				return type != TYPE_EDEFAULT;
			case ModelPackage.FINDINGS_TEMPLATE__TITLE:
				return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
			case ModelPackage.FINDINGS_TEMPLATE__INPUT_DATA:
				return inputData != null;
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
		result.append(" (type: ");
		result.append(type);
		result.append(", title: ");
		result.append(title);
		result.append(')');
		return result.toString();
	}

} //FindingsTemplateImpl
