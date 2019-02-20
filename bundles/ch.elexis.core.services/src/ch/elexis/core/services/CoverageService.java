package ch.elexis.core.services;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.holder.BillingSystemServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.rgw.tools.StringTool;

@Component
public class CoverageService implements ICoverageService {
	
	@Override
	public boolean isValid(ICoverage coverage){
		if (coverage.getPatient() == null) {
			return false;
		}
		
		// Check whether all user-defined requirements for this billing system
		// are met
		String reqs = BillingSystemServiceHolder.get().getRequirements(coverage.getBillingSystem());
		if (reqs != null) {
			for (String req : reqs.split(";")) { //$NON-NLS-1$
				String localReq = ""; //$NON-NLS-1$
				String[] r = req.split(":"); //$NON-NLS-1$
				if ((r[1].equalsIgnoreCase("X")) && (r.length > 2)) { //$NON-NLS-1$
					// *** support for additional field types (checkboxes with
					// multiple items are
					// special)
					String[] items = r[2].split("\t"); //$NON-NLS-1$
					if (items.length > 1) {
						for (int rIx = 0; rIx < items.length; rIx++) {
							localReq = (String) coverage.getExtInfo(r[0] + "_" + items[rIx]); //$NON-NLS-1$
							if (StringTool.isNothing(localReq)) {
								return false;
							}
						}
					}
				} else {
					localReq = (String) coverage.getExtInfo(r[0]);
					if (StringTool.isNothing(localReq)) {
						return false;
					}
				}
				if (r[1].equals("K")) { //$NON-NLS-1$
					Optional<IContact> contact =
						CoreModelServiceHolder.get().load(localReq, IContact.class);
					if (!contact.isPresent()) {
						return false;
					}
				}
			}
		}
		// check whether the outputter could output a bill
		if (BillingSystemServiceHolder.get()
			.getDefaultPrintSystem(coverage.getBillingSystem()) == null) {
			return false;
		}
		//		IRnOutputter outputter = getOutputter();
		//		if (outputter == null) {
		//			return false;
		//		} else {
		//			if (!outputter.canBill(this)) {
		//				return false;
		//			}
		//		}
		return true;
	}
}
