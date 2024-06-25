package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.XidServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class XidsFromExtInfo extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		StringBuilder sb = new StringBuilder();
		int withExtInfoEan = 0;
		int createdEanXid = 0;
		int alreadyEanXid = 0;
		int conflictEanXid = 0;

		IQuery<IOrganization> organizationQuery = CoreModelServiceHolder.get().getQuery(IOrganization.class);
		try (IQueryCursor<IOrganization> allOrganizations = organizationQuery.executeAsCursor()) {
			StringBuilder conflictSb = new StringBuilder();
			while (allOrganizations.hasNext()) {
				IOrganization organization = allOrganizations.next();

				Object extInfoEan = organization.getExtInfo("EAN");
				if (extInfoEan instanceof String && StringUtils.isNotBlank((String) extInfoEan)) {
					withExtInfoEan++;

					List<IOrganization> found = XidServiceHolder.get().findObjects(XidConstants.EAN,
							(String) extInfoEan, IOrganization.class);
					if (found.isEmpty()) {
						organization.addXid(XidConstants.EAN, (String) extInfoEan, false);
						createdEanXid++;
					} else {
						if (found.size() == 1) {
							if (found.get(0).equals(organization)) {
								alreadyEanXid++;
							} else {
								conflictEanXid++;
								conflictSb.append(
										"ExtInfo EAN [" + (String) extInfoEan + "] of org id [" + organization.getId()
												+ "] found in xid of org id [" + found.get(0).getId() + "]\n");
							}
						} else {
							conflictEanXid++;
							conflictSb.append("ExtInfo EAN [" + (String) extInfoEan + "] of org id ["
									+ organization.getId() + "] found in xid of multi org ids ["
									+ found.stream().map(o -> o.getId()).collect(Collectors.joining(",")) + "]\n");
						}
					}
				}
			}
			sb.append("Organization EAN:\n");
			sb.append("found EAN ExtInfo " + withExtInfoEan + "\n");
			sb.append("created EAN XID " + createdEanXid + "\n");
			sb.append("already EAN XID " + alreadyEanXid + "\n");
			sb.append("conflict EAN XID " + conflictEanXid + "\n");
			if(conflictEanXid > 0) {
				sb.append(conflictSb.toString());
			}
		}
		return sb.toString();
	}

	@Override
	public String getMaintenanceDescription() {
		return "XIDs aus ExtInfo Informationen erstellen (z.B. EAN)";
	}

}
