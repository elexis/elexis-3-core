/**
 */
package ch.elexis.core.ui.usage.model.impl;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import ch.elexis.core.ui.usage.model.IStatistic;
import ch.elexis.core.ui.usage.model.ModelPackage;
import ch.elexis.core.ui.usage.model.Statistics;
import java.util.Collection;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Statistics</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.ui.usage.model.impl.StatisticsImpl#getStatistics <em>Statistics</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StatisticsImpl extends MinimalEObjectImpl.Container implements Statistics {
	/**
	 * The cached value of the '{@link #getStatistics() <em>Statistics</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatistics()
	 * @generated
	 * @ordered
	 */
	protected EList<IStatistic> statistics;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StatisticsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.STATISTICS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<IStatistic> getStatistics() {
		if (statistics == null) {
			statistics = new EObjectResolvingEList<IStatistic>(IStatistic.class, this, ModelPackage.STATISTICS__STATISTICS);
		}
		return statistics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.STATISTICS__STATISTICS:
				return getStatistics();
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
			case ModelPackage.STATISTICS__STATISTICS:
				getStatistics().clear();
				getStatistics().addAll((Collection<? extends IStatistic>)newValue);
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
			case ModelPackage.STATISTICS__STATISTICS:
				getStatistics().clear();
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
			case ModelPackage.STATISTICS__STATISTICS:
				return statistics != null && !statistics.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //StatisticsImpl
