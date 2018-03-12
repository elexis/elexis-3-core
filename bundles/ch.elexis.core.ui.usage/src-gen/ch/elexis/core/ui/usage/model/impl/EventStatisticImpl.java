/**
 */
package ch.elexis.core.ui.usage.model.impl;

import ch.elexis.core.ui.usage.model.EventStatistic;
import ch.elexis.core.ui.usage.model.ModelPackage;

import java.util.Date;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Event Statistic</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getAction <em>Action</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getValue <em>Value</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getTime <em>Time</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getActionType <em>Action Type</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getMinDuration <em>Min Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getMaxDuration <em>Max Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getAvgDuration <em>Avg Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl#getLastStart <em>Last Start</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EventStatisticImpl extends MinimalEObjectImpl.Container implements EventStatistic {
	/**
	 * The default value of the '{@link #getAction() <em>Action</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAction()
	 * @generated
	 * @ordered
	 */
	protected static final String ACTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAction() <em>Action</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAction()
	 * @generated
	 * @ordered
	 */
	protected String action = ACTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final int VALUE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected int value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected static final Date TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected Date time = TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getActionType() <em>Action Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getActionType()
	 * @generated
	 * @ordered
	 */
	protected static final String ACTION_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getActionType() <em>Action Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getActionType()
	 * @generated
	 * @ordered
	 */
	protected String actionType = ACTION_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getMinDuration() <em>Min Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinDuration()
	 * @generated
	 * @ordered
	 */
	protected static final int MIN_DURATION_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMinDuration() <em>Min Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinDuration()
	 * @generated
	 * @ordered
	 */
	protected int minDuration = MIN_DURATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxDuration() <em>Max Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxDuration()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_DURATION_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxDuration() <em>Max Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxDuration()
	 * @generated
	 * @ordered
	 */
	protected int maxDuration = MAX_DURATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getAvgDuration() <em>Avg Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAvgDuration()
	 * @generated
	 * @ordered
	 */
	protected static final long AVG_DURATION_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getAvgDuration() <em>Avg Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAvgDuration()
	 * @generated
	 * @ordered
	 */
	protected long avgDuration = AVG_DURATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastStart() <em>Last Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastStart()
	 * @generated
	 * @ordered
	 */
	protected static final long LAST_START_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getLastStart() <em>Last Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastStart()
	 * @generated
	 * @ordered
	 */
	protected long lastStart = LAST_START_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EventStatisticImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.EVENT_STATISTIC;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getAction() {
		return action;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAction(String newAction) {
		String oldAction = action;
		action = newAction;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__ACTION, oldAction, action));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(int newValue) {
		int oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTime(Date newTime) {
		Date oldTime = time;
		time = newTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__TIME, oldTime, time));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActionType(String newActionType) {
		String oldActionType = actionType;
		actionType = newActionType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__ACTION_TYPE, oldActionType, actionType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getMinDuration() {
		return minDuration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMinDuration(int newMinDuration) {
		int oldMinDuration = minDuration;
		minDuration = newMinDuration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__MIN_DURATION, oldMinDuration, minDuration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getMaxDuration() {
		return maxDuration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMaxDuration(int newMaxDuration) {
		int oldMaxDuration = maxDuration;
		maxDuration = newMaxDuration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__MAX_DURATION, oldMaxDuration, maxDuration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public long getAvgDuration() {
		return avgDuration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAvgDuration(long newAvgDuration) {
		long oldAvgDuration = avgDuration;
		avgDuration = newAvgDuration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__AVG_DURATION, oldAvgDuration, avgDuration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public long getLastStart() {
		return lastStart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLastStart(long newLastStart) {
		long oldLastStart = lastStart;
		lastStart = newLastStart;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.EVENT_STATISTIC__LAST_START, oldLastStart, lastStart));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.EVENT_STATISTIC__ACTION:
				return getAction();
			case ModelPackage.EVENT_STATISTIC__VALUE:
				return getValue();
			case ModelPackage.EVENT_STATISTIC__TIME:
				return getTime();
			case ModelPackage.EVENT_STATISTIC__ACTION_TYPE:
				return getActionType();
			case ModelPackage.EVENT_STATISTIC__MIN_DURATION:
				return getMinDuration();
			case ModelPackage.EVENT_STATISTIC__MAX_DURATION:
				return getMaxDuration();
			case ModelPackage.EVENT_STATISTIC__AVG_DURATION:
				return getAvgDuration();
			case ModelPackage.EVENT_STATISTIC__LAST_START:
				return getLastStart();
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
			case ModelPackage.EVENT_STATISTIC__ACTION:
				setAction((String)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__VALUE:
				setValue((Integer)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__TIME:
				setTime((Date)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__ACTION_TYPE:
				setActionType((String)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__MIN_DURATION:
				setMinDuration((Integer)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__MAX_DURATION:
				setMaxDuration((Integer)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__AVG_DURATION:
				setAvgDuration((Long)newValue);
				return;
			case ModelPackage.EVENT_STATISTIC__LAST_START:
				setLastStart((Long)newValue);
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
			case ModelPackage.EVENT_STATISTIC__ACTION:
				setAction(ACTION_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__TIME:
				setTime(TIME_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__ACTION_TYPE:
				setActionType(ACTION_TYPE_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__MIN_DURATION:
				setMinDuration(MIN_DURATION_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__MAX_DURATION:
				setMaxDuration(MAX_DURATION_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__AVG_DURATION:
				setAvgDuration(AVG_DURATION_EDEFAULT);
				return;
			case ModelPackage.EVENT_STATISTIC__LAST_START:
				setLastStart(LAST_START_EDEFAULT);
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
			case ModelPackage.EVENT_STATISTIC__ACTION:
				return ACTION_EDEFAULT == null ? action != null : !ACTION_EDEFAULT.equals(action);
			case ModelPackage.EVENT_STATISTIC__VALUE:
				return value != VALUE_EDEFAULT;
			case ModelPackage.EVENT_STATISTIC__TIME:
				return TIME_EDEFAULT == null ? time != null : !TIME_EDEFAULT.equals(time);
			case ModelPackage.EVENT_STATISTIC__ACTION_TYPE:
				return ACTION_TYPE_EDEFAULT == null ? actionType != null : !ACTION_TYPE_EDEFAULT.equals(actionType);
			case ModelPackage.EVENT_STATISTIC__MIN_DURATION:
				return minDuration != MIN_DURATION_EDEFAULT;
			case ModelPackage.EVENT_STATISTIC__MAX_DURATION:
				return maxDuration != MAX_DURATION_EDEFAULT;
			case ModelPackage.EVENT_STATISTIC__AVG_DURATION:
				return avgDuration != AVG_DURATION_EDEFAULT;
			case ModelPackage.EVENT_STATISTIC__LAST_START:
				return lastStart != LAST_START_EDEFAULT;
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
		result.append(" (action: ");
		result.append(action);
		result.append(", value: ");
		result.append(value);
		result.append(", time: ");
		result.append(time);
		result.append(", actionType: ");
		result.append(actionType);
		result.append(", minDuration: ");
		result.append(minDuration);
		result.append(", maxDuration: ");
		result.append(maxDuration);
		result.append(", avgDuration: ");
		result.append(avgDuration);
		result.append(", lastStart: ");
		result.append(lastStart);
		result.append(')');
		return result.toString();
	}

} //EventStatisticImpl
