
package ch.elexis.core.ui.e4.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import jakarta.inject.Named;

public class ShowInvoiceListHandler {

	@Execute
	public void execute(@Optional @Named("filterOnCurrentPatient") String filterPatient, EPartService partService,
			IContextService contextService) {

		boolean _filterPatient = (filterPatient != null) ? Boolean.valueOf(filterPatient) : false;

		MPart invoiceListPart = partService.showPart("ch.elexis.core.ui.views.rechnung.InvoiceListView", //$NON-NLS-1$
				PartState.VISIBLE);
		if (_filterPatient) {
			java.util.Optional<IPatient> activePatient = contextService.getActivePatient();

			IRefreshablePart refreshablePart = null;
			// is org.eclipse.ui.internal.e4.compatibility.CompatibilityView
			// we need to fetch the underlying IViewPart (e3) and directly cast it to
			// IRefreshablePart
			Object compatibilityViewPart = invoiceListPart.getObject();
			try {
				Method method = compatibilityViewPart.getClass().getMethod("getPart", (Class<?>[]) null); //$NON-NLS-1$
				Object invoiceListViewPart = method.invoke(compatibilityViewPart, (Object[]) null);
				refreshablePart = (IRefreshablePart) invoiceListViewPart;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LoggerFactory.getLogger(getClass()).error("Error getting e3 view", e); //$NON-NLS-1$
			}

			if (activePatient.isPresent() && refreshablePart != null) {
				refreshablePart.refresh(Collections.singletonMap(IPatient.class, activePatient.get()));
			}
		}
		partService.showPart(invoiceListPart, PartState.ACTIVATE);

	}

}